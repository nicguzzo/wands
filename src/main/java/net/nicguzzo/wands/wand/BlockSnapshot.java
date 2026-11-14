package net.nicguzzo.wands.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

//? if >= 1.21.11 {
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
//? }

record BlockSnapshot(BlockState state, CompoundTag data) {
    static BlockSnapshot capture(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        BlockEntity entity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        //? if >= 1.21.1 {
        return new BlockSnapshot(state, entity == null ? null : entity.saveWithoutMetadata(level.registryAccess()));
        //? } else {
        /*return new BlockSnapshot(state, entity == null ? null : entity.saveWithoutMetadata());
        *///? }
    }

    private void loadData(Level level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (data == null || entity == null) return;
        //? if >= 1.21.11 {
        entity.loadWithComponents(TagValueInput.create(new ProblemReporter.Collector(), level.registryAccess(), data));
        //? } else if >= 1.21.1 {
        /*entity.loadWithComponents(data, level.registryAccess());
        *///? } else {
        /*entity.load(data);
        *///? }
        entity.setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
    }

    // Creative history retains container contents, so replacing the container must not also spill them.
    boolean changeState(Level level, BlockPos pos, BlockState target) {
        //? if >= 1.21.11 {
        return level.setBlock(pos, target, Block.UPDATE_ALL | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS);
        //? } else {
        /*if (state.getBlock() != target.getBlock() && data != null) {
            net.minecraft.world.Clearable.tryClear(level.getBlockEntity(pos));
        }
        if (level.setBlockAndUpdate(pos, target)) return true;
        loadData(level, pos);
        return false;
        *///? }
    }

    boolean restore(Level level, BlockPos pos, BlockSnapshot current) {
        if (state != current.state && !current.changeState(level, pos, state)) return false;
        loadData(level, pos);
        return true;
    }
}
