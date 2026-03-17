# OpenRune - Project Status & Context

> **Use this document when starting a new Claude chat to continue development.**
> Paste this as context so Claude knows where the project stands.

## What This Is

OpenRune is a **from-scratch, modular RS2 317 game server and client** built by Chase Foster (Computer Works DBA). It is NOT a fork of PI, Silabsoft, or any existing RSPS source. The 317 protocol was implemented from the algorithm specs, and the reference client/server uploaded at the start were only used to understand the protocol (packet opcodes, login handshake, update formats).

**GitHub Repos:**
- Main project: https://github.com/LorexValkin/Openrune
- Bootstrap (launcher distribution): https://github.com/LorexValkin/openrune-bootstrap
- Launcher fork (planned): Fork of https://github.com/Jire/runelite-launcher

## Tech Stack

- **Server:** Kotlin, JDK 21, Netty 4.1, Gson, SLF4J/Logback, Gradle 8.6
- **Client:** Java (317 client modernized for JDK 21, Applet->JPanel migration)
- **Build:** Gradle multi-module with Kotlin DSL
- **Architecture:** Plugin-based. All game content is in plugin JARs. Engine systems (collision, pathfinding, movement, update protocol) are core and not pluggable.

## Module Breakdown

| Module | Language | Lines | Description |
|--------|----------|-------|-------------|
| `api/` | Kotlin | ~835 | Pure interfaces: PlayerRef, NpcRef, EventBus, DataStore, PluginContext |
| `core/` | Kotlin | ~5,900 | Server engine: networking, game loop, all core systems |
| `cache/` | Kotlin | ~280 | 317 cache file reader and definition extractor |
| `client/` | Java | ~13,000 | Modernized 317 client (78 Java files, Applet removed) |
| `launcher/` | Kotlin | ~370 | Server CLI launcher with plugin management |
| `plugins/` | Kotlin | ~550 | Example skills + combat plugins |

**Total: ~21,000 lines across 120+ files**

## What Works (Completed)

### Server
- [x] Netty networking with full 317 login handshake (ISAAC cipher, credentials, session keys)
- [x] 600ms game tick engine with 13-phase processing
- [x] JSON data system with file-watching hot-reload (npcs, items, drops, shops, spawns, weapons, objects)
- [x] JSON player saves with versioning
- [x] Plugin system: JAR-based, isolated classloaders, dependency resolution, hot-swap
- [x] Event bus: typed events, priority ordering, per-plugin ownership
- [x] Collision map: directional walls, object blocking, projectile checks, multi-tile entities
- [x] A* pathfinding using collision map
- [x] Movement processor: walking/running queue, step-by-step collision checking
- [x] Appearance system: equipment rendering, body parts, colors, animation sets
- [x] Player update protocol (opcode 81): bit-packed with all flag blocks
- [x] NPC system: entity, manager, random walk, respawn, update protocol (opcode 65)
- [x] Object manager: spawn/despawn, temporary replacements, collision sync
- [x] Ground item manager: private->global visibility, despawn timers
- [x] Region loader (from cache, with XTEA support)
- [x] Packet dispatcher: translates raw opcodes -> typed events
- [x] Admin commands: tele, item, npc, setlevel, master, reload, plugins, engine, etc.
- [x] NPC spawn loading from data/spawns/*.json

### Client
- [x] Applet -> JPanel migration (works on Java 21)
- [x] Cache loading from ~/.openrune/cache/
- [x] Title screen rendering
- [x] Login screen with keyboard input
- [x] Successful login handshake with the server
- [x] All hardcoded paths fixed to use signlink.findcachedir()
- [x] Interface cache bounds checking (prevents ArrayIndexOutOfBounds)

### Infrastructure
- [x] Gradle multi-module build (all modules compile clean)
- [x] Windows batch scripts (setup, build, start-server, start-client, setup-cache)
- [x] GitHub Actions workflow for client releases
- [x] Bootstrap system for RuneLite-style launcher distribution
- [x] Launcher fork config (setup-openrune.sh for Jire/runelite-launcher)

## What's Next (Priority Order)

### 1. Complete Player Update Protocol
The client connects and logs in, but disconnects after ~15 seconds because the player update packet (opcode 81) isn't fully fleshed out. The bit-packed movement + appearance block needs to be complete enough that the client renders the player in the world. **This is the #1 blocker to seeing the game world.**

Key files:
- `core/src/main/kotlin/com/openrune/core/world/update/PlayerUpdateProtocol.kt`
- `core/src/main/kotlin/com/openrune/core/world/update/BitWriter.kt`
- `core/src/main/kotlin/com/openrune/core/world/appearance/AppearanceBuilder.kt`

### 2. Handle Missing Packet Opcodes
The server logs these as unhandled (client sends them after login):
```
Unhandled: 71, 209, 64, 63, 55, 124, 15, 82, 134, 27, 154, 160
```
Most are camera/focus/idle packets that need silent handlers.

### 3. Map Region Loading
When the player moves between regions, the server needs to send the map data and the client needs to load the terrain. The RegionLoader exists but needs the cache lookup table built.

### 4. Client Modernization (Ongoing)
- Rename obfuscated classes (Class21, Class36, etc.) to meaningful names
- Clean up the 78 Java files
- Add the JSON-driven interface renderer (for 562 UI skin support)

### 5. Content Plugins
- Build and test the skills plugin JAR
- Build and test the combat plugin JAR
- Drop them in server/plugins/ and toggle from launcher

### 6. Launcher Distribution
- Fork Jire/runelite-launcher, run setup-openrune.sh
- Set up openrune-bootstrap repo with GitHub Pages
- First native build (.exe, .app, .AppImage)

## Engine Architecture

```
Tick Phases (600ms cycle):
  1. Process pending logins
  2. Process pending logouts  
  3. Process incoming packets
  4. Resolve walk targets -> pathfinder
  5. Process movement (collision-checked)
  6. Scheduled tasks (plugin timers)
  7. ServerTickEvent (plugin hooks)
  8. NPC processing (AI, movement, respawns)
  9. Object manager (temporary reverts)
  10. Ground items (visibility, despawns)
  11. Player update protocol (opcode 81)
  12. NPC update protocol (opcode 65)
  13. Reset flags, periodic saves
```

**Engine-level systems (NOT pluggable):**
CollisionMap, Pathfinder, MovementProcessor, AppearanceBuilder, PlayerUpdateProtocol, NpcUpdateProtocol, BitWriter, RegionLoader, ObjectManager, GroundItemManager, Player entity, NPC entity, PlayerManager, NpcManager

**Plugin-level (hot-swappable JARs):**
Skills, Combat, Commands, Minigames, Shops, Dialogues, Quests -- anything in the `content` layer

## Key Design Decisions

1. **Kotlin for server, Java for client** -- Server benefits from Kotlin's null safety and coroutines. Client stays Java because the 317 rendering engine is 13K lines of legacy Java and rewriting it gains nothing.

2. **Plugins code against API, never core** -- The `api/` module has zero dependencies. Plugins import `api/` only. Core imports `api/` and implements it. This means a buggy plugin can't corrupt engine state.

3. **JSON everything** -- All game data is JSON with file-watch hot-reload. Edit npcs.json while the server runs, changes apply immediately. No recompile for data changes.

4. **Engine systems are locked** -- Collision, pathfinding, movement, and the update protocol are core. Plugins can trigger movement (player.walkTo) and listen to events (PlayerMoveEvent) but cannot replace the collision check or the pathfinding algorithm. A bad skill plugin can't break walking.

## File Locations on Chase's Machine

- Project: `C:\Users\User\Desktop\Open Rune\openrune\`
- Cache data: `C:\Users\User\.openrune\cache\`
- Gradle: `C:\gradle-8.6\`
- JDK 21: `C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot`
- JDK 25 (also installed, don't use for this project): `C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot`

## How to Start Every Session

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"
cd "C:\Users\User\Desktop\Open Rune\openrune"

# Terminal 1: Server
.\gradlew.bat :core:run

# Terminal 2: Client  
.\gradlew.bat :client:run
```

If you just extracted a fresh zip, run wrapper first:
```
C:\gradle-8.6\bin\gradle.bat wrapper --gradle-version 8.6
```
