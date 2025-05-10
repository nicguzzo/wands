package net.nicguzzo.wands.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.utils.Compat;
import net.minecraft.client.gui.GuiGraphics;

public class WandToast implements Toast {
    private static final ResourceLocation TEXTURE = Compat.create_resource_mc("toast/advancement");
    Component text;
     private Toast.Visibility wantedVisibility;
    WandToast(String s){
        text= Compat.literal(s);
    }
    @Override
    public void render(GuiGraphics guiGraphics, Font font, long l){
        Minecraft client=Minecraft.getInstance();
        //Compat.set_pos_tex_shader();
        //Compat.set_color(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(RenderType::guiTextured,TEXTURE, 0, 0, 0, 96, 0, 0,this.width(), this.height());
        if(client.player!=null) {
            ItemStack s = client.player.getMainHandItem();
                guiGraphics.renderFakeItem(s,10,6);
        }
        guiGraphics.drawString(client.font,text ,30, 12, 0xffffffff,false);
    }
    public Toast.Visibility getWantedVisibility() {
        return this.wantedVisibility;
    }
    public void update(ToastManager toastManager, long l) {
        if(l >= 1000L){
            this.wantedVisibility = Visibility.HIDE;
        }else {
            this.wantedVisibility = Visibility.SHOW;
        }
    }
}
