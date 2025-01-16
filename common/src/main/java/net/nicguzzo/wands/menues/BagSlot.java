package net.nicguzzo.wands.menues;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.items.MagicBagItem;

public class BagSlot extends Slot {
    public ItemStack bag;
    private HolderLookup.Provider ra;
    public BagSlot(Container container, int i, int j, int k,ItemStack bag,HolderLookup.Provider r) {
        super(container, i, j, k);
        this.bag=bag;
        this.ra=r;
    }
    @Override
    public boolean mayPlace(ItemStack itemStack) {
        if(bag!=null) {
            ItemStack item = MagicBagItem.getItem(bag,ra);
            return item.isEmpty() || MagicBagItem.getItem(bag,ra).getItem() == itemStack.getItem();
        }
        else return false;
    }
    @Override
    public ItemStack getItem() {
        if(bag!=null) {
            ItemStack item = MagicBagItem.getItem(bag,ra);
            return item;
        }else return ItemStack.EMPTY;
    }
}
