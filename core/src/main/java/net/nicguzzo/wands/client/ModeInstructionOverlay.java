package net.nicguzzo.wands.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.compat.Compat;
import net.nicguzzo.compat.MyIdExt;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.config.WandsConfig.HudMode;
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

    private static final MyIdExt RIGHT_CLICK_TEX = new MyIdExt(WandsMod.MOD_ID, "textures/gui/right-click.png");
    private static final int ICON_TEX_SIZE = 16;
    private static final int ICON_DRAW_SIZE = 8;

    /**
     * Main render method called from HUD event.
     */
    public static void render(GuiGraphics gui) {
        if (WandsMod.config.hud_mode == HudMode.OFF) return;
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

        // Get the instruction text and whether to show the right-click icon
        Instruction instruction = getInstruction(mode, hasFirstPoint, hasSecondPoint);
        if (instruction == null) {
            return;
        }

        Font font = client.font;
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int textWidth = font.width(instruction.text);
        int totalWidth = textWidth;
        if (instruction.showIcon) {
            totalWidth += 1 + ICON_DRAW_SIZE; // 1px gap + icon
        }

        int x = (screenWidth - totalWidth) / 2;
        int y = (screenHeight / 2) + 40;

        // Draw the text with shadow for visibility
        gui.drawString(font, instruction.text, x, y, TEXT_COLOR, true);

        // Draw the right-click icon after the text at half size
        if (instruction.showIcon) {
            int iconX = x + textWidth + 1;
            int iconY = y + (font.lineHeight - ICON_DRAW_SIZE) / 2;
            #if MC_VERSION >= 12111
            gui.pose().pushMatrix();
            gui.pose().translate(iconX, iconY);
            gui.pose().scale(0.5f, 0.5f);
            #else
            gui.pose().pushPose();
            gui.pose().translate(iconX, iconY, 0);
            gui.pose().scale(0.5f, 0.5f, 1.0f);
            #endif
            Compat.set_color(1.0f, 1.0f, 1.0f, 0.6f);
            Compat.blit(gui, RIGHT_CLICK_TEX, 0, 0, 0, 0, ICON_TEX_SIZE, ICON_TEX_SIZE, ICON_TEX_SIZE, ICON_TEX_SIZE);
            Compat.set_color(1.0f, 1.0f, 1.0f, 1.0f);
            #if MC_VERSION >= 12111
            gui.pose().popMatrix();
            #else
            gui.pose().popPose();
            #endif
        }
    }

    private static class Instruction {
        final Component text;
        final boolean showIcon;
        Instruction(Component text, boolean showIcon) {
            this.text = text;
            this.showIcon = showIcon;
        }
    }

    /**
     * Returns the appropriate instruction based on mode and point state.
     */
    private static Instruction getInstruction(WandProps.Mode mode, boolean hasFirstPoint, boolean hasSecondPoint) {
        String key = null;

        switch (mode) {
            case CIRCLE:
                if (!hasFirstPoint) {
                    key = "wands.instruction.circle.set_center";
                } else if (!hasSecondPoint) {
                    key = "wands.instruction.circle.set_edge";
                }
                break;

            case SPHERE:
                if (!hasFirstPoint) {
                    key = "wands.instruction.sphere.set_center";
                } else if (!hasSecondPoint) {
                    key = "wands.instruction.sphere.set_edge";
                }
                break;

            case FILL:
                if (!hasFirstPoint) {
                    key = "wands.instruction.fill.set_first_corner";
                } else if (!hasSecondPoint) {
                    key = "wands.instruction.fill.set_opposite_corner";
                }
                break;

            case COPY:
                if (!hasFirstPoint) {
                    key = "wands.instruction.copy.set_first_corner";
                } else if (!hasSecondPoint) {
                    key = "wands.instruction.copy.set_opposite_corner";
                } else {
                    key = "wands.instruction.copy.expand_selection";
                }
                break;

            case LINE:
                if (!hasFirstPoint) {
                    key = "wands.instruction.line.set_start";
                } else if (!hasSecondPoint) {
                    key = "wands.instruction.line.set_end";
                }
                break;

            default:
                break;
        }

        if (key != null) {
            return new Instruction(Component.translatable(key), true);
        }

        // Pin movement instructions (only when pin is actively set)
        Wand wand = ClientRender.wand;
        if (wand != null && wand.pin.isSet() && wand.pin.isPersistent()) {
            MutableComponent text = Component.translatable("wands.instruction.pin.move")
                    .append(Component.literal(" [←→↑↓, Shift+↑↓]").withStyle(s -> s.withColor(TEXT_COLOR_DIM)));
            return new Instruction(text, false);
        }

        return null;
    }
}
