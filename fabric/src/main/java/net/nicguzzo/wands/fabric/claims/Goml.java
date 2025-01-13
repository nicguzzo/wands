package net.nicguzzo.wands.fabric.claims;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

#if HAS_GOML
import com.jamieswhiteshirt.rtree3i.Entry;
import com.jamieswhiteshirt.rtree3i.Selection;
import draylar.goml.api.Claim;
import draylar.goml.api.ClaimBox;
import draylar.goml.api.ClaimUtils;
#endif
import net.nicguzzo.wands.WandsMod;
import java.util.stream.Collectors;


public class Goml {
    static public boolean canInteract(ServerLevel level, Player player, BlockPos pos){
        boolean r=true;

#if HAS_GOML
        //TODO: GOML check all claims!
        #if MC=="1165" || MC=="1171" 
            for(Entry<ClaimBox, Claim> entry : ClaimUtils.getClaimsAt(level, pos).collect(Collectors.toList())) {
                r = ClaimUtils.claimMatchesWith(entry, player,pos);
                return r;
            }

        #else
        for(Entry<ClaimBox, Claim> entry : ClaimUtils.getClaimsAt(level, pos).collect(Collectors.toList())) {
            r=ClaimUtils.canModifyClaimAt(level,pos,entry,player);
            //WandsMod.LOGGER.info(" Goml canInteract "+r);
            return r;
        }
        #endif
#endif
        //WandsMod.LOGGER.info(" Goml canInteract "+r);
        return r;
    }
}
