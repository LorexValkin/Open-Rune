package com.openrune.core.net.handler

import com.openrune.api.event.*
import com.openrune.api.world.Position
import com.openrune.core.net.codec.IncomingPacket
import com.openrune.core.world.PlayerManager
import org.slf4j.LoggerFactory

/**
 * Translates incoming raw packets into typed [GameEvent]s and emits them.
 *
 * This is the bridge between the network protocol and the plugin event system.
 * Plugins never see raw packets; they only see high-level events.
 *
 * Each packet opcode maps to a translator function that reads the payload
 * and produces a GameEvent.
 */
class PacketDispatcher(
    private val eventBus: EventBus,
    private val playerManager: PlayerManager
) {

    private val log = LoggerFactory.getLogger(PacketDispatcher::class.java)

    /**
     * NPC lookup function, set by the engine after construction.
     * Resolves an NPC index to a NpcRef for event emission.
     */
    var npcLookup: ((Int) -> com.openrune.api.entity.NpcRef?)? = null

    // Opcode -> translator function
    private val translators = mutableMapOf<Int, PacketTranslator>()

    fun interface PacketTranslator {
        fun translate(playerIndex: Int, packet: IncomingPacket)
    }

    init {
        registerDefaults()
    }

    /**
     * Register all standard 317 packet translators.
     */
    private fun registerDefaults() {
        // ::command
        register(103) { index, pkt ->
            val player = playerManager.getByIndex(index) ?: return@register
            val input = pkt.readString(0)
            val parts = input.split(" ")
            val command = parts[0].lowercase()
            val args = if (parts.size > 1) parts.subList(1, parts.size) else emptyList()
            eventBus.emit(CommandEvent(player, command, args))
        }

        // Regular chat
        register(4) { index, pkt ->
            val player = playerManager.getByIndex(index) ?: return@register
            val effects = pkt.readByte(0)
            val color = pkt.readByte(1)
            val msgLength = pkt.size - 2
            val compressed = pkt.payload.copyOfRange(2, 2 + msgLength)
            val message = decompressChat(compressed, msgLength)
            eventBus.emit(ChatEvent(player, message, effects, color))
        }

        // Walking
        registerWalking(164)
        registerWalking(248)
        registerWalking(98)

        // Click object (first click=132, second=252, third=70)
        registerObjectClick(132, 1)
        registerObjectClick(252, 2)
        registerObjectClick(70, 3)

        // NPC interactions - each opcode uses a different byte encoding
        // Verified against client.java doAction() write methods
        // Opcode 155: first click, client writes LEShort (method431)
        register(155) { index, pkt ->
            val player = playerManager.getByIndex(index) ?: return@register
            val npcIndex = pkt.readLEShort(0)
            val npc = npcLookup?.invoke(npcIndex) ?: return@register
            eventBus.emit(NpcInteractEvent(player, npc, 1))
        }
        // Opcode 72: second click / attack, client writes ShortA (method432)
        register(72) { index, pkt ->
            val player = playerManager.getByIndex(index) ?: return@register
            val npcIndex = pkt.readShortA(0)
            val npc = npcLookup?.invoke(npcIndex) ?: return@register
            eventBus.emit(NpcInteractEvent(player, npc, 2))
        }
        // Opcode 17: third click, client writes LEShortA (method433)
        register(17) { index, pkt ->
            val player = playerManager.getByIndex(index) ?: return@register
            val npcIndex = pkt.readLEShortA(0)
            val npc = npcLookup?.invoke(npcIndex) ?: return@register
            eventBus.emit(NpcInteractEvent(player, npc, 3))
        }
        // Opcode 21: fourth click, client writes Short (writeWord)
        register(21) { index, pkt ->
            val player = playerManager.getByIndex(index) ?: return@register
            val npcIndex = pkt.readShort(0)
            val npc = npcLookup?.invoke(npcIndex) ?: return@register
            eventBus.emit(NpcInteractEvent(player, npc, 4))
        }
        // Opcode 18: fifth click, client writes LEShort (method431)
        register(18) { index, pkt ->
            val player = playerManager.getByIndex(index) ?: return@register
            val npcIndex = pkt.readLEShort(0)
            val npc = npcLookup?.invoke(npcIndex) ?: return@register
            eventBus.emit(NpcInteractEvent(player, npc, 5))
        }
        // Opcode 131: magic on NPC, client writes LEShortA(npcIndex) + ShortA(spellId)
        register(131) { index, pkt ->
            val player = playerManager.getByIndex(index) ?: return@register
            val npcIndex = pkt.readLEShortA(0)
            val spellId = pkt.readShortA(2)
            val npc = npcLookup?.invoke(npcIndex) ?: return@register
            eventBus.emit(NpcInteractEvent(player, npc, 6))
        }

        // Item clicks
        register(122) { index, pkt -> // first click
            val player = playerManager.getByIndex(index) ?: return@register
            val interfaceId = pkt.readLEShortA(0)
            val slot = pkt.readShortA(2)
            val itemId = pkt.readLEShort(4)
            eventBus.emit(ItemClickEvent(player, itemId, slot, 1))
        }

        register(16) { index, pkt -> // second click
            val player = playerManager.getByIndex(index) ?: return@register
            val itemId = pkt.readShortA(0)
            val slot = pkt.readLEShortA(2)
            val interfaceId = pkt.readLEShortA(4)
            eventBus.emit(ItemClickEvent(player, itemId, slot, 2))
        }

        register(75) { index, pkt -> // third click
            val player = playerManager.getByIndex(index) ?: return@register
            val interfaceId = pkt.readLEShort(0)
            val slot = pkt.readLEShortA(2)
            val itemId = pkt.readShortA(4)
            eventBus.emit(ItemClickEvent(player, itemId, slot, 3))
        }

        // Drop item
        register(87) { index, pkt ->
            val player = playerManager.getByIndex(index) ?: return@register
            val itemId = pkt.readShortA(0)
            val interfaceId = pkt.readShort(2)
            val slot = pkt.readShortA(4)
            eventBus.emit(ItemDropEvent(player, itemId, slot))
        }

        // Pickup ground item
        register(236) { index, pkt ->
            val player = playerManager.getByIndex(index) ?: return@register
            val y = pkt.readLEShort(0)
            val itemId = pkt.readShort(2)
            val x = pkt.readLEShort(4)
            eventBus.emit(ItemPickupEvent(player, itemId, Position(x, y)))
        }

        // Equip item
        register(41) { index, pkt ->
            val player = playerManager.getByIndex(index) ?: return@register
            val itemId = pkt.readShort(0)
            val slot = pkt.readShortA(2)
            val interfaceId = pkt.readShortA(4)
            eventBus.emit(ItemEquipEvent(player, itemId, slot))
        }

        // Button click
        register(185) { index, pkt ->
            val player = playerManager.getByIndex(index) ?: return@register
            val buttonId = pkt.readShort(0)
            eventBus.emit(ButtonClickEvent(player, buttonId))
        }

        // Item on item
        register(53) { index, pkt ->
            val player = playerManager.getByIndex(index) ?: return@register
            val usedSlot = pkt.readShort(0)
            val targetSlot = pkt.readShortA(2)
            val usedItemId = pkt.readLEShort(4)
            val interfaceId1 = pkt.readShort(6)
            val targetItemId = pkt.readLEShortA(8)
            val interfaceId2 = pkt.readShort(10)
            eventBus.emit(ItemOnItemEvent(player, usedItemId, usedSlot, targetItemId, targetSlot))
        }

        // Item on object
        register(192) { index, pkt ->
            // Variable format; simplified
        }

        // Item on NPC
        register(57) { index, pkt ->
            val player = playerManager.getByIndex(index) ?: return@register
            val itemId = pkt.readShortA(0)
            val npcIndex = pkt.readShortA(2)
            val slot = pkt.readLEShort(4)
            val interfaceId = pkt.readShort(6)
            // NPC ref lookup would go here
        }

        // Idle / keepalive / camera / focus / misc client packets (silent)
        for (op in intArrayOf(
            0, 3, 202, 77, 86, 78, 36, 226, 246, 148, 183, 230, 136, 189, 152, 200, 85, 165, 238, 150,
            // Post-login client packets that were previously unhandled:
            // 71=camera angle, 209=idle/region, 64=camera reset, 63=button/focus,
            // 55=idle, 124=camera, 15=focus, 82=region loaded, 134=idle/client focus,
            // 27=idle, 154=interface close, 160=idle/anticheat
            71, 209, 64, 63, 55, 124, 15, 82, 134, 27, 154, 160, 121,
            // Additional post-login client packets:
            241, 100, 206, 146, 140, 242, 80, 93, 251, 23, 227, 182, 113, 35
        )) {
            register(op) { _, _ -> } // No-op
        }
    }

    private fun registerWalking(opcode: Int) {
        register(opcode) { index, pkt ->
            val player = playerManager.getByIndex(index) ?: return@register
            var size = pkt.size
            if (size < 5) return@register

            // PI: For opcode 248 (minimap click), subtract 14 bytes of anticheat data
            if (opcode == 248) size -= 14

            // Number of waypoints: (size - 5) / 2, plus the destination itself = +1
            val numSteps = (size - 5) / 2 + 1

            // Read destination X (first in packet), then intermediate deltas, then Y, then running
            // PI layout (sequential reads):
            //   firstX = readSignedWordBigEndianA() [LEShortA]
            //   for i in 1..n: deltaX[i] = readSignedByte(), deltaY[i] = readSignedByte()
            //   firstY = readSignedWordBigEndian() [LEShort]
            //   running = readSignedByteC()
            val firstX = pkt.readLEShortA(0)  // world X of destination

            // Read intermediate waypoint deltas (bytes at offsets 2..size-4)
            val waypointX = IntArray(numSteps)
            val waypointY = IntArray(numSteps)
            waypointX[0] = 0
            waypointY[0] = 0
            for (i in 1 until numSteps) {
                waypointX[i] = pkt.readSignedByte(2 + (i - 1) * 2)
                waypointY[i] = pkt.readSignedByte(2 + (i - 1) * 2 + 1)
            }

            val firstY = pkt.readLEShort(size - 3)  // world Y of destination
            val ctrlRunning = pkt.readByteC(size - 1) == 1

            // Convert to absolute local coords (relative to mapRegion base)
            // PI: mapRegionX = (absX>>3)-6, local = world - mapRegionX*8
            // Our lastRegion stores the world pos, so mapRegionBase = ((lastRegion.x>>3)-6)*8
            val mapBaseX = ((player.lastRegion.x shr 3) - 6) * 8
            val mapBaseY = ((player.lastRegion.y shr 3) - 6) * 8
            val localDestX = firstX - mapBaseX
            val localDestY = firstY - mapBaseY

            // All waypoints are relative to the destination
            for (i in 0 until numSteps) {
                waypointX[i] += localDestX
                waypointY[i] += localDestY
            }

            // Emit event
            val worldTo = Position(firstX, firstY, player.position.z)
            val event = eventBus.emit(PlayerMoveEvent(player, player.position, worldTo,
                ctrlRunning || player.walkingQueue.running))
            if (event.cancelled) return@register

            if (ctrlRunning) {
                player.walkingQueue.running = true
            }

            // Reset walking queue and fill with interpolated steps
            // PI: resetWalkingQueue() then addToWalkingQueue for each waypoint
            val queue = player.walkingQueue
            queue.clear()

            // Current position in local coords
            val curLocalX = player.position.x - mapBaseX
            val curLocalY = player.position.y - mapBaseY

            // Interpolate steps from current pos through all waypoints
            // Waypoints are direction-change points; fill in tile-by-tile steps between them
            var fromX = curLocalX
            var fromY = curLocalY

            for (i in 0 until numSteps) {
                val toX = waypointX[i]
                val toY = waypointY[i]

                // Walk tile by tile from (fromX,fromY) toward (toX,toY)
                while (fromX != toX || fromY != toY) {
                    val dx = (toX - fromX).coerceIn(-1, 1)
                    val dy = (toY - fromY).coerceIn(-1, 1)
                    fromX += dx
                    fromY += dy
                    queue.addStep(Position(fromX + mapBaseX, fromY + mapBaseY, player.position.z))
                }
            }
            // Don't set walkTarget -- no A* pathfinding needed
                player.name, numSteps, queue.stepCount(),
                player.position.x, player.position.y,
                firstX, firstY)
        }
    }

    private fun registerObjectClick(opcode: Int, option: Int) {
        register(opcode) { index, pkt ->
            val player = playerManager.getByIndex(index) ?: return@register
            // Read varies by opcode, but generally: x, y, objectId
            val x: Int
            val y: Int
            val objectId: Int

            when (opcode) {
                132 -> {
                    x = pkt.readLEShortA(0)
                    objectId = pkt.readShort(2)
                    y = pkt.readShortA(4)
                }
                252 -> {
                    objectId = pkt.readLEShortA(0)
                    y = pkt.readLEShort(2)
                    x = pkt.readShortA(4)
                }
                70 -> {
                    x = pkt.readLEShort(0)
                    y = pkt.readShort(2)
                    objectId = pkt.readLEShortA(4)
                }
                else -> return@register
            }

            eventBus.emit(ObjectInteractEvent(player, objectId, Position(x, y, player.position.z), option))
        }
    }

    /**
     * Register a custom packet translator. Plugins can call this to handle
     * custom or modified packets.
     */
    fun register(opcode: Int, translator: PacketTranslator) {
        translators[opcode] = translator
    }

    /**
     * Dispatch an incoming packet for a given player index.
     */
    fun dispatch(playerIndex: Int, packet: IncomingPacket) {
        val translator = translators[packet.opcode]
        if (translator != null) {
            try {
                translator.translate(playerIndex, packet)
            } catch (e: Exception) {
                log.error("Error handling packet opcode {} for player {}", packet.opcode, playerIndex, e)
            }
        } else {
            log.debug("Unhandled packet opcode: {} size: {}", packet.opcode, packet.size)
        }
    }

    /**
     * Simple chat decompression for 317.
     * The 317 client uses a basic Huffman-like compression.
     */
    private fun decompressChat(data: ByteArray, length: Int): String {
        val chars = CharArray(256)
        var charIdx = 0
        var bitPos = 0

        for (i in 0 until length) {
            val value = data[i].toInt() and 0xFF
            var bit = 1 shl 7
            while (bit != 0) {
                // Simplified: for now just treat as raw ASCII
                bit = bit shr 1
            }
        }

        // Fallback: direct ASCII read
        val sb = StringBuilder()
        for (b in data) {
            val c = b.toInt() and 0xFF
            if (c in 32..126) sb.append(c.toChar())
        }
        return sb.toString()
    }
}