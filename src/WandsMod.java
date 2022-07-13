package net.nicguzzo.wands;

#if MC=="1165"
import me.shedaniel.architectury.event.events.PlayerEvent;
import me.shedaniel.architectury.registry.*;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.NetworkManager.Side;
#else
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registries;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.nicguzzo.wands.mcver.MCVer;

public class WandsMod {   

    public static final WandsConfig config=WandsConfig.get_instance();
    public static final String MOD_ID = "wands";
    
    public static final Logger LOGGER = LogManager.getLogger();
    // We can use this if we don't want to use DeferredRegister
    public static final LazyLoadedValue<Registries> REGISTRIES = new LazyLoadedValue<>(() -> Registries.get(MOD_ID));

    public static final CreativeModeTab WANDS_TAB = MCVer.inst.create_tab(new ResourceLocation(MOD_ID, "wands_tab"));

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registry.ITEM_REGISTRY);
    public static final DeferredRegister<MenuType<?>> MENUES = DeferredRegister.create(MOD_ID, Registry.MENU_REGISTRY);

    public static final RegistrySupplier<Item> STONE_WAND_ITEM = ITEMS.register("stone_wand", () ->{
            return new WandItem(Tiers.STONE,config.stone_wand_limit,false,false,false,false,new Item.Properties().durability(config.stone_wand_durability).tab(WandsMod.WANDS_TAB));
    });
    public static final RegistrySupplier<Item> IRON_WAND_ITEM = ITEMS.register("iron_wand", () ->{
        return new WandItem(Tiers.IRON ,config.iron_wand_limit,false,false,false,false,new Item.Properties().durability(config.iron_wand_durability).tab(WandsMod.WANDS_TAB));
    });
    public static final RegistrySupplier<Item> DIAMOND_WAND_ITEM = ITEMS.register("diamond_wand", () ->{
        return new WandItem(Tiers.DIAMOND,config.diamond_wand_limit,true,false,false,false,new Item.Properties().durability(config.diamond_wand_durability).tab(WandsMod.WANDS_TAB));
    });
    public static final RegistrySupplier<Item> NETHERITE_WAND_ITEM = ITEMS.register("netherite_wand", () ->{
        return new WandItem(Tiers.NETHERITE,config.netherite_wand_limit,true,true,false,true,new Item.Properties().fireResistant().durability(config.netherite_wand_durability).tab(WandsMod.WANDS_TAB));
    });

    public static final RegistrySupplier<Item> CREATIVE_WAND_ITEM = ITEMS.register("creative_wand", () ->{
        return new WandItem(Tiers.NETHERITE,Wand.MAX_LIMIT,true,true,true,true,new Item.Properties().fireResistant().stacksTo(1).tab(WandsMod.WANDS_TAB));
    });

    public static final RegistrySupplier<Item> PALETTE_ITEM = ITEMS.register("palette", () ->{
        return new PaletteItem(new Item.Properties().stacksTo(1).tab(WandsMod.WANDS_TAB));
    });
    
    public static final RegistrySupplier<MenuType<PaletteScreenHandler>> PALETTE_SCREEN_HANDLER=MENUES.register("palette_menu",()-> MenuRegistry.ofExtended(PaletteScreenHandler::new));
    public static final RegistrySupplier<MenuType<WandScreenHandler>> WAND_SCREEN_HANDLER=MENUES.register("wand_menu",()-> MenuRegistry.ofExtended(WandScreenHandler::new));

    static public ResourceLocation KB_PACKET= new ResourceLocation(MOD_ID, "key_packet");
    static public ResourceLocation SND_PACKET= new ResourceLocation(MOD_ID, "sound_packet");
    static public ResourceLocation PALETTE_PACKET= new ResourceLocation(MOD_ID, "palette_packet");
    static public ResourceLocation STATE_PACKET= new ResourceLocation(MOD_ID, "state_packet");
    static public ResourceLocation WAND_PACKET= new ResourceLocation(MOD_ID, "wand_packet");
    static public ResourceLocation POS_PACKET= new ResourceLocation(MOD_ID, "pos_packet");

    static final public int wand_menu_key        = GLFW.GLFW_KEY_Y;
    static final public int wand_mode_key        = GLFW.GLFW_KEY_V;
    static final public int wand_action_key      = GLFW.GLFW_KEY_H;
    static final public int wand_orientation_key = GLFW.GLFW_KEY_X;
    static final public int wand_undo_key        = GLFW.GLFW_KEY_U;
    static final public int wand_invert_key      = GLFW.GLFW_KEY_I;
    static final public int wand_fill_circle_key = GLFW.GLFW_KEY_K;
    static final public int palette_mode_key     = GLFW.GLFW_KEY_R;
    static final public int palette_menu_key     = GLFW.GLFW_KEY_J;
    //static final public int wand_state_mode_key  = GLFW.GLFW_KEY_B;
    static final public int wand_conf_key  = GLFW.GLFW_KEY_UNKNOWN;
    static final public int wand_m_inc_key = GLFW.GLFW_KEY_RIGHT;
    static final public int wand_m_dec_key = GLFW.GLFW_KEY_LEFT;
    static final public int wand_n_inc_key = GLFW.GLFW_KEY_UP;
    static final public int wand_n_dec_key = GLFW.GLFW_KEY_DOWN;
    static final public int toggle_stair_slab_key = GLFW.GLFW_KEY_PERIOD;
    static final public int area_diagonal_spread = GLFW.GLFW_KEY_COMMA;
    static final public int inc_sel_block=GLFW.GLFW_KEY_Z;
    public static boolean is_forge=false;
	
    public static void init() {
        
        ITEMS.register();
        MENUES.register();

        NetworkManager.registerReceiver(Side.C2S, KB_PACKET, (packet,context)->{
            int key=packet.readInt();
            boolean shift=packet.readBoolean();
            boolean alt=packet.readBoolean();
            context.queue(()->{
                process_keys(context.getPlayer(), key,shift,alt);
            });
        });
        NetworkManager.registerReceiver(Side.C2S, PALETTE_PACKET, (packet,context)->{
            boolean mode=packet.readBoolean();
            boolean rotate=packet.readBoolean();
            context.queue(()->{
                process_palette(context.getPlayer(), mode,rotate);
            });
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
            context.queue(()->{
                ItemStack stack=context.getPlayer().getMainHandItem();
                if(WandItem.is_wand(stack)){
                    BlockPos pos=hitResult.getBlockPos();
                    Direction side=hitResult.getDirection();
                    Player player=context.getPlayer();
                    if(player!=null) {
                        Wand wand = PlayerWand.get(player);
                        if(wand!=null) {
                            Level level=player.level;
                            BlockState block_state=level.getBlockState(pos);
                            wand.p1=p1;
                            WandItem.Mode mode=wand.mode;
                            if (    mode == WandItem.Mode.FILL   || mode == WandItem.Mode.LINE ||
                                    mode == WandItem.Mode.CIRCLE || mode == WandItem.Mode.COPY ||
                                    mode == WandItem.Mode.RECT) {
                                if (WandItem.getIncSelBlock(stack)) {
                                    pos = pos.relative(side, 1);
                                }
                            }
                            wand.p2=p2;
                            wand.do_or_preview(player,level, block_state,pos, side, hitResult.getLocation(), stack,true);
                            wand.clear();
                        }
                    }
                }
            });
        });
        PlayerEvent.PLAYER_QUIT.register((player)->{
            LOGGER.info("PLAYER_QUIT");
            PlayerWand.remove_player(player);
        });
    }
    public static void send_state(ServerPlayer player,Wand wand){
        if(wand!=null && player!=null && !player.level.isClientSide()) {
            ItemStack wand_stack=player.getMainHandItem();
            if(wand_stack.getItem() instanceof WandItem) {
                WandItem.Mode mode = WandItem.getMode(wand_stack);
                FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
                int slot=0;
                if (wand.palette_slots.size() != 0) {
                    slot=(wand.slot + 1) % wand.palette_slots.size();
                }
                float BLOCKS_PER_XP = WandsMod.config.blocks_per_xp;

                packet.writeLong(wand.palette_seed);
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
        boolean creative=MCVer.inst.is_creative(player);
        if(is_palette){
            switch(key) {
                case palette_menu_key: {
                    if (offhand_stack.getItem() instanceof PaletteItem) {
                        MCVer.inst.open_palette((ServerPlayer) player, offhand_stack);
                    } else {
                        ItemStack mainhand_stack = player.getMainHandItem();
                        if (mainhand_stack.getItem() instanceof PaletteItem) {
                            MCVer.inst.open_palette((ServerPlayer) player, mainhand_stack);
                        }
                    }
                }
                break;
                case palette_mode_key:{
                    if (!shift && !offhand_stack.isEmpty() && offhand_stack.getItem() instanceof PaletteItem) {
                        PaletteItem.nextMode(offhand_stack);
                        player.displayClientMessage(MCVer.inst.literal("Palette mode: " + PaletteItem.getMode(offhand_stack)), false);
                    }
                }
                break;
            }
        }
        if(is_wand){
            Wand wand=PlayerWand.get(player);
            WandItem.Mode mode= WandItem.getMode(main_stack);
            switch(key) {
                case inc_sel_block:
                    WandItem.setIncSelBlock(main_stack,!WandItem.getIncSelBlock(main_stack));
                    break;
                case area_diagonal_spread:
                    WandItem.setAreaDiagonalSpread(main_stack,!WandItem.getAreaDiagonalSpread(main_stack));
                    break;
                case toggle_stair_slab_key:
                    WandItem.setStateMode(main_stack, WandItem.StateMode.APPLY);
                    WandItem.setStairSlab(main_stack,!WandItem.getStairSlab(main_stack));
                break;
                case wand_n_inc_key:
                    if(mode==WandItem.Mode.GRID) {
                        WandItem.setGridMxN(main_stack, WandItem.getGridMxN(main_stack, false) + 1, false);
                    }
                break;
                case wand_n_dec_key:
                    if(mode==WandItem.Mode.GRID) {
                        WandItem.setGridMxN(main_stack, WandItem.getGridMxN(main_stack, false) - 1, false);
                    }
                break;
                case wand_m_inc_key:
                    if(mode==WandItem.Mode.DIRECTION) {
                        WandItem.setMultiplier(main_stack,WandItem.getMultiplier(main_stack)+1);
                    }else {
                        if(mode==WandItem.Mode.ROW_COL) {
                            WandItem.setRowColLimit(main_stack,WandItem.getRowColLimit(main_stack)+1);
                        }else {
                            if (mode == WandItem.Mode.GRID) {
                                WandItem.setGridMxN(main_stack, WandItem.getGridMxN(main_stack, true) + 1, true);
                            }
                        }
                    }
                    break;
                case wand_m_dec_key:
                    if(mode==WandItem.Mode.DIRECTION) {
                        WandItem.setMultiplier(main_stack,WandItem.getMultiplier(main_stack)-1);
                    }else {
                        if(mode==WandItem.Mode.ROW_COL) {
                            WandItem.setRowColLimit(main_stack,WandItem.getRowColLimit(main_stack)-1);
                        }else {
                            if (mode == WandItem.Mode.GRID) {
                                WandItem.setGridMxN(main_stack, WandItem.getGridMxN(main_stack, true) - 1, true);
                            }
                        }
                    }
                    break;
                case wand_action_key:
                    if (shift) {
                        WandItem.prevAction(main_stack);
                    } else {
                        WandItem.nextAction(main_stack);
                    }
                    player.displayClientMessage(MCVer.inst.literal("Wand PlaceMode: " + WandItem.getAction(main_stack)), false);
                    break;
                case wand_menu_key:
                    MCVer.inst.open_wand_menu((ServerPlayer) player, main_stack);
                    break;
                case wand_mode_key:
                    if (shift) {
                        WandItem.prevMode(main_stack);
                    } else {
                        WandItem.nextMode(main_stack);
                    }
                    break;
                case wand_orientation_key:
                    if (alt) {//change axis
                        if (wand != null) {
                            WandItem.nextAxis(main_stack);
                            WandItem.setStateMode(main_stack, WandItem.StateMode.APPLY);
                            Direction.Axis a=WandItem.getAxis(main_stack);
                            player.displayClientMessage(MCVer.inst.literal("Wand Axis: " + a), false);
                            send_state((ServerPlayer) player, wand);
                        }
                    } else {
                        switch (mode) {
                            case CIRCLE:
                            case RECT:
                                WandItem.nextPlane(main_stack);
                                player.displayClientMessage(MCVer.inst.literal("Wand Plane: " + WandItem.getPlane(main_stack)), false);
                                send_state((ServerPlayer) player, wand);
                                break;
                            case DIRECTION:
                            case PASTE:

                                break;
                            default:
                                WandItem.nextOrientation(main_stack);
                                player.displayClientMessage(MCVer.inst.literal("Wand Orientation: " + WandItem.getOrientation(main_stack).toString().toLowerCase()), false);
                                break;
                        }
                    }
                    break;
                case wand_invert_key:
                    WandItem.invert(main_stack);
                    player.displayClientMessage(MCVer.inst.literal("Wand inverted: " + WandItem.isInverted(main_stack)), false);
                    break;
                case wand_fill_circle_key:
                    WandItem.toggleCircleFill(main_stack);
                    player.displayClientMessage(MCVer.inst.literal("Wand circle fill: " + WandItem.isCircleFill(main_stack)), false);
                    break;
                case palette_mode_key:
                    ItemStack offhand_stack2 = player.getOffhandItem();
                    if (!shift && !offhand_stack2.isEmpty() && offhand_stack2.getItem() instanceof PaletteItem) {
                        PaletteItem.nextMode(offhand_stack2);
                        player.displayClientMessage(MCVer.inst.literal("Palette mode: " + PaletteItem.getMode(offhand_stack2)), false);
                    } else {
                        WandItem.nextRotation(main_stack);
                        WandItem.setStateMode(main_stack, WandItem.StateMode.APPLY);
                    }
                break;
                case wand_undo_key:
                    if(creative && !player.level.isClientSide()){
                        if(wand!=null){
                            int n=1;
                            if(alt){
                                n=10;
                            }
                            if(shift){
                                wand.redo(n);
                            }else{
                                wand.undo(n);
                            }
                        }
                    }
                break;
            }
        }
        if(!main_stack.isEmpty() && main_stack.getItem() instanceof PaletteItem){
            switch(key){
                case palette_mode_key:
                    PaletteItem.nextMode(main_stack);
                    player.displayClientMessage(MCVer.inst.literal("Palette mode: "+PaletteItem.getMode(main_stack)),false);
                break;
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
