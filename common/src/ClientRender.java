package net.nicguzzo.wands;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nicguzzo.wands.Wand.CopyPasteBuffer;
import net.nicguzzo.wands.WandItem.Mode;
import net.nicguzzo.wands.mcver.MCVer;

import java.util.List;
import java.util.Random;

#if MC>="1190"
import net.minecraft.util.RandomSource;
#endif

import me.shedaniel.math.Color;

public class ClientRender {
    public static final float p_o = -0.005f;// preview_block offset
    private static long t0 = 0;
    private static long t1 = 0;
    private static long t00 = 0;
    private static boolean prnt;
    //public static Vec3 c=new Vec3(0,0,0);
    static BlockPos last_pos = null;
    static Direction last_side = null;
    static Mode last_mode;
    static Rotation last_rot = Rotation.NONE;
    static boolean last_alt = false;
    //static int last_y=0;
    static int last_buffer_size = -1;
    static WandItem.Orientation last_orientation = null;
    //private static boolean last_valid =false;
    public static Wand wand = new Wand();
    static VoxelShape preview_shape = null;

    static AABB def_aabb = new AABB(0, 0, 0, 1, 1, 1);
    static private final int grid_n = 16;
    static private int grid_i = 0;
    static private final double[] grid_vx = new double[grid_n];
    static private final double[] grid_vy = new double[grid_n];
    static private final double[] grid_vz = new double[grid_n];
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
    private static final ResourceLocation GRID_TEXTURE = new ResourceLocation("wands", "textures/blocks/grid.png");
    private static final ResourceLocation LINE_TEXTURE = new ResourceLocation("wands", "textures/blocks/line.png");

    #if MC>="1190"
    static public RandomSource random = RandomSource.create();
    #else
    static public Random random = new Random();
    #endif
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

    public static void render(PoseStack matrixStack) {
        client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null)
            return;
        if (client.options.getCameraType() != CameraType.FIRST_PERSON) {
            return;
        }
        if (update_colors) {
            update_colors = false;
            WandsConfig.get_instance().parse_colors();
            update_colors();
        }
        drawlines = !WandsMod.config.no_lines;
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
            Mode mode = WandItem.getMode(stack);
            mirroraxis=WandItem.getVal(player.getMainHandItem(), WandItem.Value.MIRRORAXIS);
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && !wand.is_alt_pressed) {
                has_target = true;

                wand.lastPlayerDirection=player.getDirection();
                //wand.digger_item_slot=-1;
                BlockHitResult block_hit = (BlockHitResult) hitResult;
                wand.lastHitResult=block_hit;
                Rotation rot = WandItem.getRotation(stack);
                WandItem.Orientation orientation = WandItem.getOrientation(stack);
                Direction side = block_hit.getDirection();
                BlockPos pos = block_hit.getBlockPos();
                BlockState block_state = client.level.getBlockState(pos);
                if (force) {

                    wand.force_render = false;
                    if (mode == Mode.FILL || mode == Mode.LINE || mode == Mode.CIRCLE || mode == Mode.COPY || mode == Mode.RECT) {
                        if (WandItem.getFlag(stack, WandItem.Flag.INCSELBLOCK)) {
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

                    wand.do_or_preview(player, player.level, block_state, pos, side, block_hit.getLocation(), stack, prnt);
                }
                preview_shape = null;
                if (block_state != null && last_pos!=null) {
                    preview_shape = block_state.getShape(client.level, last_pos);
                }
                preview_mode(wand.mode, matrixStack, block_state);

            } else {
                has_target = false;
                /*if (((wand.mode == Mode.LINE || wand.mode == Mode.CIRCLE) && wand.p1 != null) ||
                        (wand.mode == WandItem.Mode.PASTE && wand.copy_paste_buffer.size() != 0 && wand.is_alt_pressed)) {*/
                if (wand.is_alt_pressed && (wand.copy_paste_buffer.size() > 0 || wand.block_buffer.get_length()>0) ) {
                    if (!((wand.mode == Mode.LINE || wand.mode == Mode.CIRCLE))) {
                        wand.p1 = last_pos;
                    }
                    //WandsMod.log("alt pv "+wand.block_buffer.get_length(),prnt);
                    //WandsMod.log("p1 "+wand.p1,prnt);
                    preview_mode(wand.mode, matrixStack, null);
                }else{
                    wand.block_buffer.reset();
                    //wand.p1=null;
                }
                if (water) {
                    water = false;
                }
            }
        }
    }

    private static void preview_mode(Mode mode, PoseStack matrixStack, BlockState state) {
        //RenderSystem.clear(256, Minecraft.ON_OSX);
        MCVer.inst.pre_render(matrixStack);
        Camera camera = client.gameRenderer.getMainCamera();
        RenderSystem.depthMask(true);
        Tesselator tesselator = Tesselator.getInstance();

        BufferBuilder bufferBuilder = tesselator.getBuilder();
        boolean fabulous_depth_buffer = false;

        //fabulous_depth_buffer=(WandsMod.is_forge&& Minecraft.useShaderTransparency() );
        fabulous_depth_buffer = WandsMod.config.render_last && Minecraft.useShaderTransparency();

        if (Screen.hasControlDown() || fabulous_depth_buffer) {
            RenderSystem.disableDepthTest();
        } else {
            RenderSystem.enableDepthTest();
        }

        if (camera.isInitialized() && last_pos != null) {
            float last_pos_x = last_pos.getX();
            float last_pos_y = last_pos.getY();
            float last_pos_z = last_pos.getZ();
            float wand_x1 = wand.x1;
            float wand_y1 = wand.y1;
            float wand_z1 = wand.z1;

            float nx = 0.0f, ny = 0.0f, nz = 0.0f;

            float off2 = 0.05f;
            MCVer.inst.set_color(1.0F, 1.0F, 1.0F, 0.8f);
            RenderSystem.lineWidth(1.0f);
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            switch (mode) {
                case DIRECTION:
                    if (wand.valid) {
                        if (preview_shape != null && !preview_shape.isEmpty()) {
                            List<AABB> list = preview_shape.toAabbs();
                            if (!list.isEmpty() && wand.grid_voxel_index >= 0 && wand.grid_voxel_index < list.size()) {
                                if (fancy) {
                                    RenderSystem.enableTexture();
                                    RenderSystem.disableCull();
                                    RenderSystem.enableCull();
                                    RenderSystem.enableBlend();

                                    MCVer.inst.set_render_quads_block(bufferBuilder);
                                    MCVer.inst.set_texture(GRID_TEXTURE);
                                    int vi = 0;
                                    for (AABB aabb : list) {
                                        if (vi == wand.grid_voxel_index) {
                                            nx = wand.side.getNormal().getX();
                                            ny = wand.side.getNormal().getY();
                                            nz = wand.side.getNormal().getZ();
                                            switch (wand.side) {
                                                case UP:
                                                    x1 = last_pos_x + (float) aabb.minX;
                                                    y1 = last_pos_y + (float) aabb.maxY + 0.02f;
                                                    z1 = last_pos_z + (float) aabb.minZ;
                                                    x2 = last_pos_x + (float) aabb.maxX;
                                                    z2 = last_pos_z + (float) aabb.maxZ;

                                                    bufferBuilder.vertex(x1, y1, z1, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x1, y1, z2, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x2, y1, z2, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x2, y1, z1, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0, 0, nx, ny, nz);
                                                    break;
                                                case DOWN:
                                                    x1 = last_pos_x + (float) aabb.minX;
                                                    y1 = last_pos_y + (float) aabb.minY - 0.02f;
                                                    z1 = last_pos_z + (float) aabb.minZ;
                                                    x2 = last_pos_x + (float) aabb.maxX;
                                                    z2 = last_pos_z + (float) aabb.maxZ;

                                                    bufferBuilder.vertex(x1, y1, z1, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x2, y1, z1, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x2, y1, z2, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x1, y1, z2, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0, 0, nx, ny, nz);
                                                    break;
                                                case SOUTH:
                                                    x1 = last_pos_x + (float) aabb.minX;
                                                    y1 = last_pos_y + (float) aabb.minY;
                                                    z1 = last_pos_z + (float) aabb.maxZ + 0.02f;
                                                    x2 = last_pos_x + (float) aabb.maxX;
                                                    y2 = last_pos_y + (float) aabb.maxY;

                                                    bufferBuilder.vertex(x1, y1, z1, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x2, y1, z1, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x2, y2, z1, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x1, y2, z1, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0, 0, nx, ny, nz);

                                                    break;
                                                case NORTH:
                                                    x1 = last_pos_x + (float) aabb.minX;
                                                    y1 = last_pos_y + (float) aabb.minY;
                                                    z1 = last_pos_z + (float) aabb.minZ - 0.02f;
                                                    x2 = last_pos_x + (float) aabb.maxX;
                                                    y2 = last_pos_y + (float) aabb.maxY;

                                                    bufferBuilder.vertex(x1, y1, z1, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x1, y2, z1, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x2, y2, z1, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x2, y1, z1, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0, 0, nx, ny, nz);

                                                    break;
                                                case EAST:
                                                    x1 = last_pos_x + (float) aabb.maxX + 0.02f;
                                                    y1 = last_pos_y + (float) aabb.minY;
                                                    z1 = last_pos_z + (float) aabb.minZ;
                                                    y2 = last_pos_y + (float) aabb.maxY;
                                                    z2 = last_pos_z + (float) aabb.maxZ;
                                                    bufferBuilder.vertex(x1, y1, z1, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x1, y2, z1, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x1, y2, z2, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x1, y1, z2, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0, 0, nx, ny, nz);
                                                    break;
                                                case WEST:
                                                    x1 = last_pos_x + (float) aabb.minX - 0.02f;
                                                    y1 = last_pos_y + (float) aabb.minY;
                                                    z1 = last_pos_z + (float) aabb.minZ;
                                                    y2 = last_pos_y + (float) aabb.maxY;
                                                    z2 = last_pos_z + (float) aabb.maxZ;
                                                    bufferBuilder.vertex(x1, y1, z1, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x1, y1, z2, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x1, y2, z2, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0, 0, nx, ny, nz);
                                                    bufferBuilder.vertex(x1, y2, z1, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0, 0, nx, ny, nz);
                                                    break;
                                            }
                                        }
                                        vi++;
                                    }
                                    tesselator.end();
                                    RenderSystem.enableBlend();
                                    RenderSystem.disableTexture();
                                }
                                if (!fancy || !fat_lines) {
                                    MCVer.inst.set_render_lines(bufferBuilder);
                                    int vi = 0;
                                    for (AABB aabb : list) {
                                        if (vi == wand.grid_voxel_index) {
                                            grid(bufferBuilder, wand.side, last_pos_x, last_pos_y, last_pos_z, aabb);
                                        }
                                        vi++;
                                    }
                                    tesselator.end();
                                }

                            } else {
                                //no_shape=true;
                            }
                        } else {
                            //no_shape=true;
                        }
                    }

                    //break;
                case ROW_COL:
                case FILL:
                case AREA:
                case GRID:
                case LINE:
                case CIRCLE:
                case RECT:

                    if (wand.valid || (mode == Mode.LINE || mode == Mode.CIRCLE) || (wand.valid && wand.has_empty_bucket)) {
                        //bbox
                        if (drawlines && fill_outlines && (mode == Mode.ROW_COL || mode == Mode.FILL || mode == Mode.RECT)) {
                            if (fat_lines) {
                                RenderSystem.enableTexture();
                                MCVer.inst.set_render_quads_pos_tex(bufferBuilder);
                                preview_block_fat(bufferBuilder,
                                        wand.bb1_x - off2, wand.bb1_y - off2, wand.bb1_z - off2,
                                        wand.bb2_x + off2, wand.bb2_y + off2, wand.bb2_z + off2,
                                        bbox_col,false);
                                tesselator.end();
                                RenderSystem.disableTexture();
                                RenderSystem.enableCull();
                            } else {
                                MCVer.inst.set_render_lines(bufferBuilder);
                                preview_block(bufferBuilder,
                                        wand.bb1_x - off2, wand.bb1_y - off2, wand.bb1_z - off2,
                                        wand.bb2_x + off2, wand.bb2_y + off2, wand.bb2_z + off2,
                                        bbox_col);
                                tesselator.end();
                            }
                        }

                        if (wand.has_empty_bucket || (wand.valid && (has_target || wand.is_alt_pressed) && wand.block_buffer != null)) {
                            random.setSeed(0);
                            if (fancy && !wand.destroy && !wand.use && !wand.has_empty_bucket) {
                                RenderSystem.enableTexture();
                                RenderSystem.enableCull();
                                //RenderSystem.enableBlend();
                                //RenderSystem.defaultBlendFunc();
                                MCVer.inst.set_color(1.0f, 1.0f, 1.0f, opacity);
                                MCVer.inst.set_render_quads_block(bufferBuilder);
                                BlockState st;
                                for (int idx = 0; idx < wand.block_buffer.get_length() && idx < Wand.MAX_LIMIT; idx++) {
                                    if (wand.block_buffer.state[idx] != null) {
                                        //preview_shape = wand.block_buffer.state[idx].getShape(client.level, last_pos);
                                        st = wand.block_buffer.state[idx];
                                        if (wand.has_water_bucket) {
                                            st = Blocks.WATER.defaultBlockState();
                                        } else {
                                            if (wand.has_lava_bucket) {
                                                st = Blocks.LAVA.defaultBlockState();
                                            }
                                        }
                                        render_shape(matrixStack, tesselator, bufferBuilder, st,
                                                wand.block_buffer.buffer_x[idx],
                                                wand.block_buffer.buffer_y[idx],
                                                wand.block_buffer.buffer_z[idx]);

                                        //TODO: all double blocks!!
                                        if (wand.block_buffer.state[idx].hasProperty(DoublePlantBlock.HALF)) {
                                            render_shape(matrixStack, tesselator, bufferBuilder, wand.block_buffer.state[idx].setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER),
                                                    wand.block_buffer.buffer_x[idx],
                                                    wand.block_buffer.buffer_y[idx] + 1,
                                                    wand.block_buffer.buffer_z[idx]);
                                        } else {
                                            if (wand.block_buffer.state[idx].getBlock() instanceof DoorBlock) {

                                                render_shape(matrixStack, tesselator, bufferBuilder, wand.block_buffer.state[idx].setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER),
                                                        wand.block_buffer.buffer_x[idx],
                                                        wand.block_buffer.buffer_y[idx] + 1,
                                                        wand.block_buffer.buffer_z[idx]);
                                            }
                                        }
                                    }
                                }
                                tesselator.end();
                                //RenderSystem.disableBlend();
                                RenderSystem.disableTexture();
                            }


                            render_mode_outline(tesselator,bufferBuilder);
                        }
                        if (drawlines && wand.p1 != null && (mode == Mode.LINE || mode == Mode.CIRCLE)) {

                            if (fat_lines) {
                                RenderSystem.enableTexture();
                                //RenderSystem.disableCull();
                                //even_circle=true;
                                MCVer.inst.set_render_quads_pos_tex(bufferBuilder);
                                float off=(mode == Mode.CIRCLE && wand.even_circle)? -1.0f:0.0f;
                                preview_block_fat(bufferBuilder,
                                        wand.p1.getX()+off, wand.p1.getY()+off, wand.p1.getZ()+off,
                                        wand.p1.getX() + 1, wand.p1.getY() + 1, wand.p1.getZ() + 1,
                                        start_col,false
                                );
                                tesselator.end();
                                if (has_target) {
                                    MCVer.inst.set_render_quads_pos_tex(bufferBuilder);
                                    //off=(mode == Mode.CIRCLE && even_circle)? 1.0f:0.0f;
                                    preview_block_fat(bufferBuilder,
                                            last_pos_x, last_pos_y, last_pos_z,
                                            last_pos_x + 1, last_pos_y + 1, last_pos_z + 1,
                                            end_col,false);
                                    tesselator.end();

                                    RenderSystem.disableDepthTest();
                                    MCVer.inst.set_render_quads_pos_tex(bufferBuilder);

                                    off=(mode == Mode.CIRCLE && wand.even_circle)? 0.0f:0.5f;

                                    player_facing_line(bufferBuilder,
                                            (float) camera.getPosition().x, (float) camera.getPosition().y, (float) camera.getPosition().z,
                                            last_pos_x + off, last_pos_y + off, last_pos_z + off,
                                            wand_x1 + off, wand_y1 + off, wand_z1 + off,
                                            line_col);
                                    tesselator.end();
                                }

                                RenderSystem.disableTexture();
                                RenderSystem.enableDepthTest();
                                RenderSystem.enableCull();
                            } else {
                                MCVer.inst.set_render_lines(bufferBuilder);
                                bufferBuilder.vertex(last_pos_x + 0.5, last_pos_y + 0.5, last_pos_z + 0.5)
                                        .color(line_col.r, line_col.g, line_col.b, line_col.a).endVertex();
                                bufferBuilder.vertex(wand_x1 + 0.5, wand_y1 + 0.5, wand_z1 + 0.5)
                                        .color(line_col.r, line_col.g, line_col.b, line_col.a).endVertex();
                                RenderSystem.disableDepthTest();
                                preview_block(bufferBuilder,
                                        wand.p1.getX(), wand.p1.getY(), wand.p1.getZ(),
                                        wand.p1.getX() + 1, wand.p1.getY() + 1, wand.p1.getZ() + 1,
                                        start_col);
                                preview_block(bufferBuilder,
                                        last_pos_x - off2, last_pos_y - off2, last_pos_z - off2,
                                        last_pos_x + 1 + off2, last_pos_y + 1 + off2, last_pos_z + 1 + off2,
                                        end_col);
                                RenderSystem.enableDepthTest();
                                tesselator.end();
                            }
                        }
                    }
                    break;
                case COPY:
                    if (drawlines && copy_outlines && wand.valid) {

                        x1 = (wand.copy_x1 > wand.copy_x2) ? (wand.copy_x1 + off2) : (wand.copy_x1 - off2);
                        y1 = (wand.copy_y1 > wand.copy_y2) ? (wand.copy_y1 + off2) : (wand.copy_y1 - off2);
                        z1 = (wand.copy_z1 > wand.copy_z2) ? (wand.copy_z1 + off2) : (wand.copy_z1 - off2);
                        x2 = (wand.copy_x2 > wand.copy_x1) ? (wand.copy_x2 + off2) : (wand.copy_x2 - off2);
                        y2 = (wand.copy_y2 > wand.copy_y1) ? (wand.copy_y2 + off2) : (wand.copy_y2 - off2);
                        z2 = (wand.copy_z2 > wand.copy_z1) ? (wand.copy_z2 + off2) : (wand.copy_z2 - off2);
                        if (fat_lines) {
                            //RenderSystem.disableCull();
                            RenderSystem.enableTexture();
                            MCVer.inst.set_render_quads_pos_tex(bufferBuilder);
                            preview_block_fat(bufferBuilder,
                                    x1, y1, z1,
                                    x2, y2, z2,
                                    bbox_col,false);
                            tesselator.end();
                            RenderSystem.disableTexture();
                            RenderSystem.enableCull();
                        } else {
                            MCVer.inst.set_render_lines(bufferBuilder);
                            preview_block(bufferBuilder,
                                    x1, y1, z1,
                                    x2, y2, z2,
                                    bbox_col);
                            tesselator.end();
                        }
                    }
                    break;
                case PASTE:
                    if (wand.copy_paste_buffer.size() > 0) {
                        int mx=1;
                        int my=1;
                        int mz=1;
                        switch(mirroraxis){
                            case 1://X
                                mx=-1;
                                break;
                            case 2://Y
                                my=-1;
                                break;
                            case 3://Z
                                mz=-1;
                                break;
                        }
                        BlockPos b_pos = last_pos;
                        if (!(wand.replace || wand.destroy)) {
                            b_pos = last_pos.relative(last_side, 1);
                        }
                        if (wand.destroy) {
                            //render_mode_outline( tesselator,bufferBuilder);
                        } else if (fancy) {
                            RenderSystem.enableCull();
                            //RenderSystem.enableBlend();
                            //RenderSystem.defaultBlendFunc();
                            RenderSystem.enableTexture();
                            MCVer.inst.set_color(1.0f, 1.0f, 1.0f, opacity);
                            MCVer.inst.set_render_quads_block(bufferBuilder);
                            random.setSeed(0);
                            wand.random.setSeed(wand.palette_seed);
                            //use block_buffer, previously copy copy_paste_buffer to block_buffer
                            BlockPos po=wand.copy_paste_buffer.get(0).pos;

                            for (CopyPasteBuffer b : wand.copy_paste_buffer) {
                                BlockPos p = b.pos.rotate(last_rot);
                                BlockState st = wand.paste_rot(b.state);
                                if (wand.has_palette) {
                                    st = wand.get_state();
                                }
                                int px=b_pos.getX() + p.getX()*mx;
                                int py=b_pos.getY() + p.getY()*my;
                                int pz=b_pos.getZ() + p.getZ()*mz;
                                //render_shape(matrixStack, tesselator, bufferBuilder, st, b_pos.getX() + p.getX(), b_pos.getY() + p.getY(), b_pos.getZ() + p.getZ());
                                render_shape(matrixStack, tesselator, bufferBuilder, st, px,py,pz);
                            }
                            tesselator.end();
                            RenderSystem.disableTexture();
                            //RenderSystem.disableBlend();
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
                                //RenderSystem.disableCull();
                                RenderSystem.enableTexture();
                                MCVer.inst.set_render_quads_pos_tex(bufferBuilder);
                            } else {
                                MCVer.inst.set_render_lines(bufferBuilder);
                            }
                            for (CopyPasteBuffer b : wand.copy_paste_buffer) {
                                BlockPos p = b.pos.rotate(last_rot);
                                float x = b_pos.getX() + p.getX()*mx;
                                float y = b_pos.getY() + p.getY()*my;
                                float z = b_pos.getZ() + p.getZ()*mz;
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
                            RenderSystem.disableTexture();
                            if (fat_lines) {
                                RenderSystem.enableTexture();
                                MCVer.inst.set_render_quads_pos_tex(bufferBuilder);
                            } else {
                                MCVer.inst.set_render_lines(bufferBuilder);
                            }
                            if (fat_lines) {
                                preview_block_fat(bufferBuilder,
                                        x1, y1, z1,
                                        x2, y2, z2,
                                        c,false);
                            } else {
                                preview_block(bufferBuilder,
                                        x1, y1, z1,
                                        x2, y2, z2,
                                        c);
                            }
                            tesselator.end();
                            RenderSystem.disableTexture();
                        }
                    }
                    break;
            }
            //RenderSystem.enableBlend();
            //RenderSystem.enableTexture();
            //RenderSystem.enableDepthTest();
            RenderSystem.lineWidth(1.0f);
        }
        MCVer.inst.post_render(matrixStack);
        RenderSystem.depthMask(true);
    }
    public static void render_mode_outline( Tesselator tesselator,BufferBuilder bufferBuilder){
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
                RenderSystem.enableTexture();
                MCVer.inst.set_render_quads_pos_tex(bufferBuilder);
            } else {
                RenderSystem.enableCull();
                MCVer.inst.set_render_lines(bufferBuilder);
            }
            for (int idx = 0; idx < wand.block_buffer.get_length() && idx < Wand.MAX_LIMIT; idx++) {
                float x = wand.block_buffer.buffer_x[idx];
                float y = wand.block_buffer.buffer_y[idx];
                float z = wand.block_buffer.buffer_z[idx];
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
                                    x + aabb.minX, y + aabb.minY, z + aabb.minZ,
                                    x + aabb.maxX, y + aabb.maxY, z + aabb.maxZ,
                                    mode_outline_color);
                        }
                    }
                }
            }
            tesselator.end();
            if (fat_lines) {
                RenderSystem.disableTexture();
            }
        }
    }
    static void preview_block(BufferBuilder bufferBuilder,double fx1, double fy1, double fz1, double fx2, double fy2, double fz2,Colorf c) {
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
        MCVer.inst.set_color(c.r,c.g,c.b,c.a);
        MCVer.inst.set_texture(LINE_TEXTURE);
        float w=fat_lines_width;
        float nx=0.0f,ny=0.0f,nz=-1.0f;
        //north -z
        quad_line(bufferBuilder,  0, w,0, fx1, fy1  , fz1, fx2, fy1  , fz1,nx,ny,nz,c);
        quad_line(bufferBuilder,  0,-w,0, fx2, fy2  , fz1, fx1, fy2  , fz1,nx,ny,nz,c);
        quad_line(bufferBuilder,  w, 0,0, fx1, fy2-w, fz1, fx1, fy1+w, fz1,nx,ny,nz,c);
        quad_line(bufferBuilder, -w, 0,0, fx2, fy1+w, fz1, fx2, fy2-w, fz1,nx,ny,nz,c);
        if(cross) {
            quad_line(bufferBuilder, -w, 0, 0,
                    fx1+w, fy1, fz1,
                    fx2, fy2, fz1, nx, ny, nz, c);
            quad_line(bufferBuilder, w, 0, 0,
                    fx1, fy2, fz1,
                    fx2-w, fy1, fz1, nx, ny, nz, c);
        }

        //south +z
        nx=0.0f;ny=0.0f;nz=1.0f;
        quad_line(bufferBuilder,  0, w,0,  fx2, fy1,   fz2,fx1, fy1  , fz2,nx,ny,nz,c);
        quad_line(bufferBuilder,  0,-w,0, fx1, fy2  , fz2, fx2, fy2,   fz2,nx,ny,nz,c);
        quad_line(bufferBuilder,  w, 0,0, fx1, fy1+w, fz2, fx1, fy2-w, fz2,nx,ny,nz,c);
        quad_line(bufferBuilder, -w, 0,0,  fx2, fy2-w, fz2,fx2, fy1+w, fz2,nx,ny,nz,c);
        if(cross) {
            quad_line(bufferBuilder, w, 0, 0,
                    fx1, fy1, fz2,
                    fx2-w, fy2, fz2, nx, ny, nz, c);
            quad_line(bufferBuilder, -w, 0, 0,
                    fx1+w, fy2, fz2,
                    fx2, fy1, fz2, nx, ny, nz, c);
        }
        //up +y
        nx=0.0f;ny=1.0f;nz=0.0f;
        quad_line(bufferBuilder,  w,0, 0, fx1  , fy2, fz2, fx1 , fy2, fz1,nx,ny,nz,c);
        quad_line(bufferBuilder, -w,0, 0, fx2  , fy2, fz1, fx2 , fy2, fz2,nx,ny,nz,c);
        quad_line(bufferBuilder,  0,0, w, fx1+w, fy2, fz1, fx2-w, fy2, fz1,nx,ny,nz,c);
        quad_line(bufferBuilder,  0,0,-w, fx2-w, fy2, fz2, fx1+w, fy2, fz2,nx,ny,nz,c);
        if(cross) {
            quad_line(bufferBuilder, -w, 0, 0,
                    fx1+w, fy2, fz1,
                    fx2, fy2, fz2, nx, ny, nz, c);
            quad_line(bufferBuilder, w, 0, 0,
                    fx1, fy2, fz2,
                    fx2-w, fy2, fz1, nx, ny, nz, c);
        }
        //down -y
        nx=0.0f;ny=-1.0f;nz=0.0f;
        quad_line(bufferBuilder,  w,0, 0, fx1, fy1, fz1, fx1  , fy1, fz2,nx,ny,nz,c);
        quad_line(bufferBuilder, -w,0, 0, fx2  , fy1, fz2,fx2, fy1, fz1,nx,ny,nz,c);
        quad_line(bufferBuilder,  0,0, w, fx2-w, fy1, fz1,fx1+w, fy1, fz1,nx,ny,nz,c);
        quad_line(bufferBuilder,  0,0,-w, fx1+w, fy1, fz2,  fx2-w, fy1, fz2,nx,ny,nz,c);
        if(cross) {
            quad_line(bufferBuilder, w, 0, 0,
                    fx1, fy1, fz1,
                    fx2-w, fy1, fz2, nx, ny, nz, c);
            quad_line(bufferBuilder, -w, 0, 0,
                    fx1+w, fy1, fz2,
                    fx2, fy1, fz1, nx, ny, nz, c);
        }
        //east +x
        nx=1.0f;ny=0.0f;nz=0.0f;
        quad_line(bufferBuilder, 0, w, 0, fx2,   fy1, fz1, fx2,   fy1, fz2,nx,ny,nz,c);
        quad_line(bufferBuilder, 0,-w, 0, fx2,   fy2, fz2, fx2,   fy2, fz1,nx,ny,nz,c);
        quad_line(bufferBuilder, 0, 0, w, fx2, fy2-w, fz1, fx2, fy1+w, fz1,nx,ny,nz,c);
        quad_line(bufferBuilder, 0, 0,-w, fx2, fy1+w, fz2, fx2, fy2-w, fz2,nx,ny,nz,c);
        if(cross) {
            quad_line(bufferBuilder, 0, 0, w,
                    fx1, fy1, fz1,
                    fx1, fy2, fz2-w, nx, ny, nz, c);
            quad_line(bufferBuilder, 0, 0, w,
                    fx1, fy1, fz2-w,
                    fx1, fy2, fz1, nx, ny, nz, c);
        }
        //west -x
        nx=-1.0f;ny=0.0f;nz=0.0f;
        quad_line(bufferBuilder, 0, w,0,   fx1,   fy1, fz2,fx1,   fy1, fz1,nx,ny,nz,c);
        quad_line(bufferBuilder, 0,-w,0, fx1,   fy2, fz1,  fx1,   fy2, fz2,nx,ny,nz,c);
        quad_line(bufferBuilder, 0,0, w, fx1, fy1+w, fz1,  fx1, fy2-w, fz1,nx,ny,nz,c);
        quad_line(bufferBuilder, 0,0,-w,   fx1, fy2-w, fz2,fx1, fy1+w, fz2,nx,ny,nz,c);
        if(cross) {
            quad_line(bufferBuilder, 0, 0, -w,
                    fx2, fy1, fz1+w,
                    fx2, fy2, fz2, nx, ny, nz, c);
            quad_line(bufferBuilder, 0, 0, -w,
                    fx2, fy1, fz2,
                    fx2, fy2, fz1+w, nx, ny, nz, c);
        }
    }

    private static void quad_line(BufferBuilder bufferBuilder,
                                  float wx,float wy,float wz,
                                  float lx1, float ly1,float lz1,
                                  float lx2, float ly2,float lz2,
                                  float nx,float ny,float nz,Colorf c){

        bufferBuilder.vertex(lx1, ly1, lz1         , c.r,c.g,c.b, c.a, 0.0f, 0.0f, 0, 0, nx,ny,nz);
        bufferBuilder.vertex(lx1+wx, ly1+wy, lz1+wz, c.r,c.g,c.b, c.a, 0.0f, 1.0f, 0, 0, nx,ny,nz);
        bufferBuilder.vertex(lx2+wx, ly2+wy, lz2+wz, c.r,c.g,c.b, c.a, 1.0f, 1.0f, 0, 0, nx,ny,nz);
        bufferBuilder.vertex(lx2, ly2, lz2         , c.r,c.g,c.b, c.a, 1.0f, 0.0f, 0, 0, nx,ny,nz);
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
        MCVer.inst.set_texture(LINE_TEXTURE);
        MCVer.inst.set_color(c.r,c.g,c.b,c.a);

        bufferBuilder.vertex(lx1-nx, ly1-ny, lz1-nz, c.r,c.g,c.b,c.a, 0.0f, 0.0f, 0, 0, nx,ny,nz);
        bufferBuilder.vertex(lx1+nx, ly1+ny, lz1+nz, c.r,c.g,c.b,c.a, 0.0f, 1.0f, 0, 0, nx,ny,nz);
        bufferBuilder.vertex(lx2+nx, ly2+ny, lz2+nz, c.r,c.g,c.b,c.a, 1.0f, 1.0f, 0, 0, nx,ny,nz);
        bufferBuilder.vertex(lx2-nx, ly2-ny, lz2-nz, c.r,c.g,c.b,c.a, 1.0f, 0.0f, 0, 0, nx,ny,nz);

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
    private static void draw_lines(BufferBuilder bufferBuilder,int from,int to,float r,float g,float b,float a){
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

    static void render_shape(PoseStack matrixStack,Tesselator tesselator,BufferBuilder bufferBuilder,BlockState state,double x, double y,double z){
        float u1=0;
        float v1=0;
        float u0=0;
        float v0=0;
        float r= block_col.r;
        float g= block_col.g;
        float b= block_col.b;
        float a= block_col.a;
        BakedModel bakedModel;
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        //BlockPos bp=new BlockPos(x,y,z);
        bp.set(x,y,z);
        boolean is_water=state==Blocks.WATER.defaultBlockState();
        boolean is_lava=state==Blocks.LAVA.defaultBlockState();
        boolean invalid=false;
        if(is_water||is_lava||
           (wand.mode!=Mode.PASTE && wand.has_water_bucket && wand.level.getBlockState(bp).isAir())) {
            water=true;
            float h=0.875f;
            int i;
            RenderSystem.enableCull();

            TextureAtlasSprite sprite;
            if(is_water) {
                sprite = ModelBakery.WATER_FLOW.sprite();
                i= BiomeColors.getAverageWaterColor(wand.level, bp);
            }else {
                sprite = ModelBakery.LAVA_FLOW.sprite();
                i=16777215;
            }
            r = (float)(i >> 16 & 255) / 255.0F;
            g = (float)(i >> 8 & 255) / 255.0F;
            b = (float)(i & 255) / 255.0F;

            MCVer.inst.set_texture(TextureAtlas.LOCATION_BLOCKS);
            int bf = LevelRenderer.getLightColor(wand.level, bp);
            //bf=-1;
            u0 = sprite.getU0();
            v0 = sprite.getV0();
            u1 = sprite.getU1();
            v1 = sprite.getV1();
            float o=0.1f;
            //up

            bufferBuilder.vertex(x  +o,y+h-o,z  +o).color(r,g,b, a).uv(u1, v1).uv2(bf).normal(0.0F, 1.0F, 0.0F).endVertex();
            bufferBuilder.vertex(x  +o,y+h-o,z+1-o).color(r,g,b, a).uv(u1, v0).uv2(bf).normal(0.0F, 1.0F, 0.0F).endVertex();
            bufferBuilder.vertex(x+1-o,y+h-o,z+1-o).color(r,g,b, a).uv(u0, v0).uv2(bf).normal(0.0F, 1.0F, 0.0F).endVertex();
            bufferBuilder.vertex(x+1-o,y+h-o,z  +o).color(r,g,b, a).uv(u0, v1).uv2(bf).normal(0.0F, 1.0F, 0.0F).endVertex();
            //down
            bufferBuilder.vertex(x  +o,y+o,z  +o).color(r,g,b, a).uv(u1, v1).uv2(bf).normal(0.0F, -1.0F, 0.0F).endVertex();
            bufferBuilder.vertex(x+1-o,y+o,z  +o).color(r,g,b, a).uv(u0, v1).uv2(bf).normal(0.0F, -1.0F, 0.0F).endVertex();
            bufferBuilder.vertex(x+1-o,y+o,z+1-o).color(r,g,b, a).uv(u0, v0).uv2(bf).normal(0.0F, -1.0F, 0.0F).endVertex();
            bufferBuilder.vertex(x  +o,y+o,z+1-o).color(r,g,b, a).uv(u1, v0).uv2(bf).normal(0.0F, -1.0F, 0.0F).endVertex();
            //north -z
            bufferBuilder.vertex(x  +o,y+o,z  +o).color(r,g,b, a).uv(u1, v1).uv2(bf).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.vertex(x  +o,y+h-o,z+o).color(r,g,b, a).uv(u1, v0).uv2(bf).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.vertex(x+1-o,y+h-o,z+o).color(r,g,b, a).uv(u0, v0).uv2(bf).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.vertex(x+1-o,y+o,z  +o).color(r,g,b, a).uv(u0, v1).uv2(bf).normal(0.0F, 0.0F, 1.0F).endVertex();
            //south +z
            bufferBuilder.vertex(x  +o,y+o  ,z+1-o).color(r,g,b, a).uv(u1, v1).uv2(bf).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.vertex(x+1-o,y+o  ,z+1-o).color(r,g,b, a).uv(u0, v1).uv2(bf).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.vertex(x+1-o,y+h-o,z+1-o).color(r,g,b, a).uv(u0, v0).uv2(bf).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.vertex(x  +o,y+h-o,z+1-o).color(r,g,b, a).uv(u1, v0).uv2(bf).normal(0.0F, 0.0F, 1.0F).endVertex();
            //east
            bufferBuilder.vertex(x+o,y+o,z+o).color(r,g,b,     a).uv(u0, v1).uv2(bf).normal(-1.0F, 0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(x+o,y+o,z+1-o).color(r,g,b,   a).uv(u1, v1).uv2(bf).normal(-1.0F, 0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(x+o,y+h-o,z+1-o).color(r,g,b, a).uv(u1, v0).uv2(bf).normal(-1.0F, 0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(x+o,y+h-o,z+o).color(r,g,b,   a).uv(u0, v0).uv2(bf).normal(-1.0F, 0.0F, 0.0F).endVertex();
            //weast
            bufferBuilder.vertex(x+1-o,y+o  ,z+o  ).color(r,g,b,a).uv(u0, v1).uv2(bf).normal(1.0F, 0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(x+1-o,y+h-o,z+o  ).color(r,g,b,a).uv(u0, v0).uv2(bf).normal(1.0F, 0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(x+1-o,y+h-o,z+1-o).color(r,g,b,a).uv(u1, v0).uv2(bf).normal(1.0F, 0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(x+1-o,y+o  ,z+1-o).color(r,g,b,a).uv(u1, v1).uv2(bf).normal(1.0F, 0.0F, 0.0F).endVertex();

            return;
        }
        try {
            bakedModel = blockRenderer.getBlockModel(state);
            if(bakedModel!=null) {
                if(!state.canSurvive(client.level, bp)){
                     r=1.0f;
                     g=0.0f;
                     b=0.0f;
                    invalid=true;
                }
                for(Direction dir: dirs) {
                    List<BakedQuad> bake_list = bakedModel.getQuads(state, dir, random);
                    if (bake_list !=null && !bake_list.isEmpty() ) {
                        MCVer.inst.set_identity(matrixStack2);
                        if(wand.replace&& wand.mode!=Mode.COPY /*&& wand.mode!=Mode.PASTE */){
                            Vec3i n=wand.side.getNormal();
                            matrixStack2.translate(
                                    x+(0.5f*(1.0f-n.getX()))+n.getX(),
                                    y+(0.5f*(1.0f-n.getY()))+n.getY(),
                                    z+(0.5f*(1.0f-n.getZ()))+n.getZ()
                            );
                            matrixStack2.scale(0.5f,0.5f,0.5f);
                            matrixStack2.translate(-0.5f,-0.5f,-0.5f);
                        }else{
                            matrixStack2.translate(x, y, z);
                        }

                        for (BakedQuad quad : bake_list) {
                            if(wand.replace ||
                                    MCVer.inst.shouldRenderFace(state, wand.level, bp, quad.getDirection(), bp)
                                    /*Block.shouldRenderFace(state, wand.level, bp, quad.getDirection())*/
                            )
                            {
                                MCVer.inst.set_texture(TextureAtlas.LOCATION_BLOCKS);
                                int[] verts = quad.getVertices();
                                int n = verts.length / 4;
                                int ii = -1;
                                if(!invalid) {
                                    if (quad.isTinted()) {
                                        if (state.getBlock() instanceof GrassBlock)
                                            ii = BiomeColors.getAverageGrassColor(wand.level, bp);
                                        else if (state.getBlock() instanceof LeavesBlock)
                                            ii = BiomeColors.getAverageFoliageColor(wand.level, bp);
                                        else
                                            ii = client.getBlockColors().getColor(state, wand.level, last_pos);
                                        r = (float) (ii >> 16 & 255) / 255.0F;
                                        g = (float) (ii >> 8 & 255) / 255.0F;
                                        b = (float) (ii & 255) / 255.0F;
                                    } else {
                                        r = block_col.r;
                                        g = block_col.g;
                                        b = block_col.b;
                                    }
                                }
                                float f = wand.level.getShade(quad.getDirection(), quad.isShade());
                                bufferBuilder.putBulkData(matrixStack2.last(), quad, new float[]{f, f, f, f}, r, g, b, new int[]{-1, -1, -1, -1}, n, true);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            //WandsMod.log("couldn't get model, blacklisting block...", true);
        }
    }
    static void update_colors(){
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

