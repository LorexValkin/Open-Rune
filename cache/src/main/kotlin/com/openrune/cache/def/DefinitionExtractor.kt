package com.openrune.cache.def

import com.google.gson.GsonBuilder
import com.openrune.cache.io.CacheReader
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path

/**
 * Extracts definitions from the 317 cache and exports them as
 * editable JSON files for the data/ directory.
 *
 * Usage:
 *   val extractor = DefinitionExtractor(cacheReader)
 *   extractor.extractItems(Path.of("data/items/items-cache.json"))
 *   extractor.extractNpcs(Path.of("data/npcs/npcs-cache.json"))
 *
 * This bridges the binary cache format into the JSON data system,
 * giving you a complete starting dataset that you can then customize.
 */
class DefinitionExtractor(private val cache: CacheReader) {

    private val log = LoggerFactory.getLogger(DefinitionExtractor::class.java)
    private val gson = GsonBuilder().setPrettyPrinting().create()

    // ================================================================
    //  Item Definitions
    // ================================================================

    data class CacheItemDef(
        val id: Int,
        val name: String = "null",
        val examine: String = "",
        val value: Int = 0,
        val stackable: Boolean = false,
        val noted: Boolean = false,
        val noteId: Int = -1,
        val members: Boolean = false,
        val equipSlot: Int = -1
    )

    /**
     * Extract item definitions from cache index 2 (config archive)
     * and write as JSON.
     */
    fun extractItems(outputPath: Path): Int {
        // In a 317 cache, item defs are in the "config" archive (idx0, file 2)
        // They use a specific binary format with opcodes
        val archiveData = cache.readFile(0, 2) ?: run {
            log.warn("Could not read config archive for item definitions")
            return 0
        }

        val items = mutableListOf<CacheItemDef>()
        val buf = ByteBuffer.wrap(archiveData)

        // The config archive is a gzip'd container; actual parsing would need
        // the archive unpacker. For now, this is a placeholder showing the structure.
        // In production, you'd implement the full archive + item def decoder.

        log.info("Item definition extraction requires archive decoder (placeholder)")

        if (items.isNotEmpty()) {
            Files.createDirectories(outputPath.parent)
            Files.writeString(outputPath, gson.toJson(items))
            log.info("Exported {} item definitions to {}", items.size, outputPath)
        }

        return items.size
    }

    // ================================================================
    //  NPC Definitions
    // ================================================================

    data class CacheNpcDef(
        val id: Int,
        val name: String = "null",
        val examine: String = "",
        val combatLevel: Int = 0,
        val size: Int = 1,
        val standAnim: Int = -1,
        val walkAnim: Int = -1
    )

    fun extractNpcs(outputPath: Path): Int {
        log.info("NPC definition extraction requires archive decoder (placeholder)")
        return 0
    }

    // ================================================================
    //  Object Definitions
    // ================================================================

    data class CacheObjectDef(
        val id: Int,
        val name: String = "null",
        val width: Int = 1,
        val height: Int = 1,
        val solid: Boolean = true,
        val interactable: Boolean = false,
        val actions: List<String> = emptyList()
    )

    fun extractObjects(outputPath: Path): Int {
        log.info("Object definition extraction requires archive decoder (placeholder)")
        return 0
    }

    /**
     * Extract all definitions to the data directory.
     */
    fun extractAll(dataDir: Path) {
        log.info("Extracting cache definitions to {}", dataDir)
        extractItems(dataDir.resolve("items").resolve("items-cache.json"))
        extractNpcs(dataDir.resolve("npcs").resolve("npcs-cache.json"))
        extractObjects(dataDir.resolve("objects").resolve("objects-cache.json"))
        log.info("Cache extraction complete")
    }
}
