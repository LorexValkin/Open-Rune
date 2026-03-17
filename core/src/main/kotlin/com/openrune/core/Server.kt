package com.openrune.core

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.openrune.api.plugin.PluginInfo
import com.openrune.api.event.on
import com.openrune.core.engine.GameEngine
import com.openrune.core.event.EventBusImpl
import com.openrune.core.io.JsonDataStore
import com.openrune.core.io.PlayerSerializer
import com.openrune.core.net.NetworkServer
import com.openrune.core.net.handler.PacketDispatcher
import com.openrune.core.plugin.PluginContextImpl
import com.openrune.core.plugin.PluginLoader
import com.openrune.core.plugin.TaskScheduler
import com.openrune.core.world.PlayerManager
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

/**
 * OpenRune Server - A modular RS2 317 game server.
 *
 * Architecture:
 *   - All game content (skills, combat, commands) lives in plugin JARs
 *   - The core engine provides networking, the tick loop, and player management
 *   - JSON data files define NPCs, items, drops, shops, spawns (editable at runtime)
 *   - Plugins communicate through a typed event bus
 *   - The launcher manages which plugins are active
 */
fun main(args: Array<String>) {
    val server = OpenRuneServer()
    server.start()

    // Shutdown hook
    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop()
    })

    // Keep alive
    Thread.currentThread().join()
}

class OpenRuneServer {

    private val log = LoggerFactory.getLogger(OpenRuneServer::class.java)
    private val gson = GsonBuilder().setPrettyPrinting().create()

    // Core systems
    lateinit var eventBus: EventBusImpl
    lateinit var taskScheduler: TaskScheduler
    lateinit var playerManager: PlayerManager
    lateinit var dataStore: JsonDataStore
    lateinit var playerSerializer: PlayerSerializer
    lateinit var packetDispatcher: PacketDispatcher
    lateinit var engine: GameEngine
    lateinit var network: NetworkServer
    lateinit var pluginLoader: PluginLoader

    // Config
    private var port = 43594
    private var enabledPlugins = mutableSetOf<String>()

    fun start() {
        val startTime = System.currentTimeMillis()

        log.info("==============================================")
        log.info("  OpenRune Server v0.1.0")
        log.info("  Modular RS2 317 Game Engine")
        log.info("==============================================")

        // Load server configuration
        loadConfig()

        // Initialize core systems
        log.info("Initializing core systems...")

        eventBus = EventBusImpl()
        taskScheduler = TaskScheduler()
        playerManager = PlayerManager(2048)
        dataStore = JsonDataStore(Path.of("data"), eventBus)
        playerSerializer = PlayerSerializer(Path.of("data", "saves"))
        packetDispatcher = PacketDispatcher(eventBus, playerManager)

        // Load JSON data
        log.info("Loading game data...")
        dataStore.loadAll()

        // Start file watcher for hot-reload
        dataStore.startWatching()

        // Initialize game engine with cache for collision data
        // Check cache-data/ first (project root), fall back to client cache location
        val cachePath = Path.of("cache-data")
        val clientCachePath = Path.of(System.getProperty("user.home"), ".openrune", "cache")
        val resolvedCache = when {
            java.nio.file.Files.exists(cachePath) -> cachePath
            java.nio.file.Files.exists(clientCachePath) -> {
                log.info("Using client cache at: {}", clientCachePath)
                clientCachePath
            }
            else -> null
        }
        engine = GameEngine(eventBus, playerManager, packetDispatcher, playerSerializer, taskScheduler,
            resolvedCache)

        // Load NPC spawns from data
        log.info("Loading NPC spawns...")
        engine.npcManager.loadSpawns(dataStore)

        // Discover and load plugins
        log.info("Loading plugins...")
        pluginLoader = PluginLoader(Path.of("server", "plugins")) { info ->
            createPluginContext(info)
        }

        pluginLoader.discover()

        // Load plugins from config, or all discovered if none specified
        val toLoad = if (enabledPlugins.isNotEmpty()) {
            enabledPlugins
        } else {
            pluginLoader.getDescriptors().keys
        }
        if (toLoad.isNotEmpty()) {
            pluginLoader.loadEnabled(toLoad)
        }

        // Register built-in commands
        registerBuiltinCommands()

        // Start the game engine
        engine.start()

        // Start the network server
        network = NetworkServer(port, engine, playerManager, playerSerializer)
        network.start()

        val elapsed = System.currentTimeMillis() - startTime
        log.info("==============================================")
        log.info("  Server online on port {} ({}ms)", port, elapsed)
        log.info("  Plugins: {} enabled", pluginLoader.getEnabled().size)
        log.info("  NPCs: {} spawned", engine.npcManager.count)
        log.info("  Data stores: {}", dataStore.storeNames().joinToString())
        log.info("  Collision regions: {}", engine.collisionMap.regionCount())
        log.info("==============================================")
    }

    fun stop() {
        log.info("Server shutting down...")
        pluginLoader.disableAll()
        engine.stop()
        network.stop()
        dataStore.stopWatching()
        log.info("Server stopped.")
    }

    private fun loadConfig() {
        val configPath = Path.of("data", "config", "server.json")

        if (!Files.exists(configPath)) {
            // Create default config
            Files.createDirectories(configPath.parent)
            val defaults = mapOf(
                "port" to 43594,
                "maxPlayers" to 2048,
                "enabledPlugins" to listOf<String>(),
                "dataWatchEnabled" to true,
                "saveIntervalTicks" to 500,
                "debugMode" to false
            )
            Files.writeString(configPath, gson.toJson(defaults))
            log.info("Created default server config at {}", configPath)
            return
        }

        try {
            val root = JsonParser.parseString(Files.readString(configPath)).asJsonObject
            port = root.get("port")?.asInt ?: 43594

            root.getAsJsonArray("enabledPlugins")?.let { arr ->
                for (item in arr) {
                    enabledPlugins.add(item.asString)
                }
            }

            log.info("Loaded server config: port={}, plugins={}", port, enabledPlugins)
        } catch (e: Exception) {
            log.error("Failed to load server config, using defaults", e)
        }
    }

    private fun createPluginContext(info: PluginInfo): com.openrune.api.plugin.PluginContext {
        return PluginContextImpl(
            pluginInfo = info,
            events = eventBus,
            data = dataStore,
            players = playerManager,
            taskScheduler = taskScheduler,
            pluginLookup = { id -> pluginLoader.getPlugin(id) },
            tickProvider = { engine.currentTick }
        )
    }

    /**
     * Built-in admin commands that are always available.
     */
    private fun registerBuiltinCommands() {
        eventBus.on<com.openrune.api.event.CommandEvent>(owner = "core") { event ->
            val player = event.player
            if (player.rights.value < 2) return@on

            when (event.command) {
                "reload" -> {
                    if (event.args.isEmpty()) {
                        dataStore.reloadAll()
                        player.sendMessage("All data stores reloaded.")
                    } else {
                        val store = event.args[0]
                        dataStore.reload(store)
                        player.sendMessage("Reloaded data store: $store")
                    }
                    event.cancel()
                }

                "plugins" -> {
                    val enabled = pluginLoader.getEnabled()
                    val all = pluginLoader.getDescriptors()
                    player.sendMessage("Plugins (${enabled.size}/${all.size}):")
                    for ((id, desc) in all) {
                        val status = if (id in enabled) "[ON]" else "[OFF]"
                        player.sendMessage("  $status ${desc.info.name} v${desc.info.version}")
                    }
                    event.cancel()
                }

                "enableplugin" -> {
                    if (event.args.isEmpty()) {
                        player.sendMessage("Usage: ::enableplugin <id>")
                    } else {
                        val id = event.args[0]
                        try {
                            pluginLoader.enablePlugin(id)
                            player.sendMessage("Enabled plugin: $id")
                        } catch (e: Exception) {
                            player.sendMessage("Error: ${e.message}")
                        }
                    }
                    event.cancel()
                }

                "disableplugin" -> {
                    if (event.args.isEmpty()) {
                        player.sendMessage("Usage: ::disableplugin <id>")
                    } else {
                        val id = event.args[0]
                        pluginLoader.disablePlugin(id)
                        player.sendMessage("Disabled plugin: $id")
                    }
                    event.cancel()
                }

                "reloadplugin" -> {
                    if (event.args.isEmpty()) {
                        player.sendMessage("Usage: ::reloadplugin <id>")
                    } else {
                        val id = event.args[0]
                        pluginLoader.reloadPlugin(id)
                        player.sendMessage("Reloaded plugin: $id")
                    }
                    event.cancel()
                }

                "online" -> {
                    player.sendMessage("Players online: ${playerManager.count}")
                    event.cancel()
                }

                "save" -> {
                    for (p in playerManager.allPlayers()) {
                        playerSerializer.save(p)
                    }
                    player.sendMessage("All players saved.")
                    event.cancel()
                }

                "tele" -> {
                    if (event.args.size >= 2) {
                        val x = event.args[0].toIntOrNull()
                        val y = event.args[1].toIntOrNull()
                        val z = event.args.getOrNull(2)?.toIntOrNull() ?: 0
                        if (x != null && y != null) {
                            player.teleport(x, y, z)
                            player.sendMessage("Teleported to $x, $y, $z")
                        }
                    } else {
                        player.sendMessage("Usage: ::tele x y [z]")
                    }
                    event.cancel()
                }

                "npc" -> {
                    if (event.args.isEmpty()) {
                        player.sendMessage("Usage: ::npc <id> - spawn NPC at your location")
                    } else {
                        val npcId = event.args[0].toIntOrNull()
                        if (npcId != null) {
                            val npc = engine.npcManager.spawn(npcId, player.position)
                            if (npc != null) {
                                player.sendMessage("Spawned NPC $npcId (${npc.name}) at ${player.position}")
                            } else {
                                player.sendMessage("Failed to spawn NPC (array full)")
                            }
                        }
                    }
                    event.cancel()
                }

                "removenpc" -> {
                    val nearby = engine.npcManager.findNearby(player.position, 2)
                    if (nearby.isEmpty()) {
                        player.sendMessage("No NPCs nearby to remove.")
                    } else {
                        for (npc in nearby) {
                            engine.npcManager.remove(npc.index)
                            player.sendMessage("Removed NPC ${npc.id} (${npc.name})")
                        }
                    }
                    event.cancel()
                }

                "item" -> {
                    if (event.args.isEmpty()) {
                        player.sendMessage("Usage: ::item <id> [amount]")
                    } else {
                        val itemId = event.args[0].toIntOrNull()
                        val amount = event.args.getOrNull(1)?.toIntOrNull() ?: 1
                        if (itemId != null) {
                            if (player.addItem(itemId, amount)) {
                                player.sendMessage("Added item $itemId x$amount")
                            } else {
                                player.sendMessage("Inventory full.")
                            }
                        }
                    }
                    event.cancel()
                }

                "pos" -> {
                    player.sendMessage("Position: ${player.position} Region: ${player.regionId}")
                    event.cancel()
                }

                "anim" -> {
                    val animId = event.args.getOrNull(0)?.toIntOrNull()
                    if (animId != null) {
                        player.animate(animId)
                        player.sendMessage("Playing animation $animId")
                    } else {
                        player.sendMessage("Usage: ::anim <id>")
                    }
                    event.cancel()
                }

                "gfx" -> {
                    val gfxId = event.args.getOrNull(0)?.toIntOrNull()
                    if (gfxId != null) {
                        player.graphic(gfxId, 100, 0)
                        player.sendMessage("Playing graphic $gfxId")
                    } else {
                        player.sendMessage("Usage: ::gfx <id>")
                    }
                    event.cancel()
                }

                "setlevel" -> {
                    if (event.args.size >= 2) {
                        val skill = event.args[0].toIntOrNull()
                        val level = event.args[1].toIntOrNull()
                        if (skill != null && level != null && skill in 0 until com.openrune.api.entity.Skills.SKILL_COUNT) {
                            player.setLevel(skill, level)
                            player.sendMessage("Set ${com.openrune.api.entity.Skills.NAMES[skill]} to level $level")
                        }
                    } else {
                        player.sendMessage("Usage: ::setlevel <skill_id> <level>")
                    }
                    event.cancel()
                }

                "master" -> {
                    for (i in 0 until com.openrune.api.entity.Skills.SKILL_COUNT) {
                        player.setLevel(i, 99)
                        player.addExperience(i, 13034431.0 - player.getExperience(i))
                    }
                    player.currentHealth = 99
                    player.sendMessage("All skills set to 99.")
                    player.flagAppearanceUpdate()
                    event.cancel()
                }

                "engine" -> {
                    player.sendMessage("Tick: ${engine.currentTick}")
                    player.sendMessage("Players: ${playerManager.count}")
                    player.sendMessage("NPCs: ${engine.npcManager.count}")
                    player.sendMessage("Regions: ${engine.collisionMap.regionCount()}")
                    player.sendMessage("Objects: ${engine.objectManager.customObjectCount()}")
                    player.sendMessage("Ground items: ${engine.groundItemManager.itemCount()}")
                    player.sendMessage("Tasks: ${taskScheduler.taskCount()}")
                    player.sendMessage("Events: ${eventBus.handlerCount()} handlers")
                    event.cancel()
                }
            }
        }

        // Run orb toggle (button 152)
        eventBus.on<com.openrune.api.event.ButtonClickEvent>(owner = "core") { event ->
            val player = event.player as? com.openrune.core.world.Player ?: return@on
            when (event.buttonId) {
                152 -> {
                    player.walkingQueue.running = !player.walkingQueue.running
                    // Send config 173 to update the run orb visual (1=on, 0=off)
                    val config = com.openrune.core.net.codec.PacketBuilder(36)
                    config.addLEShort(173)
                    config.addByte(if (player.walkingQueue.running) 1 else 0)
                    player.send(config)
                }
            }
        }
    }
}
