package net.nicguzzo.wands.wand;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.menues.MagicBagMenu;
import net.nicguzzo.wands.menues.MagicBagMenuData;
import net.nicguzzo.wands.networking.Networking;

public class WandGameTests implements FabricGameTest {
    private static final BlockState DOUBLE_SLAB = Blocks.STONE_SLAB.defaultBlockState()
            .setValue(SlabBlock.TYPE, SlabType.DOUBLE);
    private final BlockPos target = new BlockPos(1, 2, 1);

    private ServerPlayer player(GameTestHelper test, GameType mode) {
        ServerPlayer player = test.makeMockServerPlayerInLevel();
        player.setGameMode(mode);
        player.setItemInHand(InteractionHand.MAIN_HAND,
                new ItemStack(BuiltInRegistries.ITEM.get(WandsMod.diamond_wand_id.id())));
        player.getInventory().setItem(1, new ItemStack(Items.DIAMOND_PICKAXE));
        return player;
    }

    private void fill(GameTestHelper test, ServerPlayer player, Wand wand, WandProps.Action action) {
        fill(test, player, wand, action, target);
    }

    private void fill(GameTestHelper test, ServerPlayer player, Wand wand, WandProps.Action action, BlockPos end) {
        var pos = test.absolutePos(target);
        var stack = player.getMainHandItem();
        WandProps.setMode(stack, WandProps.Mode.FILL);
        WandProps.setAction(stack, action);
        wand.player_data = new CompoundTag();
        wand.player_data.putIntArray("Tools", new int[]{1});
        wand.setP1(pos);
        wand.setP2(test.absolutePos(end));
        wand.do_or_preview(player, test.getLevel(), test.getLevel().getBlockState(pos), pos, Direction.UP,
                Vec3.atCenterOf(pos), stack, (WandItem) stack.getItem(), false);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void creativeReplaceHistory(GameTestHelper test) {
        var player = player(test, GameType.CREATIVE);
        var wand = new Wand();
        test.setBlock(target, Blocks.STONE);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.GLASS));
        fill(test, player, wand, WandProps.Action.REPLACE);
        test.assertBlockPresent(Blocks.GLASS, target);
        var other = test.getLevel().getServer().getLevel(Level.NETHER);
        wand.undoManager.replay(1, other, player, false);
        test.assertBlockPresent(Blocks.GLASS, target);
        wand.undoManager.replay(1, test.getLevel(), player, false);
        test.assertBlockPresent(Blocks.STONE, target);
        wand.undoManager.replay(1, test.getLevel(), player, true);
        test.assertBlockPresent(Blocks.GLASS, target);
        test.setBlock(target, Blocks.GOLD_BLOCK);
        wand.undoManager.replay(1, test.getLevel(), player, false);
        wand.undoManager.replay(1, test.getLevel(), player, true);
        test.assertBlockPresent(Blocks.GOLD_BLOCK, target);
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void partialUndoKeepsIndependentEditOnRedo(GameTestHelper test) {
        var player = player(test, GameType.CREATIVE);
        var wand = new Wand();
        var middle = target.east();
        var end = target.east(2);
        test.setBlock(target, Blocks.STONE);
        test.setBlock(middle, Blocks.GOLD_BLOCK);
        test.setBlock(end, Blocks.STONE);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.GLASS));
        fill(test, player, wand, WandProps.Action.REPLACE, end);
        test.assertBlockPresent(Blocks.GLASS, target);
        test.assertBlockPresent(Blocks.GLASS, middle);
        test.assertBlockPresent(Blocks.GLASS, end);

        test.setBlock(middle, Blocks.EMERALD_BLOCK);
        wand.undoManager.replay(1, test.getLevel(), player, false);
        test.assertBlockPresent(Blocks.STONE, target);
        test.assertBlockPresent(Blocks.EMERALD_BLOCK, middle);
        test.assertBlockPresent(Blocks.STONE, end);
        // A stale redo entry would now match the original block and overwrite it.
        test.setBlock(middle, Blocks.GOLD_BLOCK);
        wand.undoManager.replay(1, test.getLevel(), player, true);
        test.assertBlockPresent(Blocks.GLASS, target);
        test.assertBlockPresent(Blocks.GOLD_BLOCK, middle);
        test.assertBlockPresent(Blocks.GLASS, end);
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void containerHistory(GameTestHelper test) {
        var player = player(test, GameType.CREATIVE);
        var wand = new Wand();
        test.setBlock(target, Blocks.BARREL);
        var pos = test.absolutePos(target);
        var level = test.getLevel();
        ((net.minecraft.world.Container) level.getBlockEntity(pos)).setItem(7, new ItemStack(Items.DIAMOND, 3));
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.GLASS));
        fill(test, player, wand, WandProps.Action.REPLACE);
        test.assertBlockPresent(Blocks.GLASS, target);
        test.assertItemEntityNotPresent(Items.DIAMOND);
        wand.undoManager.replay(1, level, player, false);
        test.assertBlockPresent(Blocks.BARREL, target);
        var barrel = (net.minecraft.world.Container) level.getBlockEntity(pos);
        test.assertTrue(barrel.getItem(7).is(Items.DIAMOND) && barrel.getItem(7).getCount() == 3,
                "Undo lost the original barrel contents");
        barrel.setItem(7, new ItemStack(Items.EMERALD));
        wand.undoManager.replay(1, level, player, true);
        test.assertBlockPresent(Blocks.BARREL, target);
        test.assertTrue(barrel.getItem(7).is(Items.EMERALD), "Redo overwrote a changed inventory");
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void survivalReplaceAndDestroy(GameTestHelper test) {
        var player = player(test, GameType.SURVIVAL);
        var wand = new Wand();
        var flower = new ItemStack(Items.POPPY);
        player.setItemInHand(InteractionHand.OFF_HAND, flower);
        test.setBlock(target, Blocks.STONE);
        test.setBlock(target.below(), Blocks.AIR);
        fill(test, player, wand, WandProps.Action.REPLACE);
        test.assertBlockPresent(Blocks.STONE, target);
        test.assertTrue(flower.getCount() == 1 && player.getMainHandItem().getDamageValue() == 0
                && player.getInventory().getItem(1).getDamageValue() == 0, "Failed replace spent resources");
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        boolean previous = WandsMod.config.destroy_in_survival_drop;
        try {
            WandsMod.config.destroy_in_survival_drop = false;
            fill(test, player, wand, WandProps.Action.DESTROY);
            test.assertBlockPresent(Blocks.AIR, target);
            test.assertItemEntityNotPresent(Items.COBBLESTONE);
            test.assertTrue(player.getMainHandItem().getDamageValue() == 1
                    && player.getInventory().getItem(1).getDamageValue() == 1, "Destroy should charge once");
        } finally {
            WandsMod.config.destroy_in_survival_drop = previous;
        }
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void successfulSurvivalReplace(GameTestHelper test) {
        var player = player(test, GameType.SURVIVAL);
        var wand = new Wand();
        wand.drop_on_player = false;
        var glass = new ItemStack(Items.GLASS, 2);
        player.setItemInHand(InteractionHand.OFF_HAND, glass);
        test.setBlock(target, Blocks.STONE);
        boolean previous = WandsMod.config.destroy_in_survival_drop;
        try {
            WandsMod.config.destroy_in_survival_drop = true;
            fill(test, player, wand, WandProps.Action.REPLACE);
            test.assertBlockPresent(Blocks.GLASS, target);
            test.assertTrue(glass.getCount() == 1, "Replace should use one glass");
            test.assertTrue(player.getMainHandItem().getDamageValue() == 1
                    && player.getInventory().getItem(1).getDamageValue() == 1, "Replace should charge once");
            var dropped = test.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(test.absolutePos(target)).inflate(1), item -> item.getItem().is(Items.COBBLESTONE));
            test.assertTrue(dropped.stream().mapToInt(item -> item.getItem().getCount()).sum() == 1,
                    "Replace should drop one cobblestone");
        } finally {
            WandsMod.config.destroy_in_survival_drop = previous;
        }
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void miningIntoNearlyFullShulker(GameTestHelper test) {
        var player = player(test, GameType.SURVIVAL);
        var wand = new Wand();
        wand.drop_on_player = false;
        var shulker = new ItemStack(Items.SHULKER_BOX);
        List<ItemStack> slots = new ArrayList<>();
        for (int i = 0; i < 26; i++) slots.add(new ItemStack(Items.DIRT, 64));
        slots.add(new ItemStack(Items.STONE_SLAB, 63));
        var initialContents = ItemContainerContents.fromItems(slots);
        shulker.set(DataComponents.CONTAINER, initialContents);
        player.setItemInHand(InteractionHand.OFF_HAND, shulker);
        test.setBlock(target, DOUBLE_SLAB);
        var pos = test.absolutePos(target);
        var level = test.getLevel();
        var pickaxe = player.getInventory().getItem(1);
        player.giveExperiencePoints(2);
        boolean previousDrops = WandsMod.config.destroy_in_survival_drop;
        float previousRate = WandsMod.config.blocks_per_xp;
        try {
            WandsMod.config.destroy_in_survival_drop = true;
            WandsMod.config.blocks_per_xp = 1;
            wand.stop_on_full_inventory = true;
            fill(test, player, wand, WandProps.Action.DESTROY);
            test.assertTrue(level.getBlockState(pos).equals(DOUBLE_SLAB), "Rejected mining changed the double slab");
            test.assertTrue(initialContents.equals(shulker.get(DataComponents.CONTAINER)), "Rejected mining changed the shulker");
            test.assertItemEntityNotPresent(Items.STONE_SLAB);
            test.assertTrue(player.totalExperience == 2 && player.getMainHandItem().getDamageValue() == 0
                    && pickaxe.getDamageValue() == 0, "Rejected mining charged XP or durability");

            wand.stop_on_full_inventory = false;
            fill(test, player, wand, WandProps.Action.DESTROY);
            test.assertBlockPresent(Blocks.AIR, target);
            slots.set(26, new ItemStack(Items.STONE_SLAB, 64));
            test.assertTrue(ItemContainerContents.fromItems(slots).equals(shulker.get(DataComponents.CONTAINER)),
                    "The shulker should receive exactly one slab");
            var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1),
                    entity -> entity.getItem().is(Items.STONE_SLAB));
            test.assertTrue(drops.stream().mapToInt(entity -> entity.getItem().getCount()).sum() == 1,
                    "Exactly one overflow slab should drop");
            test.assertTrue(player.totalExperience == 1 && player.getMainHandItem().getDamageValue() == 1
                    && pickaxe.getDamageValue() == 1, "Mining should charge once for the block");
        } finally {
            WandsMod.config.destroy_in_survival_drop = previousDrops;
            WandsMod.config.blocks_per_xp = previousRate;
        }
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void doubleSlabFillWithThreeItems(GameTestHelper test) {
        var player = player(test, GameType.SURVIVAL);
        var wand = new Wand();
        var end = target.east();
        test.setBlock(target, Blocks.AIR);
        test.setBlock(end, Blocks.AIR);
        player.getInventory().setItem(2, new ItemStack(Items.STONE_SLAB, 3));
        player.giveExperiencePoints(2);
        WandProps.setStateMode(player.getMainHandItem(), WandProps.StateMode.CLONE);
        wand.p1_state = DOUBLE_SLAB;
        float previousRate = WandsMod.config.blocks_per_xp;
        try {
            WandsMod.config.blocks_per_xp = 1;
            fill(test, player, wand, WandProps.Action.PLACE, end);
            var states = List.of(test.getLevel().getBlockState(test.absolutePos(target)),
                    test.getLevel().getBlockState(test.absolutePos(end)));
            test.assertTrue(states.contains(DOUBLE_SLAB) && states.stream().anyMatch(BlockState::isAir),
                    "Exactly one double slab should be placed across the two air blocks");
            var left = player.getInventory().getItem(2);
            test.assertTrue(left.is(Items.STONE_SLAB) && left.getCount() == 1, "The unspent slab should remain");
            test.assertTrue(player.totalExperience == 1 && player.getMainHandItem().getDamageValue() == 1,
                    "Only the completed placement should cost XP and durability");
        } finally {
            WandsMod.config.blocks_per_xp = previousRate;
        }
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void shulkerMaterialPriority(GameTestHelper test) {
        var player = player(test, GameType.SURVIVAL);
        var wand = new Wand();
        var shulker = new ItemStack(Items.SHULKER_BOX);
        List<ItemStack> slots = new ArrayList<>(java.util.Collections.nCopies(27, ItemStack.EMPTY));
        slots.set(5, new ItemStack(Items.GOLD_INGOT));
        slots.set(26, new ItemStack(Items.STONE, 2));
        shulker.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(slots));
        player.setItemInHand(InteractionHand.OFF_HAND, shulker);
        player.getInventory().setItem(2, new ItemStack(Items.STONE, 7));
        var stack = player.getMainHandItem();
        WandProps.setMode(stack, WandProps.Mode.FILL);
        WandProps.setAction(stack, WandProps.Action.PLACE);
        var pos = test.absolutePos(target);
        test.setBlock(target, Blocks.AIR);
        wand.setP1(pos);
        wand.setP2(pos);
        wand.p1_state = Blocks.STONE.defaultBlockState();
        wand.do_or_preview(player, test.getLevel(), Blocks.STONE.defaultBlockState(), pos, Direction.UP,
                Vec3.atCenterOf(pos), stack, (WandItem) stack.getItem(), false);
        test.assertBlockPresent(Blocks.STONE, target);
        var actual = Compat.get_shulker_contents(shulker);
        test.assertTrue(actual.get(0).isEmpty() && actual.get(5).is(Items.GOLD_INGOT)
                && actual.get(26).getCount() == 1 && actual.get(26).is(Items.STONE), "Wrong shulker slot");
        test.assertTrue(player.getInventory().getItem(2).getCount() == 7, "Loose stone was consumed first");
        var count = wand.block_accounting.get(Items.STONE);
        test.assertTrue(count.needed == 1 && count.in_player == 9, "Incorrect material availability");
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void historyEvictionAndBranching(GameTestHelper test) {
        var h = new ActionHistory<String, Integer>(3);
        h.add("world", List.of(1, 2));
        h.add("world", List.of(3));
        h.add("world", List.of(4));
        var seen = new ArrayList<Integer>();
        h.replay("world", 3, false, seen::add);
        test.assertTrue(seen.equals(List.of(4, 3)), "An evicted action was partially retained");
        seen.clear();
        h.replay("world", 2, true, seen::add);
        test.assertTrue(seen.equals(List.of(3, 4)), "Redo lost an action after reaching capacity");
        h.replay("world", 2, false, ignored -> true);
        h.add("world", List.of(5));
        test.assertTrue(h.replay("world", 2, true, ignored -> true).applied() == 0, "Stale redo after new edit");
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void emptyPasteAfterFill(GameTestHelper test) {
        var player = player(test, GameType.CREATIVE);
        var stack = player.getMainHandItem();
        var first = test.absolutePos(target);
        var next = first.east();
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.GLASS));
        WandProps.switchMode(stack, WandProps.Mode.FILL);
        WandProps.setAction(stack, WandProps.Action.REPLACE);
        test.setBlock(target, Blocks.STONE);
        Networking.ReceivePosPacket(player, Direction.UP, true, true, first, first, Vec3.atCenterOf(first), 0);
        test.assertBlockPresent(Blocks.GLASS, target);

        test.setBlock(target, Blocks.STONE);
        test.setBlock(target.east(), Blocks.DIRT);
        WandProps.switchMode(stack, WandProps.Mode.PASTE);
        Networking.ReceivePosPacket(player, Direction.UP, true, false, next, null, Vec3.atCenterOf(next), 0);
        test.assertBlockPresent(Blocks.STONE, target);
        test.assertBlockPresent(Blocks.DIRT, target.east());
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void shulkerAndBagPreserveItemNames(GameTestHelper test) {
        var player = player(test, GameType.SURVIVAL);
        var wand = new Wand();
        wand.player = player;
        wand.level = test.getLevel();
        var inventory = new InventoryManager(player.getInventory());
        var named = new ItemStack(Items.COBBLESTONE);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Named stone"));
        var shulker = new ItemStack(Items.SHULKER_BOX);
        player.setItemInHand(InteractionHand.OFF_HAND, shulker);
        inventory.place_into(wand, named.copy());
        inventory.place_into(wand, new ItemStack(Items.COBBLESTONE));
        inventory.place_into(wand, named.copy());
        var slots = Compat.get_shulker_contents(shulker);
        test.assertTrue(Compat.is_same_with_components(slots.get(0), named) && slots.get(0).getCount() == 2,
                "Shulker should merge only matching names");
        test.assertTrue(slots.get(1).is(Items.COBBLESTONE) && !slots.get(1).has(DataComponents.CUSTOM_NAME)
                && slots.get(1).getCount() == 1, "Shulker lost the plain cobblestone");

        var bag = new ItemStack(BuiltInRegistries.ITEM.get(WandsMod.magic_bag_1_id.id()));
        player.setItemInHand(InteractionHand.OFF_HAND, bag);
        inventory.place_into(wand, named.copy());
        test.assertFalse(inventory.place_into(wand, new ItemStack(Items.COBBLESTONE)), "Bag accepted different components");
        var menu = new MagicBagMenu(1, player.getInventory(), new MagicBagMenuData(bag));
        player.getInventory().setItem(2, new ItemStack(Items.COBBLESTONE));
        menu.clicked(2, 0, net.minecraft.world.inventory.ClickType.QUICK_MOVE, player);
        test.assertTrue(MagicBagItem.getTotal(bag) == 1 && player.getInventory().getItem(2).getCount() == 1,
                "Shift-click merged different components");
        player.getInventory().setItem(2, named.copy());
        menu.clicked(2, 0, net.minecraft.world.inventory.ClickType.QUICK_MOVE, player);
        test.assertTrue(MagicBagItem.getTotal(bag) == 2 && player.getInventory().getItem(2).isEmpty(),
                "Shift-click failed to merge matching components");
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void survivalXpCharges(GameTestHelper test) {
        var player = player(test, GameType.SURVIVAL);
        var wand = new Wand();
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.GLASS, 8));
        float previous = WandsMod.config.blocks_per_xp;
        try {
            WandsMod.config.blocks_per_xp = 2;
            player.giveExperiencePoints(1);
            test.setBlock(target, Blocks.STONE);
            fill(test, player, wand, WandProps.Action.REPLACE);
            test.assertBlockPresent(Blocks.GLASS, target);
            test.assertTrue(Math.abs(player.experienceProgress * 7 - 0.5f) < 0.00001f, "Lost fractional XP");
            test.setBlock(target, Blocks.STONE);
            fill(test, player, wand, WandProps.Action.REPLACE);
            test.assertBlockPresent(Blocks.GLASS, target);
            test.assertTrue(player.experienceLevel == 0 && player.experienceProgress == 0 && player.totalExperience == 0,
                    "Spending the last point must empty the bar");
            test.setBlock(target, Blocks.STONE);
            fill(test, player, wand, WandProps.Action.REPLACE);
            test.assertBlockPresent(Blocks.STONE, target);

            WandsMod.config.blocks_per_xp = 0.05f;
            player.giveExperiencePoints(27);
            fill(test, player, wand, WandProps.Action.REPLACE);
            test.assertBlockPresent(Blocks.GLASS, target);
            test.assertTrue(player.experienceLevel == 1 && player.experienceProgress < 0.00001f
                    && player.totalExperience == 7, "Multi-level debit used the wrong XP cost");
        } finally {
            WandsMod.config.blocks_per_xp = previous;
        }
        test.succeed();
    }
}
