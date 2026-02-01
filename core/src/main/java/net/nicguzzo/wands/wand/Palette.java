package net.nicguzzo.wands.wand;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.core.HolderLookup;
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
import net.nicguzzo.compat.Compat;

import java.util.Map;
import java.util.Optional;
import java.util.Vector;

import net.nicguzzo.wands.items.PaletteItem.PaletteMode;

import net.minecraft.util.RandomSource;
import net.nicguzzo.wands.utils.WandUtils;

public class Palette {
    static public long version=0;
    static public long last_version=-1;
    public ItemStack item = null;
    public boolean has_palette = false;
    public int slot = 0;
    public RandomSource random = RandomSource.create();
    public volatile long seed = System.currentTimeMillis();
    public Vector<PaletteSlot> palette_slots = new Vector<>();
    public Vector<Vector<PaletteSlot>> palette_grid= new Vector<>();
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

    public Palette(){
        for(int i=0;i<6;i++){
            palette_grid.add(new Vector<>());
        }
    }

    void update_palette(Map<Item, BlockAccounting> block_accounting, Level level){
        if (Platform.getEnvironment() == Env.CLIENT) {
            if (version == last_version)
                return;
            version = last_version;
        }

        //WandsMod.log("update_palette",true);
        //if(mode!= Mode.DIRECTION)
        slot=0;
        if(item!=null && item.getItem() instanceof PaletteItem) {
            PaletteMode palette_mode = PaletteItem.getMode(item);
            palette_slots.clear();
            for(int i=0;i<6;i++){
                palette_grid.get(i).clear();
            }
            //palette_grid.clear();
            CompoundTag tag= Compat.getTags(item);
            Optional<ListTag> o_palette_inv = Compat.getList(tag, "Palette");
            if (o_palette_inv.isEmpty())return;
            ListTag palette_inv=o_palette_inv.get();
            //WandsMod.log("palette_inv: "+palette_inv,true);
            int s = palette_inv.size();

            for (int i = 0; i < s; i++) {
                CompoundTag stackTag = (CompoundTag) palette_inv.get(i);
                ItemStack stack =ItemStack.EMPTY;
                if(level !=null) {
                    Optional<CompoundTag> block_tg=Compat.getCompound(stackTag, "Block");
                    if(block_tg.isPresent()) {
                        stack = Compat.ItemStack_read(block_tg.get(),level).orElse(ItemStack.EMPTY);
                    }
                }
                int inv_slot=Compat.getInt(stackTag, "Slot").orElse(0);
                if (!stack.isEmpty()) {
                    Block blk = Block.byItem(stack.getItem());
                    if (blk != Blocks.AIR) {
                        Palette.PaletteSlot psl = new Palette.PaletteSlot(i, blk.defaultBlockState(), stack);

                        if(block_accounting.get(stack.getItem())==null) {
                            block_accounting.put(stack.getItem(), new BlockAccounting());
                        }
                        palette_slots.add(psl);
                        switch(palette_mode) {
                            //case ROUND_ROBIN:
                            //case RANDOM:
                            //break;
                            case GRADIENT:
                                int j=inv_slot/9;
                                if(j<palette_grid.size()) {
                                    palette_grid.get(j).add(psl);
                                }
                            break;
                        }
                    }
                }
            }
            //for (int i = 0; i < palette_grid.size(); i++) {
            //    StringBuilder row= new StringBuilder();
            //    for (int j = 0; j < palette_grid.get(i).size(); j++) {
            //        row.append(" ").append(palette_grid.get(i).get(j).state.toString());
            //    }
            //   WandsMod.log("row "+i + " "+ row, true);
            //}
        }

    }

    public BlockState get_state(Wand wand,int min,int max,int y){
        BlockState st=wand.block_state;
        if(!wand.preview){
            //WandsMod.log("get_state bp",true);
        }
        PaletteMode palette_mode = PaletteItem.getMode(item);
        int  gradient_height = PaletteItem.getGradientHeight(item);

        switch(palette_mode) {
            case ROUND_ROBIN: {
                if (!palette_slots.isEmpty()) {
                    int bound = palette_slots.size();
                    Palette.PaletteSlot ps = palette_slots.get(slot);
                    st = ps.state;
                    // Only advance slot during actual placement, not preview
                    if (!wand.preview) {
                        slot = (slot + 1) % bound;
                    }
                }
            }break;
            case RANDOM: {
                if (!palette_slots.isEmpty()) {
                    int bound = palette_slots.size();
                    slot = random.nextInt(bound);
                    Palette.PaletteSlot ps = palette_slots.get(slot);
                    st = ps.state;
                    Block blk = st.getBlock();
                    if (blk instanceof SnowLayerBlock) {
                        int sn = random.nextInt(7);
                        st = st.setValue(SnowLayerBlock.LAYERS, sn + 1);
                    }
                }
            }break;
            case GRADIENT: {
                if (!palette_grid.isEmpty()) {
                    // Use actual min/max Y of the build, not clicked position
                    // Map Y position to palette row (0=top row, 5=bottom row)
                    // min Y -> row 5 (bottom), max Y -> row 0 (top)
                    int mapped_y;
                    if (max == min) {
                        // All blocks at same Y level - use gradient_height from click position
                        int bottom_y = wand.pos.getY();
                        mapped_y = WandUtils.mapRange(bottom_y, bottom_y + gradient_height - 1, 5, 0, y);
                    } else {
                        // Use actual Y range of the build
                        mapped_y = WandUtils.mapRange(min, max, 5, 0, y);
                    }
                    if (mapped_y < 0) mapped_y = 0;
                    if (mapped_y > 5) mapped_y = 5;

                    if (mapped_y < palette_grid.size()) {
                         Vector<PaletteSlot> row = palette_grid.get(mapped_y);
                         int bound = row.size();
                         if (bound > 0) {
                             slot = random.nextInt(bound);
                             Palette.PaletteSlot ps = row.get(slot);
                             st = ps.state;
                         }
                    }
                }
            }break;
        }
        st = wand.state_for_placement(st,null);

        if (st != null && PaletteItem.getRotate(item)) {
            //TODO: fix rotation
            //st = ps.state.getBlock().defaultBlockState().rotate(Rotation.getRandom(random));
            st = st.getBlock().defaultBlockState().rotate(Rotation.getRandom(random));
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
