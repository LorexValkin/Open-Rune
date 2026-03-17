package com.openrune.core.world

import com.openrune.api.entity.PlayerRef
import com.openrune.api.plugin.PlayerRegistry
import org.slf4j.LoggerFactory

/**
 * Manages the array of online players (max 2048 as per 317 protocol).
 * Thread-safe for concurrent login/logout from network threads.
 */
class PlayerManager(private val maxPlayers: Int = 2048) : PlayerRegistry {

    private val log = LoggerFactory.getLogger(PlayerManager::class.java)
    private val players = arrayOfNulls<Player>(maxPlayers)
    private val lock = Any()

    @Volatile
    override var count: Int = 0
        private set

    /**
     * Register a new player and assign them an index.
     * Returns the assigned index, or -1 if the server is full.
     */
    fun register(player: Player): Int {
        synchronized(lock) {
            for (i in 1 until maxPlayers) {
                if (players[i] == null) {
                    player.index = i
                    players[i] = player
                    count++
                    log.info("Registered player: {} (index={}, online={})", player.name, i, count)
                    return i
                }
            }
        }
        log.warn("Server full, cannot register player: {}", player.name)
        return -1
    }

    /**
     * Remove a player by index.
     */
    fun unregister(index: Int) {
        synchronized(lock) {
            val player = players[index]
            if (player != null) {
                players[index] = null
                count--
                log.info("Unregistered player: {} (index={}, online={})", player.name, index, count)
            }
        }
    }

    /**
     * Get a player by index (internal use).
     */
    fun getByIndex(index: Int): Player? {
        if (index < 0 || index >= maxPlayers) return null
        return players[index]
    }

    /**
     * Check if a player name is already online.
     */
    fun isOnline(name: String): Boolean {
        val lower = name.lowercase()
        synchronized(lock) {
            for (p in players) {
                if (p != null && p.name.lowercase() == lower) return true
            }
        }
        return false
    }

    /**
     * Get all online players (non-null snapshot).
     */
    fun allPlayers(): List<Player> {
        synchronized(lock) {
            return players.filterNotNull()
        }
    }

    // === PlayerRegistry interface (for plugins) ===

    override fun find(name: String): PlayerRef? {
        val lower = name.lowercase()
        synchronized(lock) {
            return players.firstOrNull { it != null && it.name.lowercase() == lower }
        }
    }

    override fun findByIndex(index: Int): PlayerRef? = getByIndex(index)

    override fun iterator(): Iterator<PlayerRef> = allPlayers().iterator()
}
