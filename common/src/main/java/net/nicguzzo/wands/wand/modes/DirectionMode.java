package net.nicguzzo.wands.wand.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nicguzzo.wands.utils.WandUtils;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;

import java.util.List;

public class DirectionMode  extends WandMode {
    public void place_in_buffer(Wand wand) {
        Direction[] dirs = getDirMode0(wand.side, wand.hit.x, wand.hit.y, wand.hit.z,wand);
        boolean invert = WandProps.getFlag(wand.wand_stack, WandProps.Flag.INVERTED);
        if (invert) {
            if (dirs[0] != null)
                dirs[0] = dirs[0].getOpposite();
            if (dirs[1] != null)
                dirs[1] = dirs[1].getOpposite();
        }
        Direction d1 = dirs[0];
        Direction d2 = dirs[1];
        if (wand.preview) {
            wand.x = wand.pos.getX();
            wand.y = wand.pos.getY();
            wand.z = wand.pos.getZ();
        }
        if (d1 != null) {
            BlockPos dest;
            wand.block_buffer.reset();
            boolean diag=false;
            if (d2 != null) {
                dest = find_next_diag(wand.block_state, d1, d2, wand.pos,wand);
                diag=true;
            } else {
                dest = find_next_pos(wand.block_state, d1, wand.pos,wand);
            }
            int multiplier=WandProps.getVal(wand.wand_stack, WandProps.Value.MULTIPLIER);
            for(int i=0;i<multiplier;i++) {
                if (dest != null) {
                    wand.x1 = dest.getX();
                    wand.y1 = dest.getY();
                    wand.z1 = dest.getZ();
                    wand.x2 = wand.x1 + 1;
                    wand.y2 = wand.y1 + 1;
                    wand.z2 = wand.z1 + 1;
                    wand.valid = wand.add_to_buffer(dest.getX(), dest.getY(), dest.getZ());
                    if(diag) {
                        dest=dest.relative(d1,1);
                        dest=dest.relative(d2,1);
                    }else{
                        dest=dest.relative(d1,1);
                    }
                    BlockState bs = wand.level.getBlockState(dest);
                    if (!wand.can_place(bs,dest)) {
                        break;
                    }
                }else{
                    break;
                }
            }
        }
    }
    Direction[] getDirMode0(Direction side, double hit_x, double hit_y, double hit_z,Wand wand) {
        Direction[] ret = new Direction[2];
        ret[0] = null;
        ret[1] = null;
        double a = 0.333333f;
        double b = 0.666666f;
        double x = WandUtils.unitCoord( hit_x);
        double y = WandUtils.unitCoord( hit_y);
        double z = WandUtils.unitCoord( hit_z);
        double xo=x;
        double yo=y;
        double zo=z;
        VoxelShape shape = wand.block_state.getShape(wand.level, wand.pos);
        //log("--");
        //log("x: "+x+" y: "+y+" z: "+z);
        List<AABB> list = shape.toAabbs();
        int index=0;
        for (AABB aabb : list) {

            switch (side) {
                case UP:
                case DOWN:
                    if(xo>aabb.minX&& xo<aabb.maxX && zo> aabb.minZ && zo<aabb.maxZ) {
                        //log("bb: "+aabb);
                        if (aabb.getXsize() != 0)
                            x = (float) ((x - aabb.minX) / aabb.getXsize());
                        if (aabb.getZsize() != 0)
                            z = (float) ((z - aabb.minZ) / aabb.getZsize());
                        //log("      x: " + x + " y: " + y + " z: " + z);
                        wand.grid_voxel_index=index;
                    }
                    break;
                case EAST:
                case WEST:
                    //if(last_x>aabb.maxX)
                {
                    if (yo > aabb.minY && yo < aabb.maxY && zo > aabb.minZ && zo < aabb.maxZ) {
                        //log("bb: " + aabb);
                        if (aabb.getYsize() != 0)
                            y = (float) ((y - aabb.minY) / aabb.getYsize());
                        if (aabb.getZsize() != 0)
                            z = (float) ((z - aabb.minZ) / aabb.getZsize());
                        //log("      x: " + x + " y: " + y + " z: " + z);
                        wand.grid_voxel_index = index;
                    }
                }
                break;
                case NORTH:
                case SOUTH:
                    if(xo>aabb.minX&& xo<aabb.maxX && yo> aabb.minY && yo<aabb.maxY){
                        //log("bb: " + aabb);
                        if(aabb.getXsize()!=0)
                            x =  (float)((x-aabb.minX)/aabb.getXsize());
                        if(aabb.getYsize()!=0)
                            y = (float)((y-aabb.minY)/aabb.getYsize());
                        wand.grid_voxel_index=index;
                        //log("      x: " + x + " y: " + y + " z: " + z);
                    }
                    break;
            }
            index++;
        }
        switch (side) {
            case UP:
            case DOWN:
                if (x >= a && x <= b) {
                    if (z <= a) {
                        ret[0] = Direction.NORTH;
                    } else {
                        if (z >= b) {
                            ret[0] = Direction.SOUTH;
                        } else {
                            ret[0] = side.getOpposite();
                        }
                    }
                } else {
                    if (z >= a && z <= b) {
                        if (x <= a) {
                            ret[0] = Direction.WEST;
                        } else {
                            ret[0] = Direction.EAST;
                        }
                    } else {
                        if (x <= a && z <= a) {
                            ret[0] = Direction.WEST;
                            ret[1] = Direction.NORTH;
                        }
                        if (x >= b && z <= a) {
                            ret[0] = Direction.EAST;
                            ret[1] = Direction.NORTH;
                        }
                        if (x >= b && z >= b) {
                            ret[0] = Direction.EAST;
                            ret[1] = Direction.SOUTH;
                        }
                        if (x <= a && z >= b) {
                            ret[0] = Direction.WEST;
                            ret[1] = Direction.SOUTH;
                        }
                    }
                }
                break;
            case EAST:
            case WEST:

                if (z >= a && z <= b) {
                    if (y <= a) {
                        ret[0] = Direction.DOWN;
                    } else {
                        if (y >= b) {
                            ret[0] = Direction.UP;
                        } else {
                            ret[0] = side.getOpposite();
                        }
                    }
                } else {
                    if (y >= a && y <= b) {
                        if (z <= a) {
                            ret[0] = Direction.NORTH;
                            return ret;
                        } else {
                            ret[0] = Direction.SOUTH;
                            return ret;
                        }
                    } else {
                        if (y <= a && z <= a) {
                            ret[0] = Direction.DOWN;
                            ret[1] = Direction.NORTH;
                        }
                        if (y >= b && z <= a) {
                            ret[0] = Direction.UP;
                            ret[1] = Direction.NORTH;
                        }
                        if (y >= b && z >= b) {
                            ret[0] = Direction.UP;
                            ret[1] = Direction.SOUTH;
                        }
                        if (y <= a && z >= b) {
                            ret[0] = Direction.DOWN;
                            ret[1] = Direction.SOUTH;
                        }
                    }
                }
                break;
            case NORTH:
            case SOUTH:
                if (x >= a && x <= b) {
                    if (y <= a) {
                        ret[0] = Direction.DOWN;
                    } else {
                        if (y >= b) {
                            ret[0] = Direction.UP;
                        } else {
                            ret[0] = side.getOpposite();
                        }
                    }
                } else {
                    if (y >= a && y <= b) {
                        if (x <= a) {
                            ret[0] = Direction.WEST;
                        } else {
                            ret[0] = Direction.EAST;
                        }
                    } else {
                        if (y <= a && x <= a) {
                            ret[0] = Direction.DOWN;
                            ret[1] = Direction.WEST;
                        }
                        if (y >= b && x <= a) {
                            ret[0] = Direction.UP;
                            ret[1] = Direction.WEST;
                        }
                        if (y >= b && x >= b) {
                            ret[0] = Direction.UP;
                            ret[1] = Direction.EAST;
                        }
                        if (y <= a && x >= b) {
                            ret[0] = Direction.DOWN;
                            ret[1] = Direction.EAST;
                        }
                    }
                }
                break;
        }
        return ret;
    }
    BlockPos find_next_diag(BlockState state, Direction dir1, Direction dir2, BlockPos bpos,Wand wand) {
        BlockPos p0=bpos;
        for (int i = 0; i < wand.limit; i++) {
            BlockPos p1 = bpos.relative(dir1);
            bpos = p1.relative(dir2);
            BlockState bs = wand.level.getBlockState(bpos);
            if (bs != null) {
                if(wand.destroy || wand.use || wand.replace){
                    if (/*p0!=wand.pos && */!(bs.is(state.getBlock()) || wand.palette.state_in_slot(bs) /* ||(offhand_state!=null&&  bs.is(offhand_state.getBlock()))*/)&& p0!=null)
                        return p0;
                }else{
                    if (wand.can_place(bs,bpos)) {
                        return bpos;
                    } else {
                        if (!(bs.is(state.getBlock()) || wand.palette.state_in_slot(bs) ||(wand.offhand_state!=null&&  bs.is(wand.offhand_state.getBlock()))))
                            return null;
                    }
                }
            }
            p0=bpos;
        }
        return null;
    }
    BlockPos find_next_pos(BlockState state, Direction dir, BlockPos bpos,Wand wand) {
        for (int i = 0; i < wand.limit; i++) {
            BlockPos pos2 = bpos.relative(dir, i + 1);
            BlockState bs = wand.level.getBlockState(pos2);

            if (bs != null) {
                if (!(bs.is(state.getBlock())|| wand.palette.state_in_slot(bs) )) {
                    if(wand.destroy || wand.use || wand.replace){
                        pos2=bpos.relative(dir, i);
                        if(pos2!=wand.pos)
                            return pos2;
                    }else{
                        if (wand.can_place(bs,pos2)) {
                            return pos2;
                        } else {
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }
}
