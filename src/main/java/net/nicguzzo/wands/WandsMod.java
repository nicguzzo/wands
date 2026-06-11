package net.nicguzzo.wands;

//?if >= 1.21.11{
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
//?}
import net.nicguzzo.wands.compat.RcId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
//? if >= 1.20.5 {
//?}
import net.minecraft.world.level.Level;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.items.PaletteItem;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.networking.Networking;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.wand.PlayerWand;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandProps;

import java.util.Objects;

public class WandsMod {
    public static int platform = -1; // 0=forge; 1=fabric; 2=quilt
    public static WandsConfig config = null;
    public static final String MOD_ID = "wands";
    public static final Logger LOGGER = LogManager.getLogger();

    public static boolean has_opac = false;
    public static boolean has_ftbchunks = false;
    public static boolean has_flan = false;
    public static boolean has_goml = false;

    static public String wooden_wand_str = "wooden_wand";
    static public String stone_wand_str = "stone_wand";
    static public String iron_wand_str = "iron_wand";
    static public String copper_wand_str = "copper_wand";
    static public String gold_wand_str = "gold_wand";
    static public String diamond_wand_str = "diamond_wand";
    static public String netherite_wand_str = "netherite_wand";
    static public String creative_wand_str = "creative_wand";
    static public String palette_str = "palette";
    static public String magic_bag_1_str = "magic_bag_1";
    static public String magic_bag_2_str = "magic_bag_2";
    static public String magic_bag_3_str = "magic_bag_3";

    //?if fabric || >= 1.21.11{
    static public RcId wooden_wand_id = RcId.fromNamespaceAndPath(MOD_ID,wooden_wand_str);
    static public RcId stone_wand_id = RcId.fromNamespaceAndPath(MOD_ID,stone_wand_str);
    static public RcId iron_wand_id = RcId.fromNamespaceAndPath(MOD_ID,iron_wand_str);
    static public RcId copper_wand_id = RcId.fromNamespaceAndPath(MOD_ID,copper_wand_str);
    static public RcId gold_wand_id = RcId.fromNamespaceAndPath(MOD_ID,gold_wand_str);
    static public RcId diamond_wand_id = RcId.fromNamespaceAndPath(MOD_ID,diamond_wand_str);
    static public RcId netherite_wand_id = RcId.fromNamespaceAndPath(MOD_ID,netherite_wand_str);
    static public RcId creative_wand_id = RcId.fromNamespaceAndPath(MOD_ID,creative_wand_str);
    static public RcId palette_id = RcId.fromNamespaceAndPath(MOD_ID,palette_str);
    static public RcId magic_bag_1_id = RcId.fromNamespaceAndPath(MOD_ID,magic_bag_1_str);
    static public RcId magic_bag_2_id = RcId.fromNamespaceAndPath(MOD_ID,magic_bag_2_str);
    static public RcId magic_bag_3_id = RcId.fromNamespaceAndPath(MOD_ID,magic_bag_3_str);
    //?}
    //?if >= 1.21.11{
    static public ResourceKey<Item> wooden_wand_key = ResourceKey.create(Registries.ITEM, wooden_wand_id.id());
    static public ResourceKey<Item> stone_wand_key = ResourceKey.create(Registries.ITEM, stone_wand_id.id());
    static public ResourceKey<Item> copper_wand_key = ResourceKey.create(Registries.ITEM, copper_wand_id.id());
    static public ResourceKey<Item> iron_wand_key = ResourceKey.create(Registries.ITEM, iron_wand_id.id());
    static public ResourceKey<Item> gold_wand_key = ResourceKey.create(Registries.ITEM, gold_wand_id.id());
    static public ResourceKey<Item> diamond_wand_key = ResourceKey.create(Registries.ITEM, diamond_wand_id.id());
    static public ResourceKey<Item> netherite_wand_key = ResourceKey.create(Registries.ITEM, netherite_wand_id.id());
    static public ResourceKey<Item> creative_wand_key = ResourceKey.create(Registries.ITEM, creative_wand_id.id());
    static public ResourceKey<Item> palette_key = ResourceKey.create(Registries.ITEM, palette_id.id());
    static public ResourceKey<Item> magic_bag_1_key = ResourceKey.create(Registries.ITEM, magic_bag_1_id.id());
    static public ResourceKey<Item> magic_bag_2_key = ResourceKey.create(Registries.ITEM, magic_bag_2_id.id());
    static public ResourceKey<Item> magic_bag_3_key = ResourceKey.create(Registries.ITEM, magic_bag_3_id.id());
    //?}

    public enum WandKeys {
        MENU, MODE, ACTION, ORIENTATION, UNDO, INVERT, FILL, ROTATE, CONF, M_INC, M_DEC, N_INC, N_DEC, TOGGLE_STAIRSLAB, DIAGONAL_SPREAD, INC_SEL_BLK, PALETTE_MODE, PALETTE_MENU, CLEAR, PIN, CYCLE_PALETTE
    }

    public static boolean is_forge = false;
    public static boolean is_neoforge = false;
    public static boolean is_fabric = false;

    public static void init() {
        config=WandsConfig.get_instance();

        Networking.RegisterC2S();

    }
    static public void onPlayerJoin(ServerPlayer player){
        Wand wand = null;
        wand = PlayerWand.get(player);
        if (wand == null) {
            PlayerWand.add_player(player);
            wand = PlayerWand.get(player);
        }
        if (!Compat.player_level(player).isClientSide()) {
            if (WandsMod.config != null) {
                Networking.SendConfPacket(player,WandsMod.config.blocks_per_xp, WandsMod.config.destroy_in_survival_drop, WandsMod.config.survival_unenchanted_drops, WandsMod.config.mend_tools);
            }
            Networking.SendPlayerData(player,wand.player_data);
        }
    }
    static public void onPlayerQuit(ServerPlayer player) {
        PlayerWand.remove_player(player);
    }

    public static void send_state(ServerPlayer player, Wand wand) {
        if (wand != null && player != null && !Compat.player_level(player).isClientSide()) {
            ItemStack wand_stack = player.getMainHandItem();
            if (wand_stack.getItem() instanceof WandItem) {
                WandProps.Mode mode = WandProps.getMode(wand_stack);
                int slot = 0;
                if (wand.palette.palette_slots.size() != 0) {
                    slot = (wand.palette.slot + 1) % wand.palette.palette_slots.size();
                }
                float BLOCKS_PER_XP = WandsMod.config.blocks_per_xp;
                Networking.SendStatePacket(player,mode.ordinal(), slot, BLOCKS_PER_XP != 0, player.experienceLevel, player.experienceProgress);
            }
        }
    }

    public static void process_palette(Player player, boolean mode, boolean rotate,int grad_h) {

        ItemStack item_stack = player.getMainHandItem();
        ItemStack palette = null;
        if (!item_stack.isEmpty() && item_stack.getItem() instanceof PaletteItem) {
            palette = item_stack;
        } else {
            ItemStack offhand_stack = player.getOffhandItem();
            if (!offhand_stack.isEmpty() && offhand_stack.getItem() instanceof PaletteItem) {
                palette = offhand_stack;
            }
        }
        if (palette != null) {
            if (mode) {
                PaletteItem.nextMode(palette);
            }
            if (rotate) {
                PaletteItem.toggleRotate(palette);
            }
            if(grad_h>0){
                PaletteItem.setGradientHeight(palette,grad_h);
            }
        }
    }

    public static void process_keys(Player player, int key, boolean shift, boolean alt) {
        // Handle CYCLE_PALETTE regardless of mainhand item
        if (key >= 0 && key < WandKeys.values().length && WandKeys.values()[key] == WandKeys.CYCLE_PALETTE) {
            Wand wand = PlayerWand.get(player);
            if (wand != null) {
                net.nicguzzo.wands.wand.InventoryManager.cyclePalette(player, wand);
            }
            return;
        }

        ItemStack main_stack = player.getMainHandItem();
        ItemStack offhand_stack = player.getOffhandItem();
        boolean is_wand = main_stack.getItem() instanceof WandItem;
        boolean is_palette = main_stack.getItem() instanceof PaletteItem || offhand_stack.getItem() instanceof PaletteItem;
        boolean creative = Compat.is_creative(player);
        if (is_palette) {
            if (key >= 0 && key < WandKeys.values().length) {
                switch (WandKeys.values()[key]) {
                    case PALETTE_MENU: {
                        PaletteItem.openPaletteMenu(player);
                    }
                    break;
                    case PALETTE_MODE: {
                        if (!offhand_stack.isEmpty() && offhand_stack.getItem() instanceof PaletteItem) {
                            PaletteItem.nextMode(offhand_stack);
                            if (!WandsMod.config.disable_info_messages) {
                                Compat.displayClientMessage(player,PaletteItem.getModeName(offhand_stack), true);
                            }
                        }
                    }
                    break;
                }
            }
        }
        if (is_wand) {
            Wand wand = PlayerWand.get(player);
            WandItem wand_item = (WandItem) main_stack.getItem();
            if (wand == null) return;
            WandProps.Mode mode = WandProps.getMode(main_stack);
            int inc = (shift ? 10 : 1);
            if (key >= 0 && key < WandKeys.values().length) {
                switch (WandKeys.values()[key]) {
                    case INC_SEL_BLK:
                        if (WandProps.flagAppliesTo(WandProps.Flag.INCSELBLOCK, mode)) {
                            WandProps.toggleFlag(main_stack, WandProps.Flag.INCSELBLOCK);
                            if (!WandsMod.config.disable_info_messages) {
                                Compat.displayClientMessage(player,Compat.translatable("screen.wands.include_block").append(Compat.literal(": " + (WandProps.getFlag(main_stack, WandProps.Flag.INCSELBLOCK) ? "On" : "Off"))), true);
                            }
                        }
                        break;
                    case DIAGONAL_SPREAD:
                        if (WandProps.flagAppliesTo(WandProps.Flag.DIAGSPREAD, mode)) {
                            WandProps.toggleFlag(main_stack, WandProps.Flag.DIAGSPREAD);
                            if (!WandsMod.config.disable_info_messages) {
                                Compat.displayClientMessage(player,Compat.translatable("screen.wands.diagonal_spread").append(Compat.literal(": " + (WandProps.getFlag(main_stack, WandProps.Flag.DIAGSPREAD) ? "On" : "Off"))), true);
                            }
                        }
                        break;
                    case TOGGLE_STAIRSLAB:
                        WandProps.setStateMode(main_stack, WandProps.StateMode.APPLY);
                        WandProps.toggleFlag(main_stack, WandProps.Flag.STAIRSLAB);
                        if (!WandsMod.config.disable_info_messages) {
                            Compat.displayClientMessage(player,Compat.translatable("screen.wands.slab_flip").append(Compat.literal(": " + (WandProps.getFlag(main_stack, WandProps.Flag.STAIRSLAB) ? "On" : "Off"))), true);
                        }
                        break;
                    case N_INC:
                        if (mode == WandProps.Mode.GRID) {
                            WandProps.incGrid(main_stack, WandProps.Value.GRIDN, inc, wand_item.limit);
                            if (!WandsMod.config.disable_info_messages) {
                                Compat.displayClientMessage(player,Compat.translatable("screen.wands.grid_n").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.GRIDN))), true);
                            }
                        }
                        break;
                    case N_DEC:
                        if (mode == WandProps.Mode.GRID) {
                            WandProps.decGrid(main_stack, WandProps.Value.GRIDN, inc, wand_item.limit);
                            if (!WandsMod.config.disable_info_messages) {
                                Compat.displayClientMessage(player,Compat.translatable("screen.wands.grid_n").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.GRIDN))), true);
                            }
                        }
                        break;
                    case M_INC:
                        switch (mode) {
                            case DIRECTION:
                                WandProps.incVal(main_stack, WandProps.Value.MULTIPLIER, inc);
                                if (!WandsMod.config.disable_info_messages) {
                                    Compat.displayClientMessage(player,Compat.translatable("screen.wands.multiplier").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.MULTIPLIER))), true);
                                }
                                break;
                            case ROW_COL:
                                WandProps.incVal(main_stack, WandProps.Value.ROWCOLLIM, inc);
                                if (!WandsMod.config.disable_info_messages) {
                                    Compat.displayClientMessage(player,Compat.translatable("screen.wands.limit").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.ROWCOLLIM))), true);
                                }
                                break;
                            case GRID:
                                WandProps.incGrid(main_stack, WandProps.Value.GRIDM, inc, wand_item.limit);
                                if (!WandsMod.config.disable_info_messages) {
                                    Compat.displayClientMessage(player,Compat.translatable("screen.wands.grid_m").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.GRIDM))), true);
                                }
                                break;
                            case AREA:
                                WandProps.incVal(main_stack, WandProps.Value.AREALIM, inc);
                                if (!WandsMod.config.disable_info_messages) {
                                    Compat.displayClientMessage(player,Compat.translatable("screen.wands.limit").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.AREALIM))), true);
                                }
                                break;
                        }
                        break;
                    case M_DEC:
                        switch (mode) {
                            case DIRECTION:
                                WandProps.decVal(main_stack, WandProps.Value.MULTIPLIER, inc);
                                if (!WandsMod.config.disable_info_messages) {
                                    Compat.displayClientMessage(player,Compat.translatable("screen.wands.multiplier").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.MULTIPLIER))), true);
                                }
                                break;
                            case ROW_COL:
                                WandProps.decVal(main_stack, WandProps.Value.ROWCOLLIM, inc);
                                if (!WandsMod.config.disable_info_messages) {
                                    Compat.displayClientMessage(player,Compat.translatable("screen.wands.limit").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.ROWCOLLIM))), true);
                                }
                                break;
                            case GRID:
                                WandProps.decGrid(main_stack, WandProps.Value.GRIDM, inc, wand_item.limit);
                                if (!WandsMod.config.disable_info_messages) {
                                    Compat.displayClientMessage(player,Compat.translatable("screen.wands.grid_m").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.GRIDM))), true);
                                }
                                break;
                            case AREA:
                                WandProps.decVal(main_stack, WandProps.Value.AREALIM, inc);
                                if (!WandsMod.config.disable_info_messages) {
                                    Compat.displayClientMessage(player,Compat.translatable("screen.wands.limit").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.AREALIM))), true);
                                }
                                break;
                        }
                        break;
                    case ACTION:
                        if (WandProps.hasMultipleActions(mode)) {
                            if (shift) {
                                WandProps.prevAction(main_stack, mode);
                            } else {
                                WandProps.nextAction(main_stack, mode);
                            }
                            if (!WandsMod.config.disable_info_messages) {
                                Compat.displayClientMessage(player,Compat.translatable(WandProps.getAction(main_stack).toString()).append(" ").append(Compat.translatable(WandProps.getMode(main_stack).toString())), true);
                            }
                        }
                        break;
                    case MENU:
                        Compat.open_menu((ServerPlayer) player, main_stack, 0);
                        break;
                    case MODE:
                        if (shift) {
                            WandProps.prevMode(main_stack, wand_item.can_blast);
                        } else {
                            WandProps.nextMode(main_stack, wand_item.can_blast);
                        }
                        if (!WandsMod.config.disable_info_messages) {
                            WandProps.Mode newMode = WandProps.getMode(main_stack);
                            if (WandProps.hasMultipleActions(newMode)) {
                                Compat.displayClientMessage(player,Compat.translatable(WandProps.getAction(main_stack).toString()).append(" ").append(Compat.translatable(newMode.toString())), true);
                            } else {
                                Compat.displayClientMessage(player,Compat.translatable(newMode.toString()), true);
                            }
                        }
                        break;
                    case ORIENTATION:
                        switch (mode) {
                            case CIRCLE:
                            case FILL:
                                WandProps.nextPlane(main_stack);
                                if (!WandsMod.config.disable_info_messages) {
                                    Compat.displayClientMessage(player,Compat.translatable("screen.wands.plane").append(Compat.literal(": " + WandProps.getPlane(main_stack))), true);
                                }
                                send_state((ServerPlayer) player, wand);
                                break;
                            case ROW_COL:
                                WandProps.nextOrientation(main_stack);
                                if (!WandsMod.config.disable_info_messages) {
                                    Compat.displayClientMessage(player,Compat.translatable(WandProps.getOrientation(main_stack).toString()), true);
                                }
                                break;
                        }
                        break;
                    case INVERT:
                        WandProps.Flag invertFlag = (mode == WandProps.Mode.BOX) ? WandProps.Flag.BOX_INVERTED : WandProps.Flag.INVERTED;
                        if (WandProps.flagAppliesTo(invertFlag, mode)) {
                            WandProps.toggleFlag(main_stack, invertFlag);
                            if (!WandsMod.config.disable_info_messages) {
                                Compat.displayClientMessage(player,Compat.translatable("screen.wands.invert").append(Compat.literal(": " + (WandProps.getFlag(main_stack, invertFlag) ? "On" : "Off"))), true);
                            }
                        }
                        break;
                    case FILL:
                        switch (wand.mode) {
                            case FILL: {
                                WandProps.toggleFlag(main_stack, WandProps.Flag.RFILLED);
                                if (!WandsMod.config.disable_info_messages) {
                                    Compat.displayClientMessage(player,Compat.translatable("screen.wands.filled").append(Compat.literal(": " + (WandProps.getFlag(main_stack, WandProps.Flag.RFILLED) ? "On" : "Off"))), true);
                                }
                            }
                            break;
                            case CIRCLE: {
                                WandProps.toggleFlag(main_stack, WandProps.Flag.CFILLED);
                                if (!WandsMod.config.disable_info_messages) {
                                    Compat.displayClientMessage(player,Compat.translatable("screen.wands.filled_circle").append(Compat.literal(": " + (WandProps.getFlag(main_stack, WandProps.Flag.CFILLED) ? "On" : "Off"))), true);
                                }
                            }
                            break;
                        }
                        break;
                    case ROTATE:
                        if (WandProps.rotationAppliesTo(mode)) {
                            WandProps.nextRotation(main_stack);
                            WandProps.setStateMode(main_stack, WandProps.StateMode.APPLY);
                            if (!WandsMod.config.disable_info_messages) {
                                String rotKey;
                                switch (WandProps.getRotation(main_stack)) {
                                    case CLOCKWISE_90: rotKey = "screen.wands.rot_90"; break;
                                    case CLOCKWISE_180: rotKey = "screen.wands.rot_180"; break;
                                    case COUNTERCLOCKWISE_90: rotKey = "screen.wands.rot_270"; break;
                                    default: rotKey = "screen.wands.rot_0"; break;
                                }
                                Compat.displayClientMessage(player,Compat.translatable(rotKey), true);
                            }
                        }
                        break;
                    case UNDO:
                        if (creative && !Compat.player_level(player).isClientSide()) {
                            if (wand != null) {
                                int n = 1;
                                if (alt) {
                                    n = 10;
                                }
                                Level undoLevel = Compat.player_level(player);
                                if (shift) {
                                    wand.undoManager.redo(n, undoLevel, player);
                                } else {
                                    wand.undoManager.undo(n, undoLevel, player);
                                }
                            }
                        }
                        break;
                    case CLEAR:

                        wand.clear(true);

                        if (player != null && !WandsMod.config.disable_info_messages) {
                            Compat.displayClientMessage(player,Compat.translatable("wands.message.wand_cleared"), true);
                        }
                        break;
                    // PIN is handled client-side only — never sent to server
                }

            }
        }
        if (!main_stack.isEmpty() && main_stack.getItem() instanceof PaletteItem) {
            if (key >= 0 && key < WandKeys.values().length) {
                if (Objects.requireNonNull(WandKeys.values()[key]) == WandKeys.PALETTE_MODE) {
                    PaletteItem.nextMode(main_stack);
                    if (!WandsMod.config.disable_info_messages) {
                        Compat.displayClientMessage(player,PaletteItem.getModeName(main_stack), true);
                    }
                }
            }
        }
        if (is_wand) {
            if (key < 0) {
                Wand wand = null;
                if (!Compat.player_level(player).isClientSide()) {
                    wand = PlayerWand.get(player);
                    if (wand == null) {
                        PlayerWand.add_player(player);
                        wand = PlayerWand.get(player);
                    }
                }
                if (wand != null) {
                    wand.is_shift_pressed = shift;
                }
            }
        }
    }

    public static void log(String s, boolean b) {
        if (b) {
            LOGGER.info(s);
        }
    }

}
