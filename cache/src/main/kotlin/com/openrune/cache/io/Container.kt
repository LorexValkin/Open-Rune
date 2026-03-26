package com.openrune.cache.io

import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.zip.GZIPInputStream

/**
 * Decompresses dat2 container data.
 *
 * In the dat2 cache format, each file stored on disk is wrapped in a
 * "container" with a compression header:
 *
 * ```
 * [1 byte]  compression type (0=none, 1=bzip2, 2=gzip)
 * [4 bytes] compressed length
 * -- if type != 0: --
 * [4 bytes] decompressed length
 * [compressed data...]
 * -- if type == 0: --
 * [uncompressed data...]
 * ```
 *
 * XTEA encryption, when present, is applied to the data AFTER the
 * compression-type byte and compressed-length int (i.e., starting at
 * offset 5). Decryption must happen BEFORE decompression.
 */
object Container {

    private val log = LoggerFactory.getLogger(Container::class.java)

    const val COMPRESSION_NONE = 0
    const val COMPRESSION_BZIP2 = 1
    const val COMPRESSION_GZIP = 2

    /**
     * Decompress a dat2 container.
     *
     * @param raw     The raw container bytes (from CacheReader.readFile).
     * @param xteaKey Optional 4-int XTEA key for encrypted containers.
     *                Pass null for unencrypted data.
     * @return The decompressed content bytes, or null on failure.
     */
    fun decompress(raw: ByteArray, xteaKey: IntArray? = null): ByteArray? {
        if (raw.size < 5) return null

        val buf = ByteBuffer.wrap(raw)
        val compressionType = buf.get().toInt() and 0xFF
        val compressedLength = buf.int

        if (compressedLength < 0 || compressedLength > 5_000_000) {
            log.warn("Suspicious compressed length: {}", compressedLength)
            return null
        }

        // Calculate total data length after the 5-byte header
        val dataLength = if (compressionType == COMPRESSION_NONE) {
            compressedLength
        } else {
            compressedLength + 4 // 4 extra bytes for decompressed length
        }

        if (5 + dataLength > raw.size) {
            log.warn("Container too short: need {} bytes, have {}", 5 + dataLength, raw.size)
            return null
        }

        // XTEA decrypt the data portion (offset 5 onward) if key provided
        val data = if (xteaKey != null && xteaKey.any { it != 0 }) {
            val decrypted = raw.copyOf()
            Xtea.decrypt(decrypted, 5, dataLength, xteaKey)
            ByteBuffer.wrap(decrypted, 5, dataLength)
        } else {
            ByteBuffer.wrap(raw, 5, dataLength)
        }

        return when (compressionType) {
            COMPRESSION_NONE -> {
                val result = ByteArray(compressedLength)
                data.get(result)
                result
            }
            COMPRESSION_BZIP2 -> {
                val decompressedLength = data.int
                val compressed = ByteArray(compressedLength)
                data.get(compressed)
                val result = ByteArray(decompressedLength)
                BZip2Decompressor.decompress(result, decompressedLength, compressed, compressedLength, 0)
                result
            }
            COMPRESSION_GZIP -> {
                val decompressedLength = data.int
                val compressed = ByteArray(compressedLength)
                data.get(compressed)
                decompressGzip(compressed, decompressedLength)
            }
            else -> {
                log.warn("Unknown compression type: {}", compressionType)
                null
            }
        }
    }

    private fun decompressGzip(compressed: ByteArray, expectedLength: Int): ByteArray? {
        return try {
            val gzip = GZIPInputStream(ByteArrayInputStream(compressed))
            val baos = ByteArrayOutputStream(expectedLength)
            val buf = ByteArray(4096)
            var len: Int
            while (gzip.read(buf).also { len = it } != -1) {
                baos.write(buf, 0, len)
            }
            gzip.close()
            baos.toByteArray()
        } catch (e: Exception) {
            log.warn("GZIP decompression failed: {}", e.message)
            null
        }
    }
}
