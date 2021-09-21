/*//beginMC1_17_1
package net.nicguzzo.wands.mcver.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.blaze3d.vertex.VertexFormat;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.PaletteScreenHandler;
import net.nicguzzo.wands.WandScreenHandler;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.WandsModClient;
import net.nicguzzo.wands.mcver.MCVer;

import java.util.function.Supplier;

public class MCVer1_17_1 extends MCVer {
    @Override
    public CreativeModeTab create_tab(ResourceLocation res){
        return CreativeTabRegistry.create(res, new Supplier<ItemStack>() {
            @Override
            public ItemStack get() {
                return new ItemStack(WandsMod.DIAMOND_WAND_ITEM.get() );
            }
        });
    }
    @Override
    public boolean is_creative(Player player) {
        return player.getAbilities().instabuild;
    }

    @Override
    public Inventory get_inventory(Player player) {
        return player.getInventory();
    }

    @Override
    public void set_color(float r, float g, float b, float a){
        RenderSystem.setShaderColor(r,g,b,a);
    }

    @Override
    public void set_pos_tex_shader() {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }

    @Override
    public void set_texture(ResourceLocation tex){
        RenderSystem.setShaderTexture(0, tex);
    }

    @Override
    public void set_render_quads_block(BufferBuilder bufferBuilder) {
        RenderSystem.setShader(GameRenderer::getBlockShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
    }

    @Override
    public void set_render_quads_pos_tex(BufferBuilder bufferBuilder) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        //RenderSystem.setShader(GameRenderer::getPositionTexShader);
        //bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
    }
    @Override
    public void set_render_lines(BufferBuilder bufferBuilder) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
    }

    @Override
    public void set_render_quads_pos_col(BufferBuilder bufferBuilder) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    }

    @Override
    public void pre_render(PoseStack poseStack) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 c = camera.getPosition();
        //RenderSystem.pushMatrix();

        PoseStack poseStack2 = RenderSystem.getModelViewStack();
        poseStack2.pushPose();
        //if(WandsModClient.is_forge) {
            //poseStack2.mulPoseMatrix(poseStack.last().pose());
        //}
        poseStack2.translate(-c.x,-c.y,-c.z);
        RenderSystem.applyModelViewMatrix();

        //RenderSystem.translatef((float)-c.x,(float) -c.y,(float) -c.z);
    }

    @Override
    public void post_render(PoseStack poseStack) {
        PoseStack poseStack2 = RenderSystem.getModelViewStack();
        poseStack2.popPose();
        RenderSystem.applyModelViewMatrix();
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
    public void set_carried(Player player,AbstractContainerMenu menu,ItemStack itemStack){
        menu.setCarried(itemStack);
    }
    @Override
    public ItemStack get_carried(Player player,AbstractContainerMenu menu){
        return menu.getCarried();
    }
}
//endMC1_17_1*/  