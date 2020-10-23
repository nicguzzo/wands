package net.nicguzzo.common;

import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import net.nicguzzo.WandsMod;

public class WandsBaseRenderer{
	public enum MyDir{
		DOWN(0,1),
   		UP(1,0),
   		NORTH(2,3),
   		SOUTH(3,2),
   		WEST(4,5),
		EAST(5,4);
		private final int id;
		private final int idOpposite;
		private MyDir(int id, int idOpposite) {
			this.id = id;
			this.idOpposite = idOpposite;
		}
		public MyDir getOpposite(){
			return MyDir.values()[idOpposite];
		}
	}
	public static float BLOCKS_PER_XP = 0;	
	public static void render(World world,PlayerEntity player, BlockPos pos,BlockState block_state,double camX, double camY, double camZ,int lim,
	boolean isCreative,
	boolean isDoubleSlab,boolean isSlabTop,
	float experienceProgress,
	MyDir side,
	boolean isFullCube,
	double hit_x,double hit_y,double hit_z)
	{
		RenderSystem.pushMatrix();				
		RenderSystem.translated(-camX, -camY, -camZ);
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableTexture();
		
		
		GL11.glLineWidth(3.0f);
		GL11.glBegin(GL11.GL_LINES);
		
		Block block = block_state.getBlock();
		
		int d = 1;
		boolean is_slab = false;
		SlabBlock slab = null;		
		if (block instanceof SlabBlock) {
			is_slab = true;
			slab = (SlabBlock) block;
			if (isDoubleSlab) {
				d = 2;
			}
		}
		ItemStack item_stack = new ItemStack(block);				
		if (player.inventory.count(item_stack.getItem()) >= d 
			|| isCreative
			|| WandItem.getMode()==2
		) {
			boolean is_pane = false;					
			boolean is_fence = false;
			boolean is_fence_gate = false;
			boolean is_stairs = false;
			boolean is_leaves = false;
			if (! is_slab) {
				if (block instanceof PaneBlock) {
					is_pane = true;
				} else if (block instanceof FenceBlock) {
					is_fence = true;
				} else if (block instanceof FenceGateBlock) {
					is_fence_gate = true;
				} else if (block instanceof StairsBlock) {
					is_stairs = true;
				} else if (block instanceof LeavesBlock) {
					is_leaves = true;
				}
			}
			float xp = 0.0f;
			int max_xp_blocks = 0;
			if (BLOCKS_PER_XP > 0) {
				xp = WandItem.calc_xp(player.experienceLevel, experienceProgress);
				max_xp_blocks = (int) (xp * BLOCKS_PER_XP);
			}
			if ( (max_xp_blocks > 0 || BLOCKS_PER_XP == 0 || isCreative)
					&& (isFullCube|| is_slab || is_pane || is_fence || is_fence_gate|| is_stairs)
				) {
				float h = 1.0f;
				float y0 = 0.0f;
				if (slab != null) {
					if (!isSlabTop) {
						h = 0.5f;
					} else {
						y0 = 0.5f;
						h = 0.5f;
					}
				}
				switch (WandItem.getMode()) {
					case 0: {
						if (is_pane || is_fence || is_fence_gate) {
							WandItem.valid = false;
							break;
						}
						mode0(pos, y0, h, side, world, block_state, player,lim,hit_x,hit_y,hit_z);
					}
						break;
					case 1: {
						mode1(pos, y0, h, side, hit_x,hit_y,hit_z, world, block_state,player, lim,
								max_xp_blocks, item_stack, (is_pane || is_fence || is_fence_gate || is_stairs || is_leaves),isCreative);
					}
						break;
					case 2: {
						//player.sendMessage(new LiteralText("m use 2"),true);
						WandItem.valid = true;
						if(WandItem.fill_pos1!=null){
							float x1=WandItem.fill_pos1.getX();
							float y1=WandItem.fill_pos1.getY();
							float z1=WandItem.fill_pos1.getZ();
							float x2=pos.getX();
							float y2=pos.getY();
							float z2=pos.getZ();
							if(!WandItem.fill_pos1.equals(pos)){
								if(x1>=x2){
									x1+=1;
								}else{
									x2+=1;
								}										
								if(y1>=y2){
									y1+=1;											
								}else{
									y2+=1;
								}
								if(z1>=z2){
									z1+=1;
								}else{
									z2+=1;											
								}
							}else{
								x2=x1+1;
								y2=y1+1;
								z2=z1+1;
							}
																
							if(Math.abs(x2-x1)<=lim && Math.abs(y2-y1)<=lim && Math.abs(z2-z1)<=lim){								
								preview(x1,y1,z1,x2,y2,z2);
							}
						}
					}break;
					default: {
						WandItem.valid = false;
					}
						break;
				}
			}
		} else {
			WandItem.valid = false;
		}
		
		GL11.glEnd();
		RenderSystem.enableBlend();
		RenderSystem.enableTexture();
		RenderSystem.popMatrix();
		
	}

	private static void mode0(BlockPos pos, float y0, float h, MyDir side, 
		World world, BlockState block_state, PlayerEntity player, int lim,double hit_x,double hit_y,double hit_z) {
		
		float x = pos.getX();
		float y = pos.getY();
		float z = pos.getZ();
		float o = 0.01f;
		switch (side) {
			case UP:
				y += h + o;
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

		grid(side, x, y + y0, z,  h);
		MyDir dirs[] = getMyDirMode0(side, y0, h,hit_x, hit_y, hit_z);
		MyDir d1 = dirs[0];
		MyDir d2 = dirs[1];

		if (d1 != null) {
			BlockPos pv = null;
			if (d2 != null) {
				pv = find_next_diag(world, block_state, d1, d2, pos, lim);
			} else {
				pv = find_next_pos(world, block_state, d1, pos, lim);
			}
			if (pv != null) {
				int x1 = pv.getX();
				int y1 = pv.getY();
				int z1 = pv.getZ();
				int x2 = x1 + 1;
				int y2 = y1 + 1;
				int z2 = z1 + 1;

				if ( WandsMod.interescts_player_bb(player,x1, y1, z1, x2, y2, z2)) {
					WandItem.valid = false;
				} else {
					WandItem.valid = true;
					//WandItem.mode2_dir = d1;
					WandItem.x1 = x1;
					WandItem.y1 = y1;
					WandItem.z1 = z1;
					WandItem.x2 = x2;
					WandItem.y2 = y2;
					WandItem.z2 = z2;
					float oo = +0.0001f;
					preview( x1 + oo, y1 + oo, z1 + oo, x2 - oo, y2 - oo, z2 - oo);
				}
			} else {
				WandItem.valid = false;
			}
		}
	}

	private static void mode1(BlockPos pos, float y0, float h, MyDir side,double hit_x,double hit_y,double hit_z,
			World world, BlockState block_state, PlayerEntity player, int lim,
			int max_xp_blocks, ItemStack item_stack, boolean dont_check_state,boolean isCreative) {
		MyDir dir = MyDir.EAST;
		BlockPos pos_m = WandsMod.pos_offset(pos,side,1);
		BlockState state=world.getBlockState(pos_m);
		
		if (state.isAir() ||WandsMod.is_fluid(state)) {
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
					switch (WandItem.getOrientation()) {
						case HORIZONTAL:
							dir = MyDir.SOUTH;
							offz = -1;
							break;
						case VERTICAL:
							dir = MyDir.EAST;
							offx = -1;
							break;
					}
					break;
				case SOUTH:
				case NORTH:
					switch (WandItem.getOrientation()) {
						case HORIZONTAL:
							dir = MyDir.EAST;
							offx = -1;
							break;
						case VERTICAL:
							dir = MyDir.UP;
							offy = -1;
							break;
					}
					break;
				case EAST:
				case WEST:
					switch (WandItem.getOrientation()) {
						case HORIZONTAL:
							dir = MyDir.SOUTH;
							offz = -1;
							break;
						case VERTICAL:
							dir = MyDir.UP;
							offy = -1;
							break;
					}
					break;
			}

			MyDir op = dir.getOpposite();
			int i = lim - 1;
			int k = 0;
			boolean stop1 = false;
			boolean stop2 = false;
			boolean intersects = false;
			if (!isCreative) {
				int n = player.inventory.count(item_stack.getItem());
				if (n < i) {
					i = n - 1;
				}
				if (BLOCKS_PER_XP != 0 && max_xp_blocks < i) {
					i = max_xp_blocks - 1;
				}
			}
			//boolean is_fluid;
			boolean eq = false;
			while (k < 81 && i > 0) {
				if (!stop1 && i > 0) {
					BlockState bs0 = world.getBlockState( WandsMod.pos_offset(pos0,dir,1));
					BlockState bs1 = world.getBlockState(WandsMod.pos_offset(pos1,dir,1));
					if (dont_check_state) {
						eq = bs0.getBlock().equals(block_state.getBlock());
					} else {
						eq = bs0.equals(block_state);
					}
					//is_fluid = bs1.getFluidState().isIn(FluidTags.WATER)||bs1.getFluidState().isIn(FluidTags.LAVA);
					if (eq && (bs1.isAir() || WandsMod.is_fluid(bs1))) {
						pos0 =WandsMod.pos_offset(pos0,dir,1);
						pos1 =WandsMod.pos_offset(pos1,dir,1);
						i--;
					} else {
						stop1 = true;
					}
				}
				if (!stop2 && i > 0) {
					BlockState bs2 = world.getBlockState(WandsMod.pos_offset(pos2,op,1));
					BlockState bs3 = world.getBlockState(WandsMod.pos_offset(pos3,op,1));
					if (dont_check_state) {
						eq = bs2.getBlock().equals(block_state.getBlock());
					} else {
						eq = bs2.equals(block_state);
					}
					//is_fluid = bs3.getFluidState().isIn(FluidTags.WATER)||bs3.getFluidState().isIn(FluidTags.LAVA);
					if (eq && (bs3.isAir() || WandsMod.is_fluid(bs3))) {
						pos2 = WandsMod.pos_offset(pos2,op,1);
						pos3 = WandsMod.pos_offset(pos3,op,1);
						i--;
					} else {
						stop2 = true;
					}
				}
				if (WandsMod.interescts_player_bb(player,pos1.getX(), pos1.getY(), pos1.getZ(), pos1.getX() + 1, pos1.getY() + 1, pos1.getZ() + 1)) {
					intersects = true;
					break;
				}
				if (WandsMod.interescts_player_bb(player,pos3.getX(), pos3.getY(), pos3.getZ(), pos3.getX() + 1, pos3.getY() + 1, pos3.getZ() + 1)) {
					intersects = true;
					break;
				}
				k++;
				if (stop1 && stop2) {
					k = 1000;
				}
			}
			int x1 = pos1.getX() - offx;
			int y1 = pos1.getY() - offy;
			int z1 = pos1.getZ() - offz;
			int x2 = pos3.getX() + 1 + offx;
			int y2 = pos3.getY() + 1 + offy;
			int z2 = pos3.getZ() + 1 + offz;

			if (intersects) {
				WandItem.valid = false;
			} else {
				WandItem.valid = true;
				//WandItem.mode2_dir = dir.getOpposite();
				WandItem.x1 = x1 + offx;
				WandItem.y1 = y1 + offy;
				WandItem.z1 = z1 + offz;
				WandItem.x2 = x2 + offx;
				WandItem.y2 = y2 + offy;
				WandItem.z2 = z2 + offz;
				float oo = +0.0001f;
				preview( x1 + oo, y1 + oo, z1 + oo, x2 - oo, y2 - oo, z2 - oo);
			}
		} else {
			WandItem.valid = false;
		}
	}

	static public BlockPos find_next_diag(World world, BlockState block_state, MyDir dir1, MyDir dir2,
			BlockPos pos, int limit) {
		// BlockPos ret=null;
		for (int i = 0; i < limit; i++) {
			//pos = pos.offset(dir1, 1).offset(dir2, 1);
			BlockPos p1=WandsMod.pos_offset(pos,dir1,1);
			pos=WandsMod.pos_offset(p1,dir2,1);
			BlockState bs = world.getBlockState(pos);
			if (bs != null) {
				// if(!bs.equals(block_state)){
				//boolean is_fluid = bs.getFluidState().isIn(FluidTags.WATER)||bs.getFluidState().isIn(FluidTags.LAVA);
				if (bs.isAir() || WandsMod.is_fluid(bs)) {
					return pos;
				} else {
					if (!bs.equals(block_state))
						return null;
				}
				// }
			}
		}
		return null;
	}

	static public BlockPos find_next_pos(World world, BlockState block_state, MyDir dir, BlockPos pos, int limit) {
		for (int i = 0; i < limit; i++) {
			BlockState bs = world.getBlockState(WandsMod.pos_offset(pos,dir, i + 1));
			if (bs != null) {
				if (!bs.equals(block_state)) {
					//boolean is_fluid = bs.getFluidState().isIn(FluidTags.WATER)||bs.getFluidState().isIn(FluidTags.LAVA);
					if (bs.isAir() || WandsMod.is_fluid(bs)) {
						return WandsMod.pos_offset(pos,dir, i + 1);
					} else {
						return null;
					}
				}
			}
		}
		return null;
	}

	static public float unitCoord(float x) {
		float y = x - ((int) x);
		if (y < 0)
			y = 1.0f + y;
		return y;
	}

	static public MyDir[] getMyDirMode0(MyDir side, float y0, float h,double hit_x,double hit_y,double hit_z) {
		MyDir ret[] = new MyDir[2];
		ret[0] = null;
		ret[1] = null;
		//MinecraftClient client = MinecraftClient.getInstance();
		//ClientPlayerEntity player = client.player;
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
						ret[0] = MyDir.NORTH;
					} else {
						if (z >= b) {
							ret[0] = MyDir.SOUTH;
						} else {
							ret[0] = side.getOpposite();
						}
					}
				} else {
					if (z >= a && z <= b) {
						if (x <= a) {
							ret[0] = MyDir.WEST;
						} else {
							if (x >= b) {
								ret[0] = MyDir.EAST;
							}
						}
					} else {
						if (x <= a && z <= a) {
							ret[0] = MyDir.WEST;
							ret[1] = MyDir.NORTH;
						}
						if (x >= b && z <= a) {
							ret[0] = MyDir.EAST;
							ret[1] = MyDir.NORTH;
						}
						if (x >= b && z >= b) {
							ret[0] = MyDir.EAST;
							ret[1] = MyDir.SOUTH;
						}
						if (x <= a && z >= b) {
							ret[0] = MyDir.WEST;
							ret[1] = MyDir.SOUTH;
						}
					}
				}
				break;
			case EAST:
			case WEST:

				if (z >= a && z <= b) {
					if (y <= a2) {
						ret[0] = MyDir.DOWN;
					} else {
						if (y >= b2) {
							ret[0] = MyDir.UP;
						} else {
							ret[0] = side.getOpposite();
						}
					}
				} else {
					if (y >= a2 && y <= b2) {
						if (z <= a) {
							ret[0] = MyDir.NORTH;
							return ret;
						} else {
							if (z >= b) {
								ret[0] = MyDir.SOUTH;
								return ret;
							}
						}
					} else {
						if (y <= a2 && z <= a) {
							ret[0] = MyDir.DOWN;
							ret[1] = MyDir.NORTH;
						}
						if (y >= b2 && z <= a) {
							ret[0] = MyDir.UP;
							ret[1] = MyDir.NORTH;
						}
						if (y >= b2 && z >= b) {
							ret[0] = MyDir.UP;
							ret[1] = MyDir.SOUTH;
						}
						if (y <= a2 && z >= b) {
							ret[0] = MyDir.DOWN;
							ret[1] = MyDir.SOUTH;
						}
					}
				}
				break;
			case NORTH:
			case SOUTH:
				if (x >= a && x <= b) {
					if (y <= a2) {
						ret[0] = MyDir.DOWN;
					} else {
						if (y >= b2) {
							ret[0] = MyDir.UP;
						} else {
							ret[0] = side.getOpposite();
						}
					}
				} else {
					if (y >= a2 && y <= b2) {
						if (x <= a) {
							ret[0] = MyDir.WEST;
						} else {
							if (x >= b2) {
								ret[0] = MyDir.EAST;
							}
						}
					} else {
						if (y <= a2 && x <= a) {
							ret[0] = MyDir.DOWN;
							ret[1] = MyDir.WEST;
						}
						if (y >= b2 && x <= a) {
							ret[0] = MyDir.UP;
							ret[1] = MyDir.WEST;
						}
						if (y >= b2 && x >= b) {
							ret[0] = MyDir.UP;
							ret[1] = MyDir.EAST;
						}
						if (y <= a2 && x >= b) {
							ret[0] = MyDir.DOWN;
							ret[1] = MyDir.EAST;
						}
					}
				}
				break;
		}
		if (WandItem.getInvert()) {
			if (ret[0] != null)
				ret[0] = ret[0].getOpposite();
			if (ret[1] != null)
				ret[1] = ret[1].getOpposite();

		}
		return ret;
	}

	private static void preview(float fx1, float fy1, float fz1, float fx2, float fy2, float fz2) {
		//GL11.glVertex3d
		RenderSystem.color4f( 255f, 255f, 255f, 255f);
		GL11.glVertex3d(fx1, fy1, fz1);
		GL11.glVertex3d(fx2, fy1, fz1);
		GL11.glVertex3d(fx1, fy1, fz1);
		GL11.glVertex3d(fx1, fy1, fz2);
		GL11.glVertex3d(fx1, fy1, fz2);
		GL11.glVertex3d(fx2, fy1, fz2);
		GL11.glVertex3d(fx2, fy1, fz1);
		GL11.glVertex3d(fx2, fy1, fz2);
		GL11.glVertex3d(fx1, fy2, fz1);
		GL11.glVertex3d(fx2, fy2, fz1);
		GL11.glVertex3d(fx1, fy2, fz1);
		GL11.glVertex3d(fx1, fy2, fz2);
		GL11.glVertex3d(fx1, fy2, fz2);
		GL11.glVertex3d(fx2, fy2, fz2);
		GL11.glVertex3d(fx2, fy2, fz1);
		GL11.glVertex3d(fx2, fy2, fz2);
		GL11.glVertex3d(fx1, fy1, fz1);
		GL11.glVertex3d(fx1, fy2, fz1);
		GL11.glVertex3d(fx2, fy1, fz1);
		GL11.glVertex3d(fx2, fy2, fz1);
		GL11.glVertex3d(fx1, fy1, fz2);
		GL11.glVertex3d(fx1, fy2, fz2);
		GL11.glVertex3d(fx2, fy1, fz2);
		GL11.glVertex3d(fx2, fy2, fz2);
	}

	private static void grid(MyDir side, float x, float y, float z, float h) {
		switch (side) {
			case UP:
			case DOWN: {
				RenderSystem.color4f( 255f, 255f, 255f, 255f);//w
				GL11.glVertex3d(x, y, z);
				GL11.glVertex3d(x + 1.00f, y, z);
				GL11.glVertex3d(x, y, z);
				GL11.glVertex3d(x, y, z + 1.00f);
				GL11.glVertex3d(x + 1.00f, y, z);
				GL11.glVertex3d(x + 1.00f, y, z + 1.00f);
				GL11.glVertex3d(x, y, z + 1.00f);
				GL11.glVertex3d(x + 1.00f, y, z + 1.00f);
				GL11.glVertex3d(x, y, z + 0.25f);
				GL11.glVertex3d(x + 1.00f, y, z + 0.25f);
				GL11.glVertex3d(x, y, z + 0.75f);
				GL11.glVertex3d(x + 1.00f, y, z + 0.75f);
				GL11.glVertex3d(x + 0.25f, y, z);
				GL11.glVertex3d(x + 0.25f, y, z + 1.00f);
				GL11.glVertex3d(x + 0.75f, y, z);
				GL11.glVertex3d(x + 0.75f, y, z + 1.00f);
				RenderSystem.color4f( 178f, 0f, 0f, 255f);//r
				GL11.glVertex3d(x + 0.40f, y, z + 0.20f);
				GL11.glVertex3d(x + 0.50f, y, z + 0.05f);
				GL11.glVertex3d(x + 0.60f, y, z + 0.20f);
				GL11.glVertex3d(x + 0.50f, y, z + 0.05f);
				GL11.glVertex3d(x + 0.40f, y, z + 0.80f);
				GL11.glVertex3d(x + 0.50f, y, z + 0.95f);
				GL11.glVertex3d(x + 0.60f, y, z + 0.80f);
				GL11.glVertex3d(x + 0.50f, y, z + 0.95f);
				GL11.glVertex3d(x + 0.20f, y, z + 0.40f);
				GL11.glVertex3d(x + 0.05f, y, z + 0.50f);
				GL11.glVertex3d(x + 0.20f, y, z + 0.60f);
				GL11.glVertex3d(x + 0.05f, y, z + 0.50f);
				GL11.glVertex3d(x + 0.80f, y, z + 0.40f);
				GL11.glVertex3d(x + 0.95f, y, z + 0.50f);
				GL11.glVertex3d(x + 0.80f, y, z + 0.60f);
				GL11.glVertex3d(x + 0.95f, y, z + 0.50f);
				RenderSystem.color4f( 0f,178f,  0f, 255f);//g
				GL11.glVertex3d(x + 0.40f, y, z + 0.50f);
				GL11.glVertex3d(x + 0.50f, y, z + 0.40f);
				GL11.glVertex3d(x + 0.40f, y, z + 0.50f);
				GL11.glVertex3d(x + 0.50f, y, z + 0.60f);
				GL11.glVertex3d(x + 0.60f, y, z + 0.50f);
				GL11.glVertex3d(x + 0.50f, y, z + 0.60f);
				GL11.glVertex3d(x + 0.50f, y, z + 0.40f);
				GL11.glVertex3d(x + 0.60f, y, z + 0.50f);
				RenderSystem.color4f( 0f, 0f,178f, 255f);//b
				GL11.glVertex3d(x + 0.10f, y, z + 0.10f);
				GL11.glVertex3d(x + 0.20f, y, z + 0.14f);
				GL11.glVertex3d(x + 0.10f, y, z + 0.10f);
				GL11.glVertex3d(x + 0.14f, y, z + 0.20f);
				GL11.glVertex3d(x + 0.90f, y, z + 0.90f);
				GL11.glVertex3d(x + 0.80f, y, z + 0.86f);
				GL11.glVertex3d(x + 0.90f, y, z + 0.90f);
				GL11.glVertex3d(x + 0.86f, y, z + 0.80f);
				GL11.glVertex3d(x + 0.90f, y, z + 0.10f);
				GL11.glVertex3d(x + 0.80f, y, z + 0.14f);
				GL11.glVertex3d(x + 0.90f, y, z + 0.10f);
				GL11.glVertex3d(x + 0.86f, y, z + 0.20f);
				GL11.glVertex3d(x + 0.10f, y, z + 0.90f);
				GL11.glVertex3d(x + 0.20f, y, z + 0.86f);
				GL11.glVertex3d(x + 0.10f, y, z + 0.90f);
				GL11.glVertex3d(x + 0.14f, y, z + 0.80f);

			}
				break;
			case NORTH:
			case SOUTH: {
				RenderSystem.color4f( 255f, 255f, 255f, 255f);//w
				GL11.glVertex3d(x, y, z);
				GL11.glVertex3d(x + 1.00f, y, z);
				GL11.glVertex3d(x, y, z);
				GL11.glVertex3d(x, y + 1.00f * h, z);
				GL11.glVertex3d(x + 1.00f, y, z);
				GL11.glVertex3d(x + 1.00f, y + 1.00f * h, z);
				GL11.glVertex3d(x, y + 1.00f * h, z);
				GL11.glVertex3d(x + 1.00f, y + 1.00f * h, z);
				GL11.glVertex3d(x, y + 0.25f * h, z);
				GL11.glVertex3d(x + 1.00f, y + 0.25f * h, z);
				GL11.glVertex3d(x, y + 0.75f * h, z);
				GL11.glVertex3d(x + 1.00f, y + 0.75f * h, z);
				GL11.glVertex3d(x + 0.25f, y, z);
				GL11.glVertex3d(x + 0.25f, y + 1.00f * h, z);
				GL11.glVertex3d(x + 0.75f, y, z);
				GL11.glVertex3d(x + 0.75f, y + 1.00f * h, z);
				RenderSystem.color4f( 178f, 0, 0, 255f);//r
				GL11.glVertex3d(x + 0.40f, y + 0.20f * h, z);
				GL11.glVertex3d(x + 0.50f, y + 0.05f * h, z);
				GL11.glVertex3d(x + 0.60f, y + 0.20f * h, z);
				GL11.glVertex3d(x + 0.50f, y + 0.05f * h, z);
				GL11.glVertex3d(x + 0.40f, y + 0.80f * h, z);
				GL11.glVertex3d(x + 0.50f, y + 0.95f * h, z);
				GL11.glVertex3d(x + 0.60f, y + 0.80f * h, z);
				GL11.glVertex3d(x + 0.50f, y + 0.95f * h, z);
				GL11.glVertex3d(x + 0.20f, y + 0.40f * h, z);
				GL11.glVertex3d(x + 0.05f, y + 0.50f * h, z);
				GL11.glVertex3d(x + 0.20f, y + 0.60f * h, z);
				GL11.glVertex3d(x + 0.05f, y + 0.50f * h, z);
				GL11.glVertex3d(x + 0.80f, y + 0.40f * h, z);
				GL11.glVertex3d(x + 0.95f, y + 0.50f * h, z);
				GL11.glVertex3d(x + 0.80f, y + 0.60f * h, z);
				GL11.glVertex3d(x + 0.95f, y + 0.50f * h, z);
				RenderSystem.color4f( 0f,178f,  0.0f, 255f);//g
				GL11.glVertex3d(x + 0.40f, y + 0.50f * h, z);
				GL11.glVertex3d(x + 0.50f, y + 0.40f * h, z);
				GL11.glVertex3d(x + 0.40f, y + 0.50f * h, z);
				GL11.glVertex3d(x + 0.50f, y + 0.60f * h, z);
				GL11.glVertex3d(x + 0.60f, y + 0.50f * h, z);
				GL11.glVertex3d(x + 0.50f, y + 0.60f * h, z);
				GL11.glVertex3d(x + 0.50f, y + 0.40f * h, z);
				GL11.glVertex3d(x + 0.60f, y + 0.50f * h, z);
				RenderSystem.color4f( 0, 0,255f, 255f);//b
				GL11.glVertex3d(x + 0.10f, y + 0.10f * h, z);
				GL11.glVertex3d(x + 0.20f, y + 0.14f * h, z);
				GL11.glVertex3d(x + 0.10f, y + 0.10f * h, z);
				GL11.glVertex3d(x + 0.14f, y + 0.20f * h, z);
				GL11.glVertex3d(x + 0.90f, y + 0.90f * h, z);
				GL11.glVertex3d(x + 0.80f, y + 0.86f * h, z);
				GL11.glVertex3d(x + 0.90f, y + 0.90f * h, z);
				GL11.glVertex3d(x + 0.86f, y + 0.80f * h, z);
				GL11.glVertex3d(x + 0.90f, y + 0.10f * h, z);
				GL11.glVertex3d(x + 0.80f, y + 0.14f * h, z);
				GL11.glVertex3d(x + 0.90f, y + 0.10f * h, z);
				GL11.glVertex3d(x + 0.86f, y + 0.20f * h, z);
				GL11.glVertex3d(x + 0.10f, y + 0.90f * h, z);
				GL11.glVertex3d(x + 0.20f, y + 0.86f * h, z);
				GL11.glVertex3d(x + 0.10f, y + 0.90f * h, z);
				GL11.glVertex3d(x + 0.14f, y + 0.80f * h, z);
			}
				break;
			case EAST:
			case WEST: {
				RenderSystem.color4f( 255f, 255f, 255f, 255f);//w
				GL11.glVertex3d(x, y, z);
				GL11.glVertex3d(x, y + 1.00f * h, z);
				GL11.glVertex3d(x, y, z);
				GL11.glVertex3d(x, y, z + 1.00f);
				GL11.glVertex3d(x, y + 1.00f * h, z);
				GL11.glVertex3d(x, y + 1.00f * h, z + 1.00f);
				GL11.glVertex3d(x, y, z + 1.00f);
				GL11.glVertex3d(x, y + 1.00f * h, z + 1.00f);
				GL11.glVertex3d(x, y, z + 0.25f);
				GL11.glVertex3d(x, y + 1.00f * h, z + 0.25f);
				GL11.glVertex3d(x, y, z + 0.75f);
				GL11.glVertex3d(x, y + 1.00f * h, z + 0.75f);
				GL11.glVertex3d(x, y + 0.25f * h, z);
				GL11.glVertex3d(x, y + 0.25f * h, z + 1.00f);
				GL11.glVertex3d(x, y + 0.75f * h, z);
				GL11.glVertex3d(x, y + 0.75f * h, z + 1.00f);
				RenderSystem.color4f( 178f, 0, 0, 255f);//b
				GL11.glVertex3d(x, y + 0.40f * h, z + 0.20f);
				GL11.glVertex3d(x, y + 0.50f * h, z + 0.05f);
				GL11.glVertex3d(x, y + 0.60f * h, z + 0.20f);
				GL11.glVertex3d(x, y + 0.50f * h, z + 0.05f);
				GL11.glVertex3d(x, y + 0.40f * h, z + 0.80f);
				GL11.glVertex3d(x, y + 0.50f * h, z + 0.95f);
				GL11.glVertex3d(x, y + 0.60f * h, z + 0.80f);
				GL11.glVertex3d(x, y + 0.50f * h, z + 0.95f);
				GL11.glVertex3d(x, y + 0.20f * h, z + 0.40f);
				GL11.glVertex3d(x, y + 0.05f * h, z + 0.50f);
				GL11.glVertex3d(x, y + 0.20f * h, z + 0.60f);
				GL11.glVertex3d(x, y + 0.05f * h, z + 0.50f);
				GL11.glVertex3d(x, y + 0.80f * h, z + 0.40f);
				GL11.glVertex3d(x, y + 0.95f * h, z + 0.50f);
				GL11.glVertex3d(x, y + 0.80f * h, z + 0.60f);
				GL11.glVertex3d(x, y + 0.95f * h, z + 0.50f);
				RenderSystem.color4f( 178f, 255f, 255f, 255f);//g
				GL11.glVertex3d(x, y + 0.40f * h, z + 0.50f);
				GL11.glVertex3d(x, y + 0.50f * h, z + 0.40f);
				GL11.glVertex3d(x, y + 0.40f * h, z + 0.50f);
				GL11.glVertex3d(x, y + 0.50f * h, z + 0.60f);
				GL11.glVertex3d(x, y + 0.60f * h, z + 0.50f);
				GL11.glVertex3d(x, y + 0.50f * h, z + 0.60f);
				GL11.glVertex3d(x, y + 0.50f * h, z + 0.40f);
				GL11.glVertex3d(x, y + 0.60f * h, z + 0.50f);
				RenderSystem.color4f( 0, 0,255f, 255f);//b
				GL11.glVertex3d(x, y + 0.10f * h, z + 0.10f);
				GL11.glVertex3d(x, y + 0.20f * h, z + 0.14f);
				GL11.glVertex3d(x, y + 0.10f * h, z + 0.10f);
				GL11.glVertex3d(x, y + 0.14f * h, z + 0.20f);
				GL11.glVertex3d(x, y + 0.90f * h, z + 0.90f);
				GL11.glVertex3d(x, y + 0.80f * h, z + 0.86f);
				GL11.glVertex3d(x, y + 0.90f * h, z + 0.90f);
				GL11.glVertex3d(x, y + 0.86f * h, z + 0.80f);
				GL11.glVertex3d(x, y + 0.90f * h, z + 0.10f);
				GL11.glVertex3d(x, y + 0.80f * h, z + 0.14f);
				GL11.glVertex3d(x, y + 0.90f * h, z + 0.10f);
				GL11.glVertex3d(x, y + 0.86f * h, z + 0.20f);
				GL11.glVertex3d(x, y + 0.10f * h, z + 0.90f);
				GL11.glVertex3d(x, y + 0.20f * h, z + 0.86f);
				GL11.glVertex3d(x, y + 0.10f * h, z + 0.90f);
				GL11.glVertex3d(x, y + 0.14f * h, z + 0.80f);

			}
				break;
		}
	}

}
