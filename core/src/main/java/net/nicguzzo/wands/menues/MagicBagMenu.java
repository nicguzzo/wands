package net.nicguzzo.wands.menues;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.compat.Compat;

import java.util.Objects;

public class MagicBagMenu extends AbstractContainerMenu {
    public ItemStack bag;
    public final Inventory playerInventory;
    private final BagContainer bagcontainer;

    //public MagicBagMenu(int syncId, Inventory playerInventory, FriendlyByteBuf packetByteBuf) {
    //    this( syncId,playerInventory, Objects.requireNonNull(packetByteBuf.readNbt()).read(ItemStack.MAP_CODEC).orElse(ItemStack.EMPTY));
    //}
    public MagicBagMenu(int syncId, Inventory playerInventory, FriendlyByteBuf packetByteBuf) {
        #if MC_VERSION < 12005
        this(syncId, playerInventory, packetByteBuf.readItem());
        #else
            #if MC_VERSION < 12111
                this( syncId,playerInventory,
                        ItemStack.parse(
                                ((Level) playerInventory.player.level()).registryAccess(),
                                packetByteBuf.readNbt()).orElse(ItemStack.EMPTY
                        )
                );
            #else
                this( syncId,playerInventory, Objects.requireNonNull(packetByteBuf.readNbt()).read(ItemStack.MAP_CODEC).orElse(ItemStack.EMPTY));
            #endif
        #endif
    }

    public MagicBagMenu(int syncId, Inventory playerInventory, ItemStack bag) {
        super(WandsMod.MAGIC_WAND_CONTANIER.get(), syncId);
        this.playerInventory=playerInventory;
        this.bag=bag;

        this.bagcontainer = new BagContainer(1,bag,playerInventory.player.level());

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
        this.addSlot(new BagSlot(bagcontainer, 0, 80,32,bag,playerInventory.player.level()));
    }

    @Override
    public boolean stillValid(Player player) {
        return bag.getItem() instanceof MagicBagItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        //LOGGER.info("quickMoveStack index "+index);
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType actionType, Player player)
    {
        if (bag == null)
            return;
        //LOGGER.info("clicked "+button+" index "+slotIndex +" action: "+actionType);
        try {
            Level level= player.level();
            if(slotIndex==36) {
                Slot slot = this.slots.get(slotIndex);
                if (button == 0 || button == 1) {
                    switch (actionType) {
                        case QUICK_CRAFT:
                        case PICKUP: {
                            ItemStack carried=Compat.get_carried(player,this);
                            ItemStack itemStack = slot.getItem();
                            ItemStack itemStack2 = itemStack.copy();
                            int total = MagicBagItem.getTotal(bag);
                            if(carried.isEmpty()) {
                                if (total > 0) {
                                    int c = 1;
                                    if (button == 1) {
                                        int m = itemStack.getMaxStackSize();
                                        c = m;
                                        if (total < m) {
                                            c = total;
                                        }
                                    }
                                    itemStack2.setCount(c);
                                    MagicBagItem.dec(bag, c);
                                    if (total - c == 0) {
                                        //MagicBagItem.setItem(bag, ItemStack.EMPTY,level);
                                        //slot.set(ItemStack.EMPTY);
                                    }
                                    Compat.set_carried(player, this, itemStack2);
                                }else{
                                    MagicBagItem.setItem(bag, ItemStack.EMPTY,level);
                                    slot.set(ItemStack.EMPTY);
                                }
                            }else{
                                ItemStack bag_item= MagicBagItem.getItem(bag,level);
                                if(carried.isStackable()){
                                    if(bag_item.isEmpty()){
                                        MagicBagItem.setItem(bag, carried,level);
                                        bag_item=carried.copy();
                                        bag_item.setCount(1);
                                    }
                                    if(carried.getItem()==bag_item.getItem()){
                                        if(Compat.is_same_with_components(carried,bag_item) &&  MagicBagItem.inc(bag,carried.getCount())) {
                                            Compat.set_carried(player, this, ItemStack.EMPTY);
                                        }
                                    }
                                }
                            }
                        }break;
                        case QUICK_MOVE:{
                            int free_slot=playerInventory.getFreeSlot();
                            if(free_slot!=-1){
                                ItemStack bag_item= MagicBagItem.getItem(bag,level);
                                int total = MagicBagItem.getTotal(bag);
                                int m = bag_item.getMaxStackSize();
                                int c=m;
                                if(total<m){
                                    c=total;
                                }
                                MagicBagItem.dec(bag, c);
                                ItemStack item=bag_item.copy();
                                item.setCount(c);
                                playerInventory.setItem(free_slot,item);
                            }
                        }
                    }

                }
            }else{
                if (actionType == ClickType.QUICK_MOVE && slotIndex<this.slots.size()) {

                    Slot slot_src = this.slots.get(slotIndex);
                    Slot slot_dst = this.slots.get(36);
                    ItemStack item_src=slot_src.getItem().copy();
                    if(item_src.isStackable()){
                        if(slot_dst.hasItem()){
                            ItemStack item_dst=slot_dst.getItem();
                            if(item_src.getItem()==item_dst.getItem()) {
                                if(MagicBagItem.inc(bag,item_src.getCount())) {
                                    slot_src.set(ItemStack.EMPTY);
                                }
                            }
                        }else{
                            MagicBagItem.setItem(bag,item_src,level);
                            if(MagicBagItem.inc(bag,item_src.getCount())) {
                                slot_src.set(ItemStack.EMPTY);
                            }
                        }
                    }
                }else {
                    super.clicked(slotIndex, button, actionType, player);
                }
            }

        } catch (Exception var8) {
            CrashReport crashReport = CrashReport.forThrowable(var8, "Container click");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Click info");
            crashReportCategory.setDetail("Menu Type", () -> {
                return this.getType() != null ? BuiltInRegistries.MENU.getKey(this.getType()).toString() : "<no type>";
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
        return;
    }
}
