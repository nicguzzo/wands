package net.nicguzzo.wands.forge.claims;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import xaero.pac.common.server.api.OpenPACServerAPI;
import net.nicguzzo.wands.WandsMod;


public class Opac {

    static public boolean canInteract( ServerLevel level, Player player, BlockPos pos){
        MinecraftServer server=level.getServer();

        OpenPACServerAPI opac = OpenPACServerAPI.get(server);
        boolean r=opac.getChunkProtection().onEntityPlaceBlock(player,level,pos);
        WandsMod.LOGGER.info(" Opac canInteract "+r);
        return !r;

    }
}
