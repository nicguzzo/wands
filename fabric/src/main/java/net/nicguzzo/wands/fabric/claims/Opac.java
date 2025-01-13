package net.nicguzzo.wands.fabric.claims;

import net.minecraft.core.BlockPos;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
#if HAS_OPAC
#if MC>"1165" && MC!="1171"
import net.minecraft.server.MinecraftServer;
import xaero.pac.common.server.api.OpenPACServerAPI;
#endif
#endif
public class Opac {

    static public boolean canInteract( ServerLevel level, Player player, BlockPos pos){
#if HAS_OPAC
        #if MC>"1165" && MC!="1171"
            MinecraftServer server=level.getServer();
            OpenPACServerAPI opac = OpenPACServerAPI.get(server);
            boolean r=opac.getChunkProtection().onEntityPlaceBlock(player,level,pos);
            //WandsMod.LOGGER.info(" Opac canInteract "+r);
            return !r;
        #else
             return true;
        #endif
#else
        return true;
#endif
    }
}
