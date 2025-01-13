package net.nicguzzo.wands.menues;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
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
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.wand.PlayerWand;
import net.nicguzzo.wands.wand.Wand;

public class WandMenu extends AbstractContainerMenu {

    public ItemStack wand;
    public final Inventory playerInventory;
    private final SimpleContainer simplecontainer;

    class WandSlot extends Slot {
        public WandSlot(Container container, int i, int j, int k) {
            super(container, i, j, k);
        }

        @Override
        public boolean mayPlace(ItemStack itemStack) {
            return (itemStack.getItem() instanceof DiggerItem || itemStack.getItem() instanceof ShearsItem) && !(itemStack.getItem() instanceof WandItem);
        }
    }

    public WandMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(syncId, playerInventory, buf.readItem());
    }

    public WandMenu(int syncId, Inventory playerInventory, ItemStack _wand) {
        super(WandsMod.WAND_CONTAINER.get(), syncId);
        this.wand = _wand;
        this.playerInventory = playerInventory;
        ListTag tag = wand.getOrCreateTag().getList("Tools", Compat.NbtType.COMPOUND);
        this.simplecontainer = new SimpleContainer(9) {
            @Override
            public void setChanged() {
                wand.getOrCreateTag().put("Tools", toTag(this));
                super.setChanged();
            }
        };
        fromTag(tag, simplecontainer);
        Wand wnd = PlayerWand.get(playerInventory.player);
        if (wnd.player_data != null) {
            ClientRender.wand.player_data = wnd.player_data;
        }
        int o;
        int n;
        int k = 0;
        for (o = 0; o < 9; ++o) {
            this.addSlot(new Slot(playerInventory, k++, 8 + o * 18, 142));
        }
        for (o = 0; o < 3; ++o) {
            for (n = 0; n < 9; ++n) {
                this.addSlot(new Slot(playerInventory, k++, 8 + n * 18, 84 + o * 18));
            }
        }
        for (o = 0; o < 9; ++o) {
            this.addSlot(new WandSlot(simplecontainer, o, 8 + o * 18, 32));
        }

    }

    @Override
    public boolean stillValid(Player player) {
        return wand.getItem() instanceof WandItem;
    }

    boolean mayPlace(ItemStack itemStack) {
        return (itemStack.getItem() instanceof DiggerItem || itemStack.getItem() instanceof ShearsItem) && !(itemStack.getItem() instanceof WandItem);
    }

    boolean insert(ItemStack itemStack, int s, int e) {

        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        return ItemStack.EMPTY;
    }

    public static ListTag toTag(SimpleContainer inventory) {
        ListTag list = new ListTag();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            CompoundTag stackTag = new CompoundTag();
            stackTag.putInt("Slot", i);
            ItemStack tool = inventory.getItem(i);
            if (!tool.isEmpty()) {
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

    @Override
    public ItemStack clicked(int slotIndex, int button, ClickType actionType, Player player) {
        System.out.println("clicked " + button + " index " + slotIndex + " action: " + actionType);
        //return;
        try {
            if (player.level.isClientSide()) {
                Wand wnd = PlayerWand.get(player);
                System.out.println("player data  " + wnd.player_data.toString());
                if (ClientRender.wand != null) {
                    ClientRender.wand.player_data = wnd.player_data;
                }
                return ItemStack.EMPTY;
            }
            if (actionType == ClickType.PICKUP && slotIndex >= 36 && slotIndex < 45 && this.wand != null) {
                int n_items = 0;
                for (int i = 0; i < 9; i++) {
                    if (!simplecontainer.getItem(i).isEmpty()) {
                        n_items++;
                    }
                }
                System.out.println("invnetory size  " + player.inventory.items.size());
                int inv_free = 0;
                for (int i = 0; i < player.inventory.items.size(); i++) {
                    if (player.inventory.getItem(i).isEmpty()) {
                        inv_free++;
                    }
                }
                System.out.println("invnetory size left  " + inv_free);
                if (n_items <= inv_free) {
                    for (int i = 0; i < 9; i++) {
                        if (!simplecontainer.getItem(i).isEmpty()) {
                            int free_slot = player.inventory.getFreeSlot();
                            if (free_slot != -1) {
                                player.inventory.setItem(free_slot, simplecontainer.removeItem(i, 1));
                                System.out.println("invnetory moving from " + i + " to " + free_slot);
                                CompoundTag tag = Compat.getTags(wand);
                                tag.remove("Tools");
                                wand.removeTagKey("Tools");
                            }
                        }
                    }
                } else {
                    player.displayClientMessage(Compat.literal("need " + n_items + " free inventory slots to reclaim items"), false);
                }

            }
            if (actionType == ClickType.PICKUP && slotIndex >= 0 && slotIndex < 36 && this.wand != null) {
                Wand wnd = PlayerWand.get(player);
                if (wnd.player_data != null) {
                    if (button == 0) {
                        //ListTag tools;
                        IntArrayTag tools;
                        if (wnd.player_data.get("Tools") == null) {
                            int[] i = {};
                            wnd.player_data.putIntArray("Tools", i);
                        }
                        if (wnd.player_data.get("Tools") != null) {
                            int[] a = wnd.player_data.getIntArray("Tools");
                            tools = new IntArrayTag(a);
                            IntTag it = IntTag.valueOf(slotIndex);
                            if (tools.contains(it)) {
                                tools.remove(it);
                            } else {
                                tools.add(it);
                            }
                            wnd.player_data.put("Tools", tools);
                        }
                    }
                    if (button == 1) {

                        //ListTag tools=wnd.player_data.getList("Tools",Tag.TAG_INT);
                        //tools.clear();
                        //wnd.player_data.put("Tools",tools);
                        //wnd.player_data.remove("Tools");
                    }

                    System.out.println("player data  " + wnd.player_data.toString());
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
            crashReportCategory.setDetail("Slot Count", (Object) this.slots.size());
            crashReportCategory.setDetail("Slot", (Object) slotIndex);
            crashReportCategory.setDetail("Button", (Object) button);
            crashReportCategory.setDetail("Type", (Object) actionType);
            throw new ReportedException(crashReport);
        }

        return ItemStack.EMPTY;

    }
}
