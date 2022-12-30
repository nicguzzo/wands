package net.nicguzzo.wands.utils;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.state.BlockState;

public class AxeAccess extends AxeItem {
    public AxeAccess(Tier tier, int i, float f, Properties properties) {
        super(tier, i, f, properties);
    }
    public static boolean is_stripable(BlockState state){
        #if MC=="1165"
        return STRIPABLES.get(state.getBlock())!=null;
        #else
        return STRIPPABLES.get(state.getBlock())!=null;
        #endif
    }
}