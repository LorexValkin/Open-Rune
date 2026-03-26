package com.openrune.cache.io

import org.slf4j.LoggerFactory
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path

/**
 * Reads the RS2 file store. Supports both legacy 317 format
 * (main_file_cache.dat + idx0-4) and dat2 format
 * (main_file_cache.dat2 + idx0-24 + idx255).
 *
 * **317 format:**
 *   - main_file_cache.dat  : data sectors
 *   - main_file_cache.idx0-4 : 5 index files
 *   - Sector header: 8 bytes (2 fileId + 2 chunk + 3 nextSector + 1 index)
 *
 * **dat2 format:**
 *   - main_file_cache.dat2 : data sectors
 *   - main_file_cache.idx0-24 : archive indexes
 *   - main_file_cache.idx255 : meta/reference table index
 *   - Sector header: 8 bytes for fileId <= 65535, 10 bytes for fileId > 65535
 *     (extended: 4 fileId + 2 chunk + 3 nextSector + 1 index)
 *   - Files are containers with compression header
 *
 * Each index entry is 6 bytes: 3 bytes size + 3 bytes sector offset.
 * Each sector is 520 bytes total.
 */
class CacheReader(private val cachePath: Path) {

    private val log = LoggerFactory.getLogger(CacheReader::class.java)

    companion object {
        const val INDEX_ENTRY_SIZE = 6
        const val SECTOR_SIZE = 520
        const val SECTOR_HEADER_SIZE = 8
        const val SECTOR_HEADER_SIZE_EXTENDED = 10
        const val SECTOR_DATA_SIZE = SECTOR_SIZE - SECTOR_HEADER_SIZE
        const val SECTOR_DATA_SIZE_EXTENDED = SECTOR_SIZE - SECTOR_HEADER_SIZE_EXTENDED
        const val META_INDEX = 255
    }

    private var dataFile: RandomAccessFile? = null
    private val indexFiles = mutableMapOf<Int, RandomAccessFile>()

    /** True if the cache uses dat2 format (extended indexes, containers). */
    var isDat2: Boolean = false
        private set

    /**
     * Open the cache for reading. Auto-detects dat2 vs 317 format.
     */
    fun open(): Boolean {
        // Try dat2 first, then fall back to 317
        val dat2Path = findFile("main_file_cache.dat2")
        val datPath = findFile("main_file_cache.dat")

        if (dat2Path != null) {
            isDat2 = true
            dataFile = RandomAccessFile(dat2Path.toFile(), "r")
            log.info("Detected dat2 format cache")

            // Open all index files (0-24 + 255)
            for (i in 0..24) {
                val idxPath = findFile("main_file_cache.idx$i")
                if (idxPath != null) {
                    indexFiles[i] = RandomAccessFile(idxPath.toFile(), "r")
                    log.debug("Opened index {}: {} entries", i, indexFiles[i]!!.length() / INDEX_ENTRY_SIZE)
                }
            }
            // Meta-index (idx255)
            val metaPath = findFile("main_file_cache.idx$META_INDEX")
            if (metaPath != null) {
                indexFiles[META_INDEX] = RandomAccessFile(metaPath.toFile(), "r")
                log.debug("Opened meta-index (idx255): {} entries", indexFiles[META_INDEX]!!.length() / INDEX_ENTRY_SIZE)
            }
        } else if (datPath != null) {
            isDat2 = false
            dataFile = RandomAccessFile(datPath.toFile(), "r")
            log.info("Detected legacy 317 format cache")

            // Open index files 0-4
            for (i in 0..4) {
                val idxPath = findFile("main_file_cache.idx$i")
                if (idxPath != null) {
                    indexFiles[i] = RandomAccessFile(idxPath.toFile(), "r")
                    log.debug("Opened index {}: {} entries", i, indexFiles[i]!!.length() / INDEX_ENTRY_SIZE)
                }
            }
        } else {
            log.error("No cache data file found in {}", cachePath)
            return false
        }

        log.info("Cache opened: {} indices loaded (dat2={})", indexFiles.size, isDat2)
        return true
    }

    /**
     * Read raw sector data for a file from the cache by index and file ID.
     *
     * For dat2 caches, the returned bytes are a raw container (compression
     * header + compressed/uncompressed data). Use [Container.decompress]
     * to get the actual content.
     *
     * For 317 caches, returns the raw bytes as before.
     */
    fun readFile(indexId: Int, fileId: Int): ByteArray? {
        val index = indexFiles[indexId] ?: return null
        val data = dataFile ?: return null

        // Read index entry
        val entryOffset = fileId.toLong() * INDEX_ENTRY_SIZE
        if (entryOffset + INDEX_ENTRY_SIZE > index.length()) return null

        index.seek(entryOffset)
        val entryBuf = ByteArray(INDEX_ENTRY_SIZE)
        index.readFully(entryBuf)

        val fileSize = ((entryBuf[0].toInt() and 0xFF) shl 16) or
                       ((entryBuf[1].toInt() and 0xFF) shl 8) or
                       (entryBuf[2].toInt() and 0xFF)

        val sectorId = ((entryBuf[3].toInt() and 0xFF) shl 16) or
                       ((entryBuf[4].toInt() and 0xFF) shl 8) or
                       (entryBuf[5].toInt() and 0xFF)

        if (fileSize <= 0 || sectorId <= 0) return null

        // Determine if this file uses extended sector headers (dat2 + fileId > 65535)
        val extended = isDat2 && fileId > 65535
        val headerSize = if (extended) SECTOR_HEADER_SIZE_EXTENDED else SECTOR_HEADER_SIZE
        val dataSize = SECTOR_SIZE - headerSize

        // Read sectors
        val result = ByteArray(fileSize)
        var remaining = fileSize
        var currentSector = sectorId
        var chunk = 0
        var resultOffset = 0

        while (remaining > 0) {
            val sectorOffset = currentSector.toLong() * SECTOR_SIZE
            if (sectorOffset + SECTOR_SIZE > data.length()) {
                log.warn("Sector out of bounds: index={}, file={}, sector={}", indexId, fileId, currentSector)
                return null
            }

            data.seek(sectorOffset)
            val sectorBuf = ByteArray(SECTOR_SIZE)
            data.readFully(sectorBuf)

            // Parse sector header
            val sectorFileId: Int
            val sectorChunk: Int
            val nextSector: Int
            val sectorIndex: Int

            if (extended) {
                // Extended header: 4-byte fileId, 2-byte chunk, 3-byte nextSector, 1-byte index
                sectorFileId = ((sectorBuf[0].toInt() and 0xFF) shl 24) or
                               ((sectorBuf[1].toInt() and 0xFF) shl 16) or
                               ((sectorBuf[2].toInt() and 0xFF) shl 8) or
                               (sectorBuf[3].toInt() and 0xFF)
                sectorChunk = ((sectorBuf[4].toInt() and 0xFF) shl 8) or (sectorBuf[5].toInt() and 0xFF)
                nextSector = ((sectorBuf[6].toInt() and 0xFF) shl 16) or
                             ((sectorBuf[7].toInt() and 0xFF) shl 8) or
                             (sectorBuf[8].toInt() and 0xFF)
                sectorIndex = sectorBuf[9].toInt() and 0xFF
            } else {
                // Standard header: 2-byte fileId, 2-byte chunk, 3-byte nextSector, 1-byte index
                sectorFileId = ((sectorBuf[0].toInt() and 0xFF) shl 8) or (sectorBuf[1].toInt() and 0xFF)
                sectorChunk = ((sectorBuf[2].toInt() and 0xFF) shl 8) or (sectorBuf[3].toInt() and 0xFF)
                nextSector = ((sectorBuf[4].toInt() and 0xFF) shl 16) or
                             ((sectorBuf[5].toInt() and 0xFF) shl 8) or
                             (sectorBuf[6].toInt() and 0xFF)
                sectorIndex = sectorBuf[7].toInt() and 0xFF
            }

            // Validate sector integrity
            if (sectorFileId != fileId || sectorChunk != chunk) {
                log.warn("Sector validation failed: expected file={} chunk={}, got file={} chunk={}",
                    fileId, chunk, sectorFileId, sectorChunk)
                return null
            }

            val toRead = minOf(remaining, dataSize)
            System.arraycopy(sectorBuf, headerSize, result, resultOffset, toRead)

            resultOffset += toRead
            remaining -= toRead
            currentSector = nextSector
            chunk++
        }

        return result
    }

    /**
     * Get the number of files in a given index.
     */
    fun fileCount(indexId: Int): Int {
        val index = indexFiles[indexId] ?: return 0
        return (index.length() / INDEX_ENTRY_SIZE).toInt()
    }

    /**
     * Check if an index exists and has files.
     */
    fun hasIndex(indexId: Int): Boolean = indexFiles.containsKey(indexId)

    /**
     * Close all file handles.
     */
    fun close() {
        dataFile?.close()
        indexFiles.values.forEach { it.close() }
        indexFiles.clear()
    }

    private fun findFile(name: String): Path? {
        return Files.walk(cachePath, 3)
            .filter { it.fileName.toString() == name }
            .findFirst()
            .orElse(null)
    }
}
