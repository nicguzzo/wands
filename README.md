# Building Wands Mod for fabric/forge/quilt.

Become my [![Patreon](https://i.imgur.com/r5hfGFc.png)](https://www.patreon.com/nicguzzo "become my patron")

Get support on [![Discord](https://i.imgur.com/NyP9y98.jpg)](https://discord.gg/T9VbYBNYCR "")

---

# If the preview doesn't show when using shaders, set "render_last" to true


## Modes
### Directional mode
Single block placement in 9 directions from a block.
![Directional Mode](https://www.dropbox.com/s/qprvbqbhl3e6w3y/mode1.jpg?raw=1)
 
### Row/Column mode
Multiple blocks at once in a row or column, press x to change orientation
![Row/Col Mode](https://www.dropbox.com/s/awk892ii4ztqwsz/mode2.jpg?raw=1)

### Fill mode
Fill between 2 corners, same block, or random
![Row/Col Mode](https://www.dropbox.com/s/gf1i4zwafbtsdvp/wand_mode2.gif?raw=1)

###Area mode
Fill connected blocks
![Area Mode](https://www.dropbox.com/s/ixe0qgi44csye8l/2020-11-16_01.54.28.jpg?raw=1)

### Grid mode
Fixed 2d grid, use arrow keys or gui to change size.

### Line Mode:
Line from block to block.
![Line Mode](https://www.dropbox.com/s/ruq1b35vd1y3nhd/line.jpg?raw=1)

### Circle Mode:
Circle from center block to radius block. (v2 press k to change to filled circle)
![Circle Mode](https://www.dropbox.com/s/h7fkaypwfzuftu0/circle.jpg?raw=1)

### Copy mode:
Copy between corners.

### Paste mode: 
Paste previously copied blocks, press R to cycle rotation on Y axis by 90 degrees increments.

### Blast mode:  
Destroy blocks with an explosion, in survival the player needs tnt in the inventory.


### Shulkers

On survival, blocks are consumed from shulkerboxes first.

###Destroy Blocks

Blocks can be destroyed if there's a tool in the offhand.

In creative any tool works.

In survival the tool has to be suitable for the block. e.g. Axe for logs, wood, pickaxe for stone, etc.

### Levels of wands:
Configurable in wands.json
- Stone wand: 16 block limit.
- Iron wand: 32 block limit.
- Diamond wand: 64 block limit.
- Netherite wand: 256 block limit.

### Optional: 
Cconsume xp to place blocks. To enable change config file wands.json

"blocks_per_xp": 0      to disable xp consumption. 
"blocks_per_xp": 2      will allow to place 2 blocks per xp point.
"blocks_per_xp":0.5    would mean 2 xp cost per block
 
### Fabric dependencies: 
[Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
[Architectury Fabric](https://www.curseforge.com/minecraft/mc-mods/architectury-api)
[Cloth Config Fabric](https://www.curseforge.com/minecraft/mc-mods/cloth-config)
 Fabric version should work on quilt.

### Forge dependencies: 
[Architectury Forge](https://www.curseforge.com/minecraft/mc-mods/architectury-api)
[Cloth Config Forge](https://www.curseforge.com/minecraft/mc-mods/cloth-config) 
optional [ModMenu](https://www.curseforge.com/minecraft/mc-mods/modmenu)

#### Any feedback is welcomed, please report any issues to me.