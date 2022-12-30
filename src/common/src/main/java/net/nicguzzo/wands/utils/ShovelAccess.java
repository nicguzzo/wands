package net.nicguzzo.wands.utils;

import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.state.BlockState;
public class ShovelAccess extends ShovelItem {
        public ShovelAccess(Tier tier, int i, float f, Properties properties) {
            super(tier, i, f, properties);
        }
        public static boolean is_flattenable(BlockState state){
            return FLATTENABLES.get(state.getBlock())!=null;
        }
    }
