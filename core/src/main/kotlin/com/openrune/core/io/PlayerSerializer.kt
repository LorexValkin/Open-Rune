package com.openrune.core.io

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.openrune.api.entity.PlayerRights
import com.openrune.api.world.Position
import com.openrune.core.world.Player
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

/**
 * Saves and loads player data as JSON files.
 *
 * File format includes a version number for forward compatibility.
 * When the format changes, migration logic can be added based on version.
 *
 * Files are stored at: data/saves/{username}.json
 */
class PlayerSerializer(private val saveDir: Path) {

    private val log = LoggerFactory.getLogger(PlayerSerializer::class.java)
    private val gson = GsonBuilder().setPrettyPrinting().create()

    companion object {
        const val SAVE_VERSION = 1
    }

    init {
        if (!Files.exists(saveDir)) {
            Files.createDirectories(saveDir)
        }
    }

    /**
     * Load result: NEW (no save file), SUCCESS, or INVALID_PASSWORD.
     */
    enum class LoadResult { NEW, SUCCESS, INVALID_PASSWORD }

    /**
     * Attempt to load a player's save file and validate their password.
     * Returns the load result. If successful, the player object is populated.
     */
    fun load(player: Player, password: String): LoadResult {
        val file = saveDir.resolve("${player.name.lowercase()}.json")

        if (!Files.exists(file)) {
            log.info("New player: {}", player.name)
            return LoadResult.NEW
        }

        return try {
            val content = Files.readString(file)
            val root = JsonParser.parseString(content).asJsonObject
            val version = root.get("version")?.asInt ?: 0

            // Validate password
            val savedPassword = root.get("password")?.asString ?: ""
            if (savedPassword != password && savedPassword.isNotEmpty()) {
                return LoadResult.INVALID_PASSWORD
            }

            // Load based on version (allows migration)
            when {
                version >= 1 -> loadV1(player, root)
                else -> log.warn("Unknown save version {} for {}", version, player.name)
            }

            LoadResult.SUCCESS
        } catch (e: Exception) {
            log.error("Failed to load save for {}", player.name, e)
            LoadResult.INVALID_PASSWORD
        }
    }

    private fun loadV1(player: Player, root: JsonObject) {
        // Rights
        player.rights = PlayerRights.fromValue(root.get("rights")?.asInt ?: 0)

        // Position
        root.getAsJsonObject("position")?.let { pos ->
            player.position = Position(
                pos.get("x")?.asInt ?: 3222,
                pos.get("y")?.asInt ?: 3218,
                pos.get("z")?.asInt ?: 0
            )
        }

        // Skills
        root.getAsJsonArray("skills")?.let { skills ->
            for (i in 0 until minOf(skills.size(), player.levels.size)) {
                val skill = skills[i].asJsonObject
                player.levels[i] = skill.get("level")?.asInt ?: 1
                player.experience[i] = skill.get("experience")?.asDouble ?: 0.0
            }
        }

        // Current health
        player.currentHealth = root.get("currentHealth")?.asInt ?: player.getLevel(3)

        // Appearance
        root.getAsJsonArray("appearance")?.let { arr ->
            for (i in 0 until minOf(arr.size(), player.appearance.size)) {
                player.appearance[i] = arr[i].asInt
            }
        }

        root.getAsJsonArray("colors")?.let { arr ->
            for (i in 0 until minOf(arr.size(), player.colors.size)) {
                player.colors[i] = arr[i].asInt
            }
        }

        player.gender = root.get("gender")?.asInt ?: 0

        // Inventory
        root.getAsJsonArray("inventory")?.let { inv ->
            for (i in 0 until minOf(inv.size(), player.inventoryItems.size)) {
                val item = inv[i].asJsonObject
                player.inventoryItems[i] = item.get("id")?.asInt ?: -1
                player.inventoryAmounts[i] = item.get("amount")?.asInt ?: 0
            }
        }

        // Equipment
        root.getAsJsonArray("equipment")?.let { equip ->
            for (i in 0 until minOf(equip.size(), player.equipment.size)) {
                val item = equip[i].asJsonObject
                player.equipment[i] = item.get("id")?.asInt ?: -1
                player.equipmentAmounts[i] = item.get("amount")?.asInt ?: 0
            }
        }

        // Bank
        root.getAsJsonArray("bank")?.let { bank ->
            for (i in 0 until minOf(bank.size(), player.bankItems.size)) {
                val item = bank[i].asJsonObject
                player.bankItems[i] = item.get("id")?.asInt ?: -1
                player.bankAmounts[i] = item.get("amount")?.asInt ?: 0
            }
        }

        // Friends
        root.getAsJsonArray("friends")?.let { friends ->
            for (i in 0 until minOf(friends.size(), player.friends.size)) {
                player.friends[i] = friends[i].asLong
            }
        }
    }

    /**
     * Save a player's current state to their JSON file.
     */
    fun save(player: Player): Boolean {
        return try {
            val root = JsonObject()
            root.addProperty("version", SAVE_VERSION)
            root.addProperty("username", player.name)
            root.addProperty("password", player.password)
            root.addProperty("rights", player.rights.value)
            root.addProperty("currentHealth", player.currentHealth)
            root.addProperty("gender", player.gender)

            // Position
            val pos = JsonObject()
            pos.addProperty("x", player.position.x)
            pos.addProperty("y", player.position.y)
            pos.addProperty("z", player.position.z)
            root.add("position", pos)

            // Skills
            val skills = com.google.gson.JsonArray()
            for (i in player.levels.indices) {
                val skill = JsonObject()
                skill.addProperty("level", player.levels[i])
                skill.addProperty("experience", player.experience[i])
                skills.add(skill)
            }
            root.add("skills", skills)

            // Appearance
            val appearance = com.google.gson.JsonArray()
            for (v in player.appearance) appearance.add(v)
            root.add("appearance", appearance)

            val colors = com.google.gson.JsonArray()
            for (v in player.colors) colors.add(v)
            root.add("colors", colors)

            // Inventory (only occupied slots)
            val inventory = com.google.gson.JsonArray()
            for (i in player.inventoryItems.indices) {
                val item = JsonObject()
                item.addProperty("id", player.inventoryItems[i])
                item.addProperty("amount", player.inventoryAmounts[i])
                inventory.add(item)
            }
            root.add("inventory", inventory)

            // Equipment
            val equip = com.google.gson.JsonArray()
            for (i in player.equipment.indices) {
                val item = JsonObject()
                item.addProperty("id", player.equipment[i])
                item.addProperty("amount", player.equipmentAmounts[i])
                equip.add(item)
            }
            root.add("equipment", equip)

            // Bank (only occupied slots)
            val bank = com.google.gson.JsonArray()
            for (i in player.bankItems.indices) {
                if (player.bankItems[i] != -1) {
                    val item = JsonObject()
                    item.addProperty("id", player.bankItems[i])
                    item.addProperty("amount", player.bankAmounts[i])
                    bank.add(item)
                }
            }
            root.add("bank", bank)

            // Friends
            val friends = com.google.gson.JsonArray()
            for (f in player.friends) {
                if (f != 0L) friends.add(f)
            }
            root.add("friends", friends)

            // Write atomically (write to temp, then rename)
            val file = saveDir.resolve("${player.name.lowercase()}.json")
            val temp = saveDir.resolve("${player.name.lowercase()}.json.tmp")
            Files.writeString(temp, gson.toJson(root))
            Files.move(temp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE)

            true
        } catch (e: Exception) {
            log.error("Failed to save player: {}", player.name, e)
            false
        }
    }
}
