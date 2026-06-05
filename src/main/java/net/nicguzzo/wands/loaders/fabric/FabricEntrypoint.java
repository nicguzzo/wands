//? if fabric {
package net.nicguzzo.wands.loaders.fabric;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

//?if >26.1{
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
//?}else{
/*import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
*///?}
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import java.util.Optional;
import java.nio.file.Path;


import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.nicguzzo.wands.compat.RcId;
import net.nicguzzo.wands.menues.*;
import org.slf4j.Logger;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.WandsCommon;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.client.screens.MagicBagScreen;
import net.nicguzzo.wands.client.screens.PaletteScreen;
import net.nicguzzo.wands.client.screens.WandToolScreen;
import net.nicguzzo.wands.client.screens.WandScreen;

public class FabricEntrypoint implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    //?if >1.21{
    public static final ExtendedMenuType<WandToolsMenu, WandToolsMenuData> WAND_TOOLS_MENU_TYPE =
        new ExtendedMenuType<WandToolsMenu, WandToolsMenuData>(WandToolsMenu::new, WandToolsMenuData.STREAM_CODEC);

    public static final ExtendedMenuType<MagicBagMenu, MagicBagMenuData> MAGIC_BAG_MENU_TYPE =
        new ExtendedMenuType<MagicBagMenu, MagicBagMenuData>(MagicBagMenu::new, MagicBagMenuData.STREAM_CODEC);

    public static final ExtendedMenuType<PaletteMenu, PaletteMenuData> PALETTE_MENU_TYPE =
        new ExtendedMenuType<PaletteMenu,PaletteMenuData>(PaletteMenu::new, PaletteMenuData.STREAM_CODEC);

    //?}else{
    /*public static final ExtendedMenuType<WandToolsMenu> WAND_TOOLS_MENU_TYPE =
        new ExtendedMenuType<>(WandToolsMenu::new);
    public static final ExtendedMenuType<MagicBagMenu> MAGIC_BAG_MENU_TYPE =
        new ExtendedMenuType<>(MagicBagMenu::new);
    public static final ExtendedMenuType<PaletteMenu> PALETTE_MENU_TYPE =
        new ExtendedMenuType<>(PaletteMenu::new);
    *///?}
    @Override
    public void onInitialize() {

        WandsMod.is_fabric=true;
        Path configDir = FabricLoader.getInstance().getConfigDir();
        WandsConfig.configDir=configDir.toString();

        WandsMod.has_opac=FabricLoader.getInstance().getModContainer("openpartiesandclaims").isPresent();
        WandsMod.log("Has opac!! "+WandsMod.has_opac,true);

        WandsMod.has_ftbchunks=FabricLoader.getInstance().getModContainer("ftbchunks").isPresent();
        WandsMod.log("Has ftbchunks!! "+WandsMod.has_ftbchunks,true);

        WandsMod.has_flan=FabricLoader.getInstance().getModContainer("flan").isPresent();
        WandsMod.log("Has flan!! "+WandsMod.has_flan,true);

        WandsMod.has_goml=FabricLoader.getInstance().getModContainer("goml").isPresent();
        WandsMod.log("Has goml!! "+WandsMod.has_goml,true);

        Registry.register(BuiltInRegistries.MENU, RcId.fromNamespaceAndPath("wands", "wand_tools_menu").id(), WAND_TOOLS_MENU_TYPE);
        Registry.register(BuiltInRegistries.MENU, RcId.fromNamespaceAndPath("wands", "magic_bag_menu").id(), MAGIC_BAG_MENU_TYPE);
        Registry.register(BuiltInRegistries.MENU, RcId.fromNamespaceAndPath("wands", "palette_menu").id(), PALETTE_MENU_TYPE);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            WandsCommon.onServerStarted(server);
        });
        WandsMod.init();
    }
}
//?}
