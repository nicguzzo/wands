//? if forge {
/*package net.nicguzzo.deepslateinstamine.loaders.forge;

import org.slf4j.Logger;
import java.nio.file.Path;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.nbt.CompoundTag;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.WandsCommon;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.client.PaletteClientTooltip;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.screens.MagicBagScreen;
import net.nicguzzo.wands.client.screens.PaletteScreen;
import net.nicguzzo.wands.client.screens.WandToolScreen;
import net.nicguzzo.wands.items.PaletteTooltip;

@Mod("wands")
public class ForgeEntrypoint {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ForgeEntrypoint() {
        WandsMod.is_forge=true;
        MinecraftForge.EVENT_BUS.register(this);
    }
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        Path configDir = FMLPaths.CONFIGDIR.get();
        WandsConfig.configDir=configDir.toString();
        WandsCommon.onServerStarted(event.getServer());
    }
    @SubscribeEvent
    private void onCommonSetup(FMLCommonSetupEvent event) {
        WandsMod.has_opac = ModList.get().isLoaded("openpartiesandclaims");
        WandsMod.log("Has opac!! " + WandsMod.has_opac, true);

        WandsMod.has_ftbchunks = ModList.get().isLoaded("ftbchunks");
        WandsMod.log("Has ftbchunks!! " + WandsMod.has_ftbchunks, true);

        WandsMod.has_flan = ModList.get().isLoaded("flan");
        WandsMod.log("Has flan!! " + WandsMod.has_flan, true);
    }
    @SubscribeEvent
    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            WandsModClient.initialize();
        });
    }
}
*///?}
