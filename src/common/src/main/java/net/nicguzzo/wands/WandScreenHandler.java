package net.nicguzzo.wands;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.nicguzzo.wands.mcver.MCVer;

public class WandScreenHandler extends AbstractContainerMenu {
    
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

    public WandScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf packetByteBuf) {
        this(syncId, playerInventory, packetByteBuf.readItem());
    }

    public WandScreenHandler(int syncId, Inventory playerInventory, ItemStack _wand) {
        super(WandsMod.WAND_SCREEN_HANDLER.get(), syncId);
        this.wand=_wand;
        this.playerInventory=playerInventory;
        ListTag tag = wand.getOrCreateTag().getList("Tools", MCVer.NbtType.COMPOUND);
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
    /*
    void pick(Player player,Slot slot){
        ItemStack itemStack = MCVer.inst.get_carried(player, this);
        if(itemStack.isEmpty() && can_pickup(slot.getItem())) {
            MCVer.inst.set_carried(player, this, slot.getItem());
            slot.set(ItemStack.EMPTY);
        }
    }
    void put(Player player,Slot slot){
        if(!slot.hasItem() ) {
            ItemStack itemStack = MCVer.inst.get_carried(player, this);
            if(can_pickup(itemStack)) {
                slot.set(itemStack);
                MCVer.inst.set_carried(player, this, ItemStack.EMPTY);
            }
        }
    }

    @Override
#if MC=="1165"
    public ItemStack clicked(int slotIndex, int button, ClickType actionType, Player player)
#else
    public void clicked(int slotIndex, int button, ClickType actionType, Player player)
#endif
    {
        try {
            if(slotIndex>=0 && slotIndex<45){
                Slot slot = this.slots.get(slotIndex);
                if (actionType == ClickType.PICKUP) {
                    if (slot.hasItem()) {
                        pick(player, slot);
                    } else {
                        put(player, slot);
                    }
                }
            }
            if(slotIndex>=0 && slotIndex<36) { //player inv
                Slot slot = this.slots.get(slotIndex);
                if (actionType == ClickType.QUICK_MOVE) {
                    if (insert(slot.getItem(), 36, 45)) {
                        slot.set(ItemStack.EMPTY);
                    }
                }
            }else{
                if(slotIndex>=36 && slotIndex<45){
                    Slot slot = this.slots.get(slotIndex);
                    if (actionType == ClickType.QUICK_MOVE) {
                        if (insert(slot.getItem(), 0, 36)) {
                            slot.set(ItemStack.EMPTY);
                        }
                    }
                }
            }

        } catch (Exception var8) {
            CrashReport crashReport = CrashReport.forThrowable(var8, "Container click");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Click info");
            crashReportCategory.setDetail("Menu Type", () -> {
                return this.getType() != null ? Registry.MENU.getKey(this.getType()).toString() : "<no type>";
            });
            crashReportCategory.setDetail("Menu Class", () -> {
                return this.getClass().getCanonicalName();
            });
            crashReportCategory.setDetail("Slot Count", (Object)this.slots.size());
            crashReportCategory.setDetail("Slot", (Object)slotIndex);
            crashReportCategory.setDetail("Button", (Object)button);
            crashReportCategory.setDetail("Type", (Object)actionType);
            throw new ReportedException(crashReport);
        }
#if MC=="1165"
        return ItemStack.EMPTY;
#else
        return;
#endif
    }

    */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        //WandsMod.LOGGER.info("quick move "+index);
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
        ListTag tag = new ListTag();
        for(int i = 0; i < inventory.getContainerSize(); i++) {
            CompoundTag stackTag = new CompoundTag();
            stackTag.putInt("Slot", i);
            stackTag.put("Tool", inventory.getItem(i).save(new CompoundTag()));
            tag.add(stackTag);
        }

        return tag;
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