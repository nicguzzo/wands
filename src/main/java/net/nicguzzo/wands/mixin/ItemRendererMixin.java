package net.nicguzzo.wands.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?if >=1.21.11 {
import net.minecraft.client.renderer.item.ItemStackRenderState;
//?}
//?if >=1.20.5{
import net.minecraft.core.component.DataComponents;
//?}
import net.minecraft.client.renderer.texture.OverlayTexture;
//?if <1.21.11{
/*import net.minecraft.client.resources.model.BakedModel;

*///?}
//?if < 26.1 {
/*import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

*///?}

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.utils.WandUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.item.ItemDisplayContext;
//?if <1.21.2{
/*import net.minecraft.client.renderer.ItemModelShaper;
*///?}


//?if < 1.21 {

/*@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow
    ItemModelShaper itemModelShaper;
    @Shadow
    public abstract void render(ItemStack itemStack, ItemDisplayContext itemDisplayContext, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel);

    @Inject(method = "render", at = @At(value = "TAIL"))
    public void render(ItemStack itemStack, ItemDisplayContext itemDisplayContext, boolean bl, PoseStack pose, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel, CallbackInfo cb) {
        Level level=Minecraft.getInstance().level;
        if (level !=null && WandUtils.is_magicbag(itemStack)) {
            ItemStack item_in_bag = MagicBagItem.getItem(itemStack,level);
            if (!item_in_bag.isEmpty()) {
           BakedModel bakedModel2 = this.itemModelShaper.getItemModel(item_in_bag);
                pose.pushPose();
                pose.scale(0.5f, 0.5f, 0.5f);
                pose.translate(0, -0.3, 0.2);
                RenderSystem.disableDepthTest();
                this.render(item_in_bag, ItemDisplayContext.GUI, false, pose, multiBufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, bakedModel2);
                RenderSystem.enableDepthTest();
                pose.popPose();
            }
        }
    }
}
*///?}


//?if >= 1.21 && <1.21.11 {

/*@Mixin(GuiGraphicsExtractor.class)

public abstract class ItemRendererMixin {
    @Final
    @Shadow
    private PoseStack pose;
    @Shadow
    protected abstract void renderItem(LivingEntity livingEntity, Level level, ItemStack itemStack, int i, int j, int k, int l);

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V", at = @At(value = "TAIL"))
    public void renderItem(LivingEntity livingEntity,Level level, ItemStack itemStack, int i, int j, int k, int l, CallbackInfo cb){
        if (level !=null && WandUtils.is_magicbag(itemStack)) {
            ItemStack item_in_bag = MagicBagItem.getItem(itemStack,level);
            if (!item_in_bag.isEmpty()) {
                BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getModel(itemStack, level, livingEntity, k);
                pose.pushPose();
                pose.translate( (float)(i + 8), (float)(j + 8),(float)(150 + (bakedModel.isGui3d() ? l : 0)+10));
                pose.scale(0.5f, 0.5f, 0.5f);
                pose.translate(0, -0.3, 0);
                pose.translate(-(float)(i + 8), -(float)(j + 8),-(float)(150 + (bakedModel.isGui3d() ? l : 0)+10));
                this.renderItem(livingEntity,level,item_in_bag,i,j+5,k,161);
                pose.popPose();
            }
        }
    }

}

*///?}
//?if >=1.21.11 {
@Mixin(GuiGraphicsExtractor.class)
public abstract class ItemRendererMixin {
    @Final
    @Shadow
    private Matrix3x2fStack pose;
    @Shadow
    //?if >=26.1 {
    protected abstract void item(LivingEntity livingEntity, Level level, ItemStack itemStack, int i, int j, int k);
    //?}else{
    /*protected abstract void renderItem(LivingEntity livingEntity, Level level, ItemStack itemStack, int i, int j, int k);
    *///?}
    //?if >=26.1 {
    @Inject(method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V", at = @At(value = "TAIL"))
    //?}else{
    /*@Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V", at = @At(value = "TAIL"))
    *///?}
    public void renderItem(LivingEntity livingEntity,Level level, ItemStack itemStack, int i, int j, int k, CallbackInfo cb){
        //System.out.println("renderItem");
        if (level !=null && WandUtils.is_magicbag(itemStack)) {
            ItemStack item_in_bag = MagicBagItem.getItem(itemStack,level);
            if (!item_in_bag.isEmpty()) {
                if (itemStack.get(DataComponents.ITEM_MODEL) != null) {
                    pose.pushMatrix();
                    pose.translate((float)(i + 8), (float)(j + 8));
                    pose.scale(0.5f, 0.5f);
                    pose.translate(0.0f, -0.3f);
                    pose.translate(-(float)(i + 8), -(float)(j + 8));
                    //?if >=26.1 {
                        this.item(livingEntity,level,item_in_bag,i,j+5,k);
                    //?}else{
                        /*this.renderItem(livingEntity,level,item_in_bag,i,j+5,k);
                    *///?}
                    pose.popMatrix();

                }
            }
        }
    }

}
//?}
