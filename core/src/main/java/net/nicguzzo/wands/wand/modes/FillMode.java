package net.nicguzzo.wands.wand.modes;

import net.minecraft.core.BlockPos;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;

public class FillMode extends WandMode {
    public void place_in_buffer(Wand wand) {
        if(!need_update(wand,true)){
            return;
        }
        if (wand.getP1() != null ){
            if(wand.getP2() !=null || wand.preview){
                // Use P2 if set (placement - already offset), otherwise use effective pos (preview)
                BlockPos endPos = (wand.getP2() != null) ? wand.getP2() : wand.getEffectiveEndPos();
                wand.calc_pv_bbox(wand.getP1(), endPos);
                boolean fill = WandProps.getFlag(wand.wand_stack, WandProps.Flag.RFILLED);
                wand.fill(wand.getP1(), endPos, !fill, 0, 0, 0);
                wand.validate_buffer();
            }
        }
    }
}
