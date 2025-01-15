package net.nicguzzo.wands.wand;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.items.PaletteItem;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.utils.Compat;

import java.util.Map;
import java.util.Optional;
import java.util.Vector;

import net.nicguzzo.wands.items.PaletteItem.PaletteMode;

import net.minecraft.util.RandomSource;

public class Palette {
    public ItemStack item = null;
    public boolean has_palette = false;
    public int slot = 0;
    public RandomSource random = RandomSource.create();
    public volatile long seed = System.currentTimeMillis();
    public Vector<PaletteSlot> palette_slots = new Vector<>();
    public static class PaletteSlot {
        public ItemStack stack;
        public BlockState state;
        public int slot;

        PaletteSlot(int s, BlockState b, ItemStack stk) {
            slot = s;
            state = b;
            stack = stk;
        }
    }
    void update_palette(Map<Item, BlockAccounting> block_accounting, Level level){
        //if(mode!= Mode.DIRECTION)
            slot=0;
        if(item!=null && item.getItem() instanceof PaletteItem) {
            palette_slots.clear();
            CompoundTag tag= Compat.getTags(item);
            ListTag palette_inv = tag.getList("Palette", Compat.NbtType.COMPOUND);
            //log("palette_inv: "+palette_inv);
            int s = palette_inv.size();
            for (int i = 0; i < s; i++) {
                CompoundTag stackTag = (CompoundTag) palette_inv.get(i);
                ItemStack stack =ItemStack.EMPTY;
                if(level !=null) {
                    stack= ItemStack.parse(level.registryAccess(), stackTag.getCompound("Block")).orElse(ItemStack.EMPTY);
                }
                if (!stack.isEmpty()) {
                    Block blk = Block.byItem(stack.getItem());
                    if (blk != Blocks.AIR) {
                        Palette.PaletteSlot psl = new Palette.PaletteSlot(i, blk.defaultBlockState(), stack);

                        //if (palette_slots.stream().noneMatch(pp -> (Compat.is_same(pp.stack,stack)))) {
                        if(block_accounting.get(stack.getItem())==null) {
                            block_accounting.put(stack.getItem(), new BlockAccounting());
                        }
                        //}
                        palette_slots.add(psl);
                    }
                }
            }
        }
    }
    public BlockState get_state(Wand wand){
        BlockState st=wand.block_state;
        if(!wand.preview){
            //WandsMod.log("get_state bp",true);
        }
        if (palette_slots.size() > 0) {
            PaletteMode palette_mode = PaletteItem.getMode(item);
            int bound = palette_slots.size();
            if (palette_mode == PaletteMode.RANDOM) {
                slot = random.nextInt(bound);
            }
            Palette.PaletteSlot ps = palette_slots.get(slot);
            if (palette_mode == PaletteMode.ROUND_ROBIN ) {
                if(!(wand.mode== WandProps.Mode.DIRECTION && wand.level.isClientSide()))
                    slot = (slot + 1) % bound;
            }
            st = ps.state;
            Block blk = st.getBlock();
            if (palette_mode == PaletteItem.PaletteMode.RANDOM) {
                if (blk instanceof SnowLayerBlock) {
                    int sn = random.nextInt(7);
                    st = st.setValue(SnowLayerBlock.LAYERS, sn + 1);
                }
            }
            st = wand.state_for_placement(st,null);
            //if (palette_mode == PaletteItem.PaletteMode.RANDOM)
            {
                if (PaletteItem.getRotate(item)) {
                    //TODO: fix rotation
                    st = ps.state.getBlock().defaultBlockState().rotate(Rotation.getRandom(random));
                }
            }
        }
        return st;
    }
    public boolean state_in_slot(BlockState bs){
        boolean cond=false;
        if(has_palette) {
            for (Palette.PaletteSlot slot: palette_slots) {
                if(bs.equals(slot.state)){
                    cond=true;
                    break;
                }
            }
        }
        return cond;
    }
}
