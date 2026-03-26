package com.openrune.cache.def

/**
 * Object (loc) definition loaded directly from the game cache.
 * Contains the client-side properties of a game object: name, examine text,
 * dimensions, solidity, interaction flag, actions, animation, and map icon.
 *
 * These are the authoritative definitions from the cache binary data.
 * Server-side overrides (respawn timers, skill requirements) are layered
 * on top via JSON.
 */
data class CacheObjectDefinition(
    val id: Int = -1,
    val name: String = "null",
    val examine: String = "",
    val width: Int = 1,
    val length: Int = 1,
    val solid: Boolean = true,
    val interactable: Boolean = false,
    val actions: Array<String?> = arrayOfNulls(5),
    val animationId: Int = -1,
    val mapIcon: Int = -1
) {
    /** Check if this object has an action at the given index. */
    fun hasAction(index: Int): Boolean =
        index in actions.indices && actions[index] != null

    /** Get a specific action string, or null. */
    fun getAction(index: Int): String? =
        actions.getOrNull(index)

    override fun toString(): String = "CacheObjectDef($id: $name, ${width}x$length, solid=$solid)"
}
