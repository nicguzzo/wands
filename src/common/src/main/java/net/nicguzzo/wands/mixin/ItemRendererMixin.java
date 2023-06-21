package net.nicguzzo.wands.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.utils.WandUtils;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

#if MC <= "1193"
import net.minecraft.client.renderer.block.model.ItemTransforms;
#else
import net.minecraft.world.item.ItemDisplayContext;
#endif

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    ItemModelShaper itemModelShaper;
    #if MC <= "1193"
    @Shadow
    public float blitOffset;
    @Shadow
    public abstract void render(ItemStack itemStack, ItemTransforms.TransformType transformType, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel);
    #else
    @Shadow
    public abstract void render(ItemStack itemStack, ItemDisplayContext itemDisplayContext, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel);
    #endif

    @Inject(method = "render", at = @At(value = "TAIL"))
    #if MC <= "1193"
    public void renderM(ItemStack itemStack, ItemTransforms.TransformType transformType, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel, CallbackInfo cb) {
    #else
    public void render(ItemStack itemStack, ItemDisplayContext itemDisplayContext, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel, CallbackInfo cb) {
    #endif
        if (WandUtils.is_magicbag(itemStack)) {
            ItemStack item_in_bag = MagicBagItem.getItem(itemStack);
            if (!item_in_bag.isEmpty()) {
                BakedModel bakedModel2 = this.itemModelShaper.getItemModel(item_in_bag);
                if (bakedModel2 != null) {
                    poseStack.pushPose();
                    poseStack.scale(0.5f, 0.5f, 0.5f);
                    poseStack.translate(0, -0.3, 0.2);
                    RenderSystem.disableDepthTest();
                    #if MC <= "1193"
                        this.render(item_in_bag, ItemTransforms.TransformType.GUI, false, poseStack, multiBufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, bakedModel2);
                    #else
                        this.render(item_in_bag, ItemDisplayContext.GUI, false, poseStack, multiBufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, bakedModel2);
                    #endif
                    RenderSystem.enableDepthTest();
                    poseStack.popPose();
                }
            }
        }
    }
    #if MC < "1200"
        #if MC <= "1193"
        @Inject(method = "renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "TAIL"))
        public void renderGuiItemDecorations(Font font, ItemStack itemStack, int i, int j, String string, CallbackInfo cb) {
        #else
        @Inject(method = "renderGuiItemDecorations(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "TAIL"))
        public void renderGuiItemDecorations(PoseStack poseStack, Font font, ItemStack itemStack, int i, int j, String string,CallbackInfo cb) {
        #endif
            if (WandUtils.is_wand(itemStack)) {
                if (ClientRender.wand != null) {
                    ListTag tools = itemStack.getOrCreateTag().getList("Tools", Compat.NbtType.COMPOUND);
                    int n = tools.size();
                    if (n > 0) {
                        #if MC <= "1193"
                        PoseStack poseStack = new PoseStack();
                        poseStack.translate(0.0f, 0.0f, this.blitOffset + 200.0f);
                        String str=String.valueOf(n);
                        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                        font.drawInBatch(str, (float)(i +  6 - font.width(str)), (float)(j + 2), 0xFFFFFF, true, poseStack.last().pose(), (MultiBufferSource)bufferSource, false, 0, 0xF000F0);
                        bufferSource.endBatch();
                        #else
                        poseStack.translate(0.0f, 0.0f, 200.0f);
                        String str=String.valueOf(n);
                        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                        font.drawInBatch(str,
                                (float)(i +  6 - font.width(str)),
                                (float)(j + 2),
                                0xFFFFFF,
                                true,
                                poseStack.last().pose(),
                                (MultiBufferSource)bufferSource,
                                Font.DisplayMode.NORMAL,
                                0,
                                0xF000F0);

                        bufferSource.endBatch();
                        #endif
                    }
                }
            }
        }
    #endif
}
