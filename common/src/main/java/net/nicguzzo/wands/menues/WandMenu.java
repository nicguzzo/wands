package net.nicguzzo.wands.menues;

import dev.architectury.networking.NetworkManager;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.networking.Networking;
import net.nicguzzo.wands.wand.PlayerWand;
import net.nicguzzo.wands.wand.Wand;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class WandMenu extends AbstractContainerMenu {

    public ItemStack wand;
    public final Inventory playerInventory;

    public WandMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        this( syncId,playerInventory,Objects.requireNonNull(buf.readNbt()).read(ItemStack.MAP_CODEC).orElse(ItemStack.EMPTY));

		//this( syncId,playerInventory,
        //        ItemStack.parse(
        //               playerInventory.player.level().registryAccess(),
        //                buf.readNbt()).orElse(ItemStack.EMPTY
        //        )
        //);
    }

    public WandMenu(int syncId, Inventory playerInventory, ItemStack _wand) {
        super(WandsMod.WAND_CONTAINER.get(), syncId);
        this.wand=_wand;
        this.playerInventory=playerInventory;

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
    }

    @Override
    public boolean stillValid(Player player) {
        return wand.getItem() instanceof WandItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType actionType, Player player)
    {
        try {
            if(player.level().isClientSide()) {
                return;
            }
            if(actionType == ClickType.PICKUP && slotIndex>=0 && slotIndex<36 && this.wand!=null){
                Wand wnd= PlayerWand.get(player);
                if(wnd!=null && wnd.player_data!=null){
                    if(button==0) {
                        //ListTag tools;
                        IntArrayTag tools;
                        if(wnd.player_data.get("Tools")==null){
                            int[] i ={};
                            wnd.player_data.putIntArray("Tools",i);
                        }
                        if(wnd.player_data.get("Tools")!=null) {
                            Optional<int[]> a = wnd.player_data.getIntArray("Tools");
                            if (a.isPresent()) {
                                tools = new IntArrayTag(a.get());
                                //WandsMod.LOGGER.info("tools "+tools.toString());
                                IntTag it = IntTag.valueOf(slotIndex);
                                IntStream oi= Arrays.stream(a.get()).filter(aa-> aa==it.value());
                                OptionalInt tool_slot=oi.findFirst();
                                if(tool_slot.isPresent()) {
                                    int idx=ArrayUtils.indexOf(a.get(),tool_slot.getAsInt());
                                    //WandsMod.LOGGER.info("index "+idx);
                                    if(idx>=0 && idx< tools.size()) {
                                        tools.remove(idx);
                                    }
                                } else {
                                    tools.addTag(tools.size(),it);
                                    //tools.addLast(it);
                                }
                                wnd.player_data.put("Tools", tools);
                            }
                        }
                    }
                    if(button==1) {
                        //ListTag tools=wnd.player_data.getList("Tools",Tag.TAG_INT);
                        //tools.clear();
                        //wnd.player_data.put("Tools",tools);
                        //wnd.player_data.remove("Tools");
                    }
                    //System.out.println("player data  "+wnd.player_data.toString());
                    NetworkManager.sendToPlayer((ServerPlayer) player,new Networking.PlayerDataPacket(wnd.player_data));
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
    }
}
