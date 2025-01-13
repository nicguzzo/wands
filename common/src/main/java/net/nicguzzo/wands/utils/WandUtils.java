package net.nicguzzo.wands.utils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
#if MC>="1205"
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.core.registries.BuiltInRegistries;
#endif
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
#if MC<="1165"
import net.minecraft.world.level.material.Material;
#endif
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.mixin.AxeItemAccessor;
import net.nicguzzo.wands.mixin.HoeItemAccessor;
import net.nicguzzo.wands.mixin.ShovelItemAccessor;


import java.util.Set;

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
    static public boolean is_wand(ItemStack stack) {
        return stack!=null && !stack.isEmpty() && stack.getItem() instanceof WandItem;
    }
    static public boolean is_shulker(ItemStack item_stack){        
		return Block.byItem(item_stack.getItem()) instanceof ShulkerBoxBlock ;
    }
    static public boolean is_magicbag(ItemStack item_stack){
        return item_stack.getItem() instanceof MagicBagItem;
    }
    static public int count_in_shulker(ItemStack shulker, Item item){
        int n=0;
        if(item!=null){
            #if MC < "1205"
            CompoundTag entity_tag =shulker.getTagElement("BlockEntityTag");
            if(entity_tag!=null){
                ListTag shulker_items = entity_tag.getList("Items", Compat.NbtType.COMPOUND);
                for (int i = 0, len = shulker_items.size(); i < len; ++i) {
                    CompoundTag itemTag = shulker_items.getCompound(i);
                    ItemStack s = ItemStack.of(itemTag);
                    if( WandUtils.is_magicbag(s)) {
                        int total=MagicBagItem.getTotal(s);
                        ItemStack stack2=MagicBagItem.getItem(s);
                        if(!stack2.isEmpty()&& total >0 && stack2.getItem()==item){
                            n+=total;
                        }
                    }else{
                        if(!s.isEmpty() && s.getTag()==null && s.getItem()== item){
                            n+=s.getCount();
                        }
                    }
                }
            }
            #else
            //TODO: count_in_shulker  mc >= 1.20.5
            //Iterator var7 = ((ItemContainerContents)shulker.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)).nonEmptyItems().iterator();
            //ItemContainerContents contents=shulker.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY);
            #endif
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

    #if MC<="1165"
    private static final Set<Block>    PICKAXE_DIGGABLES= ImmutableSet.of(Blocks.ACTIVATOR_RAIL, Blocks.COAL_ORE, Blocks.COBBLESTONE, Blocks.DETECTOR_RAIL, Blocks.DIAMOND_BLOCK, Blocks.DIAMOND_ORE, new Block[]{Blocks.POWERED_RAIL, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE, Blocks.NETHER_GOLD_ORE, Blocks.ICE, Blocks.IRON_BLOCK, Blocks.IRON_ORE, Blocks.LAPIS_BLOCK, Blocks.LAPIS_ORE, Blocks.MOSSY_COBBLESTONE, Blocks.NETHERRACK, Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.RAIL, Blocks.REDSTONE_ORE, Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE, Blocks.RED_SANDSTONE, Blocks.STONE, Blocks.GRANITE, Blocks.POLISHED_GRANITE, Blocks.DIORITE, Blocks.POLISHED_DIORITE, Blocks.ANDESITE, Blocks.POLISHED_ANDESITE, Blocks.STONE_SLAB, Blocks.SMOOTH_STONE_SLAB, Blocks.SANDSTONE_SLAB, Blocks.PETRIFIED_OAK_SLAB, Blocks.COBBLESTONE_SLAB, Blocks.BRICK_SLAB, Blocks.STONE_BRICK_SLAB, Blocks.NETHER_BRICK_SLAB, Blocks.QUARTZ_SLAB, Blocks.RED_SANDSTONE_SLAB, Blocks.PURPUR_SLAB, Blocks.SMOOTH_QUARTZ, Blocks.SMOOTH_RED_SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.SMOOTH_STONE, Blocks.STONE_BUTTON, Blocks.STONE_PRESSURE_PLATE, Blocks.POLISHED_GRANITE_SLAB, Blocks.SMOOTH_RED_SANDSTONE_SLAB, Blocks.MOSSY_STONE_BRICK_SLAB, Blocks.POLISHED_DIORITE_SLAB, Blocks.MOSSY_COBBLESTONE_SLAB, Blocks.END_STONE_BRICK_SLAB, Blocks.SMOOTH_SANDSTONE_SLAB, Blocks.SMOOTH_QUARTZ_SLAB, Blocks.GRANITE_SLAB, Blocks.ANDESITE_SLAB, Blocks.RED_NETHER_BRICK_SLAB, Blocks.POLISHED_ANDESITE_SLAB, Blocks.DIORITE_SLAB, Blocks.SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.PISTON, Blocks.STICKY_PISTON, Blocks.PISTON_HEAD});
    private static final Set<Material> AXE_DIGGABLE_MATERIALS = Sets.newHashSet(new Material[]{Material.WOOD,Material.NETHER_WOOD,Material.PLANT,Material.REPLACEABLE_PLANT,Material.BAMBOO,Material.VEGETABLE});
    private static final Set<Block>    AXE_DIGGABLE_BLOCKS = Sets.newHashSet(new Block[]{Blocks.LADDER, Blocks.SCAFFOLDING, Blocks.OAK_BUTTON, Blocks.SPRUCE_BUTTON, Blocks.BIRCH_BUTTON, Blocks.JUNGLE_BUTTON, Blocks.DARK_OAK_BUTTON, Blocks.ACACIA_BUTTON, Blocks.CRIMSON_BUTTON, Blocks.WARPED_BUTTON});
    private static final Set<Block>    SHOVEL_DIGGABLES = Sets.newHashSet(new Block[]{Blocks.CLAY, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.FARMLAND, Blocks.GRASS_BLOCK, Blocks.GRAVEL, Blocks.MYCELIUM, Blocks.SAND, Blocks.RED_SAND, Blocks.SNOW_BLOCK, Blocks.SNOW, Blocks.SOUL_SAND, Blocks.GRASS_PATH, Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER, Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER, Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER, Blocks.BLACK_CONCRETE_POWDER, Blocks.SOUL_SOIL});
    private static final Set<Block>    HOE_DIGGABLES = ImmutableSet.of(Blocks.NETHER_WART_BLOCK, Blocks.WARPED_WART_BLOCK, Blocks.HAY_BLOCK, Blocks.DRIED_KELP_BLOCK, Blocks.TARGET, Blocks.SHROOMLIGHT, new Block[]{Blocks.SPONGE, Blocks.WET_SPONGE, Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.ACACIA_LEAVES, Blocks.BIRCH_LEAVES});
#endif

#if MC == "1165"
    public static boolean pickaxe_minable(BlockState state){
            return PICKAXE_DIGGABLES.contains(state.getBlock());
    }
    public static boolean axe_minable(BlockState state){
        return AXE_DIGGABLE_BLOCKS.contains(state.getBlock())||AXE_DIGGABLE_MATERIALS.contains(state.getMaterial());
    }
    public static boolean hoe_minable(BlockState state){
        return HOE_DIGGABLES.contains(state.getBlock());
    }
    public static boolean shovel_minable(BlockState state) {
        return SHOVEL_DIGGABLES.contains(state.getBlock());
    }
#else
#if MC == "1171"
    public static boolean pickaxe_minable(BlockState state){
            return BlockTags.MINEABLE_WITH_PICKAXE.contains(state.getBlock());
    }
#else
    public static boolean pickaxe_minable(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE);
    }
#endif
#endif
}
