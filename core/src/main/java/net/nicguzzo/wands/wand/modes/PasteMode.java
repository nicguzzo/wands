package net.nicguzzo.wands.wand.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.wand.CopyBuffer;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;

public class PasteMode extends WandMode {

    public void place_in_buffer(Wand wand) {
        if(wand.copy_paste_buffer==null || wand.copy_paste_buffer.size()==0)
            return;
        // Always populate buffer (for both preview and placement) so rendering matches placement
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
        }
        // wand.pos is already offset by ClientRender/WandItem when INCSELBLOCK is off
        BlockPos b_pos = wand.pos;
        wand.block_buffer.reset();

        for (CopyBuffer b : wand.copy_paste_buffer) {
            BlockPos p = b.pos.rotate(wand.rotation);
            BlockState st=b.state;
            int px=b_pos.getX() + p.getX()*mx;
            int py=b_pos.getY() + p.getY();
            int pz=b_pos.getZ() + p.getZ()*mz;
            st=wand.rotate_mirror(st,mirroraxis);
            if (wand.palette.has_palette) {
                wand.block_buffer.add(px, py, pz, wand, null);
            } else {
                wand.block_buffer.add(px, py, pz, st, st.getBlock().asItem());
            }
        }
        if(wand.block_buffer.get_length()>0) {
            wand.valid = true;
            wand.limit_reached=false;
        }
    }
}
