package net.nicguzzo.wands.wand;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.compat.claims.ClaimDispatch;
import net.nicguzzo.wands.config.WandsConfig;

public class UndoManager {
    record Edit(BlockPos pos, BlockSnapshot before, BlockSnapshot after) {
        Edit {
            pos = pos.immutable();
        }

        BlockSnapshot target(boolean forward) {
            return forward ? after : before;
        }

        boolean canReplay(ServerLevel level, Player player, boolean forward) {
            BlockSnapshot expected = target(!forward);
            if (!level.hasChunkAt(pos) || !expected.equals(BlockSnapshot.capture(level, pos))) return false;
            if (before.equals(after)) return true;
            BlockState existing = expected.state();
            BlockState targetState = target(forward).state();
            ClaimDispatch.Action action = targetState.isAir() ? ClaimDispatch.Action.BREAK
                    : existing.isAir() ? ClaimDispatch.Action.PLACE : ClaimDispatch.Action.REPLACE;
            return ClaimDispatch.canChange(level, pos, player, existing, action, targetState.isAir() ? null : targetState);
        }

        boolean replay(ServerLevel level, boolean forward, int flags) {
            return before.equals(after) || target(forward).restore(level, pos, target(!forward), flags);
        }

        void updateNeighbors(ServerLevel level, boolean forward) {
            BlockState previous = target(!forward).state();
            BlockState current = level.getBlockState(pos);
            if (previous == current) return;
            level.updateNeighborsAt(pos, previous.getBlock());
            if (current.hasAnalogOutputSignal()) level.updateNeighbourForOutputSignal(pos, current.getBlock());
            previous.updateIndirectNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
            current.updateNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
            current.updateIndirectNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
        }
    }

    private record Change(Edit block, Edit doorPartner, String action, String mode) {
        boolean replay(ServerLevel level, Player player, boolean forward) {
            if (!block.canReplay(level, player, forward)) return false;
            if (doorPartner == null) return block.replay(level, forward, Block.UPDATE_ALL);
            if (!doorPartner.canReplay(level, player, forward)) return false;

            // Let vanilla's neighbor updates see the complete door rather than a lone half.
            int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
            if (!block.replay(level, forward, flags)) return false;
            if (!doorPartner.replay(level, forward, flags)) {
                block.replay(level, !forward, flags);
                return false;
            }
            block.updateNeighbors(level, forward);
            doorPartner.updateNeighbors(level, forward);
            return true;
        }
    }

    private final ActionHistory<ResourceKey<Level>, Change> history = new ActionHistory<>(WandsConfig.max_limit);
    private final List<Change> pending = new ArrayList<>();

    void beginAction() {
        pending.clear();
    }

    void record(Edit block, Edit doorPartner, String action, String mode) {
        if (!block.before.equals(block.after) || (doorPartner != null && !doorPartner.before.equals(doorPartner.after))) {
            pending.add(new Change(block, doorPartner, action, mode));
        }
    }

    void finishAction(Level level) {
        history.add(level.dimension(), pending);
        pending.clear();
    }

    public void replay(int count, Level level, Player player, boolean forward) {
        if (!(level instanceof ServerLevel server)) return;
        Change[] first = new Change[1];
        var result = history.replay(level.dimension(), count, forward, change -> {
            if (!change.replay(server, player, forward)) return false;
            if (first[0] == null) first[0] = change;
            return true;
        });
        if (result.applied() + result.skipped() == 0 && !result.otherWorld()) return;
        var msg = Compat.translatable(forward ? "wands.hud.redo" : "wands.hud.undo");
        if (first[0] != null) {
            Change change = first[0];
            msg.append(" ").append(Compat.translatable(change.action)).append(" ")
                    .append(Compat.translatable(change.mode))
                    .append(Compat.translatable("wands.history.blocks", result.applied()));
            boolean removing = change.block.target(forward).state().isAir();
            var sound = Blocks.STONE.defaultBlockState().getSoundType();
            level.playSound(null, player.blockPosition(), removing ? sound.getBreakSound() : sound.getPlaceSound(),
                    SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        if (result.skipped() > 0) msg.append(Compat.translatable("wands.history.skipped", result.skipped()));
        if (result.otherWorld()) msg.append(Compat.translatable("wands.history.other_world"));
        Compat.displayClientMessage(player, msg, true);
    }
}
