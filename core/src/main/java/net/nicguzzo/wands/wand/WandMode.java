package net.nicguzzo.wands.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public abstract class WandMode {
    protected BlockPos last_pos=null;
    protected Direction last_side=null;
    public abstract void place_in_buffer(Wand wand);
    public boolean action(Wand wand){return true;}
    public void randomize(){};
    public boolean need_update(Wand wand,boolean valid){
        if(wand.preview && last_pos!=null && last_side!=null &&
                ((last_pos.getX()==wand.pos.getX() &&
                last_pos.getY()==wand.pos.getY() &&
                last_pos.getZ()==wand.pos.getZ())
                        ||
                        last_side==wand.side
                )
        ){
            if(wand.block_buffer.get_length()>0) {
                wand.valid = valid;
            }
            return false;
        }
        last_pos=wand.pos;
        last_side=wand.side;
        return true;
    }
    public void redraw(Wand wand){
        last_pos=null;
        last_side=null;
    }
}
