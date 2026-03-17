package com.openrune.cache.io;

/**
 * RS2 headerless BZip2 decompressor.
 * Standard BZip2 algorithm (BWT + Huffman + MTF + RLE) used by all 317 cache archives.
 * The RS2 format strips the standard "BZh" header.
 */
public final class BZip2 {

    private static final BZip2State state = new BZip2State();

    public static int decompress(byte[] output, int outputLen, byte[] input, int inputLen, int inputOffset) {
        synchronized (state) {
            state.inputBuf = input;
            state.inputOffset = inputOffset;
            state.outputBuf = output;
            state.outputOffset = 0;
            state.inputLen = inputLen;
            state.outputLen = outputLen;
            state.bitBuffer = 0;
            state.bitsBuffered = 0;
            state.inputBytesRead = 0;
            state.inputBytesReadHigh = 0;
            state.outputBytesWritten = 0;
            state.outputBytesWrittenHigh = 0;
            state.blockCount = 0;
            decodeBlocks(state);
            outputLen -= state.outputLen;
            return outputLen;
        }
    }

    private static void outputBlock(BZip2State s) {
        byte prevCh = s.rleByte;
        int repeat = s.rleRepeat;
        int pos = s.bwtDecodePos;
        int curCh = s.bwtCurrentChar;
        int[] tt = BZip2State.bwtBlock;
        int ptr = s.bwtPointer;
        byte[] out = s.outputBuf;
        int outIdx = s.outputOffset;
        int outRem = s.outputLen;
        int outStart = outRem;
        int endBlock = s.bwtEndBlock + 1;

        label0:
        do {
            if (repeat > 0) {
                do {
                    if (outRem == 0) break label0;
                    if (repeat == 1) break;
                    out[outIdx] = prevCh;
                    repeat--;
                    outIdx++;
                    outRem--;
                } while (true);
                if (outRem == 0) { repeat = 1; break; }
                out[outIdx] = prevCh;
                outIdx++;
                outRem--;
            }
            boolean again = true;
            while (again) {
                again = false;
                if (pos == endBlock) { repeat = 0; break label0; }
                prevCh = (byte) curCh;
                ptr = tt[ptr];
                byte nextCh = (byte) (ptr & 0xff);
                ptr >>= 8;
                pos++;
                if (nextCh != curCh) {
                    curCh = nextCh;
                    if (outRem == 0) { repeat = 1; } else {
                        out[outIdx] = prevCh; outIdx++; outRem--;
                        again = true; continue;
                    }
                    break label0;
                }
                if (pos != endBlock) continue;
                if (outRem == 0) { repeat = 1; break label0; }
                out[outIdx] = prevCh; outIdx++; outRem--;
                again = true;
            }
            repeat = 2;
            ptr = tt[ptr];
            byte b1 = (byte) (ptr & 0xff); ptr >>= 8;
            if (++pos != endBlock)
                if (b1 != curCh) { curCh = b1; }
                else {
                    repeat = 3;
                    ptr = tt[ptr];
                    byte b2 = (byte) (ptr & 0xff); ptr >>= 8;
                    if (++pos != endBlock)
                        if (b2 != curCh) { curCh = b2; }
                        else {
                            ptr = tt[ptr];
                            byte b3 = (byte) (ptr & 0xff); ptr >>= 8; pos++;
                            repeat = (b3 & 0xff) + 4;
                            ptr = tt[ptr];
                            curCh = (byte) (ptr & 0xff);
                            ptr >>= 8; pos++;
                        }
                }
        } while (true);

        int written = outStart - outRem;
        int prev = s.outputBytesWritten;
        s.outputBytesWritten += written;
        if (s.outputBytesWritten < prev) s.outputBytesWrittenHigh++;
        s.rleByte = prevCh;
        s.rleRepeat = repeat;
        s.bwtDecodePos = pos;
        s.bwtCurrentChar = curCh;
        BZip2State.bwtBlock = tt;
        s.bwtPointer = ptr;
        s.outputBuf = out;
        s.outputOffset = outIdx;
        s.outputLen = outRem;
    }

    private static void decodeBlocks(BZip2State s) {
        int minL = 0;
        int[] limitArr = null;
        int[] baseArr = null;
        int[] permArr = null;
        s.blockSize = 1;
        if (BZip2State.bwtBlock == null)
            BZip2State.bwtBlock = new int[s.blockSize * 100000];
        boolean moreBlocks = true;
        while (moreBlocks) {
            byte magic = readByte(s);
            if (magic == 23) return;  // end of stream
            readByte(s); readByte(s); readByte(s); readByte(s); readByte(s);
            s.blockCount++;
            readByte(s); readByte(s); readByte(s);
            // randomized flag
            byte rb = readBit(s);
            s.randomized = rb != 0;
            if (s.randomized)
                System.out.println("PANIC! RANDOMISED BLOCK!");
            s.origPointer = 0;
            s.origPointer = s.origPointer << 8 | readByte(s) & 0xff;
            s.origPointer = s.origPointer << 8 | readByte(s) & 0xff;
            s.origPointer = s.origPointer << 8 | readByte(s) & 0xff;
            // symbol usage
            for (int i = 0; i < 16; i++)
                s.groupInUse[i] = readBit(s) == 1;
            for (int i = 0; i < 256; i++) s.symbolInUse[i] = false;
            for (int g = 0; g < 16; g++)
                if (s.groupInUse[g])
                    for (int b = 0; b < 16; b++)
                        if (readBit(s) == 1)
                            s.symbolInUse[g * 16 + b] = true;
            buildSymbolMap(s);
            int alphaSize = s.symbolCount + 2;
            int nGroups = readBits(3, s);
            int nSelectors = readBits(15, s);
            if (nSelectors > 18000) throw new ArrayIndexOutOfBoundsException("Invalid selector count: " + nSelectors);
            // read selector MTF
            for (int i = 0; i < nSelectors; i++) {
                int j = 0;
                do { byte v = readBit(s); if (v == 0) break; j++; } while (true);
                s.selectorMtf[i] = (byte) j;
            }
            // undo MTF on selectors
            byte[] pos = new byte[6];
            for (byte i = 0; i < nGroups; i++) pos[i] = i;
            for (int i = 0; i < nSelectors; i++) {
                byte v = s.selectorMtf[i];
                byte tmp = pos[v];
                for (; v > 0; v--) pos[v] = pos[v - 1];
                pos[0] = tmp;
                s.selectorList[i] = tmp;
            }
            // read code lengths per group
            for (int g = 0; g < nGroups; g++) {
                int len = readBits(5, s);
                for (int sym = 0; sym < alphaSize; sym++) {
                    do {
                        byte b = readBit(s);
                        if (b == 0) break;
                        b = readBit(s);
                        if (b == 0) len++; else len--;
                    } while (true);
                    s.codeLen[g][sym] = (byte) len;
                }
            }
            // build Huffman tables
            for (int g = 0; g < nGroups; g++) {
                byte mn = 32; int mx = 0;
                for (int sym = 0; sym < alphaSize; sym++) {
                    if (s.codeLen[g][sym] > mx) mx = s.codeLen[g][sym];
                    if (s.codeLen[g][sym] < mn) mn = s.codeLen[g][sym];
                }
                buildTable(s.limit[g], s.base[g], s.perm[g], s.codeLen[g], mn, mx, alphaSize);
                s.minLen[g] = mn;
            }
            // decode MTF values
            int eob = s.symbolCount + 1;
            int blockLen = 100000 * s.blockSize;
            int[] tt = BZip2State.bwtBlock;
            int nBlock = 0;
            int groupNo = -1, groupPos = 0;
            for (int i = 0; i <= 255; i++) s.charCount[i] = 0;
            // init MTF
            int mtfI = 4095;
            for (int g15 = 15; g15 >= 0; g15--) {
                for (int b15 = 15; b15 >= 0; b15--) {
                    s.mtfSymbol[mtfI] = (byte) (g15 * 16 + b15);
                    mtfI--;
                }
                s.mtfBase[g15] = mtfI + 1;
            }
            // first symbol
            if (groupPos == 0) {
                groupNo++; groupPos = 50;
                byte gt = s.selectorList[groupNo];
                minL = s.minLen[gt]; limitArr = s.limit[gt];
                permArr = s.perm[gt]; baseArr = s.base[gt];
            }
            groupPos--;
            int zn = minL;
            int zvec;
            byte zj;
            for (zvec = readBits(zn, s); zvec > limitArr[zn]; zvec = zvec << 1 | zj) {
                zn++; zj = readBit(s);
            }
            int nextSym = permArr[zvec - baseArr[zn]];

            while (nextSym != eob) {
                if (nextSym == 0 || nextSym == 1) {
                    int es = -1, N = 1;
                    do {
                        if (nextSym == 0) es += N; else if (nextSym == 1) es += 2 * N;
                        N *= 2;
                        if (groupPos == 0) {
                            groupNo++; groupPos = 50;
                            byte gt = s.selectorList[groupNo];
                            minL = s.minLen[gt]; limitArr = s.limit[gt];
                            permArr = s.perm[gt]; baseArr = s.base[gt];
                        }
                        groupPos--;
                        zn = minL;
                        for (zvec = readBits(zn, s); zvec > limitArr[zn]; zvec = zvec << 1 | zj) {
                            zn++; zj = readBit(s);
                        }
                        nextSym = permArr[zvec - baseArr[zn]];
                    } while (nextSym == 0 || nextSym == 1);
                    es++;
                    byte uc = s.seqToUnseq[s.mtfSymbol[s.mtfBase[0]] & 0xff];
                    s.charCount[uc & 0xff] += es;
                    for (; es > 0; es--) { tt[nBlock] = uc & 0xff; nBlock++; }
                } else {
                    int nn = nextSym - 1;
                    byte uc;
                    if (nn < 16) {
                        int pp = s.mtfBase[0];
                        uc = s.mtfSymbol[pp + nn];
                        for (; nn > 3; nn -= 4) {
                            int z = pp + nn;
                            s.mtfSymbol[z] = s.mtfSymbol[z - 1];
                            s.mtfSymbol[z - 1] = s.mtfSymbol[z - 2];
                            s.mtfSymbol[z - 2] = s.mtfSymbol[z - 3];
                            s.mtfSymbol[z - 3] = s.mtfSymbol[z - 4];
                        }
                        for (; nn > 0; nn--)
                            s.mtfSymbol[pp + nn] = s.mtfSymbol[pp + nn - 1];
                        s.mtfSymbol[pp] = uc;
                    } else {
                        int lno = nn / 16, off = nn % 16;
                        int pp = s.mtfBase[lno] + off;
                        uc = s.mtfSymbol[pp];
                        for (; pp > s.mtfBase[lno]; pp--)
                            s.mtfSymbol[pp] = s.mtfSymbol[pp - 1];
                        s.mtfBase[lno]++;
                        for (; lno > 0; lno--) {
                            s.mtfBase[lno]--;
                            s.mtfSymbol[s.mtfBase[lno]] = s.mtfSymbol[s.mtfBase[lno - 1] + 16 - 1];
                        }
                        s.mtfBase[0]--;
                        s.mtfSymbol[s.mtfBase[0]] = uc;
                        if (s.mtfBase[0] == 0) {
                            int kk = 4095;
                            for (int ii = 15; ii >= 0; ii--) {
                                for (int jj = 15; jj >= 0; jj--) {
                                    s.mtfSymbol[kk] = s.mtfSymbol[s.mtfBase[ii] + jj];
                                    kk--;
                                }
                                s.mtfBase[ii] = kk + 1;
                            }
                        }
                    }
                    s.charCount[s.seqToUnseq[uc & 0xff] & 0xff]++;
                    tt[nBlock] = s.seqToUnseq[uc & 0xff] & 0xff;
                    nBlock++;
                    if (groupPos == 0) {
                        groupNo++; groupPos = 50;
                        byte gt = s.selectorList[groupNo];
                        minL = s.minLen[gt]; limitArr = s.limit[gt];
                        permArr = s.perm[gt]; baseArr = s.base[gt];
                    }
                    groupPos--;
                    zn = minL;
                    for (zvec = readBits(zn, s); zvec > limitArr[zn]; zvec = zvec << 1 | zj) {
                        zn++; zj = readBit(s);
                    }
                    nextSym = permArr[zvec - baseArr[zn]];
                }
            }
            // inverse BWT
            s.rleRepeat = 0; s.rleByte = 0;
            s.cumCount[0] = 0;
            for (int i = 1; i <= 256; i++) s.cumCount[i] = s.charCount[i - 1];
            for (int i = 1; i <= 256; i++) s.cumCount[i] += s.cumCount[i - 1];
            for (int i = 0; i < nBlock; i++) {
                byte ch = (byte) (tt[i] & 0xff);
                tt[s.cumCount[ch & 0xff]] |= (i << 8);
                s.cumCount[ch & 0xff]++;
            }
            s.bwtPointer = tt[s.origPointer] >> 8;
            s.bwtDecodePos = 0;
            s.bwtPointer = tt[s.bwtPointer];
            s.bwtCurrentChar = (byte) (s.bwtPointer & 0xff);
            s.bwtPointer >>= 8;
            s.bwtDecodePos++;
            s.bwtEndBlock = nBlock;
            outputBlock(s);
            moreBlocks = s.bwtDecodePos == s.bwtEndBlock + 1 && s.rleRepeat == 0;
        }
    }

    private static byte readByte(BZip2State s) { return (byte) readBits(8, s); }
    private static byte readBit(BZip2State s) { return (byte) readBits(1, s); }

    private static int readBits(int n, BZip2State s) {
        int result;
        do {
            if (s.bitsBuffered >= n) {
                result = (s.bitBuffer >> s.bitsBuffered - n) & ((1 << n) - 1);
                s.bitsBuffered -= n;
                break;
            }
            s.bitBuffer = s.bitBuffer << 8 | s.inputBuf[s.inputOffset] & 0xff;
            s.bitsBuffered += 8;
            s.inputOffset++;
            s.inputLen--;
            s.inputBytesRead++;
            if (s.inputBytesRead == 0) s.inputBytesReadHigh++;
        } while (true);
        return result;
    }

    private static void buildSymbolMap(BZip2State s) {
        s.symbolCount = 0;
        for (int i = 0; i < 256; i++)
            if (s.symbolInUse[i]) {
                s.seqToUnseq[s.symbolCount] = (byte) i;
                s.symbolCount++;
            }
    }

    private static void buildTable(int[] limit, int[] base, int[] perm, byte[] length, int minLen, int maxLen, int alphaSize) {
        int pp = 0;
        for (int i = minLen; i <= maxLen; i++)
            for (int j = 0; j < alphaSize; j++)
                if (length[j] == i) { perm[pp] = j; pp++; }
        for (int i = 0; i < 23; i++) base[i] = 0;
        for (int i = 0; i < alphaSize; i++) base[length[i] + 1]++;
        for (int i = 1; i < 23; i++) base[i] += base[i - 1];
        for (int i = 0; i < 23; i++) limit[i] = 0;
        int vec = 0;
        for (int i = minLen; i <= maxLen; i++) {
            vec += base[i + 1] - base[i];
            limit[i] = vec - 1;
            vec <<= 1;
        }
        for (int i = minLen + 1; i <= maxLen; i++)
            base[i] = ((limit[i - 1] + 1) << 1) - base[i];
    }
}
