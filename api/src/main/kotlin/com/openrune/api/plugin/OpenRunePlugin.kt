package com.openrune.api.plugin

/**
 * Metadata annotation for plugin classes.
 * Every plugin JAR must have exactly one class annotated with this.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PluginInfo(
    /** Unique plugin identifier (lowercase, no spaces). */
    val id: String,
    /** Human-readable display name. */
    val name: String,
    /** Semantic version string. */
    val version: String = "1.0.0",
    /** Short description shown in the launcher. */
    val description: String = "",
    /** Plugin IDs this plugin depends on (loaded first). */
    val dependencies: Array<String> = [],
    /** Plugin IDs this plugin conflicts with (cannot coexist). */
    val conflicts: Array<String> = [],
    /** Author name. */
    val author: String = "",
    /** If true, this plugin can be toggled on/off at runtime without restart. */
    val hotSwappable: Boolean = false
)

/**
 * Base class for all OpenRune plugins.
 *
 * The engine calls lifecycle methods in order:
 *   onLoad -> onEnable -> (running) -> onDisable -> onUnload
 *
 * Plugins register all their event handlers, commands, and content
 * inside [onEnable]. The engine tears everything down on [onDisable].
 */
abstract class OpenRunePlugin {

    /** Reference to the engine context, injected before onLoad. */
    lateinit var context: PluginContext

    /** Called when the plugin JAR is first loaded. Use for one-time setup. */
    open fun onLoad() {}

    /** Called when the plugin is activated. Register all handlers here. */
    open fun onEnable() {}

    /** Called when the plugin is deactivated. Cleanup happens automatically. */
    open fun onDisable() {}

    /** Called when the plugin JAR is being removed from memory. */
    open fun onUnload() {}

    /** Quick access to the plugin's own metadata. */
    val info: PluginInfo
        get() = this::class.java.getAnnotation(PluginInfo::class.java)
            ?: throw IllegalStateException("Plugin class missing @PluginInfo annotation")
}
