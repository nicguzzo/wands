package net.nicguzzo.wands.menues;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
#if MC>="1193"
import net.minecraft.core.registries.BuiltInRegistries;
#else
import net.minecraft.core.Registry;
#endif
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.utils.Compat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MagicBagMenu extends AbstractContainerMenu {
    public ItemStack bag;
    public final Inventory playerInventory;
    private final BagContainer bagcontainer;
    public static final Logger LOGGER = LogManager.getLogger();
    //private int total;
    //private CompoundTag item_id;

    public MagicBagMenu(int syncId, Inventory playerInventory, FriendlyByteBuf packetByteBuf) {
        this(syncId, playerInventory, packetByteBuf.readItem());
    }

    public MagicBagMenu(int syncId, Inventory playerInventory, ItemStack bag) {
        super(WandsMod.MAGIC_WAND_CONTANIER.get(), syncId);
        this.playerInventory=playerInventory;
        this.bag=bag;

        //ListTag tag = bag.getOrCreateTag().getList("bag", Compat.NbtType.COMPOUND);
        //ListTag tag = .getList("MagicBag", MCVer.NbtType.COMPOUND);
//        total=tag.getInt("total");
//        item_id=tag.getCompound("item");
        this.bagcontainer = new BagContainer(1,bag);
        //this.bagcontainer.clearContent();
        /*CompoundTag tag=bag.getOrCreateTag();
        ItemStack stack = ItemStack.of(tag.getCompound("item"));
        WandsMod.LOGGER.info("item "+stack);
        this.simplecontainer.setItem(0, stack);*/
        //fromTag(tag, bagcontainer);
        //bagcontainer.fromTag(tag);
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
        this.addSlot(new Slot(bagcontainer, 0, 80,32));

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
#if false
    @Override
    #if MC=="1165"
    public ItemStack clicked(int slotIndex, int button, ClickType actionType, Player player)
    #else
    public void clicked(int slotIndex, int button, ClickType actionType, Player player)
    #endif
    {
        LOGGER.info("clicked "+button+" index "+slotIndex +" action: "+actionType);
        try {
            if(slotIndex==36) {
                Slot slot = this.slots.get(slotIndex);
                if (button == 0 || button == 1) {
                    switch (actionType) {
                        case PICKUP: {
                            ItemStack carried=Compat.get_carried(player,this);
                            if(carried.isEmpty()) {
                                if (bag != null && !slot.getItem().isEmpty()) {
                                    ItemStack itemStack = slot.getItem();
                                    ItemStack itemStack2 = itemStack.copy();
                                    int total = MagicBagItem.getTotal(bag);
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
                                        MagicBagItem.setTotal(bag, total - c);
                                        if (total - c == 0) {
                                            MagicBagItem.setItem(bag, ItemStack.EMPTY);
                                            slot.set(ItemStack.EMPTY);
                                        }
                                        Compat.set_carried(player, this, itemStack2);
                                    }
                                }
                            }
                        }break;
                        case QUICK_MOVE:{

                        }
                    }

                }
            }else{
                if (actionType == ClickType.QUICK_MOVE) {

                    Slot slot_src = this.slots.get(slotIndex);
                    Slot slot_dst = this.slots.get(36);
                    ItemStack item_src=slot_src.getItem().copy();
                    if(item_src.isStackable()){
                        if(slot_dst.hasItem()){
                            ItemStack item_dst=slot_dst.getItem();
                            if(item_src.getItem()==item_dst.getItem()) {
                                MagicBagItem.inc(bag, item_src.getCount());
                                slot_src.set(ItemStack.EMPTY);
                            }
                        }else{
                            MagicBagItem.setItem(bag,item_src);
                            MagicBagItem.setTotal(bag,item_src.getCount());
                            slot_src.set(ItemStack.EMPTY);
                        }
                    }
                }else {
                    #if MC=="1165"
                    return super.clicked(slotIndex,button,actionType,player);
                    #else
                    super.clicked(slotIndex, button, actionType, player);
                    #endif
                }
            }
            /*
            if(slotIndex>=0 && slotIndex<36){
                Slot slot = this.slots.get(slotIndex);
                if (slot != null ) {
                    if (actionType == ClickType.PICKUP) {
                        if(button == 0){
                            ItemStack itemStack = slot.getItem();
                            if(!itemStack.isEmpty()){
                                ItemStack itemStack2=itemStack.copy();
                                if(can_pickup(itemStack2)){
                                    itemStack2.setCount(1);
                                    Compat.set_carried(player,this,itemStack2);
                                }
                            }
                        }
                        if(button == 1){
                            Compat.set_carried(player,this,ItemStack.EMPTY);
                        }
                    }
                }
            }else{
                if(slotIndex==36) {
                    Slot slot = this.slots.get(slotIndex);
                    if (button == 0) {
                        if (actionType == ClickType.PICKUP) {

                            if (slot.getItem().isEmpty()) {
                                ItemStack itemStack = Compat.get_carried(player, this);
                                slot.set(itemStack);
                                //slot.setChanged();
                                MagicBagItem.setItem(bag,itemStack);
                                Compat.set_carried(player,this,ItemStack.EMPTY);
                            }else{

                            }
                        }
                    }
                    if (button == 1) {
                        if (actionType == ClickType.PICKUP) {
                            if (bag!=null && !slot.getItem().isEmpty()) {
                                ItemStack itemStack = slot.getItem();
                                ItemStack itemStack2 = itemStack.copy();
                                int total=MagicBagItem.getTotal(bag);
                                if(total>0) {
                                    int c=64;
                                    if(total<64) {
                                        c=total;
                                    }
                                    itemStack2.setCount(c);
                                    MagicBagItem.setTotal(bag,total-c);
                                    Compat.set_carried(player, this, itemStack2);
                                }

                            }
                        }
                    }
                }else{
                    if (button == 1) {
                        if (actionType == ClickType.PICKUP) {
                            Compat.set_carried(player, this, ItemStack.EMPTY);
                        }
                    }
                }
            }*/
            /*if(actionType != ClickType.QUICK_CRAFT && button == 1){
                Compat.set_carried(player,this,ItemStack.EMPTY);
            }

            if(slotIndex>=0 && slotIndex<37){
                Slot slot = this.slots.get(slotIndex);
                if(actionType == ClickType.QUICK_CRAFT){
                    if(slotIndex<27){
                        ItemStack itemStack=Compat.get_carried(player,this);
                        slot.set(itemStack);
                        #if MC=="1165"
                        return ItemStack.EMPTY;
                        #else
                        return;
                        #endif

                    }
                }
                if (slot != null ) {
                    if (actionType == ClickType.CLONE){
                        ItemStack itemStack = slot.getItem();
                        ItemStack itemStack2=itemStack.copy();
                        if(can_pickup(itemStack2)){
                            itemStack2.setCount(1);
                            Compat.set_carried(player,this,itemStack2);
                        }
                    }
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
                        }
                    }
                }
            }*/
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

    public static ListTag toTag(BagContainer inventory) {
        ListTag tag = new ListTag();
        for(int i = 0; i < inventory.getContainerSize(); i++) {
            CompoundTag stackTag = new CompoundTag();
            stackTag.putInt("Slot", i);
            stackTag.put("Block", inventory.getItem(i).save(new CompoundTag()));
            tag.add(stackTag);
        }

        return tag;
    }

    public static void fromTag(ListTag tag, BagContainer inventory) {
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