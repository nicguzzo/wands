package net.nicguzzo.wands.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;


import net.minecraft.client.gui.components.toasts.Toast;
#if MC<"1212"
import net.minecraft.client.gui.components.toasts.ToastComponent;
#else
    import net.minecraft.client.gui.Font;
    import net.minecraft.client.gui.components.toasts.ToastManager;
    import net.minecraft.client.renderer.RenderType;
#endif

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.utils.Compat;
#if MC >= "1200"
import net.minecraft.client.gui.GuiGraphics;
#endif

public class WandToast implements Toast {
    #if MC > "1201"
    private static final ResourceLocation TEXTURE = Compat.create_resource_mc("toast/advancement");
    #endif
    Component text;
    #if MC >= "1212"
     private Toast.Visibility wantedVisibility;
    #endif
    WandToast(String s){
        text= Compat.literal(s);
    }
    @Override
    #if MC < "1200"
    public Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l) {
    #else
    #if MC<"1212"
    public Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long l){
    #else
    public void render(GuiGraphics guiGraphics, Font font, long l){
    #endif
    #endif

        Minecraft client=Minecraft.getInstance();
        Compat.set_pos_tex_shader();

        Compat.set_color(1.0F, 1.0F, 1.0F, 1.0F);
        #if MC <= "1193"
        Compat.set_texture(TEXTURE);
        RenderSystem.enableTexture();
        #endif
        #if MC < "1200"
        Compat.set_texture(TEXTURE);
        toastComponent.blit(poseStack, 0, 0, 0, 96, this.width(), this.height());
        #else
        #if MC<"1212"
          //guiGraphics.blit(TEXTURE, 0, 0, 0, 96, this.width(), this.height());
            guiGraphics.blitSprite(TEXTURE, 0, 0, this.width(), this.height());
        #else
          guiGraphics.blit(RenderType::guiTextured,TEXTURE, 0, 0, 0, 96, 0, 0,this.width(), this.height());
        #endif
        #endif
        if(client.player!=null) {
            ItemStack s = client.player.getMainHandItem();
            #if MC < "1200"
                #if MC <= "1193"
                client.getItemRenderer().renderAndDecorateItem(s,10,6);
                #else
                client.getItemRenderer().renderAndDecorateItem(poseStack,s,10,6);
                #endif
            #else
                guiGraphics.renderFakeItem(s,10,6);
            #endif
        }
        #if MC < "1200"
            client.font.draw(poseStack,text ,30.0F, 12.0F, -11534256);
        #else
            guiGraphics.drawString(client.font,text ,30, 12, 0xffffffff,false);
        #endif
        #if MC<"1212"
        return l >= 1000L ? Visibility.HIDE : Visibility.SHOW;
        #endif
    }
    #if MC>="1212"
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
    #endif
}
