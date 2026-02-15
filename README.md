# Building Wands

Powerful building, mining, and terraforming tools for Minecraft.

[![Patreon](https://i.imgur.com/r5hfGFc.png)](https://www.patreon.com/nicguzzo "Become a patron")
[![Discord](https://i.imgur.com/NyP9y98.jpg)](https://discord.gg/T9VbYBNYCR "Join the Discord")
[![CurseForge](https://img.shields.io/badge/Download-CurseForge-orange)](https://www.curseforge.com/minecraft/mc-mods/building-wands)

**Supported platforms:** Forge (1.20.1) · Fabric (1.20.1, 1.21.1, 1.21.11) · NeoForge (1.21.1, 1.21.11) · Quilt (via Fabric)

## Shader Note

If the preview doesn't show when using shaders, set `"render_last": true` in your config.

---

## Building Wands

Building Wands lets you place, destroy, or replace hundreds of blocks at once so you can build and mine even faster.

- **14 modes** covering many building and mining scenarios — rows, fills, circles, spheres, grids, copy/paste, vein mining, and more
- **Shulkers boxes and magic bags** in your inventory are used to pull blocks for building and store blocks for mining 
- **Creative and Survival** support (consumes blocks from your inventory in survival)
- **Real-time ghost preview** shows exactly what will change before you commit
- **Undo** support — press U to undo the last operation
- **Tiered progression** from Stone to Netherite, with configurable block limits

---

## Modes

Building Wands has 14 modes. Cycle through them with **V**.

### Direction
Single block placement in 9 directions from a target block. (1 click)

![Directional Mode](https://www.dropbox.com/s/qprvbqbhl3e6w3y/mode1.jpg?raw=1)

### Row / Column
Extend a line of blocks in a row or column. Press **X** to toggle orientation. (1 click)

![Row/Column Mode](https://www.dropbox.com/s/awk892ii4ztqwsz/mode2.jpg?raw=1)

### Fill
Fill a rectangular area between 2 corners. (2 clicks)

![Fill Mode](https://www.dropbox.com/s/gf1i4zwafbtsdvp/wand_mode2.gif?raw=1)

### Area
Flood-fill connected blocks of the same type. Press **,** (comma) to toggle diagonal spread. (1 click)

![Area Mode](https://www.dropbox.com/s/ixe0qgi44csye8l/2020-11-16_01.54.28.jpg?raw=1)

### Grid
Place blocks in a 2D grid pattern with adjustable spacing. Use **arrow keys** to change grid size. (1 click)

### Line
Draw a straight line between two points. (2 clicks)

![Line Mode](https://www.dropbox.com/s/ruq1b35vd1y3nhd/line.jpg?raw=1)

### Circle
Draw a circle from center to radius. Press **K** to toggle filled circle. (2 clicks)

![Circle Mode](https://www.dropbox.com/s/h7fkaypwfzuftu0/circle.jpg?raw=1)

### Sphere
Create a 3D sphere from center to radius. (2 clicks)

### Box
Create a box with configurable dimensions. (1 click)

### Rock
Randomly scatter blocks in a spherical cloud for natural-looking terrain. (1 click)

### Copy
Copy a region between 2 corners. (2 clicks)

### Paste
Paste previously copied blocks. Press **R** to rotate 90° on the Y axis. (1 click)

### Vein
Select connected ore veins for destruction. (1 click)

### Blast
Explode blocks in a radius. In survival, requires TNT in inventory. (1 click)

---

## Actions

Each mode supports 4 actions. Cycle with **H**:

| Action | Description |
|--------|-------------|
| **Place** | Place blocks from your inventory |
| **Destroy** | Remove blocks (requires a tool in your offhand; correct tool type needed in survival) |
| **Replace** | Swap existing blocks with your selected block |
| **Use** | Apply block states (rotate stairs, toggle slabs, etc.) |

---

## Supporting Items

### Palette

Store multiple block types and cycle through them while building.

- **3 palette modes:** Random, Sequential (round robin), Gradient
- **Recipe:** 8 Item Frames + 1 Chest
- Press **J** to open the palette menu, **P** to cycle palette mode

### Magic Bags

Portable mass block storage for survival mode.

| Tier | Capacity | Key Ingredient |
|------|----------|----------------|
| Magic Bag 1 | ~17,280 blocks | Ender Pearl |
| Magic Bag 2 | ~172,800 blocks | Eye of Ender |
| Magic Bag 3 | Unlimited | Nether Star |

All bags are crafted with String, Dye, Rabbit Hide, and the key ingredient.

### Shulker Box & Magic Bag

Shulker boxes and magic bags work seamlessly with both building and mining:

- **Placement:** When placing blocks, the wand pulls from shulker boxes and magic bags in your inventory automatically
- **Mining:** When destroying blocks, mined items are routed into shulker boxes and magic bags instead of cluttering your inventory
- **Offhand priority:** A shulker box or magic bag in your offhand is checked first for both pulling and storing

### Pin

Lock the wand's target position so you can look away, walk around, and still preview or place from the pinned block.

- **G** — Toggle pin on/off. While pinned, use **arrow keys** to nudge the target position. Shift+arrow moves vertically.
- **Alt** (hold) — Temporarily freeze the target while held; releases when you let go.
- Pin is cleared automatically after placement or when you switch modes.

---

## Wand Tiers

All limits and durability values are configurable in `wands.json`.

| Wand | Block Limit | Durability | Recipe |
|------|-------------|------------|--------|
| Stone | 16 | 256 | Cobblestone + Sticks |
| Copper | 24 | 480 | Copper Ingot + Sticks |
| Iron | 32 | 640 | Iron Ingot + Sticks |
| Diamond | 64 | 2,048 | Diamond + Sticks |
| Netherite | 256 | 4,096 | Netherite Ingot + Sticks |
| Creative | 8,000 | Infinite | Creative-only |

---

## Controls

All keybindings are customizable in Minecraft's Controls settings.

| Key | Function |
|-----|----------|
| **Y** | Open wand menu |
| **V** | Cycle mode |
| **H** | Cycle action |
| **X** | Toggle orientation (row/column) |
| **G** | Pin/Unpin — lock the wand target to a block so you can look away freely |
| **Alt** (hold) | Freeze — temporarily lock the target while held |
| **U** | Undo last operation |
| **I** | Invert selection |
| **K** | Toggle filled circle |
| **R** | Rotate paste (90°) |
| **P** | Cycle palette mode |
| **J** | Open palette menu |
| **Z** | Include additional block type |
| **C** | Clear wand selection |
| **.** (period) | Toggle stairs/slabs orientation |
| **,** (comma) | Toggle area diagonal spread |
| **Arrow keys** | Adjust grid size |

---

## Claim Mod Compatibility

Building Wands respects claimed/protected chunks. Players cannot use wands to build or destroy in areas they don't have permission in.

| Claim Mod | Loaders |
|-----------|---------|
| **FTB Chunks** | Fabric, Forge, NeoForge |
| **Flan** | Fabric, Forge, NeoForge |
| **Open Parties and Claims (OPAC)** | Fabric, Forge, NeoForge |
| **Get Off My Lawn (GOML)** | Fabric |

---

## Configuration

### Server config (`wands.json`)

- Per-wand block limits and durability
- `blocks_per_xp` — XP cost per block (0 = disabled, 2 = 2 blocks per XP point, 0.5 = 2 XP per block)
- Block denylists (`str_denied`)
- Extra tool definitions (`extra_pickaxes`, `extra_axes`, `extra_shovels`, `extra_hoes`, `extra_shears`)
- Feature toggles (`enable_vein_mode`, `enable_blast_mode`, `disable_destroy_replace`)

### Client config (via Cloth Config / Mod Menu)

- Preview colors and opacity
- HUD positioning for mode/tool displays
- Line thickness and outline settings

---

## Dependencies

### Fabric
- [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
- [Architectury API](https://www.curseforge.com/minecraft/mc-mods/architectury-api)
- [Cloth Config](https://www.curseforge.com/minecraft/mc-mods/cloth-config)
- Quilt compatible

### Forge (1.20.1)
- [Architectury API](https://www.curseforge.com/minecraft/mc-mods/architectury-api)
- [Cloth Config](https://www.curseforge.com/minecraft/mc-mods/cloth-config)
- Optional: [Mod Menu](https://www.curseforge.com/minecraft/mc-mods/modmenu)

### NeoForge (1.21.1+)
- [Architectury API](https://www.curseforge.com/minecraft/mc-mods/architectury-api)
- [Cloth Config](https://www.curseforge.com/minecraft/mc-mods/cloth-config)

---

[![Patreon](https://i.imgur.com/r5hfGFc.png)](https://www.patreon.com/nicguzzo "Become a patron")
[![Discord](https://i.imgur.com/NyP9y98.jpg)](https://discord.gg/T9VbYBNYCR "Join the Discord")

Any feedback is welcome — please report issues on Discord or GitHub.
