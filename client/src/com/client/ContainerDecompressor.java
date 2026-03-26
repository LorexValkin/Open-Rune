package com.client;


import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Decompresses dat2 container data.
 *
 * Container format:
 *   [1 byte]  compression type (0=none, 1=bzip2, 2=gzip)
 *   [4 bytes] compressed length
 *   -- if type != 0: --
 *   [4 bytes] decompressed length
 *   [compressed data...]
 *   -- if type == 0: --
 *   [uncompressed data...]
 */
public class ContainerDecompressor {

    public static final int COMPRESSION_NONE = 0;
    public static final int COMPRESSION_BZIP2 = 1;
    public static final int COMPRESSION_GZIP = 2;

    /**
     * Decompress a dat2 container.
     * @param raw The raw container bytes from the cache.
     * @return The decompressed content, or null on failure.
     */
    public static byte[] decompress(byte[] raw) {
        return decompress(raw, null);
    }

    /**
     * Decompress a dat2 container with optional XTEA decryption.
     * @param raw     The raw container bytes.
     * @param xteaKey Optional 4-int XTEA key (null for unencrypted).
     * @return The decompressed content, or null on failure.
     */
    public static byte[] decompress(byte[] raw, int[] xteaKey) {
        if (raw == null || raw.length < 5) return null;

        int compressionType = raw[0] & 0xFF;
        int compressedLength = ((raw[1] & 0xFF) << 24) | ((raw[2] & 0xFF) << 16)
                | ((raw[3] & 0xFF) << 8) | (raw[4] & 0xFF);

        if (compressionType > 3) {
            // Not a valid container — first byte isn't a known compression type
            return null;
        }
        if (compressedLength < 0 || compressedLength > 50_000_000) return null;

        int dataLength;
        if (compressionType == COMPRESSION_NONE) {
            dataLength = compressedLength;
        } else {
            dataLength = compressedLength + 4;
        }

        if (5 + dataLength > raw.length) {
            // Data shorter than expected — could be truncated or wrong format
            return null;
        }

        // XTEA decrypt if key provided
        byte[] data;
        if (xteaKey != null && hasNonZeroKey(xteaKey)) {
            data = new byte[raw.length];
            System.arraycopy(raw, 0, data, 0, raw.length);
            xteaDecrypt(data, 5, dataLength, xteaKey);
        } else {
            data = raw;
        }

        int offset = 5;
        try {
            switch (compressionType) {
                case COMPRESSION_NONE: {
                    byte[] result = new byte[compressedLength];
                    System.arraycopy(data, offset, result, 0, compressedLength);
                    return result;
                }
                case COMPRESSION_BZIP2: {
                    int decompressedLength = ((data[offset] & 0xFF) << 24) | ((data[offset + 1] & 0xFF) << 16)
                            | ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);
                    byte[] compressed = new byte[compressedLength];
                    System.arraycopy(data, offset + 4, compressed, 0, compressedLength);
                    byte[] result = new byte[decompressedLength];
                    BZip2Decompressor.method225(result, decompressedLength, compressed, compressedLength, 0);
                    return result;
                }
                case COMPRESSION_GZIP: {
                    int decompressedLength = ((data[offset] & 0xFF) << 24) | ((data[offset + 1] & 0xFF) << 16)
                            | ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);
                    byte[] compressed = new byte[compressedLength];
                    System.arraycopy(data, offset + 4, compressed, 0, compressedLength);
                    byte[] result = decompressGzip(compressed, decompressedLength);
                    if (result == null && gzipDebugCount < 3) {
                        gzipDebugCount++;
                        System.out.println("[Container] GZIP failed: compLen=" + compressedLength
                            + " decompLen=" + decompressedLength
                            + " firstGzipBytes=[" + (compressed[0]&0xFF) + "," + (compressed[1]&0xFF) + "]");
                    }
                    return result;
                }
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] decompressGzip(byte[] compressed, int expectedLength) {
        try {
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed));
            ByteArrayOutputStream baos = new ByteArrayOutputStream(expectedLength);
            byte[] buf = new byte[4096];
            int len;
            while ((len = gzip.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            gzip.close();
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private static int gzipDebugCount = 0;

    private static boolean hasNonZeroKey(int[] key) {
        for (int k : key) if (k != 0) return true;
        return false;
    }

    /**
     * XTEA decrypt in-place. 32 rounds, 8-byte blocks.
     */
    private static void xteaDecrypt(byte[] data, int offset, int length, int[] key) {
        int numBlocks = length / 8;
        int pos = offset;
        for (int block = 0; block < numBlocks; block++) {
            int v0 = ((data[pos] & 0xFF) << 24) | ((data[pos + 1] & 0xFF) << 16)
                    | ((data[pos + 2] & 0xFF) << 8) | (data[pos + 3] & 0xFF);
            int v1 = ((data[pos + 4] & 0xFF) << 24) | ((data[pos + 5] & 0xFF) << 16)
                    | ((data[pos + 6] & 0xFF) << 8) | (data[pos + 7] & 0xFF);

            int sum = 0x9E3779B9 * 32;
            for (int round = 0; round < 32; round++) {
                v1 -= (((v0 << 4) ^ (v0 >>> 5)) + v0) ^ (sum + key[(sum >>> 11) & 3]);
                sum -= 0x9E3779B9;
                v0 -= (((v1 << 4) ^ (v1 >>> 5)) + v1) ^ (sum + key[sum & 3]);
            }

            data[pos] = (byte) (v0 >>> 24);
            data[pos + 1] = (byte) (v0 >>> 16);
            data[pos + 2] = (byte) (v0 >>> 8);
            data[pos + 3] = (byte) v0;
            data[pos + 4] = (byte) (v1 >>> 24);
            data[pos + 5] = (byte) (v1 >>> 16);
            data[pos + 6] = (byte) (v1 >>> 8);
            data[pos + 7] = (byte) v1;

            pos += 8;
        }
    }
}
