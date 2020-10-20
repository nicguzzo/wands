package net.nicguzzo.mixin;

import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.math.Matrix4f;

import net.nicguzzo.WandsClientMod;

/*@Mixin(GameRenderer.class)
public abstract class RenderWorldMixin {

    @Inject(method="renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V",
            at=@At(value="INVOKE_STRING",
                   target="Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
                   args= { "ldc=hand" }
            )
    )
    
    private void onRenderWorld(float partialTicks, long nanoTime, MatrixStack matrixStack, CallbackInfo ci) {
        WandsClientMod.render(partialTicks,matrixStack);
    }

}*/

@Mixin(WorldRenderer.class)
public class RenderWorldMixin {
    @Inject(method = "render", at = @At(value = "INVOKE",
                                        target = "Lnet/minecraft/client/render/debug/DebugRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;DDD)V",
                                        ordinal = 0))
    public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        WandsClientMod.render(tickDelta,matrices,camera);
        
    }
}