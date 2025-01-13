package net.nicguzzo.wands.mixin;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;

import net.minecraft.client.renderer.debug.DebugRenderer;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.WandsMod;

import com.mojang.blaze3d.vertex.PoseStack;

#if MC>="1212"
import net.minecraft.client.renderer.culling.Frustum;
#endif

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class RenderWorldMixin {
    
    @Inject(method = "render", at = @At(value = "HEAD"))
    #if MC>="1212"
    public void render(PoseStack matrices, Frustum frustum, MultiBufferSource.BufferSource bufferIn, double d, double e, double f, CallbackInfo ci){
    #else
    public void render(PoseStack matrices, BufferSource bufferIn, double camX, double camY, double camZ, CallbackInfo ci) {
    #endif
        if(!WandsMod.config.render_last)
        {
            ClientRender.render(matrices,bufferIn);
        }
    }
}