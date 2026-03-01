package net.nicguzzo.compat.claims;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

#if HAS_GOML
import draylar.goml.api.ClaimUtils;
#endif

public class Goml {

    static public boolean canInteract(ServerLevel level, Player player, BlockPos pos) {
#if HAS_GOML
        return ClaimUtils.canModify(level, pos, player);
#else
        return true;
#endif
    }
}
