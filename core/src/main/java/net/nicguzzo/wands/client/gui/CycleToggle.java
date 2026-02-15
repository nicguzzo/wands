package net.nicguzzo.wands.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.nicguzzo.compat.Compat;

import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A widget that cycles through multiple options, displaying "Label: CurrentValue".
 *
 * Interactions:
 * - Click to cycle to next option
 * - Scroll up to cycle forward, scroll down to cycle backward
 *
 * Features:
 * - Optional filter to hide certain options dynamically
 * - Per-option tooltips
 * - Auto-validation to skip to valid option if current becomes unavailable
 *
 * Usage:
 *   new CycleToggle<>(label, options, optionLabels, () -> getState(), value -> setState(value))
 *
 * The getter (Supplier) reads the current selected value.
 * The setter (Consumer) is called when user selects a new value.
 */
public class CycleToggle<T> extends Wdgt {
    private final Component label;              // Display label (e.g., "Mode")
    private final T[] options;                  // All possible values
    private final Component[] optionLabels;     // Display text for each option
    private Component[] alternateLabels;        // Optional: simplified labels when few options available
    private boolean useAlternateLabels;         // Whether to use alternate labels
    private final Supplier<T> getter;           // Returns current selected value
    private final Consumer<T> setter;           // Called when selection changes
    private Predicate<T> filter;                // Optional: returns true if option is available
    private String[] tooltipKeys;               // Optional: tooltip translation keys per option
    private IntFunction<Component> tooltipProvider; // Optional: dynamic tooltip generator (index -> tooltip)

    /**
     * Create a cycle toggle widget.
     * @param label Text label shown before the current value
     * @param options Array of all possible values
     * @param optionLabels Display text for each option (must match options length)
     * @param getter Supplier that returns the current selected value
     * @param setter Consumer called when user selects a new value
     */
    public CycleToggle(Component label, T[] options, Component[] optionLabels,
                     Supplier<T> getter, Consumer<T> setter) {
        this.label = label;
        this.options = options;
        this.optionLabels = optionLabels;
        this.getter = getter;
        this.setter = setter;
        this.height = DEFAULT_HEIGHT;
    }

    /**
     * Create a boolean toggle with default "On"/"Off" labels.
     */
    public static CycleToggle<Boolean> ofBoolean(Component label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return ofBoolean(label, getter, setter, "On", "Off");
    }

    /**
     * Create a boolean toggle with custom labels.
     */
    public static CycleToggle<Boolean> ofBoolean(Component label, Supplier<Boolean> getter, Consumer<Boolean> setter,
                                                  String trueLabel, String falseLabel) {
        return new CycleToggle<>(label,
            new Boolean[]{true, false},
            new Component[]{Compat.literal(trueLabel), Compat.literal(falseLabel)},
            getter, setter);
    }

    /**
     * Set a filter to dynamically hide certain options.
     * Options where filter returns false will be skipped when cycling.
     * @param filter Predicate that returns true if option should be available
     * @return this for chaining
     */
    public CycleToggle<T> withFilter(Predicate<T> filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Set tooltip translation keys for each option.
     * Tooltip updates automatically when selection changes.
     * @param keys Translation keys (one per option)
     * @return this for chaining
     */
    public CycleToggle<T> withTooltips(String... keys) {
        this.tooltipKeys = keys;
        return this;
    }

    /**
     * Set a dynamic tooltip provider that generates tooltips based on current index.
     * When set, this overrides the static tooltipKeys.
     * @param provider Function that takes option index and returns tooltip Component
     */
    public void setTooltipProvider(IntFunction<Component> provider) {
        this.tooltipProvider = provider;
    }

    /**
     * Set alternate (simplified) labels for options.
     * Use setUseAlternateLabels() to toggle between regular and alternate labels.
     * @param labels Alternate labels (must match options length)
     * @return this for chaining
     */
    public CycleToggle<T> withAlternateLabels(Component[] labels) {
        this.alternateLabels = labels;
        return this;
    }

    /**
     * Toggle between regular and alternate labels.
     * @param use true to use alternate labels, false for regular labels
     */
    public void setUseAlternateLabels(boolean use) {
        this.useAlternateLabels = use && alternateLabels != null;
    }

    @Override
    public CycleToggle<T> withTooltip(Component title, Component description) {
        super.withTooltip(title, description);
        return this;
    }

    /** Check if option at index passes the filter (or no filter set) */
    private boolean isAvailable(int index) {
        return filter == null || filter.test(options[index]);
    }

    /** Find the index of the current value in options array */
    private int getCurrentIndex() {
        T current = getter.get();
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(current)) {
                return i;
            }
        }
        return 0;
    }

    /** Find next available option index (wrapping around) */
    private int getNextAvailableIndex(int fromIndex) {
        for (int i = 1; i <= options.length; i++) {
            int index = (fromIndex + i) % options.length;
            if (isAvailable(index)) {
                return index;
            }
        }
        return fromIndex; // No other option available
    }

    /** Find previous available option index (wrapping around) */
    private int getPrevAvailableIndex(int fromIndex) {
        for (int i = 1; i <= options.length; i++) {
            int index = (fromIndex - i + options.length) % options.length;
            if (isAvailable(index)) {
                return index;
            }
        }
        return fromIndex; // No other option available
    }

    /** @return true if at least one option passes the filter */
    public boolean hasAvailableOptions() {
        for (int i = 0; i < options.length; i++) {
            if (isAvailable(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * If current selection is filtered out, auto-select next available option.
     * @return true if selection was changed
     */
    public boolean validateSelection() {
        int currentIndex = getCurrentIndex();
        if (!isAvailable(currentIndex)) {
            int nextIndex = getNextAvailableIndex(currentIndex);
            if (nextIndex != currentIndex) {
                setter.accept(options[nextIndex]);
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean handleClick(int mouseX, int mouseY) {
        if (!inside(mouseX, mouseY)) return false;

        int currentIndex = getCurrentIndex();
        int nextIndex = getNextAvailableIndex(currentIndex);

        setter.accept(options[nextIndex]);
        playClickSound();
        return true;
    }

    @Override
    protected boolean handleScroll(int mouseX, int mouseY, double scrollDelta) {
        if (!inside(mouseX, mouseY)) return false;

        int currentIndex = getCurrentIndex();
        int nextIndex;
        if (scrollDelta > 0) {
            nextIndex = getNextAvailableIndex(currentIndex);
        } else {
            nextIndex = getPrevAvailableIndex(currentIndex);
        }

        if (nextIndex != currentIndex) {
            setter.accept(options[nextIndex]);
        }
        return true;
    }

    @Override
    public void render(GuiGraphics gui, Font font, int mouseX, int mouseY) {
        if (!visible) return;
        drawBackground(gui, mouseX, mouseY);

        int index = getCurrentIndex();
        int textY = getTextY(font);
        int textX = x + TEXT_PADDING;

        if (label != null) {
            String labelText = label.getString() + " ";
            gui.drawString(font, labelText, textX, textY, labelColor, drawShadow);
            textX += font.width(labelText);
        }

        Component[] labels = useAlternateLabels ? alternateLabels : optionLabels;
        Component valueText = labels[index];
        gui.drawString(font, valueText.getString(), textX, textY, valueColor, drawShadow);

        // Update tooltip to match current option
        // Use widget label as title, or current option label if no widget label
        Component titleToUse = (label != null) ? label : labels[index];
        if (tooltipProvider != null) {
            this.tooltipTitle = titleToUse;
            this.tooltip = tooltipProvider.apply(index);
        } else if (tooltipKeys != null && index < tooltipKeys.length) {
            this.tooltipTitle = titleToUse;
            this.tooltip = Component.translatable(tooltipKeys[index]);
        }
    }
}
