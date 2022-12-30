package net.nicguzzo.wands.wand.modes;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.nicguzzo.wands.wand.BlockAccounting;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;

public class BlastMode implements WandMode {
    public void place_in_buffer(Wand wand) {
        wand.block_buffer.reset();
        if(!wand.preview){
            if(!wand.creative) {
                wand.block_accounting.clear();
                BlockAccounting ba=new BlockAccounting();
                int rad= WandProps.getVal(wand.wand_stack, WandProps.Value.BLASTRAD);
                int extra_cost=rad-4;
                ba.needed=1+extra_cost;
                wand.block_accounting.put(Items.TNT,ba);
            }
        }
    }

    @Override
    public boolean action(Wand wand) {

        if( wand.wand_item.can_blast) {
            boolean do_explode = false;
            BlockAccounting ba = null;
            if (wand.creative) {
                do_explode = true;
            } else {
                ba = wand.block_accounting.get(Items.TNT);
                if (ba != null) {
                    do_explode = (ba.needed <= ba.in_player);
                }
            }
            if (do_explode) {
                float radius = (float) WandProps.getVal(wand.wand_stack, WandProps.Value.BLASTRAD);
                float eo = 1.0625f;
                if (wand.side == Direction.DOWN) {
                    eo = 0.0625f;
                }
                //Explosion.BlockInteraction bi=(creative?Explosion.BlockInteraction.DESTROY: Explosion.BlockInteraction.BREAK);
#if MC>="1193"
                wand.level.explode(wand.player, wand.pos.getX(), wand.pos.getY() + eo, wand.pos.getZ(), radius, Level.ExplosionInteraction.BLOCK);
#else
                wand.level.explode(wand.player, wand.pos.getX(), wand.pos.getY() + eo, wand.pos.getZ(), radius, Explosion.BlockInteraction.BREAK);
#endif
                if (!wand.creative && ba != null) {
                    ba.placed = ba.needed;
                }
                return true;
            }
        }
        return false;
    }
}
