package com.openrune.cache.def

/**
 * Item definition loaded directly from the game cache.
 * Contains the client-side properties of an item: name, examine text,
 * value, stack behaviour, note status, membership, equip info, and actions.
 *
 * These are the authoritative definitions from the cache binary data.
 * Server-side overrides (drop rates, bonuses, requirements) are layered
 * on top via JSON.
 */
data class CacheItemDefinition(
    val id: Int = -1,
    val name: String = "null",
    val examine: String = "",
    val value: Int = 0,
    val stackable: Boolean = false,
    val noted: Boolean = false,
    val noteId: Int = -1,
    val members: Boolean = false,
    val equipSlot: Int = -1,
    val equipActions: Array<String?> = arrayOfNulls(5),
    val groundActions: Array<String?> = arrayOfNulls(5),
    val interfaceActions: Array<String?> = arrayOfNulls(5)
) {
    /** Check if this item has a ground action at the given index. */
    fun hasGroundAction(index: Int): Boolean =
        index in groundActions.indices && groundActions[index] != null

    /** Check if this item has an interface/inventory action at the given index. */
    fun hasInterfaceAction(index: Int): Boolean =
        index in interfaceActions.indices && interfaceActions[index] != null

    override fun toString(): String = "CacheItemDef($id: $name, value=$value, stackable=$stackable)"
}
