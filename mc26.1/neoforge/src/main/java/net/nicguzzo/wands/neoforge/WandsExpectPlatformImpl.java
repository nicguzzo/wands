package net.nicguzzo.wands.neoforge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLPaths;
import net.nicguzzo.wands.WandsExpectPlatform;

import java.nio.file.Path;

public class WandsExpectPlatformImpl {
    /**
     * This is our actual method to {@link WandsExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
    public static CompoundTag getPlayerData(Player player){
        return player.getData(WandsModNeoForge.PLAYER_DATA);
    }
}
