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

        wand.valid = true;
        switch (pdir){
            case NORTH:{
                tp1.set(wand.pos.getX()-tox,wand.pos.getY()-toy,wand.pos.getZ());
                tp2.set(wand.pos.getX()+(tw-1)-tox ,wand.pos.getY()+(th-1)-toy ,wand.pos.getZ()-(td-1));
            }break;
            case SOUTH:{
                tp1.set(wand.pos.getX()+tox,wand.pos.getY()-toy,wand.pos.getZ());
                tp2.set(wand.pos.getX()-(tw-1)+tox ,wand.pos.getY()+(th-1)-toy ,wand.pos.getZ()+(td-1));
            }break;
            case EAST:{
                tp1.set(wand.pos.getX(),wand.pos.getY()-toy,wand.pos.getZ()-tox);
                tp2.set(wand.pos.getX() +(td-1) ,wand.pos.getY()+(th-1)-toy ,wand.pos.getZ()+(tw-1)-tox);
            }break;
            case WEST:{
                tp1.set(wand.pos.getX(),wand.pos.getY()-toy,wand.pos.getZ()+tox);
                tp2.set(wand.pos.getX()-(td-1) ,wand.pos.getY()+(th-1)-toy ,wand.pos.getZ()-(tw-1)+tox);
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
