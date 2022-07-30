package net.nicguzzo.wands;

import java.util.*;
import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
//import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nicguzzo.wands.PaletteItem.PaletteMode;
import net.nicguzzo.wands.WandItem.Orientation;
import net.nicguzzo.wands.WandItem.Mode;
import net.nicguzzo.wands.mcver.MCVer;

#if MC>="1190"
    import net.minecraft.util.RandomSource;
#endif

public class Wand {
    public int x = 0;
    public int y = 0;
    public int z = 0;
    public int x1 = 0;
    public int y1 = 0;
    public int z1 = 0;
    public int x2 = 0;
    public int y2 = 0;
    public int z2 = 0;
    public int bb1_x=0;
    public int bb1_y=0;
    public int bb1_z=0;
    public int bb2_x=0;
    public int bb2_y=0;
    public int bb2_z=0;

    public BlockPos p1 = null;
    public boolean p2 = false;
    public BlockState p1_state = null;
    public BlockHitResult lastHitResult=null;

    public boolean valid = false;
    public static final int MAX_UNDO = 2048;
    public static final int MAX_LIMIT = 16536;
    Player player;
    Level level;
    BlockState block_state;
    BlockState offhand_state = null;
    Block offhand_block = null;
    BlockPos pos;
    public Direction side = Direction.UP;
    Vec3 hit;
    WandItem wand_item;
    ItemStack wand_stack;
    ItemStack offhand;
    ItemStack digger_item;
    int digger_item_slot=0;
    public float y0 = 0.0f;
    public float block_height = 1.0f;
    boolean is_stair = false;
    boolean is_slab_top = false;
    boolean is_slab_bottom = false;
    boolean is_alt_pressed = false;
    boolean is_shift_pressed = false;
    public boolean replace;
    public boolean destroy;
    public boolean use;
    boolean stop = false;
    ItemStack bucket = null;
    public boolean is_double_slab = false;
    public int grid_voxel_index = 0;
    public ItemStack palette = null;
    public boolean has_palette = false;
    boolean has_bucket = false;
    boolean has_water_bucket = false;
    boolean has_lava_bucket  = false;
    boolean has_empty_bucket = false;
    boolean has_offhand = false;
    public boolean has_hoe=false;
    public boolean has_shovel=false;
    public boolean has_axe=false;
    public boolean has_shear=false;
    public boolean force_render = false;
    public boolean limit_reached=false;
    public WandItem.Plane plane=WandItem.Plane.XZ;
    public Direction.Axis axis=Direction.Axis.Y;
    public Rotation rotation;
    public WandItem.StateMode state_mode =WandItem.StateMode.CLONE;
    private boolean no_tool;
    private boolean damaged_tool;
    public boolean match_state=false;

    public static class PaletteSlot {
        public ItemStack stack;
        public BlockState state;
        public int slot;

        PaletteSlot(int s, BlockState b, ItemStack stk) {
            slot = s;
            state = b;
            stack = stk;
        }
    }
    ItemStack[] tools=new ItemStack[9];

    private static class BlockAccounting {
        public int placed = 0;
        public int needed = 0;
        public int in_player = 0;
    }

    public int slot = 0;
    #if MC>="1190"
    public RandomSource random = RandomSource.create();
    #else
    public Random random = new Random();
    #endif
    public volatile long palette_seed = System.currentTimeMillis();
    public Vector<PaletteSlot> palette_slots = new Vector<>();
    public Map<Item, BlockAccounting> block_accounting = new HashMap<>();
    public BlockBuffer block_buffer = new BlockBuffer(MAX_LIMIT);
    public CircularBuffer undo_buffer = new CircularBuffer(MAX_UNDO);

    private final BlockPos.MutableBlockPos tmp_pos = new BlockPos.MutableBlockPos();

    int MAX_COPY_VOL = 20 * 20 * 20;
    int radius=0;

    static class CopyPasteBuffer {
        public BlockPos pos;
        public BlockState state;

        public CopyPasteBuffer(BlockPos pos, BlockState state) {
            this.pos = pos;
            this.state = state;
        }
    }

    Vector<CopyPasteBuffer> copy_paste_buffer = new Vector<>();
    public BlockPos copy_pos1 = null;
    public BlockPos copy_pos2 = null;
    public int copy_x1 = 0;
    public int copy_y1 = 0;
    public int copy_z1 = 0;
    public int copy_x2 = 0;
    public int copy_y2 = 0;
    public int copy_z2 = 0;
    boolean preview;
    boolean creative=true;
    public WandItem.Mode mode;
    boolean prnt = false;
    public int limit=MAX_LIMIT;
    Inventory player_inv;
    public boolean slab_stair_bottom=true;

    //private void log(String s) {
        //WandsMod.log(s, prnt);
    //}
    static boolean once=true;
    public void clear() {
        p1 = null;
        p1_state = null;
        valid = false;
        block_height = 1.0f;
        y0 = 0.0f;

        /*if (player != null && player.level!=null && mode==Mode.COPY) {
            copy_pos1 = null;
            copy_pos2 = null;
            copy_paste_buffer.clear();
            if(!player.level.isClientSide()) {
                player.displayClientMessage(MCVer.inst.literal("Copied blocks cleared").withStyle(ChatFormatting.GREEN), false);
            }
        }*/
    }

    boolean check_advancement(ServerAdvancementManager server_advancements,PlayerAdvancements player_advancements,String a){
        ResourceLocation res=ResourceLocation.tryParse(a);
        if(res==null){
            WandsMod.log("bad advancement: "+res,prnt);
            return false;
        }
        Advancement adv= server_advancements.getAdvancement(res);
        if(adv==null){
            WandsMod.log("bad advancement: "+res,prnt);
            return false;
        }
        AdvancementProgress prog=player_advancements.getOrStartProgress(adv);
        return prog.isDone();
    }

    public void do_or_preview(
            Player player,
            Level level,
            BlockState block_state,
            BlockPos pos,
            Direction side,
            Vec3 hit,
            ItemStack wand_stack,
            boolean prnt) {
        //if(!use_previous) {
            this.player = player;
            this.level = level;
            this.block_state = block_state;
            this.pos = pos;
            this.side = side;
            this.hit = hit;
            this.wand_stack = wand_stack;
            this.prnt = prnt;
       // }
        if(this.player==null || this.level==null||this.pos==null
                || this.side==null||this.hit==null||this.wand_stack==null){
            return;
        }
        //if(use_previous && block_buffer.get_length()>0){
//            return;
//        }
        creative = MCVer.inst.is_creative(player);
        if(!creative && WandsMod.config.check_advancements && !level.isClientSide()) {
            PlayerAdvancements advs=((ServerPlayer)player).getAdvancements();
            ServerAdvancementManager advancements=player.getServer().getAdvancements();

            if(!Objects.equals(WandsMod.config.advancement_allow_stone_wand, "") && ((TieredItem )(wand_stack.getItem())).getTier()==Tiers.STONE ) {
                if(!check_advancement(advancements, advs, WandsMod.config.advancement_allow_stone_wand)){
                    return;
                }
            }
            if(!Objects.equals(WandsMod.config.advancement_allow_iron_wand, "") && ((TieredItem )(wand_stack.getItem())).getTier()==Tiers.IRON ) {
                if(!check_advancement(advancements, advs, WandsMod.config.advancement_allow_iron_wand)){
                    return;
                }
            }
            if(!Objects.equals(WandsMod.config.advancement_allow_diamond_wand, "") && ((TieredItem )(wand_stack.getItem())).getTier()==Tiers.DIAMOND ) {
                if(!check_advancement(advancements, advs, WandsMod.config.advancement_allow_diamond_wand)){
                    //WandsMod.log("need advancement: "+WandsMod.config.advancement_allow_diamond_wand,prnt);
                    return;
                }
            }
            if(!Objects.equals(WandsMod.config.advancement_allow_netherite_wand, "") && ((TieredItem )(wand_stack.getItem())).getTier()==Tiers.NETHERITE ) {
                if(!check_advancement(advancements, advs, WandsMod.config.advancement_allow_netherite_wand)){
                    return;
                }
            }
            //WandsMod.log("advancement_allow_diamond_wand: "+WandsMod.config.advancement_allow_diamond_wand,prnt);
            //WandsMod.log("prog: "+prog.isDone(),prnt);
            //AdvancementList.advancements.get(ResourceLocation.tryParse(WandsMod.config.advancement_allow_diamond_wand));
        }

        mode = WandItem.getMode(wand_stack);
        axis= WandItem.getAxis(wand_stack);
        plane= WandItem.getPlane(wand_stack);
        rotation= WandItem.getRotation(wand_stack);
        state_mode= WandItem.getStateMode(wand_stack);
        slab_stair_bottom=WandItem.getStairSlab(wand_stack);
        match_state=WandItem.getMatchState(wand_stack);
        player_inv= MCVer.inst.get_inventory(player);
        y0 = 0.0f;
        block_height = 1.0f;
        is_slab_top = false;
        is_double_slab = false;
        is_slab_bottom = false;
        is_stair = false;
        preview = level.isClientSide();
        offhand_state = null;
        stop = false;
        radius=0;
        limit_reached=false;
        random.setSeed(palette_seed);


        if (block_state == null || pos == null || side == null || level == null || player == null || hit == null || wand_stack == null) {
            return;
        }

        if(once){
            once=false;
            WandsMod.config.generate_lists();
        }
        //Done paste should respect modes place replace destroy
        //TODO support tags in allow/deny list
        //TODO palette pattern mode
        //TODO banner placement not working on sides

        wand_item = (WandItem) wand_stack.getItem();
        limit = wand_item.limit;
        if(limit>MAX_LIMIT){
            limit=MAX_LIMIT;
        }

        boolean is_copy_paste = mode == Mode.COPY || mode == Mode.PASTE;

        valid = false;
        this.replace=WandItem.getAction(wand_stack)== WandItem.Action.REPLACE;
        this.destroy=WandItem.getAction(wand_stack)== WandItem.Action.DESTROY;
        this.use=WandItem.getAction(wand_stack)== WandItem.Action.USE;

        offhand = player.getOffhandItem();
        has_offhand = false;
        has_hoe    = false;
        has_shovel = false;
        has_axe    = false;
        update_tools();
        if(use) {
            for (int i = 0; i < tools.length; i++) {
                int dmg=tools[i].getMaxDamage()- tools[i].getDamageValue();
                if(tools[i]!=null && dmg>1) {
                    Item item=tools[i].getItem();
                    has_hoe = (has_hoe || item instanceof HoeItem);
                    has_shovel = has_shovel || item instanceof ShovelItem;
                    has_axe = has_axe || item instanceof AxeItem;
                    has_shear = has_shear || item instanceof ShearsItem;
                }
            }
        }
        if (offhand != null && WandUtils.is_shulker(offhand)) {
            offhand = null;
        }
        palette = null;
        has_palette = false;
        has_bucket = false;
        has_water_bucket=false;
        has_lava_bucket=false;
        has_empty_bucket=false;
        if (offhand != null && offhand.getItem() instanceof PaletteItem) {
            //if (mode != Mode.DIRECTION)
            {
                palette = offhand;
                has_palette = true;
            }
        }
        if (offhand != null && offhand.getItem() instanceof BucketItem) {
            if (mode != Mode.DIRECTION) {
                bucket = offhand;
                has_bucket = true;
                has_empty_bucket=bucket.isStackable();
                //Item itt=Fluids.EMPTY.getBucket();
                //BucketItem
                //has_empty_bucket=bucket.getItem().equals(Fluids.EMPTY.getBucket());
                //TODO: other mods fluids?
                if(!has_empty_bucket) {
                    has_water_bucket = bucket.getItem().equals(Fluids.WATER.getBucket());
                    if (!has_water_bucket) {
                        has_lava_bucket = bucket.getItem().equals(Fluids.LAVA.getBucket());
                    }
                }
                this.replace=false;
                this.destroy=false;
                this.use=false;
            }
        }

        //item_stack = Item.byBlock(block_state.getBlock()).getDefaultInstance();

        if (offhand != null) {
            offhand_block = Block.byItem(offhand.getItem());
            if (offhand_block != Blocks.AIR) {
                has_offhand = true;
            }
        }
        if (offhand != null && !has_palette && !has_bucket && !destroy) {
            if (offhand.getTag() != null) {
                /*if (!preview) {
                    player.displayClientMessage(new TextComponent("Wand offhand can't have tag! ").withStyle(ChatFormatting.RED), false);
                }*/
                offhand = null;
                has_offhand =false;
                offhand_block=null;
                //return;
            }
            if (offhand != null && !offhand.isStackable()) {
                if (!preview) {
                    player.displayClientMessage(MCVer.inst.literal("Wand offhand must be stackable! ").withStyle(ChatFormatting.RED), false);
                }
                offhand = null;
                has_offhand =false;
                offhand_block=null;
                return;
            }
        }
        block_accounting.clear();
        if (has_palette /*&& !destroy && !is_copy_paste*/) {
            update_palette();
        }

        if (!has_palette && !has_bucket) {
            if (offhand_block != null && Blocks.AIR != offhand_block) {
                offhand_state = offhand_block.defaultBlockState();
            }
        }
        if(replace && (mode != Mode.PASTE) && !has_palette && (Blocks.AIR == offhand_block || offhand_block==null )){
            valid=false;
            if (!preview) {
                player.displayClientMessage(MCVer.inst.literal("you need a block or palette in the left hand"), false);
            }
            return;
        }
        
        switch (mode) {
            case DIRECTION: {
                boolean invert = WandItem.isInverted(wand_stack);
                mode_direction(invert,WandItem.getMultiplier(wand_stack));
            }break;
            case ROW_COL: {
                Orientation orientation = WandItem.getOrientation(wand_stack);
                mode_row_col(orientation,WandItem.getRowColLimit(wand_stack));
            }break;
            case FILL: mode_fill_rect(false); break;
            case AREA: mode_area(); break;
            case GRID: mode_grid(); break;
            case LINE: mode_line(); break;
            case CIRCLE: {
                int plane = WandItem.getPlane(wand_stack).ordinal();
                boolean fill = WandItem.isCircleFill(wand_stack);
                mode_circle(plane, fill);
            }break;
            case RECT: mode_fill_rect(true); break;
            case COPY: mode_copy(); break;
            case PASTE: mode_paste(); break;
            case BLAST: mode_blast(); break;
        }

        if (!preview) {
            //log(" using palette seed: " + palette_seed);
            if(limit_reached){
                player.displayClientMessage(MCVer.inst.literal("wand limit reached"),false);
            }
            //log("has_palette: "+has_palette);
            if(mode!=Mode.BLAST) {
                if (has_palette && !destroy && !use && !is_copy_paste) {
                    //log("palette: "+palette_slots);
                    //log("block_accounting: "+ block_accounting);

                    for (int a = 0; a < block_buffer.get_length() && a < limit && a < MAX_LIMIT; a++) {
                        if (!replace && !can_place(player.level.getBlockState(block_buffer.get(a)),block_buffer.get(a))) {
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
                            //log("bucket " + bucket);
                            //boolean is_water_bucket=bucket.is(Fluids.WATER.getBucket()));
                            //boolean is_water_bucket=bucket.getItem().equals(Fluids.WATER.getBucket());
                            if (has_water_bucket || has_lava_bucket) {
                                //log("bucket is water");
                                if (creative) {
                                    has_bucket = true;
                                    if (has_water_bucket) {
                                        block_state = Blocks.WATER.defaultBlockState();
                                    }
                                    if (has_lava_bucket) {
                                        block_state = Blocks.LAVA.defaultBlockState();
                                    }
                                    //block_state = Blocks.WATER.defaultBlockState();
                                } else {
                                    if (has_water_bucket) {
                                        //in survival check if player has another water bucket part from the one in the offhand
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
                                            player.displayClientMessage(MCVer.inst.literal("You need another water bucket in the inventory."), false);
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
                        for (int a = 0; a < block_buffer.get_length() && a < limit && a < MAX_LIMIT; a++) {
                            if (has_empty_bucket || has_water_bucket || has_lava_bucket) {
                                block_buffer.state[a] = block_state;
                                if (has_lava_bucket) {
                                    block_buffer.item[a] = bucket.getItem();
                                    pa.needed++;
                                }
                            } else {
                                if (!replace && !destroy && !use && !can_place(player.level.getBlockState(block_buffer.get(a)),block_buffer.get(a))) {
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
                        for (int a = 0; a < block_buffer.get_length() && a < limit && a < MAX_LIMIT; a++) {
                            if (!replace && !destroy && !can_place(player.level.getBlockState(block_buffer.get(a)),block_buffer.get(a))) {
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
            if ((!creative||mode == Mode.BLAST) && !destroy && !use && !has_water_bucket && mode != Mode.COPY) {
                ItemStack stack;
                for (int i = 0; i < 36; ++i) {
                    stack = player_inv.getItem(i);
                    if(stack.getItem()!= Items.AIR) {
                        if (WandUtils.is_shulker(stack)) {
                            for (Map.Entry<Item, BlockAccounting> pa : block_accounting.entrySet()) {
                                pa.getValue().in_player += WandUtils.count_in_shulker(stack, pa.getKey());
                            }
                        } else {
                            if (stack.getTag() == null) {
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
                if (offhand != null && !offhand.isEmpty()) {
                    BlockAccounting pa = block_accounting.get(offhand.getItem());
                    if (pa != null) {
                        pa.in_player += offhand.getCount();
                    }
                }
                for (Map.Entry<Item, BlockAccounting> pa : block_accounting.entrySet()) {
                    if (pa.getValue().in_player < pa.getValue().needed) {
                        MutableComponent name=MCVer.inst.translatable(pa.getKey().getDescriptionId());
                        MutableComponent mc = MCVer.inst.literal("Not enough ").withStyle(ChatFormatting.RED).append(name);
                        mc.append(". Needed: " + pa.getValue().needed);
                        mc.append(" player: " + pa.getValue().in_player);
                        player.displayClientMessage(mc, false);
                        //missing_blocks = true;
                    }
                    //log(pa.getKey().getDescriptionId()+" needed: "+pa.getValue().needed+" player has: "+pa.getValue().in_player);
                }
            }
            //log("block_buffer.length: " + block_buffer.get_length());
            //log("wand limit: " + wand_item.limit);
            //log( "limit "+limit);
            //log("missing_blocks " + missing_blocks);
            int placed = 0;

            //if (!missing_blocks || destroy || has_bucket)
            {
                AABB bb = player.getBoundingBox();
                for (int a = 0; a < block_buffer.get_length() && a < limit && a < MAX_LIMIT; a++) {
                    tmp_pos.set(block_buffer.buffer_x[a], block_buffer.buffer_y[a], block_buffer.buffer_z[a]);
                    if (!destroy && !has_bucket && !use){
                        if(bb.intersects(tmp_pos.getX(), tmp_pos.getY(), tmp_pos.getZ(), tmp_pos.getX() + 1, tmp_pos.getY() + 1, tmp_pos.getZ() + 1)) {
                            continue;
                        }
                        boolean pp=false;
                        for(Player pl: player.level.players()){
                            if(pl.getBoundingBox().intersects(tmp_pos.getX(), tmp_pos.getY(), tmp_pos.getZ(), tmp_pos.getX() + 1, tmp_pos.getY() + 1, tmp_pos.getZ() + 1)) {
                                pp=true;
                                break;
                            }
                        }
                        if(pp){
                            continue;
                        }
                    }
                    Item item = block_buffer.item[a];
                    BlockAccounting pa = null;
                    if (item != null) {
                        pa = block_accounting.get(item);
                    }
                    if ((destroy||use||creative||has_bucket||(pa != null && pa.placed<pa.in_player)) ){
                        if(place_block(tmp_pos, block_buffer.state[a])) {
                            if (pa != null)
                                pa.placed++;
                            placed++;
                        }
                    }
                    if (stop) {
                        break;
                    }
                }
                if(mode==Mode.BLAST && wand_item.can_blast) {
                    boolean do_explode=false;
                    BlockAccounting ba=null;
                    if(creative) {
                        do_explode=true;
                    }else{
                        ba=block_accounting.get(Items.TNT);
                        if(ba!=null) {
                            do_explode = (ba.needed <= ba.in_player);
                        }
                    }
                    if(do_explode) {
                        float radius=(float)WandItem.getBlastRadius(wand_stack);
                        float eo=1.0625f;
                        if(side==Direction.DOWN){
                            eo=0.0625f;
                        }
                        //Explosion.BlockInteraction bi=(creative?Explosion.BlockInteraction.DESTROY: Explosion.BlockInteraction.BREAK);
                        this.level.explode(player, pos.getX(), pos.getY() + eo, pos.getZ(), radius, Explosion.BlockInteraction.BREAK);
                        if(!creative && ba!=null){
                            ba.placed=ba.needed;
                        }
                    }
                }
                if (!creative && ((!destroy && placed > 0)||mode==Mode.BLAST)) {
                    ItemStack stack;
                    ItemStack stack_item;
                    //look for items on shulker boxes first
                    for (int pi = 0; pi < 36; ++pi) {
                        stack = player_inv.getItem(pi);
                        if(stack.getItem() != Items.AIR) {
                            if (WandUtils.is_shulker(stack)) {
                                CompoundTag shulker_tag = stack.getTagElement("BlockEntityTag");
                                if (shulker_tag != null) {
                                    ListTag shulker_items = shulker_tag.getList("Items", 10);
                                    for (int j = 0, len = shulker_items.size(); j < len; ++j) {
                                        CompoundTag itemTag = shulker_items.getCompound(j);
                                        stack_item = ItemStack.of(itemTag);
                                        if (stack_item != null && !stack_item.isEmpty() && stack_item.getTag() == null ) {
                                            BlockAccounting pa = block_accounting.get(stack_item.getItem());
                                            ItemStack rep=consume_item(pa,stack_item);
                                            if(rep!=null){
                                                if(!rep.isEmpty()) {
                                                    CompoundTag stackTag = rep.save(new CompoundTag());
                                                    stackTag.putByte("Slot",(byte)j);
                                                    shulker_items.set(j, stackTag);
                                                }else {
                                                    shulker_items.set(j, stack_item.save(itemTag));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //now look for items on player inv
                    for (int i = 0; i < 36; ++i) {
                        stack_item = player_inv.getItem(i);
                        if(stack_item.getItem() != Items.AIR) {
                            if (!WandUtils.is_shulker(stack_item) && stack_item.getTag()==null) {
                                BlockAccounting pa = block_accounting.get(stack_item.getItem());
                                ItemStack rep=consume_item(pa,stack_item);
                                if(rep!=null && !rep.isEmpty()) {
                                    player_inv.setItem(i,rep);
                                }
                            }
                        }
                    }
                    if (offhand != null && !offhand.isEmpty()) {
                        BlockAccounting pa = block_accounting.get(offhand.getItem());
                        if (pa != null) {
                            //pa.in_player += offhand.getCount();
                            consume_item(pa,offhand);
                        }
                    }
                }
            }
            //log("a: " + a);
            //log("placed: " + placed);

            if ((placed > 0 /*&& !destroy*/)||(no_tool||damaged_tool)) {
                //player.displayClientMessage(new TextComponent("Wand placed " + placed+ " blocks"), true);
                //log("placed: " + placed);

                FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
                packet.writeBlockPos(pos);
                packet.writeBoolean(destroy);
                if(!no_tool && !damaged_tool) {
                    if (p1_state != null) {
                        packet.writeItem(p1_state.getBlock().asItem().getDefaultInstance());
                    } else {
                        if (block_state != null) {
                            packet.writeItem(block_state.getBlock().asItem().getDefaultInstance());
                        } else {
                            packet.writeItem(ItemStack.EMPTY);
                        }
                    }
                }else{
                    packet.writeItem(ItemStack.EMPTY);
                }
                packet.writeBoolean(no_tool);
                packet.writeBoolean(damaged_tool);
                //packet.writeBoolean(slab_stair_bottom);
                MCVer.inst.send_to_player((ServerPlayer) player, WandsMod.SND_PACKET, packet);
                no_tool = false;
                damaged_tool = false;
            }
        }
        if (p2) {
            p1 = null;
            p2 = false;
            valid = false;
        }
    }
    ItemStack consume_item(BlockAccounting pa,ItemStack stack_item){
        if (pa != null && pa.placed > 0) {
            if(stack_item.getItem().equals(Fluids.LAVA.getBucket())){
                pa.placed--;
                //pa.consumed++;
                ItemStack ret=Items.BUCKET.getDefaultInstance();
                return ret;
            }else{
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
            return ItemStack.EMPTY;
        }
        return null;
    }
    void mode_direction(boolean invert, int multiplier) {
        Direction[] dirs = getDirMode0(side, hit.x, hit.y, hit.z);
        if (invert) {
            if (dirs[0] != null)
                dirs[0] = dirs[0].getOpposite();
            if (dirs[1] != null)
                dirs[1] = dirs[1].getOpposite();
        }
        Direction d1 = dirs[0];
        Direction d2 = dirs[1];
        if (preview) {
            x = pos.getX();
            y = pos.getY();
            z = pos.getZ();
        }
        if (d1 != null) {
            BlockPos dest;
            block_buffer.reset();
            boolean diag=false;
            if (d2 != null) {
                dest = find_next_diag(block_state, d1, d2, pos);
                diag=true;
            } else {
                dest = find_next_pos(block_state, d1, pos);
            }
            for(int i=0;i<multiplier;i++) {
                if (dest != null) {
                    x1 = dest.getX();
                    y1 = dest.getY();
                    z1 = dest.getZ();
                    x2 = x1 + 1;
                    y2 = y1 + 1;
                    z2 = z1 + 1;
                    valid = add_to_buffer(dest.getX(), dest.getY(), dest.getZ());
                    if(diag) {
                        dest=dest.relative(d1,1);
                        dest=dest.relative(d2,1);
                    }else{
                        dest=dest.relative(d1,1);
                    }
                    BlockState bs = level.getBlockState(dest);
                    if (!can_place(bs,dest)) {
                        break;
                    }
                }else{
                    break;
                }
            }
        }
    }

    void mode_row_col(Orientation orientation, int n) {
        boolean preview = player.level.isClientSide();
        Direction dir = Direction.EAST;
        BlockPos pos_m = pos.relative(side, 1);
        BlockState state = player.level.getBlockState(pos_m);
        WandItem wand = (WandItem) wand_stack.getItem();

        if (state.isAir() || replace_fluid(state) || destroy || use) {
            BlockPos pos0 = pos;
            BlockPos pos1 = pos_m;
            BlockPos pos2 = pos;
            BlockPos pos3 = pos_m;
            int offx = 0;
            int offy = 0;
            int offz = 0;

            switch (side) {
                case UP:
                case DOWN:
                    switch (orientation) {
                        case ROW: {
                            dir = Direction.SOUTH;
                            offz = -1;
                        }break;
                        case COL: {
                            dir = Direction.EAST;
                            offx = -1;
                        }break;
                    }
                    break;
                case SOUTH:
                case NORTH:
                    switch (orientation) {
                        case ROW: {
                            dir = Direction.EAST;
                            offx = -1;
                        }break;
                        case COL: {
                            dir = Direction.UP;
                            offy = -1;
                        }break;
                    }
                    break;
                case EAST:
                case WEST:
                    switch (orientation) {
                        case ROW: {
                            dir = Direction.SOUTH;
                            offz = -1;
                        }break;
                        case COL: {
                            dir = Direction.UP;
                            offy = -1;
                        }break;
                    }
                    break;
            }

            Direction op = dir.getOpposite();
            int i = wand.limit - 1;
            int k = 0;
            boolean stop1 = false;
            boolean stop2 = false;
            // boolean intersects = false;

            boolean dont_check_state = false;
            boolean eq;
            if(n==0) {
                while (k < wand.limit && i > 0) {
                    if (!stop1 && i > 0) {
                        BlockState bs0 = player.level.getBlockState(pos0.relative(dir));
                        BlockState bs1 = player.level.getBlockState(pos1.relative(dir));
                        if (dont_check_state) {
                            eq = bs0.getBlock().equals(block_state.getBlock());
                        } else {
                            eq = bs0.equals(block_state);
                        }
                        eq = eq || state_in_slot(bs0);
                        if (eq && (bs1.isAir() || replace_fluid(bs1))) {
                            pos0 = pos0.relative(dir);
                            pos1 = pos1.relative(dir);
                            i--;
                        } else {
                            stop1 = true;
                        }
                    }
                    if (!stop2 && i > 0) {
                        BlockState bs2 = player.level.getBlockState(pos2.relative(op));
                        BlockState bs3 = player.level.getBlockState(pos3.relative(op));
                        if (dont_check_state) {
                            eq = bs2.getBlock().equals(block_state.getBlock());
                        } else {
                            eq = bs2.equals(block_state);
                        }
                        eq = eq || state_in_slot(bs2);
                        if (eq && (bs3.isAir() || replace_fluid(bs3))) {
                            pos2 = pos2.relative(op);
                            pos3 = pos3.relative(op);
                            i--;
                        } else {
                            stop2 = true;
                        }
                    }
                    k++;
                    if (stop1 && stop2) {
                        k = 1000000;
                    }
                }
            }else{
                pos1=pos0.relative(side);
                if(n==1) {
                    pos3 = pos1;
                }else {
                    pos2=pos0;
                    for(int m=0;m<n-1;m++) {
                        pos2 = pos2.relative(dir);
                        BlockState bs = player.level.getBlockState(pos2.relative(side));
                        if(can_place(bs,pos2.relative(side))){
                            pos3=pos2;
                        }else{
                            break;
                        }
                    }
                    pos3 = pos3.relative(side);
                }
            }
            if (destroy || replace ||use) {
                pos1 = pos1.relative(side.getOpposite());
                pos3 = pos3.relative(side.getOpposite());
            }
            if (preview) {
                x1 = pos1.getX() - offx;
                y1 = pos1.getY() - offy;
                z1 = pos1.getZ() - offz;
                x2 = pos3.getX() + offx + 1;
                y2 = pos3.getY() + offy + 1;
                z2 = pos3.getZ() + offz + 1;
                valid = true;
            }
            calc_pv_bbox(pos1,pos3);
            fill(pos1, pos3,false);
        } else {
            valid = false;
        }
    }
    void calc_pv_bbox(BlockPos bp1,BlockPos bp2){
        x1 = bp1.getX();
        y1 = bp1.getY();
        z1 = bp1.getZ();
        x2 = bp2.getX();
        y2 = bp2.getY();
        z2 = bp2.getZ();
        if (!bp1.equals(bp2)) {
            if (x1 >= x2) {
                x1 += 1;
                bb1_x=x2;
                bb2_x=x1;
            } else {
                x2 += 1;
                bb1_x=x1;
                bb2_x=x2;
            }
            if (y1 >= y2) {
                y1 += 1;
                bb1_y=y2;
                bb2_y=y1;
            } else {
                y2 += 1;
                bb1_y=y1;
                bb2_y=y2;
            }
            if (z1 >= z2) {
                z1 += 1;
                bb1_z=z2;
                bb2_z=z1;
            } else {
                z2 += 1;
                bb1_z=z1;
                bb2_z=z2;
            }
        } else {
            x2 = x1 + 1;
            y2 = y1 + 1;
            z2 = z1 + 1;
            bb1_x=x1;
            bb1_y=y1;
            bb1_z=z1;
            bb2_x=x2;
            bb2_y=y2;
            bb2_z=z2;
        }

        valid = true;
    }
    void mode_fill_rect(boolean rect) {
        if (p1 != null && (p2 || preview)) {
            calc_pv_bbox(p1,pos);
            fill(p1, pos,rect);
        }
    }
    void mode_area() {
        int limit2=WandItem.getAreaLimit(wand_stack);
        if(limit2<=0){
            limit2=wand_item.limit;
        }
        block_buffer.reset();
        add_neighbour(pos, block_state);
        int i = 0;
        int found = 1;
        limit2-=1;
        while (i < limit2 && i < MAX_LIMIT && found <= limit2) {
            if (i < block_buffer.get_length()) {
                BlockPos p = block_buffer.get(i).relative(side, -1);
                found += find_neighbours(p, block_state,limit2);
            }
            i++;
        }
        if (destroy || replace ||use) {
            for (int a = 0; a < block_buffer.get_length(); a++) {
                block_buffer.set(a, block_buffer.get(a).relative(side, -1));
            }
        }
        validate_buffer();
    }
    void mode_grid() {
        int m=WandItem.getGridM(wand_stack)-1;
        int n=WandItem.getGridN(wand_stack)-1;
        block_buffer.reset();
        Direction dir1=Direction.SOUTH;
        Direction dir2= Direction.EAST;
        switch (side) {
            case UP:
            case DOWN:
                switch (this.rotation) {
                    case NONE:
                        dir1=player.getDirection().getClockWise();
                        dir2=player.getDirection();
                        break;
                    case CLOCKWISE_90:
                        dir1=player.getDirection().getClockWise().getClockWise();
                        dir2=player.getDirection().getClockWise();
                        break;
                    case CLOCKWISE_180:
                        dir1=player.getDirection().getClockWise().getClockWise().getClockWise();
                        dir2=player.getDirection().getClockWise().getClockWise();
                        break;
                    case COUNTERCLOCKWISE_90:
                        dir1=player.getDirection();
                        dir2=player.getDirection().getClockWise().getClockWise().getClockWise();
                        break;
                }
                break;
            case SOUTH:
            case NORTH:
            case EAST:
            case WEST:
                switch (this.rotation) {
                    case NONE:
                        dir1=player.getDirection().getClockWise();
                        dir2 = Direction.UP;
                        break;
                    case CLOCKWISE_90:
                        dir1=player.getDirection().getClockWise();
                        dir2 = Direction.DOWN;
                        break;
                    case CLOCKWISE_180:
                        dir1=player.getDirection().getClockWise().getOpposite();
                        dir2 = Direction.DOWN;
                        break;
                    case COUNTERCLOCKWISE_90:
                        dir1=player.getDirection().getClockWise().getOpposite();
                        dir2 = Direction.UP;
                        break;
                }
                break;
        }
        if (destroy || replace ||use) {
            BlockPos pos1=pos.relative(side, -1).relative(side);
            BlockPos pos2=pos.relative(side, -1).relative(dir1,m).relative(dir2,n).relative(side);
            calc_pv_bbox(pos1,pos2);
            fill(pos1, pos2,false);
        }else{
            BlockPos pos1=pos.relative(side);
            BlockPos pos2=pos.relative(dir1,m).relative(dir2,n).relative(side);
            calc_pv_bbox(pos1,pos2);
            fill(pos1, pos2,false);
        }
        validate_buffer();
    }
    void mode_line() {
        block_buffer.reset();
        if (p1 != null && (p2 || preview)) {
            int x1 = p1.getX();
            int y1 = p1.getY();
            int z1 = p1.getZ();
            int x2 = pos.getX();
            int y2 = pos.getY();
            int z2 = pos.getZ();
            int dx, dy, dz, xs, ys, zs, lp1, lp2;
            dx = Math.abs(x2 - x1);
            dy = Math.abs(y2 - y1);
            dz = Math.abs(z2 - z1);
            if (x2 > x1) {
                xs = 1;
            } else {
                xs = -1;
            }
            if (y2 > y1) {
                ys = 1;
            } else {
                ys = -1;
            }
            if (z2 > z1) {
                zs = 1;
            } else {
                zs = -1;
            }
            add_to_buffer(x1, y1, z1);
            // X
            if (dx >= dy && dx >= dz) {
                lp1 = 2 * dy - dx;
                lp2 = 2 * dz - dx;
                while (x1 != x2) {
                    x1 += xs;
                    if (lp1 >= 0) {
                        y1 += ys;
                        lp1 -= 2 * dx;
                    }
                    if (lp2 >= 0) {
                        z1 += zs;
                        lp2 -= 2 * dx;
                    }
                    lp1 += 2 * dy;
                    lp2 += 2 * dz;
                    add_to_buffer(x1, y1, z1);
                }
            } else if (dy >= dx && dy >= dz) {
                lp1 = 2 * dx - dy;
                lp2 = 2 * dz - dy;
                while (y1 != y2) {
                    y1 += ys;
                    if (lp1 >= 0) {
                        x1 += xs;
                        lp1 -= 2 * dy;
                    }
                    if (lp2 >= 0) {
                        z1 += zs;
                        lp2 -= 2 * dy;
                    }
                    lp1 += 2 * dx;
                    lp2 += 2 * dz;
                    add_to_buffer(x1, y1, z1);
                }
            } else {
                lp1 = 2 * dy - dz;
                lp2 = 2 * dx - dz;
                while (z1 != z2) {
                    z1 += zs;
                    if (lp1 >= 0) {
                        y1 += ys;
                        lp1 -= 2 * dz;
                    }
                    if (lp2 >= 0) {
                        x1 += xs;
                        lp2 -= 2 * dz;
                    }
                    lp1 += 2 * dy;
                    lp2 += 2 * dx;
                    add_to_buffer(x1, y1, z1);
                }
            }
        }
        validate_buffer();
    }

    void mode_circle(int plane, boolean fill) {
        block_buffer.reset();
        int diameter=0;
        if (p1 != null && (p2 || preview)) {
            int xc = p1.getX();
            int yc = p1.getY();
            int zc = p1.getZ();
            int px = pos.getX() - xc;
            int py = pos.getY() - yc;
            int pz = pos.getZ() - zc;
            // log("circle plane:"+plane+ " fill: "+fill);
            int r = (int) Math.sqrt(px * px + py * py + pz * pz);
            radius=r+1;
            if(r<1){
                return;
            }
            diameter=2*r;
            if (plane == 0) {// XZ;
                int x = r;
                int y = 0;
                int z = 0;
                int d = 1 - r;
                do {
                    drawCircleOctants(xc, yc, zc, x, y, z, plane);
                    z++;
                    if (d< 0 )
                        d+= 2 * z + 1;
                    else {
                        x--;
                        d+=  2 * (z - x) + 1;
                    }
                    //break;
                }while(z<=x);
                if (fill) {
                    if(r==1){
                        add_to_buffer(xc, yc, zc);
                    }else {
                        int r2 = r * r;
                        for (z = -r; z <= r; z++) {
                            for (x = -r; x <= r; x++) {
                                if ( (x * x) + (z * z) < r2) {
                                    add_to_buffer(xc + x, yc, zc + z);
                                }
                            }
                        }
                    }
                }
            } else if (plane == 1) {// XY;
                int x = r;
                int y = 0;
                int z = 0;
                int d = 1 - r;
                do {
                    drawCircleOctants(xc, yc, zc, x, y, z, plane);
                    y++;
                    if (d< 0 )
                        d+= 2 * y + 1;
                    else {
                        x--;
                        d+=  2 * (y - x) + 1;
                    }
                    //break;
                }while(y<=x);
                if (fill) {
                    if(r==1){
                        add_to_buffer(xc, yc, zc);
                    }else {
                        int r2 = r * r;
                        for (y = -r; y <= r; y++) {
                            for (x = -r; x <= r; x++) {
                                if ((x * x) + (y * y) <= r2) {
                                    add_to_buffer(xc + x, yc + y, zc);
                                }
                            }
                        }
                    }
                }
            } else if (plane == 2) {// YZ;

                int x = 0;
                int y = 0;
                int z = r;
                int d = 1 - r;
                do {
                    drawCircleOctants(xc, yc, zc, x, y, z, plane);
                    y++;
                    if (d< 0 )
                        d+= 2 * y + 1;
                    else {
                        z--;
                        d+=  2 * (y - z) + 1;
                    }
                    //break;
                }while(y<=z);
                if (fill) {
                    int r2 = r * r;
                    for (z = -r; z <= r; z++) {
                        for (y = -r; y <= r; y++) {
                            if ((y * y) + (z * z) <= r2) {
                                add_to_buffer(xc, yc + y, zc + z);
                            }
                        }
                    }
                }
            }
        }
        if (preview) {
            valid = (block_buffer.get_length() > 0) && diameter< wand_item.limit;
            if(prnt && diameter>= wand_item.limit){
                player.displayClientMessage (MCVer.inst.literal("limit reached"), true);
            }
        }else{
            if(diameter>= wand_item.limit){
                player.displayClientMessage(MCVer.inst.literal("limit reached"), false);
            }
        }
    }
    void mode_blast(){
        block_buffer.reset();
        if(!preview){
            if(!creative) {
                block_accounting.clear();
                BlockAccounting ba=new BlockAccounting();
                int rad=WandItem.getBlastRadius(wand_stack);
                int extra_cost=rad-4;
                ba.needed=1+extra_cost;
                block_accounting.put(Items.TNT,ba);
            }
        }
    }
    void mode_copy() {
        //if (!preview) {
            //WandsMod.log("mode6 copy_pos1: "+copy_pos1+" copy_pos2: "+copy_pos2 + " copy_paste_buffer: "+copy_paste_buffer.size(),prnt);
        //}
        if (copy_pos1 != null && preview) {
            valid = true;
            copy_x1 = copy_pos1.getX();
            copy_y1 = copy_pos1.getY();
            copy_z1 = copy_pos1.getZ();
            if (copy_pos2 == null) {
                copy_x2 = pos.getX();
                copy_y2 = pos.getY();
                copy_z2 = pos.getZ();
            } else {
                copy_x2 = copy_pos2.getX();
                copy_y2 = copy_pos2.getY();
                copy_z2 = copy_pos2.getZ();
            }
            if (!copy_pos1.equals(copy_pos2)) {
                if (copy_x1 >= copy_x2) {
                    copy_x1 += 1;
                } else {
                    copy_x2 += 1;
                }
                if (copy_y1 >= copy_y2) {
                    copy_y1 += 1;
                } else {
                    copy_y2 += 1;
                }
                if (copy_z1 >= copy_z2) {
                    copy_z1 += 1;
                } else {
                    copy_z2 += 1;
                }
            } else {
                copy_x2 = copy_x1 + 1;
                copy_y2 = copy_y1 + 1;
                copy_z2 = copy_z1 + 1;
            }
        }
        if (copy_pos1 != null && copy_pos2 != null) {
            //if(!preview )
            {
                int xs, ys, zs, xe, ye, ze,lx,ly,lz;
                /*if (copy_pos1.getX() >= copy_pos2.getX()) {
                    xs = copy_pos2.getX();
                    xe = copy_pos1.getX();
                } else {
                    xs = copy_pos1.getX();
                    xe = copy_pos2.getX();
                }
                if (copy_pos1.getY() >= copy_pos2.getY()) {
                    ys = copy_pos2.getY();
                    ye = copy_pos1.getY();
                } else {
                    ys = copy_pos1.getY();
                    ye = copy_pos2.getY();
                }
                if (copy_pos1.getZ() >= copy_pos2.getZ()) {
                    zs = copy_pos2.getZ();
                    ze = copy_pos1.getZ();
                } else {
                    zs = copy_pos1.getZ();
                    ze = copy_pos2.getZ();
                }*/
                xs=copy_pos1.getX();
                xe=copy_pos2.getX();
                ys=copy_pos1.getY();
                ye=copy_pos2.getY();
                zs=copy_pos1.getZ();
                ze=copy_pos2.getZ();
                int xsgn=(xs>=xe?-1:1);
                int ysgn=(ys>=ye?-1:1);
                int zsgn=(zs>=ze?-1:1);
                lx=Math.abs(xe - xs);
                ly=Math.abs(ye - ys);
                lz=Math.abs(ze - zs);
                //log("copy");
                int ll = (lx + 1) * (ly + 1) * (lz + 1);
                //int ll = lx * ly* lz);
                WandsMod.log("copy volume "+ll,prnt);
                if (ll <= MAX_COPY_VOL) {
                    BlockPos.MutableBlockPos bp = new BlockPos.MutableBlockPos();
                    copy_paste_buffer.clear();
                    int cp = 0;
                    for (int z = 0; z <= lz; z++) {
                        for (int y = 0; y <= ly; y++) {
                            for (int x = 0; x <= lx; x++) {
                                bp.set(xs+x*xsgn, ys+y*ysgn, zs+z*zsgn);
                                BlockState bs = level.getBlockState(bp);
                                if(!WandsConfig.denied.contains(bs.getBlock())){
                                    if (bs != Blocks.AIR.defaultBlockState() && !(bs.getBlock() instanceof ShulkerBoxBlock)) {
                                        cp++;
                                        copy_paste_buffer.add(new CopyPasteBuffer(new BlockPos(x*xsgn , y*ysgn , z*zsgn ), bs));
                                    }
                                }
                            }
                        }
                    }
                    //log("copied "+copy_paste_buffer.size() + " cp: "+cp);
                    if (!preview)
                        player.displayClientMessage(MCVer.inst.literal("Copied: " + cp + " blocks"), false);
                } else {
                    player.displayClientMessage(MCVer.inst.literal("Copy limit reached"), false);
                    //log("max volume");
                }
            }
        }
    }

    void mode_paste() {
        if (!preview) {
            //log("mode6 paste "+copy_paste_buffer.size());
            BlockPos b_pos = pos;
            if(!(replace||destroy)){
                b_pos = pos.relative(side, 1);
            }
            //BlockPos.MutableBlockPos bp = new BlockPos.MutableBlockPos();
            block_buffer.reset();
            random.setSeed(palette_seed);
            for (CopyPasteBuffer b : copy_paste_buffer) {
                BlockPos p = b.pos.rotate(rotation);
                BlockState st=paste_rot(b.state);
                if(has_palette) {
                    block_buffer.add(
                            b_pos.getX() + p.getX(),
                            b_pos.getY() + p.getY(),
                            b_pos.getZ() + p.getZ(),this);
                }else {
                    block_buffer.add(
                            b_pos.getX() + p.getX(),
                            b_pos.getY() + p.getY(),
                            b_pos.getZ() + p.getZ(),
                            st, st.getBlock().asItem());
                }
            }
        }
    }
    public BlockState paste_rot(BlockState st){
        Block blk=st.getBlock();
        if(this.replace && this.offhand_block!=null && this.offhand_block.asItem()!=Items.AIR){
            blk=this.offhand_block;
            st=blk.defaultBlockState();
            return st;
        }
        if(blk instanceof  StairBlock) {
            Direction d;
            switch (this.rotation) {
                case NONE:
                break;
                case CLOCKWISE_90:
                    #if MC=="1165"
                    d= st.getValue(StairBlock.FACING).getClockWise();
                    #else
                    d= st.getValue(StairBlock.FACING).getClockWise(Direction.Axis.Y);
                    #endif

                    st = st.setValue(StairBlock.FACING, d);
                break;
                case CLOCKWISE_180:
                    d = st.getValue(StairBlock.FACING).getOpposite();
                    st = st.setValue(StairBlock.FACING, d);
                    break;
                case COUNTERCLOCKWISE_90:
                    #if MC=="1165"
                    d= st.getValue(StairBlock.FACING).getCounterClockWise();
                    #else
                    d= st.getValue(StairBlock.FACING).getCounterClockWise(Direction.Axis.Y);
                    #endif
                    st = st.setValue(StairBlock.FACING, d);
                    break;
            }
        }else{
            if(blk instanceof RotatedPillarBlock){
                Direction.Axis a= st.getValue(RotatedPillarBlock.AXIS);
                if(a!=Direction.Axis.Y) {
                    switch (this.rotation) {
                        case CLOCKWISE_90:
                        case COUNTERCLOCKWISE_90:
                            if(a==Direction.Axis.X)
                                st = st.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
                            if(a==Direction.Axis.Z)
                                st = st.setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
                            break;
                            case NONE:
                            break;
                            case CLOCKWISE_180:
                            break;
                    }
                }
                //st = blk.defaultBlockState().setValue(RotatedPillarBlock.AXIS,this.axis);

            }else{
                st=st.rotate(this.rotation);
            }
        }
        return st;
    }

    void drawCircleOctants(int xc, int yc, int zc, int x, int y, int z, int plane) {
        switch (plane) {

            case 0: {// XZ
                add_to_buffer(xc + x, yc, zc + z);
                if (x != z) {
                    add_to_buffer(xc + z, yc, zc + x);
                    add_to_buffer(xc + z, yc, zc - x);
                    add_to_buffer(xc - x, yc, zc - z);
                }
                if (z > 0) {
                    add_to_buffer(xc + x, yc, zc - z);
                    add_to_buffer(xc - z, yc, zc - x);
                    add_to_buffer(xc - z, yc, zc + x);
                    if (x != z) {
                        add_to_buffer(xc - x, yc, zc + z);
                    }
                }
            }break;
            case 1: {// XY
                add_to_buffer(xc + x, yc + y, zc);
                if (x != y) {
                    add_to_buffer(xc + y, yc + x, zc);
                    add_to_buffer(xc + y, yc - x, zc);
                    add_to_buffer(xc - x, yc - y, zc);
                }
                if (y > 0) {
                    add_to_buffer(xc + x, yc - y, zc);
                    add_to_buffer(xc - y, yc - x, zc);
                    add_to_buffer(xc - y, yc + x, zc);
                    if (x != y) {
                        add_to_buffer(xc - x, yc + y, zc);
                    }
                }
            }break;
            case 2: {// YZ
                add_to_buffer(xc, yc + y, zc + z);
                if (y != z) {
                    add_to_buffer(xc, yc + z, zc + y);
                    add_to_buffer(xc, yc + z, zc - y);
                    add_to_buffer(xc, yc - y, zc - z);
                }
                if (z > 0) {
                    add_to_buffer(xc, yc + y, zc - z);
                    add_to_buffer(xc, yc - z, zc - y);
                    add_to_buffer(xc, yc - z, zc + y);
                    if (y != z) {
                        add_to_buffer(xc, yc - y, zc + z);
                    }
                }
            }break;
        }
    }
    BlockState get_state() {
        BlockState st=block_state;
        if (!has_palette/* || mode==Mode.DIRECTION*/) {
            if (offhand_state != null && !offhand_state.isAir()) {
                st= offhand_state;
            } else {
                if (mode == Mode.FILL || mode == Mode.LINE || mode == Mode.CIRCLE|| mode==Mode.RECT) {
                    if (p1_state != null)
                        st=p1_state;
                }
            }
            st= state_for_placement(st,null);
        } else {

            if (palette_slots.size() > 0) {
                PaletteMode palette_mode = PaletteItem.getMode(palette);
                int bound = palette_slots.size();
                if (palette_mode == PaletteMode.RANDOM) {
                    slot = random.nextInt(bound);
                }
                PaletteSlot ps = palette_slots.get(slot);
                if (palette_mode == PaletteMode.ROUND_ROBIN ) {
                    if(!(mode==Mode.DIRECTION && level.isClientSide()))
                        slot = (slot + 1) % bound;
                }
                st = ps.state;
                Block blk = st.getBlock();
                if (palette_mode == PaletteItem.PaletteMode.RANDOM) {
                    if (blk instanceof SnowLayerBlock) {
                        int sn = random.nextInt(7);
                        st = st.setValue(SnowLayerBlock.LAYERS, sn + 1);
                    }
                }
                st = state_for_placement(st,null);
                //if (palette_mode == PaletteItem.PaletteMode.RANDOM)
                {
                    if (PaletteItem.getRotate(palette)) {
                        st = ps.state.getBlock().rotate(ps.state, Rotation.getRandom(random));
                    }
                }
            }
        }

        return st;
    }

    Item get_item(BlockState state) {
        if (state != null) {
            return state.getBlock().asItem();
        }
        return null;
    }
    BlockState state_for_placement(BlockState st,BlockPos bp){
        Block blk=st.getBlock();
        if(state_mode== WandItem.StateMode.APPLY) {
            if (blk instanceof SlabBlock) {
                SlabType slab_type;
                if(slab_stair_bottom) {
                    slab_type = SlabType.BOTTOM;
                }else {
                    slab_type = SlabType.TOP;
                }
                st = blk.defaultBlockState().setValue(SlabBlock.TYPE, slab_type);
                
            } else {
                if (blk instanceof StairBlock) {
                    Half h;
                    if (slab_stair_bottom) {
                        h = Half.BOTTOM;
                    }else {
                        h = Half.TOP;
                    }
                    st = blk.defaultBlockState().setValue(StairBlock.HALF, h).rotate(rotation);
                     
                } else {
                    if (blk instanceof RotatedPillarBlock) {
                        st = blk.defaultBlockState().setValue(RotatedPillarBlock.AXIS, this.axis);
                    } else {
                        //if ( blk instanceof CrossCollisionBlock ||blk instanceof DoorBlock)
                        {
                            BlockHitResult hit_res = new BlockHitResult(hit, side, (bp!=null?bp:pos), true);
                            //UseOnContext uctx = new UseOnContext(player, InteractionHand.OFF_HAND,hit_res);
                            //uctx.itemStack=st.getBlock().asItem().getDefaultInstance();
                            //System.out.println("uctx "+uctx.itemStack);
                            //BlockPlaceContext pctx = new BlockPlaceContext(uctx);
                            BlockPlaceContext pctx =new BlockPlaceContext(player, InteractionHand.OFF_HAND, st.getBlock().asItem().getDefaultInstance(), hit_res);
                            st = st.getBlock().getStateForPlacement(pctx);
                            //is_door=true;
                        }
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
                        if(level.destroyBlock(p.pos, false)){
                            undo_buffer.pop();
                        }
                    } else {
                        if (p.state.canSurvive(level,p.pos) && level.setBlockAndUpdate(p.pos, p.state)) {
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
    void validate_buffer() {
        if (preview) {
            valid = (block_buffer.get_length() > 0) && block_buffer.get_length()<= wand_item.limit;
        }
    }
    void fill(BlockPos from, BlockPos to,boolean hollow) {
        //log("fill from: "+from+" to: "+to);
        int xs, ys, zs, xe, ye, ze;
        if (from.getX() >= to.getX()) {
            xs = to.getX();
            xe = from.getX();
        } else {
            xs = from.getX();
            xe = to.getX();
        }
        if (from.getY() >= to.getY()) {
            ys = to.getY();
            ye = from.getY();
        } else {
            ys = from.getY();
            ye = to.getY();
        }
        if (from.getZ() >= to.getZ()) {
            zs = to.getZ();
            ze = from.getZ();
        } else {
            zs = from.getZ();
            ze = to.getZ();
        }
        //int limit = MAX_LIMIT;
        //if (!player.getAbilities().instabuild) {
        int    limit = wand_item.limit;
        int ll=0;
        block_buffer.reset();

        for (int z = zs; z <= ze; z++) {
            for (int y = ys; y <= ye; y++) {
                for (int x = xs; x <= xe; x++) {
                    if(hollow) {
                        switch (this.axis) {
                            case X:
                                if (y > ys && y < ye && z > zs && z < ze)
                                    continue;
                                break;
                            case Y:
                                if (x > xs && x < xe && z > zs && z < ze)
                                    continue;
                                break;
                            case Z:
                                if (x > xs && x < xe && y > ys && y < ye)
                                    continue;
                                break;
                        }
                    }
                    if (ll < limit && ll < MAX_LIMIT) {
                        add_to_buffer(x, y, z);
                        ll++;
                    }
                }
            }
        }
        //if(ll>=limit){
            //log("limit: "+ll);
//            if(!preview) {
//                player.displayClientMessage(new TextComponent("Wand limit reached: "+ limit + ")"), false);
//            }
        //}
    }
    boolean place_block(BlockPos block_pos,BlockState state) {
        boolean placed = false;
        //WandsMod.log("place_block "+block_pos+" state: "+state + " destroy: " + destroy,true);
        Level level = player.level;
        if (level.isClientSide) {
            return false;
        }
        if(state==null /*&& !destroy*/){
            //log("state is null");
            return false;
        }
        Block blk=state.getBlock();
        if( WandsConfig.denied.contains(blk)){
            //WandsMod.log("block ("+blk+") is in the denied list",true);
            return false;
        }
        BlockState st = level.getBlockState(block_pos);
        Block actual_blk = st.getBlock();
        if( WandsConfig.denied.contains(actual_blk)){
            //WandsMod.log("actual block ("+actual_blk+") is in the denied list",true);
            return false;
        }
        int wand_durability = wand_stack.getMaxDamage() - wand_stack.getDamageValue();
        int tool_durability = -1;

        boolean _can_destroy=creative;
        if(!creative ){
            if (destroy||replace ||use) {
                _can_destroy = can_destroy(st, true);
                if (digger_item != null) {
                    tool_durability = digger_item.getMaxDamage() - digger_item.getDamageValue();
                } else {
                    no_tool = true;
                    return false;
                }
            }
            boolean will_break=(wand_durability == 1 ) || (tool_durability == 1 );
            /*if(will_break){
                if(WandsMod.config.allow_wand_to_break) {
                    if(WandsMod.config.allow_offhand_to_break){
                    }
                }else{
                    if(WandsMod.config.allow_offhand_to_break){
                        will_break = false;
                    }
                }
            }*/
            if(will_break) {
                damaged_tool=true;
                //stop = true;
                return false;
            }
        }else{
            if(creative && use){
                can_destroy( st, true);
                if (digger_item ==null) {
                    no_tool=true;
                    return false;
                }
            }
        }


        p1_state=state;
        if (!destroy ) {
            //if (state.getBlock() instanceof SnowLayerBlock) {

            if (offhand!=null) {
                blk = Block.byItem(offhand.getItem());
            }
            //if(!replace && !blk.canSurvive(state, level, block_pos)){
            if(!replace && !state.canSurvive(level, block_pos)){
                return false;
            }
            if(blk  instanceof SnowLayerBlock) {
                BlockState below = level.getBlockState(block_pos.below());
                if (below.getBlock() instanceof SnowLayerBlock) {
                    int layers=below.getValue(SnowLayerBlock.LAYERS);
                    if(layers<8){
                        block_pos=block_pos.below();
                        state=state.setValue(SnowLayerBlock.LAYERS,layers+1);
                        //level.setBlock(block_pos,Blocks.AIR.defaultBlockState(),2);
                        //level.destroyBlock(block_pos, false);
                    }
                }
            }else{
                if ( blk instanceof CrossCollisionBlock ||blk instanceof DoorBlock) {
                    BlockHitResult hit_res = new BlockHitResult(new Vec3(block_pos.getX() + 0.5, block_pos.getY() + 1.0, block_pos.getZ() + 0.5), side, block_pos, true);
                    UseOnContext uctx = new UseOnContext(player, InteractionHand.OFF_HAND, hit_res);
                    BlockPlaceContext pctx = new BlockPlaceContext(uctx);
                    state = state.getBlock().getStateForPlacement(pctx);
                }
            }
        }

        if(use && digger_item!=null && (has_hoe||has_shovel||has_axe) ) {
            //HoeItem hoe=(HoeItem) offhand.getItem();
            BlockHitResult hit_res=new BlockHitResult(new Vec3(block_pos.getX()+0.5,block_pos.getY()+1.0,block_pos.getZ()+0.5),Direction.UP,block_pos,true);
            UseOnContext ctx=new UseOnContext(player,InteractionHand.OFF_HAND,hit_res);
            if( digger_item.useOn(ctx) != InteractionResult.PASS) {
                if (!creative) {
                    if(!wand_item.unbreakable) {
                        wand_stack.hurtAndBreak(1, player, (Consumer<LivingEntity>) ((p) -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND)));
                    }
                    consume_xp();
                }
            }
            return true;
        }
        if (creative) {
            if (destroy) {
                if (destroyBlock(block_pos, false)) {
                    if (undo_buffer != null) {
                        undo_buffer.put(block_pos,  level.getBlockState(block_pos), destroy);
                        //undo_buffer.print();
                    }
                    return true;
                }
            } else {
                if(!use) {
                    state=state_for_placement(state,block_pos);
                    if (state!=null && state.canSurvive(level,block_pos) && level.setBlockAndUpdate(block_pos, state)) {
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
                    if (destroy || replace) {

                        if (_can_destroy) {
                            //log("can destroy: "+st);
                            placed = destroyBlock(block_pos, false);
                            //log("destroyed: "+placed);
                            if (placed && WandsMod.config.destroy_in_survival_drop && digger_item !=null) {
                                int silk_touch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH,
                                        digger_item);
                                int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE,
                                        digger_item);
                                if ( WandsMod.config.survival_unenchanted_drops || fortune > 0 || silk_touch > 0
                                        || digger_item.getItem() instanceof ShearsItem) {
                                    st.getBlock().playerDestroy(level, player, block_pos, st, null, digger_item);
                                }
                            }
                        }else{
                            //log("can't destroy: "+st);
                        }
                    }
                    if(!destroy || (replace && placed)){
                        /*boolean is_tool = offhand != null && !offhand.isEmpty()
                                && offhand.getItem() instanceof DiggerItem;*/
                        if (!use) {
                            if (state!=null && state.canSurvive(level,block_pos) && level.setBlockAndUpdate(block_pos, state)) {
                                blk.setPlacedBy(level,block_pos,state,player,blk.asItem().getDefaultInstance());
                                placed=true;
                            }
                        }
                    }
                    if(replace && !placed){
                        if(digger_item.getItem()==Items.AIR)
                            player.displayClientMessage(MCVer.inst.literal("incorrect tool"),false);
                        stop=true;
                    }
                }else{
                    if(BLOCKS_PER_XP != 0 && (xp - dec) < 0){
                        player.displayClientMessage(MCVer.inst.literal("not enough xp"),false);
                        stop=true;
                    }
                    if(wand_durability == 1 ){
                        player.displayClientMessage(MCVer.inst.literal("wand damaged"),false);
                        if(WandsMod.config.allow_wand_to_break &&
                                digger_item !=null && digger_item.getItem()==Items.AIR
                        ) {

                        }else {
                            stop = true;
                        }
                    }
                    /*if((wand_durability == 1 && WandsMod.config.allow_wand_to_break)
                        && digger_item !=null && digger_item.getItem()==Items.AIR)
                    {

                        stop=true;
                    }*/
                }

                if (placed) {
                    if ((destroy||replace) && digger_item !=null) {
                        digger_item.hurtAndBreak(1, player, (Consumer<LivingEntity>) ((p) -> p.broadcastBreakEvent(InteractionHand.OFF_HAND)));
                    }
                    if(!wand_item.unbreakable) {
                        wand_stack.hurtAndBreak(1, player, (Consumer<LivingEntity>) ((p) -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND)));
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

            /*if (bl) {
                BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(blockPos) : null;
                Block.dropResources(blockState, level, blockPos, blockEntity, null, ItemStack.EMPTY);
            }*/

            boolean bl2 = level.setBlock(blockPos, fluidState.createLegacyBlock(), 3, 512);
            if (bl2) {
                //level.gameEvent(GameEvent.BLOCK_DESTROY, blockPos, GameEvent.Context.of(entity, blockState));
            }

            return bl2;
        }
    }
    Direction[] getDirMode0(Direction side, double hit_x, double hit_y, double hit_z) {
        Direction[] ret = new Direction[2];
        ret[0] = null;
        ret[1] = null;
        double a = 0.333333f;
        double b = 0.666666f;
        double x = WandUtils.unitCoord( hit_x);
        double y = WandUtils.unitCoord( hit_y);
        double z = WandUtils.unitCoord( hit_z);
        double xo=x;
        double yo=y;
        double zo=z;
        VoxelShape shape = block_state.getShape(level, pos);
        //log("--");
        //log("x: "+x+" y: "+y+" z: "+z);
        List<AABB> list = shape.toAabbs();
        int index=0;
        for (AABB aabb : list) {

            switch (side) {
                case UP:
                case DOWN:
                    if(xo>aabb.minX&& xo<aabb.maxX && zo> aabb.minZ && zo<aabb.maxZ) {
                        //log("bb: "+aabb);
                        if (aabb.getXsize() != 0)
                            x = (float) ((x - aabb.minX) / aabb.getXsize());
                        if (aabb.getZsize() != 0)
                            z = (float) ((z - aabb.minZ) / aabb.getZsize());
                        //log("      x: " + x + " y: " + y + " z: " + z);
                        grid_voxel_index=index;
                    }
                    break;
                case EAST:
                case WEST:
                    //if(last_x>aabb.maxX)
                    {
                        if (yo > aabb.minY && yo < aabb.maxY && zo > aabb.minZ && zo < aabb.maxZ) {
                            //log("bb: " + aabb);
                            if (aabb.getYsize() != 0)
                                y = (float) ((y - aabb.minY) / aabb.getYsize());
                            if (aabb.getZsize() != 0)
                                z = (float) ((z - aabb.minZ) / aabb.getZsize());
                            //log("      x: " + x + " y: " + y + " z: " + z);
                            grid_voxel_index = index;
                        }
                    }
                    break;
                case NORTH:
                case SOUTH:
                    if(xo>aabb.minX&& xo<aabb.maxX && yo> aabb.minY && yo<aabb.maxY){
                        //log("bb: " + aabb);
                        if(aabb.getXsize()!=0)
                            x =  (float)((x-aabb.minX)/aabb.getXsize());
                        if(aabb.getYsize()!=0)
                            y = (float)((y-aabb.minY)/aabb.getYsize());
                        grid_voxel_index=index;
                        //log("      x: " + x + " y: " + y + " z: " + z);
                    }
                    break;
            }
            index++;
        }
        switch (side) {
            case UP:
            case DOWN:
                if (x >= a && x <= b) {
                    if (z <= a) {
                        ret[0] = Direction.NORTH;
                    } else {
                        if (z >= b) {
                            ret[0] = Direction.SOUTH;
                        } else {
                            ret[0] = side.getOpposite();
                        }
                    }
                } else {
                    if (z >= a && z <= b) {
                        if (x <= a) {
                            ret[0] = Direction.WEST;
                        } else {
                            ret[0] = Direction.EAST;
                        }
                    } else {
                        if (x <= a && z <= a) {
                            ret[0] = Direction.WEST;
                            ret[1] = Direction.NORTH;
                        }
                        if (x >= b && z <= a) {
                            ret[0] = Direction.EAST;
                            ret[1] = Direction.NORTH;
                        }
                        if (x >= b && z >= b) {
                            ret[0] = Direction.EAST;
                            ret[1] = Direction.SOUTH;
                        }
                        if (x <= a && z >= b) {
                            ret[0] = Direction.WEST;
                            ret[1] = Direction.SOUTH;
                        }
                    }
                }
                break;
            case EAST:
            case WEST:

                if (z >= a && z <= b) {
                    if (y <= a) {
                        ret[0] = Direction.DOWN;
                    } else {
                        if (y >= b) {
                            ret[0] = Direction.UP;
                        } else {
                            ret[0] = side.getOpposite();
                        }
                    }
                } else {
                    if (y >= a && y <= b) {
                        if (z <= a) {
                            ret[0] = Direction.NORTH;
                            return ret;
                        } else {
                            ret[0] = Direction.SOUTH;
                            return ret;
                        }
                    } else {
                        if (y <= a && z <= a) {
                            ret[0] = Direction.DOWN;
                            ret[1] = Direction.NORTH;
                        }
                        if (y >= b && z <= a) {
                            ret[0] = Direction.UP;
                            ret[1] = Direction.NORTH;
                        }
                        if (y >= b && z >= b) {
                            ret[0] = Direction.UP;
                            ret[1] = Direction.SOUTH;
                        }
                        if (y <= a && z >= b) {
                            ret[0] = Direction.DOWN;
                            ret[1] = Direction.SOUTH;
                        }
                    }
                }
                break;
            case NORTH:
            case SOUTH:
                if (x >= a && x <= b) {
                    if (y <= a) {
                        ret[0] = Direction.DOWN;
                    } else {
                        if (y >= b) {
                            ret[0] = Direction.UP;
                        } else {
                            ret[0] = side.getOpposite();
                        }
                    }
                } else {
                    if (y >= a && y <= b) {
                        if (x <= a) {
                            ret[0] = Direction.WEST;
                        } else {
                            ret[0] = Direction.EAST;
                        }
                    } else {
                        if (y <= a && x <= a) {
                            ret[0] = Direction.DOWN;
                            ret[1] = Direction.WEST;
                        }
                        if (y >= b && x <= a) {
                            ret[0] = Direction.UP;
                            ret[1] = Direction.WEST;
                        }
                        if (y >= b && x >= b) {
                            ret[0] = Direction.UP;
                            ret[1] = Direction.EAST;
                        }
                        if (y <= a && x >= b) {
                            ret[0] = Direction.DOWN;
                            ret[1] = Direction.EAST;
                        }
                    }
                }
                break;
        }
        return ret;
    }
    void tally_copied_buffer(){
        Map<String, BlockAccounting> ba_map = new HashMap<>();
        for(CopyPasteBuffer b: copy_paste_buffer){
            if(b.state!=null){
                BlockAccounting ba= ba_map.get(b.state.getBlock().getDescriptionId());
                if(ba==null) {
                    ba = new BlockAccounting();
                    ba_map.put(b.state.getBlock().getDescriptionId(),ba);
                }
                ba.needed+=1;
            }
        }
        //player.displayClientMessage(new TextComponent("Copy buffer tally"),false);
        for (Map.Entry<String, BlockAccounting> entry : ba_map.entrySet()){
            MutableComponent name=MCVer.inst.translatable(entry.getKey());
            MutableComponent st=MCVer.inst.literal("   ");
            st.append(name).append(" needed: "+entry.getValue().needed);
            player.displayClientMessage(st,false);
        }
    }
    void update_palette(){
        //if(mode!= Mode.DIRECTION)
            slot=0;
        if(palette!=null) {
            palette_slots.clear();
            ListTag palette_inv = palette.getOrCreateTag().getList("Palette", MCVer.NbtType.COMPOUND);
            //log("palette_inv: "+palette_inv);
            int s = palette_inv.size();
            for (int i = 0; i < s; i++) {
                CompoundTag stackTag = (CompoundTag) palette_inv.get(i);
                ItemStack stack = ItemStack.of(stackTag.getCompound("Block"));
                if (!stack.isEmpty()) {
                    Block blk = Block.byItem(stack.getItem());
                    if (blk != Blocks.AIR) {
                        PaletteSlot psl = new PaletteSlot(i, blk.defaultBlockState(), stack);
                        if (palette_slots.stream().noneMatch(pp -> (pp.stack.sameItem(stack)))) {
                            block_accounting.put(stack.getItem(), new BlockAccounting());
                        }
                        palette_slots.add(psl);
                    }
                }
            }
        }
    }
    public boolean add_to_buffer(int x, int y, int z) {
        if (!level.isOutsideBuildHeight(y)&& block_buffer.get_length() < limit){
            BlockState st = level.getBlockState(tmp_pos.set(x, y, z));            
            if (destroy || replace||use) {
                if(!st.isAir()||mode==Mode.AREA) {
                    return block_buffer.add(x, y, z, this);
                }
            } else {
                if (can_place(st,tmp_pos))
                    return block_buffer.add(x, y, z, this);
            }            
        }else{
            limit_reached=true;
        }
        return false;
    }
    void consume_xp(){
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
                player.experienceProgress=prog;
            }
        }
    }
    public boolean state_in_slot(BlockState bs){
        boolean cond=false;
        if(has_palette) {
            for (Wand.PaletteSlot slot: palette_slots) {
                if(bs.equals(slot.state)){
                    cond=true;
                    break;
                }
            }
        }
        return cond;
    }
    public void update_tools(){
        ListTag tag = wand_stack.getOrCreateTag().getList("Tools", MCVer.NbtType.COMPOUND);
        digger_item_slot =-1;
        for (int i = 0; i < tag.size() && i<9; i++) {
            CompoundTag stackTag = (CompoundTag) tag.get(i);
            tools[i] = ItemStack.of(stackTag.getCompound("Tool"));
            if(!creative && preview){
                int dmg=tools[i].getMaxDamage()- tools[i].getDamageValue();
                if(dmg>1 && can_dig(block_state, true, tools[i])) {
                    if(digger_item_slot==-1)
                        digger_item_slot = i;
                }
            }
        }
    }
    public boolean can_destroy(BlockState state,boolean check_speed){
        digger_item =null;
        for (int i = 0; i<tools.length; i++) {
            int dmg=tools[i].getMaxDamage()- tools[i].getDamageValue();
            if(tools[i]!=null && !tools[i].isEmpty() && dmg>1) {
                if(can_dig(state, check_speed, tools[i])){
                    if(digger_item==null) {
                        digger_item = tools[i];
                        digger_item_slot = i;
                    }
                    return true;
                }
            }
        }
        return false;
    }
    boolean can_dig(BlockState state,boolean check_speed,ItemStack digger){
        boolean is_glass=state.getBlock() instanceof AbstractGlassBlock;
        boolean is_snow_layer=false;
        boolean can_shear=false;
        Block blk=state.getBlock();
        if(blk instanceof SnowLayerBlock){
            is_snow_layer= state.getValue(SnowLayerBlock.LAYERS)==1;
        }
        Item item_digger=digger.getItem();
        if(digger!=null && !digger.isEmpty() &&(item_digger instanceof DiggerItem ||item_digger instanceof ShearsItem) ){
            boolean is_allowed=false;
            if (item_digger instanceof ShearsItem) {
                can_shear=state.is(BlockTags.LEAVES) ||
                        state.is(Blocks.COBWEB) ||
                        state.is(Blocks.GRASS) ||
                        state.is(Blocks.FERN) ||
                        state.is(Blocks.DEAD_BUSH) ||
                        state.is(Blocks.HANGING_ROOTS) ||
                        state.is(Blocks.VINE) ||
                        state.is(Blocks.TRIPWIRE) ||
                        state.is(BlockTags.WOOL);
                is_allowed = is_allowed || WandsConfig.shears_allowed.contains(blk);
            }else{
                if(item_digger instanceof PickaxeItem){
                    is_allowed= is_allowed || WandsConfig.pickaxe_allowed.contains(blk);
                }else {
                    if (item_digger instanceof AxeItem) {
                        is_allowed = is_allowed || WandsConfig.axe_allowed.contains(blk);
                    } else {
                        if (item_digger instanceof ShovelItem) {
                            is_allowed = is_allowed || WandsConfig.shovel_allowed.contains(blk);
                        } else {
                            if (item_digger instanceof HoeItem) {
                               is_allowed = is_allowed || WandsConfig.hoe_allowed.contains(blk);
                            }
                        }
                    }
                }
            }
            if(check_speed){
                float s=item_digger.getDestroySpeed(digger, state);
                boolean cc=item_digger.isCorrectToolForDrops(state);
                return creative || (item_digger.getDestroySpeed(digger, state) > 1.0f
                        && (item_digger.isCorrectToolForDrops(state)))
                        || is_glass || is_snow_layer || is_allowed ||can_shear;
            }else{
                return true;
            }
        }
        return false;
    }
    int add_neighbour(BlockPos bpos, BlockState state) {

        BlockPos pos2 = bpos.relative(side);
        if (!block_buffer.in_buffer(pos2)) {
            BlockState bs1 = level.getBlockState(bpos);
            BlockState bs2 = level.getBlockState(pos2);
            if (block_buffer.get_length() < limit &&
                    (
                      (match_state && bs1.equals(state)) ||
                      (!match_state && bs1.getBlock().equals(state.getBlock())) ||
                      state_in_slot(bs1)
                    ) &&
                    (((destroy ||replace) && bs2.isAir()) || can_place(bs2,pos2)))
            {
                //block_buffer.add(pos2, this);
                add_to_buffer(pos2.getX(),pos2.getY(),pos2.getZ());
                return 1;
            }
        }
        return 0;
    }
    int find_neighbours(BlockPos bpos, BlockState state,int limit) {
        int found=0;
        boolean diag=WandItem.getAreaDiagonalSpread(wand_stack);
        if (side == Direction.UP || side == Direction.DOWN) {
            BlockPos p0 = bpos.relative( Direction.EAST, 1);
            found+=add_neighbour(p0, state);
            if(found>= limit)
                return found;

            p0 = bpos.relative( Direction.NORTH, 1);
            found+=add_neighbour(p0, state);
            if(found>= limit)
                return found;

            p0 = bpos.relative( Direction.WEST, 1);
            found+=add_neighbour(p0, state);
            if(found>= limit)
                return found;

            p0 = bpos.relative( Direction.SOUTH, 1);
            found+=add_neighbour(p0, state);
            if(found>= limit)
                return found;
            if(!diag) {
                p0 = bpos.relative(Direction.EAST, 1);
                BlockPos p1 = p0.relative(Direction.NORTH, 1);
                found += add_neighbour(p1, state);
                if (found >= limit)
                    return found;

                p0 = bpos.relative(Direction.NORTH, 1);
                p1 = p0.relative(Direction.WEST, 1);
                found += add_neighbour(p1, state);
                if (found >= limit)
                    return found;

                p0 = bpos.relative(Direction.SOUTH, 1);
                p1 = p0.relative(Direction.WEST, 1);
                found += add_neighbour(p1, state);
                if (found >= limit)
                    return found;

                p0 = bpos.relative(Direction.SOUTH, 1);
                p1 = p0.relative(Direction.EAST, 1);
                found += add_neighbour(p1, state);
                if (found >= limit)
                    return found;
            }

        } else {
            if (side == Direction.EAST || side == Direction.WEST) {
                BlockPos p0 = bpos.relative( Direction.UP, 1);
                found+=add_neighbour(p0, state);
                if(found>= limit)
                    return found;

                p0 = bpos.relative( Direction.NORTH, 1);
                found+=add_neighbour(p0, state);
                if(found>= limit)
                    return found;

                p0 = bpos.relative( Direction.DOWN, 1);
                found+=add_neighbour(p0, state);
                if(found>= limit)
                    return found;

                p0 = bpos.relative( Direction.SOUTH, 1);
                found+=add_neighbour(p0, state);
                if(found>= limit)
                    return found;

                if(!diag) {
                    p0 = bpos.relative( Direction.UP, 1);
                    BlockPos p1 = p0.relative( Direction.NORTH, 1);
                    found+=add_neighbour(p1, state);
                    if(found>= limit)
                        return found;

                    p0 = bpos.relative( Direction.NORTH, 1);
                    p1 = p0.relative( Direction.DOWN, 1);
                    found+=add_neighbour(p1, state);
                    if(found>= limit)
                        return found;

                    p0 = bpos.relative( Direction.SOUTH, 1);
                    p1 = p0.relative( Direction.DOWN, 1);
                    found+=add_neighbour(p1, state);
                    if(found>= limit)
                        return found;

                    p0 = bpos.relative( Direction.SOUTH, 1);
                    p1 = p0.relative( Direction.UP, 1);
                    found+=add_neighbour(p1, state);
                    if(found>= limit)
                        return found;
                }

            } else if (side == Direction.NORTH || side == Direction.SOUTH) {
                BlockPos p0 = bpos.relative( Direction.EAST, 1);
                found+=add_neighbour(p0, state);
                if(found>= limit)
                    return found;

                p0 = bpos.relative( Direction.UP, 1);
                found+=add_neighbour(p0, state);
                if(found>= limit)
                    return found;

                p0 = bpos.relative( Direction.WEST, 1);
                found+=add_neighbour(p0, state);
                if(found>= limit)
                    return found;

                p0 = bpos.relative( Direction.DOWN, 1);
                found+=add_neighbour(p0, state);
                if(found>= limit)
                    return found;

                if(!diag) {
                    p0 = bpos.relative( Direction.EAST, 1);
                    BlockPos p1 = p0.relative( Direction.UP, 1);
                    found+=add_neighbour(p1, state);
                    if(found>= limit)
                        return found;
                    p0 = bpos.relative( Direction.UP, 1);
                    p1 = p0.relative( Direction.WEST, 1);
                    found+=add_neighbour(p1, state);
                    if(found>= limit)
                        return found;
                    p0 = bpos.relative( Direction.DOWN, 1);
                    p1 = p0.relative( Direction.WEST, 1);
                    found+=add_neighbour(p1, state);
                    if(found>= limit)
                        return found;
                    p0 = bpos.relative(Direction.DOWN, 1);
                    p1 = p0.relative(Direction.EAST, 1);
                    found += add_neighbour(p1, state);
                    if (found >= limit)
                        return found;
                }
            }
        }
        return found;
    }

    static public boolean is_plant(BlockState state) {
        return (state.getBlock() instanceof BushBlock)|| (state.getBlock() instanceof VineBlock);
    }
    boolean replace_fluid(BlockState state){
        if(wand_item.removes_water && wand_item.removes_lava){
            return state.getFluidState().is(FluidTags.WATER)||state.getFluidState().is(FluidTags.LAVA);
        }else if(wand_item.removes_water){
            return state.getFluidState().is(FluidTags.WATER);
        }
        return false;
    }
    boolean can_place(BlockState state,BlockPos p) {
        if(offhand_state!=null && is_plant(offhand_state)){
            return (state.isAir() || replace_fluid(state)) && offhand_state.canSurvive(level,p);
        }else{
            return (
                    state.isAir() || replace_fluid(state) || is_plant(state)||
                    state.getBlock() instanceof SnowLayerBlock||
                    (has_empty_bucket && state.getFluidState().is(FluidTags.WATER)) ||
                    (has_empty_bucket && state.getFluidState().is(FluidTags.LAVA))
            );
        }
    }

    BlockPos find_next_diag(BlockState state, Direction dir1, Direction dir2, BlockPos bpos) {
        BlockPos p0=bpos;
        for (int i = 0; i < limit; i++) {
            BlockPos p1 = bpos.relative(dir1);
            bpos = p1.relative(dir2);
            BlockState bs = level.getBlockState(bpos);
            if (bs != null) {
                if(destroy || use || replace){
                    if (p0!=pos && !(bs.equals(state) || state_in_slot(bs) /* ||(offhand_state!=null&&  bs.is(offhand_state.getBlock()))*/)&& p0!=null)
                        return p0;
                }else{
                    if (can_place(bs,bpos)) {
                        return bpos;
                    } else {
                        if (!(bs.equals(state) || state_in_slot(bs) ||(offhand_state!=null&&  bs.is(offhand_state.getBlock()))))
                            return null;
                    }
                }
            }
            p0=bpos;
        }
        return null;
    }
    BlockPos find_next_pos(BlockState state, Direction dir, BlockPos bpos) {
        for (int i = 0; i < limit; i++) {
            BlockPos pos2 = bpos.relative(dir, i + 1);
            BlockState bs = level.getBlockState(pos2);

            if (bs != null) {
                if (!(bs.is(state.getBlock())|| state_in_slot(bs) )) {
                    if(destroy || use || replace){
                        pos2=bpos.relative(dir, i);
                        if(pos2!=pos)
                            return pos2;
                    }else{
                        if (can_place(bs,pos2)) {
                            return pos2;
                        } else {
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }
}
