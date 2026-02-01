package net.nicguzzo.wands.forge.claims;
#if HAS_FTB_CHUNKS
import dev.ftb.mods.ftbchunks.api.ChunkTeamData;
import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
#endif
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class FTBChunks {

    static public boolean canInteract(ServerLevel level, Player player, BlockPos pos){

        boolean r=true;
#if HAS_FTB_CHUNKS
            ClaimedChunk chunk =FTBChunksAPI.api().getManager().getChunk(new ChunkDimPos(level,pos));
            if(chunk != null) {
                ChunkTeamData teamData = chunk.getTeamData();
                r = teamData.isTeamMember(player.getUUID()) || teamData.isAlly(player.getUUID());
            }
#endif
        //WandsMod.LOGGER.info(" FTBChunks canInteract "+r);
        return r;

    }
}
