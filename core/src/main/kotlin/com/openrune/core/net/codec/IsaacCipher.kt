package com.openrune.core.net.codec

/**
 * ISAAC (Indirection, Shift, Accumulate, Add, and Count) stream cipher.
 * Used to encrypt packet opcodes in the RS2 protocol.
 *
 * This implementation matches the client's ISAACRandomGen exactly:
 *   - After initialization, results are read in REVERSE order (255 down to 0)
 *   - The block is generated exactly ONCE during init (not twice)
 *   - The count field uses post-decrement to track position
 *
 * ENGINE-LEVEL system. Not pluggable.
 */
class IsaacCipher(seed: IntArray = IntArray(0)) {

    private val results = IntArray(256)
    private val memory = IntArray(256)
    private var accumulator = 0
    private var lastResult = 0
    private var counter = 0

    /**
     * Countdown index into the results array.
     * Starts at 256 after init (client sets count=256 after isaac()).
     * getNextKey: post-decrements count, returns results[count].
     * When count hits 0, regenerates and resets to 255.
     */
    private var count = 0

    init {
        if (seed.isNotEmpty()) {
            seed.copyInto(results, endIndex = minOf(seed.size, 256))
        }
        initialize(seed.isNotEmpty())
    }

    /**
     * Get the next pseudo-random value from the cipher stream.
     *
     * Matches the client's getNextKey() exactly:
     *   if(count-- == 0) { isaac(); count = 255; }
     *   return results[count];
     */
    fun nextValue(): Int {
        val prev = count
        count--
        if (prev == 0) {
            generateBlock()
            count = 255
        }
        return results[count]
    }

    private fun generateBlock() {
        lastResult += ++counter

        for (i in 0 until 256) {
            val x = memory[i]

            accumulator = when (i and 3) {
                0 -> accumulator xor (accumulator shl 13)
                1 -> accumulator xor (accumulator ushr 6)
                2 -> accumulator xor (accumulator shl 2)
                3 -> accumulator xor (accumulator ushr 16)
                else -> accumulator
            }

            accumulator += memory[(i + 128) and 0xFF]

            val y = memory[(x ushr 2) and 0xFF] + accumulator + lastResult
            memory[i] = y
            lastResult = memory[(y ushr 10) and 0xFF] + x
            results[i] = lastResult
        }
    }

    private fun initialize(hasSeed: Boolean) {
        var a = 0x9e3779b9.toInt()  // Golden ratio
        var b = a; var c = a; var d = a
        var e = a; var f = a; var g = a; var h = a

        // Scramble
        repeat(4) {
            a = a xor (b shl 11); d += a; b += c
            b = b xor (c ushr 2); e += b; c += d
            c = c xor (d shl 8); f += c; d += e
            d = d xor (e ushr 16); g += d; e += f
            e = e xor (f shl 10); h += e; f += g
            f = f xor (g ushr 4); a += f; g += h
            g = g xor (h shl 8); b += g; h += a
            h = h xor (a ushr 9); c += h; a += b
        }

        for (i in 0 until 256 step 8) {
            if (hasSeed) {
                a += results[i]; b += results[i + 1]; c += results[i + 2]; d += results[i + 3]
                e += results[i + 4]; f += results[i + 5]; g += results[i + 6]; h += results[i + 7]
            }

            a = a xor (b shl 11); d += a; b += c
            b = b xor (c ushr 2); e += b; c += d
            c = c xor (d shl 8); f += c; d += e
            d = d xor (e ushr 16); g += d; e += f
            e = e xor (f shl 10); h += e; f += g
            f = f xor (g ushr 4); a += f; g += h
            g = g xor (h shl 8); b += g; h += a
            h = h xor (a ushr 9); c += h; a += b

            memory[i] = a; memory[i + 1] = b; memory[i + 2] = c; memory[i + 3] = d
            memory[i + 4] = e; memory[i + 5] = f; memory[i + 6] = g; memory[i + 7] = h
        }

        if (hasSeed) {
            for (i in 0 until 256 step 8) {
                a += memory[i]; b += memory[i + 1]; c += memory[i + 2]; d += memory[i + 3]
                e += memory[i + 4]; f += memory[i + 5]; g += memory[i + 6]; h += memory[i + 7]

                a = a xor (b shl 11); d += a; b += c
                b = b xor (c ushr 2); e += b; c += d
                c = c xor (d shl 8); f += c; d += e
                d = d xor (e ushr 16); g += d; e += f
                e = e xor (f shl 10); h += e; f += g
                f = f xor (g ushr 4); a += f; g += h
                g = g xor (h shl 8); b += g; h += a
                h = h xor (a ushr 9); c += h; a += b

                memory[i] = a; memory[i + 1] = b; memory[i + 2] = c; memory[i + 3] = d
                memory[i + 4] = e; memory[i + 5] = f; memory[i + 6] = g; memory[i + 7] = h
            }
        }

        // Generate block once, then set count = 256 (matches client's init)
        generateBlock()
        count = 256
    }
}
