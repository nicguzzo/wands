package net.nicguzzo.wands.compat.claims;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

//?if has_goml {

import draylar.goml.api.ClaimUtils;

//?}

public class Goml {

    static public boolean canInteract(ServerLevel level, Player player, BlockPos pos) {
//?if has_goml {
        return ClaimUtils.canModify(level, pos, player);
//?}else{
        /*return true;
*///?}
    }
}
