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

        // Build offset table
        val offsets = IntArray(totalNpcs)
        var currentOffset = 2 // npc.dat starts after the index data stream position
        // Actually npc.dat is a separate byte array, offsets start at 0
        var datOffset = 0
        for (i in 0 until totalNpcs) {
            offsets[i] = datOffset
            datOffset += idxBuf.short.toInt() and 0xFFFF
        }

        // Decode each NPC
        val definitions = mutableMapOf<Int, CacheNpcDefinition>()
        val datBuf = ByteBuffer.wrap(npcDatBytes)

        for (id in 0 until totalNpcs) {
            datBuf.position(offsets[id])
            val def = decodeNpc(id, datBuf)
            if (def.name != "null") {
                definitions[id] = def
            }
        }

        log.info("Loaded {} NPC definitions from cache ({} total entries)", definitions.size, totalNpcs)
        return definitions
    }

    /**
     * Decode a single NPC definition from the data buffer.
     * Opcode format matches the client's NpcDefinition.readValues().
     */
    private fun decodeNpc(id: Int, buf: ByteBuffer): CacheNpcDefinition {
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

        while (true) {
            val opcode = buf.get().toInt() and 0xFF
            if (opcode == 0) break

            when (opcode) {
                1 -> {
                    // Model IDs
                    val count = buf.get().toInt() and 0xFF
                    models = IntArray(count) { buf.short.toInt() and 0xFFFF }
                }
                2 -> name = readString(buf)
                3 -> examine = readString(buf)
                12 -> size = buf.get().toInt()  // signed byte
                13 -> standAnim = buf.short.toInt() and 0xFFFF
                14 -> walkAnim = buf.short.toInt() and 0xFFFF
                15, 16 -> { buf.short } // unknown anim, skip
                17 -> {
                    walkAnim = buf.short.toInt() and 0xFFFF
                    turnAround = buf.short.toInt() and 0xFFFF
                    turnRight = buf.short.toInt() and 0xFFFF
                    turnLeft = buf.short.toInt() and 0xFFFF
                    if (turnAround == 65535) turnAround = -1
                    if (turnRight == 65535) turnRight = -1
                    if (turnLeft == 65535) turnLeft = -1
                }
                in 30..39 -> {
                    // Actions (right-click options)
                    val action = readString(buf)
                    actions[opcode - 30] = if (action.equals("hidden", ignoreCase = true)) null else action
                }
                40 -> {
                    // Recolor pairs
                    val count = buf.get().toInt() and 0xFF
                    repeat(count) { buf.short; buf.short }
                }
                41 -> {
                    // Retexture pairs
                    val count = buf.get().toInt() and 0xFF
                    repeat(count) { buf.short; buf.short }
                }
                60 -> {
                    // Dialogue head models
                    val count = buf.get().toInt() and 0xFF
                    dialogueModels = IntArray(count) { buf.short.toInt() and 0xFFFF }
                }
                93 -> onMinimap = false
                95 -> combatLevel = buf.short.toInt() and 0xFFFF
                97 -> scaleXZ = buf.short.toInt() and 0xFFFF
                98 -> scaleY = buf.short.toInt() and 0xFFFF
                99 -> {} // aBoolean93 = true (render priority)
                100 -> buf.get() // light modifier (signed byte)
                101 -> buf.get() // shadow modifier (signed byte)
                102 -> headIcon = buf.short.toInt() and 0xFFFF
                103 -> degreesToTurn = buf.short.toInt() and 0xFFFF
                106, 118 -> {
                    // Morphism / children (varbit + varp + child IDs)
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
                107 -> {} // aBoolean84 = false (clickable)
                109, 111 -> {} // empty opcodes
                else -> {
                    // Unknown opcode — can't safely skip without knowing size
                    // Log and break to avoid corrupting subsequent reads
                    log.debug("Unknown NPC opcode {} for NPC {}, stopping decode", opcode, id)
                    break
                }
            }
        }

        // Normalize 65535 to -1
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
     */
    private fun readString(buf: ByteBuffer): String {
        val sb = StringBuilder()
        while (buf.hasRemaining()) {
            val b = buf.get().toInt() and 0xFF
            if (b == 10) break
            sb.append(b.toChar())
        }
        return sb.toString()
    }
}