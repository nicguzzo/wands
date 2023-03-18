package net.nicguzzo.wands.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.utils.Compat;

public class WandToast implements Toast {
    Component text;
    WandToast(String s){
        text= Compat.literal(s);
    }
    @Override
    public Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l) {
        Minecraft client=Minecraft.getInstance();
        Compat.set_pos_tex_shader();
        Compat.set_texture(TEXTURE);
        Compat.set_color(1.0F, 1.0F, 1.0F, 1.0F);
        #if MC <= "1193"
        RenderSystem.enableTexture();
        #endif
        toastComponent.blit(poseStack, 0, 0, 0, 96, this.width(), this.height());
        if(client.player!=null) {
            ItemStack s = client.player.getMainHandItem();
            #if MC <= "1193"
            client.getItemRenderer().renderAndDecorateItem(s,10,6);
            #else
            client.getItemRenderer().renderAndDecorateItem(poseStack,s,10,6);
            #endif
        }
        client.font.draw(poseStack,text ,30.0F, 12.0F, -11534256);
        return l >= 1000L ? Visibility.HIDE : Visibility.SHOW;
    }
}
