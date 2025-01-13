package net.nicguzzo.wands.forge.claims;
import io.github.flemmli97.flan.api.ClaimHandler;
import io.github.flemmli97.flan.api.data.IPermissionContainer;

import io.github.flemmli97.flan.api.permission.PermissionRegistry ;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.nicguzzo.wands.WandsMod;

public class Flan {
    static public boolean canInteract(ServerLevel level, Player player, BlockPos pos){
        boolean r=false;
        IPermissionContainer check = ClaimHandler.getPermissionStorage(level).getForPermissionCheck(pos);
        r= check.canInteract((ServerPlayer) player, PermissionRegistry.PLACE, pos) && check.canInteract((ServerPlayer) player, PermissionRegistry.BREAK, pos);
        //WandsMod.LOGGER.info(" Flan canInteract "+r);
        return r;
    }
}
