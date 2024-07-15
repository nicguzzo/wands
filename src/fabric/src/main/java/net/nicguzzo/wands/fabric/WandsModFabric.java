package net.nicguzzo.wands.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.nicguzzo.wands.WandsMod;
import net.fabricmc.api.ModInitializer;
import java.util.Optional;

public class WandsModFabric implements ModInitializer {
    @Override
    public void onInitialize() {

        Optional<ModContainer> opac= FabricLoader.getInstance().getModContainer("openpartiesandclaims");
         if(opac.isPresent()){
            WandsMod.has_opac=true;
            WandsMod.log("has opac!!!!!!!!!!!!",true);
        }else{
             WandsMod.log("NO opac!!!!!!!!!!!!",true);
         }

        WandsMod.init();
    }
}
