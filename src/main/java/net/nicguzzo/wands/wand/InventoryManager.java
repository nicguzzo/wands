package net.nicguzzo.wands.wand;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluids;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.items.PaletteItem;
import net.nicguzzo.wands.utils.WandUtils;
import net.nicguzzo.wands.wand.WandProps.Mode;

import java.util.*;

public class InventoryManager {

    private Inventory player_inv;
    private int[] inv_aux = new int[36];
    private int inv_aux_last = 0;
    private int blocks_sent_to_inv = 0;
    public int n_water_buckets_in_inventory = 0;
    public InventoryManager(Inventory playerInv) {
        this.player_inv = playerInv;
    }

    public ItemStack getItem(int slot) {
        return player_inv.getItem(slot);
    }

    public int getBlocksSentToInv() {
        return blocks_sent_to_inv;
    }

    public void resetBlocksSentToInv() {
        blocks_sent_to_inv = 0;
    }

    public void update_inv_aux() {
        inv_aux_last = 0;
        for (int s = 0; s < 36; s++) {
            ItemStack stack = player_inv.getItem(s);
            if (WandUtils.is_shulker(stack) || WandUtils.is_magicbag(stack)) {
                inv_aux[inv_aux_last++] = s;
            }
        }
    }

    ItemStack consume_item(BlockAccounting pa, ItemStack stack_item) {
        if (pa != null && pa.placed > 0) {
            if (WandUtils.is_magicbag(stack_item)) {
                int total = MagicBagItem.getTotal(stack_item);
                if (pa.placed <= total) {
                    MagicBagItem.dec(stack_item, pa.placed);
                    pa.placed = 0;
                } else {
                    MagicBagItem.dec(stack_item, total);
                    pa.placed -= total;
                }
            } else {
                Item item = stack_item.getItem();
                if (item == Fluids.LAVA.getBucket() || item == Items.POWDER_SNOW_BUCKET) {
                    pa.placed--;
                    return Items.BUCKET.getDefaultInstance();
                } else {
                    if (pa.placed <= stack_item.getCount()) {
                        stack_item.setCount(stack_item.getCount() - pa.placed);
                        pa.placed = 0;
                    } else {
                        pa.placed -= stack_item.getCount();
                        stack_item.setCount(0);
                    }
                }
            }
            return ItemStack.EMPTY;
        }
        return null;
    }

    boolean place_into(Wand wand, ItemStack item_to_place) {
        ItemStack oh = wand.player.getOffhandItem();
        if (!oh.isEmpty()) {
            if (WandUtils.is_shulker(oh)) {
                return place_into_shulker(wand, oh, item_to_place, false);
            } else if (WandUtils.is_magicbag(oh)) {
                return place_into_bag(wand, oh, item_to_place);
            }
        }
        //look for bags and shulkers
        for (int pi = 0; pi < inv_aux_last; ++pi) {
            ItemStack stack = player_inv.getItem(inv_aux[pi]);
            if (WandUtils.is_shulker(stack)) {
                if (place_into_shulker(wand, stack, item_to_place, true)) {
                    return true;
                }
            }
            if (WandUtils.is_magicbag(stack)) {
                if (place_into_bag(wand, stack, item_to_place)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean canCollectDrops(Wand wand, ItemStack offhand, List<ItemStack> drops) {
        if (!wand.stop_on_full_inventory || !WandUtils.is_shulker(offhand)) return true;

        // Try the entire loot roll against a copy before removing the block.
        ItemStack trialShulker = offhand.copy();
        InventoryManager trial = new InventoryManager(player_inv);
        for (ItemStack stack : drops) {
            if (!trial.place_into_shulker(wand, trialShulker, stack.copy(), false)) return false;
        }
        return true;
    }

    boolean place_into_bag(Wand wand, ItemStack bag, ItemStack item_to_place) {
        ItemStack mb_item = MagicBagItem.getItem(bag, wand.level);
        int total = MagicBagItem.getTotal(bag);
        if (mb_item.isEmpty() && total == 0) {
            MagicBagItem.setItem(bag, item_to_place, wand.level);
            mb_item = item_to_place;
        }
        if (Compat.is_same_with_components(mb_item, item_to_place)) {
            if (MagicBagItem.inc(bag, item_to_place.getCount())) {
                blocks_sent_to_inv += item_to_place.getCount();
                return true;
            }
        }
        return false;
    }

    boolean place_into_shulker(Wand wand, ItemStack shulker, ItemStack item_to_place, boolean bag_only) {
        List<ItemStack> contents = Compat.get_shulker_contents(shulker);
        boolean modified = false;
        //look for bags with same item first
        for (ItemStack slot_item : contents) {
            if (WandUtils.is_magicbag(slot_item)) {
                ItemStack bag_with_same_item = MagicBagItem.getItem(slot_item, wand.level);
                if (Compat.is_same_with_components(bag_with_same_item, item_to_place)) {
                    if (place_into_bag(wand, slot_item, item_to_place)) {
                        Compat.set_shulker_contents(shulker, contents);
                        return true;
                    }
                    break;
                }
            }
        }
        if (!bag_only) {
            int item_count = item_to_place.getCount();
            //look for stack that already has the item
            for (ItemStack slot_item : contents) {
                if (slot_item != null) {
                    if (Compat.is_same_with_components(item_to_place, slot_item)) {
                        int total = item_count + slot_item.getCount();
                        if (total <= slot_item.getMaxStackSize()) {
                            slot_item.setCount(total);
                            blocks_sent_to_inv += item_count;
                            Compat.set_shulker_contents(shulker, contents);
                            return true;
                        } else {
                            //slot full
                            int added = slot_item.getMaxStackSize() - slot_item.getCount();
                            blocks_sent_to_inv += added;
                            slot_item.setCount(slot_item.getMaxStackSize());
                            item_count = total - slot_item.getMaxStackSize();
                            item_to_place.setCount(item_count);
                            modified = true;
                        }
                    } else {
                        if (WandUtils.is_magicbag(slot_item)) {
                            if (place_into_bag(wand, slot_item, item_to_place)) {
                                Compat.set_shulker_contents(shulker, contents);
                                return true;
                            }
                        }
                    }
                }
            }
            item_to_place.setCount(item_count);
            if (item_count > 0) {
                int emptySlot = contents.indexOf(ItemStack.EMPTY);
                if (emptySlot >= 0) {
                    contents.set(emptySlot, item_to_place);
                    blocks_sent_to_inv += item_count;
                    Compat.set_shulker_contents(shulker, contents);
                    return true;
                }
                //if we get here the shulker is full
                if (wand.stop_on_full_inventory) {
                    wand.stop = true;
                }
            }
            if (modified) {
                Compat.set_shulker_contents(shulker, contents);
            }
        }
        return false;
    }
    void detect_water_bucket(Wand wand){
        n_water_buckets_in_inventory = 0;
        if ( wand.has_water_bucket && (!wand.creative || wand.mode == Mode.BLAST) && !wand.destroy && !wand.use && wand.mode != Mode.COPY) {
            ItemStack stack;
            for (int i = 0; i < 36; ++i) {
                stack = player_inv.getItem(i);
                if (stack.getItem() == Items.WATER_BUCKET) {
                    n_water_buckets_in_inventory++;
                    if (n_water_buckets_in_inventory>=2) {
                        break;
                    }
                }
            }
        }
    }
    // Counting and consuming walk the same slots, starting with the offhand.
    void accountMaterials(Wand wand, boolean consume) {
        visitStorage(wand, wand.player.getOffhandItem(), consume,
                replacement -> wand.player.setItemInHand(InteractionHand.OFF_HAND, replacement));
        for (int i = 0; i < 36; i++) {
            int slot = i;
            visitStorage(wand, player_inv.getItem(slot), consume,
                    replacement -> player_inv.setItem(slot, replacement));
        }
    }

    private void visitStorage(Wand wand, ItemStack stack, boolean consume,
                              java.util.function.Consumer<ItemStack> replace) {
        if (WandUtils.is_shulker(stack)) {
            List<ItemStack> slots = Compat.get_shulker_contents(stack);
            boolean changed = false;
            for (int i = 0; i < slots.size(); i++) {
                ItemStack result = accountStack(wand, slots.get(i), consume);
                if (result != null) {
                    slots.set(i, result);
                    changed = true;
                }
            }
            if (changed) Compat.set_shulker_contents(stack, slots);
        } else {
            ItemStack result = accountStack(wand, stack, consume);
            if (result != null && result != stack) replace.accept(result);
        }
    }

    private ItemStack accountStack(Wand wand, ItemStack stack, boolean consume) {
        if (stack.isEmpty()) return null;
        boolean bag = WandUtils.is_magicbag(stack);
        if (!bag && !Compat.has_no_custom_data(stack)) return null;
        Item item = bag ? MagicBagItem.getItem(stack, wand.level).getItem() : stack.getItem();
        BlockAccounting count = wand.block_accounting.get(item);
        if (count == null) return null;
        if (!consume) {
            count.in_player += bag ? MagicBagItem.getTotal(stack) : stack.getCount();
            return null;
        }
        ItemStack replacement = consume_item(count, stack);
        if (replacement == null) return null;
        return replacement.isEmpty() ? stack : replacement;
    }

    private record PaletteLocation(int inventorySlot, int shulkerSlot) {}

    private static List<PaletteLocation> findPalettes(Inventory inv) {
        List<PaletteLocation> result = new ArrayList<>();
        for (int s = 0; s < 36; s++) {
            ItemStack stack = inv.getItem(s);
            if (WandUtils.is_shulker(stack)) {
                List<ItemStack> slots = Compat.get_shulker_contents(stack);
                for (int i = 0; i < slots.size(); i++) {
                    if (slots.get(i).getItem() instanceof PaletteItem) result.add(new PaletteLocation(s, i));
                }
            }
        }
        for (int s = 0; s < 36; s++) {
            if (inv.getItem(s).getItem() instanceof PaletteItem) result.add(new PaletteLocation(s, -1));
        }
        return result;
    }

    /** Main cycle method — called from process_keys.
     *  Stateless atomic swap: fresh-scan palettes each press, take next palette,
     *  put current offhand into the vacated slot. No stale state between presses. */
    public static void cyclePalette(net.minecraft.world.entity.player.Player player, Wand wand) {
        Inventory inv = player.getInventory();
        ItemStack offhand = player.getOffhandItem();

        List<PaletteLocation> palettes = findPalettes(inv);
        if (palettes.isEmpty()) {
            wand.paletteCycleActive = false;
            return;
        }

        int nextIdx = wand.paletteCycleActive
            ? (wand.paletteCycleIndex + 1) % palettes.size()
            : 0;

        PaletteLocation nextLoc = palettes.get(nextIdx);
        ItemStack source = inv.getItem(nextLoc.inventorySlot());
        ItemStack nextPalette;
        if (nextLoc.shulkerSlot() >= 0) {
            if (!offhand.isEmpty() && !offhand.getItem().canFitInsideContainerItems()) return;
            List<ItemStack> slots = Compat.get_shulker_contents(source);
            nextPalette = slots.set(nextLoc.shulkerSlot(), offhand);
            Compat.set_shulker_contents(source, slots);
        } else {
            nextPalette = source;
            inv.setItem(nextLoc.inventorySlot(), offhand);
        }
        player.setItemInHand(InteractionHand.OFF_HAND, nextPalette);
        wand.paletteCycleActive = true;
        wand.paletteCycleIndex = nextIdx;
    }

    void remove_from_inventory(Wand wand, int placed) {
        if (!wand.creative && ((!wand.destroy && placed > 0) || wand.mode == Mode.BLAST)) {
            accountMaterials(wand, true);
        }
    }
}
