package net.nicguzzo.wands.wand.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;

public class BoxMode extends WandMode {
    BlockPos.MutableBlockPos tp1=new BlockPos.MutableBlockPos(0,0,0);
    BlockPos.MutableBlockPos tp2=new BlockPos.MutableBlockPos(0,0,0);
    public void place_in_buffer(Wand wand) {
        /*if(!(wand.destroy||wand.replace)) {
            wand.valid=false;
            return;
        }*/
        int tw = WandProps.getVal(wand.wand_stack, WandProps.Value.BOX_W);
        int th = WandProps.getVal(wand.wand_stack, WandProps.Value.BOX_H);
        int td = WandProps.getVal(wand.wand_stack, WandProps.Value.BOX_DEPTH);
        int tox = WandProps.getVal(wand.wand_stack, WandProps.Value.BOX_OX);
        int toy = WandProps.getVal(wand.wand_stack, WandProps.Value.BOX_OY);

        // Use clicked face direction so the box is stable regardless of player angle
        Direction depthDir = wand.side;
        if (WandProps.getFlag(wand.wand_stack, WandProps.Flag.BOX_INVERTED)) {
            depthDir = depthDir.getOpposite();
        }

        // wand.pos is already offset by ClientRender/WandItem when INCSELBLOCK is off
        BlockPos b_pos = wand.pos;

        wand.valid = true;
        switch (depthDir){
            case NORTH:{
                tp1.set(b_pos.getX()-tox,b_pos.getY()-toy,b_pos.getZ());
                tp2.set(b_pos.getX()+(tw-1)-tox ,b_pos.getY()+(th-1)-toy ,b_pos.getZ()-(td-1));
            }break;
            case SOUTH:{
                tp1.set(b_pos.getX()+tox,b_pos.getY()-toy,b_pos.getZ());
                tp2.set(b_pos.getX()-(tw-1)+tox ,b_pos.getY()+(th-1)-toy ,b_pos.getZ()+(td-1));
            }break;
            case EAST:{
                tp1.set(b_pos.getX(),b_pos.getY()-toy,b_pos.getZ()-tox);
                tp2.set(b_pos.getX() +(td-1) ,b_pos.getY()+(th-1)-toy ,b_pos.getZ()+(tw-1)-tox);
            }break;
            case WEST:{
                tp1.set(b_pos.getX(),b_pos.getY()-toy,b_pos.getZ()+tox);
                tp2.set(b_pos.getX()-(td-1) ,b_pos.getY()+(th-1)-toy ,b_pos.getZ()-(tw-1)+tox);
            }break;
            case UP: {
                tp1.set(b_pos.getX() - tox, b_pos.getY() - toy, b_pos.getZ());
                tp2.set(b_pos.getX() + (tw - 1) - tox, b_pos.getY() + (th - 1) - toy, b_pos.getZ() + (td - 1));
            }break;
            case DOWN:{
                tp1.set(b_pos.getX() - tox, b_pos.getY() - toy, b_pos.getZ());
                tp2.set(b_pos.getX() + (tw - 1) - tox, b_pos.getY() - (th - 1) - toy, b_pos.getZ() + (td - 1));
           }
        }
        wand.calc_pv_bbox(tp1, tp2);
        wand.fill(tp1, tp2,false,0,0,0);
    }
}
