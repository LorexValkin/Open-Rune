package com.openrune.core.world.update

import com.openrune.core.net.codec.PacketBuilder
import com.openrune.core.world.Player
import com.openrune.core.world.collision.Direction
import com.openrune.core.world.npc.Npc
import org.slf4j.LoggerFactory

/**
 * Builds and sends the NPC update packet (opcode 65) each game tick.
 *
 * Similar structure to the player update:
 *   1. Update existing NPCs in the player's local list (movement + flags)
 *   2. Add new NPCs that came into view
 *   3. Remove NPCs that left view
 *   4. Append flag blocks for NPCs that need them
 *
 * ENGINE-LEVEL system. Not pluggable.
 */
object NpcUpdateProtocol {

    private val log = LoggerFactory.getLogger(NpcUpdateProtocol::class.java)

    const val MAX_LOCAL_NPCS = 255

    // NPC update flag masks
    const val FLAG_ANIMATION     = 0x10
    const val FLAG_HIT_2         = 0x8
    const val FLAG_GRAPHIC       = 0x80
    const val FLAG_FACE_ENTITY   = 0x20
    const val FLAG_FORCED_CHAT   = 0x1
    const val FLAG_HIT_1         = 0x40
    const val FLAG_TRANSFORM     = 0x2
    const val FLAG_FACE_POSITION = 0x4

    /**
     * Build and send the NPC update packet for a player.
     */
    fun update(player: Player, allNpcs: List<Npc>) {
        val writer = BitWriter(4096)
        val blockWriter = BitWriter(2048)

        writer.startBitAccess()

        // === Phase 1: Update existing NPCs in local list ===
        writer.writeBits(8, player.localNpcs.size)

        val toRemove = mutableListOf<Int>()
        for (npcIndex in player.localNpcs) {
            val npc = allNpcs.firstOrNull { it.index == npcIndex }

            if (npc == null || !npc.active || !npc.position.isWithinDistance(player.position, 15)) {
                // Remove
                writer.writeBits(1, 1)
                writer.writeBits(2, 3) // type 3 = remove
                toRemove.add(npcIndex)
            } else if (npc.walkingQueue.primaryDirection != Direction.NONE) {
                // Walking
                if (npc.walkingQueue.secondaryDirection != Direction.NONE) {
                    // Running NPC (rare but supported)
                    writer.writeBits(1, 1)
                    writer.writeBits(2, 2) // run
                    writer.writeBits(3, directionToClient(npc.walkingQueue.primaryDirection))
                    writer.writeBits(3, directionToClient(npc.walkingQueue.secondaryDirection))
                    writer.writeBits(1, if (npc.updateRequired) 1 else 0)
                } else {
                    // Walking
                    writer.writeBits(1, 1)
                    writer.writeBits(2, 1) // walk
                    writer.writeBits(3, directionToClient(npc.walkingQueue.primaryDirection))
                    writer.writeBits(1, if (npc.updateRequired) 1 else 0)
                }
                if (npc.updateRequired) {
                    appendUpdateBlock(blockWriter, npc)
                }
            } else if (npc.updateRequired) {
                // Standing but has update flags
                writer.writeBits(1, 1)
                writer.writeBits(2, 0) // no movement, just flags
                appendUpdateBlock(blockWriter, npc)
            } else {
                // No update needed
                writer.writeBits(1, 0)
            }
        }

        // Remove departed NPCs
        for (idx in toRemove) {
            player.localNpcs.remove(idx)
        }

        // === Phase 2: Add new NPCs that entered view ===
        for (npc in allNpcs) {
            if (player.localNpcs.size >= MAX_LOCAL_NPCS) break
            if (!npc.active) continue
            if (npc.index in player.localNpcs) continue
            if (!npc.position.isWithinDistance(player.position, 15)) continue

            addNewNpc(writer, blockWriter, player, npc)
            player.localNpcs.add(npc.index)
        }

        // End marker: only write when there are update blocks following.
        // Without blocks, the client's loop exits naturally on bit exhaustion
        // and finishBitAccess aligns correctly. Writing the marker when there
        // are no blocks inflates the packet size causing a mismatch.
        if (blockWriter.bytePosition > 0) {
            writer.writeBits(14, 16383) // 14-bit end marker
        }

        writer.finishBitAccess()

        // === Build final packet ===
        val pkt = PacketBuilder(65)
        pkt.startVariableShortSize()
        pkt.addBytes(writer.toByteArray())
        if (blockWriter.bytePosition > 0) {
            pkt.addBytes(blockWriter.toByteArray())
        }
        pkt.endVariableShortSize()
        player.send(pkt)
    }

    /**
     * Write bits to add a new NPC to the player's local list.
     * Always signals hasUpdate=1 so the client reads the initial flag block.
     */
    private fun addNewNpc(writer: BitWriter, blockWriter: BitWriter, player: Player, npc: Npc) {
        writer.writeBits(14, npc.index)

        // Delta position: 5-bit signed field needs wrapping into 0-31 range.
        // Client unwraps: if (val > 15) val -= 32, giving range -16 to +15.
        var dy = npc.position.y - player.position.y
        var dx = npc.position.x - player.position.x
        if (dy < 0) dy += 32
        if (dx < 0) dx += 32
        writer.writeBits(5, dy)
        writer.writeBits(5, dx)

        writer.writeBits(1, 0)  // discard walking queue
        writer.writeBits(14, npc.id) // NPC definition type (14 bits for this client revision)

        // Always send the initial flag block for newly visible NPCs.
        // This ensures the client gets face direction, animation state, etc.
        appendUpdateBlock(blockWriter, npc)
        writer.writeBits(1, 1)  // hasUpdate = always true for new adds
    }

    /**
     * Append flag update block for an NPC.
     */
    private fun appendUpdateBlock(writer: BitWriter, npc: Npc) {
        var mask = 0

        if (npc.animationUpdateRequired)     mask = mask or FLAG_ANIMATION
        if (npc.hit2UpdateRequired)          mask = mask or FLAG_HIT_2
        if (npc.graphicUpdateRequired)       mask = mask or FLAG_GRAPHIC
        if (npc.faceEntityUpdateRequired)    mask = mask or FLAG_FACE_ENTITY
        if (npc.forceChatUpdateRequired)     mask = mask or FLAG_FORCED_CHAT
        if (npc.hitUpdateRequired)           mask = mask or FLAG_HIT_1
        if (npc.transformUpdateRequired)     mask = mask or FLAG_TRANSFORM
        if (npc.facePositionUpdateRequired)  mask = mask or FLAG_FACE_POSITION

        writer.writeByte(mask)

        // === Animation ===
        if (npc.animationUpdateRequired) {
            writer.writeLEShort(npc.currentAnimation)
            writer.writeByte(npc.animationDelay)
        }

        // === Hit 2 ===
        if (npc.hit2UpdateRequired) {
            writer.writeByteA(npc.hitDamage2)
            writer.writeByteC(npc.hitType2)
            writer.writeByteA(npc.currentHealth)
            writer.writeByte(npc.maxHealth)
        }

        // === Graphic ===
        // Client: readUnsignedWord() [BE short] + readDWord() [BE int]
        if (npc.graphicUpdateRequired) {
            writer.writeShort(npc.graphicId)
            writer.writeInt((npc.graphicHeight shl 16) or npc.graphicDelay)
        }

        // === Face entity ===
        if (npc.faceEntityUpdateRequired) {
            writer.writeShort(npc.faceEntityIndex)
        }

        // === Forced chat ===
        if (npc.forceChatUpdateRequired) {
            for (ch in npc.forceChatText) {
                writer.writeByte(ch.code)
            }
            writer.writeByte(10) // newline terminator
        }

        // === Hit 1 ===
        // Client: method427() [byte C], method428() [byte S],
        //         method428() [byte S], method427() [byte C]
        if (npc.hitUpdateRequired) {
            writer.writeByteC(npc.hitDamage1)
            writer.writeByteS(npc.hitType1)
            writer.writeByteS(npc.currentHealth)
            writer.writeByteC(npc.maxHealth)
        }

        // === Transform ===
        // Client: method436() [LE short A]
        if (npc.transformUpdateRequired) {
            writer.writeLEShortA(npc.transformId)
        }

        // === Face position ===
        if (npc.facePositionUpdateRequired) {
            writer.writeLEShort(npc.faceX)
            writer.writeLEShort(npc.faceY)
        }
    }

    private fun directionToClient(dir: Direction): Int = when (dir) {
        Direction.NORTH_WEST  -> 0
        Direction.NORTH       -> 1
        Direction.NORTH_EAST  -> 2
        Direction.WEST        -> 3
        Direction.EAST        -> 4
        Direction.SOUTH_WEST  -> 5
        Direction.SOUTH       -> 6
        Direction.SOUTH_EAST  -> 7
        Direction.NONE        -> -1
    }
}
