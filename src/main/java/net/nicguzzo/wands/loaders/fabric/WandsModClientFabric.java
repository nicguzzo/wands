//? if fabric {
package net.nicguzzo.wands.loaders.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.PaletteClientTooltip;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.screens.MagicBagScreen;
import net.nicguzzo.wands.client.screens.PaletteScreen;
import net.nicguzzo.wands.client.screens.WandToolScreen;
import net.nicguzzo.wands.menues.WandToolsMenu;

import java.util.Optional;


public class WandsModClientFabric implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        WandsModClient.initialize();
        TooltipComponentCallback.EVENT.register(PaletteClientTooltip::tryCreate);


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

         MenuScreens.register(FabricEntrypoint.WAND_TOOLS_MENU_TYPE, WandToolScreen::new);
         MenuScreens.register(FabricEntrypoint.MAGIC_BAG_MENU_TYPE, MagicBagScreen::new);
         MenuScreens.register(FabricEntrypoint.PALETTE_MENU_TYPE, PaletteScreen::new);
    }
}
//?}
