package net.nicguzzo.wands.wand.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;

public class RowColMode implements WandMode {
    public void place_in_buffer(Wand wand) {
        WandProps.Orientation orientation = WandProps.getOrientation(wand.wand_stack);
        Level level= Compat.player_level(wand.player);
        boolean preview = level.isClientSide();
        Direction dir = Direction.EAST;
        BlockPos pos_m= wand.pos.relative(wand.side, 1);;
        WandItem wand_item = (WandItem) wand.wand_stack.getItem();
        BlockState state = level.getBlockState(pos_m);

        int limit2= WandProps.getVal(wand.wand_stack, WandProps.Value.ROWCOLLIM);
        if(limit2<=0 || limit2 >wand.limit){
            limit2=wand.limit;
        }

        if (state.isAir() || wand.replace_fluid(state) || wand.destroy || wand.use) {
            BlockPos pos0 = wand.pos;
            BlockPos pos1 = pos_m;
            BlockPos pos2 = wand.pos;
            BlockPos pos3 = pos_m;
            int offx = 0;
            int offy = 0;
            int offz = 0;

            switch (wand.side) {
                case UP:
                case DOWN:
                    switch (orientation) {
                        case ROW: {
                            dir = Direction.SOUTH;
                            offz = -1;
                        }break;
                        case COL: {
                            dir = Direction.EAST;
                            offx = -1;
                        }break;
                    }
                    break;
                case SOUTH:
                case NORTH:
                    switch (orientation) {
                        case ROW: {
                            dir = Direction.EAST;
                            offx = -1;
                        }break;
                        case COL: {
                            dir = Direction.UP;
                            offy = -1;
                        }break;
                    }
                    break;
                case EAST:
                case WEST:
                    switch (orientation) {
                        case ROW: {
                            dir = Direction.SOUTH;
                            offz = -1;
                        }break;
                        case COL: {
                            dir = Direction.UP;
                            offy = -1;
                        }break;
                    }
                    break;
            }

            Direction op = dir.getOpposite();
            int i = limit2 - 1;
            int k = 0;
            boolean stop1 = false;
            boolean stop2 = false;
            // boolean intersects = false;

            boolean dont_check_state = false;
            boolean eq;
            int n=WandProps.getVal(wand.wand_stack, WandProps.Value.ROWCOLLIM);
            if( wand.target_air && wand.mode.can_target_air() && level.getBlockState(wand.pos).isAir()) {
                pos1=wand.pos;
                pos3=wand.pos;
                while (k < limit2 && i > 0) {
                    if (!stop1 && i > 0) {
                        BlockState bs0 = level.getBlockState(pos1.relative(dir));
                        if ((bs0.isAir() || wand.replace_fluid(bs0))) {
                            pos1 = pos1.relative(dir);
                            i--;
                        } else {
                            stop1 = true;
                        }
                    }
                    if (!stop2 && i > 0) {
                        BlockState bs2 = level.getBlockState(pos3.relative(op));
                        if (bs2.isAir() || wand.replace_fluid(bs2)) {
                            pos3 = pos3.relative(op);
                            i--;
                        } else {
                            stop2 = true;
                        }
                    }
                    k++;
                    if (stop1 && stop2) {
                        k = 1000000;
                    }
                }
            }else if(n==0) {
                while (k < limit2 && i > 0) {
                    if (!stop1 && i > 0) {
                        BlockState bs0 = level.getBlockState(pos0.relative(dir));
                        BlockState bs1 = level.getBlockState(pos1.relative(dir));
                        if (dont_check_state) {
                            eq = bs0.getBlock().equals(wand.block_state.getBlock());
                        } else {
                            eq = bs0.equals(wand.block_state);
                        }
                        eq = eq || wand.palette.state_in_slot(bs0);
                        if (eq && (bs1.isAir() || wand.replace_fluid(bs1))) {
                            pos0 = pos0.relative(dir);
                            pos1 = pos1.relative(dir);
                            i--;
                        } else {
                            stop1 = true;
                        }
                    }
                    if (!stop2 && i > 0) {
                        BlockState bs2 = level.getBlockState(pos2.relative(op));
                        BlockState bs3 = level.getBlockState(pos3.relative(op));
                        if (dont_check_state) {
                            eq = bs2.getBlock().equals(wand.block_state.getBlock());
                        } else {
                            eq = bs2.equals(wand.block_state);
                        }
                        eq = eq || wand.palette.state_in_slot(bs2);
                        if (eq && (bs3.isAir() || wand.replace_fluid(bs3))) {
                            pos2 = pos2.relative(op);
                            pos3 = pos3.relative(op);
                            i--;
                        } else {
                            stop2 = true;
                        }
                    }
                    k++;
                    if (stop1 && stop2) {
                        k = 1000000;
                    }
                }
            }else{
                pos1=pos0.relative(wand.side);
                if(n==1) {
                    pos3 = pos1;
                }else {
                    pos2=pos0;
                    for(int m=0;m<n-1;m++) {
                        pos2 = pos2.relative(dir);
                        BlockState bs = level.getBlockState(pos2.relative(wand.side));
                        if(wand.can_place(bs,pos2.relative(wand.side))){
                            pos3=pos2;
                        }else{
                            break;
                        }
                    }
                    pos3 = pos3.relative(wand.side);
                }
            }
            if (wand.destroy || wand.replace ||wand.use) {
                pos1 = pos1.relative(wand.side.getOpposite());
                pos3 = pos3.relative(wand.side.getOpposite());
            }
            if (preview) {
                wand.x1 = pos1.getX() - offx;
                wand.y1 = pos1.getY() - offy;
                wand.z1 = pos1.getZ() - offz;
                wand.x2 = pos3.getX() + offx + 1;
                wand.y2 = pos3.getY() + offy + 1;
                wand.z2 = pos3.getZ() + offz + 1;
                wand.valid = true;
            }
            wand.calc_pv_bbox(pos1,pos3);
            wand.fill(pos1, pos3,false,0,0,0);
        } else {
            wand.valid = false;
        }
    }
}
