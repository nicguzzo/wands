package net.nicguzzo.wands.menues;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.items.BlueprintItem;
import net.nicguzzo.wands.items.BlueprintItem.BlockGroup;
//?if fabric {
import net.nicguzzo.wands.loaders.fabric.FabricEntrypoint;
//?}
//?if neoforge {
/*import static net.nicguzzo.wands.loaders.neoforge.NeoforgeEntrypoint.BLUEPRINT_MENU_TYPE;
*///?}
//?if forge {
/*import static net.nicguzzo.wands.loaders.forge.ForgeEntrypoint.BLUEPRINT_MENU_TYPE;
*///?}

import java.util.List;

public class BlueprintMenu extends AbstractContainerMenu {

    public static final int VISIBLE_ROWS = 4;
    public ItemStack blueprint;
    public final Inventory playerInventory;
    public final SimpleContainer replacementInventory = new SimpleContainer(VISIBLE_ROWS);
    public int rowCount = 0;
    public int scrollOffset = 0;

//?if neoforge {
    /*//? if >= 1.21 {
    public BlueprintMenu(int containerId, Inventory playerInventory, net.minecraft.network.RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, Compat.readItemStackFromBuf(extraData, playerInventory));
    }
    //?} else {
    /^public BlueprintMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, Compat.readItemStackFromBuf(extraData, playerInventory));
    }
    ^///?}
    public BlueprintMenu(int containerId, Inventory playerInventory, ItemStack _blueprint) {
        super(BLUEPRINT_MENU_TYPE.get(), containerId);
        this.blueprint = _blueprint;
*///?} else {
    //?if forge {
    /*public BlueprintMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, buf.readItem());
    }
    public BlueprintMenu(int containerId, Inventory playerInventory, ItemStack _blueprint) {
        super(BLUEPRINT_MENU_TYPE.get(), containerId);
        this.blueprint = _blueprint;
    *///?} else {
        //?if > 1.21 {
        public BlueprintMenu(int containerId, Inventory playerInventory, BlueprintMenuData extraData) {
            super(FabricEntrypoint.BLUEPRINT_MENU_TYPE, containerId);
            this.blueprint = extraData.item();
        //?} else {
            /*public BlueprintMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
                this(syncId, playerInventory, buf.readItem());
            }
            public BlueprintMenu(int syncId, Inventory playerInventory, ItemStack _blueprint) {
                super(FabricEntrypoint.BLUEPRINT_MENU_TYPE, syncId);
                this.blueprint = _blueprint;
        *///?}
    //?}
//?}
        this.playerInventory = playerInventory;
        updateRowCount(0);

        // Replacement ghost slots (slots 0..3) aligned with the 4 visible rows in BlueprintScreen
        for (int r = 0; r < VISIBLE_ROWS; r++) {
            final int row = r;
            this.addSlot(new Slot(this.replacementInventory, r, 145, 28 + r * 20) {
                @Override
                public boolean isActive() {
                    return row < BlueprintMenu.this.rowCount;
                }
            });
        }

        int o;
        int n;
        // Player inventory (slots 4..30, inventory indices 9..35)
        for (o = 0; o < 3; ++o) {
            for (n = 0; n < 9; ++n) {
                this.addSlot(new Slot(playerInventory, n + o * 9 + 9, 8 + n * 18, 128 + o * 18));
            }
        }
        // Hotbar (slots 31..39, inventory indices 0..8)
        for (o = 0; o < 9; ++o) {
            this.addSlot(new Slot(playerInventory, o, 8 + o * 18, 186));
        }
    }

    public void updateRowCount(int scrollOffset) {
        this.scrollOffset = scrollOffset;
        if (this.blueprint != null && this.blueprint.getItem() instanceof BlueprintItem) {
            List<BlockGroup> groups = BlueprintItem.getBlockGroups(this.blueprint);
            this.rowCount = Math.min(VISIBLE_ROWS, Math.max(0, groups.size() - scrollOffset));
            for (int r = 0; r < VISIBLE_ROWS; r++) {
                int idx = scrollOffset + r;
                if (idx < groups.size()) {
                    BlockGroup g = groups.get(idx);
                    if (g.replacementBlock != null && g.replacementBlock != Blocks.AIR && g.replacementBlock != g.originalBlock) {
                        this.replacementInventory.setItem(r, new ItemStack(g.replacementBlock.asItem()));
                    } else {
                        this.replacementInventory.setItem(r, ItemStack.EMPTY);
                    }
                } else {
                    this.replacementInventory.setItem(r, ItemStack.EMPTY);
                }
            }
        } else {
            this.rowCount = 0;
        }
    }

    @Override
    public void slotsChanged(Container inventory) {
        super.slotsChanged(inventory);
        this.broadcastChanges();
    }

    @Override
    public void removed(Player player) {
        Compat.set_carried(player, this, ItemStack.EMPTY);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blueprint != null && this.blueprint.getItem() instanceof BlueprintItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    boolean can_pickup(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return false;
        Item item = itemStack.getItem();
        Block blk = Block.byItem(item);
        return blk != null && blk != Blocks.AIR;
    }

    void insert(ItemStack itemStack) {
        for (int o = 0; o < VISIBLE_ROWS && o < this.rowCount; ++o) {
            Slot slot = this.slots.get(o);
            if (slot.getItem().isEmpty()) {
                slot.set(itemStack);
                slot.setChanged();
                break;
            }
        }
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput actionType, Player player) {
        try {
            if (actionType != ContainerInput.QUICK_CRAFT && button == 1) {
                Compat.set_carried(player, this, ItemStack.EMPTY);
            }

            if (slotIndex >= 0 && slotIndex < this.slots.size()) {
                Slot slot = this.slots.get(slotIndex);
                if (actionType == ContainerInput.QUICK_CRAFT) {
                    if (slotIndex < VISIBLE_ROWS && slot.isActive()) {
                        ItemStack itemStack = Compat.get_carried(player, this);
                        if (can_pickup(itemStack)) {
                            ItemStack copy = itemStack.copy();
                            copy.setCount(1);
                            slot.set(copy);
                            slot.setChanged();
                        }
                        return;
                    }
                }
                if (slot != null && slot.isActive()) {
                    if (actionType == ContainerInput.CLONE) {
                        ItemStack itemStack = slot.getItem();
                        ItemStack itemStack2 = itemStack.copy();
                        if (can_pickup(itemStack2)) {
                            itemStack2.setCount(1);
                            Compat.set_carried(player, this, itemStack2);
                        }
                    }
                    if (button == 1) {
                        if (slotIndex < VISIBLE_ROWS) {
                            if (actionType == ContainerInput.PICKUP) {
                                ItemStack itemStack = slot.getItem();
                                if (!itemStack.isEmpty()) {
                                    slot.set(ItemStack.EMPTY);
                                    slot.setChanged();
                                }
                            }
                        }
                        return;
                    }
                    if (button == 0) {
                        if (slotIndex < VISIBLE_ROWS) {
                            if (actionType == ContainerInput.PICKUP || actionType == ContainerInput.QUICK_CRAFT) {
                                ItemStack carried = Compat.get_carried(player, this);
                                if (!carried.isEmpty()) {
                                    if (can_pickup(carried)) {
                                        ItemStack copy = carried.copy();
                                        copy.setCount(1);
                                        slot.set(copy);
                                        Compat.set_carried(player, this, ItemStack.EMPTY);
                                    }
                                } else {
                                    ItemStack existing = slot.getItem();
                                    if (!existing.isEmpty()) {
                                        ItemStack copy = existing.copy();
                                        copy.setCount(1);
                                        Compat.set_carried(player, this, copy);
                                        slot.set(ItemStack.EMPTY);
                                    }
                                }
                                slot.setChanged();
                            }
                        } else {
                            if (actionType == ContainerInput.PICKUP || actionType == ContainerInput.QUICK_MOVE) {
                                ItemStack itemStack = slot.getItem();
                                ItemStack itemStack2 = itemStack.copy();
                                if (can_pickup(itemStack2)) {
                                    itemStack2.setCount(1);
                                    if (actionType == ContainerInput.PICKUP) {
                                        Compat.set_carried(player, this, itemStack2);
                                    } else {
                                        insert(itemStack2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            WandsMod.LOGGER.error(e.getMessage());
        }
    }
}
