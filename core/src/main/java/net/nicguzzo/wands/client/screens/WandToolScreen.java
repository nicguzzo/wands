package net.nicguzzo.wands.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
#if MC_VERSION >= 12111
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.input.KeyEvent;
#endif
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.client.gui.GuiGraphics;

import net.nicguzzo.compat.MyIdExt;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.menues.WandMenu;
import net.nicguzzo.compat.Compat;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class WandToolScreen extends AbstractContainerScreen<WandMenu> {

    #if MC_VERSION >= 12111
    int[] empty_tools = new int[0];
    GpuTextureView wandInventoryTexture;
    #endif
    private static final MyIdExt INV_TEX = new MyIdExt(WandsMod.MOD_ID, "textures/gui/inventory.png");

    // Hand cursor for hovering over inventory slots
    private static long handCursor = 0;
    private boolean isHandCursor = false;

    public WandToolScreen(WandMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        #if MC_VERSION >= 12111
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        wandInventoryTexture = textureManager.getTexture(INV_TEX.res).getTextureView();
        #endif
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics gui, float delta, int mouseX, int mouseY) {
        #if MC_VERSION >= 12111
        RenderSystem.outputColorTextureOverride = wandInventoryTexture;
        #else
        RenderSystem.setShaderTexture(0, INV_TEX.res);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        #endif
        int inventoryX = (width - imageWidth) / 2;
        int inventoryY = (height - imageHeight) / 2;
        Compat.blit(gui, INV_TEX, inventoryX, inventoryY, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        super.render(gui, mouseX, mouseY, delta);

        // Draw green highlights on tool slots
        if (ClientRender.wand != null && ClientRender.wand.player_data != null) {
            #if MC_VERSION >= 12111
            int[] Tools = ClientRender.wand.player_data.getIntArray("Tools").orElse(empty_tools);
            #else
            int[] Tools = ClientRender.wand.player_data.getIntArray("Tools");
            #endif

            for (int toolSlotIndex : Tools) {
                Slot slot = this.menu.slots.get(toolSlotIndex);
                int slotScreenX = slot.x + this.leftPos;
                int slotScreenY = slot.y + this.topPos;
                gui.fillGradient(slotScreenX, slotScreenY, slotScreenX + 16, slotScreenY + 16, 0x8800AA00, 0x1000AA00);
            }
        }

        // Instruction text below title
        int instructionY = topPos + titleLabelY + font.lineHeight + 16;
        gui.drawString(font, "Click an inventory slot to have ", leftPos + titleLabelX, instructionY, Compat.DARK_GRAY, false);
        gui.drawString(font, "the wand use a tool in that slot", leftPos + titleLabelX, instructionY + font.lineHeight, Compat.DARK_GRAY, false);

        // Hand cursor over inventory slots
        boolean shouldBeHand = isOverInventorySlot(mouseX, mouseY);
        if (shouldBeHand != isHandCursor) {
            isHandCursor = shouldBeHand;
            long window = Compat.getWindow();
            if (shouldBeHand) {
                if (handCursor == 0) {
                    handCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
                }
                GLFW.glfwSetCursor(window, handCursor);
            } else {
                GLFW.glfwSetCursor(window, 0);
            }
        }

        this.renderTooltip(gui, mouseX, mouseY);
    }

    private boolean isOverInventorySlot(int mx, int my) {
        for (int i = 0; i < 36; i++) {
            Slot slot = this.menu.slots.get(i);
            int slotX = slot.x + this.leftPos;
            int slotY = slot.y + this.topPos;
            if (mx >= slotX && mx < slotX + 16 && my >= slotY && my < slotY + 16) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClose() {
        if (isHandCursor) {
            GLFW.glfwSetCursor(Compat.getWindow(), 0);
            isHandCursor = false;
        }
        if (handCursor != 0) {
            GLFW.glfwDestroyCursor(handCursor);
            handCursor = 0;
        }
        super.onClose();
    }

    @Override
    #if MC_VERSION >= 12111
    public boolean keyPressed(KeyEvent keyEvent) {
        int scancode = keyEvent.scancode();
        if (WandsModClient.wand_menu_km.matches(keyEvent) || scancode == 256) {
    #else
    public boolean keyPressed(int scancode, int keysym, int k) {
        if (WandsModClient.wand_menu_km.matches(keysym, scancode) || scancode == 256) {
    #endif
            onClose();
            return true;
        } else {
            #if MC_VERSION >= 12111
            return super.keyPressed(keyEvent);
            #else
            return super.keyPressed(scancode, keysym, k);
            #endif
        }
    }
}
