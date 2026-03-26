# OpenRune Session Handoff — 2026-03-26 (Tomorrow)
# Priority: Fix Client Model Rendering

## Context
The rev 232 dat2 cache upgrade is complete on both server and client. Server works perfectly. Client logs in and communicates but the 3D world renders with yellow triangle artifacts because OSRS models aren't being decompressed correctly.

## The Problem

Model ID 2403 loads as 255 bytes but claims 3846 vertices — the data is **still Container-wrapped** (compression header included in the model bytes). The `ContainerDecompressor.decompress()` in `OnDemandFetcher.getNextNode()` works for some models but not all.

**Likely cause:** The `checkReceived()` method at `OnDemandFetcher.java:505` reads model data from `decompressors[dataType + 1].decompress(id)` — this returns raw sector-chained bytes from the dat2 cache. For dat2, these bytes ARE a Container (compression header + compressed data). But `loadModelHeader()` is called with this data BEFORE `getNextNode()` decompresses it.

**The fix:** Container decompression needs to happen in `checkReceived()` (where data is first read from cache), NOT in `getNextNode()` (which runs later). Or `loadModelHeader` needs to decompress the container itself.

## Quick Investigation Steps

1. Add a print in `checkReceived()` to see if model data first bytes are `[0, ...]` (no compression), `[1, ...]` (bzip2), or `[2, ...]` (gzip) — Container format markers
2. If they ARE container-wrapped, add `ContainerDecompressor.decompress()` call in `checkReceived()` before setting `onDemandData.buffer`
3. Test: models should now have correct sizes and render properly

## What Works

| Component | Status |
|-----------|--------|
| Server dat2 cache | FULLY WORKING — 12K NPCs, 31K items, 4.3M placements |
| Client login/comms | Working — logs in, NPC spawns, chat |
| Client UI | Working — tabs, minimap, inventory |
| Client definitions | Working — 14K NPCs, 31K items, 57K objects from dat2 |
| Client map loading | Working — 2,383 regions mapped via djb2 ref table |
| Client XTEA keys | Working — 2,266 keys loaded |
| Client model rendering | BROKEN — yellow triangles, truncated model data |
| Client terrain | Partially working — some tiles render, XTEA-encrypted regions missing |
| Sprites/interfaces | Working — loaded from legacy 317 cache |

## Key Architectural Notes

- **Server cache path:** `~/.openrune/cache-232/cache/` (auto-detected by Server.kt)
- **Client cache path:** Signlink.getCacheDataDirectory() → dat2 location for dat/idx files
- **Legacy cache:** `~/.openrune/cache/` still needed for sprites, interfaces, fonts
- **Decompressor mapping:** `[1]=idx7 models, [2]=idx0 anims, [3]=idx4 sounds, [4]=idx5 maps, [5]=idx2 configs, [25]=idx255 meta`
- **Backup:** `~/.openrune/cache-194-backup.tar`

## Build Commands
```bash
# Server
cd Open-Rune
gradlew build -x test && gradlew :core:run

# Client
cd Open-Rune/client
find src -name "*.java" > /tmp/sources.txt
javac -d bin -cp "lib/*" @/tmp/sources.txt
java -XX:-OmitStackTraceInFastThrow \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
  --add-opens java.base/java.io=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.text=ALL-UNNAMED \
  --add-opens java.base/java.math=ALL-UNNAMED \
  --add-opens java.base/java.net=ALL-UNNAMED \
  --add-opens java.desktop/java.awt=ALL-UNNAMED \
  --add-opens java.desktop/java.awt.font=ALL-UNNAMED \
  --add-opens java.desktop/java.awt.color=ALL-UNNAMED \
  --add-opens java.desktop/java.awt.event=ALL-UNNAMED \
  --add-opens java.desktop/java.awt.image=ALL-UNNAMED \
  --add-opens java.desktop/sun.awt=ALL-UNNAMED \
  --add-opens java.desktop/sun.awt.image=ALL-UNNAMED \
  --add-opens java.desktop/sun.java2d=ALL-UNNAMED \
  --add-opens java.desktop/javax.swing=ALL-UNNAMED \
  -cp "bin;lib/*" com.client.Client

# Verify dat2 cache
gradlew :cache:verifyCacheDat2
```

## After Model Fix

Once models render correctly:
1. Generate doors.json from full 4.3M map dump (server has all data)
2. Fix door close system using authoritative cache rotations
3. Move remaining configs (animations, floors, identkits) to dat2
4. Remove dependency on legacy 317 cache
