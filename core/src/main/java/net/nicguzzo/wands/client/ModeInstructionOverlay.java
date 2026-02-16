package net.nicguzzo.wands.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandProps;

/**
 * HUD overlay that displays instructional text for two-click wand modes.
 * Guides players through the point selection process for Circle, Sphere, Fill, Copy, and Line modes.
 */
public class ModeInstructionOverlay {

    // White with 60% opacity — also used by the wand HUD overlay
    public static final int TEXT_COLOR = 0x99FFFFFF;
    public static final int TEXT_COLOR_DIM = 0x99AAAAAA;

    /**
     * Main render method called from HUD event.
     */
    public static void render(GuiGraphics gui) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) {
            return;
        }

        // Check if player is holding a wand
        ItemStack stack = client.player.getMainHandItem();
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof WandItem)) {
            return;
        }

        // Get the current mode
        WandProps.Mode mode = WandProps.getMode(stack);

        // Show instructions overlay for two-click modes, or when pin is actively set
        boolean pinActive = ClientRender.wand != null && ClientRender.wand.pin.isSet() && ClientRender.wand.pin.isPersistent();
        if (mode.n_clicks() != 2 && !pinActive) {
            return;
        }

        // Get point states from the client-side wand
        boolean hasFirstPoint = ClientRender.wand != null && ClientRender.wand.getP1() != null;
        boolean hasSecondPoint = ClientRender.wand != null && ClientRender.wand.getP2() != null;

        // Get the instruction text
        Component instruction = getInstructionText(mode, hasFirstPoint, hasSecondPoint);
        if (instruction == null) {
            return;
        }

        // Calculate position: horizontally centered, 20px below vertical center
        Font font = client.font;
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int textWidth = font.width(instruction);
        int x = (screenWidth - textWidth) / 2;
        int y = (screenHeight / 2) + 40;

        // Draw the text with shadow for visibility
        gui.drawString(font, instruction, x, y, TEXT_COLOR, true);
    }

    /**
     * Returns the appropriate instruction Component based on mode and point state.
     * @param mode The current wand mode
     * @param hasFirstPoint Whether P1 has been set
     * @param hasSecondPoint Whether P2 has been set
     * @return The instruction Component, or null if no instruction should be shown
     */
    private static Component getInstructionText(WandProps.Mode mode, boolean hasFirstPoint, boolean hasSecondPoint) {
        String keyName = getUseKeyName();

        switch (mode) {
            case CIRCLE:
                if (!hasFirstPoint) {
                    return Component.translatable("wands.instruction.circle.set_center", keyName);
                } else if (!hasSecondPoint) {
                    return Component.translatable("wands.instruction.circle.set_edge", keyName);
                }
                break;

            case SPHERE:
                if (!hasFirstPoint) {
                    return Component.translatable("wands.instruction.sphere.set_center", keyName);
                } else if (!hasSecondPoint) {
                    return Component.translatable("wands.instruction.sphere.set_edge", keyName);
                }
                break;

            case FILL:
                if (!hasFirstPoint) {
                    return Component.translatable("wands.instruction.fill.set_first_corner", keyName);
                } else if (!hasSecondPoint) {
                    return Component.translatable("wands.instruction.fill.set_opposite_corner", keyName);
                }
                break;

            case COPY:
                if (!hasFirstPoint) {
                    return Component.translatable("wands.instruction.copy.set_first_corner", keyName);
                } else if (!hasSecondPoint) {
                    return Component.translatable("wands.instruction.copy.set_opposite_corner", keyName);
                } else {
                    // Copy mode shows "expand selection" after both points are set
                    return Component.translatable("wands.instruction.copy.expand_selection", keyName);
                }

            case LINE:
                if (!hasFirstPoint) {
                    return Component.translatable("wands.instruction.line.set_start", keyName);
                } else if (!hasSecondPoint) {
                    return Component.translatable("wands.instruction.line.set_end", keyName);
                }
                break;

            default:
                break;
        }

        // Pin movement instructions (only when pin is actively set)
        Wand wand = ClientRender.wand;
        if (wand != null && wand.pin.isSet() && wand.pin.isPersistent()) {
            return Component.translatable("wands.instruction.pin.move");
        }

        return null;
    }

    /**
     * Gets the display name of the "use item" keybind.
     * @return The localized key name (e.g., "Right Button")
     */
    private static String getUseKeyName() {
        Minecraft client = Minecraft.getInstance();
        String name = client.options.keyUse.getTranslatedKeyMessage().getString();
        if (name.equals("Right Button")) {
            return "Right mouse button";
        }
        return name;
    }
}
