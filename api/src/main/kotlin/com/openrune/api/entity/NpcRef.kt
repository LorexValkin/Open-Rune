package com.openrune.api.entity

import com.openrune.api.world.Position

/**
 * Plugin-facing interface to an NPC.
 */
interface NpcRef {

    val id: Int
    val index: Int
    val name: String
    var position: Position
    val spawnPosition: Position
    val combatLevel: Int

    // --- Health ---
    val maxHealth: Int
    var currentHealth: Int
    val isAlive: Boolean
    val isDead: Boolean

    // --- Movement ---
    fun walkTo(position: Position)
    fun face(target: PlayerRef)
    fun face(position: Position)
    fun resetFace()
    val isMoving: Boolean

    // --- Visual ---
    fun animate(animationId: Int, delay: Int = 0)
    fun graphic(graphicId: Int, height: Int = 100, delay: Int = 0)
    fun transform(newNpcId: Int)
    fun forceChat(text: String)

    // --- Combat ---
    fun damage(amount: Int, type: HitType = HitType.NORMAL)

    // --- Attributes (arbitrary key-value for plugin AI state) ---
    fun <T : Any> getAttribute(key: String): T?
    fun <T : Any> setAttribute(key: String, value: T)
    fun removeAttribute(key: String)
    fun hasAttribute(key: String): Boolean

    // --- Lifecycle ---
    fun despawn()
    fun respawn(delayTicks: Int)
}

enum class HitType(val value: Int) {
    NONE(-1),
    NORMAL(0),
    POISON(1),
    DISEASE(2)
}
