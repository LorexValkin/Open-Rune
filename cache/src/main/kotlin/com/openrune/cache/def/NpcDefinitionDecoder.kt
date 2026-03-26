package com.openrune.cache.def

import com.openrune.cache.io.ArchiveReader
import com.openrune.cache.io.CacheReader
import com.openrune.cache.io.Container
import com.openrune.cache.io.ReferenceTable
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

/**
 * Decodes NPC definitions from the cache.
 *
 * **317 format:** Config archive (idx0 file 2), jag archive with
 * "npc.dat" + "npc.idx". Each NPC's block boundaries are known from
 * the index, so unknown opcodes skip to block end.
 *
 * **dat2 format:** Index 2 (configs), group 9 (NPCs). Each NPC is a
 * separate file within the group container. The reference table in
 * idx255 tells us how many NPCs exist.
 *
 * Opcodes match the client's NpcDefinition.readValues().
 * Supports stock 317 + OSRS-era opcodes (rev 194-232+).
 */
object NpcDefinitionDecoder {

    private val log = LoggerFactory.getLogger(NpcDefinitionDecoder::class.java)

    /** dat2 config index and NPC archive/group ID */
    private const val CONFIG_INDEX = 2
    private const val NPC_ARCHIVE = 9

    /**
     * Load all NPC definitions from the cache.
     * Auto-detects 317 vs dat2 format.
     */
    fun load(cacheReader: CacheReader): Map<Int, CacheNpcDefinition> {
        return if (cacheReader.isDat2) {
            loadDat2(cacheReader)
        } else {
            load317(cacheReader)
        }
    }

    /**
     * Load NPCs from dat2 format cache.
     * Reads the reference table from idx255, then reads each NPC
     * file individually from index 2 group 9.
     */
    private fun loadDat2(cacheReader: CacheReader): Map<Int, CacheNpcDefinition> {
        // Read the reference table for the config index
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

        val npcGroup = refTable.group(NPC_ARCHIVE)
        if (npcGroup == null) {
            log.error("NPC group ({}) not found in config reference table", NPC_ARCHIVE)
            return emptyMap()
        }

        log.info("dat2: NPC group has {} files", npcGroup.fileCount)

        // Read the NPC archive container from index 2
        val archiveRaw = cacheReader.readFile(CONFIG_INDEX, NPC_ARCHIVE)
        if (archiveRaw == null) {
            log.error("Could not read NPC archive from index {}", CONFIG_INDEX)
            return emptyMap()
        }
        val archiveData = Container.decompress(archiveRaw)
        if (archiveData == null) {
            log.error("Could not decompress NPC archive")
            return emptyMap()
        }

        // In dat2, a group with multiple files is packed as:
        //   [file data concatenated...][file sizes table at end]
        // The last byte of the container is the number of "chunks" (always 1 for configs).
        // Then fileCount * chunks * 4 bytes of sizes at the end.
        val definitions = mutableMapOf<Int, CacheNpcDefinition>()
        val fileIds = npcGroup.fileIds

        // Debug: show archive data info
        log.info("dat2: NPC archive data size={}, last byte={}, fileIds range [{}-{}]",
            archiveData.size,
            if (archiveData.isNotEmpty()) archiveData[archiveData.size - 1].toInt() and 0xFF else -1,
            fileIds.firstOrNull() ?: -1, fileIds.lastOrNull() ?: -1)

        if (fileIds.size == 1) {
            val result = decodeNpcDat2(fileIds[0], archiveData)
            if (result.name != "null") {
                definitions[fileIds[0]] = result
            }
        } else {
            val files = splitGroupFiles(archiveData, fileIds.size)
            if (files != null) {
                // Debug: show first few file sizes
                val preview = files.take(5).mapIndexed { i, f -> "file[${fileIds[i]}]=${f.size}b" }
                log.info("dat2: Split OK, {} files. Preview: {}", files.size, preview)
                if (files.isNotEmpty() && files[0].isNotEmpty()) {
                    log.info("dat2: File[0] first 20 bytes: {}", files[0].take(20).map { it.toInt() and 0xFF })
                }

                var decodeErrors = 0
                for (i in fileIds.indices) {
                    if (i < files.size && files[i].isNotEmpty()) {
                        try {
                            val result = decodeNpcDat2(fileIds[i], files[i])
                            if (result.name != "null") {
                                definitions[fileIds[i]] = result
                            }
                        } catch (e: Exception) {
                            decodeErrors++
                        }
                    }
                }
                if (decodeErrors > 0) {
                    log.warn("dat2: {} NPC decode errors (unknown opcodes)", decodeErrors)
                }
            } else {
                log.warn("Failed to split NPC archive into {} files", fileIds.size)
            }
        }

        log.info("Loaded {} NPC definitions from dat2 cache ({} file IDs)", definitions.size, fileIds.size)
        return definitions
    }

    /**
     * Split a multi-file group container into individual file byte arrays.
     *
     * dat2 multi-file group format:
     * ```
     * [file1 chunk1 data][file2 chunk1 data]...[fileN chunk1 data]
     * [file1 chunk2 data]...(if chunks > 1)
     * [chunks byte at very end]
     * [fileCount * chunks * 4 bytes of sizes before chunks byte]
     * ```
     */
    /**
     * Split a multi-file group container into individual file byte arrays.
     *
     * dat2 multi-file group format:
     * The last byte = chunk count. Before that, fileCount * chunks * 4 bytes
     * of delta-encoded sizes. The accumulated value for each file is the
     * number of bytes that file occupies in the data area for that chunk.
     * The sum of all accumulated values equals the data area size.
     */
    private fun splitGroupFiles(data: ByteArray, fileCount: Int): List<ByteArray>? {
        if (data.isEmpty() || fileCount == 0) return null

        val chunks = data[data.size - 1].toInt() and 0xFF
        if (chunks == 0) return null

        val sizeTableLength = fileCount * chunks * 4 + 1
        if (sizeTableLength > data.size) return null

        val sizeTableStart = data.size - sizeTableLength

        // First pass: compute total size per file across all chunks
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

        // Second pass: extract file data
        val files = Array(fileCount) { java.io.ByteArrayOutputStream(totalFileSizes[it]) }
        val fileWriteOffsets = IntArray(fileCount)

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

    /**
     * Decode a single NPC from dat2 format (individual file, no block-end safety).
     * Opcodes are read until opcode 0 is encountered.
     */
    private fun decodeNpcDat2(id: Int, data: ByteArray): CacheNpcDefinition {
        val buf = ByteBuffer.wrap(data)
        return decodeNpc(id, buf, data.size, null)
    }

    // ─── Legacy 317 loading ─────────────────────────────────────────

    private fun load317(cacheReader: CacheReader): Map<Int, CacheNpcDefinition> {
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

        val npcDatBytes = archive.getEntry("npc.dat")
        val npcIdxBytes = archive.getEntry("npc.idx")

        if (npcDatBytes == null || npcIdxBytes == null) {
            log.error("npc.dat or npc.idx not found in config archive")
            return emptyMap()
        }

        val idxBuf = ByteBuffer.wrap(npcIdxBytes)
        val totalNpcs = idxBuf.short.toInt() and 0xFFFF

        val offsets = IntArray(totalNpcs)
        val blockSizes = IntArray(totalNpcs)
        var datOffset = 2
        for (i in 0 until totalNpcs) {
            offsets[i] = datOffset
            val sz = idxBuf.short.toInt() and 0xFFFF
            blockSizes[i] = sz
            datOffset += sz
        }

        val definitions = mutableMapOf<Int, CacheNpcDefinition>()
        val datBuf = ByteBuffer.wrap(npcDatBytes)
        val unknownOpcodeHits = mutableMapOf<Int, Int>()

        for (id in 0 until totalNpcs) {
            datBuf.position(offsets[id])
            val blockEnd = offsets[id] + blockSizes[id]
            val result = decodeNpc(id, datBuf, blockEnd, unknownOpcodeHits)
            if (result.name != "null") {
                definitions[id] = result
            }
        }

        if (unknownOpcodeHits.isNotEmpty()) {
            val sorted = unknownOpcodeHits.entries.sortedByDescending { it.value }
            val top10 = sorted.take(10).joinToString(", ") { "op${it.key}(x${it.value})" }
            log.warn("Unknown NPC opcodes — top hits: {}", top10)
        }

        log.info("Loaded {} NPC definitions from 317 cache ({} total entries)", definitions.size, totalNpcs)
        return definitions
    }

    // ─── Shared opcode decoder ──────────────────────────────────────

    /**
     * Decode a single NPC definition from a ByteBuffer.
     *
     * @param id         NPC ID.
     * @param buf        Buffer positioned at the start of this NPC's data.
     * @param blockEnd   End position of this NPC's data block (317 safety).
     * @param unknownOpcodeHits  Accumulator for unknown opcodes (null for dat2).
     */
    private fun decodeNpc(
        id: Int,
        buf: ByteBuffer,
        blockEnd: Int,
        unknownOpcodeHits: MutableMap<Int, Int>?
    ): CacheNpcDefinition {
        var name = "null"
        var examine = ""
        var size = 1
        var standAnim = -1
        var walkAnim = -1
        var turnAround = -1
        var turnRight = -1
        var turnLeft = -1
        var combatLevel = -1
        val actions = arrayOfNulls<String>(10)
        var onMinimap = true
        var scaleXZ = 128
        var scaleY = 128
        var headIcon = -1
        var degreesToTurn = 32
        var models = intArrayOf()
        var dialogueModels = intArrayOf()
        var childrenIds: IntArray? = null

        while (buf.position() < blockEnd) {
            val opcode = buf.get().toInt() and 0xFF
            if (opcode == 0) break

            when (opcode) {
                1 -> {
                    val count = buf.get().toInt() and 0xFF
                    models = IntArray(count) { buf.short.toInt() and 0xFFFF }
                }
                2 -> name = readString(buf, blockEnd)
                3 -> examine = readString(buf, blockEnd)
                5 -> {
                    val count = buf.get().toInt() and 0xFF
                    repeat(count) { buf.short }
                }
                // OSRS: category
                6 -> readString(buf, blockEnd) // opLabel
                12 -> size = buf.get().toInt()
                13 -> standAnim = buf.short.toInt() and 0xFFFF
                14 -> walkAnim = buf.short.toInt() and 0xFFFF
                15, 16 -> { buf.short }
                17 -> {
                    walkAnim = buf.short.toInt() and 0xFFFF
                    turnAround = buf.short.toInt() and 0xFFFF
                    turnRight = buf.short.toInt() and 0xFFFF
                    turnLeft = buf.short.toInt() and 0xFFFF
                    if (turnAround == 65535) turnAround = -1
                    if (turnRight == 65535) turnRight = -1
                    if (turnLeft == 65535) turnLeft = -1
                }
                18 -> { buf.short } // category
                // OSRS rev 232+: additional model opcodes
                26 -> { buf.short; buf.short } // head icon archiveId + spriteIndex (OSRS 232+)
                28 -> { buf.short } // render animation (OSRS 232+)
                in 30..39 -> {
                    val action = readString(buf, blockEnd)
                    actions[opcode - 30] = if (action.equals("hidden", ignoreCase = true)) null else action
                }
                40 -> {
                    val count = buf.get().toInt() and 0xFF
                    repeat(count) { buf.short; buf.short }
                }
                41 -> {
                    val count = buf.get().toInt() and 0xFF
                    repeat(count) { buf.short; buf.short }
                }
                60 -> {
                    val count = buf.get().toInt() and 0xFF
                    dialogueModels = IntArray(count) { buf.short.toInt() and 0xFFFF }
                }
                61 -> {
                    val count = buf.get().toInt() and 0xFF
                    models = IntArray(count) { buf.int }
                }
                62 -> {
                    val count = buf.get().toInt() and 0xFF
                    dialogueModels = IntArray(count) { buf.int }
                }
                74 -> { buf.short }
                75 -> { buf.short }
                76 -> { buf.short }
                77 -> { buf.short }
                78 -> { buf.short }
                79 -> { buf.short }
                // OSRS 232+: additional opcodes
                59 -> {
                    // Alternate colours/retextures (OSRS 232+)
                    val count = buf.get().toInt() and 0xFF
                    repeat(count) { buf.short; buf.short }
                }
                93 -> onMinimap = false
                // OSRS 232+: height override
                96 -> { buf.short } // heightOverride
                95 -> combatLevel = buf.short.toInt() and 0xFFFF
                97 -> scaleXZ = buf.short.toInt() and 0xFFFF
                98 -> scaleY = buf.short.toInt() and 0xFFFF
                99 -> {}
                100 -> buf.get()
                101 -> buf.get()
                102 -> headIcon = buf.short.toInt() and 0xFFFF
                103 -> degreesToTurn = buf.short.toInt() and 0xFFFF
                104 -> { buf.short }
                105 -> { buf.short }
                106, 118 -> {
                    var varbitId = buf.short.toInt() and 0xFFFF
                    if (varbitId == 65535) varbitId = -1
                    var varpId = buf.short.toInt() and 0xFFFF
                    if (varpId == 65535) varpId = -1
                    var extraChild = -1
                    if (opcode == 118) {
                        extraChild = buf.short.toInt() and 0xFFFF
                    }
                    val count = buf.get().toInt() and 0xFF
                    val ids = IntArray(count + 2)
                    for (i in 0..count) {
                        ids[i] = buf.short.toInt() and 0xFFFF
                        if (ids[i] == 65535) ids[i] = -1
                    }
                    ids[count + 1] = extraChild
                    childrenIds = ids
                }
                107 -> {}
                108 -> {}
                109 -> {}
                110 -> {}
                111 -> {}
                112 -> buf.get()
                113 -> buf.get()
                114 -> { buf.short }
                115 -> { buf.short; buf.short; buf.short; buf.short }
                116 -> { buf.short }
                117 -> { buf.short; buf.short; buf.short; buf.short }
                119 -> buf.get()
                122 -> {}
                123 -> {}
                124 -> { buf.short }
                126 -> { buf.short }
                129, 130 -> {}
                145 -> {}
                146 -> { buf.short }
                147 -> {}
                // OSRS 232+: head icon groups
                153 -> {
                    val count = buf.get().toInt() and 0xFF
                    for (i in 0 until count) {
                        buf.short // spriteGroup
                        buf.short // spriteIndex
                    }
                }
                // OSRS 232+: render animation modifier
                155 -> { buf.short; buf.short; buf.short; buf.short } // 4x u16
                // OSRS 232+: unknown render flags
                158, 159, 160 -> {}
                // OSRS 232+: sound effect opcodes
                189 -> { buf.short } // ambient sound id
                190 -> { buf.short; buf.short; buf.short; buf.get() } // ambient sound params
                191 -> { buf.short } // attackSound
                192 -> { buf.short } // defenceSound
                251, 252, 253 -> {
                    val hasOps = (buf.get().toInt() and 0xFF) == 1
                    if (hasOps) {
                        val cnt = buf.get().toInt() and 0xFF
                        for (i in 0 until cnt) { buf.short }
                    }
                }
                249 -> {
                    val count = buf.get().toInt() and 0xFF
                    for (i in 0 until count) {
                        val isString = (buf.get().toInt() and 0xFF) == 1
                        readMedium(buf)
                        if (isString) {
                            readString(buf, blockEnd)
                        } else {
                            buf.int
                        }
                    }
                }
                else -> {
                    if (unknownOpcodeHits != null) {
                        unknownOpcodeHits[opcode] = (unknownOpcodeHits[opcode] ?: 0) + 1
                        if (blockEnd <= buf.limit()) {
                            buf.position(blockEnd)
                        }
                    } else {
                        // dat2: unknown opcode with no block-end safety — log and stop
                        log.debug("Unknown NPC opcode {} for id={} at pos={}", opcode, id, buf.position() - 1)
                        return CacheNpcDefinition(id = id, name = name)
                    }
                }
            }
        }

        if (standAnim == 65535) standAnim = -1
        if (walkAnim == 65535) walkAnim = -1

        return CacheNpcDefinition(
            id = id, name = name, examine = examine, size = size,
            standAnim = standAnim, walkAnim = walkAnim,
            turnAround = turnAround, turnRight = turnRight, turnLeft = turnLeft,
            combatLevel = combatLevel, actions = actions,
            onMinimap = onMinimap, scaleXZ = scaleXZ, scaleY = scaleY,
            headIcon = headIcon, degreesToTurn = degreesToTurn,
            models = models, dialogueModels = dialogueModels,
            childrenIds = childrenIds
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
