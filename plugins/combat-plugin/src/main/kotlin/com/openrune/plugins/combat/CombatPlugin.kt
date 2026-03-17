package com.openrune.plugins.combat

import com.openrune.api.config.CombatBonuses
import com.openrune.api.entity.NpcRef
import com.openrune.api.entity.PlayerRef
import com.openrune.api.entity.Skills
import com.openrune.api.event.*
import com.openrune.api.plugin.OpenRunePlugin
import com.openrune.api.plugin.PluginContext
import com.openrune.api.plugin.PluginInfo
import kotlin.random.Random

/**
 * Combat Plugin - Handles all combat mechanics.
 *
 * Features:
 *   - Melee, Ranged, and Magic combat styles
 *   - Weapon speed and animation from JSON (data/weapons/)
 *   - Accuracy and max hit formulas
 *   - Special attacks (extensible via sub-plugins)
 *   - Death and respawn handling
 *   - Prayer effects integration (listens for prayer plugin events)
 *
 * All weapon data is loaded from data/weapons/weapons.json so you can
 * add custom weapons, change speeds, or tweak animations from the launcher.
 */
@PluginInfo(
    id = "combat",
    name = "Combat",
    version = "1.0.0",
    description = "Core combat mechanics: melee, ranged, magic, death handling.",
    author = "OpenRune",
    hotSwappable = true
)
class CombatPlugin : OpenRunePlugin() {

    private val weaponDefs = mutableMapOf<Int, WeaponDef>()

    override fun onEnable() {
        loadWeaponData()

        // Handle combat initiation
        context.events.on<CombatAttackEvent>(owner = info.id) { event ->
            handleAttack(event)
        }

        // Handle damage calculation
        context.events.on<CombatDamageEvent>(owner = info.id) { event ->
            handleDamage(event)
        }

        // Handle death
        context.events.on<DeathEvent>(owner = info.id) { event ->
            handleDeath(event)
        }

        // Handle data reload (re-read weapon defs)
        context.events.on<DataReloadEvent>(owner = info.id) { event ->
            if (event.store == "weapons" || event.store == "*") {
                loadWeaponData()
                context.log("Reloaded ${weaponDefs.size} weapon definitions")
            }
        }

        context.log("Combat plugin enabled with ${weaponDefs.size} weapon definitions")
    }

    override fun onDisable() {
        weaponDefs.clear()
        context.log("Combat plugin disabled")
    }

    // ================================================================
    //  Weapon Data
    // ================================================================

    data class WeaponDef(
        val id: Int,
        val name: String = "Unknown",
        val speed: Int = 4,
        val style: CombatStyle = CombatStyle.MELEE,
        val attackAnim: Int = 422,
        val defenceAnim: Int = 424,
        val specialBar: Boolean = false,
        val specialCost: Int = 0,
        val twoHanded: Boolean = false,
        val bonuses: CombatBonuses = CombatBonuses()
    )

    private fun loadWeaponData() {
        weaponDefs.clear()

        // Try loading from JSON data store
        val all = context.data.getAll("weapons")
        if (all.isNotEmpty()) {
            for ((id, json) in all) {
                try {
                    val def = com.google.gson.Gson().fromJson(json, WeaponDef::class.java)
                    weaponDefs[id] = def
                } catch (e: Exception) {
                    context.warn("Failed to parse weapon def $id: ${e.message}")
                }
            }
        }

        // Add defaults if store is empty (bootstrap)
        if (weaponDefs.isEmpty()) {
            weaponDefs[-1] = WeaponDef(-1, "Unarmed", 4, CombatStyle.MELEE, 422, 424)
            weaponDefs[4151] = WeaponDef(4151, "Abyssal whip", 4, CombatStyle.MELEE, 1658, 1659, true, 50)
            weaponDefs[4587] = WeaponDef(4587, "Dragon scimitar", 4, CombatStyle.MELEE, 390, 388, true, 55)
            weaponDefs[861] = WeaponDef(861, "Magic shortbow", 4, CombatStyle.RANGED, 426, 424, true, 55, twoHanded = true)
            weaponDefs[1381] = WeaponDef(1381, "Staff of air", 5, CombatStyle.MAGIC, 419, 420)
        }
    }

    private fun getWeaponDef(player: PlayerRef): WeaponDef {
        val weaponId = player.getEquipment(3) // Weapon slot
        return weaponDefs[weaponId] ?: weaponDefs[-1]!!
    }

    // ================================================================
    //  Attack Handling
    // ================================================================

    private fun handleAttack(event: CombatAttackEvent) {
        val attacker = event.attacker
        val weapon = getWeaponDef(attacker)

        // Play attack animation
        attacker.animate(weapon.attackAnim)

        // Calculate accuracy
        val attackRoll = calculateAttackRoll(attacker, weapon)
        val defenceRoll = calculateDefenceRoll(event)

        val hitChance = if (attackRoll > defenceRoll) {
            1.0 - (defenceRoll + 2.0) / (2.0 * (attackRoll + 1.0))
        } else {
            attackRoll / (2.0 * (defenceRoll + 1.0))
        }

        val hit = Random.nextDouble() < hitChance

        if (hit) {
            val maxHit = calculateMaxHit(attacker, weapon)
            val damage = Random.nextInt(0, maxHit + 1)

            // Emit damage event (other plugins like prayer can modify)
            context.events.emit(CombatDamageEvent(
                attacker = attacker,
                targetPlayer = event.targetPlayer,
                targetNpc = event.targetNpc,
                damage = damage,
                style = weapon.style
            ))
        } else {
            // Miss - 0 damage
            context.events.emit(CombatDamageEvent(
                attacker = attacker,
                targetPlayer = event.targetPlayer,
                targetNpc = event.targetNpc,
                damage = 0,
                style = weapon.style
            ))
        }

        // Schedule next attack based on weapon speed
        context.schedule(delayTicks = weapon.speed) {
            if (!attacker.isOnline || !attacker.isAlive) return@schedule
            // Re-emit attack event for continuous combat
            // (the actual combat loop would be managed by a CombatSession attribute)
        }
    }

    // ================================================================
    //  Damage Application
    // ================================================================

    private fun handleDamage(event: CombatDamageEvent) {
        // Apply damage to target
        if (event.targetPlayer != null) {
            val target = event.targetPlayer!!
            target.currentHealth = maxOf(0, target.currentHealth - event.damage)

            if (target.currentHealth <= 0) {
                context.events.emit(DeathEvent(player = target, npc = null, killer = event.attacker))
            }
        }

        // NPC damage would be handled similarly
        // event.targetNpc?.damage(event.damage)

        // Grant combat XP
        if (event.damage > 0) {
            val xpAmount = event.damage * 4.0 // Base XP rate
            when (event.style) {
                CombatStyle.MELEE -> {
                    context.events.emit(ExperienceEvent(event.attacker, Skills.ATTACK, xpAmount / 3))
                    context.events.emit(ExperienceEvent(event.attacker, Skills.STRENGTH, xpAmount / 3))
                    context.events.emit(ExperienceEvent(event.attacker, Skills.DEFENCE, xpAmount / 3))
                }
                CombatStyle.RANGED -> {
                    context.events.emit(ExperienceEvent(event.attacker, Skills.RANGED, xpAmount))
                }
                CombatStyle.MAGIC -> {
                    context.events.emit(ExperienceEvent(event.attacker, Skills.MAGIC, xpAmount))
                }
            }
            // Hitpoints XP always
            context.events.emit(ExperienceEvent(event.attacker, Skills.HITPOINTS, event.damage * 1.33))
        }
    }

    // ================================================================
    //  Death Handling
    // ================================================================

    private fun handleDeath(event: DeathEvent) {
        val player = event.player ?: return

        player.animate(836) // Death animation
        player.sendMessage("Oh dear, you are dead!")

        // Schedule respawn after death animation
        context.schedule(delayTicks = 4) {
            // Respawn at Lumbridge
            player.teleport(3222, 3218, 0)
            player.currentHealth = player.maxHealth
            player.animate(-1)
            player.sendMessage("You have respawned in Lumbridge.")
        }
    }

    // ================================================================
    //  Combat Formulas
    // ================================================================

    private fun calculateAttackRoll(player: PlayerRef, weapon: WeaponDef): Double {
        val effectiveLevel = when (weapon.style) {
            CombatStyle.MELEE -> player.getLevel(Skills.ATTACK).toDouble()
            CombatStyle.RANGED -> player.getLevel(Skills.RANGED).toDouble()
            CombatStyle.MAGIC -> player.getLevel(Skills.MAGIC).toDouble()
        }
        val equipmentBonus = when (weapon.style) {
            CombatStyle.MELEE -> weapon.bonuses.attackSlash.toDouble()
            CombatStyle.RANGED -> weapon.bonuses.attackRanged.toDouble()
            CombatStyle.MAGIC -> weapon.bonuses.attackMagic.toDouble()
        }
        return effectiveLevel * (equipmentBonus + 64)
    }

    private fun calculateDefenceRoll(event: CombatAttackEvent): Double {
        // Simplified: use target's defence level * a base bonus
        val defLevel = event.targetPlayer?.getLevel(Skills.DEFENCE)?.toDouble() ?: 1.0
        return defLevel * 64.0
    }

    private fun calculateMaxHit(player: PlayerRef, weapon: WeaponDef): Int {
        return when (weapon.style) {
            CombatStyle.MELEE -> {
                val effectiveStr = player.getLevel(Skills.STRENGTH).toDouble()
                val bonus = weapon.bonuses.meleeStrength.toDouble()
                val base = 0.5 + effectiveStr * (bonus + 64) / 640.0
                base.toInt()
            }
            CombatStyle.RANGED -> {
                val effectiveRange = player.getLevel(Skills.RANGED).toDouble()
                val bonus = weapon.bonuses.rangedStrength.toDouble()
                val base = 0.5 + effectiveRange * (bonus + 64) / 640.0
                base.toInt()
            }
            CombatStyle.MAGIC -> {
                // Magic max hit depends on the spell, not equipment
                // Default to a base value
                15
            }
        }
    }
}
