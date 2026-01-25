package net.nicguzzo.wands.client;


import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.networking.Networking;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.utils.WandUtils;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandProps;
import net.nicguzzo.wands.client.screens.WandScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WandsModClient {
    static boolean shift = false;
    static boolean alt = false;
    public static boolean has_optifine = false;
    public static boolean has_opac = false;
    public static KeyMapping wand_menu_km;

    public static final Logger LOGGER = LogManager.getLogger();
    static final public Map keys = new HashMap<KeyMapping, WandsMod.WandKeys>();
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
    public static final KeyMapping.Category tab = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(WandsMod.MOD_ID, "key.categories.wands"));
    static final String k = "key.wands.";

    public static void initialize() {

        wand_menu_km = new KeyMapping(k + "wand_menu", wand_menu_key, tab);
        keys.put(wand_menu_km, WandsMod.WandKeys.MENU);
        keys.put(new KeyMapping(k + "wand_mode", wand_mode_key, tab), WandsMod.WandKeys.MODE);
        keys.put(new KeyMapping(k + "palette_menu", palette_menu_key, tab), WandsMod.WandKeys.PALETTE_MENU);
        keys.put(new KeyMapping(k + "wand_action", wand_action_key, tab), WandsMod.WandKeys.ACTION);
        keys.put(new KeyMapping(k + "wand_orientation", wand_orientation_key, tab), WandsMod.WandKeys.ORIENTATION);
        keys.put(new KeyMapping(k + "wand_invert", wand_invert_key, tab), WandsMod.WandKeys.INVERT);
        keys.put(new KeyMapping(k + "wand_fill_circle", wand_fill_circle_key, tab), WandsMod.WandKeys.FILL);
        keys.put(new KeyMapping(k + "wand_undo", wand_undo_key, tab), WandsMod.WandKeys.UNDO);
        keys.put(new KeyMapping(k + "wand_palette_mode", palette_mode_key, tab), WandsMod.WandKeys.PALETTE_MODE);
        keys.put(new KeyMapping(k + "wand_rotate", wand_rotate, tab), WandsMod.WandKeys.ROTATE);
        keys.put(new KeyMapping(k + "m_inc", wand_m_inc_key, tab), WandsMod.WandKeys.M_INC);
        keys.put(new KeyMapping(k + "m_dec", wand_m_dec_key, tab), WandsMod.WandKeys.M_DEC);
        keys.put(new KeyMapping(k + "n_inc", wand_n_inc_key, tab), WandsMod.WandKeys.N_INC);
        keys.put(new KeyMapping(k + "n_dec", wand_n_dec_key, tab), WandsMod.WandKeys.N_DEC);
        keys.put(new KeyMapping(k + "toggle_stair_slab", toggle_stair_slab_key, tab), WandsMod.WandKeys.TOGGLE_STAIRSLAB);
        keys.put(new KeyMapping(k + "area_diagonal_spread", area_diagonal_spread, tab), WandsMod.WandKeys.DIAGONAL_SPREAD);
        keys.put(new KeyMapping(k + "inc_sel_block", inc_sel_block, tab), WandsMod.WandKeys.INC_SEL_BLK);
        keys.put(new KeyMapping(k + "clear_wand", GLFW.GLFW_KEY_C, tab), WandsMod.WandKeys.CLEAR);

        keys.forEach((km, v) -> Compat.register_key((KeyMapping) km));

        ClientTickEvent.CLIENT_PRE.register(e -> {
            boolean any = false;
            Iterator<Map.Entry<KeyMapping, WandsMod.WandKeys>> itr = keys.entrySet().iterator();
            Minecraft client = e;
            while (itr.hasNext()) {
                Map.Entry<KeyMapping, WandsMod.WandKeys> me = itr.next();
                KeyMapping km = me.getKey();
                WandsMod.WandKeys key = me.getValue();
                if (km.consumeClick()) {
                    if (!any) any = true;
                    if (key == WandsMod.WandKeys.CLEAR) {
                        cancel_wand();
                    } else {
                        send_key(key.ordinal(), client.hasShiftDown(), client.hasAltDown());
                    }
                    if (key == WandsMod.WandKeys.ROTATE) {

                        //TODO:move this to another key?
                        if (ClientRender.wand != null && ClientRender.wand.mode == WandProps.Mode.ROCK) {
                            ClientRender.wand.get_mode().randomize();
                        }
                    }
                }
            }

            if (!any) {
                if (alt != client.hasAltDown() || shift != client.hasShiftDown()) {
                    alt = client.hasAltDown();
                    shift = client.hasShiftDown();
                    ClientRender.wand.is_alt_pressed = alt;
                    ClientRender.wand.is_shift_pressed = shift;
                    send_key(-1, shift, alt);
                }
            }
        });

        //Compat.render_info();
        ClientGuiEvent.RENDER_HUD.register((e, d) -> {
            WandsModClient.render_wand_info(e);
        });

        NetworkManager.registerReceiver(Side.S2C, Networking.PlayerDataPacket.TYPE, Networking.PlayerDataPacket.STREAM_CODEC, (data, context) -> {
            //LOGGER.info("got PlayerDataPacket");
            if (ClientRender.wand != null) {
                ClientRender.wand.player_data = data.tag();
            }
        });

        NetworkManager.registerReceiver(Side.S2C, Networking.ConfPacket.TYPE, Networking.ConfPacket.STREAM_CODEC, (data, context) -> {
            //LOGGER.info("got ConfPacket");
            if (WandsMod.config != null) {
                WandsMod.config.blocks_per_xp = data.blocks_per_xp();
                WandsMod.config.destroy_in_survival_drop = data.destroy_in_survival_drop();
                WandsMod.config.survival_unenchanted_drops = data.survival_unenchanted_drops();
                //WandsMod.config.allow_wand_to_break = data.allow_wand_to_break();
                //WandsMod.config.allow_offhand_to_break = data.allow_offhand_to_break();
                WandsMod.config.mend_tools = data.mend_tools();
                //LOGGER.info("got config");
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, Networking.SndPacket.TYPE, Networking.SndPacket.STREAM_CODEC, (data, context) -> {
            //LOGGER.info("got SndPacket");
            BlockPos pos = data.pos();
            boolean destroy = data.destroy();
            ItemStack item_stack = data.item_stack();
            int i_sound = data.i_sound();

            if (i_sound > -1 && i_sound < Wand.Sounds.values().length) {
                Wand.Sounds snd = Wand.Sounds.values()[i_sound];
                SoundEvent sound = snd.get_sound();
                Compat.player_level(context.getPlayer()).playSound(context.getPlayer(), pos, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
            } else {
                if (!item_stack.isEmpty()) {
                    Block block = Block.byItem(item_stack.getItem());
                    BlockState bs = block.defaultBlockState();
                    SoundType sound_type = bs.getSoundType();
                    //SoundType sound_type = ((BlockBehaviourInvoker)block).invokeGetSoundType(block.defaultBlockState());
                    SoundEvent sound = (destroy ? sound_type.getBreakSound() : sound_type.getPlaceSound());
                    Compat.player_level(context.getPlayer()).playSound(context.getPlayer(), pos, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }

        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, Networking.ToastPacket.TYPE, Networking.ToastPacket.STREAM_CODEC, (data, context) -> {
            //LOGGER.info("got ToastPacket");
            boolean no_tool = data.no_tool();
            boolean damaged_tool = data.damaged_tool();
            if (WandsMod.config.toast_in_action_bar) {
                Player player = context.getPlayer();
                if (no_tool) {
                    player.displayClientMessage(Compat.literal("no tool"), true);
                }
                if (damaged_tool) {
                    player.displayClientMessage(Compat.literal("invalid or damaged"), true);
                }
            } else {
                if (no_tool) {
                    Compat.toast(new WandToast("no tool"));
                }
                if (damaged_tool) {
                    Compat.toast(new WandToast("invalid or damaged"));
                }
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, Networking.StatePacket.TYPE, Networking.StatePacket.STREAM_CODEC, (data, context) -> {
            //LOGGER.info("got StatePacket");
            ///long seed=data.seed();
            int mode = data.mode();
            int slot = data.slot();
            boolean xp = data.xp();
            int levels = data.levels();
            float prog = data.prog();

            if (ClientRender.wand != null) {
                ClientRender.wand.mode = WandProps.Mode.values()[mode];
                if (ClientRender.wand.mode == WandProps.Mode.DIRECTION)
                    ClientRender.wand.palette.slot = slot;
                if (xp) {
                    context.getPlayer().experienceLevel = levels;
                    context.getPlayer().experienceProgress = prog;
                }
            }
        });
    }

    public static void send_key(int key, boolean shift, boolean alt) {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() != null) {
            NetworkManager.sendToServer(new Networking.KbPacket(key, shift, alt));
        }
    }

    public static void send_palette(boolean next_mode, boolean toggle_rotate, int grad_h) {
        NetworkManager.sendToServer(new Networking.PalettePacket(next_mode, toggle_rotate, grad_h));
    }

    public static void send_wand(ItemStack item) {
        NetworkManager.sendToServer(new Networking.WandPacket(item));
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
                    ItemStack bgi = MagicBagItem.getItem(s, client.level.registryAccess());
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
                    // Determine which HUD elements to show based on mode
                    boolean showP1P2 = mode.n_clicks() == 2;  // Two-click modes show P1/P2
                    boolean showAction = WandProps.anyActionAppliesTo(mode);
                    boolean showRotation = WandProps.rotationAppliesTo(mode);

                    String p1Val = "";
                    String p2Val = "";
                    if (showP1P2) {
                        BlockPos bp1 = wand.getP1();
                        BlockPos bp2 = wand.getP2();
                        if (wand.getP1() != null) {
                            p1Val = "[" + bp1.getX() + "," + bp1.getY() + "," + bp1.getZ() + "]";
                        }
                        if (wand.getP2() != null) {
                            p2Val = "[" + bp2.getX() + "," + bp2.getY() + "," + bp2.getZ() + "]";
                        } else {
                            if (wand.getP1() != null) {
                                p2Val = "[" + ClientRender.last_pos.getX() + "," + ClientRender.last_pos.getY() + "," + ClientRender.last_pos.getZ() + "]";
                            }
                        }
                    }

                    // Build text content for each line to measure width
                    String line1Text = "";
                    if (wand.valid) {
                        switch (mode) {
                            case DIRECTION:
                                int mult = WandProps.getVal(stack, WandProps.Value.MULTIPLIER);
                                line1Text = "pos: [" + wand.pos.getX() + "," + wand.pos.getY() + "," + wand.pos.getZ() + "] x" + mult;
                                break;
                            case GRID:
                                int gm = WandProps.getVal(stack, WandProps.Value.GRIDM);
                                int gn = WandProps.getVal(stack, WandProps.Value.GRIDN);
                                int gms = WandProps.getVal(stack, WandProps.Value.GRIDMS);
                                int gns = WandProps.getVal(stack, WandProps.Value.GRIDNS);
                                line1Text = "Grid " + gm + "x" + gn;
                                if (gms > 0 || gns > 0) {
                                    line1Text += " Skip " + gms + "x" + gns;
                                }
                                break;
                            case FILL:
                                int nx = wand.fill_nx + 1;
                                int ny = wand.fill_ny + 1;
                                int nz = wand.fill_nz + 1;
                                line1Text = "Volume [" + nx + "," + ny + "," + nz + "] Blocks: " + wand.block_buffer.get_length();
                                break;
                            case ROW_COL:
                                int rowcollim = WandProps.getVal(stack, WandProps.Value.ROWCOLLIM);
                                line1Text = "Blocks: " + wand.block_buffer.get_length();
                                if (rowcollim > 0) {
                                    line1Text += " Limit: " + rowcollim;
                                }
                                break;
                            case LINE:
                                line1Text = "Blocks: " + wand.block_buffer.get_length();
                                break;
                            case AREA:
                            case VEIN:
                                int arealim = WandProps.getVal(stack, WandProps.Value.AREALIM);
                                line1Text = "Blocks: " + wand.block_buffer.get_length();
                                if (arealim > 0) {
                                    line1Text += " Limit: " + arealim;
                                }
                                break;
                            case CIRCLE:
                            case SPHERE:
                                line1Text = "Radius: " + wand.radius + " N: " + wand.block_buffer.get_length();
                                break;
                            case COPY:
                            case PASTE:
                                line1Text = "Copied Blocks: " + wand.copy_paste_buffer.size();
                                break;
                            case TUNNEL:
                                int tw = WandProps.getVal(stack, WandProps.Value.TUNNEL_W);
                                int th = WandProps.getVal(stack, WandProps.Value.TUNNEL_H);
                                int td = WandProps.getVal(stack, WandProps.Value.TUNNEL_DEPTH);
                                line1Text = "Size " + tw + "x" + th + " Depth: " + td;
                                break;
                            case BLAST:
                                int blastRad = WandProps.getVal(stack, WandProps.Value.BLASTRAD);
                                line1Text = "Radius: " + blastRad;
                                break;
                            case ROCK:
                                int rockRad = WandProps.getVal(stack, WandProps.Value.ROCK_RADIUS);
                                int rockNoise = WandProps.getVal(stack, WandProps.Value.ROCK_NOISE);
                                line1Text = "Radius: " + rockRad + " Noise: " + rockNoise;
                                break;
                        }
                    }

                    String line2Text = showAction ? "Action: " + Compat.translatable(action.toString()).getString() : "";
                    String line3Text = "Mode: " + Compat.translatable(mode.toString()).getString();
                    if (showRotation) {
                        line3Text += " Rot: " + rot;
                    }

                    // Calculate max width for background
                    int maxWidth = font.width(line1Text);
                    maxWidth = Math.max(maxWidth, font.width(line2Text));
                    maxWidth = Math.max(maxWidth, font.width(line3Text));
                    if (showP1P2) {
                        if (!p1Val.isEmpty()) maxWidth = Math.max(maxWidth, font.width("P1: " + p1Val));
                        if (!p2Val.isEmpty()) maxWidth = Math.max(maxWidth, font.width("P2: " + p2Val));
                    }

                    // Count actual lines for dynamic height
                    int lineCount = 0;
                    if (showP1P2 && !p1Val.isEmpty()) lineCount++;
                    if (showP1P2 && !p2Val.isEmpty()) lineCount++;
                    if (!line1Text.isEmpty()) lineCount++;
                    if (showAction) lineCount++;
                    lineCount++; // Mode line is always shown

                    int pad = WandScreen.SCREEN_MARGIN;
                    int contentHeight = lineCount * font.lineHeight;
                    int contentWidth = maxWidth;

                    // Calculate HUD position
                    // At 100%: background right/bottom edge is pad pixels from screen edge
                    int hudX = pad * 2 + (int) ((screenWidth - contentWidth - pad * 4) * (WandsMod.config.wand_mode_display_x_pos / 100.0f));
                    int hudY = pad * 2 + (int) ((screenHeight - contentHeight - pad * 4) * (WandsMod.config.wand_mode_display_y_pos / 100.0f));

                    // Draw background (with padding on all sides)
                    gui.fill(hudX - pad, hudY - pad, hudX + contentWidth + pad, hudY + contentHeight + pad, WandScreen.COLOR_PANEL_BACKGROUND);

                    // Draw lines sequentially from top to bottom
                    int currentY = hudY;
                    int lineX = hudX;

                    // Mode line (always shown first)
                    lineX += drawHudLabelValue(gui, font, "Mode: ", Compat.translatable(mode.toString()).getString(), lineX, currentY);
                    if (showRotation) {
                        drawHudLabelValue(gui, font, " Rot: ", rot, lineX, currentY);
                    }
                    currentY += font.lineHeight;

                    // Action line (if shown)
                    if (showAction) {
                        drawHudLabelValue(gui, font, "Action: ", Compat.translatable(action.toString()).getString(), hudX, currentY);
                        currentY += font.lineHeight;
                    }

                    // P1 and P2 (if shown)
                    if (showP1P2) {
                        if (!p1Val.isEmpty()) {
                            drawHudLabelValue(gui, font, "P1: ", p1Val, hudX, currentY);
                            currentY += font.lineHeight;
                        }
                        if (!p2Val.isEmpty()) {
                            drawHudLabelValue(gui, font, "P2: ", p2Val, hudX, currentY);
                            currentY += font.lineHeight;
                        }
                    }

                    // Mode-specific info line
                    lineX = hudX;
                    if (wand.valid && !line1Text.isEmpty()) {
                        switch (mode) {
                            case DIRECTION:
                                int mult = WandProps.getVal(stack, WandProps.Value.MULTIPLIER);
                                lineX += drawHudLabelValue(gui, font, "pos: ", "[" + wand.pos.getX() + "," + wand.pos.getY() + "," + wand.pos.getZ() + "]", lineX, currentY);
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
                                lineX += drawHudLabelValue(gui, font, "Volume ", "[" + nx + "," + ny + "," + nz + "]", lineX, currentY);
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
                            case TUNNEL:
                                int tw = WandProps.getVal(stack, WandProps.Value.TUNNEL_W);
                                int th = WandProps.getVal(stack, WandProps.Value.TUNNEL_H);
                                int td = WandProps.getVal(stack, WandProps.Value.TUNNEL_DEPTH);
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
                    }
                }
            }
        }
    }

    /** Draw HUD text with label in gray and value in white */
    private static int drawHudLabelValue(GuiGraphics gui, Font font, String label, String value, int x, int y) {
        gui.drawString(font, label, x, y, WandScreen.COLOR_WDGT_LABEL);
        int labelWidth = font.width(label);
        gui.drawString(font, value, x + labelWidth, y, WandScreen.COLOR_TEXT_PRIMARY);
        return labelWidth + font.width(value);
    }

    /** Draw HUD text - value only in white */
    private static void drawHudValue(GuiGraphics gui, Font font, String value, int x, int y) {
        gui.drawString(font, value, x, y, WandScreen.COLOR_TEXT_PRIMARY);
    }

    public static void cancel_wand() {
        if (ClientRender.wand != null && ClientRender.wand.wand_stack != null && WandUtils.is_wand(ClientRender.wand.wand_stack)) {
            ClientRender.wand.clear(true);
            if (ClientRender.wand.player != null && !WandsMod.config.disable_info_messages) {
                ClientRender.wand.player.displayClientMessage(Compat.literal("wand cleared"), false);
            }
        }
    }
}
