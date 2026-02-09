package net.nicguzzo.wands.client;


import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import io.netty.buffer.Unpooled;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.compat.MyIdExt;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.networking.Networking;
import net.nicguzzo.compat.Compat;
import net.nicguzzo.wands.utils.WandUtils;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandProps;
import net.nicguzzo.wands.client.gui.Section;
import net.nicguzzo.wands.client.screens.WandScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class WandsModClient {
    static boolean shift = false;
    static boolean alt = false;
    // Track which keys were pressed last tick for edge detection
    private static final Set<Integer> prevKeyState = new HashSet<>();
    public static boolean has_optifine = false;
    public static boolean has_opac = false;
    public static KeyMapping wand_menu_km;
    public static boolean openToolsTab = false;

    public static final Logger LOGGER = LogManager.getLogger();
    static final public Map<KeyMapping, WandsMod.WandKeys> keys = new HashMap<KeyMapping, WandsMod.WandKeys>();
    static final public Map<WandsMod.WandKeys, KeyMapping> reverseKeys = new HashMap<>();
    static final public int wand_menu_key = GLFW.GLFW_KEY_Y;// InputConstants.KEY_Y;
    static final public int wand_mode_key = GLFW.GLFW_KEY_V;//InputConstants.KEY_V;
    static final public int wand_action_key = GLFW.GLFW_KEY_H;//InputConstants.KEY_H;
    static final public int wand_orientation_key = GLFW.GLFW_KEY_X;//InputConstants.KEY_X;
    static final public int wand_undo_key = GLFW.GLFW_KEY_U;//InputConstants.KEY_U;
    static final public int wand_invert_key = GLFW.GLFW_KEY_I;//InputConstants.KEY_I;
    static final public int wand_fill_circle_key = GLFW.GLFW_KEY_K;//InputConstants.KEY_K;
    static final public int wand_rotate = GLFW.GLFW_KEY_R;//InputConstants.KEY_R;
    static final public int palette_mode_key = GLFW.GLFW_KEY_P;//InputConstants.KEY_P;
    static final public int palette_menu_key = GLFW.GLFW_KEY_J;//InputConstants.KEY_J;
    static final public int wand_conf_key = -1;
    static final public int wand_m_inc_key = GLFW.GLFW_KEY_RIGHT;//InputConstants.KEY_RIGHT;
    static final public int wand_m_dec_key = GLFW.GLFW_KEY_LEFT;//InputConstants.KEY_LEFT;
    static final public int wand_n_inc_key = GLFW.GLFW_KEY_UP;//InputConstants.KEY_UP;
    static final public int wand_n_dec_key = GLFW.GLFW_KEY_DOWN;//InputConstants.KEY_DOWN;
    static final public int toggle_stair_slab_key = GLFW.GLFW_KEY_PERIOD;//InputConstants.KEY_PERIOD;
    static final public int area_diagonal_spread = GLFW.GLFW_KEY_COMMA;//InputConstants.KEY_COMMA;
    static final public int inc_sel_block = GLFW.GLFW_KEY_Z;//InputConstants.KEY_Z;
    static final public int anchor_key = GLFW.GLFW_KEY_G;
    #if MC_VERSION >= 12111
    public static final KeyMapping.Category tab = KeyMapping.Category.register(new MyIdExt(WandsMod.MOD_ID, "wands").res);
    #else
    static final String tab = "key.categories.wands";
    #endif
    static final String k = "key.wands.";

    public static void initialize() {

        wand_menu_km = Compat.newKeyMapping(k + "wand_menu", wand_menu_key, tab);
        keys.put(wand_menu_km, WandsMod.WandKeys.MENU);
        keys.put(Compat.newKeyMapping(k + "wand_mode", wand_mode_key, tab), WandsMod.WandKeys.MODE);
        keys.put(Compat.newKeyMapping(k + "palette_menu", palette_menu_key, tab), WandsMod.WandKeys.PALETTE_MENU);
        keys.put(Compat.newKeyMapping(k + "wand_action", wand_action_key, tab), WandsMod.WandKeys.ACTION);
        keys.put(Compat.newKeyMapping(k + "wand_orientation", wand_orientation_key, tab), WandsMod.WandKeys.ORIENTATION);
        keys.put(Compat.newKeyMapping(k + "wand_invert", wand_invert_key, tab), WandsMod.WandKeys.INVERT);
        keys.put(Compat.newKeyMapping(k + "wand_fill_circle", wand_fill_circle_key, tab), WandsMod.WandKeys.FILL);
        keys.put(Compat.newKeyMapping(k + "wand_undo", wand_undo_key, tab), WandsMod.WandKeys.UNDO);
        keys.put(Compat.newKeyMapping(k + "wand_palette_mode", palette_mode_key, tab), WandsMod.WandKeys.PALETTE_MODE);
        keys.put(Compat.newKeyMapping(k + "wand_rotate", wand_rotate, tab), WandsMod.WandKeys.ROTATE);
        keys.put(Compat.newKeyMapping(k + "m_inc", wand_m_inc_key, tab), WandsMod.WandKeys.M_INC);
        keys.put(Compat.newKeyMapping(k + "m_dec", wand_m_dec_key, tab), WandsMod.WandKeys.M_DEC);
        keys.put(Compat.newKeyMapping(k + "n_inc", wand_n_inc_key, tab), WandsMod.WandKeys.N_INC);
        keys.put(Compat.newKeyMapping(k + "n_dec", wand_n_dec_key, tab), WandsMod.WandKeys.N_DEC);
        keys.put(Compat.newKeyMapping(k + "toggle_stair_slab", toggle_stair_slab_key, tab), WandsMod.WandKeys.TOGGLE_STAIRSLAB);
        keys.put(Compat.newKeyMapping(k + "area_diagonal_spread", area_diagonal_spread, tab), WandsMod.WandKeys.DIAGONAL_SPREAD);
        keys.put(Compat.newKeyMapping(k + "inc_sel_block", inc_sel_block, tab), WandsMod.WandKeys.INC_SEL_BLK);
        keys.put(Compat.newKeyMapping(k + "clear_wand", GLFW.GLFW_KEY_C, tab), WandsMod.WandKeys.CLEAR);
        keys.put(Compat.newKeyMapping(k + "anchor", anchor_key, tab), WandsMod.WandKeys.ANCHOR);

        keys.forEach((km, v) -> Compat.register_key((KeyMapping) km));

        // Build reverse lookup: WandKeys -> KeyMapping
        keys.forEach((km, v) -> reverseKeys.put((WandsMod.WandKeys) v, (KeyMapping) km));

        ClientTickEvent.CLIENT_POST.register(e -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;

            ItemStack mainHand = client.player.getMainHandItem();
            boolean holdingWand = mainHand != null && !mainHand.isEmpty() && mainHand.getItem() instanceof WandItem;
            boolean holdingPalette = mainHand != null && !mainHand.isEmpty() && mainHand.getItem() instanceof net.nicguzzo.wands.items.PaletteItem;
            boolean holdingOffhandPalette = false;
            ItemStack offHand = client.player.getOffhandItem();
            if (offHand != null && !offHand.isEmpty() && offHand.getItem() instanceof net.nicguzzo.wands.items.PaletteItem) {
                holdingOffhandPalette = true;
            }
            boolean useRawInput = holdingWand || holdingPalette || holdingOffhandPalette;

            boolean any = false;

            if (useRawInput && client.screen == null) {
                // Bypass KeyMapping conflicts: poll GLFW directly for wand keys
                long window = Compat.getWindow();
                for (Map.Entry<KeyMapping, WandsMod.WandKeys> me : keys.entrySet()) {
                    KeyMapping km = me.getKey();
                    WandsMod.WandKeys key = me.getValue();
                    int keyCode = Compat.getKeyCode(km);
                    if (keyCode <= 0) continue; // Unbound

                    boolean pressed = GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS;
                    boolean wasPressed = prevKeyState.contains(keyCode);

                    if (pressed && !wasPressed) {
                        // New press detected
                        if (!any) any = true;

                        if (key == WandsMod.WandKeys.MENU && Compat.hasShiftDown()) {
                            openToolsTab = true;
                        }

                        // Try anchor handling first — if consumed, don't send to server
                        boolean consumed = false;
                        if (holdingWand && ClientRender.wand != null) {
                            consumed = handleAnchorKey(client, mainHand, key, Compat.hasShiftDown());
                        }

                        if (!consumed) {
                            if (key == WandsMod.WandKeys.CLEAR) {
                                cancel_wand();
                            } else {
                                Networking.send_key(key.ordinal(), Compat.hasShiftDown(), Compat.hasAltDown());
                            }
                        }
                        if (key == WandsMod.WandKeys.ROTATE) {
                            if (ClientRender.wand != null && ClientRender.wand.mode == WandProps.Mode.ROCK) {
                                ClientRender.wand.get_mode().randomize();
                            }
                        }
                    }
                }

                // Update key state tracking
                prevKeyState.clear();
                for (Map.Entry<KeyMapping, WandsMod.WandKeys> me : keys.entrySet()) {
                    int keyCode = Compat.getKeyCode(me.getKey());
                    if (keyCode > 0 && GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS) {
                        prevKeyState.add(keyCode);
                    }
                }

                // Drain any pending clicks so conflicting bindings don't also fire
                for (KeyMapping km : keys.keySet()) {
                    while (km.consumeClick()) {}
                }
            } else {
                // Normal KeyMapping path when not holding wand
                prevKeyState.clear();
                for (Map.Entry<KeyMapping, WandsMod.WandKeys> me : keys.entrySet()) {
                    KeyMapping km = me.getKey();
                    WandsMod.WandKeys key = me.getValue();
                    if (km.consumeClick()) {
                        if (!any) any = true;
                        if (key == WandsMod.WandKeys.MENU && Compat.hasShiftDown()) {
                            openToolsTab = true;
                        }
                        if (key == WandsMod.WandKeys.CLEAR) {
                            cancel_wand();
                        } else {
                            Networking.send_key(key.ordinal(), Compat.hasShiftDown(), Compat.hasAltDown());
                        }
                        if (key == WandsMod.WandKeys.ROTATE) {
                            if (ClientRender.wand != null && ClientRender.wand.mode == WandProps.Mode.ROCK) {
                                ClientRender.wand.get_mode().randomize();
                            }
                        }
                    }
                }
            }

            if (!any) {
                boolean newAlt = Compat.hasAltDown();
                boolean newShift = Compat.hasShiftDown();
                if (alt != newAlt || shift != newShift) {
                    // Handle alt freeze/release via anchor system
                    if (alt != newAlt && holdingWand && ClientRender.wand != null) {
                        if (newAlt) {
                            // Alt pressed — freeze at crosshair if no toggle anchor active
                            WandProps.Mode mode = WandProps.getMode(mainHand);
                            ClientRender.wand.anchor.freeze(client.hitResult, mainHand, mode);
                        } else {
                            // Alt released — release non-persistent freeze
                            ClientRender.wand.anchor.release();
                        }
                    }
                    alt = newAlt;
                    shift = newShift;
                    if (ClientRender.wand != null) {
                        ClientRender.wand.is_shift_pressed = shift;
                    }
                    boolean frozen = ClientRender.wand != null && ClientRender.wand.anchor.isActive();
                    Networking.send_key(-1, shift, frozen);
                }
            }
        });

        //Compat.render_info();
        ClientGuiEvent.RENDER_HUD.register((e, d) -> {
            WandsModClient.render_wand_info(e);
            ModeInstructionOverlay.render(e);
        });
        Networking.RegisterReceiversS2C();
    }

    /** Get a short display name for a keybind (e.g. "V", "H", "Right") */
    public static String getKeyName(WandsMod.WandKeys key) {
        KeyMapping km = reverseKeys.get(key);
        if (km == null) return "?";
        String name = km.getTranslatedKeyMessage().getString();
        // Strip common suffixes for shorter display
        if (name.endsWith(" Key")) {
            name = name.substring(0, name.length() - 4);
        }
        // Map common long names to short forms
        switch (name) {
            case "Right Arrow": return "Right";
            case "Left Arrow": return "Left";
            case "Up Arrow": return "Up";
            case "Down Arrow": return "Down";
        }
        return name;
    }

    public static void render_wand_info(GuiGraphics gui) {
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.player != null) {
            ItemStack stack = client.player.getMainHandItem();
            ItemStack offhand_stack = client.player.getOffhandItem();
            boolean main = stack != null && !stack.isEmpty() && stack.getItem() instanceof WandItem;
            boolean main_bag = stack != null && !stack.isEmpty() && stack.getItem() instanceof MagicBagItem;
            boolean off_bag = offhand_stack != null && !offhand_stack.isEmpty() && offhand_stack.getItem() instanceof MagicBagItem;
            int screenWidth = client.getWindow().getGuiScaledWidth();
            int screenHeight = client.getWindow().getGuiScaledHeight();

            if (main_bag || off_bag || main) {
                if (main_bag || off_bag) {
                    Font font = client.font;
                    int h = 3 * font.lineHeight;
                    float x = (int) (screenWidth * ((WandsMod.config.wand_mode_display_x_pos) / 100.0f));
                    float y = (int) ((screenHeight - h) * ((WandsMod.config.wand_mode_display_y_pos) / 100.0f));
                    ItemStack s = main_bag ? stack : offhand_stack;
                    ItemStack bgi = MagicBagItem.getItem(s, client.level);
                    int y_off = 0;
                    if (main) {
                        y_off = -font.lineHeight * 3;
                    }
                    gui.drawString(font, "Item: " + Component.translatable(bgi.getItem().getDescriptionId()).getString(), (int) x, (int) y + y_off + font.lineHeight, 0xffffffff);
                    gui.drawString(font, "Total: " + MagicBagItem.getTotal(s), (int) x, (int) y + y_off + font.lineHeight * 2, 0xffffffff);
                }
                if (main) {
                    Font font = client.font;
                    Wand wand = ClientRender.wand;
                    WandProps.Mode mode = WandProps.getMode(stack);
                    WandProps.Action action = WandProps.getAction(stack);
                    Rotation r = WandProps.getRotation(stack);
                    String rot = "";
                    switch (r) {
                        case NONE:
                            rot = "0°";
                            break;
                        case CLOCKWISE_90:
                            rot = "90°";
                            break;
                        case CLOCKWISE_180:
                            rot = "180°";
                            break;
                        case COUNTERCLOCKWISE_90:
                            rot = "270°";
                            break;
                    }
                    boolean showP1P2 = mode == WandProps.Mode.COPY;
                    boolean showAction = WandProps.hasMultipleActions(mode);
                    boolean showRotation = WandProps.rotationAppliesTo(mode);

                    String modeStr = Compat.translatable(mode.toString()).getString();
                    String actionStr = showAction ? Compat.translatable(action.toString()).getString() : "";
                    String modeKey = getKeyName(WandsMod.WandKeys.MODE);
                    String actionKey = getKeyName(WandsMod.WandKeys.ACTION);
                    String rotateKey = getKeyName(WandsMod.WandKeys.ROTATE);

                    String p1Val = "";
                    String p2Val = "";
                    if (showP1P2) {
                        BlockPos bp1 = wand.getP1();
                        BlockPos bp2 = wand.getP2();
                        if (wand.getP1() != null) {
                            p1Val = bp1.getX() + ", " + bp1.getY() + ", " + bp1.getZ();
                        }
                        if (wand.getP2() != null) {
                            p2Val = bp2.getX() + ", " + bp2.getY() + ", " + bp2.getZ();
                        } else {
                            if (wand.getP1() != null) {
                                p2Val = ClientRender.last_pos.getX() + ", " + ClientRender.last_pos.getY() + ", " + ClientRender.last_pos.getZ();
                            }
                        }
                    }

                    String rotText = "";
                    if (showRotation) {
                        rotText = "Rotation " + rot + " [" + rotateKey + "]";
                    }

                    // Build orientation/plane text
                    String orientKey = getKeyName(WandsMod.WandKeys.ORIENTATION);
                    String orientText = "";
                    switch (mode) {
                        case CIRCLE:
                            orientText = Compat.translatable("screen.wands.plane").getString() + " " + WandProps.getPlane(stack) + " [" + orientKey + "]";
                            break;
                        case ROW_COL:
                            orientText = Compat.translatable(WandProps.getOrientation(stack).toString()).getString() + " [" + orientKey + "]";
                            break;
                    }

                    // Build P1/P2 text (separate lines)
                    String p1Text = "";
                    String p2Text = "";
                    if (showP1P2) {
                        if (!p1Val.isEmpty()) {
                            p1Text = "P1: " + p1Val;
                        }
                        if (!p2Val.isEmpty()) {
                            p2Text = "P2: " + p2Val;
                        }
                    }

                    // Build mode-specific info text
                    String infoText = "";
                    if (wand.valid) {
                        switch (mode) {
                            case DIRECTION:
                                int mult = WandProps.getVal(stack, WandProps.Value.MULTIPLIER);
                                infoText = "pos: " + wand.pos.getX() + ", " + wand.pos.getY() + ", " + wand.pos.getZ() + " x" + mult;
                                break;
                            case GRID:
                                int gm = WandProps.getVal(stack, WandProps.Value.GRIDM);
                                int gn = WandProps.getVal(stack, WandProps.Value.GRIDN);
                                int gms = WandProps.getVal(stack, WandProps.Value.GRIDMS);
                                int gns = WandProps.getVal(stack, WandProps.Value.GRIDNS);
                                infoText = "Grid " + gm + "x" + gn;
                                if (gms > 0 || gns > 0) {
                                    infoText += " Skip " + gms + "x" + gns;
                                }
                                break;
                            case FILL:
                                int nx = wand.fill_nx + 1;
                                int ny = wand.fill_ny + 1;
                                int nz = wand.fill_nz + 1;
                                infoText = "Volume " + nx + "x" + ny + "x" + nz + " Blocks: " + wand.block_buffer.get_length();
                                break;
                            case ROW_COL:
                                int rowcollim = WandProps.getVal(stack, WandProps.Value.ROWCOLLIM);
                                infoText = "Blocks: " + wand.block_buffer.get_length();
                                if (rowcollim > 0) {
                                    infoText += " Limit: " + rowcollim;
                                }
                                break;
                            case LINE:
                                infoText = "Blocks: " + wand.block_buffer.get_length();
                                break;
                            case AREA:
                            case VEIN:
                                int arealim = WandProps.getVal(stack, WandProps.Value.AREALIM);
                                infoText = "Blocks: " + wand.block_buffer.get_length();
                                if (arealim > 0) {
                                    infoText += " Limit: " + arealim;
                                }
                                break;
                            case CIRCLE:
                            case SPHERE:
                                infoText = "Radius: " + wand.radius + " N: " + wand.block_buffer.get_length();
                                break;
                            case COPY:
                            case PASTE:
                                infoText = "Copied Blocks: " + wand.copy_paste_buffer.size();
                                break;
                            case BOX:
                                int tw = WandProps.getVal(stack, WandProps.Value.BOX_W);
                                int th = WandProps.getVal(stack, WandProps.Value.BOX_H);
                                int td = WandProps.getVal(stack, WandProps.Value.BOX_DEPTH);
                                infoText = "Size " + tw + "x" + th + " Depth: " + td;
                                break;
                            case BLAST:
                                int blastRad = WandProps.getVal(stack, WandProps.Value.BLASTRAD);
                                infoText = "Radius: " + blastRad;
                                break;
                            case ROCK:
                                int rockRad = WandProps.getVal(stack, WandProps.Value.ROCK_RADIUS);
                                int rockNoise = WandProps.getVal(stack, WandProps.Value.ROCK_NOISE);
                                infoText = "Radius: " + rockRad + " Noise: " + rockNoise;
                                break;
                        }
                    }

                    // Undo line (creative only)
                    boolean showUndo = client.player.isCreative();
                    String undoKey = showUndo ? getKeyName(WandsMod.WandKeys.UNDO) : "";
                    String undoText = showUndo ? "Undo [" + undoKey + "]" : "";

                    int pad = WandScreen.SCREEN_MARGIN;
                    int lineSpacing = font.lineHeight + Section.VERTICAL_SPACING;

                    // Count lines — each item gets its own row
                    int lineCount = 1; // mode
                    if (showAction) lineCount++;
                    if (!orientText.isEmpty()) lineCount++;
                    if (!rotText.isEmpty()) lineCount++;
                    if (!p1Text.isEmpty()) lineCount++;
                    if (!p2Text.isEmpty()) lineCount++;
                    if (!infoText.isEmpty()) lineCount++;
                    if (showUndo) lineCount++;

                    // Calculate max width across all lines
                    String modeText = modeStr + " [" + modeKey + "]";
                    String actionText = showAction ? actionStr + " [" + actionKey + "]" : "";
                    int maxWidth = font.width(modeText);
                    if (showAction) maxWidth = Math.max(maxWidth, font.width(actionText));
                    if (!orientText.isEmpty()) maxWidth = Math.max(maxWidth, font.width(orientText));
                    if (!rotText.isEmpty()) maxWidth = Math.max(maxWidth, font.width(rotText));
                    if (!p1Text.isEmpty()) maxWidth = Math.max(maxWidth, font.width(p1Text));
                    if (!p2Text.isEmpty()) maxWidth = Math.max(maxWidth, font.width(p2Text));
                    if (!infoText.isEmpty()) maxWidth = Math.max(maxWidth, font.width(infoText));
                    if (showUndo) maxWidth = Math.max(maxWidth, font.width(undoText));

                    int contentHeight = lineCount * font.lineHeight + (lineCount - 1) * Section.VERTICAL_SPACING;
                    int contentWidth = maxWidth;

                    int hudX = pad * 2 + (int) ((screenWidth - contentWidth - pad * 4) * (WandsMod.config.wand_mode_display_x_pos / 100.0f));
                    int hudY = pad * 2 + (int) ((screenHeight - contentHeight - pad * 4) * (WandsMod.config.wand_mode_display_y_pos / 100.0f));

                    gui.fill(hudX - pad, hudY - pad, hudX + contentWidth + pad, hudY + contentHeight + pad, WandScreen.COLOR_PANEL_BACKGROUND);

                    int currentY = hudY;

                    // Mode [V]
                    drawHudValueWithHint(gui, font, modeStr, modeKey, hudX, currentY);
                    currentY += lineSpacing;

                    // Action [H]
                    if (showAction) {
                        drawHudValueWithHint(gui, font, actionStr, actionKey, hudX, currentY);
                        currentY += lineSpacing;
                    }

                    // Orientation/Plane [X]
                    if (!orientText.isEmpty()) {
                        switch (mode) {
                            case CIRCLE:
                                drawHudValueWithHint(gui, font, Compat.translatable("screen.wands.plane").getString() + " " + WandProps.getPlane(stack), orientKey, hudX, currentY);
                                break;
                            case ROW_COL:
                                drawHudValueWithHint(gui, font, Compat.translatable(WandProps.getOrientation(stack).toString()).getString(), orientKey, hudX, currentY);
                                break;
                        }
                        currentY += lineSpacing;
                    }

                    // Rotation [R]
                    if (showRotation) {
                        drawHudValueWithHint(gui, font, "Rotation " + rot, rotateKey, hudX, currentY);
                        currentY += lineSpacing;
                    }

                    // P1
                    if (!p1Text.isEmpty()) {
                        drawHudLabelValue(gui, font, "P1: ", p1Val, hudX, currentY);
                        currentY += lineSpacing;
                    }
                    // P2
                    if (!p2Text.isEmpty()) {
                        drawHudLabelValue(gui, font, "P2: ", p2Val, hudX, currentY);
                        currentY += lineSpacing;
                    }

                    // Mode-specific info
                    if (wand.valid && !infoText.isEmpty()) {
                        int lineX = hudX;
                        switch (mode) {
                            case DIRECTION:
                                int mult = WandProps.getVal(stack, WandProps.Value.MULTIPLIER);
                                lineX += drawHudLabelValue(gui, font, "pos: ", wand.pos.getX() + ", " + wand.pos.getY() + ", " + wand.pos.getZ(), lineX, currentY);
                                drawHudLabelValue(gui, font, " x", String.valueOf(mult), lineX, currentY);
                                break;
                            case GRID:
                                int gm = WandProps.getVal(stack, WandProps.Value.GRIDM);
                                int gn = WandProps.getVal(stack, WandProps.Value.GRIDN);
                                int gms = WandProps.getVal(stack, WandProps.Value.GRIDMS);
                                int gns = WandProps.getVal(stack, WandProps.Value.GRIDNS);
                                lineX += drawHudLabelValue(gui, font, "Grid ", gm + "x" + gn, lineX, currentY);
                                if (gms > 0 || gns > 0) {
                                    drawHudLabelValue(gui, font, " Skip ", gms + "x" + gns, lineX, currentY);
                                }
                                break;
                            case FILL:
                                int nx = wand.fill_nx + 1;
                                int ny = wand.fill_ny + 1;
                                int nz = wand.fill_nz + 1;
                                lineX += drawHudLabelValue(gui, font, "Volume ", nx + "x" + ny + "x" + nz, lineX, currentY);
                                lineX += font.width(" ");
                                drawHudLabelValue(gui, font, "Blocks: ", String.valueOf(wand.block_buffer.get_length()), lineX, currentY);
                                break;
                            case ROW_COL:
                                int rowcollim = WandProps.getVal(stack, WandProps.Value.ROWCOLLIM);
                                lineX += drawHudLabelValue(gui, font, "Blocks: ", String.valueOf(wand.block_buffer.get_length()), lineX, currentY);
                                if (rowcollim > 0) {
                                    drawHudLabelValue(gui, font, " Limit: ", String.valueOf(rowcollim), lineX, currentY);
                                }
                                break;
                            case LINE:
                                drawHudLabelValue(gui, font, "Blocks: ", String.valueOf(wand.block_buffer.get_length()), lineX, currentY);
                                break;
                            case AREA:
                            case VEIN:
                                int arealim = WandProps.getVal(stack, WandProps.Value.AREALIM);
                                lineX += drawHudLabelValue(gui, font, "Blocks: ", String.valueOf(wand.block_buffer.get_length()), lineX, currentY);
                                if (arealim > 0) {
                                    drawHudLabelValue(gui, font, " Limit: ", String.valueOf(arealim), lineX, currentY);
                                }
                                break;
                            case CIRCLE:
                            case SPHERE:
                                lineX += drawHudLabelValue(gui, font, "Radius: ", String.valueOf(wand.radius), lineX, currentY);
                                drawHudLabelValue(gui, font, " N: ", String.valueOf(wand.block_buffer.get_length()), lineX, currentY);
                                break;
                            case COPY:
                            case PASTE:
                                drawHudLabelValue(gui, font, "Copied Blocks: ", String.valueOf(wand.copy_paste_buffer.size()), lineX, currentY);
                                break;
                            case BOX:
                                int tw = WandProps.getVal(stack, WandProps.Value.BOX_W);
                                int th = WandProps.getVal(stack, WandProps.Value.BOX_H);
                                int td = WandProps.getVal(stack, WandProps.Value.BOX_DEPTH);
                                lineX += drawHudLabelValue(gui, font, "Size ", tw + "x" + th, lineX, currentY);
                                drawHudLabelValue(gui, font, " Depth: ", String.valueOf(td), lineX, currentY);
                                break;
                            case BLAST:
                                int blastRad = WandProps.getVal(stack, WandProps.Value.BLASTRAD);
                                drawHudLabelValue(gui, font, "Radius: ", String.valueOf(blastRad), lineX, currentY);
                                break;
                            case ROCK:
                                int rockRad = WandProps.getVal(stack, WandProps.Value.ROCK_RADIUS);
                                int rockNoise = WandProps.getVal(stack, WandProps.Value.ROCK_NOISE);
                                lineX += drawHudLabelValue(gui, font, "Radius: ", String.valueOf(rockRad), lineX, currentY);
                                drawHudLabelValue(gui, font, " Noise: ", String.valueOf(rockNoise), lineX, currentY);
                                break;
                        }
                        currentY += lineSpacing;
                    }

                    // Undo [U]
                    if (showUndo) {
                        drawHudValueWithHint(gui, font, "Undo", undoKey, hudX, currentY);
                    }
                }
            }
        }
    }

    /** Draw HUD text with label in white and value in gray */
    private static int drawHudLabelValue(GuiGraphics gui, Font font, String label, String value, int x, int y) {
        gui.drawString(font, label, x, y, WandScreen.COLOR_TEXT_PRIMARY);
        int labelWidth = font.width(label);
        gui.drawString(font, value, x + labelWidth, y, WandScreen.COLOR_WDGT_LABEL);
        return labelWidth + font.width(value);
    }

    /** Draw HUD text - value only in white, returns width */
    private static int drawHudValue(GuiGraphics gui, Font font, String value, int x, int y) {
        gui.drawString(font, value, x, y, WandScreen.COLOR_TEXT_PRIMARY);
        return font.width(value);
    }

    /** Draw a keybind hint like [V] in dim gray, returns total width */
    private static int drawHudKeybindHint(GuiGraphics gui, Font font, String keyName, int x, int y) {
        String hint = "[" + keyName + "]";
        gui.drawString(font, hint, x, y, WandScreen.COLOR_WDGT_LABEL);
        return font.width(hint);
    }

    /** Draw value in white followed by keybind hint in gray with a space between, returns total width */
    private static int drawHudValueWithHint(GuiGraphics gui, Font font, String value, String keyName, int x, int y) {
        int w = 0;
        gui.drawString(font, value, x, y, WandScreen.COLOR_TEXT_PRIMARY);
        w += font.width(value);
        String hint = " [" + keyName + "]";
        gui.drawString(font, hint, x + w, y, WandScreen.COLOR_WDGT_LABEL);
        w += font.width(hint);
        return w;
    }

    public static void cancel_wand() {
        if (ClientRender.wand != null && ClientRender.wand.wand_stack != null && WandUtils.is_wand(ClientRender.wand.wand_stack)) {
            ClientRender.wand.clear(true);
            ClientRender.wand.anchor.clear();
            if (ClientRender.wand.player != null && !WandsMod.config.disable_info_messages) {
                ClientRender.wand.player.displayClientMessage(Compat.literal("Wand cleared"), true);
            }
        }
    }

    /**
     * Handle anchor key input. Returns true if the key was consumed (should NOT be sent to server).
     */
    private static boolean handleAnchorKey(Minecraft client, ItemStack mainHand, WandsMod.WandKeys key, boolean shift) {
        Wand wand = ClientRender.wand;
        if (wand == null) return false;

        WandProps.Mode mode = WandProps.getMode(mainHand);

        if (key == WandsMod.WandKeys.ANCHOR) {
            if (!wand.anchor.isSet() && !mode.supports_anchor()) {
                // Mode doesn't support anchor — consume the key but do nothing
                return true;
            }
            boolean wasSet = wand.anchor.isSet();
            wand.anchor.toggle(client.hitResult, mainHand, mode);
            if (client.player != null && !WandsMod.config.disable_info_messages) {
                String msgKey = wasSet ? "wands.message.anchor_cleared" : "wands.message.anchor_set";
                if (wand.anchor.isSet() || wasSet) {
                    client.player.displayClientMessage(Compat.translatable(msgKey), true);
                }
            }
            return true;
        }

        // Arrow keys: delegate to anchor.move()
        if (client.player == null) return false;
        return wand.anchor.move(key, shift, client.player.getDirection());
    }
}
