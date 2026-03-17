package com.openrune.core.net.login

import com.openrune.core.net.codec.IsaacCipher
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.slf4j.LoggerFactory

/**
 * Decodes the RS2 317 login protocol.
 *
 * The login flow has two stages:
 *   Stage 0: Client sends connection type (14) + name hash
 *            Server responds with 8 ignored bytes + status 0 + server session key
 *   Stage 1: Client sends login block (type 16/18) with credentials
 *            Server responds with return code + rights + flag
 *
 * After successful login, this decoder is replaced with the game packet decoder.
 */
class LoginDecoder : ByteToMessageDecoder() {

    private val log = LoggerFactory.getLogger(LoginDecoder::class.java)

    private enum class Stage { HANDSHAKE, LOGIN_HEADER, LOGIN_PAYLOAD }

    private var stage = Stage.HANDSHAKE
    private var serverSessionKey = 0L
    private var loginSize = 0

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        when (stage) {
            Stage.HANDSHAKE -> decodeHandshake(ctx, buf, out)
            Stage.LOGIN_HEADER -> decodeLoginHeader(ctx, buf, out)
            Stage.LOGIN_PAYLOAD -> decodeLoginPayload(ctx, buf, out)
        }
    }

    private fun decodeHandshake(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (buf.readableBytes() < 2) return

        val connectionType = buf.readUnsignedByte().toInt()
        val nameHash = buf.readUnsignedByte().toInt()

        if (connectionType != 14) {
            log.debug("Unexpected connection type: {}", connectionType)
            ctx.close()
            return
        }

        // Generate server session key
        serverSessionKey = (Math.random() * 99999999.0).toLong() shl 32 or
                           (Math.random() * 99999999.0).toLong()

        // Send handshake response
        val response = ctx.alloc().buffer(17)
        response.writeLong(0)         // 8 ignored bytes
        response.writeByte(0)         // status: OK, send login
        response.writeLong(serverSessionKey)
        ctx.writeAndFlush(response)

        stage = Stage.LOGIN_HEADER
    }

    private fun decodeLoginHeader(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (buf.readableBytes() < 2) return

        val loginType = buf.readUnsignedByte().toInt()  // 16 = new, 18 = reconnect
        loginSize = buf.readUnsignedByte().toInt()

        if (loginType != 16 && loginType != 18) {
            log.debug("Unexpected login type: {}", loginType)
            ctx.close()
            return
        }

        if (loginSize <= 0) {
            log.debug("Invalid login size: {}", loginSize)
            ctx.close()
            return
        }

        stage = Stage.LOGIN_PAYLOAD
    }

    private fun decodeLoginPayload(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (buf.readableBytes() < loginSize) return

        // Read login block
        val magic = buf.readUnsignedByte().toInt()
        val clientVersion = buf.readUnsignedShort()

        if (magic != 255) {
            log.debug("Bad magic: {}", magic)
            ctx.close()
            return
        }

        val lowMemory = buf.readUnsignedByte().toInt()

        // Skip CRC keys (9 ints)
        for (i in 0 until 9) {
            buf.readInt()
        }

        // Encrypted block size check
        val encryptedSize = buf.readUnsignedByte().toInt()

        // RSA block start marker
        val rsaCheck = buf.readUnsignedByte().toInt()
        if (rsaCheck != 10) {
            log.debug("Bad RSA check: {}", rsaCheck)
            ctx.close()
            return
        }

        // Session keys
        val clientSessionKey = buf.readLong()
        val reportedServerKey = buf.readLong()

        // UID
        val uid = buf.readInt()

        // Credentials
        val username = readString(buf).trim().lowercase()
        val password = readString(buf).trim()

        // Build ISAAC cipher seeds
        val sessionKeys = intArrayOf(
            (clientSessionKey shr 32).toInt(),
            clientSessionKey.toInt(),
            (reportedServerKey shr 32).toInt(),
            reportedServerKey.toInt()
        )

        val inCipher = IsaacCipher(sessionKeys)

        val outSeeds = sessionKeys.copyOf()
        for (i in outSeeds.indices) outSeeds[i] += 50
        val outCipher = IsaacCipher(outSeeds)

        // Emit the login request for the server to process
        out.add(LoginRequest(
            username = username,
            password = password,
            uid = uid,
            clientVersion = clientVersion,
            lowMemory = lowMemory == 1,
            inCipher = inCipher,
            outCipher = outCipher,
            channel = ctx.channel()
        ))
    }

    private fun readString(buf: ByteBuf): String {
        val sb = StringBuilder()
        while (buf.isReadable) {
            val b = buf.readByte().toInt() and 0xFF
            if (b == 10) break  // newline terminator
            sb.append(b.toChar())
        }
        return sb.toString()
    }
}

/**
 * Represents a decoded login request ready for the server to process.
 */
data class LoginRequest(
    val username: String,
    val password: String,
    val uid: Int,
    val clientVersion: Int,
    val lowMemory: Boolean,
    val inCipher: IsaacCipher,
    val outCipher: IsaacCipher,
    val channel: io.netty.channel.Channel
)
