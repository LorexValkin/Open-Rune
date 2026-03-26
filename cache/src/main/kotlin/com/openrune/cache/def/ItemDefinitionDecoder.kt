package com.openrune.cache.def

import com.openrune.cache.io.ArchiveReader
import com.openrune.cache.io.CacheReader
import com.openrune.cache.io.Container
import com.openrune.cache.io.ReferenceTable
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

/**
 * Decodes item definitions from the cache.
 *
 * **317 format:** Config archive (idx0 file 2), jag archive with
 * "obj.dat" + "obj.idx".
 *
 * **dat2 format:** Index 2 (configs), group 10 (items). Each item is
 * a separate file within the group container.
 *
 * Opcodes match the client's ItemDefinition.readValues().
 * Supports stock 317 + OSRS-era opcodes (rev 194-232+).
 */
object ItemDefinitionDecoder {

    private val log = LoggerFactory.getLogger(ItemDefinitionDecoder::class.java)

    private const val CONFIG_INDEX = 2
    private const val ITEM_ARCHIVE = 10

    fun load(cacheReader: CacheReader): Map<Int, CacheItemDefinition> {
        return if (cacheReader.isDat2) {
            loadDat2(cacheReader)
        } else {
            load317(cacheReader)
        }
    }

    private fun loadDat2(cacheReader: CacheReader): Map<Int, CacheItemDefinition> {
        val refRaw = cacheReader.readFile(CacheReader.META_INDEX, CONFIG_INDEX)
        if (refRaw == null) {
            log.error("Could not read config reference table from idx255")
            return emptyMap()
        }
        val refData = Container.decompress(refRaw)
        if (refData == null) {
            log.error("Could not decompress config reference table")
            return emptyMap()
        }
        val refTable = ReferenceTable.decode(refData)
        if (refTable == null) {
            log.error("Could not parse config reference table")
            return emptyMap()
        }

        val itemGroup = refTable.group(ITEM_ARCHIVE)
        if (itemGroup == null) {
            log.error("Item group ({}) not found in config reference table", ITEM_ARCHIVE)
            return emptyMap()
        }

        log.info("dat2: Item group has {} files", itemGroup.fileCount)

        val archiveRaw = cacheReader.readFile(CONFIG_INDEX, ITEM_ARCHIVE)
        if (archiveRaw == null) {
            log.error("Could not read item archive from index {}", CONFIG_INDEX)
            return emptyMap()
        }
        val archiveData = Container.decompress(archiveRaw)
        if (archiveData == null) {
            log.error("Could not decompress item archive")
            return emptyMap()
        }

        val definitions = mutableMapOf<Int, CacheItemDefinition>()
        val fileIds = itemGroup.fileIds

        if (fileIds.size == 1) {
            val result = decodeItemDat2(fileIds[0], archiveData)
            definitions[fileIds[0]] = result
        } else {
            val files = splitGroupFiles(archiveData, fileIds.size)
            if (files != null) {
                var decodeErrors = 0
                for (i in fileIds.indices) {
                    if (i < files.size && files[i].isNotEmpty()) {
                        try {
                            val result = decodeItemDat2(fileIds[i], files[i])
                            definitions[fileIds[i]] = result
                        } catch (_: Exception) { decodeErrors++ }
                    }
                }
                if (decodeErrors > 0) log.warn("dat2: {} item decode errors", decodeErrors)
            } else {
                log.warn("Failed to split item archive into {} files", fileIds.size)
            }
        }

        log.info("Loaded {} item definitions from dat2 cache ({} file IDs)", definitions.size, fileIds.size)
        return definitions
    }

    private fun splitGroupFiles(data: ByteArray, fileCount: Int): List<ByteArray>? {
        if (data.isEmpty() || fileCount == 0) return null
        val chunks = data[data.size - 1].toInt() and 0xFF
        if (chunks == 0) return null
        val sizeTableLength = fileCount * chunks * 4 + 1
        if (sizeTableLength > data.size) return null
        val sizeTableStart = data.size - sizeTableLength

        val totalFileSizes = IntArray(fileCount)
        val buf = ByteBuffer.wrap(data)
        buf.position(sizeTableStart)
        for (chunk in 0 until chunks) {
            var chunkSize = 0
            for (file in 0 until fileCount) {
                chunkSize += buf.int
                totalFileSizes[file] += chunkSize
            }
        }

        val files = Array(fileCount) { java.io.ByteArrayOutputStream(totalFileSizes[it]) }
        buf.position(sizeTableStart)
        var dataOffset = 0
        for (chunk in 0 until chunks) {
            var chunkSize = 0
            for (file in 0 until fileCount) {
                chunkSize += buf.int
                if (chunkSize > 0 && dataOffset + chunkSize <= sizeTableStart) {
                    files[file].write(data, dataOffset, chunkSize)
                }
                dataOffset += chunkSize
            }
        }
        return files.map { it.toByteArray() }
    }

    private fun decodeItemDat2(id: Int, data: ByteArray): CacheItemDefinition {
        val buf = ByteBuffer.wrap(data)
        return decodeItem(id, buf, data.size, null)
    }

    // ─── Legacy 317 loading ─────────────────────────────────────────

    private fun load317(cacheReader: CacheReader): Map<Int, CacheItemDefinition> {
        val archiveData = cacheReader.readFile(0, 2)
        if (archiveData == null) {
            log.error("Could not read config archive (idx0 file 2)")
            return emptyMap()
        }

        val archive = try {
            ArchiveReader(archiveData)
        } catch (e: Exception) {
            log.error("Failed to unpack config archive: {}", e.message)
            return emptyMap()
        }

        val objDatBytes = archive.getEntry("obj.dat")
        val objIdxBytes = archive.getEntry("obj.idx")

        if (objDatBytes == null || objIdxBytes == null) {
            log.error("obj.dat or obj.idx not found in config archive")
            return emptyMap()
        }

        val idxBuf = ByteBuffer.wrap(objIdxBytes)
        val totalItems = idxBuf.short.toInt() and 0xFFFF

        val offsets = IntArray(totalItems)
        val blockSizes = IntArray(totalItems)
        var datOffset = 2
        for (i in 0 until totalItems) {
            offsets[i] = datOffset
            val sz = idxBuf.short.toInt() and 0xFFFF
            blockSizes[i] = sz
            datOffset += sz
        }

        val definitions = mutableMapOf<Int, CacheItemDefinition>()
        val datBuf = ByteBuffer.wrap(objDatBytes)
        val unknownOpcodeHits = mutableMapOf<Int, Int>()

        for (id in 0 until totalItems) {
            datBuf.position(offsets[id])
            val blockEnd = offsets[id] + blockSizes[id]
            val result = decodeItem(id, datBuf, blockEnd, unknownOpcodeHits)
            if (result.name != "null") {
                definitions[id] = result
            }
        }

        if (unknownOpcodeHits.isNotEmpty()) {
            val sorted = unknownOpcodeHits.entries.sortedByDescending { it.value }
            val top10 = sorted.take(10).joinToString(", ") { "op${it.key}(x${it.value})" }
            log.warn("Unknown item opcodes — top hits: {}", top10)
        }

        log.info("Loaded {} item definitions from 317 cache ({} total entries)", definitions.size, totalItems)
        return definitions
    }

    // ─── Shared opcode decoder ──────────────────────────────────────

    private fun decodeItem(
        id: Int,
        buf: ByteBuffer,
        blockEnd: Int,
        unknownOpcodeHits: MutableMap<Int, Int>?
    ): CacheItemDefinition {
        var name = "null"
        var examine = ""
        var value = 0
        var stackable = false
        var noted = false
        var noteId = -1
        var notedTemplateId = -1
        var members = false
        var equipSlot = -1
        val equipActions = arrayOfNulls<String>(5)
        val groundActions = arrayOfNulls<String>(5)
        val interfaceActions = arrayOfNulls<String>(5)

        while (buf.position() < blockEnd) {
            val opcode = buf.get().toInt() and 0xFF
            if (opcode == 0) break

            when (opcode) {
                1 -> { buf.short }
                2 -> name = readString(buf, blockEnd)
                3 -> examine = readString(buf, blockEnd)
                4 -> { buf.short }
                5 -> { buf.short }
                6 -> { buf.short }
                7 -> { buf.short }
                8 -> { buf.short }
                11 -> stackable = true
                12 -> value = buf.int
                16 -> members = true
                23 -> { buf.short; buf.get() }
                24 -> { buf.short }
                25 -> { buf.short; buf.get() }
                26 -> { buf.short }
                in 30..34 -> {
                    val action = readString(buf, blockEnd)
                    groundActions[opcode - 30] = if (action.equals("hidden", ignoreCase = true)) null else action
                }
                in 35..39 -> {
                    val action = readString(buf, blockEnd)
                    interfaceActions[opcode - 35] = if (action.equals("hidden", ignoreCase = true)) null else action
                }
                40 -> {
                    val count = buf.get().toInt() and 0xFF
                    repeat(count) { buf.short; buf.short }
                }
                41 -> {
                    val count = buf.get().toInt() and 0xFF
                    repeat(count) { buf.short; buf.short }
                }
                42 -> {
                    // Shift click index (OSRS)
                    buf.get()
                }
                65 -> {}
                78 -> { buf.short }
                79 -> { buf.short }
                90 -> { buf.short }
                91 -> { buf.short }
                92 -> { buf.short }
                93 -> { buf.short }
                95 -> { buf.short }
                97 -> noteId = buf.short.toInt() and 0xFFFF
                98 -> notedTemplateId = buf.short.toInt() and 0xFFFF
                in 100..109 -> { buf.short; buf.short }
                110 -> { buf.short }
                111 -> { buf.short }
                112 -> { buf.short }
                113 -> { buf.get() }
                114 -> { buf.get() }
                115 -> { buf.get() }
                139 -> { buf.short } // bought template
                140 -> { buf.short } // bought id
                148 -> { buf.short }
                149 -> { buf.short }
                249 -> {
                    val count = buf.get().toInt() and 0xFF
                    for (i in 0 until count) {
                        val isString = (buf.get().toInt() and 0xFF) == 1
                        readMedium(buf)
                        if (isString) readString(buf, blockEnd) else buf.int
                    }
                }
                else -> {
                    if (unknownOpcodeHits != null) {
                        unknownOpcodeHits[opcode] = (unknownOpcodeHits[opcode] ?: 0) + 1
                        if (blockEnd <= buf.limit()) buf.position(blockEnd)
                    } else {
                        log.debug("Unknown item opcode {} for id={} at pos={}", opcode, id, buf.position() - 1)
                        return CacheItemDefinition(id = id, name = name)
                    }
                }
            }
        }

        if (noteId == 65535) noteId = -1
        if (notedTemplateId == 65535) notedTemplateId = -1
        noted = notedTemplateId != -1

        return CacheItemDefinition(
            id = id, name = name, examine = examine, value = value,
            stackable = stackable, noted = noted, noteId = noteId,
            members = members, equipSlot = equipSlot,
            equipActions = equipActions, groundActions = groundActions,
            interfaceActions = interfaceActions
        )
    }

    private fun readString(buf: ByteBuffer, blockEnd: Int): String {
        val sb = StringBuilder()
        while (buf.position() < blockEnd && buf.hasRemaining()) {
            val b = buf.get().toInt() and 0xFF
            if (b == 10 || b == 0) break
            sb.append(b.toChar())
        }
        return sb.toString()
    }

    private fun readMedium(buf: ByteBuffer): Int {
        val high = (buf.get().toInt() and 0xFF) shl 16
        val low = buf.short.toInt() and 0xFFFF
        return high or low
    }
}
