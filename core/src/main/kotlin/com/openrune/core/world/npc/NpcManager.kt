package com.openrune.core.world.npc

import com.openrune.api.config.NpcDef
import com.openrune.api.config.SpawnDef
import com.openrune.api.event.NpcDespawnEvent
import com.openrune.api.event.NpcSpawnEvent
import com.openrune.api.world.Position
import com.openrune.core.event.EventBusImpl
import com.openrune.core.io.JsonDataStore
import com.openrune.core.world.collision.CollisionMap
import com.openrune.core.world.movement.MovementProcessor
import org.slf4j.LoggerFactory

/**
 * Manages all NPCs in the game world.
 *
 * Responsibilities:
 *   - Spawns NPCs from data/spawns/ (.json files) at startup
 *   - Maintains the NPC index array (max 16384 as per 317 protocol)
 *   - Processes NPC ticks: random walk, respawn countdowns
 *   - Provides NPC lookup for packet handlers and plugins
 *
 * ENGINE-LEVEL system. Plugins can interact with NPCs through [NpcRef]
 * and listen to NPC events, but cannot replace the manager.
 */
class NpcManager(
    private val maxNpcs: Int = 16384,
    private val eventBus: EventBusImpl,
    private val collisionMap: CollisionMap,
    private val movementProcessor: MovementProcessor
) {

    private val log = LoggerFactory.getLogger(NpcManager::class.java)

    /** The NPC array. Index 0 is unused. */
    private val npcs = arrayOfNulls<Npc>(maxNpcs)

    /** Count of active NPCs. */
    var count: Int = 0
        private set

    // ================================================================
    //  Spawning
    // ================================================================

    /**
     * Load spawns from the data store and create all NPCs.
     */
    fun loadSpawns(dataStore: JsonDataStore) {
        val spawns = dataStore.getAllTyped("spawns", SpawnDef::class.java)
        val npcDefs = dataStore.getAllTyped("npcs", NpcDef::class.java)

        var spawned = 0
        for ((_, spawn) in spawns) {
            val position = Position(spawn.x, spawn.y, spawn.z)
            val npcDef = npcDefs[spawn.npcId]

            val definition = if (npcDef != null) {
                NpcDefinition(
                    name = npcDef.name,
                    combatLevel = npcDef.combatLevel,
                    hitpoints = npcDef.hitpoints,
                    maxHit = npcDef.maxHit,
                    attackSpeed = npcDef.attackSpeed,
                    respawnTicks = npcDef.respawnTicks,
                    aggressive = npcDef.aggressive,
                    aggroRange = npcDef.aggroRange,
                    size = npcDef.size,
                    walkRange = spawn.walkRange,
                    attackAnim = npcDef.attackAnim,
                    defenceAnim = npcDef.defenceAnim,
                    deathAnim = npcDef.deathAnim
                )
            } else {
                NpcDefinition(name = "NPC #${spawn.npcId}", walkRange = spawn.walkRange)
            }

            val npc = spawn(spawn.npcId, position, definition)
            if (npc != null) {
                spawned++

                // Set initial facing direction if specified
                if (spawn.facing != 0) {
                    val fx = position.x + when (spawn.facing) { 1 -> 0; 2 -> 1; 3 -> 0; 4 -> -1; else -> 0 }
                    val fy = position.y + when (spawn.facing) { 1 -> 1; 2 -> 0; 3 -> -1; 4 -> 0; else -> 0 }
                    npc.facePosition(fx, fy)
                }
            }
        }

        log.info("Spawned {} NPCs from {} spawn definitions", spawned, spawns.size)
    }

    /**
     * Spawn a single NPC and assign it an index.
     */
    fun spawn(npcId: Int, position: Position, definition: NpcDefinition = NpcDefinition()): Npc? {
        for (i in 1 until maxNpcs) {
            if (npcs[i] == null) {
                val npc = Npc(
                    id = npcId,
                    index = i,
                    position = position,
                    spawnPosition = position,
                    def = definition
                )
                npcs[i] = npc
                count++

                // Add collision for large NPCs
                if (definition.size > 1) {
                    collisionMap.addObject(position.x, position.y, position.z,
                        definition.size, definition.size, true)
                }

                eventBus.emit(NpcSpawnEvent(npc))
                return npc
            }
        }
        log.warn("NPC array full, cannot spawn NPC {}", npcId)
        return null
    }

    /**
     * Remove an NPC by index.
     */
    fun remove(index: Int) {
        val npc = npcs[index] ?: return
        npc.active = false

        // Remove collision
        if (npc.def.size > 1) {
            collisionMap.removeObject(npc.position.x, npc.position.y, npc.position.z,
                npc.def.size, npc.def.size)
        }

        eventBus.emit(NpcDespawnEvent(npc))
        npcs[index] = null
        count--
    }

    // ================================================================
    //  Lookup
    // ================================================================

    fun getByIndex(index: Int): Npc? {
        if (index < 0 || index >= maxNpcs) return null
        return npcs[index]
    }

    fun allNpcs(): List<Npc> = npcs.filterNotNull().filter { it.active }

    fun findByNpcId(npcId: Int): List<Npc> = allNpcs().filter { it.id == npcId }

    fun findNearby(position: Position, range: Int = 15): List<Npc> =
        allNpcs().filter { it.position.isWithinDistance(position, range) }

    // ================================================================
    //  Tick processing (ENGINE-LEVEL)
    // ================================================================

    /**
     * Process all NPCs for one game tick.
     * Called by the GameEngine in the tick cycle.
     *
     * Handles:
     *   - Respawn countdowns
     *   - Random walking (simple single-step with collision)
     *   - Movement processing
     */
    fun process() {
        for (i in 1 until maxNpcs) {
            val npc = npcs[i] ?: continue

            // Handle respawn countdown
            if (!npc.active) {
                if (npc.respawnCountdown > 0) {
                    npc.respawnCountdown--
                    if (npc.respawnCountdown <= 0) {
                        npc.performRespawn()
                    }
                }
                continue
            }

            // Skip dead NPCs
            if (npc.isDead) continue

            // Random walk: single-step with collision check (no pathfinding)
            if (!npc.hasAttribute("ai_override") && !npc.isMoving) {
                processRandomWalk(npc)
            }

            // Process movement (pops steps from queue, checks collision per step)
            movementProcessor.process(npc)
        }
    }
    /**
     * Natural random walk: NPCs pick a random destination within walk range
     * and queue tile-by-tile steps toward it. The MovementProcessor handles
     * all collision checking — if a step is blocked, the path stops there.
     *
     * Movement is identical to player walking (1 tile per tick, same collision).
     * NPCs have walkingQueue.running=false so they never run.
     *
     * Behavior:
     *   - 15% idle chance each cycle
     *   - 2-7 tick pause between walks
     *   - Picks destination 2-4 tiles away
     *   - Hard leash: destination clamped to walkRange from spawn
     *   - Soft leash: biases toward spawn when past 50% of range
     */
    private fun processRandomWalk(npc: Npc) {
        if (npc.def.walkRange <= 0) return
        if (--npc.randomWalkTimer > 0) return

        npc.randomWalkTimer = 2 + (Math.random() * 6).toInt()

        // 15% chance to idle
        if (Math.random() < 0.15) return

        val range = npc.def.walkRange
        val spawnX = npc.spawnPosition.x
        val spawnY = npc.spawnPosition.y
        val height = npc.position.z

        // Soft leash: bias toward spawn when far
        val distFromSpawn = npc.position.distanceTo(npc.spawnPosition)
        val biasHome = distFromSpawn > (range * 0.5).toInt().coerceAtLeast(2)

        val destX: Int
        val destY: Int
        if (biasHome && Math.random() < 0.65) {
            val halfRange = (range / 2).coerceAtLeast(1)
            destX = spawnX + (-halfRange..halfRange).random()
            destY = spawnY + (-halfRange..halfRange).random()
        } else {
            destX = spawnX + (-range..range).random()
            destY = spawnY + (-range..range).random()
        }

        if (destX == npc.position.x && destY == npc.position.y) return

        // Walk distance: 2-4 tiles
        val maxSteps = 2 + (Math.random() * 3).toInt()

        // Queue steps toward destination — MovementProcessor will check
        // collision on each step and stop if blocked (same as player)
        val steps = mutableListOf<com.openrune.api.world.Position>()
        var curX = npc.position.x
        var curY = npc.position.y

        for (step in 0 until maxSteps) {
            val dx = (destX - curX).coerceIn(-1, 1)
            val dy = (destY - curY).coerceIn(-1, 1)
            if (dx == 0 && dy == 0) break

            val nextX = curX + dx
            val nextY = curY + dy
            val nextPos = com.openrune.api.world.Position(nextX, nextY, height)

            // Hard leash — don't queue steps beyond walk range
            if (nextPos.distanceTo(npc.spawnPosition) > range) break

            steps.add(nextPos)
            curX = nextX
            curY = nextY
        }

        if (steps.isNotEmpty()) {
            npc.walkingQueue.clear()
            for (step in steps) {
                npc.walkingQueue.addStep(step)
            }
        }
    }

    /**
     * Reset all NPC update flags. Called at end of tick.
     */
    fun resetFlags() {
        for (i in 1 until maxNpcs) {
            npcs[i]?.takeIf { it.active }?.resetUpdateFlags()
        }
    }
}