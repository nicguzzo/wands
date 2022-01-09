//beginMC1_16_5
package net.nicguzzo.wands.mcver.impl;

import java.util.function.Supplier;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.registry.*;

import me.shedaniel.architectury.registry.menu.ExtendedMenuProvider;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.PaletteScreenHandler;
import net.nicguzzo.wands.WandScreenHandler;
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
	public Inventory get_inventory(Player player) {
		return player.inventory;
	}

	@Override
	public void set_color(float r, float g, float b, float a){
		RenderSystem.color4f(r,g,b,a);
	}

	@Override
	public void set_pos_tex_shader() {

	}

	@Override
	public void set_texture(ResourceLocation tex){
		Minecraft.getInstance().getTextureManager().bind(tex);
	}
	@Override
	public void set_render_quads_block(BufferBuilder bufferBuilder){
		bufferBuilder.begin(7, DefaultVertexFormat.BLOCK);
	}
	@Override
	public void set_render_quads_pos_tex(BufferBuilder bufferBuilder){
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
	}
	@Override
	public void set_render_quads_pos_col(BufferBuilder bufferBuilder) {
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
	}
	@Override
	public void set_render_lines(BufferBuilder bufferBuilder) {
		RenderSystem.disableTexture();
		RenderSystem.disableBlend();
		RenderSystem.shadeModel(7425);
		RenderSystem.enableAlphaTest();
		RenderSystem.defaultAlphaFunc();
		bufferBuilder.begin(1, DefaultVertexFormat.POSITION_COLOR);
		//bufferBuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);
	}

	@Override
	public void pre_render(PoseStack poseStack) {

		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 c = camera.getPosition();

		poseStack.pushPose();
		if(WandsMod.is_forge) {
			poseStack.translate(-c.x, -c.y, -c.z); // translate
			GlStateManager._pushMatrix();
			RenderSystem.multMatrix(poseStack.last().pose());
		}else{
			GlStateManager._pushMatrix();
			RenderSystem.translated(-c.x, -c.y, -c.z);
		}
	}

	@Override
	public void post_render(PoseStack poseStack) {
		GlStateManager._popMatrix();
		poseStack.popPose();
	}

	@Override
	public void send_to_player(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buf) {
		NetworkManager.sendToPlayer(player, id, buf);
	}

	@Override
	public void open_palette(ServerPlayer player, ItemStack paletteItemStack) {
		MenuRegistry.openExtendedMenu(player, new ExtendedMenuProvider(){
			@Override
			public void saveExtraData(FriendlyByteBuf packetByteBuf) {
				packetByteBuf.writeItem(paletteItemStack);
			}
			@Override
			public Component getDisplayName(){
				return new TranslatableComponent(paletteItemStack.getItem().getDescriptionId());
			}
			@Override
			public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
				return new PaletteScreenHandler(syncId, inv, paletteItemStack);
			}
		});
	}

	@Override
	public void open_wand_menu(ServerPlayer player, ItemStack wandItemStack) {
		MenuRegistry.openExtendedMenu(player, new ExtendedMenuProvider(){
			@Override
			public void saveExtraData(FriendlyByteBuf packetByteBuf) {
				packetByteBuf.writeItem(wandItemStack);
			}
			@Override
			public Component getDisplayName(){
				return new TranslatableComponent(wandItemStack.getItem().getDescriptionId());
			}
			@Override
			public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
				return new WandScreenHandler(syncId, inv, wandItemStack);
			}
		});
	}

	@Override
	public void set_carried(Player player, AbstractContainerMenu menu, ItemStack itemStack) {
		player.inventory.setCarried(itemStack);
	}

	@Override
	public ItemStack get_carried(Player player, AbstractContainerMenu menu) {
		return player.inventory.getCarried();
	}
}
//endMC1_16_5   