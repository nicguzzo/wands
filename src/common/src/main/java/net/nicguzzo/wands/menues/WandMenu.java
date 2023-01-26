package net.nicguzzo.wands.menues;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.utils.Compat;

public class WandMenu extends AbstractContainerMenu {
    
    public ItemStack wand;
    public final Inventory playerInventory;
    private final SimpleContainer simplecontainer;

    class WandSlot extends Slot{
        public WandSlot(Container container, int i, int j, int k) {
            super(container, i, j, k);
        }
        @Override
        public boolean mayPlace(ItemStack itemStack) {
            return (itemStack.getItem() instanceof DiggerItem||itemStack.getItem() instanceof ShearsItem) && !(itemStack.getItem() instanceof WandItem);
        }
    }

    public WandMenu(int syncId, Inventory playerInventory, FriendlyByteBuf packetByteBuf) {
        this(syncId, playerInventory, packetByteBuf.readItem());
    }

    public WandMenu(int syncId, Inventory playerInventory, ItemStack _wand) {
        super(WandsMod.WAND_CONTAINER.get(), syncId);
        this.wand=_wand;
        this.playerInventory=playerInventory;
        ListTag tag = wand.getOrCreateTag().getList("Tools", Compat.NbtType.COMPOUND);
        this.simplecontainer= new SimpleContainer(9){
            @Override
            public void setChanged() {
                wand.getOrCreateTag().put("Tools", toTag(this));
                super.setChanged();
            }
        };
        fromTag(tag, simplecontainer);
        int o;
        int n;
        int k=0;
        for(o = 0; o < 9; ++o) {
            this.addSlot(new Slot(playerInventory, k++, 8 + o * 18, 142));
        }
        for(o = 0; o < 3; ++o) {
            for(n = 0; n < 9; ++n) {
                this.addSlot(new Slot(playerInventory, k++, 8 + n * 18, 84 + o * 18));
            }
        }        
        for(o = 0; o < 9; ++o) {
            this.addSlot(new WandSlot(simplecontainer, o, 8+o*18,32));
        }

    }

    @Override
    public boolean stillValid(Player player) {
        return wand.getItem() instanceof WandItem;
    }
    boolean mayPlace(ItemStack itemStack){
        return (itemStack.getItem() instanceof DiggerItem||itemStack.getItem() instanceof ShearsItem) && !(itemStack.getItem() instanceof WandItem);
    }
    boolean insert(ItemStack itemStack,int s,int e){
        for(int o = s; o < e; ++o) {
            Slot slot = this.slots.get(o);
            if(!slot.hasItem() && mayPlace(itemStack)){
                slot.set(itemStack);
                slot.setChanged();
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        //WandsMod.LOGGER.info("quick move "+index);
        //TODO: only quickmove while wand inv is visible
        Slot slot = (Slot)this.slots.get(index);
        if(index>=0 && index<36) { //player inv
                if (insert(slot.getItem(), 36, 45)) {
                    slot.set(ItemStack.EMPTY);
                }

        }else{
            if(index>=36 && index<45){
                if (insert(slot.getItem(), 0, 36)) {
                    slot.set(ItemStack.EMPTY);
                }
            }
        }
        return ItemStack.EMPTY;
    }
    public static ListTag toTag(SimpleContainer inventory) {
        ListTag list = new ListTag();
        for(int i = 0; i < inventory.getContainerSize(); i++) {
            CompoundTag stackTag = new CompoundTag();
            stackTag.putInt("Slot", i);
            ItemStack tool=inventory.getItem(i);
            if(!tool.isEmpty()) {
                stackTag.put("Tool", tool.save(new CompoundTag()));
                list.add(stackTag);
            }
        }
        return list;
    }

    public static void fromTag(ListTag tag, SimpleContainer inventory) {
        inventory.clearContent();
        tag.forEach(element -> {
            CompoundTag stackTag = (CompoundTag) element;
            int slot = stackTag.getInt("Slot");
            ItemStack stack = ItemStack.of(stackTag.getCompound("Tool"));
            inventory.setItem(slot, stack);
        });
    }
}