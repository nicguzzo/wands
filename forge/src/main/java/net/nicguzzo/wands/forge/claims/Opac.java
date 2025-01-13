package net.nicguzzo.wands.forge.claims;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class Opac {

    static public boolean canInteract( ServerLevel level, Player player, BlockPos pos){

            return true;

    }
}
