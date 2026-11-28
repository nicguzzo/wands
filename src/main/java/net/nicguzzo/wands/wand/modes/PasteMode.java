package net.nicguzzo.wands.wand.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.wand.CopyBuffer;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;

import java.util.List;

public class PasteMode extends WandMode {

    public void place_in_buffer(Wand wand) {
        wand.block_buffer.reset();
        List<CopyBuffer> sourceBuffer = wand.getActivePasteBuffer();
        wand.pasteTotalBlocks = sourceBuffer != null ? sourceBuffer.size() : 0;
        wand.pasteRemainingBlocks = 0;
        if (sourceBuffer == null || sourceBuffer.isEmpty())
            return;
        // Always populate buffer (for both preview and placement) so rendering matches placement
        int mx = 1;
        int my = 1;
        int mz = 1;
        int mirroraxis = WandProps.getVal(wand.wand_stack, WandProps.Value.MIRRORAXIS);
        switch (mirroraxis) {
            case 1://X
                mx = -1;
                break;
            case 2://Y
                mz = -1;
                break;
        }
        // wand.pos is already offset by ClientRender/WandItem when INCSELBLOCK is off
        BlockPos b_pos = wand.pos;

        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        int remaining = 0;

        for (CopyBuffer b : sourceBuffer) {
            BlockPos p = b.pos.rotate(wand.rotation);
            BlockState st = b.state;
            int px = b_pos.getX() + p.getX() * mx;
            int py = b_pos.getY() + p.getY();
            int pz = b_pos.getZ() + p.getZ() * mz;
            st = wand.rotate_mirror(st, mirroraxis);

            // Completed positions should not take up slots in the next paste batch.
            if (wand.level != null) {
                checkPos.set(px, py, pz);
                BlockState existing = wand.level.getBlockState(checkPos);
                if (wand.destroy) {
                    // Removing a waterlogged block can leave its fluid behind.
                    if (existing.isAir() || existing == existing.getFluidState().createLegacyBlock()) {
                        continue;
                    }
                } else {
                    if (!wand.palette.has_palette && existing.equals(st)) {
                        continue;
                    }
                    if (!wand.replace && wand.replace_mode != 2 && !wand.can_place(existing, checkPos)) {
                        continue;
                    }
                }
            }

            remaining++;
            if (wand.palette.has_palette) {
                wand.block_buffer.add(px, py, pz, wand, null);
            } else {
                wand.block_buffer.add(px, py, pz, st, st.getBlock().asItem());
            }
        }
        wand.pasteRemainingBlocks = remaining;
        if (wand.block_buffer.get_length() > 0) {
            wand.valid = true;
            wand.limit_reached = false;
        }
    }
}
