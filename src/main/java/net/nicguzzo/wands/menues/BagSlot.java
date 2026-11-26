package net.nicguzzo.wands.menues;

import net.nicguzzo.wands.compat.Compat;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.nicguzzo.wands.items.MagicBagItem;

public class BagSlot extends Slot {
    public ItemStack bag;
    private Level level;
    public BagSlot(Container container, int i, int j, int k, ItemStack bag, Level lvl) {
        super(container, i, j, k);
        this.bag=bag;
        this.level=lvl;
    }
    @Override
    public boolean mayPlace(ItemStack itemStack) {
        if(bag!=null) {
            ItemStack item = MagicBagItem.getItem(bag,level);
            return item.isEmpty() || Compat.is_same_with_components(item, itemStack);
        }
        else return false;
    }
    @Override
    public ItemStack getItem() {
        if(bag!=null) {
            ItemStack item = MagicBagItem.getItem(bag,level);
            return item;
        }else return ItemStack.EMPTY;
    }
}
