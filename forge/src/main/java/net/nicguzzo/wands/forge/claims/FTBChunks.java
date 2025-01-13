package net.nicguzzo.wands.forge.claims;

import dev.ftb.mods.ftbchunks.data.ClaimedChunk;
import dev.ftb.mods.ftbchunks.data.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.data.FTBChunksTeamData;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class FTBChunks {

    static public boolean canInteract(ServerLevel level, Player player, BlockPos pos) {

        boolean r = true;
        ClaimedChunk chunk = FTBChunksAPI.getManager().getChunk(new ChunkDimPos(level, pos));
        FTBChunksTeamData data = chunk.getTeamData();
        if (data != null) {
            r = chunk.getTeamData().isTeamMember(player.getUUID());
        }
        //WandsMod.LOGGER.info(" FTBChunks canInteract "+r);
        return r;

    }
}
