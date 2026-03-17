package com.openrune.core.plugin

import com.openrune.api.config.DataStore
import com.openrune.api.entity.PlayerRef
import com.openrune.api.event.EventBus
import com.openrune.api.plugin.*
import org.slf4j.LoggerFactory

/**
 * Concrete plugin context that wraps engine services.
 * Each plugin gets its own context instance so we can track ownership.
 */
class PluginContextImpl(
    private val pluginInfo: PluginInfo,
    override val events: EventBus,
    override val data: DataStore,
    override val players: PlayerRegistry,
    private val taskScheduler: TaskScheduler,
    private val pluginLookup: (String) -> OpenRunePlugin?,
    private val tickProvider: () -> Long
) : PluginContext {

    private val log = LoggerFactory.getLogger("Plugin:${pluginInfo.id}")

    override fun schedule(delayTicks: Int, repeatTicks: Int, task: () -> Unit): TaskHandle {
        return taskScheduler.schedule(pluginInfo.id, delayTicks, repeatTicks, task)
    }

    override fun log(message: String) {
        log.info(message)
    }

    override fun warn(message: String) {
        log.warn(message)
    }

    override fun getPlugin(id: String): OpenRunePlugin? = pluginLookup(id)

    override fun currentTick(): Long = tickProvider()
}

/**
 * Central task scheduler for the game engine.
 * All tasks run on the game thread during the tick cycle.
 */
class TaskScheduler {

    private val tasks = mutableListOf<ScheduledTask>()
    private val pending = mutableListOf<ScheduledTask>()

    data class ScheduledTask(
        val owner: String,
        val repeatTicks: Int,
        var remainingDelay: Int,
        val action: () -> Unit,
        var active: Boolean = true
    )

    fun schedule(owner: String, delayTicks: Int, repeatTicks: Int, action: () -> Unit): TaskHandle {
        val task = ScheduledTask(owner, repeatTicks, delayTicks, action)
        synchronized(pending) {
            pending.add(task)
        }
        return object : TaskHandle {
            override fun cancel() { task.active = false }
            override val isActive: Boolean get() = task.active
        }
    }

    /**
     * Called once per game tick. Processes all scheduled tasks.
     */
    fun tick() {
        // Add pending tasks
        synchronized(pending) {
            tasks.addAll(pending)
            pending.clear()
        }

        val iterator = tasks.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()
            if (!task.active) {
                iterator.remove()
                continue
            }

            if (task.remainingDelay > 0) {
                task.remainingDelay--
                continue
            }

            try {
                task.action()
            } catch (e: Exception) {
                LoggerFactory.getLogger(TaskScheduler::class.java)
                    .error("Error in scheduled task (owner: {})", task.owner, e)
            }

            if (task.repeatTicks > 0) {
                task.remainingDelay = task.repeatTicks
            } else {
                iterator.remove()
            }
        }
    }

    /**
     * Cancel all tasks owned by a specific plugin.
     */
    fun cancelAll(owner: String) {
        tasks.filter { it.owner == owner }.forEach { it.active = false }
        tasks.removeIf { it.owner == owner }
    }

    fun taskCount(): Int = tasks.count { it.active }
}
