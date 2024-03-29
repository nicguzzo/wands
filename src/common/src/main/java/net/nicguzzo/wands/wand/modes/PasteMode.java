package net.nicguzzo.wands.wand.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.wand.CopyBuffer;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;

public class PasteMode implements WandMode {

    public void place_in_buffer(Wand wand) {
        if(wand.copy_paste_buffer==null || wand.copy_paste_buffer.size()==0)
            return;
        if (!wand.preview) {
            int mx=1;
            int my=1;
            int mz=1;
            int mirroraxis= WandProps.getVal(wand.wand_stack, WandProps.Value.MIRRORAXIS);
            switch(mirroraxis){
                case 1://X
                    mx=-1;
                    break;
                case 2://Y
                    mz=-1;
                    break;
                /*case 3://Z
                    mz=-1;
                    break;*/
            }
            //log("mode6 paste "+copy_paste_buffer.size());
            //BlockPos b_pos = wand.pos;
            BlockPos b_pos = wand.getP1();
            boolean sel= WandProps.getFlag(wand.wand_stack, WandProps.Flag.INCSELBLOCK);
            if(sel){
                b_pos = wand.pos.relative(wand.side, 1);
            }
            /*BlockState s=wand.level.getBlockState(wand.pos);
            boolean targeting_air=s.isAir();
            if(!targeting_air && !(wand.replace || wand.destroy)) {
                b_pos = wand.pos.relative(wand.side, 1);
            }*/
            //BlockPos.MutableBlockPos bp = new BlockPos.MutableBlockPos();
            wand.block_buffer.reset();
            //wand.random.setSeed(wand.palette.seed);

            for (CopyBuffer b : wand.copy_paste_buffer) {
                BlockPos p = b.pos.rotate(wand.rotation);
                BlockState st=b.state;
                int px=b_pos.getX() + p.getX()*mx;
                int py=b_pos.getY() + p.getY();
                int pz=b_pos.getZ() + p.getZ()*mz;
                st=wand.rotate_mirror(st,mirroraxis);
                if(wand.palette.has_palette) {
                    wand.block_buffer.add(px,py,pz,wand);
                }else {
                    wand.block_buffer.add(px,py,pz,st, st.getBlock().asItem());
                }
            }
            wand.validate_buffer();
        }
    }
}
