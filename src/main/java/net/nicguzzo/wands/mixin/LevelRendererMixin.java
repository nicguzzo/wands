package net.nicguzzo.wands.mixin;

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
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.CameraRenderState;
//?}


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderBuffers;
import org.joml.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.WandsMod;

import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Shadow
    RenderBuffers renderBuffers;

    @Unique
    PoseStack posestack = new PoseStack();

    @Inject(method = "renderLevel", at = @At(value = "TAIL"))

    //?if >=26.1{
        public void renderLevel(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, ChunkSectionsToRender chunkSectionsToRender, CallbackInfo ci){
            posestack.setIdentity();
            posestack.mulPose(modelViewMatrix);
    //?}else{
        /*//?if >=1.21.11{
          public void renderLevel(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci){
              posestack.setIdentity();
              posestack.mulPose(matrix4f);
        //?}else{
            /^//?if >=1.21{
            public void renderLevel(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
                posestack.setIdentity();
                posestack.mulPose(matrix4f);
            //?}else{
                /^¹public void renderLevel(PoseStack posestack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
            ¹^///?}
        ^///?}
    *///?}
        ClientRender.render(posestack,renderBuffers.bufferSource());
    }
}
