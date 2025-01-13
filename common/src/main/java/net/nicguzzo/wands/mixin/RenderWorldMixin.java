package net.nicguzzo.wands.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.render.ClientRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class RenderWorldMixin {

    @Inject(method = "render", at = @At(value = "HEAD"))

    public void render(PoseStack matrices, BufferSource bufferIn, double camX, double camY, double camZ, CallbackInfo ci) {
        if (!WandsMod.config.render_last) {
            ClientRender.render(matrices);
        }
    }
}