package com.openrune.cache.tools

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Extracts all equippable items from the cache-exported items.json and produces:
 *
 *   1. data/equipment/equipment-cache.json  — full list of equippable items with
 *      id, name, slot, action, and twoHanded flag.
 *   2. ~/Desktop/equipment-report.txt       — per-slot totals and unclassified items.
 *
 * Slot assignment uses name-based heuristics because the 317 cache decoder does
 * not populate the equipSlot field (always -1).
 *
 * Usage: ./gradlew :cache:exportEquipment
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

    val content = Files.readString(cacheFile)
    val items = JsonParser.parseString(content).asJsonArray

    println("Loaded ${items.size()} total cache item definitions")

    // ── Filter equippable items ─────────────────────────────────────
    val equipActions = setOf("Wield", "Wear", "Equip")
    val equippableItems = mutableListOf<EquipEntry>()
    val unclassified = mutableListOf<EquipEntry>()

    for (element in items) {
        if (!element.isJsonObject) continue
        val obj = element.asJsonObject

        val id = obj.get("id")?.asInt ?: continue
        val name = obj.get("name")?.asString ?: continue
        val noted = obj.get("noted")?.asBoolean ?: false
        if (noted) continue // Skip noted variants

        // Check interfaceActions for Wield/Wear/Equip
        val ifActions = obj.getAsJsonArray("interfaceActions") ?: continue
        var action: String? = null
        for (a in ifActions) {
            if (a.isJsonNull) continue
            val actionStr = a.asString
            if (actionStr in equipActions) {
                action = actionStr
                break
            }
        }
        if (action == null) continue

        val slot = classifySlot(name, action)
        val twoHanded = isTwoHanded(name)

        val entry = EquipEntry(id, name, slot, action, twoHanded)

        if (slot == -1) {
            unclassified.add(entry)
        } else {
            equippableItems.add(entry)
        }
    }

    // Also add unclassified to the output (with slot -1) so nothing is lost
    val allEntries = (equippableItems + unclassified).sortedBy { it.id }

    println("Equippable items found: ${allEntries.size}")
    println("  Classified:   ${equippableItems.size}")
    println("  Unclassified: ${unclassified.size}")

    // ── Write equipment-cache.json ──────────────────────────────────
    val outputArray = JsonArray()
    for (entry in allEntries) {
        val obj = JsonObject()
        obj.addProperty("id", entry.id)
        obj.addProperty("name", entry.name)
        obj.addProperty("slot", entry.slot)
        obj.addProperty("action", entry.action)
        obj.addProperty("twoHanded", entry.twoHanded)
        outputArray.add(obj)
    }

    val outputFile = projectDir.resolve("data/equipment/equipment-cache.json")
    Files.createDirectories(outputFile.parent)
    Files.writeString(outputFile, gson.toJson(outputArray))
    println("\nEquipment data written to: $outputFile (${outputArray.size()} items)")

    // ── Write report ────────────────────────────────────────────────
    val slotNames = mapOf(
        0  to "Head",
        1  to "Cape",
        2  to "Amulet",
        3  to "Weapon",
        4  to "Body",
        5  to "Shield",
        7  to "Legs",
        9  to "Hands",
        10 to "Feet",
        12 to "Ring",
        13 to "Ammo",
        -1 to "Unclassified"
    )

    val slotCounts = allEntries.groupBy { it.slot }.mapValues { it.value.size }

    val reportFile = File(System.getProperty("user.home"), "Desktop/equipment-report.txt")
    reportFile.parentFile.mkdirs()

    reportFile.bufferedWriter().use { w ->
        w.write("OpenRune Equipment Export Report\n")
        w.write("=".repeat(80) + "\n\n")

        w.write("TOTAL EQUIPPABLE ITEMS: ${allEntries.size}\n")
        w.write("  Classified:   ${equippableItems.size}\n")
        w.write("  Unclassified: ${unclassified.size}\n\n")

        w.write("ITEMS PER SLOT\n")
        w.write("-".repeat(50) + "\n")
        for ((slotId, slotName) in slotNames.entries.sortedBy { it.key }) {
            val count = slotCounts[slotId] ?: 0
            if (count > 0) {
                w.write("  %-4d %-15s %d items\n".format(slotId, slotName, count))
            }
        }
        w.write("\n")

        // Two-handed weapons summary
        val twoHandedItems = allEntries.filter { it.twoHanded }
        w.write("TWO-HANDED WEAPONS: ${twoHandedItems.size}\n")
        w.write("-".repeat(50) + "\n")
        for (item in twoHandedItems.sortedBy { it.id }) {
            w.write("  ID %-6d %s\n".format(item.id, item.name))
        }
        w.write("\n")

        // Unclassified items
        if (unclassified.isNotEmpty()) {
            w.write("UNCLASSIFIED ITEMS (slot = -1)\n")
            w.write("-".repeat(80) + "\n")
            for (item in unclassified.sortedBy { it.id }) {
                w.write("  ID %-6d %-40s action=%s\n".format(item.id, item.name, item.action))
            }
            w.write("\n")
        }
    }

    println("Report written to: ${reportFile.absolutePath}")
}

// ════════════════════════════════════════════════════════════════════════
//  Data class
// ════════════════════════════════════════════════════════════════════════

private data class EquipEntry(
    val id: Int,
    val name: String,
    val slot: Int,
    val action: String,
    val twoHanded: Boolean
)

// ════════════════════════════════════════════════════════════════════════
//  Slot classification (name heuristics)
// ════════════════════════════════════════════════════════════════════════

// Slot constants matching EquipmentSlot in AppearanceBuilder.kt
private const val HEAD   = 0
private const val CAPE   = 1
private const val AMULET = 2
private const val WEAPON = 3
private const val BODY   = 4
private const val SHIELD = 5
private const val LEGS   = 7
private const val HANDS  = 9
private const val FEET   = 10
private const val RING   = 12
private const val AMMO   = 13

/**
 * Classify an item's equipment slot based on its name and interface action.
 * Returns -1 if no heuristic matches.
 *
 * The order of checks matters — more specific patterns come first to avoid
 * misclassification (e.g., "Robin hood hat" should match HEAD, not CAPE).
 */
private fun classifySlot(name: String, action: String): Int {
    val lower = name.lowercase()

    // Strip common suffixes for variant matching
    // (p), (p+), (p++), (t), (g), (or), (i) etc. are cosmetic variants
    val baseName = lower
        .replace(Regex("\\s*\\([^)]*\\)\\s*$"), "")
        .trim()

    // ── AMMO (slot 13) ──────────────────────────────────────────────
    if (matchesAny(baseName, AMMO_PATTERNS)) return AMMO

    // ── HEAD (slot 0) ───────────────────────────────────────────────
    if (matchesAny(baseName, HEAD_PATTERNS)) return HEAD

    // ── CAPE (slot 1) ───────────────────────────────────────────────
    if (matchesAny(baseName, CAPE_PATTERNS)) return CAPE

    // ── AMULET (slot 2) ─────────────────────────────────────────────
    if (matchesAny(baseName, AMULET_PATTERNS)) return AMULET

    // ── BODY (slot 4) — must check before WEAPON to avoid "chainbody" as weapon
    if (matchesAny(baseName, BODY_PATTERNS)) return BODY

    // ── SHIELD (slot 5) ─────────────────────────────────────────────
    if (matchesAny(baseName, SHIELD_PATTERNS)) return SHIELD

    // ── LEGS (slot 7) ───────────────────────────────────────────────
    if (matchesAny(baseName, LEGS_PATTERNS)) return LEGS

    // ── HANDS (slot 9) ──────────────────────────────────────────────
    if (matchesAny(baseName, HANDS_PATTERNS)) return HANDS

    // ── FEET (slot 10) ──────────────────────────────────────────────
    if (matchesAny(baseName, FEET_PATTERNS)) return FEET

    // ── RING (slot 12) ──────────────────────────────────────────────
    if (matchesAny(baseName, RING_PATTERNS)) return RING

    // ── WEAPON (slot 3) — broadest match, goes last ─────────────────
    if (action == "Wield") return WEAPON
    if (matchesAny(baseName, WEAPON_PATTERNS)) return WEAPON

    // ── Fallback: "Wear" items that didn't match any armor slot ─────
    // "Equip" items that didn't match ammo
    return -1
}

/**
 * Returns true if the name matches any of the given patterns.
 * Patterns can be:
 *   - Simple substring match (e.g., "helmet")
 *   - Regex pattern prefixed with "^" or containing special regex chars
 */
private fun matchesAny(name: String, patterns: List<String>): Boolean {
    for (pattern in patterns) {
        if (pattern.startsWith("^") || pattern.contains("$")) {
            if (Regex(pattern).containsMatchIn(name)) return true
        } else {
            if (name.contains(pattern)) return true
        }
    }
    return false
}

// ════════════════════════════════════════════════════════════════════════
//  Pattern lists for slot classification
// ════════════════════════════════════════════════════════════════════════

private val HEAD_PATTERNS = listOf(
    "helm", "hat", "hood", "mask", "coif", "tiara", "crown",
    "mitre", "beret", "headband", "bandana",
    "snelm", "fez", "turban", "cavalier", "boater",
    "sallet", "faceguard",
    "void melee", "void mage", "void ranger",
    "partyhat", "santa hat", "h'ween mask",
    "earmuffs", "facemask", "spiny helmet", "nosepeg",
    "goggles", "bearhead", "headdress",
    "robin hood hat", "third-age full helm", "third-age mage hat",
    "third-age range coif", "serpentine helm", "ancestral hat",
    "verac's helm", "dharok's helm", "guthan's helm", "torag's helm",
    "karil's coif", "ahrim's hood",
    "fighter hat", "healer hat", "ranger hat", "runner hat",
    "bomber cap", "sleeping cap", "neitiznot", "fremennik"
)

private val CAPE_PATTERNS = listOf(
    "cape", "cloak", "ava's", "accumulator", "attractor",
    "ardougne cloak", "fire cape", "infernal cape", "max cape",
    "legends cape", "obsidian cape", "team-"
)

private val AMULET_PATTERNS = listOf(
    "amulet", "necklace", "pendant", "stole",
    "symbol", "\\bscarf\\b",
    "gnome scarf", "holy symbol", "unholy symbol"
)

private val BODY_PATTERNS = listOf(
    "platebody", "chainbody", "chestplate", "robe top", "robetop",
    "torso", "hauberk", "brassard", "breastplate",
    "d'hide body", "dragonhide body", "leather body",
    "tunic", "shirt", "blouse", "jacket",
    "leathertop",
    "verac's brassard", "dharok's platebody", "guthan's platebody", "torag's platebody",
    "fighter torso", "bandos chestplate", "armadyl chestplate",
    "third-age platebody", "third-age robe top", "third-age range top",
    "ancestral robe top",
    "void knight top",
    "mystic robe top", "infinity top", "skeletal top",
    "initiate hauberk", "proselyte hauberk",
    "monk's robe top", "zamorak robe top", "shade robe top"
)

private val SHIELD_PATTERNS = listOf(
    "shield", "kiteshield", "sq shield", "defender",
    "buckler",
    "book of", "tome of",
    "toktz-ket-xil", "crystal shield",
    "dragonfire shield", "dragonfire ward", "anti-dragon shield",
    "spirit shield",
    "malediction ward", "odium ward",
    "unholy book", "holy book", "book of balance", "book of darkness",
    "book of law", "book of war"
)

private val LEGS_PATTERNS = listOf(
    "platelegs", "plateskirt", "chaps", "tassets",
    "skirt", "trousers", "pantaloons", "cuisse",
    "robe bottom", "robeskirt", "robe skirt",
    "d'hide chaps", "dragonhide chaps",
    "ahrim's robeskirt", "verac's plateskirt",
    "dharok's platelegs", "guthan's chainskirt", "torag's platelegs", "karil's leatherskirt",
    "bandos tassets", "armadyl chainskirt",
    "third-age platelegs", "third-age range legs",
    "ancestral robe bottom",
    "void knight robe",
    "mystic robe bottom", "infinity bottom", "skeletal bottoms",
    "monk's robe", "zamorak robe", "shade robe",
    "initiate cuisse", "proselyte cuisse", "proselyte tasset"
)

private val HANDS_PATTERNS = listOf(
    "gloves", "gauntlets", "vambraces", "vamb", "bracers",
    "bracelet", "brace",
    "^barrows gloves$",
    "combat bracelet", "regen bracelet",
    "void knight gloves"
)

private val FEET_PATTERNS = listOf(
    "boots", "shoes", "sandals", "slippers",
    "climbing boots", "ranger boots", "pegasian boots",
    "primordial boots", "eternal boots", "dragon boots",
    "infinity boots"
)

private val RING_PATTERNS = listOf(
    "^ring ", "ring$", " ring$", " ring ",
    "^ring of", "berserker ring", "warrior ring", "archers ring",
    "seers ring", "tyrannical ring", "treasonous ring",
    "eternal ring", "explorer's ring", "lunar ring"
)

private val AMMO_PATTERNS = listOf(
    "arrow", "bolt", "^dart", "dart$", " dart",
    "javelin", "throwing knife", "thrownaxe", "chinchompa",
    "broad bolt", "bolt rack",
    "^ogre arrow", "brutal arrow",
    "^blessing"
)

private val WEAPON_PATTERNS = listOf(
    "sword", "scimitar", "dagger", "mace", "warhammer",
    "battleaxe", "axe", "halberd", "spear", "hasta",
    "whip", "flail", "claw",
    "bow", "crossbow", "ballista",
    "staff", "wand", "trident",
    "godsword", "saradomin sword",
    "abyssal", "tentacle",
    "blowpipe", "toxic blowpipe",
    "maul", "longsword",
    "pickaxe"
)

// ════════════════════════════════════════════════════════════════════════
//  Two-handed detection
// ════════════════════════════════════════════════════════════════════════

/**
 * Returns true if the item is two-handed based on name heuristics.
 *
 * Two-handed weapons in OSRS/317:
 *   - 2h swords, godswords, halberds, spears, claws
 *   - Bows (NOT crossbows), ballistas
 *   - Mauls (granite maul, elder maul)
 *   - Some staves (Slayer's staff(e), trident, toxic staff)
 *   - Dark bow, magic comp bow
 *   - Dharok's greataxe
 *   - Crystal bow, crystal halberd
 */
private fun isTwoHanded(name: String): Boolean {
    val lower = name.lowercase()
    val baseName = lower.replace(Regex("\\s*\\([^)]*\\)\\s*$"), "").trim()

    // Explicit two-handed patterns
    if (matchesAny(baseName, TWO_HANDED_PATTERNS)) return true

    // Bows are 2H, but crossbows are 1H
    if (baseName.contains("bow") && !baseName.contains("crossbow")) return true

    return false
}

private val TWO_HANDED_PATTERNS = listOf(
    "2h sword", "two-handed",
    "godsword",
    "halberd",
    "spear",      // spears are 2h (hastas are 1h but won't match "spear")
    "claws",
    "maul",
    "ballista",
    "dharok's greataxe",
    "crystal bow", "crystal halberd",
    "dark bow",
    "magic comp bow",
    "chinchompa",
    "salamander",
    "bulwark",
    "saradomin sword",
    "trident",
    "toxic staff",
    "karil's crossbow" // 2h in 317 revision
)
