package net.nicguzzo.wands.compat.claims;

//?if has_flan {

import io.github.flemmli97.flan.api.ClaimHandler;
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import net.minecraft.server.level.ServerPlayer;

//?}
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class Flan {

    static public boolean canInteract(ServerLevel level, Player player, BlockPos pos) {
        boolean r = true;
        //?if has_flan {
        
        IPermissionContainer check = ClaimHandler.getPermissionStorage(level).getForPermissionCheck(pos);
        r = check.canInteract((ServerPlayer) player, BuiltinPermission.PLACE, pos) && check.canInteract((ServerPlayer) player, BuiltinPermission.BREAK, pos);
        
        //?}
        return r;
    }
}
