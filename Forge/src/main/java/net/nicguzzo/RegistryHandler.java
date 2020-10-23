package net.nicguzzo;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

//import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class RegistryHandler {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WandsMod.MODID);
    
    public static void init() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    public static final RegistryObject<Item> STONE_WAND = ITEMS.register("stone_wand", () ->
        new WandItemForge(7,new Item.Properties().group(ItemGroup.TOOLS).maxDamage(131)) 
    );   
    public static final RegistryObject<Item> IRON_WAND = ITEMS.register("iron_wand", () ->
        new WandItemForge(17,new Item.Properties().group(ItemGroup.TOOLS).maxDamage(250)) 
    );   
    public static final RegistryObject<Item> DIAMOND_WAND = ITEMS.register("diamond_wand", () ->
        new WandItemForge(27,new Item.Properties().group(ItemGroup.TOOLS).maxDamage(1561)) 
    );   
    public static final RegistryObject<Item> NETHERITE_WAND = ITEMS.register("netherite_wand", () ->
        new WandItemForge(37,new Item.Properties().group(ItemGroup.TOOLS).maxDamage(2031)) 
    );   
}