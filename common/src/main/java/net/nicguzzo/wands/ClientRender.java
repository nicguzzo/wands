package net.nicguzzo.wands;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector3f;
import net.minecraft.client.CameraType;
import net.minecraft.core.Vec3i;
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
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Random;

import net.nicguzzo.wands.mcver.MCVer;
import net.nicguzzo.wands.Wand.CopyPasteBuffer;
import net.nicguzzo.wands.WandItem.Mode;
import net.nicguzzo.wands.WandsConfig.Color;
import net.nicguzzo.wands.WandsConfig.Colors;

public class ClientRender {
    public static final float p_o = -0.005f;// preview_block offset
    private static long t0 = 0;
	private static long t1 = 0;
    private static long t00 = 0;
	private static boolean prnt;
    //public static Vec3 c=new Vec3(0,0,0);
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
    static boolean fat_lines=true;
    static float fat_lines_width=0.05f;
    static Minecraft client;
    private static final ResourceLocation GRID_TEXTURE = new ResourceLocation("wands", "textures/blocks/grid.png");
    private static final ResourceLocation LINE_TEXTURE = new ResourceLocation("wands", "textures/blocks/line.png");
    static Random random=new Random();
    static Direction[] dirs={ Direction.DOWN,Direction.UP,Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST,null};

    public static boolean update_colors=true;
    static Color block_col  =new Color(1.0f,1.0f,1.0f,1.0f);
    static Color bo_col  =new Color(1.0f,1.0f,1.0f,1.0f);
    static Color bbox_col=new Color(0.0f,0.0f,1.0f,1.0f);
    static Color destroy_col =new Color(1.0f,0.0f,0.0f,1.0f);
    static Color tool_use_col=new Color(0.0f,1.0f,1.0f,1.0f);
    static Color start_col   =new Color(1.0f,1.0f,0.0f,1.0f);
    static Color end_col     =new Color(1.0f,1.0f,0.0f,1.0f);
    static Color line_col   =new Color(1.0f,0.0f,1.0f,1.0f);
    static Color paste_bb_col=new Color(0.0f,0.0f,0.0f,1.0f);

    public static boolean has_target=false;

    public static void render(PoseStack matrixStack, double camX, double camY, double camZ, MultiBufferSource.BufferSource bufferIn) {
        client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null)
            return;
        if(client.options.getCameraType()  != CameraType.FIRST_PERSON){
            return;
        }
        if(update_colors){
            update_colors=false;
            update_colors();
        }
        opacity=WandsMod.config.preview_opacity;
        /*if(WandsModClient.has_optifine){
            fancy=false;
        }else {*/
        fancy = WandsConfig.get_instance().fancy_preview;
        //fancy=false;
        fat_lines= WandsConfig.get_instance().fat_lines;
        if(WandsConfig.get_instance().fat_lines_width>0 && WandsConfig.get_instance().fat_lines_width<0.5) {
            fat_lines_width = WandsConfig.get_instance().fat_lines_width;
        }
        //}
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
                has_target=true;
                int rot = WandItem.getRotation(stack);
                WandItem.Orientation orientation = WandItem.getOrientation(stack);
                Direction side = block_hit.getDirection();
                BlockPos pos = block_hit.getBlockPos();
                BlockState block_state = client.level.getBlockState(pos);
                //if(prnt){
                //    WandsMod.LOGGER.info("render block_state "+block_state);
                // }

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
                has_target=false;
                if( ((wand.mode== Mode.LINE|| wand.mode== Mode.CIRCLE) && wand.p1!=null)||
                        (wand.mode== WandItem.Mode.PASTE && wand.copy_paste_buffer.size()!=0 && wand.is_alt_pressed)) {
                    preview_mode(wand.mode, matrixStack, bufferIn);
                }
            }
        }
    }

    private static void preview_mode(Mode mode, PoseStack matrixStack, MultiBufferSource.BufferSource bufferIn) {
        //TODO: preview water
        MCVer.inst.pre_render(matrixStack);
        Camera camera = client.gameRenderer.getMainCamera();
        //client.renderBuffers().bufferSource().getBuffer(RenderType.solid());

        Tesselator tesselator = Tesselator.getInstance();

        BufferBuilder bufferBuilder = tesselator.getBuilder();
        boolean fabulous_bug=false;

        fabulous_bug=(WandsModClient.is_forge&& Minecraft.useShaderTransparency() );

        if(Screen.hasControlDown() || fabulous_bug){
            RenderSystem.disableDepthTest();
        }else{
            RenderSystem.enableDepthTest();
        }

        if (camera.isInitialized() && last_pos!=null) {
            //c = camera.getPosition().reverse();
            //c=new Vec3(0,0,0);
            double last_pos_x=last_pos.getX();
            double last_pos_y=last_pos.getY();
            double last_pos_z=last_pos.getZ();
            double wand_x1=wand.x1;
            double wand_y1=wand.y1;
            double wand_z1=wand.z1;
            double wand_x2=wand.x2;
            double wand_y2=wand.y2;
            double wand_z2=wand.z2;
            float nx=0.0f,ny=0.0f,nz=0.0f;
            float fr=1.0f,fg=1.0f,fb=1.0f,fa=1.0f;
            float off2=0.05f;
            MCVer.inst.set_color(1.0F, 1.0F, 1.0F, 1.0F);
            //WandsMod.log("mode "+mode.toString(),prnt);

            RenderSystem.disableTexture();
            //RenderSystem.disableBlend();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
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
                                    //RenderSystem.enableBlend();
                                    RenderSystem.enableTexture();
                                    RenderSystem.disableCull();
                                    MCVer.inst.set_texture(GRID_TEXTURE);
                                    MCVer.inst.set_render_quads_pos_tex(bufferBuilder);

                                    int vi = 0;

                                    for (AABB aabb : list) {
                                        if (vi == wand.grid_voxel_index) {
                                            switch (wand.side) {
                                                case UP:
                                                    nx=0; ny=1.0f;nz=0.0f;
                                                case DOWN:
                                                    if(wand.side==Direction.DOWN)
                                                        ny=-1.0f;
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
                                                case SOUTH:
                                                    nx=0; ny=0.0f;nz=1.0f;
                                                case NORTH:
                                                    if(wand.side==Direction.NORTH)
                                                        nz=-1.0f;
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
                                                case EAST:
                                                    nx=1.0f; ny=0.0f;nz=0.0f;
                                                case WEST:
                                                    if(wand.side==Direction.WEST)
                                                        nx=-1.0f;
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
                                    //RenderSystem.disableBlend();
                                    RenderSystem.disableTexture();
                                }else{                                    
                                    MCVer.inst.set_render_lines(bufferBuilder);
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
                        /*if(no_shape) {
                            MCVer.inst.set_render_lines(bufferBuilder);
                            preview_block(bufferBuilder,
                                    wand_x1, (wand_y1 + wand.y0),wand_z1,
                                    wand_x2, (wand_y1 + wand.y0 + wand.block_height), wand_z2,
                                    1.0f,1.0f,1.0f,1.0f);
                            tesselator.end();
                        }*/
                    }

                    //break;
                case ROW_COL:
                case FILL:
                case AREA:
                case LINE:
                case CIRCLE:
                case RECT:

                    //preview_mode1(bufferBuilder);
                    if (wand.valid ||  (mode == Mode.LINE || mode==Mode.CIRCLE) ) {
                        //bbox
                        if(mode==Mode.ROW_COL || mode==Mode.FILL  || mode==Mode.RECT)
                        {
                            if(fat_lines) {
                                RenderSystem.disableCull();
                                RenderSystem.enableTexture();
                                MCVer.inst.set_render_quads_pos_tex(bufferBuilder);
                                preview_block_fat(bufferBuilder,
                                        wand.bb1_x - off2, wand.bb1_y - off2, wand.bb1_z - off2,
                                        wand.bb2_x + off2, wand.bb2_y + off2, wand.bb2_z + off2,
                                        bbox_col);
                                tesselator.end();
                                RenderSystem.disableTexture();
                                RenderSystem.enableCull();
                            }else {
                                MCVer.inst.set_render_lines(bufferBuilder);
                                preview_block(bufferBuilder,
                                        wand.bb1_x - off2, wand.bb1_y - off2, wand.bb1_z - off2,
                                        wand.bb2_x + off2, wand.bb2_y + off2, wand.bb2_z + off2,
                                        bbox_col);
                                tesselator.end();
                            }
                        }

                        if (wand.valid && has_target&& wand.block_buffer != null ) {
                            random.setSeed(0);
                            if(fancy && !wand.destroy && !wand.has_bucket) {
                                RenderSystem.enableTexture();
                                RenderSystem.enableCull();
                                //RenderSystem.enableBlend();
                                //RenderSystem.defaultBlendFunc();
                                MCVer.inst.set_color(1.0f,1.0f,1.0f,opacity);
                                MCVer.inst.set_render_quads_block(bufferBuilder);
                                //MCVer.inst.set_render_quads_pos_tex(bufferBuilder);
                                
                                for (int idx = 0; idx < wand.block_buffer.get_length() && idx < Wand.MAX_LIMIT; idx++) {
                                    if (wand.block_buffer.state[idx] != null) {
                                        preview_shape = wand.block_buffer.state[idx].getShape(client.level, last_pos);
                                        render_shape(tesselator, bufferBuilder, wand.block_buffer.state[idx],
                                                wand.block_buffer.buffer_x[idx],
                                                wand.block_buffer.buffer_y[idx],
                                                wand.block_buffer.buffer_z[idx]);
                                        if(wand.block_buffer.state[idx].getBlock() instanceof DoorBlock){

                                            render_shape(tesselator, bufferBuilder, wand.block_buffer.state[idx].setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER),
                                                    wand.block_buffer.buffer_x[idx],
                                                    wand.block_buffer.buffer_y[idx]+1,
                                                    wand.block_buffer.buffer_z[idx]);
                                        }
                                    }
                                }
                                tesselator.end();
                                //RenderSystem.disableBlend();
                                RenderSystem.disableTexture();
                            }


                            Color c=bo_col;
                            if(wand.destroy){
                                c= destroy_col;
                            }
                            if(wand.is_alt_pressed &&  (wand.has_hoe|| wand.has_axe || wand.has_shovel)){
                                c= tool_use_col;
                            }
                            //RenderSystem.enableDepthTest();
                            if(fat_lines ) {
                                RenderSystem.disableCull();
                                //RenderSystem.disableDepthTest();
                                RenderSystem.enableTexture();
                                MCVer.inst.set_render_quads_pos_tex(bufferBuilder);
                            }else{
                                RenderSystem.enableCull();
                                MCVer.inst.set_render_lines(bufferBuilder);
                            }
                            for (int idx = 0; idx < wand.block_buffer.get_length() && idx < Wand.MAX_LIMIT; idx++) {
                                double x = wand.block_buffer.buffer_x[idx];
                                double y = wand.block_buffer.buffer_y[idx];
                                double z = wand.block_buffer.buffer_z[idx];
                                if (wand.block_buffer.state[idx] != null) {
                                    preview_shape = wand.block_buffer.state[idx].getShape(client.level, last_pos);
                                    List<AABB> list = preview_shape.toAabbs();
                                    for (AABB aabb : list) {
                                        if(fat_lines ){
                                            preview_block_fat(bufferBuilder,
                                                    x + aabb.minX, y + aabb.minY, z + aabb.minZ,
                                                    x + aabb.maxX, y + aabb.maxY, z + aabb.maxZ,
                                                    c);
                                        }else {
                                            preview_block(bufferBuilder,
                                                    x + aabb.minX, y + aabb.minY, z + aabb.minZ,
                                                    x + aabb.maxX, y + aabb.maxY, z + aabb.maxZ,
                                                    c);
                                        }
                                    }
                                }
                            }
                            tesselator.end();
                            if(fat_lines ) {
                                RenderSystem.disableTexture();
                            }
                        }
                        if ( wand.p1!=null && (mode == Mode.LINE || mode==Mode.CIRCLE )) {

                            if(fat_lines) {
                                RenderSystem.enableTexture();
                                RenderSystem.disableCull();
                                MCVer.inst.set_render_quads_pos_tex(bufferBuilder);

                                preview_block_fat(bufferBuilder,
                                        wand.p1.getX(), wand.p1.getY(), wand.p1.getZ(),
                                        wand.p1.getX() + 1, wand.p1.getY() + 1, wand.p1.getZ() + 1,
                                        start_col
                                );
                                tesselator.end();
                                if(has_target) {
                                    MCVer.inst.set_render_quads_pos_tex(bufferBuilder);
                                    preview_block_fat(bufferBuilder,
                                            last_pos_x , last_pos_y , last_pos_z,
                                            last_pos_x + 1, last_pos_y + 1, last_pos_z + 1 ,
                                            end_col);
                                    tesselator.end();

                                    RenderSystem.disableDepthTest();
                                    MCVer.inst.set_render_quads_pos_tex(bufferBuilder);
                                    player_facing_line(bufferBuilder,
                                            camera.getPosition().x, camera.getPosition().y, camera.getPosition().z,
                                            last_pos_x + 0.5, last_pos_y + 0.5, last_pos_z + 0.5,
                                            wand_x1 + 0.5, wand_y1 + 0.5, wand_z1 + 0.5,
                                            line_col);
                                    tesselator.end();
                                }

                                RenderSystem.disableTexture();
                                RenderSystem.enableDepthTest();
                                RenderSystem.enableCull();
                            }else {
                                MCVer.inst.set_render_lines(bufferBuilder);
                                bufferBuilder.vertex(last_pos_x + 0.5, last_pos_y + 0.5, last_pos_z + 0.5)
                                        .color(line_col.r, line_col.g,line_col.b, line_col.a).endVertex();
                                bufferBuilder.vertex(wand_x1 + 0.5, wand_y1 + 0.5, wand_z1 + 0.5)
                                        .color(line_col.r, line_col.g,line_col.b, line_col.a).endVertex();
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

                    if (wand.valid) {

                        x1=(wand.copy_x1>wand.copy_x2)? (wand.copy_x1+off2) : (wand.copy_x1-off2);
                        y1=(wand.copy_y1>wand.copy_y2)? (wand.copy_y1+off2) : (wand.copy_y1-off2);
                        z1=(wand.copy_z1>wand.copy_z2)? (wand.copy_z1+off2) : (wand.copy_z1-off2);
                        x2=(wand.copy_x2>wand.copy_x1)? (wand.copy_x2+off2) : (wand.copy_x2-off2);
                        y2=(wand.copy_y2>wand.copy_y1)? (wand.copy_y2+off2) : (wand.copy_y2-off2);
                        z2=(wand.copy_z2>wand.copy_z1)? (wand.copy_z2+off2) : (wand.copy_z2-off2);
                        if(fat_lines) {
                            RenderSystem.disableCull();
                            RenderSystem.enableTexture();
                            MCVer.inst.set_render_quads_pos_tex(bufferBuilder);
                            preview_block_fat(bufferBuilder,
                                    x1, y1, z1,
                                    x2, y2, z2,
                                    bbox_col);
                            tesselator.end();
                            RenderSystem.disableTexture();
                            RenderSystem.enableCull();
                        }else {
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

                        BlockPos b_pos = last_pos.relative(last_side, 1);
                        if (fancy) {
                            RenderSystem.enableCull();
                            //RenderSystem.enableBlend();
                            //RenderSystem.defaultBlendFunc();
                            RenderSystem.enableTexture();
                            MCVer.inst.set_color(1.0f,1.0f,1.0f,opacity);
                            MCVer.inst.set_render_quads_block(bufferBuilder);
                            //MCVer.inst.set_render_quads_pos_tex(bufferBuilder);
                            random.setSeed(0);
                            for (CopyPasteBuffer b : wand.copy_paste_buffer) {
                                BlockPos p = b.pos.rotate(Rotation.values()[last_rot]);
                                render_shape(tesselator, bufferBuilder, b.state, b_pos.getX() + p.getX(), b_pos.getY() + p.getY(), b_pos.getZ() + p.getZ());
                            }
                            tesselator.end();
                            RenderSystem.disableTexture();
                            //RenderSystem.disableBlend();
                        }

                        x1=Integer.MAX_VALUE;y1=Integer.MAX_VALUE;z1=Integer.MAX_VALUE;
                        x2=Integer.MIN_VALUE;y2=Integer.MIN_VALUE;z2=Integer.MIN_VALUE;

                        if(fat_lines) {
                            RenderSystem.disableCull();
                            RenderSystem.enableTexture();
                            MCVer.inst.set_render_quads_pos_tex(bufferBuilder);
                        }else {
                            MCVer.inst.set_render_lines(bufferBuilder);
                        }
                        for (CopyPasteBuffer b : wand.copy_paste_buffer) {
                            BlockPos p = b.pos.rotate(Rotation.values()[last_rot]);
                            double x = b_pos.getX() + p.getX();
                            double y = b_pos.getY() + p.getY();
                            double z = b_pos.getZ() + p.getZ();
                            if(fat_lines) {
                                preview_block_fat(bufferBuilder,
                                        x, y, z,
                                        x + 1, y + 1, z + 1, bo_col
                                );
                            }else{
                                preview_block(bufferBuilder,
                                        x, y, z,
                                        x + 1, y + 1, z + 1, bo_col
                                );
                            }

                            if(p.getX()<x1) x1=p.getX();
                            if(p.getY()<y1) y1=p.getY();
                            if(p.getZ()<z1) z1=p.getZ();
                            if(p.getX()+1>x2) x2=p.getX()+1;
                            if(p.getY()+1>y2) y2=p.getY()+1;
                            if(p.getZ()+1>z2) z2=p.getZ()+1;
                        }
                        x1 = b_pos.getX() + x1-off2;
                        y1 = b_pos.getY() + y1-off2;
                        z1 = b_pos.getZ() + z1-off2;
                        x2 = b_pos.getX() + x2+off2;
                        y2 = b_pos.getY() + y2+off2;
                        z2 = b_pos.getZ() + z2+off2;
                        if(fat_lines) {
                            preview_block_fat(bufferBuilder,
                                    x1, y1, z1,
                                    x2, y2, z2,
                                    paste_bb_col);
                        }else{
                            preview_block(bufferBuilder,
                                    x1, y1, z1,
                                    x2, y2, z2,
                                    paste_bb_col);
                        }
                        tesselator.end();
                        RenderSystem.disableTexture();
                    }
                break;
            }
            //RenderSystem.enableBlend();
            //RenderSystem.enableTexture();
            //RenderSystem.enableDepthTest();
        }
        MCVer.inst.post_render(matrixStack);
    }

    static void preview_block(BufferBuilder bufferBuilder,double fx1, double fy1, double fz1, double fx2, double fy2, double fz2,Color c) {
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

    static void preview_block_fat(BufferBuilder bufferBuilder,double fx1, double fy1, double fz1, double fx2, double fy2, double fz2,Color c) {
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

        //north
        quad_line(bufferBuilder,  0, w,0, fx1, fy1  , fz1, fx2, fy1 , fz1);
        quad_line(bufferBuilder,  0,-w,0, fx1, fy2  , fz1, fx2, fy2 , fz1);
        quad_line(bufferBuilder,  w, 0,0, fx1, fy1+w, fz1, fx1, fy2-w, fz1);
        quad_line(bufferBuilder, -w, 0,0, fx2, fy1+w, fz1, fx2, fy2-w, fz1);
        //south
        quad_line(bufferBuilder,  0, w,0, fx1, fy1  , fz2, fx2, fy1,   fz2);
        quad_line(bufferBuilder,  0,-w,0, fx1, fy2  , fz2, fx2, fy2,   fz2);
        quad_line(bufferBuilder,  w, 0,0, fx1, fy1+w, fz2, fx1, fy2-w, fz2);
        quad_line(bufferBuilder, -w, 0,0, fx2, fy1+w, fz2, fx2, fy2-w, fz2);
        //up
        quad_line(bufferBuilder, w,0,0, fx1, fy2, fz1,  fx1, fy2, fz2);
        quad_line(bufferBuilder, -w,0,0, fx2, fy2, fz1,  fx2, fy2, fz2);
        quad_line(bufferBuilder, 0,0,w, fx1+w, fy2, fz1,  fx2-w, fy2, fz1);
        quad_line(bufferBuilder, 0,0,-w, fx1+w, fy2, fz2,  fx2-w, fy2, fz2);
        //down
        quad_line(bufferBuilder, w,0,0, fx1, fy1, fz1,  fx1, fy1, fz2);
        quad_line(bufferBuilder, -w,0,0, fx2, fy1, fz1,  fx2, fy1, fz2);
        quad_line(bufferBuilder, 0,0,w, fx1+w, fy1, fz1,  fx2-w, fy1, fz1);
        quad_line(bufferBuilder, 0,0,-w, fx1+w, fy1, fz2,  fx2-w, fy1, fz2);
        //east
        quad_line(bufferBuilder, 0,w,0, fx2, fy1, fz1,  fx2, fy1, fz2);
        quad_line(bufferBuilder, 0,-w,0, fx2, fy2, fz1,  fx2, fy2, fz2);
        quad_line(bufferBuilder, 0,0,w, fx2, fy1+w, fz1,  fx2, fy2-w, fz1);
        quad_line(bufferBuilder, 0,0,-w, fx2, fy1+w, fz2,  fx2, fy2-w, fz2);
        //west
        quad_line(bufferBuilder, 0,w,0, fx1, fy1, fz1,  fx1, fy1, fz2);
        quad_line(bufferBuilder, 0,-w,0, fx1, fy2, fz1,  fx1, fy2, fz2);
        quad_line(bufferBuilder, 0,0,w, fx1, fy1+w, fz1,  fx1, fy2-w, fz1);
        quad_line(bufferBuilder, 0,0,-w, fx1, fy1+w, fz2,  fx1, fy2-w, fz2);

    }

    private static void quad_line(BufferBuilder bufferBuilder,float wx,float wy,float wz,double lx1, double ly1,double lz1,double lx2, double ly2,double lz2){

        bufferBuilder.vertex(lx1, ly1, lz1).uv(0,0).endVertex();
        bufferBuilder.vertex(lx2, ly2, lz2).uv(1,0).endVertex();
        bufferBuilder.vertex(lx2+wx, ly2+wy, lz2+wz).uv(1,1).endVertex();
        bufferBuilder.vertex(lx1+wx, ly1+wy, lz1+wz).uv(0,1).endVertex();
    }

    private static void player_facing_line(BufferBuilder bufferBuilder,double cx,double cy,double cz,double lx1, double ly1,double lz1,double lx2, double ly2,double lz2,Color c){

        float w=0.05f;

        double p1x=cx-lx1;
        double p1y=cy-ly1;
        double p1z=cz-lz1;

        double p2x=lx2-lx1;
        double p2y=ly2-ly1;
        double p2z=lz2-lz1;

        //cross product
        double nx = p2y * p1z - p2z * p1y;
        double ny = p2z * p1x - p2x * p1z;
        double nz = p2x * p1y - p2y * p1x;
        double l=Math.sqrt(nx*nx+ny*ny+nz*nz);
        if(l!=0){
            nx=(nx/l)*w;
            ny=(ny/l)*w;
            nz=(nz/l)*w;
        }
        MCVer.inst.set_texture(LINE_TEXTURE);
        MCVer.inst.set_color(c.r,c.g,c.b,c.a);
        bufferBuilder.vertex(lx1-nx, ly1-ny, lz1-nz).uv(0,0).endVertex();
        bufferBuilder.vertex(lx2-nx, ly2-ny, lz2-nz).uv(1,0).endVertex();
        bufferBuilder.vertex(lx2+nx, ly2+ny, lz2+nz).uv(1,1).endVertex();
        bufferBuilder.vertex(lx1+nx, ly1+ny, lz1+nz).uv(0,1).endVertex();

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

    static void render_shape(Tesselator tesselator,BufferBuilder bufferBuilder,BlockState state,double x, double y,double z){
        float vx=0;
        float vy=0;
        float vz=0;
        float u=0;
        float v=0;
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        BakedModel bakedModel = blockRenderer.getBlockModel(state);
        //TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        if(bakedModel!=null) {
            for(Direction dir: dirs) {
                List<BakedQuad> bake_list = bakedModel.getQuads(state, dir, random);

                if (!bake_list.isEmpty()) {
                    for (BakedQuad quad : bake_list) {
                        MCVer.inst.set_texture(TextureAtlas.LOCATION_BLOCKS);
                        int[] verts = quad.getVertices();
                        Vec3i vec3i = quad.getDirection().getNormal();
                        Vector3f normal = new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
                        int n=verts.length/4;
                        //WandsMod.log("vers length: "+verts.length+" n: "+n,prnt);
                        for (int i = 0; i < 4; ++i) {
                            int j = i * n;
                            vx = Float.intBitsToFloat(verts[j    ]);
                            vy = Float.intBitsToFloat(verts[j + 1]);
                            vz = Float.intBitsToFloat(verts[j + 2]);
                            u  = Float.intBitsToFloat(verts[j + 4]);
                            v  = Float.intBitsToFloat(verts[j + 5]);

                            bufferBuilder
                                    .vertex(x + vx,y + vy,z + vz)
                                    .uv(u, v)
                                    .color(block_col.r, block_col.g, block_col.b, block_col.a)
                                    .normal(normal.x(),normal.y(),normal.z())
                                    .endVertex();
                        }
                    }
                }
            }
        }
    }
    static void update_colors(){
        if(WandsConfig.colors.get(Colors.BLOCK_OUTLINE)!=null)
            bo_col  = WandsConfig.colors.get(Colors.BLOCK_OUTLINE);

        if(WandsConfig.colors.get(Colors.BOUNDING_BOX)!=null)
            bbox_col= WandsConfig.colors.get(Colors.BOUNDING_BOX);

        if(WandsConfig.colors.get(Colors.DESTROY)!=null)
            destroy_col= WandsConfig.colors.get(Colors.DESTROY);

        if(WandsConfig.colors.get(Colors.TOOL_USE)!=null)
            tool_use_col= WandsConfig.colors.get(Colors.TOOL_USE);

        if(WandsConfig.colors.get(Colors.START)!=null)
            start_col= WandsConfig.colors.get(Colors.START);

        if(WandsConfig.colors.get(Colors.END)!=null)
            end_col= WandsConfig.colors.get(Colors.END);

        if(WandsConfig.colors.get(Colors.PASTE_BB)!=null)
            paste_bb_col= WandsConfig.colors.get(Colors.PASTE_BB);

        if(WandsConfig.colors.get(Colors.BlOCK)!=null)
            block_col= WandsConfig.colors.get(Colors.BlOCK);

        if(WandsConfig.colors.get(Colors.LINE)!=null)
            line_col = WandsConfig.colors.get(Colors.LINE);
    }
}

