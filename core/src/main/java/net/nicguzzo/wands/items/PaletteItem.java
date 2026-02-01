package net.nicguzzo.wands.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
#if MC_VERSION<12111
import net.minecraft.world.InteractionResultHolder;
#else
import net.minecraft.world.item.component.TooltipDisplay;
#endif
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
#if MC_VERSION >= 12005
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
#endif
import net.minecraft.world.level.Level;
import net.nicguzzo.compat.Compat;
import net.nicguzzo.wands.utils.WandUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

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

    @Environment(EnvType.CLIENT)
    #if MC_VERSION >= 12111
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        Level level=Minecraft.getInstance().level;
    #else
#if MC_VERSION >= 12101
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        Level level=Minecraft.getInstance().level;
#else
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag){
#endif
#endif
        CompoundTag tag = Compat.getTags(stack);
        Optional<ListTag> inventory = Compat.getList(tag,"Palette");//10 COMPOUND
        if(inventory.isPresent()) {
            int s = inventory.get().size();
            for (int i = 0; i < s; i++) {
                ItemStack stack2 = ItemStack.EMPTY;
                Optional<CompoundTag> block_ct=Compat.getCompound(tag,"Block");
                if (level != null && block_ct.isPresent()) {
                    //Optional<ItemStack> is2 = ItemStack.parse(level.registryAccess(), block_ct.get());
                    Optional<ItemStack> is2 = Compat.ItemStack_read(block_ct.get(),level);
                    if (is2.isPresent()) {
                        stack2 = is2.get();
                    }
                }
                if (!stack2.isEmpty()) {
                    Component c=Component.translatable(stack2.getItem().getDescriptionId()).withStyle(ChatFormatting.GREEN);
                    #if MC_VERSION >= 12111
                    consumer.accept(c);
                    #else
                    list.add(c);
                    #endif
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
        if(mode_val!=null) {
            #if MC_VERSION >= 12111
            consumer.accept(mode_val);
            #else
            list.add(mode_val);
            #endif
        }
        Component c=Compat.literal("rotate: " + (Compat.getBoolean(tag,"rotate").orElse(false) ? "on" : "off"));
        #if MC_VERSION >= 12111
        consumer.accept(c);
        #else
        list.add(c);
        #endif
    }

    static public PaletteMode getMode(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = Compat.getTags(stack);
            int mode = Compat.getInt(tag,"mode").orElse(0);
            if (mode < PaletteMode.values().length)
                return PaletteMode.values()[mode];
        }
        return PaletteMode.RANDOM;
    }

    static public boolean getRotate(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = Compat.getTags(stack);
            return Compat.getBoolean(tag,"rotate").orElse(false);
        }
        return false;
    }
    static public int getGradientHeight(ItemStack stack) {
        int v=0;
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = Compat.getTags(stack);
            v=Compat.getInt(tag,"gradient_height").orElse(6);
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
            #if MC_VERSION >= 12005
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
            #endif
        }
    }

    static public void toggleRotate(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = Compat.getTags(stack);
            boolean rotate = Compat.getBoolean(tag,"rotate").orElse(false);
            tag.putBoolean("rotate", !rotate);
            #if MC_VERSION >= 12005
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
            #endif
        }
    }

    static public void nextMode(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = Compat.getTags(stack);
            int mode = (Compat.getInt(tag,"mode").orElse(0) + 1) % (PaletteMode.values().length);
            tag.putInt("mode", mode);
            #if MC_VERSION >= 12005
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
            #endif
        }
    }

    // Used by CycleToggle in PaletteScreen (needs direct set for bidirectional cycling and filtering)
    static public void setMode(ItemStack stack, PaletteMode mode) {
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = Compat.getTags(stack);
            tag.putInt("mode", mode.ordinal());
            #if MC_VERSION >= 12005
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
            #endif
        }
    }

    @Override
#if MC_VERSION>=12111
    public InteractionResult use(Level world, Player player, InteractionHand interactionHand) {
#else
    public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {
#endif
        ItemStack paletteItemStack = player.getItemInHand(interactionHand);
        ItemStack wand = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean is_wand=WandUtils.is_wand(wand);
        //boolean has_target=false;
        if(is_wand){

            #if MC_VERSION>=12111
               return InteractionResult.FAIL;
            #else
               return InteractionResultHolder.fail(player.getItemInHand(interactionHand));
            #endif
        }
        if (!world.isClientSide()) {
            Compat.open_menu((ServerPlayer) player, paletteItemStack, 1);
            #if MC_VERSION>=12111
               return InteractionResult.SUCCESS;
            #else
                return InteractionResultHolder.success(player.getItemInHand(interactionHand));
            #endif

        }
        #if MC_VERSION>=12111
            return InteractionResult.FAIL;
        #else
            return InteractionResultHolder.fail(player.getItemInHand(interactionHand));
        #endif

    }

    public static SimpleContainer getInventory(ItemStack stack,Level level) {
        SimpleContainer inventory = new SimpleContainer(27*2);
        //Level level = Minecraft.getInstance().level;
        if (level == null) return inventory;
        CompoundTag tag = Compat.getTags(stack);
        Optional<ListTag> inventory_tag = Compat.getList(tag,"Palette");
        if(inventory_tag.isPresent()) {
            for (int i = 0; i < inventory_tag.get().size(); i++) {
                CompoundTag slot_tag = (CompoundTag) inventory_tag.get().get(i);
                if (slot_tag.contains("Slot") && slot_tag.contains("Block") && Compat.getInt(slot_tag,"Slot").isPresent()) {
                    int slot = Compat.getInt(slot_tag,"Slot").get();
                    Tag item_tag = slot_tag.get("Block");
                    if(item_tag==null){
                        continue;
                    }
                    #if MC_VERSION>=12111
                    CompoundTag ctag=item_tag.asCompound().get();
                    #else
                    CompoundTag ctag=(CompoundTag)item_tag;
                    #endif
                    Optional<ItemStack> is= Compat.ItemStack_read(ctag, level);

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
            ListTag inventory_tag = Compat.getList(tag,"Palette").orElse(new ListTag());
            inventory_tag.clear();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                if (!inventory.getItem(i).isEmpty()) {
                    Tag item_tag = Compat.ItemStack_save(inventory.getItem(i), level);
                    CompoundTag slot_tag = new CompoundTag();
                    slot_tag.putInt("Slot", i);
                    slot_tag.put("Block", item_tag);
                    inventory_tag.add(slot_tag);
                }
            }
            tag.put("Palette", inventory_tag);
            #if MC_VERSION >= 12005
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
            #endif

        }
    }
}
