package net.nicguzzo.wands.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.WandsModClient;

import java.util.Optional;


public class WandsModClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WandsModClient.initialize();
        Optional<ModContainer> cont= FabricLoader.getInstance().getModContainer("optifabric");
        if(cont.isPresent()){
            WandsModClient.has_optifine=true;
            WandsMod.log("has optifine!!!!!!!!!!!!",true);
        }
    }
}