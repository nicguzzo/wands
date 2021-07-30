package net.nicguzzo.wands;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.Wand.CopyPasteBuffer;

public class ClientRender {
    public static final float p_o = -0.001f;// preview_block offset
    private static long t0 = 0;
	private static long t1 = 0;
	private static boolean prnt;
    public static Vec3 c=new Vec3(0,0,0);
    static BlockPos last_pos=null;
    static Direction last_side=null;
    static int last_mode=-1;
    static WandItem.Orientation last_orientation=null;
    private static boolean last_valid =false;
    public static Wand wand=new Wand();
    public static void render(PoseStack matrixStack, double camX, double camY, double camZ, MultiBufferSource.BufferSource bufferIn) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        ItemStack stack = player.getMainHandItem();
        prnt = false;
        		
        if (stack!=null && !stack.isEmpty() && stack.getItem() instanceof WandItem) {
            
            t1 = System.currentTimeMillis();
            if (t1 - t0 > 1000) {
                t0 = System.currentTimeMillis();
                prnt = true;
            }
                        
            HitResult hitResult=client.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {                
                BlockHitResult block_hit = (BlockHitResult) hitResult;
                int mode=WandItem.getMode(stack);
                WandItem.Orientation orientation=WandItem.getOrientation(stack);
                Direction side = block_hit.getDirection();
                BlockPos pos = block_hit.getBlockPos();
                BlockState block_state = client.level.getBlockState(pos);
                //if(prnt){
                //    WandsMod.LOGGER.info("render block_state "+block_state);
                //}
                if(last_pos==null || !pos.equals(last_pos) || !side.equals(last_side)
                        || mode==0 || mode==6 || mode!=last_mode || last_valid!=wand.valid
                        || orientation!=last_orientation
                ){
                    //WandsMod.LOGGER.info("wand.do_or_preview");
                    last_pos=pos;
                    last_side=side;
                    last_mode=mode;
                    last_valid=wand.valid;
                    //if(prnt){
                    //    WandsMod.LOGGER.info("render "+wand.block_height);
                    //}
                    wand.do_or_preview(player,player.level,block_state,pos,side,block_hit.getLocation(),stack,prnt);
                }
               
                preview_mode(wand.mode);
                
            }
        }
    }

    private static void preview_mode(int mode) {

        Minecraft client = Minecraft.getInstance();
        Camera camera = client.gameRenderer.getMainCamera();
        if (camera.isInitialized()) {
            c = camera.getPosition().reverse();
            
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tesselator.getBuilder();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            //RenderSystem.shadeModel(7425);
            RenderSystem.enableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.disableBlend();
            //RenderSystem.disableDepthTest();
            //RenderSystem.lineWidth(1.0f);
            bufferBuilder.begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            //bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormat.POSITION_COLOR);
            switch (mode){
                case 0:
                    grid(bufferBuilder,wand.side,
                        c.x + wand.x,
                        c.y + wand.y + wand.y0,
                        c.z + wand.z
                    );
                    if(wand.valid){
                        preview_block(bufferBuilder,
                            c.x+wand.x1 , c.y+(wand.y1+wand.y0), c.z+wand.z1 , 
                            c.x+wand.x2 , c.y+(wand.y1+wand.y0+wand.block_height), c.z+ wand.z2 );
                    }
                break;
                case 1:
                case 2:                
                    //preview_mode1(bufferBuilder);
                    if(wand.valid){
                        preview_block(bufferBuilder,
                            c.x+wand.x1 , c.y+wand.y1 , c.z+wand.z1 , 
                            c.x+wand.x2 , c.y+wand.y2 ,c.z+ wand.z2 );
                    }
                break;
                case 3:
                case 4:
                case 5:

                    if(wand.valid && wand.block_buffer!=null){
                        for (int a = 0; a < wand.block_buffer.length && a< Wand.MAX_LIMIT; a++) {			
							int x=wand.block_buffer.buffer_x[a];
                            int y=wand.block_buffer.buffer_y[a];
                            int z=wand.block_buffer.buffer_z[a];

							preview_block(bufferBuilder,
                                c.x+x  , c.y+y  , c.z+z, 
                                c.x+x+1, c.y+y+1, c.z+z+1
                            );
						}
                    }
                break;
                case 6:
                    if(wand.copy_paste_buffer.size()>0){
                        for (CopyPasteBuffer b: wand.copy_paste_buffer) {
                            int x=last_pos.getX()+(last_pos.getX()-b.pos.getX());
                            int y=last_pos.getY()+(last_pos.getY()-b.pos.getY());
                            int z=last_pos.getZ()+(last_pos.getZ()-b.pos.getZ());
                            preview_block(bufferBuilder,
                                c.x+x  , c.y+y  , c.z+z, 
                                c.x+x+1, c.y+y+1, c.z+z+1
                            );
                        }
                    }
                    if(wand.valid){
                        preview_block(bufferBuilder,
                            c.x+wand.x1 , c.y+wand.y1 , c.z+wand.z1 , 
                            c.x+wand.x2 , c.y+wand.y2 ,c.z+ wand.z2 );
                    }
                break;
            }
            tesselator.end();

            RenderSystem.enableBlend();
            RenderSystem.enableTexture();            
            //RenderSystem.shadeModel(7424);
        }
    }

    private static void preview_block(BufferBuilder bufferBuilder,double fx1, double fy1, double fz1, double fx2, double fy2, double fz2) {
        fx1 += p_o;
        fy1 += p_o;
        fz1 += p_o;
        fx2 -= p_o;
        fy2 -= p_o;
        fz2 -= p_o;
        bufferBuilder.vertex(fx1, fy1, fz1).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz1).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx1, fy1, fz1).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx1, fy1, fz2).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx1, fy1, fz2).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz2).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz1).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz2).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz1).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz1).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz1).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz2).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz2).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz2).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz1).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz2).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx1, fy1, fz1).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz1).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz1).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz1).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx1, fy1, fz2).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz2).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz2).color(255,255,255,255).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz2).color(255,255,255,255).endVertex();
    }

    private static void grid(BufferBuilder bufferBuilder,Direction side, double x, double y, double z) {
        float h=wand.block_height;
        switch (side) {
            case UP:
            case DOWN: {
                bufferBuilder.vertex(x        , y, z        ).color(255,255,255,255).endVertex();//
                bufferBuilder.vertex(x + 1.00f, y, z        ).color(255,255,255,255).endVertex();//
                bufferBuilder.vertex(x        , y, z        ).color(255,255,255,255).endVertex();//
                bufferBuilder.vertex(x        , y, z + 1.00f).color(255,255,255,255).endVertex();//
                bufferBuilder.vertex(x + 1.00f, y, z        ).color(255,255,255,255).endVertex();//
                bufferBuilder.vertex(x + 1.00f, y, z + 1.00f).color(255,255,255,255).endVertex();//
                bufferBuilder.vertex(x        , y, z + 1.00f).color(255,255,255,255).endVertex();//
                bufferBuilder.vertex(x + 1.00f, y, z + 1.00f).color(255,255,255,255).endVertex();//
                bufferBuilder.vertex(x        , y, z + 0.25f).color(255,255,255,255).endVertex();//
                bufferBuilder.vertex(x + 1.00f, y, z + 0.25f).color(255,255,255,255).endVertex();//
                bufferBuilder.vertex(x        , y, z + 0.75f).color(255,255,255,255).endVertex();//
                bufferBuilder.vertex(x + 1.00f, y, z + 0.75f).color(255,255,255,255).endVertex();//
                bufferBuilder.vertex(x + 0.25f, y, z        ).color(255,255,255,255).endVertex();//
                bufferBuilder.vertex(x + 0.25f, y, z + 1.00f).color(255,255,255,255).endVertex();//
                bufferBuilder.vertex(x + 0.75f, y, z        ).color(255,255,255,255).endVertex();//
                bufferBuilder.vertex(x + 0.75f, y, z + 1.00f).color(255,255,255,255).endVertex();//
                
                bufferBuilder.vertex(x + 0.40f, y, z + 0.20f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.50f, y, z + 0.05f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.60f, y, z + 0.20f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.50f, y, z + 0.05f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.40f, y, z + 0.80f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.50f, y, z + 0.95f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.60f, y, z + 0.80f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.50f, y, z + 0.95f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.20f, y, z + 0.40f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.05f, y, z + 0.50f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.20f, y, z + 0.60f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.05f, y, z + 0.50f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.80f, y, z + 0.40f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.95f, y, z + 0.50f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.80f, y, z + 0.60f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.95f, y, z + 0.50f).color(178,0,0,255).endVertex();
                //RenderSystem.color4f( 0f,178f,  0f, 255f);//g
                bufferBuilder.vertex(x + 0.40f, y, z + 0.50f).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x + 0.50f, y, z + 0.40f).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x + 0.40f, y, z + 0.50f).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x + 0.50f, y, z + 0.60f).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x + 0.60f, y, z + 0.50f).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x + 0.50f, y, z + 0.60f).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x + 0.50f, y, z + 0.40f).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x + 0.60f, y, z + 0.50f).color(0,178,0,255).endVertex();
                //RenderSystem.color4f( 0f, 0f,178f, 255f);//b
                bufferBuilder.vertex(x + 0.10f, y, z + 0.10f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.20f, y, z + 0.14f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.10f, y, z + 0.10f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.14f, y, z + 0.20f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.90f, y, z + 0.90f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.80f, y, z + 0.86f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.90f, y, z + 0.90f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.86f, y, z + 0.80f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.90f, y, z + 0.10f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.80f, y, z + 0.14f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.90f, y, z + 0.10f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.86f, y, z + 0.20f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.10f, y, z + 0.90f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.20f, y, z + 0.86f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.10f, y, z + 0.90f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.14f, y, z + 0.80f).color(0,0,178,255).endVertex();

            }
            break;
            case NORTH:
            case SOUTH: {
                //RenderSystem.color4f( 255f, 255f, 255f, 255f);//w
                bufferBuilder.vertex(x, y, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x + 1.00f, y, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y + 1.00f * h, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x + 1.00f, y, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x + 1.00f, y + 1.00f * h, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y + 1.00f * h, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x + 1.00f, y + 1.00f * h, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y + 0.25f * h, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x + 1.00f, y + 0.25f * h, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y + 0.75f * h, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x + 1.00f, y + 0.75f * h, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x + 0.25f, y, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x + 0.25f, y + 1.00f * h, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x + 0.75f, y, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x + 0.75f, y + 1.00f * h, z).color(255,255,255,255).endVertex();
                //RenderSystem.color4f( 178f, 0, 0, 255f);//r
                bufferBuilder.vertex(x + 0.40f, y + 0.20f * h, z).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.50f, y + 0.05f * h, z).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.60f, y + 0.20f * h, z).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.50f, y + 0.05f * h, z).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.40f, y + 0.80f * h, z).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.50f, y + 0.95f * h, z).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.60f, y + 0.80f * h, z).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.50f, y + 0.95f * h, z).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.20f, y + 0.40f * h, z).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.05f, y + 0.50f * h, z).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.20f, y + 0.60f * h, z).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.05f, y + 0.50f * h, z).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.80f, y + 0.40f * h, z).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.95f, y + 0.50f * h, z).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.80f, y + 0.60f * h, z).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x + 0.95f, y + 0.50f * h, z).color(178,0,0,255).endVertex();
                //RenderSystem.color4f( 0f,178f,  0.0f, 255f).endVertex();//g
                bufferBuilder.vertex(x + 0.40f, y + 0.50f * h, z).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x + 0.50f, y + 0.40f * h, z).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x + 0.40f, y + 0.50f * h, z).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x + 0.50f, y + 0.60f * h, z).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x + 0.60f, y + 0.50f * h, z).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x + 0.50f, y + 0.60f * h, z).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x + 0.50f, y + 0.40f * h, z).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x + 0.60f, y + 0.50f * h, z).color(0,178,0,255).endVertex();
                //RenderSystem.color4f( 0, 0,255f, 255f).endVertex();//b
                bufferBuilder.vertex(x + 0.10f, y + 0.10f * h, z).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.20f, y + 0.14f * h, z).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.10f, y + 0.10f * h, z).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.14f, y + 0.20f * h, z).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.90f, y + 0.90f * h, z).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.80f, y + 0.86f * h, z).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.90f, y + 0.90f * h, z).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.86f, y + 0.80f * h, z).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.90f, y + 0.10f * h, z).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.80f, y + 0.14f * h, z).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.90f, y + 0.10f * h, z).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.86f, y + 0.20f * h, z).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.10f, y + 0.90f * h, z).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.20f, y + 0.86f * h, z).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.10f, y + 0.90f * h, z).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x + 0.14f, y + 0.80f * h, z).color(0,0,178,255).endVertex();
            }
            break;
            case EAST:
            case WEST: {
                //RenderSystem.color4f( 255f, 255f, 255f, 255f).endVertex();//w
                bufferBuilder.vertex(x, y, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y + 1.00f * h, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y, z + 1.00f).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y + 1.00f * h, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y + 1.00f * h, z + 1.00f).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y, z + 1.00f).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y + 1.00f * h, z + 1.00f).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y, z + 0.25f).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y + 1.00f * h, z + 0.25f).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y, z + 0.75f).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y + 1.00f * h, z + 0.75f).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y + 0.25f * h, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y + 0.25f * h, z + 1.00f).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y + 0.75f * h, z).color(255,255,255,255).endVertex();
                bufferBuilder.vertex(x, y + 0.75f * h, z + 1.00f).color(255,255,255,255).endVertex();
                //RenderSystem.color4f( 178f, 0, 0, 255f).endVertex();//b
                bufferBuilder.vertex(x, y + 0.40f * h, z + 0.20f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.50f * h, z + 0.05f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.60f * h, z + 0.20f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.50f * h, z + 0.05f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.40f * h, z + 0.80f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.50f * h, z + 0.95f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.60f * h, z + 0.80f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.50f * h, z + 0.95f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.20f * h, z + 0.40f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.05f * h, z + 0.50f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.20f * h, z + 0.60f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.05f * h, z + 0.50f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.80f * h, z + 0.40f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.95f * h, z + 0.50f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.80f * h, z + 0.60f).color(178,0,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.95f * h, z + 0.50f).color(178,0,0,255).endVertex();
                //RenderSystem.color4f( 178f, 255f, 255f, 255f).endVertex();//g
                bufferBuilder.vertex(x, y + 0.40f * h, z + 0.50f).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.50f * h, z + 0.40f).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.40f * h, z + 0.50f).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.50f * h, z + 0.60f).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.60f * h, z + 0.50f).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.50f * h, z + 0.60f).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.50f * h, z + 0.40f).color(0,178,0,255).endVertex();
                bufferBuilder.vertex(x, y + 0.60f * h, z + 0.50f).color(0,178,0,255).endVertex();
                //RenderSystem.color4f( 0, 0,255f, 255f).endVertex();//b
                bufferBuilder.vertex(x, y + 0.10f * h, z + 0.10f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x, y + 0.20f * h, z + 0.14f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x, y + 0.10f * h, z + 0.10f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x, y + 0.14f * h, z + 0.20f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x, y + 0.90f * h, z + 0.90f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x, y + 0.80f * h, z + 0.86f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x, y + 0.90f * h, z + 0.90f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x, y + 0.86f * h, z + 0.80f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x, y + 0.90f * h, z + 0.10f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x, y + 0.80f * h, z + 0.14f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x, y + 0.90f * h, z + 0.10f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x, y + 0.86f * h, z + 0.20f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x, y + 0.10f * h, z + 0.90f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x, y + 0.20f * h, z + 0.86f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x, y + 0.10f * h, z + 0.90f).color(0,0,178,255).endVertex();
                bufferBuilder.vertex(x, y + 0.14f * h, z + 0.80f).color(0,0,178,255).endVertex();

            }
            break;
        }
    }

    
    
}

