//? if forge {
/*package net.nicguzzo.wands.loaders.forge;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nicguzzo.wands.client.screens.MagicBagScreen;
import net.nicguzzo.wands.client.screens.PaletteScreen;
import net.nicguzzo.wands.menues.MagicBagMenu;
import net.nicguzzo.wands.menues.PaletteMenu;
import net.nicguzzo.wands.menues.WandToolsMenu;

import java.nio.file.Path;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.WandsCommon;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.screens.WandToolScreen;

@Mod("wands")
public class ForgeEntrypoint {


    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "wands");

    public static final RegistryObject<MenuType<WandToolsMenu>> WAND_TOOLS_MENU_TYPE = MENUS.register("wand_tools_menu",
            () -> IForgeMenuType.create(WandToolsMenu::new));

    public static final RegistryObject<MenuType<MagicBagMenu>> MAGIC_BAG_MENU_TYPE = MENUS.register("magic_bag_menu",
            () -> IForgeMenuType.create(MagicBagMenu::new));

    public static final RegistryObject<MenuType<PaletteMenu>> PALETTE_MENU_TYPE = MENUS.register("palette_menu",
            () -> IForgeMenuType.create(PaletteMenu::new));

    //public static RegistryObject<MenuType<WandToolsMenu>> WAND_TOOLS_MENU_TYPE;
    //public static RegistryObject<MenuType<MagicBagMenu>> MAGIC_BAG_MENU_TYPE;
    //public static RegistryObject<MenuType<PaletteMenu>> PALETTE_MENU_TYPE;

    public ForgeEntrypoint() {
        WandsMod.is_forge=true;
        Path configDir = FMLPaths.CONFIGDIR.get();
        WandsConfig.configDir=configDir.toString();

        MinecraftForge.EVENT_BUS.register(this);

    }
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        WandsCommon.onServerStarted(event.getServer());
    }
    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        WandsMod.has_opac = ModList.get().isLoaded("openpartiesandclaims");
        WandsMod.log("Has opac!! " + WandsMod.has_opac, true);

        WandsMod.has_ftbchunks = ModList.get().isLoaded("ftbchunks");
        WandsMod.log("Has ftbchunks!! " + WandsMod.has_ftbchunks, true);

        WandsMod.has_flan = ModList.get().isLoaded("flan");
        WandsMod.log("Has flan!! " + WandsMod.has_flan, true);
    }
    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(WAND_TOOLS_MENU_TYPE.get(), WandToolScreen::new);
            MenuScreens.register(MAGIC_BAG_MENU_TYPE.get(), MagicBagScreen::new);
            MenuScreens.register(PALETTE_MENU_TYPE.get(), PaletteScreen::new);
            WandsModClient.initialize();
        });
    }
}
*///?}
