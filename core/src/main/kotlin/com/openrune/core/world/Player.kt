package com.openrune.core.world

import com.openrune.api.entity.PlayerRef
import com.openrune.api.entity.PlayerRights
import com.openrune.api.entity.Skills
import com.openrune.api.world.Position
import com.openrune.core.net.codec.GamePacketEncoder
import com.openrune.core.net.codec.IncomingPacket
import com.openrune.core.net.codec.IsaacCipher
import com.openrune.core.net.codec.PacketBuilder
import com.openrune.core.world.collision.Direction
import com.openrune.core.world.movement.MovementProcessor
import com.openrune.core.world.movement.WalkingQueue
import io.netty.channel.Channel
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Server-side player entity. Implements [PlayerRef] for plugin access
 * and [MovementProcessor.Movable] for engine-level movement.
 *
 * ENGINE-LEVEL class. Plugins interact via the [PlayerRef] interface only.
 */
class Player(
    override val name: String,
    var password: String,
    override var index: Int = -1,
    val channel: Channel,
    private val outCipher: IsaacCipher
) : PlayerRef, MovementProcessor.Movable {

    private val log = LoggerFactory.getLogger(Player::class.java)
    private val encoder = GamePacketEncoder(outCipher)

    // ================================================================
    //  Position & Region
    // ================================================================
    override var position: Position = Position(3222, 3218, 0)
    override val regionId: Int get() = position.regionId
    var lastRegion: Position = position
    var needsRegionUpdate: Boolean = true

    // ================================================================
    //  Identity
    // ================================================================
    override var rights: PlayerRights = PlayerRights.PLAYER
    var uid: Int = 0

    // ================================================================
    //  Movement (ENGINE-LEVEL, not pluggable)
    // ================================================================
    override val walkingQueue = WalkingQueue()
    override val entitySize: Int = 1
    override val isPlayer: Boolean = true
    var walkTarget: Position? = null

    override fun onMove(from: Position, to: Position, direction: Direction) {}
    override fun onRegionChange() { needsRegionUpdate = true }

    // ================================================================
    //  Local player list (for update protocol)
    // ================================================================
    val localPlayers = mutableListOf<Int>()
    val localNpcs = mutableListOf<Int>()

    // ================================================================
    //  Stats
    // ================================================================
    val levels = IntArray(Skills.SKILL_COUNT) { if (it == Skills.HITPOINTS) 10 else 1 }
    val experience = DoubleArray(Skills.SKILL_COUNT) { if (it == Skills.HITPOINTS) 1154.0 else 0.0 }

    override fun getLevel(skill: Int): Int = levels.getOrElse(skill) { 1 }
    override fun setLevel(skill: Int, level: Int) { if (skill in levels.indices) levels[skill] = level }
    override fun getExperience(skill: Int): Double = experience.getOrElse(skill) { 0.0 }
    override fun addExperience(skill: Int, amount: Double) { if (skill in experience.indices) experience[skill] += amount }

    override fun getCombatLevel(): Int {
        val base = (getLevel(Skills.DEFENCE) + getLevel(Skills.HITPOINTS) + (getLevel(Skills.PRAYER) / 2)) * 0.25
        val melee = (getLevel(Skills.ATTACK) + getLevel(Skills.STRENGTH)) * 0.325
        val range = getLevel(Skills.RANGED) * 0.4875
        val mage = getLevel(Skills.MAGIC) * 0.4875
        return (base + maxOf(melee, range, mage)).toInt()
    }

    override fun getTotalLevel(): Int = levels.sum()

    // ================================================================
    //  Health
    // ================================================================
    override val maxHealth: Int get() = getLevel(Skills.HITPOINTS)
    override var currentHealth: Int = 10
    override val isAlive: Boolean get() = currentHealth > 0

    // ================================================================
    //  Inventory (28 slots)
    // ================================================================
    val inventoryItems = IntArray(28) { -1 }
    val inventoryAmounts = IntArray(28) { 0 }

    override fun hasItem(itemId: Int, amount: Int): Boolean {
        var count = 0
        for (i in inventoryItems.indices) if (inventoryItems[i] == itemId) count += inventoryAmounts[i]
        return count >= amount
    }
    override fun addItem(itemId: Int, amount: Int): Boolean {
        for (i in inventoryItems.indices) if (inventoryItems[i] == -1) { inventoryItems[i] = itemId; inventoryAmounts[i] = amount; return true }
        return false
    }
    override fun removeItem(itemId: Int, amount: Int): Boolean {
        var remaining = amount
        for (i in inventoryItems.indices) {
            if (inventoryItems[i] == itemId) {
                val rm = minOf(remaining, inventoryAmounts[i]); inventoryAmounts[i] -= rm; remaining -= rm
                if (inventoryAmounts[i] <= 0) { inventoryItems[i] = -1; inventoryAmounts[i] = 0 }
                if (remaining <= 0) return true
            }
        }
        return remaining <= 0
    }
    override fun getItemInSlot(slot: Int): Int = inventoryItems.getOrElse(slot) { -1 }
    override fun getItemCountInSlot(slot: Int): Int = inventoryAmounts.getOrElse(slot) { 0 }
    override fun inventoryFreeSlots(): Int = inventoryItems.count { it == -1 }

    // ================================================================
    //  Equipment (14 slots)
    // ================================================================
    val equipment = IntArray(14) { -1 }
    val equipmentAmounts = IntArray(14) { 0 }

    override fun getEquipment(slot: Int): Int = equipment.getOrElse(slot) { -1 }
    override fun setEquipment(slot: Int, itemId: Int, amount: Int) {
        if (slot in equipment.indices) { equipment[slot] = itemId; equipmentAmounts[slot] = amount }
    }

    // ================================================================
    //  Bank
    // ================================================================
    val bankItems = IntArray(352) { -1 }
    val bankAmounts = IntArray(352) { 0 }

    override fun bankHasItem(itemId: Int): Boolean = bankItems.contains(itemId)
    override fun addBankItem(itemId: Int, amount: Int): Boolean {
        for (i in bankItems.indices) if (bankItems[i] == -1) { bankItems[i] = itemId; bankAmounts[i] = amount; return true }
        return false
    }

    // ================================================================
    //  Appearance (ENGINE-LEVEL base)
    // ================================================================
    val appearance = intArrayOf(0, 10, 18, 26, 33, 36, 42)
    val colors = intArrayOf(7, 8, 9, 5, 0)
    var gender: Int = 0
    var skullIcon: Int = -1
    var prayerIcon: Int = -1

    // Animation overrides (set by equipment/weapon logic)
    var standAnim: Int = -1; var standTurnAnim: Int = -1; var walkAnim: Int = -1
    var turn180Anim: Int = -1; var turnCwAnim: Int = -1; var turnCcwAnim: Int = -1; var runAnim: Int = -1

    // ================================================================
    //  Update flags (ENGINE-LEVEL, protocol-driven)
    // ================================================================
    var updateRequired: Boolean = true

    var appearanceUpdateRequired: Boolean = true
    override fun flagAppearanceUpdate() { appearanceUpdateRequired = true; updateRequired = true }

    var chatUpdateRequired: Boolean = false
    var chatMessage: ByteArray = ByteArray(0); var chatEffects: Int = 0; var chatColor: Int = 0
    override fun flagChatUpdate() { chatUpdateRequired = true; updateRequired = true }

    var animationUpdateRequired: Boolean = false
    var currentAnimation: Int = -1; var animationDelay: Int = 0
    override fun animate(animationId: Int, delay: Int) { currentAnimation = animationId; animationDelay = delay; animationUpdateRequired = true; updateRequired = true }
    override fun resetAnimation() { currentAnimation = -1; animationUpdateRequired = true; updateRequired = true }

    var graphicUpdateRequired: Boolean = false
    var graphicId: Int = -1; var graphicHeight: Int = 0; var graphicDelay: Int = 0
    override fun graphic(graphicId: Int, height: Int, delay: Int) { this.graphicId = graphicId; this.graphicHeight = height; this.graphicDelay = delay; graphicUpdateRequired = true; updateRequired = true }

    var forceChatUpdateRequired: Boolean = false; var forceChatMessage: String = ""
    fun forceChat(msg: String) { forceChatMessage = msg; forceChatUpdateRequired = true; updateRequired = true }

    var faceEntityUpdateRequired: Boolean = false; var faceEntityIndex: Int = -1
    fun faceEntity(idx: Int) { faceEntityIndex = idx; faceEntityUpdateRequired = true; updateRequired = true }

    var facePositionUpdateRequired: Boolean = false; var faceX: Int = 0; var faceY: Int = 0
    fun facePosition(x: Int, y: Int) { faceX = x; faceY = y; facePositionUpdateRequired = true; updateRequired = true }

    var hitUpdateRequired: Boolean = false; var hitDamage1: Int = 0; var hitType1: Int = 0
    var hit2UpdateRequired: Boolean = false; var hitDamage2: Int = 0; var hitType2: Int = 0
    fun applyHit(damage: Int, type: Int) {
        if (!hitUpdateRequired) { hitDamage1 = damage; hitType1 = type; hitUpdateRequired = true }
        else { hitDamage2 = damage; hitType2 = type; hit2UpdateRequired = true }
        updateRequired = true
    }

    // ================================================================
    //  Friends / Ignores
    // ================================================================
    val friends = LongArray(200) { 0L }
    val ignores = LongArray(100) { 0L }

    // ================================================================
    //  Communication
    // ================================================================
    override fun sendMessage(text: String) { val p = PacketBuilder(253); p.startVariableSize(); p.addString(text); p.endVariableSize(); send(p) }
    override fun sendSkillMenu(skill: Int) {}

    // ================================================================
    //  Interfaces
    // ================================================================
    override fun openInterface(interfaceId: Int) { val p = PacketBuilder(97); p.addShort(interfaceId); send(p) }
    override fun closeInterfaces() { send(PacketBuilder(219)) }
    override fun sendSidebar(tabId: Int, interfaceId: Int) { val p = PacketBuilder(71); p.addShort(interfaceId); p.addByteA(tabId); send(p) }

    // ================================================================
    //  Movement API (delegates to engine)
    // ================================================================
    override fun teleport(position: Position) { this.position = position; walkingQueue.clear(); walkingQueue.didTeleport = true; needsRegionUpdate = true; updateRequired = true }
    override fun teleport(x: Int, y: Int, z: Int) = teleport(Position(x, y, z))
    override fun walkTo(position: Position) { walkTarget = position }
    override val isMoving: Boolean get() = walkingQueue.hasSteps()

    // ================================================================
    //  Session
    // ================================================================
    override fun disconnect(message: String) { if (message.isNotEmpty()) sendMessage(message); try { send(PacketBuilder(109)); channel.close() } catch (_: Exception) {} }
    override val isOnline: Boolean get() = channel.isActive

    // ================================================================
    //  Attributes (plugin state)
    // ================================================================
    private val attributes = ConcurrentHashMap<String, Any>()
    @Suppress("UNCHECKED_CAST") override fun <T : Any> getAttribute(key: String): T? = attributes[key] as? T
    override fun <T : Any> setAttribute(key: String, value: T) { attributes[key] = value }
    override fun removeAttribute(key: String) { attributes.remove(key) }
    override fun hasAttribute(key: String): Boolean = attributes.containsKey(key)

    // ================================================================
    //  Networking
    // ================================================================
    val packetQueue = ConcurrentLinkedQueue<IncomingPacket>()
    fun send(packet: PacketBuilder) {
        if (!channel.isActive) return
        try { val buf = encoder.encode(channel.pipeline().firstContext(), packet); channel.writeAndFlush(buf) }
        catch (e: Exception) { log.error("Error sending packet to {}", name, e) }
    }

    // ================================================================
    //  Inventory & Equipment Sync (Gathering Skills Patch)
    // ================================================================

    /**
     * Send item container to a client interface.
     * Opcode 53 — variable short size.
     *
     * Client reads: short interfaceId, short count,
     *   then per slot: byte amount (255 = read int next), LEShortA (itemId + 1, 0 = empty).
     *
     * Standard 317 format, verified against PI reference.
     */
    fun sendInterfaceItems(interfaceId: Int, items: IntArray, amounts: IntArray, count: Int) {
        val p = PacketBuilder(53)
        p.startVariableShortSize()
        p.addShort(interfaceId)
        p.addShort(count)
        for (i in 0 until count) {
            val amount = amounts.getOrElse(i) { 0 }
            if (amount > 254) {
                p.addByte(255)
                p.addInt(amount)
            } else {
                p.addByte(amount)
            }
            val itemId = items.getOrElse(i) { -1 }
            p.addLEShortA(if (itemId >= 0) itemId + 1 else 0)
        }
        p.endVariableShortSize()
        send(p)
    }

    /**
     * Send full inventory refresh to client.
     * Interface 3214 = inventory container.
     */
    override fun sendInventory() {
        sendInterfaceItems(3214, inventoryItems, inventoryAmounts, 28)
    }

    /**
     * Send full equipment refresh to client.
     * Interface 1688 = equipment container.
     */
    override fun sendEquipment() {
        sendInterfaceItems(1688, equipment, equipmentAmounts, 14)
    }

    // ================================================================
    //  Login initialization
    // ================================================================
    fun initialize() {
        // === Sidebar Interfaces (Project51/Anguish) ===
        sendSidebar(0, 2423)    // Attack styles
        sendSidebar(1, 13917)   // Skills tab
        sendSidebar(2, 10220)   // Quest tab
        sendSidebar(3, 3213)    // Inventory
        sendSidebar(4, 1644)    // Equipment
        sendSidebar(5, 15608)   // Prayer
        sendSidebar(6, 938)     // Magic (modern)
        sendSidebar(7, 18128)   // Clan chat
        sendSidebar(8, 5065)    // Friends
        sendSidebar(9, 5715)    // Ignores
        sendSidebar(10, 2449)   // Logout
        sendSidebar(11, 42500)  // Settings (wrench)
        sendSidebar(12, 147)    // Emotes
        sendSidebar(13, 47500)  // Monster tab

        // === Configs ===
        sendConfig(108, 0)   // Brightness
        sendConfig(172, 1)   // Auto-retaliate
        sendConfig(173, if (walkingQueue.running) 1 else 0) // Run orb
        sendConfig(427, 0)   // Accept aid

        // === Reset screen ===
        resetScreen()

        // === Skill updates (opcode 134) ===
        for (i in levels.indices) sendSkillUpdate(i)

        // === Skill tab text (interface text for each skill) ===
        for (i in levels.indices) refreshSkillText(i)

        // === HP / Prayer / Run orb text ===
        sendInterfaceText("" + levels[Skills.HITPOINTS], 4016)
        sendInterfaceText("" + getLevelForXP(experience[Skills.HITPOINTS].toInt()), 4017)
        sendInterfaceText("" + levels[Skills.PRAYER], 4012)
        sendInterfaceText("" + getLevelForXP(experience[Skills.PRAYER].toInt()), 4013)
        sendInterfaceText("100%", 149)

        // === Combat / Total level text ===
        val combatLvl = getCombatLevel()
        sendInterfaceText("Combat Level: $combatLvl", 3983)
        sendInterfaceText("Total level:", 19209)
        var totalLevel = 0
        for (i in levels.indices) totalLevel += getLevelForXP(experience.getOrElse(i) { 0.0 }.toInt())
        sendInterfaceText("$totalLevel", 3984)

        // === Player right-click options ===
        showOption(4, 0, "Follow")
        showOption(5, 0, "Trade with")

        // === Friends list + Chat modes ===
        sendFriendsStatus(2)
        sendChatModes(0, 0, 0)

        // === Inventory & Equipment sync (Gathering Skills Patch) ===
        sendInventory()
        sendEquipment()

        // === Welcome message ===
        sendMessage("Welcome to OpenRune.")

        // === Map region + teleport flag ===
        sendMapRegion()
        walkingQueue.didTeleport = true

        // === Appearance ===
        flagAppearanceUpdate()
    }

    override fun sendSkillUpdate(skill: Int) { val p = PacketBuilder(134); p.addByte(skill); p.addIntME1(experience.getOrElse(skill) { 0.0 }.toInt()); p.addByte(levels.getOrElse(skill) { 1 }); send(p) }

    /**
     * Send XP drop packet (opcode 11, variable byte size).
     * Client reads: long xpAmount, byte skillCount, byte[] skillIds.
     * This triggers the floating XP drop overlay and the corner XP counter.
     */
    override fun sendXpDrop(skill: Int, amount: Int) {
        val p = PacketBuilder(11)
        p.startVariableSize()
        p.addLong(amount.toLong())
        p.addByte(1)       // 1 skill
        p.addByte(skill)   // skill ID
        p.endVariableSize()
        send(p)
    }

    /** Send a client config/varp (opcode 36). */
    fun sendConfig(id: Int, value: Int) { val p = PacketBuilder(36); p.addLEShort(id); p.addByte(value); send(p) }

    /** Send interface text (opcode 126, variable short/word size). */
    override fun sendInterfaceText(text: String, interfaceId: Int) {
        val p = PacketBuilder(126)
        p.startVariableShortSize()
        p.addString(text)
        p.addShortA(interfaceId)
        p.endVariableShortSize()
        send(p)
    }

    /**
     * Open a chatbox interface dialog (opcode 218).
     * Client reads: LEShortA interfaceId, sets dialogID.
     * Used for level-up popups, NPC dialogue, etc.
     */
    override fun sendChatboxInterface(interfaceId: Int) {
        val p = PacketBuilder(218)
        p.addLEShortA(interfaceId)
        send(p)
    }

    /**
     * Send map region packet (opcode 73).
     * PI reference: mapRegionX = (absX>>3)-6, sends mapRegionX+6 = (absX>>3).
     * Client: baseX = (received - 6) * 8 = ((absX>>3) - 6) * 8.
     * This centers the 104-tile loaded area around the player.
     */
    fun sendMapRegion() {
        lastRegion = position
        val p = PacketBuilder(73)
        p.addShortA(position.x shr 3)    // raw chunk X (PI: mapRegionX+6 = (x>>3)-6+6 = x>>3)
        p.addShort(position.y shr 3)     // raw chunk Y
        send(p)
    }

    // ================================================================
    //  End-of-tick reset
    // ================================================================

    // ================================================================
    //  UI Packet Methods (Patch 006)
    // ================================================================

    /** Reset/close all interfaces (opcode 107). */
    fun resetScreen() { send(PacketBuilder(107)) }

    /** Send player right-click option (opcode 104, variable byte). */
    fun showOption(slot: Int, topOfList: Int, text: String) {
        val p = PacketBuilder(104)
        p.startVariableSize()
        p.addByteC(slot)
        p.addByteA(topOfList)
        p.addString(text)
        p.endVariableSize()
        send(p)
    }

    /** Send friends list loading status (opcode 221). 2 = loaded. */
    fun sendFriendsStatus(status: Int) {
        val p = PacketBuilder(221)
        p.addByte(status)
        send(p)
    }

    /** Send chat mode settings (opcode 206). */
    fun sendChatModes(publicChat: Int = 0, privateChat: Int = 0, tradeChat: Int = 0) {
        val p = PacketBuilder(206)
        p.addByte(publicChat)
        p.addByte(privateChat)
        p.addByte(tradeChat)
        send(p)
    }

    /**
     * Send full skill tab text refresh for a given skill.
     * Each skill has 4 interface texts: current level, max level, current XP, XP to next level.
     * Interface IDs match the Project51/Anguish client.
     */
    override fun refreshSkillText(skill: Int) {
        val level = levels.getOrElse(skill) { 1 }
        val xp = experience.getOrElse(skill) { 0.0 }.toInt()
        val maxLevel = getLevelForXP(xp)
        val nextLevelXp = getXPForLevel(maxLevel + 1)

        // Interface IDs: [currentLevel, maxLevel, currentXP, xpToNextLevel]
        val ids = SKILL_INTERFACE_IDS.getOrNull(skill) ?: return
        sendInterfaceText("$level", ids[0])
        sendInterfaceText("$maxLevel", ids[1])
        if (ids.size > 2) sendInterfaceText("$xp", ids[2])
        if (ids.size > 3) sendInterfaceText("$nextLevelXp", ids[3])
    }

    /** Get level for XP amount. */
    fun getLevelForXP(xp: Int): Int {
        var points = 0; var output = 0
        for (lvl in 1..99) {
            points += Math.floor(lvl + 300.0 * Math.pow(2.0, lvl / 7.0)).toInt()
            output = points / 4
            if (output >= xp) return lvl
        }
        return 99
    }

    /** Get XP required for a level. */
    fun getXPForLevel(level: Int): Int {
        var points = 0; var output = 0
        for (lvl in 1..level) {
            points += Math.floor(lvl + 300.0 * Math.pow(2.0, lvl / 7.0)).toInt()
            if (lvl >= level) return output
            output = points / 4
        }
        return 0
    }

    companion object {
        /**
         * Skill interface ID mapping: [currentLevel, maxLevel, currentXP, nextLevelXP]
         * Indices match Skills.ATTACK (0) through Skills.CONSTRUCTION (22).
         */
        val SKILL_INTERFACE_IDS = arrayOf(
            intArrayOf(4004, 4005, 4044, 4045),  // 0:  Attack
            intArrayOf(4008, 4009, 4056, 4057),  // 1:  Defence
            intArrayOf(4006, 4007, 4050, 4051),  // 2:  Strength
            intArrayOf(4016, 4017, 4080, 4081),  // 3:  Hitpoints
            intArrayOf(4010, 4011, 4062, 4063),  // 4:  Ranged
            intArrayOf(4012, 4013, 4068, 4069),  // 5:  Prayer
            intArrayOf(4014, 4015, 4074, 4075),  // 6:  Magic
            intArrayOf(4034, 4035, 4134, 4135),  // 7:  Cooking
            intArrayOf(4038, 4039, 4146, 4147),  // 8:  Woodcutting
            intArrayOf(4026, 4027, 4110, 4111),  // 9:  Fletching
            intArrayOf(4032, 4033, 4128, 4129),  // 10: Fishing
            intArrayOf(4036, 4037, 4140, 4141),  // 11: Firemaking
            intArrayOf(4024, 4025, 4104, 4105),  // 12: Crafting
            intArrayOf(4030, 4031, 4122, 4123),  // 13: Smithing
            intArrayOf(4028, 4029, 4116, 4117),  // 14: Mining
            intArrayOf(4020, 4021, 4092, 4093),  // 15: Herblore
            intArrayOf(4018, 4019, 4086, 4087),  // 16: Agility
            intArrayOf(4022, 4023, 4098, 4099),  // 17: Thieving
            intArrayOf(12166, 12167, 12171, 12172), // 18: Slayer
            intArrayOf(13926, 13927, 13921, 13922), // 19: Farming
            intArrayOf(4152, 4153, 4157, 4159),  // 20: Runecrafting
            intArrayOf(18799, 18800),             // 21: Hunter (no XP interfaces)
            intArrayOf(18797, 18798)              // 22: Construction (no XP interfaces)
        )
    }

    fun resetUpdateFlags() {
        updateRequired = false; appearanceUpdateRequired = false; chatUpdateRequired = false
        animationUpdateRequired = false; graphicUpdateRequired = false; forceChatUpdateRequired = false
        faceEntityUpdateRequired = false; facePositionUpdateRequired = false
        hitUpdateRequired = false; hit2UpdateRequired = false
        needsRegionUpdate = false
        walkingQueue.resetDirections()
    }
}
