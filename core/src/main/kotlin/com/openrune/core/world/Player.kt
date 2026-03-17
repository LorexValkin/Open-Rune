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
    //  Login initialization
    // ================================================================
    fun initialize() {
        sendSidebar(0, 2423); sendSidebar(1, 3917); sendSidebar(2, 638); sendSidebar(3, 3213)
        sendSidebar(4, 1644); sendSidebar(5, 5608); sendSidebar(6, 1151); sendSidebar(8, 5065)
        sendSidebar(9, 5715); sendSidebar(10, 2449); sendSidebar(11, 904); sendSidebar(12, 147); sendSidebar(13, 962)
        for (i in levels.indices) sendSkillUpdate(i)
        sendConfig(173, if (walkingQueue.running) 1 else 0) // Run orb state
        sendMapRegion()
        // Signal the update protocol to send placement (type 3) on the first tick.
        // Without this, the client never receives setPos() and the player stays at (0,0).
        walkingQueue.didTeleport = true
        flagAppearanceUpdate()
    }

    fun sendSkillUpdate(skill: Int) { val p = PacketBuilder(134); p.addByte(skill); p.addInt(experience.getOrElse(skill) { 0.0 }.toInt()); p.addByte(levels.getOrElse(skill) { 1 }); send(p) }

    /** Send a client config/varp (opcode 36). */
    fun sendConfig(id: Int, value: Int) { val p = PacketBuilder(36); p.addLEShort(id); p.addByte(value); send(p) }

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
    fun resetUpdateFlags() {
        updateRequired = false; appearanceUpdateRequired = false; chatUpdateRequired = false
        animationUpdateRequired = false; graphicUpdateRequired = false; forceChatUpdateRequired = false
        faceEntityUpdateRequired = false; facePositionUpdateRequired = false
        hitUpdateRequired = false; hit2UpdateRequired = false
        needsRegionUpdate = false
        walkingQueue.resetDirections()
    }
}
