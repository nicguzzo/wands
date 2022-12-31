package net.nicguzzo.wands.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.menues.MagicBagMenu;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.utils.Compat;

public class MagicBagScreen extends AbstractContainerScreen<MagicBagMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("wands", "textures/gui/magicbag.png");
    ItemStack bag_stack=null;
    Item bag_item=null;
    int tier=0;
    public MagicBagScreen(MagicBagMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
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
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        Compat.set_color(1.0F, 1.0F, 1.0F, 1.0F);
        Compat.set_texture(TEXTURE);
        Compat.set_pos_tex_shader();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);

        blit(poseStack, x+imageWidth-64, y+10, 200, 64*tier, 41, 64);
    }
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        int text_color=0xffffffff;
        RenderSystem.disableDepthTest();
        ItemStack item=MagicBagItem.getItem(bag_stack);

        if(!item.isEmpty()) {
            Component text = Compat.translatable(item.getDescriptionId());
            int w = font.width(text);
            font.draw(poseStack, text, (width / 2.0f) - w / 2.0f, (height / 2.0f) - 20, text_color);
        }
        Component text2=Compat.literal(""+MagicBagItem.getTotal(bag_stack));
        int w2=font.width(text2);
        font.draw(poseStack,text2 ,(width/2.0f)-w2/2.0f,(height/2.0f)-32, text_color);
        RenderSystem.enableDepthTest();

    }
}
