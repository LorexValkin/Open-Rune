package com.openrune.plugins.skills

import com.openrune.api.config.DataStore
import com.openrune.api.entity.PlayerRef
import com.openrune.api.entity.Skills
import com.openrune.api.event.*
import com.openrune.api.plugin.OpenRunePlugin
import com.openrune.api.plugin.PluginContext
import com.openrune.api.plugin.PluginInfo
import com.openrune.api.plugin.TaskHandle
import com.openrune.api.world.Position

/**
 * Skills Plugin — Gathering skills: Woodcutting and Mining.
 *
 * Data-driven: all tree, rock, axe, and pickaxe definitions are loaded
 * from JSON via the DataStore so they can be edited without recompiling.
 *
 * Features (Gathering Skills Patch):
 *   - Wiki-accurate tree/rock definitions (all tiers, all object IDs)
 *   - Wiki-accurate axe/pickaxe definitions with per-tier animations
 *   - RS-style success formula (level + tool bonus / 256)
 *   - Resource depletion (tree → stump, rock → empty rock)
 *   - Respawn timers via ObjectReplaceEvent
 *   - Gathering loop: repeats until inventory full / resource depleted / player walks away
 *   - Finds best tool from inventory OR equipped weapon slot
 *   - Level requirement checking
 *   - XP via ExperienceEvent (so multiplier plugins can hook in)
 */
@PluginInfo(
    id = "skills",
    name = "Skills",
    version = "2.0.0",
    description = "Core skilling content: Woodcutting, Mining (wiki-accurate data-driven)",
    author = "OpenRune",
    hotSwappable = true
)
class SkillsPlugin : OpenRunePlugin() {

    private lateinit var woodcutting: WoodcuttingSkill
    private lateinit var mining: MiningSkill

    override fun onEnable() {
        context.log("Loading gathering skill data...")

        woodcutting = WoodcuttingSkill(context.data)
        mining = MiningSkill(context.data)

        // Register woodcutting tree interactions
        context.events.on<ObjectInteractEvent>(owner = info.id) { event ->
            if (!event.cancelled) woodcutting.handleObjectClick(event, context)
        }

        // Register mining rock interactions
        context.events.on<ObjectInteractEvent>(owner = info.id) { event ->
            if (!event.cancelled) mining.handleObjectClick(event, context)
        }

        // Register experience event for level-up checks
        context.events.on<ExperienceEvent>(owner = info.id) { event ->
            if (!event.cancelled) handleExperience(event)
        }

        // Reload data on hot-reload
        context.events.on<DataReloadEvent>(owner = info.id) { event ->
            when (event.store) {
                "trees", "axes" -> { woodcutting.reload(context.data); context.log("Woodcutting data reloaded") }
                "rocks", "pickaxes" -> { mining.reload(context.data); context.log("Mining data reloaded") }
            }
        }

        context.log("Skills plugin v2.0.0 enabled: ${woodcutting.treeCount()} tree object IDs, " +
                "${woodcutting.axeCount()} axes, ${mining.rockCount()} rock object IDs, ${mining.pickCount()} pickaxes")
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

        // Send skill update to client (packet 134) — updates the skill tab numbers.
        player.sendSkillUpdate(skill)
        player.refreshSkillText(skill)

        // Send XP drop (opcode 11) — triggers the floating XP drop and corner counter.
        player.sendXpDrop(skill, event.amount.toInt())

        // Check for level up
        val newLevel = calculateLevel(player.getExperience(skill))
        if (newLevel > oldLevel) {
            player.setLevel(skill, newLevel)
            // Send again with the new level so the client updates both level + XP
            player.sendSkillUpdate(skill)
            player.refreshSkillText(skill)

            // === Level-up chatbox interface ===
            val skillName = Skills.NAMES[skill]
            val article = if (skillName.first() in "AEIOUaeiou") "an" else "a"
            val interfaceId = LEVELUP_INTERFACES.getOrNull(skill)

            if (interfaceId != null && interfaceId > 0) {
                // Open the chatbox dialog FIRST so the client has the interface active
                player.sendChatboxInterface(interfaceId)
                // THEN set the text lines (shared children 4268 and 4269)
                player.sendInterfaceText("Congratulations, you've just advanced $article $skillName level!", 4268)
                player.sendInterfaceText("Your $skillName level is now $newLevel.", 4269)
            } else {
                // Fallback if no interface ID mapped — just send a chat message
                player.sendMessage("Congratulations, you've advanced $article $skillName level! You are now level $newLevel.")
            }

            player.graphic(199, 100, 0) // Level up fireworks

            context.events.emit(LevelUpEvent(player, skill, newLevel))
        }
    }

    companion object {
        /** Calculate level from total XP using the RS formula. */
        fun calculateLevel(xp: Double): Int {
            var points = 0.0
            for (level in 1..99) {
                points += Math.floor(level.toDouble() + 300.0 * Math.pow(2.0, level / 7.0))
                if (Math.floor(points / 4.0) > xp) return level
            }
            return 99
        }

        /**
         * Level-up chatbox interface IDs per skill.
         * Index matches Skills.ATTACK (0) through Skills.RUNECRAFTING (20).
         * These are the parent interfaces from RSInterface.Levelup() in the
         * Anguish/Project51 client — each contains a skill icon sprite and
         * shared text children at 4268 (line 1) and 4269 (line 2).
         */
        val LEVELUP_INTERFACES = intArrayOf(
            6247,   // 0:  Attack
            6253,   // 1:  Defence
            6206,   // 2:  Strength
            6216,   // 3:  Hitpoints
            4443,   // 4:  Ranged
            6242,   // 5:  Prayer
            6211,   // 6:  Magic
            6226,   // 7:  Cooking
            4272,   // 8:  Woodcutting
            6231,   // 9:  Fletching
            6258,   // 10: Fishing
            4282,   // 11: Firemaking
            6263,   // 12: Crafting
            6221,   // 13: Smithing
            4416,   // 14: Mining
            6237,   // 15: Herblore
            4277,   // 16: Agility
            4261,   // 17: Thieving
            12122,  // 18: Slayer
            5267,   // 19: Farming
            4267    // 20: Runecrafting
        )
    }
}

// ============================================================
//  Shared gathering constants
// ============================================================

private const val WEAPON_SLOT = 3  // EquipmentSlot.WEAPON
private const val GATHER_TICK_INTERVAL = 4  // Roll every 4 ticks (2.4s) like RS
private const val INTERACT_DISTANCE = 1     // Must be adjacent (Chebyshev distance 1)

/** Attribute key for the active gathering task handle. */
private const val ATTR_GATHER_TASK = "gathering:task"

/** Attribute key for the walk-to-object task (waiting for arrival). */
private const val ATTR_WALKTO_TASK = "gathering:walkto"

/**
 * Cancel any active gathering or walk-to task for a player.
 * Called when they move to a different target, click something else, etc.
 */
private fun cancelGathering(player: PlayerRef) {
    val task = player.getAttribute<TaskHandle>(ATTR_GATHER_TASK)
    if (task != null && task.isActive) {
        task.cancel()
    }
    player.removeAttribute(ATTR_GATHER_TASK)

    val walkTask = player.getAttribute<TaskHandle>(ATTR_WALKTO_TASK)
    if (walkTask != null && walkTask.isActive) {
        walkTask.cancel()
    }
    player.removeAttribute(ATTR_WALKTO_TASK)
}

/**
 * Wait-for-arrival helper. The client walks the player to the object
 * automatically when they click it. This schedules a 1-tick poll that
 * does NOTHING until the player has STOPPED MOVING and is ADJACENT.
 *
 * No animation, no messages, no resources until arrival is confirmed.
 *
 * Cancels if: player stops moving but isn't adjacent, goes offline,
 * or 30-tick timeout (~18 seconds).
 */
private fun waitForArrivalThen(player: PlayerRef, target: Position, ctx: PluginContext, onArrival: () -> Unit) {
    var ticksWaited = 0
    var arrivalDelay = -1  // -1 = not arrived yet, 0+ = counting down after arrival

    val task = ctx.schedule(delayTicks = 1, repeatTicks = 1) {
        ticksWaited++

        if (!player.isOnline) {
            cancelGathering(player)
            return@schedule
        }

        // Timeout
        if (ticksWaited > 30) {
            cancelGathering(player)
            return@schedule
        }

        val adjacent = player.position.distanceTo(target) <= INTERACT_DISTANCE
        val moving = player.isMoving

        // Already counting down the arrival delay
        if (arrivalDelay >= 0) {
            arrivalDelay++
            // Wait 2 ticks after server-side arrival so the client
            // finishes rendering the walk before we start animating
            if (arrivalDelay >= 1) {
                val wt = player.getAttribute<TaskHandle>(ATTR_WALKTO_TASK)
                wt?.cancel()
                player.removeAttribute(ATTR_WALKTO_TASK)
                onArrival()
            }
            return@schedule
        }

        // Still walking — wait
        if (moving && !adjacent) {
            return@schedule
        }

        // Server sees player as arrived + stopped — start the delay
        if (adjacent && !moving) {
            arrivalDelay = 0
            return@schedule
        }

        // Adjacent but still moving — wait for them to fully stop
        if (adjacent && moving) {
            return@schedule
        }

        // Stopped moving but NOT adjacent — gave up or pathing failed
        if (!moving && !adjacent) {
            cancelGathering(player)
            return@schedule
        }
    }
    player.setAttribute(ATTR_WALKTO_TASK, task)
}

// ============================================================
//  Woodcutting
// ============================================================

/**
 * Data class for a tree definition, loaded from data/trees/trees.json.
 */
data class TreeDef(
    val objectId: Int,
    val name: String,
    val logId: Int,
    val level: Int,
    val xp: Double,
    val respawnTicks: Int,
    val stumpId: Int
)

/**
 * Data class for an axe definition, loaded from data/axes/axes.json.
 */
data class AxeDef(
    val itemId: Int,
    val name: String,
    val wcLevel: Int,
    val animId: Int,
    val speed: Double
)

class WoodcuttingSkill(data: DataStore) {

    /** objectId → TreeDef */
    private var trees = mutableMapOf<Int, TreeDef>()

    /** Sorted by wcLevel descending so findBestAxe picks the highest usable tier. */
    private var axes = mutableListOf<AxeDef>()

    init { loadData(data) }

    fun reload(data: DataStore) { trees.clear(); axes.clear(); loadData(data) }

    private fun loadData(data: DataStore) {
        // Load trees from data/trees/ store
        val treeEntries = data.getAll("trees")
        for ((_, json) in treeEntries) {
            val id = json.get("id")?.asInt ?: continue
            val tree = TreeDef(
                objectId = id,
                name = json.get("name")?.asString ?: "Tree",
                logId = json.get("logId")?.asInt ?: 1511,
                level = json.get("level")?.asInt ?: 1,
                xp = json.get("xp")?.asDouble ?: 25.0,
                respawnTicks = json.get("respawnTicks")?.asInt ?: 30,
                stumpId = json.get("stumpId")?.asInt ?: 1342
            )
            trees[id] = tree
        }

        // Load axes from data/axes/ store
        val axeEntries = data.getAll("axes")
        for ((_, json) in axeEntries) {
            val axe = AxeDef(
                itemId = json.get("id")?.asInt ?: continue,
                name = json.get("name")?.asString ?: "Axe",
                wcLevel = json.get("wcLevel")?.asInt ?: 1,
                animId = json.get("animId")?.asInt ?: 879,
                speed = json.get("speed")?.asDouble ?: 1.0
            )
            axes.add(axe)
        }
        axes.sortByDescending { it.wcLevel }

        // Fallback defaults if JSON data is missing
        if (trees.isEmpty()) {
            trees[1276] = TreeDef(1276, "Tree", 1511, 1, 25.0, 30, 1342)
            trees[1278] = TreeDef(1278, "Tree", 1511, 1, 25.0, 30, 1342)
        }
        if (axes.isEmpty()) {
            axes.add(AxeDef(1351, "Bronze axe", 1, 879, 1.0))
        }
    }

    fun treeCount(): Int = trees.size
    fun axeCount(): Int = axes.size

    fun handleObjectClick(event: ObjectInteractEvent, ctx: PluginContext) {
        if (event.option != 1) return
        val tree = trees[event.objectId] ?: return
        val player = event.player

        // Cancel any existing gathering task
        cancelGathering(player)
        event.cancel()

        // Walk to the tree first, then start gathering
        waitForArrivalThen(player, event.position, ctx) {
            startChopping(player, tree, findBestAxe(player), event, ctx)
        }
    }

    /**
     * Called once the player is adjacent to the tree.
     * Validates requirements and starts the gathering loop.
     */
    private fun startChopping(player: PlayerRef, tree: TreeDef, axe: AxeDef?,
                              event: ObjectInteractEvent, ctx: PluginContext) {
        // Check level
        if (player.getLevel(Skills.WOODCUTTING) < tree.level) {
            player.sendMessage("You need a Woodcutting level of ${tree.level} to chop this tree.")
            return
        }

        // Find best axe
        if (axe == null) {
            player.sendMessage("You do not have an axe which you have the Woodcutting level to use.")
            return
        }

        // Check inventory space
        if (player.inventoryFreeSlots() == 0) {
            player.sendMessage("Your inventory is too full to hold any more logs.")
            return
        }

        // Start chopping
        player.sendMessage("You swing your axe at the tree.")
        player.animate(axe.animId)

        // Gathering loop: roll every GATHER_TICK_INTERVAL ticks
        val task = ctx.schedule(delayTicks = GATHER_TICK_INTERVAL, repeatTicks = GATHER_TICK_INTERVAL) {
            if (!player.isOnline) { cancelGathering(player); return@schedule }

            // Check if player walked away
            if (player.isMoving) {
                cancelGathering(player)
                player.resetAnimation()
                return@schedule
            }

            // Moved too far from tree (e.g. knocked back)
            if (player.position.distanceTo(event.position) > INTERACT_DISTANCE) {
                cancelGathering(player)
                player.resetAnimation()
                return@schedule
            }

            // Re-send animation every cycle to keep it looping
            player.animate(axe.animId)

            // Roll success
            val chance = successChance(player.getLevel(Skills.WOODCUTTING), tree.level, axe.speed)
            if (Math.random() < chance) {
                // Success — give log
                if (player.inventoryFreeSlots() == 0) {
                    player.sendMessage("Your inventory is too full to hold any more logs.")
                    cancelGathering(player)
                    player.resetAnimation()
                    return@schedule
                }

                val logName = tree.name.lowercase().replace(" tree", "").trim()
                player.addItem(tree.logId)
                player.sendInventory()
                player.sendMessage("You get some $logName logs.")
                ctx.events.emit(ExperienceEvent(player, Skills.WOODCUTTING, tree.xp))

                // Deplete tree (if stumpId is valid)
                if (tree.stumpId > 0) {
                    ctx.events.emit(ObjectReplaceEvent(
                        position = event.position,
                        originalId = tree.objectId,
                        replacementId = tree.stumpId,
                        respawnTicks = tree.respawnTicks
                    ))
                    cancelGathering(player)
                    player.resetAnimation()
                    return@schedule
                }

                // If no stump (e.g. hollow tree), keep chopping
                if (player.inventoryFreeSlots() == 0) {
                    player.sendMessage("Your inventory is too full to hold any more logs.")
                    cancelGathering(player)
                    player.resetAnimation()
                    return@schedule
                }
            }
            // Miss — animation already re-sent above, loop continues
        }
        player.setAttribute(ATTR_GATHER_TASK, task)
    }

    /**
     * Find the best usable axe. Checks equipped weapon slot first, then inventory.
     * Returns the highest-tier axe the player has the WC level to use.
     */
    private fun findBestAxe(player: PlayerRef): AxeDef? {
        return axes.firstOrNull { axe ->
            player.getLevel(Skills.WOODCUTTING) >= axe.wcLevel &&
            (player.hasItem(axe.itemId) || player.getEquipment(WEAPON_SLOT) == axe.itemId)
        }
    }

    /**
     * RS-style success chance per tick.
     *
     * Simplified formula (close to wiki):
     *   chance = (playerLevel - treeLevel + 20 + axeBonus) / 256
     *   axeBonus = (speed - 1.0) * 40  (so bronze=0, dragon=30)
     *   Clamped to [1/256, 255/256].
     */
    private fun successChance(playerLevel: Int, treeLevel: Int, axeSpeed: Double): Double {
        val axeBonus = (axeSpeed - 1.0) * 40.0
        val raw = (playerLevel - treeLevel + 20.0 + axeBonus) / 256.0
        return raw.coerceIn(1.0 / 256.0, 255.0 / 256.0)
    }
}

// ============================================================
//  Mining
// ============================================================

data class RockDef(
    val objectId: Int,
    val name: String,
    val oreId: Int,
    val level: Int,
    val xp: Double,
    val respawnTicks: Int,
    val emptyId: Int
)

data class PickaxeDef(
    val itemId: Int,
    val name: String,
    val miningLevel: Int,
    val animId: Int,
    val speed: Double
)

class MiningSkill(data: DataStore) {

    private var rocks = mutableMapOf<Int, RockDef>()
    private var pickaxes = mutableListOf<PickaxeDef>()

    init { loadData(data) }

    fun reload(data: DataStore) { rocks.clear(); pickaxes.clear(); loadData(data) }

    private fun loadData(data: DataStore) {
        // Load rocks
        val rockEntries = data.getAll("rocks")
        for ((_, json) in rockEntries) {
            val id = json.get("id")?.asInt ?: continue
            val rock = RockDef(
                objectId = id,
                name = json.get("name")?.asString ?: "Rock",
                oreId = json.get("oreId")?.asInt ?: 436,
                level = json.get("level")?.asInt ?: 1,
                xp = json.get("xp")?.asDouble ?: 17.5,
                respawnTicks = json.get("respawnTicks")?.asInt ?: 4,
                emptyId = json.get("emptyId")?.asInt ?: 7468
            )
            rocks[id] = rock
        }

        // Load pickaxes
        val pickEntries = data.getAll("pickaxes")
        for ((_, json) in pickEntries) {
            val pick = PickaxeDef(
                itemId = json.get("id")?.asInt ?: continue,
                name = json.get("name")?.asString ?: "Pickaxe",
                miningLevel = json.get("miningLevel")?.asInt ?: 1,
                animId = json.get("animId")?.asInt ?: 625,
                speed = json.get("speed")?.asDouble ?: 1.0
            )
            pickaxes.add(pick)
        }
        pickaxes.sortByDescending { it.miningLevel }

        // Fallback defaults
        if (rocks.isEmpty()) {
            rocks[7484] = RockDef(7484, "Copper rock", 436, 1, 17.5, 4, 7468)
        }
        if (pickaxes.isEmpty()) {
            pickaxes.add(PickaxeDef(1265, "Bronze pickaxe", 1, 625, 1.0))
        }
    }

    fun rockCount(): Int = rocks.size
    fun pickCount(): Int = pickaxes.size

    fun handleObjectClick(event: ObjectInteractEvent, ctx: PluginContext) {
        if (event.option != 1) return
        val rock = rocks[event.objectId] ?: return
        val player = event.player

        // Cancel any existing gathering task
        cancelGathering(player)
        event.cancel()

        // Walk to the rock first, then start gathering
        waitForArrivalThen(player, event.position, ctx) {
            startMining(player, rock, findBestPickaxe(player), event, ctx)
        }
    }

    /**
     * Called once the player is adjacent to the rock.
     * Validates requirements and starts the gathering loop.
     */
    private fun startMining(player: PlayerRef, rock: RockDef, pick: PickaxeDef?,
                            event: ObjectInteractEvent, ctx: PluginContext) {
        // Check level
        if (player.getLevel(Skills.MINING) < rock.level) {
            player.sendMessage("You need a Mining level of ${rock.level} to mine this rock.")
            return
        }

        // Find best pickaxe
        if (pick == null) {
            player.sendMessage("You do not have a pickaxe which you have the Mining level to use.")
            return
        }

        // Check inventory space
        if (player.inventoryFreeSlots() == 0) {
            player.sendMessage("Your inventory is too full to hold any more ore.")
            return
        }

        // Start mining
        player.sendMessage("You swing your pick at the rock.")
        player.animate(pick.animId)

        // Gathering loop
        val task = ctx.schedule(delayTicks = GATHER_TICK_INTERVAL, repeatTicks = GATHER_TICK_INTERVAL) {
            if (!player.isOnline) { cancelGathering(player); return@schedule }

            if (player.isMoving) {
                cancelGathering(player)
                player.resetAnimation()
                return@schedule
            }

            // Moved too far from rock
            if (player.position.distanceTo(event.position) > INTERACT_DISTANCE) {
                cancelGathering(player)
                player.resetAnimation()
                return@schedule
            }

            // Re-send animation every cycle to keep it looping
            player.animate(pick.animId)

            val chance = successChance(player.getLevel(Skills.MINING), rock.level, pick.speed)
            if (Math.random() < chance) {
                if (player.inventoryFreeSlots() == 0) {
                    player.sendMessage("Your inventory is too full to hold any more ore.")
                    cancelGathering(player)
                    player.resetAnimation()
                    return@schedule
                }

                val oreName = rock.name.lowercase().replace(" rock", "").trim()
                player.addItem(rock.oreId)
                player.sendInventory()
                player.sendMessage("You manage to mine some $oreName.")
                ctx.events.emit(ExperienceEvent(player, Skills.MINING, rock.xp))

                // Deplete rock
                if (rock.emptyId > 0) {
                    ctx.events.emit(ObjectReplaceEvent(
                        position = event.position,
                        originalId = rock.objectId,
                        replacementId = rock.emptyId,
                        respawnTicks = rock.respawnTicks
                    ))
                    cancelGathering(player)
                    player.resetAnimation()
                    return@schedule
                }

                // Continue mining if no depletion
                if (player.inventoryFreeSlots() == 0) {
                    player.sendMessage("Your inventory is too full to hold any more ore.")
                    cancelGathering(player)
                    player.resetAnimation()
                    return@schedule
                }
            }
            // Miss — animation already re-sent above, loop continues
        }
        player.setAttribute(ATTR_GATHER_TASK, task)
    }

    private fun findBestPickaxe(player: PlayerRef): PickaxeDef? {
        return pickaxes.firstOrNull { pick ->
            player.getLevel(Skills.MINING) >= pick.miningLevel &&
            (player.hasItem(pick.itemId) || player.getEquipment(WEAPON_SLOT) == pick.itemId)
        }
    }

    private fun successChance(playerLevel: Int, rockLevel: Int, pickSpeed: Double): Double {
        val pickBonus = (pickSpeed - 1.0) * 40.0
        val raw = (playerLevel - rockLevel + 20.0 + pickBonus) / 256.0
        return raw.coerceIn(1.0 / 256.0, 255.0 / 256.0)
    }
}
