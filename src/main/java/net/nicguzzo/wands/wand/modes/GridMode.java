package net.nicguzzo.wands.wand.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;

public class GridMode extends WandMode {
    public void place_in_buffer(Wand wand) {
        Direction playerdir=wand.player.getDirection();

        int ms=  WandProps.getVal(wand.wand_stack, WandProps.Value.GRIDMS);
        int ns=  WandProps.getVal(wand.wand_stack, WandProps.Value.GRIDNS);
        int moff=WandProps.getVal(wand.wand_stack, WandProps.Value.GRIDMOFF);
        int noff=WandProps.getVal(wand.wand_stack, WandProps.Value.GRIDNOFF);
        int m=   WandProps.getVal(wand.wand_stack, WandProps.Value.GRIDM)-1;
        int n=   WandProps.getVal(wand.wand_stack, WandProps.Value.GRIDN)-1;

        m=m*(ms+1);
        n=n*(ns+1);
        Direction wand_side=wand.getSide();
        wand.block_buffer.reset();
        Direction dir1;
        Direction dir2;
        if (wand_side.getAxis() == Direction.Axis.Y) {
            dir1 = wand.rotation.rotate(playerdir.getClockWise());
            dir2 = wand.rotation.rotate(playerdir);
        } else {
            dir1 = playerdir.getClockWise();
            dir2 = Direction.UP;
            switch (wand.rotation) {
                case CLOCKWISE_90 -> dir2 = Direction.DOWN;
                case CLOCKWISE_180 -> {
                    dir1 = dir1.getOpposite();
                    dir2 = Direction.DOWN;
                }
                case COUNTERCLOCKWISE_90 -> dir1 = dir1.getOpposite();
            }
        }
        int xskip = dir1.getAxis() == Direction.Axis.X ? ms : dir2.getAxis() == Direction.Axis.X ? ns : 0;
        int yskip = dir2.getAxis() == Direction.Axis.Y ? ns : 0;
        int zskip = dir1.getAxis() == Direction.Axis.Z ? ms : dir2.getAxis() == Direction.Axis.Z ? ns : 0;
        BlockPos pos1;
        BlockPos pos2;
        if (wand.destroy || wand.replace ||wand.use) {
            pos1=wand.pos.relative(wand_side, -1).relative(wand_side);
            pos2=wand.pos.relative(wand_side, -1).relative(dir1,m).relative(dir2,n).relative(wand_side);
        }else{
            pos1=wand.pos.relative(wand_side);
            pos2=wand.pos.relative(dir1,m).relative(dir2,n).relative(wand_side);
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
