package net.nicguzzo.wands.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.networking.Networking;

final class DoorHistoryGameTests {
    private static final BlockPos LOWER = new BlockPos(1, 2, 1);
    private static final BlockPos UPPER = LOWER.above();
    private static final int SETTLE_TICKS = 10;

    static void placedDoorUndoRedo(GameTestHelper test) {
        ServerPlayer player = playerWithWand(test);
        BlockState original = placeDoor(test, player);

        replay(player, false);
        assertCleared(test);
        test.runAfterDelay(SETTLE_TICKS, () -> {
            assertCleared(test);
            replay(player, true);
            assertDoor(test, original);
            test.runAfterDelay(SETTLE_TICKS, () -> {
                assertDoor(test, original);
                finish(test, player);
            });
        });
    }

    static void destroyedLowerHalfUndoRedo(GameTestHelper test) {
        destroyAndReplay(test, LOWER);
    }

    static void destroyedUpperHalfUndoRedo(GameTestHelper test) {
        destroyAndReplay(test, UPPER);
    }

    static void redoPlacementSkipsDoorWhenUpperSpaceChanged(GameTestHelper test) {
        ServerPlayer player = playerWithWand(test);
        placeDoor(test, player);
        replay(player, false);
        assertCleared(test);

        test.setBlock(UPPER, Blocks.GOLD_BLOCK);
        replay(player, true);
        test.runAfterDelay(SETTLE_TICKS, () -> {
            test.assertBlockPresent(Blocks.AIR, LOWER);
            test.assertBlockPresent(Blocks.GOLD_BLOCK, UPPER);
            finish(test, player);
        });
    }

    static void replacedDoorRestoresBothHalvesAndSkipsConflictingUndo(GameTestHelper test) {
        ServerPlayer player = playerWithWand(test);
        BlockState original = placeDoor(test, player);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.GLASS));
        fill(test, player, LOWER, WandProps.Action.REPLACE);
        test.assertBlockPresent(Blocks.GLASS, LOWER);
        test.assertBlockPresent(Blocks.AIR, UPPER);

        replay(player, false);
        test.runAfterDelay(SETTLE_TICKS, () -> {
            assertDoor(test, original);
            replay(player, true);
            test.assertBlockPresent(Blocks.GLASS, LOWER);
            test.assertBlockPresent(Blocks.AIR, UPPER);

            test.setBlock(UPPER, Blocks.GOLD_BLOCK);
            replay(player, false);
            test.runAfterDelay(SETTLE_TICKS, () -> {
                test.assertBlockPresent(Blocks.GLASS, LOWER);
                test.assertBlockPresent(Blocks.GOLD_BLOCK, UPPER);
                finish(test, player);
            });
        });
    }

    private static void destroyAndReplay(GameTestHelper test, BlockPos target) {
        ServerPlayer player = playerWithWand(test);
        BlockState original = placeDoor(test, player);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        fill(test, player, target, WandProps.Action.DESTROY);
        assertCleared(test);

        replay(player, false);
        assertDoor(test, original);
        test.runAfterDelay(SETTLE_TICKS, () -> {
            assertDoor(test, original);
            replay(player, true);
            assertCleared(test);
            test.runAfterDelay(SETTLE_TICKS, () -> {
                assertCleared(test);
                finish(test, player);
            });
        });
    }

    private static ServerPlayer playerWithWand(GameTestHelper test) {
        ServerPlayer player = test.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);
        BlockPos standing = test.absolutePos(LOWER.east(2));
        player.setPos(standing.getX() + 0.5, standing.getY(), standing.getZ() + 0.5);
        player.setItemInHand(InteractionHand.MAIN_HAND,
                new ItemStack(BuiltInRegistries.ITEM.get(WandsMod.diamond_wand_id.id())));
        PlayerWand.add_player(player);
        return player;
    }

    private static BlockState placeDoor(GameTestHelper test, ServerPlayer player) {
        test.setBlock(LOWER.below(), Blocks.STONE);
        test.setBlock(LOWER, Blocks.AIR);
        test.setBlock(UPPER, Blocks.AIR);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.OAK_DOOR));
        fill(test, player, LOWER, WandProps.Action.PLACE);
        BlockState lower = test.getBlockState(LOWER);
        test.assertTrue(lower.is(Blocks.OAK_DOOR)
                        && lower.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER,
                "Initial wand placement did not produce the lower door half");
        assertDoor(test, lower);
        return lower;
    }

    private static void fill(GameTestHelper test, ServerPlayer player, BlockPos target, WandProps.Action action) {
        ItemStack stack = player.getMainHandItem();
        WandProps.switchMode(stack, WandProps.Mode.FILL);
        WandProps.setAction(stack, action);
        BlockPos pos = test.absolutePos(target);
        Networking.ReceivePosPacket(player, Direction.UP, true, true, pos, pos, Vec3.atCenterOf(pos), 0);
    }

    private static void replay(ServerPlayer player, boolean redo) {
        WandsMod.process_keys(player, WandsMod.WandKeys.UNDO.ordinal(), redo, false);
    }

    private static void assertDoor(GameTestHelper test, BlockState original) {
        test.assertTrue(test.getBlockState(LOWER) == original, "Lower door state differs from the original");
        test.assertTrue(test.getBlockState(UPPER) == original.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER),
                "Upper door state differs from the original");
    }

    private static void assertCleared(GameTestHelper test) {
        test.assertBlockPresent(Blocks.AIR, LOWER);
        test.assertBlockPresent(Blocks.AIR, UPPER);
    }

    private static void finish(GameTestHelper test, ServerPlayer player) {
        PlayerWand.remove_player(player);
        test.succeed();
    }
}
