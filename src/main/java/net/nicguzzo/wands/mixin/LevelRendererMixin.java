package net.nicguzzo.wands.mixin;

//?if >=26.2{
/*import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.renderer.SubmitNodeStorage;

*///?}
//?if >=1.21.11{
import com.mojang.blaze3d.buffers.GpuBufferSlice;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
//?}
//?if >=1.21{
import net.minecraft.client.DeltaTracker;
//?}
//?if <26.1{
/*import net.minecraft.client.renderer.LightTexture;
*///?}else{
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.CameraRenderState;
//?}


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Brightness;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.WandsMod;

import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    //?if < 26.2{
    @Shadow
    RenderBuffers renderBuffers;
    //?}
    @Unique
    PoseStack posestack = new PoseStack();
    @Final
    @Shadow


    //?if = 26.2{
        /*private SubmitNodeStorage submitNodeStorage;
        @Inject(method = "render", at = @At(value = "TAIL"))
        public void render(
                GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci){
            posestack.setIdentity();
            //posestack.mulPose(modelViewMatrix);

            if(ClientRender.wand!=null /^&& ClientRender.wand.pos!=null^/) {
                ClientRender.render(posestack);
                Minecraft client = Minecraft.getInstance();
                BlockModelResolver resolver = new BlockModelResolver(client.getModelManager());
                BlockModelRenderState renderState = new BlockModelRenderState();

                Vec3 cameraPos = cameraState.pos;

                int block_buffer_length=ClientRender.wand.block_buffer.get_length();
                //WandsMod.log("=================================",true);
                for (int idx = 0; idx < block_buffer_length ; idx++) {
                    posestack.pushPose();
                    int x = ClientRender.wand.block_buffer.buffer_x[idx];
                    int y = ClientRender.wand.block_buffer.buffer_y[idx];
                    int z = ClientRender.wand.block_buffer.buffer_z[idx];
                    //WandsMod.log("x: "+x+" y: "+y+" z: "+z,true);

                    posestack.translate(
                            x - cameraPos.x,
                            y - cameraPos.y,
                            z - cameraPos.z
                    );
                    posestack.translate(0.05f,0.05f,0.05f);
                    posestack.scale(0.9f,0.9f,0.9f);

                    resolver.update(renderState, ClientRender.wand.block_buffer.state[idx], BlockDisplayContext.create());

                    int outlineColor = 0xFFFF3333;
                    renderState.submit(
                            posestack,
                            submitNodeStorage,
                            Brightness.FULL_BRIGHT.pack(),
                            OverlayTexture.NO_OVERLAY,
                            outlineColor);
                    posestack.popPose();
                }

            }

    *///?}
    //?if = 26.1.2{
        @Inject(method = "renderLevel", at = @At(value = "TAIL"))
        public void renderLevel(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, ChunkSectionsToRender chunkSectionsToRender, CallbackInfo ci){
            posestack.setIdentity();
            posestack.mulPose(modelViewMatrix);
    //?}
    //?if = 1.21.11{
        /*@Inject(method = "renderLevel", at = @At(value = "TAIL"))
        public void renderLevel(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci){
              posestack.setIdentity();
              posestack.mulPose(matrix4f);
    *///?}
    //?if = 1.21{
        /*@Inject(method = "renderLevel", at = @At(value = "TAIL"))
        public void renderLevel(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
                posestack.setIdentity();
                posestack.mulPose(matrix4f);
    *///?}
    //?if = 1.20.1{
        /*public void renderLevel(PoseStack posestack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
    *///?}

        //?if >= 26.2{
        /*ClientRender.render(posestack);
        *///?}else{
        ClientRender.render(posestack,renderBuffers.bufferSource());
        //?}
    }
}
