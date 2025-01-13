package net.nicguzzo.wands.wand.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;

public class TunnelMode implements WandMode {
    BlockPos.MutableBlockPos tp1=new BlockPos.MutableBlockPos(0,0,0);
    BlockPos.MutableBlockPos tp2=new BlockPos.MutableBlockPos(0,0,0);
    public void place_in_buffer(Wand wand) {
        /*if(!(wand.destroy||wand.replace)) {
            wand.valid=false;
            return;
        }*/
        int tw=  WandProps.getVal(wand.wand_stack, WandProps.Value.TUNNEL_W);
        int th=  WandProps.getVal(wand.wand_stack, WandProps.Value.TUNNEL_H);
        int td=  WandProps.getVal(wand.wand_stack, WandProps.Value.TUNNEL_DEPTH);
        int tox=  WandProps.getVal(wand.wand_stack, WandProps.Value.TUNNEL_OX);
        int toy=  WandProps.getVal(wand.wand_stack, WandProps.Value.TUNNEL_OY);

        Direction pdir=wand.player.getDirection();

        BlockPos b_pos = wand.pos;
        boolean sel= WandProps.getFlag(wand.wand_stack, WandProps.Flag.INCSELBLOCK);
        if(sel){
           b_pos = wand.pos.relative(wand.side, 1);
        }

        wand.valid = true;
        switch (pdir){
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
            case UP:
            case DOWN:{
                wand.valid=false;
                return;
            }
        }
        wand.calc_pv_bbox(tp1, tp2);
        wand.fill(tp1, tp2,false,0,0,0);
    }
}
