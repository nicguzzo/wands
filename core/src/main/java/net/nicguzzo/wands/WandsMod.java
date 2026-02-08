package net.nicguzzo.wands;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.platform.Platform;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.utils.Env;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
#if MC_VERSION >= 12005
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
#endif
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.items.PaletteItem;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.menues.MagicBagMenu;
import net.nicguzzo.wands.menues.PaletteMenu;
import net.nicguzzo.wands.menues.WandMenu;
import net.nicguzzo.wands.networking.Networking;
import net.nicguzzo.compat.Compat;
import net.nicguzzo.compat.MyIdExt;
import net.nicguzzo.wands.utils.WandUtils;
import net.nicguzzo.wands.wand.PlayerWand;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;
import net.nicguzzo.wands.wand.modes.RockMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.nicguzzo.wands.WandsMod;

import java.util.Objects;

public class WandsMod {
    public static int platform = -1; // 0=forge; 1=fabric; 2=quilt
    public static final WandsConfig config = WandsConfig.get_instance();
    public static final String MOD_ID = "wands";

    public static final Logger LOGGER = LogManager.getLogger();

    public static boolean has_opac = false;
    public static boolean has_ftbchunks = false;
    public static boolean has_flan = false;
    public static boolean has_goml = false;

    static MyIdExt stone_wand = new MyIdExt(MOD_ID,"stone_wand");
    static MyIdExt iron_wand = new MyIdExt(MOD_ID,"iron_wand");
    static MyIdExt copper_wand = new MyIdExt(MOD_ID,"copper_wand");
    static MyIdExt diamond_wand = new MyIdExt(MOD_ID,"diamond_wand");
    static MyIdExt netherite_wand = new MyIdExt(MOD_ID,"netherite_wand");
    static MyIdExt creative_wand = new MyIdExt(MOD_ID,"creative_wand");
    static MyIdExt palette = new MyIdExt(MOD_ID,"palette");
    static MyIdExt magic_bag_1 = new MyIdExt(MOD_ID,"magic_bag_1");
    static MyIdExt magic_bag_2 = new MyIdExt(MOD_ID,"magic_bag_2");
    static MyIdExt magic_bag_3 = new MyIdExt(MOD_ID,"magic_bag_3");

    static ResourceKey<Item> stone_wand_key = ResourceKey.create(Registries.ITEM, stone_wand.res);
    static ResourceKey<Item> copper_wand_key = ResourceKey.create(Registries.ITEM, copper_wand.res);
    static ResourceKey<Item> iron_wand_key = ResourceKey.create(Registries.ITEM, iron_wand.res);
    static ResourceKey<Item> diamond_wand_key = ResourceKey.create(Registries.ITEM, diamond_wand.res);
    static ResourceKey<Item> netherite_wand_key = ResourceKey.create(Registries.ITEM, netherite_wand.res);
    static ResourceKey<Item> creative_wand_key = ResourceKey.create(Registries.ITEM, creative_wand.res);
    static ResourceKey<Item> palette_key = ResourceKey.create(Registries.ITEM, palette.res);
    static ResourceKey<Item> magic_bag_1_key = ResourceKey.create(Registries.ITEM, magic_bag_1.res);
    static ResourceKey<Item> magic_bag_2_key = ResourceKey.create(Registries.ITEM, magic_bag_2.res);
    static ResourceKey<Item> magic_bag_3_key = ResourceKey.create(Registries.ITEM, magic_bag_3.res);

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> WANDS_TAB = TABS.register("wands_tab", () -> CreativeTabRegistry.create(Component.translatable("itemGroup.wands.wands_tab"), () -> new ItemStack(WandsMod.DIAMOND_WAND_ITEM.get())));

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final DeferredRegister<MenuType<?>> MENUES = DeferredRegister.create(MOD_ID, Registries.MENU);

    public static final RegistrySupplier<Item> STONE_WAND_ITEM = ITEMS.register(stone_wand.res, () -> {
        return new WandItem(WandItem.WandTier.STONE_WAND, config.stone_wand_limit, false, false, false, false, new Item.Properties().durability(config.stone_wand_durability)
                .arch$tab(WandsMod.WANDS_TAB)
                #if MC_VERSION >= 12111
                .setId(stone_wand_key)
                #endif
        );
    });
    public static final RegistrySupplier<Item> COPPER_WAND_ITEM = ITEMS.register(copper_wand.res, () -> {
        return new WandItem(WandItem.WandTier.COPPER_WAND, config.copper_wand_limit, false, false, false, false, new Item.Properties().durability(config.copper_wand_durability)
                .arch$tab(WandsMod.WANDS_TAB)
                #if MC_VERSION >= 12111
                .setId(copper_wand_key)
                #endif
        );
    });
    public static final RegistrySupplier<Item> IRON_WAND_ITEM = ITEMS.register(iron_wand.res, () -> {
        return new WandItem(WandItem.WandTier.IRON_WAND, config.iron_wand_limit, false, false, false, false, new Item.Properties().durability(config.iron_wand_durability)
                .arch$tab(WandsMod.WANDS_TAB)
                #if MC_VERSION >= 12111
                .setId(iron_wand_key)
                #endif
                );
    });
    public static final RegistrySupplier<Item> DIAMOND_WAND_ITEM = ITEMS.register(diamond_wand.res, () -> {
        return new WandItem(WandItem.WandTier.DIAMOND_WAND, config.diamond_wand_limit, true, false, false, false, new Item.Properties().durability(config.diamond_wand_durability)
                .arch$tab(WandsMod.WANDS_TAB)
                #if MC_VERSION >= 12111
                .setId(diamond_wand_key)
                #endif
        );
    });
    public static final RegistrySupplier<Item> NETHERITE_WAND_ITEM = ITEMS.register(netherite_wand.res, () -> {
        return new WandItem(WandItem.WandTier.NETHERITE_WAND, config.netherite_wand_limit, true, true, false, true, new Item.Properties().fireResistant().durability(config.netherite_wand_durability)
                .arch$tab(WandsMod.WANDS_TAB)
                #if MC_VERSION >= 12111
                .setId(netherite_wand_key)
                #endif
        );
    });
    public static final RegistrySupplier<Item> CREATIVE_WAND_ITEM = ITEMS.register(creative_wand.res, () -> {
        return new WandItem(WandItem.WandTier.CREATIVE_WAND, config.creative_wand_limit, true, true, true, true, new Item.Properties().fireResistant().stacksTo(1)
                .arch$tab(WandsMod.WANDS_TAB)
                #if MC_VERSION >= 12111
                .setId(creative_wand_key)
                #endif
        );
    });
    public static final RegistrySupplier<Item> PALETTE_ITEM = ITEMS.register("palette", () -> {
        return new PaletteItem(new Item.Properties().stacksTo(1)
                .arch$tab(WandsMod.WANDS_TAB)
                #if MC_VERSION >= 12111
                .setId(palette_key)
                #endif
        );
    });
    public static final RegistrySupplier<Item> MAGIC_BAG_1 = ITEMS.register("magic_bag_1", () -> {
        return new MagicBagItem(MagicBagItem.MagicBagItemTier.MAGIC_BAG_TIER_1, config.magic_bag_1_limit, new Item.Properties().stacksTo(1)
                .arch$tab(WandsMod.WANDS_TAB)
                #if MC_VERSION >= 12111
                .setId(magic_bag_1_key)
                #endif
        );
    });
    public static final RegistrySupplier<Item> MAGIC_BAG_2 = ITEMS.register("magic_bag_2", () -> {
        return new MagicBagItem(MagicBagItem.MagicBagItemTier.MAGIC_BAG_TIER_2, config.magic_bag_2_limit, new Item.Properties().stacksTo(1)
                .arch$tab(WandsMod.WANDS_TAB)
                #if MC_VERSION >= 12111
                .setId(magic_bag_2_key)
                #endif
        );
    });

    public static final RegistrySupplier<Item> MAGIC_BAG_3 = ITEMS.register("magic_bag_3", () -> {
        return new MagicBagItem(MagicBagItem.MagicBagItemTier.MAGIC_BAG_TIER_3, Integer.MAX_VALUE, new Item.Properties().stacksTo(1)
                .arch$tab(WandsMod.WANDS_TAB)
                #if MC_VERSION >= 12111
                .setId(magic_bag_3_key)
                #endif
        );
    });

    public static final RegistrySupplier<MenuType<PaletteMenu>> PALETTE_CONTAINER = MENUES.register("palette_menu", () -> MenuRegistry.ofExtended(PaletteMenu::new));
    public static final RegistrySupplier<MenuType<WandMenu>> WAND_CONTAINER = MENUES.register("wand_menu", () -> MenuRegistry.ofExtended(WandMenu::new));
    public static final RegistrySupplier<MenuType<MagicBagMenu>> MAGIC_WAND_CONTANIER = MENUES.register("magic_bag_menu", () -> MenuRegistry.ofExtended(MagicBagMenu::new));

    public enum WandKeys {
        MENU, MODE, ACTION, ORIENTATION, UNDO, INVERT, FILL, ROTATE, CONF, M_INC, M_DEC, N_INC, N_DEC, TOGGLE_STAIRSLAB, DIAGONAL_SPREAD, INC_SEL_BLK, PALETTE_MODE, PALETTE_MENU, CLEAR
    }

    public static boolean is_forge = false;
    public static boolean is_neoforge = false;
    public static boolean is_fabric = false;

    public static void init() {
        ITEMS.register();
        MENUES.register();
        TABS.register();

        if (Platform.getEnvironment() == Env.SERVER) {
            Networking.RegisterS2C();
        }

        Networking.RegisterReceivers();

        PlayerEvent.PLAYER_JOIN.register((player) -> {
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
        });
        PlayerEvent.PLAYER_QUIT.register((player) -> {
            PlayerWand.remove_player(player);
        });

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
        ItemStack main_stack = player.getMainHandItem();
        ItemStack offhand_stack = player.getOffhandItem();
        boolean is_wand = main_stack.getItem() instanceof WandItem;
        boolean is_palette = main_stack.getItem() instanceof PaletteItem || offhand_stack.getItem() instanceof PaletteItem;
        boolean creative = Compat.is_creative(player);
        if (is_palette) {
            if (key >= 0 && key < WandKeys.values().length) {
                switch (WandKeys.values()[key]) {
                    case PALETTE_MENU: {
                        if (offhand_stack.getItem() instanceof PaletteItem) {
                            Compat.open_menu((ServerPlayer) player, offhand_stack, 1);
                        } else {
                            ItemStack mainhand_stack = player.getMainHandItem();
                            if (mainhand_stack.getItem() instanceof PaletteItem) {
                                Compat.open_menu((ServerPlayer) player, mainhand_stack, 1);
                            }
                        }
                    }
                    break;
                    case PALETTE_MODE: {
                        if (!offhand_stack.isEmpty() && offhand_stack.getItem() instanceof PaletteItem) {
                            PaletteItem.nextMode(offhand_stack);
                            if (!WandsMod.config.disable_info_messages) {
                                player.displayClientMessage(PaletteItem.getModeName(offhand_stack), true);
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
                                player.displayClientMessage(Compat.translatable("screen.wands.include_block").append(Compat.literal(": " + WandProps.getFlag(main_stack, WandProps.Flag.INCSELBLOCK))), true);
                            }
                        }
                        break;
                    case DIAGONAL_SPREAD:
                        if (WandProps.flagAppliesTo(WandProps.Flag.DIAGSPREAD, mode)) {
                            WandProps.toggleFlag(main_stack, WandProps.Flag.DIAGSPREAD);
                            if (!WandsMod.config.disable_info_messages) {
                                player.displayClientMessage(Compat.translatable("screen.wands.diagonal_spread").append(Compat.literal(": " + WandProps.getFlag(main_stack, WandProps.Flag.DIAGSPREAD))), true);
                            }
                        }
                        break;
                    case TOGGLE_STAIRSLAB:
                        WandProps.setStateMode(main_stack, WandProps.StateMode.APPLY);
                        WandProps.toggleFlag(main_stack, WandProps.Flag.STAIRSLAB);
                        if (!WandsMod.config.disable_info_messages) {
                            player.displayClientMessage(Compat.translatable("screen.wands.slab_flip").append(Compat.literal(": " + WandProps.getFlag(main_stack, WandProps.Flag.STAIRSLAB))), true);
                        }
                        break;
                    case N_INC:
                        if (mode == WandProps.Mode.GRID) {
                            WandProps.incGrid(main_stack, WandProps.Value.GRIDN, inc, wand_item.limit);
                            if (!WandsMod.config.disable_info_messages) {
                                player.displayClientMessage(Compat.translatable("screen.wands.grid_n").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.GRIDN))), true);
                            }
                        }
                        break;
                    case N_DEC:
                        if (mode == WandProps.Mode.GRID) {
                            WandProps.decGrid(main_stack, WandProps.Value.GRIDN, inc, wand_item.limit);
                            if (!WandsMod.config.disable_info_messages) {
                                player.displayClientMessage(Compat.translatable("screen.wands.grid_n").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.GRIDN))), true);
                            }
                        }
                        break;
                    case M_INC:
                        switch (mode) {
                            case DIRECTION:
                                WandProps.incVal(main_stack, WandProps.Value.MULTIPLIER, inc);
                                if (!WandsMod.config.disable_info_messages) {
                                    player.displayClientMessage(Compat.translatable("screen.wands.multiplier").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.MULTIPLIER))), true);
                                }
                                break;
                            case ROW_COL:
                                WandProps.incVal(main_stack, WandProps.Value.ROWCOLLIM, inc);
                                if (!WandsMod.config.disable_info_messages) {
                                    player.displayClientMessage(Compat.translatable("screen.wands.limit").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.ROWCOLLIM))), true);
                                }
                                break;
                            case GRID:
                                WandProps.incGrid(main_stack, WandProps.Value.GRIDM, inc, wand_item.limit);
                                if (!WandsMod.config.disable_info_messages) {
                                    player.displayClientMessage(Compat.translatable("screen.wands.grid_m").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.GRIDM))), true);
                                }
                                break;
                            case AREA:
                                WandProps.incVal(main_stack, WandProps.Value.AREALIM, inc);
                                if (!WandsMod.config.disable_info_messages) {
                                    player.displayClientMessage(Compat.translatable("screen.wands.limit").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.AREALIM))), true);
                                }
                                break;
                        }
                        break;
                    case M_DEC:
                        switch (mode) {
                            case DIRECTION:
                                WandProps.decVal(main_stack, WandProps.Value.MULTIPLIER, inc);
                                if (!WandsMod.config.disable_info_messages) {
                                    player.displayClientMessage(Compat.translatable("screen.wands.multiplier").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.MULTIPLIER))), true);
                                }
                                break;
                            case ROW_COL:
                                WandProps.decVal(main_stack, WandProps.Value.ROWCOLLIM, inc);
                                if (!WandsMod.config.disable_info_messages) {
                                    player.displayClientMessage(Compat.translatable("screen.wands.limit").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.ROWCOLLIM))), true);
                                }
                                break;
                            case GRID:
                                WandProps.decGrid(main_stack, WandProps.Value.GRIDM, inc, wand_item.limit);
                                if (!WandsMod.config.disable_info_messages) {
                                    player.displayClientMessage(Compat.translatable("screen.wands.grid_m").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.GRIDM))), true);
                                }
                                break;
                            case AREA:
                                WandProps.decVal(main_stack, WandProps.Value.AREALIM, inc);
                                if (!WandsMod.config.disable_info_messages) {
                                    player.displayClientMessage(Compat.translatable("screen.wands.limit").append(Compat.literal(": " + WandProps.getVal(main_stack, WandProps.Value.AREALIM))), true);
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
                                player.displayClientMessage(Compat.translatable(WandProps.getMode(main_stack).toString()).append(" - ").append(Compat.translatable(WandProps.getAction(main_stack).toString())), true);
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
                                player.displayClientMessage(Compat.translatable(newMode.toString()).append(" - ").append(Compat.translatable(WandProps.getAction(main_stack).toString())), true);
                            } else {
                                player.displayClientMessage(Compat.translatable(newMode.toString()), true);
                            }
                        }
                        break;
                    case ORIENTATION:
                        switch (mode) {
                            case CIRCLE:
                            case FILL:
                                WandProps.nextPlane(main_stack);
                                if (!WandsMod.config.disable_info_messages) {
                                    player.displayClientMessage(Compat.translatable("screen.wands.plane").append(Compat.literal(": " + WandProps.getPlane(main_stack))), true);
                                }
                                send_state((ServerPlayer) player, wand);
                                break;
                            case DIRECTION:
                            case PASTE:

                                break;
                            default:
                                WandProps.nextOrientation(main_stack);
                                if (!WandsMod.config.disable_info_messages) {
                                    player.displayClientMessage(Compat.translatable(WandProps.getOrientation(main_stack).toString()), true);
                                }
                                break;
                        }
                        break;
                    case INVERT:
                        if (WandProps.flagAppliesTo(WandProps.Flag.INVERTED, mode)) {
                            WandProps.toggleFlag(main_stack, WandProps.Flag.INVERTED);
                            if (!WandsMod.config.disable_info_messages) {
                                player.displayClientMessage(Compat.translatable("screen.wands.invert").append(Compat.literal(": " + WandProps.getFlag(main_stack, WandProps.Flag.INVERTED))), true);
                            }
                        }
                        break;
                    case FILL:
                        switch (wand.mode) {
                            case FILL: {
                                WandProps.toggleFlag(main_stack, WandProps.Flag.RFILLED);
                                if (!WandsMod.config.disable_info_messages) {
                                    player.displayClientMessage(Compat.translatable("screen.wands.filled").append(Compat.literal(": " + WandProps.getFlag(main_stack, WandProps.Flag.RFILLED))), true);
                                }
                            }
                            break;
                            case CIRCLE: {
                                WandProps.toggleFlag(main_stack, WandProps.Flag.CFILLED);
                                if (!WandsMod.config.disable_info_messages) {
                                    player.displayClientMessage(Compat.translatable("screen.wands.filled_circle").append(Compat.literal(": " + WandProps.getFlag(main_stack, WandProps.Flag.CFILLED))), true);
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
                                player.displayClientMessage(Compat.translatable(rotKey), true);
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
                                if (shift) {
                                    wand.redo(n);
                                    if (!WandsMod.config.disable_info_messages) {
                                        player.displayClientMessage(Compat.literal("Redo"), true);
                                    }
                                } else {
                                    wand.undo(n);
                                    if (!WandsMod.config.disable_info_messages) {
                                        player.displayClientMessage(Compat.literal("Undo"), true);
                                    }
                                }
                            }
                        }
                        break;
                    case CLEAR:

                        wand.clear(true);

                        if (player != null && !WandsMod.config.disable_info_messages) {
                            player.displayClientMessage(Compat.literal("wand cleared"), true);
                        }
                        break;
                }

            }
        }
        if (!main_stack.isEmpty() && main_stack.getItem() instanceof PaletteItem) {
            if (key >= 0 && key < WandKeys.values().length) {
                if (Objects.requireNonNull(WandKeys.values()[key]) == WandKeys.PALETTE_MODE) {
                    PaletteItem.nextMode(main_stack);
                    if (!WandsMod.config.disable_info_messages) {
                        player.displayClientMessage(PaletteItem.getModeName(main_stack), true);
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
                    wand.is_alt_pressed = alt;
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
