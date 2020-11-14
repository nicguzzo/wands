package net.nicguzzo;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

//import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import java.nio.file.Path;
import net.nicguzzo.common.WandsConfig;

public class RegistryHandler {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WandsMod.MODID);
    
    public static void init() {
        //Path path= FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath());
        Path path= FMLPaths.GAMEDIR.get().resolve("config");
        //LOGGER.info("config path:" + path);
        WandsMod.config=WandsConfig.load_config(path);
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    public static final RegistryObject<Item> STONE_WAND = ITEMS.register("stone_wand", () ->
        new WandItemForge(WandsMod.config.stone_wand_limit,false,false,new Item.Properties().group(ItemGroup.TOOLS).maxDamage(WandsMod.config.stone_wand_durability)) 
    );   
    public static final RegistryObject<Item> IRON_WAND = ITEMS.register("iron_wand", () ->
        new WandItemForge(WandsMod.config.iron_wand_limit,true,false,new Item.Properties().group(ItemGroup.TOOLS).maxDamage(WandsMod.config.iron_wand__durability)) 
    );   
    public static final RegistryObject<Item> DIAMOND_WAND = ITEMS.register("diamond_wand", () ->
        new WandItemForge(WandsMod.config.diamond_wand_limit,true,false,new Item.Properties().group(ItemGroup.TOOLS).maxDamage(WandsMod.config.diamond_wand__durability)) 
    );   
    public static final RegistryObject<Item> NETHERITE_WAND = ITEMS.register("netherite_wand", () ->
        new WandItemForge(WandsMod.config.netherite_wand_limit,true,true,new Item.Properties().group(ItemGroup.TOOLS).maxDamage(WandsMod.config.netherite_wand_durability)) 
    );   
}