package net.nicguzzo.wands.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.compat.Compat;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.networking.Networking;

public class Interaction {

    public static boolean onClick(Level level, BlockPos pos, Direction side,Vec3 hit, ItemStack stack){
        WandProps.Mode mode = WandProps.getMode(stack);
        // Only apply INCSELBLOCK offset for modes that show the toggle
        boolean inc_sel = WandProps.getFlag(stack, WandProps.Flag.INCSELBLOCK);
        boolean modeSupportsIncSel = WandProps.flagAppliesTo(WandProps.Flag.INCSELBLOCK, mode);
        // Pin: override target position (offset already applied when pin was set)
        boolean pinActive = ClientRender.wand.pin.isActive();
        if (pinActive) {
            pos = ClientRender.wand.pin.getPos();
            // Dynamic side: if clicking on the pin block, use clicked face
            if (!pos.equals(ClientRender.wand.pin.getPos())) {
                side = ClientRender.wand.pin.getSide() != null ? ClientRender.wand.pin.getSide() : side;
            }
            //block_state = level.getBlockState(pos);
            //shouldOffset = false;
        }
        BlockState block_state = level.getBlockState(pos);
        boolean shouldOffset = modeSupportsIncSel && !inc_sel && !block_state.isAir() && !pinActive;
        if (ClientRender.wand.getP1() == null) {
            if (shouldOffset) {
                ClientRender.wand.setP1(pos.relative(side, 1));
            } else {
                ClientRender.wand.setP1(pos);
            }
            // Store P1's block state so client preview uses it in 2-click modes
            ClientRender.wand.p1_state = block_state;
        } else {
            if (ClientRender.wand.getP2() == null && mode.n_clicks() == 2) {
                ClientRender.wand.setP2(pos);
                if (shouldOffset) {
                    ClientRender.wand.setP2(ClientRender.wand.getP2().relative(side, 1));
                }
            } else if (mode == WandProps.Mode.COPY) {
                BlockPos clickedPos = shouldOffset ? pos.relative(side, 1) : pos;
                ClientRender.wand.extendBbox(clickedPos);
            }
        }
        if ((ClientRender.wand.getP1() != null && mode.n_clicks() == 1) || ((ClientRender.wand.getP1() != null && ClientRender.wand.getP2() != null && mode.n_clicks() == 2))) {

            if(ClientRender.wand.reach_distance>0){
                hit=ClientRender.wand.hit;
            }
            if(hit==null){
                return false;
            }
            Networking.send_placement(side, ClientRender.wand.getP1(), ClientRender.wand.getP2(), hit, ClientRender.wand.palette.seed);
            WandMode wand_mode = ClientRender.wand.get_mode();
            if(wand_mode!=null){
                wand_mode.redraw(ClientRender.wand);
            }
            ClientRender.wand.palette.seed = System.currentTimeMillis();
            ClientRender.wand.copy();
            if (WandProps.getFlag(stack, WandProps.Flag.CLEAR_P1)) {
                ClientRender.wand.pin.clear();
            }
            if (mode != WandProps.Mode.COPY) {
                ClientRender.wand.clear(mode == WandProps.Mode.PASTE  || mode== WandProps.Mode.AREA || mode == WandProps.Mode.VEIN);
            }

        }
        return true;
    }
    public static boolean onAirClick(Level level, ItemStack stack){
        if(ClientRender.wand.lastHitResult==null)
            return false;
        boolean hasBlockTarget = ClientRender.wand.lastHitResult.getType() == HitResult.Type.BLOCK;
        boolean missedTarget =  ClientRender.wand.lastHitResult.getType() == HitResult.Type.MISS;
        WandProps.Mode mode = WandProps.getMode(stack);
        if(hasBlockTarget||(missedTarget && mode.can_target_air() && ClientRender.wand.target_air)){
            Interaction.onClick(level,ClientRender.last_pos,ClientRender.last_side ,ClientRender.wand.lastHitResult.getLocation(),stack);
        }
        return false;
    }
}
