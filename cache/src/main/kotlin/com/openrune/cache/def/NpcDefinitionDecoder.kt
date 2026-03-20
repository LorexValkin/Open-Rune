package com.openrune.cache.def

import com.openrune.cache.io.ArchiveReader
import com.openrune.cache.io.CacheReader
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

/**
 * Decodes NPC definitions from the cache config archive.
 *
 * The config archive (idx0, file 2) is a jag archive containing
 * "npc.dat" (definition data) and "npc.idx" (offset index).
 *
 * npc.idx structure:
 *   [2 bytes] total NPC count
 *   [2 bytes per NPC] size of each NPC's definition block
 *
 * npc.dat structure:
 *   Sequential opcode-encoded blocks, one per NPC.
 *   Opcodes match the client's NpcDefinition.readValues().
 *
 * Supports both stock 317 opcodes AND OSRS-era opcodes (rev 194+)
 * used by the Anguish/Project51 cache. Unknown opcodes are safely
 * skipped using block-size boundaries from the index, so one bad
 * opcode never corrupts subsequent NPC decodes.
 */
object NpcDefinitionDecoder {

    private val log = LoggerFactory.getLogger(NpcDefinitionDecoder::class.java)

    /**
     * Load all NPC definitions from the cache.
     * Returns a map of NPC ID -> CacheNpcDefinition.
     */
    fun load(cacheReader: CacheReader): Map<Int, CacheNpcDefinition> {
        // Read the config jag archive from idx0, file 2
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

        // Parse index: first 2 bytes = count, then 2 bytes per NPC = block size
        val idxBuf = ByteBuffer.wrap(npcIdxBytes)
        val totalNpcs = idxBuf.short.toInt() and 0xFFFF

        // Build offset + size table so we know each NPC's exact block boundaries
        val offsets = IntArray(totalNpcs)
        val blockSizes = IntArray(totalNpcs)
        var datOffset = 0
        for (i in 0 until totalNpcs) {
            offsets[i] = datOffset
            val sz = idxBuf.short.toInt() and 0xFFFF
            blockSizes[i] = sz
            datOffset += sz
        }

        // Decode each NPC using block boundaries for safety
        val definitions = mutableMapOf<Int, CacheNpcDefinition>()
        val datBuf = ByteBuffer.wrap(npcDatBytes)
        var skippedCount = 0
        val unknownOpcodeHits = mutableMapOf<Int, Int>() // opcode -> count

        for (id in 0 until totalNpcs) {
            datBuf.position(offsets[id])
            val blockEnd = offsets[id] + blockSizes[id]
            val result = decodeNpc(id, datBuf, blockEnd, unknownOpcodeHits)
            if (result.name != "null") {
                definitions[id] = result
            }
        }

        // Log summary of unknown opcodes for diagnostics
        if (unknownOpcodeHits.isNotEmpty()) {
            val sorted = unknownOpcodeHits.entries.sortedByDescending { it.value }
            val top10 = sorted.take(10).joinToString(", ") { "op${it.key}(x${it.value})" }
            log.warn(
                "Unknown NPC opcodes encountered — top hits: {}. " +
                "These NPCs decoded with partial data (fields before the unknown opcode are preserved).",
                top10
            )
        }

        log.info("Loaded {} NPC definitions from cache ({} total entries)", definitions.size, totalNpcs)
        return definitions
    }

    /**
     * Decode a single NPC definition from the data buffer.
     *
     * Handles all stock 317 opcodes plus OSRS-era opcodes from the
     * Anguish/Project51 cache (rev 194). Unknown opcodes cause a skip
     * to [blockEnd] rather than corrupting the stream — fields parsed
     * before the unknown opcode are preserved.
     *
     * Opcode reference: client/src/com/client/definitions/NpcDefinition.java
     * + standard OSRS NPC definition format (RuneLite/OpenRS2).
     */
    private fun decodeNpc(
        id: Int,
        buf: ByteBuffer,
        blockEnd: Int,
        unknownOpcodeHits: MutableMap<Int, Int>
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
                // ── 317 + OSRS: Core identity ────────────────────────────
                1 -> {
                    // Model IDs
                    val count = buf.get().toInt() and 0xFF
                    models = IntArray(count) { buf.short.toInt() and 0xFFFF }
                }
                2 -> name = readString(buf, blockEnd)
                3 -> examine = readString(buf, blockEnd)

                // ── OSRS: Additional/secondary models (same format as 1) ─
                5 -> {
                    val count = buf.get().toInt() and 0xFF
                    repeat(count) { buf.short } // skip model IDs
                }

                // ── 317 + OSRS: Physical properties ─────────────────────
                12 -> size = buf.get().toInt()  // signed byte
                13 -> standAnim = buf.short.toInt() and 0xFFFF
                14 -> walkAnim = buf.short.toInt() and 0xFFFF
                15, 16 -> { buf.short } // idle rotate anims (u16), skip
                17 -> {
                    // Walk + 3 turn animations
                    walkAnim = buf.short.toInt() and 0xFFFF
                    turnAround = buf.short.toInt() and 0xFFFF
                    turnRight = buf.short.toInt() and 0xFFFF
                    turnLeft = buf.short.toInt() and 0xFFFF
                    if (turnAround == 65535) turnAround = -1
                    if (turnRight == 65535) turnRight = -1
                    if (turnLeft == 65535) turnLeft = -1
                }

                // ── OSRS: Category ──────────────────────────────────────
                18 -> { buf.short } // category ID (u16), skip

                // ── 317 + OSRS: Right-click actions ─────────────────────
                in 30..39 -> {
                    val action = readString(buf, blockEnd)
                    actions[opcode - 30] = if (action.equals("hidden", ignoreCase = true)) null else action
                }

                // ── 317 + OSRS: Recolor pairs ───────────────────────────
                40 -> {
                    val count = buf.get().toInt() and 0xFF
                    repeat(count) { buf.short; buf.short }
                }

                // ── OSRS: Retexture pairs ───────────────────────────────
                41 -> {
                    val count = buf.get().toInt() and 0xFF
                    repeat(count) { buf.short; buf.short }
                }

                // ── 317 + OSRS: Dialogue / chathead models ──────────────
                60 -> {
                    val count = buf.get().toInt() and 0xFF
                    dialogueModels = IntArray(count) { buf.short.toInt() and 0xFFFF }
                }

                // ── 317 + OSRS: Flags (0 data bytes) ────────────────────
                93 -> onMinimap = false
                99 -> {} // hasRenderPriority = true
                107 -> {} // isInteractable = false
                109 -> {} // rotationFlag = false
                111 -> {} // isPet / isFollower = true

                // ── 317 + OSRS: Combat & scale ──────────────────────────
                95 -> combatLevel = buf.short.toInt() and 0xFFFF
                97 -> scaleXZ = buf.short.toInt() and 0xFFFF
                98 -> scaleY = buf.short.toInt() and 0xFFFF
                100 -> buf.get() // ambient light (signed byte)
                101 -> buf.get() // contrast/shadow (signed byte)
                102 -> headIcon = buf.short.toInt() and 0xFFFF
                103 -> degreesToTurn = buf.short.toInt() and 0xFFFF

                // ── OSRS: Extra render params (u16 each) ────────────────
                104 -> { buf.short } // render param (u16)
                105 -> { buf.short } // render param (u16)

                // ── OSRS: Extended flags (0 data bytes) ─────────────────
                108 -> {} // some revisions: isResizeToMapDot flag
                110 -> {} // boolean flag (some revisions)

                // ── OSRS: Render modifiers (single byte) ────────────────
                112 -> buf.get() // render type modifier (u8)
                113 -> buf.get() // render flag (u8)

                // ── OSRS: Run animations ────────────────────────────────
                114 -> { buf.short } // runSequence (u16)
                115 -> { buf.short } // runRotate180Sequence (u16)
                116 -> { buf.short } // runRotateCWSequence (u16)
                117 -> { buf.short } // runRotateCCWSequence (u16)

                // ── 317 + OSRS: Morphism / children (varbit transforms) ─
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

                // ── OSRS: Follower priority ─────────────────────────────
                119 -> buf.get() // lowPriorityFollowerOps (u8)

                // ── OSRS: Params / attributes (key-value map) ───────────
                249 -> {
                    val count = buf.get().toInt() and 0xFF
                    for (i in 0 until count) {
                        val isString = (buf.get().toInt() and 0xFF) == 1
                        readMedium(buf) // param key (u24), skip
                        if (isString) {
                            readString(buf, blockEnd) // param string value, skip
                        } else {
                            buf.int // param int value (4 bytes), skip
                        }
                    }
                }

                // ── Unknown opcode: skip to block end ───────────────────
                // Safety net: jump to end of this NPC's index block rather
                // than misinterpreting data bytes as opcodes. All fields
                // parsed before this point are preserved.
                else -> {
                    unknownOpcodeHits[opcode] = (unknownOpcodeHits[opcode] ?: 0) + 1
                    if (blockEnd <= buf.limit()) {
                        buf.position(blockEnd)
                    }
                    // Loop condition (position >= blockEnd) will terminate
                }
            }
        }

        // Normalize 65535 sentinel to -1
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

    /**
     * Read a newline-terminated string from the buffer.
     * RS2 strings are terminated with byte 10 (0x0A).
     * Bounded by [blockEnd] to prevent runaway reads on malformed data.
     */
    private fun readString(buf: ByteBuffer, blockEnd: Int): String {
        val sb = StringBuilder()
        while (buf.position() < blockEnd && buf.hasRemaining()) {
            val b = buf.get().toInt() and 0xFF
            if (b == 10) break
            sb.append(b.toChar())
        }
        return sb.toString()
    }

    /**
     * Read a 3-byte unsigned medium (u24) from the buffer.
     * Format: (byte << 16) | unsigned_short
     * Used by opcode 249 (params) for param keys.
     */
    private fun readMedium(buf: ByteBuffer): Int {
        val high = (buf.get().toInt() and 0xFF) shl 16
        val low = buf.short.toInt() and 0xFFFF
        return high or low
    }
}