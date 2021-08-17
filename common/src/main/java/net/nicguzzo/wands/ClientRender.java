package net.nicguzzo.wands;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
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

import net.nicguzzo.wands.WandItem.Mode;

public class ClientRender {
    public static final float p_o = -0.005f;// preview_block offset
    private static long t0 = 0;
	private static long t1 = 0;
    private static long t00 = 0;
	private static boolean prnt;
    public static Vec3 c=new Vec3(0,0,0);
    static BlockPos last_pos=null;
    static Direction last_side=null;
    static Mode last_mode;
    static int last_rot=0;
    static boolean last_alt=false;
    //static int last_y=0;
    static int last_buffer_size=-1;
    static WandItem.Orientation last_orientation=null;
    //private static boolean last_valid =false;
    public static Wand wand=new Wand();
    static VoxelShape preview_shape =null;

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
    static boolean fancy=true;
    private static final ResourceLocation GRID_TEXTURE = new ResourceLocation("wands", "textures/blocks/grid.png");
    static Random random=new Random();
    static Direction[] dirs={ Direction.DOWN,Direction.UP,Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST,null};
    public static void render(PoseStack matrixStack, double camX, double camY, double camZ, MultiBufferSource.BufferSource bufferIn) {
        opacity=WandsMod.config.preview_opacity;
        fancy=WandsConfig.get_instance().fancy_preview;
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
            //WandsMod.log("inv slot0: "+player.getInventory().getItem(9),prnt);
            //if(mode==6 && wand.copy_pos1!=null && wand.copy_pos2!=null){
            //    preview_mode(wand.mode);
            //}
            HitResult hitResult=client.hitResult;
            Mode mode = WandItem.getMode(stack);
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && !(mode==Mode.PASTE && wand.is_alt_pressed)) {
                BlockHitResult block_hit = (BlockHitResult) hitResult;

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
                    if(mode==Mode.FILL||mode==Mode.LINE||mode==Mode.CIRCLE||mode==Mode.COPY||mode==Mode.RECT) {
                        if (wand.is_alt_pressed) {
                            pos = pos.relative(side, 1);
                        }
                    }
                    //WandsMod.LOGGER.info("wand.mode"+ mode);
                    last_pos = pos;
                    last_side = side;
                    last_mode = mode;
                    last_orientation = orientation;
                    last_rot=rot;
                    last_alt=wand.is_alt_pressed;
                    last_buffer_size=wand.block_buffer.get_length();

                    wand.do_or_preview(player, player.level, block_state, pos, side, block_hit.getLocation(), stack, prnt);
                }
                preview_shape =null;
                if(block_state!=null) {
                    preview_shape = block_state.getShape(client.level, last_pos);
                }
                preview_mode(wand.mode,matrixStack,bufferIn);
            }else{
                if(wand.mode== WandItem.Mode.PASTE && wand.copy_paste_buffer.size()!=0 && wand.is_alt_pressed) {
                    preview_mode(wand.mode, matrixStack, bufferIn);
                }
            }
        }
    }

    private static void preview_mode(Mode mode, PoseStack matrixStack, MultiBufferSource.BufferSource bufferIn) {

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
        if (camera.isInitialized() && last_pos!=null) {
            c = camera.getPosition().reverse();
            double last_pos_x=c.x+last_pos.getX();
            double last_pos_y=c.y+last_pos.getY();
            double last_pos_z=c.z+last_pos.getZ();
            double wand_x1=c.x+wand.x1;
            double wand_y1=c.y+wand.y1;
            double wand_z1=c.z+wand.z1;
            double wand_x2=c.x+wand.x2;
            double wand_y2=c.y+wand.y2;
            double wand_z2=c.z+wand.z2;
            float off2=0.05f;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            //WandsMod.log("mode "+mode.toString(),prnt);
            switch (mode) {
                case DIRECTION:
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
                                    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                                    int vi = 0;
                                    for (AABB aabb : list) {
                                        if (vi == wand.grid_voxel_index) {
                                            switch (wand.side) {
                                                case UP:
                                                case DOWN:
                                                    x1 = last_pos_x + aabb.minX;
                                                    y1 = (wand.side == Direction.UP ? last_pos_y + aabb.maxY + 0.01 : last_pos_y + aabb.minY - 0.01);
                                                    z1 = last_pos_z + aabb.minZ;
                                                    x2 = last_pos_x + aabb.maxX;
                                                    z2 = last_pos_z + aabb.maxZ;
                                                    bufferBuilder.vertex(x1, y1, z1).uv(0, 0).endVertex();
                                                    bufferBuilder.vertex(x1, y1, z2).uv(0, 1).endVertex();
                                                    bufferBuilder.vertex(x2, y1, z2).uv(1, 1).endVertex();
                                                    bufferBuilder.vertex(x2, y1, z1).uv(1, 0).endVertex();
                                                    break;
                                                case NORTH:
                                                case SOUTH:
                                                    x1 = last_pos_x + aabb.minX;
                                                    y1 = last_pos_y + aabb.minY;
                                                    z1 = (wand.side == Direction.SOUTH ? last_pos_z + aabb.maxZ + 0.01 : last_pos_z + aabb.minZ - 0.01);
                                                    x2 = last_pos_x + aabb.maxX;
                                                    y2 = last_pos_y + aabb.maxY;
                                                    bufferBuilder.vertex(x1, y1, z1).uv(0, 0).endVertex();
                                                    bufferBuilder.vertex(x1, y2, z1).uv(0, 1).endVertex();
                                                    bufferBuilder.vertex(x2, y2, z1).uv(1, 1).endVertex();
                                                    bufferBuilder.vertex(x2, y1, z1).uv(1, 0).endVertex();
                                                    break;
                                                case WEST:
                                                case EAST:
                                                    x1 = (wand.side == Direction.EAST ? last_pos_x + aabb.maxX + 0.01 : last_pos_x + aabb.minX - 0.01);
                                                    y1 = last_pos_y + aabb.minY;
                                                    z1 = last_pos_z + aabb.minZ;
                                                    y2 = last_pos_y + aabb.maxY;
                                                    z2 = last_pos_z + aabb.maxZ;

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
                                    bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                                    int vi = 0;
                                    for (AABB aabb : list) {
                                        if (vi == wand.grid_voxel_index) {
                                            grid(bufferBuilder, wand.side,last_pos_x,last_pos_y,last_pos_z,aabb);
                                        }
                                        vi++;
                                    }
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
                            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                            preview_block(bufferBuilder,
                                    wand_x1, (wand_y1 + wand.y0),wand_z1,
                                    wand_x2, (wand_y1 + wand.y0 + wand.block_height), wand_z2,
                                    255,255,255,255);
                            tesselator.end();
                        }
                    }
                    //break;
                case ROW_COL:
                case FILL:
                case AREA:
                case LINE:
                case CIRCLE:
                case RECT:

                    //preview_mode1(bufferBuilder);
                    if (wand.valid) {
                        if(mode==Mode.ROW_COL || mode==Mode.FILL  || mode==Mode.RECT)//bbox
                        {
                            RenderSystem.setShader(GameRenderer::getPositionColorShader);
                            RenderSystem.disableTexture();
                            RenderSystem.disableBlend();
                            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                            preview_block(bufferBuilder,
                                    c.x+wand.bb1_x-off2,c.y+wand.bb1_y-off2, c.z+wand.bb1_z-off2,
                                    c.x+wand.bb2_x+off2,c.y+wand.bb2_y+off2, c.z+wand.bb2_z+off2,
                                    0,0,255,255);
                            tesselator.end();
                        }

                        if (mode == Mode.LINE || mode==Mode.CIRCLE) {
                            RenderSystem.setShader(GameRenderer::getPositionColorShader);
                            RenderSystem.disableTexture();
                            RenderSystem.disableBlend();
                            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                            preview_block(bufferBuilder,
                                    c.x+wand.p1.getX(),c.y+wand.p1.getY(),c.z+wand.p1.getZ(),
                                    c.x+wand.p1.getX()+1,c.y+wand.p1.getY()+1,c.z+wand.p1.getZ()+1,
                                    0,255,0,255);

                            preview_block(bufferBuilder,
                                    last_pos_x-off2,last_pos_y-off2,last_pos_z-off2,
                                    last_pos_x+1+off2,last_pos_y+1+off2,last_pos_z+1+off2,
                                    0,255,0,255);


                            //bufferBuilder.vertex(last_pos_x+0.5, last_pos_y+0.5, last_pos_z+0.5).color(255,255,255,255).endVertex();
                            //bufferBuilder.vertex(wand_x1+0.5, wand_y1+0.5, wand_z1+0.5).color(255,0,255,255).endVertex();
                            tesselator.end();
                        }


                        if (wand.block_buffer != null ) {
                            random.setSeed(0);
                            if(fancy && !wand.destroy && !wand.has_bucket) {
                                setRender_shape_begin(tesselator, bufferBuilder);
                                for (int a = 0; a < wand.block_buffer.get_length() && a < Wand.MAX_LIMIT; a++) {
                                    if (wand.block_buffer.state[a] != null) {
                                        preview_shape = wand.block_buffer.state[a].getShape(client.level, last_pos);
                                        render_shape(tesselator, bufferBuilder, wand.block_buffer.state[a],
                                                wand.block_buffer.buffer_x[a],
                                                wand.block_buffer.buffer_y[a],
                                                wand.block_buffer.buffer_z[a]);
                                        if(wand.block_buffer.state[a].getBlock() instanceof DoorBlock){

                                            render_shape(tesselator, bufferBuilder, wand.block_buffer.state[a].setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER),
                                                    wand.block_buffer.buffer_x[a],
                                                    wand.block_buffer.buffer_y[a]+1,
                                                    wand.block_buffer.buffer_z[a]);
                                        }
                                    }
                                }
                                tesselator.end();
                            }
                            RenderSystem.enableCull();
                            RenderSystem.setShader(GameRenderer::getPositionColorShader);
                            RenderSystem.disableTexture();
                            RenderSystem.disableBlend();
                            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                            int r=255;
                            int g=255;
                            int b=255;
                            for (int a = 0; a < wand.block_buffer.get_length() && a < Wand.MAX_LIMIT; a++) {
                                double x = c.x+wand.block_buffer.buffer_x[a];
                                double y = c.y+wand.block_buffer.buffer_y[a];
                                double z = c.z+wand.block_buffer.buffer_z[a];
                                if (wand.block_buffer.state[a] != null) {
                                    preview_shape = wand.block_buffer.state[a].getShape(client.level, last_pos);
                                    List<AABB> list = preview_shape.toAabbs();
                                    for (AABB aabb : list) {
                                        if(wand.destroy){
                                            r=255;g=0;b=0;
                                        }
                                        preview_block(bufferBuilder,
                                                x + aabb.minX, y + aabb.minY, z + aabb.minZ,
                                                x + aabb.maxX, y + aabb.maxY, z + aabb.maxZ,
                                                r, g, b, 255);
                                    }
                                }
                            }
                            tesselator.end();
                        }else {
                            RenderSystem.setShader(GameRenderer::getPositionColorShader);
                            RenderSystem.disableTexture();
                            RenderSystem.disableBlend();
                            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                            preview_block(bufferBuilder,
                                    wand_x1, wand_y1, wand_z1,
                                    wand_x2, wand_y2, wand_z2,
                                    255,0,0,255);
                            tesselator.end();
                        }
                        if (mode == Mode.LINE || mode==Mode.CIRCLE) {
                            RenderSystem.setShader(GameRenderer::getPositionColorShader);
                            RenderSystem.disableTexture();
                            RenderSystem.disableBlend();
                            RenderSystem.disableDepthTest();
                            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

                            bufferBuilder.vertex(last_pos_x+0.5, last_pos_y+0.5, last_pos_z+0.5).color(255,0,255,255).endVertex();
                            bufferBuilder.vertex(wand_x1+0.5, wand_y1+0.5, wand_z1+0.5).color(255,0,255,255).endVertex();
                            tesselator.end();
                        }
                    }
                    break;
                case COPY:

                    if (wand.valid) {
                        RenderSystem.setShader(GameRenderer::getPositionColorShader);
                        RenderSystem.disableTexture();
                        RenderSystem.disableBlend();
                        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

                        x1=(wand.copy_x1>wand.copy_x2)? (c.x+wand.copy_x1+off2) : (c.x+wand.copy_x1-off2);
                        y1=(wand.copy_y1>wand.copy_y2)? (c.y+wand.copy_y1+off2) : (c.y+wand.copy_y1-off2);
                        z1=(wand.copy_z1>wand.copy_z2)? (c.z+wand.copy_z1+off2) : (c.z+wand.copy_z1-off2);
                        x2=(wand.copy_x2>wand.copy_x1)? (c.x+wand.copy_x2+off2) : (c.x+wand.copy_x2-off2);
                        y2=(wand.copy_y2>wand.copy_y1)? (c.y+wand.copy_y2+off2) : (c.y+wand.copy_y2-off2);
                        z2=(wand.copy_z2>wand.copy_z1)? (c.z+wand.copy_z2+off2) : (c.z+wand.copy_z2-off2);

                        preview_block(bufferBuilder,
                                x1,y1,z1,
                                x2,y2,z2,
                                0,0,255,255);
                        tesselator.end();
                    }

                break;
                case PASTE:
                    if (wand.copy_paste_buffer.size() > 0) {

                        BlockPos b_pos = last_pos.relative(last_side, 1);
                        if (fancy) {
                            setRender_shape_begin(tesselator, bufferBuilder);
                            random.setSeed(0);
                            for (CopyPasteBuffer b : wand.copy_paste_buffer) {
                                BlockPos p = b.pos.rotate(Rotation.values()[last_rot]);
                                render_shape(tesselator, bufferBuilder, b.state, b_pos.getX() + p.getX(), b_pos.getY() + p.getY(), b_pos.getZ() + p.getZ());
                            }
                            tesselator.end();
                        }
                        RenderSystem.setShader(GameRenderer::getPositionColorShader);
                        RenderSystem.disableTexture();
                        RenderSystem.disableBlend();
                        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                        x1=Integer.MAX_VALUE;y1=Integer.MAX_VALUE;z1=Integer.MAX_VALUE;
                        x2=Integer.MIN_VALUE;y2=Integer.MIN_VALUE;z2=Integer.MIN_VALUE;
                        for (CopyPasteBuffer b : wand.copy_paste_buffer) {
                            BlockPos p = b.pos.rotate(Rotation.values()[last_rot]);
                            double x = c.x + b_pos.getX() + p.getX();
                            double y = c.y + b_pos.getY() + p.getY();
                            double z = c.z + b_pos.getZ() + p.getZ();
                            preview_block(bufferBuilder,
                                    x, y, z,
                                    x + 1, y + 1, z + 1, 255, 255, 255, 255
                            );
                            if(p.getX()<x1) x1=p.getX();
                            if(p.getY()<y1) y1=p.getY();
                            if(p.getZ()<z1) z1=p.getZ();
                            if(p.getX()+1>x2) x2=p.getX()+1;
                            if(p.getY()+1>y2) y2=p.getY()+1;
                            if(p.getZ()+1>z2) z2=p.getZ()+1;
                        }
                        x1 = c.x + b_pos.getX() + x1-off2;
                        y1 = c.y + b_pos.getY() + y1-off2;
                        z1 = c.z + b_pos.getZ() + z1-off2;
                        x2 = c.x + b_pos.getX() + x2+off2;
                        y2 = c.y + b_pos.getY() + y2+off2;
                        z2 = c.z + b_pos.getZ() + z2+off2;

                        preview_block(bufferBuilder,
                                x1, y1, z1,
                                x2, y2, z2,
                                255,255, 0, 255);

                        tesselator.end();
                    }
                break;
            }
            RenderSystem.enableBlend();
            RenderSystem.enableTexture();
            RenderSystem.enableDepthTest();
        }
    }

    private static void preview_block(BufferBuilder bufferBuilder,double fx1, double fy1, double fz1, double fx2, double fy2, double fz2,int r,int g,int b,int a) {
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
    private static void draw_lines(BufferBuilder bufferBuilder,int from,int to,int r,int g,int b,int a){
        for(int i=from;i<to && i< grid_n;i++) {
            bufferBuilder.vertex(grid_vx[i],grid_vy[i], grid_vz[i]).color(r, g, b, a).endVertex();
        }
    }
    private static void grid(BufferBuilder bufferBuilder,Direction side, double x, double y, double z,AABB aabb) {
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

                draw_lines(bufferBuilder,0,16,255,255,255,255);

                grid_i=0;

                add_grid_line(x + w*0.40f, y, z + h*0.20f, x + w*0.50f, y, z + h*0.05f);
                add_grid_line(x + w*0.60f, y, z + h*0.20f, x + w*0.50f, y, z + h*0.05f);
                add_grid_line(x + w*0.40f, y, z + h*0.80f, x + w*0.50f, y, z + h*0.95f);
                add_grid_line(x + w*0.60f, y, z + h*0.80f, x + w*0.50f, y, z + h*0.95f);
                add_grid_line(x + w*0.20f, y, z + h*0.40f, x + w*0.05f, y, z + h*0.50f);
                add_grid_line(x + w*0.20f, y, z + h*0.60f, x + w*0.05f, y, z + h*0.50f);
                add_grid_line(x + w*0.80f, y, z + h*0.40f, x + w*0.95f, y, z + h*0.50f);
                add_grid_line(x + w*0.80f, y, z + h*0.60f, x + w*0.95f, y, z + h*0.50f);
                draw_lines(bufferBuilder,0,16,178,0,0,255);

                grid_i=0;
                add_grid_line(x + w*0.40f, y, z + h*0.50f,x + w*0.50f, y, z + h*0.40f);
                add_grid_line(x + w*0.40f, y, z + h*0.50f,x + w*0.50f, y, z + h*0.60f);
                add_grid_line(x + w*0.60f, y, z + h*0.50f,x + w*0.50f, y, z + h*0.60f);
                add_grid_line(x + w*0.50f, y, z + h*0.40f,x + w*0.60f, y, z + h*0.50f);
                draw_lines(bufferBuilder,0,8,0,178,0,255);

                grid_i=0;
                add_grid_line(x + w*0.10f, y, z + h*0.10f,x + w*0.20f, y, z + h*0.14f);
                add_grid_line(x + w*0.10f, y, z + h*0.10f,x + w*0.14f, y, z + h*0.20f);
                add_grid_line(x + w*0.90f, y, z + h*0.90f,x + w*0.80f, y, z + h*0.86f);
                add_grid_line(x + w*0.90f, y, z + h*0.90f,x + w*0.86f, y, z + h*0.80f);
                add_grid_line(x + w*0.90f, y, z + h*0.10f,x + w*0.80f, y, z + h*0.14f);
                add_grid_line(x + w*0.90f, y, z + h*0.10f,x + w*0.86f, y, z + h*0.20f);
                add_grid_line(x + w*0.10f, y, z + h*0.90f,x + w*0.20f, y, z + h*0.86f);
                add_grid_line(x + w*0.10f, y, z + h*0.90f,x + w*0.14f, y, z + h*0.80f);
                draw_lines(bufferBuilder,0,16,0,0,178,255);

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

                draw_lines(bufferBuilder,0,16,255,255,255,255);

                grid_i=0;

                add_grid_line(x + w*0.40f, y + h*0.20f, z, x + w*0.50f, y + h*0.05f,z);
                add_grid_line(x + w*0.60f, y + h*0.20f, z, x + w*0.50f, y + h*0.05f,z);
                add_grid_line(x + w*0.40f, y + h*0.80f, z, x + w*0.50f, y + h*0.95f,z);
                add_grid_line(x + w*0.60f, y + h*0.80f, z, x + w*0.50f, y + h*0.95f,z);
                add_grid_line(x + w*0.20f, y + h*0.40f, z, x + w*0.05f, y + h*0.50f,z);
                add_grid_line(x + w*0.20f, y + h*0.60f, z, x + w*0.05f, y + h*0.50f,z);
                add_grid_line(x + w*0.80f, y + h*0.40f, z, x + w*0.95f, y + h*0.50f,z);
                add_grid_line(x + w*0.80f, y + h*0.60f, z, x + w*0.95f, y + h*0.50f,z);
                draw_lines(bufferBuilder,0,16,178,0,0,255);

                grid_i=0;
                add_grid_line(x + w*0.40f, y + h*0.50f,z, x + w*0.50f, y + h*0.40f, z);
                add_grid_line(x + w*0.40f, y + h*0.50f,z, x + w*0.50f, y + h*0.60f, z);
                add_grid_line(x + w*0.60f, y + h*0.50f,z, x + w*0.50f, y + h*0.60f, z);
                add_grid_line(x + w*0.50f, y + h*0.40f,z, x + w*0.60f, y + h*0.50f, z);
                draw_lines(bufferBuilder,0,8,0,178,0,255);

                grid_i=0;
                add_grid_line(x + w*0.10f, y + h*0.10f,z, x + w*0.20f, y + h*0.14f , z);
                add_grid_line(x + w*0.10f, y + h*0.10f,z, x + w*0.14f, y + h*0.20f , z);
                add_grid_line(x + w*0.90f, y + h*0.90f,z, x + w*0.80f, y + h*0.86f , z);
                add_grid_line(x + w*0.90f, y + h*0.90f,z, x + w*0.86f, y + h*0.80f , z);
                add_grid_line(x + w*0.90f, y + h*0.10f,z, x + w*0.80f, y + h*0.14f , z);
                add_grid_line(x + w*0.90f, y + h*0.10f,z, x + w*0.86f, y + h*0.20f , z);
                add_grid_line(x + w*0.10f, y + h*0.90f,z, x + w*0.20f, y + h*0.86f , z);
                add_grid_line(x + w*0.10f, y + h*0.90f,z, x + w*0.14f, y + h*0.80f , z);
                draw_lines(bufferBuilder,0,16,0,0,178,255);
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

                draw_lines(bufferBuilder,0,16,255,255,255,255);

                grid_i=0;

                add_grid_line(x, y + w*0.40f, z + h*0.20f, x, y + w*0.50f, z + h*0.05f);
                add_grid_line(x, y + w*0.60f, z + h*0.20f, x, y + w*0.50f, z + h*0.05f);
                add_grid_line(x, y + w*0.40f, z + h*0.80f, x, y + w*0.50f, z + h*0.95f);
                add_grid_line(x, y + w*0.60f, z + h*0.80f, x, y + w*0.50f, z + h*0.95f);
                add_grid_line(x, y + w*0.20f, z + h*0.40f, x, y + w*0.05f, z + h*0.50f);
                add_grid_line(x, y + w*0.20f, z + h*0.60f, x, y + w*0.05f, z + h*0.50f);
                add_grid_line(x, y + w*0.80f, z + h*0.40f, x, y + w*0.95f, z + h*0.50f);
                add_grid_line(x, y + w*0.80f, z + h*0.60f, x, y + w*0.95f, z + h*0.50f);
                draw_lines(bufferBuilder,0,16,178,0,0,255);

                grid_i=0;
                add_grid_line(x,y + w*0.40f, z + h*0.50f,x, y + w*0.50f, z + h*0.40f);
                add_grid_line(x,y + w*0.40f, z + h*0.50f,x, y + w*0.50f, z + h*0.60f);
                add_grid_line(x,y + w*0.60f, z + h*0.50f,x, y + w*0.50f, z + h*0.60f);
                add_grid_line(x,y + w*0.50f, z + h*0.40f,x, y + w*0.60f, z + h*0.50f);
                draw_lines(bufferBuilder,0,8,0,178,0,255);

                grid_i=0;
                add_grid_line(x, y + w*0.10f, z + h*0.10f,x, y + w*0.20f, z + h*0.14f);
                add_grid_line(x, y + w*0.10f, z + h*0.10f,x, y + w*0.14f, z + h*0.20f);
                add_grid_line(x, y + w*0.90f, z + h*0.90f,x, y + w*0.80f, z + h*0.86f);
                add_grid_line(x, y + w*0.90f, z + h*0.90f,x, y + w*0.86f, z + h*0.80f);
                add_grid_line(x, y + w*0.90f, z + h*0.10f,x, y + w*0.80f, z + h*0.14f);
                add_grid_line(x, y + w*0.90f, z + h*0.10f,x, y + w*0.86f, z + h*0.20f);
                add_grid_line(x, y + w*0.10f, z + h*0.90f,x, y + w*0.20f, z + h*0.86f);
                add_grid_line(x, y + w*0.10f, z + h*0.90f,x, y + w*0.14f, z + h*0.80f);
                draw_lines(bufferBuilder,0,16,0,0,178,255);
                
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
        RenderSystem.setShaderColor(1.0f,1.0f,1.0f,opacity);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
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
                List<BakedQuad> bake_list = bakedModel.getQuads(state, dir, random);
                if (!bake_list.isEmpty()) {
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
                            bufferBuilder.vertex(c.x+x + vx,c.y+ y + vy,c.z+ z + vz).uv(u, v).endVertex();
                        }
                    }
                }
            }
        }
    }
}

