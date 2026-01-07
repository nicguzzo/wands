package net.nicguzzo.wands.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nicguzzo.wands.*;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.utils.Colorf;
import net.nicguzzo.wands.wand.CopyBuffer;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.items.*;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;
import net.nicguzzo.wands.wand.WandProps.Mode;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import java.util.List;

public class ClientRender {
    static class V3f{
        public float x;
        public float y;
        public float z;
    }
    //private static  V3f cam=new V3f();
    public static final float p_o = -0.005f;// preview_block offset
    private static long t0 = 0;
    private static long t1 = 0;
    private static long t00 = 0;
    private static boolean prnt;
    //public static Vec3 c=new Vec3(0,0,0);
    public static BlockPos last_pos = null;
    static Direction last_side = null;
    static Mode last_mode;
    static Rotation last_rot = Rotation.NONE;
    static boolean last_alt = false;
    static boolean targeting_air = false;
    //static int last_y=0;
    static int last_buffer_size = -1;
    static WandProps.Orientation last_orientation = null;
    //private static boolean last_valid =false;
    public static Wand wand = new Wand();
    static VoxelShape preview_shape = null;
    static Colorf white=new Colorf(1.0F,1.0F,1.0F,1.0F);
    static AABB def_aabb = new AABB(0, 0, 0, 1, 1, 1);
    static private final int grid_n = 16;
    static private int grid_i = 0;
    static private final float[] grid_vx = new float[grid_n];
    static private final float[] grid_vy = new float[grid_n];
    static private final float[] grid_vz = new float[grid_n];
    static boolean force = false;
    static float x1 = 0;
    static float y1 = 0;
    static float z1 = 0;
    static float x2 = 0;
    static float y2 = 0;
    static float z2 = 0;
    static float opacity = 0.8f;
    static boolean fancy = true;
    static boolean fat_lines = true;
    static boolean drawlines = true;
    static boolean block_outlines = false;
    static boolean fill_outlines = false;
    static boolean copy_outlines = false;
    static boolean paste_outlines = false;
    static float fat_lines_width = 0.05f;
    static Minecraft client;
    private static final ResourceLocation GRID_TEXTURE = Compat.create_resource("textures/blocks/grid.png");
    private static final ResourceLocation LINE_TEXTURE = Compat.create_resource("textures/blocks/line.png");
    static public RandomSource random = RandomSource.create();
    static Direction[] dirs = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null};
    static Vec3 player_normal;
    private static final float[] brightness=new float[]{1.0f, 1.0f, 1.0f, 1.0f};
    private static final int[] lightmap= new int[]{255,255,255,255};

    public enum Colors {
        BLOCK_OUTLINE,
        BOUNDING_BOX,
        DESTROY,
        TOOL_USE,
        START,
        END,
        LINE,
        PASTE_BB,
        BlOCK
    }

    public static boolean update_colors = true;
    static Colorf block_col = new Colorf(1.0f, 1.0f, 1.0f, 1.0f);
    static Colorf bo_col = new Colorf(1.0f, 1.0f, 1.0f, 1.0f);
    static Colorf bbox_col = new Colorf(0.0f, 0.0f, 1.0f, 1.0f);
    static Colorf destroy_col = new Colorf(1.0f, 0.0f, 0.0f, 1.0f);
    static Colorf tool_use_col = new Colorf(0.0f, 1.0f, 1.0f, 1.0f);
    static Colorf start_col = new Colorf(1.0f, 1.0f, 0.0f, 1.0f);
    static Colorf end_col = new Colorf(1.0f, 1.0f, 0.0f, 1.0f);
    static Colorf line_col = new Colorf(1.0f, 0.0f, 1.0f, 1.0f);
    static Colorf paste_bb_col = new Colorf(0.0f, 0.0f, 0.0f, 1.0f);

    public static boolean has_target = false;
    static BlockPos.MutableBlockPos bp = new BlockPos.MutableBlockPos();
    static boolean water = false;
    static int mirroraxis=0;


    public static void render(PoseStack matrixStack,MultiBufferSource.BufferSource bufferSource) {
        if(wand==null) {
            return;
        }
        client = Minecraft.getInstance();
        if(client.level==null)
            return;
        LocalPlayer player = client.player;
        if (player == null)
            return;

        if((wand.destroy||wand.replace) && WandsMod.config.disable_destroy_replace){
            return;
        }

        if (client.options.getCameraType() != CameraType.FIRST_PERSON) {
            return;
        }
        if (update_colors) {
            update_colors = false;
            WandsConfig.get_instance().parse_colors();
            ClientRender.update_colors();
        }

        drawlines = WandsMod.config.lines;
        block_outlines = WandsMod.config.block_outlines;
        fill_outlines = WandsMod.config.fill_outlines;
        copy_outlines = WandsMod.config.copy_outlines;
        paste_outlines = WandsMod.config.paste_outlines;
        opacity = WandsMod.config.preview_opacity;
        fancy = WandsConfig.get_instance().fancy_preview;
        fat_lines = WandsConfig.get_instance().fat_lines;
        if (WandsConfig.get_instance().fat_lines_width > 0 && WandsConfig.get_instance().fat_lines_width < 0.5) {
            fat_lines_width = WandsConfig.get_instance().fat_lines_width;
        }
        ItemStack stack = player.getMainHandItem();
        prnt = false;
        force = false;
        if (!stack.isEmpty() && stack.getItem() instanceof WandItem) {
            t1 = System.currentTimeMillis();
            if (t1 - t0 > 1000) {
                t0 = System.currentTimeMillis();
                prnt = true;
                //WandsMod.LOGGER.info("render");
            }//else{
            if (t1 - t00 > 100) {
                t00 = System.currentTimeMillis();
                force = true;
            }
            HitResult hitResult = client.hitResult;



            wand.target_air=WandProps.getFlag(stack,WandProps.Flag.TARGET_AIR);
            wand.lastHitResult=hitResult;
            wand.lastPlayerDirection=player.getDirection();
            Mode mode = WandProps.getMode(stack);
            mirroraxis=WandProps.getVal(player.getMainHandItem(), WandProps.Value.MIRRORAXIS);
            //WandsMod.LOGGER.info("hit result "+hitResult.getLocation());
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && !wand.is_alt_pressed) {
                has_target = true;
                targeting_air=false;
                Vec3 eye=player.getEyePosition();
                player_normal=eye.subtract(hitResult.getLocation());
                if(wand!=null) {
                    WandMode wmode = wand.get_mode();
                    if (wmode != null) {
                        wmode.redraw(wand);
                    }
                }

                BlockHitResult block_hit = (BlockHitResult) hitResult;
                //wand.lastHitResult=block_hit;
                Rotation rot = WandProps.getRotation(stack);
                WandProps.Orientation orientation = WandProps.getOrientation(stack);
                Direction side = block_hit.getDirection();
                BlockPos pos = block_hit.getBlockPos();
                BlockState block_state = client.level.getBlockState(pos);
                if (force) {
                    wand.force_render = false;
                    if (mode == Mode.FILL || mode == Mode.LINE || mode == Mode.CIRCLE || mode == Mode.SPHERE || mode == Mode.COPY|| mode == Mode.PASTE) {
                        if (WandProps.getFlag(stack, WandProps.Flag.INCSELBLOCK)) {
                            pos = pos.relative(side, 1);
                        }
                    }
                    last_pos = pos;
                    last_side = side;
                    last_mode = mode;
                    last_orientation = orientation;
                    last_rot = rot;
                    last_alt = wand.is_alt_pressed;
                    last_buffer_size = wand.block_buffer.get_length();

                    wand.do_or_preview(player,Compat.player_level(player), block_state, pos, side, block_hit.getLocation(), stack,(WandItem) stack.getItem(), prnt);
                }
                preview_shape = null;
                if (last_pos != null) {
                    preview_shape = block_state.getShape(client.level, last_pos);
                }
                preview_mode(wand.mode, matrixStack,bufferSource);

            } else {
                WandMode wmode = wand.get_mode();
                if (wmode != null) {
                    wmode.redraw(wand);
                }
                has_target = false;
                if (wand.is_alt_pressed && (!wand.copy_paste_buffer.isEmpty() || wand.block_buffer.get_length()>0) ) {
                    if (!((wand.mode == Mode.LINE || wand.mode == Mode.CIRCLE || mode == Mode.SPHERE ))) {
                        wand.setP1(last_pos);
                    }
                    preview_mode(wand.mode, matrixStack,bufferSource);
                }else{
                    if(wand.target_air && mode.can_target_air() ) {
                        targeting_air=true;
                        if(hitResult==null){
                            //WandsMod.LOGGER.info("hit result null");
                            return;
                        }
                        Vec3 hit=hitResult.getLocation();

                        BlockPos pos=wand.get_pos_from_air(hit);
                        ItemStack offhand = player.getOffhandItem();
                        Block offhand_block;
                        BlockState block_state = null;
                        offhand_block = Block.byItem(offhand.getItem());
                        if (offhand_block != Blocks.AIR) {
                            block_state=offhand_block.defaultBlockState();
                        }
                        boolean palette=wand.palette.has_palette && !wand.palette.palette_slots.isEmpty();
                        if (block_state != null|| (palette) || mode==Mode.PASTE){
                            if(palette){
                                block_state=Blocks.STONE.defaultBlockState();
                            }
                            if(mode==Mode.TUNNEL||mode==Mode.ROW_COL||mode==Mode.ROCK||mode==Mode.GRID||mode==Mode.PASTE){
                                wand.setP1(pos);
                            }
                            if (wand.getP1() != null) {
                                last_pos = wand.getP1();
                                has_target=true;
                            }else{
                                last_pos=pos;
                            }
                            /*if(prnt && wand.getP1() !=null) {
                                WandsMod.LOGGER.info("preview p1: "+ wand.getP1() +" p2: "+ wand.getP2() +" pos:"+pos);
                            }*/
                            //Direction side=wand.side;
                            Direction side=player.getDirection().getOpposite();
                            wand.do_or_preview(player, Compat.player_level(player), block_state, pos, side,
                                    hit, stack, (WandItem) stack.getItem(), prnt);
                            preview_mode(wand.mode, matrixStack,bufferSource);
                        }
                    }else{
                        if(mode!=Mode.ROCK) {
                            wand.block_buffer.reset();
                        }
                    }
                }
                if (water) {
                    water = false;
                }
            }
        }
    }

    private static void preview_mode(Mode mode, PoseStack matrixStack,MultiBufferSource.BufferSource bufferSource) {

        Camera camera = client.gameRenderer.getMainCamera();

        Vec3 _c = camera.getPosition();
        matrixStack.pushPose();
        matrixStack.translate(-(float)_c.x,-(float)_c.y,-(float)_c.z);

        //RenderSystem.applyModelViewMatrix();

        float p1_x=0,p1_y=0,p1_z=0;
        BlockPos p1=wand.getP1();
        if(p1!=null) {
            p1_x = p1.getX();
            p1_y = p1.getY();
            p1_z = p1.getZ();
        }
        //RenderSystem.depthMask(true);
        //boolean fabulous_depth_buffer = WandsMod.config.render_last && Minecraft.useShaderTransparency();
        //if (Screen.hasControlDown() || fabulous_depth_buffer) {
            //RenderSystem.disableDepthTest();
        //} else {
            //RenderSystem.enableDepthTest();
        //}

        if (camera.isInitialized() && last_pos != null) {
            float last_pos_x = last_pos.getX();
            float last_pos_y = last_pos.getY();
            float last_pos_z = last_pos.getZ();

            float off2 = 0.05f;
            float off3 = off2/2;

            //RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.8f);
            switch (mode) {
                case DIRECTION:
                    preview_direction_mode(bufferSource,matrixStack.last().pose(),last_pos_x,last_pos_y,last_pos_z);
                case ROW_COL:
                case FILL:
                case AREA:
                case GRID:
                case LINE:
                case CIRCLE:
                case SPHERE:
                case VEIN:
                case TUNNEL:
                case ROCK:
                case COPY:
                case PASTE:
                    preview_selected(mode,bufferSource,matrixStack,last_pos_x,last_pos_y,last_pos_z,off3);
                    if (wand.valid || ( (mode == Mode.ROCK || mode == Mode.FILL|| mode == Mode.COPY || mode == Mode.TUNNEL)&& wand.getP1() !=null)){
                        //bbox
                        if (drawlines && fill_outlines && (mode == Mode.ROW_COL || mode == Mode.FILL || mode == Mode.COPY|| mode == Mode.TUNNEL)) {
                            preview_bbox(bufferSource,matrixStack);
                        }
                        //actual block preview
                        preview_block_buffer(bufferSource,matrixStack);
                        if (drawlines && p1 != null  && (mode == Mode.FILL|| mode == Mode.LINE || mode == Mode.CIRCLE ||mode == Mode.SPHERE )) {
                           preview_line_circle(matrixStack.last().pose(),mode,bufferSource,p1_x,p1_y,p1_z,last_pos_x,last_pos_y,last_pos_z,off3,off2);
                        }
                    }
                break;
            }
            if (mode==Mode.PASTE && !wand.copy_paste_buffer.isEmpty()) {
                preview_paste(bufferSource,matrixStack);
            }
        }
        //RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
        matrixStack.popPose();
    }
    public static void render_mode_outline(Matrix4f matrix, MultiBufferSource.BufferSource bufferSource){
        Colorf mode_outline_color = bo_col;
        if(wand.destroy ||wand.has_empty_bucket)
        {
            mode_outline_color = destroy_col;
        }
        if(wand.use &&(wand.has_hoe||wand.has_axe ||wand.has_shovel))
        {
            mode_outline_color = tool_use_col;
        }
        if(drawlines &&block_outlines)
        {
            VertexConsumer consumer;
            if (fat_lines) {
                consumer= bufferSource.getBuffer(RenderType.debugQuads());
            } else {
                consumer= bufferSource.getBuffer(RenderType.lines());
            }

            for (int idx = 0; idx < wand.block_buffer.get_length() && idx < WandsConfig.max_limit; idx++) {
                float x = wand.block_buffer.buffer_x[idx];
                float y = wand.block_buffer.buffer_y[idx];
                float z = wand.block_buffer.buffer_z[idx];

                if (wand.block_buffer.state[idx] != null) {
                    preview_shape = wand.block_buffer.state[idx].getShape(client.level, last_pos);
                    List<AABB> list = preview_shape.toAabbs();
                    for (AABB aabb : list) {
                        if (fat_lines) {
                            preview_block_fat(matrix,consumer,
                                    x + (float) aabb.minX, y + (float) aabb.minY, z + (float) aabb.minZ,
                                    x + (float) aabb.maxX, y + (float) aabb.maxY, z + (float) aabb.maxZ,
                                    mode_outline_color,wand.destroy);
                        } else {
                            preview_block(matrix,consumer,
                                    x + (float)aabb.minX, y + (float)aabb.minY, z + (float)aabb.minZ,
                                    x + (float)aabb.maxX, y + (float)aabb.maxY, z + (float)aabb.maxZ,
                                    mode_outline_color);
                        }
                    }
                }
            }
            bufferSource.endLastBatch();
        }
    }
    static void preview_block(Matrix4f matrix,VertexConsumer consumer,float fx1, float fy1, float fz1, float fx2, float fy2, float fz2,Colorf c) {
        fx1 += p_o;
        fy1 += p_o;
        fz1 += p_o;
        fx2 -= p_o;
        fy2 -= p_o;
        fz2 -= p_o;
        consumer.vertex(matrix,fx1, fy1, fz1).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx2, fy1, fz1).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx1, fy1, fz1).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx1, fy1, fz2).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx1, fy1, fz2).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx2, fy1, fz2).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx2, fy1, fz1).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx2, fy1, fz2).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx1, fy2, fz1).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx2, fy2, fz1).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx1, fy2, fz1).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx1, fy2, fz2).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx1, fy2, fz2).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx2, fy2, fz2).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx2, fy2, fz1).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx2, fy2, fz2).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx1, fy1, fz1).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx1, fy2, fz1).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx2, fy1, fz1).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx2, fy2, fz1).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx1, fy1, fz2).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx1, fy2, fz2).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx2, fy1, fz2).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
        consumer.vertex(matrix,fx2, fy2, fz2).color(c.r,c.g,c.b,c.a).normal(0.0f,0.0f,0.0f).endVertex();
    }

    static void preview_block_fat(Matrix4f matrix,VertexConsumer consumer,float fx1, float fy1, float fz1, float fx2, float fy2, float fz2,Colorf c,boolean cross) {
        float off=0.01f;
        fx1 -= off;
        fy1 -= off;
        fz1 -= off;
        fx2 += off;
        fy2 += off;
        fz2 += off;
        //RenderSystem.setShaderColor(c.r,c.g,c.b,c.a);
        //Compat.set_texture(LINE_TEXTURE);
        float w=fat_lines_width;
        //north -z
        quad_line(matrix,consumer,  0, w,0, fx1,   fy1, fz1, fx2,   fy1, fz1,c);
        quad_line(matrix,consumer,  0,-w,0, fx2,   fy2, fz1, fx1,   fy2, fz1,c);
        quad_line(matrix,consumer,  w, 0,0, fx1, fy2-w, fz1, fx1, fy1+w, fz1,c);
        quad_line(matrix,consumer, -w, 0,0, fx2, fy1+w, fz1, fx2, fy2-w, fz1,c);
        if(cross) {
            quad_line(matrix,consumer, -w, 0, 0,fx1+w, fy1, fz1,   fx2, fy2, fz1, c);
            quad_line(matrix,consumer,  w, 0, 0,  fx1, fy2, fz1, fx2-w, fy1, fz1, c);
        }
        //south +z
        quad_line(matrix,consumer,  0, w,0, fx2,   fy1, fz2, fx1,   fy1, fz2,c);
        quad_line(matrix,consumer,  0,-w,0, fx1,   fy2, fz2, fx2,   fy2, fz2,c);
        quad_line(matrix,consumer,  w, 0,0, fx1, fy1+w, fz2, fx1, fy2-w, fz2,c);
        quad_line(matrix,consumer, -w, 0,0, fx2, fy2-w, fz2, fx2, fy1+w, fz2,c);
        if(cross) {
            quad_line(matrix,consumer,  w, 0, 0,   fx1, fy1, fz2, fx2-w, fy2, fz2, c);
            quad_line(matrix,consumer, -w, 0, 0, fx1+w, fy2, fz2,   fx2, fy1, fz2, c);
        }
        //up +y
        quad_line(matrix,consumer,  w,0, 0, fx1  , fy2, fz2, fx1 , fy2, fz1,c);
        quad_line(matrix,consumer, -w,0, 0, fx2  , fy2, fz1, fx2 , fy2, fz2,c);
        quad_line(matrix,consumer,  0,0, w, fx1+w, fy2, fz1, fx2-w, fy2, fz1,c);
        quad_line(matrix,consumer,  0,0,-w, fx2-w, fy2, fz2, fx1+w, fy2, fz2,c);
        if(cross) {
            quad_line(matrix,consumer, -w, 0, 0,fx1+w, fy2, fz1,fx2, fy2, fz2, c);
            quad_line(matrix,consumer,  w, 0, 0,fx1, fy2, fz2,fx2-w, fy2, fz1, c);
        }
        //down -y
        quad_line(matrix,consumer,  w,0, 0, fx1, fy1, fz1, fx1  , fy1, fz2,c);
        quad_line(matrix,consumer, -w,0, 0, fx2  , fy1, fz2,fx2, fy1, fz1,c);
        quad_line(matrix,consumer,  0,0, w, fx2-w, fy1, fz1,fx1+w, fy1, fz1,c);
        quad_line(matrix,consumer,  0,0,-w, fx1+w, fy1, fz2,  fx2-w, fy1, fz2,c);
        if(cross) {
            quad_line(matrix,consumer,  w, 0, 0,fx1, fy1, fz1,fx2-w, fy1, fz2, c);
            quad_line(matrix,consumer, -w, 0, 0,fx1+w, fy1, fz2,fx2, fy1, fz1, c);
        }
        //east +x
        quad_line(matrix,consumer, 0, w, 0, fx2,   fy1, fz1, fx2,   fy1, fz2,c);
        quad_line(matrix,consumer, 0,-w, 0, fx2,   fy2, fz2, fx2,   fy2, fz1,c);
        quad_line(matrix,consumer, 0, 0, w, fx2, fy2-w, fz1, fx2, fy1+w, fz1,c);
        quad_line(matrix,consumer, 0, 0,-w, fx2, fy1+w, fz2, fx2, fy2-w, fz2,c);
        if(cross) {
            quad_line(matrix,consumer, 0, 0, w,fx1, fy1, fz1,fx1, fy2, fz2-w, c);
            quad_line(matrix,consumer, 0, 0, w,fx1, fy1, fz2-w,fx1, fy2, fz1, c);
        }
        //west -x
        quad_line(matrix,consumer, 0, w,0,   fx1,   fy1, fz2,fx1,   fy1, fz1,c);
        quad_line(matrix,consumer, 0,-w,0, fx1,   fy2, fz1,  fx1,   fy2, fz2,c);
        quad_line(matrix,consumer, 0,0, w, fx1, fy1+w, fz1,  fx1, fy2-w, fz1,c);
        quad_line(matrix,consumer, 0,0,-w,   fx1, fy2-w, fz2,fx1, fy1+w, fz2,c);
        if(cross) {
            quad_line(matrix,consumer, 0, 0, -w,fx2, fy1, fz1+w,fx2, fy2, fz2, c);
            quad_line(matrix,consumer, 0, 0, -w,fx2, fy1, fz2,fx2, fy2, fz1+w, c);
        }
    }

    private static void quad_line(Matrix4f matrix, VertexConsumer consumer,
                                  float wx,float wy,float wz,
                                  float lx1, float ly1,float lz1,
                                  float lx2, float ly2,float lz2,
                                  Colorf c){

        consumer.vertex(matrix,   lx1,    ly1,    lz1).color(c.r,c.g,c.b,c.a).endVertex();
        consumer.vertex(matrix,lx1+wx, ly1+wy, lz1+wz).color(c.r,c.g,c.b,c.a).endVertex();
        consumer.vertex(matrix,lx2+wx, ly2+wy, lz2+wz).color(c.r,c.g,c.b,c.a).endVertex();
        consumer.vertex(matrix,   lx2,    ly2,    lz2).color(c.r,c.g,c.b,c.a).endVertex();
    }

    private static void player_facing_line(VertexConsumer consumer,float lx1, float ly1,float lz1,float lx2, float ly2,float lz2,Colorf c){

        float w=0.05f;

        float p1x=-lx1;
        float p1y=-ly1;
        float p1z=-lz1;

        float p2x=lx2-lx1;
        float p2y=ly2-ly1;
        float p2z=lz2-lz1;

        //cross product
        float nx = p2y * p1z - p2z * p1y;
        float ny = p2z * p1x - p2x * p1z;
        float nz = p2x * p1y - p2y * p1x;
        float l=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);
        if(l!=0){
            nx=(nx/l)*w;
            ny=(ny/l)*w;
            nz=(nz/l)*w;
        }
        RenderSystem.setShaderColor(c.r,c.g,c.b,c.a);

        consumer.vertex(lx1-nx, ly1-ny, lz1-nz).color(c.r,c.g,c.b,c.a).endVertex();
        consumer.vertex(lx1+nx, ly1+ny, lz1+nz).color(c.r,c.g,c.b,c.a).endVertex();
        consumer.vertex(lx2+nx, ly2+ny, lz2+nz).color(c.r,c.g,c.b,c.a).endVertex();
        consumer.vertex(lx2-nx, ly2-ny, lz2-nz).color(c.r,c.g,c.b,c.a).endVertex();

    }
    private static void set_grid_v(int i,float x, float y,float z){
        if(i<grid_n) {
            grid_vx[i]=x;
            grid_vy[i]=y;
            grid_vz[i]=z;
        }
    }
    private static void add_grid_line(float x1, float y1,float z1,float x2, float y2,float z2){
        set_grid_v(grid_i,x1, y1,z1);
        grid_i++;
        set_grid_v(grid_i,x2, y2,z2);
        grid_i++;
    }
    private static void draw_lines(VertexConsumer consumer,Matrix4f matrix, int from,int to,float r,float g,float b,float a){
        for(int i=from;i<to && i< grid_n;i++) {
            consumer.vertex(matrix,grid_vx[i],grid_vy[i],grid_vz[i]).color(r, g, b, a)
                    .normal(//TODO: needs normal matrix?
                            (float)player_normal.x,
                            (float)player_normal.y,
                            (float)player_normal.z).endVertex();
        }
    }
    private static void grid(VertexConsumer consumer,Matrix4f matrix, Direction side, float x, float y, float z,AABB aabb) {
        float w=1;
        float h=1;
        float w2=w*0.33333333f;
        float w3=w*0.66666666f;
        float h2=h*0.33333333f;
        float h3=h*0.66666666f;
        float o = 0.02f;
        switch (side) {
            case UP:
            case DOWN: {
                w=(float)aabb.getXsize();
                h=(float)aabb.getZsize();

                x+=(float)aabb.minX;
                z+=(float)aabb.minZ;
                if(side==Direction.UP){
                    y += (float)aabb.maxY+o;
                }else{
                    y += (float)aabb.minY-o;
                }

                w2=w*0.33333333f;
                w3=w*0.66666666f;
                h2=h*0.33333333f;
                h3=h*0.66666666f;
                grid_i=0;
                add_grid_line(x     ,y, z      ,x + w ,y, z      );
                add_grid_line(x     ,y, z      ,x     ,y, z + h  );
                add_grid_line(x + w ,y, z      ,x + w ,y, z + h  );
                add_grid_line(x     ,y, z + h  ,x + w ,y, z + h  );
                add_grid_line(x     ,y, z + h2 ,x + w ,y, z + h2 );
                add_grid_line(x     ,y, z + h3 ,x + w ,y, z + h3 );
                add_grid_line(x + w2,y, z      ,x + w2,y, z +  h );
                add_grid_line(x + w3,y, z      ,x + w3,y, z +  h );

                draw_lines(consumer, matrix, 0,16,1.0f,1.0f,1.0f,1.0f);

                grid_i=0;

                add_grid_line(x + w*0.40f, y, z + h*0.20f, x + w*0.50f, y, z + h*0.05f);
                add_grid_line(x + w*0.60f, y, z + h*0.20f, x + w*0.50f, y, z + h*0.05f);
                add_grid_line(x + w*0.40f, y, z + h*0.80f, x + w*0.50f, y, z + h*0.95f);
                add_grid_line(x + w*0.60f, y, z + h*0.80f, x + w*0.50f, y, z + h*0.95f);
                add_grid_line(x + w*0.20f, y, z + h*0.40f, x + w*0.05f, y, z + h*0.50f);
                add_grid_line(x + w*0.20f, y, z + h*0.60f, x + w*0.05f, y, z + h*0.50f);
                add_grid_line(x + w*0.80f, y, z + h*0.40f, x + w*0.95f, y, z + h*0.50f);
                add_grid_line(x + w*0.80f, y, z + h*0.60f, x + w*0.95f, y, z + h*0.50f);
                draw_lines(consumer, matrix,0,16,0.7f,0,0,1.0f);

                grid_i=0;
                add_grid_line(x + w*0.40f, y, z + h*0.50f,x + w*0.50f, y, z + h*0.40f);
                add_grid_line(x + w*0.40f, y, z + h*0.50f,x + w*0.50f, y, z + h*0.60f);
                add_grid_line(x + w*0.60f, y, z + h*0.50f,x + w*0.50f, y, z + h*0.60f);
                add_grid_line(x + w*0.50f, y, z + h*0.40f,x + w*0.60f, y, z + h*0.50f);
                draw_lines(consumer, matrix,0,8,0,0.7f,0,1.0f);

                grid_i=0;
                add_grid_line(x + w*0.10f, y, z + h*0.10f,x + w*0.20f, y, z + h*0.14f);
                add_grid_line(x + w*0.10f, y, z + h*0.10f,x + w*0.14f, y, z + h*0.20f);
                add_grid_line(x + w*0.90f, y, z + h*0.90f,x + w*0.80f, y, z + h*0.86f);
                add_grid_line(x + w*0.90f, y, z + h*0.90f,x + w*0.86f, y, z + h*0.80f);
                add_grid_line(x + w*0.90f, y, z + h*0.10f,x + w*0.80f, y, z + h*0.14f);
                add_grid_line(x + w*0.90f, y, z + h*0.10f,x + w*0.86f, y, z + h*0.20f);
                add_grid_line(x + w*0.10f, y, z + h*0.90f,x + w*0.20f, y, z + h*0.86f);
                add_grid_line(x + w*0.10f, y, z + h*0.90f,x + w*0.14f, y, z + h*0.80f);
                draw_lines(consumer, matrix,0,16,0,0,0.7f,1.0f);

            }
            break;
            case NORTH:
            case SOUTH: {
                w=(float)aabb.getXsize();
                h=(float)aabb.getYsize();
                //WandsMod.log(aabb.toString(),prnt);
                x+= (float) aabb.minX;
                y+= (float) aabb.minY;
                if(side==Direction.SOUTH){
                    z += (float)aabb.maxZ+o;
                }else{
                    z += (float) aabb.minZ-o;
                }
                w2=w*0.33333333f;
                w3=w*0.66666666f;
                h2=h*0.33333333f;
                h3=h*0.66666666f;
                grid_i=0;
                add_grid_line(x     ,y        ,z,x + w , y      , z);
                add_grid_line(x     , y      , z,x     , y + h  , z);
                add_grid_line(x + w , y      , z,x + w , y + h  , z);
                add_grid_line(x     , y + h  , z,x + w , y + h  , z);
                add_grid_line(x     , y + h2 , z,x + w , y + h2 , z);
                add_grid_line(x     , y + h3 , z,x + w , y + h3 , z);
                add_grid_line(x + w2, y      , z,x + w2, y +  h , z);
                add_grid_line(x + w3, y      , z,x + w3, y +  h , z);

                draw_lines(consumer, matrix,0,16,1.0f,1.0f,1.0f,1.0f);

                grid_i=0;

                add_grid_line(x + w*0.40f, y + h*0.20f, z, x + w*0.50f, y + h*0.05f,z);
                add_grid_line(x + w*0.60f, y + h*0.20f, z, x + w*0.50f, y + h*0.05f,z);
                add_grid_line(x + w*0.40f, y + h*0.80f, z, x + w*0.50f, y + h*0.95f,z);
                add_grid_line(x + w*0.60f, y + h*0.80f, z, x + w*0.50f, y + h*0.95f,z);
                add_grid_line(x + w*0.20f, y + h*0.40f, z, x + w*0.05f, y + h*0.50f,z);
                add_grid_line(x + w*0.20f, y + h*0.60f, z, x + w*0.05f, y + h*0.50f,z);
                add_grid_line(x + w*0.80f, y + h*0.40f, z, x + w*0.95f, y + h*0.50f,z);
                add_grid_line(x + w*0.80f, y + h*0.60f, z, x + w*0.95f, y + h*0.50f,z);
                draw_lines(consumer, matrix,0,16,0.7f,0,0,1.0f);

                grid_i=0;
                add_grid_line(x + w*0.40f, y + h*0.50f,z, x + w*0.50f, y + h*0.40f, z);
                add_grid_line(x + w*0.40f, y + h*0.50f,z, x + w*0.50f, y + h*0.60f, z);
                add_grid_line(x + w*0.60f, y + h*0.50f,z, x + w*0.50f, y + h*0.60f, z);
                add_grid_line(x + w*0.50f, y + h*0.40f,z, x + w*0.60f, y + h*0.50f, z);
                draw_lines(consumer, matrix,0,8,0,0.7f,0,1.0f);

                grid_i=0;
                add_grid_line(x + w*0.10f, y + h*0.10f,z, x + w*0.20f, y + h*0.14f , z);
                add_grid_line(x + w*0.10f, y + h*0.10f,z, x + w*0.14f, y + h*0.20f , z);
                add_grid_line(x + w*0.90f, y + h*0.90f,z, x + w*0.80f, y + h*0.86f , z);
                add_grid_line(x + w*0.90f, y + h*0.90f,z, x + w*0.86f, y + h*0.80f , z);
                add_grid_line(x + w*0.90f, y + h*0.10f,z, x + w*0.80f, y + h*0.14f , z);
                add_grid_line(x + w*0.90f, y + h*0.10f,z, x + w*0.86f, y + h*0.20f , z);
                add_grid_line(x + w*0.10f, y + h*0.90f,z, x + w*0.20f, y + h*0.86f , z);
                add_grid_line(x + w*0.10f, y + h*0.90f,z, x + w*0.14f, y + h*0.80f , z);
                draw_lines(consumer, matrix,0,16,0,0,0.7f,1.0f);
            }
            break;
            case EAST:
            case WEST: {

                y+=(float)aabb.minY;
                z+=(float)aabb.minZ;
                if(side==Direction.EAST){
                    x += (float)aabb.maxX +o;
                }else{
                    x += (float)aabb.minX-o;
                }
                w=(float)aabb.getYsize();
                h=(float)aabb.getZsize();
                w2=w*0.33333333f;
                w3=w*0.66666666f;
                h2=h*0.33333333f;
                h3=h*0.66666666f;
                grid_i=0;
                add_grid_line(x, y     , z      ,x, y + w , z      );
                add_grid_line(x, y     , z      ,x, y     , z + h  );
                add_grid_line(x, y + w , z      ,x, y + w , z + h  );
                add_grid_line(x, y     , z + h  ,x, y + w , z + h  );
                add_grid_line(x, y     , z + h2 ,x, y + w , z + h2 );
                add_grid_line(x, y     , z + h3 ,x, y + w , z + h3 );
                add_grid_line(x, y + w2, z      ,x, y + w2, z +  h );
                add_grid_line(x, y + w3, z      ,x, y + w3, z +  h );

                draw_lines(consumer, matrix,0,16,1.0f,1.0f,1.0f,1.0f);

                grid_i=0;

                add_grid_line(x, y + w*0.40f, z + h*0.20f, x, y + w*0.50f, z + h*0.05f);
                add_grid_line(x, y + w*0.60f, z + h*0.20f, x, y + w*0.50f, z + h*0.05f);
                add_grid_line(x, y + w*0.40f, z + h*0.80f, x, y + w*0.50f, z + h*0.95f);
                add_grid_line(x, y + w*0.60f, z + h*0.80f, x, y + w*0.50f, z + h*0.95f);
                add_grid_line(x, y + w*0.20f, z + h*0.40f, x, y + w*0.05f, z + h*0.50f);
                add_grid_line(x, y + w*0.20f, z + h*0.60f, x, y + w*0.05f, z + h*0.50f);
                add_grid_line(x, y + w*0.80f, z + h*0.40f, x, y + w*0.95f, z + h*0.50f);
                add_grid_line(x, y + w*0.80f, z + h*0.60f, x, y + w*0.95f, z + h*0.50f);
                draw_lines(consumer, matrix,0,16,0.7f,0,0,1.0f);

                grid_i=0;
                add_grid_line(x,y + w*0.40f, z + h*0.50f,x, y + w*0.50f, z + h*0.40f);
                add_grid_line(x,y + w*0.40f, z + h*0.50f,x, y + w*0.50f, z + h*0.60f);
                add_grid_line(x,y + w*0.60f, z + h*0.50f,x, y + w*0.50f, z + h*0.60f);
                add_grid_line(x,y + w*0.50f, z + h*0.40f,x, y + w*0.60f, z + h*0.50f);
                draw_lines(consumer, matrix,0,8,0,0.7f,0,1.0f);

                grid_i=0;
                add_grid_line(x, y + w*0.10f, z + h*0.10f,x, y + w*0.20f, z + h*0.14f);
                add_grid_line(x, y + w*0.10f, z + h*0.10f,x, y + w*0.14f, z + h*0.20f);
                add_grid_line(x, y + w*0.90f, z + h*0.90f,x, y + w*0.80f, z + h*0.86f);
                add_grid_line(x, y + w*0.90f, z + h*0.90f,x, y + w*0.86f, z + h*0.80f);
                add_grid_line(x, y + w*0.90f, z + h*0.10f,x, y + w*0.80f, z + h*0.14f);
                add_grid_line(x, y + w*0.90f, z + h*0.10f,x, y + w*0.86f, z + h*0.20f);
                add_grid_line(x, y + w*0.10f, z + h*0.90f,x, y + w*0.20f, z + h*0.86f);
                add_grid_line(x, y + w*0.10f, z + h*0.90f,x, y + w*0.14f, z + h*0.80f);
                draw_lines(consumer, matrix,0,16,0,0,0.7f,1.0f);

            }
            break;
        }
        //WandsMod.log("x: "+x+" y: "+y+" z: "+z,prnt);

    }
    static void render_fluid(VertexConsumer consumer,Matrix4f matrix, float x, float y,float z,int color,float u1,float v1,float u0,float v0) {

            float h = 0.875f;
            float o = 0.1f;
            bp.set(x,y,z);
            int bf = LevelRenderer.getLightColor(wand.level, bp);
            //up
            consumer.vertex(matrix,x + o    , y + h - o, z + o    ).uv(u1, v1).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + o    , y + h - o, z + 1 - o).uv(u1, v0).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + 1 - o, y + h - o, z + 1 - o).uv(u0, v0).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + 1 - o, y + h - o, z + o    ).uv(u0, v1).color(color).normal(0,1,0).uv2(bf).endVertex();
            //down
            consumer.vertex(matrix,x + o    , y + o, z + o    ).uv(u1, v1).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + 1 - o, y + o, z + o    ).uv(u0, v1).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + 1 - o, y + o, z + 1 - o).uv(u0, v0).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + o    , y + o, z + 1 - o).uv(u1, v0).color(color).normal(0,1,0).uv2(bf).endVertex();
            //north -z
            consumer.vertex(matrix,x + o, y + o, z + o        ).uv(u1, v1).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + o, y + h - o, z + o    ).uv(u1, v0).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + 1 - o, y + h - o, z + o).uv(u0, v0).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + 1 - o, y + o, z + o    ).uv(u0, v1).color(color).normal(0,1,0).uv2(bf).endVertex();
            //south +z
            consumer.vertex(matrix,x + o, y + o, z + 1 - o        ).uv(u1, v1).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + 1 - o, y + o, z + 1 - o    ).uv(u0, v1).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + 1 - o, y + h - o, z + 1 - o).uv(u0, v0).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + o, y + h - o, z + 1 - o    ).uv(u1, v0).color(color).normal(0,1,0).uv2(bf).endVertex();
            //east
            consumer.vertex(matrix,x + o, y + o, z + o        ).uv(u0, v1).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + o, y + o, z + 1 - o    ).uv(u1, v1).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + o, y + h - o, z + 1 - o).uv(u1, v0).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + o, y + h - o, z + o    ).uv(u0, v0).color(color).normal(0,1,0).uv2(bf).endVertex();
            //weast
            consumer.vertex(matrix,x + 1 - o, y + o, z + o        ).uv(u0, v1).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + 1 - o, y + h - o, z + o    ).uv(u0, v0).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + 1 - o, y + h - o, z + 1 - o).uv(u1, v0).color(color).normal(0,1,0).uv2(bf).endVertex();
            consumer.vertex(matrix,x + 1 - o, y + o, z + 1 - o    ).uv(u1, v1).color(color).normal(0,1,0).uv2(bf).endVertex();

        //}
    }
    static void render_shape(PoseStack matrixStack,VertexConsumer consumer,BlockState state,double x, double y,double z){
        BakedModel bakedModel;
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        //RenderSystem.setShaderColor(1.0f,1.0f,1.0f,1.0f);
        try {
            bakedModel = blockRenderer.getBlockModel(state);
            matrixStack.pushPose();
            if (wand.mode != Mode.COPY) {
                Vec3i n = wand.side.getNormal();
                if (wand.replace) {
                    matrixStack.translate(
                        x + (0.5 * (1.0 - n.getX())) + n.getX(),
                        y + (0.5 * (1.0 - n.getY())) + n.getY(),
                        z + (0.5 * (1.0 - n.getZ())) + n.getZ()
                    );
                    matrixStack.scale(0.5f, 0.5f, 0.5f);
                    matrixStack.translate(-0.5f, -0.5f, -0.5f);
                } else {
                    matrixStack.translate(x + 0.5f, y + 0.5f, z + 0.5f);
                    matrixStack.scale(0.9f, 0.9f, 0.9f);
                    matrixStack.translate(-0.5f, -0.5f, -0.5f);
                }
            } else {
                matrixStack.translate(x, y, z);
            }

            for (Direction dir : dirs) {
                List<BakedQuad> bake_list = bakedModel.getQuads(state, dir, random);
                if (!bake_list.isEmpty()) {
                    for (BakedQuad quad : bake_list) {
                        RenderSystem.setShaderTexture(0, quad.getSprite().atlasLocation());
                        int kk = client.getBlockColors().getColor(state, null, null, 0);
                        float ff = (float) (kk >> 16 & 0xFF) / 255.0F;
                        float gg = (float) (kk >> 8 & 0xFF) / 255.0F;
                        float hh = (float) (kk & 0xFF) / 255.0F;
                        float r;
                        float g;
                        float b;
                        if (quad.isTinted()) {
                            r = Mth.clamp(ff, 0.0F, 1.0F);
                            g = Mth.clamp(gg, 0.0F, 1.0F);
                            b = Mth.clamp(hh, 0.0F, 1.0F);
                        } else {
                            r = 1.0F;
                            g = 1.0F;
                            b = 1.0F;
                        }

                        consumer.putBulkData(matrixStack.last(), quad, brightness, r, g, b, lightmap,  OverlayTexture.NO_OVERLAY, true);
                    }
                }
            }
            matrixStack.popPose();
        } catch (Exception e) {
            WandsMod.log("render_shape error "+e.toString(),prnt);
            //WandsMod.log("couldn't get model, blacklisting block...", true);
        }
    }

    public static void update_colors(){
        bo_col.fromColor(WandsConfig.c_block_outline);
        bbox_col.fromColor(WandsConfig.c_bounding_box);
        destroy_col.fromColor(WandsConfig.c_destroy);
        tool_use_col.fromColor(WandsConfig.c_tool_use);
        start_col.fromColor(WandsConfig.c_start);
        end_col.fromColor(WandsConfig.c_end);
        paste_bb_col.fromColor(WandsConfig.c_paste_bb);
        block_col.fromColor(WandsConfig.c_block);
        line_col.fromColor(WandsConfig.c_line);
    }

    static void preview_direction_mode(MultiBufferSource.BufferSource bufferSource,Matrix4f matrix, float pos_x,float pos_y,float pos_z){
        if (wand.valid && (preview_shape != null && !preview_shape.isEmpty())){
            List<AABB> list = preview_shape.toAabbs();
            if (!list.isEmpty() && wand.grid_voxel_index >= 0 && wand.grid_voxel_index < list.size()) {
                if (fancy) {
                    Tesselator tesselator = Tesselator.getInstance();
                    BufferBuilder bufferBuilder = tesselator.getBuilder();
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                    Compat.set_texture(GRID_TEXTURE);
                    int vi = 0;
                    for (AABB aabb : list) {
                        if (vi == wand.grid_voxel_index) {
                            switch (wand.side) {
                                case UP:
                                    x1 = pos_x + (float)aabb.minX;
                                    y1 = pos_y + (float)aabb.maxY + 0.05f;
                                    z1 = pos_z + (float)aabb.minZ;
                                    x2 = pos_x + (float)aabb.maxX;
                                    z2 = pos_z + (float)aabb.maxZ;
                                    bufferBuilder.vertex(matrix, x1,y1,z1).uv(0.0f, 0.0f).endVertex();
                                    bufferBuilder.vertex(matrix, x1,y1,z2).uv(0.0f, 1.0f).endVertex();
                                    bufferBuilder.vertex(matrix, x2,y1,z2).uv(1.0f, 1.0f).endVertex();
                                    bufferBuilder.vertex(matrix, x2,y1,z1).uv(1.0f, 0.0f).endVertex();
                                    break;
                                case DOWN:
                                    x1 = pos_x + (float)aabb.minX;
                                    y1 = pos_y + (float)aabb.minY - 0.02f;
                                    z1 = pos_z + (float)aabb.minZ;
                                    x2 = pos_x + (float)aabb.maxX;
                                    z2 = pos_z + (float)aabb.maxZ;
                                    bufferBuilder.vertex(matrix,x1,y1,z1).uv(0.0f, 0.0f).endVertex();
                                    bufferBuilder.vertex(matrix,x2,y1,z1).uv(1.0f, 0.0f).endVertex();
                                    bufferBuilder.vertex(matrix,x2,y1,z2).uv(1.0f, 1.0f).endVertex();
                                    bufferBuilder.vertex(matrix,x1,y1,z2).uv(0.0f, 1.0f).endVertex();
                                    break;
                                case SOUTH:
                                    x1 = pos_x + (float)aabb.minX;
                                    y1 = pos_y + (float)aabb.minY;
                                    z1 = pos_z + (float)aabb.maxZ + 0.02f;
                                    x2 = pos_x + (float)aabb.maxX;
                                    y2 = pos_y + (float)aabb.maxY;
                                    bufferBuilder.vertex(matrix,x1,y1,z1).uv(0.0f, 0.0f).endVertex();
                                    bufferBuilder.vertex(matrix,x2,y1,z1).uv(1.0f, 0.0f).endVertex();
                                    bufferBuilder.vertex(matrix,x2,y2,z1).uv(1.0f, 1.0f).endVertex();
                                    bufferBuilder.vertex(matrix,x1,y2,z1).uv(0.0f, 1.0f).endVertex();
                                    break;
                                case NORTH:
                                    x1 = pos_x + (float)aabb.minX;
                                    y1 = pos_y + (float)aabb.minY;
                                    z1 = pos_z + (float)aabb.minZ - 0.02f;
                                    x2 = pos_x + (float)aabb.maxX;
                                    y2 = pos_y + (float)aabb.maxY;
                                    bufferBuilder.vertex(matrix,x1, y1, z1).uv(0.0f, 0.0f).endVertex();
                                    bufferBuilder.vertex(matrix,x1, y2, z1).uv(0.0f, 1.0f).endVertex();
                                    bufferBuilder.vertex(matrix,x2, y2, z1).uv(1.0f, 1.0f).endVertex();
                                    bufferBuilder.vertex(matrix,x2, y1, z1).uv(1.0f, 0.0f).endVertex();
                                    break;
                                case EAST:
                                    x1 = pos_x + (float)aabb.maxX + 0.02f;
                                    y1 = pos_y + (float)aabb.minY;
                                    z1 = pos_z + (float)aabb.minZ;
                                    y2 = pos_y + (float)aabb.maxY;
                                    z2 = pos_z + (float)aabb.maxZ;
                                    bufferBuilder.vertex(matrix,x1, y1, z1).uv(0.0f, 0.0f).endVertex();
                                    bufferBuilder.vertex(matrix,x1, y2, z1).uv(1.0f, 0.0f).endVertex();
                                    bufferBuilder.vertex(matrix,x1, y2, z2).uv(1.0f, 1.0f).endVertex();
                                    bufferBuilder.vertex(matrix,x1, y1, z2).uv(0.0f, 1.0f).endVertex();
                                    break;
                                case WEST:
                                    x1 = pos_x + (float)aabb.minX - 0.02f;
                                    y1 = pos_y + (float)aabb.minY;
                                    z1 = pos_z + (float)aabb.minZ;
                                    y2 = pos_y + (float)aabb.maxY;
                                    z2 = pos_z + (float)aabb.maxZ;
                                    bufferBuilder.vertex(matrix,x1, y1, z1).uv(0.0f, 0.0f).endVertex();
                                    bufferBuilder.vertex(matrix,x1, y1, z2).uv(0.0f, 1.0f).endVertex();
                                    bufferBuilder.vertex(matrix,x1, y2, z2).uv(1.0f, 1.0f).endVertex();
                                    bufferBuilder.vertex(matrix,x1, y2, z1).uv(1.0f, 0.0f).endVertex();
                                    break;
                            }
                        }
                        vi++;
                    }
                    tesselator.end();
                    //bufferSource.endLastBatch();

                }else {
                    VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
                    int vi = 0;
                    for (AABB aabb : list) {
                        if (vi == wand.grid_voxel_index) {
                            grid(consumer, matrix, wand.side, pos_x, pos_y, pos_z, aabb);
                        }
                        vi++;
                    }
                    bufferSource.endLastBatch();
                }
            }
        }
    }

    static void preview_block_buffer(MultiBufferSource.BufferSource bufferSource,PoseStack matrixStack){
        if (wand.has_empty_bucket || (wand.valid && (has_target || wand.is_alt_pressed) && wand.block_buffer != null)) {
            random.setSeed(0);
            int block_buffer_length=wand.block_buffer.get_length();
            if (block_buffer_length >0 && fancy && !wand.destroy && !wand.use && !wand.has_empty_bucket) {
                BlockState st;
                if (wand.has_water_bucket) {
                    st = Blocks.WATER.defaultBlockState();
                } else {
                    if (wand.has_lava_bucket) {
                        st = Blocks.LAVA.defaultBlockState();
                    }
                }
                if(wand.has_water_bucket || wand.has_lava_bucket) {
                    VertexConsumer consumer= bufferSource.getBuffer(RenderType.solid());

                    int i;
                    //RenderSystem.enableCull();
                    TextureAtlasSprite sprite;
                    if (wand.has_water_bucket) {
                        sprite = ModelBakery.WATER_FLOW.sprite();
                        i = BiomeColors.getAverageWaterColor(wand.level,wand.pos);
                        Compat.set_texture(sprite.atlasLocation());
                    } else {
                        sprite = ModelBakery.LAVA_FLOW.sprite();
                        i = 16777215;
                        Compat.set_texture(sprite.atlasLocation());
                    }

                    //Compat.set_texture(TextureAtlas.LOCATION_BLOCKS);
                    float u0 = sprite.getU0();
                    float v0 = sprite.getV0();
                    float u1 = sprite.getU1();
                    float v1 = sprite.getV1();

                    for (int idx = 0; idx < block_buffer_length && idx < WandsConfig.max_limit; idx++) {
                        bp.set(wand.block_buffer.buffer_x[idx],wand.block_buffer.buffer_y[idx],wand.block_buffer.buffer_z[idx]);
                        render_fluid(
                                consumer,matrixStack.last().pose(),
                                (float) wand.block_buffer.buffer_x[idx],
                                (float) wand.block_buffer.buffer_y[idx],
                                (float) wand.block_buffer.buffer_z[idx],i,u0,v0,u1,v1);
                    }
                    bufferSource.endLastBatch();
                }else {
                    VertexConsumer consumer= bufferSource.getBuffer(RenderType.translucent());
                    //WandsMod.log("block_buffer_length "+block_buffer_length ,prnt);
                     for (int idx = 0; idx < block_buffer_length && idx < WandsConfig.max_limit; idx++) {
                         //WandsMod.log("state "+wand.block_buffer.state[idx] ,prnt);
                         if (wand.block_buffer.state[idx] != null) {
                             st = wand.block_buffer.state[idx];
                            render_shape(matrixStack,consumer, st,
                                    wand.block_buffer.buffer_x[idx],
                                    wand.block_buffer.buffer_y[idx],
                                    wand.block_buffer.buffer_z[idx]);

                            //TODO: all double blocks!!
                            if (wand.block_buffer.state[idx].hasProperty(DoublePlantBlock.HALF)) {
                                render_shape(matrixStack,consumer,
                                        wand.block_buffer.state[idx].setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER),
                                        wand.block_buffer.buffer_x[idx],
                                        wand.block_buffer.buffer_y[idx] + 1,
                                        wand.block_buffer.buffer_z[idx]);
                            } else {
                                if (wand.block_buffer.state[idx].getBlock() instanceof DoorBlock) {

                                    render_shape(matrixStack,consumer,
                                            wand.block_buffer.state[idx].setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER),
                                            wand.block_buffer.buffer_x[idx],
                                            wand.block_buffer.buffer_y[idx] + 1,
                                            wand.block_buffer.buffer_z[idx]);
                                }
                            }
                         }
                     }
                     bufferSource.endLastBatch();
                 }
            }
            if (block_buffer_length >0){
                render_mode_outline(matrixStack.last().pose(), bufferSource);
            }
        }
    }

    static void preview_bbox(MultiBufferSource.BufferSource bufferSource,PoseStack matrixStack){
        float off2 = 0.05f;
        Matrix4f matrix=matrixStack.last().pose();
        float bb1_x=wand.bb1_x;
        float bb1_y=wand.bb1_y;
        float bb1_z=wand.bb1_z;
        float bb2_x=wand.bb2_x;
        float bb2_y=wand.bb2_y;
        float bb2_z=wand.bb2_z;
        if (fat_lines) {
            VertexConsumer consumer= bufferSource.getBuffer(RenderType.debugQuads());
            preview_block_fat(matrix,consumer,
                    bb1_x - off2,
                    bb1_y - off2,
                    bb1_z - off2,
                    bb2_x + off2,
                    bb2_y + off2,
                    bb2_z + off2,
                    bbox_col,false);

        } else {
            VertexConsumer consumer= bufferSource.getBuffer(RenderType.lines());
            preview_block(matrix,consumer,
                    bb1_x - off2,
                    bb1_y - off2,
                    bb1_z - off2,
                    bb2_x + off2,
                    bb2_y + off2,
                    bb2_z + off2,
                    bbox_col);

        }
        bufferSource.endLastBatch();
    }
    static void preview_paste(MultiBufferSource.BufferSource bufferSource,PoseStack matrixStack){
        int mx=1;
        //int my=1;
        int mz=1;
        Matrix4f matrix=matrixStack.last().pose();
        switch(mirroraxis){
            case 1://X
                mx=-1;
                break;
            case 2://Y
                mz=-1;
                break;
        }
        BlockPos b_pos = wand.pos;
        if (!wand.destroy &&fancy) {
            random.setSeed(0);
            //wand.random.setSeed(wand.palette.seed);
            //BlockPos po=wand.copy_paste_buffer.getFirst().pos;
            VertexConsumer consumer= bufferSource.getBuffer(RenderType.translucent());
            for (CopyBuffer b : wand.copy_paste_buffer) {
                BlockState st =b.state;
                if (wand.palette.has_palette) {
                    st = wand.get_state(b.pos.getY());
                }else{
                    st=wand.rotate_mirror(st,mirroraxis);
                    //Mirror
                    /*Block blk=st.getBlock();
                    if(blk instanceof  StairBlock && mirroraxis >0) {
                        st = wand.paste_rot(st);

                        Direction facing=st.getValue(StairBlock.FACING);
                        StairsShape shape=st.getValue(StairBlock.SHAPE);
                        if(my==-1){
                            st=st.setValue(StairBlock.HALF,st.getValue(StairBlock.HALF));
                        }else {
                            if (
                                (mx == -1 && (facing == Direction.EAST  || facing == Direction.WEST  )) ||
                                (mz == -1 && (facing == Direction.NORTH || facing == Direction.SOUTH ))
                            ) {
                                st = st.setValue(StairBlock.FACING, facing.getOpposite());
                            }
                            if ( shape!= StairsShape.STRAIGHT){
                                if(shape==StairsShape.INNER_LEFT) {
                                    st = st.setValue(StairBlock.SHAPE, StairsShape.INNER_RIGHT);
                                }else{
                                    if(shape==StairsShape.OUTER_LEFT) {
                                        st = st.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT);
                                    }else{
                                        if(shape==StairsShape.OUTER_RIGHT) {
                                            st = st.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT);
                                        }else {
                                            if (shape == StairsShape.INNER_RIGHT) {
                                                st = st.setValue(StairBlock.SHAPE, StairsShape.INNER_LEFT);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }else{
                        st = wand.paste_rot(st);
                    }*/
                }
                BlockPos p = b.pos.rotate(last_rot);
                int px=b_pos.getX() + p.getX()*mx;
                int py=b_pos.getY() + p.getY();
                int pz=b_pos.getZ() + p.getZ()*mz;
                render_shape(matrixStack,consumer, st, px ,py,pz);
            }
            bufferSource.endLastBatch();

        }
        if (drawlines && paste_outlines) {
            Colorf col=(wand.destroy? destroy_col: paste_bb_col);
            x1 = Integer.MAX_VALUE;
            y1 = Integer.MAX_VALUE;
            z1 = Integer.MAX_VALUE;
            x2 = Integer.MIN_VALUE;
            y2 = Integer.MIN_VALUE;
            z2 = Integer.MIN_VALUE;
            VertexConsumer consumer;
            if (fat_lines) {
                consumer= bufferSource.getBuffer(RenderType.debugQuads());
            } else {
                consumer= bufferSource.getBuffer(RenderType.lines());
            }
            for (CopyBuffer b : wand.copy_paste_buffer) {
                BlockPos p = b.pos.rotate(last_rot);
                float x = b_pos.getX() + p.getX()*mx;
                float y = b_pos.getY() + p.getY();
                float z = b_pos.getZ() + p.getZ()*mz;
                if (fat_lines) {
                    preview_block_fat(matrix,consumer,
                            x, y, z,
                            x + 1, y + 1, z + 1, col,
                    true);
                } else {
                    preview_block(matrix,consumer,
                            x, y, z,
                            x + 1, y + 1, z + 1, col
                    );
                }
                if (x < x1) x1 = x;
                if (y < y1) y1 = y;
                if (z < z1) z1 = z;
                if (x + 1 > x2) x2 = x + 1;
                if (y + 1 > y2) y2 = y + 1;
                if (z + 1 > z2) z2 = z + 1;
            }

            if (fat_lines) {
                preview_block_fat(matrix,consumer,x1,y1,z1,x2,y2,z2,col,false);
            } else {
                preview_block(matrix,consumer,x1,y1,z1,x2,y2,z2,col);
            }
            bufferSource.endLastBatch();
        }
    }
    static void preview_selected(Mode mode,
                                 MultiBufferSource.BufferSource bufferSource,
                                 PoseStack matrixStack,
                                 float pos_x,
                                 float pos_y,
                                 float pos_z,
                                 float off
    ){
        Matrix4f matrix=matrixStack.last().pose();
        if (drawlines && wand.getP1() ==null &&(
            mode == Mode.FILL ||
            mode == Mode.LINE ||
            mode == Mode.CIRCLE ||
            mode == Mode.SPHERE ||
            mode == Mode.COPY ||
            mode == Mode.PASTE ||
            mode == Mode.ROW_COL||
            mode == Mode.ROCK )){
        if (fancy && wand.offhand_state!=null){
            random.setSeed(0);
            VertexConsumer consumer= bufferSource.getBuffer(RenderType.translucent());
            render_shape(matrixStack,consumer, wand.offhand_state,
                                    pos_x,pos_y,pos_z);
            bufferSource.endLastBatch();
        }
        if (fat_lines) {
            VertexConsumer consumer= bufferSource.getBuffer(RenderType.debugQuads());
            preview_block_fat(matrix,consumer,
                    (pos_x  - off),
                    (pos_y  - off),
                    (pos_z  - off),
                    (pos_x+1+ off),
                    (pos_y+1+ off),
                    (pos_z+1+ off),
                    start_col,false);
            bufferSource.endLastBatch();
        } else {
            VertexConsumer consumer= bufferSource.getBuffer(RenderType.lines());
            preview_block(matrix,consumer,
                    pos_x  - off, pos_y  - off, pos_z  - off,
                    pos_x+1+ off, pos_y+1+ off, pos_z+1+ off,
                    start_col);
            bufferSource.endLastBatch();
        }
    }
    }
    static void preview_line_circle(Matrix4f matrix, Mode mode,MultiBufferSource.BufferSource bufferSource,
                                 float p1_x,
                                 float p1_y,
                                 float p1_z,
                                 float p2_x,
                                 float p2_y,
                                 float p2_z,
                                 float off,
                                 float off2
    )
    {
        if (fat_lines) {
           boolean even = WandProps.getFlag(wand.wand_stack, WandProps.Flag.EVEN);
           {
                VertexConsumer consumer= bufferSource.getBuffer(RenderType.debugQuads());
                preview_block_fat(matrix,consumer,
                        p1_x - off,
                        p1_y - off,
                        p1_z - off,
                        p1_x + 1 + off,
                        p1_y + 1 + off,
                        p1_z + 1 + off,
                        start_col, false
                );
                bufferSource.endLastBatch();
            }
           if (has_target) {
               {
                    VertexConsumer consumer= bufferSource.getBuffer(RenderType.debugQuads());
                    off = (mode == Mode.CIRCLE && even) ? -0.5f : 0.0f;
                    preview_block_fat(matrix,consumer,
                            p2_x - off + off,
                            p2_y - off,
                            p2_z - off + off,
                            p2_x + 1 + off + off,
                            p2_y + 1 + off,
                            p2_z + 1 + off + off,
                            end_col, false);
                    bufferSource.endLastBatch();
               }
               if(mode!=Mode.FILL) {
                   VertexConsumer consumer= bufferSource.getBuffer(RenderType.debugQuads());
                   off = (mode == Mode.CIRCLE && even) ? 0.0f : 0.5f;
                   player_facing_line(consumer,
                            p1_x + off,
                            p1_y + off + 0.5f,
                            p1_z + off,
                            p2_x + off,
                            p2_y + off + 0.5f,
                            p2_z + off,
                            line_col);
                    bufferSource.endLastBatch();
               }
           }
       } else {
            VertexConsumer consumer= bufferSource.getBuffer(RenderType.lines());
            consumer.vertex(p2_x + 0.5F, p2_y + 0.5F, p2_z + 0.5F)
                .color(line_col.r, line_col.g, line_col.b, line_col.a);
            consumer.vertex(wand.x1 + 0.5F, wand.y1 + 0.5F, wand.z1 + 0.5F)
                .color(line_col.r, line_col.g, line_col.b, line_col.a);
            preview_block(matrix,consumer,
                    p1_x,p1_y,p1_z,
                    p1_x + 1, p1_y + 1, p1_z + 1,
                    start_col);
            preview_block(matrix,consumer,
                    p2_x - off2,
                    p2_y - off2,
                    p2_z - off2,
                    p2_x + 1 + off2,
                    p2_y + 1 + off2,
                    p2_z + 1 + off2,
                    end_col);
            bufferSource.endLastBatch();
       }
    }
}
