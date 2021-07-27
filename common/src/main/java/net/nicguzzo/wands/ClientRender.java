package net.nicguzzo.wands;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ClientRender {
    public static final float p_o = -0.001f;// preview_block offset
    //private static long t0 = 0;
	//private static long t1 = 0;
	//private static boolean prnt = false;
    public static Vec3 c=new Vec3(0,0,0);
    public static BlockBuffer block_buffer=null;
    //private static final Logger LOGGER = LogManager.getLogger();

    public static void render(PoseStack matrixStack, double camX, double camY, double camZ, MultiBufferSource.BufferSource bufferIn) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        ItemStack stack = player.getMainHandItem();
        //prnt = false;
        		
        if (stack!=null && !stack.isEmpty() && stack.getItem() instanceof WandItem) {
            if(block_buffer==null){
                block_buffer=new BlockBuffer(PlayerWandInfo.MAX_LIMIT);
            }
            //t1 = System.currentTimeMillis();
            //if (t1 - t0 > 1000) {
            //    t0 = System.currentTimeMillis();
            //    prnt = true;
            //}
            //WandItem wand = (WandItem) stack.getItem();
            
            HitResult hitResult=client.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {                
                BlockHitResult block_hit = (BlockHitResult) hitResult;
                //int mode=WandItem.getMode(stack);
                Direction side = block_hit.getDirection();
                BlockPos pos = block_hit.getBlockPos();
                BlockState block_state = client.level.getBlockState(pos);
                WandItem.do_or_preview(player,player.level,block_state,pos,side,block_hit.getLocation(),stack,block_buffer,null);
                //if(prnt){
                //    LOGGER.info("render");
                //}
                preview_mode(WandItem.getMode(stack));                
                
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
            //RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.shadeModel(7425);
            RenderSystem.enableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.disableBlend();
            //RenderSystem.lineWidth(1.0f);
            bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormat.POSITION_COLOR);
            switch (mode){
                case 0:
                    grid(bufferBuilder,WandItem.PreviewInfo.side,
                        c.x + WandItem.PreviewInfo.x,
                        c.y + WandItem.PreviewInfo.y + WandItem.PreviewInfo.y0,
                        c.z + WandItem.PreviewInfo.z
                    );
                    if(WandItem.PreviewInfo.valid){
                        preview_block(bufferBuilder,
                            c.x+WandItem.PreviewInfo.x1 , c.y+(WandItem.PreviewInfo.y1+WandItem.PreviewInfo.y0), c.z+WandItem.PreviewInfo.z1 , 
                            c.x+WandItem.PreviewInfo.x2 , c.y+(WandItem.PreviewInfo.y1+WandItem.PreviewInfo.y0+WandItem.PreviewInfo.h), c.z+ WandItem.PreviewInfo.z2 );
                    }
                break;
                case 1:
                case 2:
                    //preview_mode1(bufferBuilder);
                    if(WandItem.PreviewInfo.valid){
                        preview_block(bufferBuilder,
                            c.x+WandItem.PreviewInfo.x1 , c.y+WandItem.PreviewInfo.y1 , c.z+WandItem.PreviewInfo.z1 , 
                            c.x+WandItem.PreviewInfo.x2 , c.y+WandItem.PreviewInfo.y2 ,c.z+ WandItem.PreviewInfo.z2 );
                    }
                break;
                case 3:
                case 4:
                case 5:
                    if(WandItem.PreviewInfo.valid && block_buffer!=null){
                        for (int a = 0; a < block_buffer.length && a< PlayerWandInfo.MAX_LIMIT; a++) {			
							int x=block_buffer.buffer_x[a];
                            int y=block_buffer.buffer_y[a];
                            int z=block_buffer.buffer_z[a];
							preview_block(bufferBuilder,
                                c.x+x  , c.y+y  , c.z+z, 
                                c.x+x+1, c.y+y+1, c.z+z+1
                            );
						}
                    }
                break;
            }
            tesselator.end();

            RenderSystem.enableBlend();
            RenderSystem.enableTexture();            
            RenderSystem.shadeModel(7424);
        }
    }

    private static void preview_block(BufferBuilder bufferBuilder,double fx1, double fy1, double fz1, double fx2, double fy2, double fz2) {
        fx1 += p_o;
        fy1 += p_o;
        fz1 += p_o;
        fx2 -= p_o;
        fy2 -= p_o;
        fz2 -= p_o;
        //TODO: optimize with line strip
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
        float h=WandItem.PreviewInfo.h;
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

