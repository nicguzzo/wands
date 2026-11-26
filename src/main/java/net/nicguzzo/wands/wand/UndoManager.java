package net.nicguzzo.wands.wand;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.compat.claims.ClaimDispatch;
import net.nicguzzo.wands.config.WandsConfig;

public class UndoManager {
    private record Change(BlockPos pos, BlockSnapshot before, BlockSnapshot after, String action, String mode) {}

    private final ActionHistory<ResourceKey<Level>, Change> history = new ActionHistory<>(WandsConfig.max_limit);
    private final List<Change> pending = new ArrayList<>();

    void beginAction() {
        pending.clear();
    }

    void record(BlockPos pos, BlockSnapshot before, BlockSnapshot after, String action, String mode) {
        if (!before.equals(after)) pending.add(new Change(pos.immutable(), before, after, action, mode));
    }

    void finishAction(Level level) {
        history.add(level.dimension(), pending);
        pending.clear();
    }

    public void replay(int count, Level level, Player player, boolean forward) {
        if (!(level instanceof ServerLevel server)) return;
        Change[] first = new Change[1];
        var result = history.replay(level.dimension(), count, forward, change -> {
            BlockSnapshot expected = forward ? change.before : change.after;
            BlockSnapshot target = forward ? change.after : change.before;
            if (!server.hasChunkAt(change.pos)
                    || !ClaimDispatch.claimCanInteract(server, change.pos, player)) return false;
            if (!expected.equals(BlockSnapshot.capture(server, change.pos))
                    || !target.restore(server, change.pos, expected)) return false;
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
            boolean removing = (forward ? change.after : change.before).state().isAir();
            var sound = Blocks.STONE.defaultBlockState().getSoundType();
            level.playSound(null, player.blockPosition(), removing ? sound.getBreakSound() : sound.getPlaceSound(),
                    SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        if (result.skipped() > 0) msg.append(Compat.translatable("wands.history.skipped", result.skipped()));
        if (result.otherWorld()) msg.append(Compat.translatable("wands.history.other_world"));
        Compat.displayClientMessage(player, msg, true);
    }
}
