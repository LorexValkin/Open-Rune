package com.openrune.core.world.interaction

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.openrune.api.entity.Skills
import com.openrune.api.event.EventPriority
import com.openrune.api.event.ItemEquipEvent
import com.openrune.api.event.ItemUnequipEvent
import com.openrune.core.event.EventBusImpl
import com.openrune.core.world.Player
import com.openrune.core.world.PlayerManager
import com.openrune.core.world.appearance.EquipmentSlot
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

/**
 * ENGINE-LEVEL handler for equipment management.
 *
 * Handles equipping and unequipping items, including:
 *   - Slot lookup from items.json (equipSlot field)
 *   - 2H weapon / shield conflict resolution
 *   - Level requirement checking
 *   - Appearance flag update after equip/unequip
 *   - Inventory & equipment interface sync
 *
 * Registered at LOW priority so plugins can override specific equip
 * behavior at NORMAL priority (e.g. quest-locked equipment).
 *
 * Added by Gathering Skills Patch.
 */
class EquipmentHandler(
    private val eventBus: EventBusImpl,
    private val playerManager: PlayerManager
) {

    private val log = LoggerFactory.getLogger(EquipmentHandler::class.java)

    /**
     * Minimal info needed to equip an item.
     * Loaded from items.json at startup.
     */
    data class ItemEquipInfo(
        val id: Int,
        val equipSlot: Int,
        val twoHanded: Boolean = false,
        val requirements: Map<String, Int> = emptyMap()
    )

    /** itemId -> equip info, populated from items.json */
    private val equipInfo = mutableMapOf<Int, ItemEquipInfo>()

    /** Skill name (lowercase) -> Skills constant */
    private val skillNameToId = mapOf(
        "attack" to Skills.ATTACK,
        "defence" to Skills.DEFENCE,
        "strength" to Skills.STRENGTH,
        "hitpoints" to Skills.HITPOINTS,
        "ranged" to Skills.RANGED,
        "prayer" to Skills.PRAYER,
        "magic" to Skills.MAGIC,
        "cooking" to Skills.COOKING,
        "woodcutting" to Skills.WOODCUTTING,
        "fletching" to Skills.FLETCHING,
        "fishing" to Skills.FISHING,
        "firemaking" to Skills.FIREMAKING,
        "crafting" to Skills.CRAFTING,
        "smithing" to Skills.SMITHING,
        "mining" to Skills.MINING,
        "herblore" to Skills.HERBLORE,
        "agility" to Skills.AGILITY,
        "thieving" to Skills.THIEVING,
        "slayer" to Skills.SLAYER,
        "farming" to Skills.FARMING,
        "runecrafting" to Skills.RUNECRAFTING
    )

    // ================================================================
    //  Initialization
    // ================================================================

    fun initialize() {
        loadItemData()

        // Equip handler (LOW priority so plugins can override at NORMAL)
        eventBus.subscribe(
            ItemEquipEvent::class,
            priority = EventPriority.LOW,
            owner = "core:equipment"
        ) { event ->
            if (!event.cancelled) {
                handleEquip(event)
            }
        }

        // Unequip handler
        eventBus.subscribe(
            ItemUnequipEvent::class,
            priority = EventPriority.LOW,
            owner = "core:equipment"
        ) { event ->
            if (!event.cancelled) {
                handleUnequip(event)
            }
        }

        log.info("Equipment handler: {} equippable items loaded", equipInfo.size)
    }

    /**
     * Load equipment slot info from data/items/items.json.
     * Only entries with an "equipSlot" field are relevant.
     */
    private fun loadItemData() {
        val file = Path.of("data/items/items.json")
        if (!Files.exists(file)) {
            log.warn("items.json not found at {}, equipment handler will have no data", file)
            return
        }

        try {
            val content = Files.readString(file)
            val items = JsonParser.parseString(content).asJsonArray

            for (elem in items) {
                val obj = elem.asJsonObject
                if (!obj.has("equipSlot")) continue

                val id = obj.get("id").asInt
                val slot = obj.get("equipSlot").asInt
                val twoHanded = obj.get("twoHanded")?.asBoolean ?: false

                val reqs = mutableMapOf<String, Int>()
                if (obj.has("requirements")) {
                    val reqObj = obj.getAsJsonObject("requirements")
                    for ((key, value) in reqObj.entrySet()) {
                        reqs[key.lowercase()] = value.asInt
                    }
                }

                equipInfo[id] = ItemEquipInfo(id, slot, twoHanded, reqs)
            }
        } catch (e: Exception) {
            log.error("Failed to load items.json for equipment handler", e)
        }
    }

    /**
     * Reload equipment data (called on ::reload items).
     */
    fun reload() {
        equipInfo.clear()
        loadItemData()
        log.info("Equipment handler reloaded: {} equippable items", equipInfo.size)
    }

    // ================================================================
    //  Equip
    // ================================================================

    private fun handleEquip(event: ItemEquipEvent) {
        val player = event.player as? Player ?: return
        val info = equipInfo[event.itemId]
        if (info == null) {
            // Not an equippable item — don't consume the event
            return
        }

        val invSlot = event.slot
        val equipSlot = info.equipSlot

        // Validate the item is actually in that inventory slot
        if (player.inventoryItems.getOrElse(invSlot) { -1 } != event.itemId) return

        // Check level requirements
        for ((skillName, reqLevel) in info.requirements) {
            val skillId = skillNameToId[skillName] ?: continue
            if (player.getLevel(skillId) < reqLevel) {
                player.sendMessage("You need a ${Skills.NAMES.getOrElse(skillId) { skillName }} level of $reqLevel to equip this.")
                event.cancel()
                return
            }
        }

        // === 2H weapon conflict check ===
        if (info.twoHanded && equipSlot == EquipmentSlot.WEAPON) {
            val shieldId = player.equipment[EquipmentSlot.SHIELD]
            val currentWeaponId = player.equipment[equipSlot]
            // Need space for: old weapon (goes into cleared inv slot) + old shield (needs a free slot)
            if (shieldId > 0 && currentWeaponId > 0) {
                // Old weapon goes into the inv slot we're clearing; shield needs one MORE free slot
                if (player.inventoryFreeSlots() < 1) {
                    player.sendMessage("You don't have enough free inventory space to do that.")
                    event.cancel()
                    return
                }
            } else if (shieldId > 0) {
                // No old weapon — we still need a slot for the shield (inv slot is consumed by the 2H)
                // Actually the inv slot is being cleared (item removed), but the 2H doesn't go back.
                // We need a free slot for the shield beyond the one we just cleared.
                // Since we clear invSlot, that gives us 1 free slot. Shield can go there IF no old weapon.
                // But we put nothing back in invSlot if there's no old weapon. So shield goes into invSlot? No.
                // Let's count properly: invSlot is cleared (+1 free). Shield needs 1 slot. Net = 0. OK.
            }
        }

        if (equipSlot == EquipmentSlot.SHIELD) {
            val weaponId = player.equipment[EquipmentSlot.WEAPON]
            if (weaponId > 0) {
                val weaponInfo = equipInfo[weaponId]
                if (weaponInfo != null && weaponInfo.twoHanded) {
                    // Removing 2H to make room for shield
                    val currentShieldId = player.equipment[EquipmentSlot.SHIELD]
                    if (currentShieldId > 0 && player.inventoryFreeSlots() < 1) {
                        // Old shield + 2H weapon both need inventory space, minus the slot we're clearing
                        player.sendMessage("You don't have enough free inventory space to do that.")
                        event.cancel()
                        return
                    }
                }
            }
        }

        // === Perform the equip ===

        // Save what's currently equipped in the target slot
        val oldEquippedId = player.equipment[equipSlot]
        val oldEquippedAmount = player.equipmentAmounts[equipSlot]

        // Remove item from inventory
        player.inventoryItems[invSlot] = -1
        player.inventoryAmounts[invSlot] = 0

        // Handle 2H: remove shield when equipping 2H weapon
        if (info.twoHanded && equipSlot == EquipmentSlot.WEAPON) {
            val shieldId = player.equipment[EquipmentSlot.SHIELD]
            if (shieldId > 0) {
                val shieldAmount = player.equipmentAmounts[EquipmentSlot.SHIELD]
                player.equipment[EquipmentSlot.SHIELD] = -1
                player.equipmentAmounts[EquipmentSlot.SHIELD] = 0
                // Put shield in the freed inventory slot if possible, otherwise find a free slot
                if (oldEquippedId > 0) {
                    // Old weapon goes to invSlot, shield goes to a free slot
                    player.addItem(shieldId, shieldAmount)
                } else {
                    // No old weapon — put shield in the cleared invSlot
                    player.inventoryItems[invSlot] = shieldId
                    player.inventoryAmounts[invSlot] = shieldAmount
                }
            }
        }

        // Handle shield equip: remove 2H weapon
        if (equipSlot == EquipmentSlot.SHIELD) {
            val weaponId = player.equipment[EquipmentSlot.WEAPON]
            if (weaponId > 0) {
                val weaponInfo = equipInfo[weaponId]
                if (weaponInfo != null && weaponInfo.twoHanded) {
                    val weaponAmount = player.equipmentAmounts[EquipmentSlot.WEAPON]
                    player.equipment[EquipmentSlot.WEAPON] = -1
                    player.equipmentAmounts[EquipmentSlot.WEAPON] = 0
                    if (oldEquippedId > 0) {
                        player.addItem(weaponId, weaponAmount)
                    } else {
                        player.inventoryItems[invSlot] = weaponId
                        player.inventoryAmounts[invSlot] = weaponAmount
                    }
                }
            }
        }

        // Put old equipment into the freed inventory slot (if not already used by 2H swap)
        if (oldEquippedId > 0 && player.inventoryItems[invSlot] == -1) {
            player.inventoryItems[invSlot] = oldEquippedId
            player.inventoryAmounts[invSlot] = oldEquippedAmount
        } else if (oldEquippedId > 0) {
            // Slot was used by 2H/shield swap, find another free slot
            player.addItem(oldEquippedId, oldEquippedAmount)
        }

        // Set new equipment
        player.equipment[equipSlot] = event.itemId
        player.equipmentAmounts[equipSlot] = 1

        // Update client
        player.flagAppearanceUpdate()
        player.sendInventory()
        player.sendEquipment()

        event.cancel() // Consumed
    }

    // ================================================================
    //  Unequip
    // ================================================================

    private fun handleUnequip(event: ItemUnequipEvent) {
        val player = event.player as? Player ?: return
        val equipSlot = event.slot

        if (equipSlot < 0 || equipSlot >= player.equipment.size) return
        val itemId = player.equipment[equipSlot]
        if (itemId <= 0) return

        // Check inventory space
        if (player.inventoryFreeSlots() == 0) {
            player.sendMessage("You don't have enough free inventory space to do that.")
            event.cancel()
            return
        }

        // Move to inventory
        val amount = player.equipmentAmounts[equipSlot]
        player.equipment[equipSlot] = -1
        player.equipmentAmounts[equipSlot] = 0
        player.addItem(itemId, amount)

        // Update client
        player.flagAppearanceUpdate()
        player.sendInventory()
        player.sendEquipment()

        event.cancel() // Consumed
    }

    // ================================================================
    //  Public query (for other systems)
    // ================================================================

    fun getEquipSlot(itemId: Int): Int = equipInfo[itemId]?.equipSlot ?: -1
    fun isTwoHanded(itemId: Int): Boolean = equipInfo[itemId]?.twoHanded ?: false
    fun isEquippable(itemId: Int): Boolean = equipInfo.containsKey(itemId)
}
