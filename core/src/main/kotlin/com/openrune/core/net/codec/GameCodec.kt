package com.openrune.core.net.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.slf4j.LoggerFactory

/**
 * Decodes incoming game packets from the client.
 * Opcodes are decrypted using the ISAAC cipher established during login.
 */
class GamePacketDecoder(private val cipher: IsaacCipher) : ByteToMessageDecoder() {

    private val log = LoggerFactory.getLogger(GamePacketDecoder::class.java)

    private var opcode = -1
    private var size = -1

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        while (buf.isReadable) {
            // Step 1: Read opcode
            if (opcode == -1) {
                if (!buf.isReadable) return
                opcode = (buf.readUnsignedByte().toInt() - cipher.nextValue()) and 0xFF

                if (opcode < 0 || opcode >= PacketSizes.INCOMING.size) {
                    log.warn("Invalid opcode: {}", opcode)
                    opcode = -1
                    return
                }

                size = PacketSizes.INCOMING[opcode]
            }

            // Step 2: Read variable size if needed
            if (size == -1) {
                if (!buf.isReadable) return
                size = buf.readUnsignedByte().toInt()
            } else if (size == -2) {
                if (buf.readableBytes() < 2) return
                size = buf.readUnsignedShort()
            }

            // Step 3: Read payload
            if (buf.readableBytes() < size) return

            val payload = ByteArray(size)
            buf.readBytes(payload)

            out.add(IncomingPacket(opcode, size, payload))

            // Reset for next packet
            opcode = -1
            size = -1
        }
    }
}

/**
 * Encodes outgoing packets with ISAAC encryption on the opcode.
 */
class GamePacketEncoder(private val cipher: IsaacCipher) {

    /**
     * Encode a [PacketBuilder] into a ByteBuf ready to write.
     */
    fun encode(ctx: ChannelHandlerContext, packet: PacketBuilder): ByteBuf {
        val data = packet.build()
        val opcode = packet.opcode()

        if (packet.bare) {
            val buf = ctx.alloc().buffer(data.size)
            buf.writeBytes(data)
            return buf
        }

        // Encrypt the opcode
        val encryptedOpcode = (opcode + cipher.nextValue()) and 0xFF

        val buf = ctx.alloc().buffer(1 + data.size)
        buf.writeByte(encryptedOpcode)
        buf.writeBytes(data)
        return buf
    }
}
