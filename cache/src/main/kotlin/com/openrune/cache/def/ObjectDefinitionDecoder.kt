package com.openrune.cache.def

import com.openrune.cache.io.ArchiveReader
import com.openrune.cache.io.CacheReader
import com.openrune.cache.io.Container
import com.openrune.cache.io.ReferenceTable
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

/**
 * Decodes object (loc) definitions from the cache.
 *
 * **317 format:** Config archive (idx0 file 2), jag archive with
 * "loc.dat" + "loc.idx".
 *
 * **dat2 format:** Index 2 (configs), group 6 (objects). Each object
 * is a separate file within the group container.
 *
 * Opcodes match the client's ObjectDefinition.readValues().
 * Supports stock 317 + OSRS-era opcodes (rev 194-232+).
 */
object ObjectDefinitionDecoder {

    private val log = LoggerFactory.getLogger(ObjectDefinitionDecoder::class.java)

    private const val CONFIG_INDEX = 2
    private const val OBJECT_ARCHIVE = 6

    fun load(cacheReader: CacheReader): Map<Int, CacheObjectDefinition> {
        return if (cacheReader.isDat2) {
            loadDat2(cacheReader)
        } else {
            load317(cacheReader)
        }
    }

    private fun loadDat2(cacheReader: CacheReader): Map<Int, CacheObjectDefinition> {
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

        val objGroup = refTable.group(OBJECT_ARCHIVE)
        if (objGroup == null) {
            log.error("Object group ({}) not found in config reference table", OBJECT_ARCHIVE)
            return emptyMap()
        }

        log.info("dat2: Object group has {} files", objGroup.fileCount)

        val archiveRaw = cacheReader.readFile(CONFIG_INDEX, OBJECT_ARCHIVE)
        if (archiveRaw == null) {
            log.error("Could not read object archive from index {}", CONFIG_INDEX)
            return emptyMap()
        }
        val archiveData = Container.decompress(archiveRaw)
        if (archiveData == null) {
            log.error("Could not decompress object archive")
            return emptyMap()
        }

        val definitions = mutableMapOf<Int, CacheObjectDefinition>()
        val fileIds = objGroup.fileIds

        if (fileIds.size == 1) {
            val result = decodeObjectDat2(fileIds[0], archiveData)
            if (result.name != "null") definitions[fileIds[0]] = result
        } else {
            val files = splitGroupFiles(archiveData, fileIds.size)
            if (files != null) {
                var decodeErrors = 0
                for (i in fileIds.indices) {
                    if (i < files.size && files[i].isNotEmpty()) {
                        try {
                            val result = decodeObjectDat2(fileIds[i], files[i])
                            if (result.name != "null") definitions[fileIds[i]] = result
                        } catch (_: Exception) { decodeErrors++ }
                    }
                }
                if (decodeErrors > 0) log.warn("dat2: {} object decode errors", decodeErrors)
            } else {
                log.warn("Failed to split object archive into {} files", fileIds.size)
            }
        }

        log.info("Loaded {} object definitions from dat2 cache ({} file IDs)", definitions.size, fileIds.size)
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

    private fun decodeObjectDat2(id: Int, data: ByteArray): CacheObjectDefinition {
        val buf = ByteBuffer.wrap(data)
        return decodeObject(id, buf, data.size, null)
    }

    // ─── Legacy 317 loading ─────────────────────────────────────────

    private fun load317(cacheReader: CacheReader): Map<Int, CacheObjectDefinition> {
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

        val locDatBytes = archive.getEntry("loc.dat")
        val locIdxBytes = archive.getEntry("loc.idx")

        if (locDatBytes == null || locIdxBytes == null) {
            log.error("loc.dat or loc.idx not found in config archive")
            return emptyMap()
        }

        val idxBuf = ByteBuffer.wrap(locIdxBytes)
        val totalObjects = idxBuf.short.toInt() and 0xFFFF

        val offsets = IntArray(totalObjects)
        val blockSizes = IntArray(totalObjects)
        var datOffset = 2
        for (i in 0 until totalObjects) {
            offsets[i] = datOffset
            val sz = idxBuf.short.toInt() and 0xFFFF
            blockSizes[i] = sz
            datOffset += sz
        }

        val definitions = mutableMapOf<Int, CacheObjectDefinition>()
        val datBuf = ByteBuffer.wrap(locDatBytes)
        val unknownOpcodeHits = mutableMapOf<Int, Int>()

        for (id in 0 until totalObjects) {
            datBuf.position(offsets[id])
            val blockEnd = offsets[id] + blockSizes[id]
            val result = decodeObject(id, datBuf, blockEnd, unknownOpcodeHits)
            if (result.name != "null") {
                definitions[id] = result
            }
        }

        if (unknownOpcodeHits.isNotEmpty()) {
            val sorted = unknownOpcodeHits.entries.sortedByDescending { it.value }
            val top10 = sorted.take(10).joinToString(", ") { "op${it.key}(x${it.value})" }
            log.warn("Unknown object opcodes — top hits: {}", top10)
        }

        log.info("Loaded {} object definitions from 317 cache ({} total entries)", definitions.size, totalObjects)
        return definitions
    }

    // ─── Shared opcode decoder ──────────────────────────────────────

    private fun decodeObject(
        id: Int,
        buf: ByteBuffer,
        blockEnd: Int,
        unknownOpcodeHits: MutableMap<Int, Int>?
    ): CacheObjectDefinition {
        var name = "null"
        var examine = ""
        var width = 1
        var length = 1
        var solid = true
        var interactable = false
        val actions = arrayOfNulls<String>(5)
        var animationId = -1
        var mapIcon = -1

        while (buf.position() < blockEnd) {
            val opcode = buf.get().toInt() and 0xFF
            if (opcode == 0) break

            when (opcode) {
                1 -> {
                    val count = buf.get().toInt() and 0xFF
                    repeat(count) { buf.short; buf.get() }
                }
                2 -> name = readString(buf, blockEnd)
                3 -> examine = readString(buf, blockEnd)
                5 -> {
                    val count = buf.get().toInt() and 0xFF
                    repeat(count) { buf.short }
                }
                14 -> width = buf.get().toInt() and 0xFF
                15 -> length = buf.get().toInt() and 0xFF
                17 -> solid = false
                18 -> {}
                19 -> {
                    val flag = buf.get().toInt() and 0xFF
                    interactable = flag == 1
                }
                21 -> {}
                22 -> {}
                23 -> {}
                24 -> {
                    animationId = buf.short.toInt() and 0xFFFF
                    if (animationId == 65535) animationId = -1
                }
                27 -> {}
                28 -> { buf.get() }
                29 -> { buf.get() }
                in 30..38 -> {
                    val action = readString(buf, blockEnd)
                    if (opcode - 30 < actions.size) {
                        actions[opcode - 30] = if (action.equals("hidden", ignoreCase = true)) null else action
                    }
                }
                39 -> { buf.get() } // aByte742 (contrast/shadow)
                40 -> {
                    val count = buf.get().toInt() and 0xFF
                    repeat(count) { buf.short; buf.short }
                }
                41 -> {
                    val count = buf.get().toInt() and 0xFF
                    repeat(count) { buf.short; buf.short }
                }
                44 -> { buf.short } // contrast data
                45 -> { buf.short } // translucency
                60 -> { buf.short } // mapSceneId (u16)
                62 -> {} // aBoolean751 = true
                64 -> {} // aBoolean779 = false
                65 -> { buf.short } // thickness (u16)
                66 -> { buf.short } // height (u16)
                67 -> { buf.short } // width (u16)
                68 -> { buf.short } // mapSceneType (u16)
                69 -> { buf.get() } // walkFlag (u8)
                70 -> { buf.short } // translateX (i16)
                71 -> { buf.short } // translateY (i16)
                72 -> { buf.short } // translateZ (i16)
                73 -> {} // obstructsGround = true
                74 -> {} // isHollow = true
                75 -> { buf.get() } // supportsItems (u8)
                // Varbit transform (no default child)
                77 -> {
                    var varbitId = buf.short.toInt() and 0xFFFF
                    if (varbitId == 65535) varbitId = -1
                    var varpId = buf.short.toInt() and 0xFFFF
                    if (varpId == 65535) varpId = -1
                    val count = buf.get().toInt() and 0xFF
                    for (i in 0..count) { buf.short }
                }
                // Ambient sound
                78 -> {
                    buf.short // ambientSoundId
                    buf.get()  // ambientSoundRange
                }
                // Ambient sound area
                79 -> {
                    buf.short // soundRetain
                    buf.short // soundRange
                    buf.get()  // soundSize
                    val soundCount = buf.get().toInt() and 0xFF
                    repeat(soundCount) { buf.short }
                }
                81 -> { buf.get() } // clipType
                82 -> { buf.short } // areaType
                // Varbit transform WITH default child (OSRS variant of 77)
                92 -> {
                    var varbitId = buf.short.toInt() and 0xFFFF
                    if (varbitId == 65535) varbitId = -1
                    var varpId = buf.short.toInt() and 0xFFFF
                    if (varpId == 65535) varpId = -1
                    var defaultChild = buf.short.toInt() and 0xFFFF
                    if (defaultChild == 65535) defaultChild = -1
                    val count = buf.get().toInt() and 0xFF
                    for (i in 0..count) { buf.short }
                }
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
                        log.debug("Unknown object opcode {} for id={} at pos={}", opcode, id, buf.position() - 1)
                        return CacheObjectDefinition(id = id, name = name)
                    }
                }
            }
        }

        return CacheObjectDefinition(
            id = id, name = name, examine = examine,
            width = width, length = length, solid = solid,
            interactable = interactable, actions = actions,
            animationId = animationId, mapIcon = mapIcon
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
