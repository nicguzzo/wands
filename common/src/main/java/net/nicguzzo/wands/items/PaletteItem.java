package net.nicguzzo.wands.items;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.utils.WandUtils;
import java.util.Optional;

public class PaletteItem extends Item {
    public enum PaletteMode {
        RANDOM, ROUND_ROBIN, GRADIENT
    }

    static public Component mode_val_random = Compat.translatable("item.wands.random");
    static public Component mode_val_gradient = Compat.translatable("item.wands.gradient");
    static public Component mode_val_rr = Compat.translatable("item.wands.round_robin");

    public PaletteItem(Properties properties) {
        super(properties);
    }
/*
    @Environment(EnvType.CLIENT)
    //@Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        CompoundTag tag = Compat.getTags(stack);
        Optional<ListTag> inventory = tag.getList("Palette");//10 COMPOUND
        if(inventory.isPresent()) {
            int s = inventory.get().size();
            for (int i = 0; i < s; i++) {
                ItemStack stack2 = ItemStack.EMPTY;
                Level level = Minecraft.getInstance().level;
                Optional<CompoundTag> block_ct=tag.getCompound("Block");
                if (level != null && block_ct.isPresent()) {
                    //Optional<ItemStack> is2 = ItemStack.parse(level.registryAccess(), block_ct.get());
                    Optional<ItemStack> is2 = WandUtils.ItemStack_read(block_ct.get(),level.registryAccess());
                    if (is2.isPresent()) {
                        stack2 = is2.get();
                    }
                }
                if (!stack2.isEmpty()) {
                    list.add(Component.translatable(stack2.getItem().getDescriptionId()).withStyle(ChatFormatting.GREEN));
                }
            }
        }
        PaletteMode mode = PaletteItem.getMode(stack);
        Component mode_val = null;
        switch (mode ) {
            case RANDOM:
                mode_val = Compat.literal("mode: " + PaletteItem.mode_val_random.getString());
            break;
            case ROUND_ROBIN:
                mode_val = Compat.literal("mode: " + PaletteItem.mode_val_rr.getString());
            break;
            case GRADIENT:
                mode_val = Compat.literal("mode: " + PaletteItem.mode_val_gradient.getString());
            break;
        }
        if(mode_val!=null)
            list.add(mode_val);
        list.add(Compat.literal("rotate: " + (tag.getBoolean("rotate").orElse(false) ? "on" : "off")));
    }
*/
    static public PaletteMode getMode(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = Compat.getTags(stack);
            int mode = tag.getInt("mode").orElse(0);
            if (mode < PaletteMode.values().length)
                return PaletteMode.values()[mode];
        }
        return PaletteMode.RANDOM;
    }

    static public boolean getRotate(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = Compat.getTags(stack);
            return tag.getBoolean("rotate").orElse(false);
        }
        return false;
    }
    static public int getGradientHeight(ItemStack stack) {
        int v=0;
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = Compat.getTags(stack);
            v=tag.getInt("gradient_height").orElse(6);
        }
        if(v==0) {
            return 6;
        }else{
            return v;
        }
    }
    static public void setGradientHeight(ItemStack stack,int height) {
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = Compat.getTags(stack);
            if(height>0) {
                tag.putInt("gradient_height", height);
            }else{
                tag.putInt("gradient_height", 1);
            }
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    static public void toggleRotate(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = Compat.getTags(stack);
            boolean rotate = tag.getBoolean("rotate").orElse(false);
            tag.putBoolean("rotate", !rotate);
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    static public void nextMode(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = Compat.getTags(stack);
            int mode = (tag.getInt("mode").orElse(0) + 1) % (PaletteMode.values().length);
            tag.putInt("mode", mode);
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand interactionHand) {
        ItemStack paletteItemStack = player.getItemInHand(interactionHand);
        ItemStack wand = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean is_wand=WandUtils.is_wand(wand);
        //boolean has_target=false;
        if(is_wand){
        //    Wand w= PlayerWand.get(player);
        //    w.
            return InteractionResult.FAIL;
        }
        if (!world.isClientSide()) {
            Compat.open_menu((ServerPlayer) player, paletteItemStack, 1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    public static SimpleContainer getInventory(ItemStack stack,Level level) {
        SimpleContainer inventory = new SimpleContainer(27*2);
        //Level level = Minecraft.getInstance().level;
        if (level == null) return inventory;
        CompoundTag tag = Compat.getTags(stack);
        Optional<ListTag> inventory_tag = tag.getList("Palette");
        if(inventory_tag.isPresent()) {
            HolderLookup.Provider provider=level.registryAccess();
            for (int i = 0; i < inventory_tag.get().size(); i++) {
                CompoundTag slot_tag = (CompoundTag) inventory_tag.get().get(i);
                if (slot_tag.contains("Slot") && slot_tag.contains("Block") && slot_tag.getInt("Slot").isPresent()) {
                    int slot = slot_tag.getInt("Slot").get();
                    Tag item_tag = slot_tag.get("Block");
                    if(item_tag==null){
                        continue;
                    }
                    //Optional<ItemStack> is = ItemStack.parse(level.registryAccess(), item_tag);
                    Optional<ItemStack> is= WandUtils.ItemStack_read(item_tag.asCompound().get(), provider);

                    if (is.isPresent()) {
                        inventory.setItem(slot, is.get());
                    }
                }
            }
        }
        return inventory;
    }

    public static void setInventory(ItemStack stack, SimpleContainer inventory,Level level) {
        if (level != null) {
            CompoundTag tag = Compat.getTags(stack);
            ListTag inventory_tag = tag.getList("Palette").orElse(new ListTag());
            inventory_tag.clear();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                if (!inventory.getItem(i).isEmpty()) {
                    //Tag item_tag = inventory.getItem(i).save(level.registryAccess());
                    Tag item_tag =WandUtils.ItemStack_save(inventory.getItem(i), level.registryAccess());
                    CompoundTag slot_tag = new CompoundTag();
                    slot_tag.putInt("Slot", i);
                    slot_tag.put("Block", item_tag);
                    inventory_tag.add(slot_tag);
                }
            }
            tag.put("Palette", inventory_tag);
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);

        }
    }
}
