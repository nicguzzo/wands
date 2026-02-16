# Changelog

## 3.0

### Supported Minecraft versions
- 1.20.1 (Fabric, Forge)
- 1.21, 1.21.1 (Fabric, NeoForge)
- 1.21.11 (Fabric, NeoForge)

### New features
- **New wand settings screen** — redesigned settings UI with better organization and tooltips
- **Quick mode switching** — hold V to open a mode selector grid, tap V to cycle through modes
- **Pin position** — lock the wand's target position in place (replaces the old Alt-freeze and Anchor system)
- **Action-level undo/redo** — undo and redo now operate on entire wand actions instead of individual blocks (creative mode)
- **Reach distance** — new setting to extend wand reach beyond the default range, works in all modes
- **Use mode: axe actions** — strip logs, scrape wax, and remove oxidation with axes
- **Use mode: creative support** — Use actions no longer require tools in creative mode
- **Fluid preview** — water and lava bucket placement now shows a ghost block preview
- **Row/Col direction** — row or column direction is now based on the player's facing direction
- **Rotate and flip controls** — block state split into independent rotate and flip settings

### Bug fixes
- Fixed dark inventory screen on MC 1.21.1
- Fixed Use mode showing wrong tool warning when block has no use action
- Fixed replace action not applying block placement, flip, and rotation
- Fixed magic bag item duplication on MC 1.21+
- Fixed magic bag load/unload/clear
- Fixed hand-mineable blocks requiring a tool to break with wand
- Fixed vein mode affected by clear position setting
- Fixed empty bucket showing red outline on fluid source blocks
