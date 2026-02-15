package net.nicguzzo.wands.utils;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.compat.Compat;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.mixin.AxeItemAccessor;
import net.nicguzzo.wands.mixin.HoeItemAccessor;
import net.nicguzzo.wands.mixin.ShovelItemAccessor;
import java.util.List;
#if MC_VERSION >= 12005
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.component.DataComponents;
#else
import net.minecraft.world.item.alchemy.PotionUtils;
#endif
import java.util.Optional;

public class WandUtils{

    public static boolean is_tillable(BlockState state){
        return HoeItemAccessor.getTillables().get(state.getBlock())!=null;
    }
    public static boolean is_strippable(BlockState state){
        return AxeItemAccessor.getStrippables().get(state.getBlock())!=null;
    }
    public static boolean is_flattenable(BlockState state){
        return ShovelItemAccessor.getFlattenables().get(state.getBlock())!=null;
    }
    public static boolean has_use_action(BlockState state){
        return is_tillable(state) || is_strippable(state) || is_flattenable(state);
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
    static public int count_in_shulker(ItemStack shulker, Item item, Level level){
        int n=0;
        if(item!=null){
            List<ItemStack> contents = Compat.get_shulker_contents(shulker);
            for (ItemStack s : contents) {
                if( WandUtils.is_magicbag(s)) {
                    int total=MagicBagItem.getTotal(s);
                    ItemStack stack2=MagicBagItem.getItem(s,level);
                    if(!stack2.isEmpty()&& total >0 && stack2.getItem()==item){
                        n+=total;
                    }
                }else{
                    if(!s.isEmpty() && Compat.has_no_custom_data(s) && s.getItem()== item){
                        n+=s.getCount();
                    }
                }
            }
        }
        return n;
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
    public static float calc_xp(final int level,float prog) {
        float xp=calc_xp_level(level);
        if(prog>0){							
            xp=xp+ prog * (calc_xp_level(level+1)-xp);
        }
        return xp;
    }
    public static float calc_xp_level(final int level) {
        float xp_points;
        final int level2 = level * level;
        if(level>=32){
            xp_points=4.5f*level2 - 162.5f *level + 2220.0f;
        }else if(level>=17){
            xp_points=2.5f*level2 - 40.5f *level + 360.0f;
        }else {
            xp_points=level2 + 6*level;
        }
        return xp_points;
    }
    public static int calc_xp_to_next_level(int level){
        int xp;
        if(level>=32){
            xp = 9 * level - 158;	
        }else if(level>=17){
            xp = 5 * level - 38 ;	
        }else {
            xp = 2  *level + 7 ;	
        }
        return xp;
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
        #if MC_VERSION >= 12005
            if (offhand.has(DataComponents.POTION_CONTENTS)) {
                PotionContents potion_c = offhand.get(DataComponents.POTION_CONTENTS);
                if (potion_c != null) {
                    return potion_c.is(Potions.WATER);
                }
            }
            return false;
        #else
            return PotionUtils.getPotion(offhand) == Potions.WATER;
        #endif
    }
}
