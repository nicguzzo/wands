package net.nicguzzo.wands.mixin;
#if MC >= "1200"

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.utils.WandUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Shadow
    PoseStack pose;

    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "TAIL"))
    public void renderItemDecorations(Font font, ItemStack itemStack, int i, int j, String string, CallbackInfo cb) {
       if (WandUtils.is_wand(itemStack)) {
           if (ClientRender.wand != null) {
               ListTag tools = itemStack.getOrCreateTag().getList("Tools", Compat.NbtType.COMPOUND);
               int n = tools.size();
               if (n > 0) {
                   pose.translate(0.0f, 0.0f, 200.0f);
                   String str=String.valueOf(n);
                   MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                   font.drawInBatch(str,
                           (float)(i +  6 - font.width(str)),
                           (float)(j + 7),
                           0xFFFFFF,
                           true,
                           pose.last().pose(),
                           (MultiBufferSource)bufferSource,
                           Font.DisplayMode.NORMAL,
                           0,
                           0xF000F0);
                   bufferSource.endBatch();
               }
           }
       }
   }
}
#else
    import org.spongepowered.asm.mixin.Mixin;
    import net.minecraft.client.renderer.entity.ItemRenderer;
    @Mixin(ItemRenderer.class)
    public abstract class GuiGraphicsMixin {

    }
#endif