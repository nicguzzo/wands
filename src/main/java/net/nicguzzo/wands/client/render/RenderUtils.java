package net.nicguzzo.wands.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.utils.Colorf;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandProps;
import net.nicguzzo.wands.wand.WandProps.Mode;
import org.joml.Matrix4f;
//?if <26.1{
/*import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
*///?}
//?if <1.21.11{
/*import net.minecraft.client.resources.model.BakedModel;
*///?}
//?if >= 26.1 {
import net.minecraft.client.resources.model.geometry.BakedQuad;
//?}

import java.util.List;

public class RenderUtils {
    public static float lines_width = 0.05f;
    static public RandomSource random;
    public static Wand wand;
    static float opacity = 0.8f;
    //?if <=1.21.1 {
    /*private static final float[] brightness=new float[]{1.0f, 1.0f, 1.0f, 1.0f};
    private static final int[] lightmap= new int[]{255,255,255,255};
    *///?}
    static Direction[] dirs = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null};
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
    static void preview_block(Matrix4f matrix, VertexConsumer consumer, float fx1, float fy1, float fz1, float fx2, float fy2, float fz2, Colorf c, boolean cross) {
        float off=0.01f;
        fx1 -= off;
        fy1 -= off;
        fz1 -= off;
        fx2 += off;
        fy2 += off;
        fz2 += off;
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
    public static void player_facing_line(VertexConsumer consumer,Matrix4f matrix,float lx1, float ly1,float lz1,float lx2, float ly2,float lz2,Colorf c){

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
    //? if>=26.1 {
    static void render_shape(PoseStack matrixStack, VertexConsumer consumer, BlockState state, double x, double y, double z) {
        net.minecraft.client.renderer.block.dispatch.BlockStateModel bakedModel;
        try {
            Minecraft client=Minecraft.getInstance();
            bakedModel = client.getModelManager().getBlockStateModelSet().get(state);
            java.util.List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart> parts_list = new java.util.ArrayList<>();
            bakedModel.collectParts(random, parts_list);

            if (!parts_list.isEmpty() ) {
                matrixStack.pushPose();
                if(wand.mode!= WandProps.Mode.COPY ){
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
                for (net.minecraft.client.renderer.block.dispatch.BlockStateModelPart part: parts_list) {
                    for(Direction dir: dirs) {
                        List<BakedQuad> bake_list = part.getQuads(dir);
                        for (net.minecraft.client.resources.model.geometry.BakedQuad quad : bake_list) {
                            int kk = -1;
                            if (quad.materialInfo().isTinted()) {
                                net.minecraft.client.color.block.BlockTintSource tintSource = client.getBlockColors().getTintSource(state, quad.materialInfo().tintIndex());
                                if (tintSource != null) {
                                    kk = tintSource.color(state);
                                }
                            }
                            float ff = (float) (kk >> 16 & 0xFF) / 255.0F;
                            float gg = (float) (kk >> 8 & 0xFF) / 255.0F;
                            float hh = (float) (kk & 0xFF) / 255.0F;
                            float k = 1.0F;
                            float l = 1.0F;
                            float m = 1.0F;
                            if (kk != -1) {
                                k = Mth.clamp(ff, 0.0F, 1.0F);
                                l = Mth.clamp(gg, 0.0F, 1.0F);
                                m = Mth.clamp(hh, 0.0F, 1.0F);
                            }
                            com.mojang.blaze3d.vertex.QuadInstance qinst = new com.mojang.blaze3d.vertex.QuadInstance();
                            int a = (int)(opacity * 255.0F);
                            int r = (int)(k * 255.0F);
                            int g = (int)(l * 255.0F);
                            int b = (int)(m * 255.0F);
                            int color = (a << 24) | (r << 16) | (g << 8) | b;
                            qinst.setColor(color);
                            qinst.setLightCoords(15728880);
                            qinst.setOverlayCoords(OverlayTexture.NO_OVERLAY);
                            consumer.putBakedQuad(matrixStack.last(), quad, qinst);
                        }
                    }
                }
                matrixStack.popPose();
            }
        } catch (Exception e) {
            //WandsMod.log("render_shape error "+e.toString(),false);
        }
    }
//?}

//? if>=1.21.11 <26.1{
    /*static void render_shape(PoseStack matrixStack,VertexConsumer consumer,BlockState state,double x, double y,double z){
        net.minecraft.client.renderer.block.model.BlockStateModel bakedModel;
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        Minecraft client = Minecraft.getInstance();
        try {
            bakedModel = blockRenderer.getBlockModel(state);
            java.util.List<net.minecraft.client.renderer.block.model.BlockModelPart> parts_list = bakedModel.collectParts(random);

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
                for (net.minecraft.client.renderer.block.model.BlockModelPart part: parts_list) {
                    for(Direction dir: dirs) {
                        List<BakedQuad> bake_list = part.getQuads(dir);
                        for (BakedQuad quad : bake_list) {
                            //if(wand.replace ||
                            //        Block.shouldRenderFace( state, wand.level.getBlockState(bp.relative(dir)),dir )
                            //)
                            {
                                //quad.sprite().atlasLocation().
                                net.minecraft.client.renderer.texture.TextureManager textureManager = Minecraft.getInstance().getTextureManager();
                                net.minecraft.client.renderer.texture.AbstractTexture abstractTexture = textureManager.getTexture(quad.sprite().atlasLocation());

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
                                //WandsMod.log("consumer.putBulkData",false);
                                consumer.putBulkData(matrixStack.last(), quad, k, l, m, opacity, 15728880, OverlayTexture.NO_OVERLAY);
                            }
                        }
                    }
                }
                matrixStack.popPose();
            }
        } catch (Exception e) {
            WandsMod.log("render_shape error "+e.toString(),false);
            //WandsMod.log("couldn't get model, blacklisting block...", true);
        }
    }
*///?}
//?if >=1.21.1 <1.21.11{
    /*static void render_shape(PoseStack matrixStack,VertexConsumer consumer,BlockState state,double x, double y,double z){
            BakedModel bakedModel;
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
            Minecraft client = Minecraft.getInstance();
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
                            //WandsMod.log("consumer.putBulkData",false);
                            consumer.putBulkData(matrixStack.last(), quad, k, l, m, opacity, 15728880, OverlayTexture.NO_OVERLAY);
                        }
                    }
                }
                matrixStack.popPose();
            } catch (Exception e) {
                WandsMod.log("render_shape error "+e.toString(),false);
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
            Minecraft client=Minecraft.getInstance();
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
            WandsMod.log("render_shape error "+e.toString(),true);
            //WandsMod.log("couldn't get model, blacklisting block...", true);
        }
    }
*///?}
}
