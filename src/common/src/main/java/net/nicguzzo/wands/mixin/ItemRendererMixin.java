package net.nicguzzo.wands.mixin;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.utils.WandUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.logging.Level;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    ItemModelShaper itemModelShaper;
    @Shadow
    public abstract void render(ItemStack itemStack, ItemTransforms.TransformType transformType, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel);

     @Inject(method ="render", at = @At(value = "TAIL"))
     public void renderM(ItemStack itemStack, ItemTransforms.TransformType transformType, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel, CallbackInfo cb){
        if(WandUtils.is_magicbag(itemStack)){
            ItemStack item_in_bag= MagicBagItem.getItem(itemStack);
            if(!item_in_bag.isEmpty()){
                BakedModel bakedModel2 = this.itemModelShaper.getItemModel(item_in_bag);
                if(bakedModel2!=null) {
                    poseStack.pushPose();
                    poseStack.scale(0.5f, 0.5f, 0.5f);
                    poseStack.translate(0,0,0.2);
                    RenderSystem.disableDepthTest();
                    this.render(item_in_bag, ItemTransforms.TransformType.GUI, false, poseStack, multiBufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, bakedModel2);
                    RenderSystem.enableDepthTest();
                    poseStack.popPose();
                }
            }
        }
     }
}
