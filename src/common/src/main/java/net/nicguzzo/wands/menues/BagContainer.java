//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.nicguzzo.wands.menues;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.utils.Compat;
import org.jetbrains.annotations.Nullable;

public class BagContainer implements Container, StackedContentsCompatible {
    private final int size;
    private final NonNullList<ItemStack> items;
    public ItemStack bag;
    @Nullable
    private List<ContainerListener> listeners;

    public BagContainer(int i,ItemStack bag) {
        this.size = i;
        this.items = NonNullList.withSize(i, ItemStack.EMPTY);
        this.bag=bag;
        clearContent();
        ListTag tag = bag.getOrCreateTag().getList("bag", Compat.NbtType.COMPOUND);
        fromTag(tag);
    }

    public void addListener(ContainerListener containerListener) {
        if (this.listeners == null) {
            this.listeners = Lists.newArrayList();
        }

        this.listeners.add(containerListener);
    }

    public void removeListener(ContainerListener containerListener) {
        if (this.listeners != null) {
            this.listeners.remove(containerListener);
        }

    }

    public ItemStack getItem(int i) {
        return i >= 0 && i < this.items.size() ? (ItemStack)this.items.get(i) : ItemStack.EMPTY;
    }

    public List<ItemStack> removeAllItems() {
        List<ItemStack> list = (List)this.items.stream().filter((itemStack) -> {
            return !itemStack.isEmpty();
        }).collect(Collectors.toList());
        this.clearContent();
        return list;
    }

    public ItemStack removeItem(int i, int j) {
        ItemStack itemStack = ContainerHelper.removeItem(this.items, i, j);
        if (!itemStack.isEmpty()) {
            this.setChanged();
        }

        return itemStack;
    }

    public ItemStack removeItemType(Item item, int i) {
        ItemStack itemStack = new ItemStack(item, 0);

        for(int j = this.size - 1; j >= 0; --j) {
            ItemStack itemStack2 = this.getItem(j);
            if (itemStack2.getItem().equals(item)) {
                int k = i - itemStack.getCount();
                ItemStack itemStack3 = itemStack2.split(k);
                itemStack.grow(itemStack3.getCount());
                if (itemStack.getCount() == i) {
                    break;
                }
            }
        }

        if (!itemStack.isEmpty()) {
            this.setChanged();
        }

        return itemStack;
    }

    public ItemStack addItem(ItemStack itemStack) {
        ItemStack itemStack2 = itemStack.copy();
        this.moveItemToOccupiedSlotsWithSameType(itemStack2);
        if (itemStack2.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.moveItemToEmptySlots(itemStack2);
            return itemStack2.isEmpty() ? ItemStack.EMPTY : itemStack2;
        }
    }

    public boolean canAddItem(ItemStack itemStack) {
        boolean bl = false;
        Iterator var3 = this.items.iterator();

        while(var3.hasNext()) {
            ItemStack itemStack2 = (ItemStack)var3.next();
            if (itemStack2.isEmpty() || ItemStack.isSameItemSameTags(itemStack2, itemStack) && itemStack2.getCount() < itemStack2.getMaxStackSize()) {
                bl = true;
                break;
            }
        }

        return bl;
    }

    public ItemStack removeItemNoUpdate(int i) {
        ItemStack itemStack = (ItemStack)this.items.get(i);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.items.set(i, ItemStack.EMPTY);
            return itemStack;
        }
    }

    public void setItem(int i, ItemStack itemStack) {

        ItemStack bag_item=MagicBagItem.getItem(bag);
        if(bag_item.isEmpty()){
            MagicBagItem.setTotal(bag,itemStack.getCount());
            this.items.set(i, itemStack.copyWithCount(1));
            this.setChanged();
        }else{
            if(itemStack.getItem()==bag_item.getItem()){
                MagicBagItem.inc(bag,itemStack.getCount());
                this.setChanged();
            }
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

            itemStack = (ItemStack)var1.next();
        } while(itemStack.isEmpty());

        return false;
    }

    public void setChanged() {
        if (this.listeners != null) {
            Iterator var1 = this.listeners.iterator();

            while(var1.hasNext()) {
                ContainerListener containerListener = (ContainerListener)var1.next();
                containerListener.containerChanged(this);
            }
        }
        bag.getOrCreateTag().put("bag", createTag());
    }

    public boolean stillValid(Player player) {
        return true;
    }

    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    public void fillStackedContents(StackedContents stackedContents) {
        Iterator var2 = this.items.iterator();

        while(var2.hasNext()) {
            ItemStack itemStack = (ItemStack)var2.next();
            stackedContents.accountStack(itemStack);
        }

    }

    public String toString() {
        return ((List)this.items.stream().filter((itemStack) -> {
            return !itemStack.isEmpty();
        }).collect(Collectors.toList())).toString();
    }

    private void moveItemToEmptySlots(ItemStack itemStack) {
        for(int i = 0; i < this.size; ++i) {
            ItemStack itemStack2 = this.getItem(i);
            if (itemStack2.isEmpty()) {
                this.setItem(i, itemStack.copy());
                itemStack.setCount(0);
                return;
            }
        }

    }

    private void moveItemToOccupiedSlotsWithSameType(ItemStack itemStack) {
        for(int i = 0; i < this.size; ++i) {
            ItemStack itemStack2 = this.getItem(i);
            if (ItemStack.isSameItemSameTags(itemStack2, itemStack)) {
                this.moveItemsBetweenStacks(itemStack, itemStack2);
                if (itemStack.isEmpty()) {
                    return;
                }
            }
        }

    }

    private void moveItemsBetweenStacks(ItemStack itemStack, ItemStack itemStack2) {
        int i = Math.min(this.getMaxStackSize(), itemStack2.getMaxStackSize());
        int j = Math.min(itemStack.getCount(), i - itemStack2.getCount());
        if (j > 0) {
            itemStack2.grow(j);
            itemStack.shrink(j);
            this.setChanged();
        }

    }

    public void fromTag(ListTag listTag) {
        this.clearContent();
        listTag.forEach(element -> {
            CompoundTag stackTag = (CompoundTag) element;
            int slot = stackTag.getInt("Slot");
            ItemStack stack = ItemStack.of(stackTag.getCompound("Block"));
            this.setItem(slot, stack);
        });
    }

    public ListTag createTag() {
        ListTag listTag = new ListTag();
        for(int i = 0; i < this.getContainerSize(); i++) {
            CompoundTag stackTag = new CompoundTag();
            stackTag.putInt("Slot", i);
            stackTag.put("Block", this.getItem(i).save(new CompoundTag()));
            listTag.add(stackTag);
        }

        return listTag;
    }
}
