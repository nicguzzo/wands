# Testing Building Wands

This guide covers setting up test environments and verifying mod compatibility with claim mods (Flan, OPAC, GOML) and JEI.

## Test environment setup

The `makeTestEnv.sh` script creates isolated [PortableMC](https://github.com/mindstorm38/portablemc) instances for each of the 6 build targets. It downloads dependencies from Modrinth and CurseForge, copies your compiled mod JAR, and generates per-instance launch scripts.

### Prerequisites

- `jq` and `curl` installed and on PATH
- A successful `./gradlew build` (so compiled JARs exist)

### Usage

```bash
# Build the mod first
./gradlew build

# Create test environments with required dependencies only
bash makeTestEnv.sh

# Include optional compat mods (JEI, OPAC, Flan, GOML)
bash makeTestEnv.sh --with-optional

# Force re-download everything and clear Modrinth API cache
bash makeTestEnv.sh --with-optional --force-update-deps
```

On first run, the script prompts for a player name and a guest player name (stored in `test-env/test-env-config.json`). It also auto-downloads the PortableMC binary if not found.

### Launching an instance

Each instance gets a launch script at `test-env/launch-<name>.sh`:

```bash
bash test-env/launch-1.21.1-Fabric.sh
bash test-env/launch-1.20.1-Forge.sh
# etc.
```

### Two-player LAN testing

Each instance also gets a guest launch script for testing multiplayer scenarios (e.g., claim mod permissions). The guest uses a different player name/UUID and a separate game directory so two clients can run simultaneously without conflicts.

```bash
# Terminal 1: Launch as host
bash test-env/launch-1.21.1-NeoForge.sh

# Terminal 2: Launch as guest
bash test-env/launch-1.21.1-NeoForge-guest.sh
```

**Workflow:**

1. **Host (Nico):** Create a world with cheats enabled, set up claims, then Open to LAN (Esc → Open to LAN → Start LAN World)
2. **Guest:** Multiplayer → Direct Connect → `localhost:<port>` (the LAN port is shown in the host's chat)
3. The guest has a different offline UUID, so claim mods treat them as a non-member — use this to verify that wands are blocked in other players' claims

The guest player name defaults to "Guest" and can be changed in `test-env/test-env-config.json`.

### Available instances

| Instance         | MC Version | Loader   | Optional compat mods                      |
|------------------|------------|----------|-------------------------------------------|
| 1.20.1-Fabric    | 1.20.1     | Fabric   | JEI, OPAC, Flan, GOML, FTB Chunks         |
| 1.20.1-Forge     | 1.20.1     | Forge    | JEI, OPAC, Flan, FTB Chunks               |
| 1.21.1-Fabric    | 1.21.1     | Fabric   | JEI, OPAC, Flan, GOML, FTB Chunks         |
| 1.21.1-NeoForge  | 1.21.1     | NeoForge | JEI, OPAC, Flan, FTB Chunks               |
| 1.21.11-Fabric   | 1.21.11    | Fabric   | JEI, OPAC, GOML                           |
| 1.21.11-NeoForge | 1.21.11    | NeoForge | JEI, OPAC                                 |

---

## Claim mod testing

Building Wands integrates with claim mods so that wand placement respects protected areas. When a wand tries to place, destroy, replace, or use blocks inside a claimed area that the player doesn't have permission for, it shows a red "claimed chunk" message on the actionbar and skips those blocks.

The most reliable way to test this is with two-player LAN testing (see above). The host creates claims, then the guest joins and verifies wands are blocked in those claims.

For all claim mods: use **Creative mode** so you have access to commands and items without survival constraints.

### Flan

[Flan](https://modrinth.com/mod/flan) uses a golden hoe to visually select claim corners.

#### Setup

1. Give yourself a golden hoe: `/give @s golden_hoe`
2. Right-click two opposite corners of the area you want to claim — Flan highlights the boundary with particles
3. The claim is created automatically once both corners are set

#### Useful commands

| Command                  | Description                                          |
|--------------------------|------------------------------------------------------|
| `/flan menu`             | Opens the claim management GUI                       |
| `/flan adminClaim`       | Toggle admin claim mode (claims not tied to a player) |
| `/flan list`             | List your claims                                     |
| `/flan delete`           | Delete the claim you're standing in                  |
| `/flan deleteAll`        | Delete all your claims                               |
| `/flan permission`       | Manage permissions for a claim                       |

#### Testing wand compat

1. Create a claim around a small area
2. Stand **inside** the claim and try placing blocks with a wand — should work (you own it)
3. Create an **admin claim** (`/flan adminClaim`, then claim an area) and try placing with a wand — should be blocked unless you have permission

### Open Parties and Claims (OPAC)

[OPAC](https://modrinth.com/mod/open-parties-and-claims) claims individual chunks and integrates with a party system.

#### Setup

1. Enter server claim mode: `/openpac-claims server-claim-mode`
2. Stand in the chunk you want to claim
3. Claim the current chunk: `/openpac-claims claim`
4. Repeat for adjacent chunks as needed

#### Useful commands

| Command                                      | Description                              |
|----------------------------------------------|------------------------------------------|
| `/openpac-claims claim`                       | Claim the chunk you're standing in       |
| `/openpac-claims unclaim`                     | Unclaim the current chunk                |
| `/openpac-claims server-claim-mode`           | Toggle server-level claim mode           |
| `/openpac-claims list`                        | List your claimed chunks                 |

#### Testing wand compat

1. Claim a few chunks using the commands above
2. Try placing blocks with a wand inside your claim — should work
3. Have another player (or use server claim mode) claim a chunk, then try placing there — should be blocked

### Get Off My Lawn (GOML)

[GOML](https://modrinth.com/mod/goml-reserved) uses placeable anchor blocks that protect a radius around them. Different anchor tiers cover different radii.

> **Note:** GOML is only available on Fabric (see the instances table above).

#### Setup

1. Open creative inventory and search for "anchor" — GOML adds several tiered claim anchors
2. Place an anchor block — it immediately claims a radius around it

#### Anchor tiers

| Anchor           | Radius |
|------------------|--------|
| Makeshift        | 10     |
| Reinforced       | 25     |
| Glistening       | 50     |
| Crystal          | 75     |
| Emeradic         | 100    |
| Withered         | 150    |

#### Useful commands

| Command                          | Description                         |
|----------------------------------|-------------------------------------|
| `/goml admin list`               | List all claims                     |
| `/goml admin remove <id>`        | Remove a claim by ID                |

#### Testing wand compat

1. Place an anchor block to create a claim
2. Try placing blocks with a wand inside the claim radius — should work (you placed the anchor)
3. Test with a different player's anchor — wand placement should be blocked

### FTB Chunks

[FTB Chunks](https://www.curseforge.com/minecraft/mc-mods/ftb-chunks-forge) claims individual chunks with a map-based GUI. It requires FTB Teams and FTB Library as dependencies (both are auto-downloaded by the test script).

> **Note:** FTB mods are downloaded from CurseForge (not Modrinth). Available for 1.20.1 and 1.21.1 (see the instances table above).

#### Setup

1. Open the FTB Chunks map: default key is **M** (or check controls for "FTB Chunks" keybinds)
2. Click on chunks in the map to claim them — claimed chunks are highlighted
3. Right-click to unclaim

#### Useful commands

| Command                                       | Description                   |
|-----------------------------------------------|-------------------------------|
| `/ftbchunks admin unclaim_all`                | Remove all claims             |
| `/ftbchunks admin claim_as <player> <x> <z>`  | Claim a chunk as another player |

#### Testing wand compat

1. Open the FTB Chunks map and claim a few chunks around you
2. Try placing blocks with a wand inside your claim — should work (you own it)
3. Have the guest player join via LAN and try placing with a wand inside your claim — should show the red "claimed chunk" message and block placement

---

## JEI testing

[JEI](https://modrinth.com/mod/jei) (Just Enough Items) shows recipes and item lists. Building Wands registers its items with JEI.

### What to verify

1. Open inventory and press **R** or **U** on a wand item — JEI should show the crafting recipe
2. Search "wand" in the JEI search bar — all wand types should appear
3. Open the wand settings screen (keybind, default **V**) — JEI should **not** render its search bar on top of the wand UI

---

## Quick compat test checklist

Use this workflow when verifying wand compat across all claim mods:

1. Build the mod: `./gradlew build`
2. Set up test environment: `bash makeTestEnv.sh --with-optional`
3. Launch host and guest (e.g., for Fabric 1.21.1):
   ```bash
   bash test-env/launch-1.21.1-Fabric.sh        # Terminal 1
   bash test-env/launch-1.21.1-Fabric-guest.sh   # Terminal 2
   ```
4. **Host:** Create a new Creative world, give yourself a wand: `/give @s wands:stone_wand`
5. **Flan test:**
   - `/give @s golden_hoe` and claim a small area
   - Place blocks with wand inside claim (should work — you own it)
   - Open to LAN, guest joins and tries wand in the claim (should show red actionbar message and block)
6. **OPAC test:**
   - `/openpac-claims claim` in a new chunk
   - Place blocks with wand (should work)
   - Guest tries wand in the same chunk (should block)
7. **GOML test (Fabric only):**
   - Place a claim anchor from creative inventory
   - Place blocks with wand inside radius (should work)
   - Guest tries wand inside the radius (should block)
8. **FTB Chunks test (1.20.1 / 1.21.1 only):**
   - Open FTB Chunks map (**M**) and claim chunks around you
   - Place blocks with wand inside claim (should work)
   - Guest tries wand inside the claim (should block)
9. **JEI test:**
   - Press **R** on a wand item to verify recipe shows
   - Open wand settings (**V**) to verify JEI doesn't overlap
10. Repeat on other instances (Forge/NeoForge) as needed — check the instances table for which compat mods are available per target
