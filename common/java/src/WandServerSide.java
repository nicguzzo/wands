package net.nicguzzo.common;

import java.util.Vector;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.nicguzzo.WandsMod;
import net.nicguzzo.common.WandsBaseRenderer.BlockBuffer;

import java.util.HashMap;

public class WandServerSide {
	
	private static final int MAX_UNDO = 2048;

	public static HashMap<String, CircularBuffer> player_undo = new HashMap<String, CircularBuffer>();

	public int slt;
	public PlayerEntity player;
	public Vector<Integer> slots;
	public BlockState state;
	public BlockState offhand_state=null;
	public BlockPos pos_state;
	public BlockPos pos0;
	public BlockPos pos1;
	public WandItem.PaletteMode palatte_mode;
	public boolean isCreative;
	public float experienceProgress;
	public ItemStack wand_stack;
	public ItemStack shulker = null;
	public boolean destroy =false;
	public int mode;
	public int plane;
	public World world;
	public MyDir side;
	ItemStack offhand;
	float BLOCKS_PER_XP = WandsMod.config.blocks_per_xp;

	public WandServerSide(World world,PlayerEntity player, BlockPos pos_state, BlockPos pos0, BlockPos pos1, int palatte_mode,
			boolean isCreative, float experienceProgress, ItemStack wand_stack, int mode, int plane,MyDir side) {
		this.slots = new Vector<Integer>();
		this.player = player;
		this.world = world;
		this.pos0 = pos0;
		this.pos1 = pos1;
		this.palatte_mode = WandItem.PaletteMode.values()[palatte_mode];
		this.state = world.getBlockState(pos_state);
		this.isCreative = isCreative;
		this.experienceProgress = experienceProgress;
		this.wand_stack = wand_stack;
		this.mode = mode;
		this.plane = plane;
		this.side=side;
		this.pos_state=pos_state;
		//System.out.println("state " + state);
		offhand = WandsMod.compat.get_player_offhand_stack(player);
		

		destroy=WandsMod.compat.can_destroy(state,offhand,isCreative);
		
		if(!destroy){
			if (WandsMod.compat.is_shulker(player, offhand)) {
				shulker = offhand;
			}
			if(shulker==null && this.mode==3 ){
				Block bbb=WandsMod.compat.block_from_item(offhand.getItem());
				//System.out.println("offhand "+offhand);
				//System.out.println("bbb "+bbb);
				if(bbb!=null && !(bbb instanceof AirBlock)){
					offhand_state=WandsMod.compat.getDefaultBlockState(bbb);
				}
				//System.out.println("offhand_state "+offhand_state);
			}
			if (this.mode<2 || this.palatte_mode == WandItem.PaletteMode.SAME) {
				if (shulker != null) {
					int sl=WandsMod.compat.in_shulker_slot(player, shulker, state);
					if (sl != -1) {					
						slots.add(sl);
					}
				}
			} else if (this.palatte_mode == WandItem.PaletteMode.RANDOM
					|| this.palatte_mode == WandItem.PaletteMode.ROUND_ROBIN) {
				if (shulker != null) {
					slots=WandsMod.compat.shulker_slots(player, shulker);
				} else {
					for (int i = 0; i < WandsMod.compat.get_main_inventory_size(player); ++i) {
						ItemStack stack2 = WandsMod.compat.get_player_main_stack(player, i);
						Block blk = WandsMod.compat.block_from_item(stack2.getItem());
						if (blk != null && blk != Blocks.AIR && !(blk instanceof ShulkerBoxBlock) && !WandsMod.compat.has_tag(stack2)) {
							slots.add(i);
						}
					}
				}
			}
		}
	}

	public void placeBlock() {
		//System.out.println("placeBlock");
		//System.out.println("pos_state "+pos_state);
		//System.out.println("pos0 "+pos0);
		//System.out.println("pos1 "+pos1);
		boolean placed=false;
		WandItem wand=WandsMod.compat.get_player_wand(player);
		if (mode==0 && pos0.equals(pos1)) {
			placed=place(pos0);
		}else if (mode == 3) {
			
			BlockBuffer bb=new BlockBuffer(wand.getLimit());			
			WandsBaseRenderer.mode3(bb,wand,pos_state, state, world, side,destroy);
			//System.out.println("length "+bb.length);
			for (int a = 0; a < bb.length; a++) {			
				BlockPos p=bb.buffer[a];
				placed= place(p)|| placed;
			}
		}else {
			
			if (mode == 4) {// line
				// System.out.println("Line! pos0 "+pos0+" pos1 "+pos1);
				placed=line();
			} else if (mode == 5) {// circle
				// System.out.println("circle! pos0 "+pos0+" pos1 "+pos1);
				placed=circle();
			} else {// box
				int xs, ys, zs, xe, ye, ze;
			
				if (pos0.getX() >= pos1.getX()) {
					xs = pos1.getX();
					xe = pos0.getX();
				} else {
					xs = pos0.getX();
					xe = pos1.getX();
				}
				if (pos0.getY() >= pos1.getY()) {
					ys = pos1.getY();
					ye = pos0.getY();
				} else {
					ys = pos0.getY();
					ye = pos1.getY();
				}
				if (pos0.getZ() >= pos1.getZ()) {
					zs = pos1.getZ();
					ze = pos0.getZ();
				} else {
					zs = pos0.getZ();
					ze = pos1.getZ();
				}
				
				int limit=32768;
				if(!isCreative){
					limit=wand.getLimit();
				}
				
				int ll=((xe-xs)+1)*((ye-ys)+1)*((ze-zs)+1);
				
				if(ll < limit){
					for (int z = zs; z <= ze; z++) {					
						for (int y = ys; y <= ye; y++) {						
							for (int x = xs; x <= xe; x++) {
								placed=place(new BlockPos(x, y, z));
							}
						}
					}
				}else{
					System.out.println("limit reached ");
				}
			}
		}
		slots = null;
		if(placed){
			WandsMod.compat.send_block_placed(player,pos_state,destroy);
			//WandsMod.compat.playBlockSound(player,state,pos,destroy);
		}
	}

	int nextSlot() {
		if (palatte_mode == WandItem.PaletteMode.RANDOM) {
			slt = WandsMod.compat.get_next_int_random(player, slots.size());
		} else if (palatte_mode == WandItem.PaletteMode.ROUND_ROBIN) {
			slt = (slt + 1) % slots.size();
		}
		return slt;
	}

	static public void undo(PlayerEntity player, int n) {		
		CircularBuffer u = player_undo.get(WandsMod.compat.get_player_uuid(player));
		if (u != null) {
			for (int i = 0; i < n && i < u.size(); i++) {
				CircularBuffer.P p = u.pop();
				if (p != null) {
					if(!p.destroyed){
						WandsMod.compat.setBlockState(WandsMod.compat.world(player),p.pos, WandsMod.compat.getDefaultBlockState( Blocks.AIR));
					}else{
						WandsMod.compat.setBlockState(WandsMod.compat.world(player),p.pos, p.state);	
					}
				}
			}
			// u.print();
		}
	}

	static public void redo(PlayerEntity player, int n) {
		CircularBuffer u = player_undo.get(WandsMod.compat.get_player_uuid(player));
		if (u != null) {
			// System.out.println("redo");
			for (int i = 0; i < n && u.can_go_forward(); i++) {
				// System.out.println("redo "+i);
				u.forward();
				CircularBuffer.P p = u.peek();
				if (p != null && p.pos != null && p.state != null) {
					if(!p.destroyed){						
						WandsMod.compat.setBlockState(WandsMod.compat.world(player),p.pos, p.state);
					}else{
						WandsMod.compat.setBlockState(WandsMod.compat.world(player),p.pos, WandsMod.compat.getDefaultBlockState( Blocks.AIR));
					}
				}
			}
			// u.print();
		}
	}

	private boolean place(BlockPos pos) {

		boolean placed = false;
		ItemStack item_stack = null;
		Block blk = null;
		
		if (mode==3 && offhand_state!=null) {
			item_stack = offhand;
		} else{
			if (slots.size() > 0) {
				if (mode != 0) {
					this.nextSlot();
				}
				if (shulker!=null) {
					item_stack =WandsMod.compat.item_from_shulker(shulker,slots.get(slt));
				} else {
					item_stack = WandsMod.compat.get_player_main_stack(player, slots.get(slt));
				}
			}
		}
		
		if (item_stack == null){
			item_stack = new ItemStack(state.getBlock());
		}
		blk = WandsMod.compat.block_from_item(item_stack.getItem());
		if (blk != null && palatte_mode == WandItem.PaletteMode.RANDOM){
			state = WandsMod.compat.random_rotate(WandsMod.compat.getDefaultBlockState(blk), world);				
		}
		//System.out.println("state " + state);
		//System.out.println("destroy " + destroy);

		Block block = state.getBlock();
		BlockState state2 = world.getBlockState(pos);
		int d = 1;
		WandItem wand = WandsMod.compat.get_player_wand(player);
		if (destroy || WandsBaseRenderer.can_place(state2, wand, world, pos)) {
			int slot = -1;

			if ((block instanceof PaneBlock) || (block instanceof FenceBlock)) {
				state = WandsMod.compat.getDefaultBlockState(state.getBlock());
			} else if (block instanceof SlabBlock) {
				if (WandsMod.compat.is_double_slab(state)) {
					d = 2;// should consume 2 if its a double slab
				}
			}
			if (palatte_mode == WandItem.PaletteMode.RANDOM && (block instanceof SnowBlock)) {
				d = WandsMod.compat.get_next_int_random(player, 7) + 1;
				
				state = WandsMod.compat.with_snow_layers(block, d);
			}			
			if (isCreative) {
				if (player_undo.get(WandsMod.compat.get_player_uuid(player)) == null) {
					player_undo.put(WandsMod.compat.get_player_uuid(player), new CircularBuffer(MAX_UNDO));
				}
				CircularBuffer u = player_undo.get(WandsMod.compat.get_player_uuid(player)	);
				u.put(pos, state,destroy);
				if(destroy){
					//placed=world.breakBlock(pos, false);
					placed=WandsMod.compat.destroy_block(world, pos, false);
					//placed=WandsMod.compat.setBlockState(world,pos, WandsMod.compat.getDefaultBlockState( Blocks.AIR));					
				}else{					
					if(offhand_state!=null){
						placed=WandsMod.compat.setBlockState(world,pos,offhand_state);
					}else{
						placed=WandsMod.compat.setBlockState(world,pos,state);
					}
				}
				/*if(placed)
					WandsMod.compat.playBlockSound(player,state,pos,destroy);*/
			} else {
				float xp = WandItem.calc_xp(player.experienceLevel, experienceProgress);
				float dec = 0.0f;
				// System.out.println("BLOCKS_PER_XP "+BLOCKS_PER_XP);
				// LOGGER.info("BLOCKS_PER_XP "+BLOCKS_PER_XP);
				if (BLOCKS_PER_XP != 0) {
					dec = (1.0f / BLOCKS_PER_XP);
				}
				if (BLOCKS_PER_XP == 0 || (xp - dec) > 0) {

					if(destroy){
						BlockState st=world.getBlockState(pos);
						if(WandsMod.compat.can_destroy(st,offhand,isCreative)){

							placed=WandsMod.compat.destroy_block(world, pos, false);
							
							if(placed && WandsMod.config.destroy_in_survival_drop){
								int silk_touch = WandsMod.compat.get_silk_touch_level(offhand);
								int fortune = WandsMod.compat.get_fortune_level(offhand);
								if(fortune>0 || silk_touch>0){
									//System.out.println("drop state "+st);
									WandsMod.compat.block_after_break(st.getBlock(),world, player, pos, state, offhand);
								}
								/*if(fortune==3){
									WandsMod.compat.dropStacks(state,world, pos);
								}
								if(silk_touch>0){
									//System.out.println("drop state "+st);
									if(st!=null){									
										ItemStack it=new ItemStack(st.getBlock());
										//System.out.println("drop item "+it);
										WandsMod.compat.dropStack(world, pos,it);
									}
								}*/
							}			
						}			
					}else{
						if (shulker != null && slots.size() > 0) {						
							placed = WandsMod.compat.setBlockState(world,pos,state);
							if (placed) {
								WandsMod.compat.remove_item_from_shulker(shulker,  slots.get(slt), d);
							}						
						} else {
							//ItemStack off_hand_stack = WandsMod.compat.get_player_offhand_stack(player);
							
							//System.out.println("item_stack "+item_stack);
							for (int i = 0; i < WandsMod.compat.get_main_inventory_size(player); ++i) {
								ItemStack stack2 = WandsMod.compat.get_player_main_stack(player, i);
								if (stack2!=null && item_stack!=null 
									&& !stack2.isEmpty() && item_stack.getItem() == stack2.getItem()
									&& stack2.getCount() >= d) 
								{
									slot = i;
								}
							}
							//System.out.println("slot "+slot);
							if (slot > -1) {
								if (offhand_state!=null) {
									placed = WandsMod.compat.setBlockState(world,pos,offhand_state);									
								} else{
									placed = WandsMod.compat.setBlockState(world,pos,state);
								}
								if (placed) {
									WandsMod.compat.player_stack_dec(player, slot, d);
								}
							}
						}
					}
					//System.out.println("placed"+placed);
					if (placed) {
						//WandsMod.compat.playBlockSound(player,state,pos,destroy);
						if(destroy){							
							WandsMod.compat.inc_wand_damage(player, offhand, 1);
						}
						WandsMod.compat.inc_wand_damage(player, wand_stack, 1);

						if (BLOCKS_PER_XP != 0) {
							float diff = WandItem.calc_xp_to_next_level(player.experienceLevel);
							float prog = experienceProgress;
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
										diff = WandItem.calc_xp_to_next_level(player.experienceLevel);
										a = (1.0f / diff) / BLOCKS_PER_XP;
										if (prog - a > 0) {
											prog = prog - a;
										}
									}
									WandsMod.compat.send_xp_to_player(player);									
								}
							}
						}
					}
				}
			}
		}
		return placed;
	}

	// bresenham 3d from
	// https://www.geeksforgeeks.org/bresenhams-algorithm-for-3-d-line-drawing/
	private boolean line() {
		boolean placed=false;
		int x1 = pos0.getX();
		int y1 = pos0.getY();
		int z1 = pos0.getZ();
		int x2 = pos1.getX();
		int y2 = pos1.getY();
		int z2 = pos1.getZ();
		int dx, dy, dz, xs, ys, zs, p1, p2;
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
		placed=place(new BlockPos(x1, y1, z1));
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
				p1 += 2 * dy;
				p2 += 2 * dz;

				placed=place(new BlockPos(x1, y1, z1)) ||placed ;
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
				p1 += 2 * dx;
				p2 += 2 * dz;
				placed=place(new BlockPos(x1, y1, z1))||placed ;
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
				p1 += 2 * dy;
				p2 += 2 * dx;
				placed=place(new BlockPos(x1, y1, z1))||placed ;
			}
		}
		return placed;
	}

	private boolean drawCircle(int xc, int yc, int zc, int x, int y, int z) {
		boolean placed=false;
		switch (plane) {
		case 0:
			placed=place(new BlockPos(xc + x, yc, zc + z))||placed ;
			placed=place(new BlockPos(xc - x, yc, zc + z))||placed ;
			placed=place(new BlockPos(xc + x, yc, zc - z))||placed ;
			placed=place(new BlockPos(xc - x, yc, zc - z))||placed ;
			placed=place(new BlockPos(xc + z, yc, zc + x))||placed ;
			placed=place(new BlockPos(xc - z, yc, zc + x))||placed ;
			placed=place(new BlockPos(xc + z, yc, zc - x))||placed ;
			placed=place(new BlockPos(xc - z, yc, zc - x))||placed ;

			break;
		case 1:
			placed=place(new BlockPos(xc + x, yc + y, zc))||placed ;
			placed=place(new BlockPos(xc - x, yc + y, zc))||placed ;
			placed=place(new BlockPos(xc + x, yc - y, zc))||placed ;
			placed=place(new BlockPos(xc - x, yc - y, zc))||placed ;
			placed=place(new BlockPos(xc + y, yc + x, zc))||placed ;
			placed=place(new BlockPos(xc - y, yc + x, zc))||placed ;
			placed=place(new BlockPos(xc + y, yc - x, zc))||placed ;
			placed=place(new BlockPos(xc - y, yc - x, zc))||placed ;
			break;
		case 2:
			placed=place(new BlockPos(xc, yc - y, zc + z))||placed ;
			placed=place(new BlockPos(xc, yc + y, zc + z))||placed ;
			placed=place(new BlockPos(xc, yc + y, zc - z))||placed ;
			placed=place(new BlockPos(xc, yc - y, zc - z))||placed ;
			placed=place(new BlockPos(xc, yc + z, zc + y))||placed ;
			placed=place(new BlockPos(xc, yc - z, zc + y))||placed ;
			placed=place(new BlockPos(xc, yc + z, zc - y))||placed ;
			placed=place(new BlockPos(xc, yc - z, zc - y))||placed ;
			break;
		}
		return placed;
	}

	boolean circle() {
		boolean placed=false;
		int r = 1;
		int xc = pos0.getX();
		int yc = pos0.getY();
		int zc = pos0.getZ();
		int px = pos1.getX() - pos0.getX();
		int py = pos1.getY() - pos0.getY();
		int pz = pos1.getZ() - pos0.getZ();
		r = (int) Math.sqrt(px * px + py * py + pz * pz);
		if (plane == 0) {// XZ;
			int x = 0, y = 0, z = r;
			int d = 3 - 2 * r;
			placed=drawCircle(xc, yc, zc, x, y, z)||placed ;
			while (z >= x) {
				x++;
				if (d > 0) {
					z--;
					d = d + 4 * (x - z) + 10;
				} else
					d = d + 4 * x + 6;
					placed=drawCircle(xc, yc, zc, x, y, z)||placed ;
			}
		} else if (plane == 1) {// XY;
			int x = 0, y = r, z = 0;
			int d = 3 - 2 * r;
			placed=drawCircle(xc, yc, zc, x, y, z)||placed ;
			while (y >= x) {
				x++;
				if (d > 0) {
					y--;
					d = d + 4 * (x - y) + 10;
				} else
					d = d + 4 * x + 6;
				placed=drawCircle(xc, yc, zc, x, y, z)||placed ;
			}
		} else if (plane == 2) {// YZ;
			int x = 0, y = 0, z = r;
			int d = 3 - 2 * r;
			placed=drawCircle(xc, yc, zc, x, y, z)||placed ;
			while (z >= y) {
				y++;
				if (d > 0) {
					z--;
					d = d + 4 * (y - z) + 10;
				} else
					d = d + 4 * y + 6;
				placed=drawCircle(xc, yc, zc, x, y, z)||placed ;
			}
		}
		return placed;
	}
}
