package com.openrune.api.world

/**
 * Immutable 3D world position.
 */
data class Position(
    val x: Int,
    val y: Int,
    val z: Int = 0
) {

    /** The 8x8 region this position falls in. */
    val regionId: Int get() = (x shr 3) / 8 * 256 + (y shr 3) / 8

    /** The chunk (8x8 tile block) X coordinate. */
    val chunkX: Int get() = x shr 3

    /** The chunk Y coordinate. */
    val chunkY: Int get() = y shr 3

    /** The region X (64x64 tile block). */
    val regionX: Int get() = x shr 6

    /** The region Y. */
    val regionY: Int get() = y shr 6

    /** Local X within the region (0-63). */
    val localX: Int get() = x - (regionX shl 6)

    /** Local Y within the region (0-63). */
    val localY: Int get() = y - (regionY shl 6)

    /** Chebyshev distance to another position (ignores height). */
    fun distanceTo(other: Position): Int =
        maxOf(kotlin.math.abs(x - other.x), kotlin.math.abs(y - other.y))

    /** Whether another position is within [range] tiles (Chebyshev). */
    fun isWithinDistance(other: Position, range: Int = 15): Boolean =
        z == other.z && distanceTo(other) <= range

    /** Create a new position offset from this one. */
    fun translate(dx: Int, dy: Int, dz: Int = 0): Position =
        Position(x + dx, y + dy, z + dz)

    override fun toString(): String = "($x, $y, $z)"
}
