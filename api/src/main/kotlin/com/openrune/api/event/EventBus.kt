package com.openrune.api.event

import kotlin.reflect.KClass

/**
 * Base class for all game events. Events flow through the [EventBus]
 * and plugins subscribe to specific event types.
 */
abstract class GameEvent {
    /** If true, subsequent handlers will not receive this event. */
    var cancelled: Boolean = false
        private set

    /** Cancel this event, preventing further processing. */
    fun cancel() {
        cancelled = true
    }
}

/**
 * Priority levels for event handlers.
 * Lower priority handlers run first, giving higher priority handlers the final say.
 */
enum class EventPriority(val value: Int) {
    /** Runs first. Use for monitoring/logging. Do not modify events here. */
    MONITOR(0),
    /** Runs early. Use for pre-processing and validation. */
    LOW(1),
    /** Default priority. Most handlers should use this. */
    NORMAL(2),
    /** Runs late. Use for modifying event results. */
    HIGH(3),
    /** Runs last. Use for final overrides. Can see if event was cancelled. */
    HIGHEST(4)
}

/**
 * Annotation to mark a function as an event handler inside a plugin.
 *
 * ```kotlin
 * @EventHandler(priority = EventPriority.NORMAL)
 * fun onPlayerLogin(event: PlayerLoginEvent) {
 *     event.player.sendMessage("Welcome!")
 * }
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventHandler(
    val priority: EventPriority = EventPriority.NORMAL,
    /** If true, this handler still receives cancelled events. */
    val receiveCancelled: Boolean = false
)

/**
 * Central event bus. Plugins register handlers and emit events through this.
 */
interface EventBus {

    /**
     * Subscribe a handler for a specific event type.
     * Returns a registration handle that can be used to unsubscribe.
     *
     * @param eventType The event class to listen for.
     * @param priority When this handler runs relative to others.
     * @param receiveCancelled Whether to receive events that were cancelled by earlier handlers.
     * @param owner An opaque owner tag (usually the plugin ID) for bulk unsubscription.
     * @param handler The function to call when the event fires.
     */
    fun <T : GameEvent> subscribe(
        eventType: KClass<T>,
        priority: EventPriority = EventPriority.NORMAL,
        receiveCancelled: Boolean = false,
        owner: String = "",
        handler: (T) -> Unit
    ): EventSubscription

    /**
     * Emit an event to all registered handlers.
     * Returns the event after all handlers have processed it (check [GameEvent.cancelled]).
     */
    fun <T : GameEvent> emit(event: T): T

    /**
     * Remove all subscriptions owned by the given owner tag.
     * Called automatically when a plugin is disabled.
     */
    fun unsubscribeAll(owner: String)
}

/**
 * Handle to a single event subscription.
 */
interface EventSubscription {
    fun unsubscribe()
}

// ===== Convenience extension for type-safe inline subscription =====

/**
 * Inline subscribe that infers the event type from the lambda parameter.
 *
 * Usage:
 * ```kotlin
 * context.events.on<PlayerLoginEvent> { event ->
 *     event.player.sendMessage("Hello!")
 * }
 * ```
 */
inline fun <reified T : GameEvent> EventBus.on(
    priority: EventPriority = EventPriority.NORMAL,
    owner: String = "",
    noinline handler: (T) -> Unit
): EventSubscription = subscribe(T::class, priority, false, owner, handler)
