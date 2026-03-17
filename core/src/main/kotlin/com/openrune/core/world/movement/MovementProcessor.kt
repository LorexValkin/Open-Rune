package com.openrune.core.world.movement

import com.openrune.api.world.Position
import com.openrune.core.world.collision.CollisionMap
import com.openrune.core.world.collision.Direction
import com.openrune.core.world.pathfinding.Pathfinder
import org.slf4j.LoggerFactory
import java.util.ArrayDeque

/**
 * Handles entity movement: step queues, walking, running, teleportation.
 *
 * Each entity has a [WalkingQueue] that holds pending steps.
 * Each tick, the [MovementProcessor] pops 1 step (walking) or 2 steps (running)
 * from the queue and moves the entity, checking collision at each step.
 *
 * ENGINE-LEVEL system. Plugins interact with movement through the API
 * (player.walkTo, player.teleport) and can intercept via PlayerMoveEvent.
 * Plugins CANNOT replace the movement processor or collision checks.
 */
class WalkingQueue {

    /** Pending steps to be processed. Each entry is a world position. */
    private val steps = ArrayDeque<Position>(128)

    /** The primary direction moved this tick (for update packet). */
    var primaryDirection: Direction = Direction.NONE

    /** The secondary direction moved this tick (running second step). */
    var secondaryDirection: Direction = Direction.NONE

    /** Whether the entity is running. Defaults to true (PI: isRunning2 = true). */
    var running: Boolean = true

    /** Whether the entity's movement was a teleport this tick. */
    var didTeleport: Boolean = false

    /** Whether the queue was modified this tick (for region update checks). */
    var needsPlacement: Boolean = false

    /**
     * Clear the step queue and reset movement state.
     */
    fun clear() {
        steps.clear()
        resetDirections()
    }

    /**
     * Reset per-tick direction state. Called at the end of each tick.
     */
    fun resetDirections() {
        primaryDirection = Direction.NONE
        secondaryDirection = Direction.NONE
        didTeleport = false
    }

    /**
     * Add a series of steps from a path to the queue.
     * Replaces any existing steps.
     */
    fun setPath(path: Pathfinder.Path) {
        steps.clear()
        for (step in path.steps) {
            steps.addLast(step)
        }
    }

    /**
     * Add a single destination. The pathfinder should have been called
     * before this to get the actual step positions.
     */
    fun addStep(position: Position) {
        steps.addLast(position)
    }

    /**
     * Add waypoints from a walking packet.
     * The client sends the final destination and intermediate waypoints.
     */
    fun addStepsFromPacket(positions: List<Position>) {
        steps.clear()
        for (pos in positions) {
            steps.addLast(pos)
        }
    }

    fun hasSteps(): Boolean = steps.isNotEmpty()
    fun peekNext(): Position? = steps.peekFirst()
    fun pollNext(): Position? = steps.pollFirst()
    fun stepCount(): Int = steps.size
    fun lastStep(): Position? = steps.peekLast()
}

/**
 * Processes movement for all entities each tick.
 * Called by the game engine as part of the tick cycle.
 */
class MovementProcessor(
    private val collisionMap: CollisionMap,
    private val pathfinder: Pathfinder
) {

    private val log = LoggerFactory.getLogger(MovementProcessor::class.java)

    /**
     * Describes an entity's movement capability for the processor.
     * Both Player and NPC implement this internally.
     */
    interface Movable {
        var position: Position
        val walkingQueue: WalkingQueue
        val entitySize: Int get() = 1
        val isPlayer: Boolean get() = false

        /** Called when the entity successfully moves. */
        fun onMove(from: Position, to: Position, direction: Direction)

        /** Called when the entity needs a region update sent. */
        fun onRegionChange()
    }

    /**
     * Process one tick of movement for a single entity.
     * Pops up to 2 steps (1 walk + 1 run) from the queue.
     */
    fun process(entity: Movable) {
        val queue = entity.walkingQueue

        // Skip if teleporting (handled elsewhere)
        if (queue.didTeleport) return

        // Reset this tick's directions
        queue.primaryDirection = Direction.NONE
        queue.secondaryDirection = Direction.NONE

        if (!queue.hasSteps()) return

        // First step (walking)
        val walkResult = takeStep(entity)
        if (walkResult != Direction.NONE) {
            queue.primaryDirection = walkResult
        }

        // Second step (running)
        if (queue.running && queue.hasSteps()) {
            val runResult = takeStep(entity)
            if (runResult != Direction.NONE) {
                queue.secondaryDirection = runResult
            }
        }
    }
    /**
     * Take a single step from the queue.
     *
     * Players: NO collision check. The 317 client does BFS pathfinding and
     * sends pre-validated waypoints. Server just walks the path (PI behavior).
     *
     * NPCs: YES collision check. They have no client to pathfind for them.
     */
    private fun takeStep(entity: Movable): Direction {
        val queue = entity.walkingQueue
        val next = queue.peekNext() ?: return Direction.NONE
        val current = entity.position

        val dir = Direction.between(current.x, current.y, next.x, next.y)
        if (dir == Direction.NONE) {
            queue.pollNext()
            return Direction.NONE
        }

        // NPC-only collision check
        if (!entity.isPlayer) {
            if (!collisionMap.canTraverse(current.x, current.y, current.z,
                    entity.entitySize, entity.entitySize, dir)) {
                queue.clear()
                return Direction.NONE
            }
        }

        // Move
        val from = entity.position
        entity.position = Position(current.x + dir.dx, current.y + dir.dy, current.z)

        if (entity.position.x == next.x && entity.position.y == next.y) {
            queue.pollNext()
        }

        val regionChanged = (from.regionX != entity.position.regionX) ||
                            (from.regionY != entity.position.regionY)
        if (regionChanged) entity.onRegionChange()
        entity.onMove(from, entity.position, dir)
        return dir
    }

    /**
     * Calculate a path and set it on the entity's walking queue.
     * This is the main entry point for "walk this entity to position X".
     */
    fun walkTo(entity: Movable, target: Position, reachDistance: Int = 0) {
        val path = pathfinder.findPath(entity.position, target, entity.entitySize, reachDistance)
        entity.walkingQueue.clear()
        entity.walkingQueue.setPath(path)
    }
}
