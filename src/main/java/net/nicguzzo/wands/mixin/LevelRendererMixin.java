package net.nicguzzo.wands.mixin;

//?if >=26.2{
import net.minecraft.client.renderer.SubmitNodeStorage;
//?}
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
import net.minecraft.client.renderer.state.level.CameraRenderState;
//?}

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.LevelRenderer;
import net.nicguzzo.wands.client.render.ClientRender;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//?if < 26.2{
/*import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
*///?}

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    //?if < 26.2{
    /*@Shadow
    private RenderBuffers renderBuffers;
    *///?}
    
    @Unique
    private final PoseStack posestack = new PoseStack();
    
    //?if >= 26.2{
    @Shadow
    private SubmitNodeStorage submitNodeStorage;

    @Inject(method = "render", at = @At(value = "TAIL"))
    public void render(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci){
        posestack.setIdentity();
        if(ClientRender.wand != null) {
            ClientRender.render(posestack, submitNodeStorage);
        }
    }
    //?}
    //?if = 26.1.2{
    /*@Inject(method = "renderLevel", at = @At(value = "TAIL"))
    public void renderLevel(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, net.minecraft.client.renderer.chunk.ChunkSectionsToRender chunkSectionsToRender, CallbackInfo ci){
        posestack.setIdentity();
        posestack.mulPose(modelViewMatrix);
        if(ClientRender.wand != null) {
            ClientRender.render(posestack, renderBuffers.bufferSource());
        }
    }
    *///?}
    //?if >= 1.21.11 < 26.1{
    /*@Inject(method = "renderLevel", at = @At(value = "TAIL"))
    public void renderLevel(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci){
        posestack.setIdentity();
        posestack.mulPose(matrix4f);
        if(ClientRender.wand != null) {
            ClientRender.render(posestack, renderBuffers.bufferSource());
        }
    }
    *///?}
    //?if > 1.20.1 < 1.21.11{
    /*@Inject(method = "renderLevel", at = @At(value = "TAIL"))
    public void renderLevel(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        posestack.setIdentity();
        posestack.mulPose(matrix4f);
        if(ClientRender.wand != null) {
            ClientRender.render(posestack, renderBuffers.bufferSource());
        }
    }
    *///?}
    //?if <= 1.20.1{
    /*@Inject(method = "renderLevel", at = @At(value = "TAIL"))
    public void renderLevel(PoseStack posestack_in, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        this.posestack.setIdentity();
        if(ClientRender.wand != null) {
            ClientRender.render(this.posestack, renderBuffers.bufferSource());
        }
    }
    *///?}
}
