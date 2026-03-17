package com.openrune.core.world.update

import com.openrune.core.net.codec.PacketBuilder
import com.openrune.core.world.Player
import com.openrune.core.world.appearance.AppearanceBuilder
import com.openrune.core.world.collision.Direction
import org.slf4j.LoggerFactory

/**
 * Builds and sends the player update packet (opcode 81) each game tick.
 *
 * The 317 player update has four phases:
 *   1. Local player movement (bit-packed)
 *   2. Other players' movement (bit-packed)
 *   3. Player list additions/removals (bit-packed)
 *   4. Update flag blocks for all players that need them (byte-packed)
 *
 * Each player maintains a "local player list" of up to 255 nearby players.
 * The update packet tells the client about movement and state changes
 * for each player in the list.
 *
 * ENGINE-LEVEL system. This is core protocol and not pluggable.
 */
object PlayerUpdateProtocol {

    private val log = LoggerFactory.getLogger(PlayerUpdateProtocol::class.java)

    /** Maximum players in the local list. */
    const val MAX_LOCAL_PLAYERS = 255

    // Update flag masks
    const val FLAG_FORCED_MOVEMENT = 0x400
    const val FLAG_GRAPHIC         = 0x100
    const val FLAG_ANIMATION       = 0x8
    const val FLAG_FORCED_CHAT     = 0x4
    const val FLAG_CHAT            = 0x80
    const val FLAG_FACE_ENTITY     = 0x1
    const val FLAG_APPEARANCE      = 0x10
    const val FLAG_FACE_POSITION   = 0x2
    const val FLAG_HIT_1           = 0x20
    const val FLAG_HIT_2           = 0x200

    /**
     * Build and send the update packet for a single player.
     */
    fun update(player: Player, allPlayers: List<Player>) {
        val writer = BitWriter(8192)
        val blockWriter = BitWriter(4096) // Holds update blocks (byte-mode)

        writer.startBitAccess()

        // === Phase 1: Local player (self) movement ===
        updateLocalPlayerMovement(writer, player)

        // The local player's flag block is queued FIRST by the client (method117).
        // We must write it to blockWriter before any other player's blocks,
        // because the client reads flag blocks in the order they were queued:
        //   local player -> existing others -> newly added players.
        if (player.updateRequired) {
            appendUpdateBlock(blockWriter, player)
        }

        // === Phase 2: Other players in local list ===
        writer.writeBits(8, player.localPlayers.size)

        val toRemove = mutableListOf<Int>()
        for (otherIndex in player.localPlayers) {
            val other = allPlayers.firstOrNull { it.index == otherIndex }

            if (other == null || !other.isOnline || !other.position.isWithinDistance(player.position, 15)) {
                // Remove from local list
                writer.writeBits(1, 1)   // needs update
                writer.writeBits(2, 3)   // type 3 = remove
                toRemove.add(otherIndex)
            } else if (other.updateRequired) {
                // Has update flags but didn't move
                if (other.walkingQueue.primaryDirection != Direction.NONE) {
                    updateOtherPlayerMovement(writer, other)
                    appendUpdateBlock(blockWriter, other)
                } else {
                    writer.writeBits(1, 1)
                    writer.writeBits(2, 0) // no movement, just flags
                    appendUpdateBlock(blockWriter, other)
                }
            } else if (other.walkingQueue.primaryDirection != Direction.NONE) {
                updateOtherPlayerMovement(writer, other)
            } else {
                writer.writeBits(1, 0) // no update needed
            }
        }

        // Remove departed players
        for (idx in toRemove) {
            player.localPlayers.remove(idx)
        }

        // === Phase 3: Add new nearby players ===
        for (other in allPlayers) {
            if (other.index == player.index) continue
            if (player.localPlayers.size >= MAX_LOCAL_PLAYERS) break
            if (other.index in player.localPlayers) continue
            if (!other.position.isWithinDistance(player.position, 15)) continue

            // Add to local list
            addNewPlayer(writer, player, other)
            appendUpdateBlock(blockWriter, other) // Always send full appearance for new players
            player.localPlayers.add(other.index)
        }

        // Terminate the new player list
        if (blockWriter.bytePosition > 0) {
            writer.writeBits(11, 2047) // end marker
        }

        writer.finishBitAccess()

        // === Build final packet ===
        val pkt = PacketBuilder(81)
        pkt.startVariableShortSize()

        // Write the bit-packed section
        val bitData = writer.toByteArray()
        pkt.addBytes(bitData)

        // Write the update blocks
        if (blockWriter.bytePosition > 0) {
            pkt.addBytes(blockWriter.toByteArray())
        }

        pkt.endVariableShortSize()
        player.send(pkt)
    }

    /**
     * Write movement bits for the local (self) player.
     */
    private fun updateLocalPlayerMovement(writer: BitWriter, player: Player) {
        val queue = player.walkingQueue

        if (queue.didTeleport) {
            // Type 3 = teleport/placement (PI: ONLY on didTeleport, never on walking region change)
            // PI: currentX = absX - mapRegionX*8 where mapRegionX = (absX>>3)-6
            // Client: baseX = (received - 6) * 8 = ((absX>>3) - 6) * 8
            // So localX = absX - ((absX>>3) - 6) * 8, which centers the player in the 104-tile area
            val baseX = ((player.lastRegion.x shr 3) - 6) * 8
            val baseY = ((player.lastRegion.y shr 3) - 6) * 8
            writer.writeBits(1, 1)   // update required
            writer.writeBits(2, 3)   // type 3 = teleport/placement
            writer.writeBits(2, player.position.z)
            writer.writeBits(1, if (queue.didTeleport) 1 else 0)
            writer.writeBits(1, if (player.updateRequired) 1 else 0)
            writer.writeBits(7, player.position.y - baseY)
            writer.writeBits(7, player.position.x - baseX)
        } else if (queue.primaryDirection != Direction.NONE) {
            if (queue.secondaryDirection != Direction.NONE) {
                // Running (two steps)
                writer.writeBits(1, 1)
                writer.writeBits(2, 2)  // type 2 = run
                writer.writeBits(3, directionToClient(queue.primaryDirection))
                writer.writeBits(3, directionToClient(queue.secondaryDirection))
                writer.writeBits(1, if (player.updateRequired) 1 else 0)
            } else {
                // Walking (one step)
                writer.writeBits(1, 1)
                writer.writeBits(2, 1)  // type 1 = walk
                writer.writeBits(3, directionToClient(queue.primaryDirection))
                writer.writeBits(1, if (player.updateRequired) 1 else 0)
            }
        } else if (player.updateRequired) {
            // No movement but has flag updates
            writer.writeBits(1, 1)
            writer.writeBits(2, 0) // type 0 = just flags
        } else {
            // Nothing at all
            writer.writeBits(1, 0)
        }
    }

    /**
     * Write movement bits for another player in the local list.
     */
    private fun updateOtherPlayerMovement(writer: BitWriter, other: Player) {
        val queue = other.walkingQueue

        if (queue.secondaryDirection != Direction.NONE) {
            writer.writeBits(1, 1)
            writer.writeBits(2, 2)
            writer.writeBits(3, directionToClient(queue.primaryDirection))
            writer.writeBits(3, directionToClient(queue.secondaryDirection))
            writer.writeBits(1, if (other.updateRequired) 1 else 0)
        } else if (queue.primaryDirection != Direction.NONE) {
            writer.writeBits(1, 1)
            writer.writeBits(2, 1)
            writer.writeBits(3, directionToClient(queue.primaryDirection))
            writer.writeBits(1, if (other.updateRequired) 1 else 0)
        }
    }

    /**
     * Write bits to add a new player to the local list.
     */
    private fun addNewPlayer(writer: BitWriter, player: Player, other: Player) {
        writer.writeBits(11, other.index)

        // Relative position
        var dx = other.position.x - player.position.x
        var dy = other.position.y - player.position.y
        if (dx < 0) dx += 32
        if (dy < 0) dy += 32

        writer.writeBits(1, 1)  // update required (always for new adds)
        writer.writeBits(1, 1)  // discard walking queue
        writer.writeBits(5, dy)
        writer.writeBits(5, dx)
    }

    /**
     * Append the byte-level update block for a player.
     * This includes appearance, chat, animation, hit, etc.
     */
    private fun appendUpdateBlock(writer: BitWriter, player: Player) {
        var mask = 0

        if (player.graphicUpdateRequired)     mask = mask or FLAG_GRAPHIC
        if (player.animationUpdateRequired)   mask = mask or FLAG_ANIMATION
        if (player.forceChatUpdateRequired)    mask = mask or FLAG_FORCED_CHAT
        if (player.chatUpdateRequired)         mask = mask or FLAG_CHAT
        if (player.faceEntityUpdateRequired)   mask = mask or FLAG_FACE_ENTITY
        if (player.appearanceUpdateRequired)   mask = mask or FLAG_APPEARANCE
        if (player.facePositionUpdateRequired) mask = mask or FLAG_FACE_POSITION
        if (player.hitUpdateRequired)          mask = mask or FLAG_HIT_1
        if (player.hit2UpdateRequired)         mask = mask or FLAG_HIT_2

        // Write mask (extended if > 0xFF)
        if (mask >= 0x100) {
            mask = mask or 0x40
            writer.writeByte(mask and 0xFF)
            writer.writeByte(mask shr 8)
        } else {
            writer.writeByte(mask)
        }

        // === Graphic ===
        // Client: method434() [LE short] + readDWord() [4-byte BE int]
        if (player.graphicUpdateRequired) {
            writer.writeLEShort(player.graphicId)
            writer.writeInt((player.graphicHeight shl 16) or player.graphicDelay)
        }

        // === Animation ===
        if (player.animationUpdateRequired) {
            writer.writeLEShort(player.currentAnimation)
            writer.writeByteC(player.animationDelay)
        }

        // === Forced chat ===
        if (player.forceChatUpdateRequired) {
            for (ch in player.forceChatMessage) {
                writer.writeByte(ch.code)
            }
            writer.writeByte(10) // terminator
        }

        // === Chat ===
        if (player.chatUpdateRequired) {
            writer.writeLEShort(((player.chatColor and 0xFF) shl 8) or (player.chatEffects and 0xFF))
            writer.writeByte(player.rights.value)
            writer.writeByteC(player.chatMessage.size)
            // Chat bytes are sent in reverse
            for (i in player.chatMessage.size - 1 downTo 0) {
                writer.writeByte(player.chatMessage[i].toInt())
            }
        }

        // === Face entity ===
        if (player.faceEntityUpdateRequired) {
            writer.writeLEShort(player.faceEntityIndex)
        }

        // === Appearance ===
        if (player.appearanceUpdateRequired) {
            val block = AppearanceBuilder.build(player)
            writer.writeByteC(block.size)
            writer.writeBytes(block)
        }

        // === Face position ===
        // Client: method436() [LE short A], method434() [LE short]
        if (player.facePositionUpdateRequired) {
            writer.writeLEShortA(player.faceX * 2 + 1)
            writer.writeLEShort(player.faceY * 2 + 1)
        }

        // === Hit 1 ===
        if (player.hitUpdateRequired) {
            writer.writeByte(player.hitDamage1)
            writer.writeByteA(player.hitType1)
            writer.writeByteC(player.currentHealth)
            writer.writeByte(player.maxHealth)
        }

        // === Hit 2 ===
        // Client: readUnsignedByte() [damage], method428() [type = byte S],
        //         readUnsignedByte() [health], method427() [maxHealth = byte C]
        if (player.hit2UpdateRequired) {
            writer.writeByte(player.hitDamage2)
            writer.writeByteS(player.hitType2)
            writer.writeByte(player.currentHealth)
            writer.writeByteC(player.maxHealth)
        }
    }

    /**
     * Convert a server Direction enum to the 317 client direction index.
     * Client uses: 0=NW, 1=N, 2=NE, 3=W, 4=E, 5=SW, 6=S, 7=SE
     */
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
