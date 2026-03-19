package com.openrune.cache.def

/**
 * NPC definition loaded directly from the game cache.
 * Contains everything the client knows about an NPC:
 * name, examine text, size, animations, actions, combat level, etc.
 *
 * These are the authoritative definitions — they match what the client
 * renders. Server-side overrides (HP, max hit, aggro, drops, AI) are
 * layered on top via JSON.
 */
data class CacheNpcDefinition(
    val id: Int = -1,
    val name: String = "null",
    val examine: String = "",
    val size: Int = 1,
    val standAnim: Int = -1,
    val walkAnim: Int = -1,
    val turnAround: Int = -1,
    val turnRight: Int = -1,
    val turnLeft: Int = -1,
    val combatLevel: Int = -1,
    val actions: Array<String?> = arrayOfNulls(10),
    val onMinimap: Boolean = true,
    val scaleXZ: Int = 128,
    val scaleY: Int = 128,
    val headIcon: Int = -1,
    val degreesToTurn: Int = 32,
    val models: IntArray = intArrayOf(),
    val dialogueModels: IntArray = intArrayOf(),
    val childrenIds: IntArray? = null
) {
    /** Check if this NPC has a right-click action at the given index. */
    fun hasAction(index: Int): Boolean =
        index in actions.indices && actions[index] != null

    /** Get a specific action string, or null. */
    fun getAction(index: Int): String? =
        actions.getOrNull(index)

    override fun toString(): String = "CacheNpcDef($id: $name, combat=$combatLevel, size=$size)"
}