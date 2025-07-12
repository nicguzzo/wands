package net.nicguzzo.wands.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.renderer.RenderBuffers;
import org.joml.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.WandsMod;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Shadow
    RenderBuffers renderBuffers;

    @Inject(method = "renderLevel", at = @At(value = "TAIL"))
    public void renderLevel(
            GraphicsResourceAllocator graphicsResourceAllocator,
            DeltaTracker deltaTracker,
            boolean bl,
            Camera camera,
            Matrix4f matrix4f,
            Matrix4f matrix4f2,
            GpuBufferSlice gpuBufferSlice,
            Vector4f vector4f,
            boolean bl2,
            CallbackInfo ci)
    {
        if(WandsMod.config.render_last) {
            PoseStack posestack = new PoseStack();
            posestack.mulPose(matrix4f);
            ClientRender.render(posestack,renderBuffers.bufferSource());
        }
    }
}
