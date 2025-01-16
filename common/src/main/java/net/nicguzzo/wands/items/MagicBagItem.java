package net.nicguzzo.wands.items;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.nicguzzo.wands.utils.Compat;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class MagicBagItem extends Item {
    public int tier;
    public int limit = Integer.MAX_VALUE;

    public MagicBagItem(int tier, int limit, Properties properties) {
        super(properties);
        this.tier = tier;
        if (limit > 0) {
            this.limit = limit;
        }
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand interactionHand) {
        ItemStack magic_bag = player.getItemInHand(interactionHand);
        if (!world.isClientSide()) {
            Compat.open_menu((ServerPlayer) player, magic_bag, 2);
        }
        return InteractionResult.PASS;
    }

    static public int getTotal(ItemStack bag) {
        if (bag != null && bag.getItem() instanceof MagicBagItem) {
            CompoundTag tag = Compat.getTags(bag);
            return tag.getInt("total");
        }
        return 0;
    }

    //returns false if it reached the limit;
    static public boolean inc(ItemStack bag, int n) {
        if (bag != null && bag.getItem() instanceof MagicBagItem) {
            int lim = ((MagicBagItem) bag.getItem()).limit;
            CompoundTag tag = Compat.getTags(bag);
            int total = tag.getInt("total");
            if (total + n < lim) {
                tag.putInt("total", total + n);
                CustomData.set(DataComponents.CUSTOM_DATA, bag, tag);
                return true;
            }
        }
        return false;
    }

    static public void dec(ItemStack bag, int n) {
        if (bag != null && bag.getItem() instanceof MagicBagItem) {
            CompoundTag tag = Compat.getTags(bag);
            int total = tag.getInt("total");
            if (total - n >= 0) {
                tag.putInt("total", total - n);
            } else {
                tag.putInt("total", 0);
            }
            CustomData.set(DataComponents.CUSTOM_DATA, bag, tag);
        }
    }

    static public void setItem(ItemStack bag, ItemStack item,HolderLookup.Provider ra) {
        if (bag != null && item != null && !item.isEmpty() && bag.getItem() instanceof MagicBagItem) {
            ItemStack item2 = item.copy();
            item2.setCount(1);
            CompoundTag tag = Compat.getTags(bag);
            if (ra != null) {
                tag.put("item", item2.save(ra, new CompoundTag()));
                CustomData.set(DataComponents.CUSTOM_DATA, bag, tag);
            }
        }
        if (item.isEmpty()) {
            CustomData.set(DataComponents.CUSTOM_DATA, bag, new CompoundTag());
        }
    }

    static public ItemStack getItem(ItemStack bag, HolderLookup.Provider ra) {
        if (bag != null && bag.getItem() instanceof MagicBagItem) {
            CompoundTag tag = Compat.getTags(bag);
            if (ra != null && tag.contains("item")) {
                Optional<ItemStack> item = ItemStack.parse(ra, tag.getCompound("item"));
                if (item.isPresent()) {
                    return item.get();
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        ItemStack i = MagicBagItem.getItem(stack,tooltipContext.registries());
        if (i.isEmpty()) {
            list.add(Compat.literal("item: none"));
        } else {
            list.add(Compat.literal("item: ").append(Component.translatable (i.getItem().getDescriptionId())));
        }
        list.add(Compat.literal("total: " + MagicBagItem.getTotal(stack)));
    }

    @Override
    public @NotNull Component getName(ItemStack itemStack) {
        if (Platform.getEnvironment() == Env.CLIENT) {
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof MagicBagItem) {
                ItemStack item = MagicBagItem.getItem(itemStack, Minecraft.getInstance().level.registryAccess());
                if (!item.isEmpty()) {
                    return Compat.literal("Bag of ").append(Component.translatable(item.getItem().getDescriptionId() )).append(" - Tier " + (tier + 1));
                }
            }
        }
        return super.getName(itemStack);
    }
}
