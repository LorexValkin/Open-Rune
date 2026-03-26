package com.openrune.cache.tools

import com.openrune.cache.def.NpcDefinitionDecoder
import com.openrune.cache.io.CacheReader
import com.google.gson.JsonParser
import java.io.File
import java.nio.file.Path

/**
 * Audits spawns.json against the cache NPC definitions.
 * Flags spawns where the description doesn't match the cache name,
 * or where the NPC ID doesn't exist / failed to decode.
 *
 * Output: Desktop/spawn-audit.txt
 */
fun main() {
    val projectDir = Path.of("C:\\Users\\User\\IdeaProjects\\Open-Rune")
    val cacheDir = Path.of(System.getProperty("user.home"), ".openrune", "cache")

    // Load cache NPC definitions
    val reader = CacheReader(cacheDir)
    if (!reader.open()) { println("ERROR: Could not open cache"); return }
    val cacheDefs = NpcDefinitionDecoder.load(reader)
    reader.close()

    // Load spawns.json
    val spawnsFile = projectDir.resolve("data/spawns/spawns.json").toFile()
    val spawns = JsonParser.parseReader(spawnsFile.reader()).asJsonArray

    val output = File(System.getProperty("user.home"), "Desktop/spawn-audit.txt")
    output.parentFile.mkdirs()

    // Categories
    val missing = mutableListOf<String>()       // NPC ID not in cache at all
    val mismatch = mutableListOf<String>()      // Cache name doesn't match description
    val matched = mutableListOf<String>()       // Looks correct
    val noDescription = mutableListOf<String>()  // Spawn has no meaningful description

    // Track unique NPC IDs and their issues
    val npcIdIssues = mutableMapOf<Int, String>() // npcId -> cache name or "MISSING"
    val npcIdCounts = mutableMapOf<Int, Int>()    // npcId -> spawn count

    for (element in spawns) {
        val spawn = element.asJsonObject
        val spawnId = spawn.get("id")?.asInt ?: -1
        val npcId = spawn.get("npcId")?.asInt ?: continue
        val desc = spawn.get("description")?.asString ?: ""
        val x = spawn.get("x")?.asInt ?: 0
        val y = spawn.get("y")?.asInt ?: 0

        npcIdCounts[npcId] = (npcIdCounts[npcId] ?: 0) + 1

        val cacheDef = cacheDefs[npcId]

        if (cacheDef == null) {
            missing.add("SPAWN #$spawnId | NPC $npcId | ($x,$y) | desc=\"$desc\" | CACHE: <not decoded>")
            npcIdIssues.putIfAbsent(npcId, "NOT_IN_CACHE")
            continue
        }

        val cacheName = cacheDef.name
        val cacheActions = cacheDef.actions.filterNotNull().joinToString(", ")

        if (desc.isBlank()) {
            noDescription.add("SPAWN #$spawnId | NPC $npcId | ($x,$y) | cache=\"$cacheName\" [$cacheActions]")
            continue
        }

        // Fuzzy match: check if cache name appears in description or vice versa
        val descLower = desc.lowercase()
        val cacheNameLower = cacheName.lowercase()
        val isMatch = cacheNameLower in descLower
                || descLower.split(" ").any { it.length > 3 && it in cacheNameLower }
                || descLower.contains(cacheNameLower.split(" ").firstOrNull() ?: "~~~")

        if (isMatch) {
            matched.add("SPAWN #$spawnId | NPC $npcId | ($x,$y) | desc=\"$desc\" | cache=\"$cacheName\" [$cacheActions]")
        } else {
            mismatch.add("SPAWN #$spawnId | NPC $npcId | ($x,$y) | desc=\"$desc\" | cache=\"$cacheName\" [$cacheActions]")
            npcIdIssues.putIfAbsent(npcId, cacheName)
        }
    }

    // Pre-compute unique ID lists for both file and console output
    val uniqueMismatches = mismatch.map { line ->
        Regex("NPC (\\d+)").find(line)?.groupValues?.get(1)?.toInt() ?: -1
    }.distinct().sorted()

    val uniqueMissing = missing.map { line ->
        Regex("NPC (\\d+)").find(line)?.groupValues?.get(1)?.toInt() ?: -1
    }.distinct().sorted()

    output.bufferedWriter().use { w ->
        w.write("OpenRune Spawn Audit Report\n")
        w.write("Cache: ${cacheDefs.size} NPC definitions loaded\n")
        w.write("Spawns: ${spawns.size()} total entries\n")
        w.write("=" .repeat(100) + "\n\n")

        w.write("SUMMARY\n")
        w.write("-".repeat(50) + "\n")
        w.write("  Matched (description ~ cache name):  ${matched.size}\n")
        w.write("  MISMATCH (description ≠ cache name): ${mismatch.size}\n")
        w.write("  MISSING (NPC not in cache):           ${missing.size}\n")
        w.write("  No description:                       ${noDescription.size}\n")
        w.write("\n")

        w.write("UNIQUE MISMATCHED NPC IDs (${uniqueMismatches.size} unique NPCs)\n")
        w.write("-".repeat(80) + "\n")
        for (npcId in uniqueMismatches) {
            val cacheName = npcIdIssues[npcId] ?: "?"
            val count = npcIdCounts[npcId] ?: 0
            w.write("  NPC $npcId: cache=\"$cacheName\" (${count} spawns)\n")
        }
        w.write("\n")

        w.write("UNIQUE MISSING NPC IDs (${uniqueMissing.size} unique NPCs)\n")
        w.write("-".repeat(80) + "\n")
        for (npcId in uniqueMissing) {
            val count = npcIdCounts[npcId] ?: 0
            w.write("  NPC $npcId: <not in cache> (${count} spawns)\n")
        }
        w.write("\n")

        w.write("=" .repeat(100) + "\n")
        w.write("DETAILED MISMATCHES\n")
        w.write("=" .repeat(100) + "\n\n")
        for (line in mismatch) w.write("$line\n")

        w.write("\n" + "=" .repeat(100) + "\n")
        w.write("DETAILED MISSING\n")
        w.write("=" .repeat(100) + "\n\n")
        for (line in missing) w.write("$line\n")

        w.write("\n" + "=" .repeat(100) + "\n")
        w.write("NO DESCRIPTION\n")
        w.write("=" .repeat(100) + "\n\n")
        for (line in noDescription) w.write("$line\n")

        w.write("\n" + "=" .repeat(100) + "\n")
        w.write("MATCHED (OK)\n")
        w.write("=" .repeat(100) + "\n\n")
        for (line in matched) w.write("$line\n")
    }

    println("Spawn Audit Complete")
    println("  Total spawns:   ${spawns.size()}")
    println("  Matched:        ${matched.size}")
    println("  MISMATCH:       ${mismatch.size}")
    println("  MISSING:        ${missing.size}")
    println("  No description: ${noDescription.size}")
    println("\nUnique mismatched NPCs: ${uniqueMismatches.size}")
    println("Unique missing NPCs:    ${uniqueMissing.size}")
    println("\nFull report: ${output.absolutePath}")
}
