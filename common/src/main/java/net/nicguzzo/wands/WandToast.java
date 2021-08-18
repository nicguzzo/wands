package net.nicguzzo.wands;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;

public class WandToast implements Toast {
    @Override
    public Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l) {
        Minecraft client=Minecraft.getInstance();
        ItemStack item_stack=client.player.getMainHandItem();
        if(item_stack!=null && item_stack.getItem() instanceof WandItem){
            WandItem.Mode mode=WandItem.getMode(item_stack);
            //RenderSystem.setShader(GameRenderer::getPositionTexShader);
            //RenderSystem.setShaderTexture(0, TEXTURE);
            //RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            return l >= 5000L ? Visibility.HIDE : Visibility.SHOW;
        }
        return Visibility.HIDE;
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
