# OpenRune 317 — Session Handoff (March 18, 2026)

## Project Location
- **Office**: `C:\Users\User\IdeaProjects\Open-Rune`
- **Home**: `C:\Users\User\Desktop\Open Rune\openrune`
- **GitHub**: LorexValkin/Open-Rune
- **Patches dir**: `%USERPROFILE%\Desktop\Computer Works\OpenRune-Patches\`
- **Build**: `.\gradlew.bat clean build -x test --no-build-cache --no-daemon`
- **Run**: `.\gradlew.bat :client:run --no-daemon`
- **Full recompile**: stop daemons, delete .gradle/build/client\build, then build
- **Patches**: PowerShell 7 — `pwsh -ExecutionPolicy Bypass -File <path>`

## Key Rule
- **Never mention Computer Works in OpenRune/RSPS project files**

---

## What Was Done This Session

### Resizable UI (Phase C) — COMPLETE
All patches applied and working:

1. **drawTabArea restructured** (OpenRune-RefMatch.ps1) — matches reference exactly:
   - Sprites 27/28/29 + bg fill now drawn inside drawTabArea (not drawUnfixedGame)
   - Wide (>=1000px): sprite 27 + 29 at cW-204/cH-310, interface at x1+28/y1+37
   - Narrow (<1000px): sprite 28 + 29 at cW-222/cH-346, interface at cW-214/cH-339
   - showTab guard on panel + interface in resizable

2. **drawRedStones + drawSideIcons** — resizable branches added:
   - Fixed mode: uses `redStones[]` and `sideIcons[]` arrays
   - Resizable: uses `cacheSprite[4-8]` for redstones, `cacheSprite[9-22]` for icons
   - Wide/narrow layouts match reference positions

3. **Right-click menus fixed**:
   - determineMenuSize: areas 1/2 restricted to fixed only (area 0 covers all in resizable)
   - processMenuClick: area 1/2/3 offsets conditional on clientSize==0
   - drawMenu: same treatment via regex (9 blocks fixed)
   - processRightClick: canClick() gate added, tab area buildInterfaceMenu uses dynamic X/Y

4. **canClick() + helpers** added — blocks walk-through on chat, tabs, minimap, tab panel

5. **processTabClick** — resizable click bounds for narrow (2 rows) and wide (33px loop)

6. **processChatModeClick** — Y offset for resizable: `y = clientHeight - 503`

7. **Chat area right-click** — dynamic Y bounds for buildChatAreaMenu

8. **Draw order fixed** — `drawUnfixedGame(); draw3dScreen();` (was reversed)

9. **Loading bar** — restored to reference red bar, text fixed with `drawRightAligned` for centering. Blit at (171, 202) on gameScreenIP.

10. **Password Enter** — triggers `login()` from password field (Tab still switches fields)

11. **Loading text** ("Loading - please wait") — centered for resizable in loadingStages()

12. **determineMenuSize area 0** — position math uses clientWidth/clientHeight in resizable

### Fields Added This Session
```java
// In client.java
public static boolean showTab = true;
public static int smallTabs = 1000;
public static boolean showChat = true;
public static int channel;

// In Entity.java (can be removed — position interpolation not working)
public int prevX, prevY, trueX, trueY;
```

### Methods Added This Session
```java
// In client.java
public boolean canClick()           // UI click guard
public boolean mouseInRegion(x1,y1,x2,y2)
public boolean mouseInRegion2(x1,x2,y1,y2)
public boolean clickInRegion(x1,y1,x2,y2)
public void saveEntityPositions()   // Position interp (not visually working)
private void applyInterpolation()   // Position interp (not visually working)
private void interpolateEntity()    // Position interp (not visually working)
private void restoreEntityPositions()
private void restoreEntity()

// In RSApplet.java
void saveEntityPositions() { }      // Stub for override
public long lastTickNanos
public static boolean smoothAnimation = true;
```

### Position Interpolation Status
**Not visually effective** — the RS engine already moves entities smoothly pixel-by-pixel via `entity.x/y` updates each tick. There's no tile-snapping to fix. The real "animation smoothing" (RuneLite style) is **animation frame interpolation** — blending bone transforms between keyframes. This is what needs to be done next.

---

## Next Session: Animation Frame Smoothing

### What RuneLite Actually Does
RuneLite's "Animation Smoothing" plugin calls `client.setAnimationInterpolationFilter()` which tells the engine to **lerp between animation keyframes**. In a 317 client, this means modifying `Model.applyTransform()` to blend between the current frame and next frame.

### Your Code Structure
```
Model.applyTransform(int frameId)
  → AnimFrame.getFrame(frameId)
  → loops animFrame.transformCount
  → calls recolorTriangle(labels, types, x, y, z)  // applies bone transform

Player.getRotatedModel()
  → getPlayerModel()
  → model.applyTransform(spotAnim frame)  // for spot anims

AnimFrame fields: transformCount, transformTypes[], transformX/Y/Z[]
AnimBase fields: badCombinations[], badWordFragments[]  (label groups)
```

### Implementation Plan
1. **Add `applyTransformInterpolated(int frame1, int frame2, int cycle, int duration)`** to Model.java
   - Gets both AnimFrames
   - For each transform: lerps X/Y/Z values by `cycle/duration` factor
   - Calls recolorTriangle with interpolated values

2. **Modify getPlayerModel() / NPC equivalent** to pass both current frame + next frame + cycle info

3. **Track animation cycle progress** — `animCycle` and animation duration per frame already exist on Entity

4. **Toggle** via `::smooth` command (already wired, just needs the real impl)

5. **Exclusion filter** — some animations break with interpolation (pure rotations). Start simple, add blocklist later.

### Key Files
| File | What to modify |
|------|---------------|
| `Model.java` | Add `applyTransformInterpolated()` method |
| `Entity.java` | Already has animFrame, animCycle, animDelay |
| `Player.java` | `getPlayerModel()` calls applyTransform — modify to use interpolated version |
| `NPC.java` | Same treatment as Player |
| `AnimFrame.java` | Read-only, provides frame data |
| `AnimBase.java` | Read-only, provides transform types |
| `Animation.java` | Has frameIds[], frameDurations[] (frame timing) |

### Reference Code
```java
// Current:
model.applyTransform(animation.frameIds[entity.animFrame]);

// Interpolated:
if (smoothAnimation && entity.animFrame + 1 < animation.frameCount) {
    int frame1 = animation.frameIds[entity.animFrame];
    int frame2 = animation.frameIds[entity.animFrame + 1];
    int cycle = entity.animCycle;
    int duration = animation.frameDurations[entity.animFrame]; // or similar
    model.applyTransformInterpolated(frame1, frame2, cycle, duration);
} else {
    model.applyTransform(animation.frameIds[entity.animFrame]);
}
```

---

## Still Outstanding (Lower Priority)

### P5: Minimap Content
`drawMinimap()` not called from `drawUnfixedGame()`. Needs same pattern as drawChatArea/drawTabArea.

### Anti-Aliasing (Priority 2)
Post-process AA on the 3D viewport. Options: FXAA shader, or supersampling.

### Bilinear Texture Filtering (Priority 3)  
Modify `Texture.java` scanline rendering to interpolate between texels.

### Cleanup
- Remove position interpolation code (prevX/prevY/trueX/trueY, saveEntityPositions, etc.) since it's not effective
- Or keep it as a no-op toggle for potential future use
- Clean up .bak files from repo

---

## File Locations

| File | Purpose |
|------|---------|
| `client\src\main\java\client.java` | Main client (~420K chars) |
| `client\src\main\java\RSApplet.java` | Game loop, mouse/keyboard |
| `client\src\main\java\Entity.java` | Base entity (x/y, anim fields) |
| `client\src\main\java\Player.java` | Player rendering (getRotatedModel) |
| `client\src\main\java\NPC.java` | NPC rendering |
| `client\src\main\java\Model.java` | 3D model + applyTransform |
| `client\src\main\java\AnimFrame.java` | Animation frame data |
| `client\src\main\java\AnimBase.java` | Animation bone base |
| `client\src\main\java\Animation.java` | Animation sequence (frameIds, durations) |
| `client\src\main\java\DrawingArea.java` | Pixel drawing + setDrawingArea clipping |
| `client\src\main\java\Sprite.java` | PNG sprite loading |

## Sprite Index Map (resizable)
| Index | Sprite | Used In |
|-------|--------|---------|
| 4-8 | Tab redstone highlights | drawRedStones (cacheSprite) |
| 9-22 | Tab side icons | drawSideIcons (cacheSprite) |
| 27 | Tab bar wide | drawTabArea |
| 28 | Tab bar narrow | drawTabArea |
| 29 | Tab inventory panel | drawTabArea |
| 30 | Chat background | drawUnfixedGame |
| 31 | Chat input bar | drawUnfixedGame |
| 33 | Minimap frame | drawUnfixedGame |

## Commands
- `::fixed` — switch to fixed 765x503
- `::resize` — switch to resizable
- `::fullscreen` — maximize
- `::zoomreset` — reset camera zoom
- `::smooth` — toggle animation smoothing (wired but not visually effective yet)
