//?if has_factions {
package net.nicguzzo.wands.compat.claims;

import io.icker.factions.api.persistents.Relationship.Permissions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.nicguzzo.wands.loaders.fabric.mixin.FactionsPermissionInvoker;

public class Factions {
    public static boolean canInteract(ServerLevel level, Player player, BlockPos pos, ClaimDispatch.Action action) {
        return (action == ClaimDispatch.Action.PLACE || FactionsPermissionInvoker.check(player, pos, level, Permissions.BREAK_BLOCKS) != InteractionResult.FAIL)
                && (action == ClaimDispatch.Action.BREAK || FactionsPermissionInvoker.check(player, pos, level, Permissions.PLACE_BLOCKS) != InteractionResult.FAIL);
    }
}
//?}
