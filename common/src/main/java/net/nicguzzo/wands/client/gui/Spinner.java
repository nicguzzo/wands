package net.nicguzzo.wands.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.nicguzzo.wands.utils.Compat;

import java.util.function.Consumer;

/**
 * A numeric spinner widget with +/- buttons and scroll wheel support.
 * Displays "Label: value" with increment/decrement buttons on the right.
 *
 * Interactions:
 * - Click +/- buttons to change value
 * - Click body area to increment
 * - Scroll wheel to increment/decrement
 * - Hold Shift for 10x, Ctrl for 100x, Shift+Ctrl for 1000x increment
 */
public class Spinner extends Wdgt {
    public static final int DEFAULT_WIDTH = 80;

    // Value and range
    protected int value;
    protected int min;
    protected int max;

    // Increment amounts (can be customized per instance)
    public int incrementValue = 1;           // Normal click/scroll
    public int shiftIncrementValue = 10;     // Shift held
    public int ctrlIncrementValue = 100;     // Ctrl held
    public int shiftCtrlIncrementValue = 1000; // Shift+Ctrl held

    // Display
    protected Component label;

    // Callback for value changes
    private Consumer<Integer> onChangeCallback;

    // Sub-widgets for +/- buttons
    private Btn incrementButton;
    private Btn decrementButton;

    /** Create spinner with default dimensions */
    public Spinner(int value, int min, int max, Component label) {
        this(value, min, max, DEFAULT_WIDTH, DEFAULT_HEIGHT, label);
    }

    /** Create spinner with custom dimensions (position set by layout) */
    public Spinner(int value, int min, int max, int width, int height, Component label) {
        this(value, min, max, 0, 0, width, height, label);
    }

    /** Create spinner with explicit position and dimensions */
    public Spinner(int value, int min, int max, int x, int y, int width, int height, Component label) {
        this.value = value;
        this.min = min;
        this.max = max;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label;

        // Create +/- buttons (positioned during render)
        // showBackground = false so they only highlight on hover (like CycleToggle)
        incrementButton = new Btn(SPINNER_BUTTON_WIDTH, height / 2, Compat.literal("+"), (mouseX, mouseY) -> {
            increment();
            notifyChange();
        });
        incrementButton.centerText = true;
        incrementButton.showBackground = false;
        decrementButton = new Btn(SPINNER_BUTTON_WIDTH, height / 2, Compat.literal("-"), (mouseX, mouseY) -> {
            decrement();
            notifyChange();
        });
        decrementButton.centerText = true;
        decrementButton.showBackground = false;
    }

    /**
     * Set a callback to run after any value change.
     * @param callback Consumer that receives the new value after change
     * @return this for chaining
     */
    public Spinner withOnChange(Consumer<Integer> callback) {
        this.onChangeCallback = callback;
        return this;
    }

    @Override
    public Spinner withTooltip(Component title, Component description) {
        super.withTooltip(title, description);
        return this;
    }

    /** Notify the onChange callback if set */
    private void notifyChange() {
        if (onChangeCallback != null) {
            onChangeCallback.accept(value);
        }
    }

    /**
     * Get the increment amount based on modifier keys held.
     */
    private int getIncrement() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hasControlDown() && mc.hasShiftDown()) {
            return shiftCtrlIncrementValue;
        } else if (mc.hasControlDown()) {
            return ctrlIncrementValue;
        } else if (mc.hasShiftDown()) {
            return shiftIncrementValue;
        } else {
            return incrementValue;
        }
    }

    /** Increment value by current increment amount, clamped to max */
    private void increment() {
        int inc = getIncrement();
        value = Math.min(value + inc, max);
    }

    /** Decrement value by current increment amount, clamped to min */
    private void decrement() {
        int inc = getIncrement();
        value = Math.max(value - inc, min);
    }

    @Override
    public void render(GuiGraphics gui, Font font, int mouseX, int mouseY) {
        if (!visible) return;
        drawBackground(gui, mouseX, mouseY);

        // Draw label and value in different colors, vertically centered
        int textY = getTextY(font);
        int textX = x + TEXT_PADDING;
        if (label != null) {
            String labelText = label.getString() + " ";
            gui.drawString(font, labelText, textX, textY, labelColor, drawShadow);
            textX += font.width(labelText);
        }
        gui.drawString(font, String.valueOf(value), textX, textY, valueColor, drawShadow);

        // Position and render +/- buttons at right edge
        // [+] on top, [-] on bottom
        incrementButton.x = x + width - SPINNER_BUTTON_WIDTH;
        incrementButton.y = y;
        decrementButton.x = x + width - SPINNER_BUTTON_WIDTH;
        decrementButton.y = y + height / 2;
        incrementButton.render(gui, font, mouseX, mouseY);
        decrementButton.render(gui, font, mouseX, mouseY);
    }

    @Override
    protected boolean handleClick(int mouseX, int mouseY) {
        if (!inside(mouseX, mouseY)) return false;

        // Check if clicking +/- buttons
        if (incrementButton.inside(mouseX, mouseY)) {
            incrementButton.click(mouseX, mouseY);
            return true;
        }
        if (decrementButton.inside(mouseX, mouseY)) {
            decrementButton.click(mouseX, mouseY);
            return true;
        }

        // Click on body area = increment
        increment();
        notifyChange();
        playClickSound();
        return true;
    }

    @Override
    protected boolean handleScroll(int mouseX, int mouseY, double scrollDelta) {
        if (!inside(mouseX, mouseY)) return false;

        if (scrollDelta > 0) {
            increment();
        } else if (scrollDelta < 0) {
            decrement();
        }
        notifyChange();
        return true;
    }
}