package com.openrune.core.world.region

import com.openrune.core.world.collision.CollisionMap
import com.openrune.core.world.collision.CollisionFlag
import com.openrune.cache.io.ArchiveReader
import com.openrune.cache.io.CacheReader
import com.openrune.cache.io.MapIndex
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.nio.ByteBuffer

/**
 * Loads region map data from the 317 cache and populates the [CollisionMap].
 *
 * On startup, reads the versionlist archive from cache index 0 to build
 * a region -> file ID mapping table. When a region needs to be loaded,
 * reads the landscape and object files from cache index 4 and applies
 * tile flags and object collision to the collision map.
 *
 * ENGINE-LEVEL system. Plugins cannot replace region loading.
 */
class RegionLoader(
    private val collisionMap: CollisionMap,
    private val cache: CacheReader?
) {

    private val log = LoggerFactory.getLogger(RegionLoader::class.java)

    private val loadedRegions = mutableSetOf<Int>()
    private var mapIndex: MapIndex? = null

    companion object {
        const val HEIGHT_LEVELS = 4
        const val TILE_BLOCKED = 1
        const val TILE_BRIDGE = 2
    }

    /**
     * Initialize the map index from the cache.
     * Must be called after the cache is opened.
     */
    fun initialize() {
        if (cache == null) {
            log.warn("No cache available, collision data will be empty")
            return
        }

        // Try every file in index 0 to find the archive containing "map_index".
        // Different 317 cache builds store it at different file IDs.
        val fileCount = cache.fileCount(0)
        log.info("Scanning {} files in index 0 for map_index...", fileCount)
        for (fileId in 0 until fileCount) {
            val data = cache.readFile(0, fileId)
            if (data == null) {
                log.debug("  File {}: read returned null", fileId)
                continue
            }
            if (data.size < 8) {
                log.debug("  File {}: too small ({} bytes)", fileId, data.size)
                continue
            }

            log.info("  File {}: {} bytes, header: [{}, {}, {}, {}, {}, {}]",
                fileId, data.size,
                data[0].toInt() and 0xFF, data[1].toInt() and 0xFF,
                data[2].toInt() and 0xFF, data[3].toInt() and 0xFF,
                data[4].toInt() and 0xFF, data[5].toInt() and 0xFF)

            try {
                val archive = ArchiveReader(data)
                val mapIndexData = archive.getEntry("map_index")
                if (mapIndexData != null && mapIndexData.size >= 7) {
                    mapIndex = MapIndex(mapIndexData)
                    log.info("Map index loaded from index 0, file {}: {} regions indexed", fileId, mapIndex!!.size)
                    return
                } else {
                    log.info("  File {}: archive parsed OK but no map_index entry", fileId)
                }
            } catch (e: Exception) {
                log.info("  File {}: parse failed: {}", fileId, e.message)
            }
        }

        log.warn("map_index not found in any index 0 archive ({} files checked)", fileCount)
    }

    fun ensureLoaded(regionId: Int) {
        if (regionId in loadedRegions) return
        synchronized(loadedRegions) {
            if (regionId in loadedRegions) return
            loadRegion(regionId)
            loadedRegions.add(regionId)
        }
    }

    private fun loadRegion(regionId: Int) {
        if (cache == null) return

        val regionX = (regionId shr 8) and 0xFF
        val regionY = regionId and 0xFF
        val baseX = regionX * 64
        val baseY = regionY * 64

        val entry = mapIndex?.lookup(regionX, regionY) ?: return

        if (entry.landscapeFileId >= 0) {
            val rawLandscape = cache.readFile(4, entry.landscapeFileId)
            if (rawLandscape != null) {
                val data = decompressGzip(rawLandscape) ?: rawLandscape
                try { decodeLandscape(data, baseX, baseY) }
                catch (e: Exception) { log.debug("Landscape decode error ({}, {}): {}", regionX, regionY, e.message) }
            }
        }

        if (entry.objectFileId >= 0) {
            val rawObjects = cache.readFile(4, entry.objectFileId)
            if (rawObjects != null) {
                val data = decompressGzip(rawObjects) ?: rawObjects
                try { decodeObjects(data, baseX, baseY) }
                catch (e: Exception) { log.debug("Object decode error ({}, {}): {}", regionX, regionY, e.message) }
            }
        }
    }

    private fun decodeLandscape(data: ByteArray, baseX: Int, baseY: Int) {
        val buf = ByteBuffer.wrap(data)
        val tileFlags = Array(HEIGHT_LEVELS) { Array(64) { IntArray(64) } }

        for (z in 0 until HEIGHT_LEVELS) {
            for (x in 0 until 64) {
                for (y in 0 until 64) {
                    decodeTile(buf, tileFlags, z, x, y)
                }
            }
        }

        for (z in 0 until HEIGHT_LEVELS) {
            for (x in 0 until 64) {
                for (y in 0 until 64) {
                    if (tileFlags[z][x][y] and TILE_BLOCKED != 0) {
                        var actualZ = z
                        if (tileFlags[1][x][y] and TILE_BRIDGE != 0) actualZ = z - 1
                        if (actualZ >= 0) {
                            collisionMap.addFlag(baseX + x, baseY + y, actualZ, CollisionFlag.OBJECT_TILE or CollisionFlag.OBJECT_BLOCK)
                        }
                    }
                }
            }
        }
    }

    private fun decodeTile(buf: ByteBuffer, flags: Array<Array<IntArray>>, z: Int, x: Int, y: Int) {
        if (!buf.hasRemaining()) return
        while (true) {
            val opcode = buf.get().toInt() and 0xFF
            if (opcode == 0) break
            else if (opcode == 1) { buf.get(); break }
            else if (opcode <= 49) buf.get()
            else if (opcode <= 81) flags[z][x][y] = opcode - 49
        }
    }

    private fun decodeObjects(data: ByteArray, baseX: Int, baseY: Int) {
        val buf = ByteBuffer.wrap(data)
        var objectId = -1

        while (buf.hasRemaining()) {
            val idOffset = readSmart(buf)
            if (idOffset == 0) break
            objectId += idOffset

            var positionOffset = 0
            while (true) {
                val posOffset = readSmart(buf)
                if (posOffset == 0) break
                positionOffset += posOffset - 1

                val localX = (positionOffset shr 6) and 0x3F
                val localY = positionOffset and 0x3F
                val height = positionOffset shr 12

                val attributes = buf.get().toInt() and 0xFF
                val objectType = attributes shr 2
                val rotation = attributes and 3

                addObjectCollision(baseX + localX, baseY + localY, height, objectId, objectType, rotation)
            }
        }
    }

    private fun addObjectCollision(x: Int, y: Int, height: Int, objectId: Int, type: Int, rotation: Int) {
        when (type) {
            in 0..3 -> collisionMap.addWall(x, y, height, type, rotation)
            9 -> collisionMap.addWall(x, y, height, type, rotation)
            10, 11 -> collisionMap.addObject(x, y, height, 1, 1, true)
            22 -> collisionMap.addFlag(x, y, height, CollisionFlag.FLOOR_DECO)
        }
    }

    /**
     * Decompress GZip data. Map files in cache index 4 are GZip compressed.
     * Returns the decompressed bytes, or null if decompression fails.
     */
    private fun decompressGzip(data: ByteArray): ByteArray? {
        return try {
            val bais = ByteArrayInputStream(data)
            val gzip = GZIPInputStream(bais)
            val baos = ByteArrayOutputStream()
            val buf = ByteArray(4096)
            var len: Int
            while (gzip.read(buf).also { len = it } != -1) {
                baos.write(buf, 0, len)
            }
            gzip.close()
            baos.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    private fun readSmart(buf: ByteBuffer): Int {
        val peek = buf.get(buf.position()).toInt() and 0xFF
        return if (peek >= 128) (buf.short.toInt() and 0xFFFF) - 32768
        else buf.get().toInt() and 0xFF
    }

    fun isLoaded(regionId: Int): Boolean = regionId in loadedRegions
    fun loadedCount(): Int = loadedRegions.size
}