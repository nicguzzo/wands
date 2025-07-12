package net.nicguzzo.wands.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.menues.MagicBagMenu;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.utils.Compat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;

public class MagicBagScreen extends AbstractContainerScreen<MagicBagMenu> {
    private static final ResourceLocation TEXTURE = Compat.create_resource("textures/gui/magicbag.png");
    ItemStack bag_stack=null;
    Item bag_item=null;
    int tier=0;
    GpuTexture magicbag_Texture;
    public MagicBagScreen(MagicBagMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        magicbag_Texture = textureManager.getTexture(TEXTURE).getTexture();

    }
    @Override
    public void init() {
        super.init();
        bag_stack = this.menu.bag;
        if (bag_stack != null && bag_stack.getItem() instanceof MagicBagItem) {
            bag_item = bag_stack.getItem();
            this.tier=((MagicBagItem)bag_item).tier;
        }
    }
    @Override
    protected void renderBg(GuiGraphics gui, float f, int i, int j) {
        //RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        //RenderSystem.setShaderTexture(0, magicbag_Texture);
        //Compat.set_pos_tex_shader();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        gui.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight,256,256);
        gui.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x+imageWidth-64, y+10, 200, 64*tier,41, 64,256,256);
    }
    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        super.render(gui, mouseX, mouseY, delta);
        Minecraft client=Minecraft.getInstance();
        int text_color=0xffffffff;
        //RenderSystem.disableDepthTest();
        ItemStack item=MagicBagItem.getItem(bag_stack,client.level.registryAccess());
        if(!item.isEmpty()) {
            Component text = Component.translatable(item.getItem().getDescriptionId());
            int w = font.width(text);
            gui.drawString(client.font, text.getString(), (width / 2) - w / 2, (height / 2) - 20, text_color,false);
            //gui.renderFakeItem(item,(width / 2) - w / 2, (height / 2) - 20);
        }
        Component text2=Compat.literal(""+MagicBagItem.getTotal(bag_stack));
        int w2=font.width(text2);
        gui.drawString(client.font, text2, (width / 2) - w2 / 2, (height / 2) - 32, text_color,false);


        Component text3=Compat.literal("shift click to load/unload");
        int w3=font.width(text3);
        gui.drawString(client.font, text3, (width / 2)-w3/2, (height / 2) - 65, text_color,false);

        this.renderTooltip(gui, mouseX,mouseY);
        //RenderSystem.enableDepthTest();
    }
}
