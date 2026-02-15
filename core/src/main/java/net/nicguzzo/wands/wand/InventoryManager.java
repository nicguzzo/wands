package net.nicguzzo.wands.wand;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluids;
import net.nicguzzo.compat.Compat;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.utils.WandUtils;
import net.nicguzzo.wands.wand.WandProps.Mode;

import java.util.*;

public class InventoryManager {

    private Inventory player_inv;
    private int[] inv_aux = new int[36];
    private int inv_aux_last = 0;
    private int blocks_sent_to_inv = 0;

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

    boolean place_into_bag(Wand wand, ItemStack bag, ItemStack item_to_place) {
        ItemStack mb_item = MagicBagItem.getItem(bag, wand.level);
        int total = MagicBagItem.getTotal(bag);
        if (mb_item.isEmpty() && total == 0) {
            MagicBagItem.setItem(bag, item_to_place, wand.level);
            mb_item = item_to_place;
        }
        if (mb_item.getItem() == item_to_place.getItem()) {
            if (MagicBagItem.inc(bag, item_to_place.getCount())) {
                blocks_sent_to_inv += item_to_place.getCount();
                return true;
            }
        }
        return false;
    }

    boolean place_into_shulker(Wand wand, ItemStack shulker, ItemStack item_to_place, boolean bag_only) {
        List<ItemStack> contents = Compat.get_shulker_contents(shulker);
        if (contents.isEmpty() && bag_only) {
            return false;
        }
        boolean modified = false;
        //look for bags with same item first
        for (ItemStack slot_item : contents) {
            if (WandUtils.is_magicbag(slot_item)) {
                ItemStack bag_with_same_item = MagicBagItem.getItem(slot_item, wand.level);
                if (bag_with_same_item.getItem() == item_to_place.getItem()) {
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
                    if (Compat.is_same(item_to_place, slot_item)) {
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
                if (contents.size() < 27) {
                    contents.add(item_to_place);
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

    void check_inventory(Wand wand) {
        if ((!wand.creative || wand.mode == Mode.BLAST) && !wand.destroy && !wand.use && !wand.has_water_bucket && wand.mode != Mode.COPY) {
            ItemStack stack;
            //now the player inventory
            for (int i = 0; i < 36; ++i) {
                stack = player_inv.getItem(i);
                if (stack.getItem() != Items.AIR) {
                    if (WandUtils.is_shulker(stack)) {
                        for (Map.Entry<Item, BlockAccounting> pa : wand.block_accounting.entrySet()) {
                            pa.getValue().in_player += WandUtils.count_in_shulker(stack, pa.getKey(), wand.level);
                        }
                    } else if (WandUtils.is_magicbag(stack)) {
                        int total = MagicBagItem.getTotal(stack);
                        ItemStack stack2 = MagicBagItem.getItem(stack, wand.level);
                        if (!stack2.isEmpty() && total > 0) {
                            BlockAccounting ba = wand.block_accounting.get(stack2.getItem());
                            if (ba != null) {
                                ba.in_player += total;
                            }
                        }
                    } else {
                        if (Compat.has_no_custom_data(stack)) {
                            for (Map.Entry<Item, BlockAccounting> pa : wand.block_accounting.entrySet()) {
                                Item item = pa.getKey();
                                if (item != null && !stack.isEmpty() && item == stack.getItem()) {
                                    pa.getValue().in_player += stack.getCount();
                                }
                            }
                        }
                    }
                }
            }
            ItemStack oh = wand.player.getOffhandItem();
            if (oh != null && !oh.isEmpty()) {
                if (oh.getItem() instanceof MagicBagItem) {
                    stack = MagicBagItem.getItem(oh, wand.level);
                    int total = MagicBagItem.getTotal(oh);
                    if (!stack.isEmpty() && total > 0) {
                        BlockAccounting ba = wand.block_accounting.get(stack.getItem());
                        if (ba != null) {
                            ba.in_player += total;
                        }
                    }
                } else {
                    if (wand.offhand != null) {
                        BlockAccounting ba = wand.block_accounting.get(wand.offhand.getItem());
                        if (ba != null) {
                            ba.in_player += wand.offhand.getCount();
                        }
                    }
                }
            }
            for (Map.Entry<Item, BlockAccounting> pa : wand.block_accounting.entrySet()) {
                if (pa.getValue().in_player < pa.getValue().needed) {
                    MutableComponent name = Compat.translatable(pa.getKey().getDescriptionId());
                    MutableComponent mc = Compat.literal("Not enough ").withStyle(ChatFormatting.RED).append(name);
                    mc.append(". " + pa.getValue().in_player);
                    mc.append("/");
                    mc.append("" + pa.getValue().needed);
                    wand.player.displayClientMessage(mc, false);
                }
            }
        }
    }

    void consume_from_shulker(Wand wand, ItemStack shulker) {
        List<ItemStack> contents = Compat.get_shulker_contents(shulker);
        boolean modified = false;
        for (ItemStack stack_item : contents) {
            if (!stack_item.isEmpty()) {
                if (WandUtils.is_magicbag(stack_item)) {
                    ItemStack bag_it = MagicBagItem.getItem(stack_item, wand.level);
                    BlockAccounting pa = wand.block_accounting.get(bag_it.getItem());
                    consume_item(pa, stack_item);
                    modified = true;
                } else {
                    if (Compat.has_no_custom_data(stack_item)) {
                        BlockAccounting pa = wand.block_accounting.get(stack_item.getItem());
                        consume_item(pa, stack_item);
                        modified = true;
                    }
                }
            }
        }
        if (modified) {
            Compat.set_shulker_contents(shulker, contents);
        }
    }

    void remove_from_inventory(Wand wand, int placed) {
        if (!wand.creative && ((!wand.destroy && placed > 0) || wand.mode == Mode.BLAST)) {
            ItemStack stack;
            ItemStack stack_item;
            //look for items on shulker boxes first
            for (int pi = 0; pi < 36; ++pi) {
                stack = player_inv.getItem(pi);
                if (stack.getItem() != Items.AIR) {
                    if (WandUtils.is_shulker(stack)) {
                        consume_from_shulker(wand, stack);
                    }
                }
            }

            ItemStack oh = wand.player.getOffhandItem();
            if (!oh.isEmpty() && oh.getItem() instanceof MagicBagItem) {
                stack_item = MagicBagItem.getItem(oh, wand.level);
                BlockAccounting pa = wand.block_accounting.get(stack_item.getItem());
                consume_item(pa, oh);
            }
            //now look for bags on player inv
            for (int i = 0; i < 36; ++i) {
                stack_item = player_inv.getItem(i);
                if (WandUtils.is_magicbag(stack_item)) {
                    ItemStack bag_it = MagicBagItem.getItem(stack_item, wand.level);
                    BlockAccounting pa = wand.block_accounting.get(bag_it.getItem());
                    consume_item(pa, stack_item);
                }
            }
            //now look for items on player inv
            for (int i = 0; i < 36; ++i) {
                stack_item = player_inv.getItem(i);
                if (stack_item.getItem() != Items.AIR) {
                    if (!WandUtils.is_shulker(stack_item) && !WandUtils.is_magicbag(stack_item)
                            && Compat.has_no_custom_data(stack_item)
                    ) {
                        BlockAccounting pa = wand.block_accounting.get(stack_item.getItem());
                        ItemStack rep = consume_item(pa, stack_item);
                        if (rep != null && !rep.isEmpty()) {
                            player_inv.setItem(i, rep);
                        }
                    }
                }
            }
            if (wand.offhand != null && !wand.offhand.isEmpty() && !WandUtils.is_magicbag(wand.offhand)) {
                BlockAccounting pa = wand.block_accounting.get(wand.offhand.getItem());
                if (pa != null) {
                    ItemStack rep = consume_item(pa, wand.offhand);
                    if (rep != null && !rep.isEmpty()) {
                        wand.player.setItemInHand(InteractionHand.OFF_HAND, rep);
                    }
                }
            }
        }
    }
}
