package net.nicguzzo.wands.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
#if MC_VERSION >= 12111
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
#endif
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.client.gui.GuiGraphics;

import net.nicguzzo.compat.MyIdExt;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.items.*;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.gui.Btn;
import net.nicguzzo.wands.client.gui.CycleToggle;
import net.nicguzzo.wands.client.gui.CycleSpinner;
import net.nicguzzo.wands.client.gui.Section;
import net.nicguzzo.wands.client.gui.Spinner;
import net.nicguzzo.wands.client.gui.Tabs;
import net.nicguzzo.wands.client.gui.Wdgt;
import net.nicguzzo.wands.menues.WandMenu;
import net.nicguzzo.wands.networking.Networking;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;
import net.nicguzzo.wands.wand.WandProps.Value;
import net.nicguzzo.compat.Compat;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class WandScreen extends AbstractContainerScreen<WandMenu> {
    ItemStack wandStack=null;
    WandItem wandItem =null;
    #if MC_VERSION >= 12111
    int[] empty_tools= new int[0];
    GpuTextureView wandInventoryTexture;
    #endif
    private static final MyIdExt INV_TEX = new MyIdExt("textures/gui/inventory.png");
    static final int IMG_WIDTH = 256;
    static final int IMG_HEIGHT = 256;

    // ===== Color Constants =====
    // Panel/Layout colors
    public static final int COLOR_PANEL_BACKGROUND = 0xE61A1A1A;  // Semi-transparent black

    // Button state colors
    public static final int COLOR_BTN_HOVER = 0xE6888888;     // Light-medium gray for button hover
    public static final int COLOR_BTN_SELECTED = 0xE6666666;  // Medium gray for selected
    public static final int COLOR_BTN_DISABLED = 0xB3B3B3B3;  // Light gray (Colorf 0.7, 0.7, 0.7, 0.7)
    public static final int COLOR_WDGT_HOVER = 0xE6666666;    // Medium gray for widget hover (spinners, toggles)

    // Text colors
    public static final int COLOR_TEXT_PRIMARY = 0xFFFFFFFF;   // White
    public static final int COLOR_WDGT_LABEL = 0xFFAAAAAA;     // Light gray for widget labels
    // Shared with PaletteScreen - use for non-shadowed instructional text
    public static final int COLOR_TEXT_DARK = Compat.DARK_GRAY;

    // Tooltip colors
    public static final int COLOR_TOOLTIP_TITLE = 0xFFFFFFFF;       // White for title line
    public static final int COLOR_TOOLTIP_DESC = 0xFFAAAAAA;        // Gray for description lines
    public static final int COLOR_TOOLTIP_BG = 0xF0100010;          // Dark background
    public static final int COLOR_TOOLTIP_BORDER = 0x505000FF;      // Purple border

    // ===== Layout Constants =====
    /** Distance from screen edges to the panel */
    public static final int SCREEN_MARGIN = 4;
    /** Vertical spacing between sections within the panel */
    public static final int SECTION_SPACING = 14;
    /** Width of the main content area (excluding tabs) */
    public static final int CONTENT_WIDTH = 100;
    /** Horizontal padding inside the panel (around tabs and section) */
    public static final int INNER_PADDING = 4;

    // Toggle for vertical divider line between tabs and mode options
    private static final boolean SHOW_TAB_DIVIDER = true;
    private static final int COLOR_TAB_DIVIDER = 0xFF444444;  // Dark gray line
    private static final int DIVIDER_LINE_WIDTH = 1;          // Divider line thickness in pixels

    // EXPERIMENTAL: Extended divider that fills from tabs to right edge with widget background color
    private static final boolean USE_EXTENDED_DIVIDER = true;

    Component rockMessage  = Compat.literal("rotate for new rock");
    private static final int SPINNER_HEIGHT = 14;

    Vector<Wdgt> wdgets = new Vector<>();

    // Layout
    Section modeOptionsSection;
    Section toolsSection;

    // Mode Tabs
    Tabs modeTabs;
    boolean isToolsTabSelected = false;  // Track if Tools tab is selected

    // Block State Section - combined state + axis
    CycleToggle<Integer> blockStateCycle;

    // Tools Section
    CycleToggle<Boolean> dropPositionToggle;
    Btn showInventoryButton;
    boolean showInventory = false;
    Btn configButton;

    // Mode Options Section
    CycleToggle<WandProps.Action> actionCycle;
    CycleSpinner targetAirSpinner;
    CycleToggle<Boolean> mirrorLRToggle;
    CycleToggle<Boolean> mirrorFBToggle;
    CycleToggle<Rotation> rotationCycle;
    Spinner rockRadiusSpinner;
    Spinner rockNoiseSpinner;
    Spinner multiplierSpinner;
    CycleToggle<Boolean> invertToggle;
    Spinner rowColumnLimitSpinner;
    CycleToggle<WandProps.Orientation> orientationCycle;
    Spinner areaLimitSpinner;
    CycleToggle<Boolean> diagonalSpreadToggle;
    Spinner skipBlockSpinner;
    CycleToggle<Boolean> matchStateToggle;
    CycleToggle<WandProps.Plane> planeCycle;
    CycleToggle<Boolean> circleFillToggle;
    CycleToggle<Boolean> evenSizeToggle;
    CycleToggle<Boolean> boxFillToggle;
    Spinner gridMSpinner;
    Spinner gridNSpinner;
    Spinner gridMSkipSpinner;
    Spinner gridNSkipSpinner;
    Spinner gridMOffsetSpinner;
    Spinner gridNOffsetSpinner;
    Spinner blastRadiusSpinner;
    Spinner boxWidthSpinner;
    Spinner boxHeightSpinner;
    Spinner boxDepthSpinner;
    Spinner boxOffsetXSpinner;
    Spinner boxOffsetYSpinner;
    CycleToggle<Boolean> includeBlockToggle;
    CycleToggle<Boolean> keepStartToggle;

    // Map widget to the modes it should be visible for
    Map<Wdgt, EnumSet<WandProps.Mode>> modeWidgets = new HashMap<>();

    // Screen position offsets (from config)
    int screenXOffset;
    int screenYOffset;

    // Cursor handling
    private static long handCursor = 0;
    private boolean isHandCursor = false;

    private void updateCursor(int mouseX, int mouseY) {
        boolean shouldBeHand = isOverClickable(mouseX, mouseY);
        if (shouldBeHand != isHandCursor) {
            isHandCursor = shouldBeHand;
            long window = Compat.getWindow();
            if (shouldBeHand) {
                if (handCursor == 0) {
                    handCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
                }
                GLFW.glfwSetCursor(window, handCursor);
            } else {
                GLFW.glfwSetCursor(window, 0);
            }
        }
    }

    private boolean isOverClickable(int mx, int my) {
        for (Wdgt wdget : wdgets) {
            if (wdget.visible && isWidgetClickable(wdget, mx, my)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOverInventorySlot(int mx, int my) {
        for (int i = 0; i < 36; i++) {
            Slot slot = this.menu.slots.get(i);
            int slotX = slot.x + this.leftPos;
            int slotY = slot.y + this.topPos;
            if (mx >= slotX && mx < slotX + 16 && my >= slotY && my < slotY + 16) {
                return true;
            }
        }
        return false;
    }

    private boolean isWidgetClickable(Wdgt widget, int mx, int my) {
        if (widget instanceof Section section) {
            for (Wdgt child : section.getChildren()) {
                if (child.visible && isWidgetClickable(child, mx, my)) {
                    return true;
                }
            }
            return false;
        } else if (widget instanceof Tabs tabs) {
            for (Btn btn : tabs.getAllVisibleButtons()) {
                if (btn.inside(mx, my) && btn.isClickable()) {
                    return true;
                }
            }
            return false;
        } else {
            return widget.inside(mx, my) && widget.isClickable();
        }
    }

    /**
     * Find the widget that should show its tooltip at the given mouse position.
     * Checks sections for hovered children, tabs for hovered buttons, and direct widgets.
     */
    private Wdgt findHoveredWidget(int mouseX, int mouseY) {
        for (Wdgt wdget : wdgets) {
            if (wdget instanceof Section section && section.visible) {
                Wdgt hovered = section.getHoveredChild(mouseX, mouseY);
                if (hovered != null && (hovered.tooltip != null || hovered.tooltipTitle != null)) {
                    return hovered;
                }
            } else if (wdget instanceof Tabs tabs) {
                Btn hovered = tabs.getHoveredButton(mouseX, mouseY);
                if (hovered != null && (hovered.tooltip != null || hovered.tooltipTitle != null)) {
                    return hovered;
                }
            } else if (wdget.shouldShowTooltip(mouseX, mouseY)) {
                return wdget;
            }
        }
        return null;
    }

    public WandScreen(WandMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        #if MC_VERSION >= 12111
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        wandInventoryTexture=textureManager.getTexture(INV_TEX.res).getTextureView();
        #endif
    }
    // Remember last action for modes that don't support actions (like COPY, BLAST)
    private WandProps.Action lastRememberedAction = WandProps.Action.PLACE;

    // Preserve action when switching modes if applicable, otherwise remember and restore
    private void resetActionForMode(WandProps.Mode mode) {
        ItemStack actualWand = getPlayerHeldWand();
        WandProps.Action currentAction = WandProps.getAction(actualWand);

        // Check if current action is valid for the new mode
        if (isActionValidForMode(currentAction, mode)) {
            // Keep current action, but remember it for later
            lastRememberedAction = currentAction;
            return;
        }

        // Current action not valid - try to restore last remembered action
        if (isActionValidForMode(lastRememberedAction, mode)) {
            WandProps.setAction(actualWand, lastRememberedAction);
            return;
        }

        // Neither current nor remembered action works - find first valid action
        for (WandProps.Action action : WandProps.actions) {
            if (isActionValidForMode(action, mode)) {
                WandProps.setAction(actualWand, action);
                return;
            }
        }
    }

    // Helper to check if an action is valid for a mode (considering config)
    private boolean isActionValidForMode(WandProps.Action action, WandProps.Mode mode) {
        if (!WandProps.actionAppliesTo(action, mode)) {
            return false;
        }
        if (WandsMod.config.disable_destroy_replace &&
            (action == WandProps.Action.DESTROY || action == WandProps.Action.REPLACE)) {
            return false;
        }
        return true;
    }

    // Helper for creating a spinner bound to a WandProps.Value
    private Spinner valSpinner(Value val, int w, int h, Component label) {
        return new Spinner(WandProps.getVal(wandStack, val), val.min, val.max, w, h, label)
            .withOnChange(value -> syncWandValue(val, value));
    }

    /** Get the player's actual held wand item (not the menu's copy) */
    private ItemStack getPlayerHeldWand() {
        if (Minecraft.getInstance().player != null) {
            ItemStack mainHand = Minecraft.getInstance().player.getMainHandItem();
            if (mainHand.getItem() instanceof WandItem) {
                return mainHand;
            }
            ItemStack offHand = Minecraft.getInstance().player.getOffhandItem();
            if (offHand.getItem() instanceof WandItem) {
                return offHand;
            }
        }
        return wandStack;  // Fallback to menu's copy
    }

    /** Force client render to recalculate preview when values change */
    private void forceClientRedraw() {
        if (ClientRender.wand != null) {
            WandMode mode = ClientRender.wand.get_mode();
            if (mode != null) {
                mode.redraw(ClientRender.wand);
            }
        }
    }

    /** Update a wand value and sync to server with client redraw */
    private void syncWandValue(Value val, int value) {
        ItemStack actualWand = getPlayerHeldWand();
        WandProps.setVal(actualWand, val, value);
        syncWand(actualWand);
    }

    /** Sync wand to server and force client redraw */
    private void syncWand(ItemStack wand) {
        Networking.send_wand(wand);
        forceClientRedraw();
    }

    /** Build dynamic tooltip for block state options based on context */
    private Component buildBlockStateTooltip(int idx, boolean isPillarBlock, boolean isHollowFill) {
        // Base tooltip keys for each option
        String baseKey = switch (idx) {
            case WandProps.BLOCK_STATE_CLONE -> "tooltip.wands.clone_state";
            case WandProps.BLOCK_STATE_APPLY_X -> "tooltip.wands.apply_x";
            case WandProps.BLOCK_STATE_APPLY_Y -> "tooltip.wands.apply_y";
            case WandProps.BLOCK_STATE_APPLY_Z -> "tooltip.wands.apply_z";
            case WandProps.BLOCK_STATE_FLIP_X -> "tooltip.wands.flip_x";
            case WandProps.BLOCK_STATE_FLIP_Y -> "tooltip.wands.flip_y";
            case WandProps.BLOCK_STATE_FLIP_Z -> "tooltip.wands.flip_z";
            case WandProps.BLOCK_STATE_NORMAL -> "tooltip.wands.normal_place";
            default -> "tooltip.wands.normal_place";
        };

        // For Clone and Normal, just return the base tooltip
        if (idx == WandProps.BLOCK_STATE_CLONE || idx == WandProps.BLOCK_STATE_NORMAL) {
            return Compat.translatable(baseKey);
        }

        // Build tooltip with context-specific additions
        StringBuilder tooltip = new StringBuilder();
        tooltip.append(Compat.translatable(baseKey).getString());

        // Add pillar block info if applicable
        if (isPillarBlock) {
            String pillarKey = baseKey + ".pillar";
            tooltip.append(" ").append(Compat.translatable(pillarKey).getString());
        }

        // Add hollow fill info if applicable
        if (isHollowFill) {
            String hollowKey = baseKey + ".hollow";
            tooltip.append(" ").append(Compat.translatable(hollowKey).getString());
        }

        return Compat.literal(tooltip.toString());
    }

    // Helper for creating a boolean toggle bound to a WandProps.Flag
    private CycleToggle<Boolean> flagToggle(WandProps.Flag flag, int w, Component label) {
        CycleToggle<Boolean> toggle = CycleToggle.ofBoolean(label,
            () -> WandProps.getFlag(getPlayerHeldWand(), flag),
            value -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setFlag(actualWand, flag, value);
                syncWand(actualWand);
            });
        toggle.width = w;
        return toggle;
    }

    /** Create boolean toggle with auto-computed translations, add to section, and auto-register mode visibility */
    private CycleToggle<Boolean> addFlagToggle(Section section, WandProps.Flag flag, int w, String key) {
        Component label = Compat.translatable("screen.wands." + key);
        Component tooltip = Compat.translatable("tooltip.wands." + key);
        CycleToggle<Boolean> toggle = flagToggle(flag, w, label);
        toggle.withTooltip(label, tooltip);
        section.add(toggle);
        // Auto-register mode visibility from FLAG_MODES
        EnumSet<WandProps.Mode> modes = WandProps.FLAG_MODES.get(flag);
        if (modes != null) {
            modeWidgets.put(toggle, modes);
        }
        return toggle;
    }

    /** Create inverted boolean toggle (ON = flag false, OFF = flag true) with auto-computed translations and auto-register */
    private CycleToggle<Boolean> addInvertedFlagToggle(Section section, WandProps.Flag flag, int w, String key) {
        Component label = Compat.translatable("screen.wands." + key);
        Component tooltip = Compat.translatable("tooltip.wands." + key);
        CycleToggle<Boolean> toggle = CycleToggle.ofBoolean(label,
            () -> !WandProps.getFlag(getPlayerHeldWand(), flag),  // Inverted: show ON when flag is false
            value -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setFlag(actualWand, flag, !value);  // Inverted: set flag to opposite of toggle
                syncWand(actualWand);
            });
        toggle.width = w;
        toggle.withTooltip(label, tooltip);
        section.add(toggle);
        // Auto-register mode visibility from FLAG_MODES
        EnumSet<WandProps.Mode> modes = WandProps.FLAG_MODES.get(flag);
        if (modes != null) {
            modeWidgets.put(toggle, modes);
        }
        return toggle;
    }

    /** Create Spinner with auto-computed translations, add to section, and auto-register mode visibility */
    private Spinner addValSpinner(Section section, Value val, int w, int h, String key) {
        Component label = Compat.translatable("screen.wands." + key);
        Component tooltip = Compat.translatable("tooltip.wands." + key);
        Spinner spinner = valSpinner(val, w, h, label);
        spinner.withTooltip(label, tooltip);
        section.add(spinner);
        // Auto-register mode visibility from VALUE_MODES
        EnumSet<WandProps.Mode> modes = WandProps.VALUE_MODES.get(val);
        if (modes != null) {
            modeWidgets.put(spinner, modes);
        }
        return spinner;
    }

    // Mode array - populated by createModeTabs
    WandProps.Mode[] modesArray;

    // Helper for text-based mode buttons
    private Btn modeTextBtn(WandProps.Mode mode, String titleKey, String descKey) {
        Component label = Compat.translatable(titleKey);
        Btn btn = new Btn(0, 0, Tabs.TAB_SIZE, Tabs.TAB_SIZE, label, (mouseX, mouseY) -> {
            isToolsTabSelected = false;
            ItemStack actualWand = getPlayerHeldWand();
            WandProps.setMode(actualWand, mode);
            resetActionForMode(mode);
            Networking.send_wand(actualWand);
        });
        btn.withTooltip(Compat.translatable(titleKey), Compat.translatable(descKey));
        return btn;
    }

    private Tabs createModeTabs() {
        Tabs tabs = new Tabs(1);  // Minimal spacing between tabs

        boolean canBlast = wandItem.can_blast && WandsMod.config.enable_blast_mode;
        boolean canVein = WandsMod.config.enable_vein_mode;

        List<WandProps.Mode> modesList = new ArrayList<>();
        List<Btn> textButtons = new ArrayList<>();

        // Add all modes as flat text buttons in order
        Btn btn;

        btn = modeTextBtn(WandProps.Mode.DIRECTION, "wands.modes.direction", "tooltip.wands.mode.direction");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.DIRECTION);

        btn = modeTextBtn(WandProps.Mode.LINE, "wands.modes.line", "tooltip.wands.mode.line");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.LINE);

        btn = modeTextBtn(WandProps.Mode.CIRCLE, "wands.modes.circle", "tooltip.wands.mode.circle");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.CIRCLE);

        btn = modeTextBtn(WandProps.Mode.ROW_COL, "wands.modes.row_col", "tooltip.wands.mode.row_col");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.ROW_COL);

        btn = modeTextBtn(WandProps.Mode.AREA, "wands.modes.area", "tooltip.wands.mode.area");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.AREA);

        btn = modeTextBtn(WandProps.Mode.FILL, "wands.modes.fill", "tooltip.wands.mode.fill");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.FILL);

        btn = modeTextBtn(WandProps.Mode.BOX, "wands.modes.box", "tooltip.wands.mode.box");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.BOX);

        btn = modeTextBtn(WandProps.Mode.SPHERE, "wands.modes.sphere", "tooltip.wands.mode.sphere");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.SPHERE);

        btn = modeTextBtn(WandProps.Mode.GRID, "wands.modes.grid", "tooltip.wands.mode.grid");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.GRID);

        btn = modeTextBtn(WandProps.Mode.ROCK, "wands.modes.rock", "tooltip.wands.mode.rock");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.ROCK);

        if (canBlast) {
            btn = modeTextBtn(WandProps.Mode.BLAST, "wands.modes.blast", "tooltip.wands.mode.blast");
            tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.BLAST);
        }

        if (canVein) {
            btn = modeTextBtn(WandProps.Mode.VEIN, "wands.modes.vein", "tooltip.wands.mode.vein");
            tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.VEIN);
        }

        btn = modeTextBtn(WandProps.Mode.COPY, "wands.modes.copy", "tooltip.wands.mode.copy");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.COPY);

        btn = modeTextBtn(WandProps.Mode.PASTE, "wands.modes.paste", "tooltip.wands.mode.paste");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.PASTE);

        // Tools tab at the end (text button for consistency with flat_text layout)
        Btn toolsTabBtn = modeTextBtn(null, "screen.wands.tools", "tooltip.wands.tools_tab");
        // Override the click handler for tools tab
        toolsTabBtn = new Btn(0, 0, Tabs.TAB_SIZE, Tabs.TAB_SIZE, Compat.translatable("screen.wands.tools"), (mouseX, mouseY) -> {
            isToolsTabSelected = !isToolsTabSelected;
        });
        toolsTabBtn.withTooltip(Compat.translatable("screen.wands.tools"), Compat.translatable("tooltip.wands.tools_tab"));
        tabs.add(toolsTabBtn);
        textButtons.add(toolsTabBtn);

        // Calculate max text width and set all buttons to that width
        net.minecraft.client.gui.Font font = Minecraft.getInstance().font;
        int maxWidth = 0;
        for (Btn textBtn : textButtons) {
            if (textBtn.labelText != null) {
                int textWidth = font.width(textBtn.labelText) + Wdgt.TEXT_PADDING * 2;
                maxWidth = Math.max(maxWidth, textWidth);
            }
        }
        // Set all text buttons to the max width
        for (Btn textBtn : textButtons) {
            textBtn.width = maxWidth;
        }

        modesArray = modesList.toArray(new WandProps.Mode[0]);
        tabs.recalculateBounds();
        return tabs;
    }

    private Section createToolsSection() {
        Section section = new Section();

        // Tools button (show inventory) - text button with "..."
        showInventoryButton = new Btn(0, 0, CONTENT_WIDTH, 14, Compat.translatable("screen.wands.pick_tools").copy().append("..."), (mouseX, mouseY) -> {
            showInventory = !showInventory;
        });
        showInventoryButton.withTooltip(Compat.translatable("screen.wands.pick_tools"), Compat.translatable("tooltip.wands.pick_tools"));
        section.add(showInventoryButton);

#if USE_CLOTHCONFIG
        // Config button - text button with "..."
        if (WandsMod.platform != 2) {
            Screen parent = this;
            configButton = new Btn(0, 0, CONTENT_WIDTH, 14, Compat.translatable("screen.wands.conf").copy().append("..."), (mouseX, mouseY) -> {
                Minecraft.getInstance().setScreen(WandConfigScreen.create(parent));
            });
            configButton.withTooltip(Compat.translatable("screen.wands.conf"), Compat.translatable("tooltip.wands.conf"));
            section.add(configButton);
        }
#endif

        return section;
    }

    private Section createModeOptionsSection(int layoutColWidth, int spinnerHeight) {
        Section section = new Section();

        // Action select (mode-conditional - hidden for COPY and BLAST, filtered for VEIN)
        Component[] actionLabels = {
            Compat.translatable("wands.action.place"),
            Compat.translatable("wands.action.replace"),
            Compat.translatable("wands.action.destroy"),
            Compat.translatable("wands.action.use")
        };
        actionCycle = new CycleToggle<>(Compat.translatable("screen.wands.action_prefix"),
            WandProps.actions, actionLabels,
            () -> WandProps.getAction(getPlayerHeldWand()),
            (a) -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setAction(actualWand, a);
                syncWand(actualWand);
            })
            .withFilter(a -> !WandsMod.config.disable_destroy_replace ||
                (a != WandProps.Action.DESTROY && a != WandProps.Action.REPLACE))
            .withTooltips("tooltip.wands.action.place", "tooltip.wands.action.replace", "tooltip.wands.action.destroy", "tooltip.wands.action.use");
        actionCycle.width = layoutColWidth;
        section.add(actionCycle);

        // Target Air - combined toggle + spinner
        targetAirSpinner = new CycleSpinner(
            this::getPlayerHeldWand,  // Supplier: always get current wand
            this::syncWand,           // Consumer: sync wand to server
            WandProps.Flag.TARGET_AIR, Value.AIR_TARGET_DISTANCE,
            layoutColWidth, spinnerHeight, Compat.translatable("screen.wands.target_air"));
        targetAirSpinner.withTooltip(Compat.translatable("screen.wands.target_air"), Compat.translatable("tooltip.wands.target_air"));
        targetAirSpinner.withOnChange(this::forceClientRedraw);
        section.add(targetAirSpinner);

        // Mirror (PASTE mode) - boolean toggles
        mirrorLRToggle = CycleToggle.ofBoolean(Compat.translatable("screen.wands.mirror_lr"),
            () -> WandProps.getVal(getPlayerHeldWand(), Value.MIRRORAXIS) == 1,
            value -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setVal(actualWand, Value.MIRRORAXIS, value ? 1 : 0);
                syncWand(actualWand);
            });
        mirrorLRToggle.width = layoutColWidth;
        mirrorLRToggle.withTooltip(Compat.translatable("screen.wands.mirror_lr"), Compat.translatable("tooltip.wands.mirror_lr"));
        section.add(mirrorLRToggle);

        mirrorFBToggle = CycleToggle.ofBoolean(Compat.translatable("screen.wands.mirror_fb"),
            () -> WandProps.getVal(getPlayerHeldWand(), Value.MIRRORAXIS) == 2,
            value -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setVal(actualWand, Value.MIRRORAXIS, value ? 2 : 0);
                syncWand(actualWand);
            });
        mirrorFBToggle.width = layoutColWidth;
        mirrorFBToggle.withTooltip(Compat.translatable("screen.wands.mirror_fb"), Compat.translatable("tooltip.wands.mirror_fb"));
        section.add(mirrorFBToggle);

        // Rotation select (GRID and PASTE modes)
        Component[] rotationLabels = {
            Compat.translatable("screen.wands.rot_0"),
            Compat.translatable("screen.wands.rot_90"),
            Compat.translatable("screen.wands.rot_180"),
            Compat.translatable("screen.wands.rot_270")
        };
        rotationCycle = new CycleToggle<>(Compat.translatable("screen.wands.rotation_prefix"),
            WandProps.rotations, rotationLabels,
            () -> WandProps.getRotation(getPlayerHeldWand()),
            (r) -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setRotation(actualWand, r);
                syncWand(actualWand);
            })
            .withTooltips("tooltip.wands.rot_0", "tooltip.wands.rot_90", "tooltip.wands.rot_180", "tooltip.wands.rot_270");
        rotationCycle.width = layoutColWidth;
        section.add(rotationCycle);

        // Rock mode spinners
        rockRadiusSpinner = addValSpinner(section, Value.ROCK_RADIUS, layoutColWidth, spinnerHeight, "rock_radius");
        rockNoiseSpinner = addValSpinner(section, Value.ROCK_NOISE, layoutColWidth, spinnerHeight, "rock_noise");

        // Direction mode
        multiplierSpinner = addValSpinner(section, Value.MULTIPLIER, layoutColWidth, spinnerHeight, "multiplier");
        invertToggle = addFlagToggle(section, WandProps.Flag.INVERTED, layoutColWidth, "invert");

        // Row/Col mode limit spinner
        rowColumnLimitSpinner = addValSpinner(section, Value.ROWCOLLIM, layoutColWidth, spinnerHeight, "limit");

        // Row/Col mode orientation select
        Component[] orientationLabels = {
            Compat.translatable("wands.orientation.row"),
            Compat.translatable("wands.orientation.col")
        };
        WandProps.Orientation[] orientations = { WandProps.Orientation.ROW, WandProps.Orientation.COL };
        orientationCycle = new CycleToggle<>(Compat.translatable("screen.wands.orientation"),
            orientations, orientationLabels,
            () -> WandProps.getOrientation(getPlayerHeldWand()),
            (o) -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setOrientation(actualWand, o);
                syncWand(actualWand);
            })
            .withTooltips("tooltip.wands.orientation.row", "tooltip.wands.orientation.col");
        orientationCycle.width = layoutColWidth;
        section.add(orientationCycle);

        // Area/Vein mode
        areaLimitSpinner = addValSpinner(section, Value.AREALIM, layoutColWidth, spinnerHeight, "limit");
        diagonalSpreadToggle = addFlagToggle(section, WandProps.Flag.DIAGSPREAD, layoutColWidth, "orthogonal_only");
        skipBlockSpinner = addValSpinner(section, Value.SKIPBLOCK, layoutColWidth, spinnerHeight, "skip_block");
        matchStateToggle = addFlagToggle(section, WandProps.Flag.MATCHSTATE, layoutColWidth, "match_state");

        // Circle mode - plane selection
        Component[] planeLabels = {
            Compat.literal("XZ"),
            Compat.literal("XY"),
            Compat.literal("YZ")
        };
        planeCycle = new CycleToggle<>(Compat.translatable("screen.wands.plane"),
            WandProps.planes, planeLabels,
            () -> WandProps.getPlane(getPlayerHeldWand()),
            (p) -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setPlane(actualWand, p);
                syncWand(actualWand);
            })
            .withTooltips("tooltip.wands.plane_xz", "tooltip.wands.plane_xy", "tooltip.wands.plane_yz");
        planeCycle.width = layoutColWidth;
        section.add(planeCycle);

        circleFillToggle = addFlagToggle(section, WandProps.Flag.CFILLED, layoutColWidth, "filled");
        evenSizeToggle = addFlagToggle(section, WandProps.Flag.EVEN, layoutColWidth, "even_size");

        // Fill mode - use custom labels for box
        boxFillToggle = CycleToggle.ofBoolean(Compat.translatable("screen.wands.filled"),
            () -> WandProps.getFlag(getPlayerHeldWand(), WandProps.Flag.RFILLED),
            value -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setFlag(actualWand, WandProps.Flag.RFILLED, value);
                syncWand(actualWand);
            },
            "Solid", "Hollow");
        boxFillToggle.width = layoutColWidth;
        boxFillToggle.withTooltip(Compat.translatable("screen.wands.filled"), Compat.translatable("tooltip.wands.filled"));
        section.add(boxFillToggle);

        // Grid mode spinners - M is rows/horizontal, N is columns/vertical
        gridMSpinner = new Spinner(WandProps.getVal(wandStack, Value.GRIDM), 1, wandItem.limit, layoutColWidth, spinnerHeight, Compat.translatable("screen.wands.grid_m"))
            .withOnChange(value -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setGridVal(actualWand, Value.GRIDM, value, wandItem.limit);
                syncWand(actualWand);
            });
        gridMSpinner.withTooltip(Compat.translatable("screen.wands.grid_m"), Compat.translatable("tooltip.wands.grid_m"));
        section.add(gridMSpinner);

        gridNSpinner = new Spinner(WandProps.getVal(wandStack, Value.GRIDN), 1, wandItem.limit, layoutColWidth, spinnerHeight, Compat.translatable("screen.wands.grid_n"))
            .withOnChange(value -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setGridVal(actualWand, Value.GRIDN, value, wandItem.limit);
                syncWand(actualWand);
            });
        gridNSpinner.withTooltip(Compat.translatable("screen.wands.grid_n"), Compat.translatable("tooltip.wands.grid_n"));
        section.add(gridNSpinner);

        gridMSkipSpinner = addValSpinner(section, Value.GRIDMS, layoutColWidth, spinnerHeight, "grid_m_skip");
        gridNSkipSpinner = addValSpinner(section, Value.GRIDNS, layoutColWidth, spinnerHeight, "grid_n_skip");
        gridMOffsetSpinner = addValSpinner(section, Value.GRIDMOFF, layoutColWidth, spinnerHeight, "grid_m_offset");
        gridNOffsetSpinner = addValSpinner(section, Value.GRIDNOFF, layoutColWidth, spinnerHeight, "grid_n_offset");

        // Blast mode
        blastRadiusSpinner = addValSpinner(section, Value.BLASTRAD, layoutColWidth, spinnerHeight, "blast_radius");
        blastRadiusSpinner.incrementValue = 2;
        blastRadiusSpinner.shiftIncrementValue = 4;

        // Box mode spinners
        boxWidthSpinner = addValSpinner(section, Value.BOX_W, layoutColWidth, spinnerHeight, "box_width");
        boxHeightSpinner = addValSpinner(section, Value.BOX_H, layoutColWidth, spinnerHeight, "box_height");
        boxDepthSpinner = addValSpinner(section, Value.BOX_DEPTH, layoutColWidth, spinnerHeight, "box_depth");
        boxOffsetXSpinner = addValSpinner(section, Value.BOX_OX, layoutColWidth, spinnerHeight, "box_offset_x");
        boxOffsetYSpinner = addValSpinner(section, Value.BOX_OY, layoutColWidth, spinnerHeight, "box_offset_y");

        // Include selected block in selection
        includeBlockToggle = addFlagToggle(section, WandProps.Flag.INCSELBLOCK, layoutColWidth, "include_block");

        // Keep start point (inverted: ON = don't clear P1, OFF = clear P1)
        keepStartToggle = addInvertedFlagToggle(section, WandProps.Flag.CLEAR_P1, layoutColWidth, "keep_start");

        // Block State select - at the bottom, only visible with Place action
        Component[] stateLabels = {
            Compat.translatable("screen.wands.clone_state"),
            Compat.translatable("screen.wands.apply_x"),
            Compat.translatable("screen.wands.apply_y"),
            Compat.translatable("screen.wands.apply_z"),
            Compat.translatable("screen.wands.flip_x"),
            Compat.translatable("screen.wands.flip_y"),
            Compat.translatable("screen.wands.flip_z"),
            Compat.translatable("screen.wands.normal_place")
        };
        // Simplified labels when only Y-axis options are available (non-pillar blocks)
        Component[] alternateStateLabels = {
            Compat.translatable("screen.wands.clone_state"),
            Compat.translatable("screen.wands.apply_x"),      // Not shown when filtered
            Compat.translatable("screen.wands.rotate"),       // "Rotate" instead of "Apply Y rotation"
            Compat.translatable("screen.wands.apply_z"),      // Not shown when filtered
            Compat.translatable("screen.wands.flip_x"),       // Not shown when filtered
            Compat.translatable("screen.wands.flip"),         // "Flip" instead of "Flip Y rotation"
            Compat.translatable("screen.wands.flip_z"),       // Not shown when filtered
            Compat.translatable("screen.wands.normal_place")
        };
        blockStateCycle = new CycleToggle<>(Compat.translatable("screen.wands.block_prefix"),
            WandProps.BLOCK_STATE_OPTIONS, stateLabels,
            () -> WandProps.getBlockStateIndex(getPlayerHeldWand()),
            (idx) -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setBlockStateIndex(actualWand, idx);
                syncWand(actualWand);
            })
            .withAlternateLabels(alternateStateLabels)
            .withTooltips(
                "tooltip.wands.clone_state",
                "tooltip.wands.apply_x", "tooltip.wands.apply_y", "tooltip.wands.apply_z",
                "tooltip.wands.flip_x", "tooltip.wands.flip_y", "tooltip.wands.flip_z",
                "tooltip.wands.normal_place");
        blockStateCycle.width = layoutColWidth;
        section.add(blockStateCycle);

        // Drop position toggle - only shown for DESTROY/REPLACE actions (at bottom)
        dropPositionToggle = CycleToggle.ofBoolean(Compat.translatable("screen.wands.drop_on"),
            () -> ClientRender.wand.drop_on_player,
            value -> {
                ClientRender.wand.drop_on_player = value;
                Networking.SendGlobalSettings(value);
            },
            "player", "block");
        dropPositionToggle.width = layoutColWidth;
        dropPositionToggle.withTooltip(Compat.translatable("screen.wands.drop_on"), Compat.translatable("tooltip.wands.drop_on_player"));
        section.add(dropPositionToggle);

        return section;
    }

    private void registerModeSpecificWidgets() {
        // Most widgets are auto-registered by addFlagToggle/addValSpinner helpers.
        // Only manually register widgets created without those helpers:

        // Cycle toggles not in FLAG_MODES/VALUE_MODES
        registerModeWidgets(EnumSet.of(WandProps.Mode.ROW_COL), orientationCycle);
        registerModeWidgets(EnumSet.of(WandProps.Mode.CIRCLE), planeCycle);
        registerModeWidgets(EnumSet.of(WandProps.Mode.GRID, WandProps.Mode.PASTE), rotationCycle);

        // CycleSpinner (combined flag+value widget)
        registerModeWidgets(WandProps.FLAG_MODES.get(WandProps.Flag.TARGET_AIR), targetAirSpinner);

        // Mirror toggles (manual, map to single Value with different values)
        registerModeWidgets(WandProps.VALUE_MODES.get(WandProps.Value.MIRRORAXIS), mirrorLRToggle, mirrorFBToggle);

        // Grid M/N spinners (manual due to custom limit logic)
        registerModeWidgets(WandProps.VALUE_MODES.get(WandProps.Value.GRIDM), gridMSpinner);
        registerModeWidgets(WandProps.VALUE_MODES.get(WandProps.Value.GRIDN), gridNSpinner);

        // Box fill toggle (manual, not using addFlagToggle)
        registerModeWidgets(WandProps.FLAG_MODES.get(WandProps.Flag.RFILLED), boxFillToggle);
    }

    @Override
    public void init(){
        super.init();
        wdgets.clear();  // Clear widgets on resize/reinit

        // Prefer the player's actual held wand over the menu's copy
        // This ensures we read the current values, not stale ones
        wandStack = null;
        if (Minecraft.getInstance().player != null) {
            ItemStack mainHand = Minecraft.getInstance().player.getMainHandItem();
            if (mainHand.getItem() instanceof WandItem) {
                wandStack = mainHand;
            } else {
                ItemStack offHand = Minecraft.getInstance().player.getOffhandItem();
                if (offHand.getItem() instanceof WandItem) {
                    wandStack = offHand;
                }
            }
        }
        // Fallback to menu's copy if player's wand not found
        if (wandStack == null) {
            wandStack = this.menu.wand;
        }

        if(wandStack==null){
            return;
        }
        if(wandStack.getItem() instanceof WandItem){
            wandItem =(WandItem)wandStack.getItem();
        }else{
            return;
        }
        if(wandItem ==null){
            return;
        }
        screenXOffset = WandsMod.config.wand_screen_x_offset;
        screenYOffset = WandsMod.config.wand_screen_y_offset;

        // Create vertical tabs for mode selection
        // Layout: [panel] + INNER_PADDING + [tabs] + INNER_PADDING + [line] + INNER_PADDING + [section] + INNER_PADDING + [panel]
        modeTabs = createModeTabs();
        int panelX = SCREEN_MARGIN;
        modeTabs.x = panelX + INNER_PADDING;
        modeTabs.y = SCREEN_MARGIN + INNER_PADDING;

        // Content area position (to the right of tabs + divider line)
        // Use modeTabs.width to handle variable-width tabs (e.g., flat_text layout)
        int contentX = modeTabs.x + modeTabs.width + INNER_PADDING + DIVIDER_LINE_WIDTH + INNER_PADDING;
        int contentY = modeTabs.y;
        int contentWidth = CONTENT_WIDTH;

        // Mode options section (shown when mode tab selected)
        modeOptionsSection = createModeOptionsSection(contentWidth, SPINNER_HEIGHT);
        modeOptionsSection.x = contentX;
        modeOptionsSection.y = contentY;
        modeOptionsSection.width = contentWidth;
        modeOptionsSection.layout();
        modeOptionsSection.recalculateBounds();
        wdgets.add(modeOptionsSection);

        // Tools section (shown when tools tab selected)
        toolsSection = createToolsSection();
        toolsSection.x = contentX;
        toolsSection.y = contentY;
        toolsSection.width = contentWidth;
        toolsSection.layout();
        toolsSection.recalculateBounds();
        wdgets.add(toolsSection);

        // Add tabs LAST so section widgets get click priority over tabs
        wdgets.add(modeTabs);

        // Register mode-specific widget visibility
        registerModeSpecificWidgets();
    }

    // Helper to register widgets with their applicable modes
    private void registerModeWidgets(EnumSet<WandProps.Mode> modes, Wdgt... widgets) {
        for (Wdgt w : widgets) {
            modeWidgets.put(w, modes);
        }
    }

    void update_selections(){
        if(wandItem !=null && wandStack!=null) {
            ItemStack actualWand = getPlayerHeldWand();
            WandProps.Mode currentMode = WandProps.getMode(actualWand);

            // Find mode index in the flat modes array
            int modeIndex = -1;
            if (modesArray != null) {
                for (int i = 0; i < modesArray.length; i++) {
                    if (modesArray[i] == currentMode) {
                        modeIndex = i;
                        break;
                    }
                }
            }

            // Update tab selection and section visibility
            int toolsTabIndex = modeTabs.size() - 1;  // Tools tab is last

            if (isToolsTabSelected) {
                // Tools tab is selected - highlight it, show tools section
                modeTabs.selected = toolsTabIndex;
                modeOptionsSection.visible = false;
                toolsSection.visible = true;
            } else {
                // Mode tab is selected - highlight it, show mode options
                modeTabs.selected = modeIndex;
                modeOptionsSection.visible = true;
                toolsSection.visible = false;
            }

            // Update mode-specific widget visibility using the map
            modeWidgets.forEach((widget, modes) -> widget.visible = modes.contains(currentMode));

            // Update action select visibility and filter based on mode
            boolean configDisabledDestroyReplace = WandsMod.config.disable_destroy_replace;
            final WandProps.Mode mode = currentMode;  // For lambda capture
            actionCycle.withFilter(action -> {
                // Check if action applies to current mode
                if (!WandProps.actionAppliesTo(action, mode)) return false;
                // Check if config disables destroy/replace
                if (configDisabledDestroyReplace &&
                    (action == WandProps.Action.DESTROY || action == WandProps.Action.REPLACE)) {
                    return false;
                }
                return true;
            });
            actionCycle.visible = actionCycle.hasAvailableOptions();
            if (actionCycle.visible) {
                actionCycle.validateSelection();
            }

            // Block state only relevant for Place action and modes that use state_mode
            WandProps.Action currentAction = WandProps.getAction(actualWand);
            blockStateCycle.visible = (currentAction == WandProps.Action.PLACE) && WandProps.stateModeAppliesTo(currentMode);

            // Drop position only relevant for Destroy/Replace actions
            dropPositionToggle.visible = (currentAction == WandProps.Action.DESTROY || currentAction == WandProps.Action.REPLACE);

            if (blockStateCycle.visible) {
                // Filter block state options - show axis variants only when relevant
                boolean isHollowFill = (currentMode == WandProps.Mode.FILL && !WandProps.getFlag(actualWand, WandProps.Flag.RFILLED));
                boolean isPillarBlock = ClientRender.wand != null && ClientRender.wand.hasPillarBlock();
                boolean showAllAxisOptions = isPillarBlock || isHollowFill;
                blockStateCycle.withFilter(idx -> {
                    // Clone and Normal always available
                    if (idx == WandProps.BLOCK_STATE_CLONE || idx == WandProps.BLOCK_STATE_NORMAL) return true;
                    // Apply-Y and Flip-Y always available (default axis for slabs/stairs)
                    if (idx == WandProps.BLOCK_STATE_APPLY_Y || idx == WandProps.BLOCK_STATE_FLIP_Y) return true;
                    // X/Z axis variants only when pillar block or hollow fill
                    return showAllAxisOptions;
                });
                // Use simplified labels ("Rotate", "Flip") when only Y-axis options available
                blockStateCycle.setUseAlternateLabels(!showAllAxisOptions);

                // Dynamic tooltips based on context
                blockStateCycle.setTooltipProvider(idx -> {
                    return buildBlockStateTooltip(idx, isPillarBlock, isHollowFill);
                });

                blockStateCycle.validateSelection();
            }
        }
    }
    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        if(showInventory) {
            #if MC_VERSION >= 12111
            RenderSystem.outputColorTextureOverride=wandInventoryTexture;
            #else
            RenderSystem.setShaderTexture(0, INV_TEX.res);
            #endif
            int inventoryX = (width - imageWidth) / 2;
            int inventoryY = (height - imageHeight) / 2;
            //gui.blit(RenderPipelines.GUI_TEXTURED, INV_TEX, inventoryX, inventoryY, 0, 0, imageWidth, imageHeight, 256, 256);
            Compat.blit(gui,INV_TEX, inventoryX, inventoryY, 0, 0, imageWidth, imageHeight, 256, 256);
            super.render(gui, mouseX, mouseY, delta);
            if(ClientRender.wand != null && ClientRender.wand.player_data != null){
                #if MC_VERSION >= 12111
                    int[] Tools= ClientRender.wand.player_data.getIntArray("Tools").orElse(empty_tools);
                #else
                    int[] Tools= ClientRender.wand.player_data.getIntArray("Tools");
                #endif

                for (int toolSlotIndex : Tools) {
                    Slot slot = this.menu.slots.get(toolSlotIndex);
                    int slotScreenX = slot.x + this.leftPos;
                    int slotScreenY = slot.y + this.topPos;
                    gui.fillGradient(slotScreenX, slotScreenY, slotScreenX + 16, slotScreenY + 16, 0x8800AA00, 0x1000AA00);
                }
            }
            // Position 4px below title (titleLabelY=6, font height ~9, so y = 6 + 9 + 4 = 19)
            int instructionY = topPos + titleLabelY + font.lineHeight + 16;
            gui.drawString(font, "Click an inventory slot to have ", leftPos + titleLabelX, instructionY, COLOR_TEXT_DARK, false);
            gui.drawString(font, "the wand use a tool in that slot", leftPos + titleLabelX, instructionY + font.lineHeight, COLOR_TEXT_DARK, false);

        }else{
            // Draw semi-transparent background panel (extends to bottom with same margin as top/sides)
            // Tabs have no left/right inner padding, section has INNER_PADDING
            Section visibleContent = isToolsTabSelected ? toolsSection : modeOptionsSection;
            int panelX = modeTabs.x - INNER_PADDING;
            int panelY = modeTabs.y - INNER_PADDING;
            int panelRight = visibleContent.x + visibleContent.width + INNER_PADDING;
            int panelBottom = this.height - SCREEN_MARGIN;
            gui.fill(panelX, panelY, panelRight, panelBottom, COLOR_PANEL_BACKGROUND);

            // Draw divider line between tabs and content
            if (SHOW_TAB_DIVIDER) {
                int lineX = modeTabs.x + modeTabs.width + INNER_PADDING;
                if (USE_EXTENDED_DIVIDER) {
                    // Extended divider: fills from lineX to right edge, full panel height
                    // Slightly lighter gray (0x484848) with 30% opacity (0x4D)
                    gui.fill(lineX, panelY, panelRight, panelBottom, 0x4D484848);
                } else {
                    gui.fill(lineX, modeTabs.y, lineX + DIVIDER_LINE_WIDTH, modeTabs.y + modeTabs.height, COLOR_TAB_DIVIDER);
                }
            }

            update_selections();
            for (Wdgt wdget : wdgets) {
                if (wdget.visible) {
                        wdget.render(gui, this.font, mouseX, mouseY);
                }
            }

            if(WandProps.getMode(getPlayerHeldWand()) == WandProps.Mode.ROCK) {
                gui.drawString(font, rockMessage, leftPos + 103, topPos + 62, 0x00ff0000, true);
            }

            // Render widget tooltips
            Wdgt hoveredWidget = findHoveredWidget(mouseX, mouseY);
            if (hoveredWidget != null) {
                Wdgt.renderWidgetTooltip(gui, font, hoveredWidget, mouseX, mouseY, this.width, this.height);
            }
        }
        // Update cursor based on hover
        if (showInventory) {
            // In tool selection menu, show hand cursor only over inventory slots
            boolean shouldBeHand = isOverInventorySlot(mouseX, mouseY);
            if (shouldBeHand != isHandCursor) {
                isHandCursor = shouldBeHand;
                long window = Compat.getWindow();
                if (shouldBeHand) {
                    if (handCursor == 0) {
                        handCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
                    }
                    GLFW.glfwSetCursor(window, handCursor);
                } else {
                    GLFW.glfwSetCursor(window, 0);
                }
            }
        } else {
            updateCursor(mouseX, mouseY);
        }

        this.renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    public void onClose() {
        // Reset cursor and free native cursor handle
        if (isHandCursor) {
            GLFW.glfwSetCursor(Compat.getWindow(), 0);
            isHandCursor = false;
        }
        if (handCursor != 0) {
            GLFW.glfwDestroyCursor(handCursor);
            handCursor = 0;
        }
        super.onClose();
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics gui, float delta, int mouseX, int mouseY) {

    }

    #if MC_VERSION >= 12101
    @Override
    public void renderBackground(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
        // Only render darkened background when showing inventory, not for main wand screen
        if (showInventory) {
            super.renderBackground(gui, mouseX, mouseY, delta);
        }
    }
    #endif
    @Override
    #if MC_VERSION >= 12111
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl){
        int mx = (int) mouseButtonEvent.x();
        int my = (int) mouseButtonEvent.y();
    #else
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        int mx = (int) mouseX;
        int my = (int) mouseY;
    #endif
        if(!showInventory) {
            // Stop propagation after first widget handles the click
            for (Wdgt wdget : wdgets) {
                if (wdget.visible && wdget.click(mx, my)) {
                    return true;
                }
            }
        }else{
            #if MC_VERSION >= 12111
            super.mouseClicked(mouseButtonEvent,bl);
            #else
            super.mouseClicked(mouseX, mouseY, button);
            #endif
        }

        return true;
    }

    #if MC_VERSION >= 12111
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {

        if (!showInventory) {
            for (Wdgt wdget : wdgets) {
                if (wdget.visible && wdget.scroll((int) mouseX, (int) mouseY, scrollY)) {
                    return true;
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    #else
    #endif

    @Override
    #if MC_VERSION >= 12111
    public boolean keyPressed(KeyEvent keyEvent){
        int scancode=keyEvent.scancode();
    #else
    public boolean keyPressed(int scancode,int keysym, int k){
    #endif
        #if MC_VERSION >= 12111
        if ((WandsModClient.wand_menu_km.matches(keyEvent) || scancode==256) ) {
        #else
        if ((WandsModClient.wand_menu_km.matches(keysym,scancode) || scancode==256) ) {
        #endif
            if(showInventory) {
                showInventory = false;
            }else{
                onClose();
            }
            return true;
        }else {
            #if MC_VERSION >= 12111
            return super.keyPressed(keyEvent);
            #else
            return super.keyPressed(scancode,keysym,k);
            #endif
        }
    }
}
