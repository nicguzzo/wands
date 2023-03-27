package net.nicguzzo.wands.wand.modes;

import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;

public class FillMode implements WandMode {
    public void place_in_buffer(Wand wand) {
        if (wand.p1 != null ){
            if(wand.p2!=null || wand.preview){
                wand.calc_pv_bbox(wand.p1, wand.pos);
                boolean fill = WandProps.getFlag(wand.wand_stack, WandProps.Flag.RFILLED);
                wand.fill(wand.p1, wand.pos, !fill, 0, 0, 0);
                wand.validate_buffer();
            }
        }
    }
}
