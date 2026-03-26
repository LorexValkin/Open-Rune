package com.openrune.cache.tools

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Cross-references the server items.json against the cache-exported items.json.
 *
 * Produces:
 *   1. A validation report at ~/Desktop/item-validation.txt
 *   2. A merged items-validated.json at data/items/items-validated.json
 *      that uses cache data as the base and layers server overrides on top.
 *
 * Cache data is authoritative for: name, stackable, value, members,
 * interfaceActions, groundActions.
 *
 * Server overrides preserved: examine, tradeable, weight, highAlch, lowAlch,
 * equipSlot, twoHanded, requirements, bonuses, and any other custom fields.
 *
 * Usage: ./gradlew :cache:validateItems
 */
fun main() {
    val projectDir = Path.of("C:\\Users\\User\\IdeaProjects\\Open-Rune")
    val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    // ── Load cache items ────────────────────────────────────────────
    val cacheFile = projectDir.resolve("cache/data/cache-export/items.json")
    if (!Files.exists(cacheFile)) {
        println("ERROR: Cache export not found at $cacheFile")
        println("Run ./gradlew :cache:exportCache first.")
        return
    }

    val cacheItems = loadItemsById(cacheFile)
    println("Loaded ${cacheItems.size} cache item definitions")

    // ── Load server items ───────────────────────────────────────────
    val serverFile = projectDir.resolve("data/items/items.json")
    if (!Files.exists(serverFile)) {
        println("ERROR: Server items.json not found at $serverFile")
        return
    }

    val serverItems = loadItemsById(serverFile)
    println("Loaded ${serverItems.size} server item definitions")

    // ── Cross-reference and collect mismatches ──────────────────────
    val nameMismatches = mutableListOf<MismatchEntry>()
    val stackMismatches = mutableListOf<MismatchEntry>()
    val valueMismatches = mutableListOf<MismatchEntry>()
    val missingFromCache = mutableListOf<Int>()

    for ((id, serverObj) in serverItems) {
        val cacheObj = cacheItems[id]
        if (cacheObj == null) {
            missingFromCache.add(id)
            continue
        }

        val serverName = serverObj.getStringOrNull("name") ?: ""
        val cacheName = cacheObj.getStringOrNull("name") ?: ""
        if (serverName != cacheName && cacheName.isNotBlank() && cacheName != "null") {
            nameMismatches.add(MismatchEntry(id, "name", serverName, cacheName))
        }

        val serverStackable = serverObj.getBoolOrNull("stackable")
        val cacheStackable = cacheObj.getBoolOrNull("stackable")
        if (serverStackable != null && cacheStackable != null && serverStackable != cacheStackable) {
            stackMismatches.add(MismatchEntry(id, "stackable", serverStackable.toString(), cacheStackable.toString()))
        }

        val serverValue = serverObj.getIntOrNull("value")
        val cacheValue = cacheObj.getIntOrNull("value")
        if (serverValue != null && cacheValue != null && serverValue != cacheValue) {
            valueMismatches.add(MismatchEntry(id, "value", serverValue.toString(), cacheValue.toString()))
        }
    }

    // ── Write validation report ─────────────────────────────────────
    val reportFile = File(System.getProperty("user.home"), "Desktop/item-validation.txt")
    reportFile.parentFile.mkdirs()

    reportFile.bufferedWriter().use { w ->
        w.write("OpenRune Item Validation Report\n")
        w.write("Cache items: ${cacheItems.size}\n")
        w.write("Server items: ${serverItems.size}\n")
        w.write("=".repeat(100) + "\n\n")

        w.write("SUMMARY\n")
        w.write("-".repeat(50) + "\n")
        w.write("  Name mismatches:      ${nameMismatches.size}\n")
        w.write("  Stackable mismatches: ${stackMismatches.size}\n")
        w.write("  Value mismatches:     ${valueMismatches.size}\n")
        w.write("  Missing from cache:   ${missingFromCache.size}\n")
        w.write("\n")

        if (nameMismatches.isNotEmpty()) {
            w.write("NAME MISMATCHES\n")
            w.write("-".repeat(80) + "\n")
            for (m in nameMismatches.sortedBy { it.id }) {
                w.write("  ID ${m.id}: server=\"${m.serverValue}\" -> cache=\"${m.cacheValue}\"\n")
            }
            w.write("\n")
        }

        if (stackMismatches.isNotEmpty()) {
            w.write("STACKABLE MISMATCHES\n")
            w.write("-".repeat(80) + "\n")
            for (m in stackMismatches.sortedBy { it.id }) {
                val serverName = serverItems[m.id]?.getStringOrNull("name") ?: "?"
                w.write("  ID ${m.id} ($serverName): server=${m.serverValue} -> cache=${m.cacheValue}\n")
            }
            w.write("\n")
        }

        if (valueMismatches.isNotEmpty()) {
            w.write("VALUE MISMATCHES\n")
            w.write("-".repeat(80) + "\n")
            for (m in valueMismatches.sortedBy { it.id }) {
                val serverName = serverItems[m.id]?.getStringOrNull("name") ?: "?"
                w.write("  ID ${m.id} ($serverName): server=${m.serverValue} -> cache=${m.cacheValue}\n")
            }
            w.write("\n")
        }

        if (missingFromCache.isNotEmpty()) {
            w.write("MISSING FROM CACHE (custom server items?)\n")
            w.write("-".repeat(80) + "\n")
            for (id in missingFromCache.sorted()) {
                val serverName = serverItems[id]?.getStringOrNull("name") ?: "?"
                w.write("  ID $id ($serverName)\n")
            }
            w.write("\n")
        }
    }

    println("\nValidation report written to: ${reportFile.absolutePath}")

    // ── Generate merged items-validated.json ────────────────────────
    //
    // Strategy: for every server item, start from the cache base (if it exists),
    // then layer server-specific overrides on top.
    //
    // Cache-authoritative fields (replaced by cache values):
    //   name, stackable, value, members, interfaceActions, groundActions
    //
    // Server-override fields (preserved from server JSON):
    //   examine, tradeable, weight, highAlch, lowAlch, equipSlot, twoHanded,
    //   requirements, bonuses, and any other custom fields not in the cache.

    val cacheAuthoritativeFields = setOf(
        "name", "stackable", "value", "members", "interfaceActions", "groundActions"
    )

    val mergedArray = JsonArray()

    for ((id, serverObj) in serverItems.entries.sortedBy { it.key }) {
        val cacheObj = cacheItems[id]
        val merged = JsonObject()

        // Always start with the item ID
        merged.addProperty("id", id)

        if (cacheObj != null) {
            // Copy cache-authoritative fields from cache
            for (field in cacheAuthoritativeFields) {
                if (cacheObj.has(field)) {
                    merged.add(field, cacheObj.get(field).deepCopy())
                }
            }

            // Copy all server fields that are NOT cache-authoritative
            for ((key, value) in serverObj.entrySet()) {
                if (key == "id") continue
                if (key in cacheAuthoritativeFields) continue
                merged.add(key, value.deepCopy())
            }

            // Add noted/noteId from cache if present and meaningful
            if (cacheObj.has("noted")) {
                merged.add("noted", cacheObj.get("noted").deepCopy())
            }
            if (cacheObj.has("noteId") && cacheObj.get("noteId").asInt != -1) {
                merged.add("noteId", cacheObj.get("noteId").deepCopy())
            }

            // Add equipSlot from cache if server doesn't specify one but cache does
            if (!serverObj.has("equipSlot") && cacheObj.has("equipSlot") && cacheObj.get("equipSlot").asInt != -1) {
                merged.addProperty("equipSlot", cacheObj.get("equipSlot").asInt)
            }
        } else {
            // Item not in cache -- preserve all server data as-is
            for ((key, value) in serverObj.entrySet()) {
                if (key == "id") continue
                merged.add(key, value.deepCopy())
            }
        }

        mergedArray.add(merged)
    }

    val outputFile = projectDir.resolve("data/items/items-validated.json")
    Files.writeString(outputFile, gson.toJson(mergedArray))
    println("Merged items written to: $outputFile (${mergedArray.size()} items)")

    // ── Console summary ─────────────────────────────────────────────
    println("\nItem Validation Complete")
    println("  Name mismatches:      ${nameMismatches.size}")
    println("  Stackable mismatches: ${stackMismatches.size}")
    println("  Value mismatches:     ${valueMismatches.size}")
    println("  Missing from cache:   ${missingFromCache.size}")
}

// ════════════════════════════════════════════════════════════════════
//  Helpers
// ════════════════════════════════════════════════════════════════════

private data class MismatchEntry(
    val id: Int,
    val field: String,
    val serverValue: String,
    val cacheValue: String
)

/**
 * Loads a JSON array file of item objects and indexes them by "id".
 * Returns an immutable map.
 */
private fun loadItemsById(file: Path): Map<Int, JsonObject> {
    val content = Files.readString(file)
    val array = JsonParser.parseString(content).asJsonArray
    val result = mutableMapOf<Int, JsonObject>()
    for (element in array) {
        if (!element.isJsonObject) continue
        val obj = element.asJsonObject
        val id = obj.get("id")?.asInt ?: continue
        result[id] = obj
    }
    return result.toMap()
}

private fun JsonObject.getStringOrNull(key: String): String? =
    if (has(key) && get(key).isJsonPrimitive) get(key).asString else null

private fun JsonObject.getBoolOrNull(key: String): Boolean? =
    if (has(key) && get(key).isJsonPrimitive) get(key).asBoolean else null

private fun JsonObject.getIntOrNull(key: String): Int? =
    if (has(key) && get(key).isJsonPrimitive) get(key).asInt else null
