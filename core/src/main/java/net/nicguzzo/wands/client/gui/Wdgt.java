package net.nicguzzo.wands.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.nicguzzo.wands.client.screens.WandScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all custom GUI widgets.
 * Provides common functionality for positioning, visibility, hit detection, and tooltips.
 */
public abstract class Wdgt {
    // Layout constants
    public static final int TEXT_PADDING = 4;
    public static final int DEFAULT_HEIGHT = 14;
    public static final int SPINNER_BUTTON_WIDTH = 10;  // Width of +/- buttons in spinners

    // Tooltip layout constants
    public static final int TOOLTIP_X_OFFSET = 8;       // Horizontal offset from cursor
    public static final int TOOLTIP_Y_OFFSET = 2;       // Vertical offset above cursor
    public static final int TOOLTIP_MIN_WIDTH = 100;    // Minimum tooltip width
    public static final int TOOLTIP_MARGIN = 8;         // Margin from screen edge
    public static final int TOOLTIP_PADDING = 4;        // Internal padding (all edges)

    // Default colors for label:value style widgets
    public int labelColor = WandScreen.COLOR_WDGT_LABEL;
    public int valueColor = WandScreen.COLOR_TEXT_PRIMARY;

    // Position (top-left corner in screen pixels)
    public int x;
    public int y;

    // Size in pixels
    public int width;
    public int height;

    // Whether this widget should be rendered and respond to input
    public boolean visible = true;

    // Whether to draw the background
    public boolean showBackground = true;

    // Whether to draw text with drop shadow
    public boolean drawShadow = true;

    // Tooltip text (shown on hover)
    public Component tooltip = null;
    public Component tooltipTitle = null;

    /**
     * Render this widget.
     * @param gui Graphics context for drawing
     * @param font Font for text rendering
     * @param mouseX Current mouse X position in screen pixels
     * @param mouseY Current mouse Y position in screen pixels
     */
    public abstract void render(GuiGraphics gui, Font font, int mouseX, int mouseY);

    /**
     * Handle a left mouse click. Checks visibility first, then delegates to handleClick().
     * Subclasses should override handleClick() instead of this method.
     * @param mouseX Mouse X position when clicked
     * @param mouseY Mouse Y position when clicked
     * @return true if the click was handled (stops propagation)
     */
    public boolean click(int mouseX, int mouseY) {
        if (!visible) return false;
        return handleClick(mouseX, mouseY);
    }

    /**
     * Override this to handle clicks. Called only when widget is visible.
     * Most widgets should check inside(mouseX, mouseY) first.
     */
    protected boolean handleClick(int mouseX, int mouseY) {
        return false;
    }

    /**
     * Handle mouse scroll wheel. Checks visibility first, then delegates to handleScroll().
     * Subclasses should override handleScroll() instead of this method.
     * @param mouseX Mouse X position
     * @param mouseY Mouse Y position
     * @param scrollDelta Scroll amount: positive = up, negative = down
     * @return true if the scroll was handled
     */
    public boolean scroll(int mouseX, int mouseY, double scrollDelta) {
        if (!visible) return false;
        return handleScroll(mouseX, mouseY, scrollDelta);
    }

    /**
     * Override this to handle scroll. Called only when widget is visible.
     * Most widgets should check inside(mouseX, mouseY) first.
     */
    protected boolean handleScroll(int mouseX, int mouseY, double scrollDelta) {
        return false;
    }

    /**
     * @return true if this widget can be clicked (used for cursor changes)
     */
    public boolean isClickable() {
        return true;
    }

    /**
     * Check if a point is within this widget's bounding box.
     * @param mouseX X coordinate to test
     * @param mouseY Y coordinate to test
     * @return true if the point is inside the widget
     */
    public boolean inside(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < (x + width)
            && mouseY >= y && mouseY < (y + height);
    }

    /**
     * Set tooltip text for this widget (fluent API).
     * @param title Tooltip title (shown first, often highlighted)
     * @param description Tooltip description
     * @return this widget for chaining
     */
    public Wdgt withTooltip(Component title, Component description) {
        this.tooltipTitle = title;
        this.tooltip = description;
        return this;
    }

    /**
     * @return true if tooltip should be shown (visible, has tooltip, mouse is hovering)
     */
    public boolean shouldShowTooltip(int mouseX, int mouseY) {
        return visible && (tooltip != null || tooltipTitle != null) && inside(mouseX, mouseY);
    }

    /**
     * @return Tooltip lines to display (title first, then description)
     */
    public List<Component> getTooltipLines() {
        if (tooltipTitle != null && tooltip != null) {
            return List.of(tooltipTitle, tooltip);
        } else if (tooltipTitle != null) {
            return List.of(tooltipTitle);
        } else if (tooltip != null) {
            return List.of(tooltip);
        }
        return List.of();
    }

    /**
     * Draw standard widget background (highlighted on hover).
     */
    protected void drawBackground(GuiGraphics gui, int mouseX, int mouseY) {
        if (!showBackground) return;
        int backgroundColor = inside(mouseX, mouseY) ? WandScreen.COLOR_WDGT_HOVER : Btn.COLOR_NORMAL;
        gui.fill(x, y, x + width, y + height, backgroundColor);
    }

    /**
     * Calculate Y position for vertically centered text.
     */
    protected int getTextY(Font font) {
        return y + (height - font.lineHeight + 1) / 2;
    }

    /**
     * Play the standard UI click sound.
     */
    protected void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(
            SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F));
    }

    /**
     * Render a tooltip for a widget with automatic line wrapping and styling.
     * Title is rendered in white, description in gray.
     *
     * @param gui Graphics context
     * @param font Font for text rendering
     * @param widget The widget whose tooltip to render
     * @param mouseX Current mouse X position
     * @param mouseY Current mouse Y position
     * @param screenWidth Total screen width for wrapping calculations
     * @param screenHeight Total screen height for vertical bounds checking
     */
    public static void renderWidgetTooltip(GuiGraphics gui, Font font, Wdgt widget, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        List<Component> tooltipLines = widget.getTooltipLines();
        if (tooltipLines.isEmpty()) return;

        int tooltipX = mouseX + TOOLTIP_X_OFFSET;
        int maxWidth = screenWidth - tooltipX - TOOLTIP_MARGIN;
        if (maxWidth < TOOLTIP_MIN_WIDTH) maxWidth = TOOLTIP_MIN_WIDTH;

        // Wrap lines that exceed max width and track if it's a title line
        List<FormattedCharSequence> wrappedLines = new ArrayList<>();
        List<Boolean> isTitleLine = new ArrayList<>();
        boolean isFirstLine = true;
        for (Component line : tooltipLines) {
            boolean isTitle = isFirstLine && widget.tooltipTitle != null;
            List<FormattedCharSequence> split = font.split(line, maxWidth);
            for (FormattedCharSequence seq : split) {
                wrappedLines.add(seq);
                isTitleLine.add(isTitle);
            }
            isFirstLine = false;
        }

        int textWidth = 0;
        for (FormattedCharSequence line : wrappedLines) {
            textWidth = Math.max(textWidth, font.width(line));
        }
        int textHeight = wrappedLines.size() * font.lineHeight;

        // Box dimensions with consistent padding on all sides
        int boxWidth = textWidth + TOOLTIP_PADDING * 2;
        int boxHeight = textHeight + TOOLTIP_PADDING * 2;
        int boxX = tooltipX;
        int boxY = mouseY - boxHeight - TOOLTIP_Y_OFFSET;

        // Clamp to screen bounds - if clipped at top, show below cursor instead
        if (boxY < TOOLTIP_MARGIN) {
            boxY = mouseY + TOOLTIP_Y_OFFSET + font.lineHeight;
        }
        // If still clipped at bottom, clamp to bottom edge
        if (boxY + boxHeight > screenHeight - TOOLTIP_MARGIN) {
            boxY = screenHeight - TOOLTIP_MARGIN - boxHeight;
        }

        // Draw background
        gui.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, WandScreen.COLOR_TOOLTIP_BG);

        // Draw text with padding offset
        int textX = boxX + TOOLTIP_PADDING;
        int textY = boxY + TOOLTIP_PADDING;
        for (int i = 0; i < wrappedLines.size(); i++) {
            int color = isTitleLine.get(i) ? WandScreen.COLOR_TOOLTIP_TITLE : WandScreen.COLOR_TOOLTIP_DESC;
            gui.drawString(font, wrappedLines.get(i), textX, textY, color, true);
            textY += font.lineHeight;
        }
    }
}