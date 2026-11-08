package net.nicguzzo.wands.client.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//?if >= 1.21.11 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.gui.Btn;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.compat.RcId;
import net.nicguzzo.wands.items.BlueprintItem;
import net.nicguzzo.wands.items.BlueprintItem.BlockGroup;
import net.nicguzzo.wands.menues.BlueprintMenu;
import net.nicguzzo.wands.networking.ClientNetworking;
//?if < 26.3 {
import org.lwjgl.glfw.GLFW;
//?}

import java.util.ArrayList;
import java.util.List;

public class BlueprintScreen extends AbstractContainerScreen<BlueprintMenu> {

    private static final RcId TEXTURE = RcId.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 210;
    private static final int TOP_HEIGHT = 114;
    private static final int LIST_X_REL = 7;
    private static final int LIST_Y_REL = 26;
    private static final int LIST_W = 162;
    private static final int ROW_H = 20;
    private static final int VISIBLE_ROWS = 4;
    private static final int LIST_H = VISIBLE_ROWS * ROW_H;

    private static final int NAME_X_REL = 7;
    private static final int NAME_Y_REL = 6;
    private static final int NAME_W = 104;
    private static final int NAME_H = 14;

    private String nameText = "";
    private boolean nameFocused = false;
    private int scrollOffset = 0;
    private List<BlockGroup> blockGroups = new ArrayList<>();
    private Btn btnSaveCopy;

    public BlueprintScreen(BlueprintMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title
                //?if >= 26.1 {
                , IMAGE_WIDTH, IMAGE_HEIGHT
                //?}
        );
        //?if < 26.1 {
        /*this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = IMAGE_HEIGHT;
        *///?}
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = -999; // Hide default title since we draw custom header / name box
    }

    @Override
    public void init() {
        super.init();
        ItemStack bp = this.menu.blueprint;
        this.nameText = BlueprintItem.getBlueprintName(bp);
        refreshGroups();

        btnSaveCopy = new Btn(this.leftPos + 114, this.topPos + 6, 55, 14, Compat.translatable("screen.wands.blueprint.save_copy"), (mx, my) -> {
            if (ClientRender.wand != null && !ClientRender.wand.copy_paste_buffer.isEmpty()) {
                BlueprintItem.saveCopyBufferToBlueprint(this.menu.blueprint, ClientRender.wand.copy_paste_buffer);
                String id = BlueprintItem.getOrCreateId(this.menu.blueprint);
                ClientRender.wand.setActiveBlueprintId(id);
                ClientNetworking.SendBlueprintAction(0, "", "");
                refreshGroups();
            }
        });
        btnSaveCopy.centerText = true;
        btnSaveCopy.withTooltip(
                Compat.translatable("tooltip.wands.blueprint.save_copy"),
                Compat.translatable("tooltip.wands.blueprint.save_copy_desc")
        );
    }

    private void refreshGroups() {
        this.blockGroups = new ArrayList<>(BlueprintItem.getBlockGroups(this.menu.blueprint));
        int maxScroll = Math.max(0, blockGroups.size() - VISIBLE_ROWS);
        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }
    }

    //?if >= 26.1 {
    @Override
    public void extractBackground(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        super.extractBackground(gui, mouseX, mouseY, delta);
    //?} else {
    /*@Override
    protected void renderBg(GuiGraphicsExtractor gui, float delta, int mouseX, int mouseY) {
    *///?}
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Top section + player inventory frame from generic_54.png
        Compat.blit(gui, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, TOP_HEIGHT, 256, 256);
        Compat.blit(gui, TEXTURE, x, y + TOP_HEIGHT, 0.0F, 126.0F, this.imageWidth, 96, 256, 256);

        // Cover the vanilla chest slots in the top region with a clean panel background
        gui.fill(x + 6, y + 5, x + imageWidth - 6, y + TOP_HEIGHT - 2, 0xFFC6C6C6);

        // Name input box
        int nx = x + NAME_X_REL;
        int ny = y + NAME_Y_REL;
        int borderCol = nameFocused ? 0xFFFFFFA0 : 0xFF555555;
        gui.fill(nx - 1, ny - 1, nx + NAME_W + 1, ny + NAME_H + 1, borderCol);
        gui.fill(nx, ny, nx + NAME_W, ny + NAME_H, 0xFF1E1E1E);

        // Block list container area
        int lx = x + LIST_X_REL;
        int ly = y + LIST_Y_REL;
        gui.fill(lx - 1, ly - 1, lx + LIST_W + 1, ly + LIST_H + 1, 0xFF373737);
        gui.fill(lx, ly, lx + LIST_W, ly + LIST_H, 0xFF222222);
    }

    //?if >= 26.1 {
    @Override
    public void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        super.extractContents(gui, mouseX, mouseY, delta);
    //?} else {
    /*@Override
    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        super.render(gui, mouseX, mouseY, delta);
    *///?}
        int x = this.leftPos;
        int y = this.topPos;

        boolean hasCopy = ClientRender.wand != null && !ClientRender.wand.copy_paste_buffer.isEmpty();
        btnSaveCopy.disabled = !hasCopy;
        btnSaveCopy.x = x + 114;
        btnSaveCopy.y = y + 6;
        btnSaveCopy.render(gui, this.font, mouseX, mouseY);

        // Render Name box text
        int nx = x + NAME_X_REL;
        int ny = y + NAME_Y_REL;
        String displayStr = nameText;
        int textColor = 0xFFFFFFFF;
        if (displayStr.isEmpty() && !nameFocused) {
            displayStr = Compat.translatable("screen.wands.blueprint.name_placeholder").getString();
            textColor = 0xFF888888;
        } else if (nameFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
            displayStr = displayStr + "_";
        }
        String trimmedName = this.font.plainSubstrByWidth(displayStr, NAME_W - 6);
        gui.text(this.font, trimmedName, nx + 3, ny + 3, textColor, false);

        // Render Block List rows
        int lx = x + LIST_X_REL;
        int ly = y + LIST_Y_REL;

        Component hoveredTooltip = null;
        ItemStack hoveredStackTooltip = ItemStack.EMPTY;

        if (blockGroups.isEmpty()) {
            String emptyMsg = Compat.translatable("screen.wands.blueprint.empty_hint").getString();
            int ew = this.font.width(emptyMsg);
            gui.text(this.font, emptyMsg, lx + (LIST_W - ew) / 2, ly + (LIST_H - 8) / 2, 0xFF999999, false);
        } else {
            int maxRows = Math.min(VISIBLE_ROWS, blockGroups.size() - scrollOffset);
            for (int r = 0; r < maxRows; r++) {
                int idx = scrollOffset + r;
                BlockGroup group = blockGroups.get(idx);
                int ry = ly + r * ROW_H;

                // Alternating row background
                int rowBg = (idx % 2 == 0) ? 0xFF2C2C2C : 0xFF252525;
                gui.fill(lx, ry, lx + LIST_W - 6, ry + ROW_H - 1, rowBg);

                // Original block icon
                ItemStack origStack = new ItemStack(group.originalBlock.asItem());
                int origIconX = lx + 2;
                int origIconY = ry + 2;
                gui.item(origStack, origIconX, origIconY);

                if (mouseX >= origIconX && mouseX < origIconX + 16 && mouseY >= origIconY && mouseY < origIconY + 16) {
                    hoveredStackTooltip = origStack;
                }

                // Count + Block name
                Block effectiveBlock = group.getEffectiveBlock();
                boolean isReplaced = group.replacementBlock != null && group.replacementBlock != Blocks.AIR && group.replacementBlock != group.originalBlock;
                String countPrefix = group.count + "x ";
                String blockLabel = Compat.translatable(effectiveBlock.getDescriptionId()).getString();
                String fullText = countPrefix + blockLabel;
                int maxTextW = LIST_W - 64;
                String trimmed = this.font.plainSubstrByWidth(fullText, maxTextW);
                int labelColor = isReplaced ? 0xFF55FF55 : 0xFFDDDDDD;
                gui.text(this.font, trimmed, lx + 21, ry + 6, labelColor, false);

                // Arrow "->"
                gui.text(this.font, "->", lx + LIST_W - 38, ry + 6, isReplaced ? 0xFFFFFF55 : 0xFF888888, false);

                // Replacement ghost slot (18x18)
                int slotX = lx + LIST_W - 25;
                int slotY = ry + 1;
                boolean slotHovered = mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18;
                int slotBorder = slotHovered ? 0xFFFFFFFF : (isReplaced ? 0xFF44AA44 : 0xFF666666);
                gui.fill(slotX, slotY, slotX + 18, slotY + 18, slotBorder);
                gui.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0xFF181818);

                if (isReplaced) {
                    ItemStack replStack = new ItemStack(group.replacementBlock.asItem());
                    gui.item(replStack, slotX + 1, slotY + 1);
                    if (slotHovered) {
                        hoveredStackTooltip = replStack;
                    }
                } else if (slotHovered) {
                    hoveredTooltip = Compat.translatable("tooltip.wands.blueprint.replacement_slot");
                }
            }

            // Scrollbar indicator on the right edge of the list
            int sbX = lx + LIST_W - 5;
            gui.fill(sbX, ly, sbX + 5, ly + LIST_H, 0xFF151515);
            if (blockGroups.size() > VISIBLE_ROWS) {
                int maxScroll = blockGroups.size() - VISIBLE_ROWS;
                int thumbH = Math.max(10, (LIST_H * VISIBLE_ROWS) / blockGroups.size());
                int thumbY = ly + ((LIST_H - thumbH) * scrollOffset) / maxScroll;
                gui.fill(sbX + 1, thumbY, sbX + 4, thumbY + thumbH, 0xFF888888);
            }
        }

        // Render tooltips
        if (btnSaveCopy.shouldShowTooltip(mouseX, mouseY)) {
            Compat.renderComponentTooltip(gui, this.font, btnSaveCopy.getTooltipLines(), mouseX, mouseY);
        } else if (!hoveredStackTooltip.isEmpty()) {
            List<Component> lines = new ArrayList<>();
            lines.add(hoveredStackTooltip.getHoverName());
            lines.add(Compat.translatable("tooltip.wands.blueprint.right_click_clear").withStyle(ChatFormatting.GRAY));
            Compat.renderComponentTooltip(gui, this.font, lines, mouseX, mouseY);
        } else if (hoveredTooltip != null) {
            Compat.renderComponentTooltip(gui, this.font, List.of(hoveredTooltip), mouseX, mouseY);
        }
    }

    private int getHoveredReplacementRow(int mouseX, int mouseY) {
        int lx = this.leftPos + LIST_X_REL;
        int ly = this.topPos + LIST_Y_REL;
        int slotX = lx + LIST_W - 25;
        int maxRows = Math.min(VISIBLE_ROWS, blockGroups.size() - scrollOffset);
        for (int r = 0; r < maxRows; r++) {
            int ry = ly + r * ROW_H;
            int slotY = ry + 1;
            if (mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                return scrollOffset + r;
            }
        }
        return -1;
    }

    private void applyReplacement(int groupIdx, Block replBlock) {
        if (groupIdx < 0 || groupIdx >= blockGroups.size()) return;
        BlockGroup group = blockGroups.get(groupIdx);
        BlueprintItem.setReplacement(this.menu.blueprint, group.originalBlock, replBlock);
        String origId = Compat.getBlockId(group.originalBlock);
        String replId = (replBlock != null && replBlock != Blocks.AIR && replBlock != group.originalBlock)
                ? Compat.getBlockId(replBlock) : "";
        ClientNetworking.SendBlueprintAction(1, origId, replId);
        if (ClientRender.wand != null) {
            ClientRender.wand.force_render = true;
        }
        refreshGroups();
    }

    boolean is_hovering(int i, int j, int k, int l, double d, double e) {
        int m = this.leftPos;
        int n = this.topPos;
        d -= (double) m;
        e -= (double) n;
        return d >= (double) (i - 1) && d < (double) (i + k + 1) && e >= (double) (j - 1) && e < (double) (j + l + 1);
    }

    public final Slot find_slot(double d, double e) {
        for (int i = 0; i < this.menu.slots.size(); ++i) {
            Slot slot = this.menu.slots.get(i);
            if (is_hovering(slot.x, slot.y, 16, 16, d, e) && slot.isActive()) {
                return slot;
            }
        }
        return null;
    }

    @Override
    //?if >= 1.21.11 {
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        int mouseX = (int) mouseButtonEvent.x();
        int mouseY = (int) mouseButtonEvent.y();
        int button = mouseButtonEvent.button();
        boolean hasShiftDown = mouseButtonEvent.hasShiftDown();
    //?} else {
    /*public boolean mouseClicked(double dMouseX, double dMouseY, int button) {
        int mouseX = (int) dMouseX;
        int mouseY = (int) dMouseY;
        boolean hasShiftDown = hasShiftDown();
    *///?}
        int nx = this.leftPos + NAME_X_REL;
        int ny = this.topPos + NAME_Y_REL;
        if (mouseX >= nx && mouseX < nx + NAME_W && mouseY >= ny && mouseY < ny + NAME_H) {
            if (button == 1) {
                // Right-click clears name
                this.nameText = "";
                BlueprintItem.setBlueprintName(this.menu.blueprint, "");
                ClientNetworking.SendBlueprintAction(2, "", "");
            }
            this.nameFocused = true;
            return true;
        } else {
            this.nameFocused = false;
        }

        if (btnSaveCopy.click(mouseX, mouseY)) {
            return true;
        }

        // Check if clicking a replacement ghost slot
        int hoveredRow = getHoveredReplacementRow(mouseX, mouseY);
        if (hoveredRow >= 0) {
            if (button == 1) {
                // Right-click clears replacement
                applyReplacement(hoveredRow, Blocks.AIR);
                return true;
            } else if (button == 0) {
                ItemStack carried = this.menu.getCarried();
                if (!carried.isEmpty()) {
                    Block blk = Block.byItem(carried.getItem());
                    if (blk != null && blk != Blocks.AIR) {
                        applyReplacement(hoveredRow, blk);
                    }
                } else {
                    // Clicking with empty hand clears replacement
                    applyReplacement(hoveredRow, Blocks.AIR);
                }
                return true;
            }
        }

        // Check player inventory slots
        Slot slot = this.find_slot(mouseX, mouseY);
        if (slot != null) {
            if (button == 0 && hasShiftDown) {
                // Shift-click a block in inventory: assign it to the first unreplaced group (or first visible row) without consuming
                ItemStack clickedStack = slot.getItem();
                if (!clickedStack.isEmpty()) {
                    Block blk = Block.byItem(clickedStack.getItem());
                    if (blk != null && blk != Blocks.AIR && !blockGroups.isEmpty()) {
                        int targetIdx = -1;
                        for (int i = scrollOffset; i < Math.min(blockGroups.size(), scrollOffset + VISIBLE_ROWS); i++) {
                            BlockGroup g = blockGroups.get(i);
                            if (g.replacementBlock == null || g.replacementBlock == Blocks.AIR) {
                                targetIdx = i;
                                break;
                            }
                        }
                        if (targetIdx == -1) {
                            targetIdx = scrollOffset;
                        }
                        applyReplacement(targetIdx, blk);
                        return true;
                    }
                }
            }
            switch (button) {
                case 0:
                    this.slotClicked(slot, slot.index, button, ContainerInput.PICKUP);
                    break;
                case 1:
                    this.slotClicked(slot, slot.index, button, ContainerInput.PICKUP);
                    break;
                case 2:
                    this.slotClicked(slot, slot.index, button, ContainerInput.CLONE);
                    break;
            }
        }
        return true;
    }

    @Override
    //?if >= 1.21.11 {
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        return true;
    }
    //?} else {
    /*public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return true;
    }
    *///?}

    //?if >= 1.20.2 {
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (blockGroups.size() > VISIBLE_ROWS) {
            int maxScroll = blockGroups.size() - VISIBLE_ROWS;
            if (scrollY < 0) {
                scrollOffset = Math.min(maxScroll, scrollOffset + 1);
            } else if (scrollY > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    //?} else {
    /*@Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (blockGroups.size() > VISIBLE_ROWS) {
            int maxScroll = blockGroups.size() - VISIBLE_ROWS;
            if (scrollY < 0) {
                scrollOffset = Math.min(maxScroll, scrollOffset + 1);
            } else if (scrollY > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollY);
    }
    *///?}

    @Override
    //?if >= 1.21.11 {
    public boolean keyPressed(KeyEvent keyEvent) {
        //?if < 26.3 {
        int keyCode = keyEvent.key();
        //?} else {
        /*int keyCode = keyEvent.keycode();
        *///?}
        if (nameFocused) {
            if (keyCode == Compat.KEY_ESCAPE) {
                nameFocused = false;
                return true;
            }
            //?if < 26.3 {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                nameFocused = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!nameText.isEmpty()) {
                    nameText = nameText.substring(0, nameText.length() - 1);
                    BlueprintItem.setBlueprintName(this.menu.blueprint, nameText);
                    ClientNetworking.SendBlueprintAction(2, nameText, "");
                }
                return true;
            }
            //?} else {
            /*if (keyCode == com.mojang.blaze3d.platform.InputConstants.KEY_RETURN || keyCode == com.mojang.blaze3d.platform.InputConstants.KEY_NUMPADENTER) {
                nameFocused = false;
                return true;
            }
            if (keyCode == com.mojang.blaze3d.platform.InputConstants.KEY_BACKSPACE) {
                if (!nameText.isEmpty()) {
                    nameText = nameText.substring(0, nameText.length() - 1);
                    BlueprintItem.setBlueprintName(this.menu.blueprint, nameText);
                    ClientNetworking.SendBlueprintAction(2, nameText, "");
                }
                return true;
            }
            *///?}
            // Consume inventory key while typing in name field
            if (Minecraft.getInstance().options.keyInventory.matches(keyEvent)) {
                return true;
            }
        }
        return super.keyPressed(keyEvent);
    }
    //?} else {
    /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nameFocused) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                nameFocused = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!nameText.isEmpty()) {
                    nameText = nameText.substring(0, nameText.length() - 1);
                    BlueprintItem.setBlueprintName(this.menu.blueprint, nameText);
                    ClientNetworking.SendBlueprintAction(2, nameText, "");
                }
                return true;
            }
            if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    *///?}

    @Override
    //?if >= 1.21.11 {
    public boolean charTyped(CharacterEvent charEvent) {
        if (nameFocused) {
            String s = charEvent.codepointAsString();
            if (s != null && !s.isEmpty() && nameText.length() < 32) {
                char c = s.charAt(0);
                if (c >= 32 && c != 127) {
                    nameText += c;
                    BlueprintItem.setBlueprintName(this.menu.blueprint, nameText);
                    ClientNetworking.SendBlueprintAction(2, nameText, "");
                }
            }
            return true;
        }
        return super.charTyped(charEvent);
    }
    //?} else {
    /*public boolean charTyped(char codePoint, int modifiers) {
        if (nameFocused) {
            if (codePoint >= 32 && codePoint != 127 && nameText.length() < 32) {
                nameText += codePoint;
                BlueprintItem.setBlueprintName(this.menu.blueprint, nameText);
                ClientNetworking.SendBlueprintAction(2, nameText, "");
            }
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }
    *///?}
}
