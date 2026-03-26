package com.openrune.core.world.region

import com.openrune.core.world.collision.CollisionMap
import com.openrune.core.world.collision.CollisionFlag
import com.openrune.cache.io.ArchiveReader
import com.openrune.cache.io.CacheReader
import com.openrune.cache.io.Container
import com.openrune.cache.io.MapIndex
import com.openrune.cache.io.ReferenceTable
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.nio.ByteBuffer

/**
 * Loads region map data from the cache and populates the [CollisionMap].
 *
 * Supports both 317 and dat2 cache formats:
 *
 * **317:** Reads versionlist archive from index 0 for region->file mapping,
 * then reads landscape/object files from index 4 (GZIP compressed).
 *
 * **dat2:** Reads map data from index 5 with XTEA decryption. Region files
 * are found by name hash ("l{x}_{y}" for objects, "m{x}_{y}" for landscape).
 *
 * ENGINE-LEVEL system. Plugins cannot replace region loading.
 */
class RegionLoader(
    private val collisionMap: CollisionMap,
    private val cache: CacheReader?,
    private val xteaKeys: Map<Int, IntArray> = emptyMap()
) {

    private val log = LoggerFactory.getLogger(RegionLoader::class.java)

    data class ObjectPlacement(val objectId: Int, val type: Int, val rotation: Int)
    private val objectPlacements = mutableMapOf<String, ObjectPlacement>()

    fun getPlacement(x: Int, y: Int, z: Int): ObjectPlacement? = objectPlacements["$x,$y,$z"]
    fun placementCount(): Int = objectPlacements.size

    private val loadedRegions = mutableSetOf<Int>()
    private var mapIndex: MapIndex? = null

    // dat2: reference table and name->groupId mapping for index 5
    private var mapRefTable: ReferenceTable? = null
    private val nameToGroupId = mutableMapOf<Int, Int>() // nameHash -> groupId

    companion object {
        const val HEIGHT_LEVELS = 4
        const val TILE_BLOCKED = 1
        const val TILE_BRIDGE = 2
        const val MAP_INDEX_ID = 5
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

        if (cache.isDat2) {
            initializeDat2()
        } else {
            initialize317()
        }
    }

    private fun initializeDat2() {
        val c = cache ?: return

        // Load reference table for index 5 (maps)
        val refRaw = c.readFile(CacheReader.META_INDEX, MAP_INDEX_ID)
        if (refRaw == null) {
            log.error("Could not read map reference table from idx255")
            return
        }
        val refData = Container.decompress(refRaw)
        if (refData == null) {
            log.error("Could not decompress map reference table")
            return
        }
        mapRefTable = ReferenceTable.decode(refData)
        if (mapRefTable == null) {
            log.error("Could not parse map reference table")
            return
        }

        log.info("dat2: Map reference table loaded with {} groups, XTEA keys: {}",
            mapRefTable!!.groupCount, xteaKeys.size)

        preloadAllObjectPlacementsDat2()
    }

    private fun initialize317() {
        val c = cache ?: return

        val fileCount = c.fileCount(0)
        log.info("Scanning {} files in index 0 for map_index...", fileCount)
        for (fileId in 0 until fileCount) {
            val data = c.readFile(0, fileId) ?: continue
            if (data.size < 8) continue

            try {
                val archive = ArchiveReader(data)
                val mapIndexData = archive.getEntry("map_index")
                if (mapIndexData != null && mapIndexData.size >= 7) {
                    mapIndex = MapIndex(mapIndexData)
                    log.info("Map index loaded from index 0, file {}: {} regions indexed", fileId, mapIndex!!.size)
                    preloadAllObjectPlacements317()
                    return
                }
            } catch (_: Exception) {}
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
        if (cache.isDat2) {
            loadRegionDat2(regionId)
        } else {
            loadRegion317(regionId)
        }
    }

    // ─── dat2 region loading ────────────────────────────────────────

    private fun loadRegionDat2(regionId: Int) {
        val c = cache ?: return
        val ref = mapRefTable ?: return

        val regionX = (regionId shr 8) and 0xFF
        val regionY = regionId and 0xFF
        val baseX = regionX * 64
        val baseY = regionY * 64
        val regionKey = (regionX shl 8) or regionY

        // Find landscape and object group IDs by name lookup
        val landscapeName = "m${regionX}_${regionY}"
        val objectName = "l${regionX}_${regionY}"

        // Read landscape
        val landscapeGroupId = ref.findGroupByName(landscapeName)
        if (landscapeGroupId >= 0) {
            val raw = c.readFile(MAP_INDEX_ID, landscapeGroupId)
            if (raw != null) {
                val data = Container.decompress(raw)
                if (data != null) {
                    try { decodeLandscape(data, baseX, baseY) }
                    catch (e: Exception) { log.debug("dat2 landscape decode error ({},{}): {}", regionX, regionY, e.message) }
                }
            }
        }

        // Read objects (may need XTEA)
        val objectGroupId = ref.findGroupByName(objectName)
        if (objectGroupId >= 0) {
            val raw = c.readFile(MAP_INDEX_ID, objectGroupId)
            if (raw != null) {
                val xteaKey = xteaKeys[regionKey]
                val data = Container.decompress(raw, xteaKey)
                if (data != null) {
                    try {
                        decodeObjects(data, baseX, baseY)
                    } catch (e: Exception) {
                        log.debug("dat2 object decode error ({},{}): {}", regionX, regionY, e.message)
                    }
                } else if (xteaKey == null) {
                    log.debug("Region ({},{}) likely XTEA-encrypted, no key available", regionX, regionY)
                }
            }
        }
    }


    // ─── 317 region loading ─────────────────────────────────────────

    private fun loadRegion317(regionId: Int) {
        val c = cache ?: return

        val regionX = (regionId shr 8) and 0xFF
        val regionY = regionId and 0xFF
        val baseX = regionX * 64
        val baseY = regionY * 64

        val entry = mapIndex?.lookup(regionX, regionY) ?: return

        if (entry.landscapeFileId >= 0) {
            val rawLandscape = c.readFile(4, entry.landscapeFileId)
            if (rawLandscape != null) {
                val data = decompressGzip(rawLandscape) ?: rawLandscape
                try { decodeLandscape(data, baseX, baseY) }
                catch (e: Exception) { log.debug("Landscape decode error ({}, {}): {}", regionX, regionY, e.message) }
            }
        }

        if (entry.objectFileId >= 0) {
            val rawObjects = c.readFile(4, entry.objectFileId)
            if (rawObjects != null) {
                val data = decompressGzip(rawObjects) ?: rawObjects
                try {
                    decodeObjects(data, baseX, baseY)
                } catch (e: Exception) {
                    log.warn("Object decode error ({}, {}): {}", regionX, regionY, e.message)
                }
            }
        }
    }

    // ─── Shared decoders ────────────────────────────────────────────

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

                val worldX = baseX + localX
                val worldY = baseY + localY
                addObjectCollision(worldX, worldY, height, objectId, objectType, rotation)
                objectPlacements["$worldX,$worldY,$height"] = ObjectPlacement(objectId, objectType, rotation)
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

    // ─── Preloading ─────────────────────────────────────────────────

    private fun preloadAllObjectPlacementsDat2() {
        val c = cache ?: return
        val ref = mapRefTable ?: return
        var regionCount = 0
        var objectCount = 0
        var encryptedSkipped = 0

        for (regionX in 0..255) {
            for (regionY in 0..255) {
                val regionKey = (regionX shl 8) or regionY
                val objectName = "l${regionX}_${regionY}"
                val objectGroupId = ref.findGroupByName(objectName)

                // Check if this group exists
                if (objectGroupId < 0) continue

                val raw = c.readFile(MAP_INDEX_ID, objectGroupId) ?: continue
                val xteaKey = xteaKeys[regionKey]
                val data = Container.decompress(raw, xteaKey)

                if (data == null) {
                    if (xteaKey == null) encryptedSkipped++
                    continue
                }

                val baseX = regionX * 64
                val baseY = regionY * 64
                val buf = ByteBuffer.wrap(data)
                var objectId = -1

                try {
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

                            val wx = baseX + localX
                            val wy = baseY + localY
                            objectPlacements["$wx,$wy,$height"] = ObjectPlacement(objectId, objectType, rotation)
                            objectCount++
                        }
                    }
                    regionCount++
                } catch (_: Exception) {}
            }
        }

        log.info("dat2: Preloaded {} object placements from {} regions ({} encrypted regions skipped due to missing keys)",
            objectCount, regionCount, encryptedSkipped)
    }

    private fun preloadAllObjectPlacements317() {
        val idx = mapIndex ?: return
        val c = cache ?: return
        var regionCount = 0
        var objectCount = 0

        for (regionX in 0..255) {
            for (regionY in 0..255) {
                val entry = idx.lookup(regionX, regionY) ?: continue
                if (entry.objectFileId < 0) continue

                val rawData = c.readFile(4, entry.objectFileId) ?: continue
                val data = decompressGzip(rawData) ?: rawData

                val baseX = regionX * 64
                val baseY = regionY * 64
                val buf = ByteBuffer.wrap(data)
                var objectId = -1

                try {
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

                            val wx = baseX + localX
                            val wy = baseY + localY
                            objectPlacements["$wx,$wy,$height"] = ObjectPlacement(objectId, objectType, rotation)
                            objectCount++
                        }
                    }
                    regionCount++
                } catch (_: Exception) {}
            }
        }

        log.info("Preloaded {} object placements from {} regions", objectCount, regionCount)
    }

    // ─── Utilities ──────────────────────────────────────────────────

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
