package net.nicguzzo.wands.wand.modes;

import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.compat.claims.ClaimDispatch;
import net.nicguzzo.wands.wand.BlockAccounting;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;

public class BlastMode extends WandMode {
    public void place_in_buffer(Wand wand) {
        wand.block_buffer.reset();
        if(!wand.preview){
            if(!wand.creative) {
                wand.block_accounting.clear();
                BlockAccounting ba=new BlockAccounting();
                int rad= WandProps.getVal(wand.wand_stack, WandProps.Value.BLASTRAD);
                int extra_cost=rad-4;
                ba.needed=1+extra_cost;
                wand.block_accounting.put(Items.TNT,ba);
            }
        }
    }

    @Override
    public boolean action(Wand wand) {

        if( wand.can_blast) {
            boolean do_explode = false;
            BlockAccounting ba = null;
            if (wand.creative) {
                do_explode = true;
            } else {
                ba = wand.block_accounting.get(Items.TNT);
                if (ba != null) {
                    do_explode = (ba.needed <= ba.in_player);
                }
            }
            if (do_explode) {
                float radius = (float) WandProps.getVal(wand.wand_stack, WandProps.Value.BLASTRAD);
                float eo = 1.0625f;
                if (wand.getSide() == Direction.DOWN) {
                    eo = 0.0625f;
                }
                ServerLevel level = (ServerLevel) wand.level;
                if (!ClaimDispatch.claimCanInteract(level, wand.pos, wand.player, ClaimDispatch.Action.BREAK, null)) {
                    Compat.displayClientMessage(wand.player, Compat.translatable("wands.message.claimed_chunk").withStyle(ChatFormatting.RED), true);
                    return false;
                }
                var calculator = new EntityBasedExplosionDamageCalculator(wand.player) {
                    private final Long2BooleanOpenHashMap permissions = new Long2BooleanOpenHashMap();

                    @Override
                    public boolean shouldBlockExplode(Explosion explosion, BlockGetter world, BlockPos pos, BlockState state, float power) {
                        if (!super.shouldBlockExplode(explosion, world, pos, state, power)) return false;
                        if (state.isAir()) return true;
                        // Neighboring blast rays commonly hit the same block.
                        long key = pos.asLong();
                        if (!permissions.containsKey(key)) {
                            permissions.put(key, ClaimDispatch.canChange(level, pos, wand.player, state, ClaimDispatch.Action.BREAK, null));
                        }
                        return permissions.get(key);
                    }
                };
                level.explode(wand.player, null, calculator, wand.pos.getX(), wand.pos.getY() + eo, wand.pos.getZ(),
                        radius, false, Level.ExplosionInteraction.BLOCK);
                if (!wand.creative && ba != null) {
                    ba.placed = ba.needed;
                }
                return true;
            }
        }
        return false;
    }
}
