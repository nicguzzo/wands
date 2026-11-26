package net.nicguzzo.wands.client.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//?if >= 1.21.11 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.nicguzzo.wands.client.gui.Btn;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.compat.RcId;
import net.nicguzzo.wands.items.BlueprintItem;
import net.nicguzzo.wands.items.BlueprintItem.BlockGroup;
import net.nicguzzo.wands.menues.BlueprintMenu;
import net.nicguzzo.wands.networking.ClientNetworking;

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
    private static final int VISIBLE_ROWS = BlueprintMenu.VISIBLE_ROWS;
    private static final int LIST_H = VISIBLE_ROWS * ROW_H;

    private static final int NAME_X_REL = 7;
    private static final int NAME_Y_REL = 6;
    private static final int NAME_W = 104;
    private static final int NAME_H = 14;

    private String nameText = "";
    private EditBox nameBox;
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

        nameBox = new EditBox(this.font, this.leftPos + NAME_X_REL, this.topPos + NAME_Y_REL, NAME_W, NAME_H, Compat.translatable("screen.wands.blueprint.name_placeholder"));
        nameBox.setMaxLength(32);
        nameBox.setValue(this.nameText);
        nameBox.setHint(Compat.translatable("screen.wands.blueprint.name_placeholder"));
        nameBox.setTextColor(0xFFFFFFFF);
        nameBox.setCanLoseFocus(true);
        nameBox.setResponder(newName -> {
            this.nameText = newName;
            BlueprintItem.setBlueprintName(this.menu.blueprint, newName);
            syncMainHandBlueprint();
            ClientNetworking.SendBlueprintAction(2, newName, "");
        });
        this.addRenderableWidget(nameBox);

        refreshGroups();

        btnSaveCopy = new Btn(this.leftPos + 114, this.topPos + 6, 55, 14, Compat.translatable("screen.wands.blueprint.save_copy"), (mx, my) -> {
            if (ClientRender.wand != null && !ClientRender.wand.copy_paste_buffer.isEmpty()) {
                BlueprintItem.saveCopyBufferToBlueprint(this.menu.blueprint, ClientRender.wand.copy_paste_buffer);
                String id = BlueprintItem.getOrCreateId(this.menu.blueprint);
                syncMainHandBlueprint();
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

    private void syncMainHandBlueprint() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack mainHand = mc.player.getMainHandItem();
            if (mainHand.getItem() instanceof BlueprintItem) {
                CompoundTag bpTag = BlueprintItem.getBlueprintTag(this.menu.blueprint);
                BlueprintItem.saveBlueprintTag(mainHand, bpTag);
            }
        }
    }

    private void refreshGroups() {
        this.blockGroups = new ArrayList<>(BlueprintItem.getBlockGroups(this.menu.blueprint));
        int maxScroll = Math.max(0, blockGroups.size() - VISIBLE_ROWS);
        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }
        this.menu.updateRowCount(this.scrollOffset);
    }

    private void syncFromSlotsToBlueprint() {
        int maxRows = Math.min(VISIBLE_ROWS, Math.max(0, blockGroups.size() - scrollOffset));
        boolean changed = false;
        for (int r = 0; r < maxRows; r++) {
            int idx = scrollOffset + r;
            BlockGroup g = blockGroups.get(idx);
            ItemStack slotStack = this.menu.replacementInventory.getItem(r);
            Block newRepl = slotStack.isEmpty() ? Blocks.AIR : Block.byItem(slotStack.getItem());
            if (newRepl == null || newRepl == g.originalBlock) {
                newRepl = Blocks.AIR;
            }
            Block oldRepl = (g.replacementBlock == null) ? Blocks.AIR : g.replacementBlock;
            if (newRepl != oldRepl) {
                BlueprintItem.setReplacement(this.menu.blueprint, g.originalBlock, newRepl);
                String origId = Compat.getBlockId(g.originalBlock);
                String replId = (newRepl != Blocks.AIR) ? Compat.getBlockId(newRepl) : "";
                ClientNetworking.SendBlueprintAction(1, origId, replId);
                changed = true;
            }
        }
        if (changed) {
            syncMainHandBlueprint();
            if (ClientRender.wand != null) {
                ClientRender.wand.force_render = true;
            }
            refreshGroups();
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

        // Block list container area
        int lx = x + LIST_X_REL;
        int ly = y + LIST_Y_REL;
        gui.fill(lx - 1, ly - 1, lx + LIST_W + 1, ly + LIST_H + 1, 0xFF373737);
        gui.fill(lx, ly, lx + LIST_W, ly + LIST_H, 0xFF222222);

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

                // Replacement slot frame (18x18 around Slot r at 145, 28 + r * 20)
                int slotX = lx + LIST_W - 25;
                int slotY = ry + 1;
                boolean slotHovered = mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18;
                int slotBorder = slotHovered ? 0xFFFFFFFF : (isReplaced ? 0xFF44AA44 : 0xFF666666);
                gui.fill(slotX, slotY, slotX + 18, slotY + 18, slotBorder);
                gui.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0xFF181818);
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

        int lx = x + LIST_X_REL;
        int ly = y + LIST_Y_REL;

        Component hoveredTooltip = null;
        ItemStack hoveredOrigStack = ItemStack.EMPTY;

        if (!blockGroups.isEmpty()) {
            int maxRows = Math.min(VISIBLE_ROWS, blockGroups.size() - scrollOffset);
            for (int r = 0; r < maxRows; r++) {
                int idx = scrollOffset + r;
                BlockGroup group = blockGroups.get(idx);
                int ry = ly + r * ROW_H;

                int origIconX = lx + 2;
                int origIconY = ry + 2;
                if (mouseX >= origIconX && mouseX < origIconX + 16 && mouseY >= origIconY && mouseY < origIconY + 16) {
                    hoveredOrigStack = new ItemStack(group.originalBlock.asItem());
                }

                int slotX = lx + LIST_W - 25;
                int slotY = ry + 1;
                boolean isReplaced = group.replacementBlock != null && group.replacementBlock != Blocks.AIR && group.replacementBlock != group.originalBlock;
                if (!isReplaced && mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                    hoveredTooltip = Compat.translatable("tooltip.wands.blueprint.replacement_slot");
                }
            }
        }

        if (btnSaveCopy.shouldShowTooltip(mouseX, mouseY)) {
            Compat.renderComponentTooltip(gui, this.font, btnSaveCopy.getTooltipLines(), mouseX, mouseY);
        } else if (!hoveredOrigStack.isEmpty()) {
            Compat.renderComponentTooltip(gui, this.font, List.of(hoveredOrigStack.getHoverName()), mouseX, mouseY);
        } else if (hoveredTooltip != null) {
            Compat.renderComponentTooltip(gui, this.font, List.of(hoveredTooltip), mouseX, mouseY);
        }
        //?if < 26.1
        //this.renderTooltip(gui, mouseX, mouseY);
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
        if (nameBox != null) {
            int nx = nameBox.getX();
            int ny = nameBox.getY();
            if (mouseX >= nx && mouseX < nx + nameBox.getWidth() && mouseY >= ny && mouseY < ny + nameBox.getHeight()) {
                if (button == 1) {
                    nameBox.setValue("");
                }
                this.setFocused(nameBox);
                nameBox.setFocused(true);
                //?if >= 1.21.11 {
                nameBox.mouseClicked(mouseButtonEvent, bl);
                //?} else {
                /*nameBox.mouseClicked(dMouseX, dMouseY, button);
                *///?}
                return true;
            } else {
                nameBox.setFocused(false);
                if (this.getFocused() == nameBox) {
                    this.setFocused(null);
                }
            }
        }

        if (btnSaveCopy.click(mouseX, mouseY)) {
            return true;
        }

        Slot slot = this.find_slot(mouseX, mouseY);
        if (slot != null) {
            switch (button) {
                case 0:
                    if (hasShiftDown) {
                        this.slotClicked(slot, slot.index, button, ContainerInput.QUICK_MOVE);
                    } else {
                        this.slotClicked(slot, slot.index, button, ContainerInput.PICKUP);
                    }
                    break;
                case 1:
                    this.slotClicked(slot, slot.index, button, ContainerInput.PICKUP);
                    break;
                case 2:
                    this.slotClicked(slot, slot.index, button, ContainerInput.CLONE);
                    break;
            }
            syncFromSlotsToBlueprint();
        } else if (button == 1) {
            // Right-clicking outside slots clears carried ghost item
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                Compat.set_carried(client.player, this.menu, ItemStack.EMPTY);
            }
        }
        return true;
    }

    @Override
    //?if >= 1.21.11 {
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        int mouseX = (int) mouseButtonEvent.x();
        int mouseY = (int) mouseButtonEvent.y();
        int button = mouseButtonEvent.button();
    //?} else {
    /*public boolean mouseReleased(double dMouseX, double dMouseY, int button) {
        int mouseX = (int) dMouseX;
        int mouseY = (int) dMouseY;
    *///?}
        if (button == 0) {
            Slot slot = this.find_slot(mouseX, mouseY);
            if (slot != null && slot.index < VISIBLE_ROWS) {
                Minecraft client = Minecraft.getInstance();
                ItemStack carried = Compat.get_carried(client.player, this.menu);
                if (!carried.isEmpty()) {
                    this.slotClicked(slot, slot.index, button, ContainerInput.PICKUP);
                    syncFromSlotsToBlueprint();
                }
            }
        }
        return true;
    }

    @Override
    //?if >= 1.21.11 {
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double d, double e) {
        int mouseX = (int) mouseButtonEvent.x();
        int mouseY = (int) mouseButtonEvent.y();
        int button = mouseButtonEvent.button();
    //?} else {
    /*public boolean mouseDragged(double dMouseX, double dMouseY, int button, double deltaX, double deltaY) {
        int mouseX = (int) dMouseX;
        int mouseY = (int) dMouseY;
    *///?}
        Slot slot = this.find_slot(mouseX, mouseY);
        if (slot != null && slot.index < VISIBLE_ROWS) {
            Minecraft client = Minecraft.getInstance();
            ItemStack itemStack = Compat.get_carried(client.player, this.menu);
            if (!itemStack.isEmpty() && slot.getItem().isEmpty()) {
                this.slotClicked(slot, slot.index, button, ContainerInput.QUICK_CRAFT);
                syncFromSlotsToBlueprint();
            }
        }
        return true;
    }

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
            this.menu.updateRowCount(this.scrollOffset);
            ClientNetworking.SendBlueprintAction(5, String.valueOf(this.scrollOffset), "");
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
            this.menu.updateRowCount(this.scrollOffset);
            ClientNetworking.SendBlueprintAction(5, String.valueOf(this.scrollOffset), "");
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollY);
    }
    *///?}

    @Override
    //?if >= 1.21.11 {
    public boolean keyPressed(KeyEvent keyEvent) {
        int keyCode = keyEvent.key();
        if (nameBox != null && nameBox.isFocused()) {
            if (keyCode == Compat.KEY_ESCAPE || keyCode == Compat.KEY_ENTER || keyCode == Compat.KEY_KP_ENTER) {
                nameBox.setFocused(false);
                if (this.getFocused() == nameBox) {
                    this.setFocused(null);
                }
                return true;
            }
            nameBox.keyPressed(keyEvent);
            return true;
        }
        return super.keyPressed(keyEvent);
    }
    //?} else {
    /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nameBox != null && nameBox.isFocused()) {
            if (keyCode == Compat.KEY_ESCAPE || keyCode == Compat.KEY_ENTER || keyCode == Compat.KEY_KP_ENTER) {
                nameBox.setFocused(false);
                if (this.getFocused() == nameBox) {
                    this.setFocused(null);
                }
                return true;
            }
            nameBox.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    *///?}

    @Override
    //?if >= 1.21.11 {
    public boolean charTyped(CharacterEvent charEvent) {
        if (nameBox != null && nameBox.isFocused()) {
            return nameBox.charTyped(charEvent);
        }
        return super.charTyped(charEvent);
    }
    //?} else {
    /*public boolean charTyped(char codePoint, int modifiers) {
        if (nameBox != null && nameBox.isFocused()) {
            return nameBox.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }
    *///?}
}
