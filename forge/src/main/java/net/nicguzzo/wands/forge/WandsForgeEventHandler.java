package net.nicguzzo.wands.forge;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.nicguzzo.wands.ClientRender;
import net.nicguzzo.wands.WandsMod;

@OnlyIn(Dist.CLIENT)
public class WandsForgeEventHandler {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderWorldLast(RenderLevelLastEvent event) {
        if(WandsMod.config.render_last) {
            //ClientRender.render(event.getPoseStack());
        }
    }
}
