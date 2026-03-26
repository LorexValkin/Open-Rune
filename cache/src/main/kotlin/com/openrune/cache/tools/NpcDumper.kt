package com.openrune.cache.tools

import com.openrune.cache.def.NpcDefinitionDecoder
import com.openrune.cache.io.CacheReader
import java.io.File
import java.nio.file.Path

/**
 * Dumps all NPC definitions from the cache to a text file.
 * Usage: run via Gradle or directly — outputs to Desktop.
 */
fun main() {
    val cacheDir = Path.of(System.getProperty("user.home"), ".openrune", "cache")
    val reader = CacheReader(cacheDir)

    if (!reader.open()) {
        println("ERROR: Could not open cache at $cacheDir")
        return
    }

    val defs = NpcDefinitionDecoder.load(reader)
    // Keep reader open for raw byte analysis below
    val reader2 = CacheReader(cacheDir)
    reader2.open()
    reader.close()

    val outputFile = File(System.getProperty("user.home"), "Desktop/npc-cache-dump.txt")
    outputFile.parentFile.mkdirs()

    outputFile.bufferedWriter().use { w ->
        w.write("ID\tName\tCombat\tSize\tStandAnim\tWalkAnim\tActions\tModels\n")
        w.write("=" .repeat(120) + "\n")

        for (id in 0..8644) {
            val def = defs[id] ?: continue
            val actions = def.actions.filterNotNull().joinToString(", ")
            val models = def.models.joinToString(", ")
            w.write("$id\t${def.name}\t${def.combatLevel}\t${def.size}\t${def.standAnim}\t${def.walkAnim}\t[$actions]\t[$models]\n")
        }
    }

    println("Dumped ${defs.size} NPC definitions to ${outputFile.absolutePath}")

    // Print fishing-relevant IDs specifically
    println("\n=== Stock 317 Fishing Spot IDs (233-334, 1174-1175) ===")
    val fishIds317 = (233..334).toList() + listOf(1174, 1175)
    for (id in fishIds317) {
        val def = defs[id]
        if (def != null) {
            val actions = def.actions.filterNotNull().joinToString(", ")
            println("  NPC $id: ${def.name} (combat=${def.combatLevel}, models=[${def.models.joinToString(",")}], actions=[$actions])")
        } else {
            println("  NPC $id: <not decoded>")
        }
    }

    println("\n=== Project51 OSRS Fishing Spot IDs ===")
    val fishIdsOsrs = listOf(3913, 3417, 3657, 1520, 1524, 6825, 7676, 635, 4712, 3317)
    for (id in fishIdsOsrs) {
        val def = defs[id]
        if (def != null) {
            val actions = def.actions.filterNotNull().joinToString(", ")
            println("  NPC $id: ${def.name} (combat=${def.combatLevel}, models=[${def.models.joinToString(",")}], actions=[$actions])")
        } else {
            println("  NPC $id: <not decoded>")
        }
    }

    // Search for anything with "fish" in the name
    println("\n=== All NPCs with 'fish' or 'spot' in name ===")
    for ((id, def) in defs.entries.sortedBy { it.key }) {
        val name = def.name.lowercase()
        if (name.contains("fish") || name.contains("spot") || name.contains("net") || name.contains("harpoon") || name.contains("rod")) {
            val actions = def.actions.filterNotNull().joinToString(", ")
            println("  NPC $id: ${def.name} (combat=${def.combatLevel}, actions=[$actions])")
        }
    }

    // Hex dump first bytes of a few undecoded fishing spot IDs to identify mystery opcodes
    println("\n=== Raw byte analysis of undecoded NPCs ===")
    val archiveData = reader2.readFile(0, 2) ?: return
    val archive = com.openrune.cache.io.ArchiveReader(archiveData)
    val npcDatBytes = archive.getEntry("npc.dat") ?: return
    val npcIdxBytes = archive.getEntry("npc.idx") ?: return

    val idxBuf = java.nio.ByteBuffer.wrap(npcIdxBytes)
    val totalNpcs2 = idxBuf.short.toInt() and 0xFFFF
    val offsets2 = IntArray(totalNpcs2)
    val sizes2 = IntArray(totalNpcs2)
    var off2 = 0
    for (i in 0 until totalNpcs2) {
        offsets2[i] = off2
        val sz = idxBuf.short.toInt() and 0xFFFF
        sizes2[i] = sz
        off2 += sz
    }

    // Dump first 20 bytes of some sample undecoded NPCs
    val sampleIds = listOf(316, 320, 233, 312, 313, 1174, 3913, 3657, 1520)
    for (sId in sampleIds) {
        if (sId >= totalNpcs2) { println("  NPC $sId: beyond cache range ($totalNpcs2)"); continue }
        val start = offsets2[sId]
        val sz = sizes2[sId]
        val end = minOf(start + sz, npcDatBytes.size)
        val bytes = npcDatBytes.sliceArray(start until minOf(start + 30, end))
        val hex = bytes.joinToString(" ") { "%02X".format(it) }
        println("  NPC $sId: blockSize=$sz, first bytes: $hex")
    }
}
