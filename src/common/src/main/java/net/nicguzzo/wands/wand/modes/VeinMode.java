package net.nicguzzo.wands.wand.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;

public class VeinMode implements WandMode {
    private BlockPos[] vein_dirs=new BlockPos[26];
    public void place_in_buffer(Wand wand) {
        if (!(wand.destroy || wand.replace ||wand.use)) {
            return;
        }
        int limit2= WandProps.getVal(wand.wand_stack, WandProps.Value.AREALIM);
        if(limit2<=0){
            limit2=wand.limit;
        }
        wand.block_buffer.reset();
        int found = 1;
        limit2-=1;
        int i=0;
        wand.add_to_buffer(wand.pos.getX(),wand.pos.getY(),wand.pos.getZ());
        int from=0;
        int to=1;
        int k;
        int cost=0;
        while (i < limit2 && i < WandsConfig.max_limit && found <= limit2) {
            k=0;
            for(int j=from;j<to;j++) {
                next_vein_layer(vein_dirs, wand.block_buffer.get(j));
                for (BlockPos p : vein_dirs) {
                    if(p!=null) {
                        BlockState st = wand.level.getBlockState(p);
                        if (!st.isAir() && (
                                (wand.match_state && st.equals(wand.block_state)) ||
                                (!wand.match_state && st.getBlock().equals(wand.block_state.getBlock())) ||
                                wand.palette.state_in_slot(st)
                        )) {
                            if (!wand.block_buffer.in_buffer(p)) {
                                wand.add_to_buffer(p.getX(), p.getY(), p.getZ());
                                found++;
                                k++;
                            }
                        }
                    }
                    cost++;
                }
            }
            if(k==0){
                break;
            }
            from=to;
            to=wand.block_buffer.get_length();
            i++;
        }
        //if(!preview){
            //WandsMod.LOGGER.info("cost: "+cost);
        //}
        wand.validate_buffer();
    }
    void next_vein_layer(BlockPos[] vein_dirs,BlockPos bpos){
        vein_dirs[0] =bpos.north();
        vein_dirs[1] =bpos.east();
        vein_dirs[2] =bpos.west();
        vein_dirs[3] =bpos.south();
        vein_dirs[4] =bpos.above();
        vein_dirs[5] =bpos.below();
        vein_dirs[6] =bpos.north().east();
        vein_dirs[7] =bpos.north().west();
        vein_dirs[8] =bpos.north().above();
        vein_dirs[9] =bpos.north().below();
        vein_dirs[10] =bpos.south().east();
        vein_dirs[11] =bpos.south().west();
        vein_dirs[12] =bpos.south().above();
        vein_dirs[13] =bpos.south().below();
        vein_dirs[14] =bpos.east().below();
        vein_dirs[15] =bpos.west().below();
        vein_dirs[16] =bpos.east().above();
        vein_dirs[17] =bpos.west().above();
        vein_dirs[18] =vein_dirs[6].above();
        vein_dirs[19] =vein_dirs[6].below();
        vein_dirs[20] =vein_dirs[7].above();
        vein_dirs[21] =vein_dirs[7].below();
        vein_dirs[22] =vein_dirs[10].above();
        vein_dirs[23] =vein_dirs[10].below();
        vein_dirs[24] =vein_dirs[11].above();
        vein_dirs[25] =vein_dirs[11].below();
    }
}
