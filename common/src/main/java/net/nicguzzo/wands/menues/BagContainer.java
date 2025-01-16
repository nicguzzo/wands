package net.nicguzzo.wands.menues;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.items.MagicBagItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class BagContainer implements Container, StackedContentsCompatible {
    private final int size;
    private final NonNullList<ItemStack> items;
    private final HolderLookup.Provider ra;
    public ItemStack bag;
    public static final Logger LOGGER = LogManager.getLogger();

    public BagContainer(int i, ItemStack bag,HolderLookup.Provider ra) {
        this.size = i;
        this.ra=ra;
        this.items = NonNullList.withSize(i, ItemStack.EMPTY);
        this.bag = bag;
        clearContent();
        //ListTag tag = bag.getOrCreateTag().getList("bag", Compat.NbtType.COMPOUND);
        //fromTag(tag);
        ItemStack bag_item = MagicBagItem.getItem(bag,ra);
        if (!bag_item.isEmpty()) {
            this.items.set(0, MagicBagItem.getItem(bag,ra));
        }
    }

    public ItemStack getItem(int i) {
        return i >= 0 && i < this.items.size() ? (ItemStack) this.items.get(i) : ItemStack.EMPTY;
    }

    public ItemStack removeItem(int i, int j) {
        //LOGGER.info("removeItem slot: "+i+" c: "+j);
        /*ItemStack itemStack = ContainerHelper.removeItem(this.items, i, j);
        if (!itemStack.isEmpty()) {
            this.setChanged();
        }*/

        return ItemStack.EMPTY;

    }

    public boolean canPlaceItem(int i, ItemStack itemStack) {
        return itemStack.isStackable();
    }

    public ItemStack removeItemNoUpdate(int i) {
        return ItemStack.EMPTY;
    }

    public void setItem(int i, ItemStack itemStack) {
        //LOGGER.info("setItem slot: "+i+" item: "+itemStack);
        ItemStack bag_item = MagicBagItem.getItem(bag,ra);
        if (bag_item.isEmpty()) {
            MagicBagItem.setItem(bag, itemStack,ra);
            ItemStack item2 = itemStack.copy();
            item2.setCount(1);
            this.items.set(i, item2);
        }
    }

    public int getContainerSize() {
        return this.size;
    }

    public boolean isEmpty() {
        Iterator var1 = this.items.iterator();

        ItemStack itemStack;
        do {
            if (!var1.hasNext()) {
                return true;
            }
            itemStack = (ItemStack) var1.next();
        } while (itemStack.isEmpty());

        return false;
    }

    public void setChanged() {
        //bag.getOrCreateTag().put("bag", createTag());
    }

    public boolean stillValid(Player player) {
        return true;
    }

    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    public String toString() {
        return ((List) this.items.stream().filter((itemStack) -> {
            return !itemStack.isEmpty();
        }).collect(Collectors.toList())).toString();
    }

    @Override
    public void fillStackedContents(StackedContents stackedItemContents) {
        Iterator var2 = this.items.iterator();
        while (var2.hasNext()) {
            ItemStack itemStack = (ItemStack) var2.next();
            stackedItemContents.accountStack(itemStack);
        }
    }
}
