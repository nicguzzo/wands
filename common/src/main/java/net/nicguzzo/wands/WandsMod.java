package net.nicguzzo.wands;

//beginMC1_16_5
import me.shedaniel.architectury.event.events.PlayerEvent;
import me.shedaniel.architectury.registry.*;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.NetworkManager.Side;
//endMC1_16_5 

/*//beginMC1_17_1
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registries;
import dev.architectury.registry.registries.RegistrySupplier;
//endMC1_17_1*/

import io.netty.buffer.Unpooled;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
            return new WandItem(config.stone_wand_limit,false,false,new Item.Properties().durability(config.stone_wand_durability).tab(WandsMod.WANDS_TAB));
    });
    public static final RegistrySupplier<Item> IRON_WAND_ITEM = ITEMS.register("iron_wand", () ->{
        return new WandItem(config.iron_wand_limit,false,false,new Item.Properties().durability(config.iron_wand_durability).tab(WandsMod.WANDS_TAB));
    });
    public static final RegistrySupplier<Item> DIAMOND_WAND_ITEM = ITEMS.register("diamond_wand", () ->{
        return new WandItem(config.diamond_wand_limit,true,false,new Item.Properties().durability(config.diamond_wand_durability).tab(WandsMod.WANDS_TAB));
    });
    public static final RegistrySupplier<Item> NETHERITE_WAND_ITEM = ITEMS.register("netherite_wand", () ->{
        return new WandItem(config.netherite_wand_limit,true,true,new Item.Properties().fireResistant().durability(config.netherite_wand_durability).tab(WandsMod.WANDS_TAB));
    });

    public static final RegistrySupplier<Item> CREATIVE_WAND_ITEM = ITEMS.register("creative_wand", () ->{
        return new WandItem(Wand.MAX_LIMIT,true,true,new Item.Properties().fireResistant().stacksTo(1).tab(WandsMod.WANDS_TAB));
    });

    public static final RegistrySupplier<Item> PALETTE_ITEM = ITEMS.register("palette", () ->{
        return new PaletteItem(new Item.Properties().stacksTo(1).tab(WandsMod.WANDS_TAB));
    });

    
    
    public static final RegistrySupplier<MenuType<PaletteScreenHandler>> PALETTE_SCREEN_HANDLER=MENUES.register("palette_menu",()-> MenuRegistry.ofExtended(PaletteScreenHandler::new));

    static public ResourceLocation KB_PACKET= new ResourceLocation(MOD_ID, "key_packet");
    static public ResourceLocation SND_PACKET= new ResourceLocation(MOD_ID, "sound_packet");
    static public ResourceLocation PALETTE_PACKET= new ResourceLocation(MOD_ID, "palette_packet");
    static public ResourceLocation STATE_PACKET= new ResourceLocation(MOD_ID, "state_packet");

    static final public int wand_mode_key        = GLFW.GLFW_KEY_V;
    static final public int wand_orientation_key = GLFW.GLFW_KEY_X;
    static final public int wand_invert_key      = GLFW.GLFW_KEY_I;
    static final public int wand_fill_circle_key = GLFW.GLFW_KEY_K;
    static final public int palette_mode_key     = GLFW.GLFW_KEY_R;
    static final public int wand_undo            = GLFW.GLFW_KEY_U;
	
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
                packet.writeLong(wand.palette_seed);
                packet.writeInt(wand.axis.ordinal());
                packet.writeInt(wand.plane.ordinal());
                packet.writeInt(mode.ordinal());
                if (wand.palette_slots.size() != 0) {
                    packet.writeInt((wand.slot + 1) % wand.palette_slots.size());
                } else {
                    packet.writeInt(0);
                }
                float BLOCKS_PER_XP = WandsMod.config.blocks_per_xp;
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
        ItemStack item_stack=player.getMainHandItem();
        if(!item_stack.isEmpty() && item_stack.getItem() instanceof WandItem){
            boolean creative=MCVer.inst.is_creative(player);
            
            Wand wand=PlayerWand.get(player);
            switch(key){
                case wand_mode_key:
                    if(shift){
                        WandItem.prevMode(item_stack);
                    }else {
                        WandItem.nextMode(item_stack);
                    }
                    //player.displayClientMessage(new TextComponent("Wand mode: "+WandItem.getMode(item_stack).toString()),false);
                break;
                case wand_orientation_key:
                    if(alt){//change axis

                        if(wand!=null){
                            Direction.Axis a=wand.axis;
                            int n=(wand.axis.ordinal()+1)%3;
                            wand.axis=Direction.Axis.values()[n];
                            player.displayClientMessage(new TextComponent("Wand Axis: " + wand.axis), false);
                            send_state((ServerPlayer) player,wand);
                        }
                    }else {
                        switch (WandItem.getMode(item_stack)) {
                            case CIRCLE:
                            case RECT:
                                WandItem.nextPlane(item_stack);
                                wand.plane=WandItem.getPlane(item_stack);
                                player.displayClientMessage(new TextComponent("Wand Plane: " + WandItem.getPlane(item_stack)), false);
                                send_state((ServerPlayer) player,wand);
                                break;
                            case DIRECTION:
                            case PASTE:

                                break;
                            default:
                                WandItem.nextOrientation(item_stack);
                                player.displayClientMessage(new TextComponent("Wand Orientation: " + WandItem.getOrientation(item_stack).toString().toLowerCase()), false);
                                break;
                        }
                    }

                break;
                case wand_invert_key:
                    WandItem.invert(item_stack);
                    player.displayClientMessage(new TextComponent("Wand inverted: "+WandItem.isInverted(item_stack)),false);
                break;
                case wand_fill_circle_key:
                    WandItem.toggleCircleFill(item_stack);
                    player.displayClientMessage(new TextComponent("Wand circle fill: "+WandItem.isCircleFill(item_stack)),false);
                break;
                case palette_mode_key:
                    ItemStack offhand_stack=player.getOffhandItem();
                    if(!shift&& !offhand_stack.isEmpty() && offhand_stack.getItem() instanceof PaletteItem){
                        PaletteItem.nextMode(offhand_stack);
                        //LOGGER.info("1 palette tag: "+ offhand_stack.getTag());
                        player.displayClientMessage(new TextComponent("Palette mode: "+PaletteItem.getMode(offhand_stack)),false);
                    }else{
                        WandItem.nextRotation(item_stack);
                        int r=WandItem.getRotation(item_stack);
                        String rot="0°";
                        switch(r) {
                            case 1:
                                rot="90°";
                                break;
                            case 2:
                                rot="180°";
                                break;
                            case 3:
                                rot="270°";
                                break;
                        }
                        player.displayClientMessage(new TextComponent("Wand Rotation: "+ rot),false);
                    }

                break;
                case wand_undo:                   

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
                    //WandItem.nextMode(item_stack);
                break;
            }
            //LOGGER.info("wand tag: "+ item_stack.getTag());
        }
        if(!item_stack.isEmpty() && item_stack.getItem() instanceof PaletteItem){
            switch(key){
                case palette_mode_key:
                    PaletteItem.nextMode(item_stack);
                    player.displayClientMessage(new TextComponent("Palette mode: "+PaletteItem.getMode(item_stack)),false);
                    //LOGGER.info("2 palette tag: "+ item_stack.getTag());
                break;
            }
        }
        if(key==-1){
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
                //WandsMod.LOGGER.info("got shift "+shift +" alt "+alt);
            }
        }
    }
    public static void log(String s,boolean b){
        if(b){
            LOGGER.info(s);
        }
    }
}
