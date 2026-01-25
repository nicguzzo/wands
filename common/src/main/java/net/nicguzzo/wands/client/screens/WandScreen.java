package net.nicguzzo.wands.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.screens.Screen;
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
import net.nicguzzo.wands.utils.Compat;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.client.gui.GuiGraphics;
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
    GpuTextureView wandInventoryTexture;
    static final int IMG_WIDTH = 256;
    static final int IMG_HEIGHT = 256;

    // ===== Color Constants =====
    // Panel/Layout colors
    public static final int COLOR_PANEL_BACKGROUND = 0xE61A1A1A;  // Semi-transparent black

    // Button state colors
    public static final int COLOR_BTN_HOVER = 0xE6666666;     // Medium gray
    public static final int COLOR_BTN_SELECTED = 0xE6666666;  // Same as hover for consistency
    public static final int COLOR_BTN_DISABLED = 0xB3B3B3B3;  // Light gray (Colorf 0.7, 0.7, 0.7, 0.7)

    // Text colors
    public static final int COLOR_TEXT_PRIMARY = 0xFFFFFFFF;   // White
    public static final int COLOR_TEXT_SECTION = COLOR_TEXT_PRIMARY;   // For section headers
    public static final int COLOR_WDGT_LABEL = 0xFFAAAAAA;     // Light gray for widget labels

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



    Component rockMessage  = Compat.literal("rotate for new rock");
    private static final Identifier INV_TEX = Compat.create_resource("textures/gui/inventory.png");
    // Mode textures
    private static final Identifier DIRECTION_TEX = Compat.create_resource("textures/gui/direction.png");
    private static final Identifier ROW_TEX = Compat.create_resource("textures/gui/row.png");
    private static final Identifier LINE_TEX = Compat.create_resource("textures/gui/line.png");
    private static final Identifier GRID_TEX = Compat.create_resource("textures/gui/grid.png");
    private static final Identifier CIRCLE_TEX = Compat.create_resource("textures/gui/circle.png");
    private static final Identifier SPHERE_TEX = Compat.create_resource("textures/gui/sphere.png");
    private static final Identifier FILL_TEX = Compat.create_resource("textures/gui/fill.png");
    private static final Identifier AREA_TEX = Compat.create_resource("textures/gui/area.png");
    private static final Identifier ROCK_TEX = Compat.create_resource("textures/gui/rock.png");
    private static final Identifier TUNNEL_TEX = Compat.create_resource("textures/gui/tunnel.png");
    private static final Identifier RECTANGLE_TEX = Compat.create_resource("textures/gui/rectangle.png");
    private static final Identifier COPY_TEX = Compat.create_resource("textures/gui/copy.png");
    private static final Identifier PASTE_TEX = Compat.create_resource("textures/gui/paste.png");
    private static final Identifier VEIN_TEX = Compat.create_resource("textures/gui/vein.png");
    private static final Identifier BLAST_TEX = Compat.create_resource("textures/gui/blast.png");
    private static final Identifier TOOLS_TEX = Compat.create_resource("textures/gui/tools.png");
    private static final Identifier CONFIG_TEX = Compat.create_resource("textures/gui/config.png");
    private static final Identifier SHAPES_TEX = Compat.create_resource("textures/gui/shapes.png");
    private static final Identifier SHAPES_3D_TEX = Compat.create_resource("textures/gui/3d_shapes.png");
    private static final int SPINNER_HEIGHT = 14;

    Vector<Wdgt> wdgets = new Vector<>();

    // Layout
    Section modeOptionsSection;
    Section toolsSection;

    // Mode Tabs
    Tabs modeTabs;
    boolean isToolsTabSelected = false;  // Track if Tools tab is selected
    int shapes2dTabIndex = -1;  // Index of the 2D Shapes parent tab
    int shapes3dTabIndex = -1;  // Index of the 3D Shapes parent tab
    WandProps.Mode[] shapes2dModesArray;  // Modes in 2D Shapes group
    WandProps.Mode[] shapes3dModesArray;  // Modes in 3D Shapes group (filtered by config)

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
    CycleToggle<Boolean> rectangleFillToggle;
    Spinner gridMSpinner;
    Spinner gridNSpinner;
    Spinner gridMSkipSpinner;
    Spinner gridNSkipSpinner;
    Spinner gridMOffsetSpinner;
    Spinner gridNOffsetSpinner;
    Spinner blastRadiusSpinner;
    Spinner tunnelWidthSpinner;
    Spinner tunnelHeightSpinner;
    Spinner tunnelDepthSpinner;
    Spinner tunnelOffsetXSpinner;
    Spinner tunnelOffsetYSpinner;
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
            long window = Minecraft.getInstance().getWindow().handle();
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
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        wandInventoryTexture=textureManager.getTexture(INV_TEX).getTextureView();
    }
    // Reset action to first valid action for the given mode
    private void resetActionForMode(WandProps.Mode mode) {
        ItemStack actualWand = getPlayerHeldWand();
        for (WandProps.Action action : WandProps.actions) {
            if (WandProps.actionAppliesTo(action, mode)) {
                if (WandsMod.config.disable_destroy_replace &&
                    (action == WandProps.Action.DESTROY || action == WandProps.Action.REPLACE)) {
                    continue;
                }
                WandProps.setAction(actualWand, action);
                return;
            }
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
        WandsModClient.send_wand(wand);
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

    /** Create boolean toggle with auto-computed translations and add to section */
    private CycleToggle<Boolean> addFlagToggle(Section section, WandProps.Flag flag, int w, String key) {
        Component label = Compat.translatable("screen.wands." + key);
        Component tooltip = Compat.translatable("tooltip.wands." + key);
        CycleToggle<Boolean> toggle = flagToggle(flag, w, label);
        toggle.withTooltip(label, tooltip);
        section.add(toggle);
        return toggle;
    }

    /** Create inverted boolean toggle (ON = flag false, OFF = flag true) with auto-computed translations */
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
        return toggle;
    }

    /** Create Spinner with auto-computed translations and add to section */
    private Spinner addValSpinner(Section section, Value val, int w, int h, String key) {
        Component label = Compat.translatable("screen.wands." + key);
        Component tooltip = Compat.translatable("tooltip.wands." + key);
        Spinner spinner = valSpinner(val, w, h, label);
        spinner.withTooltip(label, tooltip);
        section.add(spinner);
        return spinner;
    }

    // Helper for mode tab buttons (parent tabs - larger size)
    private Btn modeBtn(Identifier tex, WandProps.Mode mode, String titleKey, String descKey) {
        Btn btn = new Btn(tex, Tabs.TAB_SIZE, Tabs.TAB_ICON_SIZE, (mouseX, mouseY) -> {
            isToolsTabSelected = false;  // Deselect tools tab
            ItemStack actualWand = getPlayerHeldWand();
            WandProps.setMode(actualWand, mode);
            resetActionForMode(mode);
            WandsModClient.send_wand(actualWand);
        });
        btn.withTooltip(Compat.translatable(titleKey), Compat.translatable(descKey));
        return btn;
    }

    // Helper for subtab buttons
    private Btn subModeBtn(Identifier tex, WandProps.Mode mode, String titleKey, String descKey) {
        Btn btn = new Btn(tex, Tabs.TAB_SIZE, Tabs.TAB_ICON_SIZE, (mouseX, mouseY) -> {
            isToolsTabSelected = false;  // Deselect tools tab
            ItemStack actualWand = getPlayerHeldWand();
            WandProps.setMode(actualWand, mode);
            resetActionForMode(mode);
            WandsModClient.send_wand(actualWand);
        });
        btn.withTooltip(Compat.translatable(titleKey), Compat.translatable(descKey));
        return btn;
    }

    private Tabs createModeTabs() {
        Tabs tabs = new Tabs();

        // Tab layout:
        // 0: Direction (top-level)
        // 1: 2D Shapes (parent) -> Line, Circle, Row/Column, Area
        // 2: 3D Shapes (parent) -> Fill, Rectangle, Sphere, Grid, Rock, Blast, Vein
        // 3: Copy (top-level)
        // 4: Paste (top-level)
        // 5: Tools (top-level)

        // Direction at top
        tabs.add(modeBtn(DIRECTION_TEX, WandProps.Mode.DIRECTION, "wands.modes.direction", "tooltip.wands.mode.direction"));

        // === 2D Shapes group: Line, Circle, Row/Column, Area ===
        shapes2dModesArray = new WandProps.Mode[] {
            WandProps.Mode.LINE, WandProps.Mode.CIRCLE,
            WandProps.Mode.ROW_COL, WandProps.Mode.AREA
        };
        Btn shapes2dParent = new Btn(SHAPES_TEX, Tabs.TAB_SIZE, Tabs.TAB_ICON_SIZE, (mouseX, mouseY) -> {
            isToolsTabSelected = false;
            Tabs.TabEntry entry = tabs.getEntry(shapes2dTabIndex);
            if (entry != null) {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.Mode modeToActivate = shapes2dModesArray[entry.selectedSubTab];
                WandProps.setMode(actualWand, modeToActivate);
                resetActionForMode(modeToActivate);
                WandsModClient.send_wand(actualWand);
            }
        });
        shapes2dParent.withTooltip(Compat.translatable("wands.modes.2d_shapes"), Compat.translatable("tooltip.wands.mode.2d_shapes"));

        Btn[] shape2dSubTabs = {
            subModeBtn(LINE_TEX, WandProps.Mode.LINE, "wands.modes.line", "tooltip.wands.mode.line"),
            subModeBtn(CIRCLE_TEX, WandProps.Mode.CIRCLE, "wands.modes.circle", "tooltip.wands.mode.circle"),
            subModeBtn(ROW_TEX, WandProps.Mode.ROW_COL, "wands.modes.row_col", "tooltip.wands.mode.row_col"),
            subModeBtn(AREA_TEX, WandProps.Mode.AREA, "wands.modes.area", "tooltip.wands.mode.area"),
        };
        shapes2dTabIndex = tabs.addWithSubTabs(shapes2dParent, shape2dSubTabs);

        // === 3D Shapes group: Fill, Rectangle (Tunnel), Sphere, Grid, Rock, Blast, Vein ===
        // Note: Blast and Vein may be excluded based on wand capability and config settings
        boolean canBlast = wandItem.can_blast && WandsMod.config.enable_blast_mode;
        boolean canVein = WandsMod.config.enable_vein_mode;

        List<WandProps.Mode> shapes3dModesList = new ArrayList<>();
        List<Btn> shapes3dSubTabsList = new ArrayList<>();

        shapes3dModesList.add(WandProps.Mode.FILL);
        shapes3dSubTabsList.add(subModeBtn(FILL_TEX, WandProps.Mode.FILL, "wands.modes.fill", "tooltip.wands.mode.fill"));

        shapes3dModesList.add(WandProps.Mode.TUNNEL);
        shapes3dSubTabsList.add(subModeBtn(RECTANGLE_TEX, WandProps.Mode.TUNNEL, "wands.modes.rectangle", "tooltip.wands.mode.rectangle"));

        shapes3dModesList.add(WandProps.Mode.SPHERE);
        shapes3dSubTabsList.add(subModeBtn(SPHERE_TEX, WandProps.Mode.SPHERE, "wands.modes.sphere", "tooltip.wands.mode.sphere"));

        shapes3dModesList.add(WandProps.Mode.GRID);
        shapes3dSubTabsList.add(subModeBtn(GRID_TEX, WandProps.Mode.GRID, "wands.modes.grid", "tooltip.wands.mode.grid"));

        shapes3dModesList.add(WandProps.Mode.ROCK);
        shapes3dSubTabsList.add(subModeBtn(ROCK_TEX, WandProps.Mode.ROCK, "wands.modes.rock", "tooltip.wands.mode.rock"));

        if (canBlast) {
            shapes3dModesList.add(WandProps.Mode.BLAST);
            shapes3dSubTabsList.add(subModeBtn(BLAST_TEX, WandProps.Mode.BLAST, "wands.modes.blast", "tooltip.wands.mode.blast"));
        }

        if (canVein) {
            shapes3dModesList.add(WandProps.Mode.VEIN);
            shapes3dSubTabsList.add(subModeBtn(VEIN_TEX, WandProps.Mode.VEIN, "wands.modes.vein", "tooltip.wands.mode.vein"));
        }

        shapes3dModesArray = shapes3dModesList.toArray(new WandProps.Mode[0]);
        Btn[] shapes3dSubTabs = shapes3dSubTabsList.toArray(new Btn[0]);

        Btn shapes3dParent = new Btn(SHAPES_3D_TEX, Tabs.TAB_SIZE, Tabs.TAB_ICON_SIZE, (mouseX, mouseY) -> {
            isToolsTabSelected = false;
            Tabs.TabEntry entry = tabs.getEntry(shapes3dTabIndex);
            if (entry != null) {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.Mode modeToActivate = shapes3dModesArray[entry.selectedSubTab];
                WandProps.setMode(actualWand, modeToActivate);
                resetActionForMode(modeToActivate);
                WandsModClient.send_wand(actualWand);
            }
        });
        shapes3dParent.withTooltip(Compat.translatable("wands.modes.3d_shapes"), Compat.translatable("tooltip.wands.mode.3d_shapes"));

        shapes3dTabIndex = tabs.addWithSubTabs(shapes3dParent, shapes3dSubTabs);

        // Copy and Paste
        tabs.add(modeBtn(COPY_TEX, WandProps.Mode.COPY, "wands.modes.copy", "tooltip.wands.mode.copy"));
        tabs.add(modeBtn(PASTE_TEX, WandProps.Mode.PASTE, "wands.modes.paste", "tooltip.wands.mode.paste"));

        // Tools tab at the end
        Btn toolsTabBtn = new Btn(TOOLS_TEX, Tabs.TAB_SIZE, Tabs.TAB_ICON_SIZE, (mouseX, mouseY) -> {
            isToolsTabSelected = !isToolsTabSelected;
        });
        toolsTabBtn.withTooltip(Compat.translatable("screen.wands.tools"), Compat.translatable("tooltip.wands.tools_tab"));
        tabs.add(toolsTabBtn);

        return tabs;
    }

    private Section createToolsSection() {
        Section section = new Section(Compat.translatable("screen.wands.tools"));

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

        // Drop position toggle - on = drop on player, off = drop on block
        dropPositionToggle = CycleToggle.ofBoolean(Compat.translatable("screen.wands.drop_on"),
            () -> ClientRender.wand.drop_on_player,
            value -> {
                ClientRender.wand.drop_on_player = value;
                NetworkManager.sendToServer(new Networking.GlobalSettingsPacket(value));
            },
            "player", "block");
        dropPositionToggle.width = CONTENT_WIDTH;
        dropPositionToggle.withTooltip(Compat.translatable("screen.wands.drop_on"), Compat.translatable("tooltip.wands.drop_on_player"));
        section.add(dropPositionToggle);

        return section;
    }

    private Section createModeOptionsSection(int layoutColWidth, int spinnerHeight) {
        Section section = new Section(Compat.translatable("screen.wands.mode_options"));

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

        // Fill mode - use custom labels for rectangle
        rectangleFillToggle = CycleToggle.ofBoolean(Compat.translatable("screen.wands.filled"),
            () -> WandProps.getFlag(getPlayerHeldWand(), WandProps.Flag.RFILLED),
            value -> {
                ItemStack actualWand = getPlayerHeldWand();
                WandProps.setFlag(actualWand, WandProps.Flag.RFILLED, value);
                syncWand(actualWand);
            },
            "Solid", "Hollow");
        rectangleFillToggle.width = layoutColWidth;
        rectangleFillToggle.withTooltip(Compat.translatable("screen.wands.filled"), Compat.translatable("tooltip.wands.filled"));
        section.add(rectangleFillToggle);

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

        // Tunnel mode spinners
        tunnelWidthSpinner = addValSpinner(section, Value.TUNNEL_W, layoutColWidth, spinnerHeight, "tunnel_width");
        tunnelHeightSpinner = addValSpinner(section, Value.TUNNEL_H, layoutColWidth, spinnerHeight, "tunnel_height");
        tunnelDepthSpinner = addValSpinner(section, Value.TUNNEL_DEPTH, layoutColWidth, spinnerHeight, "tunnel_depth");
        tunnelOffsetXSpinner = addValSpinner(section, Value.TUNNEL_OX, layoutColWidth, spinnerHeight, "tunnel_offset_x");
        tunnelOffsetYSpinner = addValSpinner(section, Value.TUNNEL_OY, layoutColWidth, spinnerHeight, "tunnel_offset_y");

        // Include in selection (PASTE and TUNNEL modes)
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

        return section;
    }

    private void registerModeSpecificWidgets() {
        // Direction mode
        registerModeWidgets(EnumSet.of(WandProps.Mode.DIRECTION), multiplierSpinner, invertToggle);

        // Row/Col mode
        registerModeWidgets(EnumSet.of(WandProps.Mode.ROW_COL), rowColumnLimitSpinner, orientationCycle);

        // Area mode
        registerModeWidgets(EnumSet.of(WandProps.Mode.AREA), diagonalSpreadToggle, skipBlockSpinner);

        // Area and Vein modes
        registerModeWidgets(EnumSet.of(WandProps.Mode.AREA, WandProps.Mode.VEIN), areaLimitSpinner, matchStateToggle);

        // Circle mode
        registerModeWidgets(EnumSet.of(WandProps.Mode.CIRCLE), planeCycle, circleFillToggle, evenSizeToggle);

        // Fill mode
        registerModeWidgets(EnumSet.of(WandProps.Mode.FILL), rectangleFillToggle);

        // Grid mode
        registerModeWidgets(EnumSet.of(WandProps.Mode.GRID), gridMSpinner, gridNSpinner,
            gridMSkipSpinner, gridNSkipSpinner, gridMOffsetSpinner, gridNOffsetSpinner);

        // Paste mode
        registerModeWidgets(EnumSet.of(WandProps.Mode.PASTE), mirrorLRToggle, mirrorFBToggle);

        // Grid and Paste modes
        registerModeWidgets(EnumSet.of(WandProps.Mode.GRID, WandProps.Mode.PASTE), rotationCycle);

        // Modes with include block option
        registerModeWidgets(EnumSet.of(WandProps.Mode.PASTE, WandProps.Mode.TUNNEL,
            WandProps.Mode.LINE, WandProps.Mode.CIRCLE, WandProps.Mode.FILL), includeBlockToggle);

        // Tunnel mode
        registerModeWidgets(EnumSet.of(WandProps.Mode.TUNNEL), tunnelWidthSpinner, tunnelHeightSpinner,
            tunnelDepthSpinner, tunnelOffsetXSpinner, tunnelOffsetYSpinner);

        // Blast mode
        registerModeWidgets(EnumSet.of(WandProps.Mode.BLAST), blastRadiusSpinner);

        // Rock mode
        registerModeWidgets(EnumSet.of(WandProps.Mode.ROCK), rockRadiusSpinner, rockNoiseSpinner);

        // Target air modes
        registerModeWidgets(EnumSet.of(WandProps.Mode.ROW_COL, WandProps.Mode.GRID, WandProps.Mode.COPY,
            WandProps.Mode.PASTE, WandProps.Mode.TUNNEL, WandProps.Mode.ROCK,
            WandProps.Mode.LINE, WandProps.Mode.CIRCLE, WandProps.Mode.FILL, WandProps.Mode.SPHERE), targetAirSpinner);

        // Keep start point - 2-click modes where P1 reuse makes sense
        registerModeWidgets(EnumSet.of(WandProps.Mode.LINE, WandProps.Mode.CIRCLE, WandProps.Mode.FILL,
            WandProps.Mode.SPHERE, WandProps.Mode.COPY, WandProps.Mode.PASTE), keepStartToggle);
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
        // tabs + INNER_PADDING + line + INNER_PADDING
        int contentX = modeTabs.x + Tabs.TAB_SIZE + INNER_PADDING + DIVIDER_LINE_WIDTH + INNER_PADDING;
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

            // Calculate the mode selection index and handle sub-tabs
            // Tab indices: 0=Direction, 1=2D Shapes(parent), 2=3D Shapes(parent),
            //              3=Copy, 4=Paste, 5=Tools
            int modeIndex = -1;

            // Check if mode is in 2D Shapes group
            int shapes2dSubTabIndex = -1;
            if (shapes2dModesArray != null) {
                for (int i = 0; i < shapes2dModesArray.length; i++) {
                    if (shapes2dModesArray[i] == currentMode) {
                        shapes2dSubTabIndex = i;
                        break;
                    }
                }
            }

            // Check if mode is in 3D Shapes group
            int shapes3dSubTabIndex = -1;
            if (shapes3dModesArray != null) {
                for (int i = 0; i < shapes3dModesArray.length; i++) {
                    if (shapes3dModesArray[i] == currentMode) {
                        shapes3dSubTabIndex = i;
                        break;
                    }
                }
            }

            if (shapes2dSubTabIndex >= 0) {
                // Current mode is in 2D Shapes group
                modeIndex = shapes2dTabIndex;

                Tabs.TabEntry entry = modeTabs.getEntry(shapes2dTabIndex);
                if (entry != null) {
                    entry.selectedSubTab = shapes2dSubTabIndex;
                }
            } else if (shapes3dSubTabIndex >= 0) {
                // Current mode is in 3D Shapes group
                modeIndex = shapes3dTabIndex;

                Tabs.TabEntry entry = modeTabs.getEntry(shapes3dTabIndex);
                if (entry != null) {
                    entry.selectedSubTab = shapes3dSubTabIndex;
                }
            } else {
                // Top-level mode
                switch (currentMode) {
                    case DIRECTION: modeIndex = 0; break;
                    case COPY: modeIndex = 3; break;
                    case PASTE: modeIndex = 4; break;
                    default: break;
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

            // Update mode options section title to show current mode name
            modeOptionsSection.setTitle(Compat.translatable(currentMode.toString()));

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
            RenderSystem.outputColorTextureOverride=wandInventoryTexture;
            int inventoryX = (width - imageWidth) / 2;
            int inventoryY = (height - imageHeight) / 2;
            gui.blit(RenderPipelines.GUI_TEXTURED, INV_TEX, inventoryX, inventoryY, 0, 0, imageWidth, imageHeight, 256, 256);
            super.render(gui, mouseX, mouseY, delta);
            if(ClientRender.wand != null && ClientRender.wand.player_data != null && ClientRender.wand.player_data.getIntArray("Tools").isPresent()){
                for (int toolSlotIndex : ClientRender.wand.player_data.getIntArray("Tools").get()) {
                    Slot slot = this.menu.slots.get(toolSlotIndex);
                    int slotScreenX = slot.x + this.leftPos;
                    int slotScreenY = slot.y + this.topPos;
                    gui.fillGradient(slotScreenX, slotScreenY, slotScreenX + 16, slotScreenY + 16, 0x8800AA00, 0x1000AA00);
                }
            }
            gui.drawString(font, "click on a player inventory slot", leftPos + 3, topPos + 50, 0xffffffff, true);
            gui.drawString(font, "to mark it to be used by the wand", leftPos + 3, topPos + 62, 0xffffffff, true);

        }else{
            // Update tab expansion animation
            modeTabs.updateAnimation(delta);

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
                int lineX = modeTabs.x + Tabs.TAB_SIZE + INNER_PADDING;
                gui.fill(lineX, modeTabs.y, lineX + DIVIDER_LINE_WIDTH, modeTabs.y + modeTabs.height, COLOR_TAB_DIVIDER);
            }

            update_selections();
            for (Wdgt wdget : wdgets) {
                if (wdget.visible) {
                        wdget.render(gui, this.font, mouseX, mouseY);
                }
            }

            // Render hotbar slots
            int hotbarBaseX = ((width - IMG_WIDTH)/2 + 48) - screenXOffset;
            int hotbarY = (((height - IMG_HEIGHT) / 2) + 22) - screenYOffset;
            for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
                Slot hotbarSlot = this.menu.slots.get(36 + slotIndex);
                int slotX = hotbarBaseX + slotIndex * 18;
                gui.renderFakeItem(hotbarSlot.getItem(), slotX, hotbarY);
                gui.renderItemDecorations(font, hotbarSlot.getItem(), slotX, hotbarY);
                if (mouseX > slotX && mouseX < slotX + 16 && mouseY > hotbarY && mouseY < hotbarY + 16) {
                    this.hoveredSlot = hotbarSlot;
                }
            }
            if(WandProps.getMode(getPlayerHeldWand()) == WandProps.Mode.ROCK) {
                gui.drawString(font, rockMessage, leftPos + 103, topPos + 62, 0x00ff0000, true);
            }

            // Render widget tooltips
            Wdgt hoveredWidget = findHoveredWidget(mouseX, mouseY);
            if (hoveredWidget != null) {
                Wdgt.renderWidgetTooltip(gui, font, hoveredWidget, mouseX, mouseY, this.width);
            }
        }
        // Update cursor based on hover
        if (!showInventory) {
            updateCursor(mouseX, mouseY);
        }

        this.renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    public void onClose() {
        // Reset cursor and free native cursor handle
        if (isHandCursor) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().handle(), 0);
            isHandCursor = false;
        }
        if (handCursor != 0) {
            GLFW.glfwDestroyCursor(handCursor);
            handCursor = 0;
        }
        super.onClose();
    }

    @Override
    protected void renderBg(GuiGraphics gui, float f, int i, int j) {
    }

    @Override
    public void renderBackground(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        // Only render darkened background when showing inventory, not for main wand screen
        if (showInventory) {
            super.renderBackground(gui, mouseX, mouseY, delta);
        }
    }
    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl)
    {
        if(!showInventory) {
            int mx = (int) mouseButtonEvent.x();
            int my = (int) mouseButtonEvent.y();

            // Stop propagation after first widget handles the click
            for (Wdgt wdget : wdgets) {
                if (wdget.visible && wdget.click(mx, my)) {
                    return true;
                }
            }
        }else{
            super.mouseClicked( mouseButtonEvent,bl);
        }

        return true;
    }

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

    @Override
    //public boolean keyPressed(int i, int j, int k)
    public boolean keyPressed(KeyEvent keyEvent)
    {
        int i=keyEvent.scancode();
        if ((WandsModClient.wand_menu_km.matches(keyEvent) || i==256) ) {
            if(showInventory) {
                showInventory = false;
            }else{
                onClose();
            }
            return true;
        }else {
            return super.keyPressed(keyEvent);
        }
    }
}
