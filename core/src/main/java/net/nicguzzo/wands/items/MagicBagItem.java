package net.nicguzzo.wands.items;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
#if MC_VERSION >= 12005
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.component.CustomData;
#endif
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
#if MC_VERSION < 12111
import net.minecraft.world.InteractionResultHolder;
#endif
#if MC_VERSION >= 12111
import net.minecraft.world.item.component.TooltipDisplay;
#endif
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.nicguzzo.compat.Compat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class MagicBagItem extends Item {
    public enum MagicBagItemTier{
        MAGIC_BAG_TIER_1,
        MAGIC_BAG_TIER_2,
        MAGIC_BAG_TIER_3,
    }
    public MagicBagItemTier tier;
    public int limit = Integer.MAX_VALUE;


    public MagicBagItem(MagicBagItemTier tier, int limit, Properties properties) {
        super(properties);
        this.tier = tier;
        if (limit > 0) {
            this.limit = limit;
        }
    }

    @Override
#if MC_VERSION>=12111
    public InteractionResult use(Level world, Player player, InteractionHand interactionHand) {
#else
    public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {
#endif
        ItemStack magic_bag = player.getItemInHand(interactionHand);
        if (!world.isClientSide()) {
            Compat.open_menu((ServerPlayer) player, magic_bag, 2);
        }
#if MC_VERSION>=12111
        return InteractionResult.PASS;
#else
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
#endif
    }



    static public int getTotal(ItemStack bag) {
        if (bag != null && bag.getItem() instanceof MagicBagItem) {
            CompoundTag tag = Compat.getTags(bag);
            return Compat.getInt(tag,"total").orElse(0);
        }
        return 0;
    }

    //returns false if it reached the limit;
    static public boolean inc(ItemStack bag, int n) {
        if (bag != null && bag.getItem() instanceof MagicBagItem) {
            int lim = ((MagicBagItem) bag.getItem()).limit;
            CompoundTag tag = Compat.getTags(bag);
            int total = Compat.getInt(tag,"total").orElse(0);
            if (total + n < lim) {
                tag.putInt("total", total + n);
                #if MC_VERSION >= 12005
                CustomData.set(DataComponents.CUSTOM_DATA, bag, tag);
                #endif
                return true;
            }
        }
        return false;
    }

    static public void dec(ItemStack bag, int n) {
        if (bag != null && bag.getItem() instanceof MagicBagItem) {
            CompoundTag tag = Compat.getTags(bag);
            int total = Compat.getInt(tag,"total").orElse(0);
            if (total - n >= 0) {
                tag.putInt("total", total - n);
            } else {
                tag.putInt("total", 0);
            }
            #if MC_VERSION >= 12005
            CustomData.set(DataComponents.CUSTOM_DATA, bag, tag);
            #endif
        }
    }

    static public void setItem(ItemStack bag, ItemStack item, Level level) {
        if (bag != null && item != null && !item.isEmpty() && bag.getItem() instanceof MagicBagItem) {
            ItemStack item2 = item.copy();
            item2.setCount(1);
            CompoundTag tag = Compat.getTags(bag);
            Tag t= Compat.ItemStack_save(item2,level);
            tag.put("item",t);
    #if MC_VERSION >= 12005
            if (item.isEmpty()) {
                CustomData.set(DataComponents.CUSTOM_DATA, bag, new CompoundTag());
            }else{
                CustomData.set(DataComponents.CUSTOM_DATA, bag, tag);
            }
    #endif
        }
    }

    static public ItemStack getItem(ItemStack bag, Level level) {
        if (bag != null && bag.getItem() instanceof MagicBagItem) {
            CompoundTag tag = Compat.getTags(bag);
            Optional<CompoundTag> citem=Compat.getCompound(tag,"item");
            if (citem.isPresent()) {
                Optional<ItemStack> item = Compat.ItemStack_read(citem.get(),level);
                return item.orElse(ItemStack.EMPTY);
            }
        }
        return ItemStack.EMPTY;
    }

    @Environment(EnvType.CLIENT)
#if MC_VERSION >= 12111
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
    #else
#if MC_VERSION >= 12101
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
#else
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag){
#endif
#endif
        //ItemStack i = MagicBagItem.getItem(stack,tooltipContext.registries());
        ItemStack i = MagicBagItem.getItem(stack, Minecraft.getInstance().level);
        #if MC_VERSION >= 12111
        if (i.isEmpty()) {
            consumer.accept(Compat.literal("item: none"));
        } else {
            consumer.accept(Compat.literal("item: ").append(Component.translatable (i.getItem().getDescriptionId())));
        }
        consumer.accept(Compat.literal("total: " + MagicBagItem.getTotal(stack)));
        #else
        if (i.isEmpty()) {
            list.add(Compat.literal("item: none"));
        } else {
            list.add(Compat.literal("item: ").append(Component.translatable (i.getItem().getDescriptionId())));
        }
        list.add(Compat.literal("total: " + MagicBagItem.getTotal(stack)));
        #endif
    }

    @Override
    public @NotNull Component getName(ItemStack itemStack) {
        if (Platform.getEnvironment() == Env.CLIENT) {
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof MagicBagItem) {
                ItemStack item = MagicBagItem.getItem(itemStack, Minecraft.getInstance().level);
                if (!item.isEmpty()) {
                    return Compat.literal("Bag of ").append(Component.translatable(item.getItem().getDescriptionId() )).append(" - Tier " + (tier.ordinal() + 1));
                }
            }
        }
        return super.getName(itemStack);
    }
}
