package net.nicguzzo.wands;


//import dev.architectury.utils.NbtType;
import me.shedaniel.architectury.utils.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

class WandUtils{

    static public boolean is_shulker(ItemStack item_stack){        
		return Block.byItem(item_stack.getItem()) instanceof ShulkerBoxBlock ;
    }
    static public int count_in_shulker(ItemStack shulker, Item item){
        int n=0;
        if(item!=null){
            CompoundTag entity_tag =shulker.getTagElement("BlockEntityTag");
            if(entity_tag!=null){
                ListTag shulker_items = entity_tag.getList("Items", NbtType.COMPOUND);
                if(shulker_items!=null){
                    for (int i = 0, len = shulker_items.size(); i < len; ++i) {
                        CompoundTag itemTag = shulker_items.getCompound(i);                        
                        ItemStack s = ItemStack.of(itemTag);
                        if(!s.isEmpty() && s.getTag()==null && s.getItem()== item){
                            n+=s.getCount();
                        }							
                    }
                }
            }
        }
        return n;
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
}