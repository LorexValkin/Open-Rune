package com.openrune.core.net.codec

/**
 * Represents a decoded incoming packet from the client.
 */
data class IncomingPacket(
    val opcode: Int,
    val size: Int,
    val payload: ByteArray
) {
    fun readByte(offset: Int): Int = payload[offset].toInt() and 0xFF
    fun readShort(offset: Int): Int = (readByte(offset) shl 8) or readByte(offset + 1)
    fun readInt(offset: Int): Int = (readShort(offset) shl 16) or readShort(offset + 2)
    fun readLong(offset: Int): Long = (readInt(offset).toLong() shl 32) or (readInt(offset + 4).toLong() and 0xFFFFFFFFL)

    fun readSignedByte(offset: Int): Int = payload[offset].toInt()
    fun readLEShort(offset: Int): Int = readByte(offset) or (readByte(offset + 1) shl 8)
    fun readLEShortA(offset: Int): Int = (readByte(offset) - 128 and 0xFF) or (readByte(offset + 1) shl 8)
    fun readShortA(offset: Int): Int = (readByte(offset) shl 8) or (readByte(offset + 1) - 128 and 0xFF)
    fun readByteA(offset: Int): Int = (readByte(offset) - 128) and 0xFF
    fun readByteC(offset: Int): Int = (-readByte(offset)) and 0xFF
    fun readByteS(offset: Int): Int = (128 - readByte(offset)) and 0xFF

    fun readString(offset: Int): String {
        val sb = StringBuilder()
        var i = offset
        while (i < payload.size) {
            val b = payload[i].toInt() and 0xFF
            if (b == 10) break  // newline terminator
            sb.append(b.toChar())
            i++
        }
        return sb.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IncomingPacket) return false
        return opcode == other.opcode && payload.contentEquals(other.payload)
    }

    override fun hashCode(): Int = 31 * opcode + payload.contentHashCode()
}

/**
 * Builder for outgoing server->client packets.
 */
class PacketBuilder(private val opcode: Int = -1) {

    private var buffer = ByteArray(256)
    private var position = 0
    private var sizeIndex = -1

    /** Bare packet (no header). Used for login response. */
    var bare: Boolean = false

    private fun ensureCapacity(needed: Int) {
        if (position + needed > buffer.size) {
            buffer = buffer.copyOf(maxOf(buffer.size * 2, position + needed))
        }
    }

    fun addByte(value: Int): PacketBuilder {
        ensureCapacity(1)
        buffer[position++] = value.toByte()
        return this
    }

    fun addShort(value: Int): PacketBuilder {
        ensureCapacity(2)
        buffer[position++] = (value shr 8).toByte()
        buffer[position++] = value.toByte()
        return this
    }

    fun addLEShort(value: Int): PacketBuilder {
        ensureCapacity(2)
        buffer[position++] = value.toByte()
        buffer[position++] = (value shr 8).toByte()
        return this
    }

    fun addShortA(value: Int): PacketBuilder {
        ensureCapacity(2)
        buffer[position++] = (value shr 8).toByte()
        buffer[position++] = (value + 128).toByte()
        return this
    }

    fun addLEShortA(value: Int): PacketBuilder {
        ensureCapacity(2)
        buffer[position++] = (value + 128).toByte()
        buffer[position++] = (value shr 8).toByte()
        return this
    }

    fun addInt(value: Int): PacketBuilder {
        ensureCapacity(4)
        buffer[position++] = (value shr 24).toByte()
        buffer[position++] = (value shr 16).toByte()
        buffer[position++] = (value shr 8).toByte()
        buffer[position++] = value.toByte()
        return this
    }

    /** Middle-endian int (type 1): wire order [8, 0, 24, 16]. Client reads with method439(). */
    fun addIntME1(value: Int): PacketBuilder {
        ensureCapacity(4)
        buffer[position++] = (value shr 8).toByte()
        buffer[position++] = value.toByte()
        buffer[position++] = (value shr 24).toByte()
        buffer[position++] = (value shr 16).toByte()
        return this
    }

    fun addLong(value: Long): PacketBuilder {
        addInt((value shr 32).toInt())
        addInt(value.toInt())
        return this
    }

    fun addByteA(value: Int): PacketBuilder {
        addByte(value + 128)
        return this
    }

    fun addByteC(value: Int): PacketBuilder {
        addByte(-value)
        return this
    }

    fun addByteS(value: Int): PacketBuilder {
        addByte(128 - value)
        return this
    }

    fun addBytes(data: ByteArray): PacketBuilder {
        ensureCapacity(data.size)
        data.copyInto(buffer, position)
        position += data.size
        return this
    }

    fun addString(str: String): PacketBuilder {
        for (ch in str) {
            addByte(ch.code)
        }
        addByte(10)  // newline terminator
        return this
    }

    fun addBits(numBits: Int, value: Int): PacketBuilder {
        // Bit writing is handled separately in the player update protocol
        // This is a placeholder; actual bit access needs a BitWriter wrapper
        return this
    }

    /** Start a variable-length packet (size byte written at end). */
    fun startVariableSize(): PacketBuilder {
        sizeIndex = position
        addByte(0) // placeholder
        return this
    }

    /** Start a variable-length packet with short size. */
    fun startVariableShortSize(): PacketBuilder {
        sizeIndex = position
        addShort(0) // placeholder
        return this
    }

    /** End a variable-length packet, writing the actual size. */
    fun endVariableSize(): PacketBuilder {
        val size = position - sizeIndex - 1
        buffer[sizeIndex] = size.toByte()
        sizeIndex = -1
        return this
    }

    fun endVariableShortSize(): PacketBuilder {
        val size = position - sizeIndex - 2
        buffer[sizeIndex] = (size shr 8).toByte()
        buffer[sizeIndex + 1] = size.toByte()
        sizeIndex = -1
        return this
    }

    /** Build the final byte array. */
    fun build(): ByteArray = buffer.copyOf(position)

    /** Get current write position. */
    fun size(): Int = position

    /** Get the opcode. */
    fun opcode(): Int = opcode
}

/**
 * Packet size constants for the 317 protocol.
 * -1 = variable byte size, -2 = variable short size.
 */
object PacketSizes {

    /**
     * Client -> Server packet sizes.
     * Index = opcode, value = payload size.
     */
    /**
     * Corrected to match standard 317 protocol (verified against PI reference).
     * Key fixes: [131]=4 (magic on NPC), [152]=1 (anticheat),
     * [155]=2 (first NPC click -- was 6, causing stream corruption).
     */
    val INCOMING = intArrayOf(
        0, 0, 0, 1, -1, 0, 0, 0, 0, 0,         // 0-9
        0, 0, 0, 0, 8, 0, 6, 2, 2, 0,           // 10-19
        0, 2, 0, 6, 0, 12, 0, 0, 0, 0,          // 20-29
        0, 0, 0, 0, 0, 8, 4, 0, 0, 2,           // 30-39
        2, 6, 0, 6, 0, -1, 0, 0, 0, 0,          // 40-49
        0, 0, 0, 12, 0, 0, 0, 8, 8, 12,         // 50-59
        8, 8, 0, 0, 0, 0, 0, 0, 0, 0,           // 60-69
        6, 0, 2, 2, 8, 6, 0, -1, 0, 6,          // 70-79
        0, 0, 0, 0, 0, 1, 4, 6, 0, 0,           // 80-89
        0, 0, 0, 0, 0, 3, 0, 0, -1, 0,          // 90-99
        0, 13, 0, -1, 0, 0, 0, 0, 0, 0,         // 100-109
        0, 0, 0, 0, 0, 0, 0, 6, 0, 0,           // 110-119
        1, 0, 6, 0, 0, 0, -1, 0, 2, 6,          // 120-129
        0, 4, 6, 8, 0, 6, 0, 0, 0, 2,           // 130-139
        0, 0, 0, 0, 0, 6, 0, 0, 0, 0,           // 140-149
        0, 0, 1, 2, 0, 2, 6, 0, 0, 0,           // 150-159
        0, 0, 0, 0, -1, -1, 0, 0, 0, 0,         // 160-169
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,           // 170-179
        0, 8, 0, 3, 0, 2, 0, 0, 8, 1,           // 180-189
        0, 0, 12, 0, 0, 0, 0, 0, 0, 0,          // 190-199
        2, 0, 0, 0, 0, 0, 0, 0, 4, 0,           // 200-209
        4, 0, 0, 0, 7, 8, 0, 0, 10, 0,          // 210-219
        0, 0, 0, 0, 0, 0, -1, 0, 6, 0,          // 220-229
        1, 0, 0, 0, 6, 0, 6, 8, 1, 0,           // 230-239
        0, 4, 0, 0, 0, 0, -1, 0, -1, 4,         // 240-249
        0, 0, 6, 6, 0, 0, 0                      // 250-255
    )
}
