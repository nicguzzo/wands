//?if has_factions {
package net.nicguzzo.wands.loaders.fabric.mixin;

import io.icker.factions.api.persistents.Relationship.Permissions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

// Factions uses this private permission check in its native place and break handlers.
@Pseudo
@Mixin(targets = "io.icker.factions.core.InteractionManager", remap = false)
public interface FactionsPermissionInvoker {
    @Invoker("checkPermissions")
    static InteractionResult check(Player player, BlockPos pos, Level level, Permissions permission) {
        throw new AssertionError();
    }
}
//?}
