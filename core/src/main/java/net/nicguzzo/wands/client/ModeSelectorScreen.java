package net.nicguzzo.wands.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.compat.Compat;
import net.nicguzzo.compat.MyIdExt;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.screens.WandScreen;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.networking.Networking;
import net.nicguzzo.wands.wand.WandProps;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mode selector grid screen.
 * Hold MODE key to open, release to commit selection.
 * Tap MODE key to cycle to next mode.
 */
public class ModeSelectorScreen extends Screen {

    // --- Constants ---
    private static final int HOLD_THRESHOLD = 4;
    private static final int GRID_COLS = 4;
    private static final int GRID_ROWS = 4;
    private static final int TILE_SIZE = 38;
    private static final int TILE_GAP = 2;
    private static final int GRID_PADDING = 12;
    private static final int ICON_SIZE = 16;

    private static final int COLOR_SCRIM = 0x80000000;
    private static final int COLOR_TILE_NORMAL = 0xE6333333;
    private static final int COLOR_TILE_HIGHLIGHT = WandScreen.COLOR_BTN_HOVER;
    private static final int COLOR_TILE_DISABLED = 0x80333333;
    private static final int COLOR_CURRENT_BORDER = 0xFF44AA44;
    private static final int COLOR_HINT_TEXT = 0xFF888888;

    private static final WandProps.Mode[] GRID_SLOTS = {
        WandProps.Mode.DIRECTION, WandProps.Mode.ROW_COL, WandProps.Mode.FILL,   WandProps.Mode.AREA,
        WandProps.Mode.GRID,      WandProps.Mode.LINE,    WandProps.Mode.CIRCLE,  WandProps.Mode.BOX,
        WandProps.Mode.SPHERE,    WandProps.Mode.ROCK,    WandProps.Mode.VEIN,    WandProps.Mode.BLAST,
        WandProps.Mode.COPY,      WandProps.Mode.PASTE,   null,                   null
    };

    private static final Map<WandProps.Mode, MyIdExt> ICON_TEXTURES = new HashMap<>();

    static {
        ICON_TEXTURES.put(WandProps.Mode.DIRECTION, new MyIdExt(WandsMod.MOD_ID, "textures/gui/direction.png"));
        ICON_TEXTURES.put(WandProps.Mode.ROW_COL,   new MyIdExt(WandsMod.MOD_ID, "textures/gui/row.png"));
        ICON_TEXTURES.put(WandProps.Mode.FILL,      new MyIdExt(WandsMod.MOD_ID, "textures/gui/fill.png"));
        ICON_TEXTURES.put(WandProps.Mode.AREA,      new MyIdExt(WandsMod.MOD_ID, "textures/gui/area.png"));
        ICON_TEXTURES.put(WandProps.Mode.GRID,      new MyIdExt(WandsMod.MOD_ID, "textures/gui/grid.png"));
        ICON_TEXTURES.put(WandProps.Mode.LINE,      new MyIdExt(WandsMod.MOD_ID, "textures/gui/line.png"));
        ICON_TEXTURES.put(WandProps.Mode.CIRCLE,    new MyIdExt(WandsMod.MOD_ID, "textures/gui/circle.png"));
        ICON_TEXTURES.put(WandProps.Mode.BOX,       new MyIdExt(WandsMod.MOD_ID, "textures/gui/box.png"));
        ICON_TEXTURES.put(WandProps.Mode.SPHERE,    new MyIdExt(WandsMod.MOD_ID, "textures/gui/sphere.png"));
        ICON_TEXTURES.put(WandProps.Mode.ROCK,      new MyIdExt(WandsMod.MOD_ID, "textures/gui/rock.png"));
        ICON_TEXTURES.put(WandProps.Mode.VEIN,      new MyIdExt(WandsMod.MOD_ID, "textures/gui/vein.png"));
        ICON_TEXTURES.put(WandProps.Mode.BLAST,     new MyIdExt(WandsMod.MOD_ID, "textures/gui/blast.png"));
        ICON_TEXTURES.put(WandProps.Mode.COPY,      new MyIdExt(WandsMod.MOD_ID, "textures/gui/copy.png"));
        ICON_TEXTURES.put(WandProps.Mode.PASTE,     new MyIdExt(WandsMod.MOD_ID, "textures/gui/paste.png"));
    }

    // --- Static state (persists across screen instances) ---
    private static long pointingHandCursor = 0;
    private static int holdStartTick = -1;
    private static boolean holdTriggered = false;

    // --- Instance state ---
    private final ItemStack wand;
    private int highlightedSlot = -1;
    private boolean usingKeyboardFocus = false;
    private int keyFocusRow = 0;
    private int keyFocusCol = 0;
    private final Set<Integer> prevArrowState = new HashSet<>();

    public ModeSelectorScreen(ItemStack wand) {
        super(Component.empty());
        this.wand = wand;
        WandProps.Mode current = WandProps.getMode(wand);
        for (int i = 0; i < GRID_SLOTS.length; i++) {
            if (GRID_SLOTS[i] == current) {
                keyFocusRow = i / GRID_COLS;
                keyFocusCol = i % GRID_COLS;
                break;
            }
        }
    }

    // ========== Static API (called from WandsModClient) ==========

    public static boolean consumesKey(WandsMod.WandKeys key) {
        if (key == WandsMod.WandKeys.MODE) return true;
        if (Minecraft.getInstance().screen instanceof ModeSelectorScreen) {
            switch (key) {
                case N_INC:
                case N_DEC:
                case M_INC:
                case M_DEC:
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    public static void clientTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            resetHold();
            return;
        }

        if (client.screen instanceof ModeSelectorScreen) return;
        if (client.screen != null) {
            resetHold();
            return;
        }

        ItemStack mainHand = client.player.getMainHandItem();
        boolean holdingWand = mainHand != null && !mainHand.isEmpty() && mainHand.getItem() instanceof WandItem;
        if (!holdingWand) {
            resetHold();
            return;
        }

        long window = Compat.getWindow();
        int modeKeyCode = getModeKeyCode();
        if (modeKeyCode <= 0) {
            resetHold();
            return;
        }

        boolean modeKeyPressed = GLFW.glfwGetKey(window, modeKeyCode) == GLFW.GLFW_PRESS;
        int currentTick = client.gui.getGuiTicks();

        if (modeKeyPressed) {
            if (holdStartTick == -1) {
                holdStartTick = currentTick;
                holdTriggered = false;
            } else if (!holdTriggered && (currentTick - holdStartTick) >= HOLD_THRESHOLD) {
                holdTriggered = true;
                client.setScreen(new ModeSelectorScreen(mainHand));
            }
        } else {
            if (holdStartTick != -1 && !holdTriggered) {
                tapToggle(client, mainHand);
            }
            resetHold();
        }
    }

    public static boolean isGridOpen() {
        return Minecraft.getInstance().screen instanceof ModeSelectorScreen;
    }

    // ========== Screen overrides ==========

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        if (pointingHandCursor == 0) {
            pointingHandCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_POINTING_HAND_CURSOR);
        }
    }

    @Override
    public void tick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            onClose();
            return;
        }

        ItemStack mainHand = client.player.getMainHandItem();
        boolean holdingWand = mainHand != null && !mainHand.isEmpty() && mainHand.getItem() instanceof WandItem;
        if (!holdingWand) {
            onClose();
            return;
        }

        long window = Compat.getWindow();
        int modeKeyCode = getModeKeyCode();

        if (modeKeyCode > 0 && GLFW.glfwGetKey(window, modeKeyCode) != GLFW.GLFW_PRESS) {
            if (highlightedSlot >= 0 && highlightedSlot < GRID_SLOTS.length) {
                WandProps.Mode mode = GRID_SLOTS[highlightedSlot];
                if (mode != null && isModeEnabled(mode, wand)) {
                    WandProps.Mode current = WandProps.getMode(wand);
                    if (mode != current) {
                        commitMode(wand, mode);
                    }
                }
            }
            onClose();
            return;
        }

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            onClose();
            return;
        }

        handleArrowKeys(window);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        Font font = this.font;
        WandProps.Mode currentMode = WandProps.getMode(wand);
        int screenW = this.width;
        int screenH = this.height;

        updateMouseHighlight(mouseX, mouseY);

        int gridW = GRID_COLS * TILE_SIZE + (GRID_COLS - 1) * TILE_GAP;
        int gridH = GRID_ROWS * TILE_SIZE + (GRID_ROWS - 1) * TILE_GAP;
        int panelW = gridW + GRID_PADDING * 2;
        int titleHeight = font.lineHeight + 6;
        int hintHeight = font.lineHeight + 6;
        int panelH = GRID_PADDING + titleHeight + gridH + hintHeight + GRID_PADDING;
        int panelX = (screenW - panelW) / 2;
        int panelY = (screenH - panelH) / 2;
        int gridX = panelX + GRID_PADDING;
        int gridY = panelY + GRID_PADDING + titleHeight;

        // Scrim
        gui.fill(0, 0, screenW, screenH, COLOR_SCRIM);

        // Panel background
        gui.fill(panelX, panelY, panelX + panelW, panelY + panelH, WandScreen.COLOR_PANEL_BACKGROUND);

        // Title
        String title = Compat.translatable("wands.mode_selector.title").getString();
        int titleW = font.width(title);
        gui.drawString(font, title, panelX + (panelW - titleW) / 2, panelY + GRID_PADDING, WandScreen.COLOR_TEXT_PRIMARY);

        // Tiles
        for (int i = 0; i < GRID_SLOTS.length; i++) {
            WandProps.Mode mode = GRID_SLOTS[i];
            if (mode == null) continue;

            int col = i % GRID_COLS;
            int row = i / GRID_COLS;
            int tileX = gridX + col * (TILE_SIZE + TILE_GAP);
            int tileY = gridY + row * (TILE_SIZE + TILE_GAP);

            boolean isHighlighted = (i == highlightedSlot);
            boolean isDisabled = !isModeEnabled(mode, wand);
            boolean isCurrent = (mode == currentMode);

            int bgColor;
            if (isDisabled) {
                bgColor = COLOR_TILE_DISABLED;
            } else if (isHighlighted) {
                bgColor = COLOR_TILE_HIGHLIGHT;
            } else {
                bgColor = COLOR_TILE_NORMAL;
            }
            gui.fill(tileX, tileY, tileX + TILE_SIZE, tileY + TILE_SIZE, bgColor);

            if (isCurrent) {
                drawBorder(gui, tileX, tileY, TILE_SIZE, TILE_SIZE, COLOR_CURRENT_BORDER);
            }

            MyIdExt tex = ICON_TEXTURES.get(mode);
            if (tex != null) {
                int iconX = tileX + (TILE_SIZE - ICON_SIZE) / 2;
                int iconY = tileY + (TILE_SIZE - ICON_SIZE - font.lineHeight) / 2;
                Compat.blit(gui, tex, iconX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            }

            String label = getShortLabel(mode);
            int labelW = font.width(label);
            int labelX = tileX + (TILE_SIZE - labelW) / 2;
            int labelY = tileY + TILE_SIZE - font.lineHeight - 1;
            int labelColor = isDisabled ? 0xFF666666 : WandScreen.COLOR_WDGT_LABEL;
            gui.drawString(font, label, labelX, labelY, labelColor);

            if (isHighlighted) {
                List<Component> tooltipLines = new ArrayList<>();
                String desc = isDisabled ? getDisabledReason(mode) : getDescription(mode);
                if (desc != null) {
                    tooltipLines.add(Compat.literal(desc));
                }
                if (!tooltipLines.isEmpty()) {
                    Compat.renderComponentTooltip(gui, font, tooltipLines, mouseX, mouseY);
                }
            }
        }

        // Hint centered between bottom tile row and panel bottom
        String modeKeyName = getModeKeyName();
        String hint = "Release [" + modeKeyName + "] to select";
        int hintW = font.width(hint);
        int hintY = gridY + gridH + (panelY + panelH - gridY - gridH - font.lineHeight) / 2;
        gui.drawString(font, hint, panelX + (panelW - hintW) / 2, hintY, COLOR_HINT_TEXT);

        // Finger cursor only over enabled tiles
        GLFW.glfwSetCursor(Compat.getWindow(), highlightedSlot >= 0 ? pointingHandCursor : 0);
    }

    #if MC_VERSION >= 12101
    @Override
    public void renderBackground(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        // Don't render default background — we draw our own scrim
    }
    #endif

    @Override
    public void onClose() {
        GLFW.glfwSetCursor(Compat.getWindow(), 0);
        resetHold();
        super.onClose();
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        usingKeyboardFocus = false;
        super.mouseMoved(mouseX, mouseY);
    }

    // ========== Private helpers ==========

    private static void resetHold() {
        holdStartTick = -1;
        holdTriggered = false;
    }

    private static void tapToggle(Minecraft client, ItemStack wand) {
        boolean canBlast = wand.getItem() instanceof WandItem && ((WandItem) wand.getItem()).can_blast;
        WandProps.nextMode(wand, canBlast);
        Networking.send_wand(wand);
        showModeMessage(client, wand);
    }

    private static void commitMode(ItemStack wand, WandProps.Mode mode) {
        WandProps.switchMode(wand, mode);
        Networking.send_wand(wand);
        showModeMessage(Minecraft.getInstance(), wand);
    }

    private static void showModeMessage(Minecraft client, ItemStack wand) {
        if (WandsMod.config.disable_info_messages || client.player == null) return;
        WandProps.Mode mode = WandProps.getMode(wand);
        if (WandProps.hasMultipleActions(mode)) {
            client.player.displayClientMessage(Compat.translatable(WandProps.getAction(wand).toString()).append(" ").append(Compat.translatable(mode.toString())), true);
        } else {
            client.player.displayClientMessage(Compat.translatable(mode.toString()), true);
        }
    }

    private static boolean isModeEnabled(WandProps.Mode mode, ItemStack wand) {
        if (mode == WandProps.Mode.VEIN) return WandsMod.config.enable_vein_mode;
        if (mode == WandProps.Mode.BLAST) {
            if (!WandsMod.config.enable_blast_mode) return false;
            if (wand.getItem() instanceof WandItem) return ((WandItem) wand.getItem()).can_blast;
            return false;
        }
        return true;
    }

    private static int getModeKeyCode() {
        var km = WandsModClient.reverseKeys.get(WandsMod.WandKeys.MODE);
        if (km == null) return -1;
        return Compat.getKeyCode(km);
    }

    private static String getModeKeyName() {
        return WandsModClient.getKeyName(WandsMod.WandKeys.MODE);
    }

    private static int getArrowKeyCode(WandsMod.WandKeys key) {
        var km = WandsModClient.reverseKeys.get(key);
        if (km == null) return -1;
        return Compat.getKeyCode(km);
    }

    private static String getShortLabel(WandProps.Mode mode) {
        String translated = Compat.translatable(mode.toString()).getString();
        if (translated.startsWith("wands.modes.")) {
            switch (mode) {
                case DIRECTION: return "Dir";
                case ROW_COL:   return "Row";
                case FILL:      return "Fill";
                case AREA:      return "Area";
                case GRID:      return "Grid";
                case LINE:      return "Line";
                case CIRCLE:    return "Circle";
                case BOX:       return "Box";
                case SPHERE:    return "Sphere";
                case ROCK:      return "Rock";
                case VEIN:      return "Vein";
                case BLAST:     return "Blast";
                case COPY:      return "Copy";
                case PASTE:     return "Paste";
                default:        return mode.name();
            }
        }
        if (translated.length() > 6) return translated.substring(0, 5) + ".";
        return translated;
    }

    private static String getDescription(WandProps.Mode mode) {
        String key = "tooltip.wands.mode." + mode.name().toLowerCase();
        if (mode == WandProps.Mode.ROW_COL) key = "tooltip.wands.mode.row_col";
        String translated = Compat.translatable(key).getString();
        if (translated.equals(key)) return null;
        return translated;
    }

    private static String getDisabledReason(WandProps.Mode mode) {
        if (mode == WandProps.Mode.VEIN) return "Vein mode disabled in config";
        if (mode == WandProps.Mode.BLAST) return "Blast mode not available";
        return "Mode disabled";
    }

    private static void drawBorder(GuiGraphics gui, int x, int y, int w, int h, int color) {
        gui.fill(x, y, x + w, y + 1, color);
        gui.fill(x, y + h - 1, x + w, y + h, color);
        gui.fill(x, y, x + 1, y + h, color);
        gui.fill(x + w - 1, y, x + w, y + h, color);
    }

    private void updateMouseHighlight(int mouseX, int mouseY) {
        if (usingKeyboardFocus) return;

        Font font = this.font;
        int screenW = this.width;
        int screenH = this.height;

        int gridW = GRID_COLS * TILE_SIZE + (GRID_COLS - 1) * TILE_GAP;
        int gridH = GRID_ROWS * TILE_SIZE + (GRID_ROWS - 1) * TILE_GAP;
        int panelW = gridW + GRID_PADDING * 2;
        int titleHeight = font.lineHeight + 6;
        int hintHeight = font.lineHeight + 6;
        int panelH = GRID_PADDING + titleHeight + gridH + hintHeight + GRID_PADDING;
        int panelX = (screenW - panelW) / 2;
        int panelY = (screenH - panelH) / 2;
        int gridX = panelX + GRID_PADDING;
        int gridY = panelY + GRID_PADDING + titleHeight;

        int relX = mouseX - gridX;
        int relY = mouseY - gridY;

        if (relX < 0 || relY < 0 || relX >= gridW || relY >= gridH) {
            highlightedSlot = -1;
            return;
        }

        int step = TILE_SIZE + TILE_GAP;
        int col = relX / step;
        int row = relY / step;

        int inTileX = relX - col * step;
        int inTileY = relY - row * step;
        if (inTileX > TILE_SIZE || inTileY > TILE_SIZE) {
            highlightedSlot = -1;
            return;
        }

        if (col >= 0 && col < GRID_COLS && row >= 0 && row < GRID_ROWS) {
            int idx = row * GRID_COLS + col;
            if (idx < GRID_SLOTS.length && GRID_SLOTS[idx] != null && isModeEnabled(GRID_SLOTS[idx], wand)) {
                highlightedSlot = idx;
            } else {
                highlightedSlot = -1;
            }
        } else {
            highlightedSlot = -1;
        }
    }

    private void handleArrowKeys(long window) {
        int upKey = getArrowKeyCode(WandsMod.WandKeys.N_INC);
        int downKey = getArrowKeyCode(WandsMod.WandKeys.N_DEC);
        int rightKey = getArrowKeyCode(WandsMod.WandKeys.M_INC);
        int leftKey = getArrowKeyCode(WandsMod.WandKeys.M_DEC);

        boolean moved = false;
        moved |= checkArrowPress(window, upKey, 0, -1);
        moved |= checkArrowPress(window, downKey, 0, 1);
        moved |= checkArrowPress(window, leftKey, -1, 0);
        moved |= checkArrowPress(window, rightKey, 1, 0);

        prevArrowState.clear();
        addIfPressed(window, upKey);
        addIfPressed(window, downKey);
        addIfPressed(window, leftKey);
        addIfPressed(window, rightKey);

        if (moved) {
            clampKeyboardFocus();
            usingKeyboardFocus = true;
            highlightedSlot = keyFocusRow * GRID_COLS + keyFocusCol;
        }
    }

    private boolean checkArrowPress(long window, int keyCode, int dc, int dr) {
        if (keyCode <= 0) return false;
        boolean pressed = GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS;
        boolean wasPressed = prevArrowState.contains(keyCode);
        if (pressed && !wasPressed) {
            int newRow = keyFocusRow + dr;
            int newCol = keyFocusCol + dc;
            if (newRow >= 0 && newRow < GRID_ROWS && newCol >= 0 && newCol < GRID_COLS) {
                keyFocusRow = newRow;
                keyFocusCol = newCol;
                return true;
            }
        }
        return false;
    }

    private void addIfPressed(long window, int keyCode) {
        if (keyCode > 0 && GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS) {
            prevArrowState.add(keyCode);
        }
    }

    private void clampKeyboardFocus() {
        int idx = keyFocusRow * GRID_COLS + keyFocusCol;
        if (idx < GRID_SLOTS.length && GRID_SLOTS[idx] != null && isModeEnabled(GRID_SLOTS[idx], wand)) {
            return;
        }
        for (int dist = 1; dist <= 3; dist++) {
            int[][] offsets = {{0, -dist}, {0, dist}, {-dist, 0}, {dist, 0}};
            for (int[] off : offsets) {
                int r = keyFocusRow + off[0];
                int c = keyFocusCol + off[1];
                if (r >= 0 && r < GRID_ROWS && c >= 0 && c < GRID_COLS) {
                    int i = r * GRID_COLS + c;
                    if (i < GRID_SLOTS.length && GRID_SLOTS[i] != null && isModeEnabled(GRID_SLOTS[i], wand)) {
                        keyFocusRow = r;
                        keyFocusCol = c;
                        return;
                    }
                }
            }
        }
    }
}
