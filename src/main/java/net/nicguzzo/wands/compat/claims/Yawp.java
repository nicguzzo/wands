package net.nicguzzo.wands.compat.claims;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
//?if has_yawp {
import de.z0rdak.yawp.api.FlagEvaluator;
import de.z0rdak.yawp.core.flag.FlagState;
import de.z0rdak.yawp.core.flag.RegionFlag;
import de.z0rdak.yawp.platform.Services;
//?}
//?if has_yawp && =1.21.11 {
/*import de.z0rdak.yawp.api.events.region.FlagCheckEvent;
*///?} else if has_yawp {
import de.z0rdak.yawp.api.events.flag.FlagCheckRequest;
//?}

public class Yawp {
    public static boolean canInteract(ServerLevel level, Player player, BlockPos pos, ClaimDispatch.Action action) {
        //?if has_yawp {
        return (action == ClaimDispatch.Action.PLACE || allows(level, player, pos, RegionFlag.BREAK_BLOCKS))
                && (action == ClaimDispatch.Action.BREAK || allows(level, player, pos, RegionFlag.PLACE_BLOCKS));
        //?}else{
        /*return true;
        *///?}
    }

    //?if has_yawp {
    private static boolean allows(ServerLevel level, Player player, BlockPos pos, RegionFlag flag) {
        //?if =1.21.11 {
        /*FlagCheckEvent check = new FlagCheckEvent(pos, flag, level.dimension(), player);
        return Services.EVENT.post(check) || FlagEvaluator.processCheck(check) != FlagState.DENIED;
        *///?}else{
        FlagCheckRequest check = new FlagCheckRequest(pos, flag, level.dimension(), player);
        //?if fabric && =1.20.1 {
        /*// On this Fabric release, post returns true when every listener accepts the check.
        return !Services.FLAG_EVENT_DISPATCHER.post(check) || FlagEvaluator.processCheck(check) != FlagState.DENIED;
        *///?}else{
        return Services.FLAG_EVENT_DISPATCHER.post(check) || FlagEvaluator.processCheck(check) != FlagState.DENIED;
        //?}
        //?}
    }
    //?}
}
