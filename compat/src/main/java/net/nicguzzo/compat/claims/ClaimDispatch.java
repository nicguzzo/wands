package net.nicguzzo.compat.claims;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.nicguzzo.wands.WandsMod;

public class ClaimDispatch {

    public static boolean claimCanInteract(ServerLevel level, BlockPos pos, Player player) {
        if (WandsMod.has_opac && !Opac.canInteract(level, player, pos)) {
            return false;
        }
        if (WandsMod.has_ftbchunks && !FTBChunks.canInteract(level, player, pos)) {
            return false;
        }
        if (WandsMod.has_flan && !Flan.canInteract(level, player, pos)) {
            return false;
        }
        if (WandsMod.has_goml && !Goml.canInteract(level, player, pos)) {
            return false;
        }
        return true;
    }
}
