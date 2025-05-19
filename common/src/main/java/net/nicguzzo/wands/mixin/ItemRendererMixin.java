package net.nicguzzo.wands.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.utils.WandUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.item.ItemDisplayContext;

@Mixin(GuiGraphics.class)
public abstract class ItemRendererMixin {

    @Shadow
    private final Minecraft minecraft;


    @Final
    @Shadow
    private PoseStack pose;

    protected ItemRendererMixin(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Shadow
    protected abstract void renderItem(LivingEntity livingEntity, Level level, ItemStack itemStack, int i, int j, int k, int l);


    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V", at = @At(value = "TAIL"))
    public void renderItem(LivingEntity livingEntity,Level level, ItemStack itemStack, int i, int j, int k, int l, CallbackInfo cb){

        if (level !=null && WandUtils.is_magicbag(itemStack)) {

            ItemStack item_in_bag = MagicBagItem.getItem(itemStack,level.registryAccess());
            //System.out.println("renderItem "+item_in_bag.isEmpty());
            if (!item_in_bag.isEmpty()) {
                BakedModel bakedModel = this.minecraft.getItemRenderer().getModel(itemStack, level, livingEntity, k);
                //RenderSystem.enableDepthTest();
                //RenderSystem.disableScissor();
                //RenderSystem.disableColorLogicOp();
                pose.pushPose();
                pose.translate( (float)(i + 8), (float)(j + 8),(float)(150 + (bakedModel.isGui3d() ? l : 0)+10));
                pose.scale(0.5f, 0.5f, 0.5f);
                pose.translate(0, -0.3, 0);
                pose.translate(-(float)(i + 8), -(float)(j + 8),-(float)(150 + (bakedModel.isGui3d() ? l : 0)+10));
                this.renderItem(livingEntity,level,item_in_bag,i,j+5,k,161);
                pose.popPose();
                //RenderSystem.disableDepthTest();

            }
        }
    }
    /*

    @Shadow
    ItemModelShaper itemModelShaper;

    @Shadow public abstract void renderStatic(ItemStack itemStack, ItemDisplayContext itemDisplayContext, int i, int j, PoseStack poseStack, MultiBufferSource multiBufferSource, @Nullable Level level, int k);

    @Inject(method = "renderStatic", at = @At(value = "TAIL"))
    public void renderStatic(ItemStack itemStack, ItemDisplayContext itemDisplayContext, int i, int j, PoseStack poseStack, MultiBufferSource multiBufferSource, @Nullable Level level, int k, CallbackInfo cb){

        if (level !=null && WandUtils.is_magicbag(itemStack)) {
            ItemStack item_in_bag = MagicBagItem.getItem(itemStack,level.registryAccess());
            if (!item_in_bag.isEmpty()) {
                BakedModel bakedModel2 = this.itemModelShaper.getItemModel(item_in_bag);
                if (bakedModel2 != null) {
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
*/
}
