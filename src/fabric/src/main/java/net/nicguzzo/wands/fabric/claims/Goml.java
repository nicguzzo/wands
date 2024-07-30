package net.nicguzzo.wands.fabric.claims;

import draylar.goml.api.Claim;
import draylar.goml.api.ClaimBox;
import draylar.goml.api.ClaimUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import com.jamieswhiteshirt.rtree3i.Entry;
import com.jamieswhiteshirt.rtree3i.Selection;
import net.nicguzzo.wands.WandsMod;

import java.util.stream.Collectors;


public class Goml {
    static public boolean canInteract(ServerLevel level, Player player, BlockPos pos){
        boolean r=false;
        for(Entry<ClaimBox, Claim> entry : ClaimUtils.getClaimsAt(level, pos).collect(Collectors.toList())) {
            r=ClaimUtils.canModifyClaimAt(level,pos,entry,player);
            WandsMod.LOGGER.info(" Goml canInteract "+r);
            return r;
        }
        WandsMod.LOGGER.info(" Goml canInteract "+r);
        return r;
    }
}
