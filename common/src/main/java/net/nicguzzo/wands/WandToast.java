package net.nicguzzo.wands;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.mcver.MCVer;

public class WandToast implements Toast {
    Component text;
    WandToast(String s){
        text=MCVer.inst.literal(s);
    }
    @Override
    public Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l) {
        Minecraft client=Minecraft.getInstance();
        MCVer.inst.set_pos_tex_shader();
        MCVer.inst.set_texture(TEXTURE);
        MCVer.inst.set_color(1.0F, 1.0F, 1.0F, 1.0F);
        //RenderSystem.setShader(GameRenderer::getPositionTexShader);
        //RenderSystem.setShaderTexture(0, TEXTURE);
        //RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableTexture();
        toastComponent.blit(poseStack, 0, 0, 0, 96, this.width(), this.height());
        if(client.player!=null) {
            ItemStack s = client.player.getMainHandItem();
            client.getItemRenderer().renderAndDecorateItem(s,10,6);
        }
        client.font.draw(poseStack,text ,30.0F, 12.0F, -11534256);
        return l >= 1000L ? Visibility.HIDE : Visibility.SHOW;
    }
/*
    @Override
    public Object getToken() {
        return Toast.super.getToken();
    }

    @Override
    public int width() {
        return Toast.super.width();
    }

    @Override
    public int height() {
        return Toast.super.height();
    }*/
}
