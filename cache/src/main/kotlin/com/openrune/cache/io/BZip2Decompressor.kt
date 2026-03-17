package com.openrune.cache.io

/**
 * RS2 headerless BZip2 decompressor.
 *
 * Clean Kotlin port of the client's working BZip2 implementation
 * (Class13.java / Class32.java). The RS2 cache uses standard BZip2 but
 * strips the "BZh" header - data starts directly at the block magic bytes.
 *
 * Architecture note: Self-contained, zero-dependency decompressor
 * designed for the OpenRune tool suite. Handles all 317 cache data
 * including jag archives (index 0) and map/landscape files (index 4).
 */
object BZip2Decompressor {

    fun decompress(output: ByteArray, outputLen: Int, input: ByteArray, inputLen: Int, inputOffset: Int = 0): Int {
        val s = State()
        s.inputBuf = input
        s.inputOffset = inputOffset
        s.outputBuf = output
        s.outputOffset = 0
        s.inputLen = inputLen
        s.outputLen = outputLen
        s.bitBuffer = 0
        s.bitsBuffered = 0
        s.bytesRead = 0
        s.bytesReadHigh = 0
        s.bytesWritten = 0
        s.bytesWrittenHigh = 0
        s.blockCount = 0
        decodeBlocks(s)
        return outputLen - s.outputLen
    }

    private fun decodeBlocks(s: State) {
        var minLen = 0
        var limitArr: IntArray? = null
        var baseArr: IntArray? = null
        var permArr: IntArray? = null

        s.blockSize = 1
        if (s.tt == null) {
            s.tt = IntArray(s.blockSize * 100_000)
        }

        var moreBlocks = true
        while (moreBlocks) {
            val magic = readByte(s)
            if (magic.toInt() == 23) return

            readByte(s); readByte(s); readByte(s); readByte(s); readByte(s)
            s.blockCount++
            readByte(s); readByte(s); readByte(s)

            val randomBit = readBit(s)
            if (randomBit.toInt() != 0) {
                println("WARNING: Randomized BZip2 block encountered (unsupported)")
            }

            s.origPointer = 0
            s.origPointer = (s.origPointer shl 8) or (readByte(s).toInt() and 0xFF)
            s.origPointer = (s.origPointer shl 8) or (readByte(s).toInt() and 0xFF)
            s.origPointer = (s.origPointer shl 8) or (readByte(s).toInt() and 0xFF)

            for (i in 0 until 16) {
                s.groupUsed[i] = readBit(s).toInt() == 1
            }
            for (i in 0 until 256) s.symbolUsed[i] = false
            for (g in 0 until 16) {
                if (s.groupUsed[g]) {
                    for (b in 0 until 16) {
                        if (readBit(s).toInt() == 1) {
                            s.symbolUsed[g * 16 + b] = true
                        }
                    }
                }
            }

            buildSymbolMap(s)
            val alphaSize = s.symbolCount + 2

            val nGroups = readBits(3, s)
            val nSelectors = readBits(15, s)

            for (i in 0 until nSelectors) {
                var j = 0
                while (true) {
                    val v = readBit(s)
                    if (v.toInt() == 0) break
                    j++
                }
                s.selectorMtf[i] = j.toByte()
            }

            val pos = ByteArray(6)
            for (i in 0 until nGroups) pos[i] = i.toByte()
            for (i in 0 until nSelectors) {
                var v = s.selectorMtf[i].toInt() and 0xFF
                val tmp = pos[v]
                while (v > 0) {
                    pos[v] = pos[v - 1]
                    v--
                }
                pos[0] = tmp
                s.selectorList[i] = tmp
            }

            for (g in 0 until nGroups) {
                var len = readBits(5, s)
                for (sym in 0 until alphaSize) {
                    while (true) {
                        val b = readBit(s)
                        if (b.toInt() == 0) break
                        val b2 = readBit(s)
                        if (b2.toInt() == 0) len++ else len--
                    }
                    s.codeLen[g][sym] = len.toByte()
                }
            }

            for (g in 0 until nGroups) {
                var mn: Byte = 32
                var mx = 0
                for (sym in 0 until alphaSize) {
                    if (s.codeLen[g][sym] > mx) mx = s.codeLen[g][sym].toInt()
                    if (s.codeLen[g][sym] < mn) mn = s.codeLen[g][sym]
                }
                buildHuffmanTable(s.limit[g], s.base[g], s.perm[g], s.codeLen[g], mn.toInt(), mx, alphaSize)
                s.minLen[g] = mn.toInt()
            }

            val eob = s.symbolCount + 1
            val tt = s.tt!!
            var nBlock = 0
            var groupNo = -1
            var groupPos = 0

            for (i in 0..255) s.charCount[i] = 0

            var mtfIdx = 4095
            for (g in 15 downTo 0) {
                for (b in 15 downTo 0) {
                    s.mtfSymbol[mtfIdx] = (g * 16 + b).toByte()
                    mtfIdx--
                }
                s.mtfBase[g] = mtfIdx + 1
            }

            if (groupPos == 0) {
                groupNo++; groupPos = 50
                val gt = s.selectorList[groupNo].toInt() and 0xFF
                minLen = s.minLen[gt]; limitArr = s.limit[gt]
                permArr = s.perm[gt]; baseArr = s.base[gt]
            }
            groupPos--

            var zn = minLen
            var zvec = readBits(zn, s)
            while (zvec > limitArr!![zn]) {
                zn++
                val zj = readBit(s).toInt()
                zvec = (zvec shl 1) or zj
            }
            var nextSym = permArr!![zvec - baseArr!![zn]]

            while (nextSym != eob) {
                if (nextSym == 0 || nextSym == 1) {
                    var es = -1
                    var N = 1
                    do {
                        if (nextSym == 0) es += N else if (nextSym == 1) es += 2 * N
                        N *= 2
                        if (groupPos == 0) {
                            groupNo++; groupPos = 50
                            val gt = s.selectorList[groupNo].toInt() and 0xFF
                            minLen = s.minLen[gt]; limitArr = s.limit[gt]
                            permArr = s.perm[gt]; baseArr = s.base[gt]
                        }
                        groupPos--
                        zn = minLen
                        zvec = readBits(zn, s)
                        while (zvec > limitArr!![zn]) {
                            zn++
                            val zj = readBit(s).toInt()
                            zvec = (zvec shl 1) or zj
                        }
                        nextSym = permArr!![zvec - baseArr!![zn]]
                    } while (nextSym == 0 || nextSym == 1)

                    es++
                    val uc = s.seqToUnseq[s.mtfSymbol[s.mtfBase[0]].toInt() and 0xFF]
                    s.charCount[uc.toInt() and 0xFF] += es
                    while (es > 0) {
                        tt[nBlock] = uc.toInt() and 0xFF
                        nBlock++
                        es--
                    }
                } else {
                    val nn = nextSym - 1
                    val uc: Byte
                    if (nn < 16) {
                        val pp = s.mtfBase[0]
                        uc = s.mtfSymbol[pp + nn]
                        var j = nn
                        while (j > 3) {
                            val z = pp + j
                            s.mtfSymbol[z] = s.mtfSymbol[z - 1]
                            s.mtfSymbol[z - 1] = s.mtfSymbol[z - 2]
                            s.mtfSymbol[z - 2] = s.mtfSymbol[z - 3]
                            s.mtfSymbol[z - 3] = s.mtfSymbol[z - 4]
                            j -= 4
                        }
                        while (j > 0) {
                            s.mtfSymbol[pp + j] = s.mtfSymbol[pp + j - 1]
                            j--
                        }
                        s.mtfSymbol[pp] = uc
                    } else {
                        val lno0 = nn / 16
                        val off = nn % 16
                        var pp = s.mtfBase[lno0] + off
                        uc = s.mtfSymbol[pp]
                        while (pp > s.mtfBase[lno0]) {
                            s.mtfSymbol[pp] = s.mtfSymbol[pp - 1]
                            pp--
                        }
                        s.mtfBase[lno0]++
                        var lno = lno0
                        while (lno > 0) {
                            s.mtfBase[lno]--
                            s.mtfSymbol[s.mtfBase[lno]] = s.mtfSymbol[s.mtfBase[lno - 1] + 16 - 1]
                            lno--
                        }
                        s.mtfBase[0]--
                        s.mtfSymbol[s.mtfBase[0]] = uc
                        if (s.mtfBase[0] == 0) {
                            var kk = 4095
                            for (ii in 15 downTo 0) {
                                for (jj in 15 downTo 0) {
                                    s.mtfSymbol[kk] = s.mtfSymbol[s.mtfBase[ii] + jj]
                                    kk--
                                }
                                s.mtfBase[ii] = kk + 1
                            }
                        }
                    }
                    s.charCount[s.seqToUnseq[uc.toInt() and 0xFF].toInt() and 0xFF]++
                    tt[nBlock] = s.seqToUnseq[uc.toInt() and 0xFF].toInt() and 0xFF
                    nBlock++

                    if (groupPos == 0) {
                        groupNo++; groupPos = 50
                        val gt = s.selectorList[groupNo].toInt() and 0xFF
                        minLen = s.minLen[gt]; limitArr = s.limit[gt]
                        permArr = s.perm[gt]; baseArr = s.base[gt]
                    }
                    groupPos--
                    zn = minLen
                    zvec = readBits(zn, s)
                    while (zvec > limitArr!![zn]) {
                        zn++
                        val zj = readBit(s).toInt()
                        zvec = (zvec shl 1) or zj
                    }
                    nextSym = permArr!![zvec - baseArr!![zn]]
                }
            }

            s.rleRepeat = 0
            s.rleByte = 0
            s.cumCount[0] = 0
            for (i in 1..256) s.cumCount[i] = s.charCount[i - 1]
            for (i in 1..256) s.cumCount[i] += s.cumCount[i - 1]
            for (i in 0 until nBlock) {
                val ch = (tt[i] and 0xFF).toByte()
                tt[s.cumCount[ch.toInt() and 0xFF]] = tt[s.cumCount[ch.toInt() and 0xFF]] or (i shl 8)
                s.cumCount[ch.toInt() and 0xFF]++
            }

            s.bwtPointer = tt[s.origPointer] shr 8
            s.bwtDecodePos = 0
            s.bwtPointer = tt[s.bwtPointer]
            s.bwtCurrentChar = (s.bwtPointer and 0xFF).toByte().toInt()
            s.bwtPointer = s.bwtPointer shr 8
            s.bwtDecodePos++
            s.bwtEndBlock = nBlock

            outputBlock(s)
            moreBlocks = s.bwtDecodePos == s.bwtEndBlock + 1 && s.rleRepeat == 0
        }
    }

    private fun outputBlock(s: State) {
        var prevCh = s.rleByte
        var repeat = s.rleRepeat
        var pos = s.bwtDecodePos
        var curCh = s.bwtCurrentChar
        val tt = s.tt!!
        var ptr = s.bwtPointer
        val out = s.outputBuf!!
        var outIdx = s.outputOffset
        var outRem = s.outputLen
        val outStart = outRem
        val endBlock = s.bwtEndBlock + 1

        loop@ while (true) {
            if (repeat > 0) {
                while (true) {
                    if (outRem == 0) break@loop
                    if (repeat == 1) break
                    out[outIdx] = prevCh
                    repeat--
                    outIdx++
                    outRem--
                }
                if (outRem == 0) { repeat = 1; break }
                out[outIdx] = prevCh
                outIdx++
                outRem--
            }
            var again = true
            while (again) {
                again = false
                if (pos == endBlock) { repeat = 0; break@loop }
                prevCh = curCh.toByte()
                ptr = tt[ptr]
                val nextCh = (ptr and 0xFF).toByte()
                ptr = ptr shr 8
                pos++
                if (nextCh.toInt() != curCh) {
                    curCh = nextCh.toInt()
                    if (outRem == 0) { repeat = 1; break@loop }
                    out[outIdx] = prevCh; outIdx++; outRem--
                    again = true; continue
                }
                if (pos != endBlock) continue
                if (outRem == 0) { repeat = 1; break@loop }
                out[outIdx] = prevCh; outIdx++; outRem--
                again = true
            }
            repeat = 2
            ptr = tt[ptr]
            val b1 = (ptr and 0xFF).toByte(); ptr = ptr shr 8
            if (++pos != endBlock) {
                if (b1.toInt() != curCh) {
                    curCh = b1.toInt()
                } else {
                    repeat = 3
                    ptr = tt[ptr]
                    val b2 = (ptr and 0xFF).toByte(); ptr = ptr shr 8
                    if (++pos != endBlock) {
                        if (b2.toInt() != curCh) {
                            curCh = b2.toInt()
                        } else {
                            ptr = tt[ptr]
                            val b3 = (ptr and 0xFF).toByte(); ptr = ptr shr 8; pos++
                            repeat = (b3.toInt() and 0xFF) + 4
                            ptr = tt[ptr]
                            curCh = (ptr and 0xFF).toByte().toInt()
                            ptr = ptr shr 8; pos++
                        }
                    }
                }
            }
        }

        val written = outStart - outRem
        val prev = s.bytesWritten
        s.bytesWritten += written
        if (s.bytesWritten < prev) s.bytesWrittenHigh++
        s.rleByte = prevCh
        s.rleRepeat = repeat
        s.bwtDecodePos = pos
        s.bwtCurrentChar = curCh
        s.bwtPointer = ptr
        s.outputOffset = outIdx
        s.outputLen = outRem
    }

    private fun readByte(s: State): Byte = readBits(8, s).toByte()
    private fun readBit(s: State): Byte = readBits(1, s).toByte()

    private fun readBits(n: Int, s: State): Int {
        while (true) {
            if (s.bitsBuffered >= n) {
                val result = (s.bitBuffer shr (s.bitsBuffered - n)) and ((1 shl n) - 1)
                s.bitsBuffered -= n
                return result
            }
            s.bitBuffer = (s.bitBuffer shl 8) or (s.inputBuf!![s.inputOffset].toInt() and 0xFF)
            s.bitsBuffered += 8
            s.inputOffset++
            s.inputLen--
            s.bytesRead++
            if (s.bytesRead == 0) s.bytesReadHigh++
        }
    }

    private fun buildSymbolMap(s: State) {
        s.symbolCount = 0
        for (i in 0 until 256) {
            if (s.symbolUsed[i]) {
                s.seqToUnseq[s.symbolCount] = i.toByte()
                s.symbolCount++
            }
        }
    }

    private fun buildHuffmanTable(
        limit: IntArray, base: IntArray, perm: IntArray,
        length: ByteArray, minLen: Int, maxLen: Int, alphaSize: Int
    ) {
        var pp = 0
        for (i in minLen..maxLen) {
            for (j in 0 until alphaSize) {
                if (length[j].toInt() == i) {
                    perm[pp] = j; pp++
                }
            }
        }
        for (i in 0 until 23) base[i] = 0
        for (i in 0 until alphaSize) base[length[i].toInt() + 1]++
        for (i in 1 until 23) base[i] += base[i - 1]
        for (i in 0 until 23) limit[i] = 0
        var vec = 0
        for (i in minLen..maxLen) {
            vec += base[i + 1] - base[i]
            limit[i] = vec - 1
            vec = vec shl 1
        }
        for (i in (minLen + 1)..maxLen) {
            base[i] = ((limit[i - 1] + 1) shl 1) - base[i]
        }
    }

    private class State {
        var inputBuf: ByteArray? = null
        var inputOffset = 0
        var inputLen = 0
        var bytesRead = 0
        var bytesReadHigh = 0

        var outputBuf: ByteArray? = null
        var outputOffset = 0
        var outputLen = 0
        var bytesWritten = 0
        var bytesWrittenHigh = 0

        var bitBuffer = 0
        var bitsBuffered = 0
        var blockCount = 0
        var blockSize = 1

        var rleByte: Byte = 0
        var rleRepeat = 0
        var origPointer = 0

        var bwtDecodePos = 0
        var bwtCurrentChar = 0
        var bwtPointer = 0
        var bwtEndBlock = 0

        var tt: IntArray? = null
        var symbolCount = 0

        val charCount = IntArray(256)
        val cumCount = IntArray(257)
        val symbolUsed = BooleanArray(256)
        val groupUsed = BooleanArray(16)
        val seqToUnseq = ByteArray(256)
        val mtfSymbol = ByteArray(4096)
        val mtfBase = IntArray(16)
        val selectorList = ByteArray(18002)
        val selectorMtf = ByteArray(18002)
        val codeLen = Array(6) { ByteArray(258) }
        val limit = Array(6) { IntArray(258) }
        val base = Array(6) { IntArray(258) }
        val perm = Array(6) { IntArray(258) }
        val minLen = IntArray(6)
    }
}