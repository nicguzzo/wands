package net.nicguzzo.wands.mixin;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.WandsMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class RenderWorldMixin {
    
    //@Inject(method = "render", at = @At(value = "HEAD"))
    //public void render(PoseStack poseStack, Frustum frustum, MultiBufferSource.BufferSource bufferSource, double d, double e, double f, boolean bl, CallbackInfo ci){
    //    if(!WandsMod.config.render_last)
    //    {
    //        ClientRender.render(poseStack,bufferSource);
    //    }
    //}
}