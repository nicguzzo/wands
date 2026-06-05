package net.nicguzzo.wands.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//?if >= 1.21.11{
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.texture.TextureManager;
//?}

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.compat.RcId;
import net.nicguzzo.wands.menues.WandToolsMenu;
import net.nicguzzo.wands.compat.Compat;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class WandToolScreen extends AbstractContainerScreen<WandToolsMenu> {

    //?if >= 1.21.11{
    int[] empty_tools = new int[0];
    GpuTextureView wandInventoryTexture;
    //?}
    private static final RcId INV_TEX = RcId.fromNamespaceAndPath(WandsMod.MOD_ID, "textures/gui/inventory.png");

    // Hand cursor for hovering over inventory slots
    private static long handCursor = 0;
    private boolean isHandCursor = false;

    public WandToolScreen(WandToolsMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    //?if >= 1.21.11{
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        wandInventoryTexture = textureManager.getTexture(INV_TEX.id()).getTextureView();
    //?}
    }

    //?if >=26.1 {
    protected void extractBackground(GuiGraphicsExtractor gui, float delta, int mouseXi, int mouseY) {
    //?}else{
    /*@Override
    protected void renderBg(@NotNull GuiGraphicsExtractor gui, float delta, int mouseX, int mouseY) {
    *///?}

    //?if >= 1.21.11{
        RenderSystem.outputColorTextureOverride = wandInventoryTexture;
    //?}else{
        
        /*RenderSystem.setShaderTexture(0, INV_TEX.id());
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    *///?}
        int inventoryX = (width - imageWidth) / 2;
        int inventoryY = (height - imageHeight) / 2;
        Compat.blit(gui, INV_TEX, inventoryX, inventoryY, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    //?if >=26.1 {
    public void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta){
        super.extractContents(gui, mouseX, mouseY, delta);
    //?}else{
    /*public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta){
        super.render(gui, mouseX, mouseY, delta);
    *///?}

        // Draw green highlights on tool slots
        if (ClientRender.wand != null && ClientRender.wand.player_data != null) {
        //?if >= 1.21.11{
            int[] Tools = ClientRender.wand.player_data.getIntArray("Tools").orElse(empty_tools);
        //?}else{
            /*int[] Tools = ClientRender.wand.player_data.getIntArray("Tools");
        *///?}

            for (int toolSlotIndex : Tools) {
                Slot slot = this.menu.slots.get(toolSlotIndex);
                int slotScreenX = slot.x + this.leftPos;
                int slotScreenY = slot.y + this.topPos;
                gui.fillGradient(slotScreenX, slotScreenY, slotScreenX + 16, slotScreenY + 16, 0x8800AA00, 0x1000AA00);
            }
        }

        // Instruction text below title
        int instructionY = topPos + titleLabelY + font.lineHeight + 16;
        gui.text(font, "Click an inventory slot to have ", leftPos + titleLabelX, instructionY, Compat.DARK_GRAY, false);
        gui.text(font, "the wand use a tool in that slot", leftPos + titleLabelX, instructionY + font.lineHeight, Compat.DARK_GRAY, false);

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
        //?if <26.1
        //this.renderTooltip(gui, mouseX, mouseY);
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
    //?if >= 1.21.11{
    public boolean keyPressed(KeyEvent keyEvent) {
        int scancode = keyEvent.scancode();
        if (WandsModClient.wand_menu_km.matches(keyEvent) || scancode == 256) {
    //?}else{
        
        /*public boolean keyPressed(int scancode, int keysym, int k) {
        if (WandsModClient.wand_menu_km.matches(keysym, scancode) || scancode == 256) {
        
    *///?}
            onClose();
            return true;
        } else {
            //?if >= 1.21.11{
                return super.keyPressed(keyEvent);
            //?}else{
            
            /*return super.keyPressed(scancode, keysym, k);
            *///?}
        }
    }
}
