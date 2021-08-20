package net.nicguzzo.wands.mcver.impl;

import java.util.function.Supplier;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.registry.*;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.WandsModClient;
import net.nicguzzo.wands.mcver.MCVer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public class MCVer1_16_5 extends MCVer{
	
	@Override
	public CreativeModeTab create_tab(ResourceLocation res){
		return CreativeTabs.create(res, new Supplier<ItemStack>() {
			@Override
			public ItemStack get() {
				return new ItemStack(WandsMod.DIAMOND_WAND_ITEM.get());
			}
		});
	}	
	@Override
	public boolean is_creative(Player player) {
		return player.abilities.instabuild;
	}
	@Override
	public void set_color(float r, float g, float b, float a){
		RenderSystem.color4f(r,g,b,a);
	}
	@Override
	public void set_texture(ResourceLocation tex){
		Minecraft.getInstance().getTextureManager().bind(tex);
		RenderSystem.enableTexture();
	}
	@Override
	public void set_render_quads(BufferBuilder bufferBuilder){
		RenderSystem.enableBlend();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);

	}
	@Override
	public void set_render_lines(BufferBuilder bufferBuilder) {
		RenderSystem.disableTexture();
		RenderSystem.disableBlend();
		RenderSystem.shadeModel(7425);
		RenderSystem.enableAlphaTest();
		RenderSystem.defaultAlphaFunc();
		bufferBuilder.begin(1, DefaultVertexFormat.POSITION_COLOR);
	}

	@Override
	public void pre_render(PoseStack poseStack) {

		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 c = camera.getPosition();
		//poseStack.pushPose();
		//poseStack.translate(-c.x, -c.y, -c.z); // translate
		//GlStateManager._pushMatrix();
		//RenderSystem.multMatrix(poseStack.last().pose());

		//GlStateManager._pushMatrix();
		RenderSystem.pushMatrix();
		if(WandsModClient.is_forge) {
			RenderSystem.multMatrix(poseStack.last().pose());
		}
		RenderSystem.translatef((float)-c.x,(float) -c.y,(float) -c.z);

	}

	@Override
	public void post_render(PoseStack poseStack) {
		RenderSystem.popMatrix();
		//GlStateManager._popMatrix();
		//poseStack.popPose();
	}
}