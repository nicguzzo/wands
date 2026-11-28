package net.nicguzzo.wands.wand;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.items.BlueprintItem;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.networking.Networking;

public class BlueprintGameTests implements FabricGameTest {
    private ServerPlayer player(GameTestHelper test, GameType mode) {
        ServerPlayer player = test.makeMockServerPlayerInLevel();
        player.setGameMode(mode);
        player.setItemInHand(InteractionHand.MAIN_HAND,
                new ItemStack(BuiltInRegistries.ITEM.get(WandsMod.diamond_wand_id.id())));
        if (PlayerWand.get(player) == null) {
            PlayerWand.add_player(player);
        }
        return player;
    }

    private ItemStack blank() {
        return new ItemStack(BuiltInRegistries.ITEM.get(WandsMod.blueprint_id.id()));
    }

    private void action(ServerPlayer player, int action, String first, String second) {
        Networking.ReceiveBlueprintAction(player, action, first, second);
    }

    private void copy(GameTestHelper test, ServerPlayer player, BlockPos start, BlockPos end) {
        WandProps.switchMode(player.getMainHandItem(), WandProps.Mode.COPY);
        BlockPos first = test.absolutePos(start);
        Networking.ReceivePosPacket(player, Direction.UP, true, true, first, test.absolutePos(end),
                Vec3.atCenterOf(first), 0);
    }

    private void paste(GameTestHelper test, ServerPlayer player, BlockPos origin) {
        paste(test, player, origin, WandProps.Action.PLACE);
    }

    private void paste(GameTestHelper test, ServerPlayer player, BlockPos origin, WandProps.Action action) {
        ItemStack wand = player.getMainHandItem();
        WandProps.switchMode(wand, WandProps.Mode.PASTE);
        WandProps.setAction(wand, action);
        BlockPos absolute = test.absolutePos(origin);
        Networking.ReceivePosPacket(player, Direction.UP, true, false, absolute, null,
                Vec3.atCenterOf(absolute), 0);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void placeResumesPastTheWandLimit(GameTestHelper test) {
        checkTierLimitedPaste(test, WandProps.Action.PLACE, Blocks.AIR, Blocks.STONE);
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void destroyResumesPastTheWandLimit(GameTestHelper test) {
        checkTierLimitedPaste(test, WandProps.Action.DESTROY, Blocks.STONE, Blocks.AIR);
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void destroyResumesPastWaterLeftByWaterloggedBlocks(GameTestHelper test) {
        ServerPlayer player = player(test, GameType.CREATIVE);
        BlockPos origin = new BlockPos(2, 2, 2);
        var waterloggedSlab = Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.WATERLOGGED, true);
        var stone = Blocks.STONE.defaultBlockState();
        ItemStack blueprint = blank();
        BlueprintItem.saveCopyBufferToBlueprint(blueprint, List.of(
                new CopyBuffer(BlockPos.ZERO, waterloggedSlab),
                new CopyBuffer(new BlockPos(1, 0, 0), waterloggedSlab),
                new CopyBuffer(new BlockPos(2, 0, 0), stone),
                new CopyBuffer(new BlockPos(3, 0, 0), stone)));
        test.setBlock(origin, waterloggedSlab);
        test.setBlock(origin.east(), waterloggedSlab);
        test.setBlock(origin.east(2), stone);
        test.setBlock(origin.east(3), stone);
        player.getInventory().setItem(2, blueprint);
        action(player, 3, BlueprintItem.getId(blueprint), "");

        WandItem stoneWand = (WandItem) BuiltInRegistries.ITEM.get(WandsMod.stone_wand_id.id());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(stoneWand));
        int previousLimit = stoneWand.limit;
        try {
            stoneWand.limit = 2;
            paste(test, player, origin, WandProps.Action.DESTROY);
            test.assertBlockPresent(Blocks.WATER, origin);
            test.assertBlockPresent(Blocks.WATER, origin.east());
            test.assertBlockPresent(Blocks.STONE, origin.east(2));
            test.assertBlockPresent(Blocks.STONE, origin.east(3));

            paste(test, player, origin, WandProps.Action.DESTROY);
            test.assertBlockPresent(Blocks.WATER, origin);
            test.assertBlockPresent(Blocks.WATER, origin.east());
            test.assertBlockPresent(Blocks.AIR, origin.east(2));
            test.assertBlockPresent(Blocks.AIR, origin.east(3));
        } finally {
            stoneWand.limit = previousLimit;
        }
        test.succeed();
    }

    private void checkTierLimitedPaste(GameTestHelper test, WandProps.Action operation, Block before, Block after) {
        ServerPlayer player = player(test, GameType.CREATIVE);
        BlockPos origin = new BlockPos(2, 2, 2);
        ItemStack blueprint = blank();
        List<CopyBuffer> cells = new ArrayList<>();
        for (int index = 0; index < 4; index++) {
            cells.add(new CopyBuffer(new BlockPos(index, 0, 0), Blocks.STONE.defaultBlockState()));
            test.setBlock(origin.east(index), before);
        }
        BlueprintItem.saveCopyBufferToBlueprint(blueprint, cells);
        player.getInventory().setItem(2, blueprint);
        action(player, 3, BlueprintItem.getId(blueprint), "");

        WandItem stoneWand = (WandItem) BuiltInRegistries.ITEM.get(WandsMod.stone_wand_id.id());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(stoneWand));
        int previousLimit = stoneWand.limit;
        try {
            stoneWand.limit = 2;
            paste(test, player, origin, operation);
            test.assertBlockPresent(after, origin);
            test.assertBlockPresent(after, origin.east());
            test.assertBlockPresent(before, origin.east(2));
            test.assertBlockPresent(before, origin.east(3));

            paste(test, player, origin, operation);
            int changed = 0;
            for (int index = 0; index < 4; index++) {
                if (test.getLevel().getBlockState(test.absolutePos(origin.east(index))).is(after)) {
                    changed++;
                }
            }
            test.assertTrue(changed == 4, operation.name() + " should change 4 cells after two clicks, changed " + changed);
        } finally {
            stoneWand.limit = previousLimit;
        }
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void savedLayoutSurvivesReloadAndNewCopy(GameTestHelper test) {
        ServerPlayer player = player(test, GameType.CREATIVE);
        ItemStack wand = player.getMainHandItem();
        BlockPos firstCorner = new BlockPos(4, 3, 3);
        BlockPos secondCorner = new BlockPos(2, 2, 2);
        BlockPos stairs = new BlockPos(3, 2, 2);
        test.setBlock(firstCorner, Blocks.GLASS);
        test.setBlock(secondCorner, Blocks.OAK_PLANKS);
        test.setBlock(stairs, Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        copy(test, player, firstCorner, secondCorner);
        test.assertTrue(PlayerWand.get(player).copy_paste_buffer.size() == 3, "Copy must contain exactly three solid blocks");

        ItemStack blueprint = blank();
        player.setItemInHand(InteractionHand.MAIN_HAND, blueprint);
        action(player, 0, "", "");
        action(player, 1, "minecraft:oak_planks", "minecraft:spruce_planks");
        action(player, 1, "minecraft:oak_stairs", "minecraft:spruce_stairs");
        action(player, 2, "Gate corner", "");
        var registries = test.getLevel().registryAccess();
        ItemStack reloaded = ItemStack.parse(registries, blueprint.save(registries)).orElseThrow();
        String id = BlueprintItem.getId(blueprint);
        test.assertTrue(!id.isEmpty() && id.equals(BlueprintItem.getId(reloaded)), "Blueprint identity was lost during item serialization");
        test.assertTrue(BlueprintItem.getBlueprintName(reloaded).equals("Gate corner"), "Name was lost during item serialization");
        test.assertTrue(BlueprintItem.getTotalBlockCount(reloaded) == 3
                && Arrays.equals(BlueprintItem.getDimensions(reloaded), new int[]{3, 2, 2}), "Incorrect saved dimensions or block count");
        test.assertTrue(BlueprintItem.getReplacements(reloaded).get(Blocks.OAK_STAIRS) == Blocks.SPRUCE_STAIRS,
                "Material replacement was lost during item serialization");
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);
        player.getInventory().setItem(2, reloaded);

        BlockPos newerCopy = new BlockPos(2, 2, 5);
        test.setBlock(newerCopy, Blocks.DIAMOND_BLOCK);
        copy(test, player, newerCopy, newerCopy);
        test.assertTrue(PlayerWand.get(player).getActiveBlueprintId().isEmpty(), "New copy did not select the in-memory source");
        BlockPos memoryTarget = new BlockPos(4, 2, 5);
        test.setBlock(memoryTarget, Blocks.AIR);
        paste(test, player, memoryTarget);
        test.assertBlockPresent(Blocks.DIAMOND_BLOCK, memoryTarget);

        action(player, 3, id, "");
        WandProps.setRotation(wand, Rotation.CLOCKWISE_90);
        BlockPos origin = new BlockPos(6, 3, 5);
        BlockPos expectedPlanks = origin.offset(1, -1, -2);
        BlockPos expectedStairs = origin.offset(1, -1, -1);
        BlockPos preservedGap = origin.offset(0, -1, -1);
        test.setBlock(origin, Blocks.AIR);
        test.setBlock(expectedPlanks, Blocks.AIR);
        test.setBlock(expectedStairs, Blocks.AIR);
        test.setBlock(preservedGap, Blocks.GOLD_BLOCK);
        paste(test, player, origin);
        test.assertBlockPresent(Blocks.GLASS, origin);
        test.assertBlockPresent(Blocks.SPRUCE_PLANKS, expectedPlanks);
        test.assertTrue(test.getLevel().getBlockState(test.absolutePos(expectedStairs)).is(Blocks.SPRUCE_STAIRS)
                && test.getLevel().getBlockState(test.absolutePos(expectedStairs)).getValue(StairBlock.FACING) == Direction.EAST,
                "Rotated stair did not preserve its substituted material and facing");
        test.assertBlockPresent(Blocks.GOLD_BLOCK, preservedGap);
        test.assertBlockPresent(Blocks.OAK_PLANKS, secondCorner);
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void shulkerSaveUpdatesOnlyTheSelectedSlot(GameTestHelper test) {
        ServerPlayer player = player(test, GameType.CREATIVE);
        BlockPos source = new BlockPos(2, 2, 2);
        test.setBlock(source, Blocks.IRON_BLOCK);
        copy(test, player, source, source);
        ItemStack blueprint = blank();
        String id = BlueprintItem.getOrCreateId(blueprint);
        BlueprintItem.setBlueprintName(blueprint, "Shulker design");
        ItemStack shulker = new ItemStack(Items.SHULKER_BOX);
        List<ItemStack> slots = new ArrayList<>(Compat.get_shulker_contents(shulker));
        slots.set(5, new ItemStack(Items.EMERALD, 3));
        slots.set(17, blueprint);
        Compat.set_shulker_contents(shulker, slots);
        player.getInventory().setItem(2, shulker);

        action(player, 4, id, "");
        List<ItemStack> actual = Compat.get_shulker_contents(shulker);
        ItemStack saved = actual.get(17);
        test.assertTrue(actual.get(5).is(Items.EMERALD) && actual.get(5).getCount() == 3, "Adjacent shulker contents changed");
        test.assertTrue(id.equals(BlueprintItem.getId(saved)) && BlueprintItem.getBlueprintName(saved).equals("Shulker design")
                && BlueprintItem.getTotalBlockCount(saved) == 1 && saved.getCount() == 1, "Saved blueprint or its item metadata was lost");
        test.assertTrue(PlayerWand.get(player).getActiveBlueprintId().equals(id), "Shulker blueprint was not selected after save");
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void survivalResumesWithoutChargingForExistingOrBlockedBlocks(GameTestHelper test) {
        ServerPlayer player = player(test, GameType.SURVIVAL);
        ItemStack blueprint = blank();
        BlueprintItem.saveCopyBufferToBlueprint(blueprint, List.of(
                new CopyBuffer(BlockPos.ZERO, Blocks.OAK_PLANKS.defaultBlockState()),
                new CopyBuffer(new BlockPos(1, 0, 0), Blocks.OAK_PLANKS.defaultBlockState()),
                new CopyBuffer(new BlockPos(2, 0, 0), Blocks.OAK_PLANKS.defaultBlockState()),
                new CopyBuffer(new BlockPos(3, 0, 0), Blocks.GLASS.defaultBlockState())));
        BlueprintItem.setReplacement(blueprint, Blocks.OAK_PLANKS, Blocks.SPRUCE_PLANKS);
        player.getInventory().setItem(2, blueprint);
        player.getInventory().setItem(3, new ItemStack(Items.SPRUCE_PLANKS, 2));
        player.getInventory().setItem(4, new ItemStack(Items.OAK_PLANKS, 4));
        action(player, 3, BlueprintItem.getId(blueprint), "");

        BlockPos origin = new BlockPos(2, 2, 2);
        test.setBlock(origin, Blocks.SPRUCE_PLANKS);
        test.setBlock(origin.east(), Blocks.AIR);
        test.setBlock(origin.east(2), Blocks.GOLD_BLOCK);
        test.setBlock(origin.east(3), Blocks.AIR);
        paste(test, player, origin);
        test.assertBlockPresent(Blocks.SPRUCE_PLANKS, origin);
        test.assertBlockPresent(Blocks.SPRUCE_PLANKS, origin.east());
        test.assertBlockPresent(Blocks.GOLD_BLOCK, origin.east(2));
        test.assertBlockPresent(Blocks.AIR, origin.east(3));
        test.assertTrue(player.getInventory().getItem(3).getCount() == 1, "First paste should consume exactly one spruce plank");

        player.getInventory().setItem(5, new ItemStack(Items.GLASS, 2));
        paste(test, player, origin);
        paste(test, player, origin);
        test.assertBlockPresent(Blocks.GLASS, origin.east(3));
        test.assertBlockPresent(Blocks.GOLD_BLOCK, origin.east(2));
        test.assertTrue(player.getInventory().getItem(3).getCount() == 1 && player.getInventory().getItem(4).getCount() == 4
                && player.getInventory().getItem(5).getCount() == 1, "Repeated paste charged for blocks it did not place");
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void denseBlueprintPreservesOffsetsAcrossSignedPackingBoundaries(GameTestHelper test) {
        ItemStack blueprint = blank();
        List<CopyBuffer> dense = new ArrayList<>();
        for (int x = -513; x <= 512; x++) {
            Block block = x < 0 ? Blocks.STONE : Blocks.GLASS;
            dense.add(new CopyBuffer(new BlockPos(x, 0, 0), block.defaultBlockState()));
        }
        BlueprintItem.saveCopyBufferToBlueprint(blueprint, dense);
        assertSavedLayout(test, blueprint, dense, new int[]{1026, 1, 1});
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void sparseBlueprintPreservesExactOffsetsAndGaps(GameTestHelper test) {
        ItemStack blueprint = blank();
        List<CopyBuffer> sparse = List.of(
                new CopyBuffer(new BlockPos(-513, -513, -513), Blocks.STONE.defaultBlockState()),
                new CopyBuffer(BlockPos.ZERO, Blocks.COPPER_BLOCK.defaultBlockState()),
                new CopyBuffer(new BlockPos(512, 512, 512), Blocks.GLASS.defaultBlockState()));
        BlueprintItem.saveCopyBufferToBlueprint(blueprint, sparse);
        assertSavedLayout(test, blueprint, sparse, new int[]{1026, 1026, 1026});
        test.succeed();
    }

    private void assertSavedLayout(GameTestHelper test, ItemStack stack, List<CopyBuffer> expected, int[] dimensions) {
        var registries = test.getLevel().registryAccess();
        ItemStack reloaded = ItemStack.parse(registries, stack.save(registries)).orElseThrow();
        List<CopyBuffer> restored = BlueprintItem.getCopyBuffer(reloaded);
        test.assertTrue(BlueprintItem.getTotalBlockCount(reloaded) == expected.size() && restored.size() == expected.size(),
                "Expected " + expected.size() + " cells, decoded " + restored.size());
        test.assertTrue(Arrays.equals(BlueprintItem.getDimensions(reloaded), dimensions), "Incorrect bounding dimensions");
        for (int index = 0; index < expected.size(); index++) {
            CopyBuffer cell = expected.get(index);
            CopyBuffer actual = restored.get(index);
            test.assertTrue(cell.pos.equals(actual.pos) && cell.state.equals(actual.state),
                    "Expected " + cell.state + " at " + cell.pos + ", decoded " + actual.state + " at " + actual.pos);
        }
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void packedBlueprintFrom26_2KeepsBothPaletteFormatsAndOrientation(GameTestHelper test) throws Exception {
        CompoundTag legacy;
        try (InputStream fixture = BlueprintGameTests.class.getResourceAsStream("/wands-tests/blueprint-26.2.snbt")) {
            if (fixture == null) throw new IOException("Missing saved 26.2 blueprint fixture");
            legacy = TagParser.parseTag(new String(fixture.readAllBytes(), StandardCharsets.UTF_8));
        }
        assertCapturedLayout(test, legacy, "26.2");

        CompoundTag modern = legacy.copy();
        var palette = modern.getCompound("components").getCompound("minecraft:custom_data")
                .getCompound("Blueprint").getList("States", 10);
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag state = palette.getCompound(index);
            state.put("id", state.get("Name"));
            state.remove("Name");
            if (state.contains("Properties")) {
                state.put("properties", state.get("Properties"));
                state.remove("Properties");
            }
        }
        assertCapturedLayout(test, modern, "modern");
        test.succeed();
    }

    private void assertCapturedLayout(GameTestHelper test, CompoundTag item, String format) {
        ItemStack stack = ItemStack.parse(test.getLevel().registryAccess(), item).orElseThrow();
        test.assertTrue("bac84b51-7ba3-40af-983b-a33b1c047817".equals(BlueprintItem.getId(stack))
                && "Bridge test".equals(BlueprintItem.getBlueprintName(stack)), format + " item identity changed");
        test.assertTrue(BlueprintItem.getReplacements(stack).get(Blocks.OAK_STAIRS) == Blocks.SPRUCE_STAIRS,
                format + " replacement is missing");
        assertSavedLayout(test, stack, List.of(
                new CopyBuffer(BlockPos.ZERO, Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.EAST).setValue(StairBlock.HALF, Half.TOP)),
                new CopyBuffer(new BlockPos(-2, 1, 3), Blocks.COPPER_BLOCK.defaultBlockState())), new int[]{3, 2, 4});
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void selectorCanSaveToAPristineBlankBlueprint(GameTestHelper test) {
        ServerPlayer player = player(test, GameType.CREATIVE);
        BlockPos source = new BlockPos(2, 2, 2);
        test.setBlock(source, Blocks.STONE);
        copy(test, player, source, source);
        ItemStack blueprint = blank();
        player.getInventory().setItem(2, blueprint);
        InventoryManager.findBlueprints(player);
        test.assertTrue(BlueprintItem.getId(blueprint).isEmpty(), "Inventory discovery assigned an ID before selector open");
        action(player, 6, "", "");
        String selected = BlueprintItem.getId(player.getInventory().getItem(2));
        test.assertTrue(!selected.isEmpty(), "Selector open failed to assign an ID on the server");

        ItemStack swapped = blank();
        player.getInventory().setItem(2, swapped);
        action(player, 4, selected, "");
        test.assertTrue(BlueprintItem.getId(swapped).isEmpty() && BlueprintItem.isEmptyBlueprint(swapped),
                "A stale selector ID wrote into a different blank blueprint");

        player.getInventory().setItem(2, blueprint);
        ItemStack clientSynchronizedCopy = player.getInventory().getItem(2).copy();
        action(player, 4, BlueprintItem.getId(clientSynchronizedCopy), "");
        test.assertTrue(BlueprintItem.getTotalBlockCount(blueprint) == 1,
                "Saving by the synchronized ID left the pristine server-side blueprint empty");
        test.succeed();
    }
}
