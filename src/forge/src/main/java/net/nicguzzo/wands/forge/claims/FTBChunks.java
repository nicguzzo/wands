package net.nicguzzo.wands.forge.claims;

import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.nicguzzo.wands.WandsMod;

public class FTBChunks {

    static public boolean canInteract(ServerLevel level, Player player, BlockPos pos){

        ClaimedChunk chunk =FTBChunksAPI.api().getManager().getChunk(new ChunkDimPos(level,pos));
        boolean r=true;
        if(chunk != null) {
            r = chunk.getTeamData().isTeamMember(player.getUUID());
        }
        WandsMod.LOGGER.info(" FTBChunks canInteract "+r);
        return r;

    }
}
