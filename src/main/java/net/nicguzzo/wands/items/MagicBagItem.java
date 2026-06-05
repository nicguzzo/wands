package net.nicguzzo.wands.items;

import net.minecraft.ChatFormatting;
//? if >= 1.20.5 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//? }
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
//? if < 1.21.11 {

/*import net.minecraft.world.InteractionResultHolder;

*///? }
//? if >= 1.21.11 {
import net.minecraft.world.item.component.TooltipDisplay;
//? }
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.compat.Compat;

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

    public static MagicBagItem create_tier_1(){
        return new MagicBagItem(MagicBagItemTier.MAGIC_BAG_TIER_1, WandsMod.config.magic_bag_1_limit,new Item.Properties().stacksTo(1)
                //?if >=1.21.11
                .setId(WandsMod.magic_bag_1_key)
        );
    }

    public static MagicBagItem create_tier_2(){
        return new MagicBagItem(MagicBagItemTier.MAGIC_BAG_TIER_2, WandsMod.config.magic_bag_2_limit,new Item.Properties().stacksTo(1)
                //?if >=1.21.11
                .setId(WandsMod.magic_bag_2_key)
        );
    }

    public static MagicBagItem create_tier_3(){
        return new MagicBagItem(MagicBagItemTier.MAGIC_BAG_TIER_3, 0,new Item.Properties().stacksTo(1)
                //?if >=1.21.11
                .setId(WandsMod.magic_bag_3_key)
        );
    }

    @Override
//? if >= 1.21.11 {
    public InteractionResult use(Level world, Player player, InteractionHand interactionHand) {
//? } else {

    /*public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {

*///? }
        ItemStack magic_bag = player.getItemInHand(interactionHand);
        if (!world.isClientSide()) {
            Compat.open_menu((ServerPlayer) player, magic_bag, 2);
        }
//? if >= 1.21.11 {
        return InteractionResult.PASS;
//? } else {

        /*return InteractionResultHolder.pass(player.getItemInHand(interactionHand));

*///? }
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
                //? if >= 1.20.5 {
                CustomData.set(DataComponents.CUSTOM_DATA, bag, tag);
                //? }
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
            //? if >= 1.20.5 {
            CustomData.set(DataComponents.CUSTOM_DATA, bag, tag);
            //? }
        }
    }

    static public void setItem(ItemStack bag, ItemStack item, Level level) {
        if (bag != null && item != null && bag.getItem() instanceof MagicBagItem) {
            ItemStack item2 = item.copy();
            item2.setCount(1);
            CompoundTag tag = Compat.getTags(bag);
            Tag t= Compat.ItemStack_save(item2,level);
            tag.put("item",t);
    //? if >= 1.20.5 {
            if (item.isEmpty()) {
                CustomData.set(DataComponents.CUSTOM_DATA, bag, new CompoundTag());
            }else{
                CustomData.set(DataComponents.CUSTOM_DATA, bag, tag);
            }
    //? }
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


//? if >= 1.21.11 {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        //Level level= ClientUtils.getClientLevelSafe();
        Level level= null;
        if (level == null) return;
    //? } else {
    
/*//? if >= 1.21.1 {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        //Level level= ClientUtils.getClientLevelSafe();
        Level level= null;
        if (level == null) return;
//? } else {
    /^@Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag){
^///? }

*///? }
        //? if >= 1.21.11 {
        Consumer<Component> addLine = consumer;
        //? } else {
        
        /*Consumer<Component> addLine = list::add;
        
        *///? }

        ItemStack i = MagicBagItem.getItem(stack, level);
        if (i.isEmpty()) {
            addLine.accept(Compat.literal("Item: None").withStyle(ChatFormatting.GRAY));
        } else {
            addLine.accept(Compat.literal("Item: ").append(Component.translatable(i.getItem().getDescriptionId())).withStyle(ChatFormatting.GRAY));
        }
        addLine.accept(Compat.literal("Total: " + MagicBagItem.getTotal(stack)).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public @NotNull Component getName(ItemStack itemStack) {
        //Level level= ClientUtils.getClientLevelSafe();
        Level level= null;
        if (level == null) return super.getName(itemStack);
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof MagicBagItem) {
            ItemStack item = MagicBagItem.getItem(itemStack, level);
            if (!item.isEmpty()) {
                return Compat.literal("Bag of ").append(Component.translatable(item.getItem().getDescriptionId() )).append(" - Tier " + (tier.ordinal() + 1));
            }
        }
        return super.getName(itemStack);
    }
}
