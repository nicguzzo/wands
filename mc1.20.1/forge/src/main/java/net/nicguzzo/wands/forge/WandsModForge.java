package net.nicguzzo.wands.forge;

#if MC=="1165"
import me.shedaniel.architectury.platform.forge.EventBuses;
import me.shedaniel.architectury.utils.Env;
import me.shedaniel.architectury.utils.EnvExecutor;
#else
import dev.architectury.platform.forge.EventBuses;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
#endif

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.PaletteClientTooltip;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.screens.MagicBagScreen;
import net.nicguzzo.wands.client.screens.PaletteScreen;
import net.nicguzzo.wands.client.screens.WandToolScreen;
import net.nicguzzo.wands.items.PaletteTooltip;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
@Mod(WandsMod.MOD_ID)

public class WandsModForge {


    public WandsModForge() {
        WandsMod.is_forge=true;
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(WandsMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        WandsMod.init();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        EnvExecutor.runInEnv(Env.CLIENT, () ->
            ()-> {
                WandsModClient.initialize();
            }
        );
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(
            (net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent e) -> e.register(PaletteTooltip.class, PaletteClientTooltip::new));
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        WandsMod.has_opac = ModList.get().isLoaded("openpartiesandclaims");
        WandsMod.log("Has opac!! " + WandsMod.has_opac, true);

        WandsMod.has_ftbchunks = ModList.get().isLoaded("ftbchunks");
        WandsMod.log("Has ftbchunks!! " + WandsMod.has_ftbchunks, true);

        WandsMod.has_flan = ModList.get().isLoaded("flan");
        WandsMod.log("Has flan!! " + WandsMod.has_flan, true);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(WandsMod.PALETTE_CONTAINER.get(), PaletteScreen::new);
            MenuScreens.register(WandsMod.WAND_CONTAINER.get(), WandToolScreen::new);
            MenuScreens.register(WandsMod.MAGIC_WAND_CONTANIER.get(), MagicBagScreen::new);
        });
    }
}
