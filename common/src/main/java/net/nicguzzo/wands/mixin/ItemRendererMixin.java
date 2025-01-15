package net.nicguzzo.wands.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.component.DataComponents;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.utils.WandUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.item.ItemDisplayContext;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow public abstract void renderStatic(ItemStack itemStack, ItemDisplayContext itemDisplayContext, int i, int j, PoseStack poseStack, MultiBufferSource multiBufferSource, @Nullable Level level, int k);

    @Inject(method = "renderStatic", at = @At(value = "TAIL"))
    public void renderStatic(ItemStack itemStack, ItemDisplayContext itemDisplayContext, int i, int j, PoseStack poseStack, MultiBufferSource multiBufferSource, @Nullable Level level, int k, CallbackInfo cb){

        if (WandUtils.is_magicbag(itemStack)) {
            ItemStack item_in_bag = MagicBagItem.getItem(itemStack);
            if (!item_in_bag.isEmpty()) {
                //BakedModel bakedModel2 = this.itemModelShaper.getItemModel(item_in_bag);

                if (itemStack.get(DataComponents.ITEM_MODEL) != null) {
                    poseStack.pushPose();
                    poseStack.scale(0.5f, 0.5f, 0.5f);
                    poseStack.translate(0, -0.3, 0.2);
                    RenderSystem.disableDepthTest();

                    this.renderStatic(itemStack,itemDisplayContext,i,j,poseStack,multiBufferSource,level,k);

                    RenderSystem.enableDepthTest();
                    poseStack.popPose();
                }
            }
        }
    }

}
