package net.nicguzzo.wands.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.utils.WandUtils;

import java.util.List;
import java.util.Optional;

public class PaletteItem extends Item {
    public enum PaletteMode {
        RANDOM, ROUND_ROBIN
    }

    static public Component mode_val_random = Compat.translatable("item.wands.random");
    static public Component mode_val_rr = Compat.translatable("item.wands.round_robin");

    public PaletteItem(Properties properties) {
        super(properties);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        CompoundTag tag = Compat.getTags(stack);
        ListTag inventory = tag.getList("Palette", Compat.NbtType.COMPOUND);//10 COMPOUND
        int s = inventory.size();
        for (int i = 0; i < s; i++) {
            CompoundTag stackTag = (CompoundTag) inventory.get(i);

            ItemStack stack2 = ItemStack.EMPTY;
            Level level = Minecraft.getInstance().level;
            if (level != null && tag.contains("Block")) {
                Optional<ItemStack> is2 = ItemStack.parse(level.registryAccess(), tag.getCompound("Block"));
                if (is2.isPresent()) {
                    stack2 = is2.get();
                }
            }
            if (!stack2.isEmpty()) {
                list.add(Component.translatable(stack2.getItem().getDescriptionId() ).withStyle(ChatFormatting.GREEN));
            }
        }
        PaletteMode mode = PaletteItem.getMode(stack);
        Component mode_val;
        if (mode == PaletteMode.ROUND_ROBIN) {
            mode_val = Compat.literal("mode: " + PaletteItem.mode_val_rr.getString());
        } else {
            mode_val = Compat.literal("mode: " + PaletteItem.mode_val_random.getString());
        }
        list.add(mode_val);
        list.add(Compat.literal("rotate: " + (tag.getBoolean("rotate") ? "on" : "off")));
    }

    static public PaletteMode getMode(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = Compat.getTags(stack);
            int mode = tag.getInt("mode");
            if (mode < PaletteMode.values().length)
                return PaletteMode.values()[mode];
        }
        return PaletteMode.RANDOM;
    }

    static public boolean getRotate(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = Compat.getTags(stack);
            return tag.getBoolean("rotate");
        }
        return false;
    }

    static public void toggleRotate(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = Compat.getTags(stack);
            boolean rotate = tag.getBoolean("rotate");
            tag.putBoolean("rotate", !rotate);
        }
    }

    static public void nextMode(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = Compat.getTags(stack);
            int mode = (tag.getInt("mode") + 1) % (2);
            tag.putInt("mode", mode);
        }
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand interactionHand) {
        ItemStack paletteItemStack = player.getItemInHand(interactionHand);
        //ItemStack wand = player.getItemInHand(InteractionHand.MAIN_HAND);
        //boolean is_wand=WandUtils.is_wand(wand);
        if (!world.isClientSide() && interactionHand==InteractionHand.MAIN_HAND) {
            Compat.open_menu((ServerPlayer) player, paletteItemStack, 1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    public static SimpleContainer getInventory(ItemStack stack,Level level) {
        SimpleContainer inventory = new SimpleContainer(27); // Default size
        //Level level = Minecraft.getInstance().level;
        if (level == null) return inventory;

        CompoundTag tag = Compat.getTags(stack);
        ListTag inventory_tag = tag.getList("Palette", Compat.NbtType.COMPOUND);
        //inventory.fromTag(inventory_tag,level.registryAccess());
        for (int i = 0; i < inventory_tag.size(); i++) {

            CompoundTag slot_tag = (CompoundTag) inventory_tag.get(i);
            if (slot_tag.contains("Slot") && slot_tag.contains("Block")) {
                int slot = slot_tag.getInt("Slot");
                Tag item_tag = slot_tag.get("Block");
                Optional<ItemStack> is = ItemStack.parse(level.registryAccess(), item_tag);
                if (is.isPresent()) {
                    inventory.setItem(slot, is.get());
                }
            }

        }
        return inventory;
    }

    public static void setInventory(ItemStack stack, SimpleContainer inventory,Level level) {

        if (level != null) {
            CompoundTag tag = Compat.getTags(stack);

            ListTag inventory_tag = tag.getList("Palette", Compat.NbtType.COMPOUND);
            inventory_tag.clear();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                if (!inventory.getItem(i).isEmpty()) {
                    Tag item_tag = inventory.getItem(i).save(level.registryAccess());
                    CompoundTag slot_tag = new CompoundTag();
                    slot_tag.putInt("Slot", i);
                    slot_tag.put("Block", item_tag);
                    inventory_tag.add(slot_tag);
                }
//                //tag.put("Palette", item2.save(level.registryAccess(), new CompoundTag()));
            }
            tag.put("Palette", inventory_tag);
            //CompoundTag tag= Compat.getTags(stack);
            //tag.put("Palette",inventory.createTag(level.registryAccess()));
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }
}
