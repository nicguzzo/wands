package net.nicguzzo.wands;



import dev.architectury.utils.NbtType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

class WandUtils{

    static public boolean is_shulker(ItemStack item_stack){        
		return Block.byItem(item_stack.getItem()) instanceof ShulkerBoxBlock ;
    }
    static public int count_in_shulker(ItemStack shulker, Item item){
        int n=0;
        if(item!=null){
            CompoundTag entity_tag =shulker.getTagElement("BlockEntityTag");
            if(entity_tag!=null){
                ListTag shulker_items = entity_tag.getList("Items", NbtType.COMPOUND);
                if(shulker_items!=null){
                    for (int i = 0, len = shulker_items.size(); i < len; ++i) {
                        CompoundTag itemTag = shulker_items.getCompound(i);                        
                        ItemStack s = ItemStack.of(itemTag);
                        if(!s.isEmpty() && s.getItem()== item){
                            n+=s.getCount();
                        }							
                    }
                }
            }
        }
        return n;
    }
    static public boolean can_destroy(Player player,BlockState block_state,boolean check_speed){
        ItemStack offhand_item=player.getOffhandItem();
        
        boolean is_glass=block_state.getBlock() instanceof AbstractGlassBlock;
        boolean is_snow_layer=false;
        if(block_state.getBlock() instanceof SnowLayerBlock){
            is_snow_layer= block_state.getValue(SnowLayerBlock.LAYERS)==1;
        }
        if(offhand_item!=null && !offhand_item.isEmpty() &&offhand_item.getItem() instanceof DiggerItem){
            if(check_speed){
                DiggerItem mt=(DiggerItem)offhand_item.getItem();
                if(mt!=null) {
                    return player.getAbilities().instabuild || mt.getDestroySpeed(null, block_state) > 1.0f || is_glass|| is_snow_layer;
                }
            }else{
                return true;
            }
        }        
        return false;
    }
    static public double unitCoord(double a) {
        double b = a - ((int) a);
        if (b < 0)
            b = 1.0f + b;
        return b;
    }
    static public boolean is_plant(BlockState state) {
        return (state.getBlock() instanceof BushBlock);
    }
    static public boolean is_fluid(BlockState state,boolean water, boolean lava){
        if(water && lava){                
            return state.getFluidState().is(FluidTags.WATER)||state.getFluidState().is(FluidTags.LAVA);
        }else if(water){
            return state.getFluidState().is(FluidTags.WATER);
        }    
        return false;        
    }    
    static public boolean can_place(BlockState state,boolean water, boolean lava) {
        return (state.isAir() || is_fluid(state,water,lava) || is_plant(state) || state.getBlock() instanceof SnowLayerBlock);
    }

    static public BlockPos find_next_diag(Level world, BlockState block_state, Direction dir1, Direction dir2, BlockPos pos,
    WandItem wand,boolean destroy,BlockState offhand_state,Wand w) {
        BlockPos p0=pos;
        for (int i = 0; i < wand.limit; i++) {
            BlockPos p1 = pos.relative(dir1);
            pos = p1.relative(dir2);
            BlockState bs = world.getBlockState(pos);
            if (bs != null) {
                if(destroy){
                    if (!(bs.equals(block_state) ||(offhand_state!=null&&  bs.is(offhand_state.getBlock())))&& p0!=null)
                        return p0;
                }else{
                    if (can_place(bs,wand.removes_water,wand.removes_lava)) {
                        return pos;
                    } else {
                        if (!(bs.equals(block_state) || w.state_in_slot(bs) ||(offhand_state!=null&&  bs.is(offhand_state.getBlock()))))
                            return null;
                    }
                }
            }
            p0=pos;
        }
        return null;
    }
    static public BlockPos find_next_pos(Level world, BlockState block_state, Direction dir, BlockPos pos, WandItem wand,boolean destroy,BlockState offhand_state,Wand w) {
        for (int i = 0; i < wand.limit; i++) {
            BlockPos pos2 = pos.relative(dir, i + 1);
            BlockState bs = world.getBlockState(pos2);
            
            if (bs != null) {
                if (!(bs.is(block_state.getBlock())|| w.state_in_slot(bs)||  (offhand_state!=null&&  bs.is(offhand_state.getBlock()))  )) {
                    if(destroy){
                        return pos.relative(dir, i);
                    }else{
                        if (can_place(bs,wand.removes_water,wand.removes_lava)) {
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
    static public int add_neighbour(BlockBuffer block_buffer,WandItem wand,BlockPos pos, BlockState block_state, Level world, Direction side,Wand w) {
        BlockPos pos2 = pos.relative(side);
        if (!block_buffer.in_buffer(pos2)) {
            BlockState bs1 = world.getBlockState(pos);
            BlockState bs2 = world.getBlockState(pos2);
            if (block_buffer.get_length() < w.wand_item.limit &&
                    (bs1.equals(block_state) || w.state_in_slot(bs1)) &&
                    can_place(bs2, wand.removes_water, wand.removes_lava))
            {
                block_buffer.add(pos2, w);
                return 1;
            }
        }
        return 0;
    }
    static public int find_neighbours(BlockBuffer block_buffer,WandItem wand, BlockPos pos, BlockState block_state, Level world, Direction side,Wand w) {
        int found=0;
		if (side == Direction.UP || side == Direction.DOWN) {
			BlockPos p0 = pos.relative( Direction.EAST, 1);
			found+=add_neighbour(block_buffer,wand, p0, block_state, world, side,w);
			if(found>= wand.limit)
			    return found;

			p0 = pos.relative( Direction.EAST, 1);
			BlockPos p1 = p0.relative( Direction.NORTH, 1);
            found+=add_neighbour(block_buffer,wand, p1, block_state, world, side,w);
            if(found>= wand.limit)
                return found;

			p0 = pos.relative( Direction.NORTH, 1);
            found+=add_neighbour(block_buffer,wand, p0, block_state, world, side,w);
            if(found>= wand.limit)
                return found;

			p0 = pos.relative( Direction.NORTH, 1);
			p1 = p0.relative( Direction.WEST, 1);
            found+=add_neighbour(block_buffer,wand, p1, block_state, world, side,w);
            if(found>= wand.limit)
                return found;

			p0 = pos.relative( Direction.WEST, 1);
            found+=add_neighbour(block_buffer,wand, p0, block_state, world, side,w);
            if(found>= wand.limit)
                return found;

			p0 = pos.relative( Direction.SOUTH, 1);
			p1 = p0.relative( Direction.WEST, 1);
            found+=add_neighbour(block_buffer,wand, p1, block_state, world, side,w);
            if(found>= wand.limit)
                return found;

			p0 = pos.relative( Direction.SOUTH, 1);
            found+=add_neighbour(block_buffer,wand, p0, block_state, world, side,w);
            if(found>= wand.limit)
                return found;

			p0 = pos.relative( Direction.SOUTH, 1);
			p1 = p0.relative( Direction.EAST, 1);
            found+=add_neighbour(block_buffer,wand, p1, block_state, world, side,w);
            if(found>= wand.limit)
                return found;

		} else {
			if (side == Direction.EAST || side == Direction.WEST) {
				BlockPos p0 = pos.relative( Direction.UP, 1);
                found+=add_neighbour(block_buffer,wand, p0, block_state, world, side,w);
                if(found>= wand.limit)
                    return found;

				p0 = pos.relative( Direction.UP, 1);
				BlockPos p1 = p0.relative( Direction.NORTH, 1);
                found+=add_neighbour(block_buffer,wand, p1, block_state, world, side,w);
                if(found>= wand.limit)
                    return found;

				p0 = pos.relative( Direction.NORTH, 1);
                found+=add_neighbour(block_buffer,wand, p0, block_state, world, side,w);
                if(found>= wand.limit)
                    return found;

				p0 = pos.relative( Direction.NORTH, 1);
				p1 = p0.relative( Direction.DOWN, 1);
                found+=add_neighbour(block_buffer,wand, p1, block_state, world, side,w);
                if(found>= wand.limit)
                    return found;

				p0 = pos.relative( Direction.DOWN, 1);
                found+=add_neighbour(block_buffer,wand, p0, block_state, world, side,w);
                if(found>= wand.limit)
                    return found;

				p0 = pos.relative( Direction.SOUTH, 1);
				p1 = p0.relative( Direction.DOWN, 1);
                found+=add_neighbour(block_buffer,wand, p1, block_state, world, side,w);
                if(found>= wand.limit)
                    return found;

				p0 = pos.relative( Direction.SOUTH, 1);
                found+=add_neighbour(block_buffer,wand, p0, block_state, world, side,w);
                if(found>= wand.limit)
                    return found;

				p0 = pos.relative( Direction.SOUTH, 1);
				p1 = p0.relative( Direction.UP, 1);
                found+=add_neighbour(block_buffer,wand, p1, block_state, world, side,w);
                if(found>= wand.limit)
                    return found;

			} else if (side == Direction.NORTH || side == Direction.SOUTH) {
				BlockPos p0 = pos.relative( Direction.EAST, 1);
                found+=add_neighbour(block_buffer,wand, p0, block_state, world, side,w);
                if(found>= wand.limit)
                    return found;

				p0 = pos.relative( Direction.EAST, 1);
				BlockPos p1 = p0.relative( Direction.UP, 1);
                found+=add_neighbour(block_buffer,wand, p1, block_state, world, side,w);
                if(found>= wand.limit)
                    return found;

				p0 = pos.relative( Direction.UP, 1);
                found+=add_neighbour(block_buffer,wand, p0, block_state, world, side,w);
                if(found>= wand.limit)
                    return found;

				p0 = pos.relative( Direction.UP, 1);
				p1 = p0.relative( Direction.WEST, 1);
                found+=add_neighbour(block_buffer,wand, p1, block_state, world, side,w);
                if(found>= wand.limit)
                    return found;

				p0 = pos.relative( Direction.WEST, 1);
                found+=add_neighbour(block_buffer,wand, p0, block_state, world, side,w);
                if(found>= wand.limit)
                    return found;

				p0 = pos.relative( Direction.DOWN, 1);
				p1 = p0.relative( Direction.WEST, 1);
                found+=add_neighbour(block_buffer,wand, p1, block_state, world, side,w);
                if(found>= wand.limit)
                    return found;

				p0 = pos.relative( Direction.DOWN, 1);
                found+=add_neighbour(block_buffer,wand, p0, block_state, world, side,w);
                if(found>= wand.limit)
                    return found;

				p0 = pos.relative( Direction.DOWN, 1);
				p1 = p0.relative( Direction.EAST, 1);
                found+=add_neighbour(block_buffer,wand, p1, block_state, world, side,w);
                if(found>= wand.limit)
                    return found;

			}
		}
		return found;
	}
    public static float calc_xp(final int level,float prog) {
        float xp=calc_xp_level(level);
        if(prog>0){							
            xp=xp+ prog * (calc_xp_level(level+1)-xp);
        }
        return xp;
    }
    public static float calc_xp_level(final int level) {
        float xp_points = 0;
        final int level2 = level * level;
        if(level>=32){
            xp_points=4.5f*level2 - 162.5f *level + 2220.0f;
        }else if(level>=17){
            xp_points=2.5f*level2 - 40.5f *level + 360.0f;
        }else {
            xp_points=level2 + 6*level;
        }
        return xp_points;
    }
    public static int calc_xp_to_next_level(int level){
        int xp=0;
        if(level>=32){
            xp = 9 * level - 158;	
        }else if(level>=17){
            xp = 5 * level - 38 ;	
        }else {
            xp = 2  *level + 7 ;	
        }
        return xp;
    }
}