package net.nicguzzo.compat.claims;

#if HAS_FTB_CHUNKS
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.api.Protection;
import net.minecraft.world.InteractionHand;
#endif
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class FTBChunks {

    static public boolean canInteract(ServerLevel level, Player player, BlockPos pos) {
#if HAS_FTB_CHUNKS
        return !FTBChunksAPI.api().getManager().shouldPreventInteraction(player, InteractionHand.MAIN_HAND, pos, Protection.EDIT_BLOCK, null);
#else
        return true;
#endif
    }
}
