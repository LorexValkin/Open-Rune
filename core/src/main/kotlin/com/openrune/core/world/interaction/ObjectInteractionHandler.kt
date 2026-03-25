package com.openrune.core.world.interaction

import com.openrune.api.event.EventPriority
import com.openrune.api.event.ObjectInteractEvent
import com.openrune.api.event.on
import com.openrune.api.world.Position
import com.openrune.core.event.EventBusImpl
import com.openrune.core.net.codec.PacketBuilder
import com.openrune.core.world.Player
import com.openrune.core.world.PlayerManager
import org.slf4j.LoggerFactory

/**
 * ENGINE-LEVEL handler for world object interactions.
 *
 * Doors, stairs, and ladders are world infrastructure, not content plugins.
 * This handler registers at LOW priority so content plugins can override
 * or cancel specific interactions at NORMAL priority.
 *
 * Door system:
 *   - Data-driven closed/open ID pairs loaded from JSON
 *   - Rotation-based position shifting for wall-type doors
 *   - Auto-close timer (configurable per door)
 *   - State tracked per world position
 *
 * Stair/Ladder system:
 *   - Data-driven definitions loaded from JSON
 *   - Supports height-change (z +/- 1) and fixed-destination teleports
 *   - Climb animation
 */
class ObjectInteractionHandler(
    private val eventBus: EventBusImpl,
    private val playerManager: PlayerManager
) {

    private val log = LoggerFactory.getLogger(ObjectInteractionHandler::class.java)

    /** Object IDs known to be trees or rocks — skip stair/door handling for these. */
    private val gatheringObjectIds = mutableSetOf<Int>()

    /** Load tree/rock IDs from the DataStore so we don't confuse them with stairs. */
    fun loadGatheringIds(dataStore: com.openrune.api.config.DataStore) {
        gatheringObjectIds.clear()
        for ((_, json) in dataStore.getAll("trees")) {
            json.get("id")?.asInt?.let { gatheringObjectIds.add(it) }
        }
        for ((_, json) in dataStore.getAll("rocks")) {
            json.get("id")?.asInt?.let { gatheringObjectIds.add(it) }
        }
        log.info("Loaded {} gathering object IDs (trees + rocks) for conflict avoidance", gatheringObjectIds.size)
    }

    // ================================================================
    //  Door definitions
    // ================================================================

    /**
     * A door pair: closed ID <-> open ID.
     * [autoCloseTicks] = -1 means it stays open until clicked again.
     */
    data class DoorDef(
        val closedId: Int,
        val openId: Int,
        val autoCloseTicks: Int = 10   // ~6 seconds at 600ms ticks
    )

    /** All known door pairs, keyed by BOTH closed and open IDs for fast lookup. */
    private val doorLookup = mutableMapOf<Int, DoorDef>()

    /** Currently open doors, keyed by their ORIGINAL (closed) position. */
    private val openDoors = mutableMapOf<Long, OpenDoorState>()

    private data class OpenDoorState(
        val def: DoorDef,
        val closedPos: Position,
        val closedRotation: Int,
        val closedType: Int,
        val openPos: Position,
        val openRotation: Int,
        var ticksRemaining: Int
    )

    // ================================================================
    //  Stair / Ladder definitions
    // ================================================================

    data class StairDef(
        val objectId: Int,
        val action: StairAction,
        val destX: Int = -1,
        val destY: Int = -1,
        val destZ: Int = -1
    )

    enum class StairAction {
        CLIMB_UP,       // z + 1
        CLIMB_DOWN,     // z - 1
        TELEPORT        // fixed destination
    }

    private val stairLookup = mutableMapOf<Int, StairDef>()

    // ================================================================
    //  Initialization
    // ================================================================

    fun initialize() {
        loadDoorDefaults()
        loadStairDefaults()

        // Register at LOW priority so plugins can override at NORMAL
        eventBus.subscribe(
            ObjectInteractEvent::class,
            priority = EventPriority.LOW,
            owner = "core:object-interaction"
        ) { event ->
            if (!event.cancelled) {
                handleObjectInteraction(event)
            }
        }

        log.info("Object interaction handler: {} doors, {} stairs/ladders",
            doorLookup.size / 2, stairLookup.size)
    }

    // ================================================================
    //  Event handler
    // ================================================================

    private fun handleObjectInteraction(event: ObjectInteractEvent) {
        val player = event.player as? Player ?: return
        val objectId = event.objectId
        val position = event.position
        val option = event.option

        // Debug trace for diagnosing willow/climb issue (temporary — revert to log.debug after fix)
        log.info("[OBJECT CLICK] id={} option={} pos={} inGathering={} inStairs={} inDoors={}",
            objectId, option, position,
            gatheringObjectIds.contains(objectId),
            stairLookup.containsKey(objectId),
            doorLookup.containsKey(objectId))

        // Skip objects that are known trees/rocks — let the skills plugin handle them
        if (gatheringObjectIds.contains(objectId)) return

        // Try door interaction (option 1 = "Open" / "Close")
        if (option == 1 && doorLookup.containsKey(objectId)) {
            handleDoor(player, objectId, position)
            event.cancel()
            return
        }

        // Try stair/ladder interaction (option 1 = "Climb-up", "Climb-down", "Climb")
        if (option == 1 && stairLookup.containsKey(objectId)) {
            handleStair(player, stairLookup[objectId]!!)
            event.cancel()
            return
        }
    }

    // ================================================================
    //  Door logic
    // ================================================================

    private fun handleDoor(player: Player, objectId: Int, position: Position) {
        val def = doorLookup[objectId] ?: return

        // Check if this is an already-open door being closed
        val openKey = packPosition(position)
        val existingOpen = openDoors.values.find { it.openPos == position }

        if (existingOpen != null) {
            // Close the door: restore original
            closeDoor(existingOpen)
            return
        }

        // Check if this door is already open at this closed position
        val closedKey = packPosition(position)
        if (openDoors.containsKey(closedKey)) {
            // Already open, close it
            closeDoor(openDoors[closedKey]!!)
            return
        }

        // Open the door
        // Standard 317 door: type 0 (wall), rotation determines facing
        // We infer the rotation from which side the player is on
        val rotation = inferDoorRotation(player, position)
        openDoor(def, position, rotation, 0) // type 0 = wall
    }

    /**
     * Infer the original rotation of a door based on player position relative to the door.
     *
     * Without cache object defs, we approximate:
     *   - Player south of door → door faces south (rotation 0)
     *   - Player west of door  → door faces west  (rotation 1)
     *   - Player north of door → door faces north (rotation 2)
     *   - Player east of door  → door faces east  (rotation 3)
     */
    private fun inferDoorRotation(player: Player, doorPos: Position): Int {
        val dx = player.position.x - doorPos.x
        val dy = player.position.y - doorPos.y

        return when {
            dy < 0  -> 0  // Player is south
            dx < 0  -> 1  // Player is west
            dy > 0  -> 2  // Player is north
            dx > 0  -> 3  // Player is east
            else    -> 0  // On top of door, default south
        }
    }

    private fun openDoor(def: DoorDef, closedPos: Position, closedRotation: Int, closedType: Int) {
        val openRotation = (closedRotation + 1) and 3

        // Calculate the open position: door swings to adjacent tile
        val openPos = when (closedRotation) {
            0 -> Position(closedPos.x - 1, closedPos.y, closedPos.z)
            1 -> Position(closedPos.x, closedPos.y + 1, closedPos.z)
            2 -> Position(closedPos.x + 1, closedPos.y, closedPos.z)
            3 -> Position(closedPos.x, closedPos.y - 1, closedPos.z)
            else -> closedPos
        }

        val state = OpenDoorState(
            def = def,
            closedPos = closedPos,
            closedRotation = closedRotation,
            closedType = closedType,
            openPos = openPos,
            openRotation = openRotation,
            ticksRemaining = def.autoCloseTicks
        )

        openDoors[packPosition(closedPos)] = state

        // Send packets to all nearby players
        for (p in playerManager.allPlayers()) {
            if (p.position.isWithinDistance(closedPos, 60)) {
                sendRemoveObject(p, closedPos, closedType, closedRotation)
                sendSpawnObject(p, openPos, def.openId, closedType, openRotation)
            }
        }

        log.debug("Door opened: {} at {} → {} at {}", def.closedId, closedPos, def.openId, openPos)
    }

    private fun closeDoor(state: OpenDoorState) {
        openDoors.remove(packPosition(state.closedPos))

        // Send packets to all nearby players
        for (p in playerManager.allPlayers()) {
            if (p.position.isWithinDistance(state.closedPos, 60)) {
                sendRemoveObject(p, state.openPos, state.closedType, state.openRotation)
                sendSpawnObject(p, state.closedPos, state.def.closedId, state.closedType, state.closedRotation)
            }
        }

        log.debug("Door closed: {} at {}", state.def.closedId, state.closedPos)
    }

    // ================================================================
    //  Stair / Ladder logic
    // ================================================================

    private fun handleStair(player: Player, def: StairDef) {
        val dest = when (def.action) {
            StairAction.CLIMB_UP -> {
                player.sendMessage("You climb up the stairs.")
                Position(player.position.x, player.position.y, player.position.z + 1)
            }
            StairAction.CLIMB_DOWN -> {
                player.sendMessage("You climb down the stairs.")
                Position(player.position.x, player.position.y, player.position.z - 1)
            }
            StairAction.TELEPORT -> {
                if (def.destZ > player.position.z) {
                    player.sendMessage("You climb up.")
                } else {
                    player.sendMessage("You climb down.")
                }
                Position(def.destX, def.destY, def.destZ)
            }
        }

        // Validate destination height
        if (dest.z < 0 || dest.z > 3) {
            player.sendMessage("You can't go that way.")
            return
        }

        // Climb animation (828 = climb up, 827 = climb down)
        val animId = if (dest.z > player.position.z) 828 else 827
        player.animate(animId)

        player.teleport(dest)
        log.debug("Player {} used stair {} → {}", player.name, def.objectId, dest)
    }

    // ================================================================
    //  Tick processing (auto-close doors)
    // ================================================================

    /**
     * Called once per game tick from GameEngine.
     * Handles auto-closing doors whose timer has expired.
     */
    fun process() {
        val iterator = openDoors.entries.iterator()
        while (iterator.hasNext()) {
            val (_, state) = iterator.next()
            if (state.def.autoCloseTicks > 0) {
                state.ticksRemaining--
                if (state.ticksRemaining <= 0) {
                    // Auto-close: send packets and remove
                    for (p in playerManager.allPlayers()) {
                        if (p.position.isWithinDistance(state.closedPos, 60)) {
                            sendRemoveObject(p, state.openPos, state.closedType, state.openRotation)
                            sendSpawnObject(p, state.closedPos, state.def.closedId, state.closedType, state.closedRotation)
                        }
                    }
                    iterator.remove()
                }
            }
        }
    }

    // ================================================================
    //  Packet construction
    // ================================================================

    /**
     * Send opcode 85 (set reference position) + opcode 101 (remove object).
     *
     * Client reads opcode 85 as:
     *   anInt1269 = method427() → readByteC (Y local)
     *   anInt1268 = method427() → readByteC (X local)
     *
     * Client reads opcode 101 as:
     *   byte1 = method427() → readByteC → (type << 2) | rotation
     *   byte2 = readUnsignedByte() → offset from reference
     */
    private fun sendRemoveObject(player: Player, pos: Position, type: Int, rotation: Int) {
        // Calculate local coordinates within the 104-tile loaded area
        val baseX = ((player.lastRegion.x shr 3) - 6) * 8
        val baseY = ((player.lastRegion.y shr 3) - 6) * 8
        val localX = pos.x - baseX
        val localY = pos.y - baseY

        if (localX < 0 || localX >= 104 || localY < 0 || localY >= 104) return

        // Opcode 85: set reference position
        val ref = PacketBuilder(85)
        ref.addByteC(localY)
        ref.addByteC(localX)
        player.send(ref)

        // Opcode 101: remove object (offset 0,0 since reference IS the target)
        val remove = PacketBuilder(101)
        remove.addByteC((type shl 2) or (rotation and 3))
        remove.addByte(0)  // offset = (dx << 4) | dy, both 0
        player.send(remove)
    }

    /**
     * Send opcode 85 (set reference position) + opcode 151 (spawn object).
     *
     * Client reads opcode 151 as:
     *   byte1 = method426() → readByteA → offset from reference
     *   short = method434() → readLEShort → objectId
     *   byte2 = method428() → readByteS → (type << 2) | rotation
     */
    private fun sendSpawnObject(player: Player, pos: Position, objectId: Int, type: Int, rotation: Int) {
        val baseX = ((player.lastRegion.x shr 3) - 6) * 8
        val baseY = ((player.lastRegion.y shr 3) - 6) * 8
        val localX = pos.x - baseX
        val localY = pos.y - baseY

        if (localX < 0 || localX >= 104 || localY < 0 || localY >= 104) return

        // Opcode 85: set reference position
        val ref = PacketBuilder(85)
        ref.addByteC(localY)
        ref.addByteC(localX)
        player.send(ref)

        // Opcode 151: spawn object (offset 0,0)
        val spawn = PacketBuilder(151)
        spawn.addByteA(0)  // offset = (dx << 4) | dy, both 0
        spawn.addLEShort(objectId)
        spawn.addByteS((type shl 2) or (rotation and 3))
        player.send(spawn)
    }

    // ================================================================
    //  Default data (common 317 doors, stairs, ladders)
    // ================================================================

    private fun loadDoorDefaults() {
        // Common 317 door pairs: closedId <-> openId
        // These are the standard single doors found throughout the world
        val pairs = listOf(
            // Lumbridge / general
            1530 to 1531, 1533 to 1534, 1516 to 1517, 1519 to 1520,
            1536 to 1537, 1539 to 1540,
            // Standard wooden doors
            1804 to 1805, 1806 to 1807, 1808 to 1809, 1810 to 1811,
            // Metal gates
            1551 to 1552, 1553 to 1554, 1555 to 1556, 1557 to 1558,
            // Generic doors
            1967 to 1968, 1969 to 1970, 1971 to 1972, 1973 to 1974,
            // Al Kharid gate
            2882 to 2883,
            // Taverly gate
            2623 to 2624,
            // General doors
            11707 to 11708, 11709 to 11710, 11711 to 11712, 11713 to 11714,
            // Varrock
            2112 to 2113, 2114 to 2115, 2116 to 2117,
            // More standard doors
            1543 to 1544, 1545 to 1546,
            // Large doors
            1560 to 1561, 1562 to 1563, 1564 to 1565, 1596 to 1597,
        )

        for ((closed, open) in pairs) {
            val def = DoorDef(closedId = closed, openId = open)
            doorLookup[closed] = def
            doorLookup[open] = def
        }
    }

    private fun loadStairDefaults() {
        // Standard 317 staircases - climb up (z + 1)
        val climbUp = listOf(
            // Regular staircases
            1722, 1723, 1725, 1727, 1729, 1731, 1733, 1735,
            // Lumbridge castle stairs
            1738, 1740,
            // Spiral staircases
            2113,
        )
        for (id in climbUp) {
            stairLookup[id] = StairDef(id, StairAction.CLIMB_UP)
        }

        // Standard 317 staircases - climb down (z - 1)
        val climbDown = listOf(
            1724, 1726, 1728, 1730, 1732, 1734, 1736, 1737,
            1739, 1741,
        )
        for (id in climbDown) {
            stairLookup[id] = StairDef(id, StairAction.CLIMB_DOWN)
        }

        // Ladders - climb up
        // NOTE: 1750, 1756, 1760 removed — they are trees in this cache, not ladders.
        //       The gathering check (gatheringObjectIds) takes priority anyway,
        //       but removing avoids confusion.
        val laddersUp = listOf(
            1746, 1748, 1752, 1754, 1758, 1762, 1764,
            2884,
        )
        for (id in laddersUp) {
            stairLookup[id] = StairDef(id, StairAction.CLIMB_UP)
        }

        // Ladders - climb down
        // NOTE: 1751 removed — it is a tree in this cache, not a ladder.
        val laddersDown = listOf(
            1747, 1749, 1753, 1755, 1757, 1759, 1761, 1763, 1765,
            2885,
        )
        for (id in laddersDown) {
            stairLookup[id] = StairDef(id, StairAction.CLIMB_DOWN)
        }
    }

    // ================================================================
    //  Public API for adding definitions at runtime
    // ================================================================

    /**
     * Register a door pair. Call from plugins or data loaders.
     */
    fun registerDoor(closedId: Int, openId: Int, autoCloseTicks: Int = 10) {
        val def = DoorDef(closedId, openId, autoCloseTicks)
        doorLookup[closedId] = def
        doorLookup[openId] = def
    }

    /**
     * Register a stair or ladder. Call from plugins or data loaders.
     */
    fun registerStair(objectId: Int, action: StairAction, destX: Int = -1, destY: Int = -1, destZ: Int = -1) {
        stairLookup[objectId] = StairDef(objectId, action, destX, destY, destZ)
    }

    fun doorCount(): Int = doorLookup.size / 2
    fun stairCount(): Int = stairLookup.size
    fun gatheringIdCount(): Int = gatheringObjectIds.size
    fun isGatheringObject(id: Int): Boolean = gatheringObjectIds.contains(id)

    // ================================================================
    //  Utility
    // ================================================================

    private fun packPosition(pos: Position): Long =
        (pos.x.toLong() shl 32) or (pos.y.toLong() shl 16) or pos.z.toLong()
}
