package net.nicguzzo.wands.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.nicguzzo.wands.WandsMod;

/**
 * Client-side anchor that decouples wand preview/placement from the crosshair.
 * Works for all modes. Cleared after each action or on mode change.
 *
 * Two activation paths:
 * - toggle() — persistent, activated by G key, supports arrow-key movement
 * - freeze() — non-persistent, activated by alt-hold, no arrow-key movement
 */
public class Anchor {
    private boolean set = false;
    private boolean persistent = false;
    private BlockPos pos = null;
    private Direction side = null;

    public boolean isSet() { return set; }
    public boolean isActive() { return set && pos != null; }
    public boolean isPersistent() { return persistent; }
    public BlockPos getPos() { return pos; }
    public Direction getSide() { return side; }

    public void clear() {
        set = false;
        persistent = false;
        pos = null;
        side = null;
    }

    /**
     * Toggle anchor on/off from the crosshair hit (G key).
     * Persistent — supports arrow-key movement.
     * Applies INCSELBLOCK offset only for modes that support it.
     * @return true always — the key is always consumed client-side
     */
    public boolean toggle(HitResult hitResult, ItemStack wand, WandProps.Mode mode) {
        if (set) {
            clear();
        } else if (!mode.supports_anchor()) {
            return false;
        } else if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos hitPos = blockHit.getBlockPos();
            Direction hitSide = blockHit.getDirection();
            boolean modeSupportsIncSel = WandProps.flagAppliesTo(WandProps.Flag.INCSELBLOCK, mode);
            if (modeSupportsIncSel && !WandProps.getFlag(wand, WandProps.Flag.INCSELBLOCK)) {
                hitPos = hitPos.relative(hitSide, 1);
            }
            set = true;
            persistent = true;
            pos = hitPos;
            side = hitSide;
        }
        return true;
    }

    /**
     * Freeze anchor from alt-hold. Non-persistent — no arrow movement, released on key up.
     * Does nothing if anchor is already active (toggle takes precedence).
     * @return true if freeze was activated, false if not (already active, unsupported mode, no block target)
     */
    public boolean freeze(HitResult hitResult, ItemStack wand, WandProps.Mode mode) {
        if (set) return false;
        if (!mode.supports_anchor()) return false;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) return false;

        BlockHitResult blockHit = (BlockHitResult) hitResult;
        BlockPos hitPos = blockHit.getBlockPos();
        Direction hitSide = blockHit.getDirection();
        boolean modeSupportsIncSel = WandProps.flagAppliesTo(WandProps.Flag.INCSELBLOCK, mode);
        if (modeSupportsIncSel && !WandProps.getFlag(wand, WandProps.Flag.INCSELBLOCK)) {
            hitPos = hitPos.relative(hitSide, 1);
        }
        set = true;
        persistent = false;
        pos = hitPos;
        side = hitSide;
        return true;
    }

    /**
     * Release alt-hold freeze. Only clears if non-persistent (alt-hold).
     * @return true if the anchor was cleared
     */
    public boolean release() {
        if (!set || persistent) return false;
        clear();
        return true;
    }

    /**
     * Move anchor by arrow key. Only allowed for persistent (toggle) anchors.
     * @return true if the key was consumed (anchor active), false to let the key pass through to server
     */
    public boolean move(WandsMod.WandKeys key, boolean shift, Direction playerFacing) {
        if (!isActive()) return false;
        if (!persistent) return false;

        switch (key) {
            case N_INC: pos = shift ? pos.above() : pos.relative(playerFacing); break;
            case N_DEC: pos = shift ? pos.below() : pos.relative(playerFacing.getOpposite()); break;
            case M_INC: pos = pos.relative(playerFacing.getClockWise()); break;
            case M_DEC: pos = pos.relative(playerFacing.getCounterClockWise()); break;
            default: return false;
        }
        return true;
    }

    /**
     * Get the effective side direction. If the crosshair is on the anchor block,
     * use that face (allows changing direction by looking at different faces).
     * Otherwise fall back to the stored side from when the anchor was set.
     */
    public Direction getEffectiveSide(HitResult hitResult, Direction playerDirection) {
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK
                && ((BlockHitResult) hitResult).getBlockPos().equals(pos)) {
            return ((BlockHitResult) hitResult).getDirection();
        }
        return side != null ? side : playerDirection.getOpposite();
    }
}
