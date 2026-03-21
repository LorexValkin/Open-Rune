package com.openrune.plugins.admin

import com.openrune.api.entity.PlayerRef
import com.openrune.api.entity.PlayerRights
import com.openrune.api.entity.Skills
import com.openrune.api.event.*
import com.openrune.api.plugin.OpenRunePlugin
import com.openrune.api.plugin.PluginInfo

/**
 * Admin Commands Plugin — Provides all player-facing admin/debug commands.
 *
 * Commands (require admin rights):
 *   ::item <id> [amount]       — Spawn item into inventory
 *   ::tele <x> <y> [z]        — Teleport to coordinates
 *   ::pos                      — Show current position
 *   ::setlevel <skill> <lvl>   — Set a skill level
 *   ::master                   — Set all skills to 99
 *   ::anim <id>                — Play animation
 *   ::gfx <id>                 — Play graphic
 *   ::online                   — Show player count
 *   ::reload [store]           — Reload JSON data
 *   ::bank                     — Open bank interface
 *   ::empty                    — Clear inventory
 *   ::cmds                     — List available commands
 *
 * Engine-level commands (NPC spawning, plugin management, engine stats)
 * remain in Server.kt since they need engine internals.
 */
@PluginInfo(
    id = "admin-commands",
    name = "Admin Commands",
    version = "1.0.0",
    description = "Player-facing admin and debug commands",
    author = "OpenRune",
    hotSwappable = true
)
class AdminCommandsPlugin : OpenRunePlugin() {

    override fun onEnable() {
        context.events.on<CommandEvent>(owner = info.id) { event ->
            val player = event.player
            if (player.rights.value < 2) return@on

            when (event.command) {
                "item" -> cmdItem(player, event)
                "tele" -> cmdTele(player, event)
                "pos" -> cmdPos(player, event)
                "setlevel" -> cmdSetLevel(player, event)
                "master" -> cmdMaster(player, event)
                "anim" -> cmdAnim(player, event)
                "gfx" -> cmdGfx(player, event)
                "online" -> cmdOnline(player, event)
                "reload" -> cmdReload(player, event)
                "bank" -> cmdBank(player, event)
                "empty" -> cmdEmpty(player, event)
                "cmds", "commands" -> cmdHelp(player, event)
            }
        }

        context.log("Admin commands plugin enabled")
    }

    override fun onDisable() {
        context.log("Admin commands plugin disabled")
    }

    // ================================================================
    //  ::item <id> [amount]
    // ================================================================
    private fun cmdItem(player: PlayerRef, event: CommandEvent) {
        if (event.args.isEmpty()) {
            player.sendMessage("Usage: ::item <id> [amount]")
        } else {
            val itemId = event.args[0].toIntOrNull()
            val amount = event.args.getOrNull(1)?.toIntOrNull() ?: 1
            if (itemId != null) {
                if (player.addItem(itemId, amount)) {
                    player.sendMessage("Spawned item $itemId x$amount")
                    player.sendInventory()
                } else {
                    player.sendMessage("Inventory full.")
                }
            } else {
                player.sendMessage("Invalid item ID.")
            }
        }
        event.cancel()
    }

    // ================================================================
    //  ::tele <x> <y> [z]
    // ================================================================
    private fun cmdTele(player: PlayerRef, event: CommandEvent) {
        if (event.args.size >= 2) {
            val x = event.args[0].toIntOrNull()
            val y = event.args[1].toIntOrNull()
            val z = event.args.getOrNull(2)?.toIntOrNull() ?: 0
            if (x != null && y != null) {
                player.teleport(x, y, z)
                player.sendMessage("Teleported to $x, $y, $z")
            }
        } else {
            player.sendMessage("Usage: ::tele <x> <y> [z]")
        }
        event.cancel()
    }

    // ================================================================
    //  ::pos
    // ================================================================
    private fun cmdPos(player: PlayerRef, event: CommandEvent) {
        player.sendMessage("Position: ${player.position} Region: ${player.regionId}")
        event.cancel()
    }

    // ================================================================
    //  ::setlevel <skill_id> <level>
    // ================================================================
    private fun cmdSetLevel(player: PlayerRef, event: CommandEvent) {
        if (event.args.size >= 2) {
            val skill = event.args[0].toIntOrNull()
            val level = event.args[1].toIntOrNull()
            if (skill != null && level != null && skill in 0 until Skills.SKILL_COUNT && level in 1..99) {
                player.setLevel(skill, level)
                // Set XP to match the level
                val xp = getXPForLevel(level)
                val currentXp = player.getExperience(skill)
                if (currentXp < xp) {
                    player.addExperience(skill, xp - currentXp)
                }
                player.sendSkillUpdate(skill)
                player.sendMessage("Set ${Skills.NAMES[skill]} to level $level")
            } else {
                player.sendMessage("Invalid skill (0-${Skills.SKILL_COUNT - 1}) or level (1-99).")
            }
        } else {
            player.sendMessage("Usage: ::setlevel <skill_id> <level>")
            player.sendMessage("Skills: 0=Atk 1=Def 2=Str 3=HP 4=Range 5=Prayer 6=Mage 8=WC 14=Mining")
        }
        event.cancel()
    }

    // ================================================================
    //  ::master — All skills to 99
    // ================================================================
    private fun cmdMaster(player: PlayerRef, event: CommandEvent) {
        for (i in 0 until Skills.SKILL_COUNT) {
            player.setLevel(i, 99)
            val target = 13034431.0
            val current = player.getExperience(i)
            if (current < target) {
                player.addExperience(i, target - current)
            }
            player.sendSkillUpdate(i)
        }
        player.currentHealth = 99
        player.sendMessage("All skills set to 99.")
        player.flagAppearanceUpdate()
        player.sendInventory()
        player.sendEquipment()
        event.cancel()
    }

    // ================================================================
    //  ::anim <id>
    // ================================================================
    private fun cmdAnim(player: PlayerRef, event: CommandEvent) {
        val animId = event.args.getOrNull(0)?.toIntOrNull()
        if (animId != null) {
            player.animate(animId)
            player.sendMessage("Playing animation $animId")
        } else {
            player.sendMessage("Usage: ::anim <id>")
        }
        event.cancel()
    }

    // ================================================================
    //  ::gfx <id>
    // ================================================================
    private fun cmdGfx(player: PlayerRef, event: CommandEvent) {
        val gfxId = event.args.getOrNull(0)?.toIntOrNull()
        if (gfxId != null) {
            player.graphic(gfxId, 100, 0)
            player.sendMessage("Playing graphic $gfxId")
        } else {
            player.sendMessage("Usage: ::gfx <id>")
        }
        event.cancel()
    }

    // ================================================================
    //  ::online
    // ================================================================
    private fun cmdOnline(player: PlayerRef, event: CommandEvent) {
        player.sendMessage("Players online: ${context.players.count}")
        event.cancel()
    }

    // ================================================================
    //  ::reload [store]
    // ================================================================
    private fun cmdReload(player: PlayerRef, event: CommandEvent) {
        if (event.args.isEmpty()) {
            context.data.reloadAll()
            player.sendMessage("All data stores reloaded.")
        } else {
            val store = event.args[0]
            context.data.reload(store)
            player.sendMessage("Reloaded data store: $store")
        }
        event.cancel()
    }

    // ================================================================
    //  ::bank — Open bank interface (simple placeholder)
    // ================================================================
    private fun cmdBank(player: PlayerRef, event: CommandEvent) {
        player.openInterface(5292)
        player.sendMessage("Bank opened.")
        event.cancel()
    }

    // ================================================================
    //  ::empty — Clear inventory
    // ================================================================
    private fun cmdEmpty(player: PlayerRef, event: CommandEvent) {
        for (slot in 0 until 28) {
            val itemId = player.getItemInSlot(slot)
            if (itemId >= 0) {
                player.removeItem(itemId, player.getItemCountInSlot(slot))
            }
        }
        player.sendInventory()
        player.sendMessage("Inventory cleared.")
        event.cancel()
    }

    // ================================================================
    //  ::cmds — List commands
    // ================================================================
    private fun cmdHelp(player: PlayerRef, event: CommandEvent) {
        player.sendMessage("--- Admin Commands ---")
        player.sendMessage("::item <id> [amt] - Spawn item")
        player.sendMessage("::tele <x> <y> [z] - Teleport")
        player.sendMessage("::pos - Show position")
        player.sendMessage("::setlevel <skill> <lvl> - Set skill")
        player.sendMessage("::master - All 99s")
        player.sendMessage("::anim <id> / ::gfx <id>")
        player.sendMessage("::empty - Clear inventory")
        player.sendMessage("::bank - Open bank")
        player.sendMessage("::online / ::reload [store]")
        player.sendMessage("--- Engine (Server.kt) ---")
        player.sendMessage("::npc / ::removenpc / ::plugins")
        player.sendMessage("::enableplugin / ::disableplugin")
        player.sendMessage("::save / ::engine")
        event.cancel()
    }

    // ================================================================
    //  Utility
    // ================================================================

    /** Get XP required for a level (RS formula). */
    private fun getXPForLevel(level: Int): Double {
        var points = 0.0
        for (lvl in 1 until level) {
            points += Math.floor(lvl.toDouble() + 300.0 * Math.pow(2.0, lvl / 7.0))
        }
        return Math.floor(points / 4.0)
    }
}
