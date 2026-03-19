package com.openrune.core.engine

import com.openrune.api.event.*
import com.openrune.core.event.EventBusImpl
import com.openrune.core.io.PlayerSerializer
import com.openrune.core.net.handler.PacketDispatcher
import com.openrune.core.plugin.TaskScheduler
import com.openrune.core.world.Player
import com.openrune.core.world.PlayerManager
import com.openrune.core.world.collision.CollisionMap
import com.openrune.core.world.movement.MovementProcessor
import com.openrune.core.world.npc.NpcManager
import com.openrune.core.world.pathfinding.Pathfinder
import com.openrune.core.world.region.RegionLoader
import com.openrune.core.world.update.NpcUpdateProtocol
import com.openrune.core.world.update.PlayerUpdateProtocol
import com.openrune.core.world.ObjectManager
import com.openrune.core.world.GroundItemManager
import com.openrune.core.world.interaction.ObjectInteractionHandler
import com.openrune.cache.io.CacheReader
import com.openrune.cache.def.CacheNpcDefinition
import com.openrune.cache.def.NpcDefinitionDecoder
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * The core game engine. Runs the 600ms tick cycle.
 *
 * Tick phases (in order):
 *   1. Process pending logins
 *   2. Process pending logouts
 *   3. Process incoming packets for all players
 *   4. Resolve walk targets via pathfinder
 *   5. Process movement (walking/running) for all entities
 *   6. Run scheduled tasks (plugin timers)
 *   7. Emit ServerTickEvent
 *   8. NPC processing
 *   9. Player update protocol (build and send opcode 81)
 *  10. Reset update flags
 *  11. Periodic saves
 *
 * The engine owns all core systems:
 *   - [CollisionMap] : tile-level collision flags
 *   - [Pathfinder]   : A* pathfinding
 *   - [MovementProcessor] : step queue processing
 *   - [RegionLoader] : cache map loading
 *   - [PlayerUpdateProtocol] : 317 player update
 *
 * These are ENGINE-LEVEL. Plugins cannot replace them.
 */
class GameEngine(
    val eventBus: EventBusImpl,
    val playerManager: PlayerManager,
    val packetDispatcher: PacketDispatcher,
    val playerSerializer: PlayerSerializer,
    val taskScheduler: TaskScheduler,
    cachePath: Path?
) {

    private val log = LoggerFactory.getLogger(GameEngine::class.java)

    companion object {
        const val TICK_RATE_MS = 600L
        const val SAVE_INTERVAL_TICKS = 500
    }

    // === Engine-level systems (not pluggable) ===
    val collisionMap = CollisionMap()
    val pathfinder = Pathfinder(collisionMap)
    val movementProcessor = MovementProcessor(collisionMap, pathfinder)
    val objectManager = ObjectManager(collisionMap)
    val groundItemManager = GroundItemManager()
    val npcManager: NpcManager
    var cacheNpcDefs: Map<Int, CacheNpcDefinition> = emptyMap()
        private set
    val regionLoader: RegionLoader
    val objectInteractionHandler: ObjectInteractionHandler

    init {
        var cacheReader: CacheReader? = null
        if (cachePath != null) {
            cacheReader = CacheReader(cachePath)
            if (cacheReader.open()) {
                log.info("Cache opened: {} model files, {} map files",
                    cacheReader.fileCount(0), cacheReader.fileCount(4))
            } else {
                log.warn("Failed to open cache at {}, collision data will be empty", cachePath)
                cacheReader = null
            }
        }
        // Load NPC definitions from cache
        if (cacheReader != null) {
            cacheNpcDefs = NpcDefinitionDecoder.load(cacheReader)
        }

        regionLoader = RegionLoader(collisionMap, cacheReader)
        regionLoader.initialize()  // Build map index from cache versionlist
        npcManager = NpcManager(16384, eventBus, collisionMap, movementProcessor)

        // Wire NPC lookup into packet dispatcher
        packetDispatcher.npcLookup = { index -> npcManager.getByIndex(index) }

        // Object interactions (doors, stairs, ladders) - engine-level
        objectInteractionHandler = ObjectInteractionHandler(eventBus, playerManager)
        objectInteractionHandler.initialize()
    }

    @Volatile var running = false; private set
    var currentTick: Long = 0L; private set

    val pendingLogins = ConcurrentLinkedQueue<Player>()
    val pendingLogouts = ConcurrentLinkedQueue<Player>()

    private lateinit var engineThread: Thread

    fun start() {
        running = true
        engineThread = Thread({
            log.info("Game engine started ({}ms tick)", TICK_RATE_MS)
            log.info("  Collision map: {} regions loaded", collisionMap.regionCount())
            while (running) {
                val tickStart = System.currentTimeMillis()
                try { tick() } catch (e: Exception) { log.error("Error in tick {}", currentTick, e) }
                val elapsed = System.currentTimeMillis() - tickStart
                val sleep = TICK_RATE_MS - elapsed
                if (sleep > 0) Thread.sleep(sleep)
                else log.warn("Tick {} over budget: {}ms", currentTick, elapsed)
            }
            log.info("Game engine stopped")
        }, "GameEngine")
        engineThread.start()
    }

    fun stop() {
        log.info("Stopping game engine...")
        running = false
        for (player in playerManager.allPlayers()) {
            try { playerSerializer.save(player) } catch (e: Exception) { log.error("Shutdown save failed for {}", player.name, e) }
        }
        if (::engineThread.isInitialized) engineThread.join(5000)
    }

    private fun tick() {
        currentTick++

        // Phase 1: Logins
        processLogins()

        // Phase 2: Logouts
        processLogouts()

        // Phase 3: Packets
        processPackets()

        // Phase 4: NPC walk target resolution (players use client BFS, no server pathfinding)
        // Player walking is handled directly by the walking packet handler
        // which fills the queue from client waypoints. No server A* needed.

        // Phase 4: Get all players for this tick
        val players = playerManager.allPlayers()

        // Phase 5: Movement processing
        for (player in players) {
            movementProcessor.process(player)
        }

        // Phase 6: Scheduled tasks
        taskScheduler.tick()

        // Phase 7: Server tick event
        eventBus.emit(ServerTickEvent(currentTick))

        // Phase 8: NPC processing (movement, random walk, respawns)
        npcManager.process()

        // Phase 9: Object manager (temporary object reverts)
        objectManager.process()

        // Phase 9b: Object interaction handler (auto-close doors)
        objectInteractionHandler.process()

        // Phase 10: Ground item processing (private->global, despawns)
        val groundResult = groundItemManager.process()

        // Phase 11: Region change detection + Player update protocol
        for (player in players) {
            try {
                // Check if the player is within 16 tiles of the edge of the loaded
                // 104x104 tile area. The loaded area base is 6 chunks BEFORE the
                // center chunk that was sent in the map region packet.
                // Server sends: (lastRegion.x >> 3) + 6
                // Client computes: baseX = (received - 6) * 8 = (lastRegion.x >> 3) * 8
                // But the actual loaded tile range starts 6 chunks earlier:
                //   loadedBase = ((lastRegion.x >> 3) - 6) * 8
                //   loadedEnd  = loadedBase + 103
                val loadedBaseX = ((player.lastRegion.x shr 3) - 6) * 8
                val loadedBaseY = ((player.lastRegion.y shr 3) - 6) * 8
                val relX = player.position.x - loadedBaseX
                val relY = player.position.y - loadedBaseY
                if (relX < 16 || relX >= 88 || relY < 16 || relY >= 88) {
                    player.needsRegionUpdate = true
                }

                // Ensure current region is loaded
                regionLoader.ensureLoaded(player.regionId)

                // Send region update if needed
                if (player.needsRegionUpdate) {
                    player.sendMapRegion()
                    // Re-send custom objects and ground items in the new region
                    objectManager.sendRegionObjects(player)
                }

                // Send player update (opcode 81)
                PlayerUpdateProtocol.update(player, players)

                // Send NPC update (opcode 65)
                NpcUpdateProtocol.update(player, npcManager.allNpcs())
            } catch (e: Exception) {
                log.error("Error updating player {}", player.name, e)
            }
        }

        // Phase 12: Ground item client sync (newly global items, despawned items)
        for (player in players) {
            for (item in groundResult.becameGlobal) {
                if (item.position.isWithinDistance(player.position, 60) && item.owner != player.name) {
                    groundItemManager.sendItemToPlayer(player, item)
                }
            }
            for (item in groundResult.despawned) {
                if (item.position.isWithinDistance(player.position, 60)) {
                    groundItemManager.sendRemoveToPlayer(player, item)
                }
            }
        }

        // Phase 13: Reset flags
        for (player in players) {
            player.resetUpdateFlags()
        }
        npcManager.resetFlags()

        // Phase 11: Periodic save
        if (currentTick % SAVE_INTERVAL_TICKS == 0L) {
            for (player in players) {
                try { playerSerializer.save(player) } catch (e: Exception) { log.error("Save failed for {}", player.name, e) }
            }
        }
    }

    private fun processLogins() {
        var count = 0
        while (count < 50) {
            val player = pendingLogins.poll() ?: break
            try {
                val index = playerManager.register(player)
                if (index == -1) { sendLoginResponse(player, 7); continue }

                val event = eventBus.emit(PlayerLoginEvent(player))
                if (event.cancelled) { playerManager.unregister(index); sendLoginResponse(player, 4); continue }

                sendLoginResponse(player, 2)

                // Ensure spawn region is loaded for collision
                regionLoader.ensureLoaded(player.regionId)

                player.initialize()
                eventBus.emit(PlayerPostLoginEvent(player))
                count++
            } catch (e: Exception) {
                log.error("Login error for {}", player.name, e)
                sendLoginResponse(player, 11)
            }
        }
    }

    private fun processLogouts() {
        while (true) {
            val player = pendingLogouts.poll() ?: break
            try {
                eventBus.emit(PlayerLogoutEvent(player))
                playerSerializer.save(player)
                playerManager.unregister(player.index)
                player.channel.close()
            } catch (e: Exception) { log.error("Logout error for {}", player.name, e) }
        }
    }

    private fun processPackets() {
        for (player in playerManager.allPlayers()) {
            var count = 0
            while (count < 25) {
                val packet = player.packetQueue.poll() ?: break
                packetDispatcher.dispatch(player.index, packet)
                count++
            }
        }
    }

    private fun sendLoginResponse(player: Player, code: Int) {
        val buf = player.channel.alloc().buffer(3)
        buf.writeByte(code)
        buf.writeByte(if (code == 2) player.rights.value else 0)
        buf.writeByte(0)
        player.channel.writeAndFlush(buf)
    }
}
