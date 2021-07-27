package net.nicguzzo.wands;


import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.NetworkManager.Side;
import me.shedaniel.architectury.registry.CreativeTabs;
import me.shedaniel.architectury.registry.DeferredRegister;
import me.shedaniel.architectury.registry.MenuRegistry;
import me.shedaniel.architectury.registry.Registries;
import me.shedaniel.architectury.registry.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class WandsMod {
    public static WandsConfig config=WandsConfig.get_instance();
    public static final String MOD_ID = "wands";
    
    public static final Logger LOGGER = LogManager.getLogger();
    // We can use this if we don't want to use DeferredRegister
    public static final LazyLoadedValue<Registries> REGISTRIES = new LazyLoadedValue<>(() -> Registries.get(MOD_ID));
    // Registering a new creative tab
    public static final CreativeModeTab WANDS_TAB = CreativeTabs.create(new ResourceLocation(MOD_ID, "wands_tab"), new Supplier<ItemStack>() {
        @Override
        public ItemStack get() {
            return new ItemStack(STONE_WAND_ITEM.get());
        }
    });
    
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

    public static final RegistrySupplier<Item> PALETTE_ITEM = ITEMS.register("palette", () ->{
        return new PaletteItem(new Item.Properties().stacksTo(1).tab(WandsMod.WANDS_TAB));
    });

    public static final RegistrySupplier<MenuType<PaletteScreenHandler>> PALETTE_SCREEN_HANDLER=MENUES.register("palette_menu",()-> MenuRegistry.ofExtended(PaletteScreenHandler::new));

    static public ResourceLocation KB_PACKET= new ResourceLocation(MOD_ID, "key_packet");
    static public ResourceLocation SND_PACKET= new ResourceLocation(MOD_ID, "sound_packet");
    
    static final public int wand_mode_key        = GLFW.GLFW_KEY_V;
    static final public int wand_orientation_key = GLFW.GLFW_KEY_X;
    static final public int wand_invert_key      = GLFW.GLFW_KEY_I;
    static final public int wand_fill_circle_key = GLFW.GLFW_KEY_K;
    static final public int palette_mode_key     = GLFW.GLFW_KEY_R;

	public static HashMap<String, CircularBuffer> player_undo = new HashMap<String, CircularBuffer>();
    public static void init() {
        
        ITEMS.register();
        MENUES.register();
        System.out.println(WandsExpectPlatform.getConfigDirectory().toAbsolutePath().normalize().toString());
        //NetworkReceiver
        NetworkManager.registerReceiver(Side.C2S, KB_PACKET, (packet,context)->{
            int key=packet.readInt();
            //LOGGER.info("key from client: "+key);
            context.queue(()->{
                process_keys(context.getPlayer(), key);
            });
        });
    }
    public static void process_keys(Player player,int key){
        ItemStack item_stack=player.getMainHandItem();
        if(!item_stack.isEmpty() && item_stack.getItem() instanceof WandItem){               
            
            switch(key){
                case wand_mode_key:
                    WandItem.nextMode(item_stack);
                break;
                case wand_orientation_key:
                    int mode=WandItem.getMode(item_stack);
                    if(mode==5){
                        WandItem.nextPlane(item_stack);
                    }else{
                        WandItem.nextOrientation(item_stack);
                    }
                break;
                case wand_invert_key:
                    WandItem.invert(item_stack);
                break;
                case wand_fill_circle_key:
                    WandItem.toggleCircleFill(item_stack);
                break;
                case palette_mode_key:
                    ItemStack offhand_stack=player.getOffhandItem();
                    if(!offhand_stack.isEmpty() && offhand_stack.getItem() instanceof PaletteItem){
                        PaletteItem.nextMode(offhand_stack);
                        //LOGGER.info("1 palette tag: "+ offhand_stack.getTag());
                    }
                break;
            }
            //LOGGER.info("wand tag: "+ item_stack.getTag());
        }
        if(!item_stack.isEmpty() && item_stack.getItem() instanceof PaletteItem){       
            switch(key){
                case palette_mode_key:
                    PaletteItem.nextMode(item_stack);
                    //LOGGER.info("2 palette tag: "+ item_stack.getTag());
                break;
            }
        }
    }
}
