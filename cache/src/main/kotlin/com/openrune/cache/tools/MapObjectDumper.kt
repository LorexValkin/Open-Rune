package com.openrune.cache.tools

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.openrune.cache.def.MapObject
import com.openrune.cache.def.MapObjectDecoder
import com.openrune.cache.io.CacheReader
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Dumps all map object placements from the cache, then cross-references with
 * object definitions to identify and export door objects.
 *
 * Outputs:
 *   - data/cache-export/doors.json   — all door objects with rotation info
 *   - ~/Desktop/map-objects-report.txt — summary statistics
 *
 * Usage: ./gradlew :cache:dumpMapObjects
 */
fun main() {
    val log = LoggerFactory.getLogger("MapObjectDumper")
    val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    val cacheDir = Path.of(System.getProperty("user.home"), ".openrune", "cache")
    val reader = CacheReader(cacheDir)

    if (!reader.open()) {
        log.error("Could not open cache at {}", cacheDir)
        return
    }

    // ── Step 1: Load all map objects ────────────────────────────────
    log.info("Loading map objects from cache...")
    val allObjects = MapObjectDecoder.loadAll(reader)
    reader.close()

    if (allObjects.isEmpty()) {
        log.error("No map objects decoded — check cache at {}", cacheDir)
        return
    }
    log.info("Total map objects loaded: {}", allObjects.size)

    // ── Step 2: Load object definitions for name/action lookup ─────
    val objectDefs = loadObjectDefinitions()
    log.info("Object definitions loaded: {}", objectDefs.size)

    // ── Step 3: Identify door objects ──────────────────────────────
    // A door is any object whose definition has "Open" or "Close" as
    // one of its actions.
    val doorDefIds = objectDefs
        .filter { (_, def) -> isDoorDefinition(def) }
        .keys

    log.info("Door definition IDs found: {}", doorDefIds.size)

    val doorObjects = allObjects.filter { it.id in doorDefIds }
    log.info("Door placements found: {}", doorObjects.size)

    // ── Step 4: Export doors.json ──────────────────────────────────
    val outputDir = Path.of("data", "cache-export")
    Files.createDirectories(outputDir)

    val doorsJson = buildDoorsJson(doorObjects, objectDefs)
    val doorsFile = outputDir.resolve("doors.json")
    Files.writeString(doorsFile, gson.toJson(doorsJson))
    log.info("Exported {} doors to {}", doorObjects.size, doorsFile)

    // ── Step 5: Write summary report ───────────────────────────────
    val reportFile = File(System.getProperty("user.home"), "Desktop/map-objects-report.txt")
    reportFile.parentFile.mkdirs()
    writeReport(reportFile, allObjects, doorObjects, objectDefs, doorDefIds)
    log.info("Summary report written to {}", reportFile.absolutePath)
}

/**
 * Load object definitions from the exported objects.json.
 * Returns a map of object ID to a parsed JSON object containing
 * name and actions.
 */
private fun loadObjectDefinitions(): Map<Int, JsonObject> {
    val objectsPath = Path.of("data", "cache-export", "objects.json")
    if (!Files.exists(objectsPath)) {
        // Try relative to cache module
        val altPath = Path.of("cache", "data", "cache-export", "objects.json")
        if (!Files.exists(altPath)) {
            println("WARNING: objects.json not found at $objectsPath or $altPath")
            println("  Run './gradlew :cache:exportCache' first to generate it.")
            return emptyMap()
        }
        return parseObjectsFile(altPath)
    }
    return parseObjectsFile(objectsPath)
}

private fun parseObjectsFile(path: Path): Map<Int, JsonObject> {
    val json = Files.readString(path)
    val array = JsonParser.parseString(json).asJsonArray
    val map = mutableMapOf<Int, JsonObject>()
    for (element in array) {
        val obj = element.asJsonObject
        val id = obj.get("id").asInt
        map[id] = obj
    }
    return map
}

/**
 * Check if an object definition represents a door (has "Open" or "Close" action).
 */
private fun isDoorDefinition(def: JsonObject): Boolean {
    val actions = def.getAsJsonArray("actions") ?: return false
    for (action in actions) {
        if (action.isJsonNull) continue
        val actionStr = action.asString
        if (actionStr.equals("Open", ignoreCase = true) ||
            actionStr.equals("Close", ignoreCase = true)) {
            return true
        }
    }
    return false
}

/**
 * Find the first door-related action ("Open" or "Close") from a definition.
 */
private fun findDoorAction(def: JsonObject): String {
    val actions = def.getAsJsonArray("actions") ?: return "Unknown"
    for (action in actions) {
        if (action.isJsonNull) continue
        val actionStr = action.asString
        if (actionStr.equals("Open", ignoreCase = true) ||
            actionStr.equals("Close", ignoreCase = true)) {
            return actionStr
        }
    }
    return "Unknown"
}

private fun getObjectName(def: JsonObject): String {
    return def.get("name")?.asString ?: "null"
}

/**
 * Build the doors JSON array:
 * [{id, name, x, y, z, rotation, action}, ...]
 */
private fun buildDoorsJson(doors: List<MapObject>, defs: Map<Int, JsonObject>): JsonArray {
    val array = JsonArray()
    for (door in doors) {
        val def = defs[door.id]
        val entry = JsonObject()
        entry.addProperty("id", door.id)
        entry.addProperty("name", def?.let { getObjectName(it) } ?: "Unknown")
        entry.addProperty("x", door.x)
        entry.addProperty("y", door.y)
        entry.addProperty("z", door.z)
        entry.addProperty("type", door.type)
        entry.addProperty("rotation", door.rotation)
        entry.addProperty("action", def?.let { findDoorAction(it) } ?: "Unknown")
        array.add(entry)
    }
    return array
}

/**
 * Write a human-readable summary report.
 */
private fun writeReport(
    file: File,
    allObjects: List<MapObject>,
    doorObjects: List<MapObject>,
    objectDefs: Map<Int, JsonObject>,
    doorDefIds: Set<Int>
) {
    val rotationNames = arrayOf("North", "East", "South", "West")

    file.bufferedWriter().use { w ->
        w.write("========================================\n")
        w.write("  Map Objects Report\n")
        w.write("========================================\n\n")

        w.write("Total object placements: ${allObjects.size}\n")
        w.write("Unique object IDs used:  ${allObjects.map { it.id }.distinct().size}\n")
        w.write("Object definitions with Open/Close action: ${doorDefIds.size}\n")
        w.write("Total door placements: ${doorObjects.size}\n\n")

        // Breakdown by object type
        w.write("── Object Type Breakdown ──\n")
        val typeGroups = allObjects.groupBy { it.type }
        for (type in 0..22) {
            val count = typeGroups[type]?.size ?: 0
            if (count > 0) {
                val label = objectTypeLabel(type)
                w.write("  Type %2d %-22s : %,d\n".format(type, "($label)", count))
            }
        }
        w.write("\n")

        // Breakdown by height level
        w.write("── Height Level Breakdown ──\n")
        val heightGroups = allObjects.groupBy { it.z }
        for (z in 0..3) {
            val count = heightGroups[z]?.size ?: 0
            w.write("  Level $z: %,d objects\n".format(count))
        }
        w.write("\n")

        // Door rotation breakdown
        w.write("── Door Rotation Breakdown ──\n")
        val rotationGroups = doorObjects.groupBy { it.rotation }
        for (r in 0..3) {
            val count = rotationGroups[r]?.size ?: 0
            w.write("  Rotation $r (${rotationNames[r]}): %,d doors\n".format(count))
        }
        w.write("\n")

        // Top 20 most common door definitions
        w.write("── Top 20 Most Common Doors ──\n")
        val doorCounts = doorObjects.groupBy { it.id }
            .map { (id, list) -> id to list.size }
            .sortedByDescending { it.second }
            .take(20)

        for ((id, count) in doorCounts) {
            val def = objectDefs[id]
            val name = def?.let { getObjectName(it) } ?: "Unknown"
            val action = def?.let { findDoorAction(it) } ?: "?"
            w.write("  ID %5d  %-25s  action=%-6s  placements=%,d\n".format(id, name, action, count))
        }
        w.write("\n")

        w.write("========================================\n")
        w.write("  End of Report\n")
        w.write("========================================\n")
    }
}

private fun objectTypeLabel(type: Int): String = when (type) {
    0 -> "Wall"
    1 -> "Wall diagonal"
    2 -> "Wall corner"
    3 -> "Wall unfinished"
    4 -> "Wall decoration"
    in 5..8 -> "Corner wall"
    9 -> "Diagonal wall"
    10 -> "Interactive object"
    11 -> "Interactive object"
    in 12..21 -> "Ground decoration"
    22 -> "Floor decoration"
    else -> "Unknown"
}
