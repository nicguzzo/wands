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

public class CopyMode extends WandMode {

    public void place_in_buffer(Wand wand) {
        int copy_x1 = 0;
        int copy_y1 = 0;
        int copy_z1 = 0;
        int copy_x2 = 0;
        int copy_y2 = 0;
        int copy_z2 = 0;
        if (wand.getP1() != null && wand.preview) {
            wand.calc_pv_bbox(wand.getP1(), wand.pos);
            wand.valid = true;
            copy_x1 = wand.getP1().getX();
            copy_y1 = wand.getP1().getY();
            copy_z1 = wand.getP1().getZ();
            if (wand.getP2() == null) {
                copy_x2 = wand.pos.getX();
                copy_y2 = wand.pos.getY();
                copy_z2 = wand.pos.getZ();
            } else {
                copy_x2 = wand.getP2().getX();
                copy_y2 = wand.getP2().getY();
                copy_z2 = wand.getP2().getZ();
            }
            if (copy_x1!=copy_x2 || copy_y1!=copy_y2 || copy_z1!=copy_z2 ) {
                if (copy_x1 >= copy_x2) {
                    copy_x1 += 1;
                } else {
                    copy_x2 += 1;
                }
                if (copy_y1 >= copy_y2) {
                    copy_y1 += 1;
                } else {
                    copy_y2 += 1;
                }
                if (copy_z1 >= copy_z2) {
                    copy_z1 += 1;
                } else {
                    copy_z2 += 1;
                }
            } else {
                copy_x2 = copy_x1 + 1;
                copy_y2 = copy_y1 + 1;
                copy_z2 = copy_z1 + 1;
            }
        }
        if (wand.getP1() != null && wand.getP2() != null) {
            {
                int xs, ys, zs, xe, ye, ze,lx,ly,lz;

                xs=wand.getP1().getX();
                xe=wand.getP2().getX();
                ys=wand.getP1().getY();
                ye=wand.getP2().getY();
                zs=wand.getP1().getZ();
                ze=wand.getP2().getZ();
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
                                    if (bs != Blocks.AIR.defaultBlockState() && !(bs.getBlock() instanceof ShulkerBoxBlock) && !bs.hasBlockEntity()) {
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
