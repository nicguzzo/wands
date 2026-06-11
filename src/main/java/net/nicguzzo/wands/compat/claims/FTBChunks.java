package net.nicguzzo.wands.compat.claims;

//?if has_ftb_chunks {

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.api.Protection;
import net.minecraft.world.InteractionHand;

//?}
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class FTBChunks {

    static public boolean canInteract(ServerLevel level, Player player, BlockPos pos) {
    //?if has_ftb_chunks {
        return !FTBChunksAPI.api().getManager().shouldPreventInteraction(player, InteractionHand.MAIN_HAND, pos, Protection.EDIT_BLOCK, null);
    //?}else{
        /*return true;
    *///?}
    }
}
