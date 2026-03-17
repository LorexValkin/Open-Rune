package com.openrune.core.world.pathfinding

import com.openrune.api.world.Position
import com.openrune.core.world.collision.CollisionMap
import com.openrune.core.world.collision.Direction
import java.util.LinkedList
import java.util.PriorityQueue

/**
 * A* pathfinding on the collision map.
 *
 * Finds the shortest walkable path between two positions,
 * respecting walls, objects, and entity size.
 *
 * ENGINE-LEVEL system. Plugins cannot replace the pathfinder.
 * Plugins can trigger pathing via the API (player.walkTo, npc.walkTo).
 */
class Pathfinder(private val collisionMap: CollisionMap) {

    companion object {
        /** Maximum tiles to explore before giving up. Prevents server stalling. */
        const val MAX_SEARCH = 4096

        /** Maximum path length (tiles). */
        const val MAX_PATH_LENGTH = 100

        /** The 8 directions to explore, cardinals first (cheaper). */
        private val DIRECTIONS = arrayOf(
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST,
            Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.SOUTH_WEST, Direction.NORTH_WEST
        )
    }

    /**
     * A completed path result.
     */
    data class Path(
        /** Ordered list of positions from start to destination (excluding start). */
        val steps: List<Position>,
        /** True if the path reaches the exact destination. */
        val reachedDestination: Boolean,
        /** The final position of the path (may differ from target if blocked). */
        val end: Position
    ) {
        val isEmpty: Boolean get() = steps.isEmpty()
        val size: Int get() = steps.size
    }

    /**
     * Internal node for the A* open set.
     */
    private data class Node(
        val x: Int,
        val y: Int,
        val gCost: Int,       // Cost from start
        val hCost: Int,       // Heuristic to goal
        val parent: Node?
    ) : Comparable<Node> {
        val fCost: Int get() = gCost + hCost
        override fun compareTo(other: Node): Int = fCost.compareTo(other.fCost)
    }

    /**
     * Find a path from [start] to [target].
     *
     * @param start Starting position.
     * @param target Desired destination.
     * @param entitySize Size of the entity (1 for players, varies for NPCs).
     * @param reachDistance If > 0, path stops when within this distance of target
     *                     (useful for interacting with objects/NPCs from adjacent tiles).
     * @return A [Path] result. May be empty if no path exists.
     */
    fun findPath(
        start: Position,
        target: Position,
        entitySize: Int = 1,
        reachDistance: Int = 0
    ): Path {
        // Same tile
        if (start == target) return Path(emptyList(), true, start)

        // Different height levels
        if (start.z != target.z) return Path(emptyList(), false, start)

        val height = start.z
        val openSet = PriorityQueue<Node>()
        val visited = HashSet<Long>(MAX_SEARCH)

        val startNode = Node(start.x, start.y, 0, heuristic(start.x, start.y, target.x, target.y), null)
        openSet.add(startNode)

        var closestNode = startNode
        var closestDist = heuristic(start.x, start.y, target.x, target.y)
        var searched = 0

        while (openSet.isNotEmpty() && searched < MAX_SEARCH) {
            val current = openSet.poll()
            searched++

            val key = packCoord(current.x, current.y)
            if (key in visited) continue
            visited.add(key)

            // Check if we've reached the target (or close enough)
            val dist = chebyshev(current.x, current.y, target.x, target.y)
            if (dist <= reachDistance || (current.x == target.x && current.y == target.y)) {
                return buildPath(current, start)
            }

            // Track closest node in case we can't reach the target
            if (dist < closestDist) {
                closestDist = dist
                closestNode = current
            }

            // Explore neighbors
            for (dir in DIRECTIONS) {
                val nx = current.x + dir.dx
                val ny = current.y + dir.dy

                if (packCoord(nx, ny) in visited) continue

                // Collision check using the map
                if (!collisionMap.canTraverse(current.x, current.y, height, entitySize, entitySize, dir)) {
                    continue
                }

                val stepCost = if (dir.isDiagonal) 14 else 10  // sqrt(2)*10 vs 10
                val gCost = current.gCost + stepCost
                val hCost = heuristic(nx, ny, target.x, target.y)

                // Don't exceed max path length
                if (gCost / 10 > MAX_PATH_LENGTH) continue

                openSet.add(Node(nx, ny, gCost, hCost, current))
            }
        }

        // Couldn't reach target; return path to closest reachable tile
        return if (closestNode != startNode) {
            buildPath(closestNode, start).copy(reachedDestination = false)
        } else {
            Path(emptyList(), false, start)
        }
    }

    /**
     * Build the step list by walking backwards from the goal node.
     */
    private fun buildPath(endNode: Node, start: Position): Path {
        val steps = LinkedList<Position>()
        var node: Node? = endNode

        while (node != null && !(node.x == start.x && node.y == start.y)) {
            steps.addFirst(Position(node.x, node.y, start.z))
            node = node.parent
        }

        return Path(
            steps = steps,
            reachedDestination = true,
            end = if (steps.isNotEmpty()) steps.last() else start
        )
    }

    /**
     * Octile distance heuristic (consistent for 8-directional movement).
     * Scaled by 10 to match the integer g-costs.
     */
    private fun heuristic(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        val dx = kotlin.math.abs(x1 - x2)
        val dy = kotlin.math.abs(y1 - y2)
        return 10 * (dx + dy) + (14 - 20) * minOf(dx, dy)
    }

    /**
     * Chebyshev distance (max of dx, dy). Used for reach distance checks.
     */
    private fun chebyshev(x1: Int, y1: Int, x2: Int, y2: Int): Int =
        maxOf(kotlin.math.abs(x1 - x2), kotlin.math.abs(y1 - y2))

    /**
     * Pack coordinates into a single long for the visited set.
     */
    private fun packCoord(x: Int, y: Int): Long =
        (x.toLong() shl 32) or (y.toLong() and 0xFFFFFFFFL)
}
