package net.nicguzzo.wands.quilt;

import net.nicguzzo.wands.WandsMod;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class WandsModQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        WandsMod.platform=2;
        WandsMod.init();
    }
}
