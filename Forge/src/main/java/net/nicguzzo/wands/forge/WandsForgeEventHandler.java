package net.nicguzzo.wands.forge;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.nicguzzo.wands.ClientRender;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.WandsModClient;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class WandsForgeEventHandler {
    static boolean optifine_check=false;
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
       /* if(!optifine_check){
            for (ModFileScanData scanData:ModList.get().getAllScanData()) {
                WandsMod.log("scanData mod: "+scanData.toString(),true);
            }
            optifine_check=true;
            for (ModInfo mod:ModList.get().getMods()) {
                WandsMod.log("has mod: "+mod.getModId(),true);
            }
            if(ModList.get().isLoaded("optifine")){
                WandsModClient.has_optifine=true;
                WandsMod.log("has optifine!!!",true);
            }else{
                WandsMod.log("no optifine!!!",true);
            }
        }*/
        //System.out.println("forge render!!");
        /*Minecraft client = Minecraft.getInstance();
        LocalPlayer player=client.player;

        //beginMC1_16_5
        PoseStack poseStack=event.getMatrixStack();
        Camera camera = client.gameRenderer.getMainCamera();
        Vec3 c = camera.getPosition();
        poseStack.translate(-c.x, -c.y, -c.z); // translate

        GlStateManager._pushMatrix();
        RenderSystem.multMatrix(poseStack.last().pose());*/
        ClientRender.render(event.getMatrixStack(), 0, 0, 0, Minecraft.getInstance().renderBuffers().bufferSource());
        //GlStateManager._popMatrix();

        //poseStack2.popPose();
        //RenderSystem.applyModelViewMatrix();
        //endMC1_16_5

        /*//beginMC1_17_1
        PoseStack poseStack=event.getMatrixStack();
        PoseStack poseStack2 = RenderSystem.getModelViewStack();
        poseStack2.pushPose();
        poseStack2.mulPoseMatrix(poseStack.last().pose());
        RenderSystem.applyModelViewMatrix();
        ClientRender.render(event.getMatrixStack(), 0, 0, 0, Minecraft.getInstance().renderBuffers().bufferSource());
        poseStack2.popPose();
        RenderSystem.applyModelViewMatrix();
        //endMC1_17_1*/
    }
    /*@OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void modsLoaded(FMLClientSetupEvent event) {
        WandsMod.log("checking for optifine!!!",true);
        if(ModList.get().isLoaded("optifine")){
            WandsModClient.has_optifine=true;
            WandsMod.log("has optifine!!!",true);
        }else{
            WandsMod.log("no optifine!!!",true);
        }
    }*/
}
