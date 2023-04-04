package net.nicguzzo.wands.wand.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;

public class GridMode implements WandMode {
    public void place_in_buffer(Wand wand) {
        Direction playerdir=wand.player.getDirection();;

        int ms=  WandProps.getVal(wand.wand_stack, WandProps.Value.GRIDMS);
        int ns=  WandProps.getVal(wand.wand_stack, WandProps.Value.GRIDNS);
        int moff=WandProps.getVal(wand.wand_stack, WandProps.Value.GRIDMOFF);
        int noff=WandProps.getVal(wand.wand_stack, WandProps.Value.GRIDNOFF);
        int m=   WandProps.getVal(wand.wand_stack, WandProps.Value.GRIDM)-1;
        int n=   WandProps.getVal(wand.wand_stack, WandProps.Value.GRIDN)-1;

        m=m*(ms+1);
        n=n*(ns+1);
        int xskip=ms;
        int yskip=0;
        int zskip=ns;
        if(playerdir== Direction.EAST||playerdir==Direction.WEST){
            xskip=ns;
            zskip=ms;
        }

        wand.block_buffer.reset();
        Direction dir1=Direction.SOUTH;
        Direction dir2= Direction.EAST;
        switch (wand.side) {
            case UP:
            case DOWN:
                switch (wand.rotation) {
                    case NONE:
                        dir1=playerdir.getClockWise();
                        dir2=playerdir;
                        break;
                    case CLOCKWISE_90:
                        dir1=playerdir.getClockWise().getClockWise();
                        dir2=playerdir.getClockWise();
                        break;
                    case CLOCKWISE_180:
                        dir1=playerdir.getClockWise().getClockWise().getClockWise();
                        dir2=playerdir.getClockWise().getClockWise();
                        break;
                    case COUNTERCLOCKWISE_90:
                        dir1=playerdir;
                        dir2=playerdir.getClockWise().getClockWise().getClockWise();
                        break;
                }
                break;
            case SOUTH:
            case NORTH:
            case EAST:
            case WEST:
                if(playerdir==Direction.SOUTH||playerdir==Direction.NORTH)
                    zskip=0;
                if(playerdir==Direction.EAST||playerdir==Direction.WEST)
                    xskip=0;
                yskip=ns;
                switch (wand.rotation) {
                    case NONE:
                        dir1=playerdir.getClockWise();
                        dir2 = Direction.UP;
                        break;
                    case CLOCKWISE_90:
                        dir1=playerdir.getClockWise();
                        dir2 = Direction.DOWN;
                        break;
                    case CLOCKWISE_180:
                        dir1=playerdir.getClockWise().getOpposite();
                        dir2 = Direction.DOWN;
                        break;
                    case COUNTERCLOCKWISE_90:
                        dir1=playerdir.getClockWise().getOpposite();
                        dir2 = Direction.UP;
                        break;
                }
                break;
        }
        BlockPos pos1;
        BlockPos pos2;
        if (wand.destroy || wand.replace ||wand.use) {
            pos1=wand.pos.relative(wand.side, -1).relative(wand.side);
            pos2=wand.pos.relative(wand.side, -1).relative(dir1,m).relative(dir2,n).relative(wand.side);
        }else{
            pos1=wand.pos.relative(wand.side);
            pos2=wand.pos.relative(dir1,m).relative(dir2,n).relative(wand.side);
        }
        if(moff>0||noff>0){
            pos1=pos1.relative(dir1,-moff).relative(dir2,-noff);
            pos2=pos2.relative(dir1,-moff).relative(dir2,-noff);
        }
        wand.calc_pv_bbox(pos1,pos2);
        wand.fill(pos1, pos2,false,xskip,yskip,zskip);
        wand.validate_buffer();
    }
}
