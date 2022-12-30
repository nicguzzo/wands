package net.nicguzzo.wands.utils;

import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.state.BlockState;

public class HoeAccess extends HoeItem {
        public HoeAccess(Tier tier, int i, float f, Properties properties) {
            super(tier, i, f, properties);
        }
        public static boolean is_tillable(BlockState state){
            return TILLABLES.get(state.getBlock())!=null;
        }
    }
