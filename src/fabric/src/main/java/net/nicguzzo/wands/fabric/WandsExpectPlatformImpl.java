package net.nicguzzo.wands.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.nicguzzo.wands.WandsExpectPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import net.minecraft.world.entity.player.Player;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.fabric.claims.Opac;

public class WandsExpectPlatformImpl {
    /**
     * This is our actual method to {@link WandsExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
    public static boolean claim_can_break(ServerLevel level, BlockPos pos, Player player){
        WandsMod.LOGGER.info(" Chunk Protection canBreakBlock");
        if(WandsMod.has_opac){
            return Opac.canInteract(level,player,pos);
        }
        return true;
    }
    public static boolean claim_can_place(ServerLevel level, BlockPos pos,Player player){
        WandsMod.LOGGER.info(" Chunk Protection canPlaceBlock");
        if(WandsMod.has_opac){
            return Opac.canInteract(level,player,pos);
        }
        return true;
    }
}
