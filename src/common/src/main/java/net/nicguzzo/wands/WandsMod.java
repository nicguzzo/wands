package net.nicguzzo.wands;

#if MC=="1165"
import me.shedaniel.architectury.event.events.PlayerEvent;
import me.shedaniel.architectury.registry.*;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.NetworkManager.Side;
import net.minecraft.core.Registry;
#else

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;

import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
#if MC>="1193"
    import com.google.common.base.Suppliers;
    import com.google.common.base.Supplier;
    import dev.architectury.registry.registries.RegistrarManager;
    import dev.architectury.registry.CreativeTabRegistry;
    import net.minecraft.core.registries.Registries;
#else
    import dev.architectury.registry.registries.Registries;
    import net.minecraft.core.Registry;
#endif
import dev.architectury.registry.registries.RegistrySupplier;
#endif

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.nicguzzo.wands.menues.MagicBagMenu;
import net.nicguzzo.wands.menues.PaletteMenu;
import net.nicguzzo.wands.menues.WandMenu;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.items.PaletteItem;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.utils.WandUtils;
import net.nicguzzo.wands.wand.PlayerWand;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandProps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;

#if MC<"1193"
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.util.LazyLoadedValue;
#endif

import java.util.Objects;

public class WandsMod {   
    public static int platform=-1; // 0=forge; 1=fabric; 2=quilt
    public static final WandsConfig config=WandsConfig.get_instance();
    public static final String MOD_ID = "wands";
    
    public static final Logger LOGGER = LogManager.getLogger();
    // We can use this if we don't want to use DeferredRegister

    #if MC>="1193"
        public static final Supplier<RegistrarManager> REGISTRIES = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));
        public static final CreativeTabRegistry.TabSupplier WANDS_TAB = CreativeTabRegistry.create(new ResourceLocation(MOD_ID, "wands_tab"),
                () -> new ItemStack(WandsMod.DIAMOND_WAND_ITEM.get()));
    #else
        public static final LazyLoadedValue<Registries> REGISTRIES = new LazyLoadedValue<>(() -> Registries.get(MOD_ID));
        public static final CreativeModeTab WANDS_TAB = Compat.create_tab(new ResourceLocation(MOD_ID, "wands_tab"));
    #endif



    #if MC>="1193"
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final DeferredRegister<MenuType<?>> MENUES = DeferredRegister.create(MOD_ID, Registries.MENU);
    #else
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registry.ITEM_REGISTRY);
    public static final DeferredRegister<MenuType<?>> MENUES = DeferredRegister.create(MOD_ID, Registry.MENU_REGISTRY);
    #endif
    public static final RegistrySupplier<Item> STONE_WAND_ITEM = ITEMS.register("stone_wand", () ->{
            return new WandItem(Tiers.STONE,config.stone_wand_limit,false,false,false,false,new Item.Properties().durability(config.stone_wand_durability)
                    #if MC>="1193"
                    .arch$tab(WandsMod.WANDS_TAB));
                    #else
                    .tab(WandsMod.WANDS_TAB));
                    #endif
    });
    public static final RegistrySupplier<Item> IRON_WAND_ITEM = ITEMS.register("iron_wand", () ->{
        return new WandItem(Tiers.IRON ,config.iron_wand_limit,false,false,false,false,new Item.Properties().durability(config.iron_wand_durability)
                #if MC>="1193"
                .arch$tab(WandsMod.WANDS_TAB));
                #else
                .tab(WandsMod.WANDS_TAB));
                #endif
    });
    public static final RegistrySupplier<Item> DIAMOND_WAND_ITEM = ITEMS.register("diamond_wand", () ->{
        return new WandItem(Tiers.DIAMOND,config.diamond_wand_limit,true,false,false,false,new Item.Properties().durability(config.diamond_wand_durability)
                #if MC>="1193"
                .arch$tab(WandsMod.WANDS_TAB));
                #else
                .tab(WandsMod.WANDS_TAB));
                #endif
    });
    public static final RegistrySupplier<Item> NETHERITE_WAND_ITEM = ITEMS.register("netherite_wand", () ->{
        return new WandItem(Tiers.NETHERITE,config.netherite_wand_limit,true,true,false,true,new Item.Properties().fireResistant().durability(config.netherite_wand_durability)
                #if MC>="1193"
                .arch$tab(WandsMod.WANDS_TAB));
                #else
                .tab(WandsMod.WANDS_TAB));
                #endif
    });

    public static final RegistrySupplier<Item> CREATIVE_WAND_ITEM = ITEMS.register("creative_wand", () ->{
        return new WandItem(Tiers.NETHERITE,config.creative_wand_limit,true,true,true,true,new Item.Properties().fireResistant().stacksTo(1)
                #if MC>="1193"
                .arch$tab(WandsMod.WANDS_TAB));
                #else
                .tab(WandsMod.WANDS_TAB));
                #endif
    });

    public static final RegistrySupplier<Item> PALETTE_ITEM = ITEMS.register("palette", () ->{
        return new PaletteItem(new Item.Properties().stacksTo(1)
                #if MC>="1193"
                .arch$tab(WandsMod.WANDS_TAB));
                #else
                .tab(WandsMod.WANDS_TAB));
                #endif
    });
    public static final RegistrySupplier<Item> MAGIC_BAG_1 = ITEMS.register("magic_bag_1", () ->{
        return new MagicBagItem(0,config.magic_bag_1_limit,new Item.Properties().stacksTo(1)
                #if MC>="1193"
                .arch$tab(WandsMod.WANDS_TAB));
                #else
                .tab(WandsMod.WANDS_TAB));
                #endif
    });
    public static final RegistrySupplier<Item> MAGIC_BAG_2 = ITEMS.register("magic_bag_2", () ->{
        return new MagicBagItem(1,config.magic_bag_2_limit,new Item.Properties().stacksTo(1)
                #if MC>="1193"
                .arch$tab(WandsMod.WANDS_TAB));
                #else
                .tab(WandsMod.WANDS_TAB));
                #endif
    });

    public static final RegistrySupplier<Item> MAGIC_BAG_3 = ITEMS.register("magic_bag_3", () ->{
        return new MagicBagItem(2,Integer.MAX_VALUE,new Item.Properties().stacksTo(1)
                #if MC>="1193"
                .arch$tab(WandsMod.WANDS_TAB));
                #else
                .tab(WandsMod.WANDS_TAB));
                #endif
    });
    
    public static final RegistrySupplier<MenuType<PaletteMenu>> PALETTE_CONTAINER =MENUES.register("palette_menu",()-> MenuRegistry.ofExtended(PaletteMenu::new));
    public static final RegistrySupplier<MenuType<WandMenu>> WAND_CONTAINER =MENUES.register("wand_menu",()-> MenuRegistry.ofExtended(WandMenu::new));
    public static final RegistrySupplier<MenuType<MagicBagMenu>> MAGIC_WAND_CONTANIER=MENUES.register("magic_bag_menu",()-> MenuRegistry.ofExtended(MagicBagMenu::new));

    static public ResourceLocation KB_PACKET= new ResourceLocation(MOD_ID, "key_packet");
    static public ResourceLocation SND_PACKET= new ResourceLocation(MOD_ID, "sound_packet");
    static public ResourceLocation PALETTE_PACKET= new ResourceLocation(MOD_ID, "palette_packet");
    static public ResourceLocation STATE_PACKET= new ResourceLocation(MOD_ID, "state_packet");
    static public ResourceLocation WAND_PACKET= new ResourceLocation(MOD_ID, "wand_packet");
    static public ResourceLocation POS_PACKET= new ResourceLocation(MOD_ID, "pos_packet");
    static public ResourceLocation CONF_PACKET= new ResourceLocation(MOD_ID, "conf_packet");
    static public ResourceLocation GLOBAL_SETTINGS_PACKET= new ResourceLocation(MOD_ID, "global_settings_packet");
    public enum WandKeys{
        MENU,
        MODE,
        ACTION,
        ORIENTATION,
        UNDO,
        INVERT,
        FILL,
        ROTATE,
        CONF,
        M_INC,
        M_DEC,
        N_INC,
        N_DEC,
        TOGGLE_STAIRSLAB,
        DIAGONAL_SPREAD,
        INC_SEL_BLK,
        PALETTE_MODE,
        PALETTE_MENU
    }
   public static boolean is_forge=false;
	
    public static void init() {
        ITEMS.register();
        MENUES.register();

        NetworkManager.registerReceiver(Side.C2S, KB_PACKET, (packet,context)->{
            int key=packet.readInt();
            boolean shift=packet.readBoolean();
            boolean alt=packet.readBoolean();
            context.queue(()-> process_keys(context.getPlayer(), key,shift,alt));
        });
        NetworkManager.registerReceiver(Side.C2S, PALETTE_PACKET, (packet,context)->{
            boolean mode=packet.readBoolean();
            boolean rotate=packet.readBoolean();
            context.queue(()-> process_palette(context.getPlayer(), mode,rotate));
        });
        NetworkManager.registerReceiver(Side.C2S, WAND_PACKET, (packet,context)->{
            ItemStack item=packet.readItem();
            context.queue(()->{
                ItemStack wand_stack=context.getPlayer().getMainHandItem();
                CompoundTag tag=item.getTag();
                if(tag!=null) {
                    wand_stack.setTag(tag);
                }
            });
        });
        NetworkManager.registerReceiver(Side.C2S, POS_PACKET, (packet,context)->{

            BlockHitResult hitResult=packet.readBlockHitResult();
            BlockPos p1=packet.readBlockPos();
            BlockPos pos2=packet.readBlockPos();
            boolean p2=packet.readBoolean();
            Direction player_dir=Direction.values()[packet.readInt()];
            context.queue(()->{
                ItemStack stack=context.getPlayer().getMainHandItem();
                if(WandUtils.is_wand(stack)){
                    BlockPos pos=hitResult.getBlockPos();
                    Direction side=hitResult.getDirection();
                    Player player=context.getPlayer();
                    if(player!=null) {
                        Wand wand = PlayerWand.get(player);
                        if(wand!=null) {
                            Level level=player.level;
                            BlockState block_state=level.getBlockState(pos);
                            wand.p1=p1;
                            WandProps.Mode mode=wand.mode;
                            if (    mode == WandProps.Mode.FILL   || mode == WandProps.Mode.LINE ||
                                    mode == WandProps.Mode.CIRCLE || mode == WandProps.Mode.COPY /*||
                                    mode == WandProps.Mode.RECT*/) {
                                if (WandProps.getFlag(stack, WandProps.Flag.INCSELBLOCK)) {
                                    pos = pos.relative(side, 1);
                                }
                            }
                            wand.p2=p2;
                            wand.lastPlayerDirection=player_dir;
                            wand.do_or_preview(player,level, block_state,pos, side, hitResult.getLocation(), stack,true);
                            wand.clear();
                        }
                    }
                }
            });
        });
        NetworkManager.registerReceiver(Side.C2S, GLOBAL_SETTINGS_PACKET, (packet,context)->{
            boolean drop_pos=packet.readBoolean();
            context.queue(()-> {
                Player player=context.getPlayer();
                if(player!=null) {
                    Wand wand = PlayerWand.get(player);
                    if (wand != null) {
                        wand.drop_on_player=drop_pos;
                    }
                }
            });
        });
        PlayerEvent.PLAYER_JOIN.register((player)->{
            LOGGER.info("PLAYER_JOIN");
            //send config
            if(!player.level.isClientSide()){
                if(WandsMod.config!=null){
                    FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
                    packet.writeFloat(WandsMod.config.blocks_per_xp);
                    packet.writeBoolean(WandsMod.config.destroy_in_survival_drop);
                    packet.writeBoolean(WandsMod.config.survival_unenchanted_drops);
                    packet.writeBoolean(WandsMod.config.allow_wand_to_break);
                    packet.writeBoolean(WandsMod.config.allow_offhand_to_break);
                    packet.writeBoolean(WandsMod.config.mend_tools);
                    NetworkManager.sendToPlayer(player, WandsMod.CONF_PACKET, packet);
                    LOGGER.info("config sent");
                }
                Wand wand=null;            
                wand=PlayerWand.get(player);
                if(wand==null){
                    PlayerWand.add_player(player);
                    wand=PlayerWand.get(player);
                }
            }
        });
        PlayerEvent.PLAYER_QUIT.register((player)->{
            //LOGGER.info("PLAYER_QUIT");
            PlayerWand.remove_player(player);
        });

    }
    public static void send_state(ServerPlayer player,Wand wand){
        if(wand!=null && player!=null && !player.level.isClientSide()) {
            ItemStack wand_stack=player.getMainHandItem();
            if(wand_stack.getItem() instanceof WandItem) {
                WandProps.Mode mode = WandProps.getMode(wand_stack);
                FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
                int slot=0;
                if (wand.palette.palette_slots.size() != 0) {
                    slot=(wand.palette.slot + 1) % wand.palette.palette_slots.size();
                }
                float BLOCKS_PER_XP = WandsMod.config.blocks_per_xp;

                packet.writeLong(wand.palette.seed);
                packet.writeInt(mode.ordinal());
                packet.writeInt(slot);
                packet.writeBoolean(BLOCKS_PER_XP != 0);
                packet.writeInt(player.experienceLevel);
                packet.writeFloat(player.experienceProgress);
                NetworkManager.sendToPlayer(player, WandsMod.STATE_PACKET, packet);
            }
        }
    }
    public static void process_palette(Player player,boolean mode,boolean rotate){
        
        ItemStack item_stack=player.getMainHandItem();
        ItemStack palette=null;
        if(!item_stack.isEmpty() && item_stack.getItem() instanceof PaletteItem){
            palette=item_stack;
        }else{
            ItemStack offhand_stack=player.getOffhandItem();
            if(!offhand_stack.isEmpty() && offhand_stack.getItem() instanceof PaletteItem){
                palette=offhand_stack;
            }
        }
        if(palette!=null){
            if(mode){
                PaletteItem.nextMode(palette);
            }
            if(rotate){
                PaletteItem.toggleRotate(palette);
            }
        }
    }
    public static void process_keys(Player player,int key,boolean shift,boolean alt){
        ItemStack main_stack=player.getMainHandItem();
        ItemStack offhand_stack = player.getOffhandItem();
        boolean is_wand=main_stack.getItem() instanceof WandItem;
        boolean is_palette=main_stack.getItem() instanceof PaletteItem ||offhand_stack.getItem() instanceof PaletteItem;
        boolean creative= Compat.is_creative(player);
        if(is_palette){
            if (key >= 0 && key < WandKeys.values().length) {
                switch (WandKeys.values()[key]) {
                    case PALETTE_MENU: {
                        if (offhand_stack.getItem() instanceof PaletteItem) {
                            Compat.open_menu((ServerPlayer) player, offhand_stack,1);
                        } else {
                            ItemStack mainhand_stack = player.getMainHandItem();
                            if (mainhand_stack.getItem() instanceof PaletteItem) {
                                Compat.open_menu((ServerPlayer) player, mainhand_stack,1);
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
        if(is_wand) {
            Wand wand = PlayerWand.get(player);
            if(wand==null) return;
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
                            WandProps.incGrid(main_stack, WandProps.Value.GRIDN, inc);
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
                                WandProps.incGrid(main_stack, WandProps.Value.GRIDM, inc);
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
                        Compat.open_menu((ServerPlayer) player, main_stack,0);
                        break;
                    case MODE:
                        if (shift) {
                            WandProps.prevMode(main_stack);
                        } else {
                            WandProps.nextMode(main_stack);
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
                        switch (wand.mode){
                            case FILL: {
                                WandProps.toggleFlag(main_stack, WandProps.Flag.RFILLED);
                                player.displayClientMessage(Compat.literal("Wand fill rect: " + WandProps.getFlag(main_stack, WandProps.Flag.RFILLED)), false);
                            }break;
                            case CIRCLE: {
                                WandProps.toggleFlag(main_stack, WandProps.Flag.CFILLED);
                                player.displayClientMessage(Compat.literal("Wand circle fill: " + WandProps.getFlag(main_stack, WandProps.Flag.CFILLED)), false);
                            }break;
                        }
                        break;
                    case ROTATE:
                    /*ItemStack offhand_stack2 = player.getOffhandItem();
                    if (!shift && !offhand_stack2.isEmpty() && offhand_stack2.getItem() instanceof PaletteItem) {
                        PaletteItem.nextMode(offhand_stack2);
                        player.displayClientMessage(Compat.literal("Palette mode: " + PaletteItem.getMode(offhand_stack2)), false);
                    } else {*/
                        WandProps.nextRotation(main_stack);
                        WandProps.setStateMode(main_stack, WandProps.StateMode.APPLY);
//                    }
                        break;
                    case UNDO:
                        if (creative && !player.level.isClientSide()) {
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
                }

            }
        }
        if(!main_stack.isEmpty() && main_stack.getItem() instanceof PaletteItem){
            if (key >= 0 && key < WandKeys.values().length) {
                if (Objects.requireNonNull(WandKeys.values()[key]) == WandKeys.PALETTE_MODE) {
                    PaletteItem.nextMode(main_stack);
                    player.displayClientMessage(Compat.literal("Palette mode: " + PaletteItem.getMode(main_stack)), false);
                }
            }
        }
        if(is_wand){
            if(key<0){
                Wand wand=null;
                if(!player.level.isClientSide()){
                    wand=PlayerWand.get(player);
                    if(wand==null){
                        PlayerWand.add_player(player);
                        wand=PlayerWand.get(player);
                    }
                }
                if(wand!=null){
                    wand.is_alt_pressed=alt;
                    wand.is_shift_pressed=shift;
                }
            }
        }
    }
    public static void log(String s,boolean b){
        if(b){
            LOGGER.info(s);
        }
    }
}
