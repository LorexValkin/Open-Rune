package com.openrune.core.net

import com.openrune.core.engine.GameEngine
import com.openrune.core.io.PlayerSerializer
import com.openrune.core.net.codec.GamePacketDecoder
import com.openrune.core.net.codec.IncomingPacket
import com.openrune.core.net.login.LoginDecoder
import com.openrune.core.net.login.LoginRequest
import com.openrune.core.world.Player
import com.openrune.core.world.PlayerManager
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleStateHandler
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * Netty-based network server for the RS2 317 protocol.
 *
 * Pipeline stages:
 *   1. IdleStateHandler (30s timeout)
 *   2. LoginDecoder (handshake + login)
 *   3. GameSessionHandler (routes login results and game packets)
 *
 * After successful login, the LoginDecoder is swapped for GamePacketDecoder.
 */
class NetworkServer(
    private val port: Int,
    private val engine: GameEngine,
    private val playerManager: PlayerManager,
    private val playerSerializer: PlayerSerializer
) {

    private val log = LoggerFactory.getLogger(NetworkServer::class.java)
    private val bossGroup = NioEventLoopGroup(1)
    private val workerGroup = NioEventLoopGroup(4)

    fun start() {
        val bootstrap = ServerBootstrap()
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(
                        IdleStateHandler(30, 0, 0, TimeUnit.SECONDS),
                    )
                    ch.pipeline().addLast("loginDecoder", LoginDecoder())
                    ch.pipeline().addLast("handler", GameSessionHandler(engine, playerManager, playerSerializer))
                }
            })

        val future = bootstrap.bind(port).sync()
        log.info("Network server listening on port {}", port)
    }

    fun stop() {
        log.info("Shutting down network server...")
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }
}

/**
 * Handles the lifecycle of a single client connection.
 *
 * Receives [LoginRequest] from LoginDecoder, processes login,
 * then swaps the pipeline to receive [IncomingPacket] from GamePacketDecoder.
 */
class GameSessionHandler(
    private val engine: GameEngine,
    private val playerManager: PlayerManager,
    private val playerSerializer: PlayerSerializer
) : SimpleChannelInboundHandler<Any>() {

    private val log = LoggerFactory.getLogger(GameSessionHandler::class.java)

    private var player: Player? = null

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is LoginRequest -> handleLogin(ctx, msg)
            is IncomingPacket -> handlePacket(msg)
        }
    }

    private fun handleLogin(ctx: ChannelHandlerContext, request: LoginRequest) {
        val username = request.username.trim().lowercase()

        // Validate username
        if (!username.matches(Regex("[a-z0-9 ]+"))) {
            sendLoginResponse(ctx, 4) // Invalid username
            return
        }
        if (username.length > 12) {
            sendLoginResponse(ctx, 8) // Username too long
            return
        }

        // Check if already online
        if (playerManager.isOnline(username)) {
            sendLoginResponse(ctx, 5) // Already logged in
            return
        }

        // Check server capacity
        if (playerManager.count >= 2000) {
            sendLoginResponse(ctx, 7) // World full
            return
        }

        // Create player and attempt to load save
        val displayName = username.replaceFirstChar { it.uppercase() }
        val newPlayer = Player(
            name = displayName,
            password = request.password,
            channel = request.channel,
            outCipher = request.outCipher
        )

        val loadResult = playerSerializer.load(newPlayer, request.password)
        when (loadResult) {
            PlayerSerializer.LoadResult.INVALID_PASSWORD -> {
                sendLoginResponse(ctx, 3) // Bad password
                return
            }
            PlayerSerializer.LoadResult.NEW -> {
                log.info("New player created: {}", displayName)
            }
            PlayerSerializer.LoadResult.SUCCESS -> {
                log.info("Save loaded for: {}", displayName)
            }
        }

        // Swap pipeline: remove login decoder, add game packet decoder
        ctx.pipeline().replace("loginDecoder", "gameDecoder",
            GamePacketDecoder(request.inCipher)
        )

        // Store player reference for this session
        this.player = newPlayer

        // Queue the login for the game engine to process on the game thread
        engine.pendingLogins.add(newPlayer)
    }

    private fun handlePacket(packet: IncomingPacket) {
        val p = player ?: return
        // Queue the packet for processing on the game thread
        p.packetQueue.add(packet)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val p = player ?: return
        // Queue logout for the game engine
        engine.pendingLogouts.add(p)
        player = null
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent) {
            // Client timed out
            val p = player
            if (p != null) {
                log.debug("Idle timeout for {}", p.name)
                engine.pendingLogouts.add(p)
                player = null
            }
            ctx.close()
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        val p = player
        if (p != null) {
            log.debug("Connection error for {}: {}", p.name, cause.message)
            engine.pendingLogouts.add(p)
            player = null
        }
        ctx.close()
    }

    private fun sendLoginResponse(ctx: ChannelHandlerContext, code: Int) {
        val buf = ctx.alloc().buffer(3)
        buf.writeByte(code)
        buf.writeByte(0)
        buf.writeByte(0)
        ctx.writeAndFlush(buf).addListener(ChannelFutureListener.CLOSE)
    }
}
