package net.nicguzzo.wands.menues;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.items.MagicBagItem;

public class BagSlot extends Slot {
    public ItemStack bag;
    public BagSlot(Container container, int i, int j, int k,ItemStack bag) {
        super(container, i, j, k);
        this.bag=bag;
    }
    @Override
    public boolean mayPlace(ItemStack itemStack) {
        if(bag!=null) {
            ItemStack item = MagicBagItem.getItem(bag);
            return item.isEmpty() || MagicBagItem.getItem(bag).getItem() == itemStack.getItem();
        }
        else return false;
    }
    @Override
    public ItemStack getItem() {
        if(bag!=null) {
            ItemStack item = MagicBagItem.getItem(bag);
            return item;
        }else return ItemStack.EMPTY;
    }
}
