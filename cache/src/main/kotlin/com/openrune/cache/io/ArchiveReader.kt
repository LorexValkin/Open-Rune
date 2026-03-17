package com.openrune.cache.io

import java.nio.ByteBuffer

/**
 * Reads RS2 317 "jag" archives.
 *
 * A jag archive is a container with named entries. The container may be
 * bzip2-compressed (if decompressed size != compressed size in the header).
 * Each entry is identified by a name hash and has its own compressed/decompressed sizes.
 *
 * Structure:
 *   [3 bytes] decompressed size
 *   [3 bytes] compressed size
 *   -- if compressed != decompressed, the entire payload is bzip2'd --
 *   [2 bytes] entry count
 *   For each entry:
 *     [4 bytes] name hash
 *     [3 bytes] decompressed size
 *     [3 bytes] compressed size
 *   Entry data follows sequentially
 *
 * ENGINE-LEVEL utility.
 */
class ArchiveReader(rawData: ByteArray) {

    private val data: ByteArray
    private val entryCount: Int
    private val nameHashes: IntArray
    private val decompressedSizes: IntArray
    private val compressedSizes: IntArray
    private val dataOffsets: IntArray

    init {
        val buf = ByteBuffer.wrap(rawData)
        val decompressedSize = read3Bytes(buf)
        val compressedSize = read3Bytes(buf)

        val payload = if (compressedSize != decompressedSize) {
            // Entire archive is bzip2 compressed
            val decompressed = ByteArray(decompressedSize)
            // DIAGNOSTIC: print first 20 bytes at BZip2 start offset
            val preview = (6 until minOf(26, rawData.size)).map { rawData[it].toInt() and 0xFF }
            println("  [RAW] decompSize=$decompressedSize compSize=$compressedSize first20bytes=$preview")
            BZip2Decompressor.decompress(decompressed, decompressedSize, rawData, compressedSize, 6)
            decompressed
        } else {
            rawData
        }

        val archiveBuf = if (compressedSize != decompressedSize) {
            ByteBuffer.wrap(payload)
        } else {
            // Skip the 6-byte header we already read
            buf
        }

        entryCount = archiveBuf.short.toInt() and 0xFFFF
        nameHashes = IntArray(entryCount)
        decompressedSizes = IntArray(entryCount)
        compressedSizes = IntArray(entryCount)
        dataOffsets = IntArray(entryCount)

        // Read entry headers
        var dataStart = archiveBuf.position() + entryCount * 10
        for (i in 0 until entryCount) {
            nameHashes[i] = archiveBuf.int
            decompressedSizes[i] = read3Bytes(archiveBuf)
            compressedSizes[i] = read3Bytes(archiveBuf)
            dataOffsets[i] = dataStart
            dataStart += compressedSizes[i]
        }

        data = payload
    }

    /**
     * Get entry data by name. Returns null if not found.
     * The name is hashed using the RS2 name hash function.
     */
    fun getEntry(name: String): ByteArray? {
        val hash = nameToHash(name)
        for (i in 0 until entryCount) {
            if (nameHashes[i] == hash) {
                val result = ByteArray(decompressedSizes[i])
                if (compressedSizes[i] != decompressedSizes[i]) {
                    // Entry is individually compressed
                    BZip2Decompressor.decompress(result, decompressedSizes[i], data, compressedSizes[i], dataOffsets[i])
                } else {
                    System.arraycopy(data, dataOffsets[i], result, 0, decompressedSizes[i])
                }
                return result
            }
        }
        return null
    }

    companion object {
        /**
         * RS2 name hash function. Matches the client's StreamLoader hash.
         */
        fun nameToHash(name: String): Int {
            var hash = 0
            for (ch in name.uppercase()) {
                hash = hash * 61 + ch.code - 32
            }
            return hash
        }

        private fun read3Bytes(buf: ByteBuffer): Int {
            return ((buf.get().toInt() and 0xFF) shl 16) or
                   ((buf.get().toInt() and 0xFF) shl 8) or
                   (buf.get().toInt() and 0xFF)
        }
    }
}

/**
 * Parses the map_index data from the versionlist archive.
 *
 * Each entry is 7 bytes:
 *   [2 bytes] region key (regionX << 8 | regionY)
 *   [2 bytes] landscape file ID (terrain data)
 *   [2 bytes] object file ID (object placements)
 *   [1 byte]  members flag
 *
 * ENGINE-LEVEL utility. Used by [RegionLoader] to find map file IDs.
 */
class MapIndex(mapIndexData: ByteArray) {

    data class RegionEntry(
        val regionKey: Int,
        val landscapeFileId: Int,
        val objectFileId: Int,
        val members: Boolean
    ) {
        val regionX: Int get() = regionKey shr 8
        val regionY: Int get() = regionKey and 0xFF
    }

    private val entries: Map<Int, RegionEntry>

    init {
        val count = mapIndexData.size / 7
        val buf = ByteBuffer.wrap(mapIndexData)
        val map = mutableMapOf<Int, RegionEntry>()

        for (i in 0 until count) {
            val regionKey = buf.short.toInt() and 0xFFFF
            val landscapeId = buf.short.toInt() and 0xFFFF
            val objectId = buf.short.toInt() and 0xFFFF
            val members = (buf.get().toInt() and 0xFF) != 0

            map[regionKey] = RegionEntry(regionKey, landscapeId, objectId, members)
        }

        entries = map
    }

    /** Look up file IDs for a region. Returns null if the region isn't in the index. */
    fun lookup(regionX: Int, regionY: Int): RegionEntry? {
        return entries[(regionX shl 8) or regionY]
    }

    /** Total number of indexed regions. */
    val size: Int get() = entries.size
}
