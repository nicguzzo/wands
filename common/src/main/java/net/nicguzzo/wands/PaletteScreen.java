package net.nicguzzo.wands;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
public class PaletteScreen extends AbstractContainerScreen<PaletteScreenHandler> {
    
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/shulker_box.png");

    public PaletteScreen(PaletteScreenHandler handler, Inventory inventory,Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        //RenderSystem.setShader(GameRenderer::getPositionTexShader);
        //RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        //RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        blit(matrices, x, y, 0, 0, imageWidth, imageHeight);
    }
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Slot slot = this.findSlot(mouseX, mouseY);
        if(slot!=null){
            //System.out.println("mouseClicked "+button);
            switch(button){
                case 0:
                    if (hasShiftDown()) {
                        this.slotClicked(slot, slot.index, button, ClickType.QUICK_MOVE);
                    }else{
                        this.slotClicked(slot, slot.index, button, ClickType.PICKUP);
                    }
                break;
                case 1:
                    this.slotClicked(slot, slot.index, button, ClickType.PICKUP);
                break;
                case 2:
                    this.slotClicked(slot, slot.index, button, ClickType.CLONE);
                break;
            }
        }
        return true;
    }
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return true;
    }
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        Slot slot = this.findSlot(mouseX, mouseY);
        if(slot!=null){
            ItemStack itemStack = this.menu.playerInventory.getCarried();
            if(itemStack != ItemStack.EMPTY && slot.getItem() == ItemStack.EMPTY){
                this.slotClicked(slot, slot.index, button, ClickType.QUICK_CRAFT);
            }
        }
        return true;
    }
}