package com.openrune.cache.io

/**
 * XTEA block cipher implementation for RS2 cache decryption.
 *
 * XTEA operates on 8-byte (64-bit) blocks using a 128-bit key
 * (4 x 32-bit integers). The RS2 cache uses 32 rounds (standard).
 *
 * In the dat2 cache format, XTEA is used to encrypt map region data.
 * The keys are per-region and stored separately (e.g., keys.json from
 * OpenRS2). Decryption is applied to the container data BEFORE
 * decompression.
 */
object Xtea {

    private const val ROUNDS = 32
    private const val GOLDEN_RATIO = -0x61C88647 // 0x9E3779B9 as signed int
    private const val BLOCK_SIZE = 8

    /**
     * Decrypt data in-place using XTEA.
     *
     * Only full 8-byte blocks are decrypted. Trailing bytes (< 8) are
     * left unchanged, which matches the RS2 client behavior.
     *
     * @param data   The byte array to decrypt in-place.
     * @param offset Start offset in the array.
     * @param length Number of bytes to process.
     * @param key    The 4-int XTEA key.
     */
    fun decrypt(data: ByteArray, offset: Int, length: Int, key: IntArray) {
        require(key.size == 4) { "XTEA key must be 4 ints" }

        val numBlocks = length / BLOCK_SIZE
        var pos = offset

        for (block in 0 until numBlocks) {
            // Read two 32-bit big-endian words
            var v0 = ((data[pos].toInt() and 0xFF) shl 24) or
                     ((data[pos + 1].toInt() and 0xFF) shl 16) or
                     ((data[pos + 2].toInt() and 0xFF) shl 8) or
                     (data[pos + 3].toInt() and 0xFF)

            var v1 = ((data[pos + 4].toInt() and 0xFF) shl 24) or
                     ((data[pos + 5].toInt() and 0xFF) shl 16) or
                     ((data[pos + 6].toInt() and 0xFF) shl 8) or
                     (data[pos + 7].toInt() and 0xFF)

            // Compute the decryption starting sum
            var sum = GOLDEN_RATIO * ROUNDS

            for (round in 0 until ROUNDS) {
                v1 -= (((v0 shl 4) xor (v0 ushr 5)) + v0) xor (sum + key[(sum ushr 11) and 3])
                sum -= GOLDEN_RATIO
                v0 -= (((v1 shl 4) xor (v1 ushr 5)) + v1) xor (sum + key[sum and 3])
            }

            // Write back big-endian
            data[pos]     = (v0 ushr 24).toByte()
            data[pos + 1] = (v0 ushr 16).toByte()
            data[pos + 2] = (v0 ushr 8).toByte()
            data[pos + 3] = v0.toByte()
            data[pos + 4] = (v1 ushr 24).toByte()
            data[pos + 5] = (v1 ushr 16).toByte()
            data[pos + 6] = (v1 ushr 8).toByte()
            data[pos + 7] = v1.toByte()

            pos += BLOCK_SIZE
        }
    }
}
