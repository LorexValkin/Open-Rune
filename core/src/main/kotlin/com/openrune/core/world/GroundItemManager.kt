package com.openrune.core.world

import com.openrune.api.world.Position
import com.openrune.core.net.codec.PacketBuilder
import org.slf4j.LoggerFactory

/**
 * A ground item visible in the game world.
 */
data class GroundItem(
    val itemId: Int,
    val amount: Int,
    val position: Position,
    /** Player name who dropped it (null = global). */
    val owner: String? = null,
    /** Tick when this was placed. */
    val spawnTick: Long = 0,
    /** Ticks until this becomes visible to everyone (0 = already global). */
    var privateTimer: Int = 100,
    /** Ticks until this despawns entirely. */
    var despawnTimer: Int = 300
)

/**
 * Manages ground items: dropping, picking up, visibility transitions, despawning.
 *
 * Ground items start private (visible only to the dropper), then become
 * global after [privateTimer] ticks, then despawn after [despawnTimer].
 *
 * ENGINE-LEVEL system.
 */
class GroundItemManager {

    private val log = LoggerFactory.getLogger(GroundItemManager::class.java)

    private val items = mutableListOf<GroundItem>()

    /**
     * Drop a ground item at a position, owned by a player.
     */
    fun drop(itemId: Int, amount: Int, position: Position, owner: String?, tick: Long): GroundItem {
        val item = GroundItem(
            itemId = itemId,
            amount = amount,
            position = position,
            owner = owner,
            spawnTick = tick
        )
        items.add(item)
        return item
    }

    /**
     * Spawn a global ground item (e.g. NPC drops).
     */
    fun spawnGlobal(itemId: Int, amount: Int, position: Position, tick: Long, despawnTicks: Int = 300): GroundItem {
        val item = GroundItem(
            itemId = itemId,
            amount = amount,
            position = position,
            owner = null,
            spawnTick = tick,
            privateTimer = 0,
            despawnTimer = despawnTicks
        )
        items.add(item)
        return item
    }

    /**
     * Remove a ground item (picked up).
     */
    fun remove(itemId: Int, position: Position, playerName: String?): GroundItem? {
        val item = items.firstOrNull {
            it.itemId == itemId && it.position == position &&
            (it.owner == null || it.owner == playerName || it.privateTimer <= 0)
        }
        if (item != null) {
            items.remove(item)
        }
        return item
    }

    /**
     * Get all ground items visible to a specific player at a position.
     */
    fun getVisibleItems(position: Position, playerName: String): List<GroundItem> {
        return items.filter {
            it.position == position &&
            (it.owner == null || it.owner == playerName || it.privateTimer <= 0)
        }
    }

    /**
     * Get all ground items near a position (for region updates).
     */
    fun getItemsNear(position: Position, range: Int = 60, playerName: String): List<GroundItem> {
        return items.filter {
            it.position.isWithinDistance(position, range) &&
            (it.owner == null || it.owner == playerName || it.privateTimer <= 0)
        }
    }

    /**
     * Process one tick: decrement timers, transition private->global, despawn expired.
     * Returns lists of items that changed state for client notification.
     */
    fun process(): TickResult {
        val becameGlobal = mutableListOf<GroundItem>()
        val despawned = mutableListOf<GroundItem>()

        val iterator = items.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()

            // Private -> Global transition
            if (item.privateTimer > 0) {
                item.privateTimer--
                if (item.privateTimer <= 0) {
                    becameGlobal.add(item)
                }
            }

            // Despawn countdown
            item.despawnTimer--
            if (item.despawnTimer <= 0) {
                despawned.add(item)
                iterator.remove()
            }
        }

        return TickResult(becameGlobal, despawned)
    }

    data class TickResult(
        val becameGlobal: List<GroundItem>,
        val despawned: List<GroundItem>
    )

    // ================================================================
    //  Client packets
    // ================================================================

    /**
     * Send a "create ground item" packet to a player.
     */
    fun sendItemToPlayer(player: Player, item: GroundItem) {
        sendPositionHint(player, item.position)

        // Opcode 44: create ground item
        // Client reads: LEUShortA (itemId), IntAlt1 (amount), UByte (offset)
        val pkt = PacketBuilder(44)
        pkt.addLEShortA(item.itemId)
        pkt.addIntME1(item.amount)   // 4-byte middle-endian, NOT addShort
        pkt.addByte(0) // Offset from position hint
        player.send(pkt)
    }

    /**
     * Send a "remove ground item" packet to a player.
     */
    fun sendRemoveToPlayer(player: Player, item: GroundItem) {
        sendPositionHint(player, item.position)

        // Opcode 156: remove ground item
        // Client reads: UByteA (offset), UWord (itemId)
        val pkt = PacketBuilder(156)
        pkt.addByteA(0) // Offset from position hint
        pkt.addShort(item.itemId)
        player.send(pkt)
    }

    /**
     * Send the position hint packet that precedes ground item/object packets.
     * Offsets are relative to the last-sent map region base (chunk-based).
     */
    private fun sendPositionHint(player: Player, position: Position) {
        val pkt = PacketBuilder(85) // Set active tile for subsequent item/object packets
        // PI: offset = worldPos - mapRegionX*8 where mapRegionX = (absX>>3)-6
        val baseX = ((player.lastRegion.x shr 3) - 6) * 8
        val baseY = ((player.lastRegion.y shr 3) - 6) * 8
        val offsetX = position.x - baseX
        val offsetY = position.y - baseY
        pkt.addByteC(offsetY)
        pkt.addByteC(offsetX)
        player.send(pkt)
    }

    fun itemCount(): Int = items.size
}
