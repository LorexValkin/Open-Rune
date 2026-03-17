# OpenRune - Installation & Setup Guide

## Prerequisites

### 1. JDK 21 (Required)
- Download: https://adoptium.net/temurin/releases/?version=21
- Pick **Windows x64 .msi** (or Mac/Linux equivalent)
- During install, CHECK **"Set JAVA_HOME variable"**
- Verify after install:
  ```
  java -version
  ```
  Should say `openjdk version "21.x.x"`

### 2. Gradle 8.6 (One-time bootstrap)
- Download: https://gradle.org/releases/ (binary-only zip for 8.6)
- Extract to `C:\gradle-8.6` (or anywhere you like)
- You only need this to generate the wrapper once

---

## First Time Setup

### Step 1: Clone or extract the project
```
git clone https://github.com/LorexValkin/Openrune.git
cd Openrune
```
Or extract the zip to a folder (avoid spaces in path if possible).

### Step 2: Set JAVA_HOME (every new terminal)
**PowerShell:**
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"
```
**CMD:**
```cmd
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot
```
**Linux/Mac:**
```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk
```

> **Tip:** The exact folder name depends on your install. Check what's in `C:\Program Files\Eclipse Adoptium\` and use that.

### Step 3: Generate Gradle wrapper
```
cd path\to\Openrune
C:\gradle-8.6\bin\gradle.bat wrapper --gradle-version 8.6
```
This creates `gradle/wrapper/gradle-wrapper.jar`. You only do this once (or after extracting a fresh zip).

### Step 4: Build the project
```
.\gradlew.bat build -x test
```
First build downloads dependencies (~2-3 min). Subsequent builds are fast (~5 sec).

### Step 5: Set up the 317 cache
```
.\setup-cache.bat
```
This copies cache files from `cache-data/` to `%USERPROFILE%\.openrune\cache\`. Only needed once per machine.

---

## Running

### Start the Server (Terminal 1)
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"
cd "path\to\Openrune"
.\gradlew.bat :core:run
```

You should see:
```
  OpenRune Server v0.1.0
  Server online on port 43594 (xxxms)
  Plugins: 0 enabled
  NPCs: 8 spawned
  Data stores: npcs, drops, saves, objects, shops, config, items, spawns, weapons
```

### Start the Client (Terminal 2)
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"
cd "path\to\Openrune"
.\gradlew.bat :client:run
```

The client window opens, loads the title screen from cache, and shows the login screen. Type a username/password and click login.

### Using Batch Scripts
After the first setup, you can use the convenience scripts:
- `start-server.bat` -- Starts the server
- `start-client.bat` -- Starts the client (runs cache setup if needed)
- `build.bat` -- Rebuild after code changes
- `setup-cache.bat` -- Copy cache files (one-time)

> **Note:** The batch scripts don't set JAVA_HOME. If your system JAVA_HOME points to Java 25 instead of 21, either set it system-wide or use the manual PowerShell commands above.

---

## Rebuilding After Changes

```
.\gradlew.bat build -x test
```

Or build specific modules:
```
.\gradlew.bat :core:build -x test     # Server only
.\gradlew.bat :client:compileJava     # Client only
.\gradlew.bat :api:build              # API module only
```

---

## Project Structure Quick Reference

```
Openrune/
  api/             # Plugin API interfaces (what plugins code against)
  core/            # Game server (engine, networking, world)
  cache/           # Cache reader module (Gradle module, not data)
  cache-data/      # 317 cache files (main_file_cache.dat, etc.)
  client/          # 317 game client (Java, modernized for JDK 21)
  launcher/        # Server launcher CLI
  plugins/         # Content plugins (skills, combat)
  data/            # JSON game data (editable at runtime)
    config/        # Server configuration
    npcs/          # NPC definitions
    items/         # Item definitions
    drops/         # Drop tables
    shops/         # Shop inventories
    spawns/        # NPC spawn locations
    weapons/       # Weapon stats
    objects/       # Object definitions
    saves/         # Player save files (auto-created)
  server/plugins/  # Compiled plugin JARs go here
```

---

## Troubleshooting

**"Could not find or load main class org.gradle.wrapper.GradleWrapperMain"**
Run the wrapper command: `C:\gradle-8.6\bin\gradle.bat wrapper --gradle-version 8.6`

**"JAVA_HOME is set to an invalid directory"**
Check the exact folder name: `dir "C:\Program Files\Eclipse Adoptium"`

**Build errors about Java version**
Make sure JAVA_HOME points to JDK 21, not 25. Check with: `& "$env:JAVA_HOME\bin\java.exe" -version`

**Client opens but crashes immediately**
Run `setup-cache.bat` first. The client needs cache files in `%USERPROFILE%\.openrune\cache\`.

**Client connects but disconnects after ~15 seconds**
Expected at current stage. The player update protocol needs completion (next phase of development).

**Server says "0 data stores" or "0 NPCs"**
The working directory is wrong. Make sure `core/build.gradle.kts` has:
```kotlin
tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}
```

---

## Admin Commands (In-Game)

Once logged in with a rights level 2+ account:
```
::tele x y [z]        Teleport to coordinates
::item id [amount]    Give yourself an item
::npc id              Spawn an NPC at your location
::removenpc           Remove nearby NPCs
::setlevel skill lvl  Set a skill level
::master              All skills to 99
::pos                 Show current position
::anim id             Play an animation
::gfx id              Play a graphic
::reload [store]      Reload JSON data
::plugins             List plugin status
::enableplugin id     Enable a plugin
::disableplugin id    Disable a plugin
::reloadplugin id     Hot-reload a plugin
::online              Player count
::save                Force save all players
::engine              Engine stats
```

## Server Console Commands

While the server is running via the launcher:
```
help          Show commands
players       List online players
npcs          NPC/object stats
engine        Engine tick stats
plugins       Plugin status
reload [name] Reload JSON data
enable id     Enable plugin
disable id    Disable plugin
stop          Shut down
```
