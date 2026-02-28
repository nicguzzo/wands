package net.nicguzzo.wands.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.PaletteClientTooltip;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.screens.MagicBagScreen;
import net.nicguzzo.wands.client.screens.PaletteScreen;
import net.nicguzzo.wands.client.screens.WandToolScreen;

import java.util.Optional;


public class WandsModClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WandsModClient.initialize();
        TooltipComponentCallback.EVENT.register(PaletteClientTooltip::tryCreate);

        MenuScreens.register(WandsMod.PALETTE_CONTAINER.get(), PaletteScreen::new);
        MenuScreens.register(WandsMod.WAND_CONTAINER.get(), WandToolScreen::new);
        MenuScreens.register(WandsMod.MAGIC_WAND_CONTANIER.get(), MagicBagScreen::new);

        Optional<ModContainer> cont= FabricLoader.getInstance().getModContainer("optifabric");
        if(cont.isPresent()){
            WandsModClient.has_optifine=true;
            //WandsMod.log("has optifine!!!!!!!!!!!!",true);
        }
        Optional<ModContainer> opac= FabricLoader.getInstance().getModContainer("openpartiesandclaims");
         if(opac.isPresent()){
            WandsModClient.has_opac=true;
            //WandsMod.log("cli has opac!!!!!!!!!!!!",true);
        }else{
             //WandsMod.log("cli NO opac!!!!!!!!!!!!",true);
         }
    }
}