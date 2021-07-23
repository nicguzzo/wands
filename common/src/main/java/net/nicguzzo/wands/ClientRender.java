package net.nicguzzo.wands;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

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
    private static long t0 = 0;
	private static long t1 = 0;
	private static boolean prnt = false;
    public static Vec3 c=new Vec3(0,0,0);

    public static void render(PoseStack matrixStack, double camX, double camY, double camZ, MultiBufferSource.BufferSource bufferIn) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        ItemStack stack = player.getMainHandItem();
        prnt = false;
		t1 = System.currentTimeMillis();
		if (t1 - t0 > 1000) {
			t0 = System.currentTimeMillis();
			prnt = true;			
            
		}
        if (stack!=null && !stack.isEmpty() && stack.getItem() instanceof WandItem) {
            WandItem wand = (WandItem) stack.getItem();
            HitResult hitResult=client.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult block_hit = (BlockHitResult) hitResult;
                Direction side = block_hit.getDirection();
                BlockPos pos = block_hit.getBlockPos();
                BlockState block_state = client.level.getBlockState(pos);
                if(wand.do_or_preview(player.level,block_state,pos,side,block_hit.getLocation(),stack,true)){
                    if(prnt){
                        System.out.println("render");
                    }
                    preview_mode(WandItem.getMode(stack));
                }
            } else {
                WandsModClient.valid = false;
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
                    preview_mode0(bufferBuilder);
                    break;
            }

            tesselator.end();

            RenderSystem.enableBlend();
            RenderSystem.enableTexture();            
            RenderSystem.shadeModel(7424);
        }
    }
    private static void preview_mode0(BufferBuilder bufferBuilder){
        grid(bufferBuilder,WandItem.PreviewInfo.side,
                c.x + WandItem.PreviewInfo.x,
                c.y + WandItem.PreviewInfo.y + WandItem.PreviewInfo.y0,
                c.z + WandItem.PreviewInfo.z
        );
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

    static private void prev_drawCircle(BufferBuilder bufferBuilder,Vec3 c,int xc, int yc,int zc, int x, int y,int z,int plane)
    {
        switch(plane){
            case 0://XZ
                preview_block(bufferBuilder, c.x+ xc+x, c.y+ yc, c.z+ zc+z,   c.x+ xc+x+1, c.y+ yc+1, c.z+ zc+z+1 );
                preview_block(bufferBuilder, c.x+ xc-x, c.y+ yc, c.z+ zc+z,   c.x+ xc-x+1, c.y+ yc+1, c.z+ zc+z+1 );
                preview_block(bufferBuilder, c.x+ xc+x, c.y+ yc, c.z+ zc-z,   c.x+ xc+x+1, c.y+ yc+1, c.z+ zc-z+1 );
                preview_block(bufferBuilder, c.x+ xc-x, c.y+ yc, c.z+ zc-z,   c.x+ xc-x+1, c.y+ yc+1, c.z+ zc-z+1 );
                preview_block(bufferBuilder, c.x+ xc+z, c.y+ yc, c.z+ zc+x,   c.x+ xc+z+1, c.y+ yc+1, c.z+ zc+x+1 );
                preview_block(bufferBuilder, c.x+ xc-z, c.y+ yc, c.z+ zc+x,   c.x+ xc-z+1, c.y+ yc+1, c.z+ zc+x+1 );
                preview_block(bufferBuilder, c.x+ xc+z, c.y+ yc, c.z+ zc-x,   c.x+ xc+z+1, c.y+ yc+1, c.z+ zc-x+1 );
                preview_block(bufferBuilder, c.x+ xc-z, c.y+ yc, c.z+ zc-x,   c.x+ xc-z+1, c.y+ yc+1, c.z+ zc-x+1 );
                break;
            case 1://XY
                preview_block(bufferBuilder, c.x+ xc+x, c.y+ yc+y, c.z+ zc,   c.x+  xc+x+1,c.y+  yc+y+1,c.z+  zc+1 );
                preview_block(bufferBuilder, c.x+ xc-x, c.y+ yc+y, c.z+ zc,   c.x+  xc-x+1,c.y+  yc+y+1,c.z+  zc+1 );
                preview_block(bufferBuilder, c.x+ xc+x, c.y+ yc-y, c.z+ zc,   c.x+  xc+x+1,c.y+  yc-y+1,c.z+  zc+1 );
                preview_block(bufferBuilder, c.x+ xc-x, c.y+ yc-y, c.z+ zc,   c.x+  xc-x+1,c.y+  yc-y+1,c.z+  zc+1 );
                preview_block(bufferBuilder, c.x+ xc+y, c.y+ yc+x, c.z+ zc,   c.x+  xc+y+1,c.y+  yc+x+1,c.z+  zc+1 );
                preview_block(bufferBuilder, c.x+ xc-y, c.y+ yc+x, c.z+ zc,   c.x+  xc-y+1,c.y+  yc+x+1,c.z+  zc+1 );
                preview_block(bufferBuilder, c.x+ xc+y, c.y+ yc-x, c.z+ zc,   c.x+  xc+y+1,c.y+  yc-x+1,c.z+  zc+1 );
                preview_block(bufferBuilder, c.x+ xc-y, c.y+ yc-x, c.z+ zc,   c.x+  xc-y+1,c.y+  yc-x+1,c.z+  zc+1 );
                break;
            case 2://YZ
                preview_block(bufferBuilder, c.x+ xc, c.y+ yc+y,c.z+  zc+z,   c.x+  xc+1, c.y+ yc+y+1, c.z+ zc+z+1  );
                preview_block(bufferBuilder, c.x+ xc, c.y+ yc-y,c.z+  zc+z,   c.x+  xc+1, c.y+ yc-y+1, c.z+ zc+z+1  );
                preview_block(bufferBuilder, c.x+ xc, c.y+ yc+y,c.z+  zc-z,   c.x+  xc+1, c.y+ yc+y+1, c.z+ zc-z+1  );
                preview_block(bufferBuilder, c.x+ xc, c.y+ yc-y,c.z+  zc-z,   c.x+  xc+1, c.y+ yc-y+1, c.z+ zc-z+1  );
                preview_block(bufferBuilder, c.x+ xc, c.y+ yc+z,c.z+  zc+y,   c.x+  xc+1, c.y+ yc+z+1, c.z+ zc+y+1  );
                preview_block(bufferBuilder, c.x+ xc, c.y+ yc-z,c.z+  zc+y,   c.x+  xc+1, c.y+ yc-z+1, c.z+ zc+y+1  );
                preview_block(bufferBuilder, c.x+ xc, c.y+ yc+z,c.z+  zc-y,   c.x+  xc+1, c.y+ yc+z+1, c.z+ zc-y+1  );
                preview_block(bufferBuilder, c.x+ xc, c.y+ yc-z,c.z+  zc-y,   c.x+  xc+1, c.y+ yc-z+1, c.z+ zc-y+1  );
                break;
        }
    }
    private static void circle(BufferBuilder bufferBuilder,Vec3 c,BlockPos pos0,BlockPos pos1,int plane){

        int r =1;
        int xc=pos0.getX();
        int yc=pos0.getY();
        int zc=pos0.getZ();
        int px=pos1.getX()-pos0.getX();
        int py=pos1.getY()-pos0.getY();
        int pz=pos1.getZ()-pos0.getZ();
        r=(int)Math.sqrt(px*px+py*py+pz*pz );

        if(plane==0){//XZ;
            int x = 0, y=0, z = r;
            int d = 3 - 2 * r;
            prev_drawCircle(bufferBuilder,c,xc, yc, zc, x, y, z, plane);
            while (z >= x)
            {
                x++;
                if (d > 0)
                {
                    z--;
                    d = d + 4 * (x - z) + 10;
                } else
                    d = d + 4 * x + 6;
                prev_drawCircle(bufferBuilder,c,xc, yc, zc, x, y, z, plane);
            }
        } else if (plane == 1) {// XY;
            int x = 0, y = r, z = 0;
            int d = 3 - 2 * r;
            prev_drawCircle(bufferBuilder,c,xc, yc, zc, x, y, z, plane);
            while (y >= x)
            {
                x++;
                if (d > 0)
                {
                    y--;
                    d = d + 4 * (x - y) + 10;
                } else
                    d = d + 4 * x + 6;
                prev_drawCircle(bufferBuilder,c,xc, yc, zc, x, y, z, plane);
            }
        } else if (plane == 2) {// YZ;
            int x = 0, y = 0, z = r;
            int d = 3 - 2 * r;
            prev_drawCircle(bufferBuilder,c,xc, yc, zc, x, y, z, plane);
            while (z >= y)
            {
                y++;
                if (d > 0)
                {
                    z--;
                    d = d + 4 * (y - z) + 10;
                } else
                    d = d + 4 * y + 6;
                prev_drawCircle(bufferBuilder,c,xc, yc, zc, x, y, z, plane);
            }
        }

    }

    private static void line(BufferBuilder bufferBuilder,Vec3 c,BlockPos pos0,BlockPos pos1)
    {

        int x1=pos0.getX();
        int y1=pos0.getY();
        int z1=pos0.getZ();
        int x2=pos1.getX();
        int y2=pos1.getY();
        int z2=pos1.getZ();
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
        preview_block(bufferBuilder,c.x+x1, c.y+y1, c.z+z1,c.x+x1+1,c.y+y1+1,c.z+z1+1);
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

                preview_block(bufferBuilder,c.x+x1, c.y+y1, c.z+z1,c.x+x1+1,c.y+y1+1,c.z+z1+1);
                //LOGGER.info("line pos " +pos);

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
                preview_block(bufferBuilder,c.x+x1, c.y+y1, c.z+z1,c.x+x1+1,c.y+y1+1,c.z+z1+1);

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
                preview_block(bufferBuilder,c.x+x1, c.y+y1, c.z+z1,c.x+x1+1,c.y+y1+1,c.z+z1+1);
            }
        }
    }
}

