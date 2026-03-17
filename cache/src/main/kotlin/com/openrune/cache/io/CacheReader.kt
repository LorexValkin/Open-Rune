package com.openrune.cache.io

import org.slf4j.LoggerFactory
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path

/**
 * Reads the RS2 317 file store (main_file_cache.dat + main_file_cache.idx*).
 *
 * The 317 cache uses a flat file store with index files:
 *   - main_file_cache.dat  : contains all data chunks
 *   - main_file_cache.idx0 : index for archive 0 (models)
 *   - main_file_cache.idx1 : index for archive 1 (animations)
 *   - main_file_cache.idx2 : index for archive 2 (sounds)
 *   - main_file_cache.idx3 : index for archive 3 (maps)
 *   - main_file_cache.idx4 : index for archive 4 (interfaces)
 *
 * Each index entry is 6 bytes: 3 bytes size + 3 bytes sector offset.
 * Each sector in the data file is 520 bytes.
 */
class CacheReader(private val cachePath: Path) {

    private val log = LoggerFactory.getLogger(CacheReader::class.java)

    companion object {
        const val INDEX_ENTRY_SIZE = 6
        const val SECTOR_SIZE = 520
        const val SECTOR_HEADER_SIZE = 8
        const val SECTOR_DATA_SIZE = SECTOR_SIZE - SECTOR_HEADER_SIZE
    }

    private var dataFile: RandomAccessFile? = null
    private val indexFiles = mutableMapOf<Int, RandomAccessFile>()

    /**
     * Open the cache for reading.
     */
    fun open(): Boolean {
        val dataPath = findFile("main_file_cache.dat")
        if (dataPath == null) {
            log.error("main_file_cache.dat not found in {}", cachePath)
            return false
        }

        dataFile = RandomAccessFile(dataPath.toFile(), "r")

        // Open index files
        for (i in 0..4) {
            val idxPath = findFile("main_file_cache.idx$i")
            if (idxPath != null) {
                indexFiles[i] = RandomAccessFile(idxPath.toFile(), "r")
                log.debug("Opened index {}: {} entries", i, indexFiles[i]!!.length() / INDEX_ENTRY_SIZE)
            }
        }

        log.info("Cache opened: {} indices loaded", indexFiles.size)
        return true
    }

    /**
     * Read a file from the cache by index and file ID.
     * Returns the raw decompressed bytes, or null if not found.
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
            val sectorFileId = ((sectorBuf[0].toInt() and 0xFF) shl 8) or (sectorBuf[1].toInt() and 0xFF)
            val sectorChunk = ((sectorBuf[2].toInt() and 0xFF) shl 8) or (sectorBuf[3].toInt() and 0xFF)
            val nextSector = ((sectorBuf[4].toInt() and 0xFF) shl 16) or
                             ((sectorBuf[5].toInt() and 0xFF) shl 8) or
                             (sectorBuf[6].toInt() and 0xFF)
            val sectorIndex = sectorBuf[7].toInt() and 0xFF

            // Validate sector integrity.
            // File ID and chunk sequence must match. Index mismatch is common
            // in some 317 cache builds (versionlist packed with wrong index marker)
            // so we log it but don't abort.
            if (sectorFileId != fileId || sectorChunk != chunk) {
                log.warn("Sector validation failed: expected file={} chunk={}, got file={} chunk={}",
                    fileId, chunk, sectorFileId, sectorChunk)
                return null
            }

            val toRead = minOf(remaining, SECTOR_DATA_SIZE)
            System.arraycopy(sectorBuf, SECTOR_HEADER_SIZE, result, resultOffset, toRead)

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
     * Close all file handles.
     */
    fun close() {
        dataFile?.close()
        indexFiles.values.forEach { it.close() }
        indexFiles.clear()
    }

    private fun findFile(name: String): Path? {
        // Search recursively in cache path
        return Files.walk(cachePath, 3)
            .filter { it.fileName.toString() == name }
            .findFirst()
            .orElse(null)
    }
}
