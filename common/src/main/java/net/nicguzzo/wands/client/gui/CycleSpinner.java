package net.nicguzzo.wands.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.wand.WandProps;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A combined toggle + numeric spinner widget.
 * When off: Shows "Label: Off" (no +/- buttons visible)
 * When on: Shows "Label: value" with +/- buttons like a normal Spinner
 *
 * Interactions:
 * - Left click when off: Enable (set to min value)
 * - Left click when on: Increment
 * - Scroll up when off: Enable
 * - Scroll up when on: Increment
 * - Scroll down when on at min: Disable
 * - Scroll down when on above min: Decrement
 */
public class CycleSpinner extends Wdgt {
    // Bound wand properties - uses suppliers to always get current wand
    private final Supplier<ItemStack> wandSupplier;
    private final Consumer<ItemStack> wandSyncer;
    private final WandProps.Flag flag;   // Boolean on/off state
    private final WandProps.Value val;   // Numeric value when enabled
    private final Component label;

    // Optional callback to run after value changes (e.g., to force redraw)
    private Runnable onChangeCallback = null;

    // Sub-widgets for +/- buttons (only shown when enabled)
    private Btn incrementButton;
    private Btn decrementButton;

    // Increment amounts (can be customized per instance)
    public int incrementValue = 1;
    public int shiftIncrementValue = 10;
    public int ctrlIncrementValue = 100;
    public int shiftCtrlIncrementValue = 1000;

    /**
     * Create a CycleSpinner with suppliers for the wand.
     * @param wandSupplier Supplier that returns the current wand ItemStack
     * @param wandSyncer Consumer that syncs the wand after changes (e.g., send to server)
     * @param flag The flag that controls the enabled/disabled state
     * @param val The value to adjust when enabled
     * @param width Widget width
     * @param height Widget height
     * @param label Display label
     */
    public CycleSpinner(Supplier<ItemStack> wandSupplier, Consumer<ItemStack> wandSyncer,
                        WandProps.Flag flag, WandProps.Value val, int width, int height, Component label) {
        this.wandSupplier = wandSupplier;
        this.wandSyncer = wandSyncer;
        this.flag = flag;
        this.val = val;
        this.label = label;
        this.width = width;
        this.height = height;

        // Create +/- buttons (positioned during render)
        incrementButton = new Btn(SPINNER_BUTTON_WIDTH, height / 2, Compat.literal("+"), (mouseX, mouseY) -> {
            if (isEnabled()) {
                increment();
            }
        });
        incrementButton.centerText = true;

        decrementButton = new Btn(SPINNER_BUTTON_WIDTH, height / 2, Compat.literal("-"), (mouseX, mouseY) -> {
            if (isEnabled()) {
                decrement();
            }
        });
        decrementButton.centerText = true;
    }

    public WandProps.Flag getFlag() {
        return flag;
    }

    public WandProps.Value getVal() {
        return val;
    }

    /** Set a callback to run after any value change (e.g., to force client redraw) */
    public CycleSpinner withOnChange(Runnable callback) {
        this.onChangeCallback = callback;
        return this;
    }

    private void notifyChange() {
        if (onChangeCallback != null) {
            onChangeCallback.run();
        }
    }

    /** Get the current wand from the supplier */
    private ItemStack getWand() {
        return wandSupplier.get();
    }

    /** Sync the wand after modifications */
    private void syncWand(ItemStack wand) {
        wandSyncer.accept(wand);
    }

    private boolean isEnabled() {
        return WandProps.getFlag(getWand(), flag);
    }

    private int getValue() {
        return WandProps.getVal(getWand(), val);
    }

    private void setEnabled(boolean enabled) {
        ItemStack wand = getWand();
        WandProps.setFlag(wand, flag, enabled);
        syncWand(wand);
    }

    private void setValue(int value) {
        ItemStack wand = getWand();
        WandProps.setVal(wand, val, value);
        syncWand(wand);
        notifyChange();
    }

    private int getIncrement() {
        if (Minecraft.getInstance().hasControlDown() && Minecraft.getInstance().hasShiftDown()) {
            return shiftCtrlIncrementValue;
        } else if (Minecraft.getInstance().hasControlDown()) {
            return ctrlIncrementValue;
        } else if (Minecraft.getInstance().hasShiftDown()) {
            return shiftIncrementValue;
        } else {
            return incrementValue;
        }
    }

    private void enable() {
        ItemStack wand = getWand();
        WandProps.setFlag(wand, flag, true);
        WandProps.setVal(wand, val, val.min);
        syncWand(wand);
        notifyChange();
    }

    private void disable() {
        ItemStack wand = getWand();
        WandProps.setFlag(wand, flag, false);
        syncWand(wand);
        notifyChange();
    }

    private void increment() {
        int iv = getIncrement();
        int current = getValue();
        int newVal = Math.min(current + iv, val.max);
        setValue(newVal);
        // notifyChange() called by setValue()
    }

    private void decrement() {
        int iv = getIncrement();
        int current = getValue();
        int newVal = current - iv;
        if (newVal < val.min) {
            // At or below min, turn off
            disable();
            // notifyChange() called by disable()
        } else {
            setValue(newVal);
            // notifyChange() called by setValue()
        }
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
            gui.drawString(font, labelText, textX, textY, labelColor, true);
            textX += font.width(labelText);
        }

        String value = isEnabled() ? String.valueOf(getValue()) : "Off";
        gui.drawString(font, value, textX, textY, valueColor, true);

        // Only show +/- buttons when enabled
        if (isEnabled()) {
            incrementButton.x = x + width - SPINNER_BUTTON_WIDTH;
            incrementButton.y = y;
            decrementButton.x = x + width - SPINNER_BUTTON_WIDTH;
            decrementButton.y = y + height / 2;
            incrementButton.render(gui, font, mouseX, mouseY);
            decrementButton.render(gui, font, mouseX, mouseY);
        }
    }

    @Override
    protected boolean handleClick(int mouseX, int mouseY) {
        if (!inside(mouseX, mouseY)) {
            return false;
        }

        if (!isEnabled()) {
            // Turn on when clicked while disabled
            enable();
            playClickSound();
        } else {
            // Check if clicking +/- buttons
            if (incrementButton.inside(mouseX, mouseY)) {
                incrementButton.click(mouseX, mouseY);
            } else if (decrementButton.inside(mouseX, mouseY)) {
                decrementButton.click(mouseX, mouseY);
            } else {
                // Click on body area = increment
                increment();
                playClickSound();
            }
        }
        return true;
    }

    @Override
    protected boolean handleScroll(int mouseX, int mouseY, double scrollDelta) {
        if (!inside(mouseX, mouseY)) return false;

        if (scrollDelta > 0) {
            // Scroll up: enable or increment
            if (!isEnabled()) {
                enable();
            } else {
                increment();
            }
            return true;
        } else if (scrollDelta < 0) {
            // Scroll down: decrement (disables at min)
            if (isEnabled()) {
                decrement();
            }
            // When off, scroll down does nothing
            return true;
        }

        return false;
    }
}
