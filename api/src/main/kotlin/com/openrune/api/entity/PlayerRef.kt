package com.openrune.api.entity

import com.openrune.api.world.Position

/**
 * Plugin-facing interface to a player.
 * Plugins never see the internal Player implementation directly.
 */
interface PlayerRef {

    // --- Identity ---
    val name: String
    val index: Int
    val rights: PlayerRights

    // --- Position ---
    var position: Position
    val regionId: Int

    // --- Stats ---
    fun getLevel(skill: Int): Int
    fun setLevel(skill: Int, level: Int)
    fun getExperience(skill: Int): Double
    fun addExperience(skill: Int, amount: Double)
    fun getCombatLevel(): Int
    fun getTotalLevel(): Int

    // --- Health ---
    val maxHealth: Int
    var currentHealth: Int
    val isAlive: Boolean

    // --- Inventory ---
    fun hasItem(itemId: Int, amount: Int = 1): Boolean
    fun addItem(itemId: Int, amount: Int = 1): Boolean
    fun removeItem(itemId: Int, amount: Int = 1): Boolean
    fun getItemInSlot(slot: Int): Int
    fun getItemCountInSlot(slot: Int): Int
    fun inventoryFreeSlots(): Int

    // --- Equipment ---
    fun getEquipment(slot: Int): Int
    fun setEquipment(slot: Int, itemId: Int, amount: Int = 1)

    // --- Bank ---
    fun bankHasItem(itemId: Int): Boolean
    fun addBankItem(itemId: Int, amount: Int = 1): Boolean

    // --- Communication ---
    fun sendMessage(text: String)
    fun sendSkillMenu(skill: Int)

    // --- Interfaces ---
    fun openInterface(interfaceId: Int)
    fun closeInterfaces()
    fun sendSidebar(tabId: Int, interfaceId: Int)

    // --- Movement ---
    fun teleport(position: Position)
    fun teleport(x: Int, y: Int, z: Int = 0)
    fun walkTo(position: Position)
    val isMoving: Boolean

    // --- Animations & Graphics ---
    fun animate(animationId: Int, delay: Int = 0)
    fun graphic(graphicId: Int, height: Int = 100, delay: Int = 0)
    fun resetAnimation()

    // --- Flags ---
    fun flagAppearanceUpdate()
    fun flagChatUpdate()

    // --- Client Sync (Admin Commands Patch) ---

    /** Send inventory contents to the client (packet 53, interface 3214). */
    fun sendInventory()

    /** Send equipment contents to the client (packet 53, interface 1688). */
    fun sendEquipment()

    /** Send a single skill update to the client (opcode 134). */
    fun sendSkillUpdate(skill: Int)

    /** Refresh skill tab tooltip text (current/max level, XP, XP to next level). */
    fun refreshSkillText(skill: Int)

    /**
     * Send XP drop packet (opcode 11) — triggers the floating XP drop overlay
     * and feeds the XP counter in the corner. Separate from sendSkillUpdate
     * which only updates the skill tab numbers.
     */
    fun sendXpDrop(skill: Int, amount: Int)

    /** Send text to a client interface element (opcode 126). */
    fun sendInterfaceText(text: String, interfaceId: Int)

    /** Open a chatbox interface dialog (opcode 218) — used for level-up popups. */
    fun sendChatboxInterface(interfaceId: Int)

    // --- Session ---
    fun disconnect(message: String = "")
    val isOnline: Boolean

    // --- Attributes (arbitrary key-value for plugin state) ---
    fun <T : Any> getAttribute(key: String): T?
    fun <T : Any> setAttribute(key: String, value: T)
    fun removeAttribute(key: String)
    fun hasAttribute(key: String): Boolean
}

/**
 * Player rights / privilege levels.
 */
enum class PlayerRights(val value: Int) {
    PLAYER(0),
    MODERATOR(1),
    ADMINISTRATOR(2),
    OWNER(3);

    companion object {
        fun fromValue(v: Int): PlayerRights = entries.firstOrNull { it.value == v } ?: PLAYER
    }
}

/**
 * Skill ID constants. Matches the 317 protocol ordering.
 */
object Skills {
    const val ATTACK = 0
    const val DEFENCE = 1
    const val STRENGTH = 2
    const val HITPOINTS = 3
    const val RANGED = 4
    const val PRAYER = 5
    const val MAGIC = 6
    const val COOKING = 7
    const val WOODCUTTING = 8
    const val FLETCHING = 9
    const val FISHING = 10
    const val FIREMAKING = 11
    const val CRAFTING = 12
    const val SMITHING = 13
    const val MINING = 14
    const val HERBLORE = 15
    const val AGILITY = 16
    const val THIEVING = 17
    const val SLAYER = 18
    const val FARMING = 19
    const val RUNECRAFTING = 20
    const val SKILL_COUNT = 21

    val NAMES = arrayOf(
        "Attack", "Defence", "Strength", "Hitpoints", "Ranged",
        "Prayer", "Magic", "Cooking", "Woodcutting", "Fletching",
        "Fishing", "Firemaking", "Crafting", "Smithing", "Mining",
        "Herblore", "Agility", "Thieving", "Slayer", "Farming",
        "Runecrafting"
    )
}
