package net.nicguzzo.wands.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A container widget that arranges buttons in a vertical tab list.
 * Used for mode selection as vertical tabs on the left side.
 *
 * Features:
 * - Vertical layout with configurable spacing
 * - Selection tracking (set selected = index to highlight a tab)
 * - Sub-tabs support with inline expansion (sub-tabs appear below parent, pushing others down)
 * - Animated expansion/collapse (200ms linear)
 * - Tooltip support for child buttons
 */
public class Tabs extends Wdgt {
    public static final int DEFAULT_SPACING = 4;

    // Tab sizes (same for parent and subtabs)
    public static final int TAB_SIZE = 14;          // Tab button size
    public static final int TAB_ICON_SIZE = 12;     // Icon size inside tab

    // Expanded group background (same as toggle/spinner background)
    public static final int EXPANDED_BG_COLOR = Btn.COLOR_NORMAL;
    public static final int EXPANDED_BG_PADDING = 2;  // Padding around expanded group

    /** Animation duration in seconds */
    public static final float ANIMATION_DURATION = 0.1f;  // 100ms
    /** Animation speed (1.0 / duration) */
    public static final float ANIMATION_SPEED = 1.0f / ANIMATION_DURATION;

    private final List<TabEntry> entries = new ArrayList<>();
    private int spacing;

    /** Index of selected tab, or -1 for no selection */
    public int selected = -1;

    /** Last frame time for animation delta calculation */
    private long lastFrameTime = 0;

    /**
     * Inner class to hold a tab button with optional sub-tabs.
     */
    public static class TabEntry {
        public Btn button;
        public List<Btn> subTabs = null;  // null = no sub-tabs
        public int selectedSubTab = 0;    // Which sub-tab is selected (default first)

        // Animation state
        public boolean expanded = false;      // Target state
        public float expandProgress = 0.0f;   // 0.0 = collapsed, 1.0 = fully expanded

        public TabEntry(Btn button) {
            this.button = button;
        }

        public boolean hasSubTabs() {
            return subTabs != null && !subTabs.isEmpty();
        }

        /** Get the height contribution of sub-tabs based on animation progress */
        public int getSubTabsHeight(int spacing) {
            if (!hasSubTabs() || expandProgress <= 0) return 0;
            int fullHeight = 0;
            for (Btn subTab : subTabs) {
                fullHeight += subTab.height + spacing;
            }
            return (int)(fullHeight * expandProgress);
        }
    }

    public Tabs() {
        this(DEFAULT_SPACING);
    }

    public Tabs(int spacing) {
        this.spacing = spacing;
    }

    /** Add a regular tab (no sub-tabs) */
    public Btn add(Btn btn) {
        entries.add(new TabEntry(btn));
        recalculateBounds();
        return btn;
    }

    /**
     * Add a parent tab with sub-tabs.
     * @param parentBtn The parent button (clicking expands/collapses sub-tabs)
     * @param subTabBtns The sub-tab buttons
     * @return The index of this entry in the entries list
     */
    public int addWithSubTabs(Btn parentBtn, Btn... subTabBtns) {
        TabEntry entry = new TabEntry(parentBtn);
        entry.subTabs = new ArrayList<>(Arrays.asList(subTabBtns));
        entries.add(entry);
        recalculateBounds();
        return entries.size() - 1;
    }

    /** Get the TabEntry at the given index */
    public TabEntry getEntry(int index) {
        if (index >= 0 && index < entries.size()) {
            return entries.get(index);
        }
        return null;
    }

    /**
     * Update animation state. Call this every frame.
     * Automatically calculates delta time since last call.
     * @param ignored Unused parameter (kept for API compatibility)
     * @return true if any animation is still in progress
     */
    public boolean updateAnimation(float ignored) {
        // Calculate actual delta time
        long currentTime = System.nanoTime();
        float deltaSeconds;
        if (lastFrameTime == 0) {
            deltaSeconds = 0.016f; // Assume ~60fps for first frame
        } else {
            deltaSeconds = (currentTime - lastFrameTime) / 1_000_000_000.0f;
            // Clamp to reasonable range (prevent huge jumps if paused)
            deltaSeconds = Math.min(deltaSeconds, 0.1f);
        }
        lastFrameTime = currentTime;

        boolean animating = false;
        float delta = deltaSeconds * ANIMATION_SPEED;

        for (TabEntry entry : entries) {
            if (!entry.hasSubTabs()) continue;

            if (entry.expanded) {
                // Expanding
                if (entry.expandProgress < 1.0f) {
                    entry.expandProgress = Math.min(1.0f, entry.expandProgress + delta);
                    animating = true;
                }
            } else {
                // Collapsing
                if (entry.expandProgress > 0.0f) {
                    entry.expandProgress = Math.max(0.0f, entry.expandProgress - delta);
                    animating = true;
                }
            }
        }
        return animating;
    }

    /** Recalculate total width/height based on entries and expansion state */
    public void recalculateBounds() {
        if (entries.isEmpty()) {
            this.width = 0;
            this.height = 0;
            return;
        }

        int maxWidth = 0;
        int totalHeight = 0;
        for (int i = 0; i < entries.size(); i++) {
            TabEntry entry = entries.get(i);
            maxWidth = Math.max(maxWidth, entry.button.width);
            if (i > 0) {
                totalHeight += spacing;
            }
            totalHeight += entry.button.height;

            // Add expanded sub-tabs height
            if (entry.hasSubTabs()) {
                totalHeight += entry.getSubTabsHeight(spacing);
                // Check sub-tab widths too
                for (Btn subTab : entry.subTabs) {
                    maxWidth = Math.max(maxWidth, subTab.width);
                }
            }
        }

        this.width = maxWidth;
        this.height = totalHeight;
    }

    /** Position buttons in vertical layout with inline sub-tabs */
    private void layout() {
        int currentY = this.y;
        for (int i = 0; i < entries.size(); i++) {
            TabEntry entry = entries.get(i);

            // Position parent button
            entry.button.x = this.x;
            entry.button.y = currentY;
            currentY += entry.button.height + spacing;

            // Position sub-tabs inline (below parent) if expanded
            if (entry.hasSubTabs() && entry.expandProgress > 0) {
                for (Btn subTab : entry.subTabs) {
                    subTab.x = this.x;
                    subTab.y = currentY;
                    currentY += (int)((subTab.height + spacing) * entry.expandProgress);
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics gui, Font font, int mouseX, int mouseY) {
        if (!visible) return;
        recalculateBounds();
        layout();

        // Render all tabs and expanded sub-tabs
        for (int i = 0; i < entries.size(); i++) {
            TabEntry entry = entries.get(i);
            Btn btn = entry.button;

            // Update button selection state
            // Parent tab only shows selected when collapsed, not when expanded (subtab shows selected instead)
            btn.selected = (i == selected) && !(entry.hasSubTabs() && entry.expanded);
            btn.showBackground = false;

            // Draw background behind parent AND subtabs when expanded
            if (entry.hasSubTabs() && entry.expandProgress > 0) {
                Btn lastSubTab = entry.subTabs.get(entry.subTabs.size() - 1);
                int bgX = btn.x - EXPANDED_BG_PADDING;
                int bgY = btn.y - EXPANDED_BG_PADDING;
                int bgRight = btn.x + btn.width + EXPANDED_BG_PADDING;
                int bgBottom = lastSubTab.y + (int)(lastSubTab.height * entry.expandProgress) + EXPANDED_BG_PADDING;
                gui.fill(bgX, bgY, bgRight, bgBottom, EXPANDED_BG_COLOR);
            }

            btn.render(gui, font, mouseX, mouseY);

            // Render sub-tabs if expanded
            if (entry.hasSubTabs() && entry.expandProgress > 0) {
                for (int j = 0; j < entry.subTabs.size(); j++) {
                    Btn subTab = entry.subTabs.get(j);
                    subTab.selected = (j == entry.selectedSubTab);
                    subTab.showBackground = false;

                    // Only render if visible (Y position is within the animated area)
                    int subTabRelativeIndex = j + 1;
                    float visibilityThreshold = (float)subTabRelativeIndex / entry.subTabs.size();
                    if (entry.expandProgress >= visibilityThreshold * 0.5f) {
                        subTab.render(gui, font, mouseX, mouseY);
                    }
                }
            }
        }
    }

    @Override
    protected boolean handleClick(int mouseX, int mouseY) {
        // Early exit if click is outside tabs area
        if (!inside(mouseX, mouseY)) {
            return false;
        }

        // Check sub-tabs first (they're rendered on top when expanded)
        for (int i = 0; i < entries.size(); i++) {
            TabEntry entry = entries.get(i);
            if (entry.hasSubTabs() && entry.expandProgress > 0.5f) {
                for (int j = 0; j < entry.subTabs.size(); j++) {
                    Btn subTab = entry.subTabs.get(j);
                    if (subTab.inside(mouseX, mouseY)) {
                        entry.selectedSubTab = j;
                        subTab.click(mouseX, mouseY);
                        return true;
                    }
                }
            }
        }

        // Then check parent tabs
        for (int i = 0; i < entries.size(); i++) {
            TabEntry entry = entries.get(i);
            if (entry.button.inside(mouseX, mouseY)) {
                // For tabs with sub-tabs: toggle expanded state
                if (entry.hasSubTabs()) {
                    boolean wasExpanded = entry.expanded;
                    // Collapse all other tabs first
                    for (TabEntry other : entries) {
                        if (other != entry && other.hasSubTabs()) {
                            other.expanded = false;
                        }
                    }
                    // Toggle this tab
                    entry.expanded = !wasExpanded;
                }
                entry.button.click(mouseX, mouseY);
                return true;
            }
        }
        return false;
    }

    /** Get all currently visible buttons (including expanded sub-tabs) */
    public List<Btn> getAllVisibleButtons() {
        List<Btn> buttons = new ArrayList<>();
        for (TabEntry entry : entries) {
            buttons.add(entry.button);
            // Include sub-tabs if expanded enough to be interactive
            if (entry.hasSubTabs() && entry.expandProgress > 0.5f) {
                buttons.addAll(entry.subTabs);
            }
        }
        return buttons;
    }

    /** Returns number of top-level entries (not including sub-tabs) */
    public int size() {
        return entries.size();
    }

    @Override
    public boolean shouldShowTooltip(int mouseX, int mouseY) {
        if (!visible) return false;
        for (Btn btn : getAllVisibleButtons()) {
            if (btn.shouldShowTooltip(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    /** Find the button the mouse is hovering over (for tooltip display) */
    public Btn getHoveredButton(int mouseX, int mouseY) {
        if (!visible) return null;
        for (Btn btn : getAllVisibleButtons()) {
            if (btn.inside(mouseX, mouseY) && (btn.tooltip != null || btn.tooltipTitle != null)) {
                return btn;
            }
        }
        return null;
    }
}
