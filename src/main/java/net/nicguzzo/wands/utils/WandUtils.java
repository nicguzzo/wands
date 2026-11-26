package net.nicguzzo.wands.utils;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.items.WandItem;
//?if < 26.3 {
import net.nicguzzo.wands.mixin.AxeItemAccessor;
import net.nicguzzo.wands.mixin.HoeItemAccessor;
import net.nicguzzo.wands.mixin.ShovelItemAccessor;
//?}
//?if  >= 1.20.5 {
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.component.DataComponents;
//?}else{
/*import net.minecraft.world.item.alchemy.PotionUtils;
*///?}

public class WandUtils{

    public static boolean is_tillable(BlockState state){
        //?if < 26.3 {
        return HoeItemAccessor.getTillables().get(state.getBlock())!=null;
        //?} else {
        /*return state.is(BlockTags.DIRT);
        *///?}
    }
    public static boolean is_strippable(BlockState state){
        //?if < 26.3 {
        return AxeItemAccessor.getStrippables().get(state.getBlock())!=null;
        //?} else {
        /*return state.is(BlockTags.LOGS);
        *///?}
    }
    public static boolean can_axe_use(BlockState state){
        return is_strippable(state) ||
               WeatheringCopper.getPrevious(state).isPresent() ||
               HoneycombItem.WAX_OFF_BY_BLOCK.get().containsKey(state.getBlock());
    }
    public static boolean is_flattenable(BlockState state){
        //?if < 26.3 {
        return ShovelItemAccessor.getFlattenables().get(state.getBlock())!=null;
        //?} else {
        /*return state.is(BlockTags.TURNS_INTO_DIRT_PATH) || state.is(BlockTags.DIRT);
        *///?}
    }
    public static boolean has_use_action(BlockState state){
        return is_tillable(state) || can_axe_use(state) || is_flattenable(state);
    }
    static public boolean is_wand(ItemStack stack) {
        return stack!=null && !stack.isEmpty() && stack.getItem() instanceof WandItem;
    }
    static public boolean is_shulker(ItemStack item_stack){
		return Block.byItem(item_stack.getItem()) instanceof ShulkerBoxBlock ;
    }
    static public boolean is_magicbag(ItemStack item_stack){
        return item_stack.getItem() instanceof MagicBagItem;
    }
    static public boolean is_plant(BlockState state) {
        return (state.getBlock() instanceof BushBlock)|| (state.getBlock() instanceof VineBlock);
    }
    static public double unitCoord(double a) {
        double b = a - ((int) a);
        if (b < 0)
            b = 1.0f + b;
        return b;
    }
    /** Total XP needed to reach the start of a vanilla experience level. */
    public static double xpAtLevel(int level) {
        double square = (double) level * level;
        if (level >= 32) return 4.5 * square - 162.5 * level + 2220;
        if (level >= 17) return 2.5 * square - 40.5 * level + 360;
        return square + 6.0 * level;
    }

    public static boolean pickaxe_minable(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE);
    }
    public static double mapRange(double a1, double a2, double b1, double b2, double s){
		return b1 + ((s - a1)*(b2 - b1))/(a2 - a1);
	}
    public static int mapRange(int a1, int a2, int b1, int b2, int s){
        int den=a2 - a1;
        if(den==0) return b1;
		return b1 + ((s - a1)*(b2 - b1))/den;
	}
    public static boolean hasWaterPotion(ItemStack offhand) {
        //?if  >= 1.20.5 {
            if (offhand.has(DataComponents.POTION_CONTENTS)) {
                PotionContents potion_c = offhand.get(DataComponents.POTION_CONTENTS);
                if (potion_c != null) {
                    return potion_c.is(Potions.WATER);
                }
            }
            return false;
        //?}else{
            /*return PotionUtils.getPotion(offhand) == Potions.WATER;
        *///?}
    }
}
