package net.nicguzzo.wands.fabric;

import net.nicguzzo.wands.WandsMod;
import net.fabricmc.api.ModInitializer;

public class WandsModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        WandsMod.init();
    }
}
