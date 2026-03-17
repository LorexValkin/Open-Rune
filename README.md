# OpenRune - Modular RS2 317 Game Engine

A from-scratch, plugin-based RS2 game server targeting the 317 protocol. Built with Kotlin, Netty, and a fully modular architecture where every piece of game content is a hot-swappable plugin.

## Architecture

```
openrune/
├── api/           # Pure interfaces & events - what plugins code against
├── core/          # Engine: networking, tick loop, player management
├── cache/         # 317 cache reader & definition extractor
├── launcher/      # CLI launcher for managing plugins & data
├── plugins/       # Content plugins (skills, combat, etc.)
│   ├── skills-plugin/
│   └── combat-plugin/
├── data/          # JSON game data (hot-reloadable)
│   ├── config/    # Server config + plugin manifest
│   ├── npcs/      # NPC definitions
│   ├── items/     # Item definitions
│   ├── objects/   # Object definitions
│   ├── spawns/    # NPC spawn locations
│   ├── drops/     # NPC drop tables
│   ├── shops/     # Shop inventories
│   ├── weapons/   # Weapon stats & animations
│   └── saves/     # Player save files (JSON)
└── server/
    └── plugins/   # Compiled plugin JARs go here
```

## Key Concepts

### Plugin System
Every piece of game content is a plugin JAR in `server/plugins/`. Plugins:
- Implement `OpenRunePlugin` and annotate with `@PluginInfo`
- Register event handlers via the `EventBus`
- Access game data through the `DataStore` API
- Can be enabled/disabled/reloaded at runtime without restart
- Have dependency resolution (plugin A can require plugin B)
- Are isolated via separate classloaders for true hot-swap

### Event-Driven Architecture
The engine communicates with plugins through typed events:
- `PlayerLoginEvent`, `CommandEvent`, `ObjectInteractEvent`, etc.
- Events have priorities (MONITOR -> LOW -> NORMAL -> HIGH -> HIGHEST)
- Events can be cancelled to prevent default behavior
- Plugins emit events to trigger other plugin behavior

### JSON Data System
All game definitions live in `data/*.json`:
- Edit any JSON file while the server is running
- Changes are automatically detected and reloaded
- Use `::reload` in-game or the launcher console
- No recompilation needed for data changes

### Clean Separation
- **api/** module has zero implementation dependencies
- Plugins only depend on `api/`, never on `core/`
- The engine is protocol-agnostic in its plugin interface
- Could support 562, OSRS, or custom protocols with core changes

## Building

```bash
# Build everything
./gradlew build

# Build plugin JARs
./gradlew :plugins:skills-plugin:jar
./gradlew :plugins:combat-plugin:jar

# Copy plugin JARs to server/plugins/
cp plugins/skills-plugin/build/libs/skills-plugin-0.1.0.jar server/plugins/
cp plugins/combat-plugin/build/libs/combat-plugin-0.1.0.jar server/plugins/
```

## Running

### Via Launcher (interactive mode)
```bash
./gradlew :launcher:run
```
Gives you a menu to:
- Toggle plugins on/off
- Browse data files
- View server config
- Start the server

### Direct Start
```bash
./gradlew :launcher:run --args="--start"
```
Or:
```bash
./gradlew :core:run
```

### Server Console Commands
Once running, the launcher provides a server console:
```
server> help
server> players
server> plugins
server> reload npcs
server> enable combat
server> disable skills
server> stop
```

### In-Game Admin Commands
```
::reload          - Reload all JSON data
::reload npcs     - Reload specific data store
::plugins         - List plugin status
::enableplugin X  - Enable plugin at runtime
::disableplugin X - Disable plugin at runtime
::reloadplugin X  - Hot-reload a plugin JAR
::tele X Y [Z]    - Teleport
::online          - Player count
::save            - Force save all players
```

## Writing a Plugin

```kotlin
@PluginInfo(
    id = "my-content",
    name = "My Content Pack",
    version = "1.0.0",
    description = "Adds custom content",
    dependencies = ["skills"],  // Optional: requires skills plugin
    hotSwappable = true
)
class MyPlugin : OpenRunePlugin() {

    override fun onEnable() {
        // Subscribe to events
        context.events.on<CommandEvent>(owner = info.id) { event ->
            if (event.command == "hello") {
                event.player.sendMessage("Hello from my plugin!")
                event.cancel()
            }
        }

        context.events.on<ObjectInteractEvent>(owner = info.id) { event ->
            if (event.objectId == 1234) {
                event.player.sendMessage("You clicked my custom object!")
                event.cancel()
            }
        }

        // Schedule repeating task
        context.schedule(delayTicks = 10, repeatTicks = 100) {
            for (player in context.players) {
                player.sendMessage("Server announcement!")
            }
        }

        // Access JSON data
        val npcDef = context.data.getTyped("npcs", 1, NpcDef::class.java)
        context.log("Loaded NPC: ${npcDef?.name}")
    }
}
```

Build your plugin, drop the JAR in `server/plugins/`, enable it from the launcher.

## Technology Stack
- **Kotlin** (JVM 21) - Clean, null-safe, coroutine-ready
- **Netty 4.1** - High-performance async networking
- **Gson** - JSON data serialization
- **SLF4J + Logback** - Structured logging
- **Gradle** - Multi-module build system

## Data File Format

### NPC Definition (data/npcs/npcs.json)
```json
{
  "id": 1,
  "name": "Man",
  "combatLevel": 2,
  "hitpoints": 7,
  "maxHit": 1,
  "attackSpeed": 4,
  "respawnTicks": 25,
  "aggressive": false,
  "attackAnim": 422,
  "defenceAnim": 424,
  "deathAnim": 836
}
```

### Drop Table (data/drops/drops.json)
```json
{
  "id": 1,
  "npcId": 1,
  "npcName": "Man",
  "drops": [
    {"itemId": 526, "minAmount": 1, "maxAmount": 1, "chance": 1.0, "table": "ALWAYS"},
    {"itemId": 995, "minAmount": 1, "maxAmount": 3, "chance": 0.5, "table": "MAIN"}
  ]
}
```

### Player Save (data/saves/player.json)
```json
{
  "version": 1,
  "username": "Player",
  "password": "...",
  "rights": 2,
  "position": {"x": 3222, "y": 3218, "z": 0},
  "skills": [
    {"level": 99, "experience": 13034431.0},
    ...
  ],
  "inventory": [
    {"id": 4151, "amount": 1},
    ...
  ]
}
```

## Roadmap
- [ ] Full player update protocol (bit-packing)
- [ ] NPC system with AI plugins
- [ ] Pathfinding and collision
- [ ] Cache definition extractor (auto-populate data/ from cache)
- [ ] Map editor integration
- [ ] Client modernization (Java 21)
- [ ] GUI launcher (JavaFX or web-based)
- [ ] 562 interface plugin support
- [ ] Smooth animation client plugin
