package net.nicguzzo.wands;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

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

import java.util.List;
import java.util.Random;

public class ClientRender {
    public static final float p_o = -0.001f;// preview_block offset
    private static long t0 = 0;
	private static long t1 = 0;
    private static long t00 = 0;
	private static boolean prnt;
    public static Vec3 c=new Vec3(0,0,0);
    static BlockPos last_pos=null;
    static Direction last_side=null;
    static int last_mode=-1;
    static int last_rot=0;
    static boolean last_alt=false;
    //static int last_y=0;
    static int last_buffer_size=-1;
    static WandItem.Orientation last_orientation=null;
    //private static boolean last_valid =false;
    public static Wand wand=new Wand();
    static VoxelShape preview_shape =null;
    static RenderShape render_shape =null;
    static AABB def_aabb=new AABB(0,0,0,1,1,1);
    static private final int grid_n=16;
    static private int grid_i=0;
    static private final double[] grid_vx=new double[grid_n];
    static private final double[] grid_vy=new double[grid_n];
    static private final double[] grid_vz=new double[grid_n];
    static boolean force=false;
    static double x1=0;
    static double y1=0;
    static double z1=0;
    static double x2=0;
    static double y2=0;
    static double z2=0;
    static float opacity=0.8f;
    private static final ResourceLocation GRID_TEXTURE = new ResourceLocation("wands", "textures/blocks/grid.png");
    static Random random=new Random();
    static Direction[] dirs={ Direction.DOWN,Direction.UP,Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST,null};
    public static void render(PoseStack matrixStack, double camX, double camY, double camZ, MultiBufferSource.BufferSource bufferIn) {
        opacity=WandsMod.config.preview_opacity;
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        ItemStack stack = player.getMainHandItem();
        prnt = false;
        force=false;
        if (stack!=null && !stack.isEmpty() && stack.getItem() instanceof WandItem) {
            
            t1 = System.currentTimeMillis();
            if (t1 - t0 > 1000) {
                t0 = System.currentTimeMillis();
                prnt = true;
            }//else{
            if (t1 - t00 > 100) {
                t00 = System.currentTimeMillis();
               force=true;
            }

            //if(mode==6 && wand.copy_pos1!=null && wand.copy_pos2!=null){
            //    preview_mode(wand.mode);
            //}
            HitResult hitResult=client.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult block_hit = (BlockHitResult) hitResult;
                int mode = WandItem.getMode(stack);
                int rot = WandItem.getRotation(stack);
                WandItem.Orientation orientation = WandItem.getOrientation(stack);
                Direction side = block_hit.getDirection();
                BlockPos pos = block_hit.getBlockPos();
                BlockState block_state = client.level.getBlockState(pos);
                //if(prnt){
                //    WandsMod.LOGGER.ainfo("render block_state "+block_state);
                //}

                /*
                ItemStack offhand = player.getOffhandItem();
                Block offhand_block=Block.byItem(offhand.getItem());

                if (offhand_block instanceof SlabBlock || offhand_block instanceof StairBlock) {
                    double hity = WandUtils.unitCoord(hitResult.getLocation().y);
                    if (hity > 0.5) {
                        last_y=0;
                    }else{
                        last_y=1;
                    }
                    wand.force_render=last_y!=((int)(hity+0.5));
                    //force=true;
                }             */
                if ( force /*|| wand.force_render || !pos.equals(last_pos) || !side.equals(last_side)
                        || mode == 0 || mode != last_mode || last_valid != wand.valid
                        || orientation != last_orientation || last_rot!=rot || last_alt!=wand.is_alt_pressed
                        || last_buffer_size!=wand.block_buffer.get_length()*/
                ) {
                    wand.force_render=false;
                    //WandsMod.log("render ",prnt);
                    if(mode==2||mode==4||mode==5||mode==6) {
                        if (wand.is_alt_pressed) {
                            //WandsMod.log("pos "+pos,prnt);
                            pos = pos.relative(side, 1);
                            //WandsMod.log("pos "+pos,prnt);
                        }
                    }
                    //WandsMod.LOGGER.info("wand.do_or_preview");
                    last_pos = pos;
                    last_side = side;
                    last_mode = mode;
                    last_orientation = orientation;
                    last_rot=rot;
                    //last_valid = wand.valid;
                    last_alt=wand.is_alt_pressed;
                    last_buffer_size=wand.block_buffer.get_length();
                    //if(prnt){
                    //    WandsMod.LOGGER.info("render "+wand.block_height);
                    //}
                    wand.do_or_preview(player, player.level, block_state, pos, side, block_hit.getLocation(), stack, prnt);
                }
                preview_shape =null;
                //if(offhand_block!=null && offhand_block!= Blocks.AIR){
//                    block_state=offhand_block.defaultBlockState();
//                }
                if(block_state!=null) {
                    preview_shape = block_state.getShape(client.level, last_pos);
                    render_shape=block_state.getRenderShape();
                    //block_state.getMaterial()
                }
                preview_mode(wand.mode,matrixStack,bufferIn);
            }
        }
    }

    private static void preview_mode(int mode, PoseStack matrixStack,MultiBufferSource.BufferSource bufferIn) {

        Minecraft client = Minecraft.getInstance();
        Camera camera = client.gameRenderer.getMainCamera();
        //client.gameRenderer.lightTexture().turnOnLightLayer();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        //RenderSystem.depthMask(false);
        //RenderSystem.depthMask(false);
        //RenderSystem.depthMask(Minecraft.useShaderTransparency());
        //RenderSystem.enableDepthTest();
        if(Screen.hasControlDown() || (WandsModClient.is_forge&& Minecraft.useShaderTransparency() )){
            RenderSystem.disableDepthTest();
        }else{
            RenderSystem.enableDepthTest();
        }
        //RenderSystem.clearDepth(0);
        if (camera.isInitialized()) {
            c = camera.getPosition().reverse();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            boolean fancy=true;
            switch (mode) {
                case 0:
                    if (wand.valid) {
                        boolean no_shape=false;
                        //preview_shape=null;
                        if(preview_shape !=null && !preview_shape.isEmpty()) {
                            List<AABB> list = preview_shape.toAabbs();

                            //WandsMod.log("--",prnt);
                            if(!list.isEmpty() && wand.grid_voxel_index>=0 && wand.grid_voxel_index< list.size()) {

                                if(fancy) {

//                                RenderSystem.disableBlend();
                                    RenderSystem.enableBlend();
                                    RenderSystem.defaultBlendFunc();
                                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                                    RenderSystem.setShaderTexture(0, GRID_TEXTURE);
                                    RenderSystem.enableTexture();
                                    RenderSystem.disableCull();
                                    //RenderSystem.enableCull();
                                    bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                                    int vi = 0;
                                    for (AABB aabb : list) {
                                        if (vi == wand.grid_voxel_index) {
                                            switch (wand.side) {
                                                case UP:
                                                case DOWN:
                                                    x1 = c.x + last_pos.getX() + aabb.minX;
                                                    y1 = c.y + (wand.side == Direction.UP ? last_pos.getY() + aabb.maxY + 0.01 : last_pos.getY() + aabb.minY - 0.01);
                                                    z1 = c.z + last_pos.getZ() + aabb.minZ;
                                                    x2 = c.x + last_pos.getX() + aabb.maxX;
                                                    z2 = c.z + last_pos.getZ() + aabb.maxZ;
                                                    bufferBuilder.vertex(x1, y1, z1).uv(0, 0).endVertex();
                                                    bufferBuilder.vertex(x1, y1, z2).uv(0, 1).endVertex();
                                                    bufferBuilder.vertex(x2, y1, z2).uv(1, 1).endVertex();
                                                    bufferBuilder.vertex(x2, y1, z1).uv(1, 0).endVertex();
                                                    break;
                                                case NORTH:
                                                case SOUTH:
                                                    x1 = c.x + last_pos.getX() + aabb.minX;
                                                    y1 = c.y + last_pos.getY() + aabb.minY;
                                                    z1 = c.z + (wand.side == Direction.SOUTH ? last_pos.getZ() + aabb.maxZ + 0.01 : last_pos.getZ() + aabb.minZ - 0.01);
                                                    x2 = c.x + last_pos.getX() + aabb.maxX;
                                                    y2 = c.y + last_pos.getY() + aabb.maxY;
                                                    bufferBuilder.vertex(x1, y1, z1).uv(0, 0).endVertex();
                                                    bufferBuilder.vertex(x1, y2, z1).uv(0, 1).endVertex();
                                                    bufferBuilder.vertex(x2, y2, z1).uv(1, 1).endVertex();
                                                    bufferBuilder.vertex(x2, y1, z1).uv(1, 0).endVertex();
                                                    break;
                                                case WEST:
                                                case EAST:
                                                    x1 = c.x + (wand.side == Direction.EAST ? last_pos.getX() + aabb.maxX + 0.01 : last_pos.getX() + aabb.minX - 0.01);
                                                    y1 = c.y + last_pos.getY() + aabb.minY;
                                                    z1 = c.z + last_pos.getZ() + aabb.minZ;
                                                    y2 = c.y + last_pos.getY() + aabb.maxY;
                                                    z2 = c.z + last_pos.getZ() + aabb.maxZ;

                                                    bufferBuilder.vertex(x1, y1, z1).uv(0, 0).endVertex();
                                                    bufferBuilder.vertex(x1, y1, z2).uv(0, 1).endVertex();
                                                    bufferBuilder.vertex(x1, y2, z2).uv(1, 1).endVertex();
                                                    bufferBuilder.vertex(x1, y2, z1).uv(1, 0).endVertex();
                                                    break;
                                            }
                                        }
                                        vi++;
                                    }
                                    tesselator.end();
                                }else{
                                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                                    RenderSystem.disableTexture();
                                    RenderSystem.disableBlend();
                                    RenderSystem.disableCull();
                                    RenderSystem.lineWidth(2.0F);
                                    bufferBuilder.begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                                    int vi = 0;
                                    for (AABB aabb : list) {
                                        if (vi == wand.grid_voxel_index) {
                                            //WandsMod.log(aabb.toString(),prnt);
                                            //WandsMod.log("wand x: "+wand.x+" y: "+wand.y+" z: "+wand.z,prnt);
                                            grid(bufferBuilder, wand.side, c,
                                                    last_pos.getX(),
                                                    last_pos.getY(),
                                                    last_pos.getZ(),
                                                    aabb
                                            );
                                        }
                                        vi++;
                                    }
                                    //preview_block(bufferBuilder,
                                    //    c.x + wand.x1 + aabb.minX, c.y + wand.y1 + aabb.minY, c.z + wand.z1 + aabb.minZ,
                                    //   c.x + wand.x1 + aabb.maxX, c.y + wand.y1 + aabb.maxY, c.z + wand.z1 + aabb.maxZ,
                                    //    255,255,255,255);
                                    //}
                                    tesselator.end();
                                }

                            }else{
                                no_shape=true;
                            }
                        }else {
                            no_shape=true;
                        }
                        if(no_shape) {
                            RenderSystem.setShader(GameRenderer::getPositionColorShader);
                            RenderSystem.disableTexture();
                            RenderSystem.disableBlend();
                            bufferBuilder.begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                            grid(bufferBuilder, wand.side,c,
                                    wand.x,
                                    wand.y,
                                    wand.z,
                                    def_aabb
                                    //1.0f,wand.block_height
                            );
                            preview_block(bufferBuilder,
                                    c.x + wand.x1, c.y + (wand.y1 + wand.y0), c.z + wand.z1,
                                    c.x + wand.x2, c.y + (wand.y1 + wand.y0 + wand.block_height), c.z + wand.z2,
                                    255,255,255,255);
                            tesselator.end();
                        }
                    }
                    //break;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:

                    //preview_mode1(bufferBuilder);
                    if (wand.valid) {
                        if(mode==1 || mode==2)
                        {
                            RenderSystem.setShader(GameRenderer::getPositionColorShader);
                            RenderSystem.disableTexture();
                            RenderSystem.disableBlend();
                            bufferBuilder.begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                            preview_block(bufferBuilder,
                                    c.x + wand.x1+0.01f, c.y + wand.y1+0.01f, c.z + wand.z1-0.01f,
                                    c.x + wand.x2-0.01f, c.y + wand.y2-0.01f, c.z + wand.z2+0.01f,
                                    0,255,0,255);
                            tesselator.end();
                        }

                        if (wand.block_buffer != null) {
                            random.setSeed(0);
                            if(!wand.destroy) {
                                setRender_shape_begin(tesselator, bufferBuilder);
                                for (int a = 0; a < wand.block_buffer.get_length() && a < Wand.MAX_LIMIT; a++) {
                                    int x = wand.block_buffer.buffer_x[a];
                                    int y = wand.block_buffer.buffer_y[a];
                                    int z = wand.block_buffer.buffer_z[a];
                                    if (wand.block_buffer.state[a] != null) {
                                        preview_shape = wand.block_buffer.state[a].getShape(client.level, last_pos);
                                        render_shape(tesselator, bufferBuilder, wand.block_buffer.state[a], x, y, z);
                                    }
                                }
                                tesselator.end();
                            }
                            RenderSystem.enableCull();
                            RenderSystem.setShader(GameRenderer::getPositionColorShader);
                            RenderSystem.disableTexture();
                            RenderSystem.disableBlend();
                            bufferBuilder.begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                            for (int a = 0; a < wand.block_buffer.get_length() && a < Wand.MAX_LIMIT; a++) {
                                int x = wand.block_buffer.buffer_x[a];
                                int y = wand.block_buffer.buffer_y[a];
                                int z = wand.block_buffer.buffer_z[a];
                                if (wand.block_buffer.state[a] != null) {
                                    preview_shape = wand.block_buffer.state[a].getShape(client.level, last_pos);
                                    List<AABB> list = preview_shape.toAabbs();
                                    for (AABB aabb : list) {
                                        preview_block(bufferBuilder,
                                                c.x + x + aabb.minX, c.y + y + aabb.minY, c.z + z + aabb.minZ,
                                                c.x + x + aabb.maxX, c.y + y + aabb.maxY, c.z + z + aabb.maxZ,
                                                255, 255, 255, 255);
                                    }
                                }
                            }
                            tesselator.end();
                        }else {
                            RenderSystem.setShader(GameRenderer::getPositionColorShader);
                            RenderSystem.disableTexture();
                            RenderSystem.disableBlend();
                            bufferBuilder.begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                            preview_block(bufferBuilder,
                                    c.x + wand.x1, c.y + wand.y1, c.z + wand.z1,
                                    c.x + wand.x2, c.y + wand.y2, c.z + wand.z2,
                                    255,255,255,255);
                            /*preview_block(bufferBuilder,
                                    c.x + x, c.y + y, c.z + z,
                                    c.x + x + 1, c.y + y + 1, c.z + z + 1,
                                    255, 255, 255, 255
                            );*/
                            tesselator.end();
                        }
                    }
                    break;
                case 6:

                    if (wand.valid) {
                        RenderSystem.setShader(GameRenderer::getPositionColorShader);
                        RenderSystem.disableTexture();
                        RenderSystem.disableBlend();
                        bufferBuilder.begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                        preview_block(bufferBuilder,
                                c.x + wand.copy_x1, c.y + wand.copy_y1, c.z + wand.copy_z1,
                                c.x + wand.copy_x2, c.y + wand.copy_y2, c.z + wand.copy_z2,
                                0,0,255,255);
                        tesselator.end();
                    }

                break;
                case 7:
                    if (wand.copy_paste_buffer.size() > 0) {

                        BlockPos b_pos = last_pos.relative(last_side, 1);
                        setRender_shape_begin(tesselator,bufferBuilder);
                        random.setSeed(0);
                        for (CopyPasteBuffer b : wand.copy_paste_buffer) {
                            BlockPos p=b.pos.rotate(Rotation.values()[last_rot]);
                            render_shape(tesselator,bufferBuilder,b.state,b_pos.getX() + p.getX(),b_pos.getY() + p.getY(),b_pos.getZ() + p.getZ());
                        }
                        tesselator.end();
                        RenderSystem.setShader(GameRenderer::getPositionColorShader);
                        RenderSystem.disableTexture();
                        RenderSystem.disableBlend();
                        bufferBuilder.begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                        for (CopyPasteBuffer b : wand.copy_paste_buffer) {
                            BlockPos p=b.pos.rotate(Rotation.values()[last_rot]);
                            int x = b_pos.getX() + p.getX();
                            int y = b_pos.getY() + p.getY();
                            int z = b_pos.getZ() + p.getZ();
                            preview_block(bufferBuilder,
                                    c.x + x, c.y + y, c.z + z,
                                    c.x + x + 1, c.y + y + 1, c.z + z + 1,255,255,255,255
                            );
                        }
                        tesselator.end();
                    }
                break;
            }
            RenderSystem.enableBlend();
            RenderSystem.enableTexture();

            //RenderSystem.shadeModel(7424);
        }
    }

    private static void preview_block(BufferBuilder bufferBuilder,double fx1, double fy1, double fz1, double fx2, double fy2, double fz2
    ,int r,int g,int b,int a) {
        fx1 += p_o;
        fy1 += p_o;
        fz1 += p_o;
        fx2 -= p_o;
        fy2 -= p_o;
        fz2 -= p_o;
        bufferBuilder.vertex(fx1, fy1, fz1).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz1).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx1, fy1, fz1).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx1, fy1, fz2).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx1, fy1, fz2).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz2).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz1).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz2).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz1).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz1).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz1).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz2).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz2).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz2).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz1).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz2).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx1, fy1, fz1).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz1).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz1).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz1).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx1, fy1, fz2).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz2).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz2).color(r,g,b,a).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz2).color(r,g,b,a).endVertex();
    }
    private static void set_grid_v(int i,double x, double y,double z){
        if(i<grid_n) {
            grid_vx[i]=x;
            grid_vy[i]=y;
            grid_vz[i]=z;
        }
    }
    private static void add_grid_line(double x1, double y1,double z1,double x2, double y2,double z2){
        set_grid_v(grid_i,x1, y1,z1);
        grid_i++;
        set_grid_v(grid_i,x2, y2,z2);
        grid_i++;
    }
    private static void draw_lines(BufferBuilder bufferBuilder,Vec3 c,int from,int to,int r,int g,int b,int a){
        for(int i=from;i<to && i< grid_n;i++) {
            bufferBuilder.vertex(c.x+grid_vx[i], c.y+grid_vy[i], c.z+grid_vz[i]).color(r, g, b, a).endVertex();
        }
    }
    private static void grid(BufferBuilder bufferBuilder,Direction side,Vec3 c, double x, double y, double z,AABB aabb) {
        double w=1;
        double h=1;
        double w2=w*0.33333333;
        double w3=w*0.66666666;
        double h2=h*0.33333333;
        double h3=h*0.66666666;
        float o = 0.01f;
        //WandsMod.log(aabb.toString(),prnt);
        //WandsMod.log("x: "+x+" y: "+y+" z: "+z,prnt);
        //float h=wand.block_height;
        switch (side) {
            case UP:
            case DOWN: {
                w=aabb.getXsize();
                h=aabb.getZsize();

                x+=aabb.minX;
                z+=aabb.minZ;
                if(side==Direction.UP){
                    y += aabb.maxY+o;
                }else{
                    y += aabb.minY-o;
                }

                w2=w*0.33333333;
                w3=w*0.66666666;
                h2=h*0.33333333;
                h3=h*0.66666666;
                grid_i=0;
                add_grid_line(x     ,y, z      ,x + w ,y, z      );
                add_grid_line(x     ,y, z      ,x     ,y, z + h  );
                add_grid_line(x + w ,y, z      ,x + w ,y, z + h  );
                add_grid_line(x     ,y, z + h  ,x + w ,y, z + h  );
                add_grid_line(x     ,y, z + h2 ,x + w ,y, z + h2 );
                add_grid_line(x     ,y, z + h3 ,x + w ,y, z + h3 );
                add_grid_line(x + w2,y, z      ,x + w2,y, z +  h );
                add_grid_line(x + w3,y, z      ,x + w3,y, z +  h );

                draw_lines(bufferBuilder,c,0,16,255,255,255,255);

                grid_i=0;

                add_grid_line(x + w*0.40f, y, z + h*0.20f, x + w*0.50f, y, z + h*0.05f);
                add_grid_line(x + w*0.60f, y, z + h*0.20f, x + w*0.50f, y, z + h*0.05f);
                add_grid_line(x + w*0.40f, y, z + h*0.80f, x + w*0.50f, y, z + h*0.95f);
                add_grid_line(x + w*0.60f, y, z + h*0.80f, x + w*0.50f, y, z + h*0.95f);
                add_grid_line(x + w*0.20f, y, z + h*0.40f, x + w*0.05f, y, z + h*0.50f);
                add_grid_line(x + w*0.20f, y, z + h*0.60f, x + w*0.05f, y, z + h*0.50f);
                add_grid_line(x + w*0.80f, y, z + h*0.40f, x + w*0.95f, y, z + h*0.50f);
                add_grid_line(x + w*0.80f, y, z + h*0.60f, x + w*0.95f, y, z + h*0.50f);
                draw_lines(bufferBuilder,c,0,16,178,0,0,255);

                grid_i=0;
                add_grid_line(x + w*0.40f, y, z + h*0.50f,x + w*0.50f, y, z + h*0.40f);
                add_grid_line(x + w*0.40f, y, z + h*0.50f,x + w*0.50f, y, z + h*0.60f);
                add_grid_line(x + w*0.60f, y, z + h*0.50f,x + w*0.50f, y, z + h*0.60f);
                add_grid_line(x + w*0.50f, y, z + h*0.40f,x + w*0.60f, y, z + h*0.50f);
                draw_lines(bufferBuilder,c,0,8,0,178,0,255);

                grid_i=0;
                add_grid_line(x + w*0.10f, y, z + h*0.10f,x + w*0.20f, y, z + h*0.14f);
                add_grid_line(x + w*0.10f, y, z + h*0.10f,x + w*0.14f, y, z + h*0.20f);
                add_grid_line(x + w*0.90f, y, z + h*0.90f,x + w*0.80f, y, z + h*0.86f);
                add_grid_line(x + w*0.90f, y, z + h*0.90f,x + w*0.86f, y, z + h*0.80f);
                add_grid_line(x + w*0.90f, y, z + h*0.10f,x + w*0.80f, y, z + h*0.14f);
                add_grid_line(x + w*0.90f, y, z + h*0.10f,x + w*0.86f, y, z + h*0.20f);
                add_grid_line(x + w*0.10f, y, z + h*0.90f,x + w*0.20f, y, z + h*0.86f);
                add_grid_line(x + w*0.10f, y, z + h*0.90f,x + w*0.14f, y, z + h*0.80f);
                draw_lines(bufferBuilder,c,0,16,0,0,178,255);

            }
            break;
            case NORTH:
            case SOUTH: {
                w=aabb.getXsize();
                h=aabb.getYsize();
                //WandsMod.log(aabb.toString(),prnt);
                x+=aabb.minX;
                y+=aabb.minY;
                if(side==Direction.SOUTH){
                    z += aabb.maxZ+o;
                }else{
                    z += aabb.minZ-o;
                }
                w2=w*0.33333333;
                w3=w*0.66666666;
                h2=h*0.33333333;
                h3=h*0.66666666;
                grid_i=0;
                add_grid_line(x     ,y        ,z,x + w , y      , z);
                add_grid_line(x     , y      , z,x     , y + h  , z);
                add_grid_line(x + w , y      , z,x + w , y + h  , z);
                add_grid_line(x     , y + h  , z,x + w , y + h  , z);
                add_grid_line(x     , y + h2 , z,x + w , y + h2 , z);
                add_grid_line(x     , y + h3 , z,x + w , y + h3 , z);
                add_grid_line(x + w2, y      , z,x + w2, y +  h , z);
                add_grid_line(x + w3, y      , z,x + w3, y +  h , z);

                draw_lines(bufferBuilder,c,0,16,255,255,255,255);

                grid_i=0;

                add_grid_line(x + w*0.40f, y + h*0.20f, z, x + w*0.50f, y + h*0.05f,z);
                add_grid_line(x + w*0.60f, y + h*0.20f, z, x + w*0.50f, y + h*0.05f,z);
                add_grid_line(x + w*0.40f, y + h*0.80f, z, x + w*0.50f, y + h*0.95f,z);
                add_grid_line(x + w*0.60f, y + h*0.80f, z, x + w*0.50f, y + h*0.95f,z);
                add_grid_line(x + w*0.20f, y + h*0.40f, z, x + w*0.05f, y + h*0.50f,z);
                add_grid_line(x + w*0.20f, y + h*0.60f, z, x + w*0.05f, y + h*0.50f,z);
                add_grid_line(x + w*0.80f, y + h*0.40f, z, x + w*0.95f, y + h*0.50f,z);
                add_grid_line(x + w*0.80f, y + h*0.60f, z, x + w*0.95f, y + h*0.50f,z);
                draw_lines(bufferBuilder,c,0,16,178,0,0,255);

                grid_i=0;
                add_grid_line(x + w*0.40f, y + h*0.50f,z, x + w*0.50f, y + h*0.40f, z);
                add_grid_line(x + w*0.40f, y + h*0.50f,z, x + w*0.50f, y + h*0.60f, z);
                add_grid_line(x + w*0.60f, y + h*0.50f,z, x + w*0.50f, y + h*0.60f, z);
                add_grid_line(x + w*0.50f, y + h*0.40f,z, x + w*0.60f, y + h*0.50f, z);
                draw_lines(bufferBuilder,c,0,8,0,178,0,255);

                grid_i=0;
                add_grid_line(x + w*0.10f, y + h*0.10f,z, x + w*0.20f, y + h*0.14f , z);
                add_grid_line(x + w*0.10f, y + h*0.10f,z, x + w*0.14f, y + h*0.20f , z);
                add_grid_line(x + w*0.90f, y + h*0.90f,z, x + w*0.80f, y + h*0.86f , z);
                add_grid_line(x + w*0.90f, y + h*0.90f,z, x + w*0.86f, y + h*0.80f , z);
                add_grid_line(x + w*0.90f, y + h*0.10f,z, x + w*0.80f, y + h*0.14f , z);
                add_grid_line(x + w*0.90f, y + h*0.10f,z, x + w*0.86f, y + h*0.20f , z);
                add_grid_line(x + w*0.10f, y + h*0.90f,z, x + w*0.20f, y + h*0.86f , z);
                add_grid_line(x + w*0.10f, y + h*0.90f,z, x + w*0.14f, y + h*0.80f , z);
                draw_lines(bufferBuilder,c,0,16,0,0,178,255);
            }
            break;
            case EAST:
            case WEST: {

                y+=aabb.minY;
                z+=aabb.minZ;
                if(side==Direction.EAST){
                    x += aabb.maxX +o;
                }else{
                    x += aabb.minX-o;
                }
                w=aabb.getYsize();
                h=aabb.getZsize();
                w2=w*0.33333333;
                w3=w*0.66666666;
                h2=h*0.33333333;
                h3=h*0.66666666;
                grid_i=0;
                add_grid_line(x, y     , z      ,x, y + w , z      );
                add_grid_line(x, y     , z      ,x, y     , z + h  );
                add_grid_line(x, y + w , z      ,x, y + w , z + h  );
                add_grid_line(x, y     , z + h  ,x, y + w , z + h  );
                add_grid_line(x, y     , z + h2 ,x, y + w , z + h2 );
                add_grid_line(x, y     , z + h3 ,x, y + w , z + h3 );
                add_grid_line(x, y + w2, z      ,x, y + w2, z +  h );
                add_grid_line(x, y + w3, z      ,x, y + w3, z +  h );

                draw_lines(bufferBuilder,c,0,16,255,255,255,255);

                grid_i=0;

                add_grid_line(x, y + w*0.40f, z + h*0.20f, x, y + w*0.50f, z + h*0.05f);
                add_grid_line(x, y + w*0.60f, z + h*0.20f, x, y + w*0.50f, z + h*0.05f);
                add_grid_line(x, y + w*0.40f, z + h*0.80f, x, y + w*0.50f, z + h*0.95f);
                add_grid_line(x, y + w*0.60f, z + h*0.80f, x, y + w*0.50f, z + h*0.95f);
                add_grid_line(x, y + w*0.20f, z + h*0.40f, x, y + w*0.05f, z + h*0.50f);
                add_grid_line(x, y + w*0.20f, z + h*0.60f, x, y + w*0.05f, z + h*0.50f);
                add_grid_line(x, y + w*0.80f, z + h*0.40f, x, y + w*0.95f, z + h*0.50f);
                add_grid_line(x, y + w*0.80f, z + h*0.60f, x, y + w*0.95f, z + h*0.50f);
                draw_lines(bufferBuilder,c,0,16,178,0,0,255);

                grid_i=0;
                add_grid_line(x,y + w*0.40f, z + h*0.50f,x, y + w*0.50f, z + h*0.40f);
                add_grid_line(x,y + w*0.40f, z + h*0.50f,x, y + w*0.50f, z + h*0.60f);
                add_grid_line(x,y + w*0.60f, z + h*0.50f,x, y + w*0.50f, z + h*0.60f);
                add_grid_line(x,y + w*0.50f, z + h*0.40f,x, y + w*0.60f, z + h*0.50f);
                draw_lines(bufferBuilder,c,0,8,0,178,0,255);

                grid_i=0;
                add_grid_line(x, y + w*0.10f, z + h*0.10f,x, y + w*0.20f, z + h*0.14f);
                add_grid_line(x, y + w*0.10f, z + h*0.10f,x, y + w*0.14f, z + h*0.20f);
                add_grid_line(x, y + w*0.90f, z + h*0.90f,x, y + w*0.80f, z + h*0.86f);
                add_grid_line(x, y + w*0.90f, z + h*0.90f,x, y + w*0.86f, z + h*0.80f);
                add_grid_line(x, y + w*0.90f, z + h*0.10f,x, y + w*0.80f, z + h*0.14f);
                add_grid_line(x, y + w*0.90f, z + h*0.10f,x, y + w*0.86f, z + h*0.20f);
                add_grid_line(x, y + w*0.10f, z + h*0.90f,x, y + w*0.20f, z + h*0.86f);
                add_grid_line(x, y + w*0.10f, z + h*0.90f,x, y + w*0.14f, z + h*0.80f);
                draw_lines(bufferBuilder,c,0,16,0,0,178,255);
                
            }
            break;
        }
        //WandsMod.log("x: "+x+" y: "+y+" z: "+z,prnt);
    }

    static void setRender_shape_begin(Tesselator tesselator,BufferBuilder bufferBuilder){
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        /*RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_COLOR,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
        RenderSystem.blendEquation(32774);*/
        /*RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_DST_ALPHA);*/
        RenderSystem.setShaderColor(1.0f,1.0f,1.0f,opacity);
        bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
    }
    static void render_shape(Tesselator tesselator,BufferBuilder bufferBuilder,BlockState state,double x, double y,double z){
        float vx=0;
        float vy=0;
        float vz=0;
        float u=0;
        float v=0;
        BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel bakedModel = blockRenderDispatcher.getBlockModel(state);
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        if(bakedModel!=null) {
            for(Direction dir: dirs) {
                //WandsMod.log("dir "+dir, prnt);
                List<BakedQuad> bake_list = bakedModel.getQuads(state, dir, random);
                if (!bake_list.isEmpty()) {
                    //WandsMod.log("quads! "+bake_list.size(), prnt);
                    //RenderSystem.set
                    for (BakedQuad quad : bake_list) {
                        RenderSystem.setShaderTexture(0, quad.getSprite().atlas().getId());
                        int[] verts = quad.getVertices();
                        for (int i = 0; i < 4; ++i) {
                            int j = i * 8;
                            vx = Float.intBitsToFloat(verts[j]);
                            vy = Float.intBitsToFloat(verts[j + 1]);
                            vz = Float.intBitsToFloat(verts[j + 2]);
                            u = Float.intBitsToFloat(verts[j + 4]);
                            v = Float.intBitsToFloat(verts[j + 5]);
                            //WandsMod.log("vert " + i + "  " + x1 + " " + y1 + " " + z1 + " uv: " + u + " " + v, prnt);
                            bufferBuilder.vertex(c.x + x + vx, +c.y + y + vy, c.z + z + vz).uv(u, v).endVertex();
                        }
                    }
                }
            }
        }
//        tesselator.end();
    }
    
    
}

