package net.nicguzzo.wands.fabric;

import net.minecraft.nbt.CompoundTag;
import net.nicguzzo.wands.WandsExpectPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import net.minecraft.world.entity.player.Player;
import net.nicguzzo.wands.utils.IEntityDataSaver;

public class WandsExpectPlatformImpl {
    /**
     * This is our actual method to {@link WandsExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
    public static CompoundTag getPlayerData(Player player){
        return ((IEntityDataSaver) player).getPersistentData();
    }
}
