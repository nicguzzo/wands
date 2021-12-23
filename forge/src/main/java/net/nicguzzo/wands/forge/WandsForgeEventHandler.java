package net.nicguzzo.wands.forge;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.nicguzzo.wands.ClientRender;

@OnlyIn(Dist.CLIENT)
public class WandsForgeEventHandler {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderWorldLast(RenderLevelLastEvent event) {
        ClientRender.render(event.getPoseStack());
    }
}
