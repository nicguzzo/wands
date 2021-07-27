package net.nicguzzo.wands;
import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
public class WandItem extends Item{
    static class PreviewInfo {
        public static float x=0.0f;
        public static float y=0.0f;
        public static float z=0.0f;
        public static float x1=0.0f;
        public static float y1=0.0f;
        public static float z1=0.0f;
        public static float x2=0.0f;
        public static float y2=0.0f;
        public static float z2=0.0f;
        public static BlockPos p1 =null;// new BlockPos.MutableBlockPos();
        public static BlockPos p2 =null;// new BlockPos.MutableBlockPos();
        public static BlockState p1_state=null;
        public static float h = 1.0f;
        public static float y0 = 0.0f;
        public static Direction side = Direction.UP;
        public static boolean valid=false;
        static public void clear(){
            p1=null;
            p2=null;
            //p1_state=null;
            valid=false;
            h = 1.0f;
            y0 = 0.0f;
            WandsMod.LOGGER.info("wand cleared");
        }
    };
    
    
    public enum Orientation {
        HORIZONTAL, VERTICAL
    }
    public enum Plane {
        XZ,XY,YZ
    }
    
    public int limit = 0;
    public boolean removes_water;
    public boolean removes_lava;
    static private final int max_mode=5;
    
    
    //private static Orientation orientation = Orientation.HORIZONTAL;
    //private static Plane plane = Plane.XZ;
    public WandItem(int limit,boolean removes_water,boolean removes_lava,Properties properties) {
        super(properties);
        this.limit=limit;
        this.removes_lava=removes_lava;
        this.removes_water=removes_water;
    }
    static public int getMode(ItemStack stack) {
        if(stack!=null && !stack.isEmpty())
            return stack.getOrCreateTag().getInt("mode");
        return -1;
    }   
    
    static public void nextMode(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int mode=(tag.getInt("mode")+1) % (max_mode+1);        
            tag.putInt("mode", mode);
        }
    }
    
    static public boolean isInverted(ItemStack stack) {
        if(stack!=null && !stack.isEmpty())
            return stack.getOrCreateTag().getBoolean("inverted");
        return false;
    }
    static public void invert(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            boolean inverted=tag.getBoolean("inverted");
            tag.putBoolean("inverted", !inverted);
        }
    }
    static public Orientation getOrientation(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            int o=stack.getOrCreateTag().getInt("orientation");
            return Orientation.values()[o];
        }
        return Orientation.HORIZONTAL;
    }
    static public void nextOrientation(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int o=(tag.getInt("orientation")+1) %2;
            tag.putInt("orientation", o);
        }
    }
    static public Plane getPlane(ItemStack stack) {
        Plane plane=Plane.XZ;
        if(stack!=null && !stack.isEmpty()){
            int p=stack.getOrCreateTag().getInt("plane");
            if(p>=0 && p< Plane.values().length)
                plane=Plane.values()[p];
        }
        return plane;
    }
    static public void nextPlane(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int plane=(tag.getInt("plane")+1) % 3;
            tag.putInt("plane", plane);
        }
    }
    static public void toggleCircleFill(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            boolean cfill=tag.getBoolean("cfill");
            tag.putBoolean("cfill", !cfill);
        }
    }
    //TODO: send feedback to player
    static public boolean isCircleFill(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            return stack.getOrCreateTag().getBoolean("cfill");
        }
        return false;
    }
 
    @Override
    public InteractionResult useOn(UseOnContext context) {    
        WandsMod.LOGGER.info("UseOn");
        Level world=context.getLevel();
        if(!world.isClientSide()){
            PlayerWandInfo s_info=PlayerWandInfo.get(context.getPlayer());
            if(s_info==null){
                PlayerWandInfo.add_player(context.getPlayer());
                s_info=PlayerWandInfo.get(context.getPlayer());
            }
            
            ItemStack stack = context.getPlayer().getMainHandItem();//check anyway...
            if (stack!=null && !stack.isEmpty() && stack.getItem() instanceof WandItem) {
                //context.getPlayer()
                Vec3 hit = context.getClickLocation();
                BlockPos pos = context.getClickedPos();
                Direction side = context.getClickedFace();
                BlockState block_state = world.getBlockState(pos);
                //s_info.slots_tmp.clear();
                do_or_preview(context.getPlayer(),world, block_state, pos, side, hit, stack,s_info.block_buffer,s_info);
            }
        }else{
            ItemStack stack = context.getPlayer().getMainHandItem();
            int mode=getMode(stack);
            if(mode==2||mode==4||mode==5){
                if(PreviewInfo.p1==null){
                    PreviewInfo.clear();
                    PreviewInfo.p1=context.getClickedPos();
                    PreviewInfo.p1_state=world.getBlockState(PreviewInfo.p1);
                    PreviewInfo.valid = true;
                    PreviewInfo.x1=PreviewInfo.p1.getX();
                    PreviewInfo.y1=PreviewInfo.p1.getY();
                    PreviewInfo.z1=PreviewInfo.p1.getZ();
                    WandsMod.LOGGER.info("cli pos1 "+PreviewInfo.p1);
                }else{
                    PreviewInfo.p1=null;                    
                    PreviewInfo.p2=null;
                    PreviewInfo.valid = false;
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public  InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {   
        WandsMod.LOGGER.info("use");     
        
        if(!world.isClientSide){
            PlayerWandInfo s_info=PlayerWandInfo.get(player);
            if(s_info==null){
                PlayerWandInfo.add_player(player);
                s_info=PlayerWandInfo.get(player);
            }
            s_info.p1=null;
            s_info.p2=null;
            s_info.p1_state=null;

        }else{
            PreviewInfo.clear();
        }
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }

    static public void do_or_preview(Player player,Level world,BlockState block_state,BlockPos pos,Direction side,
                                     Vec3 hit,ItemStack wand_stack,BlockBuffer block_buffer,PlayerWandInfo s_info){
        boolean preview=player.level.isClientSide();
        
        //BlockState offhand_state=null;
        float y0 = 0.0f;
        float block_height=1.0f;        
        boolean is_slab_top = false;
        boolean is_slab_bottom = false;
        WandItem wand=(WandItem)wand_stack.getItem();
        int mode=getMode(wand_stack);
        if (block_state.getBlock() instanceof SlabBlock) {
            if(!preview&& s_info!=null){
                s_info.is_double_slab = block_state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE;
            }
            is_slab_top    = block_state.getValue(SlabBlock.TYPE) == SlabType.TOP;
            is_slab_bottom = block_state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
        }else{
            if (block_state.getBlock() instanceof CrossCollisionBlock) {
                if(mode==0){
                    return;
                }
            }
        }
        //TODO: check snow...
        if(is_slab_top || is_slab_bottom){
            block_height=0.5f;
            if(is_slab_top){
                y0=0.5f;
            }
        }
        if(preview){
            PreviewInfo.y0=y0;
            PreviewInfo.h=block_height;
        }

        PreviewInfo.valid = false;
        boolean destroy=WandUtils.can_destroy(player,block_state);

        ItemStack offhand=player.getOffhandItem();
        //Block offhand_block=null;
        ItemStack item_stack=null;
        
        if(WandUtils.is_shulker(offhand)){
            return;
        }
        
        if(!preview){            
            if(s_info!=null){
                s_info.palette=null;
                if(offhand.getItem() instanceof PaletteItem){
                    if(mode>0){
                        s_info.palette=offhand;                
                    }
                }
            }
        }

        //offhand_block=Block.byItem(offhand.getItem());
        //if(offhand_block != Blocks.AIR){
        //    block_state=offhand_block.defaultBlockState();
        //    item_stack=offhand;
        //}
        if(item_stack==null){
            item_stack=Item.byBlock(block_state.getBlock()).getDefaultInstance();
        }
        //if(item_stack.isEmpty()){
        //    return;
        //}
        if(WandUtils.is_shulker(item_stack)){
            return;
        }
        
        //int n_items=WandUtils.count_in_player(player, item_stack);

        int placed=0;
        switch(mode){
            case 0:
                placed+=mode0(player,block_state, pos, side, block_height, y0, hit, null,wand_stack,destroy,s_info);
            break;
            case 1:
                placed+=mode1(player,block_state, pos, side, block_height, y0, hit, null,wand_stack,destroy,s_info);
            break;
            case 2:
                placed+=mode2(player, pos, block_state,wand,destroy,s_info);
            break;
            case 3:
                placed+=mode3(player, block_buffer, wand, pos, block_state, side, destroy,s_info);
            break;
            case 4:
                placed+=mode4(player, block_state, pos, block_buffer, preview, destroy, s_info);
            break;
            case 5:
                int plane=WandItem.getPlane(wand_stack).ordinal();
                boolean fill=WandItem.isCircleFill(wand_stack);
                placed+=mode5(player, block_state, pos, block_buffer, preview, destroy,plane,fill ,s_info);
            break;
        }
        PreviewInfo.p1_state=block_state;
        if(!preview && placed>0){
            WandsMod.LOGGER.info("placed: "+placed);
            
            if(s_info!=null && s_info.sound_block_state!=null){
                block_state=s_info.sound_block_state;
            }
            
            FriendlyByteBuf packet=new FriendlyByteBuf(Unpooled.buffer());
            packet.writeBlockPos(pos);
            packet.writeBoolean(destroy);
            NetworkManager.sendToPlayer((ServerPlayer)player, WandsMod.SND_PACKET, packet);
            //NetworkManager.sendToC(WandsMod.SND_PACKET, packet);
            //SoundType sound_type = block_state.getBlock().getSoundType(block_state);
            //SoundEvent sound=sound_type.getPlaceSound();
            //player.level.playSound(null,pos,sound,SoundSource.BLOCKS, 1.0f, 1.0f);            
        }
    }

    static public int mode0(Player player,BlockState block_state,BlockPos pos,Direction side,
    float block_height,float y0,Vec3 hit,BlockState offhand_state,ItemStack wand_stack,boolean destroy,PlayerWandInfo s_info){
        boolean preview=player.level.isClientSide();
        WandItem wand=(WandItem)wand_stack.getItem();
        boolean invert =WandItem.isInverted(wand_stack) ;

        Direction dirs[] = getDirMode0(side, y0, block_height,hit.x, hit.y, hit.z);
        if (invert) {
            if (dirs[0] != null)
                dirs[0] = dirs[0].getOpposite();
            if (dirs[1] != null)
                dirs[1] = dirs[1].getOpposite();
        }
        Direction d1 = dirs[0];
        Direction d2 = dirs[1];
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
            PreviewInfo.x=x;
            PreviewInfo.y=y;
            PreviewInfo.z=z;
            PreviewInfo.side=side;
            PreviewInfo.h=block_height;
            PreviewInfo.y0=y0;
        }
        if (d1 != null) {
            BlockPos dest = null;
            if (d2 != null) {
                dest = WandUtils.find_next_diag(player.level, block_state, d1, d2, pos, wand,destroy,offhand_state);
            } else {
                dest = WandUtils.find_next_pos(player.level, block_state, d1, pos, wand,destroy,offhand_state);
            }
            if (dest != null) {
                if(preview){
                    PreviewInfo.x1=dest.getX();
                    PreviewInfo.y1=dest.getY();
                    PreviewInfo.z1=dest.getZ();
                    PreviewInfo.x2=WandItem.PreviewInfo.x1+1;
                    PreviewInfo.y2=WandItem.PreviewInfo.y1+1;
                    PreviewInfo.z2=WandItem.PreviewInfo.z1+1;
                    PreviewInfo.valid = true;
                }else{                    
                    if(place_block(player, block_state, dest,destroy,s_info)){                    
                        return 1;
                    }
                }
            }
        }
        return 0;
    }    
    static public int mode1(Player player,BlockState block_state,BlockPos pos,Direction side,float block_height,float y0,Vec3 hit,BlockState offhand_state,ItemStack wand_stack,boolean destroy,PlayerWandInfo s_info){
        boolean preview=player.level.isClientSide();
		Direction dir = Direction.EAST;
		BlockPos pos_m = pos.relative(side, 1);
		BlockState state = player.level.getBlockState(pos_m);
        WandItem wand=(WandItem)wand_stack.getItem();        

		if (state.isAir() || WandUtils.is_fluid(state, wand.removes_water,wand.removes_lava) ||destroy) {
			BlockPos pos0 = pos;
			BlockPos pos1 = pos_m;
			BlockPos pos2 = pos;
			BlockPos pos3 = pos_m;
			int offx = 0;
			int offy = 0;
			int offz = 0;
            Orientation orientation=getOrientation(wand_stack);
			switch (side) {
			case UP:
			case DOWN:
				switch (orientation) {
				case HORIZONTAL:
					dir = Direction.SOUTH;
					offz = -1;
					break;
				case VERTICAL:
					dir = Direction.EAST;
					offx = -1;
					break;
				}
				break;
			case SOUTH:
			case NORTH:
				switch (orientation) {
				case HORIZONTAL:
					dir = Direction.EAST;
					offx = -1;
					break;
				case VERTICAL:
					dir = Direction.UP;
					offy = -1;
					break;
				}
				break;
			case EAST:
			case WEST:
				switch (orientation) {
				case HORIZONTAL:
					dir = Direction.SOUTH;
					offz = -1;
					break;
				case VERTICAL:
					dir = Direction.UP;
					offy = -1;
					break;
				}
				break;
			}

			Direction op = dir.getOpposite();
			int i = wand.limit - 1;
			int k = 0;
			boolean stop1 = false;
			boolean stop2 = false;
			//boolean intersects = false;
        
			
			boolean dont_check_state=false;
			boolean eq = false;
			while (k < wand.limit && i > 0) {
				if (!stop1 && i > 0) {
					BlockState bs0 = player.level.getBlockState(pos0.relative( dir));
					BlockState bs1 = player.level.getBlockState(pos1.relative( dir));
					if (dont_check_state) {
						eq = bs0.getBlock().equals(block_state.getBlock());
					} else {
						eq = bs0.equals(block_state);
					}
					if (eq && (bs1.isAir() || WandUtils.is_fluid(bs1, wand.removes_water,wand.removes_lava))) {
						pos0 = pos0.relative( dir);
						pos1 = pos1.relative( dir);
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
					if (eq && (bs3.isAir() || WandUtils.is_fluid(bs3, wand.removes_water,wand.removes_lava))) {
						pos2 = pos2.relative(op);
						pos3 = pos3.relative(op);
						i--;
					} else {
						stop2 = true;
					}
				}
				if(!destroy){
					//if (WandsMod.compat.interescts_player_bb(player, pos1.getX(), pos1.getY(), pos1.getZ(), pos1.getX() + 1,
					//		pos1.getY() + 1, pos1.getZ() + 1)) {
					//	intersects = true;
					//	break;
					//}
					//if (WandsMod.compat.interescts_player_bb(player, pos3.getX(), pos3.getY(), pos3.getZ(), pos3.getX() + 1,
					//		pos3.getY() + 1, pos3.getZ() + 1)) {
					//	intersects = true;
					//	break;
					//}
				}
				k++;
				if (stop1 && stop2) {
					k = 1000000;
				}
			}
			if(destroy){
				pos1=pos1.relative(side.getOpposite());
				pos3=pos3.relative(side.getOpposite());
			}
            if(preview){
                PreviewInfo.x1=pos1.getX() - offx;
                PreviewInfo.y1=pos1.getY() - offy;
                PreviewInfo.z1=pos1.getZ() - offz;
                PreviewInfo.x2=pos3.getX() + offx +1 ;
                PreviewInfo.y2=pos3.getY() + offy +1 ;
                PreviewInfo.z2=pos3.getZ() + offz +1 ;
                PreviewInfo.valid = true;
            }else{
                return fill(player, block_state, pos1, pos2, wand,destroy,s_info);
            }
		} else {
            PreviewInfo.valid = false;
		}
        return 0;
	}
    static public int mode2(Player player,BlockPos pos, BlockState block_state,WandItem wand,boolean destroy,PlayerWandInfo s_info){
        boolean preview=player.level.isClientSide();
        if(preview ){
            if(PreviewInfo.p1!=null){     
                PreviewInfo.x1 = PreviewInfo.p1.getX();
                PreviewInfo.y1 = PreviewInfo.p1.getY();
                PreviewInfo.z1 = PreviewInfo.p1.getZ();
                PreviewInfo.x2 = pos.getX();
                PreviewInfo.y2 = pos.getY();
                PreviewInfo.z2 = pos.getZ();
                if (!PreviewInfo.p1.equals(pos)) {
                    if (PreviewInfo.x1 >= PreviewInfo.x2) {
                        PreviewInfo.x1 += 1;
                    } else {
                        PreviewInfo.x2 += 1;
                    }
                    if (PreviewInfo.y1 >= PreviewInfo.y2) {
                        PreviewInfo.y1 += 1;
                    } else {
                        PreviewInfo.y2 += 1;
                    }
                    if (PreviewInfo.z1 >= PreviewInfo.z2) {
                        PreviewInfo.z1 += 1;
                    } else {
                        PreviewInfo.z2 += 1;
                    }
                } else {
                    PreviewInfo.x2 = PreviewInfo.x1 + 1;
                    PreviewInfo.y2 = PreviewInfo.y1 + 1;
                    PreviewInfo.z2 = PreviewInfo.z1 + 1;
                }
                PreviewInfo.valid = true;
            }
        }else{            
            if(s_info!=null){
                if(s_info.p1==null){
                    s_info.p1=pos;
                    s_info.p2=null;
                    s_info.p1_state=block_state;
                    s_info.sound_block_state=block_state;
                }else{
                    s_info.p2=pos;
                }
                if(s_info.p1!=null && s_info.p2!=null && s_info.p1_state!=null){
                    WandsMod.LOGGER.info("fill!");
                    int placed= fill(player, s_info.p1_state, s_info.p1, s_info.p2, wand,destroy,s_info);                
                    s_info.p1=null;
                    s_info.p2=null;
                    return placed;
                }
            }
        }
        return 0;
    }
    static public int mode3(Player player,BlockBuffer block_buffer,WandItem wand,BlockPos pos, BlockState block_state, Direction side,boolean destroy,PlayerWandInfo s_info) {
        boolean preview=player.level.isClientSide();
		block_buffer.length = 0;
		WandUtils.add_neighbour(block_buffer,wand, pos, block_state, player.level, side);
		int i = 0;		
		int placed=0;
		while (i < wand.limit && i < PlayerWandInfo.MAX_LIMIT) {
			if (i < block_buffer.length) {
				BlockPos p = block_buffer.get(i).relative(side,-1);
				WandUtils.find_neighbours(block_buffer,wand, p, block_state, player.level, side);
			}
			i++;
		}
		if(destroy){
			for (int a = 0; a < block_buffer.length; a++) {
				block_buffer.set(a,block_buffer.get(i).relative(side,-1));
			}
		}
        placed=from_buffer(player, block_state, block_buffer, preview, destroy, s_info);
        return placed;
	}
    static public int from_buffer(Player player,BlockState block_state,BlockBuffer block_buffer,boolean preview,boolean destroy,PlayerWandInfo s_info){
        int placed=0;
        if(preview){
            PreviewInfo.valid = (block_buffer.length > 0);
        }else{
            for (int a = 0; a < block_buffer.length && a< PlayerWandInfo.MAX_LIMIT; a++) {
                if(place_block(player, block_state,block_buffer.get(a),destroy,s_info)){
                    placed++;
                }
            }
        }
        return placed;
    }
    static public int fill(Player player,BlockState block_state,BlockPos from,BlockPos to,WandItem wand,boolean destroy,PlayerWandInfo s_info){
        int placed=0;
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
        
        int limit=PlayerWandInfo.MAX_LIMIT;
        if(!player.abilities.instabuild){
            limit=wand.limit;
        }
        
        int ll=((xe-xs)+1)*((ye-ys)+1)*((ze-zs)+1);
        
        if(ll <= limit){
            BlockPos.MutableBlockPos bp=new BlockPos.MutableBlockPos();
            for (int z = zs; z <= ze; z++) {					
                for (int y = ys; y <= ye; y++) {						
                    for (int x = xs; x <= xe; x++) {								
                        if(place_block(player,block_state, bp.set(x, y, z),destroy,s_info)){
                            placed++;
                        }
                    }
                }
            }
        }
        
        return placed;
    }
    static public boolean place_block(Player player,BlockState block_state,BlockPos pos,boolean destroy,PlayerWandInfo s_info){
        Level level=player.level;
        boolean creative=player.abilities.instabuild;
        if(level.isClientSide){
            return false;
        }
        ItemStack item_stack = null;                        
        int count[]=null;
        int count_in_player=0;
        int count_in_shulker=0;
        int n = 1;
        if(s_info.palette!=null && !destroy){
            item_stack=PaletteItem.get_item(s_info.palette, s_info,player);
            if(!item_stack.isEmpty()){
                block_state=Block.byItem(item_stack.getItem()).defaultBlockState();
                PaletteItem.PaletteMode palatte_mode=PaletteItem.getMode(s_info.palette);            
                if (palatte_mode == PaletteItem.PaletteMode.RANDOM ){
                    if (block_state.getBlock() instanceof SnowLayerBlock) {
                        n = player.level.random.nextInt(7) + 1;
                        block_state=block_state.setValue(SnowLayerBlock.LAYERS, n);
                    }
                    block_state.rotate(Rotation.getRandom(level.random));
                }	
            }else{
                if(!creative){
                    return false;
                }
            }        
        }
        if(s_info.palette==null && !destroy){
            ItemStack offhand=player.getOffhandItem();
            Block offhand_block=Block.byItem(offhand.getItem());
            if(offhand_block != Blocks.AIR){
                block_state=offhand_block.defaultBlockState();
                item_stack=offhand;
            }		
        }
        
        if(!creative && !destroy){
            
            if (item_stack == null){
                item_stack = new ItemStack(block_state.getBlock());
            }
            count=WandUtils.count_in_player(player, item_stack);
            if (block_state.getBlock() instanceof SlabBlock) {
                if(block_state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE){
                    n=2;
                }
            }
            count_in_player=count[0];
            count_in_shulker=count[1];
        }
        WandItem wand=(WandItem)player.getMainHandItem().getItem();
        
        if(!destroy){
            BlockState state=player.level.getBlockState(pos);
            if(!WandUtils.can_place(state, wand.removes_water,wand.removes_lava)){
                return false;
            }
        }
        
        if(creative){
            //TODO: undo keybinding
            if(s_info!=null && s_info.undo_buffer!=null){
                s_info.undo_buffer.put(pos, block_state,destroy);
            }
            if(destroy){
                if(level.destroyBlock(pos, false)){
                    return true;
                }
            }else{
                if(level.setBlockAndUpdate(pos, block_state)){
                    return true;
                }
            }
        }else{
            if (destroy || (count_in_player+count_in_shulker)>=n) {
                boolean placed=false;
                float xp = WandUtils.calc_xp(player.experienceLevel,player.experienceProgress);
                float dec = 0.0f;            
                float BLOCKS_PER_XP=WandsMod.config.blocks_per_xp;
                if (BLOCKS_PER_XP != 0) {
                    dec = (1.0f / BLOCKS_PER_XP);
                }
                ItemStack wand_stack=player.getMainHandItem();
                ItemStack offhand=player.getOffhandItem();
                int wand_durability=wand_stack.getMaxDamage()-wand_stack.getDamageValue();
                //System.out.println("wand_stack.getDamage() "+wand_durability);
                if ((wand_durability>1 || WandsMod.config.allow_wand_to_break) && (BLOCKS_PER_XP == 0 || (xp - dec) >= 0)) {
                    if(destroy){
                        BlockState st=level.getBlockState(pos);
                        if(WandUtils.can_destroy(player,st)){
                            int offhand_durability=offhand.getMaxDamage()-offhand.getDamageValue();
                            if(offhand_durability>1 || WandsMod.config.allow_offhand_to_break){
                                placed=level.destroyBlock(pos, false);
                                if(placed && WandsMod.config.destroy_in_survival_drop){
                                    int silk_touch =EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, offhand);
                                    int fortune=EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, offhand);
                                    System.out.println("offhand "+fortune);
                                    System.out.println("offhand "+silk_touch);
                                    if(fortune>0 || silk_touch>0){                                    
                                        st.getBlock().playerDestroy(level, player, pos, st, null, offhand);
                                    }
                                }
                            }
                        }
                    }else{
                        boolean is_tool=offhand!=null && !offhand.isEmpty() && offhand.getItem() instanceof DiggerItem;
                        if(!is_tool){
                        //System.out.println("slot "+slot);
		                
                        int player_inv_size=player.inventory.getContainerSize();
                        ItemStack stack2 = null;
                        /*
                        //count item in shulkers and in main inv
                        for (int i = 0; i < player_inv_size; ++i) {
                            stack2 = player.inventory.getItem(i);
                            if(stack2!=null && WandUtils.is_shulker(stack2)){
                                count_in_shulker+=WandUtils.count_in_shulker( stack2, item_stack);
                            }
                            if (stack2!=null && item_stack!=null 
                                && !stack2.isEmpty() && item_stack.getItem() == stack2.getItem()
                                && stack2.getCount() >0) 
                            {
                                count_in_player+=stack2.getCount();				
                            }
                        }*/
                        //WandsMod.LOGGER.info("count_in_player: "+count_in_player);
                        //WandsMod.LOGGER.info("count_in_shulker: "+count_in_shulker);
                    
                        if (level.setBlockAndUpdate(pos, block_state)){
                            int removed=0;
                            if(count_in_shulker>0){//try shulkers first
                                for (int i = 0; i < player_inv_size; ++i) {
                                    stack2 = player.inventory.getItem(i);
                                    if(stack2!=null && WandUtils.is_shulker(stack2)){
                                        removed=WandUtils.remove_item_from_shulker(stack2, item_stack, n);
                                    }
                                }
                            }
                            if(removed<n){
                                for (int i = 0; i < player_inv_size; ++i) {
                                    stack2 = player.inventory.getItem(i);												
                                    if (stack2!=null && item_stack!=null 
                                        && !stack2.isEmpty() && item_stack.getItem() == stack2.getItem()
                                        && stack2.getCount() > 0) 
                                    {
                                        if(stack2.getCount() >=n){
                                            player.inventory.items.get(i).shrink(n);
                                            removed=n;
                                        }else{
                                            player.inventory.items.get(i).shrink(1);
                                            removed++;
                                        }
                                        if(removed==n)
                                            break;
                                    }
                                }
                            }
                            placed=removed==n;
                            if(!placed){
                                placed=level.destroyBlock(pos, false);
                            }
                        }
                        }
                    }
                }
                //WandsMod.LOGGER.info("placed"+placed);
                if (placed) {
                    if(destroy){                        
                        offhand.hurtAndBreak(1, (LivingEntity)player, 
                            (Consumer<LivingEntity>)((p) -> {                                
                                    ((LivingEntity)p).broadcastBreakEvent(InteractionHand.OFF_HAND);
                                }
                            )
					    );
                    }
                    wand_stack.hurtAndBreak(1, (LivingEntity)player, 
                        (Consumer<LivingEntity>)((p) -> {                                
                                ((LivingEntity)p).broadcastBreakEvent(InteractionHand.MAIN_HAND);
                            }
                        )
                    );
                    if (BLOCKS_PER_XP != 0) {
                        float diff = WandUtils.calc_xp_to_next_level(player.experienceLevel);
                        float prog = player.experienceProgress;
                        if (diff > 0 && BLOCKS_PER_XP != 0.0f) {
                            float a = (1.0f / diff) / BLOCKS_PER_XP;
                            if (prog - a > 0) {
                                prog = prog - a;
                            } else {
                                if (prog > 0.0f) {
                                    // TODO: dirty solution....
                                    prog = 1.0f + (a - prog);
                                } else {
                                    prog = 1.0f;
                                }
                                if (player.experienceLevel > 0) {
                                    player.experienceLevel--;
                                    diff = WandUtils.calc_xp_to_next_level(player.experienceLevel);
                                    a = (1.0f / diff) / BLOCKS_PER_XP;
                                    if (prog - a > 0) {
                                        prog = prog - a;
                                    }
                                }
                                
                                //WandsMod.compat.send_xp_to_player(player);									
                            }
                        }
                    }
                }
				
			}
        }
        return false;
    }
    static public Direction[] getDirMode0(Direction side, float y0, float h, double hit_x, double hit_y, double hit_z) {
        Direction ret[] = new Direction[2];
        ret[0] = null;
        ret[1] = null;
        float x = WandUtils.unitCoord((float) hit_x);
        float y = WandUtils.unitCoord((float) hit_y);
        float z = WandUtils.unitCoord((float) hit_z);
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
    //line
    public static int mode4(Player player,BlockState block_state,BlockPos pos,BlockBuffer block_buffer,boolean preview,boolean destroy,PlayerWandInfo s_info)
    {        
        block_buffer.length=0;
        int x1=0;
        int y1=0;
        int z1=0;
        int x2=0;
        int y2=0;
        int z2=0;
        if(preview ){
            if(PreviewInfo.p1!=null){     
                x1=PreviewInfo.p1.getX();
                y1=PreviewInfo.p1.getY();
                z1=PreviewInfo.p1.getZ();
                x2=pos.getX();
                y2=pos.getY();
                z2=pos.getZ();
                PreviewInfo.valid=true;
            }else{
                return 0;
            }
        }else{
            if(s_info!=null){
                if(s_info.p1==null){
                    s_info.p1=pos;
                    x1=pos.getX();
                    y1=pos.getY();
                    z1=pos.getZ();
                    s_info.p1_state=block_state;
                    return 0;
                }else{
                    x1=s_info.p1.getX();
                    y1=s_info.p1.getY();
                    z1=s_info.p1.getZ();
                    x2=pos.getX();
                    y2=pos.getY();
                    z2=pos.getZ();
                    s_info.p1=null;
                }
            }else{
                return 0;
            }
        }
        
        int dx,dy,dz,xs,ys,zs,p1,p2;
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
        block_buffer.add(x1,y1,z1);
        // X
        if (dx >= dy && dx >= dz) {
            p1 = 2 * dy - dx;
            p2 = 2 * dz - dx;
            while (x1 != x2) {
                x1 += xs;
                if (p1 >= 0) {
                    y1 += ys;
                    p1 -= 2 * dx;
                }
                if (p2 >= 0) {
                    z1 += zs;
                    p2 -= 2 * dx;
                }
                p1 += 2 * dy ;
                p2 += 2 * dz ;
                block_buffer.add(x1,y1,z1);
            }
        } else if (dy >= dx && dy >= dz) {
            p1 = 2 * dx - dy;
            p2 = 2 * dz - dy;
            while (y1 != y2) {
                y1 += ys;
                if (p1 >= 0) {
                    x1 += xs;
                    p1 -= 2 * dy;
                }
                if (p2 >= 0) {
                    z1 += zs;
                    p2 -= 2 * dy;
                }
                p1 += 2 * dx ;
                p2 += 2 * dz ;
                block_buffer.add(x1,y1,z1);
            }
        } else {
            p1 = 2 * dy - dz;
            p2 = 2 * dx - dz;
            while (z1 != z2) {
                z1 += zs;
                if (p1 >= 0) {
                    y1 += ys;
                    p1 -= 2 * dz;
                }
                if (p2 >= 0) {
                    x1 += xs;
                    p2 -= 2 * dz;
                }
                p1 += 2 * dy ;
                p2 += 2 * dx ;
                block_buffer.add(x1,y1,z1);
            }
        }
        BlockState bs=(preview? PreviewInfo.p1_state: s_info.p1_state);
        return from_buffer(player, bs, block_buffer, preview, destroy, s_info);
    }
    public static int mode5(Player player,BlockState block_state,BlockPos pos,BlockBuffer block_buffer,boolean preview,boolean destroy,int plane,boolean fill,PlayerWandInfo s_info){
        block_buffer.length=0;
        
        int xc=0; //pos0.getX();
        int yc=0; //pos0.getY();
        int zc=0; //pos0.getZ();
        int px=0; //pos1.getX()-pos0.getX();
        int py=0; //pos1.getY()-pos0.getY();
        int pz=0; //pos1.getZ()-pos0.getZ();
        if(preview ){
            if(PreviewInfo.p1!=null){     
                xc=PreviewInfo.p1.getX();
                yc=PreviewInfo.p1.getY();
                zc=PreviewInfo.p1.getZ();
                px=pos.getX()-xc;
                py=pos.getY()-yc;
                pz=pos.getZ()-zc;
                PreviewInfo.valid=true;
            }else{
                return 0;
            }
            //WandsMod.LOGGER.info("circle  plane:"+plane+ " fill: "+fill);
        }else{
            if(s_info!=null){
                if(s_info.p1==null){
                    s_info.p1=pos;
                    xc=pos.getX();
                    yc=pos.getY();
                    zc=pos.getZ();
                    s_info.p1_state=block_state;
                    return 0;
                }else{
                    xc=s_info.p1.getX();
                    yc=s_info.p1.getY();
                    zc=s_info.p1.getZ();
                    px=pos.getX()-xc;
                    py=pos.getY()-yc;
                    pz=pos.getZ()-zc;
                    s_info.p1=null;
                }
                WandsMod.LOGGER.info("circle  plane:"+plane+ " fill: "+fill);
            }else{
                return 0;
            }
        }
        int r =(int)Math.sqrt(px*px+py*py+pz*pz);
        block_buffer.length=0;
        
        if(plane==0){//XZ;
            int x = 0, y=0, z = r;
            int d = 3 - 2 * r;
            drawCircle(block_buffer,xc, yc, zc, x, y, z, plane);
            
            while (z >= x)
            {
                x++;
                if (d > 0)
                {
                    z--;
                    d = d + 4 * (x - z) + 10;
                } else
                    d = d + 4 * x + 6;
                    drawCircle(block_buffer,xc, yc, zc, x, y, z, plane);
            }
            if(fill){
				int r2=r*r;
				//BlockPos.MutableBlockPos bp=new BlockPos.MutableBlockPos();
                if(!preview ){
                    WandsMod.LOGGER.info("xc: "+xc);
                    WandsMod.LOGGER.info("yc: "+yc);
                    WandsMod.LOGGER.info("zc: "+zc);
                    WandsMod.LOGGER.info("r: "+r);
                    WandsMod.LOGGER.info("r2: "+r2);
                }
				for (z = -r; z <= r; z++){
					for (x = -r; x <= r; x++){
                        int det=(x * x) + (z * z);
                        if(!preview ){
                            //WandsMod.LOGGER.info("x:"+x +" z:"+z+" x*x+x*x:"+ ((x * x) + (z * z))+ " r2: "+r2);
                        }
						if (det <= r2){
                            //bp.set(xc+x, yc, zc + z);
                            //if(!preview ){
                                //WandsMod.LOGGER.info("in circle "+bp);
                            //}
                            block_buffer.add( xc+x, yc, zc+z);
						}
					}
				}			
			}
        } else if (plane == 1) {// XY;
            int x = 0, y = r, z = 0;
            int d = 3 - 2 * r;
            drawCircle(block_buffer,xc, yc, zc, x, y, z, plane);
            while (y >= x)
            {
                x++;
                if (d > 0)
                {
                    y--;
                    d = d + 4 * (x - y) + 10;
                } else
                    d = d + 4 * x + 6;
                    drawCircle(block_buffer,xc, yc, zc, x, y, z, plane);
            }
            if(fill){
				int r2=r*r;
				BlockPos.MutableBlockPos bp=new BlockPos.MutableBlockPos();
				for (y = -r; y <= r; y++){
					for (x = -r; x <= r; x++){
						if ((x * x) + (y * y) <= r2){
                            block_buffer.add(bp.set(xc+x, yc+y, zc));
						}
					}
				}			
			}
        } else if (plane == 2) {// YZ;
            int x = 0, y = 0, z = r;
            int d = 3 - 2 * r;
            drawCircle(block_buffer,xc, yc, zc, x, y, z, plane);
            while (z >= y)
            {
                y++;
                if (d > 0)
                {
                    z--;
                    d = d + 4 * (y - z) + 10;
                } else
                    d = d + 4 * y + 6;
                drawCircle(block_buffer,xc, yc, zc, x, y, z, plane);
            }
            if(fill){
				int r2=r*r;
				BlockPos.MutableBlockPos bp=new BlockPos.MutableBlockPos();
				for (z = -r; z <= r; z++){
					for (y = -r; y <= r; y++){
						if ((y * y) + (z * z) <= r2){
                            block_buffer.add(bp.set(xc, yc+y, zc + z));
						}
					}
				}			
			}
        }
        BlockState bs=(preview? PreviewInfo.p1_state: s_info.p1_state);
        return from_buffer(player,bs, block_buffer, preview, destroy, s_info);
    }    

    static private void drawCircle(BlockBuffer block_buffer,int xc, int yc,int zc, int x, int y,int z,int plane)
    {
        switch(plane){
            case 0://XZ
                block_buffer.add(  xc+x, yc, zc+z);
                block_buffer.add(  xc-x, yc, zc+z);
                block_buffer.add(  xc+x, yc, zc-z);
                block_buffer.add(  xc-x, yc, zc-z);
                block_buffer.add(  xc+z, yc, zc+x);
                block_buffer.add(  xc-z, yc, zc+x);
                block_buffer.add(  xc+z, yc, zc-x);
                block_buffer.add(  xc-z, yc, zc-x);
                break;
            case 1://XY
                block_buffer.add(xc+x, yc+y, zc);
                block_buffer.add(xc-x, yc+y, zc);
                block_buffer.add(xc+x, yc-y, zc);
                block_buffer.add(xc-x, yc-y, zc);
                block_buffer.add(xc+y, yc+x, zc);
                block_buffer.add(xc-y, yc+x, zc);
                block_buffer.add(xc+y, yc-x, zc);
                block_buffer.add(xc-y, yc-x, zc);
                break;
            case 2://YZ
                block_buffer.add(xc, yc+y, zc+z);
                block_buffer.add(xc, yc-y, zc+z);
                block_buffer.add(xc, yc+y, zc-z);
                block_buffer.add(xc, yc-y, zc-z);
                block_buffer.add(xc, yc+z, zc+y);
                block_buffer.add(xc, yc-z, zc+y);
                block_buffer.add(xc, yc+z, zc-y);
                block_buffer.add(xc, yc-z, zc-y);
                break;
        }
    }
    static public void undo(PlayerWandInfo s_info, Level level, int n) {		
        if(s_info!=null){
            CircularBuffer u = s_info.undo_buffer;
            if (u != null) {
                for (int i = 0; i < n && i < u.size(); i++) {
                    CircularBuffer.P p = u.pop();
                    if (p != null) {
                        if(!p.destroyed){
                            level.setBlockAndUpdate(p.pos, Blocks.AIR.defaultBlockState());
                        }else{
                            level.setBlockAndUpdate(p.pos, p.state);
                        }
                    }
                }
                // u.print();
            }
        }
	}

	static public void redo(PlayerWandInfo s_info, Level level, int n) {
		if(s_info!=null){
            CircularBuffer u = s_info.undo_buffer;
            if (u != null) {
                for (int i = 0; i < n && u.can_go_forward(); i++) {
                    u.forward();
                    CircularBuffer.P p = u.peek();
                    if (p != null && p.pos != null && p.state != null) {
                        if(!p.destroyed){						
                            level.setBlockAndUpdate(p.pos, p.state);
                        }else{
                            level.setBlockAndUpdate(p.pos, Blocks.AIR.defaultBlockState());
                        }
                    }
                }
                // u.print();
            }
        }
	}
}
