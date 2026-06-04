package net.nicguzzo.wands;

import net.minecraft.server.MinecraftServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class WandsCommon {

    public static void onServerStarted(MinecraftServer server) {
        // The server is fully initialized and the worlds are loaded.
        // Run your post-initialization code here!
        WandsMod.init();
    }
    public static CompoundTag getPlayerData(Player player){
        throw new AssertionError();
    }

}
