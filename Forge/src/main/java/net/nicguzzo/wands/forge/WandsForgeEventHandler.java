package net.nicguzzo.wands.forge;

import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.nicguzzo.wands.ClientRender;

@OnlyIn(Dist.CLIENT)
public class WandsForgeEventHandler {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {        
        //System.out.println("forge render!!");
        PoseStack poseStack=event.getMatrixStack();
        PoseStack poseStack2 = RenderSystem.getModelViewStack();
        poseStack2.pushPose();
        poseStack2.mulPoseMatrix(poseStack.last().pose());
        RenderSystem.applyModelViewMatrix();
        ClientRender.render(event.getMatrixStack(), 0, 0, 0, null);

        poseStack2.popPose();
        RenderSystem.applyModelViewMatrix();
    }
    
}
