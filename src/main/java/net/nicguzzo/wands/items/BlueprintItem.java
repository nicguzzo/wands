package net.nicguzzo.wands.items;

//? if >= 1.21.5 {
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
//? } else {
/*import org.jetbrains.annotations.Nullable;
*///? }
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
//? if >= 1.21.2 {
import net.minecraft.world.InteractionResult;
//? } else {
/*import net.minecraft.world.InteractionResultHolder;
*///? }
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.wand.CopyBuffer;

import java.util.*;

public class BlueprintItem extends Item {

    public static final String TAG_BLUEPRINT = "Blueprint";
    public static final String TAG_ID = "Id";
    public static final String TAG_NAME = "Name";
    public static final String TAG_SIZE = "Size";
    public static final String TAG_STATES = "States";
    public static final String TAG_BLOCKS = "Blocks";
    public static final String TAG_REPLACEMENTS = "Replacements";

    public static class BlockGroup {
        public final Block originalBlock;
        public int count;
        public Block replacementBlock;

        public BlockGroup(Block originalBlock, int count, Block replacementBlock) {
            this.originalBlock = originalBlock;
            this.count = count;
            this.replacementBlock = replacementBlock;
        }

        public Block getEffectiveBlock() {
            return (replacementBlock != null && replacementBlock != Blocks.AIR) ? replacementBlock : originalBlock;
        }
    }

    public BlueprintItem(Properties properties) {
        super(properties);
    }

    public static BlueprintItem create() {
        return new BlueprintItem(new Item.Properties().stacksTo(1)
                //?if >= 1.21.11
                .setId(net.nicguzzo.wands.WandsMod.blueprint_key)
        );
    }

    //? if >= 1.21.2 {
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack blueprintStack = player.getItemInHand(interactionHand);
        if (!level.isClientSide() && interactionHand == InteractionHand.MAIN_HAND) {
            getOrCreateId(blueprintStack);
            Compat.open_menu((ServerPlayer) player, blueprintStack, 3);
        }
        return InteractionResult.PASS;
    }
    //? } else {
    /*@Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack blueprintStack = player.getItemInHand(interactionHand);
        if (!level.isClientSide() && interactionHand == InteractionHand.MAIN_HAND) {
            getOrCreateId(blueprintStack);
            Compat.open_menu((ServerPlayer) player, blueprintStack, 3);
        }
        return InteractionResultHolder.pass(blueprintStack);
    }
    *///? }

    //? if >= 1.21.5 {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        appendBlueprintTooltip(stack, consumer);
    }
    //? } else {
        //? if >= 1.20.5 {
        /*@Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag tooltipFlag) {
            appendBlueprintTooltip(stack, list::add);
        }
        *///? } else {
        /*@Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
            appendBlueprintTooltip(stack, list::add);
        }
        *///? }
    //? }

    private void appendBlueprintTooltip(ItemStack stack, java.util.function.Consumer<Component> out) {
        String name = getBlueprintName(stack);
        if (!name.isEmpty()) {
            out.accept(Compat.literal(name).withStyle(ChatFormatting.AQUA));
        }
        int totalBlocks = getTotalBlockCount(stack);
        if (totalBlocks > 0) {
            int[] size = getDimensions(stack);
            out.accept(Compat.literal("Blocks: " + totalBlocks + " (" + size[0] + "x" + size[1] + "x" + size[2] + ")").withStyle(ChatFormatting.GRAY));
            List<BlockGroup> groups = getBlockGroups(stack);
            int limit = Math.min(groups.size(), 8);
            for (int i = 0; i < limit; i++) {
                BlockGroup g = groups.get(i);
                MutableComponent line = Compat.literal("  " + g.count + "x ")
                        .append(Compat.translatable(g.originalBlock.getDescriptionId()));
                if (g.replacementBlock != null && g.replacementBlock != Blocks.AIR && g.replacementBlock != g.originalBlock) {
                    line.append(Compat.literal(" -> ").withStyle(ChatFormatting.YELLOW))
                        .append(Compat.translatable(g.replacementBlock.getDescriptionId()).withStyle(ChatFormatting.GREEN));
                }
                out.accept(line.withStyle(ChatFormatting.DARK_GRAY));
            }
            if (groups.size() > limit) {
                out.accept(Compat.literal("  +" + (groups.size() - limit) + " more...").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
            }
        } else {
            out.accept(Compat.translatable("item.wands.blueprint.empty").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static CompoundTag getBlueprintTag(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlueprintItem)) {
            return new CompoundTag();
        }
        CompoundTag root = Compat.getTags(stack);
        return Compat.getCompound(root, TAG_BLUEPRINT).orElseGet(CompoundTag::new);
    }

    public static void saveBlueprintTag(ItemStack stack, CompoundTag bpTag) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlueprintItem)) {
            return;
        }
        CompoundTag root = Compat.getTags(stack);
        if (!Compat.getString(bpTag, TAG_ID).isPresent() || Compat.getString(bpTag, TAG_ID).get().isEmpty()) {
            bpTag.putString(TAG_ID, UUID.randomUUID().toString());
        }
        root.put(TAG_BLUEPRINT, bpTag);
        Compat.saveCustomData(stack, root);
    }

    public static String getOrCreateId(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlueprintItem)) {
            return "";
        }
        CompoundTag bpTag = getBlueprintTag(stack);
        String id = Compat.getString(bpTag, TAG_ID).orElse("");
        if (id.isEmpty()) {
            id = UUID.randomUUID().toString();
            bpTag.putString(TAG_ID, id);
            saveBlueprintTag(stack, bpTag);
        }
        return id;
    }

    public static String getId(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlueprintItem)) {
            return "";
        }
        CompoundTag bpTag = getBlueprintTag(stack);
        return Compat.getString(bpTag, TAG_ID).orElse("");
    }

    public static String getBlueprintName(ItemStack stack) {
        CompoundTag bpTag = getBlueprintTag(stack);
        return Compat.getString(bpTag, TAG_NAME).orElse("");
    }

    public static void setBlueprintName(ItemStack stack, String name) {
        CompoundTag bpTag = getBlueprintTag(stack);
        bpTag.putString(TAG_NAME, name != null ? name.trim() : "");
        saveBlueprintTag(stack, bpTag);
    }

    public static String getDisplayLabel(ItemStack stack) {
        String name = getBlueprintName(stack);
        if (!name.isEmpty()) {
            return name;
        }
        String hover = stack.getHoverName().getString();
        int count = getTotalBlockCount(stack);
        if (count > 0) {
            int[] sz = getDimensions(stack);
            return hover + " (" + count + " blks, " + sz[0] + "x" + sz[1] + "x" + sz[2] + ")";
        }
        return hover + " (Empty)";
    }

    public static boolean isEmptyBlueprint(ItemStack stack) {
        return getTotalBlockCount(stack) == 0;
    }

    public static int getTotalBlockCount(ItemStack stack) {
        CompoundTag bpTag = getBlueprintTag(stack);
        int[] blocks = Compat.getIntArray(bpTag, TAG_BLOCKS).orElse(new int[0]);
        return blocks.length / 2;
    }

    public static int[] getDimensions(ItemStack stack) {
        CompoundTag bpTag = getBlueprintTag(stack);
        int[] size = Compat.getIntArray(bpTag, TAG_SIZE).orElse(new int[0]);
        if (size.length == 3) {
            return size;
        }
        return new int[]{0, 0, 0};
    }

    public static int packPos(int x, int y, int z) {
        return ((x & 0x3FF) << 20) | ((y & 0x3FF) << 10) | (z & 0x3FF);
    }

    public static BlockPos unpackPos(int packed) {
        int x = (packed >> 20) & 0x3FF;
        if ((x & 0x200) != 0) x |= ~0x3FF;
        int y = (packed >> 10) & 0x3FF;
        if ((y & 0x200) != 0) y |= ~0x3FF;
        int z = packed & 0x3FF;
        if ((z & 0x200) != 0) z |= ~0x3FF;
        return new BlockPos(x, y, z);
    }

    public static void saveCopyBufferToBlueprint(ItemStack stack, List<CopyBuffer> copyBuffer) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlueprintItem)) {
            return;
        }
        CompoundTag bpTag = getBlueprintTag(stack);
        String id = Compat.getString(bpTag, TAG_ID).orElse("");
        if (id.isEmpty()) {
            id = UUID.randomUUID().toString();
            bpTag.putString(TAG_ID, id);
        }

        // Sort blocks by Y ascending, then Z, then X for natural bottom-to-top incremental placement
        List<CopyBuffer> sorted = new ArrayList<>(copyBuffer);
        sorted.sort(Comparator.comparingInt((CopyBuffer b) -> b.pos.getY())
                .thenComparingInt(b -> b.pos.getZ())
                .thenComparingInt(b -> b.pos.getX()));

        List<BlockState> palette = new ArrayList<>();
        Map<BlockState, Integer> paletteIndex = new LinkedHashMap<>();
        int[] packedData = new int[sorted.size() * 2];

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        int idx = 0;
        for (CopyBuffer cb : sorted) {
            if (cb.state == null || cb.state.isAir()) continue;
            Integer stateIdx = paletteIndex.get(cb.state);
            if (stateIdx == null) {
                stateIdx = palette.size();
                palette.add(cb.state);
                paletteIndex.put(cb.state, stateIdx);
            }
            int x = cb.pos.getX();
            int y = cb.pos.getY();
            int z = cb.pos.getZ();
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;

            packedData[idx++] = packPos(x, y, z);
            packedData[idx++] = stateIdx;
        }

        if (idx < packedData.length) {
            packedData = Arrays.copyOf(packedData, idx);
        }

        ListTag statesList = new ListTag();
        for (BlockState st : palette) {
            statesList.add(Compat.writeBlockState(st));
        }

        int sizeX = idx > 0 ? (maxX - minX + 1) : 0;
        int sizeY = idx > 0 ? (maxY - minY + 1) : 0;
        int sizeZ = idx > 0 ? (maxZ - minZ + 1) : 0;

        bpTag.put(TAG_STATES, statesList);
        bpTag.putIntArray(TAG_BLOCKS, packedData);
        bpTag.putIntArray(TAG_SIZE, new int[]{sizeX, sizeY, sizeZ});
        // Keep existing name and compatible replacements, or prune replacements not in new blocks
        Map<Block, Block> existingRepl = getReplacements(stack);
        Set<Block> presentBlocks = new HashSet<>();
        for (BlockState st : palette) {
            presentBlocks.add(st.getBlock());
        }
        ListTag replList = new ListTag();
        for (Map.Entry<Block, Block> e : existingRepl.entrySet()) {
            if (presentBlocks.contains(e.getKey()) && e.getValue() != null && e.getValue() != Blocks.AIR) {
                CompoundTag rt = new CompoundTag();
                rt.putString("Orig", Compat.getBlockId(e.getKey()));
                rt.putString("Repl", Compat.getBlockId(e.getValue()));
                replList.add(rt);
            }
        }
        bpTag.put(TAG_REPLACEMENTS, replList);
        saveBlueprintTag(stack, bpTag);
    }

    public static Map<Block, Block> getReplacements(ItemStack stack) {
        Map<Block, Block> map = new LinkedHashMap<>();
        CompoundTag bpTag = getBlueprintTag(stack);
        Optional<ListTag> replOpt = Compat.getList(bpTag, TAG_REPLACEMENTS);
        if (replOpt.isPresent()) {
            ListTag replList = replOpt.get();
            for (int i = 0; i < replList.size(); i++) {
                Optional<CompoundTag> entryOpt = Compat.getListCompound(replList, i);
                if (entryOpt.isPresent()) {
                    CompoundTag entry = entryOpt.get();
                    String origStr = Compat.getString(entry, "Orig").orElse("");
                    String replStr = Compat.getString(entry, "Repl").orElse("");
                    Block origBlock = Compat.getBlockById(origStr);
                    Block replBlock = Compat.getBlockById(replStr);
                    if (origBlock != Blocks.AIR && replBlock != Blocks.AIR) {
                        map.put(origBlock, replBlock);
                    }
                }
            }
        }
        return map;
    }

    public static void setReplacement(ItemStack stack, Block origBlock, Block replBlock) {
        if (origBlock == null || origBlock == Blocks.AIR) return;
        Map<Block, Block> map = getReplacements(stack);
        if (replBlock == null || replBlock == Blocks.AIR || replBlock == origBlock) {
            map.remove(origBlock);
        } else {
            map.put(origBlock, replBlock);
        }
        CompoundTag bpTag = getBlueprintTag(stack);
        ListTag replList = new ListTag();
        for (Map.Entry<Block, Block> e : map.entrySet()) {
            CompoundTag rt = new CompoundTag();
            rt.putString("Orig", Compat.getBlockId(e.getKey()));
            rt.putString("Repl", Compat.getBlockId(e.getValue()));
            replList.add(rt);
        }
        bpTag.put(TAG_REPLACEMENTS, replList);
        saveBlueprintTag(stack, bpTag);
    }

    public static List<BlockGroup> getBlockGroups(ItemStack stack) {
        CompoundTag bpTag = getBlueprintTag(stack);
        Optional<ListTag> statesOpt = Compat.getList(bpTag, TAG_STATES);
        int[] blocks = Compat.getIntArray(bpTag, TAG_BLOCKS).orElse(new int[0]);
        if (!statesOpt.isPresent() || blocks.length < 2) {
            return Collections.emptyList();
        }
        ListTag statesList = statesOpt.get();
        List<BlockState> palette = new ArrayList<>(statesList.size());
        for (int i = 0; i < statesList.size(); i++) {
            CompoundTag stTag = Compat.getListCompound(statesList, i).orElseGet(CompoundTag::new);
            palette.add(Compat.readBlockState(stTag));
        }

        Map<Block, Integer> counts = new LinkedHashMap<>();
        for (int i = 0; i + 1 < blocks.length; i += 2) {
            int stateIdx = blocks[i + 1];
            if (stateIdx >= 0 && stateIdx < palette.size()) {
                Block b = palette.get(stateIdx).getBlock();
                if (b != Blocks.AIR) {
                    counts.put(b, counts.getOrDefault(b, 0) + 1);
                }
            }
        }

        Map<Block, Block> replacements = getReplacements(stack);
        List<BlockGroup> groups = new ArrayList<>(counts.size());
        for (Map.Entry<Block, Integer> e : counts.entrySet()) {
            groups.add(new BlockGroup(e.getKey(), e.getValue(), replacements.getOrDefault(e.getKey(), Blocks.AIR)));
        }
        // Sort by count descending
        groups.sort((a, b) -> Integer.compare(b.count, a.count));
        return groups;
    }

    public static List<CopyBuffer> getCopyBuffer(ItemStack stack) {
        CompoundTag bpTag = getBlueprintTag(stack);
        Optional<ListTag> statesOpt = Compat.getList(bpTag, TAG_STATES);
        int[] blocks = Compat.getIntArray(bpTag, TAG_BLOCKS).orElse(new int[0]);
        if (!statesOpt.isPresent() || blocks.length < 2) {
            return Collections.emptyList();
        }
        ListTag statesList = statesOpt.get();
        Map<Block, Block> replacements = getReplacements(stack);

        List<BlockState> resolvedPalette = new ArrayList<>(statesList.size());
        for (int i = 0; i < statesList.size(); i++) {
            CompoundTag stTag = Compat.getListCompound(statesList, i).orElseGet(CompoundTag::new);
            BlockState origState = Compat.readBlockState(stTag);
            Block replBlock = replacements.get(origState.getBlock());
            if (replBlock != null && replBlock != Blocks.AIR && replBlock != origState.getBlock()) {
                resolvedPalette.add(replBlock.withPropertiesOf(origState));
            } else {
                resolvedPalette.add(origState);
            }
        }

        List<CopyBuffer> result = new ArrayList<>(blocks.length / 2);
        for (int i = 0; i + 1 < blocks.length; i += 2) {
            BlockPos relPos = unpackPos(blocks[i]);
            int stateIdx = blocks[i + 1];
            if (stateIdx >= 0 && stateIdx < resolvedPalette.size()) {
                BlockState st = resolvedPalette.get(stateIdx);
                if (!st.isAir()) {
                    result.add(new CopyBuffer(relPos, st));
                }
            }
        }
        return result;
    }
}
