package net.nicguzzo.wands.forge;

import net.nicguzzo.wands.WandsExpectPlatform;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class WandsExpectPlatformImpl {
    /**
     * This is our actual method to {@link WandsExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
