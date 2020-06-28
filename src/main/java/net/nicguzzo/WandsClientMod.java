package net.nicguzzo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.LiteralText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class WandsClientMod implements ClientModInitializer {

	
	private static KeyBinding modeKB;
	private static KeyBinding orientationKB;
	private static KeyBinding invertKB;
	private static KeyBinding undoKB;
	private static KeyBinding randomizeKB;
	public static float BLOCKS_PER_XP = 0;
	public static boolean conf = false;
	public static BlockPos fill_pos1=null;

	@Override
	public void onInitializeClient() {

		ClientSidePacketRegistry.INSTANCE.register(WandsMod.WANDXP_PACKET_ID, (packetContext, attachedData) -> {

			int xp_l = attachedData.readInt();
			float xp_p = attachedData.readFloat();
			packetContext.getTaskQueue().execute(() -> {
				MinecraftClient client = MinecraftClient.getInstance();
				ClientPlayerEntity player = client.player;
				player.experienceLevel = xp_l;
				player.experienceProgress = xp_p;
				// System.out.println("update xp!");
				// System.out.println("xp prog: "+ player.experienceProgress);
			});
		});
		ClientSidePacketRegistry.INSTANCE.register(WandsMod.WANDCONF_PACKET_ID, (packetContext, attachedData) -> {

			float bpxp = attachedData.readFloat();

			packetContext.getTaskQueue().execute(() -> {
				WandsClientMod.BLOCKS_PER_XP = bpxp;
				System.out.println("got BLOCKS_PER_XP from server " + BLOCKS_PER_XP);
			});
		});

		modeKB = new KeyBinding("key.wands.wand_mode", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.wands");
		KeyBindingHelper.registerKeyBinding(modeKB);
		orientationKB = new KeyBinding("key.wands.wand_orientation", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "category.wands");
		KeyBindingHelper.registerKeyBinding(orientationKB);
		//undoKB        = new KeyBinding("key.wands.wand_undo", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, "category.wands" );
		//KeyBindingHelper.registerKeyBinding(undoKB);
		invertKB      = new KeyBinding("key.wands.wand_invert", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, "category.wands" );
		KeyBindingHelper.registerKeyBinding(invertKB);
		randomizeKB      = new KeyBinding("key.wands.wand_randomize", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.wands" );
		KeyBindingHelper.registerKeyBinding(randomizeKB);
		
		ClientTickCallback.EVENT.register(e -> {
			
			if (modeKB.wasPressed()) {
				if(hasWandOnHand(e.player)) WandItem.toggleMode();
			}
			if (orientationKB.wasPressed()) {
				if(hasWandOnHand(e.player)) WandItem.cycleOrientation();
			}
			//if (undoKB.wasPressed()) {
			//	WandItem.undo();
			//}
			if (invertKB.wasPressed()) {
				if(hasWandOnHand(e.player)) WandItem.toggleInvert();
			}
			if (randomizeKB.wasPressed()) {
				if(hasWandOnHand(e.player)) WandItem.toggleRandomize();
			}
		});
		
	/*	AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			ItemStack item = player.inventory.getMainHandStack();	
			if (item.getItem() instanceof WandItem) {
					//player.sendMessage(new LiteralText("wand attack"),true);        
					return ActionResult.SUCCESS;
			} else {
					return ActionResult.PASS;
			}
		});*/
	}
	public boolean hasWandOnHand(PlayerEntity player){
		ItemStack item = player.inventory.getMainHandStack();	
		return (item.getItem() instanceof WandItem);
	}
	public static void render(float partialTicks, MatrixStack matrixStack) {

		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		ItemStack item = player.inventory.getMainHandStack();

		if (item.getItem() instanceof WandItem) {
			HitResult hitResult = client.crosshairTarget;
			if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
				int lim = ((WandItem) item.getItem()).getLimit();
				// System.out.println("lim: "+lim);
				BlockHitResult block_hit = (BlockHitResult) hitResult;
				Direction side = block_hit.getSide();
				BlockPos pos = block_hit.getBlockPos();
				BlockState block_state = client.world.getBlockState(pos);
				Box bb = player.getBoundingBox();
				Matrix4f matrix4f = matrixStack.peek().getModel();
				RenderSystem.pushMatrix();
				RenderSystem.multMatrix(matrix4f);
				Entity cplayer = MinecraftClient.getInstance().getCameraEntity();
				double cameraX = cplayer.lastRenderX + (cplayer.getX() - cplayer.lastRenderX) * (double) partialTicks;
				double cameraY = cplayer.lastRenderY + (cplayer.getY() - cplayer.lastRenderY) * (double) partialTicks
						+ cplayer.getEyeHeight(cplayer.getPose());
				double cameraZ = cplayer.lastRenderZ + (cplayer.getZ() - cplayer.lastRenderZ) * (double) partialTicks;
				RenderSystem.translated(-cameraX, -cameraY, -cameraZ);
				RenderSystem.disableTexture();
				RenderSystem.disableBlend();
				RenderSystem.lineWidth(1.0f);
				RenderSystem.enableAlphaTest();
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferBuilder = tessellator.getBuffer();
				bufferBuilder.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
				GlStateManager.lineWidth(3.0f);
				Block block = block_state.getBlock();
				SlabBlock slab = null;
				boolean is_slab = false;
				int d = 1;
				if (block instanceof SlabBlock) {
					is_slab = true;
					slab = (SlabBlock) block;
					if (block_state.get(SlabBlock.TYPE) == SlabType.DOUBLE) {
						d = 2;
					}
				}
				ItemStack item_stack = new ItemStack(block);				
				if (player.inventory.count(item_stack.getItem()) >= d 
						|| player.abilities.creativeMode
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
						xp = WandsMod.calc_xp(player.experienceLevel, player.experienceProgress);
						max_xp_blocks = (int) (xp * BLOCKS_PER_XP);
					}
					if ((max_xp_blocks > 0 || BLOCKS_PER_XP == 0 || player.abilities.creativeMode)
							&& (block_state.isFullCube(client.world, pos) || is_slab || is_pane || is_fence || is_fence_gate
									|| is_stairs)) {
						float h = 1.0f;
						float y0 = 0.0f;
						if (slab != null) {
							if (block_state.get(SlabBlock.TYPE) == SlabType.BOTTOM) {
								h = 0.5f;
							} else if (block_state.get(SlabBlock.TYPE) == SlabType.TOP) {
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
								mode0(pos, y0, h, side, bufferBuilder, block_hit, client.world, block_state, bb, lim);
							}
								break;
							case 1: {
								mode1(pos, y0, h, side, bufferBuilder, block_hit, client.world, block_state, bb, player, lim,
										max_xp_blocks, item_stack, (is_pane || is_fence || is_fence_gate || is_stairs || is_leaves));
							}
								break;
							case 2: {
								//player.sendMessage(new LiteralText("m use 2"),true);
								WandItem.valid = true;
								if(fill_pos1!=null){
									float x1=fill_pos1.getX();
									float y1=fill_pos1.getY();
									float z1=fill_pos1.getZ();
									float x2=pos.getX();
									float y2=pos.getY();
									float z2=pos.getZ();
									if(!fill_pos1.equals(pos)){
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
										//System.out.println("z2-z1: "+Math.abs(z2-z1));
										preview(bufferBuilder, x1,y1,z1,x2,y2,z2);
									}else{
										//fill_pos1=null;
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
				tessellator.draw();
				RenderSystem.translated(0, 0, 0);
				RenderSystem.lineWidth(1.0f);
				RenderSystem.enableBlend();
				RenderSystem.enableTexture();
				RenderSystem.popMatrix();
			} else {
				WandItem.valid = false;
			}
		}
	}

	private static void mode0(BlockPos pos, float y0, float h, Direction side, BufferBuilder bufferBuilder,
			BlockHitResult block_hit, World world, BlockState block_state, Box bb, int lim) {
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

		grid(side, x, y + y0, z, bufferBuilder, h);
		Direction dirs[] = getDirectionMode0(block_hit.getPos(), side, y0, h);
		Direction d1 = dirs[0];
		Direction d2 = dirs[1];

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

				if (bb.intersects(x1, y1, z1, x2, y2, z2)) {
					WandItem.valid = false;
				} else {
					WandItem.valid = true;
					WandItem.mode2_dir = d1;
					WandItem.x1 = x1;
					WandItem.y1 = y1;
					WandItem.z1 = z1;
					WandItem.x2 = x2;
					WandItem.y2 = y2;
					WandItem.z2 = z2;
					float oo = +0.0001f;
					preview(bufferBuilder, x1 + oo, y1 + oo, z1 + oo, x2 - oo, y2 - oo, z2 - oo);
				}
			} else {
				WandItem.valid = false;
			}
		}
	}

	private static void mode1(BlockPos pos, float y0, float h, Direction side, BufferBuilder bufferBuilder,
			BlockHitResult block_hit, World world, BlockState block_state, Box bb, ClientPlayerEntity player, int lim,
			int max_xp_blocks, ItemStack item_stack, boolean dont_check_state) {
		Direction dir = Direction.EAST;
		BlockPos pos_m = pos.offset(side, 1);
		
		if (world.getBlockState(pos_m).isAir() || world.getBlockState(pos_m).getFluidState().isIn(FluidTags.WATER)) {
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
					switch (WandItem.getOrientation()) {
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
					switch (WandItem.getOrientation()) {
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
			int i = lim - 1;
			int k = 0;
			boolean stop1 = false;
			boolean stop2 = false;
			boolean intersects = false;
			if (!player.abilities.creativeMode) {
				int n = player.inventory.count(item_stack.getItem());
				if (n < i) {
					i = n - 1;
				}
				if (BLOCKS_PER_XP != 0 && max_xp_blocks < i) {
					i = max_xp_blocks - 1;
				}
			}
			boolean is_water;
			boolean eq = false;
			while (k < 81 && i > 0) {
				if (!stop1 && i > 0) {
					BlockState bs0 = world.getBlockState(pos0.offset(dir, 1));
					BlockState bs1 = world.getBlockState(pos1.offset(dir, 1));
					if (dont_check_state) {
						eq = bs0.getBlock().equals(block_state.getBlock());
					} else {
						eq = bs0.equals(block_state);
					}
					is_water = bs1.getFluidState().isIn(FluidTags.WATER);
					if (eq && (bs1.isAir() || is_water)) {
						pos0 = pos0.offset(dir, 1);
						pos1 = pos1.offset(dir, 1);
						i--;
					} else {
						stop1 = true;
					}
				}
				if (!stop2 && i > 0) {
					BlockState bs2 = world.getBlockState(pos2.offset(op, 1));
					BlockState bs3 = world.getBlockState(pos3.offset(op, 1));
					if (dont_check_state) {
						eq = bs2.getBlock().equals(block_state.getBlock());
					} else {
						eq = bs2.equals(block_state);
					}
					is_water = bs3.getFluidState().isIn(FluidTags.WATER);
					if (eq && (bs3.isAir() || is_water)) {
						pos2 = pos2.offset(op, 1);
						pos3 = pos3.offset(op, 1);
						i--;
					} else {
						stop2 = true;
					}
				}
				if (bb.intersects(pos1.getX(), pos1.getY(), pos1.getZ(), pos1.getX() + 1, pos1.getY() + 1, pos1.getZ() + 1)) {
					intersects = true;
					break;
				}
				if (bb.intersects(pos3.getX(), pos3.getY(), pos3.getZ(), pos3.getX() + 1, pos3.getY() + 1, pos3.getZ() + 1)) {
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
				WandItem.mode2_dir = dir.getOpposite();
				WandItem.x1 = x1 + offx;
				WandItem.y1 = y1 + offy;
				WandItem.z1 = z1 + offz;
				WandItem.x2 = x2 + offx;
				WandItem.y2 = y2 + offy;
				WandItem.z2 = z2 + offz;
				float oo = +0.0001f;
				preview(bufferBuilder, x1 + oo, y1 + oo, z1 + oo, x2 - oo, y2 - oo, z2 - oo);
			}
		} else {
			WandItem.valid = false;
		}
	}

	static public BlockPos find_next_diag(World world, BlockState block_state, Direction dir1, Direction dir2,
			BlockPos pos, int limit) {
		// BlockPos ret=null;
		for (int i = 0; i < limit; i++) {
			pos = pos.offset(dir1, 1).offset(dir2, 1);
			BlockState bs = world.getBlockState(pos);
			if (bs != null) {
				// if(!bs.equals(block_state)){
				boolean is_water = bs.getFluidState().isIn(FluidTags.WATER);
				if (bs.isAir() || is_water) {
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

	static public BlockPos find_next_pos(World world, BlockState block_state, Direction dir, BlockPos pos, int limit) {
		for (int i = 0; i < limit; i++) {
			BlockState bs = world.getBlockState(pos.offset(dir, i + 1));
			if (bs != null) {
				if (!bs.equals(block_state)) {
					boolean is_water = bs.getFluidState().isIn(FluidTags.WATER);
					if (bs.isAir() || is_water) {
						return pos.offset(dir, i + 1);
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

	static public Direction[] getDirectionMode0(Vec3d hitPos, Direction side, float y0, float h) {
		Direction ret[] = new Direction[2];
		ret[0] = null;
		ret[1] = null;
		//MinecraftClient client = MinecraftClient.getInstance();
		//ClientPlayerEntity player = client.player;
		float x = unitCoord((float) hitPos.getX());
		float y = unitCoord((float) hitPos.getY());
		float z = unitCoord((float) hitPos.getZ());
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
		if (WandItem.getInvert()) {
			if (ret[0] != null)
				ret[0] = ret[0].getOpposite();
			if (ret[1] != null)
				ret[1] = ret[1].getOpposite();

		}
		return ret;
	}

	private static void preview(BufferBuilder b, float fx1, float fy1, float fz1, float fx2, float fy2, float fz2) {

		b.vertex(fx1, fy1, fz1).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx2, fy1, fz1).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx1, fy1, fz1).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx1, fy1, fz2).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx1, fy1, fz2).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx2, fy1, fz2).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx2, fy1, fz1).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx2, fy1, fz2).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx1, fy2, fz1).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx2, fy2, fz1).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx1, fy2, fz1).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx1, fy2, fz2).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx1, fy2, fz2).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx2, fy2, fz2).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx2, fy2, fz1).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx2, fy2, fz2).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx1, fy1, fz1).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx1, fy2, fz1).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx2, fy1, fz1).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx2, fy2, fz1).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx1, fy1, fz2).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx1, fy2, fz2).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx2, fy1, fz2).color(1.0f, 1.0f, 1.0f, 1.0f).next();
		b.vertex(fx2, fy2, fz2).color(1.0f, 1.0f, 1.0f, 1.0f).next();
	}

	private static void grid(Direction side, float x, float y, float z, BufferBuilder b, float h) {
		switch (side) {
			case UP:
			case DOWN: {
				b.vertex(x, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 1.00f, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y, z + 1.00f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 1.00f, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 1.00f, y, z + 1.00f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y, z + 1.00f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 1.00f, y, z + 1.00f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y, z + 0.25f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 1.00f, y, z + 0.25f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y, z + 0.75f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 1.00f, y, z + 0.75f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.25f, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.25f, y, z + 1.00f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.75f, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.75f, y, z + 1.00f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.40f, y, z + 0.20f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.50f, y, z + 0.05f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.60f, y, z + 0.20f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.50f, y, z + 0.05f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.40f, y, z + 0.80f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.50f, y, z + 0.95f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.60f, y, z + 0.80f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.50f, y, z + 0.95f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.20f, y, z + 0.40f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.05f, y, z + 0.50f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.20f, y, z + 0.60f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.05f, y, z + 0.50f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.80f, y, z + 0.40f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.95f, y, z + 0.50f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.80f, y, z + 0.60f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.95f, y, z + 0.50f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.40f, y, z + 0.50f).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x + 0.50f, y, z + 0.40f).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x + 0.40f, y, z + 0.50f).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x + 0.50f, y, z + 0.60f).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x + 0.60f, y, z + 0.50f).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x + 0.50f, y, z + 0.60f).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x + 0.50f, y, z + 0.40f).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x + 0.60f, y, z + 0.50f).color(0.0f, 0.7f, 0.0f, 1.0f).next();

				b.vertex(x + 0.10f, y, z + 0.10f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.20f, y, z + 0.14f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.10f, y, z + 0.10f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.14f, y, z + 0.20f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.90f, y, z + 0.90f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.80f, y, z + 0.86f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.90f, y, z + 0.90f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.86f, y, z + 0.80f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.90f, y, z + 0.10f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.80f, y, z + 0.14f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.90f, y, z + 0.10f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.86f, y, z + 0.20f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.10f, y, z + 0.90f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.20f, y, z + 0.86f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.10f, y, z + 0.90f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.14f, y, z + 0.80f).color(0.0f, 0.0f, 1.0f, 1.0f).next();

			}
				break;
			case NORTH:
			case SOUTH: {
				b.vertex(x, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 1.00f, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 1.00f * h, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 1.00f, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 1.00f, y + 1.00f * h, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 1.00f * h, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 1.00f, y + 1.00f * h, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.25f * h, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 1.00f, y + 0.25f * h, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.75f * h, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 1.00f, y + 0.75f * h, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.25f, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.25f, y + 1.00f * h, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.75f, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.75f, y + 1.00f * h, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.40f, y + 0.20f * h, z).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.50f, y + 0.05f * h, z).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.60f, y + 0.20f * h, z).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.50f, y + 0.05f * h, z).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.40f, y + 0.80f * h, z).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.50f, y + 0.95f * h, z).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.60f, y + 0.80f * h, z).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.50f, y + 0.95f * h, z).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.20f, y + 0.40f * h, z).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.05f, y + 0.50f * h, z).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.20f, y + 0.60f * h, z).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.05f, y + 0.50f * h, z).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.80f, y + 0.40f * h, z).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.95f, y + 0.50f * h, z).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.80f, y + 0.60f * h, z).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.95f, y + 0.50f * h, z).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x + 0.40f, y + 0.50f * h, z).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x + 0.50f, y + 0.40f * h, z).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x + 0.40f, y + 0.50f * h, z).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x + 0.50f, y + 0.60f * h, z).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x + 0.60f, y + 0.50f * h, z).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x + 0.50f, y + 0.60f * h, z).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x + 0.50f, y + 0.40f * h, z).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x + 0.60f, y + 0.50f * h, z).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x + 0.10f, y + 0.10f * h, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.20f, y + 0.14f * h, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.10f, y + 0.10f * h, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.14f, y + 0.20f * h, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.90f, y + 0.90f * h, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.80f, y + 0.86f * h, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.90f, y + 0.90f * h, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.86f, y + 0.80f * h, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.90f, y + 0.10f * h, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.80f, y + 0.14f * h, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.90f, y + 0.10f * h, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.86f, y + 0.20f * h, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.10f, y + 0.90f * h, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.20f, y + 0.86f * h, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.10f, y + 0.90f * h, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x + 0.14f, y + 0.80f * h, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
			}
				break;
			case EAST:
			case WEST: {
				b.vertex(x, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 1.00f * h, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y, z + 1.00f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 1.00f * h, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 1.00f * h, z + 1.00f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y, z + 1.00f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 1.00f * h, z + 1.00f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y, z + 0.25f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 1.00f * h, z + 0.25f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y, z + 0.75f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 1.00f * h, z + 0.75f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.25f * h, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.25f * h, z + 1.00f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.75f * h, z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.75f * h, z + 1.00f).color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.40f * h, z + 0.20f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.50f * h, z + 0.05f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.60f * h, z + 0.20f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.50f * h, z + 0.05f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.40f * h, z + 0.80f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.50f * h, z + 0.95f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.60f * h, z + 0.80f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.50f * h, z + 0.95f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.20f * h, z + 0.40f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.05f * h, z + 0.50f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.20f * h, z + 0.60f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.05f * h, z + 0.50f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.80f * h, z + 0.40f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.95f * h, z + 0.50f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.80f * h, z + 0.60f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.95f * h, z + 0.50f).color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.40f * h, z + 0.50f).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.50f * h, z + 0.40f).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.40f * h, z + 0.50f).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.50f * h, z + 0.60f).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.60f * h, z + 0.50f).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.50f * h, z + 0.60f).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.50f * h, z + 0.40f).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.60f * h, z + 0.50f).color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x, y + 0.10f * h, z + 0.10f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.20f * h, z + 0.14f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.10f * h, z + 0.10f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.14f * h, z + 0.20f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.90f * h, z + 0.90f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.80f * h, z + 0.86f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.90f * h, z + 0.90f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.86f * h, z + 0.80f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.90f * h, z + 0.10f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.80f * h, z + 0.14f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.90f * h, z + 0.10f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.86f * h, z + 0.20f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.10f * h, z + 0.90f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.20f * h, z + 0.86f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.10f * h, z + 0.90f).color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x, y + 0.14f * h, z + 0.80f).color(0.0f, 0.0f, 1.0f, 1.0f).next();

			}
				break;
		}
	}

}
