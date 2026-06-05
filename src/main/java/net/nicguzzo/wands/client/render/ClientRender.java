package net.nicguzzo.wands.client.render;

//? if>=26.1 {
    
    import net.minecraft.client.renderer.block.BlockAndTintGetter;
    import com.mojang.blaze3d.textures.GpuTextureView;
    import net.minecraft.client.renderer.rendertype.RenderTypes;
//?}

//? if>=1.21.11 <26.1{
    /*import net.minecraft.client.renderer.block.BlockRenderDispatcher;
    import net.minecraft.client.renderer.block.model.BakedQuad;
    import net.minecraft.client.renderer.block.model.BlockModelPart;
    import net.minecraft.client.renderer.block.model.BlockStateModel;
    import net.minecraft.client.renderer.rendertype.RenderTypes;
*///?}
//?if >1.21 <1.21.11{
/*import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
*///?}
//?if <1.21.1{
/*import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
*///?}

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;

import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
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
import net.nicguzzo.wands.compat.RcId;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.utils.Colorf;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.items.*;
import net.nicguzzo.wands.wand.WandProps;
import net.nicguzzo.wands.wand.WandProps.Mode;
import net.nicguzzo.wands.WandsMod;
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
    public static Direction last_side = null;
    static Mode last_mode;
    static Rotation last_rot = Rotation.NONE;
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
    static boolean drawlines = true;
    static boolean block_outlines = false;
    static boolean fill_outlines = false;
    static boolean copy_outlines = false;
    static boolean paste_outlines = false;
    //static PoseStack matrixStack2 = new PoseStack();
    static float lines_width = 0.05f;
    static Minecraft client;
    private static final RcId GRID_TEXTURE = RcId.fromNamespaceAndPath(WandsMod.MOD_ID, "textures/blocks/grid.png");
    //private static final Identifier LINE_TEXTURE = Identifier.fromNamespaceAndPath(WandsMod.MOD_ID,"textures/blocks/line.png");
    //?if > 1.21.11 {
    private static GpuTextureView water_texture=null;
    private static GpuTextureView lava_texture=null;
    //?}
    //private static GpuTexture grid_texture=null;
    static public RandomSource random = RandomSource.create();
    static Direction[] dirs = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null};
    static Vec3 player_normal;
    //?if <=1.21.1 {
    /*private static final float[] brightness=new float[]{1.0f, 1.0f, 1.0f, 1.0f};
    private static final int[] lightmap= new int[]{255,255,255,255};
    *///?}
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
            WandsMod.config.parse_colors();
            ClientRender.update_colors();
        }
        drawlines = WandsMod.config.lines;
        block_outlines = WandsMod.config.block_outlines;
        fill_outlines = WandsMod.config.fill_outlines;
        copy_outlines = WandsMod.config.copy_outlines;
        paste_outlines = WandsMod.config.paste_outlines;
        opacity = WandsMod.config.preview_opacity;
        fancy = WandsConfig.get_instance().fancy_preview;
        if (WandsConfig.get_instance().fat_lines_width > 0 && WandsConfig.get_instance().fat_lines_width < 0.5) {
            lines_width = WandsConfig.get_instance().fat_lines_width;
        }
        ItemStack stack = player.getMainHandItem();
        prnt = false;
        force = false;
        if (!stack.isEmpty() && stack.getItem() instanceof WandItem) {
            t1 = System.currentTimeMillis();
            if (t1 - t0 > 1000) {
                t0 = System.currentTimeMillis();
                prnt = true;
            }
            if (t1 - t00 > 100) {
                t00 = System.currentTimeMillis();
                force = true;
            }
            wand.reach_distance = WandProps.getVal(stack, WandProps.Value.REACH_DISTANCE);
            wand.target_air=WandProps.getFlag(stack,WandProps.Flag.TARGET_AIR);
            wand.lastPlayerDirection=player.getDirection();

            mirroraxis=WandProps.getVal(player.getMainHandItem(), WandProps.Value.MIRRORAXIS);

            boolean pinActive = wand.pin.isActive();

            if (pinActive) {
                preview_pinned(player,client.level,stack,matrixStack,bufferSource);
            }else{
                preview_interactive(player,client.level,stack,matrixStack,bufferSource);
            }
        }
    }


    static private void preview_pinned(LocalPlayer player,Level level, ItemStack stack,PoseStack matrixStack, MultiBufferSource.BufferSource bufferSource) {
        if(last_pos != wand.pin.getPos()) {
            last_pos = wand.pin.getPos();
            last_side = wand.pin.getEffectiveSide(ClientRender.client.hitResult, player.getDirection());
            BlockState block_state = level.getBlockState(last_pos);
            Vec3 hitLoc = player.getEyePosition();
            wand.do_or_preview(player, level, block_state, last_pos, last_side, hitLoc, stack, (WandItem) stack.getItem(), prnt);
        }
        preview_mode(wand.mode, matrixStack, bufferSource);
    }

    static private void preview_interactive(LocalPlayer player, Level level, ItemStack stack, PoseStack matrixStack, MultiBufferSource.BufferSource bufferSource) {
        HitResult hitResult;
        if (wand.reach_distance > 0) {
            double baseReach = Compat.is_creative(player) ? 5.0 : 4.5;
            hitResult = player.pick(baseReach + wand.reach_distance, Compat.getPartialTick(), false);
        } else {
            hitResult = client.hitResult;
        }
        wand.lastHitResult = hitResult;
        boolean hasBlockTarget = hitResult != null && hitResult.getType() == HitResult.Type.BLOCK;
        boolean missedTarget = hitResult != null && hitResult.getType() == HitResult.Type.MISS;
        //if(!hasBlockTarget)
        //    return;
        Rotation rot = WandProps.getRotation(stack);
        WandProps.Orientation orientation = WandProps.getOrientation(stack);
        Mode mode = WandProps.getMode(stack);
        Direction side=null;
        BlockPos pos=null;
        BlockState block_state=null;
        Vec3 hitLoc=null;
        if (hasBlockTarget){
            BlockHitResult block_hit = (BlockHitResult) hitResult;
            side = block_hit.getDirection();
            pos = block_hit.getBlockPos();
            block_state = level.getBlockState(pos);
            hitLoc = block_hit.getLocation();
        }else{
            if(missedTarget && mode.can_target_air() && wand.target_air){
                pos = wand.get_pos_from_air(hitResult.getLocation());
                side = player.getDirection().getOpposite();
                block_state = level.getBlockState(pos);
                hitLoc = hitResult.getLocation();
            }else{
                wand.lastHitResult =null;
                return;
            }
        }
        if (force) {
            wand.force_render = false;
            // Only apply INCSELBLOCK offset for modes that support it (skip for pin — already applied when set)
            if (WandProps.flagAppliesTo(WandProps.Flag.INCSELBLOCK, mode) && !WandProps.getFlag(stack, WandProps.Flag.INCSELBLOCK)) {
                pos = pos.relative(side, 1);
            }
            last_pos = pos;
            last_side = side;
            last_mode = mode;
            last_orientation = orientation;
            last_rot = rot;
            last_buffer_size = wand.block_buffer.get_length();
            wand.do_or_preview(player, level, block_state, pos, side, hitLoc, stack, (WandItem) stack.getItem(), prnt);
        }
        preview_shape = null;
        if (last_pos != null) {
            preview_shape = block_state.getShape(level, last_pos);
        }
        //WandsMod.log("buffer "+wand.block_buffer.get_length(),prnt);
        //WandsMod.log("last_pos "+last_pos,prnt);
        //WandsMod.log("last_side "+last_side,prnt);
        preview_mode(wand.mode, matrixStack, bufferSource);
    }

    /** Main preview entry point - routes to appropriate preview methods based on mode */
    private static void preview_mode(Mode mode, PoseStack poseStack,MultiBufferSource.BufferSource bufferSource) {

        Camera camera = client.gameRenderer.getMainCamera();

        if (camera.isInitialized() && last_pos != null) {
            poseStack.pushPose();
            Vec3 cam = Compat.getCameraPosition(camera);
            //Very important to subtract cam from last_pos first and then push a translate matrix to the stack
            //this allows correct rendering at large distances, eliminating precision errors on floats
            //then always subtract last_pos to the block about to be rendered
            float last_pos_x = (float)(last_pos.getX() - cam.x);
            float last_pos_y = (float)(last_pos.getY() - cam.y);
            float last_pos_z = (float)(last_pos.getZ() - cam.z);
            poseStack.translate(last_pos_x,last_pos_y, last_pos_z);

            float p1_x=0,p1_y=0,p1_z=0;
            BlockPos p1=wand.getP1();
            if(p1!=null) {
                p1_x = p1.getX()-last_pos.getX();
                p1_y = p1.getY()-last_pos.getY();
                p1_z = p1.getZ()-last_pos.getZ();
            }
            float off2 = 0.05f;
            float off3 = off2/2;

            //RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.8f);
            switch (mode) {
                case DIRECTION:
                    preview_direction_mode_grid(bufferSource,poseStack.last().pose(),0,0,0);
                case ROW_COL:
                case FILL:
                case AREA:
                case GRID:
                case LINE:
                case CIRCLE:
                case SPHERE:
                case VEIN:
                case BOX:
                case ROCK:
                case COPY:
                case PASTE:
                    preview_selected(mode,bufferSource,poseStack,last_pos.getX(),last_pos.getY(), last_pos.getZ(),off3);
                    if (wand.valid || (mode.n_clicks() == 2 && wand.getP1() != null)){
                        //bbox
                        boolean showBbox = (mode == Mode.COPY && copy_outlines) ||
                            (fill_outlines && (mode == Mode.ROW_COL || mode == Mode.FILL || mode == Mode.BOX));
                        if (drawlines && showBbox) {
                            preview_bbox(bufferSource,poseStack);
                            if (!block_outlines) {
                                preview_bbox_faces(bufferSource, poseStack);
                            }
                        }
                        //actual block preview
                        preview_block_buffer(bufferSource,poseStack,last_pos.getX(),last_pos.getY(), last_pos.getZ());
                        if (drawlines && p1 != null  && (mode == Mode.FILL|| mode == Mode.LINE || mode == Mode.CIRCLE || mode == Mode.SPHERE || mode == Mode.COPY)) {
                           preview_line_circle(poseStack.last().pose(),mode,bufferSource,p1_x,p1_y,p1_z,off3,off2);
                        }
                    }
                break;
            }
            poseStack.popPose();
        }
    }
    /** Shared outline renderer: draws wireframe outlines from block_buffer */
    public static void render_mode_outline(Matrix4f matrix, MultiBufferSource.BufferSource bufferSource,int pos_x,int pos_y,int pos_z){
        if(client.level==null) {
            return;
        }
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
            // Always use debugQuads - RenderTypes.lines() has incompatible vertex format in 1.21
            if(wand.has_empty_bucket ){
                preview_shape = Blocks.STONE.defaultBlockState().getShape(client.level, last_pos);
            };
            VertexConsumer consumer =getVertexConsumerDebugQuads(bufferSource);
            BlockPos.MutableBlockPos fluid_pos=new BlockPos.MutableBlockPos();
            for (int idx = 0; idx < wand.block_buffer.get_length() && idx < WandsConfig.max_limit; idx++) {
                float x = wand.block_buffer.buffer_x[idx]-pos_x;
                float y = wand.block_buffer.buffer_y[idx]-pos_y;
                float z = wand.block_buffer.buffer_z[idx]-pos_z;

                if (wand.block_buffer.state[idx] != null) {
                    if(!wand.has_empty_bucket){
                        preview_shape = wand.block_buffer.state[idx].getShape(client.level, last_pos);
                    }else{
                        fluid_pos.set(x,y,z);
                        FluidState fluid_state=client.level.getFluidState(fluid_pos);
                        if(!fluid_state.isSource()){
                            continue;
                        }
                    }
                    if(preview_shape==null){
                        continue;
                    }
                    List<AABB> list = preview_shape.toAabbs();
                    for (AABB aabb : list) {
                            preview_block_fat(matrix,consumer,
                            x + (float) aabb.minX, y + (float) aabb.minY, z + (float) aabb.minZ,
                            x + (float) aabb.maxX, y + (float) aabb.maxY, z + (float) aabb.maxZ,
                            mode_outline_color,wand.destroy);
                    }
                }
            }
            bufferSource.endLastBatch();
        }
    }

    /** Render an outline for the target air block position (used in Copy mode with target_air) */
    public static void render_air_target_outline(BlockPos pos, PoseStack matrixStack, MultiBufferSource.BufferSource bufferSource) {
        Camera camera = client.gameRenderer.getMainCamera();
        //?if>=1.21.11{
        Vec3 camPos = camera.position();
        //?}else{
        /*Vec3 camPos = camera.getPosition();
        *///?}

        matrixStack.pushPose();
        matrixStack.translate(-camPos.x, -camPos.y, -camPos.z);

        // Always use debugQuads - RenderTypes.lines() has incompatible vertex format in 1.21
        VertexConsumer consumer= getVertexConsumerDebugQuads(bufferSource);

        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();

        preview_block_fat(matrixStack.last().pose(), consumer, x, y, z, x + 1, y + 1, z + 1, bo_col, false);

        bufferSource.endLastBatch();
        matrixStack.popPose();
    }

    // Draws box outline using quads (works with debugQuads render type)
    static void preview_block(Matrix4f matrix, VertexConsumer consumer, float fx1, float fy1, float fz1, float fx2, float fy2, float fz2, Colorf c) {
        // Use thin quad lines when fat_lines is disabled
        float w = lines_width;
        float off = 0.005f;
        fx1 -= off;
        fy1 -= off;
        fz1 -= off;
        fx2 += off;
        fy2 += off;
        fz2 += off;
        // Draw box edges as thin quads
        //north -z
        quad_line(matrix,consumer,  0, w,0, fx1,   fy1, fz1, fx2,   fy1, fz1,c);
        quad_line(matrix,consumer,  0,-w,0, fx2,   fy2, fz1, fx1,   fy2, fz1,c);
        quad_line(matrix,consumer,  w, 0,0, fx1, fy2-w, fz1, fx1, fy1+w, fz1,c);
        quad_line(matrix,consumer, -w, 0,0, fx2, fy1+w, fz1, fx2, fy2-w, fz1,c);
        //south +z
        quad_line(matrix,consumer,  0, w,0, fx2,   fy1, fz2, fx1,   fy1, fz2,c);
        quad_line(matrix,consumer,  0,-w,0, fx1,   fy2, fz2, fx2,   fy2, fz2,c);
        quad_line(matrix,consumer,  w, 0,0, fx1, fy1+w, fz2, fx1, fy2-w, fz2,c);
        quad_line(matrix,consumer, -w, 0,0, fx2, fy2-w, fz2, fx2, fy1+w, fz2,c);
        //up +y
        quad_line(matrix,consumer,  w,0, 0, fx1  , fy2, fz2, fx1 , fy2, fz1,c);
        quad_line(matrix,consumer, -w,0, 0, fx2  , fy2, fz1, fx2 , fy2, fz2,c);
        quad_line(matrix,consumer,  0,0, w, fx1+w, fy2, fz1, fx2-w, fy2, fz1,c);
        quad_line(matrix,consumer,  0,0,-w, fx2-w, fy2, fz2, fx1+w, fy2, fz2,c);
        //down -y
        quad_line(matrix,consumer,  w,0, 0, fx1, fy1, fz1, fx1  , fy1, fz2,c);
        quad_line(matrix,consumer, -w,0, 0, fx2  , fy1, fz2,fx2, fy1, fz1,c);
        quad_line(matrix,consumer,  0,0, w, fx2-w, fy1, fz1,fx1+w, fy1, fz1,c);
        quad_line(matrix,consumer,  0,0,-w, fx1+w, fy1, fz2,  fx2-w, fy1, fz2,c);
        //east +x
        quad_line(matrix,consumer, 0, w, 0, fx2,   fy1, fz1, fx2,   fy1, fz2,c);
        quad_line(matrix,consumer, 0,-w, 0, fx2,   fy2, fz2, fx2,   fy2, fz1,c);
        quad_line(matrix,consumer, 0, 0, w, fx2, fy2-w, fz1, fx2, fy1+w, fz1,c);
        quad_line(matrix,consumer, 0, 0,-w, fx2, fy1+w, fz2, fx2, fy2-w, fz2,c);
        //west -x
        quad_line(matrix,consumer, 0, w,0,   fx1,   fy1, fz2,fx1,   fy1, fz1,c);
        quad_line(matrix,consumer, 0,-w, 0, fx1,   fy2, fz1, fx1,   fy2, fz2,c);
        quad_line(matrix,consumer, 0, 0,-w, fx1, fy2-w, fz1, fx1, fy1+w, fz1,c);
        quad_line(matrix,consumer, 0, 0, w, fx1, fy1+w, fz2, fx1, fy2-w, fz2,c);
    }

    /** Low-level: draws a single wireframe box as thick quads, with optional X cross */
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
        float w= lines_width;
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

        Compat.consumerAddVertexColor(consumer,matrix,   lx1,    ly1,    lz1,c);
        Compat.consumerAddVertexColor(consumer,matrix,lx1+wx, ly1+wy, lz1+wz,c);
        Compat.consumerAddVertexColor(consumer,matrix,lx2+wx, ly2+wy, lz2+wz,c);
        Compat.consumerAddVertexColor(consumer,matrix,   lx2,    ly2,    lz2,c);
    }

    private static void player_facing_line(VertexConsumer consumer,Matrix4f matrix,float lx1, float ly1,float lz1,float lx2, float ly2,float lz2,Colorf c){

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
        //RenderSystem.setShaderColor(c.r,c.g,c.b,c.a);

        Compat.consumerAddVertexColor(consumer,matrix,lx1-nx, ly1-ny, lz1-nz,c);
        Compat.consumerAddVertexColor(consumer,matrix,lx1+nx, ly1+ny, lz1+nz,c);
        Compat.consumerAddVertexColor(consumer,matrix,lx2+nx, ly2+ny, lz2+nz,c);
        Compat.consumerAddVertexColor(consumer,matrix,lx2-nx, ly2-ny, lz2-nz,c);

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
    // Draws lines as quads (for use with debugQuads render type)
    private static void draw_lines(Matrix4f matrix, VertexConsumer consumer, int from, int to, float r, float g, float b, float a) {
        float w = lines_width * 0.5f;
        Colorf c = new Colorf(r, g, b, a);
        // Draw lines as pairs of vertices (from is start, every 2 vertices is a line)
        for (int i = from; i < to - 1 && i < grid_n - 1; i += 2) {
            float x1 = grid_vx[i], y1 = grid_vy[i], z1 = grid_vz[i];
            float x2 = grid_vx[i+1], y2 = grid_vy[i+1], z2 = grid_vz[i+1];
            // Determine line direction and offset for quad width
            float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
            if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > Math.abs(dz)) {
                // Horizontal X line - offset in Y and Z
                quad_line(matrix, consumer, 0, w, 0, x1, y1, z1, x2, y2, z2, c);
            } else if (Math.abs(dy) > Math.abs(dz)) {
                // Vertical Y line - offset in X and Z
                quad_line(matrix, consumer, w, 0, 0, x1, y1, z1, x2, y2, z2, c);
            } else {
                // Horizontal Z line - offset in X and Y
                quad_line(matrix, consumer, w, 0, 0, x1, y1, z1, x2, y2, z2, c);
            }
        }
    }

    static void render_fluid(VertexConsumer consumer, Matrix4f matrix, float x, float y,float z,int color,float u1,float v1,float u0,float v0) {

            float h = 1.0f;
            float o = 0.05f;
            int bf = 15728880;
            //up
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + o    , y + h - o, z + o    ,u1, v1,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + o    , y + h - o, z + 1 - o,u1, v0,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + 1 - o, y + h - o, z + 1 - o,u0, v0,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + 1 - o, y + h - o, z + o    ,u0, v1,color,0,1,0,bf);
            //down
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + o    , y + o, z + o    ,u1, v1,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + 1 - o, y + o, z + o    ,u0, v1,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + 1 - o, y + o, z + 1 - o,u0, v0,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + o    , y + o, z + 1 - o,u1, v0,color,0,1,0,bf);
            //north -z
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + o, y + o, z + o        ,u1, v1,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + o, y + h - o, z + o    ,u1, v0,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + 1 - o, y + h - o, z + o,u0, v0,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + 1 - o, y + o, z + o    ,u0, v1,color,0,1,0,bf);
            //south +z
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + o, y + o, z + 1 - o        ,u1, v1,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + 1 - o, y + o, z + 1 - o    ,u0, v1,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + 1 - o, y + h - o, z + 1 - o,u0, v0,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + o, y + h - o, z + 1 - o    ,u1, v0,color,0,1,0,bf);
            //east
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + o, y + o, z + o        ,u0, v1,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + o, y + o, z + 1 - o    ,u1, v1,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + o, y + h - o, z + 1 - o,u1, v0,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + o, y + h - o, z + o    ,u0, v0,color,0,1,0,bf);
            //weast
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + 1 - o, y + o, z + o        ,u0, v1,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + 1 - o, y + h - o, z + o    ,u0, v0,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + 1 - o, y + h - o, z + 1 - o,u1, v0,color,0,1,0,bf);
            Compat.consumerAddVertexUvColorNormalLight(consumer,matrix,x + 1 - o, y + o, z + 1 - o    ,u1, v1,color,0,1,0,bf);

    }

    static int get_fluid_color() {
        int alpha = ((int)(opacity * 255)) << 24;
        if (wand.has_water_bucket) {
            return BiomeColors.getAverageWaterColor(
            //? if>=26.1
            (BlockAndTintGetter)
            wand.level,
            wand.pos) | alpha;
        } else {
            return 0xFFFFFF | alpha;
        }
    }

    static void preview_fluid_buffer(MultiBufferSource.BufferSource bufferSource, PoseStack matrixStack,int pos_x,int pos_y, int pos_z) {
        try {
            TextureAtlasSprite sprite = Compat.getFluidFlowSprite(wand.has_water_bucket);
            int color = get_fluid_color();
            VertexConsumer consumer = getVertexConsumerPVBlock(bufferSource);

            float u0 = sprite.getU0();
            float v0 = sprite.getV0();
            float u1 = sprite.getU1();
            float v1 = sprite.getV1();

            int block_buffer_length = wand.block_buffer.get_length();
            for (int idx = 0; idx < block_buffer_length && idx < WandsConfig.max_limit; idx++) {
                bp.set(wand.block_buffer.buffer_x[idx], wand.block_buffer.buffer_y[idx], wand.block_buffer.buffer_z[idx]);
                matrixStack.pushPose();
                matrixStack.translate(
                    wand.block_buffer.buffer_x[idx]-pos_x,
                    wand.block_buffer.buffer_y[idx]-pos_y,
                    wand.block_buffer.buffer_z[idx]-pos_z);
                render_fluid(consumer, matrixStack.last().pose(),
                    0, 0, 0,
                    color, u0, v1, u1, v0);
                matrixStack.popPose();
            }
            bufferSource.endLastBatch();
        } catch (Exception e) {
            WandsMod.log("preview_fluid_buffer exception: " + e.getMessage(), true);
        }
    }
//? if>=26.1 {
    static void render_shape(PoseStack matrixStack,VertexConsumer consumer,BlockState state,double x, double y,double z) {
    }
//?}

//? if>=1.21.11 <26.1{
    /*static void render_shape(PoseStack matrixStack,VertexConsumer consumer,BlockState state,double x, double y,double z){
        BlockStateModel bakedModel;
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        try {
            bakedModel = blockRenderer.getBlockModel(state);
            List<BlockModelPart> parts_list = bakedModel.collectParts(random);

            if (!parts_list.isEmpty() ) {
                matrixStack.pushPose();
                //Compat.set_identity(matrixStack2);
                if(wand.mode!=Mode.COPY ){
                    Vec3i n=wand.getSide().getUnitVec3i();
                    if(wand.replace) {
                        matrixStack.translate(
                            (0.5*(1.0-n.getX()))+n.getX(),
                            (0.5*(1.0-n.getY()))+n.getY(),
                            (0.5*(1.0-n.getZ()))+n.getZ()
                        );
                        matrixStack.scale(0.5f, 0.5f, 0.5f);
                        matrixStack.translate(-0.5f,-0.5f,-0.5f);
                    }else {
                        matrixStack.translate(x+0.5f,y+0.5f,z+0.5f);
                        matrixStack.scale(0.9f, 0.9f, 0.9f);
                        matrixStack.translate(-0.5f,-0.5f,-0.5f);
                    }
                }
                for (BlockModelPart part: parts_list) {
                    for(Direction dir: dirs) {
                        List<BakedQuad> bake_list = part.getQuads(dir);
                        for (BakedQuad quad : bake_list) {
                            //if(wand.replace ||
                            //        Block.shouldRenderFace( state, wand.level.getBlockState(bp.relative(dir)),dir )
                            //)
                            {
                                //quad.sprite().atlasLocation().
                                TextureManager textureManager = Minecraft.getInstance().getTextureManager();
                                AbstractTexture abstractTexture = textureManager.getTexture(quad.sprite().atlasLocation());

                                //RenderSystem.setShaderTexture(0, abstractTexture.getTextureView());

                                //float f = wand.level.getShade(quad.direction(), quad.shade());
                                int kk = client.getBlockColors().getColor(state, null, null, 0);
                                float ff = (float) (kk >> 16 & 0xFF) / 255.0F;
                                float gg = (float) (kk >> 8 & 0xFF) / 255.0F;
                                float hh = (float) (kk & 0xFF) / 255.0F;
                                float k = 1.0F;
                                float l = 1.0F;
                                float m = 1.0F;
                                if (quad.isTinted()) {
                                    k = Mth.clamp(ff, 0.0F, 1.0F);
                                    l = Mth.clamp(gg, 0.0F, 1.0F);
                                    m = Mth.clamp(hh, 0.0F, 1.0F);
                                }
                                //WandsMod.log("consumer.putBulkData",prnt);
                                consumer.putBulkData(matrixStack.last(), quad, k, l, m, opacity, 15728880, OverlayTexture.NO_OVERLAY);
                            }
                        }
                    }
                }
                matrixStack.popPose();
            }
        } catch (Exception e) {
            WandsMod.log("render_shape error "+e.toString(),prnt);
            //WandsMod.log("couldn't get model, blacklisting block...", true);
        }
    }
*///?}
//?if >1.21 <1.21.11{
    /*static void render_shape(PoseStack matrixStack,VertexConsumer consumer,BlockState state,double x, double y,double z){
            BakedModel bakedModel;
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
            RenderSystem.setShaderColor(1.0f,1.0f,1.0f,1.0f);
            try {
                bakedModel = blockRenderer.getBlockModel(state);
                matrixStack.pushPose();
                if (wand.mode != Mode.COPY) {
                    Vec3i n = wand.getSide().getNormal();
                    if (wand.replace) {
                        matrixStack.translate(
                                 (0.5 * (1.0 - n.getX())) + n.getX(),
                                 (0.5 * (1.0 - n.getY())) + n.getY(),
                                 (0.5 * (1.0 - n.getZ())) + n.getZ()
                        );
                        matrixStack.scale(0.5f, 0.5f, 0.5f);
                        matrixStack.translate(-0.5f, -0.5f, -0.5f);
                    } else {
                        matrixStack.translate(x + 0.5f, y + 0.5f, z + 0.5f);
                        matrixStack.scale(0.9f, 0.9f, 0.9f);
                        matrixStack.translate(-0.5f, -0.5f, -0.5f);
                    }
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
                            float k;
                            float l;
                            float m;
                            if (quad.isTinted()) {
                                k = Mth.clamp(ff, 0.0F, 1.0F);
                                l = Mth.clamp(gg, 0.0F, 1.0F);
                                m = Mth.clamp(hh, 0.0F, 1.0F);
                            } else {
                                k = 1.0F;
                                l = 1.0F;
                                m = 1.0F;
                            }
                            //WandsMod.log("consumer.putBulkData",prnt);
                            consumer.putBulkData(matrixStack.last(), quad, k, l, m, opacity, 15728880, OverlayTexture.NO_OVERLAY);
                        }
                    }
                }
                matrixStack.popPose();
            } catch (Exception e) {
                WandsMod.log("render_shape error "+e.toString(),prnt);
                //WandsMod.log("couldn't get model, blacklisting block...", true);
            }
        }
    
*///?}
//?if <1.21.1{
    /*static void render_shape(PoseStack matrixStack,VertexConsumer consumer,BlockState state,double x, double y,double z){
        BakedModel bakedModel;
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        //RenderSystem.setShaderColor(1.0f,1.0f,1.0f,1.0f);
        try {
            bakedModel = blockRenderer.getBlockModel(state);
            matrixStack.pushPose();
            if (wand.mode != Mode.COPY) {
                Vec3i n = wand.getSide().getNormal();
                if (wand.replace) {
                    matrixStack.translate(
                            (0.5 * (1.0 - n.getX())) + n.getX(),
                            (0.5 * (1.0 - n.getY())) + n.getY(),
                            (0.5 * (1.0 - n.getZ())) + n.getZ()
                    );
                    matrixStack.scale(0.5f, 0.5f, 0.5f);
                    matrixStack.translate(-0.5f, -0.5f, -0.5f);
                } else {
                    matrixStack.translate(x + 0.5f, y + 0.5f, z + 0.5f);
                    matrixStack.scale(0.9f, 0.9f, 0.9f);
                    matrixStack.translate(-0.5f, -0.5f, -0.5f);
                }
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
*///?}
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

    /** Renders the 3x3 grid overlay on block faces for DIRECTION mode */
    static void preview_direction_mode_grid(MultiBufferSource.BufferSource bufferSource, Matrix4f matrix, float pos_x, float pos_y, float pos_z){
        if (wand.valid && (preview_shape != null && !preview_shape.isEmpty())){
            List<AABB> list = preview_shape.toAabbs();
            if (!list.isEmpty() && wand.grid_voxel_index >= 0 && wand.grid_voxel_index < list.size()) {
                VertexConsumer consumer=getVertexConsumerDirMode(bufferSource);
                int vi = 0;
                int color=0xffffffff;
                int light=15728880;
                int overlay= OverlayTexture.NO_OVERLAY;
                for (AABB aabb : list) {
                    if (vi == wand.grid_voxel_index) {
                        switch (wand.getSide()) {
                            case UP:
                                x1 = pos_x + (float)aabb.minX;
                                y1 = pos_y + (float)aabb.maxY + 0.02f;
                                z1 = pos_z + (float)aabb.minZ;
                                x2 = pos_x + (float)aabb.maxX;
                                z2 = pos_z + (float)aabb.maxZ;

                                consumerDirMode(consumer,matrix,x1,y1,z1,0.0f, 0.0f,color,0,1,0,light,overlay);
                                consumerDirMode(consumer,matrix,x1,y1,z2,0.0f, 1.0f,color,0,1,0,light,overlay);
                                consumerDirMode(consumer,matrix,x2,y1,z2,1.0f, 1.0f,color,0,1,0,light,overlay);
                                consumerDirMode(consumer,matrix,x2,y1,z1,1.0f, 0.0f,color,0,1,0,light,overlay);
                                break;
                            case DOWN:
                                x1 = pos_x + (float)aabb.minX;
                                y1 = pos_y + (float)aabb.minY - 0.02f;
                                z1 = pos_z + (float)aabb.minZ;
                                x2 = pos_x + (float)aabb.maxX;
                                z2 = pos_z + (float)aabb.maxZ;
                                consumerDirMode(consumer,matrix,x1,y1,z1,0.0f, 0.0f,color,0,-1,0,light,overlay);
                                consumerDirMode(consumer,matrix,x2,y1,z1,1.0f, 0.0f,color,0,-1,0,light,overlay);
                                consumerDirMode(consumer,matrix,x2,y1,z2,1.0f, 1.0f,color,0,-1,0,light,overlay);
                                consumerDirMode(consumer,matrix,x1,y1,z2,0.0f, 1.0f,color,0,-1,0,light,overlay);
                                break;
                            case SOUTH:
                                x1 = pos_x + (float)aabb.minX;
                                y1 = pos_y + (float)aabb.minY;
                                z1 = pos_z + (float)aabb.maxZ + 0.02f;
                                x2 = pos_x + (float)aabb.maxX;
                                y2 = pos_y + (float)aabb.maxY;
                                consumerDirMode(consumer,matrix,x1,y1,z1,0.0f, 0.0f,color,0,0,1,light,overlay);
                                consumerDirMode(consumer,matrix,x2,y1,z1,1.0f, 0.0f,color,0,0,1,light,overlay);
                                consumerDirMode(consumer,matrix,x2,y2,z1,1.0f, 1.0f,color,0,0,1,light,overlay);
                                consumerDirMode(consumer,matrix,x1,y2,z1,0.0f, 1.0f,color,0,0,1,light,overlay);
                                break;
                            case NORTH:
                                x1 = pos_x + (float)aabb.minX;
                                y1 = pos_y + (float)aabb.minY;
                                z1 = pos_z + (float)aabb.minZ - 0.02f;
                                x2 = pos_x + (float)aabb.maxX;
                                y2 = pos_y + (float)aabb.maxY;
                                consumerDirMode(consumer,matrix,x1, y1, z1,0.0f, 0.0f,color,0,0,-1,light,overlay);
                                consumerDirMode(consumer,matrix,x1, y2, z1,0.0f, 1.0f,color,0,0,-1,light,overlay);
                                consumerDirMode(consumer,matrix,x2, y2, z1,1.0f, 1.0f,color,0,0,-1,light,overlay);
                                consumerDirMode(consumer,matrix,x2, y1, z1,1.0f, 0.0f,color,0,0,-1,light,overlay);
                                break;
                            case EAST:
                                x1 = pos_x + (float)aabb.maxX + 0.02f;
                                y1 = pos_y + (float)aabb.minY;
                                z1 = pos_z + (float)aabb.minZ;
                                y2 = pos_y + (float)aabb.maxY;
                                z2 = pos_z + (float)aabb.maxZ;
                                consumerDirMode(consumer,matrix,x1, y1, z1,0.0f, 0.0f,color,1,0,0,light,overlay);
                                consumerDirMode(consumer,matrix,x1, y2, z1,1.0f, 0.0f,color,1,0,0,light,overlay);
                                consumerDirMode(consumer,matrix,x1, y2, z2,1.0f, 1.0f,color,1,0,0,light,overlay);
                                consumerDirMode(consumer,matrix,x1, y1, z2,0.0f, 1.0f,color,1,0,0,light,overlay);
                                break;
                            case WEST:
                                x1 = pos_x + (float)aabb.minX - 0.02f;
                                y1 = pos_y + (float)aabb.minY;
                                z1 = pos_z + (float)aabb.minZ;
                                y2 = pos_y + (float)aabb.maxY;
                                z2 = pos_z + (float)aabb.maxZ;
                                consumerDirMode(consumer,matrix,x1, y1, z1,0.0f, 0.0f,color,-1,0,0,light,overlay);
                                consumerDirMode(consumer,matrix,x1, y1, z2,0.0f, 1.0f,color,-1,0,0,light,overlay);
                                consumerDirMode(consumer,matrix,x1, y2, z2,1.0f, 1.0f,color,-1,0,0,light,overlay);
                                consumerDirMode(consumer,matrix,x1, y2, z1,1.0f, 0.0f,color,-1,0,0,light,overlay);
                                break;
                        }
                    }
                    vi++;
                }
                //?if > 1.20.1{
                    bufferSource.endLastBatch();
                //?}else{
                    /*Tesselator tesselator = Tesselator.getInstance();
                    tesselator.end();
                *///?}
            }
        }
    }

    /** Shared preview: renders actual block shapes from block_buffer - the single source of truth */
    static void preview_block_buffer(MultiBufferSource.BufferSource bufferSource,PoseStack matrixStack,int pos_x,int pos_y,int pos_z){
        //if (wand.has_empty_bucket || (wand.valid && (has_target || wand.pin.isActive()) && wand.block_buffer != null)) {
        if (wand.has_empty_bucket || (wand.valid && wand.block_buffer != null)) {
            random.setSeed(0);
            int block_buffer_length=wand.block_buffer.get_length();
            //int pos_x = last_pos.getX();
            //int pos_y = last_pos.getY();
            //int pos_z = last_pos.getZ();
            if (block_buffer_length >0 && fancy && !wand.destroy && !wand.use ) {
                BlockState st=null;
                if (wand.has_water_bucket) {
                    st = Blocks.WATER.defaultBlockState();
                } else {
                    if (wand.has_lava_bucket) {
                        st = Blocks.LAVA.defaultBlockState();
                    }
                }
                //st=Blocks.STONE.defaultBlockState();

                if(st!=null) {
                    preview_fluid_buffer(bufferSource, matrixStack,pos_x,pos_y,pos_z);
                }else {
                    //RenderType rt=RenderTypes.translucentMovingBlock();
                    //String sampler0=rt.pipeline().getSamplers().getFirst();
                    //Map<String, RenderSetup.TextureAndSampler> tmap= rt.state.getTextures();
                    //tmap.get("Sampler0").sampler().
                    VertexConsumer consumer=getVertexConsumerPVBlock(bufferSource);

                    //WandsMod.log("block_buffer_length "+block_buffer_length ,prnt);

                     for (int idx = 0; idx < block_buffer_length && idx < WandsConfig.max_limit; idx++) {
                         //WandsMod.log("state "+wand.block_buffer.state[idx] ,prnt);
                         if (wand.block_buffer.state[idx] != null) {
                             st = wand.block_buffer.state[idx];
                            render_shape(matrixStack,consumer, st,
                                    wand.block_buffer.buffer_x[idx]-pos_x,
                                    wand.block_buffer.buffer_y[idx]-pos_y,
                                    wand.block_buffer.buffer_z[idx]-pos_z);

                            //TODO: all double blocks!!
                            if (wand.block_buffer.state[idx].hasProperty(DoublePlantBlock.HALF)) {
                                render_shape(matrixStack,consumer,
                                        wand.block_buffer.state[idx].setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER),
                                        wand.block_buffer.buffer_x[idx]-pos_x,
                                        wand.block_buffer.buffer_y[idx]-pos_y + 1,
                                        wand.block_buffer.buffer_z[idx]-pos_z);
                            } else {
                                if (wand.block_buffer.state[idx].getBlock() instanceof DoorBlock) {

                                    render_shape(matrixStack,consumer,
                                            wand.block_buffer.state[idx].setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER),
                                            wand.block_buffer.buffer_x[idx]-pos_x,
                                            wand.block_buffer.buffer_y[idx]-pos_y + 1,
                                            wand.block_buffer.buffer_z[idx]-pos_z);
                                }
                            }
                         }
                     }
                     bufferSource.endLastBatch();
                 }
            }
            if (block_buffer_length >0){
                render_mode_outline(matrixStack.last().pose(), bufferSource,pos_x,pos_y,pos_z);
            }
        }
    }

    /** Renders bounding box outline for COPY, FILL, BOX, PASTE modes */
    static void preview_bbox(MultiBufferSource.BufferSource bufferSource,PoseStack matrixStack){
        float off2 = 0.05f;
        Matrix4f matrix=matrixStack.last().pose();
        float bb1_x=wand.bb1_x;
        float bb1_y=wand.bb1_y;
        float bb1_z=wand.bb1_z;
        float bb2_x=wand.bb2_x;
        float bb2_y=wand.bb2_y;
        float bb2_z=wand.bb2_z;
        // Always use debugQuads - RenderTypes.lines() has incompatible vertex format in 1.21
        VertexConsumer consumer= getVertexConsumerDebugQuads(bufferSource);
        preview_block_fat(matrix,consumer,
                bb1_x - off2,
                bb1_y - off2,
                bb1_z - off2,
                bb2_x + off2,
                bb2_y + off2,
                bb2_z + off2,
                bbox_col,false);

        bufferSource.endLastBatch();
    }
    /** Renders animated translucent faces for bounding box when block_outlines is disabled */
    static void preview_bbox_faces(MultiBufferSource.BufferSource bufferSource, PoseStack matrixStack) {
        // Calculate animation: sine wave over 3 seconds (3000ms)
        long currentTime = System.currentTimeMillis();
        double phase = ((currentTime % 3000) / 3000.0) * 2 * Math.PI;
        float alpha = 0.3f * (float)((Math.sin(phase) + 1.0) / 2.0);

        // Get bbox coordinates with same offset as outline
        float off2 = 0.05f;
        float x1 = wand.bb1_x - off2;
        float y1 = wand.bb1_y - off2;
        float z1 = wand.bb1_z - off2;
        float x2 = wand.bb2_x + off2;
        float y2 = wand.bb2_y + off2;
        float z2 = wand.bb2_z + off2;

        Matrix4f matrix = matrixStack.last().pose();
        VertexConsumer consumer =getVertexConsumerDebugQuads(bufferSource);
        Colorf color=new Colorf(bbox_col.r,bbox_col.g,bbox_col.b,alpha);

        // Top face (y = y2)
        Compat.consumerAddVertexColor(consumer,matrix, x1, y2, z1,color);
        Compat.consumerAddVertexColor(consumer,matrix, x1, y2, z2,color);
        Compat.consumerAddVertexColor(consumer,matrix, x2, y2, z2,color);
        Compat.consumerAddVertexColor(consumer,matrix, x2, y2, z1,color);

        // Bottom face (y = y1)
        Compat.consumerAddVertexColor(consumer,matrix, x1, y1, z1,color);
        Compat.consumerAddVertexColor(consumer,matrix, x2, y1, z1,color);
        Compat.consumerAddVertexColor(consumer,matrix, x2, y1, z2,color);
        Compat.consumerAddVertexColor(consumer,matrix, x1, y1, z2,color);

        // North face (z = z1)
        Compat.consumerAddVertexColor(consumer,matrix, x1, y1, z1,color);
        Compat.consumerAddVertexColor(consumer,matrix, x1, y2, z1,color);
        Compat.consumerAddVertexColor(consumer,matrix, x2, y2, z1,color);
        Compat.consumerAddVertexColor(consumer,matrix, x2, y1, z1,color);

        // South face (z = z2)
        Compat.consumerAddVertexColor(consumer,matrix, x1, y1, z2,color);
        Compat.consumerAddVertexColor(consumer,matrix, x2, y1, z2,color);
        Compat.consumerAddVertexColor(consumer,matrix, x2, y2, z2,color);
        Compat.consumerAddVertexColor(consumer,matrix, x1, y2, z2,color);

        // West face (x = x1)
        Compat.consumerAddVertexColor(consumer,matrix, x1, y1, z1,color);
        Compat.consumerAddVertexColor(consumer,matrix, x1, y1, z2,color);
        Compat.consumerAddVertexColor(consumer,matrix, x1, y2, z2,color);
        Compat.consumerAddVertexColor(consumer,matrix, x1, y2, z1,color);

        // East face (x = x2)
        Compat.consumerAddVertexColor(consumer,matrix, x2, y1, z1,color);
        Compat.consumerAddVertexColor(consumer,matrix, x2, y2, z1,color);
        Compat.consumerAddVertexColor(consumer,matrix, x2, y2, z2,color);
        Compat.consumerAddVertexColor(consumer,matrix, x2, y1, z2,color);

        bufferSource.endLastBatch();
    }

    /** Renders highlight at cursor position showing selected/target block */
    static void preview_selected(Mode mode,
                                 MultiBufferSource.BufferSource bufferSource,
                                 PoseStack matrixStack,
                                 float pos_x,
                                 float pos_y,
                                 float pos_z,
                                 float off
    ){
        Matrix4f matrix=matrixStack.last().pose();
        boolean pinActive = wand.pin.isActive();
        // Skip cursor ghost block/outline when pin is active — pin outline replaces it
        if (drawlines && !pinActive && wand.getP1() ==null &&(
            mode == Mode.FILL ||
            mode == Mode.LINE ||
            mode == Mode.CIRCLE ||
            mode == Mode.SPHERE ||
            mode == Mode.COPY ||
            mode == Mode.PASTE ||
            mode == Mode.ROW_COL||
            mode == Mode.ROCK ))
        {

            if (fancy && mode != Mode.COPY){
                if (wand.offhand_state!=null) {
                    random.setSeed(0);
                    VertexConsumer consumer=getVertexConsumerPVBlock(bufferSource);
                    render_shape(matrixStack,consumer, wand.offhand_state,
                                            pos_x,pos_y,pos_z);
                    bufferSource.endLastBatch();
                } else if (wand.has_water_bucket || wand.has_lava_bucket) {
                    //try {
                    //    TextureAtlasSprite sprite = Compat.getFluidFlowSprite(wand.has_water_bucket);
                    //    int color = get_fluid_color();
                    //    VertexConsumer consumer = getVertexConsumerPVBlock(bufferSource);
                    //    matrixStack.pushPose();
                    //    //matrixStack.translate(pos_x, pos_y, pos_z);
                    //    render_fluid(consumer, matrixStack.last().pose(),
                    //        0, 0, 0, color,
                    //        sprite.getU0(), sprite.getV1(), sprite.getU1(), sprite.getV0());
                    //    matrixStack.popPose();
                    //    bufferSource.endLastBatch();
                    //} catch (Exception e) {
                    //    WandsMod.log("preview_selected fluid exception: " + e.getMessage(), true);
                    //}
                }
            }
            // Always use debugQuads - RenderTypes.lines() has incompatible vertex format in 1.21
            VertexConsumer consumer= getVertexConsumerDebugQuads(bufferSource);
            preview_block_fat(matrix,consumer,
                        (0  - off),
                        (0  - off),
                        (0  - off),
                        (1+ off),
                        (1+ off),
                        (1+ off),
                        start_col,false);
            bufferSource.endLastBatch();
        }

        // Draw P1 outline when P1 is set in 2-click modes
        if (drawlines && wand.getP1() != null && mode.n_clicks() == 2) {
            float p1x = wand.getP1().getX()-pos_x;
            float p1y = wand.getP1().getY()-pos_y;
            float p1z = wand.getP1().getZ()-pos_z;
            VertexConsumer consumer = getVertexConsumerDebugQuads(bufferSource);

            preview_block_fat(matrix, consumer,
                        p1x - off, p1y - off, p1z - off,
                        p1x + 1 + off, p1y + 1 + off, p1z + 1 + off,
                        start_col, false);

            bufferSource.endLastBatch();
        }

        // Draw pin outline at the position where blocks will appear
        if (drawlines && pinActive) {
            BlockPos pinDrawPos = wand.pin.getPos();
            // Modes that offset internally (ROW_COL, GRID) place at pos+side, not pos
            if (mode.offsets_pos_internally()) {
                Direction pinSide = wand.pin.getSide();
                if (pinSide != null) {
                    pinDrawPos = pinDrawPos.relative(pinSide, 1);
                }
            }
            float ax = pinDrawPos.getX()-pos_x;
            float ay = pinDrawPos.getY()-pos_y;
            float az = pinDrawPos.getZ()-pos_z;
            VertexConsumer consumer = getVertexConsumerDebugQuads(bufferSource);

            preview_block_fat(matrix, consumer,
                    ax - off, ay - off, az - off,
                    ax + 1 + off, ay + 1 + off, az + 1 + off,
                    start_col, false);

            bufferSource.endLastBatch();
        }
    }
    /** Renders P1/P2 markers and connecting line for LINE, CIRCLE, SPHERE, FILL modes */
    static void preview_line_circle(Matrix4f matrix, Mode mode,MultiBufferSource.BufferSource bufferSource,
                                 float p1_x,
                                 float p1_y,
                                 float p1_z,
                                 float off,
                                 float off2
    )
    {
       boolean even = WandProps.getFlag(wand.wand_stack, WandProps.Flag.EVEN);
       {
            VertexConsumer consumer= getVertexConsumerDebugQuads(bufferSource);
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
                VertexConsumer consumer= getVertexConsumerDebugQuads(bufferSource);
                off = (mode == Mode.CIRCLE && even) ? -0.5f : 0.0f;
                preview_block_fat(matrix,consumer,
                        0 - off + off,
                        0 - off,
                        0 - off + off,
                        1 + off + off,
                        1 + off,
                        1 + off + off,
                        end_col, false);
                bufferSource.endLastBatch();
           }
           if(mode!=Mode.FILL) {
               VertexConsumer consumer= getVertexConsumerDebugQuads(bufferSource);
               off = (mode == Mode.CIRCLE && even) ? 0.0f : 0.5f;
               player_facing_line(consumer,matrix,
                        p1_x + off,
                        p1_y + off + 0.5f,
                        p1_z + off,
                        off,
                        off + 0.5f,
                        off,
                        line_col);
                bufferSource.endLastBatch();
           }
       }
    }
    static private VertexConsumer getVertexConsumerDebugQuads(MultiBufferSource.BufferSource bufferSource){
        //?if>=1.21.11{
            return bufferSource.getBuffer(RenderTypes.debugQuads());
        //?}else{
            /*return bufferSource.getBuffer(RenderType.debugQuads());
        *///?}
    }
    static private VertexConsumer getVertexConsumerPVBlock(MultiBufferSource.BufferSource bufferSource){
        //?if>=1.21.11{
            return bufferSource.getBuffer(RenderTypes.translucentMovingBlock());
        //?}else{
            /*return bufferSource.getBuffer(RenderType.translucentMovingBlock());
        *///?}
    }

    static private VertexConsumer getVertexConsumerDirMode(MultiBufferSource.BufferSource bufferSource){
        //?if>=1.21.11{
            return bufferSource.getBuffer(RenderTypes.entityTranslucent(GRID_TEXTURE.id()));
        //?}else{
            /*//?if>=1.21{
                return bufferSource.getBuffer(RenderType.entityTranslucent(GRID_TEXTURE.id()));
            //?}else{
                /^Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferBuilder = tesselator.getBuilder();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                RenderSystem.setShaderTexture(0, GRID_TEXTURE.id());
                return  bufferBuilder;
            ^///?}
        *///?}
    }
    static public void consumerDirMode(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float u, float v, int color, float nx, float ny, float nz, int light, int overlay) {
        //?if >=1.21{
        consumer.addVertex(matrix,x, y, z).setUv(u, v).setColor(color).setNormal(nx,ny,nz).setLight(light).setOverlay(overlay);
        //?}else{
            /*//?if >=1.21{
            consumer.vertex(matrix,x, y, z).uv(u,v).color(color).normal(nx,ny,nz).uv2(light).overlayCoords(overlay).endVertex();
            //?}else{
            /^consumer.vertex(matrix,x, y, z).uv(u,v).endVertex();
            ^///?}
        *///?}
    }

}
