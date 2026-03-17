# OpenRune Fix 2 -- ISAAC Cipher Desync + Client Null Guards
# Computer Works DBA -- Chase Foster
# Date: 2026-03-16

## Root Cause

The ISAAC cipher implementation on the server did not match the client.
This caused EVERY server->client packet opcode to decrypt to garbage,
making all packets after login unreadable. The T1/T2 errors in the
client console, the interface NPE, and the broken rendering were all
symptoms of this single root cause.

## Two bugs in IsaacCipher.kt

1. READ ORDER: Server read results forward (index 0, 1, 2...).
   Client reads results BACKWARD (index 255, 254, 253...).

2. DOUBLE GENERATION: Server generated the ISAAC block once during
   initialize(), then generated it AGAIN on the first nextValue()
   call (because resultIndex started at 256). Client only generates
   once during init, then reads from count=256 downward.

## Files Changed (2 total)

### 1. core/.../net/codec/IsaacCipher.kt (SERVER)
Rewrote nextValue() to match the client's getNextKey() exactly:
  - Count-based reverse iteration (255 down to 0)
  - Post-decrement matching Java's `if(count-- == 0)`
  - Single generation during init, count starts at 256

### 2. client/src/main/java/client.java (CLIENT)
Added null guards to prevent NPE on missing interface IDs:
  - method119(): bounds check + null check before accessing interfaceCache
  - drawTabArea(): bounds check + null check for tab/overlay interfaces

These guards are safety nets. The real fix is the ISAAC cipher.
With correct opcodes, the server's sidebar packets will land properly
and the interfaces will exist when the client tries to render them.

## How to Apply

Extract over your project directory:
  C:\Users\User\Desktop\Open Rune\openrune\

Then rebuild everything:
  .\gradlew.bat build -x test

## Expected Result

After this fix, server->client opcodes decrypt correctly:
  - Map region (73) loads the right area
  - Sidebar tabs (71) set interface IDs that exist in cache
  - Player update (81) parses without offset errors
  - Skill updates (134) populate the stats tab
  - No more T1/T2 error spam in client console
  - Player renders in the game world
