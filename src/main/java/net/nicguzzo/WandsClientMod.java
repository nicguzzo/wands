package net.nicguzzo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class WandsClientMod implements ClientModInitializer {

	private static FabricKeyBinding mode_keyBinding;
	private static FabricKeyBinding orientation_keyBinding;

	@Override
	public void onInitializeClient() {
		
		mode_keyBinding = FabricKeyBinding.Builder.create(
				new Identifier("wands", "wand_mode"),
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_V,
				"Wands"
		).build();
		orientation_keyBinding = FabricKeyBinding.Builder.create(
				new Identifier("wands", "wand_orientation"),
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_X,
				"Wands"
		).build();
		KeyBindingRegistry.INSTANCE.addCategory("Wands");
		KeyBindingRegistry.INSTANCE.register(mode_keyBinding);
		KeyBindingRegistry.INSTANCE.register(orientation_keyBinding);
		ClientTickCallback.EVENT.register(e ->
		{
			if(mode_keyBinding.wasPressed()) {
				WandItem.toggleMode();
			}
			if(orientation_keyBinding.wasPressed()) {
				WandItem.cycleOrientation();
			}
		});
	}
	public static void render(float partialTicks,MatrixStack matrixStack){
		MinecraftClient client=MinecraftClient.getInstance();
		ClientPlayerEntity player=client.player;
		ItemStack item=player.inventory.getMainHandStack();		
		
        if(item.getItem() instanceof WandItem){
            HitResult hitResult = client.crosshairTarget;            
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
				int lim=((WandItem)item.getItem()).getLimit();
				//System.out.println("lim: "+lim);
                BlockHitResult block_hit=(BlockHitResult)hitResult;                
				Direction side=block_hit.getSide();              
				BlockPos pos = block_hit.getBlockPos(); 				
				BlockState block_state=client.world.getBlockState(pos);
				Box bb=player.getBoundingBox();
                Matrix4f matrix4f = matrixStack.peek().getModel();
                RenderSystem.pushMatrix();
                RenderSystem.multMatrix(matrix4f);
                Entity cplayer = MinecraftClient.getInstance().getCameraEntity();
                double cameraX = cplayer.lastRenderX + (cplayer.getX() - cplayer.lastRenderX) * (double)partialTicks;
                double cameraY = cplayer.lastRenderY + (cplayer.getY() - cplayer.lastRenderY) * (double)partialTicks + cplayer.getEyeHeight(cplayer.getPose());
                double cameraZ = cplayer.lastRenderZ + (cplayer.getZ() - cplayer.lastRenderZ) * (double)partialTicks;        
                RenderSystem.translated(-cameraX, -cameraY, -cameraZ);
                RenderSystem.disableTexture();
                RenderSystem.disableBlend();
                RenderSystem.lineWidth(1.0f);
                RenderSystem.enableAlphaTest(); 
                Tessellator tessellator=Tessellator.getInstance();
                BufferBuilder bufferBuilder = tessellator.getBuffer();
				bufferBuilder.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
				GlStateManager.lineWidth(3.0f);
				int x1=0,y1=0,z1=0;
				int x2=0,y2=0,z2=0;				
				boolean preview=false;
				Direction dir=Direction.EAST;
				ItemStack item_stack=new ItemStack(block_state.getBlock());
				if(player.inventory.contains(item_stack) || player.abilities.creativeMode){
				
					Identifier id=  block_state.getBlock().getDropTableId();
				
					//TODO: find a better way to test if its a slab...
					
					boolean is_slab=id.toString().contains("slab");
				
				

				
					if(block_state.isFullCube(client.world, pos)||is_slab){
						if(WandItem.getMode()==0 ){										
							float h=1.0f;
							float x=pos.getX();
							float y=pos.getY();
							float z=pos.getZ();
							float o=0.01f;
							if(is_slab)
								h=0.5f;
							switch(side){
								case UP:
									y+=h+o;
									break;
								case DOWN:
									y-=o;
									break;
								case SOUTH:
									z+=1+o;
									break;
								case NORTH:
									z-=o;
									break;
								case EAST:
									x+=1+o;
									break;
								case WEST:
									x-=o;
									break;
							}
							
							grid( side, x, y, z,bufferBuilder,h);
							Direction dirs[]=getDirectionMode0(block_hit.getPos(),side,h);
							Direction d1=dirs[0];
							Direction d2=dirs[1];
						
							if(d1!=null){												
								BlockPos pv=null;
								if(d2!=null){
									pv = find_next_diag(client.world, block_state, d1,d2, pos,lim);
								}else{
									pv= find_next_pos(client.world, block_state, d1, pos,lim);
									//pv=pos.offset(d1,p+1);
								}
								if (pv !=null) {
									x1=pv.getX();
									y1=pv.getY();
									z1=pv.getZ();
									x2=x1+1;
									y2=y1+1;
									z2=z1+1;
									
									if(bb.intersects(x1, y1, z1, x2, y2, z2)){
										preview=false;
										WandItem.valid=false;
									}else{
										preview=true;																									
										WandItem.valid=true;
										WandItem.mode2_dir=d1;
										WandItem.x1=x1;
										WandItem.y1=y1;
										WandItem.z1=z1;
										WandItem.x2=x2;
										WandItem.y2=y2;
										WandItem.z2=z2;
									}
									
								}else{
									preview=false;
									WandItem.valid=false;
								}
							}
						}else{
							BlockPos pos_m=pos.offset(side,1);
							if(client.world.getBlockState(pos_m).isAir()){
								BlockPos pos0=pos;
								BlockPos pos1=pos_m;
								BlockPos pos2=pos;
								BlockPos pos3=pos_m;							
								int offx=0;
								int offy=0;
								int offz=0;							
								switch(side){
									case UP:
									case DOWN:
										switch(WandItem.getOrientation()){
											case HORIZONTAL:
												dir=Direction.SOUTH;
												offz=-1;
											break;
											case VERTICAL:
												dir=Direction.EAST;
												offx=-1;
											break;
										}
										break;
									case SOUTH:									
									case NORTH:
										switch(WandItem.getOrientation()){
											case HORIZONTAL:
												dir=Direction.EAST;
												offx=-1;
											break;
											case VERTICAL:
												dir=Direction.UP;											
												offy=-1;
											break;
										}
										break;
									case EAST:									
									case WEST:							
										switch(WandItem.getOrientation()){
											case HORIZONTAL:
												dir=Direction.SOUTH;
												offz=-1;
											break;
											case VERTICAL:
												dir=Direction.UP;
												offy=-1;
											break;
										}								
										break;
								}
								
								Direction op=dir.getOpposite();
								int i=lim-1;
								int k=0;
								boolean stop1=false;
								boolean stop2=false;
								boolean intersects=false;
								int n=player.inventory.countInInv(item_stack.getItem());								
								if(n<i){
									i=n-1;
								}								
								while(k<81 && i>0 ){								
									if(!stop1 && i>0){
										BlockState bs0 =client.world.getBlockState(pos0.offset(dir,1));
										BlockState bs1 =client.world.getBlockState(pos1.offset(dir,1));									
										if(bs0.equals(block_state) && bs1.isAir()){
											pos0=pos0.offset(dir,1);
											pos1=pos1.offset(dir,1);
											i--;
										}else{										
											stop1=true;
										}
									}									
									if(!stop2 && i>0){
										BlockState bs2 =client.world.getBlockState(pos2.offset(op,1));
										BlockState bs3 =client.world.getBlockState(pos3.offset(op,1));
										if(bs2.equals(block_state) && bs3.isAir()){
											pos2=pos2.offset(op,1);
											pos3=pos3.offset(op,1);
											i--;
										}else{										
											stop2=true;
										}
									}
									if(bb.intersects(pos1.getX(), pos1.getY(), pos1.getZ(), pos1.getX()+1, pos1.getY()+1, pos1.getZ()+1)){
										intersects=true;
										break;
									}
									if(bb.intersects(pos3.getX(), pos3.getY(), pos3.getZ(), pos3.getX()+1, pos3.getY()+1, pos3.getZ()+1)){
										intersects=true;
										break;
									}
									k++;
									if(stop1 && stop2){
										k=1000;
									}
								}							
								x1=pos1.getX()-offx;
								y1=pos1.getY()-offy;
								z1=pos1.getZ()-offz;	
								x2=pos3.getX()+1+offx;
								y2=pos3.getY()+1+offy;
								z2=pos3.getZ()+1+offz;	
								
								if(intersects){								
									preview=false;
									WandItem.valid=preview;
								}else{
									preview=true;
									WandItem.valid=preview;
									WandItem.mode2_dir=dir.getOpposite();
									
									WandItem.x1=x1+offx;
									WandItem.y1=y1+offy;
									WandItem.z1=z1+offz;
									WandItem.x2=x2+offx;
									WandItem.y2=y2+offy;
									WandItem.z2=z2+offz;
								}
							}else{
								preview=false;
								WandItem.valid=false;
							}
						}
					}else{
						preview=false;					
						WandItem.valid=false;
					}
				}else{
					preview=false;					
					WandItem.valid=false;
				}
				if(preview){
					float fx1=x1+0.0001f;
					float fy1=y1+0.0001f;
					float fz1=z1+0.0001f;
					float fx2=x2-0.0001f;
					float fy2=y2-0.0001f;
					float fz2=z2-0.0001f;
					bufferBuilder.vertex(fx1, fy1,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy1,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy1,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy1,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();					
					bufferBuilder.vertex(fx1, fy1,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy1,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy1,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy1,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy2,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy2,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy2,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy2,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();					
					bufferBuilder.vertex(fx1, fy2,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy2,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy2,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy2,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy1,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy2,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy1,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy2,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy1,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy2,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy1,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy2,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				}
                tessellator.draw();
                RenderSystem.translated(0, 0, 0);
                RenderSystem.lineWidth(1.0f);
                RenderSystem.enableBlend();
                RenderSystem.enableTexture();
                RenderSystem.popMatrix();
            }else{
				WandItem.valid=false;
			}
        }
	}
	static public BlockPos find_next_diag(World world,BlockState block_state,Direction dir1,Direction dir2,BlockPos pos,int limit){        
		//BlockPos ret=null;
        for(int i=0;i<limit;i++){
			pos=pos.offset(dir1,1).offset(dir2,1);
			BlockState bs =world.getBlockState(pos);			
            if(bs!=null){
                //if(!bs.equals(block_state)){
                    if(bs.isAir()){						
						return pos;
                    }else{
						if(!bs.equals(block_state))
							return null;
					}
                //} 
            }
        }
        return null;
    }
	static public BlockPos find_next_pos(World world,BlockState block_state,Direction dir,BlockPos pos,int limit){        
        for(int i=0;i<limit;i++){
            BlockState bs =world.getBlockState(pos.offset(dir,i+1));
            if(bs!=null){
                if(!bs.equals(block_state)){
                    if(bs.isAir()){
                        return pos.offset(dir,i+1);
                    }else{
                        return null;
                    }
                } 
            }
        }
        return null;
    }
    static public float unitCoord(float x){
        float y=x-((int) x);        
        if(y<0)
            y=1.0f+y;        
        return y;
    }
    static public Direction[] getDirectionMode0(Vec3d hitPos,Direction side,float h){
		Direction ret[]=new Direction[2];
		ret[0]=null;
		ret[1]=null;
        MinecraftClient client=MinecraftClient.getInstance();
		ClientPlayerEntity player=client.player;
        float x=unitCoord((float)hitPos.getX());
        float y=unitCoord((float)hitPos.getY());
        float z=unitCoord((float)hitPos.getZ());
		float a=0.25f;
		float b=0.75f;
		float a2=a*h;
		float b2=b*h;
        switch (side) {
            case UP:
            case DOWN:
                if (x >= a && x <= b) {
                    if (z <= a){
						ret[0]= Direction.NORTH;						
                    }else {
                        if (z >= b){
							ret[0]= Direction.SOUTH;							
                        }else{
                            if(player.isSneaking()){
								ret[0]= side;								
                            }else{
								ret[0]= side.getOpposite();								
                            }
                        }
                    }
                } else {
                    if (z >= a && z <= b) {
                        if (x <= a){
							ret[0]= Direction.WEST;							
                        }else {
                            if (x >= b){
								ret[0]= Direction.EAST;								
                            }
                        }
                    }else{
						if (x <= a && z <= a){
							ret[0]= Direction.WEST;	
							ret[1]= Direction.NORTH;
						}
						if (x >= b && z <= a){
							ret[0]= Direction.EAST;	
							ret[1]= Direction.NORTH;
						}
						if (x >= b && z >= b){
							ret[0]= Direction.EAST;	
							ret[1]= Direction.SOUTH;
						}
						if (x <= a && z >= b){
							ret[0]= Direction.WEST;	
							ret[1]= Direction.SOUTH;
						}
					}
                }
                break;
            case EAST:
			case WEST:
				
                if (z >= a && z <= b) {
                    if (y <= a2){
						ret[0]= Direction.DOWN;
						return ret;
					}else {
                        if (y >= b2){
							ret[0]= Direction.UP;
							return ret;
                        }else{
                            if(player.isSneaking()){
								ret[0]= side;
								return ret;
                            }else{
								ret[0]= side.getOpposite();
								return ret;
                            }
                        }
                    }
                } else {
                    if (y >= a2 && y <= b2) {
                        if (z <= a){
							ret[0]= Direction.NORTH;
							return ret;
                        }else {
                            if (z >= b){
								ret[0]= Direction.SOUTH;
								return ret;
                            }
                        }
                    }else{
						if (y <= a2 && z <= a){
							ret[0]= Direction.DOWN;	
							ret[1]= Direction.NORTH;
						}
						if (y >= b2 && z <= a){
							ret[0]= Direction.UP;	
							ret[1]= Direction.NORTH;
						}
						if (y >= b2 && z >= b){
							ret[0]= Direction.UP;	
							ret[1]= Direction.SOUTH;
						}
						if (y <= a2 && z >= b){
							ret[0]= Direction.DOWN;	
							ret[1]= Direction.SOUTH;
						}
					}
                }
                break;
            case NORTH:
            case SOUTH:
                if (x >= a && x <= b) {
                    if (y <= a2){
						ret[0]= Direction.DOWN;
						return ret;
					}else {
                        if (y >= b2){
							ret[0]= Direction.UP;
							return ret;
                        }else{
                            if(player.isSneaking()){
								ret[0]= side;
								return ret;
                            }else{
								ret[0]= side.getOpposite();
								return ret;
							}
                        }
                    }
                } else {
                    if (y >= a2 && y <= b2) {
                        if (x <= a){
							ret[0]= Direction.WEST;
							return ret;
                        }else {
                            if (x >= b2){
								ret[0]= Direction.EAST;
								return ret;
                            }
                        }
                    }else{
						if (y <= a2 && x <= a){
							ret[0]= Direction.DOWN;	
							ret[1]= Direction.WEST;
						}
						if (y >= b2 && x <= a){
							ret[0]= Direction.UP;	
							ret[1]= Direction.WEST;
						}
						if (y >= b2 && x >= b){
							ret[0]= Direction.UP;	
							ret[1]= Direction.EAST;
						}
						if (y <= a2 && x >= b){
							ret[0]= Direction.DOWN;	
							ret[1]= Direction.EAST;
						}
					}
                }
            break;
        }
        return ret;        
	}
	public static void grid(Direction side,float x, float y,float z,BufferBuilder b,float h){
		switch(side){
			case UP:                        
			case DOWN:                    
				{
				b.vertex(x      , y, z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+1.00f, y, z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();                        
				b.vertex(x      , y, z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x      , y, z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();                        
				b.vertex(x+1.00f, y, z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+1.00f, y, z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x      , y, z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+1.00f, y, z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x      , y, z+0.25f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+1.00f, y, z+0.25f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x      , y, z+0.75f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+1.00f, y, z+0.75f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.25f, y, z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.25f, y, z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.75f, y, z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.75f, y, z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();						
				b.vertex(x+0.40f, y, z+0.20f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.50f, y, z+0.05f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.60f, y, z+0.20f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.50f, y, z+0.05f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.40f, y, z+0.80f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.50f, y, z+0.95f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.60f, y, z+0.80f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.50f, y, z+0.95f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.20f, y, z+0.40f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.05f, y, z+0.50f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();						
				b.vertex(x+0.20f, y, z+0.60f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.05f, y, z+0.50f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.80f, y, z+0.40f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.95f, y, z+0.50f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();						
				b.vertex(x+0.80f, y, z+0.60f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.95f, y, z+0.50f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.40f, y, z+0.50f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x+0.50f, y, z+0.40f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x+0.40f, y, z+0.50f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x+0.50f, y, z+0.60f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x+0.60f, y, z+0.50f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x+0.50f, y, z+0.60f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x+0.50f, y, z+0.40f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x+0.60f, y, z+0.50f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();

				b.vertex(x+0.10f, y, z+0.10f) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.20f, y, z+0.14f) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.10f, y, z+0.10f) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.14f, y, z+0.20f) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.90f, y, z+0.90f) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.80f, y, z+0.86f) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.90f, y, z+0.90f) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.86f, y, z+0.80f) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.90f, y, z+0.10f) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.80f, y, z+0.14f) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.90f, y, z+0.10f) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.86f, y, z+0.20f) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.10f, y, z+0.90f) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.20f, y, z+0.86f) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.10f, y, z+0.90f) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.14f, y, z+0.80f) .color(0.0f, 0.0f, 1.0f, 1.0f).next();

			}break;
			case NORTH:                        
			case SOUTH:{
				b.vertex(x      , y        , z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+1.00f, y        , z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();                        
				b.vertex(x      , y        , z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x      , y+1.00f*h, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();                        
				b.vertex(x+1.00f, y        , z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+1.00f, y+1.00f*h, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x      , y+1.00f*h, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+1.00f, y+1.00f*h, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x      , y+0.25f*h, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+1.00f, y+0.25f*h, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x      , y+0.75f*h, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+1.00f, y+0.75f*h, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.25f, y        , z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.25f, y+1.00f*h, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.75f, y       , z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.75f, y+1.00f*h, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.40f, y+0.20f*h, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.50f, y+0.05f*h, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.60f, y+0.20f*h, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.50f, y+0.05f*h, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.40f, y+0.80f*h, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.50f, y+0.95f*h, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.60f, y+0.80f*h, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.50f, y+0.95f*h, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.20f, y+0.40f*h, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.05f, y+0.50f*h, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();						
				b.vertex(x+0.20f, y+0.60f*h, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.05f, y+0.50f*h, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.80f, y+0.40f*h, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.95f, y+0.50f*h, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();						
				b.vertex(x+0.80f, y+0.60f*h, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.95f, y+0.50f*h, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x+0.40f, y+0.50f*h, z) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x+0.50f, y+0.40f*h, z) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x+0.40f, y+0.50f*h, z) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x+0.50f, y+0.60f*h, z) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x+0.60f, y+0.50f*h, z) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x+0.50f, y+0.60f*h, z) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x+0.50f, y+0.40f*h, z) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x+0.60f, y+0.50f*h, z) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x+0.10f, y+0.10f*h, z) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.20f, y+0.14f*h, z) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.10f, y+0.10f*h, z) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.14f, y+0.20f*h, z) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.90f, y+0.90f*h, z) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.80f, y+0.86f*h, z) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.90f, y+0.90f*h, z) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.86f, y+0.80f*h, z) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.90f, y+0.10f*h, z) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.80f, y+0.14f*h, z) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.90f, y+0.10f*h, z) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.86f, y+0.20f*h, z) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.10f, y+0.90f*h, z) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.20f, y+0.86f*h, z) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.10f, y+0.90f*h, z) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x+0.14f, y+0.80f*h, z) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
			}
			break;
			case EAST:                        
			case WEST:{
				
				b.vertex(x,y        ,z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+1.00f*h,z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();                        
				b.vertex(x,y        ,z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x,y        ,z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();                        
				b.vertex(x,y+1.00f*h,z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+1.00f*h,z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x,y        ,z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+1.00f*h,z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x,y        ,z+0.25f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+1.00f*h,z+0.25f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x,y        ,z+0.75f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+1.00f*h,z+0.75f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.25f*h,z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.25f*h,z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.75f*h,z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.75f*h,z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.40f*h,z+0.20f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.50f*h,z+0.05f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.60f*h,z+0.20f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.50f*h,z+0.05f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.40f*h,z+0.80f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.50f*h,z+0.95f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.60f*h,z+0.80f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.50f*h,z+0.95f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.20f*h,z+0.40f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.05f*h,z+0.50f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();						
				b.vertex(x,y+0.20f*h,z+0.60f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.05f*h,z+0.50f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.80f*h,z+0.40f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.95f*h,z+0.50f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();						
				b.vertex(x,y+0.80f*h,z+0.60f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.95f*h,z+0.50f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.40f*h,z+0.50f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.50f*h,z+0.40f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.40f*h,z+0.50f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.50f*h,z+0.60f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.60f*h,z+0.50f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.50f*h,z+0.60f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.50f*h,z+0.40f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.60f*h,z+0.50f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
				b.vertex(x,y+0.10f*h,z+0.10f ) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.20f*h,z+0.14f ) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.10f*h,z+0.10f ) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.14f*h,z+0.20f ) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.90f*h,z+0.90f ) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.80f*h,z+0.86f ) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.90f*h,z+0.90f ) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.86f*h,z+0.80f ) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.90f*h,z+0.10f ) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.80f*h,z+0.14f ) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.90f*h,z+0.10f ) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.86f*h,z+0.20f ) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.10f*h,z+0.90f ) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.20f*h,z+0.86f ) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.10f*h,z+0.90f ) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				b.vertex(x,y+0.14f*h,z+0.80f ) .color(0.0f, 0.0f, 1.0f, 1.0f).next();
				
			}
			break;
		}
	}
}
