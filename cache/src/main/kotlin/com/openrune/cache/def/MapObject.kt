package com.openrune.cache.def

/**
 * A single object placement decoded from a 317 cache map file.
 *
 * Map files use the "l{regionX}_{regionY}" naming convention inside cache
 * index 4. Each placement records which object definition to use, its
 * absolute world position, the object type (wall, decoration, etc.), and
 * its rotation.
 *
 * @property id       Object definition ID (references [CacheObjectDefinition]).
 * @property x        Absolute world X coordinate.
 * @property y        Absolute world Y coordinate.
 * @property z        Height level (0-3).
 * @property type     Object type (0-22). See type ranges below.
 * @property rotation Facing direction: 0=North, 1=East, 2=South, 3=West.
 *
 * Type ranges:
 *   0-3   Walls
 *   4     Wall decoration
 *   5-8   Corner walls
 *   9     Diagonal walls
 *   10-11 Interactive objects
 *   12-21 Ground decoration
 *   22    Floor decoration
 */
data class MapObject(
    val id: Int,
    val x: Int,
    val y: Int,
    val z: Int,
    val type: Int,
    val rotation: Int
)
