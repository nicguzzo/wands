package net.nicguzzo.wands.menues;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
//?if >= 1.21.11 {
import net.minecraft.world.inventory.ContainerInput;
//?} else {
/*import net.minecraft.world.inventory.ClickType;
*///?}
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.items.BlueprintItem;
//?if fabric {
import net.nicguzzo.wands.loaders.fabric.FabricEntrypoint;
//?}
//?if neoforge {
/*import static net.nicguzzo.wands.loaders.neoforge.NeoforgeEntrypoint.BLUEPRINT_MENU_TYPE;
*///?}
//?if forge {
/*import static net.nicguzzo.wands.loaders.forge.ForgeEntrypoint.BLUEPRINT_MENU_TYPE;
*///?}

public class BlueprintMenu extends AbstractContainerMenu {

    public ItemStack blueprint;
    public final Inventory playerInventory;

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

        int o;
        int n;
        // Player inventory (slots 0..26, inventory indices 9..35)
        for (o = 0; o < 3; ++o) {
            for (n = 0; n < 9; ++n) {
                this.addSlot(new Slot(playerInventory, n + o * 9 + 9, 8 + n * 18, 128 + o * 18));
            }
        }
        // Hotbar (slots 27..35, inventory indices 0..8)
        for (o = 0; o < 9; ++o) {
            this.addSlot(new Slot(playerInventory, o, 8 + o * 18, 186));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blueprint != null && this.blueprint.getItem() instanceof BlueprintItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    //?if >= 1.21.11 {
    @Override
    public void clicked(int slotIndex, int button, ContainerInput actionType, Player player) {
        try {
            if (slotIndex >= 0 && slotIndex < 36) {
                Slot slot = this.slots.get(slotIndex);
                if (slot != null && slot.getItem() == this.blueprint) {
                    return;
                }
            }
            if (actionType != ContainerInput.QUICK_MOVE) {
                super.clicked(slotIndex, button, actionType, player);
            }
        } catch (Exception e) {
            WandsMod.LOGGER.error(e.getMessage());
        }
    }
    //?} else {
    /*@Override
    public void clicked(int slotIndex, int button, ClickType actionType, Player player) {
        try {
            if (slotIndex >= 0 && slotIndex < 36) {
                Slot slot = this.slots.get(slotIndex);
                if (slot != null && slot.getItem() == this.blueprint) {
                    return;
                }
            }
            if (actionType != ClickType.QUICK_MOVE) {
                super.clicked(slotIndex, button, actionType, player);
            }
        } catch (Exception e) {
            WandsMod.LOGGER.error(e.getMessage());
        }
    }
    *///?}
}
