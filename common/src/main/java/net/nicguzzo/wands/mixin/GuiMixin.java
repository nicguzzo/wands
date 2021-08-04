package net.nicguzzo.wands.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.WandItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.nicguzzo.wands.WandsMod;

@Mixin(Gui.class)
public abstract class GuiMixin {

    Minecraft client=null;
    @Inject(at = @At("TAIL"), method = "render")
    public void render(PoseStack poseStack, float f,CallbackInfo info) {
        if(client==null) {
            client = Minecraft.getInstance();
        }
        ItemStack stack=client.player.getMainHandItem();
        if(stack!=null && !stack.isEmpty() && stack.getItem() instanceof WandItem){
            int screenWidth =client.getWindow().getGuiScaledWidth();
            int screenHeight = client.getWindow().getGuiScaledHeight();
            Font font = client.font;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            String msg="Mode: "+WandItem.getModeString(stack);
            //int w=font.width(msg);
            int h=font.lineHeight;
            float x=(int)(screenWidth* (((float)WandsMod.config.wand_mode_display_x_pos)/100.0f));
            float y=(int)((screenHeight-h)* (((float)WandsMod.config.wand_mode_display_y_pos)/100.0f));
            font.draw(poseStack,msg,x,y,0xffffff);
        }
    }
}
