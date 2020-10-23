package net.nicguzzo.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.nicguzzo.WandsRendererForge;


@Mixin(DebugRenderer.class)
public class RenderWorldMixin {
    
    @Inject(method = "render", at = @At(value = "TAIL")) 
    public void render(MatrixStack matrices, IRenderTypeBuffer.Impl bufferIn, double camX, double camY, double camZ, CallbackInfo ci) {
        WandsRendererForge.render(matrices,camX, camY, camZ);
    }
}