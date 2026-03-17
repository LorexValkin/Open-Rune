package com.openrune.core.util

import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * Centralized debug system for OpenRune.
 *
 * Usage:
 *   DebugManager.enable(DebugCategory.MOVEMENT)
 *   DebugManager.log(DebugCategory.MOVEMENT, "Player moved to ({}, {})", x, y)
 *
 * Categories can be toggled at runtime via:
 *   - Server config (server-config.json: "debug": ["MOVEMENT", "COLLISION"])
 *   - Launch args (--debug=MOVEMENT,COLLISION)
 *   - In-game command (::debug movement on)
 *
 * All debug output goes through SLF4J at DEBUG level, so it respects
 * the logging framework config (logback.xml) as well.
 */
object DebugManager {

    private val log = LoggerFactory.getLogger("OpenRune-Debug")

    /**
     * Active debug categories. Thread-safe for runtime toggling.
     */
    private val activeCategories = ConcurrentHashMap.newKeySet<DebugCategory>()

    /**
     * Master switch. When false, ALL debug output is suppressed
     * regardless of individual category states.
     */
    @Volatile
    var enabled: Boolean = false
        private set

    // ================================================================
    //  Toggle API
    // ================================================================

    /** Enable the master debug switch. */
    fun enableAll() {
        enabled = true
        log.info("[Debug] Master debug ENABLED")
    }

    /** Disable the master debug switch. All output suppressed. */
    fun disableAll() {
        enabled = false
        log.info("[Debug] Master debug DISABLED")
    }

    /** Enable a specific debug category. Also enables master switch. */
    fun enable(category: DebugCategory) {
        enabled = true
        activeCategories.add(category)
        log.info("[Debug] Category {} ENABLED", category.name)
    }

    /** Disable a specific debug category. */
    fun disable(category: DebugCategory) {
        activeCategories.remove(category)
        log.info("[Debug] Category {} DISABLED", category.name)
    }

    /** Toggle a category on/off. Returns new state. */
    fun toggle(category: DebugCategory): Boolean {
        return if (activeCategories.contains(category)) {
            disable(category)
            false
        } else {
            enable(category)
            true
        }
    }

    /** Check if a category is currently active. */
    fun isActive(category: DebugCategory): Boolean =
        enabled && activeCategories.contains(category)

    /** Get all currently active categories. */
    fun activeList(): Set<DebugCategory> = activeCategories.toSet()

    // ================================================================
    //  Logging API
    // ================================================================

    /** Log a debug message if the category is active. */
    fun log(category: DebugCategory, message: String) {
        if (!isActive(category)) return
        log.debug("[{}] {}", category.tag, message)
    }

    /** Log a debug message with format args if the category is active. */
    fun log(category: DebugCategory, format: String, vararg args: Any?) {
        if (!isActive(category)) return
        log.debug("[{}] $format", category.tag, *args)
    }

    // ================================================================
    //  Initialization from config/args
    // ================================================================

    /**
     * Initialize from launch arguments.
     * Accepts: --debug=MOVEMENT,COLLISION,PACKETS
     * Or: --debug=ALL (enables everything)
     */
    fun initFromArgs(args: Array<String>) {
        val debugArg = args.firstOrNull { it.startsWith("--debug=") } ?: return
        val categories = debugArg.removePrefix("--debug=").uppercase().split(",")

        if ("ALL" in categories) {
            enableAll()
            DebugCategory.entries.forEach { activeCategories.add(it) }
            log.info("[Debug] ALL categories enabled via launch args")
            return
        }

        for (name in categories) {
            try {
                val cat = DebugCategory.valueOf(name.trim())
                enable(cat)
            } catch (e: IllegalArgumentException) {
                log.warn("[Debug] Unknown category in launch args: {}", name)
            }
        }
    }

    /**
     * Initialize from a list of category names (e.g. from JSON config).
     */
    fun initFromConfig(categoryNames: List<String>) {
        if (categoryNames.isEmpty()) return

        for (name in categoryNames) {
            val upper = name.uppercase().trim()
            if (upper == "ALL") {
                enableAll()
                DebugCategory.entries.forEach { activeCategories.add(it) }
                return
            }
            try {
                enable(DebugCategory.valueOf(upper))
            } catch (e: IllegalArgumentException) {
                log.warn("[Debug] Unknown category in config: {}", name)
            }
        }
    }

    /** Summary string for server startup banner. */
    fun statusSummary(): String {
        if (!enabled || activeCategories.isEmpty()) return "disabled"
        return activeCategories.joinToString(", ") { it.name.lowercase() }
    }
}

/**
 * Debug categories. Each represents a subsystem that can have its
 * debug output independently toggled.
 *
 * Add new categories here as the server grows.
 */
enum class DebugCategory(val tag: String, val description: String) {

    // Movement & Pathfinding
    MOVEMENT    ("MOVE",      "Player/NPC step-by-step movement"),
    COLLISION   ("COLL",      "Collision checks and flag queries"),
    PATHFINDING ("PATH",      "A* pathfinder searches"),

    // Networking
    PACKETS     ("PKT",       "Incoming/outgoing packet data"),
    LOGIN       ("LOGIN",     "Login handshake and session creation"),

    // World
    REGIONS     ("REGION",    "Region loading and map data"),
    OBJECTS     ("OBJ",       "Object spawns, doors, interactions"),
    NPC_AI      ("NPC-AI",    "NPC random walk, combat AI, targeting"),
    SPAWNS      ("SPAWN",     "NPC/item spawn and despawn"),

    // Cache
    CACHE       ("CACHE",     "Cache reading, BZip2, archive parsing"),

    // Combat
    COMBAT      ("COMBAT",    "Damage calculation, hit processing"),
    PROJECTILES ("PROJ",      "Projectile creation and tracking"),

    // Systems
    PLUGINS     ("PLUGIN",    "Plugin loading, events, hot-reload"),
    TASKS       ("TASK",      "Scheduled task execution"),
    SAVES       ("SAVE",      "Player save/load operations"),

    // General
    COMMANDS    ("CMD",       "Player command processing"),
    EVENTS      ("EVENT",     "Event bus dispatch and handling")
}