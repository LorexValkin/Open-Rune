package com.openrune.core.event

import com.openrune.api.event.*
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

/**
 * Thread-safe event bus implementation.
 * Handlers are sorted by priority and called in order.
 * Lower priority values run first.
 */
class EventBusImpl : EventBus {

    private val log = LoggerFactory.getLogger(EventBusImpl::class.java)

    private data class HandlerEntry<T : GameEvent>(
        val priority: EventPriority,
        val receiveCancelled: Boolean,
        val owner: String,
        val handler: (T) -> Unit
    ) : Comparable<HandlerEntry<*>> {
        override fun compareTo(other: HandlerEntry<*>): Int =
            this.priority.value.compareTo(other.priority.value)
    }

    // Map of event class -> sorted list of handlers
    private val handlers = ConcurrentHashMap<KClass<*>, CopyOnWriteArrayList<HandlerEntry<*>>>()

    override fun <T : GameEvent> subscribe(
        eventType: KClass<T>,
        priority: EventPriority,
        receiveCancelled: Boolean,
        owner: String,
        handler: (T) -> Unit
    ): EventSubscription {
        val entry = HandlerEntry(priority, receiveCancelled, owner, handler)
        val list = handlers.computeIfAbsent(eventType) { CopyOnWriteArrayList() }
        list.add(entry)
        // Re-sort by priority after insertion
        list.sortWith(compareBy { it.priority.value })

        return object : EventSubscription {
            override fun unsubscribe() {
                list.remove(entry)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : GameEvent> emit(event: T): T {
        val list = handlers[event::class] ?: return event

        for (entry in list) {
            if (event.cancelled && !entry.receiveCancelled) continue

            try {
                (entry as HandlerEntry<T>).handler(event)
            } catch (e: Exception) {
                log.error("Error in event handler for ${event::class.simpleName} (owner: ${entry.owner})", e)
            }
        }
        return event
    }

    override fun unsubscribeAll(owner: String) {
        var removed = 0
        for ((_, list) in handlers) {
            val before = list.size
            list.removeIf { it.owner == owner }
            removed += before - list.size
        }
        if (removed > 0) {
            log.debug("Unsubscribed {} handlers for owner '{}'", removed, owner)
        }
    }

    /** Diagnostic: count total registered handlers. */
    fun handlerCount(): Int = handlers.values.sumOf { it.size }
}
