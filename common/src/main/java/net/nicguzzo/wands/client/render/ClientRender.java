package net.nicguzzo.wands.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
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
import net.nicguzzo.wands.wand.PlayerWand;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.items.*;
import net.nicguzzo.wands.wand.WandProps;
import net.nicguzzo.wands.wand.WandProps.Mode;
import net.minecraft.util.RandomSource;

import java.util.List;

public class ClientRender {
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
    static PoseStack matrixStack2 = new PoseStack();
    static BlockPos.MutableBlockPos bp = new BlockPos.MutableBlockPos();
    static boolean water = false;
    static BlockState AIR = Blocks.AIR.defaultBlockState();
    static int mirroraxis=0;


    public static void render(PoseStack matrixStack,MultiBufferSource.BufferSource bufferSource) {
        client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null)
            return;

        Wand wnd= PlayerWand.get(player);
        //System.out.println("player data  "+wnd.player_data.toString());
        //if(ClientRender.wand!=null){
            ClientRender.wand.player_data=wnd.player_data;
            //ClientRender.wand.player_data=((IEntityDataSaver)player).getPersistentData();
        //}

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
        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof WandItem) {
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

                BlockHitResult block_hit = (BlockHitResult) hitResult;
                //wand.lastHitResult=block_hit;
                Rotation rot = WandProps.getRotation(stack);
                WandProps.Orientation orientation = WandProps.getOrientation(stack);
                Direction side = block_hit.getDirection();
                BlockPos pos = block_hit.getBlockPos();
                BlockState block_state = client.level.getBlockState(pos);
                if (force) {
                    wand.force_render = false;
                    if (mode == Mode.FILL || mode == Mode.LINE || mode == Mode.CIRCLE || mode == Mode.COPY|| mode == Mode.PASTE) {
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
                if (block_state != null && last_pos!=null) {
                    preview_shape = block_state.getShape(client.level, last_pos);
                }
                preview_mode(wand.mode, matrixStack,bufferSource);

            } else {
                has_target = false;
                if (wand.is_alt_pressed && (wand.copy_paste_buffer.size() > 0 || wand.block_buffer.get_length()>0) ) {
                    if (!((wand.mode == Mode.LINE || wand.mode == Mode.CIRCLE))) {
                        wand.setP1(last_pos);
                    }
                    preview_mode(wand.mode, matrixStack,bufferSource);
                }else{
                    if(wand.target_air && mode.can_target_air() ) {
                        targeting_air=true;
                        Vec3 hit=hitResult.getLocation();
                        BlockPos pos=wand.get_pos_from_air(hit);
                        ItemStack offhand = player.getOffhandItem();
                        Block offhand_block;
                        BlockState block_state = null;
                        offhand_block = Block.byItem(offhand.getItem());
                        if (offhand_block != Blocks.AIR) {
                            block_state=offhand_block.defaultBlockState();
                        }
                        if (block_state != null || mode==Mode.PASTE){
                            if(mode==Mode.TUNNEL||mode==Mode.ROW_COL||mode==Mode.GRID||mode==Mode.PASTE){
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
                        wand.block_buffer.reset();
                    }
                }
                if (water) {
                    water = false;
                }
            }
        }
    }

    private static void preview_mode(Mode mode, PoseStack matrixStack,MultiBufferSource.BufferSource bufferSource) {
        /*if (!wand.valid) {
            return;
        }*/
        //RenderSystem.clear(256, Minecraft.ON_OSX);
        //Compat.pre_render(matrixStack);
        Camera camera = client.gameRenderer.getMainCamera();

        Vec3 _c = camera.getPosition();
        float cx=(float)_c.x;
        float cy=(float)_c.y;
        float cz=(float)_c.z;
        matrixStack.pushPose();
        matrixStack.translate(-cx,-cy,-cz);

        RenderSystem.depthMask(true);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        boolean fabulous_depth_buffer = false;

        //fabulous_depth_buffer=(WandsMod.is_forge&& Minecraft.useShaderTransparency() );
        fabulous_depth_buffer = WandsMod.config.render_last && Minecraft.useShaderTransparency();

        if (Screen.hasControlDown() || fabulous_depth_buffer) {
            RenderSystem.disableDepthTest();
        } else {
            RenderSystem.enableDepthTest();
        }

        if (camera.isInitialized() && last_pos != null) {
            //float last_pos_x = last_pos.getX();
            //float last_pos_y = last_pos.getY();
            //float last_pos_z = last_pos.getZ();
            float last_pos_x = last_pos.getX()-cx;
            float last_pos_y = last_pos.getY()-cy;
            float last_pos_z = last_pos.getZ()-cz;
            float wand_x1 = wand.x1;
            float wand_y1 = wand.y1;
            float wand_z1 = wand.z1;

            float nx = 0.0f, ny = 0.0f, nz = 0.0f;

            float off2 = 0.05f;
            float off3 = off2/2;
            Compat.set_color(1.0F, 1.0F, 1.0F, 0.8f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            switch (mode) {
                case DIRECTION:
                    if (wand.valid && (preview_shape != null && !preview_shape.isEmpty())){
                        //matrixStack.pushPose();
                        //matrixStack.translate(c);
                        //matrixStack.mulPose(RenderSystem.getModelViewStack());
                        //matrixStack.translate(c.multiply(-1,-1,-1));
                        List<AABB> list = preview_shape.toAabbs();
                        if (!list.isEmpty() && wand.grid_voxel_index >= 0 && wand.grid_voxel_index < list.size()) {
                            if (fancy) {
                                RenderSystem.disableCull();
                                //RenderSystem.enableCull();
                                RenderSystem.enableBlend();
                                Compat.set_shader_pos_tex();
                                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                                Compat.set_texture(GRID_TEXTURE);
                                int vi = 0;
                                for (AABB aabb : list) {
                                    if (vi == wand.grid_voxel_index) {
                                        switch (wand.side) {
                                            case UP:
                                                x1 = last_pos_x + (float)aabb.minX;
                                                y1 = last_pos_y + (float)aabb.maxY + 0.02f;
                                                z1 = last_pos_z + (float)aabb.minZ;
                                                x2 = last_pos_x + (float)aabb.maxX;
                                                z2 = last_pos_z + (float)aabb.maxZ;
                                                Compat.addVertex_pos_uv(bufferBuilder,x1,y1,z1, 0.0f, 0.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x1,y1,z2, 0.0f, 1.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x2,y1,z2, 1.0f, 1.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x2,y1,z1, 1.0f, 0.0f);
                                                break;
                                            case DOWN:
                                                x1 = last_pos_x + (float)aabb.minX;
                                                y1 = last_pos_y + (float)aabb.minY - 0.02f;
                                                z1 = last_pos_z + (float)aabb.minZ;
                                                x2 = last_pos_x + (float)aabb.maxX;
                                                z2 = last_pos_z + (float)aabb.maxZ;
                                                Compat.addVertex_pos_uv(bufferBuilder,x1,y1,z1, 0.0f, 0.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x2,y1,z1, 1.0f, 0.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x2,y1,z2, 1.0f, 1.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x1,y1,z2, 0.0f, 1.0f);
                                                break;
                                            case SOUTH:
                                                x1 = last_pos_x + (float)aabb.minX;
                                                y1 = last_pos_y + (float)aabb.minY;
                                                z1 = last_pos_z + (float)aabb.maxZ + 0.02f;
                                                x2 = last_pos_x + (float)aabb.maxX;
                                                y2 = last_pos_y + (float)aabb.maxY;
                                                Compat.addVertex_pos_uv(bufferBuilder,x1,y1,z1,0.0f, 0.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x2,y1,z1,1.0f, 0.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x2,y2,z1,1.0f, 1.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x1,y2,z1,0.0f, 1.0f);
                                                break;
                                            case NORTH:
                                                x1 = last_pos_x + (float)aabb.minX;
                                                y1 = last_pos_y + (float)aabb.minY;
                                                z1 = last_pos_z + (float)aabb.minZ - 0.02f;
                                                x2 = last_pos_x + (float)aabb.maxX;
                                                y2 = last_pos_y + (float)aabb.maxY;
                                                Compat.addVertex_pos_uv(bufferBuilder,x1, y1, z1, 0.0f, 0.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x1, y2, z1, 0.0f, 1.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x2, y2, z1, 1.0f, 1.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x2, y1, z1, 1.0f, 0.0f);
                                                break;
                                            case EAST:
                                                x1 = last_pos_x + (float)aabb.maxX + 0.02f;
                                                y1 = last_pos_y + (float)aabb.minY;
                                                z1 = last_pos_z + (float)aabb.minZ;
                                                y2 = last_pos_y + (float)aabb.maxY;
                                                z2 = last_pos_z + (float)aabb.maxZ;
                                                Compat.addVertex_pos_uv(bufferBuilder,x1, y1, z1, 0.0f, 0.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x1, y2, z1, 1.0f, 0.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x1, y2, z2, 1.0f, 1.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x1, y1, z2, 0.0f, 1.0f);
                                                break;
                                            case WEST:
                                                x1 = last_pos_x + (float)aabb.minX - 0.02f;
                                                y1 = last_pos_y + (float)aabb.minY;
                                                z1 = last_pos_z + (float)aabb.minZ;
                                                y2 = last_pos_y + (float)aabb.maxY;
                                                z2 = last_pos_z + (float)aabb.maxZ;
                                                Compat.addVertex_pos_uv(bufferBuilder,x1, y1, z1, 0.0f, 0.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x1, y1, z2, 0.0f, 1.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x1, y2, z2, 1.0f, 1.0f);
                                                Compat.addVertex_pos_uv(bufferBuilder,x1, y2, z1, 1.0f, 0.0f);
                                                break;
                                        }
                                    }
                                    vi++;
                                }
                                tesselator.end();

                                RenderSystem.enableBlend();
                            }
                            if (!fancy || !fat_lines) {
                                Compat.set_shader_lines();
                                bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
                                int vi = 0;
                                for (AABB aabb : list) {
                                    if (vi == wand.grid_voxel_index) {
                                        grid(bufferBuilder, wand.side, last_pos_x, last_pos_y, last_pos_z, aabb);
                                    }
                                    vi++;
                                }
                                tesselator.end();
                            }
                        }
                        //matrixStack.popPose();
                    }
                case ROW_COL:
                case FILL:
                case AREA:
                case GRID:
                case LINE:
                case CIRCLE:
                case VEIN:
                case TUNNEL:
                case COPY:
                case PASTE:
                    //if ((mode == Mode.LINE || mode == Mode.CIRCLE) || wand.has_empty_bucket) {
                    if (drawlines && wand.getP1() ==null &&(mode==Mode.FILL || mode == Mode.LINE || mode == Mode.CIRCLE ||mode==Mode.COPY ||mode==Mode.PASTE||mode==Mode.ROW_COL)){
                        if (fancy && wand.offhand_state!=null){
                            random.setSeed(0);
                            //RenderSystem.enableCull();
                            //Compat.set_color(1.0f, 1.0f, 1.0f, opacity);
                            //Compat.set_shader_block();
                            //bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
                            //render_shape(matrixStack, tesselator, bufferBuilder,bufferSource, wand.offhand_state,last_pos_x,last_pos_y,last_pos_z);
                            //tesselator.end();
                            matrixStack.pushPose();
                            matrixStack.translate(last_pos_x,last_pos_y,last_pos_z);
                            blockRenderer.renderSingleBlock(wand.offhand_state,matrixStack,bufferSource,15728880, OverlayTexture.NO_OVERLAY);
                            matrixStack.popPose();
                            RenderSystem.depthMask(true);
                        }
                        if (fat_lines) {
                            //Compat.enableTexture();
                            Compat.set_shader_pos_color();
                            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

                            preview_block_fat(bufferBuilder,
                                    (last_pos_x  - off3),
                                    (last_pos_y  - off3),
                                    (last_pos_z  - off3),
                                    (last_pos_x+1+ off3),
                                    (last_pos_y+1+ off3),
                                    (last_pos_z+1+ off3),
                                    start_col,false);
                            tesselator.end();
                            //Compat.disableTexture();
                            RenderSystem.enableCull();
                        } else {
                            Compat.set_shader_lines();
                            bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
                            preview_block(bufferBuilder,
                                    last_pos_x  - off3, last_pos_y  - off3, last_pos_z  - off3,
                                    last_pos_x+1+ off3, last_pos_y+1+ off3, last_pos_z+1+ off3,
                                    start_col);
                            tesselator.end();
                        }
                    }
                    if (wand.valid || ( (mode == Mode.FILL|| mode == Mode.COPY || mode == Mode.TUNNEL)&& wand.getP1() !=null)){
                        //bbox
                        if (drawlines && fill_outlines && (mode == Mode.ROW_COL || mode == Mode.FILL || mode == Mode.COPY|| mode == Mode.TUNNEL)) {
                            if (fat_lines) {
                                //Compat.enableTexture();
                                Compat.set_shader_pos_color();
                                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                                preview_block_fat(bufferBuilder,
                                        wand.bb1_x-cx - off2,
                                        wand.bb1_y-cy - off2,
                                        wand.bb1_z-cz - off2,
                                        wand.bb2_x-cx + off2,
                                        wand.bb2_y-cy + off2,
                                        wand.bb2_z-cz + off2,
                                        bbox_col,false);
                                tesselator.end();
                               // Compat.disableTexture();
                                RenderSystem.enableCull();
                            } else {
                                Compat.set_shader_lines();
                                bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
                                preview_block(bufferBuilder,
                                        wand.bb1_x-cx - off2,
                                        wand.bb1_y-cy - off2,
                                        wand.bb1_z-cz - off2,
                                        wand.bb2_x-cx + off2,
                                        wand.bb2_y-cy + off2,
                                        wand.bb2_z-cz + off2,
                                        bbox_col);
                                tesselator.end();
                            }
                        }
                        //actual block preview

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
                                    Compat.set_shader_pos_tex();
                                    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                                    int i;
                                    RenderSystem.enableCull();
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
                                                bufferBuilder,
                                                (float) wand.block_buffer.buffer_x[idx] - cx,
                                                (float) wand.block_buffer.buffer_y[idx] - cy,
                                                (float) wand.block_buffer.buffer_z[idx] - cz,i,u0,v0,u1,v1);
                                    }
                                    tesselator.end();
                                }else {
                                     //Compat.set_shader_block();
                                     //bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
                                     for (int idx = 0; idx < block_buffer_length && idx < WandsConfig.max_limit; idx++) {
                                         if (wand.block_buffer.state[idx] != null) {
                                             //preview_shape = wand.block_buffer.state[idx].getShape(client.level, last_pos);
                                             st = wand.block_buffer.state[idx];
                                             matrixStack.pushPose();
                                             matrixStack.translate(
                                                     wand.block_buffer.buffer_x[idx],
                                                     wand.block_buffer.buffer_y[idx],
                                                     wand.block_buffer.buffer_z[idx]
                                             );
                                             blockRenderer.renderSingleBlock(st,matrixStack,bufferSource,15728880, OverlayTexture.NO_OVERLAY);
                                             matrixStack.popPose();
                                             /*render_shape(matrixStack, tesselator, bufferBuilder,bufferSource, st,
                                                    wand.block_buffer.buffer_x[idx] - cx,
                                                    wand.block_buffer.buffer_y[idx] - cy,
                                                    wand.block_buffer.buffer_z[idx] - cz);

                                             //TODO: all double blocks!!
                                             if (wand.block_buffer.state[idx].hasProperty(DoublePlantBlock.HALF)) {
                                                 render_shape(matrixStack, tesselator, bufferBuilder,bufferSource,
                                                         wand.block_buffer.state[idx].setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER),
                                                         wand.block_buffer.buffer_x[idx] - cx,
                                                         wand.block_buffer.buffer_y[idx] - cy + 1,
                                                         wand.block_buffer.buffer_z[idx] - cz);
                                             } else {
                                                 if (wand.block_buffer.state[idx].getBlock() instanceof DoorBlock) {

                                                     render_shape(matrixStack, tesselator, bufferBuilder,bufferSource,
                                                             wand.block_buffer.state[idx].setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER),
                                                             wand.block_buffer.buffer_x[idx] - cx,
                                                             wand.block_buffer.buffer_y[idx] - cy + 1,
                                                             wand.block_buffer.buffer_z[idx] - cz);
                                                 }
                                             }*/
                                         }
                                     }
                                     RenderSystem.depthMask(true);
                                     //tesselator.end();
                                 }
                            }
                            if (block_buffer_length >0){
                                render_mode_outline(tesselator,bufferBuilder, cx,cy,cz);
                            }
                        }
                        BlockPos p1=wand.getP1();
                        if (drawlines && p1 != null  && (mode == Mode.FILL|| mode == Mode.LINE || mode == Mode.CIRCLE)) {
                            if (fat_lines) {
                                boolean even = WandProps.getFlag(wand.wand_stack, WandProps.Flag.EVEN);
                                float off = (mode == Mode.CIRCLE && even) ? -1.0f : 0.0f;

                                //Compat.enableTexture();
                                {
                                    Compat.set_shader_pos_color();
                                    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                                    preview_block_fat(bufferBuilder,
                                            p1.getX()-cx - off3,
                                            p1.getY()-cy - off3,
                                            p1.getZ()-cz - off3,
                                            p1.getX()-cx + 1 + off3,
                                            p1.getY()-cy + 1 + off3,
                                            p1.getZ()-cz + 1 + off3,
                                            start_col, false
                                    );
                                    tesselator.end();
                                }
                                if (has_target) {
                                    {
                                        Compat.set_shader_pos_color();
                                        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                                        off = (mode == Mode.CIRCLE && even) ? -0.5f : 0.0f;
                                        //off=(mode == Mode.CIRCLE && even_circle)? 1.0f:0.0f;
                                        preview_block_fat(bufferBuilder,
                                                last_pos_x - off3 + off,
                                                last_pos_y - off3,
                                                last_pos_z - off3 + off,
                                                last_pos_x + 1 + off3 + off,
                                                last_pos_y + 1 + off3,
                                                last_pos_z + 1 + off3 + off,
                                                end_col, false);
                                       tesselator.end();
                                    }
                                    RenderSystem.disableDepthTest();
                                    if(mode!=Mode.FILL) {
                                        Compat.set_shader_pos_color();
                                        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                                        RenderSystem.disableCull();
                                        off = (mode == Mode.CIRCLE && even) ? 0.0f : 0.5f;
                                        player_facing_line(bufferBuilder,
                                                cx,cy,cz,
                                                p1.getX()-cx + off,
                                                p1.getY()-cy + off + 0.5f,
                                                p1.getZ()-cz + off,
                                                last_pos_x + off,
                                                last_pos_y + off + 0.5f,
                                                last_pos_z + off,
                                                line_col);
                                        tesselator.end();
                                    }
                                }
                                RenderSystem.enableDepthTest();
                                RenderSystem.enableCull();
                            } else {
                                Compat.set_shader_lines();
                                bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
                                    bufferBuilder
                                            .vertex(last_pos_x + 0.5F, last_pos_y + 0.5F, last_pos_z + 0.5F)
                                            .color(line_col.r, line_col.g, line_col.b, line_col.a).endVertex();
                                    bufferBuilder
                                            .vertex(wand_x1 + 0.5F, wand_y1 + 0.5F, wand_z1 + 0.5F)
                                            .color(line_col.r, line_col.g, line_col.b, line_col.a).endVertex();
                                RenderSystem.disableDepthTest();
                                preview_block(bufferBuilder,
                                        wand.getP1().getX()-cx,
                                        wand.getP1().getY()-cy,
                                        wand.getP1().getZ()-cz,
                                        wand.getP1().getX()-cx + 1,
                                        wand.getP1().getY()-cy + 1,
                                        wand.getP1().getZ()-cz + 1,
                                        start_col);
                                preview_block(bufferBuilder,
                                        last_pos_x - off2,
                                        last_pos_y - off2,
                                        last_pos_z - off2,
                                        last_pos_x + 1 + off2,
                                        last_pos_y + 1 + off2,
                                        last_pos_z + 1 + off2,
                                        end_col);
                                RenderSystem.enableDepthTest();
                                tesselator.end();
                            }
                        }
                    }
                    break;

            }

            if (mode==Mode.PASTE && wand.copy_paste_buffer.size() > 0) {
                int mx=1;
                //int my=1;
                int mz=1;
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
                    BlockPos po=wand.copy_paste_buffer.get(0).pos;

                    for (CopyBuffer b : wand.copy_paste_buffer) {
                        BlockState st =b.state;
                        if (wand.palette.has_palette) {
                            st = wand.get_state();
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

                        matrixStack.pushPose();
                        matrixStack.translate( px,py,pz);
                        blockRenderer.renderSingleBlock(st,matrixStack,bufferSource,15728880, OverlayTexture.NO_OVERLAY);
                        matrixStack.popPose();
                    }
                }
                if (drawlines && paste_outlines) {
                    Colorf c=(wand.destroy? destroy_col: paste_bb_col);
                    x1 = Integer.MAX_VALUE;
                    y1 = Integer.MAX_VALUE;
                    z1 = Integer.MAX_VALUE;
                    x2 = Integer.MIN_VALUE;
                    y2 = Integer.MIN_VALUE;
                    z2 = Integer.MIN_VALUE;

                    if (fat_lines) {
                        Compat.set_shader_pos_color();
                        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                    } else {
                        Compat.set_shader_lines();
                        bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
                    }
                    for (CopyBuffer b : wand.copy_paste_buffer) {
                        BlockPos p = b.pos.rotate(last_rot);
                        float x = b_pos.getX()-cx + p.getX()*mx;
                        float y = b_pos.getY()-cy + p.getY();
                        float z = b_pos.getZ()-cz + p.getZ()*mz;
                        if (fat_lines) {
                            preview_block_fat(bufferBuilder,
                                    x, y, z,
                                    x + 1, y + 1, z + 1, c,
                            true);
                        } else {
                            preview_block(bufferBuilder,
                                    x, y, z,
                                    x + 1, y + 1, z + 1, c
                            );
                        }
                        if (x < x1) x1 = x;
                        if (y < y1) y1 = y;
                        if (z < z1) z1 = z;
                        if (x + 1 > x2) x2 = x + 1;
                        if (y + 1 > y2) y2 = y + 1;
                        if (z + 1 > z2) z2 = z + 1;
                    }
                    tesselator.end();
                    if (fat_lines) {
                        Compat.set_shader_pos_color();
                        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                    } else {
                        RenderSystem.enableCull();
                        Compat.set_shader_lines();
                        bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
                    }
                    if (fat_lines) {
                        preview_block_fat(bufferBuilder,
                                x1-cx,
                                y1-cy,
                                z1-cz,
                                x2-cx,
                                y2-cy,
                                z2-cz,
                                c,false);
                    } else {
                        preview_block(bufferBuilder,
                                x1-cx,
                                y1-cy,
                                z1-cz,
                                x2-cx,
                                y2-cy,
                                z2-cz,
                                c);
                    }
                    tesselator.end();
                }
            }
        }
        Compat.set_color(1.0F, 1.0F, 1.0F, 1.0f);
        matrixStack.popPose();
        RenderSystem.depthMask(true);
    }
    public static void render_mode_outline( Tesselator tesselator,BufferBuilder bufferBuilder,float cx,float cy, float cz){
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

            if (fat_lines) {
                Compat.set_shader_pos_color();
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            } else {
                RenderSystem.enableCull();
                Compat.set_shader_lines();
                bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
            }

            for (int idx = 0; idx < wand.block_buffer.get_length() && idx < WandsConfig.max_limit; idx++) {
                float x = wand.block_buffer.buffer_x[idx]-cx;
                float y = wand.block_buffer.buffer_y[idx]-cy;
                float z = wand.block_buffer.buffer_z[idx]-cz;

                if (wand.block_buffer.state[idx] != null) {
                    preview_shape = wand.block_buffer.state[idx].getShape(client.level, last_pos);
                    List<AABB> list = preview_shape.toAabbs();
                    for (AABB aabb : list) {
                        if (fat_lines) {
                            preview_block_fat(bufferBuilder,
                                    x + (float) aabb.minX, y + (float) aabb.minY, z + (float) aabb.minZ,
                                    x + (float) aabb.maxX, y + (float) aabb.maxY, z + (float) aabb.maxZ,
                                    mode_outline_color,wand.destroy);
                        } else {
                            preview_block(bufferBuilder,
                                    x + (float)aabb.minX, y + (float)aabb.minY, z + (float)aabb.minZ,
                                    x + (float)aabb.maxX, y + (float)aabb.maxY, z + (float)aabb.maxZ,
                                    mode_outline_color);
                        }
                    }
                }
            }
            tesselator.end();
        }
    }
    static void preview_block(BufferBuilder bufferBuilder,float fx1, float fy1, float fz1, float fx2, float fy2, float fz2,Colorf c) {
        fx1 += p_o;
        fy1 += p_o;
        fz1 += p_o;
        fx2 -= p_o;
        fy2 -= p_o;
        fz2 -= p_o;
        bufferBuilder.vertex(fx1, fy1, fz1).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz1).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx1, fy1, fz1).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx1, fy1, fz2).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx1, fy1, fz2).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz2).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz1).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz2).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz1).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz1).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz1).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz2).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz2).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz2).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz1).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz2).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx1, fy1, fz1).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz1).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz1).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz1).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx1, fy1, fz2).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx1, fy2, fz2).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx2, fy1, fz2).color(c.r,c.g,c.b,c.a).endVertex();
        bufferBuilder.vertex(fx2, fy2, fz2).color(c.r,c.g,c.b,c.a).endVertex();
    }

    static void preview_block_fat(BufferBuilder bufferBuilder,float fx1, float fy1, float fz1, float fx2, float fy2, float fz2,Colorf c,boolean cross) {
        float off=0.01f;
        fx1 -= off;
        fy1 -= off;
        fz1 -= off;
        fx2 += off;
        fy2 += off;
        fz2 += off;
        Compat.set_color(c.r,c.g,c.b,c.a);
        //Compat.set_texture(LINE_TEXTURE);
        float w=fat_lines_width;
        //north -z
        quad_line(bufferBuilder,  0, w,0, fx1,   fy1, fz1, fx2,   fy1, fz1,c);
        quad_line(bufferBuilder,  0,-w,0, fx2,   fy2, fz1, fx1,   fy2, fz1,c);
        quad_line(bufferBuilder,  w, 0,0, fx1, fy2-w, fz1, fx1, fy1+w, fz1,c);
        quad_line(bufferBuilder, -w, 0,0, fx2, fy1+w, fz1, fx2, fy2-w, fz1,c);
        if(cross) {
            quad_line(bufferBuilder, -w, 0, 0,fx1+w, fy1, fz1,   fx2, fy2, fz1, c);
            quad_line(bufferBuilder,  w, 0, 0,  fx1, fy2, fz1, fx2-w, fy1, fz1, c);
        }
        //south +z
        quad_line(bufferBuilder,  0, w,0, fx2,   fy1, fz2, fx1,   fy1, fz2,c);
        quad_line(bufferBuilder,  0,-w,0, fx1,   fy2, fz2, fx2,   fy2, fz2,c);
        quad_line(bufferBuilder,  w, 0,0, fx1, fy1+w, fz2, fx1, fy2-w, fz2,c);
        quad_line(bufferBuilder, -w, 0,0, fx2, fy2-w, fz2, fx2, fy1+w, fz2,c);
        if(cross) {
            quad_line(bufferBuilder,  w, 0, 0,   fx1, fy1, fz2, fx2-w, fy2, fz2, c);
            quad_line(bufferBuilder, -w, 0, 0, fx1+w, fy2, fz2,   fx2, fy1, fz2, c);
        }
        //up +y
        quad_line(bufferBuilder,  w,0, 0, fx1  , fy2, fz2, fx1 , fy2, fz1,c);
        quad_line(bufferBuilder, -w,0, 0, fx2  , fy2, fz1, fx2 , fy2, fz2,c);
        quad_line(bufferBuilder,  0,0, w, fx1+w, fy2, fz1, fx2-w, fy2, fz1,c);
        quad_line(bufferBuilder,  0,0,-w, fx2-w, fy2, fz2, fx1+w, fy2, fz2,c);
        if(cross) {
            quad_line(bufferBuilder, -w, 0, 0,fx1+w, fy2, fz1,fx2, fy2, fz2, c);
            quad_line(bufferBuilder,  w, 0, 0,fx1, fy2, fz2,fx2-w, fy2, fz1, c);
        }
        //down -y
        quad_line(bufferBuilder,  w,0, 0, fx1, fy1, fz1, fx1  , fy1, fz2,c);
        quad_line(bufferBuilder, -w,0, 0, fx2  , fy1, fz2,fx2, fy1, fz1,c);
        quad_line(bufferBuilder,  0,0, w, fx2-w, fy1, fz1,fx1+w, fy1, fz1,c);
        quad_line(bufferBuilder,  0,0,-w, fx1+w, fy1, fz2,  fx2-w, fy1, fz2,c);
        if(cross) {
            quad_line(bufferBuilder,  w, 0, 0,fx1, fy1, fz1,fx2-w, fy1, fz2, c);
            quad_line(bufferBuilder, -w, 0, 0,fx1+w, fy1, fz2,fx2, fy1, fz1, c);
        }
        //east +x
        quad_line(bufferBuilder, 0, w, 0, fx2,   fy1, fz1, fx2,   fy1, fz2,c);
        quad_line(bufferBuilder, 0,-w, 0, fx2,   fy2, fz2, fx2,   fy2, fz1,c);
        quad_line(bufferBuilder, 0, 0, w, fx2, fy2-w, fz1, fx2, fy1+w, fz1,c);
        quad_line(bufferBuilder, 0, 0,-w, fx2, fy1+w, fz2, fx2, fy2-w, fz2,c);
        if(cross) {
            quad_line(bufferBuilder, 0, 0, w,fx1, fy1, fz1,fx1, fy2, fz2-w, c);
            quad_line(bufferBuilder, 0, 0, w,fx1, fy1, fz2-w,fx1, fy2, fz1, c);
        }
        //west -x
        quad_line(bufferBuilder, 0, w,0,   fx1,   fy1, fz2,fx1,   fy1, fz1,c);
        quad_line(bufferBuilder, 0,-w,0, fx1,   fy2, fz1,  fx1,   fy2, fz2,c);
        quad_line(bufferBuilder, 0,0, w, fx1, fy1+w, fz1,  fx1, fy2-w, fz1,c);
        quad_line(bufferBuilder, 0,0,-w,   fx1, fy2-w, fz2,fx1, fy1+w, fz2,c);
        if(cross) {
            quad_line(bufferBuilder, 0, 0, -w,fx2, fy1, fz1+w,fx2, fy2, fz2, c);
            quad_line(bufferBuilder, 0, 0, -w,fx2, fy1, fz2,fx2, fy2, fz1+w, c);
        }
    }

    private static void quad_line(BufferBuilder bufferBuilder,
                                  float wx,float wy,float wz,
                                  float lx1, float ly1,float lz1,
                                  float lx2, float ly2,float lz2,
                                  Colorf c){

        Compat.addVertex_pos_color(bufferBuilder,   lx1,    ly1,    lz1,c);
        Compat.addVertex_pos_color(bufferBuilder,lx1+wx, ly1+wy, lz1+wz,c);
        Compat.addVertex_pos_color(bufferBuilder,lx2+wx, ly2+wy, lz2+wz,c);
        Compat.addVertex_pos_color(bufferBuilder,   lx2,    ly2,    lz2,c);
    }

    private static void player_facing_line(BufferBuilder bufferBuilder,float cx,float cy,float cz,float lx1, float ly1,float lz1,float lx2, float ly2,float lz2,Colorf c){

        float w=0.05f;

        float p1x=cx-lx1;
        float p1y=cy-ly1;
        float p1z=cz-lz1;

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
        Compat.set_color(c.r,c.g,c.b,c.a);
        Compat.addVertex_pos_color(bufferBuilder,lx1-nx, ly1-ny, lz1-nz,c);
        Compat.addVertex_pos_color(bufferBuilder,lx1+nx, ly1+ny, lz1+nz,c);
        Compat.addVertex_pos_color(bufferBuilder,lx2+nx, ly2+ny, lz2+nz,c);
        Compat.addVertex_pos_color(bufferBuilder,lx2-nx, ly2-ny, lz2-nz,c);

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
    private static void draw_lines(BufferBuilder bufferBuilder,int from,int to,float r,float g,float b,float a){
        for(int i=from;i<to && i< grid_n;i++) {
            bufferBuilder.vertex(grid_vx[i],grid_vy[i],grid_vz[i]).color(r, g, b, a);
        }
    }
    private static void grid(BufferBuilder bufferBuilder,Direction side, float x, float y, float z,AABB aabb) {
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

                draw_lines(bufferBuilder,0,16,1.0f,1.0f,1.0f,1.0f);

                grid_i=0;

                add_grid_line(x + w*0.40f, y, z + h*0.20f, x + w*0.50f, y, z + h*0.05f);
                add_grid_line(x + w*0.60f, y, z + h*0.20f, x + w*0.50f, y, z + h*0.05f);
                add_grid_line(x + w*0.40f, y, z + h*0.80f, x + w*0.50f, y, z + h*0.95f);
                add_grid_line(x + w*0.60f, y, z + h*0.80f, x + w*0.50f, y, z + h*0.95f);
                add_grid_line(x + w*0.20f, y, z + h*0.40f, x + w*0.05f, y, z + h*0.50f);
                add_grid_line(x + w*0.20f, y, z + h*0.60f, x + w*0.05f, y, z + h*0.50f);
                add_grid_line(x + w*0.80f, y, z + h*0.40f, x + w*0.95f, y, z + h*0.50f);
                add_grid_line(x + w*0.80f, y, z + h*0.60f, x + w*0.95f, y, z + h*0.50f);
                draw_lines(bufferBuilder,0,16,0.7f,0,0,1.0f);

                grid_i=0;
                add_grid_line(x + w*0.40f, y, z + h*0.50f,x + w*0.50f, y, z + h*0.40f);
                add_grid_line(x + w*0.40f, y, z + h*0.50f,x + w*0.50f, y, z + h*0.60f);
                add_grid_line(x + w*0.60f, y, z + h*0.50f,x + w*0.50f, y, z + h*0.60f);
                add_grid_line(x + w*0.50f, y, z + h*0.40f,x + w*0.60f, y, z + h*0.50f);
                draw_lines(bufferBuilder,0,8,0,0.7f,0,1.0f);

                grid_i=0;
                add_grid_line(x + w*0.10f, y, z + h*0.10f,x + w*0.20f, y, z + h*0.14f);
                add_grid_line(x + w*0.10f, y, z + h*0.10f,x + w*0.14f, y, z + h*0.20f);
                add_grid_line(x + w*0.90f, y, z + h*0.90f,x + w*0.80f, y, z + h*0.86f);
                add_grid_line(x + w*0.90f, y, z + h*0.90f,x + w*0.86f, y, z + h*0.80f);
                add_grid_line(x + w*0.90f, y, z + h*0.10f,x + w*0.80f, y, z + h*0.14f);
                add_grid_line(x + w*0.90f, y, z + h*0.10f,x + w*0.86f, y, z + h*0.20f);
                add_grid_line(x + w*0.10f, y, z + h*0.90f,x + w*0.20f, y, z + h*0.86f);
                add_grid_line(x + w*0.10f, y, z + h*0.90f,x + w*0.14f, y, z + h*0.80f);
                draw_lines(bufferBuilder,0,16,0,0,0.7f,1.0f);

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

                draw_lines(bufferBuilder,0,16,1.0f,1.0f,1.0f,1.0f);

                grid_i=0;

                add_grid_line(x + w*0.40f, y + h*0.20f, z, x + w*0.50f, y + h*0.05f,z);
                add_grid_line(x + w*0.60f, y + h*0.20f, z, x + w*0.50f, y + h*0.05f,z);
                add_grid_line(x + w*0.40f, y + h*0.80f, z, x + w*0.50f, y + h*0.95f,z);
                add_grid_line(x + w*0.60f, y + h*0.80f, z, x + w*0.50f, y + h*0.95f,z);
                add_grid_line(x + w*0.20f, y + h*0.40f, z, x + w*0.05f, y + h*0.50f,z);
                add_grid_line(x + w*0.20f, y + h*0.60f, z, x + w*0.05f, y + h*0.50f,z);
                add_grid_line(x + w*0.80f, y + h*0.40f, z, x + w*0.95f, y + h*0.50f,z);
                add_grid_line(x + w*0.80f, y + h*0.60f, z, x + w*0.95f, y + h*0.50f,z);
                draw_lines(bufferBuilder,0,16,0.7f,0,0,1.0f);

                grid_i=0;
                add_grid_line(x + w*0.40f, y + h*0.50f,z, x + w*0.50f, y + h*0.40f, z);
                add_grid_line(x + w*0.40f, y + h*0.50f,z, x + w*0.50f, y + h*0.60f, z);
                add_grid_line(x + w*0.60f, y + h*0.50f,z, x + w*0.50f, y + h*0.60f, z);
                add_grid_line(x + w*0.50f, y + h*0.40f,z, x + w*0.60f, y + h*0.50f, z);
                draw_lines(bufferBuilder,0,8,0,0.7f,0,1.0f);

                grid_i=0;
                add_grid_line(x + w*0.10f, y + h*0.10f,z, x + w*0.20f, y + h*0.14f , z);
                add_grid_line(x + w*0.10f, y + h*0.10f,z, x + w*0.14f, y + h*0.20f , z);
                add_grid_line(x + w*0.90f, y + h*0.90f,z, x + w*0.80f, y + h*0.86f , z);
                add_grid_line(x + w*0.90f, y + h*0.90f,z, x + w*0.86f, y + h*0.80f , z);
                add_grid_line(x + w*0.90f, y + h*0.10f,z, x + w*0.80f, y + h*0.14f , z);
                add_grid_line(x + w*0.90f, y + h*0.10f,z, x + w*0.86f, y + h*0.20f , z);
                add_grid_line(x + w*0.10f, y + h*0.90f,z, x + w*0.20f, y + h*0.86f , z);
                add_grid_line(x + w*0.10f, y + h*0.90f,z, x + w*0.14f, y + h*0.80f , z);
                draw_lines(bufferBuilder,0,16,0,0,0.7f,1.0f);
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

                draw_lines(bufferBuilder,0,16,1.0f,1.0f,1.0f,1.0f);

                grid_i=0;

                add_grid_line(x, y + w*0.40f, z + h*0.20f, x, y + w*0.50f, z + h*0.05f);
                add_grid_line(x, y + w*0.60f, z + h*0.20f, x, y + w*0.50f, z + h*0.05f);
                add_grid_line(x, y + w*0.40f, z + h*0.80f, x, y + w*0.50f, z + h*0.95f);
                add_grid_line(x, y + w*0.60f, z + h*0.80f, x, y + w*0.50f, z + h*0.95f);
                add_grid_line(x, y + w*0.20f, z + h*0.40f, x, y + w*0.05f, z + h*0.50f);
                add_grid_line(x, y + w*0.20f, z + h*0.60f, x, y + w*0.05f, z + h*0.50f);
                add_grid_line(x, y + w*0.80f, z + h*0.40f, x, y + w*0.95f, z + h*0.50f);
                add_grid_line(x, y + w*0.80f, z + h*0.60f, x, y + w*0.95f, z + h*0.50f);
                draw_lines(bufferBuilder,0,16,0.7f,0,0,1.0f);

                grid_i=0;
                add_grid_line(x,y + w*0.40f, z + h*0.50f,x, y + w*0.50f, z + h*0.40f);
                add_grid_line(x,y + w*0.40f, z + h*0.50f,x, y + w*0.50f, z + h*0.60f);
                add_grid_line(x,y + w*0.60f, z + h*0.50f,x, y + w*0.50f, z + h*0.60f);
                add_grid_line(x,y + w*0.50f, z + h*0.40f,x, y + w*0.60f, z + h*0.50f);
                draw_lines(bufferBuilder,0,8,0,0.7f,0,1.0f);

                grid_i=0;
                add_grid_line(x, y + w*0.10f, z + h*0.10f,x, y + w*0.20f, z + h*0.14f);
                add_grid_line(x, y + w*0.10f, z + h*0.10f,x, y + w*0.14f, z + h*0.20f);
                add_grid_line(x, y + w*0.90f, z + h*0.90f,x, y + w*0.80f, z + h*0.86f);
                add_grid_line(x, y + w*0.90f, z + h*0.90f,x, y + w*0.86f, z + h*0.80f);
                add_grid_line(x, y + w*0.90f, z + h*0.10f,x, y + w*0.80f, z + h*0.14f);
                add_grid_line(x, y + w*0.90f, z + h*0.10f,x, y + w*0.86f, z + h*0.20f);
                add_grid_line(x, y + w*0.10f, z + h*0.90f,x, y + w*0.20f, z + h*0.86f);
                add_grid_line(x, y + w*0.10f, z + h*0.90f,x, y + w*0.14f, z + h*0.80f);
                draw_lines(bufferBuilder,0,16,0,0,0.7f,1.0f);

            }
            break;
        }
        //WandsMod.log("x: "+x+" y: "+y+" z: "+z,prnt);

    }
    static void render_fluid(BufferBuilder bufferBuilder,float x, float y,float z,int color,float u1,float v1,float u0,float v0) {
        float h = 0.875f;
        float o = 0.1f;
        //up
        bufferBuilder.vertex(x + o    , y + h - o, z + o).uv(u1, v1).color(color).endVertex();
        bufferBuilder.vertex(x + o    , y + h - o, z + 1 - o).uv(u1, v0).color(color).endVertex();
        bufferBuilder.vertex(x + 1 - o, y + h - o, z + 1 - o).uv(u0, v0).color(color).endVertex();
        bufferBuilder.vertex(x + 1 - o, y + h - o, z + o).uv(u0, v1).color(color).endVertex();
        //down
        bufferBuilder.vertex(x + o    , y + o, z + o).uv(u1, v1).color(color).endVertex();
        bufferBuilder.vertex(x + 1 - o, y + o, z + o).uv(u0, v1).color(color).endVertex();
        bufferBuilder.vertex(x + 1 - o, y + o, z + 1 - o).uv(u0, v0).color(color).endVertex();
        bufferBuilder.vertex(x + o    , y + o, z + 1 - o).uv(u1, v0).color(color).endVertex();
        //north -z
        bufferBuilder.vertex(x + o, y + o, z + o).uv(u1, v1).color(color).endVertex();
        bufferBuilder.vertex(x + o, y + h - o, z + o).uv(u1, v0).color(color).endVertex();
        bufferBuilder.vertex(x + 1 - o, y + h - o, z + o).uv(u0, v0).color(color).endVertex();
        bufferBuilder.vertex(x + 1 - o, y + o, z + o).uv(u0, v1).color(color).endVertex();
        //south +z
        bufferBuilder.vertex(x + o, y + o, z + 1 - o).uv(u1, v1).color(color).endVertex();
        bufferBuilder.vertex(x + 1 - o, y + o, z + 1 - o).uv(u0, v1).color(color).endVertex();
        bufferBuilder.vertex(x + 1 - o, y + h - o, z + 1 - o).uv(u0, v0).color(color).endVertex();
        bufferBuilder.vertex(x + o, y + h - o, z + 1 - o).uv(u1, v0).color(color).endVertex();
        //east
        bufferBuilder.vertex(x + o, y + o, z + o).uv(u0, v1).color(color).endVertex();
        bufferBuilder.vertex(x + o, y + o, z + 1 - o).uv(u1, v1).color(color).endVertex();
        bufferBuilder.vertex(x + o, y + h - o, z + 1 - o).uv(u1, v0).color(color).endVertex();
        bufferBuilder.vertex(x + o, y + h - o, z + o).uv(u0, v0).color(color).endVertex();
        //weast
        bufferBuilder.vertex(x + 1 - o, y + o, z + o).uv(u0, v1).color(color).endVertex();
        bufferBuilder.vertex(x + 1 - o, y + h - o, z + o).uv(u0, v0).color(color).endVertex();
        bufferBuilder.vertex(x + 1 - o, y + h - o, z + 1 - o).uv(u1, v0).color(color).endVertex();
        bufferBuilder.vertex(x + 1 - o, y + o, z + 1 - o).uv(u1, v1).color(color).endVertex();
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
}

