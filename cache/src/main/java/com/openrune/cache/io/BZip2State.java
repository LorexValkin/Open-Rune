package com.openrune.cache.io;

/**
 * Internal state for the RS2 BZip2 decompressor.
 * This is the standard RS2 headerless BZip2 format used in all 317 cache archives.
 */
final class BZip2State {
    byte[] inputBuf;
    int inputOffset;
    int inputLen;
    int inputBytesRead;
    int inputBytesReadHigh;

    byte[] outputBuf;
    int outputOffset;
    int outputLen;
    int outputBytesWritten;
    int outputBytesWrittenHigh;

    int bitBuffer;
    int bitsBuffered;
    int blockCount;

    byte rleByte;
    int rleRepeat;
    boolean randomized;
    int origPointer;
    int blockSize;

    int bwtDecodePos;
    int bwtCurrentChar;
    int bwtPointer;
    int bwtEndBlock;

    final int[] charCount = new int[256];
    final int[] cumCount = new int[257];
    static int[] bwtBlock;
    int symbolCount;
    final boolean[] symbolInUse = new boolean[256];
    final boolean[] groupInUse = new boolean[16];
    final byte[] seqToUnseq = new byte[256];
    final byte[] mtfSymbol = new byte[4096];
    final int[] mtfBase = new int[16];
    final byte[] selectorList = new byte[18002];
    final byte[] selectorMtf = new byte[18002];
    final byte[][] codeLen = new byte[6][258];
    final int[][] limit = new int[6][258];
    final int[][] base = new int[6][258];
    final int[][] perm = new int[6][258];
    final int[] minLen = new int[6];

    BZip2State() {}
}
