package com.openrune.core.world

import com.openrune.api.world.Position
import com.openrune.core.net.codec.PacketBuilder
import com.openrune.core.world.collision.CollisionMap
import org.slf4j.LoggerFactory

/**
 * A game object in the world (tree, rock, door, etc.).
 */
data class GameObject(
    val id: Int,
    val position: Position,
    val type: Int = 10,     // Object type (0-3 = wall, 10-11 = interactive, 22 = floor deco)
    val rotation: Int = 0,  // Orientation (0-3)
    val sizeX: Int = 1,
    val sizeY: Int = 1,
    val solid: Boolean = true,
    /** If > 0, this object will revert after this many ticks. */
    val revertTicks: Int = -1,
    /** The original object ID to revert to (e.g. tree stump -> tree). */
    val revertId: Int = -1
)

/**
 * Manages world objects: spawning, despawning, temporary replacements,
 * collision updates, and sending object state to players.
 *
 * ENGINE-LEVEL system. Plugins interact through events and the API.
 * They can request object spawns/despawns, but the collision update
 * and client synchronization is handled here.
 */
class ObjectManager(
    private val collisionMap: CollisionMap
) {

    private val log = LoggerFactory.getLogger(ObjectManager::class.java)

    /**
     * PlayerManager reference for sending visual sync packets.
     * Set by GameEngine after construction.
     * Added by Gathering Skills Patch.
     */
    var playerManager: PlayerManager? = null

    /** All active custom (non-cache) objects, keyed by packed position. */
    private val customObjects = mutableMapOf<Long, GameObject>()

    /** Objects pending revert (temporary replacements like tree stumps). */
    private val pendingReverts = mutableListOf<PendingRevert>()

    private data class PendingRevert(
        val position: Position,
        val originalObject: GameObject,
        var ticksRemaining: Int
    )

    // ================================================================
    //  Spawning
    // ================================================================

    /**
     * Spawn a game object at a position. Adds collision if solid.
     * Returns the created object.
     */
    fun spawn(obj: GameObject): GameObject {
        val key = packPosition(obj.position)
        customObjects[key] = obj

        // Update collision
        if (obj.solid) {
            if (obj.type in 0..3) {
                collisionMap.addWall(obj.position.x, obj.position.y, obj.position.z, obj.type, obj.rotation)
            } else {
                collisionMap.addObject(obj.position.x, obj.position.y, obj.position.z, obj.sizeX, obj.sizeY, true)
            }
        }

        return obj
    }

    /**
     * Remove a game object at a position. Clears collision.
     */
    fun despawn(position: Position) {
        val key = packPosition(position)
        val obj = customObjects.remove(key) ?: return

        if (obj.solid) {
            collisionMap.removeObject(position.x, position.y, position.z, obj.sizeX, obj.sizeY)
        }
    }

    /**
     * Replace an object temporarily (e.g. tree -> stump for respawn).
     * After [ticks] game ticks, the original object is restored.
     */
    fun replaceTemporary(position: Position, replacementId: Int, ticks: Int, originalId: Int,
                         sizeX: Int = 1, sizeY: Int = 1, type: Int = 10, rotation: Int = 0) {
        // Remove current collision
        despawn(position)

        // Spawn the replacement (usually not solid, e.g. a stump)
        val replacement = GameObject(
            id = replacementId, position = position,
            type = type, rotation = rotation,
            sizeX = sizeX, sizeY = sizeY,
            solid = false
        )
        spawn(replacement)

        // Schedule revert
        val original = GameObject(
            id = originalId, position = position,
            type = type, rotation = rotation,
            sizeX = sizeX, sizeY = sizeY,
            solid = true
        )
        pendingReverts.add(PendingRevert(position, original, ticks))

        // Send visual update to all nearby players (Gathering Skills Patch)
        sendObjectChangeToPlayers(position, originalId, replacementId, type, rotation)
    }

    // ================================================================
    //  Tick processing
    // ================================================================

    /**
     * Process pending object reverts. Called once per game tick.
     */
    fun process() {
        val iterator = pendingReverts.iterator()
        while (iterator.hasNext()) {
            val revert = iterator.next()
            if (--revert.ticksRemaining <= 0) {
                // Despawn the temporary and restore the original
                val tempObj = customObjects[packPosition(revert.position)]
                despawn(revert.position)
                spawn(revert.originalObject)
                iterator.remove()

                // Send visual update to all nearby players (Gathering Skills Patch)
                // Removes the depleted object and restores the original
                if (tempObj != null) {
                    sendObjectChangeToPlayers(
                        revert.position,
                        tempObj.id,
                        revert.originalObject.id,
                        revert.originalObject.type,
                        revert.originalObject.rotation
                    )
                }
            }
        }
    }

    // ================================================================
    //  Visual sync (Gathering Skills Patch)
    // ================================================================

    /**
     * Send object replacement visual update to all nearby players.
     * Uses opcode 85 (set reference) + 101 (remove) + 151 (spawn).
     *
     * This follows the same pattern as ObjectInteractionHandler's door system.
     */
    private fun sendObjectChangeToPlayers(position: Position, oldId: Int, newId: Int, type: Int, rotation: Int) {
        val pm = playerManager ?: return
        for (player in pm.allPlayers()) {
            if (player.position.isWithinDistance(position, 60)) {
                sendRefAndRemove(player, position, type, rotation)
                sendRefAndSpawn(player, position, newId, type, rotation)
            }
        }
    }

    /**
     * Send opcode 85 (set reference position) + opcode 101 (remove object).
     */
    private fun sendRefAndRemove(player: Player, pos: Position, type: Int, rotation: Int) {
        val baseX = ((player.lastRegion.x shr 3) - 6) * 8
        val baseY = ((player.lastRegion.y shr 3) - 6) * 8
        val localX = pos.x - baseX
        val localY = pos.y - baseY
        if (localX < 0 || localX >= 104 || localY < 0 || localY >= 104) return

        val ref = PacketBuilder(85)
        ref.addByteC(localY)
        ref.addByteC(localX)
        player.send(ref)

        val remove = PacketBuilder(101)
        remove.addByteC((type shl 2) or (rotation and 3))
        remove.addByte(0)
        player.send(remove)
    }

    /**
     * Send opcode 85 (set reference position) + opcode 151 (spawn object).
     */
    private fun sendRefAndSpawn(player: Player, pos: Position, objectId: Int, type: Int, rotation: Int) {
        val baseX = ((player.lastRegion.x shr 3) - 6) * 8
        val baseY = ((player.lastRegion.y shr 3) - 6) * 8
        val localX = pos.x - baseX
        val localY = pos.y - baseY
        if (localX < 0 || localX >= 104 || localY < 0 || localY >= 104) return

        val ref = PacketBuilder(85)
        ref.addByteC(localY)
        ref.addByteC(localX)
        player.send(ref)

        val spawn = PacketBuilder(151)
        spawn.addByteA(0)
        spawn.addLEShort(objectId)
        spawn.addByteS((type shl 2) or (rotation and 3))
        player.send(spawn)
    }

    // ================================================================
    //  Client synchronization
    // ================================================================

    /**
     * Send an "add object" packet to a player.
     * Used when the player enters a region with custom objects.
     */
    fun sendObjectToPlayer(player: Player, obj: GameObject) {
        // Opcode 151 = spawn object
        val pkt = PacketBuilder(151)
        pkt.addByteA(0) // Offset from region base (localX << 4 | localY)
        pkt.addLEShort(obj.id)
        pkt.addByteS((obj.type shl 2) or (obj.rotation and 3))
        player.send(pkt)
    }

    /**
     * Send a "remove object" packet to a player.
     */
    fun sendObjectRemovalToPlayer(player: Player, position: Position) {
        val pkt = PacketBuilder(101)
        pkt.addByteC((0 shl 2) or 0) // type and rotation of the removed object
        pkt.addByte(0) // offset
        player.send(pkt)
    }

    /**
     * Send all custom objects in a region to a player (on region load).
     */
    fun sendRegionObjects(player: Player) {
        for ((_, obj) in customObjects) {
            if (obj.position.isWithinDistance(player.position, 60)) {
                sendObjectToPlayer(player, obj)
            }
        }
    }

    // ================================================================
    //  Queries
    // ================================================================

    fun getObject(position: Position): GameObject? =
        customObjects[packPosition(position)]

    fun getObjectsInRegion(regionId: Int): List<GameObject> =
        customObjects.values.filter { it.position.regionId == regionId }

    fun customObjectCount(): Int = customObjects.size

    private fun packPosition(pos: Position): Long =
        (pos.x.toLong() shl 32) or (pos.y.toLong() shl 16) or pos.z.toLong()
}
