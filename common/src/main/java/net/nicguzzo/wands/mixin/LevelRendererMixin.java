package net.nicguzzo.wands.mixin;


import com.mojang.blaze3d.vertex.PoseStack;
#if MC>="1193"
#if MC>="1210"
import net.minecraft.client.DeltaTracker;
#endif
#if MC>="1212"
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
#endif
import net.minecraft.client.renderer.RenderBuffers;
import org.joml.Matrix4f;
#else
import com.mojang.math.Matrix4f;
#endif

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.nicguzzo.wands.client.render.ClientRender;

import net.nicguzzo.wands.WandsMod;

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
    public void renderLevel(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        if(WandsMod.config.render_last) {
            ClientRender.render(poseStack,renderBuffers.bufferSource());
        }
    }

}
