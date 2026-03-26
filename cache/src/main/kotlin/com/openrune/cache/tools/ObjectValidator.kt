package com.openrune.cache.tools

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Cross-references server tree and rock definitions against the cache-exported objects.json,
 * then generates cooking and smithing data files from the cache exports.
 *
 * Produces:
 *   1. A validation report at ~/Desktop/object-validation.txt
 *   2. data/cooking/cooking-objects.json   (ranges, fires, etc. with "Cook" action)
 *   3. data/cooking/cookable-items.json    (raw->cooked item mappings with levels/xp)
 *   4. data/smithing/furnaces.json         (furnace objects with "Smelt" action)
 *   5. data/smithing/anvils.json           (anvil objects with "Smith" action)
 *   6. data/smithing/smelting.json         (ore->bar smelting recipes)
 *
 * Usage: ./gradlew :cache:validateObjects
 */
fun main() {
    val projectDir = Path.of("C:\\Users\\User\\IdeaProjects\\Open-Rune")
    val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    // ── Load cache objects ────────────────────────────────────────
    val cacheObjectsFile = projectDir.resolve("cache/data/cache-export/objects.json")
    if (!Files.exists(cacheObjectsFile)) {
        println("ERROR: Cache export not found at $cacheObjectsFile")
        println("Run ./gradlew :cache:exportCache first.")
        return
    }
    val cacheObjects = loadObjectsById(cacheObjectsFile)
    println("Loaded ${cacheObjects.size} cache object definitions")

    // ── Load cache items ──────────────────────────────────────────
    val cacheItemsFile = projectDir.resolve("cache/data/cache-export/items.json")
    if (!Files.exists(cacheItemsFile)) {
        println("ERROR: Cache items export not found at $cacheItemsFile")
        println("Run ./gradlew :cache:exportCache first.")
        return
    }
    val cacheItems = loadObjectsById(cacheItemsFile)
    println("Loaded ${cacheItems.size} cache item definitions")

    // Build item lookup by name (lowercase) -> list of (id, obj)
    val itemsByName = buildItemNameIndex(cacheItems)

    // ── Load server trees ─────────────────────────────────────────
    val treesFile = projectDir.resolve("data/trees/trees.json")
    val treeDefs = if (Files.exists(treesFile)) loadJsonArray(treesFile) else emptyList()
    println("Loaded ${treeDefs.size} tree definitions")

    // ── Load server rocks ─────────────────────────────────────────
    val rocksFile = projectDir.resolve("data/rocks/rocks.json")
    val rockDefs = if (Files.exists(rocksFile)) loadJsonArray(rocksFile) else emptyList()
    println("Loaded ${rockDefs.size} rock definitions")

    // ══════════════════════════════════════════════════════════════
    // STEP 1: Validate trees
    // ══════════════════════════════════════════════════════════════
    val treeResults = validateSkillObjects(
        defs = treeDefs,
        cacheObjects = cacheObjects,
        expectedAction = "Chop down",
        defType = "Tree"
    )

    // ══════════════════════════════════════════════════════════════
    // STEP 2: Validate rocks
    // ══════════════════════════════════════════════════════════════
    val rockResults = validateSkillObjects(
        defs = rockDefs,
        cacheObjects = cacheObjects,
        expectedAction = "Mine",
        defType = "Rock"
    )

    // ══════════════════════════════════════════════════════════════
    // STEP 3: Build cooking data
    // ══════════════════════════════════════════════════════════════
    val cookingObjects = findObjectsWithAction(cacheObjects, "Cook")
    val cookingObjectsArray = buildCookingObjectsJson(cookingObjects)

    val cookableItems = buildCookableItemsJson(cacheItems, itemsByName)

    val cookingDir = projectDir.resolve("data/cooking")
    Files.createDirectories(cookingDir)
    Files.writeString(cookingDir.resolve("cooking-objects.json"), gson.toJson(cookingObjectsArray))
    println("Wrote ${cookingObjectsArray.size()} cooking objects to data/cooking/cooking-objects.json")
    Files.writeString(cookingDir.resolve("cookable-items.json"), gson.toJson(cookableItems))
    println("Wrote ${cookableItems.size()} cookable items to data/cooking/cookable-items.json")

    // ══════════════════════════════════════════════════════════════
    // STEP 4: Build smithing data
    // ══════════════════════════════════════════════════════════════
    val furnaceObjects = findObjectsWithAction(cacheObjects, "Smelt")
    val furnacesArray = buildSmithingObjectsJson(furnaceObjects)

    val anvilObjects = findObjectsByNameAndAction(cacheObjects, "Anvil", "Smith")
    val anvilsArray = buildSmithingObjectsJson(anvilObjects)

    val smeltingRecipes = buildSmeltingRecipesJson(cacheItems, itemsByName)

    val smithingDir = projectDir.resolve("data/smithing")
    Files.createDirectories(smithingDir)
    Files.writeString(smithingDir.resolve("furnaces.json"), gson.toJson(furnacesArray))
    println("Wrote ${furnacesArray.size()} furnace objects to data/smithing/furnaces.json")
    Files.writeString(smithingDir.resolve("anvils.json"), gson.toJson(anvilsArray))
    println("Wrote ${anvilsArray.size()} anvil objects to data/smithing/anvils.json")
    Files.writeString(smithingDir.resolve("smelting.json"), gson.toJson(smeltingRecipes))
    println("Wrote ${smeltingRecipes.size()} smelting recipes to data/smithing/smelting.json")

    // ══════════════════════════════════════════════════════════════
    // STEP 5: Write validation report
    // ══════════════════════════════════════════════════════════════
    val reportFile = File(System.getProperty("user.home"), "Desktop/object-validation.txt")
    reportFile.parentFile.mkdirs()

    reportFile.bufferedWriter().use { w ->
        w.write("OpenRune Object Validation Report\n")
        w.write("=".repeat(100) + "\n")
        w.write("Cache objects: ${cacheObjects.size}\n")
        w.write("Cache items: ${cacheItems.size}\n")
        w.write("Tree definitions: ${treeDefs.size}\n")
        w.write("Rock definitions: ${rockDefs.size}\n\n")

        // ── Tree validation ───────────────────────────────────────
        w.write("TREE VALIDATION\n")
        w.write("-".repeat(80) + "\n")
        writeValidationResults(w, treeResults, "Tree")

        // ── Rock validation ───────────────────────────────────────
        w.write("\nROCK VALIDATION\n")
        w.write("-".repeat(80) + "\n")
        writeValidationResults(w, rockResults, "Rock")

        // ── Cooking summary ───────────────────────────────────────
        w.write("\nCOOKING DATA GENERATED\n")
        w.write("-".repeat(80) + "\n")
        w.write("  Cooking objects (ranges/fires with 'Cook' action): ${cookingObjectsArray.size()}\n")
        for (i in 0 until cookingObjectsArray.size()) {
            val obj = cookingObjectsArray[i].asJsonObject
            w.write("    ID ${obj.get("id").asInt}: ${obj.get("name").asString}\n")
        }
        w.write("  Cookable items: ${cookableItems.size()}\n")
        for (i in 0 until cookableItems.size()) {
            val item = cookableItems[i].asJsonObject
            w.write("    ${item.get("rawName").asString} (${item.get("rawItemId").asInt})")
            w.write(" -> ${item.get("cookedName").asString} (${item.get("cookedItemId").asInt})")
            w.write(" | Level ${item.get("level").asInt}, XP ${item.get("xp").asDouble}\n")
        }

        // ── Smithing summary ──────────────────────────────────────
        w.write("\nSMITHING DATA GENERATED\n")
        w.write("-".repeat(80) + "\n")
        w.write("  Furnaces (with 'Smelt' action): ${furnacesArray.size()}\n")
        for (i in 0 until furnacesArray.size()) {
            val obj = furnacesArray[i].asJsonObject
            w.write("    ID ${obj.get("id").asInt}: ${obj.get("name").asString}\n")
        }
        w.write("  Anvils (with 'Smith' action): ${anvilsArray.size()}\n")
        for (i in 0 until anvilsArray.size()) {
            val obj = anvilsArray[i].asJsonObject
            w.write("    ID ${obj.get("id").asInt}: ${obj.get("name").asString}\n")
        }
        w.write("  Smelting recipes: ${smeltingRecipes.size()}\n")
        for (i in 0 until smeltingRecipes.size()) {
            val recipe = smeltingRecipes[i].asJsonObject
            val ores = recipe.getAsJsonArray("requiredOres")
            val oreDesc = (0 until ores.size()).joinToString(" + ") { idx ->
                val ore = ores[idx].asJsonObject
                "${ore.get("amount").asInt}x ${ore.get("name").asString}"
            }
            w.write("    ${recipe.get("barName").asString} (${recipe.get("barId").asInt})")
            w.write(" <- $oreDesc")
            w.write(" | Level ${recipe.get("level").asInt}, XP ${recipe.get("xp").asDouble}\n")
        }
    }

    println("\nValidation report written to: ${reportFile.absolutePath}")

    // ── Console summary ───────────────────────────────────────────
    println("\n${"=".repeat(60)}")
    println("Object Validation Complete")
    println("${"=".repeat(60)}")
    println("TREES:  ${treeResults.matched} matched, ${treeResults.nameMismatches.size} name mismatches, " +
            "${treeResults.actionMismatches.size} action mismatches, ${treeResults.missing.size} missing from cache")
    println("ROCKS:  ${rockResults.matched} matched, ${rockResults.nameMismatches.size} name mismatches, " +
            "${rockResults.actionMismatches.size} action mismatches, ${rockResults.missing.size} missing from cache")
    println("COOKING: ${cookingObjectsArray.size()} objects, ${cookableItems.size()} cookable items")
    println("SMITHING: ${furnacesArray.size()} furnaces, ${anvilsArray.size()} anvils, ${smeltingRecipes.size()} smelting recipes")
}

// ════════════════════════════════════════════════════════════════════
//  Validation logic
// ════════════════════════════════════════════════════════════════════

private data class ValidationResults(
    val matched: Int,
    val nameMismatches: List<ObjectMismatch>,
    val actionMismatches: List<ObjectMismatch>,
    val missing: List<MissingObject>,
    val duplicateIds: List<Int>
)

private data class ObjectMismatch(
    val id: Int,
    val serverValue: String,
    val cacheValue: String
)

private data class MissingObject(
    val id: Int,
    val serverName: String
)

private fun validateSkillObjects(
    defs: List<JsonObject>,
    cacheObjects: Map<Int, JsonObject>,
    expectedAction: String,
    defType: String
): ValidationResults {
    val nameMismatches = mutableListOf<ObjectMismatch>()
    val actionMismatches = mutableListOf<ObjectMismatch>()
    val missing = mutableListOf<MissingObject>()
    var matched = 0

    // Check for duplicate IDs
    val idCounts = defs.groupingBy { it.get("id").asInt }.eachCount()
    val duplicateIds = idCounts.filter { it.value > 1 }.keys.toList().sorted()

    for (def in defs) {
        val id = def.get("id").asInt
        val serverName = def.get("name")?.asString ?: ""
        val cacheObj = cacheObjects[id]

        if (cacheObj == null) {
            missing.add(MissingObject(id, serverName))
            continue
        }

        val cacheName = cacheObj.get("name")?.asString ?: ""
        val cacheActions = extractActions(cacheObj)

        // Name comparison: cache names for rocks are generic ("Rocks") and trees
        // may differ ("Willow" vs "Willow tree"), so we check containment
        val nameMatches = cacheName.equals(serverName, ignoreCase = true) ||
                serverName.contains(cacheName, ignoreCase = true) ||
                cacheName.contains(serverName, ignoreCase = true)

        if (!nameMatches && cacheName.isNotBlank()) {
            nameMismatches.add(ObjectMismatch(id, serverName, cacheName))
        }

        if (expectedAction !in cacheActions) {
            val actionsStr = cacheActions.joinToString(", ").ifEmpty { "<none>" }
            actionMismatches.add(ObjectMismatch(id, expectedAction, actionsStr))
        } else {
            matched++
        }
    }

    return ValidationResults(matched, nameMismatches, actionMismatches, missing, duplicateIds)
}

private fun writeValidationResults(
    w: java.io.BufferedWriter,
    results: ValidationResults,
    defType: String
) {
    w.write("  Matched (correct action): ${results.matched}\n")
    w.write("  Name mismatches: ${results.nameMismatches.size}\n")
    w.write("  Action mismatches: ${results.actionMismatches.size}\n")
    w.write("  Missing from cache: ${results.missing.size}\n")
    w.write("  Duplicate IDs in server data: ${results.duplicateIds.size}\n\n")

    if (results.duplicateIds.isNotEmpty()) {
        w.write("  DUPLICATE IDS:\n")
        for (id in results.duplicateIds) {
            w.write("    ID $id appears multiple times in server data\n")
        }
        w.write("\n")
    }

    if (results.nameMismatches.isNotEmpty()) {
        w.write("  NAME MISMATCHES:\n")
        for (m in results.nameMismatches.sortedBy { it.id }) {
            w.write("    ID ${m.id}: server=\"${m.serverValue}\" vs cache=\"${m.cacheValue}\"\n")
        }
        w.write("\n")
    }

    if (results.actionMismatches.isNotEmpty()) {
        w.write("  ACTION MISMATCHES (expected action not found in cache):\n")
        for (m in results.actionMismatches.sortedBy { it.id }) {
            w.write("    ID ${m.id}: expected=\"${m.serverValue}\", cache actions=[${m.cacheValue}]\n")
        }
        w.write("\n")
    }

    if (results.missing.isNotEmpty()) {
        w.write("  MISSING FROM CACHE:\n")
        for (m in results.missing.sortedBy { it.id }) {
            w.write("    ID ${m.id} (${m.serverName})\n")
        }
        w.write("\n")
    }
}

// ════════════════════════════════════════════════════════════════════
//  Cooking data generation
// ════════════════════════════════════════════════════════════════════

private fun buildCookingObjectsJson(objects: List<Pair<Int, JsonObject>>): JsonArray {
    val array = JsonArray()
    for ((id, obj) in objects.sortedBy { it.first }) {
        val entry = JsonObject()
        entry.addProperty("id", id)
        entry.addProperty("name", obj.get("name")?.asString ?: "")
        entry.add("actions", extractActionsArray(obj))
        array.add(entry)
    }
    return array
}

private fun buildCookableItemsJson(
    cacheItems: Map<Int, JsonObject>,
    itemsByName: Map<String, List<Pair<Int, JsonObject>>>
): JsonArray {
    // Known cookable items: raw name -> (cooked name, level, xp, burnStopLevel)
    data class CookingRecipe(
        val rawName: String,
        val cookedName: String,
        val level: Int,
        val xp: Double,
        val burnLevel: Int
    )

    val recipes = listOf(
        CookingRecipe("Raw shrimps", "Shrimps", 1, 30.0, 34),
        CookingRecipe("Raw sardine", "Sardine", 1, 40.0, 38),
        CookingRecipe("Raw herring", "Herring", 5, 50.0, 41),
        CookingRecipe("Raw anchovies", "Anchovies", 1, 30.0, 34),
        CookingRecipe("Raw mackerel", "Mackerel", 10, 60.0, 45),
        CookingRecipe("Raw trout", "Trout", 15, 70.0, 49),
        CookingRecipe("Raw cod", "Cod", 18, 75.0, 51),
        CookingRecipe("Raw pike", "Pike", 20, 80.0, 53),
        CookingRecipe("Raw salmon", "Salmon", 25, 90.0, 58),
        CookingRecipe("Raw tuna", "Tuna", 30, 100.0, 63),
        CookingRecipe("Raw lobster", "Lobster", 40, 120.0, 74),
        CookingRecipe("Raw bass", "Bass", 43, 130.0, 80),
        CookingRecipe("Raw swordfish", "Swordfish", 45, 140.0, 86),
        CookingRecipe("Raw monkfish", "Monkfish", 62, 150.0, 90),
        CookingRecipe("Raw shark", "Shark", 80, 210.0, 94),
        CookingRecipe("Raw sea turtle", "Sea turtle", 82, 211.3, 99),
        CookingRecipe("Raw manta ray", "Manta ray", 91, 216.3, 99),
        CookingRecipe("Raw anglerfish", "Anglerfish", 84, 230.0, 99),
        CookingRecipe("Raw beef", "Cooked meat", 1, 30.0, 31),
        CookingRecipe("Raw rat meat", "Cooked meat", 1, 30.0, 31),
        CookingRecipe("Raw bear meat", "Cooked meat", 1, 30.0, 31),
        CookingRecipe("Raw chicken", "Cooked chicken", 1, 30.0, 31),
        CookingRecipe("Raw lava eel", "Lava eel", 53, 30.0, 53),
        CookingRecipe("Raw ugthanki meat", "Ugthanki meat", 1, 40.0, 99)
    )

    val array = JsonArray()

    for (recipe in recipes) {
        val rawNameLower = recipe.rawName.lowercase()
        val cookedNameLower = recipe.cookedName.lowercase()

        // Find raw item ID (non-noted)
        val rawCandidates = itemsByName[rawNameLower] ?: continue
        val rawItem = rawCandidates.firstOrNull { (_, obj) ->
            obj.get("noted")?.asBoolean != true
        } ?: continue

        // Find cooked item ID (non-noted)
        val cookedCandidates = itemsByName[cookedNameLower] ?: continue
        val cookedItem = cookedCandidates.firstOrNull { (_, obj) ->
            obj.get("noted")?.asBoolean != true
        } ?: continue

        // Find burnt version name
        val burntName = "Burnt ${recipe.cookedName.lowercase()}"
        val burntNameFish = "Burnt fish"
        val burntNameMeat = "Burnt meat"
        val burntNameChicken = "Burnt chicken"

        val burntCandidates = itemsByName[burntName]
            ?: itemsByName[burntNameFish]
            ?: itemsByName[burntNameMeat]
            ?: itemsByName[burntNameChicken]
        val burntId = burntCandidates?.firstOrNull { (_, obj) ->
            obj.get("noted")?.asBoolean != true
        }?.first ?: -1

        val entry = JsonObject()
        entry.addProperty("rawItemId", rawItem.first)
        entry.addProperty("rawName", recipe.rawName)
        entry.addProperty("cookedItemId", cookedItem.first)
        entry.addProperty("cookedName", recipe.cookedName)
        entry.addProperty("burntItemId", burntId)
        entry.addProperty("level", recipe.level)
        entry.addProperty("xp", recipe.xp)
        entry.addProperty("burnStopLevel", recipe.burnLevel)
        array.add(entry)
    }

    return array
}

// ════════════════════════════════════════════════════════════════════
//  Smithing data generation
// ════════════════════════════════════════════════════════════════════

private fun buildSmithingObjectsJson(objects: List<Pair<Int, JsonObject>>): JsonArray {
    val array = JsonArray()
    for ((id, obj) in objects.sortedBy { it.first }) {
        val entry = JsonObject()
        entry.addProperty("id", id)
        entry.addProperty("name", obj.get("name")?.asString ?: "")
        entry.add("actions", extractActionsArray(obj))
        array.add(entry)
    }
    return array
}

private fun buildSmeltingRecipesJson(
    cacheItems: Map<Int, JsonObject>,
    itemsByName: Map<String, List<Pair<Int, JsonObject>>>
): JsonArray {
    data class OreRequirement(val name: String, val amount: Int)
    data class SmeltingRecipe(
        val barName: String,
        val ores: List<OreRequirement>,
        val level: Int,
        val xp: Double
    )

    val recipes = listOf(
        SmeltingRecipe("Bronze bar", listOf(OreRequirement("Copper ore", 1), OreRequirement("Tin ore", 1)), 1, 6.2),
        SmeltingRecipe("Iron bar", listOf(OreRequirement("Iron ore", 1)), 15, 12.5),
        SmeltingRecipe("Silver bar", listOf(OreRequirement("Silver ore", 1)), 20, 13.7),
        SmeltingRecipe("Steel bar", listOf(OreRequirement("Iron ore", 1), OreRequirement("Coal", 2)), 30, 17.5),
        SmeltingRecipe("Gold bar", listOf(OreRequirement("Gold ore", 1)), 40, 22.5),
        SmeltingRecipe("Mithril bar", listOf(OreRequirement("Mithril ore", 1), OreRequirement("Coal", 4)), 50, 30.0),
        SmeltingRecipe("Adamantite bar", listOf(OreRequirement("Adamantite ore", 1), OreRequirement("Coal", 6)), 70, 37.5),
        SmeltingRecipe("Runite bar", listOf(OreRequirement("Runite ore", 1), OreRequirement("Coal", 8)), 85, 50.0)
    )

    val array = JsonArray()

    for (recipe in recipes) {
        val barNameLower = recipe.barName.lowercase()
        val barCandidates = itemsByName[barNameLower] ?: continue
        val barItem = barCandidates.firstOrNull { (_, obj) ->
            obj.get("noted")?.asBoolean != true
        } ?: continue

        val oresArray = JsonArray()
        var allOresFound = true

        for (oreReq in recipe.ores) {
            val oreNameLower = oreReq.name.lowercase()
            val oreCandidates = itemsByName[oreNameLower]
            val oreItem = oreCandidates?.firstOrNull { (_, obj) ->
                obj.get("noted")?.asBoolean != true
            }

            if (oreItem == null) {
                allOresFound = false
                break
            }

            val oreEntry = JsonObject()
            oreEntry.addProperty("id", oreItem.first)
            oreEntry.addProperty("name", oreReq.name)
            oreEntry.addProperty("amount", oreReq.amount)
            oresArray.add(oreEntry)
        }

        if (!allOresFound) continue

        val entry = JsonObject()
        entry.addProperty("barId", barItem.first)
        entry.addProperty("barName", recipe.barName)
        entry.add("requiredOres", oresArray)
        entry.addProperty("level", recipe.level)
        entry.addProperty("xp", recipe.xp)
        array.add(entry)
    }

    return array
}

// ════════════════════════════════════════════════════════════════════
//  Search helpers
// ════════════════════════════════════════════════════════════════════

private fun findObjectsWithAction(
    cacheObjects: Map<Int, JsonObject>,
    action: String
): List<Pair<Int, JsonObject>> {
    return cacheObjects.entries
        .filter { (_, obj) -> action in extractActions(obj) }
        .map { (id, obj) -> id to obj }
}

private fun findObjectsByNameAndAction(
    cacheObjects: Map<Int, JsonObject>,
    nameContains: String,
    action: String
): List<Pair<Int, JsonObject>> {
    return cacheObjects.entries
        .filter { (_, obj) ->
            val name = obj.get("name")?.asString ?: ""
            name.contains(nameContains, ignoreCase = true) && action in extractActions(obj)
        }
        .map { (id, obj) -> id to obj }
}

// ════════════════════════════════════════════════════════════════════
//  JSON helpers
// ════════════════════════════════════════════════════════════════════

private fun loadObjectsById(file: Path): Map<Int, JsonObject> {
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

private fun loadJsonArray(file: Path): List<JsonObject> {
    val content = Files.readString(file)
    val array = JsonParser.parseString(content).asJsonArray
    return array.mapNotNull { if (it.isJsonObject) it.asJsonObject else null }
}

private fun extractActions(obj: JsonObject): List<String> {
    val actions = obj.getAsJsonArray("actions") ?: return emptyList()
    return actions.mapNotNull { el ->
        if (el.isJsonNull) null else el.asString.takeIf { it.isNotBlank() }
    }
}

private fun extractActionsArray(obj: JsonObject): JsonArray {
    val result = JsonArray()
    val actions = extractActions(obj)
    for (action in actions) {
        result.add(action)
    }
    return result
}

private fun buildItemNameIndex(
    cacheItems: Map<Int, JsonObject>
): Map<String, List<Pair<Int, JsonObject>>> {
    val index = mutableMapOf<String, MutableList<Pair<Int, JsonObject>>>()
    for ((id, obj) in cacheItems) {
        val name = obj.get("name")?.asString ?: continue
        if (name.isBlank() || name == "null") continue
        val key = name.lowercase()
        index.getOrPut(key) { mutableListOf() }.add(id to obj)
    }
    return index.mapValues { it.value.toList() }
}
