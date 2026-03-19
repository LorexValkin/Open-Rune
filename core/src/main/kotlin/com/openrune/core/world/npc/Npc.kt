package com.openrune.core.world.npc

import com.openrune.api.entity.HitType
import com.openrune.api.entity.NpcRef
import com.openrune.api.entity.PlayerRef
import com.openrune.api.world.Position
import com.openrune.core.world.collision.Direction
import com.openrune.core.world.movement.MovementProcessor
import com.openrune.core.world.movement.WalkingQueue
import java.util.concurrent.ConcurrentHashMap

/**
 * Server-side NPC entity. Implements [NpcRef] for plugin access
 * and [MovementProcessor.Movable] for engine-level movement.
 *
 * NPCs use the same collision and movement systems as players.
 * AI behavior is driven by plugins listening to ServerTickEvent,
 * but the base entity, movement, and update flags are engine-level.
 */
class Npc(
    override val id: Int,
    override val index: Int,
    override var position: Position,
    override val spawnPosition: Position = position,
    /** NPC definition data (loaded from JSON). */
    val def: NpcDefinition = NpcDefinition()
) : NpcRef, MovementProcessor.Movable {

    // ================================================================
    //  Definition accessors
    // ================================================================

    override val name: String get() = def.name
    override val combatLevel: Int get() = def.combatLevel
    override val maxHealth: Int get() = def.hitpoints
    override var currentHealth: Int = def.hitpoints
    override val isAlive: Boolean get() = currentHealth > 0
    override val isDead: Boolean get() = currentHealth <= 0

    // ================================================================
    //  Movement (ENGINE-LEVEL)
    // ================================================================

    override val walkingQueue = WalkingQueue().also { it.running = false }
    override val entitySize: Int get() = def.size

    override fun onMove(from: Position, to: Position, direction: Direction) {}
    override fun onRegionChange() {}

    override fun walkTo(position: Position) {
        // Movement target; resolved by the engine's MovementProcessor
        walkTarget = position
    }

    var walkTarget: Position? = null
    override val isMoving: Boolean get() = walkingQueue.hasSteps()

    // ================================================================
    //  Face / Interaction
    // ================================================================

    var faceEntityIndex: Int = -1
    var faceX: Int = 0
    var faceY: Int = 0

    override fun face(target: PlayerRef) {
        faceEntityIndex = target.index + 32768 // Player indices offset by 32768 in NPC face
        faceEntityUpdateRequired = true
        updateRequired = true
    }

    override fun face(position: Position) {
        faceX = position.x * 2 + 1
        faceY = position.y * 2 + 1
        facePositionUpdateRequired = true
        updateRequired = true
    }

    override fun resetFace() {
        faceEntityIndex = -1
        faceEntityUpdateRequired = true
        updateRequired = true
    }

    /** Set facing to specific coordinates (used by spawner). */
    fun facePosition(x: Int, y: Int) {
        faceX = x * 2 + 1
        faceY = y * 2 + 1
        facePositionUpdateRequired = true
        updateRequired = true
    }

    // ================================================================
    //  Visual
    // ================================================================

    var currentAnimation: Int = -1
    var animationDelay: Int = 0
    var graphicId: Int = -1
    var graphicHeight: Int = 0
    var graphicDelay: Int = 0
    var forceChatText: String = ""
    var transformId: Int = -1 // For NPC transformation (e.g. barrows brothers)

    override fun animate(animationId: Int, delay: Int) {
        currentAnimation = animationId; animationDelay = delay
        animationUpdateRequired = true; updateRequired = true
    }

    override fun graphic(graphicId: Int, height: Int, delay: Int) {
        this.graphicId = graphicId; this.graphicHeight = height; this.graphicDelay = delay
        graphicUpdateRequired = true; updateRequired = true
    }

    override fun transform(newNpcId: Int) {
        transformId = newNpcId
        transformUpdateRequired = true; updateRequired = true
    }

    override fun forceChat(text: String) {
        forceChatText = text
        forceChatUpdateRequired = true; updateRequired = true
    }

    // ================================================================
    //  Combat
    // ================================================================

    override fun damage(amount: Int, type: HitType) {
        currentHealth = maxOf(0, currentHealth - amount)
        if (!hitUpdateRequired) {
            hitDamage1 = amount; hitType1 = type.value
            hitUpdateRequired = true
        } else {
            hitDamage2 = amount; hitType2 = type.value
            hit2UpdateRequired = true
        }
        updateRequired = true
    }

    var hitDamage1: Int = 0; var hitType1: Int = 0
    var hitDamage2: Int = 0; var hitType2: Int = 0

    // ================================================================
    //  Lifecycle
    // ================================================================

    var active: Boolean = true
    var respawnCountdown: Int = -1

    override fun despawn() {
        active = false
    }

    override fun respawn(delayTicks: Int) {
        respawnCountdown = delayTicks
    }

    /**
     * Called by the engine when respawn countdown reaches 0.
     */
    fun performRespawn() {
        position = spawnPosition
        currentHealth = def.hitpoints
        active = true
        respawnCountdown = -1
        walkingQueue.clear()
        resetUpdateFlags()
        updateRequired = true
    }

    // ================================================================
    //  Update flags (ENGINE-LEVEL)
    // ================================================================

    var updateRequired: Boolean = false
    var animationUpdateRequired: Boolean = false
    var graphicUpdateRequired: Boolean = false
    var forceChatUpdateRequired: Boolean = false
    var faceEntityUpdateRequired: Boolean = false
    var facePositionUpdateRequired: Boolean = false
    var hitUpdateRequired: Boolean = false
    var hit2UpdateRequired: Boolean = false
    var transformUpdateRequired: Boolean = false

    fun resetUpdateFlags() {
        updateRequired = false
        animationUpdateRequired = false
        graphicUpdateRequired = false
        forceChatUpdateRequired = false
        faceEntityUpdateRequired = false
        facePositionUpdateRequired = false
        hitUpdateRequired = false
        hit2UpdateRequired = false
        transformUpdateRequired = false
        walkingQueue.resetDirections()
    }

    // ================================================================
    //  Random walk (basic built-in behavior)
    // ================================================================

    var randomWalkTimer: Int = 0

    /**
     * Basic random walk around spawn point. Called by the engine each tick.
     * Plugins can override NPC behavior by setting a "behavior" attribute
     * and handling movement themselves.
     */
    fun tickRandomWalk(): Position? {
        if (def.walkRange <= 0) return null
        if (--randomWalkTimer > 0) return null

        randomWalkTimer = 3 + (Math.random() * 8).toInt()

        val dx = (-1..1).random()
        val dy = (-1..1).random()
        if (dx == 0 && dy == 0) return null

        val target = position.translate(dx, dy)
        // Check if within walk range of spawn
        if (target.distanceTo(spawnPosition) > def.walkRange) return null

        return target
    }

    // ================================================================
    //  Attributes (plugin AI state)
    // ================================================================

    private val attributes = ConcurrentHashMap<String, Any>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getAttribute(key: String): T? = attributes[key] as? T
    override fun <T : Any> setAttribute(key: String, value: T) { attributes[key] = value }
    override fun removeAttribute(key: String) { attributes.remove(key) }
    override fun hasAttribute(key: String): Boolean = attributes.containsKey(key)
}

/**
 * NPC definition data. Loaded from JSON via the DataStore.
 * Kept separate from the entity so definitions can be reloaded without
 * recreating all active NPCs.
 */
data class NpcDefinition(
    val name: String = "null",
    val examine: String = "",
    val combatLevel: Int = 0,
    val hitpoints: Int = 1,
    val maxHit: Int = 0,
    val attackSpeed: Int = 4,
    val respawnTicks: Int = 50,
    val aggressive: Boolean = false,
    val aggroRange: Int = 4,
    val size: Int = 1,
    val walkRange: Int = 5,
    val attackAnim: Int = -1,
    val defenceAnim: Int = -1,
    val deathAnim: Int = -1,
    val standAnim: Int = -1,
    val walkAnim: Int = -1,
    val actions: List<String?> = emptyList()
)
