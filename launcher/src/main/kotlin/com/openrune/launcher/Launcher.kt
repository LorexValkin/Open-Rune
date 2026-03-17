package com.openrune.launcher

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.openrune.core.OpenRuneServer
import com.openrune.core.plugin.PluginLoader
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.Scanner

/**
 * OpenRune Server Launcher
 *
 * Provides a management interface for:
 *   - Discovering and toggling plugins on/off
 *   - Editing JSON data files
 *   - Starting/stopping the server
 *   - Managing the plugin manifest (which plugins are active)
 *
 * The manifest is saved to data/config/plugins.json so selections
 * persist between sessions.
 */
fun main(args: Array<String>) {
    val launcher = Launcher()

    if (args.contains("--start") || args.contains("-s")) {
        // Direct start mode: load manifest and boot
        launcher.loadManifest()
        launcher.startServer()
        return
    }

    launcher.interactiveMode()
}

class Launcher {

    private val log = LoggerFactory.getLogger(Launcher::class.java)
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val manifestPath = Path.of("data", "config", "plugins.json")
    private val pluginDir = Path.of("server", "plugins")

    private val manifest = mutableMapOf<String, Boolean>()
    private var server: OpenRuneServer? = null

    // Discover available plugins (without loading them)
    private val discoveredPlugins: Map<String, PluginLoader.PluginDescriptor> by lazy {
        val loader = PluginLoader(pluginDir) { throw IllegalStateException() }
        loader.discover()
        loader.getDescriptors()
    }

    /**
     * Interactive CLI mode.
     */
    fun interactiveMode() {
        val scanner = Scanner(System.`in`)

        println()
        println("  ╔══════════════════════════════════════════╗")
        println("  ║       OpenRune Server Launcher v0.1      ║")
        println("  ║    Modular RS2 317 Game Engine            ║")
        println("  ╚══════════════════════════════════════════╝")
        println()

        loadManifest()

        var running = true
        while (running) {
            println()
            println("  [1] Manage Plugins")
            println("  [2] Edit Data Files")
            println("  [3] Server Configuration")
            println("  [4] Start Server")
            println("  [5] Exit")
            println()
            print("  > ")

            when (scanner.nextLine().trim()) {
                "1" -> pluginMenu(scanner)
                "2" -> dataMenu(scanner)
                "3" -> configMenu(scanner)
                "4" -> {
                    startServer()
                    running = false
                }
                "5" -> running = false
                else -> println("  Invalid option.")
            }
        }
    }

    // ================================================================
    //  Plugin Management
    // ================================================================

    private fun pluginMenu(scanner: Scanner) {
        println()
        println("  ─── Plugin Manager ───")
        println()

        if (discoveredPlugins.isEmpty()) {
            println("  No plugin JARs found in $pluginDir")
            println("  Build plugins with: ./gradlew :plugins:skills-plugin:jar")
            return
        }

        val plugins = discoveredPlugins.entries.toList()
        for ((i, entry) in plugins.withIndex()) {
            val (id, desc) = entry
            val enabled = manifest[id] ?: false
            val status = if (enabled) " [ON]" else "[OFF]"
            val deps = if (desc.info.dependencies.isNotEmpty()) " (requires: ${desc.info.dependencies.joinToString()})" else ""
            println("  ${i + 1}. $status ${desc.info.name} v${desc.info.version} - ${desc.info.description}$deps")
        }

        println()
        println("  Enter number to toggle, 'all' to enable all, 'none' to disable all, or 'back'")
        print("  > ")

        val input = scanner.nextLine().trim().lowercase()
        when {
            input == "back" -> return
            input == "all" -> {
                for (id in discoveredPlugins.keys) manifest[id] = true
                saveManifest()
                println("  All plugins enabled.")
            }
            input == "none" -> {
                for (id in discoveredPlugins.keys) manifest[id] = false
                saveManifest()
                println("  All plugins disabled.")
            }
            input.toIntOrNull() != null -> {
                val idx = input.toInt() - 1
                if (idx in plugins.indices) {
                    val id = plugins[idx].key
                    val current = manifest[id] ?: false
                    manifest[id] = !current
                    saveManifest()
                    val newState = if (!current) "ENABLED" else "DISABLED"
                    println("  ${plugins[idx].value.info.name} -> $newState")
                } else {
                    println("  Invalid selection.")
                }
            }
        }
    }

    // ================================================================
    //  Data File Editor
    // ================================================================

    private fun dataMenu(scanner: Scanner) {
        val dataDir = Path.of("data")
        if (!Files.exists(dataDir)) {
            println("  Data directory not found.")
            return
        }

        println()
        println("  ─── Data File Manager ───")
        println()

        val stores = Files.list(dataDir)
            .filter { Files.isDirectory(it) && it.fileName.toString() != "saves" }
            .toList()
            .sortedBy { it.fileName.toString() }

        for ((i, dir) in stores.withIndex()) {
            val fileCount = Files.list(dir).filter { it.toString().endsWith(".json") }.count()
            println("  ${i + 1}. ${dir.fileName} ($fileCount files)")
        }

        println()
        println("  Enter number to list files, or 'back'")
        print("  > ")

        val input = scanner.nextLine().trim()
        if (input == "back") return

        val idx = input.toIntOrNull()?.minus(1)
        if (idx == null || idx !in stores.indices) {
            println("  Invalid selection.")
            return
        }

        val dir = stores[idx]
        val files = Files.list(dir).filter { it.toString().endsWith(".json") }.toList()

        println()
        println("  Files in ${dir.fileName}/:")
        for ((fi, file) in files.withIndex()) {
            val size = Files.size(file)
            println("  ${fi + 1}. ${file.fileName} (${size} bytes)")
        }

        println()
        println("  These files can be edited with any text editor.")
        println("  Changes are automatically detected and reloaded while the server runs.")
        println()
        println("  File locations:")
        for (file in files) {
            println("    ${file.toAbsolutePath()}")
        }
    }

    // ================================================================
    //  Server Configuration
    // ================================================================

    private fun configMenu(scanner: Scanner) {
        val configPath = Path.of("data", "config", "server.json")

        println()
        println("  ─── Server Configuration ───")
        println()

        if (Files.exists(configPath)) {
            try {
                val content = Files.readString(configPath)
                val root = JsonParser.parseString(content).asJsonObject

                println("  Port:           ${root.get("port")?.asInt ?: 43594}")
                println("  Max Players:    ${root.get("maxPlayers")?.asInt ?: 2048}")
                println("  Debug Mode:     ${root.get("debugMode")?.asBoolean ?: false}")
                println("  Auto-reload:    ${root.get("dataWatchEnabled")?.asBoolean ?: true}")
                println("  Save interval:  ${root.get("saveIntervalTicks")?.asInt ?: 500} ticks")
                println()
                println("  Edit: ${configPath.toAbsolutePath()}")
            } catch (e: Exception) {
                println("  Error reading config: ${e.message}")
            }
        } else {
            println("  No config file found. It will be created on first server start.")
        }
    }

    // ================================================================
    //  Server Start
    // ================================================================

    fun startServer() {
        println()
        println("  Starting OpenRune Server...")
        println()

        // Write manifest so core server reads it
        saveManifest()

        server = OpenRuneServer()
        server!!.start()

        // Console command loop while server runs
        val scanner = Scanner(System.`in`)
        println()
        println("  Server running. Type 'stop' to shut down, 'help' for commands.")
        println()

        while (true) {
            print("  server> ")
            val line = scanner.nextLine().trim()

            when {
                line == "stop" || line == "exit" -> {
                    server!!.stop()
                    break
                }
                line == "help" -> {
                    println("  stop          - Shut down the server")
                    println("  players       - List online players")
                    println("  npcs          - NPC count and stats")
                    println("  engine        - Engine tick stats")
                    println("  plugins       - List plugin status")
                    println("  reload [name] - Reload a data store (or all)")
                    println("  enable <id>   - Enable a plugin at runtime")
                    println("  disable <id>  - Disable a plugin at runtime")
                }
                line == "players" -> {
                    println("  Online: ${server!!.playerManager.count}")
                    for (p in server!!.playerManager.allPlayers()) {
                        println("    [${p.index}] ${p.name} at ${p.position}")
                    }
                }
                line == "npcs" -> {
                    val eng = server!!.engine
                    println("  Active NPCs: ${eng.npcManager.count}")
                    println("  Collision regions: ${eng.collisionMap.regionCount()}")
                    println("  Ground items: ${eng.groundItemManager.itemCount()}")
                    println("  Custom objects: ${eng.objectManager.customObjectCount()}")
                }
                line == "engine" -> {
                    val eng = server!!.engine
                    println("  Tick: ${eng.currentTick}")
                    println("  Players: ${server!!.playerManager.count}")
                    println("  NPCs: ${eng.npcManager.count}")
                    println("  Regions loaded: ${eng.regionLoader.loadedCount()}")
                    println("  Collision regions: ${eng.collisionMap.regionCount()}")
                    println("  Ground items: ${eng.groundItemManager.itemCount()}")
                    println("  Custom objects: ${eng.objectManager.customObjectCount()}")
                    println("  Tasks: ${server!!.taskScheduler.taskCount()}")
                    println("  Event handlers: ${server!!.eventBus.handlerCount()}")
                }
                line == "plugins" -> {
                    for ((id, plugin) in server!!.pluginLoader.getEnabled()) {
                        println("    [ON]  ${plugin.info.name} v${plugin.info.version}")
                    }
                    for ((id, desc) in server!!.pluginLoader.getDescriptors()) {
                        if (!server!!.pluginLoader.isEnabled(id)) {
                            println("    [OFF] ${desc.info.name} v${desc.info.version}")
                        }
                    }
                }
                line.startsWith("reload") -> {
                    val store = line.removePrefix("reload").trim()
                    if (store.isEmpty()) {
                        server!!.dataStore.reloadAll()
                        println("  All data stores reloaded.")
                    } else {
                        server!!.dataStore.reload(store)
                        println("  Reloaded: $store")
                    }
                }
                line.startsWith("enable ") -> {
                    val id = line.removePrefix("enable ").trim()
                    try {
                        server!!.pluginLoader.enablePlugin(id)
                        println("  Enabled: $id")
                    } catch (e: Exception) {
                        println("  Error: ${e.message}")
                    }
                }
                line.startsWith("disable ") -> {
                    val id = line.removePrefix("disable ").trim()
                    server!!.pluginLoader.disablePlugin(id)
                    println("  Disabled: $id")
                }
                line.isNotEmpty() -> {
                    println("  Unknown command. Type 'help'.")
                }
            }
        }
    }

    // ================================================================
    //  Manifest Persistence
    // ================================================================

    fun loadManifest() {
        if (!Files.exists(manifestPath)) return
        try {
            val root = JsonParser.parseString(Files.readString(manifestPath)).asJsonObject
            for ((key, value) in root.entrySet()) {
                manifest[key] = value.asBoolean
            }
        } catch (e: Exception) {
            log.error("Failed to load plugin manifest", e)
        }
    }

    private fun saveManifest() {
        Files.createDirectories(manifestPath.parent)
        val obj = com.google.gson.JsonObject()
        for ((id, enabled) in manifest) {
            obj.addProperty(id, enabled)
        }
        Files.writeString(manifestPath, gson.toJson(obj))
    }
}
