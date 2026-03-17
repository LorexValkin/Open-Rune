package com.openrune.api.event

import com.openrune.api.entity.NpcRef
import com.openrune.api.entity.PlayerRef
import com.openrune.api.world.Position

// ============================================================
//  Player Lifecycle
// ============================================================

/** Fired when a player's login is being processed. Cancel to reject. */
class PlayerLoginEvent(val player: PlayerRef) : GameEvent()

/** Fired after a player has fully logged in and entered the world. */
class PlayerPostLoginEvent(val player: PlayerRef) : GameEvent()

/** Fired when a player disconnects or logs out. */
class PlayerLogoutEvent(val player: PlayerRef) : GameEvent()

// ============================================================
//  Player Actions
// ============================================================

/** Fired when a player types a ::command. */
class CommandEvent(
    val player: PlayerRef,
    val command: String,
    val args: List<String>
) : GameEvent()

/** Fired when a player sends a chat message. */
class ChatEvent(
    val player: PlayerRef,
    val message: String,
    val effects: Int = 0,
    val color: Int = 0
) : GameEvent()

/** Fired when a player walks or runs. */
class PlayerMoveEvent(
    val player: PlayerRef,
    val from: Position,
    val to: Position,
    val running: Boolean
) : GameEvent()

// ============================================================
//  Interactions
// ============================================================

/** Fired when a player clicks an object in the world. */
class ObjectInteractEvent(
    val player: PlayerRef,
    val objectId: Int,
    val position: Position,
    val option: Int
) : GameEvent()

/** Fired when a player interacts with an NPC. */
class NpcInteractEvent(
    val player: PlayerRef,
    val npc: NpcRef,
    val option: Int
) : GameEvent()

/** Fired when a player uses an item on another item. */
class ItemOnItemEvent(
    val player: PlayerRef,
    val usedItemId: Int,
    val usedSlot: Int,
    val targetItemId: Int,
    val targetSlot: Int
) : GameEvent()

/** Fired when a player uses an item on an object. */
class ItemOnObjectEvent(
    val player: PlayerRef,
    val itemId: Int,
    val objectId: Int,
    val objectPosition: Position
) : GameEvent()

/** Fired when a player uses an item on an NPC. */
class ItemOnNpcEvent(
    val player: PlayerRef,
    val itemId: Int,
    val npc: NpcRef
) : GameEvent()

/** Fired when a player clicks an item in their inventory. */
class ItemClickEvent(
    val player: PlayerRef,
    val itemId: Int,
    val slot: Int,
    val option: Int
) : GameEvent()

/** Fired when a player equips an item. Cancel to prevent. */
class ItemEquipEvent(
    val player: PlayerRef,
    val itemId: Int,
    val slot: Int
) : GameEvent()

/** Fired when a player drops an item. */
class ItemDropEvent(
    val player: PlayerRef,
    val itemId: Int,
    val slot: Int
) : GameEvent()

/** Fired when a player picks up a ground item. */
class ItemPickupEvent(
    val player: PlayerRef,
    val itemId: Int,
    val position: Position
) : GameEvent()

/** Fired when a player clicks a button/interface element. */
class ButtonClickEvent(
    val player: PlayerRef,
    val buttonId: Int
) : GameEvent()

// ============================================================
//  Combat
// ============================================================

/** Fired when an entity is about to attack another. Cancel to prevent. */
class CombatAttackEvent(
    val attacker: PlayerRef,
    val targetPlayer: PlayerRef? = null,
    val targetNpc: NpcRef? = null,
    val style: CombatStyle = CombatStyle.MELEE
) : GameEvent()

/** Fired when damage is about to be applied. Modify [damage] to change it. */
class CombatDamageEvent(
    val attacker: PlayerRef,
    val targetPlayer: PlayerRef? = null,
    val targetNpc: NpcRef? = null,
    var damage: Int,
    val style: CombatStyle
) : GameEvent()

/** Fired when an entity dies. */
class DeathEvent(
    val player: PlayerRef?,
    val npc: NpcRef?,
    val killer: PlayerRef? = null
) : GameEvent()

enum class CombatStyle { MELEE, RANGED, MAGIC }

// ============================================================
//  Skills
// ============================================================

/** Fired when XP is about to be granted. Modify [amount] to change. */
class ExperienceEvent(
    val player: PlayerRef,
    val skill: Int,
    var amount: Double
) : GameEvent()

/** Fired when a player levels up. */
class LevelUpEvent(
    val player: PlayerRef,
    val skill: Int,
    val newLevel: Int
) : GameEvent()

// ============================================================
//  World / Server
// ============================================================

/** Fired every game tick. Plugins can hook into the main loop. */
class ServerTickEvent(val tick: Long) : GameEvent()

/** Fired when JSON data files are reloaded. */
class DataReloadEvent(val store: String) : GameEvent()

/** Fired when an NPC spawns into the world. */
class NpcSpawnEvent(val npc: NpcRef) : GameEvent()

/** Fired when an NPC is removed from the world. */
class NpcDespawnEvent(val npc: NpcRef) : GameEvent()
