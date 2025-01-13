package net.nicguzzo.wands.menues;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
#if MC>="1193"
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
#else
import net.minecraft.core.Registry;
#endif
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
#if MC>="1205"
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
#endif
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.nicguzzo.wands.items.PaletteItem;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.utils.Compat;

import java.util.Optional;

public class PaletteMenu extends AbstractContainerMenu {
    
    public ItemStack palette;
    private final SimpleContainer inventory;
    public final Inventory playerInventory;

    public PaletteMenu(int syncId, Inventory playerInventory, FriendlyByteBuf packetByteBuf) {
        #if MC<"1205"
        this(syncId, playerInventory, packetByteBuf.readItem());
        #else
            this( syncId,playerInventory,
                ItemStack.parse(
                        ((Level) playerInventory.player.level()).registryAccess(),
                        packetByteBuf.readNbt()).orElse(ItemStack.EMPTY
                )
            );
        #endif
    }

    public PaletteMenu(int syncId, Inventory playerInventory, ItemStack palette) {
        super(WandsMod.PALETTE_CONTAINER.get(), syncId);
        this.palette=palette;

        this.playerInventory=playerInventory;
#if MC<"1205"
        CompoundTag tag= Compat.getTags(palette);
        ListTag list_tag = tag.getList("Palette", Compat.NbtType.COMPOUND);
        this.inventory= new SimpleContainer(27){
            @Override
            public void setChanged() {
                tag.put("Palette", toTag(this));
                super.setChanged();
            }
        };
        fromTag(list_tag, inventory);
#else
        this.inventory=PaletteItem.getInventory(palette);
#endif
        if (palette.getItem() instanceof PaletteItem) {
            int o;
            int n;
            for(o = 0; o < 3; ++o) {
                for(n = 0; n < 9; ++n) {
                   this.addSlot(new Slot(inventory, n + o * 9,
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
    public void slotsChanged(Container inventory) {
        super.slotsChanged(inventory);
        this.broadcastChanges();
        //PaletteItem.setInventory(palette,this.inventory);
    }

    @Override
    public void removed(Player player) {
        Inventory inventory = Compat.get_inventory(player);
        if (!inventory.getSelected().isEmpty()) {
            Compat.set_carried(player,this,ItemStack.EMPTY);
        }
    }
    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
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
    #if MC=="1165"
    public ItemStack clicked(int slotIndex, int button, ClickType actionType, Player player)
    #else
    public void clicked(int slotIndex, int button, ClickType actionType, Player player)
    #endif
    {
        //System.out.println("clicked "+button+" index "+slotIndex +" action: "+actionType);
        //return;
        try {
            
            if(actionType != ClickType.QUICK_CRAFT && button == 1){
                Compat.set_carried(player,this,ItemStack.EMPTY);
            }
            
            if(slotIndex>=0 && slotIndex<63){
                Slot slot = this.slots.get(slotIndex);
                if(actionType == ClickType.QUICK_CRAFT){
                    if(slotIndex<27){
                        ItemStack itemStack=Compat.get_carried(player,this);
                        slot.set(itemStack);
#if MC>="1205"
                        PaletteItem.setInventory(palette,this.inventory);
#endif
                        #if MC=="1165"
                        return ItemStack.EMPTY;
                        #else
                        return;
                        #endif

                    }
                }
                if (slot != null ) {
                    //slot.hasStack()
                    if (actionType == ClickType.CLONE){
                        ItemStack itemStack = slot.getItem();
                        ItemStack itemStack2=itemStack.copy();                            
                        if(can_pickup(itemStack2)){
                            itemStack2.setCount(1);
                            Compat.set_carried(player,this,itemStack2);
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
#if MC>="1205"
                        PaletteItem.setInventory(palette,this.inventory);
#endif
                        #if MC=="1165"
                        return ItemStack.EMPTY;
                        #else
                        return;
                        #endif
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
                                        Compat.set_carried(player,this,itemStack2);
                                    }
                                }else{
                                    itemStack=Compat.get_carried(player,this);
                                    if(!itemStack.isEmpty()){
                                        slot.set(itemStack);
                                        Compat.set_carried(player,this,ItemStack.EMPTY);
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
                                        Compat.set_carried(player,this,itemStack2);
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
#if MC>="1205"
            PaletteItem.setInventory(palette,this.inventory);
#endif
         } catch (Exception var8) {
            CrashReport crashReport = CrashReport.forThrowable(var8, "Container click");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Click info");
            crashReportCategory.setDetail("Menu Type", () -> {
                #if MC>="1193"
                    return this.getType() != null ? BuiltInRegistries.MENU.getKey(this.getType()).toString() : "<no type>";
                #else
                    return this.getType() != null ? Registry.MENU.getKey(this.getType()).toString() : "<no type>";
                #endif
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
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
#if MC<"1205"
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
#endif
}
