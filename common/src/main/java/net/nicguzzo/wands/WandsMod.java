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

    static public ResourceLocation KB_PACKET= new ResourceLocation(MOD_ID, "key");
    
    

	public static HashMap<String, CircularBuffer> player_undo = new HashMap<String, CircularBuffer>();
    public static void init() {
        
        ITEMS.register();
        MENUES.register();
        System.out.println(WandsExpectPlatform.getConfigDirectory().toAbsolutePath().normalize().toString());
        //NetworkReceiver
        NetworkManager.registerReceiver(Side.C2S, KB_PACKET, (packet,context)->{
            int key=packet.readInt();
            LOGGER.info("key from client: "+key);
            context.queue(()->{
                ItemStack item_stack=context.getPlayer().getMainHandItem();
                if(!item_stack.isEmpty() && item_stack.getItem() instanceof WandItem){
                    switch(key){
                        case GLFW.GLFW_KEY_V:
                            WandItem.nextMode(item_stack);
                        break;
                    }
                }
            });
        });
    }
}
