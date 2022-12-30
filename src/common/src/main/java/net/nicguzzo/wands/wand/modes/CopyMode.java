package net.nicguzzo.wands.wand.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.wand.CopyBuffer;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;

public class CopyMode implements WandMode {

    public void place_in_buffer(Wand wand) {
        if (wand.copy_pos1 != null && wand.preview) {
            wand.valid = true;
            wand.copy_x1 = wand.copy_pos1.getX();
            wand.copy_y1 = wand.copy_pos1.getY();
            wand.copy_z1 = wand.copy_pos1.getZ();
            if (wand.copy_pos2 == null) {
                wand.copy_x2 = wand.pos.getX();
                wand.copy_y2 = wand.pos.getY();
                wand.copy_z2 = wand.pos.getZ();
            } else {
                wand.copy_x2 = wand.copy_pos2.getX();
                wand.copy_y2 = wand.copy_pos2.getY();
                wand.copy_z2 = wand.copy_pos2.getZ();
            }
            if (!wand.copy_pos1.equals(wand.copy_pos2)) {
                if (wand.copy_x1 >= wand.copy_x2) {
                    wand.copy_x1 += 1;
                } else {
                    wand.copy_x2 += 1;
                }
                if (wand.copy_y1 >= wand.copy_y2) {
                    wand.copy_y1 += 1;
                } else {
                    wand.copy_y2 += 1;
                }
                if (wand.copy_z1 >= wand.copy_z2) {
                    wand.copy_z1 += 1;
                } else {
                    wand.copy_z2 += 1;
                }
            } else {
                wand.copy_x2 = wand.copy_x1 + 1;
                wand.copy_y2 = wand.copy_y1 + 1;
                wand.copy_z2 = wand.copy_z1 + 1;
            }
        }
        if (wand.copy_pos1 != null && wand.copy_pos2 != null) {
            {
                int xs, ys, zs, xe, ye, ze,lx,ly,lz;

                xs=wand.copy_pos1.getX();
                xe=wand.copy_pos2.getX();
                ys=wand.copy_pos1.getY();
                ye=wand.copy_pos2.getY();
                zs=wand.copy_pos1.getZ();
                ze=wand.copy_pos2.getZ();
                int xsgn=(xs>=xe?-1:1);
                int ysgn=(ys>=ye?-1:1);
                int zsgn=(zs>=ze?-1:1);
                lx=Math.abs(xe - xs);
                ly=Math.abs(ye - ys);
                lz=Math.abs(ze - zs);
                //log("copy");
                int ll = (lx + 1) * (ly + 1) * (lz + 1);
                //int ll = lx * ly* lz);
                //WandsMod.log("copy volume "+ll,prnt);
                if (ll <= wand.MAX_COPY_VOL) {
                    BlockPos.MutableBlockPos bp = new BlockPos.MutableBlockPos();
                    wand.copy_paste_buffer.clear();
                    int cp = 0;
                    for (int z = 0; z <= lz; z++) {
                        for (int y = 0; y <= ly; y++) {
                            for (int x = 0; x <= lx; x++) {
                                bp.set(xs+x*xsgn, ys+y*ysgn, zs+z*zsgn);
                                BlockState bs = wand.level.getBlockState(bp);
                                if(!WandsConfig.denied.contains(bs.getBlock())){
                                    if (bs != Blocks.AIR.defaultBlockState() && !(bs.getBlock() instanceof ShulkerBoxBlock)) {
                                        cp++;
                                        wand.copy_paste_buffer.add(new CopyBuffer(new BlockPos(x*xsgn , y*ysgn , z*zsgn ), bs));
                                    }
                                }
                            }
                        }
                    }
                    //log("copied "+copy_paste_buffer.size() + " cp: "+cp);
                    if (!wand.preview)
                        wand.player.displayClientMessage(Compat.literal("Copied: " + cp + " blocks"), false);
                } else {
                    wand.player.displayClientMessage(Compat.literal("Copy limit reached"), false);
                    //log("max volume");
                }
            }
        }
    }
}
