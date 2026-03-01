# Testing Building Wands

This guide covers setting up test environments and verifying mod compatibility with claim mods (Flan, OPAC, GOML) and JEI.

## Test environment setup

The `makeTestEnv.sh` script creates isolated [PortableMC](https://github.com/mindstorm38/portablemc) instances for each of the 6 build targets. It downloads dependencies from Modrinth, copies your compiled mod JAR, and generates per-instance launch scripts.

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

On first run, the script prompts for a player name (stored in `test-env/test-env-config.json`). It also auto-downloads the PortableMC binary if not found.

### Launching an instance

Each instance gets a launch script at `test-env/launch-<name>.sh`:

```bash
bash test-env/launch-1.21.1-Fabric.sh
bash test-env/launch-1.20.1-Forge.sh
# etc.
```

### Available instances

| Instance         | MC Version | Loader   | Optional compat mods                      |
|------------------|------------|----------|-------------------------------------------|
| 1.20.1-Fabric    | 1.20.1     | Fabric   | JEI, OPAC, Flan, GOML                     |
| 1.20.1-Forge     | 1.20.1     | Forge    | JEI, OPAC, Flan                           |
| 1.21.1-Fabric    | 1.21.1     | Fabric   | JEI, OPAC, Flan, GOML                     |
| 1.21.1-NeoForge  | 1.21.1     | NeoForge | JEI, OPAC, Flan                           |
| 1.21.11-Fabric   | 1.21.11    | Fabric   | JEI, OPAC, GOML                           |
| 1.21.11-NeoForge | 1.21.11    | NeoForge | JEI, OPAC                                 |

---

## Claim mod testing

Building Wands integrates with claim mods so that wand placement respects protected areas. The following sections explain how to set up each claim mod in-game for testing.

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
3. Launch a Fabric instance (has all compat mods): `bash test-env/launch-1.21.1-Fabric.sh`
4. Create a new Creative world
5. Give yourself a wand: `/give @s wands:stone_wand`
6. **Flan test:**
   - `/give @s golden_hoe` and claim a small area
   - Place blocks with wand inside claim (should work)
   - `/flan adminClaim`, claim another area, try wand (should block)
7. **OPAC test:**
   - `/openpac-claims claim` in a new chunk
   - Place blocks with wand (should work)
   - `/openpac-claims server-claim-mode`, claim another chunk, try wand (should block)
8. **GOML test:**
   - Place a claim anchor from creative inventory
   - Place blocks with wand inside radius (should work)
9. **JEI test:**
   - Press **R** on a wand item to verify recipe shows
   - Open wand settings (**V**) to verify JEI doesn't overlap
10. Repeat on other instances (Forge/NeoForge) as needed — check the instances table for which compat mods are available per target
