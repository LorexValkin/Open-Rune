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
 */
object DefaultAppearance {
    val MALE   = intArrayOf(0, 10, 18, 26, 33, 36, 42)
    val FEMALE = intArrayOf(45, -1, 56, 61, 67, 70, 79)
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
 * Builds the appearance update block for the Project51/Anguish client.
 *
 * This variant includes extra fields not present in the stock 317 protocol:
 *   - Title string + title color string (after gender)
 *   - Health status mask byte
 *   - Invisible flag (after name)
 *   - Player rights byte (after combat level)
 *
 * ENGINE-LEVEL system. The block format is protocol-defined and not pluggable.
 */
object AppearanceBuilder {

    /**
     * Build the full appearance properties block for a player.
     * Returns the raw bytes (without the leading size byte, that's added by the updater).
     */
    fun build(player: Player): ByteArray {
        val out = ByteArrayOutputStream(128)

        // === Gender (0 = male, 1 = female) ===
        out.write(player.gender)

        // === Title string (Project51 extension) ===
        // Empty string for now — just write the terminator
        writeString(out, "")

        // === Title color string (Project51 extension) ===
        // Empty string for now — just write the terminator
        writeString(out, "")

        // === Health status mask (Project51 extension) ===
        // 0 = normal
        out.write(0)

        // === Overhead icon: prayer (-1 = none, 0+ = prayer icon) ===
        out.write(player.prayerIcon)

        // === Overhead icon: skull (-1 = none, 0 = skull, 1 = red skull) ===
        out.write(player.skullIcon)

        // === NPC transform check ===
        // If player is transformed to an NPC, send -1 + npcId instead of equipment
        val npcTransformId = -1  // No NPC transform support yet
        if (npcTransformId >= 0) {
            writeShort(out, 65535) // -1 as unsigned short
            writeShort(out, npcTransformId)
        } else {
            // === Equipment / Body model slots (12 slots: 0-11) ===
            for (slot in 0 until 12) {
                val equipId = player.equipment.getOrElse(mapSlot(slot)) { -1 }

                if (equipId > 0) {
                    writeShort(out, 0x200 + equipId)
                } else {
                    val bodyPart = slotToBodyPart(slot, player.gender)
                    if (bodyPart >= 0) {
                        val idkId = player.appearance.getOrElse(bodyPart) {
                            if (player.gender == 0) DefaultAppearance.MALE[bodyPart]
                            else DefaultAppearance.FEMALE[bodyPart]
                        }

                        if (idkId >= 0) {
                            writeShort(out, 0x100 + idkId)
                        } else {
                            out.write(0)
                        }
                    } else {
                        out.write(0)
                    }
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

        // === Invisible flag (Project51 extension) ===
        out.write(0) // 0 = visible, 1 = invisible

        // === Combat level ===
        out.write(player.getCombatLevel())

        // === Player rights (Project51 extension) ===
        // 0 = regular, 1 = mod, 2 = admin, etc.
        out.write(player.rights.value)

        // === Total level (for skill total worlds, usually 0) ===
        writeShort(out, 0)

        return out.toByteArray()
    }

    /**
     * Map the 12 appearance update slots to equipment slot indices.
     */
    private fun mapSlot(updateSlot: Int): Int = when (updateSlot) {
        0  -> EquipmentSlot.HEAD
        1  -> EquipmentSlot.CAPE
        2  -> EquipmentSlot.AMULET
        3  -> EquipmentSlot.WEAPON
        4  -> EquipmentSlot.CHEST
        5  -> EquipmentSlot.SHIELD
        6  -> -1  // Arms (follow chest)
        7  -> EquipmentSlot.LEGS
        8  -> -1  // Head model
        9  -> EquipmentSlot.HANDS
        10 -> EquipmentSlot.FEET
        11 -> -1  // Beard
        else -> -1
    }

    /**
     * Map the 12 update protocol slots to body part indices.
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

    /**
     * Write a null-terminated string (terminated with 10 / 0x0A).
     */
    private fun writeString(out: ByteArrayOutputStream, s: String) {
        for (c in s) {
            out.write(c.code)
        }
        out.write(10) // 0x0A terminator
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