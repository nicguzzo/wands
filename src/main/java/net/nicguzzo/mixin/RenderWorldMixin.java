package net.nicguzzo.mixin;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import net.nicguzzo.WandsMod;

@Mixin(GameRenderer.class)
public abstract class RenderWorldMixin {

    @Inject(method="renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V",
            at=@At(value="INVOKE_STRING",
                   target="Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
                   args= { "ldc=hand" }
            )
    )
    
    private void onRenderWorld(float partialTicks, long nanoTime, MatrixStack matrixStack, CallbackInfo ci) {
        WandsMod.render(partialTicks,matrixStack);
    }

}
