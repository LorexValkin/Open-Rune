package com.openrune.cache.io

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.slf4j.LoggerFactory
import java.io.FileReader
import java.nio.file.Path

/**
 * Loads XTEA keys from an OpenRS2-format keys.json file.
 *
 * Each entry in the JSON array has:
 * ```json
 * {
 *   "mapsquare": 12850,
 *   "key": [-1303394492, 1234604739, -1845593033, -1096028287]
 * }
 * ```
 *
 * The mapsquare is the region key: (regionX << 8) | regionY.
 * The key is a 4-int XTEA key used to decrypt map data for that region.
 */
object XteaKeyLoader {

    private val log = LoggerFactory.getLogger(XteaKeyLoader::class.java)

    /**
     * JSON model matching the OpenRS2 keys.json format.
     * Only the fields we need are declared; extras are ignored by Gson.
     */
    private data class XteaEntry(
        val mapsquare: Int = 0,
        val key: IntArray = intArrayOf()
    )

    /**
     * Load XTEA keys from a JSON file.
     *
     * @param path Path to keys.json.
     * @return Map of region key (mapsquare) -> 4-int XTEA key array.
     */
    fun load(path: Path): Map<Int, IntArray> {
        val file = path.toFile()
        if (!file.exists()) {
            log.warn("XTEA keys file not found: {}", path)
            return emptyMap()
        }

        val type = object : TypeToken<List<XteaEntry>>() {}.type
        val entries: List<XteaEntry> = Gson().fromJson(FileReader(file), type)

        val keys = mutableMapOf<Int, IntArray>()
        for (entry in entries) {
            if (entry.key.size == 4) {
                keys[entry.mapsquare] = entry.key
            }
        }

        log.info("Loaded {} XTEA keys from {}", keys.size, path.fileName)
        return keys
    }
}
