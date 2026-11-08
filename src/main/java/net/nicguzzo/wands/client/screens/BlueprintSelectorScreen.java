package net.nicguzzo.wands.client.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
//?if >= 1.21.11 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.gui.Btn;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.items.BlueprintItem;
import net.nicguzzo.wands.networking.ClientNetworking;
import net.nicguzzo.wands.wand.InventoryManager;
import net.nicguzzo.wands.wand.InventoryManager.BlueprintEntry;

import java.util.ArrayList;
import java.util.List;

public class BlueprintSelectorScreen extends Screen {

    private static final int PANEL_W = 240;
    private static final int PANEL_H = 190;
    private static final int LIST_W = 224;
    private static final int ROW_H = 22;
    private static final int VISIBLE_ROWS = 6;
    private static final int LIST_H = VISIBLE_ROWS * ROW_H;

    private static class SelectorRow {
        final String id; // "" for in-memory copy buffer
        final String title;
        final String subtitle;
        final ItemStack iconStack;
        final boolean isBlueprint;

        SelectorRow(String id, String title, String subtitle, ItemStack iconStack, boolean isBlueprint) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.iconStack = iconStack;
            this.isBlueprint = isBlueprint;
        }
    }

    private final List<SelectorRow> rows = new ArrayList<>();
    private int scrollOffset = 0;
    private String selectedId = "";
    private Btn btnSaveCopy;
    private Btn btnClose;

    public BlueprintSelectorScreen() {
        super(Compat.translatable("screen.wands.blueprint_selector.title"));
    }

    @Override
    protected void init() {
        super.init();
        if (ClientRender.wand != null) {
            this.selectedId = ClientRender.wand.getActiveBlueprintId();
        }
        refreshRows();

        int panelX = (this.width - PANEL_W) / 2;
        int panelY = (this.height - PANEL_H) / 2;

        btnSaveCopy = new Btn(panelX + 8, panelY + PANEL_H - 22, 148, 16, Compat.translatable("screen.wands.blueprint_selector.save_to_selected"), (mx, my) -> {
            if (ClientRender.wand != null && !ClientRender.wand.copy_paste_buffer.isEmpty() && !selectedId.isEmpty()) {
                Minecraft client = Minecraft.getInstance();
                if (client.player != null) {
                    BlueprintEntry entry = InventoryManager.findBlueprintById(client.player, selectedId);
                    if (entry != null) {
                        InventoryManager.updateBlueprintEntry(client.player, entry, stack -> {
                            BlueprintItem.saveCopyBufferToBlueprint(stack, ClientRender.wand.copy_paste_buffer);
                        });
                    }
                }
                ClientRender.wand.setActiveBlueprintId(selectedId);
                ClientNetworking.SendBlueprintAction(4, selectedId, "");
                refreshRows();
            }
        });
        btnSaveCopy.centerText = true;

        btnClose = new Btn(panelX + PANEL_W - 68, panelY + PANEL_H - 22, 60, 16, Compat.translatable("screen.wands.blueprint_selector.done"), (mx, my) -> {
            onClose();
        });
        btnClose.centerText = true;
    }

    private void refreshRows() {
        rows.clear();
        Minecraft client = Minecraft.getInstance();
        int copyCount = (ClientRender.wand != null && ClientRender.wand.copy_paste_buffer != null)
                ? ClientRender.wand.copy_paste_buffer.size() : 0;
        String copySub = copyCount > 0 ? (copyCount + " blocks copied in memory") : "Empty (use Copy mode first)";
        rows.add(new SelectorRow("", Compat.translatable("screen.wands.blueprint.in_memory").getString(), copySub, new ItemStack(Items.PAPER), false));

        if (client.player != null) {
            List<BlueprintEntry> entries = InventoryManager.findBlueprints(client.player);
            for (BlueprintEntry e : entries) {
                String name = BlueprintItem.getBlueprintName(e.stack);
                if (name.isEmpty()) {
                    name = e.stack.getHoverName().getString();
                }
                int count = BlueprintItem.getTotalBlockCount(e.stack);
                int[] sz = BlueprintItem.getDimensions(e.stack);
                String loc = e.inShulker ? " [Shulker]" : (e.invSlot == -1 ? " [Offhand]" : "");
                String sub = count > 0
                        ? (count + " blocks (" + sz[0] + "x" + sz[1] + "x" + sz[2] + ")" + loc)
                        : ("Empty blueprint" + loc);
                rows.add(new SelectorRow(e.id, name, sub, e.stack, true));
            }
        }

        // Ensure selectedId exists in rows, otherwise default to ""
        boolean found = false;
        for (SelectorRow r : rows) {
            if (r.id.equals(selectedId)) {
                found = true;
                break;
            }
        }
        if (!found) {
            selectedId = "";
        }

        int maxScroll = Math.max(0, rows.size() - VISIBLE_ROWS);
        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }
    }

    private void selectBlueprint(String id) {
        this.selectedId = id != null ? id : "";
        if (ClientRender.wand != null) {
            ClientRender.wand.setActiveBlueprintId(this.selectedId);
        }
        ClientNetworking.SendBlueprintAction(3, this.selectedId, "");
    }

    @Override
    //?if >= 26.1 {
    public void extractBackground(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        super.extractBackground(gui, mouseX, mouseY, delta);
    //?} else {
    /*public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        super.render(gui, mouseX, mouseY, delta);
    *///?}
        int panelX = (this.width - PANEL_W) / 2;
        int panelY = (this.height - PANEL_H) / 2;

        // Scrim & Panel
        gui.fill(0, 0, this.width, this.height, 0x80000000);
        gui.fill(panelX - 1, panelY - 1, panelX + PANEL_W + 1, panelY + PANEL_H + 1, 0xFF555555);
        gui.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, WandScreen.COLOR_PANEL_BACKGROUND);

        // Title
        String titleStr = this.title.getString();
        int tw = this.font.width(titleStr);
        gui.text(this.font, titleStr, panelX + (PANEL_W - tw) / 2, panelY + 8, WandScreen.COLOR_TEXT_PRIMARY, false);

        // List background
        int listX = panelX + 8;
        int listY = panelY + 24;
        gui.fill(listX - 1, listY - 1, listX + LIST_W + 1, listY + LIST_H + 1, 0xFF444444);
        gui.fill(listX, listY, listX + LIST_W, listY + LIST_H, 0xFF1E1E1E);

        String activeId = ClientRender.wand != null ? ClientRender.wand.getActiveBlueprintId() : "";

        int maxRows = Math.min(VISIBLE_ROWS, rows.size() - scrollOffset);
        for (int r = 0; r < maxRows; r++) {
            int idx = scrollOffset + r;
            SelectorRow row = rows.get(idx);
            int ry = listY + r * ROW_H;
            int rowRight = listX + LIST_W - 6;

            boolean isHovered = mouseX >= listX && mouseX < rowRight && mouseY >= ry && mouseY < ry + ROW_H;
            boolean isSelected = row.id.equals(selectedId);
            boolean isActive = row.id.equals(activeId);

            int bgCol = isSelected ? 0xFF3A4A3A : (isHovered ? 0xFF333333 : ((idx % 2 == 0) ? 0xFF262626 : 0xFF202020));
            gui.fill(listX, ry, rowRight, ry + ROW_H - 1, bgCol);

            if (isActive) {
                // Green left accent bar for active paste source
                gui.fill(listX, ry, listX + 3, ry + ROW_H - 1, 0xFF44CC44);
            }

            gui.item(row.iconStack, listX + 6, ry + 3);

            int titleColor = isActive ? 0xFF55FF55 : 0xFFFFFFFF;
            String trimmedTitle = this.font.plainSubstrByWidth(row.title, LIST_W - 36);
            gui.text(this.font, trimmedTitle, listX + 26, ry + 2, titleColor, false);

            String trimmedSub = this.font.plainSubstrByWidth(row.subtitle, LIST_W - 36);
            gui.text(this.font, trimmedSub, listX + 26, ry + 12, 0xFFAAAAAA, false);
        }

        // Scrollbar
        int sbX = listX + LIST_W - 5;
        gui.fill(sbX, listY, sbX + 5, listY + LIST_H, 0xFF141414);
        if (rows.size() > VISIBLE_ROWS) {
            int maxScroll = rows.size() - VISIBLE_ROWS;
            int thumbH = Math.max(12, (LIST_H * VISIBLE_ROWS) / rows.size());
            int thumbY = listY + ((LIST_H - thumbH) * scrollOffset) / maxScroll;
            gui.fill(sbX + 1, thumbY, sbX + 4, thumbY + thumbH, 0xFF888888);
        }

        // Buttons
        boolean canSave = ClientRender.wand != null && !ClientRender.wand.copy_paste_buffer.isEmpty() && !selectedId.isEmpty();
        btnSaveCopy.disabled = !canSave;
        btnSaveCopy.x = panelX + 8;
        btnSaveCopy.y = panelY + PANEL_H - 22;
        btnSaveCopy.render(gui, this.font, mouseX, mouseY);

        btnClose.x = panelX + PANEL_W - 68;
        btnClose.y = panelY + PANEL_H - 22;
        btnClose.render(gui, this.font, mouseX, mouseY);
    }

    //?if >= 1.21.1 < 26.1 {
    /*@Override
    public void renderBackground(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        // Handled in render()
    }
    *///?}

    @Override
    //?if >= 1.21.11 {
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        int mouseX = (int) mouseButtonEvent.x();
        int mouseY = (int) mouseButtonEvent.y();
    //?} else {
    /*public boolean mouseClicked(double dMouseX, double dMouseY, int button) {
        int mouseX = (int) dMouseX;
        int mouseY = (int) dMouseY;
    *///?}
        if (btnSaveCopy.click(mouseX, mouseY)) return true;
        if (btnClose.click(mouseX, mouseY)) return true;

        int panelX = (this.width - PANEL_W) / 2;
        int panelY = (this.height - PANEL_H) / 2;
        int listX = panelX + 8;
        int listY = panelY + 24;
        int rowRight = listX + LIST_W - 6;

        if (mouseX >= listX && mouseX < rowRight && mouseY >= listY && mouseY < listY + LIST_H) {
            int clickedRow = (mouseY - listY) / ROW_H;
            int idx = scrollOffset + clickedRow;
            if (idx >= 0 && idx < rows.size()) {
                selectBlueprint(rows.get(idx).id);
                return true;
            }
        }
        return super.mouseClicked(
                //?if >= 1.21.11 {
                mouseButtonEvent, bl
                //?} else {
                /*dMouseX, dMouseY, button
                *///?}
        );
    }

    //?if >= 1.20.2 {
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (rows.size() > VISIBLE_ROWS) {
            int maxScroll = rows.size() - VISIBLE_ROWS;
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
        if (rows.size() > VISIBLE_ROWS) {
            int maxScroll = rows.size() - VISIBLE_ROWS;
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
        int scancode = keyEvent.scancode();
        //?} else {
        /*int scancode = keyEvent.keycode();
        *///?}
        var bpKm = WandsModClient.reverseKeys.get(WandsMod.WandKeys.BLUEPRINT);
        if ((bpKm != null && bpKm.matches(keyEvent)) || scancode == 256) {
            onClose();
            return true;
        }
        return super.keyPressed(keyEvent);
    }
    //?} else {
    /*public boolean keyPressed(int keysym, int scancode, int modifiers) {
        var bpKm = WandsModClient.reverseKeys.get(WandsMod.WandKeys.BLUEPRINT);
        if ((bpKm != null && bpKm.matches(keysym, scancode)) || keysym == 256) {
            onClose();
            return true;
        }
        return super.keyPressed(keysym, scancode, modifiers);
    }
    *///?}
}
