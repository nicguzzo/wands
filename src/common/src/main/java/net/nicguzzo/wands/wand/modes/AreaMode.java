package net.nicguzzo.wands.wand.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;

public class AreaMode implements WandMode {

    @Override
    public void place_in_buffer(Wand wand) {
        int limit2= WandProps.getVal(wand.wand_stack, WandProps.Value.AREALIM);
        if(limit2<=0){
            limit2=wand.wand_item.limit;
        }

        wand.block_buffer.reset();
        add_neighbour(wand.pos, wand.block_state,wand);
        int i = 0;
        int found = 1;
        limit2-=1;
        while (i < limit2 && i < wand.MAX_LIMIT && found <= limit2) {
            if (i < wand.block_buffer.get_length()) {
                BlockPos p = wand.block_buffer.get(i).relative(wand.side, -1);
                found += find_neighbours(p, wand.block_state,limit2,wand);
            }
            i++;
        }
        if (wand.destroy || wand.replace ||wand.use) {
            for (int a = 0; a < wand.block_buffer.get_length(); a++) {
                wand.block_buffer.set(a, wand.block_buffer.get(a).relative(wand.side, -1));
            }
        }
        wand.skip();
        wand.validate_buffer();
    }

    int add_neighbour(BlockPos bpos, BlockState state,Wand wand) {

        BlockPos pos2 = bpos.relative(wand.side);
        if (!wand.block_buffer.in_buffer(pos2)) {
            BlockState bs1 = wand.level.getBlockState(bpos);
            BlockState bs2 = wand.level.getBlockState(pos2);
            if (wand.block_buffer.get_length() < wand.limit &&
                    (
                            (wand.match_state && bs1.equals(state)) ||
                                    (!wand.match_state && bs1.getBlock().equals(state.getBlock())) ||
                                    wand.palette.state_in_slot(bs1)
                    ) &&
                    (((wand.destroy ||wand.replace) && bs2.isAir()) || wand.can_place(bs2,pos2)))
            {
                wand.add_to_buffer(pos2.getX(),pos2.getY(),pos2.getZ());
                return 1;

            }
        }
        return 0;
    }
    int find_neighbours(BlockPos bpos, BlockState state, int limit,Wand wand) {
        int found=0;
        //boolean diag=WandProps.getAreaDiagonalSpread(wand_stack);
        boolean diag= WandProps.getFlag(wand.wand_stack, WandProps.Flag.DIAGSPREAD);
        if (wand.side == Direction.UP || wand.side == Direction.DOWN) {
            BlockPos p0 = bpos.relative( Direction.EAST, 1);
            found+=add_neighbour(p0, state,wand);
            if(found>= limit)
                return found;

            p0 = bpos.relative( Direction.NORTH, 1);
            found+=add_neighbour(p0, state,wand);
            if(found>= limit)
                return found;

            p0 = bpos.relative( Direction.WEST, 1);
            found+=add_neighbour(p0, state,wand);
            if(found>= limit)
                return found;

            p0 = bpos.relative( Direction.SOUTH, 1);
            found+=add_neighbour(p0, state,wand);
            if(found>= limit)
                return found;
            if(!diag) {
                p0 = bpos.relative(Direction.EAST, 1);
                BlockPos p1 = p0.relative(Direction.NORTH, 1);
                found += add_neighbour(p1, state,wand);
                if (found >= limit)
                    return found;

                p0 = bpos.relative(Direction.NORTH, 1);
                p1 = p0.relative(Direction.WEST, 1);
                found += add_neighbour(p1, state,wand);
                if (found >= limit)
                    return found;

                p0 = bpos.relative(Direction.SOUTH, 1);
                p1 = p0.relative(Direction.WEST, 1);
                found += add_neighbour(p1, state,wand);
                if (found >= limit)
                    return found;

                p0 = bpos.relative(Direction.SOUTH, 1);
                p1 = p0.relative(Direction.EAST, 1);
                found += add_neighbour(p1, state,wand);
                if (found >= limit)
                    return found;
            }

        } else {
            if (wand.side == Direction.EAST || wand.side == Direction.WEST) {
                BlockPos p0 = bpos.relative( Direction.UP, 1);
                found+=add_neighbour(p0, state,wand);
                if(found>= limit)
                    return found;

                p0 = bpos.relative( Direction.NORTH, 1);
                found+=add_neighbour(p0, state,wand);
                if(found>= limit)
                    return found;

                p0 = bpos.relative( Direction.DOWN, 1);
                found+=add_neighbour(p0, state,wand);
                if(found>= limit)
                    return found;

                p0 = bpos.relative( Direction.SOUTH, 1);
                found+=add_neighbour(p0, state,wand);
                if(found>= limit)
                    return found;

                if(!diag) {
                    p0 = bpos.relative( Direction.UP, 1);
                    BlockPos p1 = p0.relative( Direction.NORTH, 1);
                    found+=add_neighbour(p1, state,wand);
                    if(found>= limit)
                        return found;

                    p0 = bpos.relative( Direction.NORTH, 1);
                    p1 = p0.relative( Direction.DOWN, 1);
                    found+=add_neighbour(p1, state,wand);
                    if(found>= limit)
                        return found;

                    p0 = bpos.relative( Direction.SOUTH, 1);
                    p1 = p0.relative( Direction.DOWN, 1);
                    found+=add_neighbour(p1, state,wand);
                    if(found>= limit)
                        return found;

                    p0 = bpos.relative( Direction.SOUTH, 1);
                    p1 = p0.relative( Direction.UP, 1);
                    found+=add_neighbour(p1, state,wand);
                    if(found>= limit)
                        return found;
                }

            } else if (wand.side == Direction.NORTH || wand.side == Direction.SOUTH) {
                BlockPos p0 = bpos.relative( Direction.EAST, 1);
                found+=add_neighbour(p0, state,wand);
                if(found>= limit)
                    return found;

                p0 = bpos.relative( Direction.UP, 1);
                found+=add_neighbour(p0, state,wand);
                if(found>= limit)
                    return found;

                p0 = bpos.relative( Direction.WEST, 1);
                found+=add_neighbour(p0, state,wand);
                if(found>= limit)
                    return found;

                p0 = bpos.relative( Direction.DOWN, 1);
                found+=add_neighbour(p0, state,wand);
                if(found>= limit)
                    return found;

                if(!diag) {
                    p0 = bpos.relative( Direction.EAST, 1);
                    BlockPos p1 = p0.relative( Direction.UP, 1);
                    found+=add_neighbour(p1, state,wand);
                    if(found>= limit)
                        return found;
                    p0 = bpos.relative( Direction.UP, 1);
                    p1 = p0.relative( Direction.WEST, 1);
                    found+=add_neighbour(p1, state,wand);
                    if(found>= limit)
                        return found;
                    p0 = bpos.relative( Direction.DOWN, 1);
                    p1 = p0.relative( Direction.WEST, 1);
                    found+=add_neighbour(p1, state,wand);
                    if(found>= limit)
                        return found;
                    p0 = bpos.relative(Direction.DOWN, 1);
                    p1 = p0.relative(Direction.EAST, 1);
                    found += add_neighbour(p1, state,wand);
                    if (found >= limit)
                        return found;
                }
            }
        }
        return found;
    }
}
