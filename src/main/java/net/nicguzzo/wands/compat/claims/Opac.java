package net.nicguzzo.wands.compat.claims;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
//?if has_opac {

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import xaero.pac.common.server.api.OpenPACServerAPI;

//?}

public class Opac {

    static public boolean canInteract(ServerLevel level, Player player, BlockPos pos, ClaimDispatch.Action action) {
    //?if has_opac {
    
        var protection = OpenPACServerAPI.get(level.getServer()).getChunkProtection();
        return (action == ClaimDispatch.Action.BREAK || !protection.onEntityPlaceBlock(player, level, pos))
                && (action == ClaimDispatch.Action.PLACE || !protection.onBlockInteraction(player, InteractionHand.MAIN_HAND,
                        player.getMainHandItem(), level, pos, Direction.UP, true, false));
        
    //?}else{
        /*return true;
    *///?}
    }
}
