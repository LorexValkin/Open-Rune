# OpenRune Session Handoff — 2026-03-25 (Session 5)
# Cache Upgrade: Rev 232 dat2 — Server + Client

## Summary
Both server and client now load from the rev 232 dat2 cache. Server fully works. Client logs in, communicates, renders UI, but the 3D world has yellow triangle artifacts from model format issues.

## Server: FULLY WORKING
- 12,293 NPCs, 31,172 items, 21,391 objects from dat2
- 4.3M object placements from 2,266 XTEA-decrypted regions
- Map loading via djb2 name hash → reference table lookup

## Client: PARTIALLY WORKING
- Login, server comms, UI, minimap all work
- 14,793 NPCs / 31,172 items / 57,690 objects loaded from dat2
- 2,383 map regions mapped via dat2 reference table
- 2,266 XTEA keys loaded for map decryption
- OSRS model format detection (FF FF/FE/FD magic bytes)
- Legacy 317 cache used for sprites, interfaces, fonts, animations

### Remaining Issue: Yellow Triangle Models
- Some OSRS models have garbage vertex/face counts (e.g., 3846 verts in 255 bytes)
- Root cause: models from idx7 may need Container decompression at a different layer, or the index mapping is wrong
- The `read525Model`/`read622Model` parsers handle OSRS format but some data is truncated
- Need to investigate: is `decompressors[1]` (mapped to idx7) returning complete model containers?

### Files Changed (Client)
```
Signlink.java          — isDat2, getCacheDataDirectory()
Decompressor.java      — extended sector headers
ContainerDecompressor.java — NEW: dat2 container decompression + XTEA
Dat2ConfigLoader.java  — NEW: reference table + group splitting
Buffer.java            — null-terminated strings
Client.java            — index remapping, dat2 config, legacy fallback
Model.java             — OSRS format detection, read525Model hasFaceRenderTypes fix, crash protection
OnDemandFetcher.java   — Container decompress, XTEA keys, dat2 map index (djb2 ref table)
ObjectManager.java     — try-catch on renderObject, lighting, AnimatedSceneObject
ObjectDefinition.java  — dat2 loading, forID bounds, try-catch readValues
ItemDefinition.java    — dat2 loading, forID bounds
NpcDefinition.java     — dat2 loading, forID bounds
```

### Next Session: Fix Model Rendering
1. Debug why model 2403 has 255 bytes but claims 3846 vertices
2. Check if ContainerDecompressor output matches expected model sizes
3. Verify idx7 → decompressors[1] mapping produces valid model containers
4. May need to add Container decompression in `checkReceived()` before `loadModelHeader`
