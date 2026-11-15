package net.nicguzzo.wands.compat.claims;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
//?if has_cadmus && <1.21 {
/*import earth.terrarium.cadmus.api.claims.ClaimApi;
*///?}else if has_cadmus {
/*import earth.terrarium.cadmus.common.protections.Protections;
*///?}

public class Cadmus {
    public static boolean canInteract(ServerLevel level, Player player, BlockPos pos, ClaimDispatch.Action action, BlockState placedState) {
        //?if has_cadmus && <1.21 {
        /*return (action == ClaimDispatch.Action.PLACE || ClaimApi.API.canBreakBlock(level, pos, player))
                && (action == ClaimDispatch.Action.BREAK || ClaimApi.API.canPlaceBlock(level, pos, player));
        *///?}else if has_cadmus {
        /*if (action != ClaimDispatch.Action.PLACE && !Protections.BLOCK_BREAKING.canBreakBlock(player, pos)) return false;
        if (action == ClaimDispatch.Action.BREAK) return true;
        if (placedState != null) return Protections.BLOCK_PLACING.canPlaceBlock(player, pos, placedState);
        return Protections.BLOCK_PLACING.getId(level, pos)
                .map(team -> Protections.BLOCK_PLACING.isPlayerAllowed(player, team)).orElse(true);
        *///?}else{
        return true;
        //?}
    }
}
