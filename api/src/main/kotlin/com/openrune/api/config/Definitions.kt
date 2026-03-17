package com.openrune.api.config

/**
 * Item definition loaded from data/items/ (.json files).
 * All fields have sensible defaults so partial JSON entries work.
 */
data class ItemDef(
    val id: Int = -1,
    val name: String = "null",
    val examine: String = "",
    val tradeable: Boolean = true,
    val stackable: Boolean = false,
    val noted: Boolean = false,
    val noteId: Int = -1,
    val value: Int = 0,
    val highAlch: Int = 0,
    val lowAlch: Int = 0,
    val weight: Double = 0.0,
    val members: Boolean = false,
    val equipSlot: Int = -1,
    val twoHanded: Boolean = false,
    val requirements: Map<String, Int> = emptyMap(),  // "attack" -> 70, etc.
    val bonuses: CombatBonuses = CombatBonuses()
)

/**
 * NPC definition loaded from data/npcs/ (.json files).
 */
data class NpcDef(
    val id: Int = -1,
    val name: String = "null",
    val examine: String = "",
    val combatLevel: Int = 0,
    val hitpoints: Int = 1,
    val attackSpeed: Int = 4,
    val respawnTicks: Int = 50,
    val aggressive: Boolean = false,
    val aggroRange: Int = 4,
    val poisonous: Boolean = false,
    val maxHit: Int = 0,
    val attackAnim: Int = -1,
    val defenceAnim: Int = -1,
    val deathAnim: Int = -1,
    val size: Int = 1,
    val walkRange: Int = 5,
    val bonuses: CombatBonuses = CombatBonuses()
)

/**
 * Object definition loaded from data/objects/ (.json files).
 */
data class ObjectDef(
    val id: Int = -1,
    val name: String = "null",
    val examine: String = "",
    val width: Int = 1,
    val height: Int = 1,
    val solid: Boolean = true,
    val interactable: Boolean = false,
    val actions: List<String> = emptyList(),
    val faceDirection: Int = 0
)

/**
 * NPC spawn definition loaded from data/spawns/ (.json files).
 */
data class SpawnDef(
    val npcId: Int,
    val x: Int,
    val y: Int,
    val z: Int = 0,
    val walkRange: Int = 5,
    val facing: Int = 0,
    val description: String = ""
)

/**
 * Drop table entry loaded from data/drops/ (.json files).
 */
data class DropDef(
    val itemId: Int,
    val minAmount: Int = 1,
    val maxAmount: Int = 1,
    val chance: Double = 1.0,    // 1.0 = always, 0.01 = 1/100
    val table: DropTable = DropTable.MAIN
)

enum class DropTable { ALWAYS, MAIN, RARE, VERY_RARE }

/**
 * Shop definition loaded from data/shops/ (.json files).
 */
data class ShopDef(
    val id: Int,
    val name: String,
    val sellMultiplier: Double = 1.0,
    val buyMultiplier: Double = 0.6,
    val currency: Int = 995,     // Coins by default
    val stock: List<ShopItem> = emptyList()
)

data class ShopItem(
    val itemId: Int,
    val amount: Int = 100,
    val price: Int = -1          // -1 = use item value
)

/**
 * Combat bonuses shared by items and NPCs.
 */
data class CombatBonuses(
    val attackStab: Int = 0,
    val attackSlash: Int = 0,
    val attackCrush: Int = 0,
    val attackMagic: Int = 0,
    val attackRanged: Int = 0,
    val defenceStab: Int = 0,
    val defenceSlash: Int = 0,
    val defenceCrush: Int = 0,
    val defenceMagic: Int = 0,
    val defenceRanged: Int = 0,
    val meleeStrength: Int = 0,
    val rangedStrength: Int = 0,
    val magicDamage: Int = 0,
    val prayer: Int = 0
)
