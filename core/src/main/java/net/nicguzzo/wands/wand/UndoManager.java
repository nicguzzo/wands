package net.nicguzzo.wands.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.compat.Compat;
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
        if (player == null || level == null) return;
        int totalBlocks = 0;
        String modeName = null;
        String actionName = null;
        for (int i = 0; i < n; i++) {
            CircularBuffer.P first = buffer.peek();
            if (first == null) break;
            if (modeName == null) {
                modeName = first.modeName;
                actionName = actionNameFromEntry(first);
            }
            totalBlocks += undoOneAction(level);
        }
        // Undo of Place → break sound; Undo of Destroy/Use → place sound
        showFeedback("Undo", "Place".equals(actionName), modeName, actionName, totalBlocks, level, player);
    }

    /** Redoes n complete actions, playing sound and showing action bar feedback. */
    public void redo(int n, Level level, Player player) {
        if (player == null || level == null) return;
        int totalBlocks = 0;
        String modeName = null;
        String actionName = null;
        for (int i = 0; i < n; i++) {
            CircularBuffer.P first = buffer.peekForward();
            if (first == null) break;
            if (modeName == null) {
                modeName = first.modeName;
                actionName = actionNameFromEntry(first);
            }
            totalBlocks += redoOneAction(level);
        }
        // Redo of Place → place sound; Redo of Destroy/Use → break sound
        showFeedback("Redo", !"Place".equals(actionName), modeName, actionName, totalBlocks, level, player);
    }

    private int undoOneAction(Level level) {
        CircularBuffer.P first = buffer.peek();
        if (first == null) return 0;
        int actionId = first.actionId;
        int count = 0;
        while (true) {
            CircularBuffer.P p = buffer.peek();
            if (p == null || p.actionId != actionId) break;
            if (!p.destroyed) {
                level.setBlockAndUpdate(p.pos, Blocks.AIR.defaultBlockState());
            } else {
                level.setBlockAndUpdate(p.pos, p.state);
            }
            count++;
            buffer.pop();
        }
        return count;
    }

    private int redoOneAction(Level level) {
        CircularBuffer.P first = buffer.peekForward();
        if (first == null) return 0;
        int actionId = first.actionId;
        int count = 0;
        while (buffer.can_go_forward()) {
            CircularBuffer.P next = buffer.peekForward();
            if (next == null || next.actionId != actionId) break;
            buffer.forward();
            CircularBuffer.P p = buffer.peek();
            if (p == null || p.pos == null) continue;
            if (!p.destroyed) {
                if (p.state != null) {
                    level.setBlockAndUpdate(p.pos, p.state);
                }
            } else {
                level.setBlockAndUpdate(p.pos, p.newState != null ? p.newState : Blocks.AIR.defaultBlockState());
            }
            count++;
        }
        return count;
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
        if (modeName != null) {
            msg.append(Compat.translatable(modeName));
        }
        if (actionName != null) {
            msg.append(" ").append(actionName);
        }
        msg.append(" - " + totalBlocks + " blocks");
        player.displayClientMessage(msg, true);
    }
}
