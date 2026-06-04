//? if fabric {
package net.nicguzzo.wands.loaders.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screens.MenuScreens;
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
        ClientTooltipComponentCallback.EVENT.register(PaletteClientTooltip::tryCreate);

        MenuScreens.register(WandsMod.PALETTE_MENU_TYPE, PaletteScreen::new);
        MenuScreens.register(WandsMod.WAND_MENU_TYPE, WandToolScreen::new);
        MenuScreens.register(WandsMod.MAGICBAG_MENU_TYPE, MagicBagScreen::new);

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
//?}
