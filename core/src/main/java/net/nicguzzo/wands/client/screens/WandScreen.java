package net.nicguzzo.wands.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
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

import net.nicguzzo.wands.client.gui.Section;
import net.nicguzzo.wands.client.gui.Spinner;
import net.nicguzzo.wands.client.gui.Tabs;
import net.nicguzzo.wands.client.gui.Divider;
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
    private static final MyIdExt INV_TEX = new MyIdExt(WandsMod.MOD_ID,"textures/gui/inventory.png");
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
    public static final int COLOR_TAB_DIVIDER = 0xFF444444;  // Dark gray line
    private static final int DIVIDER_LINE_WIDTH = 1;          // Divider line thickness in pixels

    // EXPERIMENTAL: Extended divider that fills from tabs to right edge with widget background color
    private static final boolean USE_EXTENDED_DIVIDER = true;

    private static final int SPINNER_HEIGHT = 14;

    Vector<Wdgt> wdgets = new Vector<>();

    // Layout
    Section modeOptionsSection;
    Section toolsSection;

    // Mode Tabs
    Tabs modeTabs;
    boolean isToolsTabSelected = false;  // Track if Tools tab is selected

    // Block State Section - independent controls
    CycleToggle<WandProps.StateMode> stateModeCycle;
    CycleToggle<Boolean> blockFlipToggle;
    CycleToggle<Rotation> blockFacingCycle;
    CycleToggle<Direction.Axis> blockAxisCycle;

    // Tools Section
    CycleToggle<Integer> replaceModeCycle;
    CycleToggle<Boolean> dropPositionToggle;
    Btn showInventoryButton;
    boolean showInventory = false;
    Btn configButton;

    // Mode Options Section
    CycleToggle<WandProps.Action> actionCycle;
    CycleToggle<Boolean> targetAirToggle;
    Spinner reachDistanceSpinner;
    CycleToggle<Boolean> mirrorLRToggle;
    CycleToggle<Boolean> mirrorFBToggle;
    CycleToggle<Rotation> rotationCycle;
    Spinner rockRadiusSpinner;
    Spinner rockNoiseSpinner;
    Spinner multiplierSpinner;
    CycleToggle<Boolean> invertToggle;
    CycleToggle<Boolean> boxInvertToggle;
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
    Divider sectionDivider;

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
        if (WandsModClient.openToolsTab) {
            isToolsTabSelected = true;
            showInventory = true;
            WandsModClient.openToolsTab = false;
        }
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
    private CycleToggle<Boolean> makeFlagToggle(WandProps.Flag flag, int w, String key) {
        return makeFlagToggle(flag, w, key, null);
    }

    /** Create boolean toggle with keybind hint in the tooltip */
    private CycleToggle<Boolean> makeFlagToggle(WandProps.Flag flag, int w, String key, WandsMod.WandKeys wandKey) {
        Component label = Compat.translatable("screen.wands." + key);
        Component tooltip = Compat.translatable("tooltip.wands." + key);
        CycleToggle<Boolean> toggle = flagToggle(flag, w, label);
        toggle.withTooltip(label, tooltip);
        // Auto-register mode visibility from FLAG_MODES
        EnumSet<WandProps.Mode> modes = WandProps.FLAG_MODES.get(flag);
        if (modes != null) {
            modeWidgets.put(toggle, modes);
        }
        if (wandKey != null) {
            toggle.keybindHint = WandsModClient.getKeyName(wandKey);
        }
        return toggle;
    }

    /** Create inverted boolean toggle (ON = flag false, OFF = flag true) with auto-computed translations and auto-register */
    private CycleToggle<Boolean> makeInvertedFlagToggle(WandProps.Flag flag, int w, String key) {
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
        // Auto-register mode visibility from FLAG_MODES
        EnumSet<WandProps.Mode> modes = WandProps.FLAG_MODES.get(flag);
        if (modes != null) {
            modeWidgets.put(toggle, modes);
        }
        return toggle;
    }

    /** Create Spinner with auto-computed translations and auto-register mode visibility */
    private Spinner makeValSpinner(Value val, int w, int h, String key) {
        Component label = Compat.translatable("screen.wands." + key);
        Component tooltip = Compat.translatable("tooltip.wands." + key);
        Spinner spinner = valSpinner(val, w, h, label);
        spinner.withTooltip(label, tooltip);
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
            WandProps.switchMode(actualWand, mode);
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

        btn = modeTextBtn(WandProps.Mode.ROW_COL, "wands.modes.row_col", "tooltip.wands.mode.row_col");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.ROW_COL);

        btn = modeTextBtn(WandProps.Mode.FILL, "wands.modes.fill", "tooltip.wands.mode.fill");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.FILL);

        btn = modeTextBtn(WandProps.Mode.AREA, "wands.modes.area", "tooltip.wands.mode.area");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.AREA);

        btn = modeTextBtn(WandProps.Mode.BOX, "wands.modes.box", "tooltip.wands.mode.box");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.BOX);

        btn = modeTextBtn(WandProps.Mode.GRID, "wands.modes.grid", "tooltip.wands.mode.grid");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.GRID);

        btn = modeTextBtn(WandProps.Mode.LINE, "wands.modes.line", "tooltip.wands.mode.line");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.LINE);

        btn = modeTextBtn(WandProps.Mode.CIRCLE, "wands.modes.circle", "tooltip.wands.mode.circle");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.CIRCLE);

        btn = modeTextBtn(WandProps.Mode.ROCK, "wands.modes.rock", "tooltip.wands.mode.rock");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.ROCK);

        btn = modeTextBtn(WandProps.Mode.SPHERE, "wands.modes.sphere", "tooltip.wands.mode.sphere");
        tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.SPHERE);

        if (canVein) {
            btn = modeTextBtn(WandProps.Mode.VEIN, "wands.modes.vein", "tooltip.wands.mode.vein");
            tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.VEIN);
        }

        if (canBlast) {
            btn = modeTextBtn(WandProps.Mode.BLAST, "wands.modes.blast", "tooltip.wands.mode.blast");
            tabs.add(btn); textButtons.add(btn); modesList.add(WandProps.Mode.BLAST);
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

        // ===== Create all widgets =====

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
        actionCycle.keybindHint = WandsModClient.getKeyName(WandsMod.WandKeys.ACTION);

        // Target Air toggle
        targetAirToggle = CycleToggle.ofBoolean(Compat.translatable("screen.wands.target_air"),
            () -> WandProps.getFlag(getPlayerHeldWand(), WandProps.Flag.TARGET_AIR),
            value -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setFlag(actualWand, WandProps.Flag.TARGET_AIR, value);
                syncWand(actualWand);
            });
        targetAirToggle.width = layoutColWidth;
        targetAirToggle.withTooltip(Compat.translatable("screen.wands.target_air"), Compat.translatable("tooltip.wands.target_air"));

        // Reach Distance spinner (all modes)
        reachDistanceSpinner = new Spinner(WandProps.getVal(wandStack, Value.REACH_DISTANCE), Value.REACH_DISTANCE.min, Value.REACH_DISTANCE.max, layoutColWidth, spinnerHeight, Compat.translatable("screen.wands.reach_distance"))
            .withOnChange(value -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setVal(actualWand, Value.REACH_DISTANCE, value);
                syncWand(actualWand);
            })
            .withValueFormatter(v -> v == 0 ? "Default" : "+" + v);
        reachDistanceSpinner.withTooltip(Compat.translatable("screen.wands.reach_distance"), Compat.translatable("tooltip.wands.reach_distance"));

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
            .withTooltips("tooltip.wands.area_rot_0", "tooltip.wands.area_rot_90", "tooltip.wands.area_rot_180", "tooltip.wands.area_rot_270");
        rotationCycle.width = layoutColWidth;
        rotationCycle.keybindHint = WandsModClient.getKeyName(WandsMod.WandKeys.ROTATE);

        // Match state (AREA, VEIN modes)
        matchStateToggle = makeFlagToggle(WandProps.Flag.MATCHSTATE, layoutColWidth, "match_state");

        // Include selected block in selection
        includeBlockToggle = makeFlagToggle(WandProps.Flag.INCSELBLOCK, layoutColWidth, "include_block", WandsMod.WandKeys.INC_SEL_BLK);

        // Keep start point (inverted: ON = don't clear P1, OFF = clear P1)
        keepStartToggle = makeInvertedFlagToggle(WandProps.Flag.CLEAR_P1, layoutColWidth, "keep_start");

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
        boxFillToggle.keybindHint = WandsModClient.getKeyName(WandsMod.WandKeys.FILL);

        // Area/Vein mode limit
        areaLimitSpinner = makeValSpinner(Value.AREALIM, layoutColWidth, spinnerHeight, "limit");

        // Block State controls - independent state mode, flip, block rotation, and axis
        // State mode cycle: Clone / Adjust / Auto
        WandProps.StateMode[] stateModeOptions = { WandProps.StateMode.CLONE, WandProps.StateMode.APPLY, WandProps.StateMode.TARGET };
        Component[] stateModeLabels = {
            Compat.translatable("screen.wands.state_mode.clone"),
            Compat.translatable("screen.wands.state_mode.adjust"),
            Compat.translatable("screen.wands.state_mode.auto")
        };
        stateModeCycle = new CycleToggle<>(Compat.translatable("screen.wands.state_mode_prefix"),
            stateModeOptions, stateModeLabels,
            () -> {
                WandProps.StateMode sm = WandProps.getStateMode(getPlayerHeldWand());
                // Map APPLY_FLIP to APPLY (= Adjust) for display
                if (sm == WandProps.StateMode.APPLY_FLIP) return WandProps.StateMode.APPLY;
                return sm;
            },
            (sm) -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setStateMode(actualWand, sm);
                syncWand(actualWand);
            })
            .withTooltips("tooltip.wands.state_mode.clone", "tooltip.wands.state_mode.adjust", "tooltip.wands.state_mode.auto");
        stateModeCycle.width = layoutColWidth;

        // Flip toggle: Bottom / Top (controls APPLY vs APPLY_FLIP)
        blockFlipToggle = CycleToggle.ofBoolean(Compat.translatable("screen.wands.block_flip"),
            () -> WandProps.getStateMode(getPlayerHeldWand()) == WandProps.StateMode.APPLY_FLIP,
            value -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setStateMode(actualWand, value ? WandProps.StateMode.APPLY_FLIP : WandProps.StateMode.APPLY);
                syncWand(actualWand);
            },
            "Bottom", "Top");
        blockFlipToggle.width = layoutColWidth;
        blockFlipToggle.withTooltips("tooltip.wands.block_flip.bottom", "tooltip.wands.block_flip.top");

        // Block rotation cycle: 0° / 90° / 180° / 270° (independent of area rotation)
        Component[] blockRotLabels = {
            Compat.translatable("screen.wands.rot_0"),
            Compat.translatable("screen.wands.rot_90"),
            Compat.translatable("screen.wands.rot_180"),
            Compat.translatable("screen.wands.rot_270")
        };
        blockFacingCycle = new CycleToggle<>(Compat.translatable("screen.wands.block_facing_prefix"),
            WandProps.rotations, blockRotLabels,
            () -> WandProps.getBlockRotation(getPlayerHeldWand()),
            (r) -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setBlockRotation(actualWand, r);
                syncWand(actualWand);
            })
            .withTooltips("tooltip.wands.block_facing.0", "tooltip.wands.block_facing.90", "tooltip.wands.block_facing.180", "tooltip.wands.block_facing.270");
        blockFacingCycle.width = layoutColWidth;

        // Axis cycle: X / Y / Z (for pillar blocks and hollow fill)
        Component[] axisLabels = {
            Compat.literal("X"),
            Compat.literal("Y"),
            Compat.literal("Z")
        };
        blockAxisCycle = new CycleToggle<>(Compat.translatable("screen.wands.axis"),
            WandProps.axes, axisLabels,
            () -> WandProps.getAxis(getPlayerHeldWand()),
            (a) -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setAxis(actualWand, a);
                syncWand(actualWand);
            })
            .withTooltips("tooltip.wands.axis_x", "tooltip.wands.axis_y", "tooltip.wands.axis_z");
        blockAxisCycle.width = layoutColWidth;

        // Replace mode cycle - only shown for PLACE action
        Integer[] replaceModeValues = {0, 1, 2};
        Component[] replaceModeLabels = {
            Compat.literal("None"),
            Compat.literal("Replaceable"),
            Compat.literal("All")
        };
        replaceModeCycle = new CycleToggle<>(Compat.translatable("screen.wands.replace_blocks"),
            replaceModeValues, replaceModeLabels,
            () -> WandProps.getVal(getPlayerHeldWand(), Value.REPLACE_MODE),
            (v) -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setVal(actualWand, Value.REPLACE_MODE, v);
                syncWand(actualWand);
            })
            .withTooltips("tooltip.wands.replace_blocks.none", "tooltip.wands.replace_blocks.replaceable", "tooltip.wands.replace_blocks.all");
        replaceModeCycle.width = layoutColWidth;

        // Drop position toggle - only shown for DESTROY/REPLACE actions
        dropPositionToggle = CycleToggle.ofBoolean(Compat.translatable("screen.wands.drop_on"),
            () -> ClientRender.wand.drop_on_player,
            value -> {
                ClientRender.wand.drop_on_player = value;
                Networking.SendGlobalSettings(value);
            },
            "player", "block");
        dropPositionToggle.width = layoutColWidth;
        dropPositionToggle.withTooltip(Compat.translatable("screen.wands.drop_on"), Compat.translatable("tooltip.wands.drop_on_player"));

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

        mirrorFBToggle = CycleToggle.ofBoolean(Compat.translatable("screen.wands.mirror_fb"),
            () -> WandProps.getVal(getPlayerHeldWand(), Value.MIRRORAXIS) == 2,
            value -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setVal(actualWand, Value.MIRRORAXIS, value ? 2 : 0);
                syncWand(actualWand);
            });
        mirrorFBToggle.width = layoutColWidth;
        mirrorFBToggle.withTooltip(Compat.translatable("screen.wands.mirror_fb"), Compat.translatable("tooltip.wands.mirror_fb"));

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
        orientationCycle.keybindHint = WandsModClient.getKeyName(WandsMod.WandKeys.ORIENTATION);

        // Row/Col mode limit spinner
        rowColumnLimitSpinner = makeValSpinner(Value.ROWCOLLIM, layoutColWidth, spinnerHeight, "limit");

        // Direction mode
        multiplierSpinner = makeValSpinner(Value.MULTIPLIER, layoutColWidth, spinnerHeight, "multiplier");
        invertToggle = makeFlagToggle(WandProps.Flag.INVERTED, layoutColWidth, "invert", WandsMod.WandKeys.INVERT);

        // Box mode
        boxInvertToggle = makeFlagToggle(WandProps.Flag.BOX_INVERTED, layoutColWidth, "box_invert", WandsMod.WandKeys.INVERT);

        // Area mode
        diagonalSpreadToggle = makeFlagToggle(WandProps.Flag.DIAGSPREAD, layoutColWidth, "orthogonal_only", WandsMod.WandKeys.DIAGONAL_SPREAD);
        skipBlockSpinner = makeValSpinner(Value.SKIPBLOCK, layoutColWidth, spinnerHeight, "skip_block");

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
        planeCycle.keybindHint = WandsModClient.getKeyName(WandsMod.WandKeys.ORIENTATION);

        circleFillToggle = makeFlagToggle(WandProps.Flag.CFILLED, layoutColWidth, "filled", WandsMod.WandKeys.FILL);
        evenSizeToggle = makeFlagToggle(WandProps.Flag.EVEN, layoutColWidth, "even_size");

        // Grid mode spinners - M is rows/horizontal, N is columns/vertical
        gridMSpinner = new Spinner(WandProps.getVal(wandStack, Value.GRIDM), 1, wandItem.limit, layoutColWidth, spinnerHeight, Compat.translatable("screen.wands.grid_m"))
            .withOnChange(value -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setGridVal(actualWand, Value.GRIDM, value, wandItem.limit);
                syncWand(actualWand);
            });
        gridMSpinner.withTooltip(Compat.translatable("screen.wands.grid_m"), Compat.translatable("tooltip.wands.grid_m"));

        gridNSpinner = new Spinner(WandProps.getVal(wandStack, Value.GRIDN), 1, wandItem.limit, layoutColWidth, spinnerHeight, Compat.translatable("screen.wands.grid_n"))
            .withOnChange(value -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setGridVal(actualWand, Value.GRIDN, value, wandItem.limit);
                syncWand(actualWand);
            });
        gridNSpinner.withTooltip(Compat.translatable("screen.wands.grid_n"), Compat.translatable("tooltip.wands.grid_n"));

        gridMSkipSpinner = makeValSpinner(Value.GRIDMS, layoutColWidth, spinnerHeight, "grid_m_skip");
        gridNSkipSpinner = makeValSpinner(Value.GRIDNS, layoutColWidth, spinnerHeight, "grid_n_skip");
        gridMOffsetSpinner = makeValSpinner(Value.GRIDMOFF, layoutColWidth, spinnerHeight, "grid_m_offset");
        gridNOffsetSpinner = makeValSpinner(Value.GRIDNOFF, layoutColWidth, spinnerHeight, "grid_n_offset");

        // Rock mode spinners
        rockRadiusSpinner = makeValSpinner(Value.ROCK_RADIUS, layoutColWidth, spinnerHeight, "rock_radius");
        rockNoiseSpinner = makeValSpinner(Value.ROCK_NOISE, layoutColWidth, spinnerHeight, "rock_noise");

        // Blast mode
        blastRadiusSpinner = makeValSpinner(Value.BLASTRAD, layoutColWidth, spinnerHeight, "blast_radius");
        blastRadiusSpinner.incrementValue = 2;
        blastRadiusSpinner.shiftIncrementValue = 4;

        // Box mode spinners
        boxWidthSpinner = makeValSpinner(Value.BOX_W, layoutColWidth, spinnerHeight, "box_width");
        boxHeightSpinner = makeValSpinner(Value.BOX_H, layoutColWidth, spinnerHeight, "box_height");
        boxDepthSpinner = makeValSpinner(Value.BOX_DEPTH, layoutColWidth, spinnerHeight, "box_depth");
        boxOffsetXSpinner = makeValSpinner(Value.BOX_OX, layoutColWidth, spinnerHeight, "box_offset_x");
        boxOffsetYSpinner = makeValSpinner(Value.BOX_OY, layoutColWidth, spinnerHeight, "box_offset_y");

        // Divider between shared and mode-specific options
        sectionDivider = new Divider(layoutColWidth);

        // ===== Add widgets to section in display order =====

        // --- Shared options (used by 2+ modes) ---
        section.add(actionCycle);
        section.add(replaceModeCycle);          // shown/hidden by actionCycle (PLACE)
        section.add(dropPositionToggle);       // shown/hidden by actionCycle (DESTROY/REPLACE or PLACE+All)
        section.add(stateModeCycle);            // shown/hidden by actionCycle (PLACE)
        section.add(blockFlipToggle);           // shown/hidden by stateModeCycle
        section.add(blockFacingCycle);        // shown/hidden by stateModeCycle
        section.add(blockAxisCycle);            // shown/hidden by stateModeCycle
        section.add(targetAirToggle);
        section.add(reachDistanceSpinner);
        section.add(rotationCycle);
        section.add(matchStateToggle);
        section.add(includeBlockToggle);
        section.add(keepStartToggle);
        section.add(areaLimitSpinner);

        // --- Divider ---
        section.add(sectionDivider);

        // --- Mode-specific options (unique to 1 mode) ---
        section.add(mirrorLRToggle);
        section.add(mirrorFBToggle);
        section.add(orientationCycle);
        section.add(rowColumnLimitSpinner);
        section.add(multiplierSpinner);
        section.add(invertToggle);
        section.add(boxFillToggle);
        section.add(diagonalSpreadToggle);
        section.add(skipBlockSpinner);
        section.add(planeCycle);
        section.add(circleFillToggle);
        section.add(evenSizeToggle);
        section.add(gridMSpinner);
        section.add(gridNSpinner);
        section.add(gridMSkipSpinner);
        section.add(gridNSkipSpinner);
        section.add(gridMOffsetSpinner);
        section.add(gridNOffsetSpinner);
        section.add(rockRadiusSpinner);
        section.add(rockNoiseSpinner);
        section.add(blastRadiusSpinner);
        section.add(boxInvertToggle);
        section.add(boxWidthSpinner);
        section.add(boxHeightSpinner);
        section.add(boxDepthSpinner);
        section.add(boxOffsetXSpinner);
        section.add(boxOffsetYSpinner);

        return section;
    }

    private void registerModeSpecificWidgets() {
        // Most widgets are auto-registered by addFlagToggle/addValSpinner helpers.
        // Only manually register widgets created without those helpers:

        // Cycle toggles not in FLAG_MODES/VALUE_MODES
        registerModeWidgets(EnumSet.of(WandProps.Mode.ROW_COL), orientationCycle);
        registerModeWidgets(EnumSet.of(WandProps.Mode.CIRCLE), planeCycle);
        registerModeWidgets(EnumSet.of(WandProps.Mode.GRID, WandProps.Mode.PASTE), rotationCycle);

        // Replace mode cycle - visible for modes that support PLACE action
        registerModeWidgets(WandProps.VALUE_MODES.get(WandProps.Value.REPLACE_MODE), replaceModeCycle);

        // Block state controls - visible for all state_mode-applicable modes
        registerModeWidgets(WandProps.STATE_MODE_MODES, stateModeCycle, blockFlipToggle,
            blockFacingCycle, blockAxisCycle);

        // Target air toggle (air-capable modes only)
        registerModeWidgets(WandProps.FLAG_MODES.get(WandProps.Flag.TARGET_AIR), targetAirToggle);

        // Reach distance spinner (all modes)
        registerModeWidgets(WandProps.VALUE_MODES.get(WandProps.Value.REACH_DISTANCE), reachDistanceSpinner);

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

            // Show divider only when there are mode-specific widgets visible below it
            sectionDivider.visible = mirrorLRToggle.visible || mirrorFBToggle.visible
                || orientationCycle.visible || rowColumnLimitSpinner.visible
                || multiplierSpinner.visible || invertToggle.visible
                || boxFillToggle.visible || boxInvertToggle.visible
                || diagonalSpreadToggle.visible || skipBlockSpinner.visible
                || planeCycle.visible || circleFillToggle.visible || evenSizeToggle.visible
                || gridMSpinner.visible || rockRadiusSpinner.visible
                || blastRadiusSpinner.visible || boxWidthSpinner.visible;

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

            // Block state controls - only relevant for Place action and modes that use state_mode
            WandProps.Action currentAction = WandProps.getAction(actualWand);
            boolean showStateControls = (currentAction == WandProps.Action.PLACE || currentAction == WandProps.Action.REPLACE)
                && WandProps.stateModeAppliesTo(currentMode);
            WandProps.StateMode stateMode = WandProps.getStateMode(actualWand);
            boolean isAdjustMode = (stateMode == WandProps.StateMode.APPLY
                || stateMode == WandProps.StateMode.APPLY_FLIP);

            stateModeCycle.visible = showStateControls;
            blockFlipToggle.visible = showStateControls && isAdjustMode;
            blockFacingCycle.visible = showStateControls && isAdjustMode;

            boolean isHollowFill = (currentMode == WandProps.Mode.FILL && !WandProps.getFlag(actualWand, WandProps.Flag.RFILLED));
            boolean isPillarBlock = ClientRender.wand != null && ClientRender.wand.hasPillarBlock();
            blockAxisCycle.visible = showStateControls && isAdjustMode && (isPillarBlock || isHollowFill);

            // Replace mode cycle only visible for PLACE action
            replaceModeCycle.visible = replaceModeCycle.visible && (currentAction == WandProps.Action.PLACE);

            // Drop position relevant for Destroy/Replace actions, or PLACE with replace_mode==All
            int currentReplaceMode = WandProps.getVal(actualWand, Value.REPLACE_MODE);
            dropPositionToggle.visible = (currentAction == WandProps.Action.DESTROY || currentAction == WandProps.Action.REPLACE)
                || (currentAction == WandProps.Action.PLACE && currentReplaceMode == 2 && replaceModeCycle.visible);
        }
    }
    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        if(showInventory) {
            #if MC_VERSION >= 12111
            RenderSystem.outputColorTextureOverride=wandInventoryTexture;
            #else
            RenderSystem.setShaderTexture(0, INV_TEX.res);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
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

            // Render widget tooltips using vanilla tooltip rendering
            Wdgt hoveredWidget = findHoveredWidget(mouseX, mouseY);
            if (hoveredWidget != null) {
                Compat.renderComponentTooltip(gui, font, hoveredWidget.getTooltipLines(), mouseX, mouseY);
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
        // Intentionally empty: MC 1.21.1 draws a dark overlay here that darkens
        // the inventory texture. The wand screen handles its own background rendering.
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
