package com.openrune.cache.tools

import com.openrune.cache.io.CacheReader
import com.openrune.cache.io.Container
import com.openrune.cache.io.ReferenceTable
import com.openrune.cache.io.XteaKeyLoader
import com.openrune.cache.def.NpcDefinitionDecoder
import com.openrune.cache.def.ItemDefinitionDecoder
import com.openrune.cache.def.ObjectDefinitionDecoder
import java.nio.file.Path

/**
 * Quick verification tool for the dat2 cache reader.
 *
 * Tests:
 * 1. Open the dat2 cache at ~/.openrune/cache-232/cache/
 * 2. Read the reference table from idx255
 * 3. Decode NPC, Item, and Object definitions
 * 4. Load XTEA keys and attempt map region decompression
 * 5. Print summary statistics
 */
fun main() {
    val cachePath = Path.of(System.getProperty("user.home"), ".openrune", "cache-232", "cache")
    val keysPath = Path.of(System.getProperty("user.home"), ".openrune", "cache-232", "keys.json")

    println("=== dat2 Cache Verification ===")
    println("Cache path: $cachePath")
    println()

    // Step 1: Open cache
    val reader = CacheReader(cachePath)
    if (!reader.open()) {
        println("FAIL: Could not open cache")
        return
    }
    println("OK: Cache opened (isDat2=${reader.isDat2})")

    // Step 2: Read reference tables from idx255
    println()
    println("--- Reference Tables (idx255) ---")
    for (indexId in 0..24) {
        if (!reader.hasIndex(indexId)) continue
        val refRaw = reader.readFile(CacheReader.META_INDEX, indexId)
        if (refRaw != null) {
            val refData = Container.decompress(refRaw)
            if (refData != null) {
                try {
                    val refTable = ReferenceTable.decode(refData)
                    if (refTable != null) {
                        println("  Index $indexId: ${refTable.groupCount} groups")
                    } else {
                        println("  Index $indexId: ref table parse FAILED")
                    }
                } catch (e: Exception) {
                    println("  Index $indexId: EXCEPTION: ${e.javaClass.simpleName}: ${e.message} (data=${refData.size} bytes, first bytes: ${refData.take(10).map { it.toInt() and 0xFF }})")
                }
            } else {
                println("  Index $indexId: container decompress FAILED (${refRaw.size} raw bytes)")
            }
        }
    }

    // Step 3: Decode definitions
    println()
    println("--- Definition Decoding ---")

    // Manually decode first NPC from raw decompressed bytes
    println("  --- Manual NPC decode from raw bytes ---")
    run {
        val archRaw = reader.readFile(2, 9) ?: return@run
        val archData = Container.decompress(archRaw) ?: return@run
        println("  Archive: ${archData.size} bytes, last byte=${archData[archData.size-1].toInt() and 0xFF}")

        // Try treating the data as directly decodable NPC opcode stream
        // with null-terminated strings (OSRS dat2 format)
        val buf = java.nio.ByteBuffer.wrap(archData)
        // Check size table area
        val lastByte = archData[archData.size - 1].toInt() and 0xFF
        val chunks = lastByte
        val fileCount = 14793
        val sizeTableLen = fileCount * chunks * 4 + 1
        val sizeTableStart = archData.size - sizeTableLen
        println("  Size table: chunks=$chunks, sizeTableStart=$sizeTableStart")
        println("  Bytes at sizeTableStart: ${archData.slice(sizeTableStart until sizeTableStart+40).map { it.toInt() and 0xFF }}")
        println("  Bytes before sizeTableStart: ${archData.slice(sizeTableStart-20 until sizeTableStart).map { it.toInt() and 0xFF }}")

        // Manually find NPC boundaries by scanning for opcode 0 terminators
        println("  Scanning for NPC boundaries (first 10):")
        var pos = 0
        for (npcIdx in 0 until 10) {
            val startPos = pos
            // Read opcodes until opcode 0
            while (pos < archData.size) {
                val op = archData[pos].toInt() and 0xFF
                pos++
                if (op == 0) break
                // Skip opcode data
                pos += opcodeDataSize(op, archData, pos)
            }
            println("    NPC $npcIdx: bytes [$startPos, $pos) = ${pos - startPos} bytes")
        }
        // Read ALL deltas and compute sum
        val sizeTableStartPos = archData.size - (14793 * 1 * 4 + 1)
        val sizeBuf = java.nio.ByteBuffer.wrap(archData)
        sizeBuf.position(sizeTableStartPos)
        var sumDirect = 0L
        var sumAccum = 0L
        var accum = 0
        var negCount = 0
        for (i in 0 until 14793) {
            val delta = sizeBuf.int
            accum += delta
            sumDirect += delta.toLong()
            sumAccum += accum.toLong()
            if (delta < 0) negCount++
        }
        println("  Size table analysis:")
        println("    Data area size: $sizeTableStartPos")
        println("    Sum of raw deltas: $sumDirect")
        println("    Sum of accumulated: $sumAccum")
        println("    Negative delta count: $negCount")
        println("    Data area matches sum-of-raw? ${sumDirect == sizeTableStartPos.toLong()}")
        println("    Data area matches sum-of-accum? ${sumAccum == sizeTableStartPos.toLong()}")
    }
    println()

    println("  --- Manual NPC archive inspection ---")
    val npcRefRaw = reader.readFile(CacheReader.META_INDEX, 2)
    if (npcRefRaw != null) {
        val npcRefData = Container.decompress(npcRefRaw)
        if (npcRefData != null) {
            val npcRef = ReferenceTable.decode(npcRefData)
            if (npcRef != null) {
                println("  Config index groups: ${npcRef.groupIds().sorted().take(20)}")
                for (gid in npcRef.groupIds().sorted().take(10)) {
                    val g = npcRef.group(gid)!!
                    println("    Group $gid: ${g.fileCount} files, fileIds[0..4]: ${g.fileIds.take(5).toList()}")
                }
            }
        }
    }

    // Try reading archive 9 (NPCs) raw from index 2
    val npcArchiveRaw = reader.readFile(2, 9)
    if (npcArchiveRaw != null) {
        println("  NPC archive raw: ${npcArchiveRaw.size} bytes")
        println("    First 20 bytes: ${npcArchiveRaw.take(20).map { it.toInt() and 0xFF }}")
        val npcArchiveData = Container.decompress(npcArchiveRaw)
        if (npcArchiveData != null) {
            println("  NPC archive decompressed: ${npcArchiveData.size} bytes")
            println("    First 60 bytes: ${npcArchiveData.take(60).map { it.toInt() and 0xFF }}")
            println("    Last 10 bytes: ${npcArchiveData.takeLast(10).map { it.toInt() and 0xFF }}")
        } else {
            println("  NPC archive: decompress FAILED")
        }
    } else {
        println("  NPC archive: read FAILED from index 2, file 9")
    }

    val npcs = NpcDefinitionDecoder.load(reader)
    println("  NPCs: ${npcs.size} loaded")
    npcs.values.take(5).forEach { println("    ${it.id}: ${it.name} (combat=${it.combatLevel})") }

    val items = ItemDefinitionDecoder.load(reader)
    println("  Items: ${items.size} loaded")
    items.values.take(5).forEach { println("    ${it.id}: ${it.name} (value=${it.value})") }

    val objects = ObjectDefinitionDecoder.load(reader)
    println("  Objects: ${objects.size} loaded")
    objects.values.take(5).forEach { println("    ${it.id}: ${it.name} (${it.width}x${it.length})") }

    // Step 4: XTEA keys
    println()
    println("--- XTEA Keys ---")
    val xteaKeys = XteaKeyLoader.load(keysPath)
    println("  Loaded: ${xteaKeys.size} region keys")

    // Step 5: Try decrypting a map region
    if (xteaKeys.isNotEmpty()) {
        println()
        println("--- Map Region Test ---")
        val mapRefRaw = reader.readFile(CacheReader.META_INDEX, 5)
        if (mapRefRaw != null) {
            val mapRefData = Container.decompress(mapRefRaw)
            if (mapRefData != null) {
                val mapRef = ReferenceTable.decode(mapRefData)
                if (mapRef != null) {
                    println("  Map index: ${mapRef.groupCount} groups")

                    // Try a few known regions
                    val testRegions = listOf(
                        50 to 50,   // Lumbridge area
                        48 to 48,   // Varrock area
                        46 to 51    // Falador area
                    )

                    for ((rx, ry) in testRegions) {
                        val regionKey = (rx shl 8) or ry
                        val xteaKey = xteaKeys[regionKey]
                        val objectName = "l${rx}_${ry}"
                        val groupId = mapRef.findGroupByName(objectName)

                        if (groupId >= 0) {
                            val raw = reader.readFile(5, groupId)
                            if (raw != null) {
                                val data = Container.decompress(raw, xteaKey)
                                if (data != null) {
                                    println("  Region ($rx,$ry): OK, ${data.size} bytes decompressed (groupId=$groupId)")
                                } else {
                                    println("  Region ($rx,$ry): decompress FAILED (key=${xteaKey != null}, groupId=$groupId)")
                                }
                            } else {
                                println("  Region ($rx,$ry): read FAILED (groupId=$groupId)")
                            }
                        } else {
                            println("  Region ($rx,$ry): not in map index")
                        }
                    }
                }
            }
        }
    }

    reader.close()
    println()
    println("=== Verification Complete ===")
}

/** Estimate data bytes consumed by an NPC opcode (null-terminated strings). */
private fun opcodeDataSize(op: Int, data: ByteArray, pos: Int): Int {
    if (pos >= data.size) return 0
    return when (op) {
        1 -> { val c = data[pos].toInt() and 0xFF; 1 + c * 2 }
        2, 3 -> { var i = 0; while (pos + i < data.size && data[pos + i].toInt() != 0) i++; i + 1 }
        5 -> { val c = data[pos].toInt() and 0xFF; 1 + c * 2 }
        6 -> { var i = 0; while (pos + i < data.size && data[pos + i].toInt() != 0) i++; i + 1 }
        12 -> 1
        13, 14 -> 2
        15, 16 -> 2
        17 -> 8
        18 -> 2
        26 -> 4
        28 -> 2
        in 30..39 -> { var i = 0; while (pos + i < data.size && data[pos + i].toInt() != 0) i++; i + 1 }
        40, 41 -> { val c = data[pos].toInt() and 0xFF; 1 + c * 4 }
        59 -> { val c = data[pos].toInt() and 0xFF; 1 + c * 4 }
        60 -> { val c = data[pos].toInt() and 0xFF; 1 + c * 2 }
        61, 62 -> { val c = data[pos].toInt() and 0xFF; 1 + c * 4 }
        93, 99, 107, 109, 111 -> 0
        95, 97, 98, 102, 103, 104, 105 -> 2
        96 -> 2
        100, 101 -> 1
        106 -> { 4 + 1 + ((data[pos + 4].toInt() and 0xFF) + 1) * 2 }
        108, 110, 122, 123, 129, 130, 145, 147 -> 0
        112, 113, 119 -> 1
        114, 116, 124, 126, 146 -> 2
        115, 117 -> 8
        118 -> { 6 + 1 + ((data[pos + 6].toInt() and 0xFF) + 1) * 2 }
        153 -> { val c = data[pos].toInt() and 0xFF; 1 + c * 4 }
        189 -> 2
        249 -> {
            val c = data[pos].toInt() and 0xFF
            var sz = 1
            for (i in 0 until c) {
                val isStr = data[pos + sz].toInt() and 0xFF
                sz += 1 + 3 // type + key
                if (isStr == 1) {
                    while (pos + sz < data.size && data[pos + sz].toInt() != 0) sz++
                    sz++
                } else {
                    sz += 4
                }
            }
            sz
        }
        else -> 0 // unknown — will mess up the scan
    }
}

private fun readNullStr(buf: java.nio.ByteBuffer): String {
    val sb = StringBuilder()
    while (buf.hasRemaining()) {
        val b = buf.get().toInt() and 0xFF
        if (b == 0) break
        sb.append(b.toChar())
    }
    return sb.toString()
}

private fun nameHash(name: String): Int {
    var hash = 0
    for (ch in name.lowercase()) {
        hash = hash * 31 + ch.code
    }
    return hash
}
