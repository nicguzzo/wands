package net.nicguzzo.wands.wand;

import dev.architectury.networking.NetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.WandsExpectPlatform;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.items.PaletteItem;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.mixin.DropExperienceBlockAccessor;
import net.nicguzzo.wands.networking.Networking;
import net.nicguzzo.wands.utils.BlockBuffer;
import net.nicguzzo.wands.utils.CircularBuffer;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.utils.WandUtils;
import net.nicguzzo.wands.wand.WandProps.Mode;

import java.util.*;
//import com.github.clevernucleus.opc.api.OfflinePlayerCacheAPI;
//import com.github.clevernucleus.opc.api.claim.Claim;

//TODO implement https://github.com/Patbox/common-protection-api for claimed chunks
//TODO fix mirroring and rotation
//TODO augmentation items, durability, range, damage?, planting?
//TODO support other mods tools
//TODO infinite (cretive) wand recipe, optional
//TODO drop items on wand merge craft
//TODO fix plants place, force samestate, needs air bug
//TODO fix tunnel mode only on netherite, should be iron+
//TODO stone wand no blast
//TODO support tags in allow/deny list
//TODO palette pattern mode
//TODO banner placement not working on sides
//DONE fix leaves default state
//DONE bug, copy undoes last destroy
//DONE place bamboo
//DONE walls/fences state
//DONE bug, mud with potion on survival
//DONE bug shear pumpkins

public class Wand {
    private static final int TOOL_DAMAGE_STOP = 5;
    public int x = 0;
    public int y = 0;
    public int z = 0;
    public int x1 = 0;
    public int y1 = 0;
    public int z1 = 0;
    public int x2 = 0;
    public int y2 = 0;
    public int z2 = 0;
    public int bb1_x = 0;
    public int bb1_y = 0;
    public int bb1_z = 0;
    public int bb2_x = 0;
    public int bb2_y = 0;
    public int bb2_z = 0;
    public int fill_nx = 0;
    public int fill_ny = 0;
    public int fill_nz = 0;
    private BlockPos p1 = null;
    private BlockPos p2 = null;
    //public boolean p2 = false;
    public BlockState p1_state = null;
    public HitResult lastHitResult = null;

    public boolean valid = false;

    public Player player;
    public Level level;
    public BlockState block_state;
    public BlockState offhand_state = null;
    Block offhand_block = null;
    public BlockPos pos;
    public Direction side = Direction.NORTH;
    public Direction lastPlayerDirection = Direction.NORTH;
    public Vec3 hit;
    public ItemStack wand_stack;

    //ItemStack[] tools=new ItemStack[9];
    ItemStack[] tools = new ItemStack[9 + 27];
    int n_tools = 0;

    ItemStack offhand;
    ItemStack digger_item;
    //public int current_digger_item=-1;
    public int digger_item_slot = 0;
    public float y0 = 0.0f;
    public float block_height = 1.0f;
    boolean is_stair = false;
    boolean is_slab_top = false;
    boolean is_slab_bottom = false;
    public boolean is_alt_pressed = false;
    public boolean is_shift_pressed = false;
    public boolean replace;
    public boolean destroy;
    public boolean use;
    boolean stop = false;
    ItemStack bucket = null;
    public boolean is_double_slab = false;
    public int grid_voxel_index = 0;


    boolean has_bucket = false;
    public boolean has_water_bucket = false;
    public boolean has_lava_bucket = false;
    public boolean has_empty_bucket = false;
    #if MC>="1190"
    boolean has_water_potion = false;
    #endif
    int send_sound = -1;
    boolean has_offhand = false;
    public boolean has_hoe = false;
    public boolean has_shovel = false;
    public boolean has_axe = false;
    public boolean has_shear = false;
    public boolean force_render = false;
    public boolean limit_reached = false;
    public WandProps.Plane plane = WandProps.Plane.XZ;
    public Direction.Axis axis = Direction.Axis.Y;
    public Rotation rotation;
    public WandProps.StateMode state_mode = WandProps.StateMode.CLONE;
    private boolean no_tool;
    private boolean damaged_tool;
    public boolean match_state = false;
    //public boolean even_circle=false;
    public boolean mine_to_inventory = true;
    public boolean stop_on_full_inventory = true;
    public boolean target_air = false;
    public int target_air_distance = 0;
    public boolean unbreakable = false;
    public boolean removes_water = false;
    public boolean removes_lava = false;
    public boolean can_blast = false;


    public BlockPos getP1() {
        return p1;
    }

    public void setP1(BlockPos p1) {
        this.p1 = p1;
        //WandsMod.LOGGER.info("set p1 "+p1);
    }

    public BlockPos getP2() {
        return p2;
    }

    public void setP2(BlockPos p2) {
        this.p2 = p2;
        //WandsMod.LOGGER.info("set p2 "+p2);
    }

    public enum Sounds {
        SPLASH {
            @Override
            public SoundEvent get_sound() {
                return SoundEvents.GENERIC_SPLASH;
            }
        };

        public abstract SoundEvent get_sound();
    }

    public RandomSource random = RandomSource.create();
    public Palette palette = new Palette();
    public Map<Item, BlockAccounting> block_accounting = new HashMap<>();
    public BlockBuffer block_buffer = new BlockBuffer(WandsConfig.max_limit);
    public CircularBuffer undo_buffer = new CircularBuffer(WandsConfig.max_limit);
    public Vector<CopyBuffer> copy_paste_buffer = new Vector<>();

    private final BlockPos.MutableBlockPos tmp_pos = new BlockPos.MutableBlockPos();
    private int blocks_sent_to_inv = 0;
    public int MAX_COPY_VOL = WandsConfig.max_limit;
    public int radius = 0;

    public boolean preview;
    public boolean creative = true;
    public WandProps.Mode mode = null;
    public boolean prnt = false;
    public int limit = WandsConfig.max_limit;
    Inventory player_inv;
    public boolean slab_stair_bottom = true;
    static boolean once = true;

    WandMode[] modes = null;

    int[] inv_aux = new int[36];
    int inv_aux_last = 0;
    public boolean drop_on_player = true;

    public CompoundTag player_data;

    public Wand() {
        modes = new WandMode[WandProps.modes.length];
        for (int i = 0; i < modes.length; i++) {
            modes[i] = WandProps.modes[i].get_mode();
        }
    }
    public WandMode get_mode(){
        if(modes!=null && mode!=null)
            return modes[mode.ordinal()];
        else{
            return null;
        }
    }

    public void clear() {
        setP1(null);
        setP2(null);
        p1_state = null;
        valid = false;
        block_height = 1.0f;
        y0 = 0.0f;
        fill_nx = 0;
        fill_ny = 0;
        fill_nz = 0;
        Palette.version++;
        //WandsMod.LOGGER.info("clear");
        /*if(player!=null)
            player.displayClientMessage(Compat.literal("wand cleared"),false);*/
    }

    public void do_or_preview(Player player, Level level, BlockState block_state, BlockPos pos, Direction side,
                              Vec3 hit, ItemStack wand_stack, WandItem wand_item,
                              boolean prnt) {
        if (wand_stack == null || wand_item == null)
            return;
        this.limit = wand_item.limit;
        if (limit > WandsConfig.max_limit) {
            this.limit = WandsConfig.max_limit;
        }
        this.unbreakable = wand_item.unbreakable;
        this.removes_water = wand_item.removes_water;
        this.removes_lava = wand_item.removes_lava;
        this.can_blast = wand_item.can_blast;
        this.replace = WandProps.getAction(wand_stack) == WandProps.Action.REPLACE;
        this.destroy = WandProps.getAction(wand_stack) == WandProps.Action.DESTROY;
        this.use = WandProps.getAction(wand_stack) == WandProps.Action.USE;
        this.target_air = WandProps.getFlag(wand_stack, WandProps.Flag.TARGET_AIR);
        if ((destroy || replace) && WandsMod.config.disable_destroy_replace) {
            destroy = false;
            replace = false;
            WandProps.setAction(wand_stack, WandProps.Action.PLACE);

        }
        this.player = player;
        this.level = level;
        this.block_state = block_state;
        this.pos = pos;
        this.side = side;
        this.hit = hit;
        this.wand_stack = wand_stack;
        this.prnt = prnt;
        if (this.player == null || this.level == null || this.pos == null
                || this.side == null || this.hit == null || this.wand_stack == null) {
            return;
        }
        creative = Compat.is_creative(player);
        check_advancements();
        mode = WandProps.getMode(wand_stack);
        axis = WandProps.getAxis(wand_stack);
        plane = WandProps.getPlane(wand_stack);
        rotation = WandProps.getRotation(wand_stack);
        state_mode = WandProps.getStateMode(wand_stack);
        slab_stair_bottom = WandProps.getFlag(wand_stack, WandProps.Flag.STAIRSLAB);
        match_state = WandProps.getFlag(wand_stack, WandProps.Flag.MATCHSTATE);
        player_inv = Compat.get_inventory(player);
        y0 = 0.0f;
        block_height = 1.0f;
        is_slab_top = false;
        is_double_slab = false;
        is_slab_bottom = false;
        is_stair = false;
        preview = level.isClientSide();
        offhand_state = null;
        stop = false;
        radius = 0;
        limit_reached = false;
        send_sound = -1;
        random.setSeed(palette.seed);
        palette.random.setSeed(palette.seed);
        target_air_distance = WandProps.getVal(wand_stack, WandProps.Value.AIR_TARGET_DISTANCE);

        if (block_state == null || pos == null || side == null || level == null || player == null || hit == null || wand_stack == null) {
            return;
        }

        if (once) {
            once = false;
            WandsMod.config.generate_lists();
        }
        boolean is_copy_paste = mode == Mode.COPY || mode == Mode.PASTE;

        valid = false;


        offhand = player.getOffhandItem();
        has_offhand = false;
        has_hoe = false;
        has_shovel = false;
        has_axe = false;
        update_tools();
        if (offhand != null && WandUtils.is_shulker(offhand)) {
            offhand = null;
        }
        palette.item = null;
        palette.has_palette = false;
        has_bucket = false;
        has_water_bucket = false;
        has_lava_bucket = false;
        has_empty_bucket = false;
        if (offhand != null && offhand.getItem() instanceof PaletteItem) {
            palette.item = offhand;
            palette.has_palette = true;
        }
        if (offhand != null && offhand.getItem() instanceof BucketItem) {
            if (mode != Mode.DIRECTION) {
                bucket = offhand;
                has_bucket = true;
                has_empty_bucket = bucket.isStackable();
                //Item itt=Fluids.EMPTY.getBucket();
                //BucketItem
                //has_empty_bucket=bucket.getItem().equals(Fluids.EMPTY.getBucket());
                //TODO: other mods fluids?
                if (!has_empty_bucket) {
                    has_water_bucket = bucket.getItem().equals(Fluids.WATER.getBucket());
                    if (!has_water_bucket) {
                        has_lava_bucket = bucket.getItem().equals(Fluids.LAVA.getBucket());
                    }
                }
                this.replace = false;
                this.destroy = false;
                this.use = false;
            }
        }
        this.has_water_potion = false;
        if (offhand != null && offhand.getItem() instanceof PotionItem) {

            if (offhand.has(DataComponents.POTION_CONTENTS)) {
                PotionContents potion_c = offhand.get(DataComponents.POTION_CONTENTS);
                if (potion_c != null) {
                    this.has_water_potion = potion_c.is(Potions.WATER);
                }
            }
            if (!creative && has_water_potion) {
                int nbuckets = 0;
                for (int i = 0; i < 36; ++i) {
                    ItemStack stack = player_inv.getItem(i);
                    boolean is_water_bucket = stack.getItem().equals(Fluids.WATER.getBucket());
                    if (stack.getItem() instanceof BucketItem && is_water_bucket) {
                        nbuckets++;
                    }
                }
                if (nbuckets < 2) {
                    has_water_potion = false;
                    if (!preview) {
                        player.displayClientMessage(Compat.literal("You need 2 water buckets in the inventory."), false);
                    }
                    return;
                }
            }
        }
        boolean has_torch=false;
        if (offhand != null) {
            offhand_block = Block.byItem(offhand.getItem());
            if (offhand_block != Blocks.AIR) {
                has_offhand = true;
            }
            has_torch=offhand_block instanceof TorchBlock;
        }
        if (offhand != null && !offhand.isEmpty() && !palette.has_palette && !has_bucket /*&& !destroy*/) {
            if (offhand.get(DataComponents.CUSTOM_DATA) != null) {
                offhand = null;
                has_offhand = false;
                offhand_block = null;
                //return;
            }
            if (offhand != null && !offhand.isStackable() && !has_water_potion) {
                //if (!preview) {
                //    player.displayClientMessage(Compat.literal("Wand offhand must be stackable! ").withStyle(ChatFormatting.RED), false);
                //}
                offhand = null;
                has_offhand = false;
                offhand_block = null;
                //return;
            }
            if(destroy && has_torch){
                offhand = null;
                has_offhand = false;
                offhand_block = null;
            }
        }
        block_accounting.clear();
        if (palette.has_palette /*&& !destroy && !is_copy_paste*/) {
            palette.update_palette(block_accounting, level);
        }

        if (!palette.has_palette && !has_bucket) {
            if (offhand_block != null && Blocks.AIR != offhand_block) {
                offhand_state = offhand_block.defaultBlockState();
            }
        }
        if (replace && (mode != Mode.PASTE) && !palette.has_palette && (Blocks.AIR == offhand_block || offhand_block == null)) {
            valid = false;
            if (!preview) {
                player.displayClientMessage(Compat.literal("you need a block or palette in the left hand"), false);
            }
            return;
        }
        /*if(has_offhand && (destroy)&& offhand_block!=null && offhand_state!=block_state){
            if (!preview) {
                Component name=Compat.translatable(offhand.getDescriptionId());
                player.displayClientMessage(Compat.literal("restricted to offand block: ").append(name), false);
            }
            return;
        }*/

        update_inv_aux();
        blocks_sent_to_inv = 0;
        //process the current mode
        int m = mode.ordinal();
        if (m >= 0 && m < modes.length && modes[m] != null) {
            modes[m].place_in_buffer(this);
        }

        //server stuff
        //WandsMod.log(" using palette seed: " + palette.seed,prnt);
        if (!preview) {
            //log(" using palette seed: " + palette_seed);
            if (limit_reached && (mode != Mode.VEIN)) {
                player.displayClientMessage(Compat.literal("wand limit reached"), false);
            }
            if (mode != Mode.BLAST) {
                if (palette.has_palette && !destroy && !use && !is_copy_paste) {
                    for (int a = 0; a < block_buffer.get_length() && a < limit && a < WandsConfig.max_limit; a++) {
                        if (!replace && !can_place(level.getBlockState(block_buffer.get(a)), block_buffer.get(a))) {
                            block_buffer.state[a] = null;
                            block_buffer.item[a] = null;
                            continue;
                        }
                        BlockState st = block_buffer.state[a];
                        if (st == null) {
                            continue;
                        }
                        Item it = block_buffer.item[a];
                        if (it == null) {
                            continue;
                        }
                        BlockAccounting pa = block_accounting.get(it);
                        if (pa == null) {
                            //log("no palette accounting found for "+ps.stack.getItem());
                            continue;
                        }
                        pa.needed++;

                        if (st.getBlock() instanceof SlabBlock) {
                            if (st.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
                                pa.needed++;
                            }
                        } else {
                            if (st.getBlock() instanceof SnowLayerBlock) {
                                int sn = st.getValue(SnowLayerBlock.LAYERS);
                                pa.needed += sn - 1;
                            }
                        }
                    }
                } else {
                    if (!is_copy_paste) {
                        if (has_bucket) {
                            has_bucket = false;
                            if (has_water_bucket || has_lava_bucket) {
                                if (creative) {
                                    has_bucket = true;
                                    if (has_water_bucket) {
                                        block_state = Blocks.WATER.defaultBlockState();
                                    }
                                    if (has_lava_bucket) {
                                        block_state = Blocks.LAVA.defaultBlockState();
                                    }
                                } else {
                                    if (has_water_bucket) {
                                        //in survival check if player has another water bucket apart from the one in the offhand
                                        for (int i = 0; i < 36; ++i) {
                                            ItemStack stack = player_inv.getItem(i);
                                            boolean is_water_bucket = stack.getItem().equals(Fluids.WATER.getBucket());
                                            if (stack.getItem() instanceof BucketItem && is_water_bucket) {
                                                has_bucket = true;
                                                has_water_bucket = true;
                                                block_state = Blocks.WATER.defaultBlockState();
                                                break;
                                            }
                                        }
                                        if (!has_bucket) {
                                            player.displayClientMessage(Compat.literal("You need another water bucket in the inventory."), false);
                                            return;
                                        }
                                    }
                                    if (has_lava_bucket) {
                                        block_state = Blocks.LAVA.defaultBlockState();
                                    }
                                }
                            }
                            if (has_empty_bucket) {
                                has_bucket = true;
                                block_state = Blocks.AIR.defaultBlockState();
                            }
                        }

                        BlockAccounting pa = new BlockAccounting();
                        for (int a = 0; a < block_buffer.get_length() && a < limit && a < WandsConfig.max_limit; a++) {
                            if (has_empty_bucket || has_water_bucket || has_lava_bucket) {
                                block_buffer.state[a] = block_state;
                                if (has_lava_bucket) {
                                    block_buffer.item[a] = bucket.getItem();
                                    pa.needed++;
                                }
                            } else {
                                if (!replace && !destroy && !use && !can_place(level.getBlockState(block_buffer.get(a)), block_buffer.get(a))) {
                                    block_buffer.state[a] = null;
                                    block_buffer.item[a] = null;
                                } else {
                                    pa.needed++;
                                }
                            }
                        }
                        if (block_buffer.get_length() > 0 && pa.needed > 0) {
                            block_accounting.put(block_buffer.item[0], pa);
                        }
                    } else {
                        //copy paste
                        for (int a = 0; a < block_buffer.get_length() && a < limit && a < WandsConfig.max_limit; a++) {
                            if (!replace && !destroy && !can_place(level.getBlockState(block_buffer.get(a)), block_buffer.get(a))) {
                                block_buffer.state[a] = null;
                                block_buffer.item[a] = null;
                            } else {
                                BlockAccounting pa = block_accounting.get(block_buffer.item[a]);
                                if (pa == null) {
                                    pa = new BlockAccounting();
                                    pa.needed++;
                                    block_accounting.put(block_buffer.item[a], pa);
                                } else {
                                    pa.needed++;
                                }
                            }
                        }
                    }
                }
            }
            //deal with inventory
            check_inventory();

            int placed = 0;

            // DO ACTIONS
            {
                AABB bb = player.getBoundingBox();
                if (mode != Mode.COPY) {
                    for (int a = 0; a < block_buffer.get_length() && a < limit && a < WandsConfig.max_limit; a++) {
                        tmp_pos.set(block_buffer.buffer_x[a], block_buffer.buffer_y[a], block_buffer.buffer_z[a]);
                        if (!destroy && !has_bucket && !use) {
                            if (bb.intersects(tmp_pos.getX(), tmp_pos.getY(), tmp_pos.getZ(), tmp_pos.getX() + 1, tmp_pos.getY() + 1, tmp_pos.getZ() + 1)) {
                                continue;
                            }
                            boolean pp = false;
                            for (Player pl : Compat.player_level(player).players()) {
                                if (pl.getBoundingBox().intersects(tmp_pos.getX(), tmp_pos.getY(), tmp_pos.getZ(), tmp_pos.getX() + 1, tmp_pos.getY() + 1, tmp_pos.getZ() + 1)) {
                                    pp = true;
                                    break;
                                }
                            }
                            if (pp) {
                                continue;
                            }
                        }
                        Item item = block_buffer.item[a];
                        BlockAccounting pa = null;
                        if (item != null) {
                            pa = block_accounting.get(item);
                        }
                        if ((destroy || use || creative || has_bucket ||
                                (pa != null && pa.placed < pa.in_player))//survival has blocks in inventory
                        ) {
                            if (place_block(tmp_pos, block_buffer.state[a])) {
                                if (pa != null)
                                    pa.placed++;
                                placed++;
                            }
                        }
                        if (stop) {
                            break;
                        }
                    }
                }
                modes[m].action(this);

                remove_from_inventory(placed);
            }
            //System.out.println("placed " + placed);
            if ((placed > 0) || (no_tool || damaged_tool)) {

                if (blocks_sent_to_inv > 0) {
                    MutableComponent mc = Compat.literal(blocks_sent_to_inv + " blocks sent to bag/shulker").withStyle(ChatFormatting.BLUE);
                    player.displayClientMessage(mc, false);
                }
                ItemStack is = ItemStack.EMPTY;
                if (!no_tool && !damaged_tool) {
                    if (p1_state != null) {
                        is = p1_state.getBlock().asItem().getDefaultInstance();
                    } else {
                        if (block_state != null) {
                            is = block_state.getBlock().asItem().getDefaultInstance();
                        }
                    }
                }
                if (!is.isEmpty()) {
                    NetworkManager.sendToPlayer((ServerPlayer) player,
                            new Networking.SndPacket(pos, destroy, is, send_sound)
                    );
                }
                if (no_tool || damaged_tool) {
                    NetworkManager.sendToPlayer((ServerPlayer) player,
                            new Networking.ToastPacket(no_tool, damaged_tool)
                    );
                }
                no_tool = false;
                damaged_tool = false;
            }
        }
        if (getP2() != null) {
            setP1(null);
            setP2(null);
            valid = false;
        }
    }

    ItemStack consume_item(BlockAccounting pa, ItemStack stack_item) {
        if (pa != null && pa.placed > 0) {
            if (WandUtils.is_magicbag(stack_item)) {
                int total = MagicBagItem.getTotal(stack_item);
                //ItemStack item_in_bag=MagicBagItem.getItem(stack_item);
                if (pa.placed <= total) {
                    MagicBagItem.dec(stack_item, pa.placed);
                    pa.placed = 0;
                } else {
                    MagicBagItem.dec(stack_item, total);
                    pa.placed -= total;
                }
            } else {
                if (stack_item.getItem().equals(Fluids.LAVA.getBucket())) {
                    pa.placed--;
                    //pa.consumed++;
                    ItemStack ret = Items.BUCKET.getDefaultInstance();
                    return ret;
                } else {
                    if (pa.placed <= stack_item.getCount()) {
                        //pa.consumed+=stack_item.getCount();
                        stack_item.setCount(stack_item.getCount() - pa.placed);
                        pa.placed = 0;
                    } else {
                        //pa.consumed+=stack_item.getCount();
                        pa.placed -= stack_item.getCount();
                        stack_item.setCount(0);
                    }
                }
            }
            return ItemStack.EMPTY;
        }
        return null;
    }

    public void skip() {
        int skip_probability = WandProps.getVal(wand_stack, WandProps.Value.SKIPBLOCK);
        if (skip_probability > 0) {
            for (int a = 0; a < block_buffer.get_length(); a++) {
                int r = random.nextInt(100);
                boolean skip = (r >= skip_probability);
                if (!skip) {
                    block_buffer.state[a] = null;
                }
            }
        }
    }

    public void calc_pv_bbox(BlockPos bp1, BlockPos bp2) {
        x1 = bp1.getX();
        y1 = bp1.getY();
        z1 = bp1.getZ();
        x2 = bp2.getX();
        y2 = bp2.getY();
        z2 = bp2.getZ();
        if (!bp1.equals(bp2)) {
            if (x1 >= x2) {
                x1 += 1;
                bb1_x = x2;
                bb2_x = x1;
            } else {
                x2 += 1;
                bb1_x = x1;
                bb2_x = x2;
            }
            if (y1 >= y2) {
                y1 += 1;
                bb1_y = y2;
                bb2_y = y1;
            } else {
                y2 += 1;
                bb1_y = y1;
                bb2_y = y2;
            }
            if (z1 >= z2) {
                z1 += 1;
                bb1_z = z2;
                bb2_z = z1;
            } else {
                z2 += 1;
                bb1_z = z1;
                bb2_z = z2;
            }
        } else {
            x2 = x1 + 1;
            y2 = y1 + 1;
            z2 = z1 + 1;
            bb1_x = x1;
            bb1_y = y1;
            bb1_z = z1;
            bb2_x = x2;
            bb2_y = y2;
            bb2_z = z2;
        }

        //valid = true;
    }

    public BlockState rotate_mirror(BlockState st, int mirroraxis) {
        switch (mirroraxis) {
            case 1:
                if (rotation == Rotation.NONE || rotation == Rotation.CLOCKWISE_180) {
                    return st.mirror(Mirror.FRONT_BACK).rotate(this.rotation);
                } else {
                    return st.mirror(Mirror.FRONT_BACK).rotate(this.rotation.getRotated(Rotation.CLOCKWISE_180));
                }
            case 2:
                if (rotation == Rotation.NONE || rotation == Rotation.CLOCKWISE_180) {
                    return st.mirror(Mirror.FRONT_BACK).rotate(this.rotation.getRotated(Rotation.CLOCKWISE_180));
                } else {
                    return st.mirror(Mirror.FRONT_BACK).rotate(this.rotation);
                }
            default:
                return st.rotate(this.rotation);
        }
    }

    public BlockState get_state(int y) {
        BlockState st = block_state;
        if (palette.has_palette) {
            int min_y=level.isInsideBuildHeight(block_buffer.min_y)?block_buffer.min_y:0;
            int max_y=level.isInsideBuildHeight(block_buffer.max_y)?block_buffer.max_y:0;

            st = palette.get_state(this,min_y,max_y,y);
        } else {
            if (offhand_state != null && !offhand_state.isAir()) {
                st = offhand_state;
            } else {
                if (mode == Mode.FILL || mode == Mode.LINE || mode == Mode.CIRCLE || mode==Mode.SPHERE) {
                    if (p1_state != null)
                        st = p1_state;
                }
            }
            st = state_for_placement(st, null);
        }

        return st;
    }

    public Item get_item(BlockState state) {
        if (state != null) {
            return state.getBlock().asItem();
        }
        return null;
    }

    BlockState state_for_placement(BlockState st, BlockPos bp) {

        if (mode == Mode.PASTE)
            return st;
        Block blk = st.getBlock();
        if (blk instanceof LeavesBlock) {
            return blk.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        }
        if (blk instanceof WallBlock || blk instanceof CrossCollisionBlock) {
            BlockHitResult hit_res = new BlockHitResult(hit, side, (bp != null ? bp : pos), true);
            BlockPlaceContext pctx = new BlockPlaceContext(player, InteractionHand.OFF_HAND, st.getBlock().asItem().getDefaultInstance(), hit_res);
            return st.getBlock().getStateForPlacement(pctx);
        }
        if (state_mode == WandProps.StateMode.TARGET) {
            BlockHitResult hit_res = new BlockHitResult(hit, side, (bp != null ? bp : pos), true);
            BlockPlaceContext pctx = new BlockPlaceContext(player, InteractionHand.OFF_HAND, st.getBlock().asItem().getDefaultInstance(), hit_res);
            return st.getBlock().getStateForPlacement(pctx);
        }
        if (state_mode == WandProps.StateMode.APPLY) {
            if (blk instanceof SlabBlock) {
                SlabType slab_type;
                if (slab_stair_bottom) {
                    slab_type = SlabType.BOTTOM;
                } else {
                    slab_type = SlabType.TOP;
                }
                return blk.defaultBlockState().setValue(SlabBlock.TYPE, slab_type);

            } else {
                if (blk instanceof StairBlock) {
                    Half h;
                    if (slab_stair_bottom) {
                        h = Half.BOTTOM;
                    } else {
                        h = Half.TOP;
                    }
                    return blk.defaultBlockState().setValue(StairBlock.HALF, h).rotate(rotation);

                } else {
                    if (blk instanceof RotatedPillarBlock) {
                        return blk.defaultBlockState().setValue(RotatedPillarBlock.AXIS, this.axis);
                    } else {
                        BlockHitResult hit_res = new BlockHitResult(hit, side, (bp != null ? bp : pos), true);
                        BlockPlaceContext pctx = new BlockPlaceContext(player, InteractionHand.OFF_HAND, st.getBlock().asItem().getDefaultInstance(), hit_res);
                        return st.getBlock().getStateForPlacement(pctx);
                    }
                }
            }
        }
        return st;
    }

    public void undo(int n) {
        if (undo_buffer != null) {
            for (int i = 0; i < n && i < undo_buffer.size(); i++) {
                CircularBuffer.P p = undo_buffer.peek();
                if (p != null) {
                    if (!p.destroyed) {
                        if (level.destroyBlock(p.pos, false)) {
                            undo_buffer.pop();
                        }
                    } else {
                        if (p.state.canSurvive(level, p.pos) && level.setBlockAndUpdate(p.pos, p.state)) {
                            undo_buffer.pop();
                        }
                    }
                }
            }
            //undo_buffer.print();
        }
    }

    public void redo(int n) {
        if (undo_buffer != null) {
            for (int i = 0; i < n && undo_buffer.can_go_forward(); i++) {
                undo_buffer.forward();
                CircularBuffer.P p = undo_buffer.peek();
                if (p != null && p.pos != null && p.state != null) {
                    if (!p.destroyed) {
                        level.setBlockAndUpdate(p.pos, p.state);
                    } else {
                        level.setBlockAndUpdate(p.pos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
            //undo_buffer.print();
        }
    }

    public void validate_buffer() {
        valid = (block_buffer.get_length() > 0) && block_buffer.get_length() <= this.limit;
        if (!preview && block_buffer.get_length() > this.limit) {
            limit_reached = true;
        }
    }

    public void fill(BlockPos from, BlockPos to, boolean hollow, int xskip, int yskip, int zskip) {
        int xs, ys, zs, xe, ye, ze;
        xs = from.getX();
        xe = to.getX();
        ys = from.getY();
        ye = to.getY();
        zs = from.getZ();
        ze = to.getZ();
        int ox = (xs >= xe) ? -1 : 1;
        int oy = (ys >= ye) ? -1 : 1;
        int oz = (zs >= ze) ? -1 : 1;
        int nx = (xs >= xe) ? xs - xe : xe - xs;
        int ny = (ys >= ye) ? ys - ye : ye - ys;
        int nz = (zs >= ze) ? zs - ze : ze - zs;
        int limit = this.limit;
        int ll = 0;
        block_buffer.reset();
        fill_nx = nx;
        fill_ny = ny;
        fill_nz = nz;
        for (int z = zs, z0 = 0; z0 <= nz; z += oz, z0++) {
            if (zskip != 0 && (z0 % (zskip + 1)) != 0) {
                continue;
            }
            for (int y = ys, y0 = 0; y0 <= ny; y += oy, y0++) {
                if (yskip != 0 && (y0 % (yskip + 1)) != 0) {
                    continue;
                }
                for (int x = xs, x0 = 0; x0 <= nx; x += ox, x0++) {
                    if (hollow) {
                        switch (this.axis) {
                            case X:
                                //if (y > ys && y < ye && z > zs && z < ze)
                                if (y != ys && y != ye && z != zs && z != ze)
                                    continue;
                                break;
                            case Y:
                                //if (x > xs && x < xe && z > zs && z < ze)
                                if (x != xs && x != xe && z != zs && z != ze)
                                    continue;
                                break;
                            case Z:
                                //if (x > xs && x < xe && y > ys && y < ye)
                                if (x != xs && x != xe && y != ys && y != ye)
                                    continue;
                                break;
                        }
                    }
                    if (xskip != 0 && (x0 % (xskip + 1)) != 0) {
                        continue;
                    }
                    if (ll < limit && ll < WandsMod.config.max_limit) {
                        add_to_buffer(x, y, z);
                        ll++;
                    }
                }
            }
        }
        skip();
    }

    void hurt_main_hand(ItemStack stack) {
        if (!this.unbreakable) {
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
    }

    void hurt_tool(ItemStack stack, int tool_slot) {
        if (!this.unbreakable) {
            stack.hurtAndBreak(1, player, EquipmentSlot.OFFHAND);
        }
    }

    boolean place_block(BlockPos block_pos, BlockState state) {
        boolean placed = false;
        //WandsMod.log("place_block "+block_pos+" state: "+state + " destroy: " + destroy,true);
        //Level level = Compat.player_level(player);
        //if (level.isClientSide) {
        //    return false;
        //}

        if (!WandsExpectPlatform.claimCanInteract((ServerLevel) level, block_pos, player)) {
            player.displayClientMessage(Compat.literal("can't use wand on claimed chunk"), false);
            return false;
        }
        if (state == null /*&& !destroy*/) {
            //log("state is null");
            return false;
        }
        Block blk = state.getBlock();
        if (WandsConfig.denied.contains(blk)) {
            //WandsMod.log("block ("+blk+") is in the denied list",true);
            return false;
        }
        BlockState st = level.getBlockState(block_pos);
        Block actual_blk = st.getBlock();
        if (WandsConfig.denied.contains(actual_blk)) {
            //WandsMod.log("actual block ("+actual_blk+") is in the denied list",true);
            return false;
        }
        if (destroy && (mode != Mode.VEIN) && has_offhand && offhand_block != null && offhand_state != st) {
            return false;
        }

        //CommonPro
        int wand_durability = wand_stack.getMaxDamage() - wand_stack.getDamageValue();
        int tool_durability = -1;


        boolean _can_destroy = creative;
        no_tool = false;
        boolean _tool_would_break=false;
        boolean _wand_would_break=wand_would_break();
        if (!creative) {
            if (!_wand_would_break) {
                damaged_tool = true;
                return false;
            }
            if (destroy || replace || (use && !has_water_potion)) {
                _can_destroy = can_destroy(st, true);
                if (digger_item != null) {
                    _tool_would_break = tool_would_break((WandItem)wand_stack.getItem(),digger_item);
                } else {
                    no_tool = true;
                    return false;
                }
                if (!_tool_would_break) {
                    damaged_tool = true;
                    return false;
                }
            }
        } else {
            if (creative && use) {
                can_destroy(st, true);
                if (digger_item == null && !has_water_potion) {
                    no_tool = true;
                    return false;

                }
            }
        }
        if ((use) && has_shear && state.is(Blocks.PUMPKIN)) {
            BlockState carved_pumpkin = Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, player.getDirection().getOpposite());
            level.setBlockAndUpdate(block_pos, carved_pumpkin);
            if (!creative) {
                ItemStack pumpkin_seeds = Items.PUMPKIN_SEEDS.getDefaultInstance();
                pumpkin_seeds.setCount(4);
                drop(block_pos, state, null, pumpkin_seeds);
                if(_wand_would_break) {
                    hurt_main_hand(wand_stack);
                }
                consume_xp();
            }
            return true;
        }

        if ((use) && has_water_potion && state.is(BlockTags.CONVERTABLE_TO_MUD)) {
            level.setBlockAndUpdate(block_pos, Blocks.MUD.defaultBlockState());
            send_sound = Sounds.SPLASH.ordinal();
            if (!creative) {
                if(_wand_would_break) {
                    hurt_main_hand(wand_stack);
                }
                consume_xp();
            }
            return true;
        }

        p1_state = state;
        if (!destroy) {
            if (offhand != null) {
                blk = Block.byItem(offhand.getItem());
            }

            //if(!replace && !blk.canSurvive(state, level, block_pos)){
            if (!(replace) && !state.canSurvive(level, block_pos)) {
                return false;
            }
            if (blk instanceof SnowLayerBlock) {
                BlockState below = level.getBlockState(block_pos.below());
                if (below.getBlock() instanceof SnowLayerBlock) {
                    int layers = below.getValue(SnowLayerBlock.LAYERS);
                    if (layers < 8) {
                        block_pos = block_pos.below();
                        state = state.setValue(SnowLayerBlock.LAYERS, layers + 1);
                    }
                }
            } else {
                if (blk instanceof CrossCollisionBlock || blk instanceof DoorBlock) {
                    BlockHitResult hit_res = new BlockHitResult(new Vec3(block_pos.getX() + 0.5, block_pos.getY() + 1.0, block_pos.getZ() + 0.5), side, block_pos, true);
                    UseOnContext uctx = new UseOnContext(player, InteractionHand.OFF_HAND, hit_res);
                    BlockPlaceContext pctx = new BlockPlaceContext(uctx);
                    state = state.getBlock().getStateForPlacement(pctx);
                }
            }
        }

        if (use && digger_item != null && (has_hoe || has_shovel || has_axe || has_shear)) {
            BlockHitResult hit_res = new BlockHitResult(new Vec3(block_pos.getX() + 0.5, block_pos.getY() + 1.0, block_pos.getZ() + 0.5), Direction.UP, block_pos, true);
            UseOnContext ctx = new UseOnContext(player, InteractionHand.OFF_HAND, hit_res);
            if (digger_item.useOn(ctx) != InteractionResult.PASS) {
                if (!creative ) {
                    if(_wand_would_break) {
                        hurt_main_hand(wand_stack);
                    }
                    if(_tool_would_break) {
                        hurt_tool(digger_item, digger_item_slot);
                    }
                    consume_xp();
                }
            } else {
                return false;
            }
            return true;
        }

        if (creative) {
            if (destroy) {
                if (destroyBlock(block_pos, false)) {
                    if (undo_buffer != null) {
                        undo_buffer.put(block_pos, level.getBlockState(block_pos), destroy);
                        //undo_buffer.print();
                    }
                    return true;
                }
            } else {
                if (!use) {
                    state = state_for_placement(state, block_pos);
                    if (state != null && state.canSurvive(level, block_pos) && level.setBlockAndUpdate(block_pos, state)) {
                        blk.setPlacedBy(level, block_pos, state, player, blk.asItem().getDefaultInstance());
                        if (undo_buffer != null) {
                            undo_buffer.put(block_pos, state, destroy);
                            //undo_buffer.print();
                        }
                        return true;
                    }
                }
            }
        } else {
            //if (destroy)
            {

                float xp = WandUtils.calc_xp(player.experienceLevel, player.experienceProgress);
                float dec = 0.0f;
                float BLOCKS_PER_XP = WandsMod.config.blocks_per_xp;
                if (BLOCKS_PER_XP != 0) {
                    dec = (1.0f / BLOCKS_PER_XP);
                }
                if ((BLOCKS_PER_XP == 0 || (xp - dec) >= 0)) {
                    if (WandsMod.config.destroy_in_survival_drop && (destroy || replace)) {
                        if (_can_destroy) {
                            placed = destroyBlock(block_pos, true);
                        }
                    }
                    if (!destroy || (replace && placed)) {
                        if (!use) {
                            if (state != null && state.canSurvive(level, block_pos) && level.setBlockAndUpdate(block_pos, state)) {
                                blk.setPlacedBy(level, block_pos, state, player, blk.asItem().getDefaultInstance());
                                placed = true;
                            }
                        }
                    }
                    if (replace && !placed) {
                        if (digger_item.getItem() == Items.AIR)
                            player.displayClientMessage(Compat.literal("incorrect tool"), false);
                        stop = true;
                    }
                } else {
                    if (BLOCKS_PER_XP != 0 && (xp - dec) < 0) {
                        player.displayClientMessage(Compat.literal("not enough xp"), false);
                        stop = true;
                    }
                    if (wand_durability == 1) {
                        player.displayClientMessage(Compat.literal("wand damaged"), false);
                        if (WandsMod.config.allow_wand_to_break &&
                           digger_item != null && digger_item.getItem() == Items.AIR
                        ) {

                        } else {
                            stop = true;
                        }
                    }
                }

                if (placed) {
                    if ((destroy || replace) && digger_item != null && _tool_would_break) {
                        hurt_tool(digger_item, digger_item_slot);
                    }
                    if (!this.unbreakable && _wand_would_break) {
                        hurt_main_hand(wand_stack);
                    }
                    consume_xp();
                }
            }
        }
        //log("place_block placed: "+placed);
        return placed;
    }

    public boolean destroyBlock(BlockPos blockPos, boolean bl) {
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.isAir()) {
            return false;
        } else {
            FluidState fluidState = level.getFluidState(blockPos);
            if (!(blockState.getBlock() instanceof BaseFireBlock)) {
                //level.levelEvent(2001, blockPos, Block.getId(blockState));
            }

            if (bl) {
                BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(blockPos) : null;
                //System.out.println("destroyBlock "+bl);
                if (this.mine_to_inventory) {
                    if (level instanceof ServerLevel) {
                        if (!drop(blockPos, blockState, blockEntity, null)) {
                            return false;
                        }

                    }
                } else {
                    blockState.getBlock().playerDestroy(level, player, pos, blockState, blockEntity, digger_item);
                }

            }
            player.awardStat(Stats.BLOCK_MINED.get(blockState.getBlock()));
            player.causeFoodExhaustion(0.005F);
            boolean bl2 = level.setBlock(blockPos, fluidState.createLegacyBlock(), 3, 512);
            if (bl2) {
                //level.gameEvent(GameEvent.BLOCK_DESTROY, blockPos, GameEvent.Context.of(entity, blockState));
            }
            return bl2;
        }
    }

    boolean drop(BlockPos blockPos, BlockState blockState, BlockEntity blockEntity, ItemStack drop_item) {
        BlockPos drop_pos;
        if (drop_on_player) {
            drop_pos = player.getOnPos().above();
        } else {
            drop_pos = blockPos;
        }
        if (drop_item != null) {
            if (!place_into(drop_item)) {
                if (!this.stop) {
                    Block.popResource(level, drop_pos, drop_item);
                }
            }
        } else {
            Block.getDrops(blockState, (ServerLevel) level, blockPos, blockEntity, player, digger_item).forEach(
                    (itemStackx) -> {
                        //player.displayClientMessage(Compat.literal(itemStackx.getDescriptionId()),false);
                        if (!place_into(itemStackx)) {
                            if (!this.stop) {
                                Block.popResource(level, drop_pos, itemStackx);
                            }
                        }
                    });
        }

        if (!this.stop && digger_item != null && blockState.getBlock() instanceof DropExperienceBlock && !Compat.has_silktouch(digger_item, level)) {
            DropExperienceBlock dblock = (DropExperienceBlock) blockState.getBlock();
            int xp = ((DropExperienceBlockAccessor) dblock).getXpRange().sample(level.random);
            //WandsMod.LOGGER.info("drop xp "+xp);
            if (xp > 0) {
                if (((ServerLevel) level).getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
                    ExperienceOrb.award((ServerLevel) level, Vec3.atCenterOf(drop_pos), xp);
                }
            }
        }
        if (stop) {
            if (!preview) {
                player.displayClientMessage(Compat.literal("inventory full"), false);
            }
            return false;
        }
        return true;
    }

    boolean place_into(ItemStack item_to_place) {
        ItemStack oh = player.getOffhandItem();
        //System.out.println("offhand "+oh.getTag());
        if (!oh.isEmpty()) {
            if (WandUtils.is_shulker(oh)) {
                return place_into_shulker(oh, item_to_place, false);
            } else if (WandUtils.is_magicbag(oh)) {
                return place_into_bag(oh, item_to_place);
            }
        }
        //look for bags and shulkers
        for (int pi = 0; pi < inv_aux_last; ++pi) {
            ItemStack stack = player_inv.getItem(inv_aux[pi]);
            if (WandUtils.is_shulker(stack)) {
                if (place_into_shulker(stack, item_to_place, true)) {
                    return true;
                }
            }
            if (WandUtils.is_magicbag(stack)) {
                if (place_into_bag(stack, item_to_place)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean place_into_bag(ItemStack bag, ItemStack item_to_place) {
        ItemStack mb_item = MagicBagItem.getItem(bag,this.level.registryAccess());
        int total = MagicBagItem.getTotal(bag);
        if (mb_item.isEmpty() && total == 0) {
            MagicBagItem.setItem(bag, item_to_place,this.level.registryAccess());
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

    boolean place_into_shulker(ItemStack shulker, ItemStack item_to_place, boolean bag_only) {

        ItemContainerContents contents= shulker.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        Iterator<ItemStack> it=contents.nonEmptyItems().iterator();
        while(it.hasNext()){
            ItemStack slot_item=it.next();
            if(WandUtils.is_magicbag(slot_item) ) {
                ItemStack bag_with_same_item=MagicBagItem.getItem(slot_item,this.level.registryAccess());
                if(bag_with_same_item.getItem()==item_to_place.getItem()){
                    if(place_into_bag(slot_item,item_to_place)){
                        return true;
                    }
                    break;
                }
            }
        }
        if(!bag_only) {
            Iterator<ItemStack> it2=contents.nonEmptyItems().iterator();
            int item_count = item_to_place.getCount();
            //look for stack that alreadey have the item
            int slots=0;
            while(it2.hasNext()){
                slots++;
                ItemStack slot_item=it2.next();
                if (slot_item != null) {
                    if (Compat.is_same(item_to_place,slot_item)) {
                        int total = item_count + slot_item.getCount();
                        if (total <= slot_item.getMaxStackSize()) {
                            slot_item.setCount(total);
                            blocks_sent_to_inv += item_count;
                            return true;
                        } else {
                            //slot full
                            blocks_sent_to_inv += slot_item.getMaxStackSize()-slot_item.getCount();
                            slot_item.setCount(slot_item.getMaxStackSize());
                            item_count = total - slot_item.getMaxStackSize();
                        }
                    } else {
                        if (WandUtils.is_magicbag(slot_item)) {
                            if (place_into_bag(slot_item, item_to_place)) {
                                return true;
                            }
                        }
                    }
                }
            }
            item_to_place.setCount(item_count);
            if (item_count > 0) {
                if (slots >= 0 && slots < 27) {
                    List<ItemStack> list= new ArrayList<>(contents.stream().toList());
                    if(list.size()<27){
                        list.add(item_to_place);
                        blocks_sent_to_inv += item_count;
                        shulker.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(list));
                        return true;
                    }
                }
                //if we get here the shulker is full
                if (this.stop_on_full_inventory) {
                    stop = true;
                }
            }
        }
        return false;
    }

    public void update_inv_aux() {
        inv_aux_last = 0;
        for (int s = 0; s < 36; s++) {
            ItemStack stack = player_inv.getItem(s);
            if (WandUtils.is_shulker(stack)) {
                inv_aux[inv_aux_last] = s;
                inv_aux_last++;
            } else if (WandUtils.is_magicbag(stack)) {
                inv_aux[inv_aux_last] = s;
                inv_aux_last++;
            }
        }
    }

    public boolean add_to_buffer(int x, int y, int z) {
        if (!level.isOutsideBuildHeight(y) && block_buffer.get_length() < limit) {
            BlockState st = level.getBlockState(tmp_pos.set(x, y, z));
            if (destroy || replace || use) {
                if (!st.isAir() || mode == Mode.AREA) {
                    return block_buffer.add(x, y, z, this);
                }
            } else {
                if (can_place(st, tmp_pos))
                    return block_buffer.add(x, y, z, this);
            }
        } else {
            limit_reached = true;
        }
        return false;
    }

    void consume_xp() {
        float BLOCKS_PER_XP = WandsMod.config.blocks_per_xp;
        if (BLOCKS_PER_XP != 0) {
            float diff = WandUtils.calc_xp_to_next_level(player.experienceLevel);
            float prog = player.experienceProgress;
            if (diff > 0 && BLOCKS_PER_XP != 0.0f) {
                float a = (1.0f / diff) / BLOCKS_PER_XP;
                if (prog - a > 0) {
                    prog = prog - a;
                } else {
                    if (prog > 0.0f) {
                        prog = 1.0f + (a - prog);
                    } else {
                        prog = 1.0f;
                    }
                    if (player.experienceLevel > 0) {
                        player.experienceLevel--;
                        a = (1.0f / diff) / BLOCKS_PER_XP;
                        if (prog - a > 0) {
                            prog = prog - a;
                        }
                    }
                }
                player.experienceProgress = prog;
            }
        }
    }

    public void update_tools() {
        digger_item_slot = -1;
        n_tools = 0;
        Optional<int[]> a =this.player_data.getIntArray("Tools");
        if(a.isEmpty()) return;
        int[] tools_slots = a.get();
        for (int t = 0; t < tools_slots.length; t++) {
            ItemStack tool_item = player.getInventory().getItem(tools_slots[t]);
            if (tool_item.isEmpty()) continue;
            tools[n_tools] = tool_item;
            n_tools++;
            Item item = tool_item.getItem();
            has_hoe = (has_hoe || item instanceof HoeItem);
            has_shovel = has_shovel || item instanceof ShovelItem;
            has_axe = has_axe || item instanceof AxeItem;
            has_shear = has_shear || item instanceof ShearsItem;
        }
    }

    public boolean can_destroy(BlockState state, boolean check_speed) {
        digger_item = null;
        WandItem wand_item=(WandItem)this.wand_stack.getItem();
        for (int i = 0; i < n_tools; i++) {
            if (tools[i] != null) {
                if (!tools[i].isEmpty() && tool_would_break(wand_item,tools[i])) {
                    if (((destroy || replace) && can_dig(state, check_speed, tools[i])) ||
                            ((use) && (
                                    (tools[i].getItem() instanceof HoeItem && WandUtils.is_tillable(state)) ||
                                    (tools[i].getItem() instanceof AxeItem && WandUtils.is_strippable(state)) ||
                                    (tools[i].getItem() instanceof ShovelItem && WandUtils.is_flattenable(state)) ||
                                    (tools[i].getItem() instanceof ShearsItem && can_shear(state))
                            ))
                    ) {
                        if (digger_item == null) {
                            digger_item = tools[i];
                            digger_item_slot = i;
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean can_dig(BlockState state, boolean check_speed, ItemStack digger) {
        boolean is_glass = state.getBlock() instanceof TransparentBlock;
        boolean is_snow_layer = false;
        boolean can_shear = false;
        Block blk = state.getBlock();
        if (blk instanceof SnowLayerBlock) {
            is_snow_layer = state.getValue(SnowLayerBlock.LAYERS) == 1;
        }
        Item item_digger = digger.getItem();
        Tool tool = digger.get(DataComponents.TOOL);
        if (digger != null && !digger.isEmpty() && (tool!=null || item_digger instanceof ShearsItem)) {
            boolean is_allowed = false;
            boolean minable = false;
            if (item_digger instanceof ShearsItem) {
                can_shear = can_shear(state);
                is_allowed = is_allowed || WandsConfig.shears_allowed.contains(blk);
            } else {
                if (item_digger instanceof AxeItem) {
                    is_allowed = is_allowed || WandsConfig.axe_allowed.contains(blk);
                } else {
                    if (item_digger instanceof ShovelItem) {
                        is_allowed = is_allowed || WandsConfig.shovel_allowed.contains(blk);
                    } else {
                        if (item_digger instanceof HoeItem) {
                            is_allowed = is_allowed || WandsConfig.hoe_allowed.contains(blk);
                        }else{
                            //TODO: find a new way to check if it's a pickaxe
                            is_allowed = is_allowed || WandsConfig.pickaxe_allowed.contains(blk);
                        }
                    }
                }
            }
            if (check_speed) {
                float destroy_speed = item_digger.getDestroySpeed(digger, state);
                boolean correct_tool = false;
                if (!digger.isEmpty()) {
                    Tool dtool = digger.get(DataComponents.TOOL);
                    correct_tool = dtool != null && dtool.isCorrectForDrops(state);
                }
                return creative || (destroy_speed > 1.0f && correct_tool)
                        || is_glass || is_snow_layer || is_allowed || can_shear;
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean replace_fluid(BlockState state) {
        if (this.removes_water && this.removes_lava) {
            return state.getFluidState().is(FluidTags.WATER) || state.getFluidState().is(FluidTags.LAVA);
        } else if (this.removes_water) {
            return state.getFluidState().is(FluidTags.WATER);
        }
        return false;
    }

    public boolean can_place(BlockState state, BlockPos p) {
        if (offhand_state != null && WandUtils.is_plant(offhand_state)) {
            return (state.isAir() || replace_fluid(state)) && offhand_state.canSurvive(level, p);
        } else {
            return (
                    state.isAir() || replace_fluid(state) || WandUtils.is_plant(state) ||
                            state.getBlock() instanceof SnowLayerBlock ||
                            (has_empty_bucket && state.getFluidState().is(FluidTags.WATER)) ||
                            (has_empty_bucket && state.getFluidState().is(FluidTags.LAVA))
            );
        }
    }

    public boolean can_shear(BlockState state) {
        return (
                state.is(BlockTags.LEAVES) ||
                        state.is(Blocks.COBWEB) ||
                        state.is(Blocks.SHORT_GRASS) ||
                        state.is(Blocks.FERN) ||
                        state.is(Blocks.DEAD_BUSH) ||
                        state.is(Blocks.VINE) ||
                        state.is(Blocks.TRIPWIRE) ||
                        state.is(Blocks.PUMPKIN) ||
                        state.is(BlockTags.WOOL) ||
                        state.is(Blocks.HANGING_ROOTS)
        );
    }

    void check_inventory() {
        if ((!creative || mode == Mode.BLAST) && !destroy && !use && !has_water_bucket && mode != Mode.COPY) {
            ItemStack stack;
            //now the player inventory
            for (int i = 0; i < 36; ++i) {
                stack = player_inv.getItem(i);
                if (stack.getItem() != Items.AIR) {
                    if (WandUtils.is_shulker(stack)) {
                        for (Map.Entry<Item, BlockAccounting> pa : block_accounting.entrySet()) {
                            pa.getValue().in_player += WandUtils.count_in_shulker(stack, pa.getKey(),level.registryAccess());
                        }
                    } else if (WandUtils.is_magicbag(stack)) {
                        int total = MagicBagItem.getTotal(stack);
                        ItemStack stack2 = MagicBagItem.getItem(stack,this.level.registryAccess());
                        if (!stack2.isEmpty() && total > 0) {
                            BlockAccounting ba = block_accounting.get(stack2.getItem());
                            if (ba != null) {
                                ba.in_player += total;
                            }
                        }
                    } else {
                        //if (stack.getTags().findAny().isEmpty()) {
                        if (stack.get(DataComponents.CUSTOM_DATA) == null) { //TODO: verify
                            for (Map.Entry<Item, BlockAccounting> pa : block_accounting.entrySet()) {
                                Item item = pa.getKey();
                                if (item != null && !stack.isEmpty() && item == stack.getItem()) {
                                    pa.getValue().in_player += stack.getCount();
                                }
                            }
                        }
                    }
                }
            }
            ItemStack oh = player.getOffhandItem();
            if (oh != null && !oh.isEmpty()) {
                if (oh.getItem() instanceof MagicBagItem) {
                    stack = MagicBagItem.getItem(oh,this.level.registryAccess());
                    int total = MagicBagItem.getTotal(oh);
                    if (!stack.isEmpty() && total > 0) {
                        BlockAccounting ba = block_accounting.get(stack.getItem());
                        if (ba != null) {
                            ba.in_player += total;
                        }
                    }
                } else {
                    if (offhand != null) {
                        BlockAccounting ba = block_accounting.get(offhand.getItem());
                        if (ba != null) {
                            ba.in_player += offhand.getCount();
                        }
                    }
                }
            }
            for (Map.Entry<Item, BlockAccounting> pa : block_accounting.entrySet()) {
                if (pa.getValue().in_player < pa.getValue().needed) {
                    MutableComponent name = Compat.translatable(pa.getKey().getDescriptionId());
                    MutableComponent mc = Compat.literal("Not enough ").withStyle(ChatFormatting.RED).append(name);
                    mc.append(". " + pa.getValue().in_player);
                    mc.append("/");
                    mc.append("" + pa.getValue().needed);
                    player.displayClientMessage(mc, false);
                    //missing_blocks = true;
                }
                //log(pa.getKey().getDescriptionId()+" needed: "+pa.getValue().needed+" player has: "+pa.getValue().in_player);
            }
        }
    }

    void remove_from_inventory(int placed) {
        if (!creative && ((!destroy && placed > 0) || mode == Mode.BLAST)) {
            ItemStack stack;
            ItemStack stack_item;
            //look for items on shulker boxes first
            for (int pi = 0; pi < 36; ++pi) {
                stack = player_inv.getItem(pi);
                if(stack.getItem() != Items.AIR) {
                    if (WandUtils.is_shulker(stack)) {
                        Iterator<ItemStack> it = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItems().iterator();
                        while(it.hasNext()){
                            stack_item = it.next();
                            if (!stack_item.isEmpty() ) {
                                if (WandUtils.is_magicbag(stack_item)) {
                                    ItemStack bag_it=MagicBagItem.getItem(stack_item,this.level.registryAccess());
                                    BlockAccounting pa = block_accounting.get(bag_it.getItem());
                                    consume_item(pa, stack_item);
                                } else {
                                    BlockAccounting pa = block_accounting.get(stack_item.getItem());
                                    consume_item(pa, stack_item);
                                }
                            }
                        }
                    }
                }
            }

            ItemStack oh = player.getOffhandItem();
            if (!oh.isEmpty() && oh.getItem() instanceof MagicBagItem) {
                stack_item = MagicBagItem.getItem(oh,this.level.registryAccess());
                BlockAccounting pa = block_accounting.get(stack_item.getItem());
                consume_item(pa, oh);
            }
            //now look for bags on player inv
            for (int i = 0; i < 36; ++i) {
                stack_item = player_inv.getItem(i);
                if (WandUtils.is_magicbag(stack_item)) {
                    ItemStack bag_it = MagicBagItem.getItem(stack_item,this.level.registryAccess());
                    BlockAccounting pa = block_accounting.get(bag_it.getItem());
                    consume_item(pa, stack_item);
                }
            }
            //now look for items on player inv
            for (int i = 0; i < 36; ++i) {
                stack_item = player_inv.getItem(i);
                if (stack_item.getItem() != Items.AIR) {
                    if (!WandUtils.is_shulker(stack_item) && !WandUtils.is_magicbag(stack_item)
                            &&
                            stack_item.get(DataComponents.CUSTOM_DATA) == null
                    ) {
                        BlockAccounting pa = block_accounting.get(stack_item.getItem());
                        ItemStack rep = consume_item(pa, stack_item);
                        if (rep != null && !rep.isEmpty()) {
                            player_inv.setItem(i, rep);
                        }
                    }
                }
            }
            if (offhand != null && !offhand.isEmpty() && !WandUtils.is_magicbag(offhand)) {
                BlockAccounting pa = block_accounting.get(offhand.getItem());
                if (pa != null) {
                    //pa.in_player += offhand.getCount();
                    consume_item(pa, offhand);
                }
            }
        }
    }

    boolean check_advancement(ServerAdvancementManager server_advancements, PlayerAdvancements player_advancements, String a) {
        ResourceLocation res = ResourceLocation.tryParse(a);
        if (res == null) {
            WandsMod.log("bad advancement: " + res, prnt);
            return false;
        }
        AdvancementHolder adv = server_advancements.get(res);
        if (adv == null) {
            WandsMod.log("bad advancement: " + res, prnt);
            return false;
        }
        AdvancementProgress prog = player_advancements.getOrStartProgress(adv);
        return prog.isDone();
    }

    void check_advancements() {
        if (!creative && WandsMod.config.check_advancements && !level.isClientSide()) {
            PlayerAdvancements advs = ((ServerPlayer) player).getAdvancements();
            MinecraftServer server = player.getServer();
            if (server == null) return;
            ServerAdvancementManager advancements = server.getAdvancements();
            if (advancements == null) return;

            if (!Objects.equals(WandsMod.config.advancement_allow_stone_wand, "") && ((WandItem) (wand_stack.getItem())).tier == WandItem.WandTier.STONE_WAND) {
                if (!check_advancement(advancements, advs, WandsMod.config.advancement_allow_stone_wand)) {
                    return;
                }
            }
            if (!Objects.equals(WandsMod.config.advancement_allow_iron_wand, "") && ((WandItem) (wand_stack.getItem())).tier == WandItem.WandTier.IRON_WAND) {
                if (!check_advancement(advancements, advs, WandsMod.config.advancement_allow_iron_wand)) {
                    return;
                }
            }
            if (!Objects.equals(WandsMod.config.advancement_allow_diamond_wand, "") && ((WandItem) (wand_stack.getItem())).tier == WandItem.WandTier.DIAMOND_WAND) {
                if (!check_advancement(advancements, advs, WandsMod.config.advancement_allow_diamond_wand)) {
                    //WandsMod.log("need advancement: "+WandsMod.config.advancement_allow_diamond_wand,prnt);
                    return;
                }
            }
            if (!Objects.equals(WandsMod.config.advancement_allow_netherite_wand, "") && ((WandItem) (wand_stack.getItem())).tier == WandItem.WandTier.NETHERITE_WAND) {
                if (!check_advancement(advancements, advs, WandsMod.config.advancement_allow_netherite_wand)) {
                    return;
                }
            }
        }
    }

    public BlockPos get_pos_from_air(Vec3 hit) {
        if (player == null)
            return new BlockPos((int) hit.x, (int) hit.y, (int) hit.z);
        float r=player.getYRot();
        Vec3 eye=player.getEyePosition();
        hit=hit.add(hit.subtract(eye).normalize().scale(target_air_distance));
        Direction dir = player.getDirection();
        int offx = 0;
        int offy = 0;
        int offz = 0;
        switch (dir) {
            case NORTH:
            case SOUTH:
                if (hit.x < 0) {
                    offx = -1;
                }
                break;
            case EAST:
            case WEST:
                if (hit.z < 0) {
                    //offz = 1;
                    offz = -1;
                } else {

                }
                break;

        }
        if (hit.y < 0) {
            offy = -1;
        }

        //BlockPos p=new BlockPos((int) hit.x + offx, (int) hit.y + offy, (int) hit.z + offz).relative(dir,target_air_distance);
        return new BlockPos((int) hit.x + offx, (int) hit.y + offy, (int) hit.z + offz);
    }

    public void copy() {
        if (mode == Mode.COPY) {
            int m = mode.ordinal();
            modes[m].place_in_buffer(this);
        }
    }

    private boolean tool_would_break(WandItem wand_item,ItemStack tool){

        int dmg = tool.getMaxDamage() - tool.getDamageValue();
        Item tool_item=tool.getItem();
        if(WandsMod.config.allow_stone_tools_to_break) {
            if(Items.STONE_PICKAXE.getDefaultInstance().is(tool_item) ||
               Items.STONE_AXE.getDefaultInstance().is(tool_item) ||
               Items.STONE_SHOVEL.getDefaultInstance().is(tool_item)||
               Items.STONE_HOE.getDefaultInstance().is(tool_item)
            ) {
                return true;
            }
        }else if(WandsMod.config.allow_iron_tools_to_break) {
            if(Items.IRON_PICKAXE.getDefaultInstance().is(tool_item) ||
               Items.IRON_AXE.getDefaultInstance().is(tool_item) ||
               Items.IRON_SHOVEL.getDefaultInstance().is(tool_item)||
               Items.IRON_HOE.getDefaultInstance().is(tool_item)
            ) {
                return true;
            }
        }else if(WandsMod.config.allow_diamond_tools_to_break) {
            if(Items.DIAMOND_PICKAXE.getDefaultInstance().is(tool_item) ||
               Items.DIAMOND_AXE.getDefaultInstance().is(tool_item) ||
               Items.DIAMOND_SHOVEL.getDefaultInstance().is(tool_item)||
               Items.DIAMOND_HOE.getDefaultInstance().is(tool_item)
            ) {
                return true;
            }
        } else if(WandsMod.config.allow_netherite_tools_to_break) {
            if(Items.NETHERITE_PICKAXE.getDefaultInstance().is(tool_item) ||
               Items.NETHERITE_AXE.getDefaultInstance().is(tool_item) ||
               Items.NETHERITE_SHOVEL.getDefaultInstance().is(tool_item)||
               Items.NETHERITE_HOE.getDefaultInstance().is(tool_item)
            ) {
                return true;
            }
        }
        return dmg>= TOOL_DAMAGE_STOP;
    }
    private boolean wand_would_break(){
        if(this.wand_stack==null){
            return true;
        }
        int dmg = this.wand_stack.getMaxDamage() - this.wand_stack.getDamageValue();
        if(dmg<=1) {
            WandItem wand_item = (WandItem) this.wand_stack.getItem();
            switch (wand_item.tier) {
                case STONE_WAND:
                    return WandsMod.config.allow_stone_wand_to_break;
                case IRON_WAND:
                    return WandsMod.config.allow_iron_wand_to_break;
                case DIAMOND_WAND:
                    return WandsMod.config.allow_diamond_wand_to_break;
                case NETHERITE_WAND:
                    return WandsMod.config.allow_netherite_wand_to_break;
            }
        }
        return dmg>=1;
    }
}
