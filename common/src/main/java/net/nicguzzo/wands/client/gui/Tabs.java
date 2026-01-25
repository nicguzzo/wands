package net.nicguzzo.wands.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * A container widget that arranges buttons in a vertical tab list.
 * Used for mode selection as vertical tabs on the left side.
 *
 * Features:
 * - Vertical layout with configurable spacing
 * - Selection tracking (set selected = index to highlight a tab)
 * - Tooltip support for child buttons
 */
public class Tabs extends Wdgt {
    public static final int DEFAULT_SPACING = 4;

    // Tab size
    public static final int TAB_SIZE = 14;          // Tab button height

    private final List<Btn> buttons = new ArrayList<>();
    private int spacing;

    /** Index of selected tab, or -1 for no selection */
    public int selected = -1;

    public Tabs() {
        this(DEFAULT_SPACING);
    }

    public Tabs(int spacing) {
        this.spacing = spacing;
    }

    /** Set the vertical spacing between tabs */
    public Tabs setSpacing(int spacing) {
        this.spacing = spacing;
        recalculateBounds();
        return this;
    }

    /** Get the current spacing */
    public int getSpacing() {
        return this.spacing;
    }

    /** Add a tab button */
    public Btn add(Btn btn) {
        buttons.add(btn);
        recalculateBounds();
        return btn;
    }

    /** Recalculate total width/height based on buttons */
    public void recalculateBounds() {
        if (buttons.isEmpty()) {
            this.width = 0;
            this.height = 0;
            return;
        }

        int maxWidth = 0;
        int totalHeight = 0;
        for (int i = 0; i < buttons.size(); i++) {
            Btn btn = buttons.get(i);
            maxWidth = Math.max(maxWidth, btn.width);
            if (i > 0) {
                totalHeight += spacing;
            }
            totalHeight += btn.height;
        }

        this.width = maxWidth;
        this.height = totalHeight;
    }

    /** Position buttons in vertical layout */
    private void layout() {
        int currentY = this.y;
        for (Btn btn : buttons) {
            btn.x = this.x;
            btn.y = currentY;
            currentY += btn.height + spacing;
        }
    }

    @Override
    public void render(GuiGraphics gui, Font font, int mouseX, int mouseY) {
        if (!visible) return;
        recalculateBounds();
        layout();

        // Render all tabs
        for (int i = 0; i < buttons.size(); i++) {
            Btn btn = buttons.get(i);
            btn.selected = (i == selected);
            btn.showBackground = false;
            btn.render(gui, font, mouseX, mouseY);
        }
    }

    @Override
    protected boolean handleClick(int mouseX, int mouseY) {
        // Early exit if click is outside tabs area
        if (!inside(mouseX, mouseY)) {
            return false;
        }

        // Check each button
        for (Btn btn : buttons) {
            if (btn.inside(mouseX, mouseY)) {
                btn.click(mouseX, mouseY);
                return true;
            }
        }
        return false;
    }

    /** Get all buttons */
    public List<Btn> getAllVisibleButtons() {
        return buttons;
    }

    /** Returns number of tabs */
    public int size() {
        return buttons.size();
    }

    @Override
    public boolean shouldShowTooltip(int mouseX, int mouseY) {
        if (!visible) return false;
        for (Btn btn : buttons) {
            if (btn.shouldShowTooltip(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    /** Find the button the mouse is hovering over (for tooltip display) */
    public Btn getHoveredButton(int mouseX, int mouseY) {
        if (!visible) return null;
        for (Btn btn : buttons) {
            if (btn.inside(mouseX, mouseY) && (btn.tooltip != null || btn.tooltipTitle != null)) {
                return btn;
            }
        }
        return null;
    }
}
