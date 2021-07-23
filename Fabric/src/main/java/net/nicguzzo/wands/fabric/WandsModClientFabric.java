package net.nicguzzo.wands.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.nicguzzo.wands.WandsModClient;


public class WandsModClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WandsModClient.initialize();
    }
}