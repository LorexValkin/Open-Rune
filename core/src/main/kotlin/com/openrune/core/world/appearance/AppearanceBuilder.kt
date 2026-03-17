package com.openrune.core.world.appearance

import com.openrune.core.world.Player
import java.io.ByteArrayOutputStream

/**
 * Equipment slot constants.
 */
object EquipmentSlot {
    const val HEAD       = 0
    const val CAPE       = 1
    const val AMULET     = 2
    const val WEAPON     = 3
    const val CHEST      = 4
    const val SHIELD     = 5
    const val LEGS       = 7
    const val HANDS      = 9
    const val FEET       = 10
    const val RING       = 12
    const val ARROWS     = 13

    /** Total equipment slots. */
    const val SIZE = 14
}

/**
 * Body part indices for the default player model.
 * These map to the IDK (Identity Kit) definitions in the cache.
 *
 * Plugins can extend the available IDK set (e.g. adding new hairstyles)
 * by registering additional entries in the data store. The appearance
 * system reads from there when building the block.
 */
object BodyPart {
    const val HEAD  = 0
    const val BEARD = 1
    const val CHEST = 2
    const val ARMS  = 3
    const val HANDS = 4
    const val LEGS  = 5
    const val FEET  = 6

    const val COUNT = 7
}

/**
 * Default IDK (Identity Kit) values for male and female characters.
 * Index = BodyPart constant. Value = cache IDK ID.
 */
object DefaultAppearance {
    val MALE   = intArrayOf(0, 10, 18, 26, 33, 36, 42)
    val FEMALE = intArrayOf(45, -1, 56, 61, 67, 70, 79) // -1 = no beard for female
}

/**
 * Color indices for player model coloring.
 */
object BodyColor {
    const val HAIR  = 0
    const val TORSO = 1
    const val LEGS  = 2
    const val FEET  = 3
    const val SKIN  = 4
    const val COUNT = 5
}

/**
 * Default standing/walking animation set.
 * These are the idle/movement animation IDs used for the appearance block.
 */
object DefaultAnimations {
    const val STAND       = 0x328  // 808
    const val STAND_TURN  = 0x337  // 823
    const val WALK        = 0x333  // 819
    const val TURN_180    = 0x334  // 820
    const val TURN_CW     = 0x335  // 821
    const val TURN_CCW    = 0x336  // 822
    const val RUN         = 0x338  // 824
}

/**
 * Builds the appearance update block for the 317 player update protocol.
 *
 * The appearance block tells other clients how to render this player:
 *   - Gender
 *   - Head icon (skull, prayer)
 *   - Equipment or body model for each slot
 *   - Body colors
 *   - Stand/walk/run animation IDs
 *   - Player name (encoded as long)
 *   - Combat level
 *   - Total level
 *
 * ENGINE-LEVEL system. The block format is protocol-defined and not pluggable.
 * Plugins can modify appearance state (equipment, body parts, colors) through
 * the player API, and those changes are reflected in the next update block.
 */
object AppearanceBuilder {

    /**
     * Build the full appearance properties block for a player.
     * Returns the raw bytes (without the leading size byte, that's added by the updater).
     */
    fun build(player: Player): ByteArray {
        val out = ByteArrayOutputStream(128)

        // Gender (0 = male, 1 = female)
        out.write(player.gender)

        // Overhead icon: prayer (-1 = none, 0+ = prayer icon)
        // Client reads this as "headIcon" (byte 2 of the appearance block)
        out.write(player.prayerIcon)

        // Overhead icon: skull (-1 = none, 0 = skull, 1 = red skull)
        // Client reads this as "skullIcon" (byte 3 of the appearance block)
        out.write(player.skullIcon)

        // === Equipment / Body model slots (12 slots: 0-11) ===
        // For each slot, we either send:
        //   0x0000            = nothing visible (1 byte: 0)
        //   0x0200 + equipId  = show equipment model (2 bytes: short)
        //   0x0100 + idkId    = show body part model (2 bytes: short)

        for (slot in 0 until 12) {
            val equipId = player.equipment.getOrElse(mapSlot(slot)) { -1 }

            if (equipId > 0) {
                // Show equipment model
                writeShort(out, 0x200 + equipId)
            } else {
                // Show body part (if this slot has one)
                val bodyPart = slotToBodyPart(slot, player.gender)
                if (bodyPart >= 0) {
                    val idkId = player.appearance.getOrElse(bodyPart) {
                        if (player.gender == 0) DefaultAppearance.MALE[bodyPart]
                        else DefaultAppearance.FEMALE[bodyPart]
                    }

                    if (idkId >= 0) {
                        writeShort(out, 0x100 + idkId)
                    } else {
                        out.write(0) // No model for this slot
                    }
                } else {
                    out.write(0) // No body part mapped to this slot
                }
            }
        }

        // === Body colors (5 colors) ===
        for (i in 0 until BodyColor.COUNT) {
            out.write(player.colors.getOrElse(i) { 0 })
        }

        // === Animation set (7 animation IDs) ===
        writeShort(out, player.standAnim.takeIf { it >= 0 } ?: DefaultAnimations.STAND)
        writeShort(out, player.standTurnAnim.takeIf { it >= 0 } ?: DefaultAnimations.STAND_TURN)
        writeShort(out, player.walkAnim.takeIf { it >= 0 } ?: DefaultAnimations.WALK)
        writeShort(out, player.turn180Anim.takeIf { it >= 0 } ?: DefaultAnimations.TURN_180)
        writeShort(out, player.turnCwAnim.takeIf { it >= 0 } ?: DefaultAnimations.TURN_CW)
        writeShort(out, player.turnCcwAnim.takeIf { it >= 0 } ?: DefaultAnimations.TURN_CCW)
        writeShort(out, player.runAnim.takeIf { it >= 0 } ?: DefaultAnimations.RUN)

        // === Name (encoded as long) ===
        writeLong(out, nameToLong(player.name))

        // === Combat level ===
        out.write(player.getCombatLevel())

        // === Total level (for skill total worlds, usually 0) ===
        writeShort(out, 0)

        return out.toByteArray()
    }

    /**
     * Map the 12 appearance update slots to equipment slot indices.
     * The update protocol uses slots 0-11, equipment uses different indices.
     */
    private fun mapSlot(updateSlot: Int): Int = when (updateSlot) {
        0  -> EquipmentSlot.HEAD
        1  -> EquipmentSlot.CAPE
        2  -> EquipmentSlot.AMULET
        3  -> EquipmentSlot.WEAPON
        4  -> EquipmentSlot.CHEST
        5  -> EquipmentSlot.SHIELD
        6  -> -1  // Not an equipment slot (arms follow chest)
        7  -> EquipmentSlot.LEGS
        8  -> -1  // Not an equipment slot (head model)
        9  -> EquipmentSlot.HANDS
        10 -> EquipmentSlot.FEET
        11 -> -1  // Not an equipment slot (beard)
        else -> -1
    }

    /**
     * Map the 12 update protocol slots to body part indices.
     * Returns -1 if the slot doesn't have a body part model.
     */
    private fun slotToBodyPart(updateSlot: Int, gender: Int): Int = when (updateSlot) {
        0  -> -1   // Head slot (hat hides head)
        1  -> -1   // Cape slot
        2  -> -1   // Amulet slot
        3  -> -1   // Weapon slot
        4  -> BodyPart.CHEST
        5  -> -1   // Shield slot
        6  -> BodyPart.ARMS
        7  -> BodyPart.LEGS
        8  -> BodyPart.HEAD
        9  -> BodyPart.HANDS
        10 -> BodyPart.FEET
        11 -> if (gender == 0) BodyPart.BEARD else -1
        else -> -1
    }

    private fun writeShort(out: ByteArrayOutputStream, value: Int) {
        out.write(value shr 8)
        out.write(value)
    }

    private fun writeLong(out: ByteArrayOutputStream, value: Long) {
        writeShort(out, (value shr 48).toInt())
        writeShort(out, (value shr 32).toInt())
        writeShort(out, (value shr 16).toInt())
        writeShort(out, value.toInt())
    }

    /**
     * Encode a player name to a long (317 protocol format).
     */
    fun nameToLong(name: String): Long {
        var encoded = 0L
        for (c in name.take(12)) {
            encoded *= 37L
            encoded += when {
                c in 'A'..'Z' -> (c - 'A' + 1).toLong()
                c in 'a'..'z' -> (c - 'a' + 1).toLong()
                c in '0'..'9' -> (c - '0' + 27).toLong()
                else -> 0L
            }
        }
        while (encoded % 37L == 0L && encoded != 0L) encoded /= 37L
        return encoded
    }
}
