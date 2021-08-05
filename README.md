#Building Wands Mod for fabric/forge.


###Directional mode
Single block placement in 9 directions from a block.
![Directional Mode](https://www.dropbox.com/s/qprvbqbhl3e6w3y/mode1.jpg?raw=1)
 
###Row/Column mode
Multiple blocks at once in a row or column, press x to change orientation
![Row/Col Mode](https://www.dropbox.com/s/awk892ii4ztqwsz/mode2.jpg?raw=1)
###Fill mode
Fill between 2 corners, same block, or random
![Row/Col Mode](https://www.dropbox.com/s/gf1i4zwafbtsdvp/wand_mode2.gif?raw=1)
###Area mode
Fill connected blocks
![Area Mode](https://www.dropbox.com/s/ixe0qgi44csye8l/2020-11-16_01.54.28.jpg?raw=1)
###Line Mode:
Line from block to block.
![Line Mode](https://www.dropbox.com/s/ruq1b35vd1y3nhd/line.jpg?raw=1)
###Circle Mode:
Circle from center block to radius block. (v2 press k to change to filled circle)
![Circle Mode](https://www.dropbox.com/s/h7fkaypwfzuftu0/circle.jpg?raw=1)

##V2 new modes:

###Copy mode:
Copy between corners.

###Paste mode: 
Paste previously copied blocks, press R to cycle rotation on Y axis by 90 degrees increments.

 

##Keybindings:

- "V" change mode
- "I" inverts direction (opposite, mode 1 only)
- "X" change between row or column, or plane for circle (xz, xy, yz)
- "R" cycle  randomize/round-robin/same using blocks in inventory or shulker
- "U" undo/redo , shift to redo, alt to undo/redo by 10 blocks.

 

###Shulkers

On survival, blocks are consumed from shulkerboxes first.

###Destroy Blocks

Blocks can be destroyed if there's a tool in the offhand.

In creative any tool works.

In survival the tool has to be suitable for the block. e.g. Axe for logs, wood, pickaxe for stone, etc.

By default, destroyed blocks drop their item in survival if the tool in the offhand has silk touch or fortune III, otherwise they're lost, this can be configured.

Right click on the air to reset wand and display mode info

###Levels of wands:
Configurable in wands.json
- Stone wand: 9 block limit.
- Iron wand: 49 block limit.
- Diamond wand: 64 block limit.
- Netherite wand: 128 block limit.

###Optional: 
Cconsume xp to place blocks. To enable change config file wands.json

"blocks_per_xp": 0      to disable xp consumption. 

"blocks_per_xp": 2      will allow to place 2 blocks per xp point.

"blocks_per_xp":0.5    would mean 2 xp cost per block

 

##Version 2 now depends on Architectury Mod
###Fabric dependencies: 
[Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
[Architectury Fabric](https://www.curseforge.com/minecraft/mc-mods/architectury-fabric)
  

###Forge dependencies: 
[Architectury Forge](https://www.curseforge.com/minecraft/mc-mods/architectury-forge)
 

##V2.0 Changes

- New copy/paste modes
- Improved block shape preview
- New palette item to set up palettes of blocks
- Modes are now per item using nbt tags
- Show wand mode on screen
- Show more info on wand/palette tooltips
- Keeping alt key pressed uses block next to targeted side as picking block for fill - Mode, lines, circles and copy.
 

####Any feedback is welcomed, please report any issues to me.