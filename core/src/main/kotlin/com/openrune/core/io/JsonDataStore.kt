package com.openrune.core.io

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.openrune.api.config.DataStore
import com.openrune.api.event.DataReloadEvent
import com.openrune.api.event.EventBus
import org.slf4j.LoggerFactory
import java.nio.file.*
import java.util.concurrent.ConcurrentHashMap

/**
 * JSON-backed data store with hot-reload support.
 *
 * Directory layout:
 *   data/
 *     npcs/
 *       npcs.json          (array of objects, each must have an "id" field)
 *     items/
 *       items.json
 *     drops/
 *       drops.json         (keyed by NPC ID)
 *     shops/
 *       shops.json
 *     spawns/
 *       spawns.json
 *     objects/
 *       objects.json
 *     config/
 *       server.json        (global server configuration)
 *
 * Each store can contain one or more .json files. They are merged.
 * Files can be edited externally and reloaded via [reload] or ::reload command.
 */
class JsonDataStore(
    private val dataDir: Path,
    private val eventBus: EventBus?
) : DataStore {

    private val log = LoggerFactory.getLogger(JsonDataStore::class.java)
    private val gson: Gson = GsonBuilder().setPrettyPrinting().setLenient().create()

    // Store name -> (ID -> JsonObject)
    private val stores = ConcurrentHashMap<String, MutableMap<Int, JsonObject>>()

    // File watcher for hot-reload
    private var watchThread: Thread? = null
    private var watchService: WatchService? = null

    /**
     * Initial load of all data directories.
     */
    fun loadAll() {
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir)
            log.warn("Data directory created at {}, it's empty -- populate with JSON files", dataDir)
            return
        }

        Files.list(dataDir)
            .filter { Files.isDirectory(it) }
            .forEach { dir ->
                val storeName = dir.fileName.toString()
                loadStore(storeName, dir)
            }

        log.info("Loaded {} data store(s): {}", stores.size, stores.keys.joinToString())
    }

    private fun loadStore(name: String, dir: Path) {
        val entries = mutableMapOf<Int, JsonObject>()

        Files.list(dir)
            .filter { it.toString().endsWith(".json") }
            .forEach { file ->
                try {
                    val content = Files.readString(file)
                    val element = JsonParser.parseString(content)

                    when {
                        // Array of objects, each with an "id" field
                        element.isJsonArray -> {
                            for (item in element.asJsonArray) {
                                if (item.isJsonObject) {
                                    val obj = item.asJsonObject
                                    val id = obj.get("id")?.asInt
                                    if (id != null) {
                                        entries[id] = obj
                                    } else {
                                        log.warn("Entry in {} missing 'id' field, skipping", file.fileName)
                                    }
                                }
                            }
                        }
                        // Object keyed by ID strings
                        element.isJsonObject -> {
                            for ((key, value) in element.asJsonObject.entrySet()) {
                                val id = key.toIntOrNull()
                                if (id != null && value.isJsonObject) {
                                    val obj = value.asJsonObject
                                    if (!obj.has("id")) obj.addProperty("id", id)
                                    entries[id] = obj
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    log.error("Failed to parse {}: {}", file.fileName, e.message)
                }
            }

        stores[name] = entries
        log.info("  {} -> {} entries", name, entries.size)
    }

    // ================================================================
    //  DataStore interface
    // ================================================================

    override fun get(store: String, id: Int): JsonObject? =
        stores[store]?.get(id)

    override fun getAll(store: String): Map<Int, JsonObject> =
        stores[store]?.toMap() ?: emptyMap()

    override fun query(store: String, predicate: (Int, JsonObject) -> Boolean): List<Pair<Int, JsonObject>> =
        stores[store]?.filter { (id, obj) -> predicate(id, obj) }?.map { it.toPair() } ?: emptyList()

    override fun <T> getTyped(store: String, id: Int, type: Class<T>): T? {
        val json = get(store, id) ?: return null
        return gson.fromJson(json, type)
    }

    override fun <T> getAllTyped(store: String, type: Class<T>): Map<Int, T> {
        val entries = stores[store] ?: return emptyMap()
        val result = mutableMapOf<Int, T>()
        for ((id, json) in entries) {
            try {
                result[id] = gson.fromJson(json, type)
            } catch (e: Exception) {
                log.warn("Failed to deserialize {} ID {} as {}: {}", store, id, type.simpleName, e.message)
            }
        }
        return result
    }

    override fun reload(store: String) {
        val dir = dataDir.resolve(store)
        if (!Files.exists(dir)) {
            log.warn("Cannot reload store '{}' -- directory not found", store)
            return
        }
        loadStore(store, dir)
        eventBus?.emit(DataReloadEvent(store))
        log.info("Reloaded data store: {} ({} entries)", store, stores[store]?.size ?: 0)
    }

    override fun reloadAll() {
        stores.clear()
        loadAll()
        eventBus?.emit(DataReloadEvent("*"))
        log.info("Reloaded all data stores")
    }

    override fun storeNames(): Set<String> = stores.keys.toSet()

    // ================================================================
    //  File Watching (Hot-Reload)
    // ================================================================

    /**
     * Start watching the data directory for changes.
     * When a JSON file is modified, the corresponding store is reloaded.
     */
    fun startWatching() {
        watchService = FileSystems.getDefault().newWatchService()

        // Register each subdirectory
        Files.list(dataDir)
            .filter { Files.isDirectory(it) }
            .forEach { dir ->
                dir.register(watchService!!, StandardWatchEventKinds.ENTRY_MODIFY)
            }

        watchThread = Thread({
            log.info("Data file watcher started")
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val key = watchService?.poll(1, java.util.concurrent.TimeUnit.SECONDS) ?: continue
                    val dir = key.watchable() as Path
                    val storeName = dir.fileName.toString()

                    for (event in key.pollEvents()) {
                        val filename = event.context()?.toString() ?: continue
                        if (filename.endsWith(".json")) {
                            log.info("Detected change in {}/{}, reloading store...", storeName, filename)
                            reload(storeName)
                        }
                    }
                    key.reset()
                } catch (_: InterruptedException) {
                    break
                } catch (e: Exception) {
                    log.error("File watcher error", e)
                }
            }
        }, "DataFileWatcher").apply {
            isDaemon = true
            start()
        }
    }

    fun stopWatching() {
        watchThread?.interrupt()
        watchService?.close()
    }
}
