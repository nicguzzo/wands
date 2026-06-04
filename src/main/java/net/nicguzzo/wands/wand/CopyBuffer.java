package net.nicguzzo.wands.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CopyBuffer{
    public BlockPos pos;
    public BlockState state;
    public CopyBuffer(BlockPos pos, BlockState state) {
       this.pos = pos;
       this.state = state;
    }
}
