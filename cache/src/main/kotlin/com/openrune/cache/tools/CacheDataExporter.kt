package com.openrune.cache.tools

import com.google.gson.GsonBuilder
import com.openrune.cache.def.ItemDefinitionDecoder
import com.openrune.cache.def.NpcDefinitionDecoder
import com.openrune.cache.def.ObjectDefinitionDecoder
import com.openrune.cache.io.CacheReader
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

/**
 * Exports all cache definitions (NPCs, items, objects) to JSON files.
 *
 * Output directory: data/cache-export/
 *   - npcs.json
 *   - items.json
 *   - objects.json
 *
 * Usage: run via `./gradlew :cache:exportCache` or directly.
 */
fun main() {
    val log = LoggerFactory.getLogger("CacheDataExporter")
    val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    val cacheDir = Path.of(System.getProperty("user.home"), ".openrune", "cache")
    val reader = CacheReader(cacheDir)

    if (!reader.open()) {
        log.error("Could not open cache at {}", cacheDir)
        return
    }

    // Resolve output directory relative to working directory
    val outputDir = Path.of("data", "cache-export")
    Files.createDirectories(outputDir)

    try {
        // ── NPCs ──────────────────────────────────────────────────────
        log.info("Loading NPC definitions...")
        val npcs = NpcDefinitionDecoder.load(reader)
        val npcsFile = outputDir.resolve("npcs.json")
        val npcList = npcs.values.sortedBy { it.id }
        Files.writeString(npcsFile, gson.toJson(npcList))
        log.info("Exported {} NPCs to {}", npcList.size, npcsFile)

        // ── Items ─────────────────────────────────────────────────────
        log.info("Loading item definitions...")
        val items = ItemDefinitionDecoder.load(reader)
        val itemsFile = outputDir.resolve("items.json")
        val itemList = items.values.sortedBy { it.id }
        Files.writeString(itemsFile, gson.toJson(itemList))
        log.info("Exported {} items to {}", itemList.size, itemsFile)

        // ── Objects ───────────────────────────────────────────────────
        log.info("Loading object definitions...")
        val objects = ObjectDefinitionDecoder.load(reader)
        val objectsFile = outputDir.resolve("objects.json")
        val objectList = objects.values.sortedBy { it.id }
        Files.writeString(objectsFile, gson.toJson(objectList))
        log.info("Exported {} objects to {}", objectList.size, objectsFile)

        log.info("Cache export complete: {} NPCs, {} items, {} objects",
            npcList.size, itemList.size, objectList.size)
    } finally {
        reader.close()
    }
}
