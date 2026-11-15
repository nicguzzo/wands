package net.nicguzzo.wands.wand;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.compat.claims.ClaimDispatch;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.utils.CircularBuffer;

/**
 * Manages undo/redo history for wand actions.
 * Each wand action (place, destroy, use) is recorded as a group of entries
 * sharing the same actionId, so undo/redo operates on entire actions at once.
 */
public class UndoManager {

    private final CircularBuffer buffer = new CircularBuffer(WandsConfig.max_limit);
    private int nextActionId = 0;
    private int currentActionId = -1;

    /** Call at the start of each server-side wand action to assign a new actionId. */
    public void beginAction() {
        currentActionId = nextActionId++;
    }

    /** Records a single block change to the undo buffer. */
    public void record(BlockPos pos, BlockState originalState, boolean destroyed, BlockState newState, String modeName) {
        buffer.put(pos, originalState, destroyed, currentActionId, newState, modeName);
    }

    /** Undoes n complete actions, playing sound and showing action bar feedback. */
    public void undo(int n, Level level, Player player) {
        replay(n, level, player, false);
    }

    /** Redoes n complete actions, playing sound and showing action bar feedback. */
    public void redo(int n, Level level, Player player) {
        replay(n, level, player, true);
    }

    private void replay(int remainingActions, Level level, Player player, boolean redo) {
        if (!(level instanceof ServerLevel serverLevel) || player == null || remainingActions <= 0) return;
        CircularBuffer.P entry = redo ? buffer.peekForward() : buffer.peek();
        if (entry == null) return;
        String modeName = entry.modeName;
        String actionName = actionNameFromEntry(entry);
        int totalBlocks = 0;
        boolean denied = false;
        while (entry != null && remainingActions > 0) {
            int actionId = entry.actionId;
            BlockState target;
            if (redo) {
                target = entry.destroyed ? entry.newState : entry.state;
            } else {
                target = entry.destroyed ? entry.state : null;
            }
            if (target == null) target = Blocks.AIR.defaultBlockState();
            BlockState existing = level.getBlockState(entry.pos);
            ClaimDispatch.Action action = target.isAir() ? ClaimDispatch.Action.BREAK
                    : existing.isAir() ? ClaimDispatch.Action.PLACE : ClaimDispatch.Action.REPLACE;
            if (!ClaimDispatch.canChange(serverLevel, entry.pos, player, existing, action, target.isAir() ? null : target)) {
                // Preserve the cursor so a denied undo cannot later become an unearned redo.
                denied = true;
                break;
            }
            if (level.setBlockAndUpdate(entry.pos, target)) totalBlocks++;
            if (redo) {
                buffer.forward();
                entry = buffer.peekForward();
            } else {
                buffer.pop();
                entry = buffer.peek();
            }
            if (entry == null || entry.actionId != actionId) remainingActions--;
        }
        showFeedback(redo ? "Redo" : "Undo", redo != "Place".equals(actionName), modeName, actionName, totalBlocks, level, player);
        if (denied) {
            Compat.displayClientMessage(player, Compat.translatable("wands.message.claimed_chunk").withStyle(ChatFormatting.RED), true);
        }
    }

    private static String actionNameFromEntry(CircularBuffer.P entry) {
        if (!entry.destroyed) return "Place";
        return entry.newState != null ? "Use" : "Destroy";
    }

    private void showFeedback(String prefix, boolean playBreakSound, String modeName, String actionName, int totalBlocks, Level level, Player player) {
        if (totalBlocks <= 0 || player == null) return;
        BlockState soundRef = Blocks.STONE.defaultBlockState();
        if (playBreakSound) {
            level.playSound(null, player.blockPosition(), soundRef.getSoundType().getBreakSound(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
        } else {
            level.playSound(null, player.blockPosition(), soundRef.getSoundType().getPlaceSound(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        MutableComponent msg = Compat.literal(prefix + ": ");
        if (actionName != null) {
            msg.append(actionName);
        }
        if (modeName != null) {
            msg.append(" ").append(Compat.translatable(modeName));
        }
        msg.append(" - " + totalBlocks + " blocks");
        Compat.displayClientMessage(player,msg, true);
    }
}
