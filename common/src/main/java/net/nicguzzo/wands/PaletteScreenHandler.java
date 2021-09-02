package net.nicguzzo.wands;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.nicguzzo.wands.mcver.MCVer;

public class PaletteScreenHandler extends AbstractContainerMenu {
    
    public ItemStack palette;
    private final SimpleContainer simplecontainer;
    public final Inventory playerInventory;

    public PaletteScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf packetByteBuf) {
        this(syncId, playerInventory, packetByteBuf.readItem());
    }

    public PaletteScreenHandler(int syncId, Inventory playerInventory, ItemStack palette) {
        super(WandsMod.PALETTE_SCREEN_HANDLER.get(), syncId);
        this.palette=palette;
        this.playerInventory=playerInventory;
        ListTag tag = palette.getOrCreateTag().getList("Palette", MCVer.NbtType.COMPOUND);
        this.simplecontainer= new SimpleContainer(27){
            @Override
            public void setChanged() {
                palette.getOrCreateTag().put("Palette", toTag(this));
                super.setChanged();
            }
        };
        fromTag(tag, simplecontainer);
        if (palette.getItem() instanceof PaletteItem) {
            int o;
            int n;
            for(o = 0; o < 3; ++o) {
                for(n = 0; n < 9; ++n) {
                   this.addSlot(new Slot(simplecontainer, n + o * 9,
                           8 + n * 18, 18 + o * 18));
                }
            }
            for(o = 0; o < 3; ++o) {                
                for(n = 0; n < 9; ++n) {
                   this.addSlot(new Slot(playerInventory, n + o * 9 + 9,
                           8 + n * 18, 84 + o * 18));
                }
            }
            for(o = 0; o < 9; ++o) {
                this.addSlot(new Slot(playerInventory, o, 8 + o * 18, 142));
            }
        } else {
            Player player = playerInventory.player;
            this.removed(player);
        }
    }
    @Override
    public void removed(Player player) {
        Inventory inventory = MCVer.inst.get_inventory(player);

        if (!inventory.getSelected().isEmpty()) {
            MCVer.inst.set_carried(player,this,ItemStack.EMPTY);
        }
    }
    @Override
    public boolean stillValid(Player player) {
        return palette.getItem() instanceof PaletteItem;
    }
    boolean can_pickup(ItemStack itemStack){
        Item  item=itemStack.getItem();
        Block blk=Block.byItem(item);        
        return itemStack.isStackable() /*&& item.canBeNested()*/ && blk!=Blocks.AIR;
    }
    void insert(ItemStack itemStack){
        for(int o = 0; o < 27; ++o) {
            Slot slot = this.slots.get(o);
            if(slot.getItem().isEmpty()){                    
                slot.set(itemStack);
                slot.setChanged();
                break;
            }
        }
    }

    @Override
    //beginMC1_16_5
    public ItemStack clicked(int slotIndex, int button, ClickType actionType, Player player) {
    //endMC1_16_5  
    /*//beginMC1_17_1
    public void clicked(int slotIndex, int button, ClickType actionType, Player player) {
    //endMC1_17_1*/ 

        //System.out.println("clicked "+button+" index "+slotIndex +" action: "+actionType);
        //return;
        try {
            
            if(actionType != ClickType.QUICK_CRAFT && button == 1){
                MCVer.inst.set_carried(player,this,ItemStack.EMPTY);
            }
            
            if(slotIndex>=0 && slotIndex<63){
                Slot slot = this.slots.get(slotIndex);
                if(actionType == ClickType.QUICK_CRAFT){
                    if(slotIndex<27){
                        ItemStack itemStack=MCVer.inst.get_carried(player,this);
                        slot.set(itemStack);
                        //beginMC1_16_5
                        return ItemStack.EMPTY;
                        //endMC1_16_5  
                        /*//beginMC1_17_1
                        return;
                        //endMC1_17_1*/ 

                    }
                }
                if (slot != null ) {
                    //slot.hasStack()
                    if (actionType == ClickType.CLONE){
                        ItemStack itemStack = slot.getItem();
                        ItemStack itemStack2=itemStack.copy();                            
                        if(can_pickup(itemStack2)){
                            itemStack2.setCount(1);
                            MCVer.inst.set_carried(player,this,itemStack2);
                        }
                    }
                    //System.out.println("itemStack2: "+itemStack);
                    if(button == 1){
                        if(slotIndex<27 ) {
                            if (actionType == ClickType.PICKUP) {
                                ItemStack itemStack = slot.getItem();
                                if (!itemStack.isEmpty()) {
                                    //System.out.println("empty");
                                    slot.set(ItemStack.EMPTY);
                                    slot.setChanged();
                                }
                            }
                        }
                        //beginMC1_16_5
                        return ItemStack.EMPTY;
                        //endMC1_16_5  
                        /*//beginMC1_17_1
                        return;
                        //endMC1_17_1*/ 
                    }
                    if(button == 0){
                        if(slotIndex<27 ){
                            if (actionType == ClickType.PICKUP || actionType == ClickType.QUICK_CRAFT) {
                                
                                ItemStack itemStack = slot.getItem();
                                if(!itemStack.isEmpty()){
                                    //System.out.println("empty");
                                    //slot.set(ItemStack.EMPTY);
                                    ItemStack itemStack2=itemStack.copy();
                                    if(can_pickup(itemStack2)){
                                        itemStack2.setCount(1);
                                        MCVer.inst.set_carried(player,this,itemStack2);
                                    }
                                }else{
                                    itemStack=MCVer.inst.get_carried(player,this);
                                    if(!itemStack.isEmpty()){
                                        slot.set(itemStack);
                                        MCVer.inst.set_carried(player,this,ItemStack.EMPTY);
                                    }
                                    //this.setCarried(ItemStack.EMPTY);
                                }
                                slot.setChanged();
                            }
                        }else{
                            if (actionType == ClickType.PICKUP||actionType == ClickType.QUICK_MOVE){
                                ItemStack itemStack = slot.getItem();
                                ItemStack itemStack2=itemStack.copy();                            
                                if(can_pickup(itemStack2)){
                                    itemStack2.setCount(1);
                                    if(actionType == ClickType.PICKUP){
                                        MCVer.inst.set_carried(player,this,itemStack2);
                                    }else{
                                        insert(itemStack2);      
                                    }
                                }
                            }
                            //if(!itemStack.isEmpty())
                            {
                            //    insert(itemStack);
                                //ItemStack itemStack2=itemStack.copy();
                                //itemStack2.setCount(1);
                                //slot.set(itemStack2);
                            }
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
        //beginMC1_16_5
        return ItemStack.EMPTY;
        //endMC1_16_5  
        /*//beginMC1_17_1
        return;
        //endMC1_17_1*/ 
    }
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
    public static ListTag toTag(SimpleContainer inventory) {
        ListTag tag = new ListTag();        
        for(int i = 0; i < inventory.getContainerSize(); i++) {
            CompoundTag stackTag = new CompoundTag();
            stackTag.putInt("Slot", i);
            stackTag.put("Block", inventory.getItem(i).save(new CompoundTag()));
            tag.add(stackTag);
        }

        return tag;
    }

    public static void fromTag(ListTag tag, SimpleContainer inventory) {
        inventory.clearContent();
        tag.forEach(element -> {
            CompoundTag stackTag = (CompoundTag) element;
            int slot = stackTag.getInt("Slot");
            ItemStack stack = ItemStack.of(stackTag.getCompound("Block"));
            inventory.setItem(slot, stack);
        });
    }

}