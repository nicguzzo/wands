# Wands Mode Guide

A user guide for all wand modes, settings, and palette features.

## Wand Modes

### DIRECTION
Places blocks in a line extending from the targeted block in the direction you're facing. Click once to place.

| Setting | Description |
|---------|-------------|
| Multiplier | Number of blocks to place (1-16) |
| Inverted | Reverses the placement direction |

---

### ROW / COLUMN
Places blocks in a row or column pattern from the targeted block. Click once to place.

| Setting | Description |
|---------|-------------|
| Limit | Maximum blocks to place (0 = unlimited) |
| Orientation | Choose ROW or COLUMN placement |
| Target Air | Allow targeting empty space |

---

### FILL
Fills a 3D rectangular area between two points. Click twice to define corners.

| Setting | Description |
|---------|-------------|
| Fill Rect | ON: solid fill, OFF: hollow walls only |
| Axis | When hollow, controls which faces stay open (see below) |

**Hollow Fill (Fill Rect OFF):**

| Axis | Result | Open Faces |
|------|--------|------------|
| X | Horizontal tunnel (east-west) | East, West |
| Y | Vertical shaft | Top, Bottom |
| Z | Horizontal tunnel (north-south) | North, South |

---

### AREA
Flood-fills connected blocks of the same type, spreading outward from the target. Click once to fill.

| Setting | Description |
|---------|-------------|
| Limit | Maximum blocks to fill |
| Diagonal | ON: spread diagonally, OFF: adjacent faces only |
| Skip % | Randomly skip percentage of blocks (0-100) |
| Match State | ON: match exact block state, OFF: match block type |

---

### VEIN
Mining mode - finds connected ore/blocks of the same type. Click once to mine. Also supports USE action for bulk tool usage (tilling, stripping, pathing) on connected blocks.

| Setting | Description |
|---------|-------------|
| Limit | Maximum blocks to mine |
| Match State | ON: match exact block state |

---

### GRID
Places blocks in a grid pattern. Click once to place.

| Setting | Description |
|---------|-------------|
| M / N | Grid dimensions |
| M Skip / N Skip | Skip intervals for patterns |
| M Offset / N Offset | Pattern offsets |
| Rotation | Rotate grid (0°, 90°, 180°, 270°) |
| Target Air | Allow targeting empty space |

---

### LINE
Places blocks in a straight line between two points. Click twice to define endpoints.

*No mode-specific settings*

---

### CIRCLE
Places blocks in a circle or disc pattern. Click twice to define center and radius.

| Setting | Description |
|---------|-------------|
| Plane | Circle orientation (XZ, XY, or YZ) |
| Filled | ON: filled disc, OFF: ring outline |
| Even | ON: even diameter, OFF: odd diameter |

---

### COPY
Copies a 3D region to the wand's clipboard. Click twice to define corners.

*No mode-specific settings*

---

### PASTE
Pastes the copied region from the clipboard. Click once to paste at target location.

| Setting | Description |
|---------|-------------|
| Mirror | Mirror the paste (None, Left-Right, Front-Back) |
| Rotation | Rotate pasted content (0°, 90°, 180°, 270°) |
| Include Block | Include the targeted block in selection |
| Target Air | Allow targeting empty space |

---

### TUNNEL
Creates a tunnel of specified dimensions. Click once to start.

| Setting | Description |
|---------|-------------|
| Width | Tunnel width |
| Height | Tunnel height |
| Depth | Tunnel length |
| X Offset | Horizontal offset from center |
| Y Offset | Vertical offset from center |
| Include Block | Include targeted block in tunnel |
| Target Air | Allow targeting empty space |

---

### BLAST
Creates an explosion-like spherical destruction pattern. Click once to blast. Requires wand with blast capability.

| Setting | Description |
|---------|-------------|
| Radius | Blast radius (4-16) |

---

### SPHERE
Places blocks in a spherical pattern. Click twice to define center and radius.

*No mode-specific settings*

---

### ROCK
Places a natural-looking rock formation with randomized noise. Click once to place. Rotate wand for new random shape.

| Setting | Description |
|---------|-------------|
| Radius | Base radius of the rock |
| Noise | Amount of randomization (0-16) |
| Target Air | Allow targeting empty space |

---

## Global Settings

These settings apply to all modes:

| Setting | Description |
|---------|-------------|
| Stair/Slab | Toggle top/bottom placement for stairs and slabs |
| Axis | X/Y/Z axis for pillar block orientation and hollow fills |
| State Mode | How block states are handled (see below) |
| Drop Position | Where items drop: on player or at block |
| Action | PLACE, REPLACE, DESTROY, or USE |

### State Mode Options

- **APPLY**: Apply axis orientation to placed blocks
- **TARGET**: Match the orientation of the targeted block
- **CLONE**: Copy exact block state from source blocks
- **NONE**: Use default block state

---

## Palette

The Palette is an item that holds multiple block types for varied placement. Equip it in your offhand while using a wand.

### Palette Modes

#### Sequential (Round Robin)
Cycles through palette blocks in order. Each placed block uses the next block type in sequence.

#### Random
Randomly selects from palette blocks for each placement.

#### Gradient
Creates vertical gradients using palette rows. Blocks higher in the build use blocks from the top rows of the palette, while lower blocks use bottom rows.

| Setting | Description |
|---------|-------------|
| Height | Vertical span for the gradient (used when all blocks are at same Y level) |
| Rotate | Randomly rotate placed blocks |

**Gradient Row Mapping:**
- Palette row 1 (top) → Top of your build
- Palette row 2 → Upper-middle
- Palette row 3 → Middle-upper
- Palette row 4 → Middle-lower
- Palette row 5 → Lower-middle
- Palette row 6 (bottom) → Bottom of your build

**Tip:** Place different block types in each row of the palette. For example, stone variants at the bottom, transitioning to grass/foliage at the top for natural-looking terrain.

**Compatible Modes:**
Gradient works best with modes that place blocks at different Y levels:
- Fill
- Sphere
- Tunnel/Rectangle
- Rock
- Line (when vertical)

For horizontal placements (Row, Column, Circle, etc.), gradient has minimal effect since all blocks are at the same Y level.

---

## Actions

| Action | Description |
|--------|-------------|
| PLACE | Place blocks from inventory |
| REPLACE | Replace existing blocks |
| DESTROY | Break blocks (drops items) |
| USE | Use tool action (till, strip, path, shear) on blocks |

**Mode-specific notes:**
- COPY and BLAST have no selectable actions
- VEIN doesn't support PLACE (mining mode only)

---

## Quick Tips

1. **Preview**: Most two-click modes show a preview before placing
2. **Cancel**: Press **C** to cancel a pending operation
3. **Undo**: Press **U** to undo recent placements
4. **Block Selection**: Press **Z** to cycle through hotbar blocks
5. **Wand Settings**: Press **Y** to open the wand configuration screen
