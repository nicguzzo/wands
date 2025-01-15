package net.nicguzzo.wands.fabric.claims;

#if HAS_FLAN
    import io.github.flemmli97.flan.api.ClaimHandler;
    import io.github.flemmli97.flan.api.data.IPermissionContainer;
    import io.github.flemmli97.flan.api.permission.BuiltinPermission;
#endif

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.nicguzzo.wands.WandsMod;

public class Flan {
    static public boolean canInteract(ServerLevel level, Player player, BlockPos pos){
        boolean r=true;
#if HAS_FLAN
        IPermissionContainer check = ClaimHandler.getPermissionStorage(level).getForPermissionCheck(pos);
        r= check.canInteract((ServerPlayer) player, BuiltinPermission.PLACE, pos) && check.canInteract((ServerPlayer) player, BuiltinPermission.BREAK, pos);
#endif
        //WandsMod.LOGGER.info(" Flan canInteract "+r);
        return r;
    }
}
