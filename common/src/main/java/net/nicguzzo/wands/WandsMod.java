package net.nicguzzo.wands;


import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.platform.Platform;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.utils.Env;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.utils.WandUtils;
import net.nicguzzo.wands.wand.PlayerWand;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;
import net.nicguzzo.wands.wand.modes.RockMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    // We can use this if we don't want to use DeferredRegister

    public static final Supplier<RegistrarManager> REGISTRIES = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> WANDS_TAB = TABS.register("wands_tab", () -> CreativeTabRegistry.create(Component.translatable("itemGroup.wands.wands_tab"), () -> new ItemStack(WandsMod.DIAMOND_WAND_ITEM.get())));


    static ResourceLocation stone_wand = Compat.create_resource("stone_wand");
    static ResourceLocation iron_wand = Compat.create_resource("iron_wand");
    static ResourceLocation copper_wand = Compat.create_resource("copper_wand");
    static ResourceLocation diamond_wand = Compat.create_resource("diamond_wand");
    static ResourceLocation netherite_wand = Compat.create_resource("netherite_wand");
    static ResourceLocation creative_wand = Compat.create_resource("creative_wand");
    static ResourceLocation palette = Compat.create_resource("palette");
    static ResourceLocation magic_bag_1 = Compat.create_resource("magic_bag_1");
    static ResourceLocation magic_bag_2 = Compat.create_resource("magic_bag_2");
    static ResourceLocation magic_bag_3 = Compat.create_resource("magic_bag_3");

    static ResourceKey<Item> stone_wand_key = ResourceKey.create(Registries.ITEM, stone_wand);
    static ResourceKey<Item> copper_wand_key = ResourceKey.create(Registries.ITEM, copper_wand);
    static ResourceKey<Item> iron_wand_key = ResourceKey.create(Registries.ITEM, iron_wand);
    static ResourceKey<Item> diamond_wand_key = ResourceKey.create(Registries.ITEM, diamond_wand);
    static ResourceKey<Item> netherite_wand_key = ResourceKey.create(Registries.ITEM, netherite_wand);
    static ResourceKey<Item> creative_wand_key = ResourceKey.create(Registries.ITEM, creative_wand);
    static ResourceKey<Item> palette_key = ResourceKey.create(Registries.ITEM, palette);
    static ResourceKey<Item> magic_bag_1_key = ResourceKey.create(Registries.ITEM, magic_bag_1);
    static ResourceKey<Item> magic_bag_2_key = ResourceKey.create(Registries.ITEM, magic_bag_2);
    static ResourceKey<Item> magic_bag_3_key = ResourceKey.create(Registries.ITEM, magic_bag_3);


    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final DeferredRegister<MenuType<?>> MENUES = DeferredRegister.create(MOD_ID, Registries.MENU);

    public static final RegistrySupplier<Item> STONE_WAND_ITEM = ITEMS.register(stone_wand, () -> {
        return new WandItem(WandItem.WandTier.STONE_WAND, config.stone_wand_limit, false, false, false, false, new Item.Properties().durability(config.stone_wand_durability).arch$tab(WandsMod.WANDS_TAB));
    });
    public static final RegistrySupplier<Item> COPPER_WAND_ITEM = ITEMS.register(copper_wand, () -> {
        return new WandItem(WandItem.WandTier.COPPER_WAND, config.copper_wand_limit, false, false, false, false, new Item.Properties().durability(config.copper_wand_durability).arch$tab(WandsMod.WANDS_TAB));
    });
    public static final RegistrySupplier<Item> IRON_WAND_ITEM = ITEMS.register(iron_wand, () -> {
        return new WandItem(WandItem.WandTier.IRON_WAND, config.iron_wand_limit, false, false, false, false, new Item.Properties().durability(config.iron_wand_durability).arch$tab(WandsMod.WANDS_TAB));
    });
    public static final RegistrySupplier<Item> DIAMOND_WAND_ITEM = ITEMS.register(diamond_wand, () -> {
        return new WandItem(WandItem.WandTier.DIAMOND_WAND, config.diamond_wand_limit, true, false, false, false, new Item.Properties().durability(config.diamond_wand_durability).arch$tab(WandsMod.WANDS_TAB));
    });
    public static final RegistrySupplier<Item> NETHERITE_WAND_ITEM = ITEMS.register(netherite_wand, () -> {
        return new WandItem(WandItem.WandTier.NETHERITE_WAND, config.netherite_wand_limit, true, true, false, true, new Item.Properties().fireResistant().durability(config.netherite_wand_durability).arch$tab(WandsMod.WANDS_TAB));
    });
    public static final RegistrySupplier<Item> CREATIVE_WAND_ITEM = ITEMS.register(creative_wand, () -> {
        return new WandItem(WandItem.WandTier.CREATIVE_WAND, config.creative_wand_limit, true, true, true, true, new Item.Properties().fireResistant().stacksTo(1).arch$tab(WandsMod.WANDS_TAB));
    });

    public static final RegistrySupplier<Item> PALETTE_ITEM = ITEMS.register("palette", () -> {
        return new PaletteItem(new Item.Properties().stacksTo(1).arch$tab(WandsMod.WANDS_TAB));
    });
    public static final RegistrySupplier<Item> MAGIC_BAG_1 = ITEMS.register("magic_bag_1", () -> {
        return new MagicBagItem(MagicBagItem.MagicBagItemTier.MAGIC_BAG_TIER_1, config.magic_bag_1_limit, new Item.Properties().stacksTo(1).arch$tab(WandsMod.WANDS_TAB));
    });
    public static final RegistrySupplier<Item> MAGIC_BAG_2 = ITEMS.register("magic_bag_2", () -> {
        return new MagicBagItem(MagicBagItem.MagicBagItemTier.MAGIC_BAG_TIER_2, config.magic_bag_2_limit, new Item.Properties().stacksTo(1).arch$tab(WandsMod.WANDS_TAB));
    });

    public static final RegistrySupplier<Item> MAGIC_BAG_3 = ITEMS.register("magic_bag_3", () -> {
        return new MagicBagItem(MagicBagItem.MagicBagItemTier.MAGIC_BAG_TIER_3, Integer.MAX_VALUE, new Item.Properties().stacksTo(1).arch$tab(WandsMod.WANDS_TAB));
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
            //LOGGER.info("registerS2CPayloadType ConfPacket");
            NetworkManager.registerS2CPayloadType(Networking.ConfPacket.TYPE, Networking.ConfPacket.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(Networking.SndPacket.TYPE, Networking.SndPacket.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(Networking.ToastPacket.TYPE, Networking.ToastPacket.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(Networking.StatePacket.TYPE, Networking.StatePacket.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(Networking.PlayerDataPacket.TYPE, Networking.PlayerDataPacket.STREAM_CODEC);
        }
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, Networking.KbPacket.TYPE, Networking.KbPacket.STREAM_CODEC, (data, context) -> {
            //LOGGER.info("got KbPacket");
            process_keys(context.getPlayer(), data.key(), data.shift(), data.alt());
        });

        NetworkManager.registerReceiver(Side.C2S, Networking.PalettePacket.TYPE, Networking.PalettePacket.STREAM_CODEC, (data, context) -> {
            LOGGER.info("got PalettePacket");
            process_palette(context.getPlayer(), data.mode(), data.rotate(),data.grad_h());
        });
        NetworkManager.registerReceiver(Side.C2S, Networking.WandPacket.TYPE, Networking.WandPacket.STREAM_CODEC, (data, context) -> {
            //LOGGER.info("got WandPacket");
            ItemStack wand_stack = context.getPlayer().getMainHandItem();
            wand_stack.applyComponents(data.item_stack().getComponents());

            //TODO: wand packet
            //wand_stack.set()
            //wand_stack.set(data.item_stack().get);
            //CompoundTag tag=item.getTag();
            //if(tag!=null) {
            //wand_stack.setTag(tag);
            //}
        });
        NetworkManager.registerReceiver(Side.C2S, Networking.SyncRockPacket.TYPE, Networking.SyncRockPacket.STREAM_CODEC, (data, context) -> {
            //LOGGER.info("got PalettePacket");
            Player player = context.getPlayer();
            if (player == null) {
                WandsMod.LOGGER.error("player is null");
                return;
            }
            ItemStack stack = context.getPlayer().getMainHandItem();
            if (!WandUtils.is_wand(stack)) {
                WandsMod.LOGGER.error("player doesn't have a wand in main hand");
                return;
            }
            Wand wand = PlayerWand.get(player);
            if (wand == null) {
                WandsMod.LOGGER.error("wand is null");
                return;
            }
            if(wand.mode == WandProps.Mode.ROCK) {
                WandMode m=wand.get_mode();
                if( m instanceof RockMode){
                    ((RockMode)m).set_random_pos(data.rx(),data.ry(),data.rz());
                }
            }
        });
        NetworkManager.registerReceiver(Side.C2S, Networking.PosPacket.TYPE, Networking.PosPacket.STREAM_CODEC, (data, context) -> {
            //LOGGER.info("got PosPacket");
            Player player = context.getPlayer();
            if (player == null) {
                WandsMod.LOGGER.error("player is null");
                return;
            }
            Level level = Compat.player_level(player);
            ItemStack stack = context.getPlayer().getMainHandItem();
            if (!WandUtils.is_wand(stack)) {
                WandsMod.LOGGER.error("player doesn't have a wand in main hand");
                return;
            }
            Wand wand = PlayerWand.get(player);
            if (wand == null) {
                WandsMod.LOGGER.error("wand is null");
                return;
            }
            BlockPos p1;
            BlockPos p2;

            int d = data.dir();
            int has_p1_p2 = data.has_p1_p2();
            Direction side = Direction.values()[d];
            if (has_p1_p2 == 1) {
                p1 = data.p1();
            } else {
                p1 = null;
                //WandsMod.LOGGER.info("needs at least 1 position");
                //return;
            }
            if (has_p1_p2 == 2) {
                p2 = data.p2();
            } else {
                p2 = null;
            }
            if (has_p1_p2 == 3) {
                p1 = data.p1();
                p2 = data.p2();
            }
            if (p1 == null) {
                WandsMod.LOGGER.info("needs at least 1 position");
                return;
            }
            BlockState block_state;
            BlockPos pos;
            if (p2 != null) {
                block_state = level.getBlockState(p2);
                pos = p2;
            } else {
                block_state = level.getBlockState(p1);
                pos = p1;
            }
            if (block_state.isAir()) {
                block_state = level.getBlockState(p1);
            }
            wand.setP1(p1);
            wand.setP2(p2);
            Vec3 hit = new Vec3(data.hit().x, data.hit().y, data.hit().z);
            //WandsMod.log(" received_placement palette seed: " + seed,true);
            wand.palette.seed = data.seed();
            //wand.lastPlayerDirection=player_dir;
            //WandsMod.LOGGER.info("got_placement p1: "+ wand.getP1() +" p2: "+ wand.getP2() +" pos:"+ pos);
            wand.do_or_preview(player, level, block_state, pos, side, hit, stack, (WandItem) stack.getItem(), true);
            wand.clear();
        });
        NetworkManager.registerReceiver(Side.C2S, Networking.GlobalSettingsPacket.TYPE, Networking.GlobalSettingsPacket.STREAM_CODEC, (packet, context) -> {
            Player player = context.getPlayer();
            if (player != null) {
                Wand wand = PlayerWand.get(player);
                if (wand != null) {
                    wand.drop_on_player = packet.drop_pos();
                }
            }

        });

        PlayerEvent.PLAYER_JOIN.register((player) -> {
            //LOGGER.info("PLAYER_JOIN");
            //send config
            Wand wand = null;
            wand = PlayerWand.get(player);
            if (wand == null) {
                PlayerWand.add_player(player);
                wand = PlayerWand.get(player);
            }
            if (!Compat.player_level(player).isClientSide()) {
                if (WandsMod.config != null) {
                    NetworkManager.sendToPlayer(player, new Networking.ConfPacket(WandsMod.config.blocks_per_xp, WandsMod.config.destroy_in_survival_drop, WandsMod.config.survival_unenchanted_drops, WandsMod.config.allow_wand_to_break, WandsMod.config.allow_offhand_to_break, WandsMod.config.mend_tools));
                    //LOGGER.info("config sent");
                }
                NetworkManager.sendToPlayer(player,new Networking.PlayerDataPacket(wand.player_data));
            }
        });
        PlayerEvent.PLAYER_QUIT.register((player) -> {
            //LOGGER.info("PLAYER_QUIT");
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

                NetworkManager.sendToPlayer(player, new Networking.StatePacket(mode.ordinal(), slot, BLOCKS_PER_XP != 0, player.experienceLevel, player.experienceProgress));

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
                            player.displayClientMessage(Compat.literal("Palette mode: " + PaletteItem.getMode(offhand_stack)), false);
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
                        WandProps.toggleFlag(main_stack, WandProps.Flag.INCSELBLOCK);
                        break;
                    case DIAGONAL_SPREAD:
                        WandProps.toggleFlag(main_stack, WandProps.Flag.DIAGSPREAD);
                        break;
                    case TOGGLE_STAIRSLAB:
                        WandProps.setStateMode(main_stack, WandProps.StateMode.APPLY);
                        WandProps.toggleFlag(main_stack, WandProps.Flag.STAIRSLAB);
                        break;
                    case N_INC:
                        if (mode == WandProps.Mode.GRID) {
                            //WandProps.incVal(main_stack, WandProps.Value.GRIDN ,inc,wand.limit);
                            WandProps.incGrid(main_stack, WandProps.Value.GRIDN, inc, wand_item.limit);
                        }
                        break;
                    case N_DEC:
                        if (mode == WandProps.Mode.GRID) {
                            WandProps.decVal(main_stack, WandProps.Value.GRIDN, inc);
                        }
                        break;
                    case M_INC:
                        switch (mode) {
                            case DIRECTION:
                                WandProps.incVal(main_stack, WandProps.Value.MULTIPLIER, inc);
                                break;
                            case ROW_COL:
                                WandProps.incVal(main_stack, WandProps.Value.ROWCOLLIM, inc);
                                break;
                            case GRID:
                                WandProps.incGrid(main_stack, WandProps.Value.GRIDM, inc, wand_item.limit);
                                //WandProps.incVal(main_stack, WandProps.Value.GRIDM ,inc);
                                break;
                            case AREA:
                                WandProps.incVal(main_stack, WandProps.Value.AREALIM, inc);
                                break;
                        }
                        break;
                    case M_DEC:
                        switch (mode) {
                            case DIRECTION:
                                WandProps.decVal(main_stack, WandProps.Value.MULTIPLIER, inc);
                                break;
                            case ROW_COL:
                                WandProps.decVal(main_stack, WandProps.Value.ROWCOLLIM, inc);
                                break;
                            case GRID:
                                WandProps.decVal(main_stack, WandProps.Value.GRIDM, inc);
                                break;
                            case AREA:
                                WandProps.decVal(main_stack, WandProps.Value.AREALIM, inc);
                                break;
                        }
                        break;
                    case ACTION:
                        if (shift) {
                            WandProps.prevAction(main_stack);
                        } else {
                            WandProps.nextAction(main_stack);
                        }
                        player.displayClientMessage(Compat.literal("Wand Action: ").append(Compat.translatable(WandProps.getAction(main_stack).toString())), false);
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
                        break;
                    case ORIENTATION:
                        switch (mode) {
                            case CIRCLE:
                            case FILL:
                                WandProps.nextPlane(main_stack);
                                player.displayClientMessage(Compat.literal("Wand Plane: " + WandProps.getPlane(main_stack)), false);
                                send_state((ServerPlayer) player, wand);
                                break;
                            case DIRECTION:
                            case PASTE:

                                break;
                            default:
                                WandProps.nextOrientation(main_stack);
                                player.displayClientMessage(Compat.literal("Wand Orientation: ").append(Compat.translatable(WandProps.getOrientation(main_stack).toString())), false);
                                break;
                        }
                        break;
                    case INVERT:
                        WandProps.toggleFlag(main_stack, WandProps.Flag.INVERTED);
                        player.displayClientMessage(Compat.literal("Wand inverted: " + WandProps.getFlag(main_stack, WandProps.Flag.INVERTED)), false);
                        break;
                    case FILL:
                        switch (wand.mode) {
                            case FILL: {
                                WandProps.toggleFlag(main_stack, WandProps.Flag.RFILLED);
                                player.displayClientMessage(Compat.literal("Wand fill rect: " + WandProps.getFlag(main_stack, WandProps.Flag.RFILLED)), false);
                            }
                            break;
                            case CIRCLE: {
                                WandProps.toggleFlag(main_stack, WandProps.Flag.CFILLED);
                                player.displayClientMessage(Compat.literal("Wand circle fill: " + WandProps.getFlag(main_stack, WandProps.Flag.CFILLED)), false);
                            }
                            break;
                        }
                        break;
                    case ROTATE:
                        if(mode != WandProps.Mode.ROCK){
                            WandProps.nextRotation(main_stack);
                            WandProps.setStateMode(main_stack, WandProps.StateMode.APPLY);
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
                                } else {
                                    wand.undo(n);
                                }
                            }
                        }
                        break;
                    case CLEAR:

                        wand.clear();

                        if (player != null) player.displayClientMessage(Compat.literal("wand cleared"), false);
                        break;
                }

            }
        }
        if (!main_stack.isEmpty() && main_stack.getItem() instanceof PaletteItem) {
            if (key >= 0 && key < WandKeys.values().length) {
                if (Objects.requireNonNull(WandKeys.values()[key]) == WandKeys.PALETTE_MODE) {
                    PaletteItem.nextMode(main_stack);
                    player.displayClientMessage(Compat.literal("Palette mode: " + PaletteItem.getMode(main_stack)), false);
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
