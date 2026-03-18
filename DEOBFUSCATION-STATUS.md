# OpenRune Client Deobfuscation Status

**Last Updated:** March 18, 2026
**Progress:** 86% (1,250 / 1,441 symbols renamed)
**GitHub:** https://github.com/LorexValkin/Open-Rune
**Office PC repo:** `C:\Users\User\IdeaProjects\Open-Rune`
**Home PC repo:** `C:\Users\User\Desktop\Open Rune\openrune`
**Patches folder:** `C:\Users\User\Desktop\Open-Rune-Patches`
**Logs folder:** `$env:USERPROFILE\Desktop\Computer Works`

---

## CRITICAL: Patch Path Order

All patches MUST check IdeaProjects FIRST:
```powershell
$officePath = "C:\Users\User\IdeaProjects\Open-Rune"  # CHECK FIRST
$homePath   = "C:\Users\User\Desktop\Open Rune\openrune"
```
Earlier patches checked home first, causing cross-contamination. Fixed mid-session.

## CRITICAL: IdeaProjects Pre-Existing Renames

The IdeaProjects copy had methods/fields already renamed BEFORE our session. This caused duplicate declaration errors when our renames collided. Known collisions that were fixed:

| File | Our Rename | Collision | Fix Applied |
|------|-----------|-----------|-------------|
| Model.java | `method462 → getModel` | Already had `byte[] getModel(int)` (file reader) | Renamed file reader to `readModelFile` |
| TextDrawingArea.java | `method386 → drawText` | Already had `drawText` wrapper (centering) | Deleted duplicate wrapper |
| TextDrawingArea.java | `method384 → getTextWidth` | Already had `getTextWidth` (with @-tag handling) | Deleted duplicate simple version |
| Censor.java | `method585 → isLetter` | Already had `isLetter` (consonant checker) | Deleted bogus consonant version |
| DrawingArea.java | `anInt1387 → bottomY` | Already had `bottomY` (real clip boundary) | Renamed to `viewportHalfHeight` |
| Sprite.java | `anInt1444 → maxWidth`, `anInt1445 → maxHeight` | Already existed | Removed duplicate declarations |
| RSInterface.java | `anInt263 → invSpritePadX`, `anInt265 → invSpritePadY` | Already existed | Removed duplicate declarations |
| Varp.java | `anIntArray703 → cache` | Already had `Varp[] cache` | Renamed to `varpSettings` |
| client.java | `anIntArray1030 → chatRights` | Already existed | Removed duplicate + duplicate init |

## FIXED BUG: Model Occlusion on Camera Rotation

**Status:** RESOLVED (March 18, 2026)
**Symptom:** Models (walls, NPCs, items) disappeared and reappeared when rotating the camera in any direction, everywhere in the world.
**Root cause:** The 4 frustum cull early-exit checks in `Model.renderAtPoint` (lines ~1225-1238) used `DrawingArea.viewportHalfHeight` for bounding-sphere screen rejection. This static field is overwritten by `setDrawingArea()` calls for chat text, overhead text, and interface rendering mid-frame, causing it to hold incorrect values when some models are tested.
**Fix applied:** Removed all 4 frustum cull checks entirely. These were a 2004-era CPU optimization that skip model rendering when the bounding sphere is off-screen. The triangle rasterizer in `Texture.java` already clips per-scanline to screen bounds (`DrawingArea.bottomY`, `screenBoundX`), so nothing draws incorrectly without the checks. Zero visual difference, negligible CPU impact on modern hardware.
**Additional fixes applied during investigation:**
- `DrawingArea.centerY`: fixed initialization from `bottomX / 2` (width — wrong) to `bottomY / 2` (height — correct)
- `DrawingArea.centerX` renamed to `screenBoundX` across 35 references in 4 files (DrawingArea, Model, Texture, WorldController) — this field is `bottomX - 1` (max screen pixel index), not a center value
**Build note:** When modifying rendering code, always use `gradlew clean build -x test` — Gradle may cache stale `.class` files that mask or reintroduce bugs.
**Reference:** https://rune-server.org/threads/tilting-camera-backwards.585497/

## KNOWN BUG: Login Screen Layout

**Status:** Partially fixed. Login screen renders but positioning is slightly off.
**Cause:** Deleted `drawText` centering wrapper from TextDrawingArea. Added `drawCenteredText` as replacement but not all callers updated.

---

## Phase 1: Class Renames (20 classes)

All `ClassNN` and `ObjectN` files renamed. Old files deleted from repo.

| Old Name | New Name | Purpose |
|----------|----------|---------|
| Class4 | RotationUtil | Coordinate rotation math |
| Class6 | SoundTrack | Audio track synthesis |
| Class11 | CollisionMap | Tile collision flags |
| Class13 | BZip2Decoder | BZip2 decompression |
| Class18 | AnimBase | Animation skeleton base |
| Class21 | AnimTransform | Animation transform data |
| Class29 | SoundEnvelope | Sound envelope curves |
| Class30_Sub1 | SpawnObjectNode | Spawned object tracking |
| Class32 | BZip2State | BZip2 decompression state |
| Class33 | VertexNormal | 3D vertex normal data |
| Class36 | AnimFrame | Animation frame cache |
| Class39 | AudioFilter | Audio filter processing |
| Class40 | ShapedTile | Complex terrain tile |
| Class43 | PlainTile | Simple terrain tile |
| Class47 | Occluder | Occlusion culling planes |
| Object1 | WallObject | Wall scene object |
| Object2 | WallDecoration | Wall decoration object |
| Object3 | GroundDecoration | Ground decoration object |
| Object4 | GroundItemPile | Ground item stack |
| Object5 | InteractiveObject | Interactive scene object |

## Phase 1b-1e: Method Renames (363 methods, ZERO remaining)

All `methodNNN` identifiers renamed across every file. Key files:

- **CollisionMap:** 12 methods (addWall, addObject, setBlocked, etc.)
- **ObjectManager:** 20 methods (parseTileData, placeObject, parseLandscape, etc.)
- **WorldController:** 49 methods (addTile, addWallObject, renderScene, etc.)
- **Stream:** 23 methods (readUnsignedByte, readString, writeWord, etc.)
- **Model:** 29 methods (applyTransform, calculateBounds, rotateY90, etc.)
- **DrawingArea:** 6 methods (drawFilledRectangle, etc.)
- **Sprite:** 11 methods (drawSprite, drawAdvanced, etc.)
- **TextDrawingArea:** 14 methods (drawText, getTextWidth, drawGlyph, etc.)
- **Texture:** 14 methods (drawTriangle, drawScanline, etc.)
- **client.java:** 48 methods (processGameLoop, drawGameScreen, etc.)
- **OnDemandFetcher:** 10 methods
- **Censor:** 27 methods
- Others: ~94 methods across remaining files

## Phase 2a: ObjectDef Fields (30 fields)

sizeX, sizeY, modelIds, modelTypes, blocksProjectile, impenetrable, contouredGround, mergeNormals, animationId, scaleX/Y/Z, translateX/Y/Z, varbitId, settingId, childrenIDs, interactable, castsShadow, name, actions, objectHeight, originalColors, modifiedColors, description, isRotated, mapIcon, mapScene, invertClipSize, supportItems, ambient, contrast, options

## Phase 2b: Scene Object Fields (115 fields)

Across Ground, PlainTile, ShapedTile, Occluder, WallObject, WallDecoration, GroundDecoration, InteractiveObject, GroundItemPile, SpawnObjectNode, CollisionMap — all tile/scene object fields fully named.

## Phase 2c: ObjectManager + Flo Fields (34 fields)

**ObjectManager:** tileHeights, overlayFloorIds, tileShadowMap, tileLightIntensity, overlayShapes, underlayFloorIds, overlayOrientations, tileSettings, lightmap, mapSizeX/Y, minimumPlane, currentPlane, blendHue/Saturation/Lightness/HueDivisor/DirectionCount, hueRandomizer, lightnessRandomizer, DIRECTION_OFFSET_X/Y, WALL_DECO_ROT_OFFSET, WALL_TYPE_FLAGS, WALL_ROTATION_FLAGS

**Flo:** rgb, textureId, occluding, hue, saturation, lightness, blendHue, hslWeight, blendedHSL

## Phase 2d: Entity + Player Fields (51 fields)

**Entity:** stepDelayCounter, turnSpeed, walkAnimId, tileSize, pathRemainder, pathRunning, standAnimId, turnAnimId, turnAroundAnimId, movementAnimId/Frame/Cycle, animFrame/Cycle/Delay/FrameCount/ResetCycle, animStretches, spotAnimId/Frame/Cycle/Delay/Height, textColor/Effect/Alpha, forceMoveStartX/EndX/StartY/EndY/EndCycle/StartCycle/Direction, faceAngle, walkBackAnimId, walkLeftAnimId, walkRightAnimId, runAnimId

**Player:** cachedModelKey, lowDetail, bodyColors, gender, attachedModelStartCycle/EndCycle/Height/X/OffsetY/Y, attachedModel, objectAppearanceStartIndex, appearanceHash

## Phase 2e: Model + Animation Fields (100 fields)

**Model instance:** vertexCount, vertexX/Y/Z, faceCount, faceVertexA/B/C, texturedFaceCount, texTriangleA/B/C, faceColorA/B/C, faceRenderType, faceRenderPriorities, faceAlphas, faceColors, facePriority, vertexLabels, faceLabels, boundsMinX/MaxX/MaxZ/MinZ/XZRadius/BottomY, boundsSphereRadius, boundsNearRadius, objectHeight, labelGroups, labelGroupsUnused, singleTile, mergedNormals

**Model static:** sharedModel, tmpVertexX/Y/Z/W, faceNearClipped, faceClippedX, projectedVertexX/Y/Z, depthBuffer, screenXVertices, screenYVertices, depthFaceCount, depthFaceIndices, priorityFaceCount, priorityFaceIndices, facePriorityDepthSum, normalFaceDepth, priorityDepthSum, tmpFaceA/B/C, transformTempX/Y/Z, mousePickingEnabled, mousePickX/Y/Count/Results, SINE, COSINE, HSL_TO_RGB, LIGHT_DECAY, modelHeaders, onDemandFetcher

**VertexNormal:** x, y, z, magnitude

**AnimTransform:** rawData, vertexCount, faceCount, texturedFaceCount, vertexFlagsOffset, vertexX/Y/ZOffset, vertexLabelsOffset, faceVerticesOffset, faceTypesOffset, faceColorsOffset, faceTexturesOffset, facePrioritiesOffset, faceAlphasOffset, faceLabelsOffset, texCoordOffset

**AnimFrame:** frameCache, displayLength, base, transformCount, transformTypes, transformX/Y/Z, frameMissing

## Phase 2f: WorldController Fields (64 fields)

renderEnabled, planeCount, mapSizeX/Y, tileHeightMap, currentPlane, renderFlags, renderedTileCount, renderPlane, renderCycle, viewMinTileX/Y, viewMaxTileX/Y, cameraTileX/Y, cameraWorldX/Y/Z, cameraPitchSin/Cos, cameraYawSin/Cos, clickPending, clickScreenX/Y, maxOccluderPlanes, occluderCount, activeOccluderCount, tileQueue, visibilityMatrix, currentVisibility, midX/Y, leftX, rightX, topY, bottomY, mergedObjects, vertexMergeTagA/B, mergeIndex, MINIMAP_WALL_X1/Y1/X2/Y2, MINIMAP_COLORS/SATURATION/DIRECTION_TYPE/ADJ_NE/NW/SE/SW, MINIMAP_HSL_OVERRIDE, TILE_SHAPE_MASKS, TILE_ROTATION_INDICES

**Ground fields:** visible, rendered, hasObjects, wallCullDirection, wallCullPlane0/1/2/3

## Phase 2g: EntityDef + Animation + SpotAnim + VarBit + ItemDef Fields (68 fields)

**EntityDef:** walkBackAnim, cacheIndex, varbitId, turnAroundAnim, settingId, decompressedLength, tileSpan, headModelIds, headIcon, degreesToTurn, walkRightAnim, clickable, ambient, scaleY, drawOnMinimap, scaleXZ, contrast, priorityRender

**Animation:** frameCount, frameIds, frameDelays, framePadding, loopOffset, interleaveOrder, stretches, priority, rightHandItem, leftHandItem, maxLoops, precedenceAnimating, walkMerge, replayMode, animCount

**SpotAnim:** unk400, id, modelId, animationId, animation, originalColors, modifiedColors, scaleXY, scaleZ, rotation, ambient, contrast, modelCache

**VarBit:** settingIndex, lowBit, highBit

**ItemDef:** femaleEquipYOffset, femaleDialogueModel, maleDialogueHatModel, femaleDialogueHatModel, resizeX, noteInfoId, maleDialogueModel, lightIntensity, maleEquipYOffset, modelPositionX, resizeZ, resizeY, lightMagnitude, notedItemId, modelPositionZ, modelPositionY, ambient2, maleEquipYOffset2

**Animable:** vertexNormals

## Phase 2h: Texture + Animable_Sub3/4/5 + Misc Fields (71 fields)

**Texture:** opaque, textureTranslucent, textureMipmap, textureCycle, reciprocal, lightDecay, SINE, COSINE, scanlineOffset, textureCount, textures, textureLoaded, averageTextureColor, texturePoolSize, texturePixelCache, textureLastUsed, textureLastCycle, textureCycleCounter, HSL_TO_RGB, texturePixels

**DrawingArea:** bottomY, viewportHalfHeight (= bottomY / 2), centerY (= bottomY / 2, was incorrectly bottomX / 2), screenBoundX (was centerX = bottomX - 1, renamed to prevent confusion with a center value)

**Animable_Sub3 (SpotAnimEntity):** plane, startX, startY, startZ, endCycle, finished, spotAnim, animFrame, animCycle

**Animable_Sub4 (Projectile):** startCycle, endCycle, velocityX/Y/XY/Z, accelerationZ, moving, startX/Y/Z, targetEntityIndex, currentX/Y/Z, slopeAngle, startSpeed, targetLocSize, spotAnim, animFrame, animCycle, yawAngle, pitchAngle, sourceEntityIndex

**Animable_Sub5 (AnimableObject):** animFrame, childrenIDs, varbitId, settingId, southWestX/Y, northEastX/Y, animation, animStartCycle, objectId, objectType, objectFace

**Background:** textureWidth
**RSImageProducer:** pixels, width
**OnDemandFetcher:** streamOffset

## Phase 2i: Scattered Fields (116 fields)

**RSInterface:** animationId, scriptDefaults, enabledColor, disabledColor, textCentered, enabledText, enabledMediaType, filled, hoverColor, scriptCompareType, enabledSpriteId, opacity, disabledMediaType, enabledMedia, enabledAnimation, disabledAnimation, replaceItems, invSpritePadX/Y

**SoundEnvelope:** segmentCount, segmentDuration, segmentPeak, formDuration, formStart, formEnd, currentSegment, segmentPosition, currentPeak, nextPeak, interpolationStep, sampleRate

**SoundTrack:** pitchEnvelope, volumeEnvelope, pitchModEnvelope, pitchModRangeEnvelope, gatingEnvelope, gatingFreqEnvelope, filterEnvelope, filterRangeEnvelope, oscillatorVolume/Pitch/Delay, delayTime, delayDecay, releaseEnvelope, duration, offset, oscillatorFrequency/Phase/Amplitude/Semitone/Start/VolumeDelta/PitchDelta/MinDelay, outputSize, outputBuffer, mixBuffer

**Sounds:** tracks, loopCount, loopStart, delays

**IDK:** bodyPartId, bodyModelIds, originalColors, modifiedColors, headModelIds

**Background:** height, offsetX, offsetY, maxHeight

**Sprite:** offsetX, offsetY, maxWidth, maxHeight

**TextDrawingArea:** glyphPixels, glyphWidth, glyphHeight, glyphOffsetX, glyphOffsetY, charWidth, fontHeight

**StreamLoader:** nameHashes, fileSizes, decompressedSizes, fileOffsets, isCompressed

**Stream:** BIT_MASKS

**RSApplet:** targetFps, debugFlags

**Varp:** type, usage, varpSettings (renamed from cache collision)

**AnimBase:** transformCount, transformTypes, transformLabels

**Censor:** badWordHashes, hostHashes, tldHashes, badWordFragments, badCombinations

**Item:** itemQuantity

**Texture:** textureConst

## Phase 2j: client.java Fields (218 fields)

**Connection/Login:** serverSeed, constructMapTiles, npcUpdateTypes, npcLocalIndices, midiFading, loginStream, npcUpdateCount, entityUpdateIndices/Count/Index, chatScrollMax, clickToContinueString, outStream, pendingInput, lastKnownPlane, entityUpdateX/Y/Id/Face, cameraAngle, minimapRotation

**Drawing/UI:** continuedDialogue, tabAreaFlashCycle, tabFlashIndex, sidebarFlashing, lastItemSelectedSlot/Interface, entityCount, entityIndices, playerBuffers, lastChatId, mapRegionCount, mapRegions, mapSize, terrainData/Index, walkingQueueSize/X/Y, constructRegionData

**Camera:** cameraTargetIndex, cameraTargetTileX/Y, cameraTargetHeight, cameraTargetLocalX/Y/Z, cameraPosX/Y/Height, cameraSpeed, cameraAcceleration, cameraSmoothedX/Y, cameraLocX/Y/Height/Speed/Accel, cameraOscillationH/Speed

**Minimap:** loginFireLeft/Right, flameLeftX/RightX, minimapZoomDelta/Target/Zoom, minimapRotationDelta, minimapImages, minimapHintX/Y, minimapSprite

**Overhead text:** maxOverheadCount, overheadX/Y/Height/Width/TextColor/TextEffect/TextCycle/TextStr

**RSImageProducers:** tabImageProducer, mapAreaIP, gameScreenIP, chatAreaIP, chatSettingIP, topSideIP1/2, bottomSideIP1/2, titleIP1/2/3, titleMuralIP, titleButtonIP, loginMsgIP, topCenterIP

**Sprites:** chatAreaBackground, chatSettingsBackground, loginBoxSprite, loginDetailSprite, loginScreenSprites

**Fonts:** boldFont, fancyFont

**Chat:** chatAreaScrollPos/Max, chatScrollAmount, chatFilterScrollMax, chatFilterOffsets/Types/Names/Messages, chatRights, chatColors, inputTitle, chatLastTyped

**Map loading:** regionAbsBaseX/Y, mapRegionX/Y, mapFunctionCount/X/Y, mapChunkX/X2/Y2, mapChunkLandscapeIds, mapLandscapeData, mapObjectData/Ids, mapLoadProgress/State, lastMapRegionX/Y

**Interfaces:** activeInterfaceId, flashingTab, flashingSideicon, chatboxInterface, tabFlashTimer/CycleAlt, actionType, clickMode, spellCastOnType, selectedArea, selectedInventorySlot/Interface

**Hint icons:** hintArrowType, hintText, hintIconNpcIndex, hintIconPlayerIndex, hintIconX/Y/DrawX/DrawY, hintIconDelay

**Movement:** walkDest/X/Y, walkPathLength, walkQueueLength, moveItemSlotStart/End/InterfaceId

**Drag:** dragFromSlotInterface/Slot, dragStartX/Y

**Projectiles/spotanims:** projectileList, spotAnimList, spawnObjectList

**Camera modes:** songSwitchDelay, songSwitching

**Misc:** XP_TABLE, SKIN_COLORS, compassWidth, tileFlags, tabAreaX/Y, menuActionCounter/Types, menuActionCmd4/5, maxMenuEntries, pixelData, clickedTileX/Y, scrollBarDrag, lastClickTime, loginTimer, idleTime, idleLogout, tabToReplyPM, serverUpdateCounter, walkPathLength

---

## Remaining (~191 fields, 14% of total)

### Low priority (BZip2 compression internals — 76 fields)
- BZip2Decoder.java: 38 fields
- BZip2State.java: 38 fields

### Medium priority (scattered singles — 57 fields)
- client.java: 58 remaining (mostly ambiguous state flags like anInt1079, anInt1104, etc.)
- OnDemandFetcher.java: 8 (network/caching internals)
- AudioFilter.java: 5 (DSP internals)
- Censor.java: 6 (word filter internals)
- Player.java: 5 (anInt1719-1722 unknown purpose)
- RSInterface.java: 3
- Sounds.java: 3
- RSApplet.java: 2
- RSImageProducer.java: 2
- Stream.java: 2
- TextInput.java: 2
- VarBit.java: 2
- WorldController.java: 2
- 10 files with 1 remaining each

---

## Files 100% Deobfuscated

CollisionMap, Flo, VertexNormal, AnimTransform, AnimFrame, Ground, PlainTile, ShapedTile, Occluder, WallObject, WallDecoration, GroundDecoration, GroundItemPile, InteractiveObject, SpawnObjectNode, Animable_Sub3, Animable_Sub4, Animable_Sub5

## Files 95%+ Deobfuscated

Model, WorldController, ObjectDef, ObjectManager, Entity, Player, Animation, SpotAnim, EntityDef, ItemDef, Texture, DrawingArea, TextDrawingArea, Sprite, Background, StreamLoader, Varp, VarBit, IDK, SoundEnvelope, SoundTrack, Sounds, client.java

---

## Server-side Kotlin Fixes Applied

Two orphaned debug log lines fixed during session:
- `core/src/main/kotlin/com/openrune/core/net/handler/PacketDispatcher.kt` lines 309-311 deleted
- `core/src/main/kotlin/com/openrune/core/world/movement/MovementProcessor.kt` lines 167-168 deleted

## Build Notes

- **Gradle task:** `.\gradlew.bat build -x test` (compile only)
- **Client launch:** `.\gradlew.bat :client:run` (requires `client/build.gradle.kts` with application plugin)
- **Play.bat** calls Start-Client.bat which uses `:client:run`
- `client/build.gradle.kts` was accidentally deleted during file copy — restored with application plugin pointing to `client` main class
- **IMPORTANT:** Always use `.\gradlew.bat clean build -x test` when modifying rendering code — Gradle caches `.class` files aggressively and stale classes can mask or reintroduce bugs
- Never mention Computer Works in OpenRune/RSPS project files
