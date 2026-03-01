package net.nicguzzo.wands.forge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
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
    public static CompoundTag getPlayerData(Player player){
        CompoundTag forgeData = player.getPersistentData();
        String key = "wands_player_data";
        if (!forgeData.contains(key)) {
            forgeData.put(key, new CompoundTag());
        }
        return forgeData.getCompound(key);
    }
}
