package com.openrune.plugins.skills

import com.openrune.api.config.DataStore
import com.openrune.api.entity.PlayerRef
import com.openrune.api.entity.Skills
import com.openrune.api.event.*
import com.openrune.api.plugin.OpenRunePlugin
import com.openrune.api.plugin.PluginInfo
import com.openrune.api.world.Position

/**
 * Skills Plugin - Provides all skilling content.
 *
 * This is an example of how content is structured as a plugin.
 * Each skill can be its own class, all registered from onEnable().
 *
 * Data-driven: tree definitions, rock definitions, fish spots, etc.
 * are loaded from JSON via the DataStore so they can be edited
 * without recompiling.
 */
@PluginInfo(
    id = "skills",
    name = "Skills",
    version = "1.0.0",
    description = "Core skilling content: Woodcutting, Mining, Fishing, Cooking, etc.",
    author = "OpenRune",
    hotSwappable = true
)
class SkillsPlugin : OpenRunePlugin() {

    private lateinit var woodcutting: WoodcuttingSkill
    private lateinit var mining: MiningSkill

    override fun onEnable() {
        context.log("Registering skill handlers...")

        woodcutting = WoodcuttingSkill(context.data)
        mining = MiningSkill(context.data)

        // Register woodcutting tree interactions
        context.events.on<ObjectInteractEvent>(owner = info.id) { event ->
            woodcutting.handleObjectClick(event, context)
        }

        // Register mining rock interactions
        context.events.on<ObjectInteractEvent>(owner = info.id) { event ->
            mining.handleObjectClick(event, context)
        }

        // Register experience event for level-up checks
        context.events.on<ExperienceEvent>(owner = info.id) { event ->
            handleExperience(event)
        }

        context.log("Skills plugin enabled with ${woodcutting.treeCount()} trees, ${mining.rockCount()} rocks")
    }

    override fun onDisable() {
        context.log("Skills plugin disabled")
    }

    private fun handleExperience(event: ExperienceEvent) {
        val player = event.player
        val skill = event.skill
        val oldLevel = player.getLevel(skill)

        // Apply the XP
        player.addExperience(skill, event.amount)

        // Check for level up
        val newLevel = calculateLevel(player.getExperience(skill))
        if (newLevel > oldLevel) {
            player.setLevel(skill, newLevel)
            player.sendMessage("Congratulations, you've advanced a ${Skills.NAMES[skill]} level! You are now level $newLevel.")
            player.graphic(199, 100, 0) // Level up fireworks

            context.events.emit(LevelUpEvent(player, skill, newLevel))
        }
    }

    companion object {
        /**
         * Calculate level from total XP using the RS formula.
         */
        fun calculateLevel(xp: Double): Int {
            var points = 0.0
            for (level in 1..99) {
                points += Math.floor(level.toDouble() + 300.0 * Math.pow(2.0, level / 7.0))
                if (Math.floor(points / 4.0) > xp) return level
            }
            return 99
        }
    }
}

// ============================================================
//  Woodcutting
// ============================================================

/**
 * Tree definitions loaded from JSON.
 * Edit data/skills/trees.json to add or modify trees.
 */
data class TreeDef(
    val objectId: Int,
    val logId: Int,
    val level: Int,
    val experience: Double,
    val respawnTicks: Int,
    val depletedObjectId: Int = 1342, // Tree stump
    val name: String = "Tree"
)

/**
 * Axe definitions.
 */
data class AxeDef(
    val itemId: Int,
    val level: Int,
    val animationId: Int,
    val speed: Double,
    val name: String
)

class WoodcuttingSkill(private val data: DataStore) {

    // Hardcoded defaults; in production these would come from data/skills/trees.json
    private val trees = mapOf(
        1278 to TreeDef(1278, 1511, 1, 25.0, 15, name = "Tree"),
        1276 to TreeDef(1276, 1511, 1, 25.0, 15, name = "Tree"),
        1286 to TreeDef(1286, 1521, 15, 37.5, 25, name = "Oak tree"),
        1281 to TreeDef(1281, 1519, 30, 67.5, 40, name = "Willow tree"),
        1308 to TreeDef(1308, 1517, 45, 100.0, 60, name = "Maple tree"),
        1306 to TreeDef(1306, 1515, 60, 175.0, 100, name = "Yew tree"),
        1309 to TreeDef(1309, 1513, 75, 250.0, 150, name = "Magic tree")
    )

    private val axes = listOf(
        AxeDef(1351, 1, 879, 1.0, "Bronze axe"),
        AxeDef(1349, 1, 877, 1.2, "Iron axe"),
        AxeDef(1353, 6, 875, 1.4, "Steel axe"),
        AxeDef(1361, 6, 873, 1.6, "Black axe"),
        AxeDef(1355, 21, 871, 1.8, "Mithril axe"),
        AxeDef(1357, 31, 869, 2.0, "Adamant axe"),
        AxeDef(1359, 41, 867, 2.4, "Rune axe"),
        AxeDef(6739, 61, 2846, 3.0, "Dragon axe")
    )

    fun treeCount(): Int = trees.size

    fun handleObjectClick(event: ObjectInteractEvent, ctx: com.openrune.api.plugin.PluginContext) {
        if (event.option != 1) return
        val tree = trees[event.objectId] ?: return

        val player = event.player

        // Check level
        if (player.getLevel(Skills.WOODCUTTING) < tree.level) {
            player.sendMessage("You need a Woodcutting level of ${tree.level} to chop this tree.")
            event.cancel()
            return
        }

        // Find best axe
        val axe = findBestAxe(player)
        if (axe == null) {
            player.sendMessage("You do not have an axe which you have the Woodcutting level to use.")
            event.cancel()
            return
        }

        // Check inventory space
        if (player.inventoryFreeSlots() == 0) {
            player.sendMessage("Your inventory is too full to hold any more logs.")
            event.cancel()
            return
        }

        // Start chopping
        player.sendMessage("You swing your axe at the ${tree.name.lowercase()}.")
        player.animate(axe.animationId)
        event.cancel()

        // Schedule the chop result
        val chopDelay = calculateChopDelay(player.getLevel(Skills.WOODCUTTING), tree.level, axe.speed)
        ctx.schedule(delayTicks = chopDelay) {
            if (!player.isOnline) return@schedule

            // Give log
            if (player.addItem(tree.logId)) {
                player.sendMessage("You get some ${tree.name.lowercase().replace(" tree", "")} logs.")

                // Grant XP via the event system (so XP multiplier plugins can modify it)
                ctx.events.emit(ExperienceEvent(player, Skills.WOODCUTTING, tree.experience))
            }

            player.resetAnimation()
        }
    }

    private fun findBestAxe(player: PlayerRef): AxeDef? {
        return axes.sortedByDescending { it.level }
            .firstOrNull { axe ->
                player.getLevel(Skills.WOODCUTTING) >= axe.level &&
                (player.hasItem(axe.itemId) || player.getEquipment(3) == axe.itemId)
            }
    }

    private fun calculateChopDelay(playerLevel: Int, treeLevel: Int, axeSpeed: Double): Int {
        val diff = playerLevel - treeLevel
        val base = 5 - (diff / 10).coerceAtMost(3)
        return maxOf(1, (base / axeSpeed).toInt())
    }
}

// ============================================================
//  Mining (stub - same pattern as woodcutting)
// ============================================================

class MiningSkill(private val data: DataStore) {

    data class RockDef(
        val objectId: Int,
        val oreId: Int,
        val level: Int,
        val experience: Double,
        val respawnTicks: Int,
        val name: String = "Rock"
    )

    private val rocks = mapOf(
        7484 to RockDef(7484, 436, 1, 17.5, 5, "Copper rock"),
        7486 to RockDef(7486, 438, 1, 17.5, 5, "Tin rock"),
        7488 to RockDef(7488, 440, 15, 35.0, 10, "Iron rock"),
        7489 to RockDef(7489, 453, 30, 60.0, 20, "Coal rock"),
        7492 to RockDef(7492, 444, 40, 65.0, 30, "Gold rock"),
        7491 to RockDef(7491, 447, 55, 80.0, 50, "Mithril rock"),
        7460 to RockDef(7460, 449, 70, 95.0, 100, "Adamantite rock"),
        7461 to RockDef(7461, 451, 85, 125.0, 200, "Runite rock")
    )

    fun rockCount(): Int = rocks.size

    fun handleObjectClick(event: ObjectInteractEvent, ctx: com.openrune.api.plugin.PluginContext) {
        if (event.option != 1) return
        val rock = rocks[event.objectId] ?: return

        val player = event.player

        if (player.getLevel(Skills.MINING) < rock.level) {
            player.sendMessage("You need a Mining level of ${rock.level} to mine this rock.")
            event.cancel()
            return
        }

        if (player.inventoryFreeSlots() == 0) {
            player.sendMessage("Your inventory is too full to hold any more ore.")
            event.cancel()
            return
        }

        player.sendMessage("You swing your pick at the rock.")
        player.animate(625) // Generic mining anim
        event.cancel()

        ctx.schedule(delayTicks = 3) {
            if (!player.isOnline) return@schedule
            if (player.addItem(rock.oreId)) {
                player.sendMessage("You manage to mine some ${rock.name.lowercase().replace(" rock", "")}.")
                ctx.events.emit(ExperienceEvent(player, Skills.MINING, rock.experience))
            }
            player.resetAnimation()
        }
    }
}
