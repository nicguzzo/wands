package net.nicguzzo.wands.wand;

#if MC_VERSION >= 12005
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.Tool;
import net.minecraft.advancements.AdvancementHolder;
#endif
#if MC_VERSION >= 12111
import net.minecraft.world.level.gamerules.GameRules;
#else
import net.minecraft.world.level.GameRules;
#endif
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.MutableComponent;
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
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;


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
import net.nicguzzo.compat.MyIdExt;
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
import net.nicguzzo.compat.Compat;
import net.nicguzzo.wands.utils.WandUtils;
import net.nicguzzo.wands.wand.WandProps.Mode;

import java.util.*;
import java.util.function.Consumer;
//import com.github.clevernucleus.opc.api.OfflinePlayerCacheAPI;
//import com.github.clevernucleus.opc.api.claim.Claim;

//TODO implement https://github.com/Patbox/common-protection-api for claimed chunks
//TODO fix mirroring and rotation
//TODO augmentation items, durability, range, damage?, planting?
//TODO support other mods tools
//TODO infinite (creative) wand recipe, optional
//TODO drop items on wand merge craft
//TODO fix plants place, force samestate, needs air bug
//TODO fix box mode only on netherite, should be iron+
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
    public boolean clear_p1 = true;
    public boolean clear_p2 = true;
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

    enum ToolType {
        PICKAXE,
        AXE,
        SHOVEL,
        HOE,
        SHEAR
    }

    static class WandTool {
        boolean empty = true;
        ItemStack tool = null;
        ToolType tooltype = null;
    }
    //ItemStack[] tools = new ItemStack[9 + 27];
    WandTool[] tools = new WandTool[9 + 27];
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
    public boolean has_powder_snow_bucket = false;
    public boolean has_empty_bucket = false;
    #if MC_VERSION>="11900"
    boolean has_water_potion = false;
    #endif
    int send_sound = -1;
    boolean has_offhand = false;
    public boolean has_pickaxe = false;
    public boolean has_hoe = false;
    public boolean has_shovel = false;
    public boolean has_axe = false;
    public boolean has_shear = false;
    public boolean force_render = false;
    public boolean limit_reached = false;
    public WandProps.Plane plane = WandProps.Plane.XZ;
    public Direction.Axis axis = Direction.Axis.Y;
    public Rotation rotation;
    public Rotation block_rotation = Rotation.NONE;
    public WandProps.StateMode state_mode = WandProps.StateMode.CLONE;
    private boolean no_tool;
    private boolean damaged_tool;
    private boolean no_use_action;
    private String needed_tool = "";
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
    private boolean allow_wand_to_break = false;

    MyIdExt adv_id=new MyIdExt();

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

    /**
     * Extends the bounding box to include the clicked block.
     * Moves whichever point (P1 or P2) defines the boundary on each axis.
     */
    public void extendBbox(BlockPos clickedPos) {
        if (p1 == null || p2 == null) return;

        int newP1x = p1.getX(), newP1y = p1.getY(), newP1z = p1.getZ();
        int newP2x = p2.getX(), newP2y = p2.getY(), newP2z = p2.getZ();
        int cx = clickedPos.getX(), cy = clickedPos.getY(), cz = clickedPos.getZ();

        // X axis
        int minX = Math.min(p1.getX(), p2.getX());
        int maxX = Math.max(p1.getX(), p2.getX());
        if (cx < minX) {
            if (p1.getX() <= p2.getX()) newP1x = cx; else newP2x = cx;
        } else if (cx > maxX) {
            if (p1.getX() >= p2.getX()) newP1x = cx; else newP2x = cx;
        }

        // Y axis
        int minY = Math.min(p1.getY(), p2.getY());
        int maxY = Math.max(p1.getY(), p2.getY());
        if (cy < minY) {
            if (p1.getY() <= p2.getY()) newP1y = cy; else newP2y = cy;
        } else if (cy > maxY) {
            if (p1.getY() >= p2.getY()) newP1y = cy; else newP2y = cy;
        }

        // Z axis
        int minZ = Math.min(p1.getZ(), p2.getZ());
        int maxZ = Math.max(p1.getZ(), p2.getZ());
        if (cz < minZ) {
            if (p1.getZ() <= p2.getZ()) newP1z = cz; else newP2z = cz;
        } else if (cz > maxZ) {
            if (p1.getZ() >= p2.getZ()) newP1z = cz; else newP2z = cz;
        }

        setP1(new BlockPos(newP1x, newP1y, newP1z));
        setP2(new BlockPos(newP2x, newP2y, newP2z));
        calc_pv_bbox(p1, p2);
    }

    /**
     * Returns the effective end position for 2-click modes.
     * ClientRender already applies INCSELBLOCK offset before calling do_or_preview,
     * so this method just returns pos directly.
     */
    public BlockPos getEffectiveEndPos() {
        return pos;
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

    public final Pin pin = new Pin();

    private final BlockPos.MutableBlockPos tmp_pos = new BlockPos.MutableBlockPos();
    InventoryManager inventory;
    public int MAX_COPY_VOL = WandsConfig.max_limit;
    public int radius = 0;

    public boolean preview;
    public boolean creative = true;
    public WandProps.Mode mode = null;
    public WandProps.Mode prevMode = null;  // Track previous mode to detect changes
    public boolean prnt = false;
    public int limit = WandsConfig.max_limit;
    public boolean slab_stair_bottom = true;
    static boolean once = true;

    WandMode[] modes = null;

    public boolean drop_on_player = true;

    public CompoundTag player_data;

    public Wand() {
        modes = new WandMode[WandProps.modes.length];
        for (int i = 0; i < modes.length; i++) {
            modes[i] = WandProps.modes[i].get_mode();
        }
        for (int i = 0; i < tools.length; i++) {
            tools[i] = new WandTool();
        }
        this.block_buffer.reset();
        this.clear(true);
    }

    public WandMode get_mode() {
        if (modes != null && mode != null)
            return modes[mode.ordinal()];
        else {
            return null;
        }
    }

    public void clear(boolean force_clear_p1) {
        this.clear_p1 = WandProps.getFlag(wand_stack, WandProps.Flag.CLEAR_P1);
        if (force_clear_p1 || clear_p1)
            setP1(null);
        if (clear_p2)
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
        WandProps.Mode newMode = WandProps.getMode(wand_stack);
        // Clear buffer and selection when mode changes
        if (prevMode != null && prevMode != newMode) {
            block_buffer.reset();
            clear(true);
            pin.clear();
        }
        prevMode = newMode;
        mode = newMode;
        axis = WandProps.getAxis(wand_stack);
        plane = WandProps.getPlane(wand_stack);
        rotation = WandProps.getRotation(wand_stack);
        block_rotation = WandProps.getBlockRotation(wand_stack);
        state_mode = WandProps.getStateMode(wand_stack);
        slab_stair_bottom = WandProps.getFlag(wand_stack, WandProps.Flag.STAIRSLAB);
        match_state = WandProps.getFlag(wand_stack, WandProps.Flag.MATCHSTATE);
        inventory = new InventoryManager(Compat.get_inventory(player));
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

        // Copy and Paste modes don't need block_state - Copy calculates bbox from positions,
        // Paste uses copy_paste_buffer for block states
        boolean needsBlockState = mode != Mode.COPY && mode != Mode.PASTE;
        if ((needsBlockState && block_state == null) || pos == null || side == null || level == null || player == null || hit == null || wand_stack == null) {
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
        if (offhand != null && WandUtils.is_magicbag(offhand)) {
            ItemStack bag_item = MagicBagItem.getItem(offhand, level);
            int total = MagicBagItem.getTotal(offhand);
            if (!bag_item.isEmpty() && total > 0) {
                offhand_block = Block.byItem(bag_item.getItem());
                if (offhand_block != Blocks.AIR) {
                    has_offhand = true;
                }
            }
            offhand = null;
        }
        palette.item = null;
        palette.has_palette = false;
        has_bucket = false;
        has_water_bucket = false;
        has_lava_bucket = false;
        has_powder_snow_bucket = false;
        has_empty_bucket = false;
        if (offhand != null && offhand.getItem() instanceof PaletteItem) {
            palette.item = offhand;
            palette.has_palette = true;
            // Warn if gradient mode won't have an effect with current wand mode
            if (!level.isClientSide() && PaletteItem.getMode(offhand) == PaletteItem.PaletteMode.GRADIENT) {
                boolean gradientWorks = mode == Mode.FILL || mode == Mode.SPHERE ||
                    mode == Mode.BOX || mode == Mode.ROCK || mode == Mode.LINE;
                if (!gradientWorks) {
                    player.displayClientMessage(Compat.translatable("wands.message.gradient_no_effect"), true);
                }
            }
        }
        detect_bucket();
        this.has_water_potion = false;
        if (offhand != null && offhand.getItem() instanceof PotionItem) {

            this.has_water_potion=WandUtils.hasWaterPotion(offhand);

            if (!creative && has_water_potion) {
                int nbuckets = 0;
                for (int i = 0; i < 36; ++i) {
                    ItemStack stack = inventory.getItem(i);
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
        boolean has_torch = false;
        if (offhand != null) {
            offhand_block = Block.byItem(offhand.getItem());
            if (offhand_block != Blocks.AIR) {
                has_offhand = true;
            }
            has_torch = offhand_block instanceof TorchBlock;
        }
        if (offhand != null && !offhand.isEmpty() && !palette.has_palette && !has_bucket /*&& !destroy*/) {

        #if MC_VERSION >= 12005
            if (offhand.get(DataComponents.CUSTOM_DATA) != null) {
        #else
            if (offhand.getTag() != null) {
        #endif
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
            if (destroy && has_torch) {
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

        setup_bucket_block_state();

        inventory.update_inv_aux();
        inventory.resetBlocksSentToInv();
        //process the current mode
        int m = mode.ordinal();
        if (m >= 0 && m < modes.length && modes[m] != null) {
            //if (!preview) {
                //debug only
            //    player.displayClientMessage(Compat.literal("wand debug"), false);
            //}
            modes[m].place_in_buffer(this);
        }

        override_buffer_for_bucket();

        // Copy and Paste modes use global limit instead of wand limit
        int effectiveLimit = (mode == Mode.COPY || mode == Mode.PASTE) ? WandsConfig.max_limit : limit;

        //server stuff
        //WandsMod.log(" using palette seed: " + palette.seed,prnt);
        if (!preview) {
            //log(" using palette seed: " + palette_seed);
            if (limit_reached && (mode != Mode.VEIN)) {
                player.displayClientMessage(Compat.literal("wand limit reached"), false);
            }
            if (mode != Mode.BLAST) {
                if (palette.has_palette && !destroy && !use && !is_copy_paste) {
                    for (int a = 0; a < block_buffer.get_length() && a < effectiveLimit; a++) {
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
                            if (!validate_bucket_for_server()) return;
                        }

                        BlockAccounting pa = new BlockAccounting();
                        for (int a = 0; a < block_buffer.get_length() && a < effectiveLimit; a++) {
                            if (has_empty_bucket || has_water_bucket || has_lava_bucket || has_powder_snow_bucket) {
                                apply_bucket_accounting(pa, a);
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
                        for (int a = 0; a < block_buffer.get_length() && a < effectiveLimit; a++) {
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
            inventory.check_inventory(this);

            int placed = 0;
            no_use_action = false;

            // DO ACTIONS - process blocks that have matching tools, skip others
            {
                AABB bb = player.getBoundingBox();
                if (mode != Mode.COPY) {
                    for (int a = 0; a < block_buffer.get_length() && a < effectiveLimit; a++) {
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

                inventory.remove_from_inventory(this, placed);
                player.inventoryMenu.broadcastChanges();
            }
            //System.out.println("placed " + placed);
            if (no_use_action && placed == 0) {
                player.displayClientMessage(Compat.translatable("wands.message.no_use_action"), true);
                no_use_action = false;
            }
            if ((placed > 0) || (no_tool || damaged_tool)) {
                if (inventory.getBlocksSentToInv() > 0) {
                    Compat.player_level(player).playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.2f, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
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
                    Networking.sendSndPacket((ServerPlayer) player,pos, destroy, is, send_sound);

                }
                if (no_tool || damaged_tool) {
                    Networking.sendToastPacket((ServerPlayer) player,no_tool, damaged_tool, needed_tool);
                }
                no_tool = false;
                damaged_tool = false;
                needed_tool = "";
            }
        }
        // Clear P1/P2 after placement, but Copy mode keeps them for bbox extension
        if (getP2() != null && !(mode == WandProps.Mode.COPY && preview)) {
            setP1(null);
            setP2(null);
            valid = false;
        }
    }

    /** Inspects offhand for bucket items and sets bucket type flags. */
    private void detect_bucket() {
        if (offhand != null && (offhand.getItem() instanceof BucketItem || offhand.getItem() == Items.POWDER_SNOW_BUCKET)) {
            if (mode != Mode.DIRECTION) {
                // Show action bar message if bucket overrides destroy/replace (server-side only)
                if (!level.isClientSide() && (this.destroy || this.replace)) {
                    player.displayClientMessage(Compat.translatable("wands.message.bucket_blocks_destroy"), true);
                }
                bucket = offhand;
                has_bucket = true;
                has_powder_snow_bucket = bucket.getItem() == Items.POWDER_SNOW_BUCKET;
                has_empty_bucket = !has_powder_snow_bucket && bucket.isStackable();
                if (!has_empty_bucket && !has_powder_snow_bucket) {
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
    }

    /** Maps bucket type to the corresponding block_state for preview and placement. */
    private void setup_bucket_block_state() {
        if (has_bucket) {
            if (has_water_bucket) {
                block_state = Blocks.WATER.defaultBlockState();
            } else if (has_lava_bucket) {
                block_state = Blocks.LAVA.defaultBlockState();
            } else if (has_powder_snow_bucket) {
                block_state = Blocks.POWDER_SNOW.defaultBlockState();
            } else if (has_empty_bucket) {
                block_state = Blocks.AIR.defaultBlockState();
            }
        }
    }

    /** Overrides all buffer block states for bucket modes after place_in_buffer. */
    private void override_buffer_for_bucket() {
        if (has_water_bucket || has_lava_bucket || has_powder_snow_bucket || has_empty_bucket) {
            int len = block_buffer.get_length();
            for (int a = 0; a < len; a++) {
                block_buffer.state[a] = block_state;
            }
        }
    }

    /**
     * Re-validates bucket state on the server side before placement.
     * In creative mode, always allows. In survival, checks for a second water bucket.
     * Returns false if placement should be aborted.
     */
    private boolean validate_bucket_for_server() {
        has_bucket = false;
        if (has_water_bucket || has_lava_bucket || has_powder_snow_bucket) {
            if (creative) {
                has_bucket = true;
                if (has_water_bucket) {
                    block_state = Blocks.WATER.defaultBlockState();
                }
                if (has_lava_bucket) {
                    block_state = Blocks.LAVA.defaultBlockState();
                }
                if (has_powder_snow_bucket) {
                    block_state = Blocks.POWDER_SNOW.defaultBlockState();
                }
            } else {
                has_bucket = true;
                if (has_water_bucket) {
                    block_state = Blocks.WATER.defaultBlockState();
                }
                if (has_lava_bucket) {
                    block_state = Blocks.LAVA.defaultBlockState();
                }
                if (has_powder_snow_bucket) {
                    block_state = Blocks.POWDER_SNOW.defaultBlockState();
                }
            }
        }
        if (has_empty_bucket) {
            has_bucket = true;
            block_state = Blocks.AIR.defaultBlockState();
        }
        return true;
    }

    /** Sets buffer state/item for a single bucket entry and increments accounting for lava/powder snow. */
    private void apply_bucket_accounting(BlockAccounting pa, int a) {
        block_buffer.state[a] = block_state;
        if (has_lava_bucket || has_powder_snow_bucket) {
            block_buffer.item[a] = bucket.getItem();
            pa.needed++;
        }
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

    public BlockState get_state(int y, BlockState state_behind_block) {
        BlockState st = block_state;
        if (palette.has_palette) {
            int min_y = Compat.isInsideBuildHeight(level,block_buffer.min_y) ? block_buffer.min_y : 0;
            int max_y = Compat.isInsideBuildHeight(level,block_buffer.max_y) ? block_buffer.max_y : 0;

            st = palette.get_state(this, min_y, max_y, y);
        } else {
            if (offhand_state != null && !offhand_state.isAir()) {

                if (state_mode == WandProps.StateMode.CLONE && state_behind_block != null) {
                    return offhand_state.getBlock().withPropertiesOf(state_behind_block);
                } else {
                    st = offhand_state;
                }
            } else {
                if (mode == Mode.FILL || mode == Mode.LINE || mode == Mode.CIRCLE || mode == Mode.SPHERE) {
                    if (p1_state != null)
                        st = p1_state;
                }else{
                    if( state_mode== WandProps.StateMode.CLONE && state_behind_block!=null){
                        return state_behind_block;
                    }
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
        switch (state_mode) {
            case TARGET: {
                BlockHitResult hit_res = new BlockHitResult(hit, side, (bp != null ? bp : pos), true);
                BlockPlaceContext pctx = new BlockPlaceContext(player, InteractionHand.OFF_HAND, st.getBlock().asItem().getDefaultInstance(), hit_res);
                return st.getBlock().getStateForPlacement(pctx);
            }
            case APPLY:
            case APPLY_FLIP:
                if (blk instanceof SlabBlock) {
                    SlabType slab_type;
                    if (state_mode == WandProps.StateMode.APPLY) {
                        slab_type = SlabType.BOTTOM;
                    } else {
                        slab_type = SlabType.TOP;
                    }
                    return blk.defaultBlockState().setValue(SlabBlock.TYPE, slab_type);

                } else {
                    if (blk instanceof StairBlock) {
                        Half h;
                        if (state_mode == WandProps.StateMode.APPLY) {
                            h = Half.BOTTOM;
                        } else {
                            h = Half.TOP;
                        }
                        return blk.defaultBlockState().setValue(StairBlock.HALF, h).rotate(block_rotation);

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
            case CLONE: {
                //return st.getBlock().withPropertiesOf(st);
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
        // Pre-set min/max Y for gradient palette mode
        block_buffer.min_y = Math.min(ys, ye);
        block_buffer.max_y = Math.max(ys, ye);
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
                        add_to_buffer(x, y, z, null);
                        ll++;
                    }
                }
            }
        }
        skip();
    }

    void hurt_main_hand(ItemStack stack) {
        if (!this.unbreakable) {
            #if MC_VERSION< 12100
            stack.hurtAndBreak(1, player, (Consumer<LivingEntity>) ((p) -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND)));
            #else
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            #endif
        }
    }

    void hurt_tool(ItemStack stack, int tool_slot) {
        #if MC_VERSION< 12100
            stack.hurtAndBreak(1, player, (Consumer<LivingEntity>) ((p) -> p.broadcastBreakEvent(InteractionHand.OFF_HAND)));
        #else
            stack.hurtAndBreak(1, player, EquipmentSlot.OFFHAND);
        #endif
    }

    boolean place_block(BlockPos block_pos, BlockState state) {
        boolean placed = false;
        if (!WandsExpectPlatform.claimCanInteract((ServerLevel) level, block_pos, player)) {
            player.displayClientMessage(Compat.literal("can't use wand on claimed chunk"), false);
            return false;
        }
        if (state == null) {
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
        if (destroy && (mode != Mode.VEIN) && has_offhand && offhand_block != null && offhand_block != st.getBlock()) {
            // Action bar message: offhand block restricts destroy to matching blocks only
            player.displayClientMessage(Compat.translatable("wands.message.offhand_restricts_destroy"), true);
            return false;
        }

        //CommonPro
        int wand_durability = wand_stack.getMaxDamage() - wand_stack.getDamageValue();
        int tool_durability = -1;


        boolean _can_destroy = creative;
        no_tool = false;
        boolean _tool_would_break = false;
        boolean _wand_would_break = wand_would_break();
        if (!creative) {
            if (_wand_would_break) {
                damaged_tool = true;
                return false;
            }
            if (destroy || replace || (use && !has_water_potion)) {
                _can_destroy = can_destroy(st, true);
                if (digger_item != null) {
                    _tool_would_break = tool_would_break(digger_item);
                } else {
                    if (use && !WandUtils.has_use_action(st) && !can_shear(st)) {
                        no_use_action = true;
                        return false;
                    }
                    no_tool = true;
                    needed_tool = (n_tools > 0) ? getNeededToolType(st) : "";
                    return false;
                }
                if (_tool_would_break) {
                    damaged_tool = true;
                    return false;
                }
            }
        } else {
            if (creative && use) {
                can_destroy(st, true);
                if (digger_item == null && !has_water_potion) {
                    if (!WandUtils.has_use_action(st) && !can_shear(st)) {
                        no_use_action = true;
                        return false;
                    }
                    no_tool = true;
                    needed_tool = (n_tools > 0) ? getNeededToolType(st) : "";
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
                if (!_wand_would_break) {
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
                if (!_wand_would_break) {
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
                if (!creative) {
                    if (!_wand_would_break) {
                        hurt_main_hand(wand_stack);
                    }
                    if (!_tool_would_break) {
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
                            state = state_for_placement(state, block_pos);
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
                        if (this.allow_wand_to_break &&
                                digger_item != null && digger_item.getItem() == Items.AIR
                        ) {

                        } else {
                            stop = true;
                        }
                    }
                }

                if (placed) {
                    if ((destroy || replace) && digger_item != null && !_tool_would_break) {
                        hurt_tool(digger_item, digger_item_slot);
                    }
                    if (!this.unbreakable && !_wand_would_break) {
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
            if (!inventory.place_into(this, drop_item)) {
                if (!this.stop) {
                    Block.popResource(level, drop_pos, drop_item);
                }
            }
        } else {
            Block.getDrops(blockState, (ServerLevel) level, blockPos, blockEntity, player, digger_item).forEach(
                    (itemStackx) -> {
                        //player.displayClientMessage(Compat.literal(itemStackx.getDescriptionId()),false);
                        if (!inventory.place_into(this, itemStackx)) {
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
                #if MC_VERSION >= 12111
                if (((ServerLevel) level).getGameRules().get(GameRules.BLOCK_DROPS))
                #else
                if (level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS))
                #endif
                {
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


    public boolean add_to_buffer(int x, int y, int z) {
        return add_to_buffer(x, y, z, null);
    }

    public boolean add_to_buffer(int x, int y, int z, BlockState with_state) {
        if (!level.isOutsideBuildHeight(y) && block_buffer.get_length() < limit) {
            BlockState st = level.getBlockState(tmp_pos.set(x, y, z));
            if (destroy || replace || use) {
                if (!st.isAir() || mode == Mode.AREA) {
                    return block_buffer.add(x, y, z, this, with_state);
                }
            } else {
                if (can_place(st, tmp_pos))
                    return block_buffer.add(x, y, z, this, with_state);
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
        has_pickaxe = false;
        has_hoe = false;
        has_shovel = false;
        has_axe = false;
        has_shear = false;  

        if (this.player_data == null) {
            return;
        }
        #if MC_VERSION >= 12102
        Optional<int[]> a = this.player_data.getIntArray("Tools");
        if (a.isEmpty()) {
            return;
        }
        int[] tools_slots = a.get();
        #else
        int[] tools_slots = this.player_data.getIntArray("Tools");
        #endif
        for (int t = 0; t < tools.length; t++) {
            tools[t].empty = true;
            tools[t].tool = null;
        }
        for (int t = 0; t < tools_slots.length; t++) {
            int slot = tools_slots[t];
            tools[slot].empty = true;
            ItemStack tool_item = player.getInventory().getItem(slot);
            tools[slot].tool = tool_item;
            if (tool_item.isEmpty()) continue;
            tools[slot].empty = false;
            Item item = tool_item.getItem();
            if (tool_item.is(ItemTags.PICKAXES) || WandsConfig.extra_pickaxes_list.contains(item)) {
                has_pickaxe = true;
                tools[slot].tooltype = ToolType.PICKAXE;
            }
            if (tool_item.is(ItemTags.HOES) || WandsConfig.extra_hoes_list.contains(item)) {
                has_hoe = true;
                tools[slot].tooltype = ToolType.HOE;
            }
            if (tool_item.is(ItemTags.SHOVELS) || WandsConfig.extra_shovels_list.contains(item)) {
                has_shovel = true;
                tools[slot].tooltype = ToolType.SHOVEL;
            }
            if (tool_item.is(ItemTags.AXES) || WandsConfig.extra_axes_list.contains(item)) {
                has_axe = true;
                tools[slot].tooltype = ToolType.AXE;
            }
            if (item instanceof ShearsItem || WandsConfig.extra_shears_list.contains(item)) {
                has_shear = true;
                tools[slot].tooltype = ToolType.SHEAR;
            }
            n_tools++;
        }
    }

    public boolean can_destroy(BlockState state, boolean check_speed) {
        digger_item = null;
        WandItem wand_item=(WandItem)this.wand_stack.getItem();
        for (int i = 0; i < tools.length; i++) {
            if (!tools[i].empty && tools[i] != null) {
                if (!tool_would_break(tools[i].tool)) {
                    if (((destroy || replace) && can_dig(state, check_speed, tools[i].tool)) ||
                            ((use) && (
                                    (tools[i].tooltype == ToolType.HOE && WandUtils.is_tillable(state)) ||
                                    (tools[i].tooltype == ToolType.AXE && WandUtils.is_strippable(state)) ||
                                    (tools[i].tooltype == ToolType.SHOVEL && WandUtils.is_flattenable(state)) ||
                                    (tools[i].tooltype == ToolType.SHEAR && can_shear(state))
                            ))
                    ) {
                        if (digger_item == null) {
                            digger_item = tools[i].tool;
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
        if (digger == null) {
            return false;
        }
        #if MC_VERSION>=12100
        boolean is_glass = state.getBlock() instanceof TransparentBlock;
        #else
        boolean is_glass = state.getBlock() instanceof GlassBlock;
        #endif
        boolean is_snow_layer = false;
        boolean can_shear = false;
        Block blk = state.getBlock();
        if (blk instanceof SnowLayerBlock) {
            is_snow_layer = state.getValue(SnowLayerBlock.LAYERS) == 1;
        }
        Item item_digger = digger.getItem();
        #if MC_VERSION>=12005
            boolean is_tool = digger.get(DataComponents.TOOL)  != null ;
        #else
            boolean is_tool=item_digger instanceof DiggerItem ;
        #endif
        if (digger != null && !digger.isEmpty() && (is_tool || item_digger instanceof ShearsItem)) {
            boolean is_allowed = false;
            boolean minable = false;
            if (item_digger instanceof ShearsItem) {
                can_shear = can_shear(state);
                is_allowed = WandsConfig.shears_allowed.contains(blk);
            } else {
                if (item_digger instanceof AxeItem) {
                    is_allowed = WandsConfig.axe_allowed.contains(blk);
                } else {
                    if (item_digger instanceof ShovelItem) {
                        is_allowed = WandsConfig.shovel_allowed.contains(blk);
                    } else {
                        if (item_digger instanceof HoeItem) {
                            is_allowed = WandsConfig.hoe_allowed.contains(blk);
                        }else{
                            //TODO: find a new way to check if it's a pickaxe
                            is_allowed = WandsConfig.pickaxe_allowed.contains(blk);
                        }
                    }
                }
            }
            if (check_speed) {
                float destroy_speed = item_digger.getDestroySpeed(digger, state);
                boolean correct_tool = false;
                if (!digger.isEmpty()) {
                    #if MC_VERSION>=12005
                        Tool dtool = digger.get(DataComponents.TOOL);
                        correct_tool = dtool != null && dtool.isCorrectForDrops(state);
                    #else
                        correct_tool = item_digger.isCorrectToolForDrops(state);
                    #endif
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
#if MC_VERSION>=12100   state.is(Blocks.SHORT_GRASS) || #endif
                        state.is(Blocks.FERN) ||
                        state.is(Blocks.DEAD_BUSH) ||
                        state.is(Blocks.VINE) ||
                        state.is(Blocks.TRIPWIRE) ||
                        state.is(Blocks.PUMPKIN) ||
                        state.is(BlockTags.WOOL) ||
                        state.is(Blocks.HANGING_ROOTS)
        );
    }

    boolean check_advancement(ServerAdvancementManager server_advancements, PlayerAdvancements player_advancements, String a) {

        adv_id.tryParse(a);

        if (adv_id.res == null) {
            WandsMod.log("bad advancement: " + adv_id.res, prnt);
            return false;
        }
        #if MC_VERSION >=12100
        AdvancementHolder adv = server_advancements.get(adv_id.res);
        #else
        Advancement adv = server_advancements.getAdvancement(adv_id.res);
        #endif
        if (adv == null) {
            WandsMod.log("bad advancement: " + adv_id.res, prnt);
            return false;
        }
        AdvancementProgress prog = player_advancements.getOrStartProgress(adv);
        return prog.isDone();
    }

    void check_advancements() {
        if (!creative && WandsMod.config.check_advancements && !level.isClientSide()) {
            PlayerAdvancements advs = ((ServerPlayer) player).getAdvancements();

            MinecraftServer server = player.level().getServer();
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
        float r = player.getYRot();
        Vec3 eye = player.getEyePosition();
        hit = hit.add(hit.subtract(eye).normalize().scale(target_air_distance));
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

    private boolean tool_would_break(ItemStack tool) {
        if (tool == null) {
            return true;
        }
        int dmg = tool.getMaxDamage() - tool.getDamageValue();
        boolean would_break = dmg <= TOOL_DAMAGE_STOP;
        Item tool_item = tool.getItem();
        if (WandsMod.config.allow_wooden_tools_to_break) {
            if (Items.WOODEN_PICKAXE.getDefaultInstance().is(tool_item) ||
                    Items.WOODEN_AXE.getDefaultInstance().is(tool_item) ||
                    Items.WOODEN_SHOVEL.getDefaultInstance().is(tool_item) ||
                    Items.WOODEN_HOE.getDefaultInstance().is(tool_item)
            ) {
                return false;
            }
        }
        if (WandsMod.config.allow_stone_tools_to_break) {
            if (Items.STONE_PICKAXE.getDefaultInstance().is(tool_item) ||
                    Items.STONE_AXE.getDefaultInstance().is(tool_item) ||
                    Items.STONE_SHOVEL.getDefaultInstance().is(tool_item) ||
                    Items.STONE_HOE.getDefaultInstance().is(tool_item)
            ) {
                return false;
            }
        }
        #if MC_VERSION>=12109
        if (WandsMod.config.allow_copper_tools_to_break) {
            if (Items.COPPER_PICKAXE.getDefaultInstance().is(tool_item) ||
                    Items.COPPER_AXE.getDefaultInstance().is(tool_item) ||
                    Items.COPPER_SHOVEL.getDefaultInstance().is(tool_item) ||
                    Items.COPPER_HOE.getDefaultInstance().is(tool_item)
            ) {
                return false;
            }
        }
        #endif
        if (WandsMod.config.allow_iron_tools_to_break) {
            if (Items.IRON_PICKAXE.getDefaultInstance().is(tool_item) ||
                    Items.IRON_AXE.getDefaultInstance().is(tool_item) ||
                    Items.IRON_SHOVEL.getDefaultInstance().is(tool_item) ||
                    Items.IRON_HOE.getDefaultInstance().is(tool_item)
            ) {
                return false;
            }
        }
        if (WandsMod.config.allow_diamond_tools_to_break) {
            if (Items.DIAMOND_PICKAXE.getDefaultInstance().is(tool_item) ||
                    Items.DIAMOND_AXE.getDefaultInstance().is(tool_item) ||
                    Items.DIAMOND_SHOVEL.getDefaultInstance().is(tool_item) ||
                    Items.DIAMOND_HOE.getDefaultInstance().is(tool_item)
            ) {
                return false;
            }
        }
        if (WandsMod.config.allow_netherite_tools_to_break) {
            if (Items.NETHERITE_PICKAXE.getDefaultInstance().is(tool_item) ||
                    Items.NETHERITE_AXE.getDefaultInstance().is(tool_item) ||
                    Items.NETHERITE_SHOVEL.getDefaultInstance().is(tool_item) ||
                    Items.NETHERITE_HOE.getDefaultInstance().is(tool_item)
            ) {
                return false;
            }
        }
        return would_break;
    }

    private boolean wand_would_break() {
        if (this.wand_stack == null) {
            return true;
        }
        this.allow_wand_to_break = false;
        int dmg = this.wand_stack.getMaxDamage() - this.wand_stack.getDamageValue();
        WandItem wand_item = (WandItem) this.wand_stack.getItem();
        if (dmg <= 1) {
            switch (wand_item.tier) {
                case STONE_WAND:
                    this.allow_wand_to_break = WandsMod.config.allow_stone_wand_to_break;
                    return !this.allow_wand_to_break;
                case COPPER_WAND:
                    this.allow_wand_to_break = WandsMod.config.allow_copper_wand_to_break;
                    return !this.allow_wand_to_break;
                case IRON_WAND:
                    this.allow_wand_to_break = WandsMod.config.allow_iron_wand_to_break;
                    return !this.allow_wand_to_break;
                case DIAMOND_WAND:
                    this.allow_wand_to_break = WandsMod.config.allow_diamond_wand_to_break;
                    return !this.allow_wand_to_break;
                case NETHERITE_WAND:
                    this.allow_wand_to_break = WandsMod.config.allow_netherite_wand_to_break;
                    return !this.allow_wand_to_break;
            }
        }
        if (wand_item.unbreakable) {
            return false;
        }
        return dmg <= 1;
    }

    private String getNeededToolType(BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return "pickaxe";
        } else if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return "shovel";
        } else if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
            return "axe";
        } else if (state.is(BlockTags.MINEABLE_WITH_HOE)) {
            return "hoe";
        }
        return "";
    }
    /**
     * Check if any active block source (offhand or palette) contains a RotatedPillarBlock.
     * Used for determining whether to show axis-specific rotation options.
     * @return true if any pillar block is available for placement
     */
    public boolean hasPillarBlock() {
        // Check palette first
        if (palette.has_palette && !palette.palette_slots.isEmpty()) {
            for (Palette.PaletteSlot slot : palette.palette_slots) {
                if (slot.state != null && slot.state.getBlock() instanceof RotatedPillarBlock) {
                    return true;
                }
            }
        }
        // Check offhand
        if (offhand_state != null && offhand_state.getBlock() instanceof RotatedPillarBlock) {
            return true;
        }
        return false;
    }
}
