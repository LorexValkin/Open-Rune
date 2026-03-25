# OpenRune 317 — Session Handoff (March 24, 2026 — Session 2)
# Woodcutting Algorithm, Fishing Skill, NPC ID Investigation

## Project Location
- **Office**: `C:\Users\User\IdeaProjects\Open-Rune`
- **Home**: `C:\Users\User\Desktop\Open Rune\openrune` (may need git pull)
- **GitHub**: `LorexValkin/Open-Rune`
- **Anguish Reference**: `C:\Users\User\Documents\Project51 Package` (has server source + client)
- **Cache**: Anguish client, OSRS build 190 cache
- **Build server**: `.\gradlew.bat build`
- **Build client**: `cd client && find src -name "*.java" > sources.txt && javac -d bin -cp "lib/xpp3-1.1.3.4.C.jar;lib/xstream-1.3.1.jar" -encoding UTF-8 -nowarn @sources.txt && rm sources.txt`
- **CRITICAL**: After building plugins, must copy JARs: `cp plugins/*/build/libs/*.jar server/plugins/`
- **Run server**: `.\gradlew.bat :core:run`
- **Run client**: from `client/` directory, use `launch.bat` or `run.bat`

## Key Rules
- **Never mention Computer Works in OpenRune/RSPS project files**
- **Never use git checkout to restore files** — always local `.bak_` backups
- **After any API (PlayerRef) change** — must `.\gradlew.bat clean build` and recopy ALL plugin JARs
- **Client changes require separate build** — client is NOT a Gradle module
- **After plugin changes** — must copy JARs from `plugins/*/build/libs/*.jar` to `server/plugins/`

---

## What Was Done This Session

### 1. Willow/Climb Bug — FIXED
**Root cause**: The objects at willow locations were actually NPC ID 1750, not 5551/8481. ID 1750 was in the stairLookup as a ladder and was removed from trees.json.

**Fix**:
- Added 1750, 1751, 1756, 1760 back to trees.json as willow trees
- Removed them from stairLookup in ObjectInteractionHandler.kt
- stumpId set to 1342 (generic stump) — correct stump for these IDs still unknown

### 2. Woodcutting Algorithm — IMPLEMENTED (Wiki-Accurate)
Replaced the simplified formula with the real RS community-sourced skilling formula:
```
chance = floor(low * (99 - level) / 98 + high * (level - 1) / 98 + 0.5)
probability = (1 + chance) / 256
```

- Each tree+axe combo has unique [low, high] values from OSRS Wiki
- trees.json now has `chanceByAxe` and `depletionChance` fields
- axes.json now uses `tier` string instead of `speed` float
- Multi-log trees (oak+) have 1/8 depletion chance per log

### 3. Fishing Skill — IMPLEMENTED (Backend Only)
Full fishing skill with data-driven architecture:
- `data/fishing/spots.json` — defines spot types, NPC IDs, tools, bait, fish, low/high values
- FishingSkill class in SkillsPlugin.kt — handles NpcInteractEvent
- Cascade roll system (highest-level fish rolled first)
- Bait consumption, tool checks, level checks
- Uses same RS skilling formula as woodcutting

### 4. Fishing Spawns — ADDED BUT WRONG NPC IDs
Added 73 fishing spot spawns (IDs 2985-3057) to data/spawns/spawns.json with walkRange: 0.
**PROBLEM**: NPC IDs 233-334 render as Emblem Traders / other NPCs in this OSRS 190 cache, NOT as fishing spots.

### 5. Debug Tools Added
- `::objinfo <id>` — client command to dump ObjectDefinition
- `::npcinfo <id>` — client command to dump NpcDefinition
- `::findnpc <name>` — client command to search NPC definitions by name (searches 0-15000)
- `::gatherids` — server command to verify gathering object IDs
- Object click logging in ObjectInteractionHandler (INFO level)

---

## Known Issues (Fix Next Session)

### 1. Fishing Spot NPC IDs — PRIORITY
**Problem**: The 233-334 NPC IDs used in spots.json and spawns render as wrong NPCs (Emblem Trader etc.) in this Anguish/OSRS 190 cache.

**What to do**:
1. Log in and run `::findnpc fishing spot` to find the correct NPC IDs in this cache
2. The Anguish reference server (`Project51 Package`) uses: 3913 (shrimp), 3417 (monkfish), 3657 (lobster/swordfish), 1520 (shark) — but 3913/3417/3657 don't exist in the server's npcs.json
3. The cache (loaded by client) likely has these NPCs at higher IDs not in the server JSON
4. Once correct IDs are found:
   - Update `data/fishing/spots.json` npcIds arrays
   - Update all fishing spawns in `data/spawns/spawns.json` (IDs 2985-3057)
   - May need to add NPC definitions to `data/npcs/npcs.json`

### 2. Fishing Spot Movement (Despawn/Respawn)
RS fishing spots periodically move to nearby tiles (~100-300 ticks). Currently they're stationary (walkRange: 0). Implement later as a tick timer that despawns/respawns at a nearby valid tile.

### 3. stumpId for 1750/1751/1756/1760
Currently using generic stump 1342. Run `::objinfo 1750` to check the actual object and find proper stump ID.

### 4. Debug logging cleanup
ObjectInteractionHandler has `log.info` for object clicks — revert to `log.debug` after debugging is done.

### 5. Stop Hook Errors
Errors from ECC plugin hooks in console. Still needs investigation.

---

## Test Commands
```
::item 303            Small fishing net
::item 307            Fishing rod
::item 309            Fly fishing rod
::item 311            Harpoon
::item 301            Lobster pot
::item 313 100        Fishing bait
::item 314 100        Feathers
::item 1351           Bronze axe
::item 6739           Dragon axe
::setlevel 10 1       Reset Fishing to 1
::setlevel 8 1        Reset Woodcutting to 1
::master              All skills to 99
::debuggather         Toggle instant gathering
::findnpc <name>      Search NPC defs by name
::npcinfo <id>        Dump NPC definition
::objinfo <id>        Dump Object definition
::gatherids           Check gathering object IDs loaded
::tele 3240 3146 0    Lumbridge Swamp (net/bait)
::tele 3104 3424 0    Barbarian Village (lure/bait)
::tele 2924 3178 0    Karamja dock (cage/harpoon)
```

## Files Changed This Session
```
core/src/main/kotlin/.../ObjectInteractionHandler.kt  — Debug logging, removed 1750/1751/1756/1760 from stairs, added gatheringIdCount/isGatheringObject
core/src/main/kotlin/.../Server.kt                     — Added ::gatherids command
plugins/skills-plugin/.../SkillsPlugin.kt              — RS formula, multi-log depletion, FishingSkill class
client/src/com/client/Client.java                       — Added ::objinfo, ::npcinfo, ::findnpc commands
data/trees/trees.json                                   — Added chanceByAxe, depletionChance, restored 1750/1751/1756/1760
data/axes/axes.json                                     — Changed speed→tier
data/fishing/spots.json                                 — NEW: fishing spot definitions
data/spawns/spawns.json                                 — Added 73 fishing spot spawns (IDs 2985-3057) — WRONG NPC IDs, needs fix
```

## Suggested Next Steps
1. **Fix fishing spot NPC IDs** — run `::findnpc fishing` to find correct cache IDs
2. **Firemaking** — use logs on tinderbox, play animation, spawn fire object, grant XP
3. **Commit all changes** — lots of uncommitted work
4. **Clean up debug logging** — revert log.info to log.debug in ObjectInteractionHandler
