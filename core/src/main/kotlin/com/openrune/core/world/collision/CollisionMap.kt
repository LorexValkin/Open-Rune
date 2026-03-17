package com.openrune.core.world.collision

import org.slf4j.LoggerFactory

/**
 * Core collision flag constants.
 *
 * Each tile in the world has a 32-bit collision mask.
 * Flags indicate which directions are blocked and what type of object occupies the tile.
 * These are loaded from the cache map data and modified at runtime when objects spawn/despawn.
 *
 * This is an ENGINE-LEVEL system. Plugins cannot replace it.
 * Plugins can query collision state through the API but cannot modify flag constants.
 */
object CollisionFlag {

    // === Directional wall flags ===
    const val WALL_NORTH         = 0x2
    const val WALL_EAST          = 0x8
    const val WALL_SOUTH         = 0x20
    const val WALL_WEST          = 0x80

    // === Diagonal wall flags ===
    const val WALL_NORTH_EAST    = 0x4
    const val WALL_SOUTH_EAST    = 0x10
    const val WALL_SOUTH_WEST    = 0x40
    const val WALL_NORTH_WEST    = 0x1

    // === Full tile blockage (objects, solid deco) ===
    const val OBJECT_TILE        = 0x100
    const val FLOOR_DECO         = 0x40000

    // === Projectile versions (ranged/magic can fly over low walls but not full objects) ===
    const val PROJECTILE_NORTH       = 0x400
    const val PROJECTILE_EAST        = 0x1000
    const val PROJECTILE_SOUTH       = 0x4000
    const val PROJECTILE_WEST        = 0x10000
    const val PROJECTILE_NORTH_EAST  = 0x800
    const val PROJECTILE_SOUTH_EAST  = 0x2000
    const val PROJECTILE_SOUTH_WEST  = 0x8000
    const val PROJECTILE_NORTH_WEST  = 0x200
    const val PROJECTILE_FULL        = 0x20000

    // === Combined masks for movement checks ===
    const val BLOCK_NORTH = WALL_NORTH or OBJECT_TILE or FLOOR_DECO
    const val BLOCK_EAST  = WALL_EAST or OBJECT_TILE or FLOOR_DECO
    const val BLOCK_SOUTH = WALL_SOUTH or OBJECT_TILE or FLOOR_DECO
    const val BLOCK_WEST  = WALL_WEST or OBJECT_TILE or FLOOR_DECO

    const val BLOCK_NORTH_EAST = WALL_NORTH_EAST or WALL_NORTH or WALL_EAST or OBJECT_TILE or FLOOR_DECO
    const val BLOCK_SOUTH_EAST = WALL_SOUTH_EAST or WALL_SOUTH or WALL_EAST or OBJECT_TILE or FLOOR_DECO
    const val BLOCK_SOUTH_WEST = WALL_SOUTH_WEST or WALL_SOUTH or WALL_WEST or OBJECT_TILE or FLOOR_DECO
    const val BLOCK_NORTH_WEST = WALL_NORTH_WEST or WALL_NORTH or WALL_WEST or OBJECT_TILE or FLOOR_DECO

    /** Check if a flag mask blocks movement in the given direction. */
    fun isBlocked(flags: Int, direction: Direction): Boolean {
        return when (direction) {
            Direction.NORTH      -> flags and BLOCK_NORTH != 0
            Direction.EAST       -> flags and BLOCK_EAST != 0
            Direction.SOUTH      -> flags and BLOCK_SOUTH != 0
            Direction.WEST       -> flags and BLOCK_WEST != 0
            Direction.NORTH_EAST -> flags and BLOCK_NORTH_EAST != 0
            Direction.SOUTH_EAST -> flags and BLOCK_SOUTH_EAST != 0
            Direction.SOUTH_WEST -> flags and BLOCK_SOUTH_WEST != 0
            Direction.NORTH_WEST -> flags and BLOCK_NORTH_WEST != 0
            Direction.NONE       -> false
        }
    }
}

/**
 * The 8 cardinal + ordinal directions plus NONE.
 * Each direction has an x/y delta for stepping.
 */
enum class Direction(val dx: Int, val dy: Int) {
    NONE(0, 0),
    NORTH(0, 1),
    NORTH_EAST(1, 1),
    EAST(1, 0),
    SOUTH_EAST(1, -1),
    SOUTH(0, -1),
    SOUTH_WEST(-1, -1),
    WEST(-1, 0),
    NORTH_WEST(-1, 1);

    /** True if this is a diagonal direction. */
    val isDiagonal: Boolean get() = dx != 0 && dy != 0

    companion object {
        /**
         * Get the direction from one tile to an adjacent tile.
         * Returns NONE if the tiles are not adjacent or are the same tile.
         */
        fun between(fromX: Int, fromY: Int, toX: Int, toY: Int): Direction {
            val dx = (toX - fromX).coerceIn(-1, 1)
            val dy = (toY - fromY).coerceIn(-1, 1)
            return entries.firstOrNull { it.dx == dx && it.dy == dy } ?: NONE
        }
    }
}

/**
 * Collision map for the game world.
 *
 * The world is divided into regions (64x64 tiles). Each region at each height level
 * has its own collision flag array. Flags are loaded from cache data at startup
 * and modified when game objects are spawned or removed.
 *
 * This is the single source of truth for "can entity X move to tile Y?"
 * The pathfinder and movement system both query this.
 */
class CollisionMap {

    private val log = LoggerFactory.getLogger(CollisionMap::class.java)

    /**
     * Key: packed region key (regionX << 16 | regionY << 8 | height)
     * Value: 64x64 int array of collision flags
     */
    private val regions = HashMap<Int, IntArray>(1024)

    companion object {
        const val REGION_SIZE = 64
        private fun regionKey(regionX: Int, regionY: Int, height: Int): Int =
            (regionX shl 16) or (regionY shl 8) or (height and 3)

        private fun localIndex(localX: Int, localY: Int): Int =
            localX * REGION_SIZE + localY
    }

    /**
     * Get or create the collision flag array for a region.
     */
    private fun getOrCreateRegion(regionX: Int, regionY: Int, height: Int): IntArray {
        val key = regionKey(regionX, regionY, height)
        return regions.getOrPut(key) { IntArray(REGION_SIZE * REGION_SIZE) }
    }

    /**
     * Get collision flags for an absolute world coordinate.
     * Returns 0 (no collision) if the region hasn't been loaded.
     */
    fun getFlags(x: Int, y: Int, height: Int): Int {
        val regionX = x shr 6
        val regionY = y shr 6
        val key = regionKey(regionX, regionY, height)
        val region = regions[key] ?: return 0
        val localX = x and 63
        val localY = y and 63
        return region[localIndex(localX, localY)]
    }

    /**
     * Add collision flags at an absolute world coordinate.
     */
    fun addFlag(x: Int, y: Int, height: Int, flag: Int) {
        val regionX = x shr 6
        val regionY = y shr 6
        val region = getOrCreateRegion(regionX, regionY, height)
        val localX = x and 63
        val localY = y and 63
        region[localIndex(localX, localY)] = region[localIndex(localX, localY)] or flag
    }

    /**
     * Remove collision flags at an absolute world coordinate.
     */
    fun removeFlag(x: Int, y: Int, height: Int, flag: Int) {
        val regionX = x shr 6
        val regionY = y shr 6
        val key = regionKey(regionX, regionY, height)
        val region = regions[key] ?: return
        val localX = x and 63
        val localY = y and 63
        region[localIndex(localX, localY)] = region[localIndex(localX, localY)] and flag.inv()
    }

    /**
     * Set the full collision mask for a tile (used during cache loading).
     */
    fun setFlags(x: Int, y: Int, height: Int, flags: Int) {
        val regionX = x shr 6
        val regionY = y shr 6
        val region = getOrCreateRegion(regionX, regionY, height)
        val localX = x and 63
        val localY = y and 63
        region[localIndex(localX, localY)] = flags
    }

    // ================================================================
    //  Movement queries (used by pathfinder and movement system)
    // ================================================================

    /**
     * Check if a 1x1 entity can step from (x, y) in the given direction.
     * Checks both the source tile's exit flags and the destination tile's entry flags.
     */
    fun canTraverse(x: Int, y: Int, height: Int, direction: Direction): Boolean {
        if (direction == Direction.NONE) return true

        val destX = x + direction.dx
        val destY = y + direction.dy

        // For diagonals, also check the two cardinal components
        if (direction.isDiagonal) {
            val horizontalDir = Direction.between(x, y, destX, y)
            val verticalDir = Direction.between(x, y, x, destY)

            // Must be able to step both cardinally to reach the diagonal
            if (!canTraverseSingle(x, y, height, horizontalDir)) return false
            if (!canTraverseSingle(x, y, height, verticalDir)) return false

            // Check the diagonal tile itself from each approach angle
            if (!canTraverseSingle(x + horizontalDir.dx, y, height, verticalDir)) return false
            if (!canTraverseSingle(x, y + verticalDir.dy, height, horizontalDir)) return false
        }

        return canTraverseSingle(x, y, height, direction)
    }

    /**
     * Single-direction traversal check (no diagonal decomposition).
     */
    private fun canTraverseSingle(x: Int, y: Int, height: Int, direction: Direction): Boolean {
        val destX = x + direction.dx
        val destY = y + direction.dy
        val destFlags = getFlags(destX, destY, height)

        // Check if the destination tile blocks entry from our direction
        val oppositeBlock = when (direction) {
            Direction.NORTH      -> CollisionFlag.BLOCK_SOUTH
            Direction.EAST       -> CollisionFlag.BLOCK_WEST
            Direction.SOUTH      -> CollisionFlag.BLOCK_NORTH
            Direction.WEST       -> CollisionFlag.BLOCK_EAST
            Direction.NORTH_EAST -> CollisionFlag.BLOCK_SOUTH_WEST
            Direction.SOUTH_EAST -> CollisionFlag.BLOCK_NORTH_WEST
            Direction.SOUTH_WEST -> CollisionFlag.BLOCK_NORTH_EAST
            Direction.NORTH_WEST -> CollisionFlag.BLOCK_SOUTH_EAST
            Direction.NONE       -> return true
        }

        return destFlags and oppositeBlock == 0
    }

    /**
     * Check if a larger entity (sizeX x sizeY) can step in a direction.
     * Tests all edge tiles of the entity's footprint.
     */
    fun canTraverse(x: Int, y: Int, height: Int, sizeX: Int, sizeY: Int, direction: Direction): Boolean {
        if (sizeX == 1 && sizeY == 1) return canTraverse(x, y, height, direction)

        // Check all tiles along the leading edge in the direction of movement
        when (direction) {
            Direction.NORTH -> {
                for (dx in 0 until sizeX) {
                    if (!canTraverse(x + dx, y + sizeY - 1, height, direction)) return false
                }
            }
            Direction.SOUTH -> {
                for (dx in 0 until sizeX) {
                    if (!canTraverse(x + dx, y, height, direction)) return false
                }
            }
            Direction.EAST -> {
                for (dy in 0 until sizeY) {
                    if (!canTraverse(x + sizeX - 1, y + dy, height, direction)) return false
                }
            }
            Direction.WEST -> {
                for (dy in 0 until sizeY) {
                    if (!canTraverse(x, y + dy, height, direction)) return false
                }
            }
            Direction.NORTH_EAST -> {
                if (!canTraverse(x + sizeX - 1, y + sizeY - 1, height, direction)) return false
                for (dx in 0 until sizeX - 1) {
                    if (!canTraverse(x + dx, y + sizeY - 1, height, Direction.NORTH)) return false
                }
                for (dy in 0 until sizeY - 1) {
                    if (!canTraverse(x + sizeX - 1, y + dy, height, Direction.EAST)) return false
                }
            }
            Direction.SOUTH_EAST -> {
                if (!canTraverse(x + sizeX - 1, y, height, direction)) return false
                for (dx in 0 until sizeX - 1) {
                    if (!canTraverse(x + dx, y, height, Direction.SOUTH)) return false
                }
                for (dy in 1 until sizeY) {
                    if (!canTraverse(x + sizeX - 1, y + dy, height, Direction.EAST)) return false
                }
            }
            Direction.SOUTH_WEST -> {
                if (!canTraverse(x, y, height, direction)) return false
                for (dx in 1 until sizeX) {
                    if (!canTraverse(x + dx, y, height, Direction.SOUTH)) return false
                }
                for (dy in 1 until sizeY) {
                    if (!canTraverse(x, y + dy, height, Direction.WEST)) return false
                }
            }
            Direction.NORTH_WEST -> {
                if (!canTraverse(x, y + sizeY - 1, height, direction)) return false
                for (dx in 1 until sizeX) {
                    if (!canTraverse(x + dx, y + sizeY - 1, height, Direction.NORTH)) return false
                }
                for (dy in 0 until sizeY - 1) {
                    if (!canTraverse(x, y + dy, height, Direction.WEST)) return false
                }
            }
            Direction.NONE -> return true
        }
        return true
    }

    /**
     * Check if a projectile (ranged/magic) can pass through a tile.
     */
    fun canShootThrough(x: Int, y: Int, height: Int, direction: Direction): Boolean {
        val destX = x + direction.dx
        val destY = y + direction.dy
        val flags = getFlags(destX, destY, height)
        return flags and CollisionFlag.PROJECTILE_FULL == 0
    }

    // ================================================================
    //  Object collision registration
    // ================================================================

    /**
     * Add collision flags for a wall object at the given position and orientation.
     */
    fun addWall(x: Int, y: Int, height: Int, type: Int, rotation: Int) {
        when (type) {
            0 -> { // Straight wall
                when (rotation) {
                    0 -> { addFlag(x, y, height, CollisionFlag.WALL_WEST); addFlag(x - 1, y, height, CollisionFlag.WALL_EAST) }
                    1 -> { addFlag(x, y, height, CollisionFlag.WALL_NORTH); addFlag(x, y + 1, height, CollisionFlag.WALL_SOUTH) }
                    2 -> { addFlag(x, y, height, CollisionFlag.WALL_EAST); addFlag(x + 1, y, height, CollisionFlag.WALL_WEST) }
                    3 -> { addFlag(x, y, height, CollisionFlag.WALL_SOUTH); addFlag(x, y - 1, height, CollisionFlag.WALL_NORTH) }
                }
            }
            1, 3 -> { // Diagonal wall / corner
                when (rotation) {
                    0 -> { addFlag(x, y, height, CollisionFlag.WALL_NORTH_WEST); addFlag(x - 1, y + 1, height, CollisionFlag.WALL_SOUTH_EAST) }
                    1 -> { addFlag(x, y, height, CollisionFlag.WALL_NORTH_EAST); addFlag(x + 1, y + 1, height, CollisionFlag.WALL_SOUTH_WEST) }
                    2 -> { addFlag(x, y, height, CollisionFlag.WALL_SOUTH_EAST); addFlag(x + 1, y - 1, height, CollisionFlag.WALL_NORTH_WEST) }
                    3 -> { addFlag(x, y, height, CollisionFlag.WALL_SOUTH_WEST); addFlag(x - 1, y - 1, height, CollisionFlag.WALL_NORTH_EAST) }
                }
            }
            2 -> { // Corner piece (two sides)
                when (rotation) {
                    0 -> {
                        addFlag(x, y, height, CollisionFlag.WALL_NORTH or CollisionFlag.WALL_WEST)
                        addFlag(x - 1, y, height, CollisionFlag.WALL_EAST)
                        addFlag(x, y + 1, height, CollisionFlag.WALL_SOUTH)
                    }
                    1 -> {
                        addFlag(x, y, height, CollisionFlag.WALL_NORTH or CollisionFlag.WALL_EAST)
                        addFlag(x + 1, y, height, CollisionFlag.WALL_WEST)
                        addFlag(x, y + 1, height, CollisionFlag.WALL_SOUTH)
                    }
                    2 -> {
                        addFlag(x, y, height, CollisionFlag.WALL_SOUTH or CollisionFlag.WALL_EAST)
                        addFlag(x + 1, y, height, CollisionFlag.WALL_WEST)
                        addFlag(x, y - 1, height, CollisionFlag.WALL_NORTH)
                    }
                    3 -> {
                        addFlag(x, y, height, CollisionFlag.WALL_SOUTH or CollisionFlag.WALL_WEST)
                        addFlag(x - 1, y, height, CollisionFlag.WALL_EAST)
                        addFlag(x, y - 1, height, CollisionFlag.WALL_NORTH)
                    }
                }
            }
        }
    }

    /**
     * Add collision for a solid game object (e.g. a rock, tree, table).
     * Marks all tiles in its footprint as blocked.
     */
    fun addObject(x: Int, y: Int, height: Int, sizeX: Int, sizeY: Int, solid: Boolean) {
        if (!solid) return
        for (dx in 0 until sizeX) {
            for (dy in 0 until sizeY) {
                addFlag(x + dx, y + dy, height, CollisionFlag.OBJECT_TILE)
            }
        }
    }

    /**
     * Remove collision for a solid game object.
     */
    fun removeObject(x: Int, y: Int, height: Int, sizeX: Int, sizeY: Int) {
        for (dx in 0 until sizeX) {
            for (dy in 0 until sizeY) {
                removeFlag(x + dx, y + dy, height, CollisionFlag.OBJECT_TILE)
            }
        }
    }

    /** Number of loaded regions. */
    fun regionCount(): Int = regions.size
}
