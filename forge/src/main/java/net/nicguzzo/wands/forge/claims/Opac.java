package net.nicguzzo.wands.forge.claims;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
#if MC!="1171"
#if MC>"1165"
import net.minecraft.server.MinecraftServer;
import xaero.pac.common.server.api.OpenPACServerAPI;
#endif
#endif

public class Opac {

    static public boolean canInteract( ServerLevel level, Player player, BlockPos pos){

        #if MC>"1165" && MC!="1171"
            MinecraftServer server=level.getServer();
            OpenPACServerAPI opac = OpenPACServerAPI.get(server);
            boolean r=opac.getChunkProtection().onEntityPlaceBlock(player,level,pos);
            WandsMod.LOGGER.info(" Opac canInteract "+r);
            return !r;
        #else
            return true;
        #endif
    }
}
