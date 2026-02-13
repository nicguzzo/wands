package net.nicguzzo.wands.client.screens;

#if MC_VERSION >= 12111
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.renderer.texture.TextureManager;
#endif
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.compat.MyIdExt;
import net.nicguzzo.wands.menues.MagicBagMenu;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.compat.Compat;
import net.nicguzzo.wands.WandsMod;

import java.util.List;

public class MagicBagScreen extends AbstractContainerScreen<MagicBagMenu> {
    private static final MyIdExt TEXTURE = new MyIdExt(WandsMod.MOD_ID,"textures/gui/magicbag.png");
    ItemStack bag_stack=null;
    Item bag_item=null;
    MagicBagItem.MagicBagItemTier tier= MagicBagItem.MagicBagItemTier.MAGIC_BAG_TIER_1;
    List<Component> help;
    #if MC_VERSION >= 12111
    GpuTexture magicbag_Texture;
    #endif
    public MagicBagScreen(MagicBagMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
        #if MC_VERSION >= 12111
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        magicbag_Texture = textureManager.getTexture(TEXTURE.res).getTexture();
        #endif
    }
    @Override
    public void init() {
        super.init();
        bag_stack = this.menu.bag;
        if (bag_stack != null && bag_stack.getItem() instanceof MagicBagItem) {
            bag_item = bag_stack.getItem();
            this.tier=((MagicBagItem)bag_item).tier;
        }
        help=List.of(
                Compat.literal("shift click to load/unload"),
                Compat.literal("left click to unload 1 item"),
                Compat.literal("right click to unload 1 stack"),
                Compat.literal("click when total is 0 to clear the item")
        );
    }
    @Override
    protected void renderBg(GuiGraphics gui, float f, int i, int j) {

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        Compat.blit(gui, TEXTURE, x, y, 0, 0, imageWidth, imageHeight,256,256);
        Compat.blit(gui, TEXTURE, x+100, y+33, 256-16, 16*tier.ordinal(), 16, 16, 256, 256);
        //gui.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight,256,256);
        //gui.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x+100, y+33, 256-16, 16*tier.ordinal(), 16, 16, 256, 256);
    }
    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        super.render(gui, mouseX, mouseY, delta);
        Minecraft client=Minecraft.getInstance();
        int text_color=0xffffffff;
        //RenderSystem.disableDepthTest();
        ItemStack item=MagicBagItem.getItem(bag_stack,client.level);
        if(!item.isEmpty()) {
            Component text = Component.translatable(item.getItem().getDescriptionId());
            int w = font.width(text);
            gui.drawString(client.font, text.getString(), (width / 2) - w / 2, (height / 2) - 20, text_color,false);
            //gui.renderFakeItem(item,(width / 2) - w / 2, (height / 2) - 20);
        }
        Component text2=Compat.literal(""+MagicBagItem.getTotal(bag_stack));
        int w2=font.width(text2);
        gui.drawString(client.font, text2, (width / 2) - w2 / 2, (height / 2) - 32, text_color,false);


        Component text3=Compat.literal("help");
        int w3=font.width(text3);
        int x3=(width / 2)-w3/2;
        int y3= (height / 2) - 65;
        gui.drawString(client.font, text3, x3, y3, text_color,false);

        if(mouseX>=x3 && mouseX<x3+w3 && mouseY>=y3 && mouseY<y3+10) {
            Compat.renderComponentTooltip(gui,font,help,mouseX,mouseY);
        }

        this.renderTooltip(gui, mouseX,mouseY);
        //RenderSystem.enableDepthTest();
    }
}
