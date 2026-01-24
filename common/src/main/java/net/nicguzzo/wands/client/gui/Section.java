package net.nicguzzo.wands.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.nicguzzo.wands.client.screens.WandScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * A container widget that groups child widgets vertically with a title.
 * Used to organize related controls into labeled sections.
 *
 * Layout:
 *   [Title]
 *   [Child 1]
 *   [Child 2]
 *   ...
 *
 * Children are laid out vertically with VERTICAL_SPACING between them.
 * Only visible children are rendered and included in layout calculations.
 */
public class Section extends Wdgt {
    // Layout constants
    public static final int VERTICAL_SPACING = 4;   // Space between child widgets
    public static final int TITLE_HEIGHT = 10;      // Height reserved for title text
    public static final int TITLE_MARGIN = 2;       // Space between title and first child

    private Component title;
    private final List<Wdgt> children = new ArrayList<>();
    private int titleColor = WandScreen.COLOR_TEXT_SECTION;

    public Section(Component title) {
        this.title = title;
    }

    @Override
    public Section withTooltip(Component title, Component description) {
        super.withTooltip(title, description);
        return this;
    }

    /** Add a child widget to this section */
    public Section add(Wdgt widget) {
        children.add(widget);
        return this;
    }

    /**
     * Recalculate section bounds based on ALL children (not just visible).
     * This ensures consistent bounds so clicks don't fall through when widgets hide.
     * Call after adding children and setting x position.
     */
    public void recalculateBounds() {
        int maxWidth = 0;
        int totalHeight = TITLE_HEIGHT + TITLE_MARGIN;
        boolean first = true;

        for (Wdgt child : children) {
            // Include ALL children in bounds calculation, not just visible ones
            maxWidth = Math.max(maxWidth, child.width);
            if (!first) {
                totalHeight += VERTICAL_SPACING;
            }
            totalHeight += child.height;
            first = false;
        }

        this.width = Math.max(this.width, maxWidth);
        this.height = totalHeight;
    }

    /**
     * Position visible children vertically below the title.
     * Called automatically during render.
     */
    public void layout() {
        int currentY = this.y + TITLE_HEIGHT + TITLE_MARGIN;
        for (Wdgt child : children) {
            if (!child.visible) continue;
            child.x = this.x;
            child.y = currentY;
            currentY += child.height + VERTICAL_SPACING;
        }
    }

    @Override
    public void render(GuiGraphics gui, Font font, int mouseX, int mouseY) {
        if (!visible) return;
        layout();

        // Draw title
        gui.drawString(font, title, x, y, titleColor, true);

        // Render children
        for (Wdgt child : children) {
            if (child.visible) {
                child.render(gui, font, mouseX, mouseY);
            }
        }
    }

    @Override
    protected boolean handleClick(int mouseX, int mouseY) {
        // Early exit if click is outside section bounds
        if (!inside(mouseX, mouseY)) {
            return false;
        }

        // Forward click to children, stop on first handled
        // (child.click() handles visibility check via base class)
        for (Wdgt child : children) {
            if (child.click(mouseX, mouseY)) {
                return true;
            }
        }
        // Consume click even if no child handled it (prevent fall-through to widgets behind)
        return true;
    }

    @Override
    protected boolean handleScroll(int mouseX, int mouseY, double scrollDelta) {
        // Early exit if scroll is outside section bounds
        if (!inside(mouseX, mouseY)) return false;

        // Forward scroll to children until one handles it
        // (child.scroll() handles visibility check via base class)
        for (Wdgt child : children) {
            if (child.scroll(mouseX, mouseY, scrollDelta)) {
                return true;
            }
        }
        // Don't consume scroll if no child handled it (allow other widgets to scroll)
        return false;
    }

    public List<Wdgt> getChildren() {
        return children;
    }

    public void setTitle(Component title) {
        this.title = title;
    }

    @Override
    public boolean shouldShowTooltip(int mouseX, int mouseY) {
        if (!visible) return false;
        for (Wdgt child : children) {
            if (!child.visible) continue;
            if (child.shouldShowTooltip(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    /** Find the child widget the mouse is hovering over (for tooltip display) */
    public Wdgt getHoveredChild(int mouseX, int mouseY) {
        if (!visible) return null;
        for (Wdgt child : children) {
            if (!child.visible) continue;
            if (child.inside(mouseX, mouseY)) {
                return child;
            }
        }
        return null;
    }
}
