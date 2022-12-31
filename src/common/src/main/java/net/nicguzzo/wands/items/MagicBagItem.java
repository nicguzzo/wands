package net.nicguzzo.wands.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {
        ItemStack magic_bag =player.getItemInHand(interactionHand);
        if(!world.isClientSide()) {
            Compat.open_menu((ServerPlayer) player,magic_bag,2);
        }
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }
    static public int getTotal(ItemStack bag) {
        if(bag != null && bag.getItem() instanceof MagicBagItem) {
            return bag.getOrCreateTag().getInt("total");
        }
        return 0;
    }
    /*static public int setTotal(ItemStack bag,int total) {
        if(bag != null && bag.getItem() instanceof MagicBagItem) {
            int lim=((MagicBagItem)bag.getItem()).limit;
            if(total<lim) {
                bag.getOrCreateTag().putInt("total", total);
                return 0;
            }else{
                bag.getOrCreateTag().putInt("total", total);
                return 0;
            }
            //if(total<=0){
//                bag.getOrCreateTag().remove("item");
//            }
        }
        return 0;
    }*/
    //returns false if it reached the limit;
    static public boolean inc(ItemStack bag,int n) {
        if(bag != null && bag.getItem() instanceof MagicBagItem) {
            int lim=((MagicBagItem)bag.getItem()).limit;
            int total=bag.getOrCreateTag().getInt("total");
            if(total+n<lim) {
                bag.getOrCreateTag().putInt("total", total + n);
                return true;
            }
        }
        return false;
    }
    static public void dec(ItemStack bag,int n) {
        if(bag != null && bag.getItem() instanceof MagicBagItem) {
            int total=bag.getOrCreateTag().getInt("total");
            if(total-n>=0) {
                bag.getOrCreateTag().putInt("total", total - n);
            }else{
                bag.getOrCreateTag().putInt("total", 0);
            }
        }
    }
    static public void setItem(ItemStack bag, ItemStack item) {
        if(bag != null && item!=null && bag.getItem() instanceof MagicBagItem) {
            ItemStack item2=item.copy();
            item2.setCount(1);
            bag.getOrCreateTag().put("item",item2.save(new CompoundTag()));
        }
    }

    static public ItemStack getItem(ItemStack bag) {
        if(bag != null && bag.getItem() instanceof MagicBagItem) {
            CompoundTag tag = bag.getOrCreateTag();
            return ItemStack.of(tag.getCompound("item"));
        }
        return ItemStack.EMPTY;
    }
    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        ItemStack i=MagicBagItem.getItem(stack);
        if(i.isEmpty()) {
            list.add(Compat.literal("item: none"));
        }else{
            list.add(Compat.literal("item: ").append(Compat.translatable(i.getDescriptionId())));
        }
        list.add(Compat.literal("total: " + MagicBagItem.getTotal(stack) ));

    }
}
