package com.openrune.cache.io

import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

/**
 * Reads the dat2 reference table (aka "index metadata") for an archive.
 *
 * In the dat2 cache format, idx255 stores a reference table for each
 * regular index (0-24). The reference table describes the groups (archives)
 * within that index and the files within each group.
 *
 * Reference table binary format:
 * ```
 * [1 byte]   protocol (format: 5, 6, or 7)
 * [4 bytes]  revision (if protocol >= 6)
 * [1 byte]   flags (bit 0 = named, bit 1 = unknown hashes, bit 2 = whirlpool, bit 3 = sizes)
 * [2/smart]  number of groups
 * For each group: group ID delta (cumulative, short or smart)
 * [optional] name hashes (int per group, if flags & 0x01)
 * CRC per group (int)
 * [optional] unknown hashes (int per group, if flags & 0x02)
 * [optional] whirlpool hashes (64 bytes per group, if flags & 0x04)
 * [optional] compressed+decompressed sizes (2 ints per group, if flags & 0x08)
 * Version per group (int)
 * File counts per group (short or smart)
 * File ID deltas per group
 * [optional] file name hashes (if flags & 0x01)
 * ```
 */
class ReferenceTable private constructor(
    val groups: Map<Int, GroupEntry>,
    private val nameHashToGroupId: Map<Int, Int>
) {

    data class GroupEntry(
        val groupId: Int,
        val nameHash: Int,
        val fileIds: IntArray
    ) {
        val fileCount: Int get() = fileIds.size
    }

    val groupCount: Int get() = groups.size

    fun group(groupId: Int): GroupEntry? = groups[groupId]

    fun groupIds(): Set<Int> = groups.keys

    /**
     * Find a group by name using djb2 hash lookup.
     * Returns the group ID, or -1 if not found.
     */
    fun findGroupByName(name: String): Int {
        val hash = djb2(name)
        return nameHashToGroupId[hash] ?: -1
    }

    /** djb2 hash function matching RuneLite/OSRS. */
    private fun djb2(str: String): Int {
        var hash = 0
        for (ch in str) {
            hash = ch.code + ((hash shl 5) - hash)
        }
        return hash
    }

    companion object {

        private val log = LoggerFactory.getLogger(ReferenceTable::class.java)

        fun decode(data: ByteArray): ReferenceTable? {
            if (data.size < 2) return null

            val buf = ByteBuffer.wrap(data)
            val protocol = buf.get().toInt() and 0xFF

            if (protocol < 5 || protocol > 7) {
                log.warn("Unsupported reference table protocol: {}", protocol)
                return null
            }

            // Protocol 6+ has a 4-byte revision
            if (protocol >= 6) {
                buf.int // revision, skip
            }

            // Flags byte
            val flags = buf.get().toInt() and 0xFF
            val named = (flags and 0x01) != 0
            val hasWhirlpool = (flags and 0x02) != 0
            val hasSizes = (flags and 0x04) != 0

            // Number of groups
            val groupCount = if (protocol >= 7) {
                readBigSmart(buf)
            } else {
                buf.short.toInt() and 0xFFFF
            }

            // Read group ID deltas (cumulative)
            val groupIds = IntArray(groupCount)
            var groupAccum = 0
            for (i in 0 until groupCount) {
                val delta = if (protocol >= 7) readBigSmart(buf) else buf.short.toInt() and 0xFFFF
                groupAccum += delta
                groupIds[i] = groupAccum
            }

            // Name hashes (if named flag set)
            val nameHashes = IntArray(groupCount)
            if (named) {
                for (i in 0 until groupCount) {
                    nameHashes[i] = buf.int
                }
            }

            // CRC per group
            for (i in 0 until groupCount) {
                buf.int // CRC, skip
            }

            // Whirlpool hashes (64 bytes per group)
            if (hasWhirlpool) {
                for (i in 0 until groupCount) {
                    val whirlpool = ByteArray(64)
                    buf.get(whirlpool) // skip
                }
            }

            // Compressed + decompressed sizes
            if (hasSizes) {
                for (i in 0 until groupCount) {
                    buf.int // compressed size, skip
                    buf.int // decompressed size, skip
                }
            }

            // Version per group
            for (i in 0 until groupCount) {
                buf.int // version, skip
            }

            // File counts per group
            val fileCounts = IntArray(groupCount)
            for (i in 0 until groupCount) {
                fileCounts[i] = if (protocol >= 7) readBigSmart(buf) else buf.short.toInt() and 0xFFFF
            }

            // File ID deltas per group
            val fileIds = Array(groupCount) { intArrayOf() }
            for (i in 0 until groupCount) {
                val count = fileCounts[i]
                val ids = IntArray(count)
                var fileAccum = 0
                for (j in 0 until count) {
                    val delta = if (protocol >= 7) readBigSmart(buf) else buf.short.toInt() and 0xFFFF
                    fileAccum += delta
                    ids[j] = fileAccum
                }
                fileIds[i] = ids
            }

            // File name hashes (if named) — skip
            if (named) {
                for (i in 0 until groupCount) {
                    for (j in 0 until fileCounts[i]) {
                        buf.int // file name hash, skip
                    }
                }
            }

            // Build the group map and name hash lookup
            val groups = mutableMapOf<Int, GroupEntry>()
            val nameHashLookup = mutableMapOf<Int, Int>()
            for (i in 0 until groupCount) {
                groups[groupIds[i]] = GroupEntry(groupIds[i], nameHashes[i], fileIds[i])
                if (named && nameHashes[i] != 0) {
                    nameHashLookup[nameHashes[i]] = groupIds[i]
                }
            }

            log.info("Reference table: {} groups, protocol {}, flags 0x{}, named={}", groupCount, protocol, Integer.toHexString(flags), named)
            return ReferenceTable(groups, nameHashLookup)
        }

        /**
         * Read a "big smart" value used in protocol 7.
         * If the first byte has bit 7 set, reads a 4-byte int (masked).
         * Otherwise reads a 2-byte short.
         */
        private fun readBigSmart(buf: ByteBuffer): Int {
            val peek = buf.get(buf.position()).toInt() and 0xFF
            return if (peek >= 128) {
                (buf.int and 0x7FFFFFFF)
            } else {
                buf.short.toInt() and 0xFFFF
            }
        }
    }
}
