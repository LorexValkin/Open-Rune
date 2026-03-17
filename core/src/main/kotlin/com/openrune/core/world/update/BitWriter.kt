package com.openrune.core.world.update

/**
 * Writes individual bits into a byte buffer.
 * Used by the player update protocol which mixes bit-level and byte-level data.
 *
 * Usage:
 *   val writer = BitWriter(buffer)
 *   writer.startBitAccess()
 *   writer.writeBits(1, 1)  // 1 bit: update required = true
 *   writer.writeBits(2, 3)  // 2 bits: movement type = run
 *   writer.finishBitAccess()
 *   // Now back to byte access on the buffer
 */
class BitWriter(initialCapacity: Int = 4096) {

    var buffer = ByteArray(initialCapacity)
        private set

    var bytePosition = 0
        private set

    private var bitPosition = 0

    /**
     * Switch from byte access to bit access.
     * Must be called before any writeBits() calls.
     */
    fun startBitAccess() {
        bitPosition = bytePosition * 8
    }

    /**
     * Write [numBits] of [value] into the buffer.
     */
    fun writeBits(numBits: Int, value: Int) {
        var remaining = numBits

        // Ensure capacity
        val needed = (bitPosition + remaining + 7) / 8
        if (needed >= buffer.size) {
            buffer = buffer.copyOf(maxOf(buffer.size * 2, needed + 256))
        }

        var byteIdx = bitPosition shr 3
        var bitIdx = 8 - (bitPosition and 7)

        bitPosition += remaining

        while (remaining > bitIdx) {
            // Fill the rest of the current byte
            buffer[byteIdx] = (buffer[byteIdx].toInt() and BIT_MASK_OUT[bitIdx].inv()
                or ((value shr (remaining - bitIdx)) and BIT_MASK_IN[bitIdx])).toByte()
            remaining -= bitIdx
            byteIdx++
            bitIdx = 8
        }

        if (remaining == bitIdx) {
            buffer[byteIdx] = (buffer[byteIdx].toInt() and BIT_MASK_OUT[bitIdx].inv()
                or (value and BIT_MASK_IN[bitIdx])).toByte()
        } else {
            buffer[byteIdx] = (buffer[byteIdx].toInt() and (BIT_MASK_OUT[remaining] shl (bitIdx - remaining)).inv()
                or ((value and BIT_MASK_IN[remaining]) shl (bitIdx - remaining))).toByte()
        }
    }

    /**
     * Switch from bit access back to byte access.
     * Aligns to the next byte boundary.
     */
    fun finishBitAccess() {
        bytePosition = (bitPosition + 7) / 8
    }

    /**
     * Write a single byte (byte access mode only).
     */
    fun writeByte(value: Int) {
        ensureCapacity(1)
        buffer[bytePosition++] = value.toByte()
    }

    /**
     * Write a short (2 bytes, big-endian).
     */
    fun writeShort(value: Int) {
        ensureCapacity(2)
        buffer[bytePosition++] = (value shr 8).toByte()
        buffer[bytePosition++] = value.toByte()
    }

    /**
     * Write raw bytes.
     */
    fun writeBytes(data: ByteArray) {
        ensureCapacity(data.size)
        data.copyInto(buffer, bytePosition)
        bytePosition += data.size
    }

    /**
     * Write byte with negation (byte C modifier).
     */
    fun writeByteC(value: Int) {
        writeByte(-value)
    }

    /**
     * Write byte with add 128 (byte A modifier).
     * Client reads with: method426() = (byte - 128) & 0xFF
     */
    fun writeByteA(value: Int) {
        writeByte(value + 128)
    }

    /**
     * Write byte with subtract from 128 (byte S modifier).
     * Client reads with: method428() = (128 - byte) & 0xFF
     */
    fun writeByteS(value: Int) {
        writeByte(128 - value)
    }

    /**
     * Write a 4-byte big-endian int.
     */
    fun writeInt(value: Int) {
        ensureCapacity(4)
        buffer[bytePosition++] = (value shr 24).toByte()
        buffer[bytePosition++] = (value shr 16).toByte()
        buffer[bytePosition++] = (value shr 8).toByte()
        buffer[bytePosition++] = value.toByte()
    }

    /**
     * Write short with little-endian byte order.
     */
    fun writeLEShort(value: Int) {
        ensureCapacity(2)
        buffer[bytePosition++] = value.toByte()
        buffer[bytePosition++] = (value shr 8).toByte()
    }

    /**
     * Write little-endian short with byte A modifier on the low byte.
     * Low byte = (value + 128), high byte = (value >> 8).
     * Client reads with: method436() = ((high << 8) + ((low - 128) & 0xFF))
     */
    fun writeLEShortA(value: Int) {
        ensureCapacity(2)
        buffer[bytePosition++] = (value + 128).toByte()
        buffer[bytePosition++] = (value shr 8).toByte()
    }

    /**
     * Get the built bytes up to the current position.
     */
    fun toByteArray(): ByteArray = buffer.copyOf(bytePosition)

    private fun ensureCapacity(needed: Int) {
        if (bytePosition + needed >= buffer.size) {
            buffer = buffer.copyOf(maxOf(buffer.size * 2, bytePosition + needed + 256))
        }
    }

    companion object {
        private val BIT_MASK_IN = IntArray(32) { (1 shl it) - 1 }
        private val BIT_MASK_OUT = IntArray(32) { ((1 shl it) - 1) }
    }
}
