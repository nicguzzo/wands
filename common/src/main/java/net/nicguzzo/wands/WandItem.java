package net.nicguzzo.wands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

public class WandItem extends Item{
    static class PreviewInfo {
        public static float x=0.0f;
        public static float y=0.0f;
        public static float z=0.0f;
        public static BlockPos p1 = new BlockPos.MutableBlockPos();
        public static BlockPos p2 = new BlockPos.MutableBlockPos();
        public static float h = 1.0f;
        public static float y0 = 0.0f;
        public static Direction side = Direction.UP;
    };
    private static final Logger LOGGER = LogManager.getLogger();
    public enum Orientation {
        HORIZONTAL, VERTICAL
    }
    public enum Plane {
        XZ,XY,YZ
    }
    
    private int limit = 0;
    private boolean removes_water;
    private boolean removes_lava;
    
    private static Orientation orientation = Orientation.HORIZONTAL;
    private static Plane plane = Plane.XZ;
    public WandItem(int limit,boolean removes_water,boolean removes_lava,Properties properties) {
        super(properties);
        this.limit=limit;
        this.removes_lava=removes_lava;
        this.removes_water=removes_water;
    }
    static public int getMode(ItemStack stack) {
        int mode=stack.getOrCreateTag().getInt("mode");
        return mode;
    }
    /*
    static public boolean getInvert() {
        return invert;
    }

    static public void toggleInvert() {
        String state = "off";
        invert = !invert;
        if (invert) {
            state = "on";
        }        
        send_message_to_player("Wand inverted " + state);
    }*/
    
    //static void send_message_to_player(String msg){
    //    assert Minecraft.getInstance().player != null;
    //    Minecraft instance=Minecraft.getInstance();
    //    instance.player.displayClientMessage(new TranslatableComponent(msg), true);
    //}

    public int getLimit() {
        return limit;
    }

    static public Orientation getOrientation() {
        return orientation;
    }

    static public Plane getPlane() {
        return plane;
    }
    @Override
    public InteractionResult useOn(UseOnContext context) {    
        Level world=context.getLevel();
        if(!world.isClientSide()){
            LOGGER.info("UseOn");
            ItemStack stack = context.getPlayer().getMainHandItem();//check anyway...
            if (stack!=null && !stack.isEmpty() && stack.getItem() instanceof WandItem) {
                //context.getPlayer()
                Vec3 hit = context.getClickLocation();
                BlockPos pos = context.getClickedPos();
                Direction side = context.getClickedFace();
                BlockState block_state = world.getBlockState(pos);

                do_or_preview(world, block_state, pos, side, hit, stack, false);
            }
        }
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public  InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {
        LOGGER.info("use");
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }

    public boolean do_or_preview(Level world,BlockState block_state,BlockPos pos,Direction side,Vec3 hit,ItemStack wand_stack,boolean preview){
        BlockState offhand_state=null;
        float y0 = 0.0f;
        float block_height=1.0f;
        boolean is_double_slab = false;
        boolean is_slab_top = false;
        boolean is_slab_bottom = false;
        if (block_state.getBlock() instanceof SlabBlock) {
            is_double_slab = block_state.getValue(SlabBlock .TYPE) == SlabType.DOUBLE;
            is_slab_top    = block_state.getValue(SlabBlock.TYPE) == SlabType.TOP;
            is_slab_bottom = block_state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
        }
        if(is_slab_top || is_slab_bottom){
            block_height=0.5f;
            if(is_slab_top){
                y0=0.5f;
            }
        }
        int mode=getMode(wand_stack);
        boolean ret=false;
        switch(mode){
            case 0:
                ret=mode0(world, block_state, pos, side, block_height, y0, hit, null, preview);
            break;

        }
        return ret;
    }

    public boolean mode0(Level world,BlockState block_state,BlockPos pos,Direction side,float block_height,float y0,Vec3 hit,BlockState offhand_state,boolean preview){

        boolean destroy=false;
        boolean invert =false;

        Direction dirs[] = getDirMode0(side, y0, block_height,hit.x, hit.y, hit.z);
        if (invert) {
            if (dirs[0] != null)
                dirs[0] = dirs[0].getOpposite();
            if (dirs[1] != null)
                dirs[1] = dirs[1].getOpposite();
        }
        Direction d1 = dirs[0];
        Direction d2 = dirs[1];

        if (d1 != null) {
            BlockPos pv = null;
            if (d2 != null) {
                pv = find_next_diag(world, block_state, d1, d2, pos, this,destroy,offhand_state);
            } else {
                pv = find_next_pos(world, block_state, d1, pos, this,destroy,offhand_state);
            }
            if (pv != null) {
                if(preview){
                    float x = pos.getX();
                    float y = pos.getY();
                    float z = pos.getZ();
                    float o = 0.01f;
                    switch (side) {
                        case UP:
                            y += block_height + o;
                            break;
                        case DOWN:
                            y -= o;
                            break;
                        case SOUTH:
                            z += 1 + o;
                            break;
                        case NORTH:
                            z -= o;
                            break;
                        case EAST:
                            x += 1 + o;
                            break;
                        case WEST:
                            x -= o;
                            break;
                    }
                    WandItem.PreviewInfo.x=x;
                    WandItem.PreviewInfo.y=y;
                    WandItem.PreviewInfo.z=z;
                    WandItem.PreviewInfo.side=side;
                    WandItem.PreviewInfo.h=block_height;
                    WandItem.PreviewInfo.y0=y0;
                    /*int x1 = pv.getX();
                    int y1 = pv.getY();
                    int z1 = pv.getZ();
                    int x2 = x1 + 1;
                    int y2 = y1 + 1;
                    int z2 = z1 + 1;                
                    if (WandsMod.compat.interescts_player_bb(player, x1, y1, z1, x2, y2, z2)) {
                        WandItem.valid = false;
                    } else {
                        WandItem.valid = true;
                        WandItem.x1 = x1;
                        WandItem.y1 = y1;
                        WandItem.z1 = z1;
                        WandItem.x2 = x2;
                        WandItem.y2 = y2;
                        WandItem.z2 = z2;                        
                        //preview(bufferBuilder, c.x+x1 , c.y+y1 , c.z+z1 , c.x+x2 , c.y+y2 ,c.z+ z2 );
                    
                    }*/

                }else{
                    world.setBlockAndUpdate(pv, block_state);
                }
                return true;
            }
        }	
        return false;
    }
    static public float unitCoord(float x) {
        float y = x - ((int) x);
        if (y < 0)
            y = 1.0f + y;
        return y;
    }
    public Direction[] getDirMode0(Direction side, float y0, float h, double hit_x, double hit_y, double hit_z) {
        Direction ret[] = new Direction[2];
        ret[0] = null;
        ret[1] = null;
        float x = unitCoord((float) hit_x);
        float y = unitCoord((float) hit_y);
        float z = unitCoord((float) hit_z);
        float a = 0.25f;
        float b = 0.75f;
        float a2 = y0 + a * h;
        float b2 = y0 + b * h;
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
                        if (x >= b) {
                            ret[0] = Direction.EAST;
                        }
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
                if (y <= a2) {
                    ret[0] = Direction.DOWN;
                } else {
                    if (y >= b2) {
                        ret[0] = Direction.UP;
                    } else {
                        ret[0] = side.getOpposite();
                    }
                }
            } else {
                if (y >= a2 && y <= b2) {
                    if (z <= a) {
                        ret[0] = Direction.NORTH;
                        return ret;
                    } else {
                        if (z >= b) {
                            ret[0] = Direction.SOUTH;
                            return ret;
                        }
                    }
                } else {
                    if (y <= a2 && z <= a) {
                        ret[0] = Direction.DOWN;
                        ret[1] = Direction.NORTH;
                    }
                    if (y >= b2 && z <= a) {
                        ret[0] = Direction.UP;
                        ret[1] = Direction.NORTH;
                    }
                    if (y >= b2 && z >= b) {
                        ret[0] = Direction.UP;
                        ret[1] = Direction.SOUTH;
                    }
                    if (y <= a2 && z >= b) {
                        ret[0] = Direction.DOWN;
                        ret[1] = Direction.SOUTH;
                    }
                }
            }
            break;
        case NORTH:
        case SOUTH:
            if (x >= a && x <= b) {
                if (y <= a2) {
                    ret[0] = Direction.DOWN;
                } else {
                    if (y >= b2) {
                        ret[0] = Direction.UP;
                    } else {
                        ret[0] = side.getOpposite();
                    }
                }
            } else {
                if (y >= a2 && y <= b2) {
                    if (x <= a) {
                        ret[0] = Direction.WEST;
                    } else {
                        if (x >= b2) {
                            ret[0] = Direction.EAST;
                        }
                    }
                } else {
                    if (y <= a2 && x <= a) {
                        ret[0] = Direction.DOWN;
                        ret[1] = Direction.WEST;
                    }
                    if (y >= b2 && x <= a) {
                        ret[0] = Direction.UP;
                        ret[1] = Direction.WEST;
                    }
                    if (y >= b2 && x >= b) {
                        ret[0] = Direction.UP;
                        ret[1] = Direction.EAST;
                    }
                    if (y <= a2 && x >= b) {
                        ret[0] = Direction.DOWN;
                        ret[1] = Direction.EAST;
                    }
                }
            }
            break;
        }
        
        return ret;
    }


    public boolean is_plant(BlockState state) {
        return (state.getBlock() instanceof BushBlock);
    }
    public boolean is_fluid(BlockState state){
        if(removes_water && removes_lava){                
            return state.getFluidState().is(FluidTags.WATER)||state.getFluidState().is(FluidTags.LAVA);
        }else if(removes_water){
            return state.getFluidState().is(FluidTags.WATER);
        }    
        return false;        
    }    
    public boolean can_place(BlockState state) {
        return (state.isAir() || is_fluid(state) || is_plant(state));
    }
    //static public BlockPos pos_offset(BlockPos pos,Direction dir,int o){        
    //    return new BlockPos(pos.getX() + dir.getOffsetX()* o, pos.getY() + dir.getOffsetY()*o, pos.getZ() + dir.getOffsetZ()*o);
    //}

    static private void add_neighbour(BlockBuffer block_buffer,WandItem wand,BlockPos pos, BlockState block_state, Level world, Direction side) {
        
        BlockPos pos2 = pos.relative(side);
        if (!block_buffer.in_buffer(pos2)) {
            BlockState bs1 = world.getBlockState(pos);
            BlockState bs2 = world.getBlockState(pos2);
            if (bs1.equals(block_state) && wand.can_place(bs2)) {
                block_buffer.add_buffer(pos2);
            }
        }
    }

    static public BlockPos find_next_diag(Level world, BlockState block_state, Direction dir1, Direction dir2, BlockPos pos,
    WandItem wand,boolean destroy,BlockState offhand_state) {
        BlockPos p0=pos;
        for (int i = 0; i < wand.getLimit(); i++) {
            BlockPos p1 = pos.relative(dir1);
            pos = p1.relative(dir2);
            BlockState bs = world.getBlockState(pos);
            if (bs != null) {
                if(destroy){
                    if (!(bs.equals(block_state) ||bs.equals(offhand_state))&& p0!=null)
                        return p0;
                }else{
                    if (wand.can_place(bs)) {
                        return pos;
                    } else {
                        if (!(bs.equals(block_state)||bs.equals(offhand_state)))
                            return null;
                    }
                }
            }
            p0=pos;
        }
        return null;
    }

    static public BlockPos find_next_pos(Level world, BlockState block_state, Direction dir, BlockPos pos, WandItem wand,boolean destroy,BlockState offhand_state) {
        for (int i = 0; i < wand.getLimit(); i++) {
            BlockPos pos2 = pos.relative(dir, i + 1);
            BlockState bs = world.getBlockState(pos2);
            
            if (bs != null) {
                if (!(bs.equals(block_state)||bs.equals(offhand_state))) {
                    if(destroy){
                        return pos.relative(dir, i);
                    }else{
                        if (wand.can_place(bs)) {
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
