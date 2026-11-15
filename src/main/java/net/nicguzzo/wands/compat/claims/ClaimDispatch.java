package net.nicguzzo.wands.compat.claims;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.nicguzzo.wands.WandsMod;

public class ClaimDispatch {

    public enum Action { PLACE, BREAK, REPLACE }

    /** Removing a multipart block can also destroy its partner through a vanilla neighbor update. */
    public static boolean canChange(ServerLevel level, BlockPos pos, Player player, BlockState existing, Action action, BlockState placedState) {
        if (!claimCanInteract(level, pos, player, action, placedState)) return false;
        BlockPos other;
        if (existing.hasProperty(BedBlock.PART)) {
            other = pos.relative(BedBlock.getConnectedDirection(existing));
        } else if (existing.getBlock() instanceof DoorBlock || existing.getBlock() instanceof DoublePlantBlock) {
            other = existing.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        } else if (existing.is(Blocks.PISTON_HEAD)) {
            var facing = existing.getValue(BlockStateProperties.FACING);
            other = pos.relative(facing.getOpposite());
            var base = level.getBlockState(other);
            var matchingBase = existing.getValue(BlockStateProperties.PISTON_TYPE) == PistonType.STICKY ? Blocks.STICKY_PISTON : Blocks.PISTON;
            return !base.is(matchingBase) || !base.getValue(BlockStateProperties.EXTENDED)
                    || base.getValue(BlockStateProperties.FACING) != facing || claimCanInteract(level, other, player, Action.BREAK, null);
        } else if ((existing.is(Blocks.PISTON) || existing.is(Blocks.STICKY_PISTON)) && existing.getValue(BlockStateProperties.EXTENDED)) {
            var facing = existing.getValue(BlockStateProperties.FACING);
            other = pos.relative(facing);
            var head = level.getBlockState(other);
            return !head.is(Blocks.PISTON_HEAD) || head.getValue(BlockStateProperties.FACING) != facing
                    || claimCanInteract(level, other, player, Action.BREAK, null);
        } else {
            return true;
        }
        return !level.getBlockState(other).is(existing.getBlock()) || claimCanInteract(level, other, player, Action.BREAK, null);
    }

    public static boolean placesOtherHalf(BlockState state) {
        return state.hasProperty(BedBlock.PART) || state.getBlock() instanceof DoorBlock || state.getBlock() instanceof DoublePlantBlock;
    }

    /** Vanilla setPlacedBy writes the head of a bed or the upper half of a tall block. */
    public static boolean canPlaceOtherHalf(ServerLevel level, BlockPos pos, Player player, BlockState state) {
        BlockPos other;
        if (state.hasProperty(BedBlock.PART)) {
            other = pos.relative(state.getValue(BedBlock.FACING));
        } else {
            other = pos.above();
        }
        BlockState existing = level.getBlockState(other);
        return canChange(level, other, player, existing, existing.canBeReplaced() ? Action.PLACE : Action.REPLACE, state);
    }

    public static boolean claimCanInteract(ServerLevel level, BlockPos pos, Player player, Action action, BlockState placedState) {
        if (WandsMod.has_opac && !Opac.canInteract(level, player, pos, action)) {
            return false;
        }
        if (WandsMod.has_ftbchunks && !FTBChunks.canInteract(level, player, pos)) {
            return false;
        }
        if (WandsMod.has_flan && !Flan.canInteract(level, player, pos, action)) {
            return false;
        }
        if (WandsMod.has_goml && !Goml.canInteract(level, player, pos)) {
            return false;
        }
        if (WandsMod.has_yawp && !Yawp.canInteract(level, player, pos, action)) {
            return false;
        }
        if (WandsMod.has_cadmus && !Cadmus.canInteract(level, player, pos, action, placedState)) {
            return false;
        }
        //?if has_factions {
        /*if (WandsMod.has_factions && !Factions.canInteract(level, player, pos, action)) {
            return false;
        }
        *///?}
        return true;
    }
}
