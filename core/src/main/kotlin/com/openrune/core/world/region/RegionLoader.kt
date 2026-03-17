package com.openrune.core.world.region

import com.openrune.core.world.collision.CollisionMap
import com.openrune.core.world.collision.CollisionFlag
import com.openrune.cache.io.ArchiveReader
import com.openrune.cache.io.CacheReader
import com.openrune.cache.io.MapIndex
import org.slf4j.LoggerFactory
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
        for (fileId in 0 until fileCount) {
            val data = cache.readFile(0, fileId) ?: continue
            if (data.size < 8) continue

            try {
                val archive = ArchiveReader(data)
                val mapIndexData = archive.getEntry("map_index")
                if (mapIndexData != null && mapIndexData.size >= 7) {
                    mapIndex = MapIndex(mapIndexData)
                    log.info("Map index loaded from index 0, file {}: {} regions indexed", fileId, mapIndex!!.size)
                    return
                }
            } catch (_: Exception) {
                // Not the right file or not a valid archive -- try next
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
            val data = cache.readFile(4, entry.landscapeFileId)
            if (data != null) {
                try { decodeLandscape(data, baseX, baseY) }
                catch (e: Exception) { log.debug("Landscape decode error ({}, {}): {}", regionX, regionY, e.message) }
            }
        }

        if (entry.objectFileId >= 0) {
            val data = cache.readFile(4, entry.objectFileId)
            if (data != null) {
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
                            collisionMap.addFlag(baseX + x, baseY + y, actualZ, CollisionFlag.OBJECT_TILE)
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

    private fun readSmart(buf: ByteBuffer): Int {
        val peek = buf.get(buf.position()).toInt() and 0xFF
        return if (peek >= 128) (buf.short.toInt() and 0xFFFF) - 32768
        else buf.get().toInt() and 0xFF
    }

    fun isLoaded(regionId: Int): Boolean = regionId in loadedRegions
    fun loadedCount(): Int = loadedRegions.size
}
