package net.nicguzzo.wands.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.nicguzzo.wands.client.screens.WandScreen;

/**
 * A horizontal divider line widget for separating groups of options.
 * Renders a 1px line centered vertically with 3px padding above and below.
 */
public class Divider extends Wdgt {
    private static final int LINE_HEIGHT = 1;
    private static final int VERTICAL_PADDING = 3;

    public Divider(int width) {
        this.width = width;
        this.height = LINE_HEIGHT + VERTICAL_PADDING * 2;  // 7px total
    }

    @Override
    public void render(GuiGraphics gui, Font font, int mouseX, int mouseY) {
        if (!visible) return;
        int lineY = y + VERTICAL_PADDING;
        gui.fill(x, lineY, x + width, lineY + LINE_HEIGHT, WandScreen.COLOR_TAB_DIVIDER);
    }

    @Override
    public boolean isClickable() {
        return false;
    }
}
