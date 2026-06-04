//? if fabric {
package net.nicguzzo.deepslateinstamine.loaders.fabric;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import java.util.Optional;
import java.nio.file.Path;
import org.slf4j.Logger;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.WandsCommon;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.client.screens.MagicBagScreen;
import net.nicguzzo.wands.client.screens.PaletteScreen;
import net.nicguzzo.wands.client.screens.WandToolScreen;


public class FabricEntrypoint implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {

        WandsMod.is_fabric=true;
        WandsMod.has_opac=FabricLoader.getInstance().getModContainer("openpartiesandclaims").isPresent();
        WandsMod.log("Has opac!! "+WandsMod.has_opac,true);

        WandsMod.has_ftbchunks=FabricLoader.getInstance().getModContainer("ftbchunks").isPresent();
        WandsMod.log("Has ftbchunks!! "+WandsMod.has_ftbchunks,true);

        WandsMod.has_flan=FabricLoader.getInstance().getModContainer("flan").isPresent();
        WandsMod.log("Has flan!! "+WandsMod.has_flan,true);

        WandsMod.has_goml=FabricLoader.getInstance().getModContainer("goml").isPresent();
        WandsMod.log("Has goml!! "+WandsMod.has_goml,true);
        MenuScreens.register(WandsMod.PALETTE_MENU_TYPE, PaletteScreen::new);
        MenuScreens.register(WandsMod.WAND_MENU_TYPE, WandToolScreen::new);
        MenuScreens.register(WandsMod.MAGICBAG_MENU_TYPE, MagicBagScreen::new);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            WandsConfig.configDir=configDir.toString();
            WandsCommon.onServerStarted(server);
        });

    }
}
//?}
