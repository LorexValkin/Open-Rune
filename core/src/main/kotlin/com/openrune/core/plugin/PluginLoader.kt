package com.openrune.core.plugin

import com.openrune.api.plugin.OpenRunePlugin
import com.openrune.api.plugin.PluginContext
import com.openrune.api.plugin.PluginInfo
import org.slf4j.LoggerFactory
import java.net.URLClassLoader
import java.nio.file.*
import java.util.jar.JarFile

/**
 * Discovers, loads, and manages plugin lifecycle.
 *
 * Plugins are JARs placed in the [pluginDir] directory.
 * Each JAR must contain exactly one class annotated with [PluginInfo].
 * The loader resolves dependencies and calls lifecycle methods in order.
 */
class PluginLoader(
    private val pluginDir: Path,
    private val contextFactory: (PluginInfo) -> PluginContext
) {

    private val log = LoggerFactory.getLogger(PluginLoader::class.java)

    /** All discovered plugin descriptors, keyed by plugin ID. */
    private val descriptors = mutableMapOf<String, PluginDescriptor>()

    /** Currently enabled plugins in load order. */
    private val enabled = linkedMapOf<String, OpenRunePlugin>()

    /** Plugin manifest read from config, controlling what's enabled. */
    private val manifest = mutableMapOf<String, Boolean>()

    data class PluginDescriptor(
        val info: PluginInfo,
        val jarPath: Path,
        val mainClass: String,
        var classLoader: URLClassLoader? = null,
        var instance: OpenRunePlugin? = null
    )

    // ================================================================
    //  Discovery
    // ================================================================

    /**
     * Scan the plugin directory for JARs and read their metadata.
     * Does NOT load or enable anything yet.
     */
    fun discover(): List<PluginDescriptor> {
        if (!Files.exists(pluginDir)) {
            Files.createDirectories(pluginDir)
            log.info("Created plugin directory: {}", pluginDir)
        }

        val jars = Files.list(pluginDir)
            .filter { it.toString().endsWith(".jar") }
            .toList()

        log.info("Scanning {} plugin JAR(s) in {}", jars.size, pluginDir)

        for (jar in jars) {
            try {
                val descriptor = scanJar(jar)
                if (descriptor != null) {
                    if (descriptors.containsKey(descriptor.info.id)) {
                        log.warn("Duplicate plugin ID '{}' in {}, skipping", descriptor.info.id, jar.fileName)
                        continue
                    }
                    descriptors[descriptor.info.id] = descriptor
                    log.info("  Found plugin: {} v{} ({})", descriptor.info.name, descriptor.info.version, descriptor.info.id)
                }
            } catch (e: Exception) {
                log.error("Failed to scan plugin JAR: {}", jar.fileName, e)
            }
        }

        return descriptors.values.toList()
    }

    private fun scanJar(jarPath: Path): PluginDescriptor? {
        val jar = JarFile(jarPath.toFile())
        val classLoader = URLClassLoader(arrayOf(jarPath.toUri().toURL()), this::class.java.classLoader)

        val entries = jar.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            if (!entry.name.endsWith(".class")) continue

            val className = entry.name
                .removeSuffix(".class")
                .replace('/', '.')

            try {
                val clazz = classLoader.loadClass(className)
                val annotation = clazz.getAnnotation(PluginInfo::class.java)
                if (annotation != null) {
                    jar.close()
                    return PluginDescriptor(
                        info = annotation,
                        jarPath = jarPath,
                        mainClass = className,
                        classLoader = classLoader
                    )
                }
            } catch (_: Throwable) {
                // Skip classes that fail to load during scan
            }
        }

        jar.close()
        classLoader.close()
        log.warn("No @PluginInfo class found in {}", jarPath.fileName)
        return null
    }

    // ================================================================
    //  Dependency Resolution
    // ================================================================

    /**
     * Resolve load order using topological sort based on dependencies.
     * Returns plugin IDs in the order they should be loaded.
     * Throws if there are unresolved or circular dependencies.
     */
    fun resolveLoadOrder(pluginIds: Set<String>): List<String> {
        val resolved = mutableListOf<String>()
        val visiting = mutableSetOf<String>()
        val visited = mutableSetOf<String>()

        fun visit(id: String) {
            if (id in visited) return
            if (id in visiting) {
                throw IllegalStateException("Circular plugin dependency detected involving '$id'")
            }

            visiting.add(id)

            val descriptor = descriptors[id]
                ?: throw IllegalStateException("Plugin '$id' not found (required as dependency)")

            // Check conflicts
            for (conflict in descriptor.info.conflicts) {
                if (conflict in pluginIds) {
                    throw IllegalStateException(
                        "Plugin '${descriptor.info.id}' conflicts with '$conflict' -- both cannot be enabled"
                    )
                }
            }

            // Visit dependencies first
            for (dep in descriptor.info.dependencies) {
                if (dep !in pluginIds) {
                    throw IllegalStateException(
                        "Plugin '${descriptor.info.id}' requires '$dep' which is not available"
                    )
                }
                visit(dep)
            }

            visiting.remove(id)
            visited.add(id)
            resolved.add(id)
        }

        for (id in pluginIds) {
            visit(id)
        }

        return resolved
    }

    // ================================================================
    //  Loading & Lifecycle
    // ================================================================

    /**
     * Load and enable all plugins that are marked as enabled in the manifest.
     */
    fun loadEnabled(enabledIds: Set<String>) {
        manifest.clear()
        for (id in descriptors.keys) {
            manifest[id] = id in enabledIds
        }

        val toLoad = enabledIds.filter { it in descriptors }
        if (toLoad.isEmpty()) {
            log.info("No plugins to load")
            return
        }

        val loadOrder = resolveLoadOrder(toLoad.toSet())
        log.info("Loading {} plugin(s) in order: {}", loadOrder.size, loadOrder)

        for (id in loadOrder) {
            try {
                enablePlugin(id)
            } catch (e: Exception) {
                log.error("Failed to load plugin '{}'", id, e)
            }
        }
    }

    /**
     * Enable a single plugin by ID.
     */
    fun enablePlugin(id: String) {
        if (id in enabled) {
            log.warn("Plugin '{}' is already enabled", id)
            return
        }

        val descriptor = descriptors[id] ?: throw IllegalStateException("Unknown plugin: $id")

        // Create a fresh classloader if needed
        if (descriptor.classLoader == null || descriptor.instance == null) {
            val cl = URLClassLoader(
                arrayOf(descriptor.jarPath.toUri().toURL()),
                this::class.java.classLoader
            )
            descriptor.classLoader = cl

            val clazz = cl.loadClass(descriptor.mainClass)
            val instance = clazz.getDeclaredConstructor().newInstance() as OpenRunePlugin
            descriptor.instance = instance
        }

        val plugin = descriptor.instance!!

        // Inject the context
        val context = contextFactory(descriptor.info)
        plugin.context = context

        // Lifecycle
        plugin.onLoad()
        plugin.onEnable()
        enabled[id] = plugin

        log.info("Enabled plugin: {} v{}", descriptor.info.name, descriptor.info.version)
    }

    /**
     * Disable a single plugin by ID.
     */
    fun disablePlugin(id: String) {
        val plugin = enabled.remove(id) ?: return
        val descriptor = descriptors[id] ?: return

        try {
            plugin.onDisable()
            plugin.onUnload()
        } catch (e: Exception) {
            log.error("Error disabling plugin '{}'", id, e)
        }

        // Unsubscribe all event handlers owned by this plugin
        plugin.context.events.unsubscribeAll(id)

        // Close classloader for true unloading
        try {
            descriptor.classLoader?.close()
            descriptor.classLoader = null
            descriptor.instance = null
        } catch (e: Exception) {
            log.error("Error closing classloader for plugin '{}'", id, e)
        }

        log.info("Disabled plugin: {}", descriptor.info.name)
    }

    /**
     * Hot-swap: disable and re-enable a plugin (reload from JAR).
     */
    fun reloadPlugin(id: String) {
        log.info("Hot-reloading plugin '{}'...", id)
        disablePlugin(id)
        enablePlugin(id)
    }

    /**
     * Disable all plugins in reverse load order.
     */
    fun disableAll() {
        val reverseOrder = enabled.keys.toList().reversed()
        for (id in reverseOrder) {
            disablePlugin(id)
        }
    }

    // ================================================================
    //  Queries
    // ================================================================

    fun getDescriptors(): Map<String, PluginDescriptor> = descriptors.toMap()
    fun getEnabled(): Map<String, OpenRunePlugin> = enabled.toMap()
    fun isEnabled(id: String): Boolean = id in enabled
    fun getPlugin(id: String): OpenRunePlugin? = enabled[id]
}
