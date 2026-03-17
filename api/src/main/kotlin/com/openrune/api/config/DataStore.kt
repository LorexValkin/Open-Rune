package com.openrune.api.config

import com.google.gson.JsonObject

/**
 * Provides access to all JSON data files (items, npcs, objects, etc.).
 * Data can be reloaded at runtime via the launcher or ::reload command.
 *
 * Each "store" is a named collection backed by a JSON file in the data/ directory.
 * For example, "npcs" loads from data/npcs/ (all .json files).
 */
interface DataStore {

    /**
     * Get a definition by store name and ID.
     * Returns null if the ID doesn't exist in that store.
     *
     * Example: data.get("items", 4151) returns the item definition for Abyssal Whip.
     */
    fun get(store: String, id: Int): JsonObject?

    /**
     * Get all entries in a store as a map of ID -> JsonObject.
     */
    fun getAll(store: String): Map<Int, JsonObject>

    /**
     * Query entries matching a predicate.
     */
    fun query(store: String, predicate: (Int, JsonObject) -> Boolean): List<Pair<Int, JsonObject>>

    /**
     * Get a typed definition from a store.
     * The engine will deserialize the JSON into the requested class.
     */
    fun <T> getTyped(store: String, id: Int, type: Class<T>): T?

    /**
     * Get all typed definitions from a store.
     */
    fun <T> getAllTyped(store: String, type: Class<T>): Map<Int, T>

    /**
     * Reload a specific data store from disk.
     * Fires a [com.openrune.api.event.DataReloadEvent] when complete.
     */
    fun reload(store: String)

    /**
     * Reload all data stores from disk.
     */
    fun reloadAll()

    /** Get the list of registered store names. */
    fun storeNames(): Set<String>
}

// Inline reified helper
inline fun <reified T> DataStore.getTyped(store: String, id: Int): T? =
    getTyped(store, id, T::class.java)

inline fun <reified T> DataStore.getAllTyped(store: String): Map<Int, T> =
    getAllTyped(store, T::class.java)
