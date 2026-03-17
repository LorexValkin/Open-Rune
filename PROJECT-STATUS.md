# OpenRune - Project Status & Context

> **Use this document when starting a new Claude chat to continue development.**
> Paste this as context so Claude knows where the project stands.
> Last updated: March 17, 2026

## What This Is

OpenRune is a **from-scratch, modular RS2 317 game server and client** built by Chase Foster (LorexValkin). It is NOT a fork of PI, Silabsoft, or any existing RSPS source. The 317 protocol was implemented from the algorithm specs, and the reference client/server uploaded at the start were only used to understand the protocol (packet opcodes, login handshake, update formats).

**GitHub:** https://github.com/LorexValkin/Open-Rune
**Git config:** email=fchaseh@pm.me, user=LorexValkin

**File Locations:**
- Home PC: `C:\Users\User\Desktop\Open Rune\openrune\`
- Office PC: `C:\Users\User\IdeaProjects\Open-Rune\`
- Patches (Home): `C:\Users\User\Desktop\Open Rune\Patches`
- Patches (Office): `C:\Users\User\Desktop\Open-Rune-Patches`
- Cache (both machines): `%USERPROFILE%\.openrune\cache\` (client cache, server falls back here)
- IDE: IntelliJ IDEA

## Tech Stack

- **Server:** Kotlin, JDK 21, Netty 4.1, Gson, SLF4J/Logback, Gradle 8.6
- **Client:** Java (317 client modernized for JDK 21, Applet->JPanel migration)
- **Build:** Gradle multi-module with Kotlin DSL
- **Architecture:** Plugin-based. All game content is in plugin JARs. Engine systems (collision, pathfinding, movement, update protocol) are core and not pluggable.
- **Reference source:** PI (Project Insanity) for protocol specifics. Displee's rs-cache-library studied for architecture reference (not used as dependency).

## Module Breakdown

| Module | Language | Lines | Description |
|--------|----------|-------|-------------|
| `api/` | Kotlin | ~835 | Pure interfaces: PlayerRef, NpcRef, EventBus, DataStore, PluginContext |
| `core/` | Kotlin | ~6,500 | Server engine: networking, game loop, all core systems |
| `cache/` | Kotlin | ~500 | 317 cache reader, BZip2 decompressor, archive parser |
| `client/` | Java | ~13,000 | 317 client (78 Java files, mostly obfuscated) |
| `launcher/` | Kotlin | ~370 | Server CLI launcher with plugin management |
| `plugins/` | Kotlin | ~550 | Example skills + combat plugins |

## Completed Systems

### Server Core
- [x] Netty networking with full 317 login handshake (ISAAC cipher, credentials, session keys)
- [x] 600ms game tick engine with phased processing
- [x] JSON data system with file-watching hot-reload (npcs, items, drops, shops, spawns, weapons, objects)
- [x] JSON player saves with versioning
- [x] Plugin system: JAR-based, isolated classloaders, dependency resolution, hot-swap
- [x] Event bus: typed events, priority ordering, per-plugin ownership
- [x] Packet dispatcher: per-opcode handlers matching client write methods
- [x] Admin commands: tele, item, npc, setlevel, master, reload, plugins, engine, etc.
- [x] NPC spawn loading from data/spawns/*.json
- [x] DebugManager: 17 toggleable debug categories, enable via `--debug=MOVEMENT,COLLISION` or `::debug movement on`

### Cache & Map System
- [x] CacheReader: opens all 5 indices (0-4) from client cache location
- [x] BZip2Decompressor: clean Kotlin port from client's Class13/Class32 (fixed missing CRC byte)
- [x] ArchiveReader: parses index 0 archives, decompresses BZip2 entries
- [x] Map index: loaded from index 0 file 5 — **660 regions indexed**
- [x] RegionLoader: GZip decompresses map data from index 4 before parsing landscape/objects
- [x] Cache fallback: checks `cache-data/` first, falls back to `%USERPROFILE%\.openrune\cache\`

### Collision System (FULLY OPERATIONAL)
- [x] CollisionMap with directional wall flags, OBJECT_TILE + OBJECT_BLOCK
- [x] Standard 317 blocking masks matching PI (0x100 + 0x200000)
- [x] Two-sided wall checks (source exit + destination entry)
- [x] Landscape blocked tiles (water, cliffs) use full blocking flags
- [x] Regions load on-demand when player enters area
- [x] NPC spawn regions pre-loaded at startup

### Movement System
- [x] **Players: trust client BFS pathfinding** — zero server collision checking (PI behavior)
- [x] **NPCs: server collision checking per step** — no client to pathfind for them
- [x] `isPlayer` flag on Movable interface distinguishes player vs NPC in takeStep
- [x] Walking packet handler fills queue directly from client waypoints (no server A*)
- [x] A* pathfinder exists for future NPC combat following / interaction reach checks
- [x] Running support (WalkingQueue.running, 2 steps per tick)

### NPC System
- [x] Destination-based natural random walk (2-4 tiles per walk)
- [x] 15% idle chance, 2-7 tick pause between walks
- [x] Soft leash at 50% of walkRange biases toward spawn
- [x] Hard leash: never exceeds walkRange from spawn
- [x] Collision checked per step before queuing
- [x] walkingQueue.running = false (NPCs walk, not run)
- [x] NPC update protocol (opcode 65)

### Object Interactions
- [x] ObjectInteractionHandler: 33 doors, 43 stairs/ladders
- [x] Doors: open/close state tracking, auto-close timer, rotation-based position shift
- [x] Stairs/ladders: teleport z±1 with climb animation

### Client
- [x] Applet -> JPanel migration (works on Java 21)
- [x] Cache loading from ~/.openrune/cache/
- [x] Right-click menu fix (isMetaDown -> SwingUtilities.isRightMouseButton for JDK 9+)
- [x] Login handshake works, player renders in world
- [x] Walking, running, doors all functional

## Engine Architecture

```
Tick Phases (600ms cycle):
  1. Process pending logins
  2. Process pending logouts
  3. Process incoming packets
  4. Get all players for this tick
  5. Process movement (players: no collision, NPCs: collision checked)
  6. Scheduled tasks (plugin timers)
  7. ServerTickEvent (plugin hooks)
  8. NPC processing (AI, movement, respawns)
  9. Object manager (temporary reverts)
  10. Ground items (visibility, despawns)
  11. Player update protocol (opcode 81)
  12. NPC update protocol (opcode 65)
  13. Reset flags, periodic saves
```

Note: Phase 4 previously ran A* pathfinding for player walk targets. This was removed — player walking is now 100% client-driven (PI behavior). A* pathfinder is retained for NPC combat/interaction use.

**Engine-level systems (NOT pluggable):**
CollisionMap, Pathfinder, MovementProcessor, AppearanceBuilder, PlayerUpdateProtocol, NpcUpdateProtocol, BitWriter, RegionLoader, ObjectManager, GroundItemManager, ObjectInteractionHandler, DebugManager

**Plugin-level (hot-swappable JARs):**
Skills, Combat, Commands, Minigames, Shops, Dialogues, Quests

## Key Design Decisions

1. **Player movement trusts client** — The 317 client does BFS pathfinding and sends pre-validated waypoints. Server just walks the path. Zero server-side collision for players. This matches PI and all standard 317 servers. Server collision is for NPCs only.

2. **NPC collision is server-side** — NPCs have no client to pathfind for them. The server checks collision per step and stops at walls. A* pathfinder is available for future combat following.

3. **BZip2 from scratch** — Built own BZip2Decompressor.kt (Kotlin port of client's Class13/Class32) instead of using Displee's library as a dependency. Used Displee as reference only.

4. **Map data is GZip compressed** — Cache index 4 files must be GZip decompressed before parsing. The client's OnDemandFetcher does this with GZIPInputStream. Server mirrors this.

5. **Kotlin for server, Java for client** — Server benefits from Kotlin's null safety. Client stays Java because the 317 rendering engine is 13K lines of legacy Java.

6. **Plugins code against API, never core** — The `api/` module has zero dependencies. Plugins import `api/` only.

7. **JSON everything** — All game data is JSON with file-watch hot-reload. No recompile for data changes.

## Session Fix Log (March 17, 2026)

| Fix | Description | Files Changed |
|-----|-------------|---------------|
| 10 | Right-click menu + NPC interaction packets | RSApplet.java, Packet.kt, PacketDispatcher.kt |
| 11 | Object interactions (doors/stairs/ladders) | ObjectInteractionHandler.kt (NEW), GameEngine.kt |
| 12-13 | BZip2 decompressor (missing CRC byte fix) | BZip2Decompressor.kt (NEW), ArchiveReader.kt |
| 14 | Cache fallback path | Server.kt |
| 15 | NPC collision flags (OBJECT_BLOCK + two-sided walls) | CollisionMap.kt |
| 16 | NPC walking v2 (destination-based, multi-tile) | NpcManager.kt |
| 17 | Water/terrain collision (full blocking flags) | RegionLoader.kt |
| 18-19 | NPC walking v4 (running disabled, collision restored) | Npc.kt, NpcManager.kt |
| 20 | GZip decompress map data from cache index 4 | RegionLoader.kt |
| 21 | Smart rerouting (A* on blocked step) | MovementProcessor.kt |
| 22-23 | Player walking trusts client (PI behavior) | MovementProcessor.kt, GameEngine.kt, Player.kt |
| — | DebugManager system (17 categories, toggleable) | DebugManager.kt (NEW) |
| — | NPC spawn region preloading | Server.kt |
| — | Packet.kt INCOMING size table corrected (~15 values) | Packet.kt |

## Known Issues

- **Landscape decode error (50, 50)**: Region 50,50 (Lumbridge) shows "null" error — minor, may be edge-case tile format
- **Collision regions: 0 at startup**: Normal if NPC preload code not synced to machine. Regions load on-demand when player enters area.
- **Diagnostic println in ArchiveReader**: `[RAW]` debug line still present. Should be removed or moved to DebugManager.

---

## NEXT PHASE: Client Enhancement & Modernization

### Phase 1: Client Deobfuscation

The 317 client has 78 Java files, most with obfuscated names like `Class11`, `Class32`, `Class36`, etc. Methods are named `method180`, `method183`, etc. This makes development extremely slow.

**Approach: Incremental renaming, no rewrites.**

Priority classes to deobfuscate (by importance to development):

| Obfuscated | Proposed Name | Purpose |
|-----------|---------------|---------|
| `client.java` | Keep as `Client.java` | Main class, already partially readable |
| `Class36` | `ObjectManager` | Loads/renders map objects, method180/183/189 |
| `Class11` | `CollisionMap` | Client-side collision/clipping data |
| `Class13` | `BZip2Decoder` | BZip2 decompression (already understood) |
| `Class32` | `BZip2State` | BZip2 state object (already understood) |
| `Class21` | `AnimationDefinition` or `SeqDef` | Animation sequences |
| `Class33` | `ItemDefinition` or `ObjDef` | Item definitions from cache |
| `Class9` | `Widget` or `RSInterface` | Interface/widget definitions |
| `RSApplet` | `GameShell` | Base game frame (already modified for JDK 9+) |
| `Decompressor` | Keep or rename `CacheIndex` | Cache index file reader |
| `OnDemandFetcher` | Keep or rename `CacheRequester` | On-demand cache file loading |
| `ObjectManager` | `SceneBuilder` or `MapLoader` | Builds the 3D scene from map data |

**Rules:**
- Rename one class at a time, rebuild, test
- Keep a rename log so server-side references stay in sync
- Use IntelliJ refactor (Shift+F6) for safe renames
- Don't rename internal fields yet — just classes and key methods first

### Phase 2: Client Quality-of-Life

These are small changes that make the client feel modern without touching the rendering engine:

| Feature | Description | Difficulty |
|---------|-------------|------------|
| Scroll wheel zoom | Map mouse wheel to camera zoom | Easy |
| Middle-click rotate | Camera rotation via middle mouse drag | Easy |
| Resizable window | Remove fixed 765x503, allow window resize | Medium |
| Tab-to-reply | Tab key focuses last PM sender | Easy |
| Shift-click drop | Drop items without right-click menu | Easy |
| XP drops | Floating XP text on skill use | Medium |
| Ground item names | Show item names on the ground | Medium |
| Chat filter toggle | Toggle game messages, public, private | Easy |

### Phase 3: Client Rendering Improvements

| Feature | Description | Difficulty |
|---------|-------------|------------|
| Bilinear texture filtering | Smoother textures (replace nearest-neighbor) | Medium |
| Anti-aliasing | MSAA or FXAA post-process | Hard |
| Resolution cap removal | Render at native resolution, not 512x334 | Medium |
| FPS uncap | Remove 50fps limit, or make configurable | Easy |
| JOGL/OpenGL renderer | Replace software rasterizer with GPU rendering | Very Hard (future) |

### Phase 4: Settings Panel & Launcher Integration

| Feature | Description |
|---------|-------------|
| Swing settings panel | In-game settings: resolution, zoom, debug toggles, keybinds |
| Debug toggle in settings | Checkbox per DebugManager category |
| Server address config | Point client at different server IPs |
| Cache path config | Override default cache location |
| Launcher integration | Bootstrap auto-update, version checking |

## How to Start Every Session

```powershell
# Office PC
cd "C:\Users\User\IdeaProjects\Open-Rune"

# Home PC
cd "C:\Users\User\Desktop\Open Rune\openrune"

# Terminal 1: Server
.\gradlew.bat :core:run

# Terminal 2: Client
.\gradlew.bat :client:run

# With debug enabled
.\gradlew.bat :core:run --args="--debug=MOVEMENT,COLLISION"
```

## Patch Workflow

All fixes are delivered as PowerShell scripts saved to the patches folder:
- Home: `C:\Users\User\Desktop\Open Rune\Patches`
- Office: `C:\Users\User\Desktop\Open-Rune-Patches`

```powershell
copy "$env:USERPROFILE\Downloads\fix-name.ps1" "C:\Users\User\Desktop\Open-Rune-Patches\"
powershell -ExecutionPolicy Bypass -File "C:\Users\User\Desktop\Open-Rune-Patches\fix-name.ps1"

# Then commit
cd "C:\Users\User\IdeaProjects\Open-Rune"
git add -A
git commit -m "Fix N: description"
git push origin main
```

Always `git pull` on the other machine before working there.
