package net.nicguzzo.wands.fabric.claims;
#if HAS_FTB_CHUNKS
import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
#endif
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.nicguzzo.wands.WandsMod;

public class FTBChunks {

    static public boolean canInteract(ServerLevel level, Player player, BlockPos pos){
        boolean r=true;
#if HAS_FTB_CHUNKS
            ClaimedChunk chunk =FTBChunksAPI.api().getManager().getChunk(new ChunkDimPos(level,pos));
            if(chunk != null) {
                r = chunk.getTeamData().isTeamMember(player.getUUID());
            }
#endif
        //WandsMod.LOGGER.info(" FTBChunks canInteract "+r);
        return r;

    }
}
