package net.nicguzzo.wands.quilt;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import net.nicguzzo.wands.WandsModClient;

public class WandsModClientQuilt implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer mod) {
        WandsModClient.initialize();
    }
}