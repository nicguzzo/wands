package net.nicguzzo.wands.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.nicguzzo.wands.utils.Compat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class MagicBagItem extends Item {
    public int tier;
    public int limit=Integer.MAX_VALUE;
    public MagicBagItem(int tier,int limit,Properties properties) {
        super(properties);
        this.tier=tier;
        if(limit>0) {
            this.limit = limit;
        }
    }
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {
        ItemStack magic_bag =player.getItemInHand(interactionHand);
        if(!world.isClientSide()) {
            Compat.open_menu((ServerPlayer) player,magic_bag,2);
        }
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }
    static public int getTotal(ItemStack bag) {
        if(bag != null && bag.getItem() instanceof MagicBagItem) {
            CompoundTag tag= Compat.getTags(bag);
            return tag.getInt("total");
        }
        return 0;
    }

    //returns false if it reached the limit;
    static public boolean inc(ItemStack bag,int n) {
        if(bag != null && bag.getItem() instanceof MagicBagItem) {
            int lim=((MagicBagItem)bag.getItem()).limit;
            CompoundTag tag= Compat.getTags(bag);
            int total=tag.getInt("total");
            if(total+n<lim) {
                tag.putInt("total", total + n);
                return true;
            }
        }
        return false;
    }
    static public void dec(ItemStack bag,int n) {
        if(bag != null && bag.getItem() instanceof MagicBagItem) {
            CompoundTag tag= Compat.getTags(bag);
            int total=tag.getInt("total");
            tag.putInt("total", Math.max(total - n, 0));
        }
    }
    static public void setItem(ItemStack bag, ItemStack item) {
        if(bag != null && item!=null && !item.isEmpty() && bag.getItem() instanceof MagicBagItem) {
            ItemStack item2=item.copy();
            item2.setCount(1);
            CompoundTag tag= Compat.getTags(bag);
            tag.put("item",item2.save(new CompoundTag()));
        }
    }

    static public ItemStack getItem(ItemStack bag) {
        if(bag != null && bag.getItem() instanceof MagicBagItem) {
            CompoundTag tag= Compat.getTags(bag);
            return ItemStack.of(tag.getCompound("item"));
        }
        return ItemStack.EMPTY;
    }
    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, @NotNull TooltipFlag tooltipFlag)
    {
        ItemStack i=MagicBagItem.getItem(stack);
        if(i.isEmpty()) {
            list.add(Compat.literal("item: none"));
        }else{
            list.add(Compat.literal("item: ").append(Compat.translatable_item_name(i)));
        }
        list.add(Compat.literal("total: " + MagicBagItem.getTotal(stack) ));
    }
    @Override
    public @NotNull Component getName(ItemStack itemStack) {
        if(!itemStack.isEmpty() && itemStack.getItem() instanceof MagicBagItem){
            ItemStack item= MagicBagItem.getItem(itemStack);
            if(!item.isEmpty()) {
                return Compat.literal("Bag of ").append(Compat.translatable_item_name(item)).append(" - Tier "+(tier+1));
            }
        }
        return super.getName(itemStack);
    }
}
