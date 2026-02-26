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
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.nicguzzo.compat.MyIdExt;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.config.WandsConfig.HudMode;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.items.PaletteItem;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
    static final public int pin_key = GLFW.GLFW_KEY_G;
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
        keys.put(Compat.newKeyMapping(k + "pin", pin_key, tab), WandsMod.WandKeys.PIN);

        keys.forEach((km, v) -> Compat.register_key((KeyMapping) km));

        // Build reverse lookup: WandKeys -> KeyMapping
        keys.forEach((km, v) -> reverseKeys.put((WandsMod.WandKeys) v, (KeyMapping) km));

        ClientTickEvent.CLIENT_POST.register(e -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;

            // Tick the mode selector grid (handles hold/tap/release for MODE key)
            ModeSelectorScreen.clientTick();

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
                        // Skip keys consumed by ModeSelector (MODE key, arrow keys while grid is open)
                        if (ModeSelectorScreen.consumesKey(key)) continue;

                        // New press detected
                        if (!any) any = true;

                        if (key == WandsMod.WandKeys.MENU && Compat.hasShiftDown()) {
                            openToolsTab = true;
                        }

                        // Try pin handling first — if consumed, don't send to server
                        boolean consumed = false;
                        if (holdingWand && ClientRender.wand != null) {
                            consumed = handlePinKey(client, mainHand, key, Compat.hasShiftDown());
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
                        // Skip keys consumed by ModeSelector
                        if (ModeSelectorScreen.consumesKey(key)) continue;
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
                    // Handle alt freeze/release via pin system
                    if (alt != newAlt && holdingWand && ClientRender.wand != null) {
                        if (newAlt) {
                            // Alt pressed — freeze at crosshair if no toggle pin active
                            WandProps.Mode mode = WandProps.getMode(mainHand);
                            // Use extended hitResult (accounts for reach distance) instead of vanilla client.hitResult
                            HitResult altHit = ClientRender.wand.lastHitResult != null ? ClientRender.wand.lastHitResult : client.hitResult;
                            ClientRender.wand.pin.freeze(altHit, mainHand, mode);
                        } else {
                            // Alt released — release non-persistent freeze
                            ClientRender.wand.pin.release();
                        }
                    }
                    alt = newAlt;
                    shift = newShift;
                    if (ClientRender.wand != null) {
                        ClientRender.wand.is_shift_pressed = shift;
                    }
                    boolean frozen = ClientRender.wand != null && ClientRender.wand.pin.isActive();
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
        if (WandsMod.config.hud_mode == HudMode.OFF) return;
        boolean isFull = WandsMod.config.hud_mode == HudMode.FULL;
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.player != null) {
            ItemStack stack = client.player.getMainHandItem();
            ItemStack offhand_stack = client.player.getOffhandItem();
            boolean main = stack != null && !stack.isEmpty() && stack.getItem() instanceof WandItem;
            boolean main_bag = stack != null && !stack.isEmpty() && stack.getItem() instanceof MagicBagItem;
            boolean off_bag = offhand_stack != null && !offhand_stack.isEmpty() && offhand_stack.getItem() instanceof MagicBagItem;
            boolean main_palette = stack != null && !stack.isEmpty() && stack.getItem() instanceof PaletteItem;
            boolean off_palette = offhand_stack != null && !offhand_stack.isEmpty() && offhand_stack.getItem() instanceof PaletteItem;
            // Show palette in HUD only when mainhand holds a wand or palette
            ItemStack paletteStack = main_palette ? stack : ((main && off_palette) ? offhand_stack : null);
            int screenWidth = client.getWindow().getGuiScaledWidth();
            int screenHeight = client.getWindow().getGuiScaledHeight();

            if (main_bag || off_bag || main || paletteStack != null) {
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
                List<ItemStack> paletteItems = getPaletteItems(paletteStack, client);
                int paletteGridHeight = paletteGridHeight(paletteItems);
                int paletteGridWidth = paletteGridWidth(paletteItems);
                if (main) {
                    Font font = client.font;
                    Wand wand = ClientRender.wand;
                    WandProps.Mode mode = WandProps.getMode(stack);
                    WandProps.Action action = WandProps.getAction(stack);

                    boolean showAction = WandProps.hasMultipleActions(mode);
                    boolean showPin = mode.supports_pin();
                    boolean pinIsActive = wand != null && wand.pin.isSet() && wand.pin.isPersistent();

                    String modeStr = Compat.translatable(mode.toString()).getString();
                    String actionStr = showAction ? Compat.translatable(action.toString()).getString() : "";
                    String modeKey = getKeyName(WandsMod.WandKeys.MODE);
                    String actionKey = getKeyName(WandsMod.WandKeys.ACTION);
                    String pinKey = showPin ? getKeyName(WandsMod.WandKeys.PIN) : "";
                    String settingsKey = getKeyName(WandsMod.WandKeys.MENU);
                    String pinStr = showPin ? Compat.translatable(pinIsActive ? "wands.hud.unpin" : "wands.hud.pin").getString() : "";
                    String movePinStr = Compat.translatable("wands.hud.move_pin").getString();
                    String settingsStr = Compat.translatable("wands.hud.settings").getString();

                    // Rotation / Randomize hint
                    boolean showRotation = WandProps.rotationAppliesTo(mode) || mode == WandProps.Mode.ROCK;
                    Rotation r = (isFull && WandProps.rotationAppliesTo(mode)) ? WandProps.getRotation(stack) : null;
                    String rot = "";
                    if (r != null) {
                        switch (r) {
                            case NONE: rot = "0°"; break;
                            case CLOCKWISE_90: rot = "90°"; break;
                            case CLOCKWISE_180: rot = "180°"; break;
                            case COUNTERCLOCKWISE_90: rot = "270°"; break;
                        }
                    }
                    String rotLabel = mode == WandProps.Mode.ROCK
                        ? Compat.translatable("wands.hud.randomize").getString()
                        : Compat.translatable("wands.hud.rotate").getString() + (rot.isEmpty() ? "" : " " + rot);

                    // FULL-only: orientation/plane
                    String orientKey = isFull ? getKeyName(WandsMod.WandKeys.ORIENTATION) : "";
                    String orientText = "";
                    if (isFull) {
                        switch (mode) {
                            case CIRCLE:
                                orientText = Compat.translatable("screen.wands.plane").getString() + " " + WandProps.getPlane(stack) + " [" + orientKey + "]";
                                break;
                            case ROW_COL:
                                orientText = Compat.translatable(WandProps.getOrientation(stack).toString()).getString() + " [" + orientKey + "]";
                                break;
                        }
                    }

                    // FULL-only: P1/P2
                    boolean showP1P2 = isFull && mode == WandProps.Mode.COPY;
                    String p1Val = "";
                    String p2Val = "";
                    String p1Text = "";
                    String p2Text = "";
                    if (showP1P2 && wand != null) {
                        BlockPos bp1 = wand.getP1();
                        BlockPos bp2 = wand.getP2();
                        if (bp1 != null) {
                            p1Val = bp1.getX() + ", " + bp1.getY() + ", " + bp1.getZ();
                        }
                        if (bp2 != null) {
                            p2Val = bp2.getX() + ", " + bp2.getY() + ", " + bp2.getZ();
                        } else if (bp1 != null) {
                            p2Val = ClientRender.last_pos.getX() + ", " + ClientRender.last_pos.getY() + ", " + ClientRender.last_pos.getZ();
                        }
                        if (!p1Val.isEmpty()) p1Text = "P1: " + p1Val;
                        if (!p2Val.isEmpty()) p2Text = "P2: " + p2Val;
                    }

                    // FULL-only: mode-specific info
                    String infoText = "";
                    if (isFull && wand != null && wand.valid) {
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
                                infoText = "Size " + tw + "x" + th + "x" + td;
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

                    // FULL-only: clear, undo/redo
                    boolean showClear = isFull && wand != null && (wand.getP1() != null || wand.block_buffer.get_length() > 0);
                    String clearKey = showClear ? getKeyName(WandsMod.WandKeys.CLEAR) : "";
                    boolean showUndo = isFull && client.player.isCreative();
                    String undoKey = showUndo ? getKeyName(WandsMod.WandKeys.UNDO) : "";

                    int pad = WandScreen.SCREEN_MARGIN;
                    int lineSpacing = font.lineHeight + Section.VERTICAL_SPACING;

                    // Count lines
                    int lineCount = 1; // mode
                    if (showAction) lineCount++;
                    if (isFull && !orientText.isEmpty()) lineCount++;
                    if (showRotation) lineCount++;
                    if (!p1Text.isEmpty()) lineCount++;
                    if (!p2Text.isEmpty()) lineCount++;
                    if (isFull && !infoText.isEmpty()) lineCount++;
                    if (showPin) lineCount++;
                    if (pinIsActive) lineCount++; // Move pin
                    if (showClear) lineCount++;
                    if (showUndo) lineCount += 2; // Undo + Redo
                    lineCount++; // Settings

                    // Calculate max width
                    String modeText = modeStr + " [" + modeKey + ", hold " + modeKey + "]";
                    String actionText = showAction ? actionStr + " [" + actionKey + "]" : "";
                    String pinText = showPin ? pinStr + " [" + pinKey + "]" : "";
                    String movePinText = movePinStr + " [←→↑↓, Shift+↑↓]";
                    String settingsText = settingsStr + " [" + settingsKey + "]";

                    int maxWidth = font.width(modeText);
                    if (showAction) maxWidth = Math.max(maxWidth, font.width(actionText));
                    if (isFull && !orientText.isEmpty()) maxWidth = Math.max(maxWidth, font.width(orientText));
                    if (showRotation) maxWidth = Math.max(maxWidth, font.width(rotLabel + " [" + getKeyName(WandsMod.WandKeys.ROTATE) + "]"));
                    if (!p1Text.isEmpty()) maxWidth = Math.max(maxWidth, font.width(p1Text));
                    if (!p2Text.isEmpty()) maxWidth = Math.max(maxWidth, font.width(p2Text));
                    if (isFull && !infoText.isEmpty()) maxWidth = Math.max(maxWidth, font.width(infoText));
                    if (showPin) maxWidth = Math.max(maxWidth, font.width(pinText));
                    if (pinIsActive) maxWidth = Math.max(maxWidth, font.width(movePinText));
                    if (showClear) maxWidth = Math.max(maxWidth, font.width("Clear [" + clearKey + "]"));
                    if (showUndo) {
                        maxWidth = Math.max(maxWidth, font.width("Undo [" + undoKey + "]"));
                        maxWidth = Math.max(maxWidth, font.width("Redo [Shift+" + undoKey + "]"));
                    }
                    maxWidth = Math.max(maxWidth, font.width(settingsText));

                    int paletteExtra = paletteGridHeight > 0 ? Section.VERTICAL_SPACING + paletteGridHeight : 0;
                    int contentHeight = lineCount * font.lineHeight + (lineCount - 1) * Section.VERTICAL_SPACING + paletteExtra;
                    int contentWidth = Math.max(maxWidth, paletteGridWidth);

                    int hudX = pad + (int) ((screenWidth - contentWidth - pad * 2) * (WandsMod.config.wand_mode_display_x_pos / 100.0f));
                    int hudY = pad + (int) ((screenHeight - contentHeight - pad * 2) * (WandsMod.config.wand_mode_display_y_pos / 100.0f));

                    int currentY = hudY;

                    // Palette grid (above text)
                    currentY += renderPaletteGrid(gui, paletteItems, hudX, currentY, paletteGridHeight);

                    // Mode [V, hold V]
                    drawHudValueWithHint(gui, font, modeStr, modeKey + ", hold " + modeKey, hudX, currentY);
                    currentY += lineSpacing;

                    // Action [H]
                    if (showAction) {
                        drawHudValueWithHint(gui, font, actionStr, actionKey, hudX, currentY);
                        currentY += lineSpacing;
                    }

                    // FULL: Orientation/Plane [X]
                    if (isFull && !orientText.isEmpty()) {
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

                    // Rotation [R] / Randomize [R]
                    if (showRotation) {
                        drawHudValueWithHint(gui, font, rotLabel, getKeyName(WandsMod.WandKeys.ROTATE), hudX, currentY);
                        currentY += lineSpacing;
                    }

                    // FULL: P1/P2
                    if (!p1Text.isEmpty()) {
                        drawHudLabelValue(gui, font, "P1: ", p1Val, hudX, currentY);
                        currentY += lineSpacing;
                    }
                    if (!p2Text.isEmpty()) {
                        drawHudLabelValue(gui, font, "P2: ", p2Val, hudX, currentY);
                        currentY += lineSpacing;
                    }

                    // FULL: Mode-specific info
                    if (isFull && wand != null && wand.valid && !infoText.isEmpty()) {
                        int lineX = hudX;
                        switch (mode) {
                            case DIRECTION:
                                int mult2 = WandProps.getVal(stack, WandProps.Value.MULTIPLIER);
                                lineX += drawHudLabelValue(gui, font, "pos: ", wand.pos.getX() + ", " + wand.pos.getY() + ", " + wand.pos.getZ(), lineX, currentY);
                                drawHudLabelValue(gui, font, " x", String.valueOf(mult2), lineX, currentY);
                                break;
                            case GRID:
                                int gm2 = WandProps.getVal(stack, WandProps.Value.GRIDM);
                                int gn2 = WandProps.getVal(stack, WandProps.Value.GRIDN);
                                int gms2 = WandProps.getVal(stack, WandProps.Value.GRIDMS);
                                int gns2 = WandProps.getVal(stack, WandProps.Value.GRIDNS);
                                lineX += drawHudLabelValue(gui, font, "Grid ", gm2 + "x" + gn2, lineX, currentY);
                                if (gms2 > 0 || gns2 > 0) {
                                    drawHudLabelValue(gui, font, " Skip ", gms2 + "x" + gns2, lineX, currentY);
                                }
                                break;
                            case FILL:
                                int nx2 = wand.fill_nx + 1;
                                int ny2 = wand.fill_ny + 1;
                                int nz2 = wand.fill_nz + 1;
                                lineX += drawHudLabelValue(gui, font, "Volume ", nx2 + "x" + ny2 + "x" + nz2, lineX, currentY);
                                lineX += font.width(" ");
                                drawHudLabelValue(gui, font, "Blocks: ", String.valueOf(wand.block_buffer.get_length()), lineX, currentY);
                                break;
                            case ROW_COL:
                                int rowcollim2 = WandProps.getVal(stack, WandProps.Value.ROWCOLLIM);
                                lineX += drawHudLabelValue(gui, font, "Blocks: ", String.valueOf(wand.block_buffer.get_length()), lineX, currentY);
                                if (rowcollim2 > 0) {
                                    drawHudLabelValue(gui, font, " Limit: ", String.valueOf(rowcollim2), lineX, currentY);
                                }
                                break;
                            case LINE:
                                drawHudLabelValue(gui, font, "Blocks: ", String.valueOf(wand.block_buffer.get_length()), lineX, currentY);
                                break;
                            case AREA:
                            case VEIN:
                                int arealim2 = WandProps.getVal(stack, WandProps.Value.AREALIM);
                                lineX += drawHudLabelValue(gui, font, "Blocks: ", String.valueOf(wand.block_buffer.get_length()), lineX, currentY);
                                if (arealim2 > 0) {
                                    drawHudLabelValue(gui, font, " Limit: ", String.valueOf(arealim2), lineX, currentY);
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
                                int tw2 = WandProps.getVal(stack, WandProps.Value.BOX_W);
                                int th2 = WandProps.getVal(stack, WandProps.Value.BOX_H);
                                int td2 = WandProps.getVal(stack, WandProps.Value.BOX_DEPTH);
                                drawHudLabelValue(gui, font, "Size ", tw2 + "x" + th2 + "x" + td2, lineX, currentY);
                                break;
                            case BLAST:
                                int blastRad2 = WandProps.getVal(stack, WandProps.Value.BLASTRAD);
                                drawHudLabelValue(gui, font, "Radius: ", String.valueOf(blastRad2), lineX, currentY);
                                break;
                            case ROCK:
                                int rockRad2 = WandProps.getVal(stack, WandProps.Value.ROCK_RADIUS);
                                int rockNoise2 = WandProps.getVal(stack, WandProps.Value.ROCK_NOISE);
                                lineX += drawHudLabelValue(gui, font, "Radius: ", String.valueOf(rockRad2), lineX, currentY);
                                drawHudLabelValue(gui, font, " Noise: ", String.valueOf(rockNoise2), lineX, currentY);
                                break;
                        }
                        currentY += lineSpacing;
                    }

                    // Pin/Unpin [G]
                    if (showPin) {
                        drawHudValueWithHint(gui, font, pinStr, pinKey, hudX, currentY);
                        currentY += lineSpacing;
                    }

                    // Move pin [←→↑↓, Shift+↑↓]
                    if (pinIsActive) {
                        drawHudValueWithHint(gui, font, movePinStr, "←→↑↓, Shift+↑↓", hudX, currentY);
                        currentY += lineSpacing;
                    }

                    // FULL: Clear [C]
                    if (showClear) {
                        drawHudValueWithHint(gui, font, "Clear", clearKey, hudX, currentY);
                        currentY += lineSpacing;
                    }

                    // FULL: Undo [U] / Redo [Shift+U]
                    if (showUndo) {
                        drawHudValueWithHint(gui, font, "Undo", undoKey, hudX, currentY);
                        currentY += lineSpacing;
                        drawHudValueWithHint(gui, font, "Redo", "Shift+" + undoKey, hudX, currentY);
                        currentY += lineSpacing;
                    }

                    // Settings [Y]
                    drawHudValueWithHint(gui, font, settingsStr, settingsKey, hudX, currentY);
                } else if (!paletteItems.isEmpty()) {
                    // Standalone palette (mainhand is palette, no wand)
                    Font font = client.font;
                    int pad = WandScreen.SCREEN_MARGIN;
                    int lineSpacing = font.lineHeight + Section.VERTICAL_SPACING;

                    String palModeStr = PaletteItem.getModeName(stack).getString();
                    String palRotateStr = PaletteItem.getRotate(stack) ? "On" : "Off";

                    int lineCount = 2; // mode + rotate
                    int textMaxWidth = Math.max(font.width(palModeStr), font.width("Rotate: " + palRotateStr));
                    int paletteExtra = paletteGridHeight > 0 ? Section.VERTICAL_SPACING + paletteGridHeight : 0;
                    int contentHeight = lineCount * font.lineHeight + (lineCount - 1) * Section.VERTICAL_SPACING + paletteExtra;
                    int contentWidth = Math.max(textMaxWidth, paletteGridWidth);

                    int hudX = pad + (int) ((screenWidth - contentWidth - pad * 2) * (WandsMod.config.wand_mode_display_x_pos / 100.0f));
                    int hudY = pad + (int) ((screenHeight - contentHeight - pad * 2) * (WandsMod.config.wand_mode_display_y_pos / 100.0f));

                    int currentY = hudY;
                    currentY += renderPaletteGrid(gui, paletteItems, hudX, currentY, paletteGridHeight);

                    drawHudLabelValue(gui, font, "Mode: ", palModeStr, hudX, currentY);
                    currentY += lineSpacing;
                    drawHudLabelValue(gui, font, "Rotate: ", palRotateStr, hudX, currentY);
                }
            }
        }
    }

    /** Draw HUD text with label in white and value in gray */
    private static int drawHudLabelValue(GuiGraphics gui, Font font, String label, String value, int x, int y) {
        gui.drawString(font, label, x, y, ModeInstructionOverlay.TEXT_COLOR);
        int labelWidth = font.width(label);
        gui.drawString(font, value, x + labelWidth, y, ModeInstructionOverlay.TEXT_COLOR_DIM);
        return labelWidth + font.width(value);
    }

    /** Draw value in white followed by keybind hint in gray with a space between, returns total width */
    private static int drawHudValueWithHint(GuiGraphics gui, Font font, String value, String keyName, int x, int y) {
        int w = 0;
        gui.drawString(font, value, x, y, ModeInstructionOverlay.TEXT_COLOR);
        w += font.width(value);
        String hint = " [" + keyName + "]";
        gui.drawString(font, hint, x + w, y, ModeInstructionOverlay.TEXT_COLOR_DIM);
        w += font.width(hint);
        return w;
    }

    private static final int PALETTE_SLOT_SIZE = 18;
    private static final int PALETTE_MAX_COLS = 9;

    /** Collect non-empty items from a palette stack's inventory. */
    private static List<ItemStack> getPaletteItems(ItemStack paletteStack, Minecraft client) {
        List<ItemStack> items = new ArrayList<>();
        if (paletteStack != null && client.level != null) {
            SimpleContainer inv = PaletteItem.getInventory(paletteStack, client.level);
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (!s.isEmpty()) items.add(s);
            }
        }
        return items;
    }

    private static int paletteGridWidth(List<ItemStack> items) {
        if (items.isEmpty()) return 0;
        return Math.min(items.size(), PALETTE_MAX_COLS) * PALETTE_SLOT_SIZE;
    }

    private static int paletteGridHeight(List<ItemStack> items) {
        if (items.isEmpty()) return 0;
        int rows = (items.size() + PALETTE_MAX_COLS - 1) / PALETTE_MAX_COLS;
        return rows * PALETTE_SLOT_SIZE;
    }

    /** Render palette item icons in a grid. Returns the Y advance (0 if empty). */
    private static int renderPaletteGrid(GuiGraphics gui, List<ItemStack> items, int x, int y, int gridHeight) {
        if (items.isEmpty()) return 0;
        for (int i = 0; i < items.size(); i++) {
            int col = i % PALETTE_MAX_COLS;
            int row = i / PALETTE_MAX_COLS;
            gui.renderItem(items.get(i), x + col * PALETTE_SLOT_SIZE, y + row * PALETTE_SLOT_SIZE);
        }
        return gridHeight + Section.VERTICAL_SPACING;
    }

    public static void cancel_wand() {
        if (ClientRender.wand != null && ClientRender.wand.wand_stack != null && WandUtils.is_wand(ClientRender.wand.wand_stack)) {
            ClientRender.wand.clear(true);
            ClientRender.wand.pin.clear();
            if (ClientRender.wand.player != null && !WandsMod.config.disable_info_messages) {
                ClientRender.wand.player.displayClientMessage(Compat.translatable("wands.message.wand_cleared"), true);
            }
        }
    }

    /**
     * Handle pin key input. Returns true if the key was consumed (should NOT be sent to server).
     */
    private static boolean handlePinKey(Minecraft client, ItemStack mainHand, WandsMod.WandKeys key, boolean shift) {
        Wand wand = ClientRender.wand;
        if (wand == null) return false;

        WandProps.Mode mode = WandProps.getMode(mainHand);

        if (key == WandsMod.WandKeys.PIN) {
            if (!wand.pin.isSet() && !mode.supports_pin()) {
                // Mode doesn't support pin — consume the key but do nothing
                return true;
            }
            boolean wasSet = wand.pin.isSet();
            // Use extended hitResult (accounts for reach distance) instead of vanilla client.hitResult
            HitResult hitResult = wand.lastHitResult != null ? wand.lastHitResult : client.hitResult;
            wand.pin.toggle(hitResult, mainHand, mode);
            if (client.player != null && !WandsMod.config.disable_info_messages) {
                String msgKey = wasSet ? "wands.message.pin_cleared" : "wands.message.pin_set";
                if (wand.pin.isSet() || wasSet) {
                    client.player.displayClientMessage(Compat.translatable(msgKey), true);
                }
            }
            return true;
        }

        // Arrow keys: delegate to pin.move()
        if (client.player == null) return false;
        return wand.pin.move(key, shift, client.player.getDirection());
    }
}
