package com.openrune.api.plugin

import com.openrune.api.config.DataStore
import com.openrune.api.entity.PlayerRef
import com.openrune.api.event.EventBus

/**
 * Injected into every plugin before [OpenRunePlugin.onLoad].
 * This is the plugin's window into the engine. All interactions
 * with the game world go through here.
 */
interface PluginContext {

    /** The global event bus. Plugins subscribe to and emit events here. */
    val events: EventBus

    /** Access to JSON data stores (npcs.json, items.json, etc.). */
    val data: DataStore

    /** Access to online players. */
    val players: PlayerRegistry

    /** Schedule a task on the game thread. */
    fun schedule(delayTicks: Int = 0, repeatTicks: Int = -1, task: () -> Unit): TaskHandle

    /** Log a message under this plugin's logger. */
    fun log(message: String)

    /** Log a warning under this plugin's logger. */
    fun warn(message: String)

    /** Get a reference to another loaded plugin by ID, or null. */
    fun getPlugin(id: String): OpenRunePlugin?

    /** Get the server's current tick count. */
    fun currentTick(): Long
}

/**
 * Handle to a scheduled task, allowing cancellation.
 */
interface TaskHandle {
    fun cancel()
    val isActive: Boolean
}

/**
 * Read-only view of online players.
 */
interface PlayerRegistry : Iterable<PlayerRef> {
    val count: Int
    fun find(name: String): PlayerRef?
    fun findByIndex(index: Int): PlayerRef?
}
