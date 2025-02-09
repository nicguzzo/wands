package net.nicguzzo.wands.menues;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.items.PaletteItem;
import net.nicguzzo.wands.utils.Compat;

public class PaletteMenu extends AbstractContainerMenu {
    private final int containerRows=6;
    public ItemStack palette;
    private final SimpleContainer inventory;
    public final Inventory playerInventory;

    public PaletteMenu(int syncId, Inventory playerInventory, FriendlyByteBuf packetByteBuf) {
        this(syncId, playerInventory,ItemStack.parse(
                        playerInventory.player.level().registryAccess(),
                        packetByteBuf.readNbt()
                ).orElse(ItemStack.EMPTY)
        );
    }

    public PaletteMenu(int syncId, Inventory playerInventory, ItemStack palette) {
        super(WandsMod.PALETTE_CONTAINER.get(), syncId);
        this.palette = palette;
        this.playerInventory = playerInventory;
        this.inventory = PaletteItem.getInventory(palette,playerInventory.player.level());
        if (palette.getItem() instanceof PaletteItem) {
            int k = 18;
    		this.addPaletteGrid(inventory, 8, k);
            int l = k + this.containerRows * k + 13;
		    this.addStandardInventorySlots(playerInventory, 8, l);

            /*int o;
            int n;
            for (o = 0; o < 6; ++o) {
                for (n = 0; n < 9; ++n) {
                    this.addSlot(new Slot(inventory, n + o * 9,
                            8 + n * 18, 18 + o * 18));
                }
            }
            for (o = 0; o < 3; ++o) {
                for (n = 0; n < 9; ++n) {
                    this.addSlot(new Slot(playerInventory, n + o * 9 + 9,
                            8 + n * 18, 84 + o * 18));
                }
            }
            for (o = 0; o < 9; ++o) {
                this.addSlot(new Slot(playerInventory, o, 8 + o * 18, 142));
            }*/
        } else {
            Player player = playerInventory.player;
            this.removed(player);
        }
    }

    private void addPaletteGrid(Container container, int i, int j) {
		for (int k = 0; k < 6; k++) {
			for (int l = 0; l < 9; l++) {
				this.addSlot(new Slot(container, l + k * 9, i + l * 18, j + k * 18));
			}
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
            Compat.set_carried(player, this, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    boolean can_pickup(ItemStack itemStack) {
        Item item = itemStack.getItem();
        Block blk = Block.byItem(item);
        return itemStack.isStackable() /*&& item.canBeNested()*/ && blk != Blocks.AIR;
    }

    void insert(ItemStack itemStack) {
        for (int o = 0; o < this.inventory.getContainerSize(); ++o) {
            Slot slot = this.slots.get(o);
            if (slot.getItem().isEmpty()) {
                slot.set(itemStack);
                slot.setChanged();
                break;
            }
        }
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType actionType, Player player) {
        System.out.println("clicked "+button+" index "+slotIndex +" action: "+actionType);
        //return;
        try {

            if (actionType != ClickType.QUICK_CRAFT && button == 1) {
                Compat.set_carried(player, this, ItemStack.EMPTY);
            }

            if (slotIndex >= 0 && slotIndex < 90) {
                Slot slot = this.slots.get(slotIndex);
                if (actionType == ClickType.QUICK_CRAFT) {
                    if (slotIndex < this.inventory.getContainerSize()) {
                        ItemStack itemStack = Compat.get_carried(player, this);
                        slot.set(itemStack);
                        PaletteItem.setInventory(palette, this.inventory,player.level());
                        return;

                    }
                }
                if (slot != null) {
                    //slot.hasStack()
                    if (actionType == ClickType.CLONE) {
                        ItemStack itemStack = slot.getItem();
                        ItemStack itemStack2 = itemStack.copy();
                        if (can_pickup(itemStack2)) {
                            itemStack2.setCount(1);
                            Compat.set_carried(player, this, itemStack2);
                        }
                    }
                    //System.out.println("itemStack2: "+itemStack);
                    if (button == 1) {
                        if (slotIndex < 54) {
                            if (actionType == ClickType.PICKUP) {
                                ItemStack itemStack = slot.getItem();
                                if (!itemStack.isEmpty()) {
                                    //System.out.println("empty");
                                    slot.set(ItemStack.EMPTY);
                                    slot.setChanged();
                                }
                            }
                        }
                        PaletteItem.setInventory(palette, this.inventory,player.level());
                        return;
                    }
                    if (button == 0) {
                        if (slotIndex < 54) {
                            if (actionType == ClickType.PICKUP || actionType == ClickType.QUICK_CRAFT) {

                                ItemStack itemStack = slot.getItem();
                                if (!itemStack.isEmpty()) {
                                    //System.out.println("empty");
                                    //slot.set(ItemStack.EMPTY);
                                    ItemStack itemStack2 = itemStack.copy();
                                    if (can_pickup(itemStack2)) {
                                        itemStack2.setCount(1);
                                        Compat.set_carried(player, this, itemStack2);
                                    }
                                } else {
                                    itemStack = Compat.get_carried(player, this);
                                    if (!itemStack.isEmpty()) {
                                        slot.set(itemStack);
                                        Compat.set_carried(player, this, ItemStack.EMPTY);
                                    }
                                    //this.setCarried(ItemStack.EMPTY);
                                }
                                slot.setChanged();
                            }
                        } else {
                            if (actionType == ClickType.PICKUP || actionType == ClickType.QUICK_MOVE) {
                                ItemStack itemStack = slot.getItem();
                                ItemStack itemStack2 = itemStack.copy();
                                if (can_pickup(itemStack2)) {
                                    itemStack2.setCount(1);
                                    if (actionType == ClickType.PICKUP) {
                                        Compat.set_carried(player, this, itemStack2);
                                    } else {
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
            PaletteItem.setInventory(palette, this.inventory,player.level());
        } catch (Exception var8) {
            CrashReport crashReport = CrashReport.forThrowable(var8, "Container click");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Click info");
            crashReportCategory.setDetail("Menu Type", () -> {
                return this.getType() != null ? BuiltInRegistries.MENU.getKey(this.getType()).toString() : "<no type>";
            });
            crashReportCategory.setDetail("Menu Class", () -> {
                return this.getClass().getCanonicalName();
            });
            crashReportCategory.setDetail("Slot Count", (Object) this.slots.size());
            crashReportCategory.setDetail("Slot", (Object) slotIndex);
            crashReportCategory.setDetail("Button", (Object) button);
            crashReportCategory.setDetail("Type", (Object) actionType);
            throw new ReportedException(crashReport);
        }
        return;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

}
