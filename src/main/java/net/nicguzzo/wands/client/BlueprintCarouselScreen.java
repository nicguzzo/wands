package net.nicguzzo.wands.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.client.screens.BlueprintSelectorScreen;
import net.nicguzzo.wands.client.screens.WandScreen;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.items.BlueprintItem;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.networking.ClientNetworking;
import net.nicguzzo.wands.wand.InventoryManager;
import net.nicguzzo.wands.wand.InventoryManager.BlueprintEntry;
import net.nicguzzo.wands.wand.WandProps;
//?if < 26.3 {
import org.lwjgl.glfw.GLFW;
//?}

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Blueprint carousel overlay screen.
 * - Tap BLUEPRINT key (`B`): cycles to the next blueprint (or in-memory copy buffer).
 * - Hold BLUEPRINT key (`B` >= 4 ticks): opens this non-pausing horizontal carousel.
 *   Mouse wheel or Left/Right arrow keys scroll through items; releasing `B` selects the highlighted blueprint.
 * - Shift + BLUEPRINT key (`Shift + B`): opens the full BlueprintSelectorScreen.
 */
public class BlueprintCarouselScreen extends Screen {

    private static final int HOLD_THRESHOLD = 4;
    private static final int VISIBLE_CARDS = 5;
    private static final int CARD_W = 84;
    private static final int CARD_H = 54;
    private static final int CARD_GAP = 6;
    private static final int PANEL_PADDING = 10;

    private static final int COLOR_SCRIM = 0x80000000;
    private static final int COLOR_CARD_NORMAL = 0xE6282828;
    private static final int COLOR_CARD_HIGHLIGHT = 0xE6506880;
    private static final int COLOR_ACTIVE_BORDER = 0xFF44CC44;
    private static final int COLOR_HIGHLIGHT_BORDER = 0xFFFFFFFF;
    private static final int COLOR_HINT_TEXT = 0xFF999999;

    public static class CarouselEntry {
        public final String id; // "" for in-memory copy buffer
        public final String name;
        public final String info;
        public final ItemStack iconStack;

        public CarouselEntry(String id, String name, String info, ItemStack iconStack) {
            this.id = id;
            this.name = name;
            this.info = info;
            this.iconStack = iconStack;
        }
    }

    private static int holdStartTick = -1;
    private static boolean holdTriggered = false;

    private final ItemStack wand;
    private final List<CarouselEntry> entries = new ArrayList<>();
    private int selectedIndex = 0;
    private final Set<Integer> prevArrowState = new HashSet<>();

    public BlueprintCarouselScreen(ItemStack wand) {
        super(Component.empty());
        this.wand = wand;
        buildEntries();
    }

    private void buildEntries() {
        entries.clear();
        Minecraft client = Minecraft.getInstance();
        int copyCount = (ClientRender.wand != null && ClientRender.wand.copy_paste_buffer != null)
                ? ClientRender.wand.copy_paste_buffer.size() : 0;
        if (copyCount > 0) {
            entries.add(new CarouselEntry("", "Copied Buffer", copyCount + " blks", new ItemStack(Items.PAPER)));
        }
        if (client.player != null) {
            List<BlueprintEntry> bpList = InventoryManager.findBlueprints(client.player);
            for (BlueprintEntry bp : bpList) {
                if (bp.id.isEmpty()) continue;
                String name = BlueprintItem.getBlueprintName(bp.stack);
                if (name.isEmpty()) {
                    name = bp.stack.getHoverName().getString();
                }
                int count = BlueprintItem.getTotalBlockCount(bp.stack);
                int[] sz = BlueprintItem.getDimensions(bp.stack);
                String info = count > 0 ? (count + " (" + sz[0] + "x" + sz[1] + "x" + sz[2] + ")") : "Empty";
                entries.add(new CarouselEntry(bp.id, name, info, bp.stack));
            }
        }
        if (entries.isEmpty()) {
            entries.add(new CarouselEntry("", "Copied Buffer", "Empty", new ItemStack(Items.PAPER)));
        }

        String activeId = ClientRender.wand != null ? ClientRender.wand.getActiveBlueprintId() : "";
        selectedIndex = 0;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).id.equals(activeId)) {
                selectedIndex = i;
                break;
            }
        }
    }

    public static boolean consumesKey(WandsMod.WandKeys key) {
        if (key == WandsMod.WandKeys.BLUEPRINT) return true;
        //?if >= 26.2 {
        /*Screen screen = Minecraft.getInstance().gui.screen();
        *///?} else {
        Screen screen = Minecraft.getInstance().screen;
        //?}
        if (screen instanceof BlueprintCarouselScreen) {
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
        //?if >= 26.2 {
        /*Screen screen = Minecraft.getInstance().gui.screen();
        *///?} else {
        Screen screen = Minecraft.getInstance().screen;
        //?}
        if (screen instanceof BlueprintCarouselScreen) return;
        if (screen != null) {
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
        int bpKeyCode = getBlueprintKeyCode();
        if (bpKeyCode <= 0) {
            resetHold();
            return;
        }

        boolean bpKeyPressed = Compat.isKeyDown(window, bpKeyCode);
        //?if >= 26.2 {
        /*int currentTick = client.gui.hud.getGuiTicks();
        *///?} else {
        int currentTick = client.gui.getGuiTicks();
        //?}
        if (bpKeyPressed) {
            if (Compat.hasShiftDown()) {
                if (holdStartTick == -1) {
                    holdStartTick = currentTick;
                    holdTriggered = true;
                    //?if >= 26.2 {
                    /*client.gui.setScreen(new BlueprintSelectorScreen());
                    *///?} else {
                    client.setScreen(new BlueprintSelectorScreen());
                    //?}
                }
                return;
            }
            if (holdStartTick == -1) {
                holdStartTick = currentTick;
                holdTriggered = false;
            } else if (!holdTriggered && (currentTick - holdStartTick) >= HOLD_THRESHOLD) {
                holdTriggered = true;
                //?if >= 26.2 {
                /*client.gui.setScreen(new BlueprintCarouselScreen(mainHand));
                *///?} else {
                client.setScreen(new BlueprintCarouselScreen(mainHand));
                //?}
            }
        } else {
            if (holdStartTick != -1 && !holdTriggered) {
                tapCycle(client, mainHand);
            }
            resetHold();
        }
    }

    private static void resetHold() {
        holdStartTick = -1;
        holdTriggered = false;
    }

    private static void tapCycle(Minecraft client, ItemStack wandStack) {
        if (client.player == null || ClientRender.wand == null) return;
        List<CarouselEntry> list = new ArrayList<>();
        int copyCount = ClientRender.wand.copy_paste_buffer != null ? ClientRender.wand.copy_paste_buffer.size() : 0;
        if (copyCount > 0) {
            list.add(new CarouselEntry("", "Copied Buffer (" + copyCount + " blocks)", "", new ItemStack(Items.PAPER)));
        }
        for (BlueprintEntry bp : InventoryManager.findBlueprints(client.player)) {
            if (bp.id.isEmpty()) continue;
            list.add(new CarouselEntry(bp.id, BlueprintItem.getDisplayLabel(bp.stack), "", bp.stack));
        }
        if (list.isEmpty()) {
            Compat.displayClientMessage(client.player, Compat.translatable("wands.message.no_blueprints"), true);
            return;
        }
        String currentId = ClientRender.wand.getActiveBlueprintId();
        int currIdx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id.equals(currentId)) {
                currIdx = i;
                break;
            }
        }
        int nextIdx = (currIdx + 1) % list.size();
        CarouselEntry chosen = list.get(nextIdx);
        commitSelection(client, wandStack, chosen);
    }

    private static void commitSelection(Minecraft client, ItemStack wandStack, CarouselEntry chosen) {
        if (ClientRender.wand == null || chosen == null) return;
        ClientRender.wand.setActiveBlueprintId(chosen.id);
        ClientNetworking.SendBlueprintAction(3, chosen.id, "");
        if (WandProps.getMode(wandStack) != WandProps.Mode.PASTE) {
            WandProps.switchMode(wandStack, WandProps.Mode.PASTE);
            ClientNetworking.SendWand(wandStack);
        }
        if (client.player != null && !WandsMod.config.disable_info_messages) {
            Compat.displayClientMessage(client.player, Compat.literal("Blueprint: " + chosen.name), true);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
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
        int bpKeyCode = getBlueprintKeyCode();

        if (bpKeyCode > 0 && !Compat.isKeyDown(window, bpKeyCode)) {
            if (selectedIndex >= 0 && selectedIndex < entries.size()) {
                commitSelection(client, wand, entries.get(selectedIndex));
            }
            onClose();
            return;
        }

        if (Compat.isKeyDown(window, Compat.KEY_ESCAPE)) {
            onClose();
            return;
        }

        handleArrowKeys(window);
    }

    private void handleArrowKeys(long window) {
        int rightKey = getArrowKeyCode(WandsMod.WandKeys.M_INC);
        int leftKey = getArrowKeyCode(WandsMod.WandKeys.M_DEC);
        int upKey = getArrowKeyCode(WandsMod.WandKeys.N_INC);
        int downKey = getArrowKeyCode(WandsMod.WandKeys.N_DEC);

        if (checkKeyEdge(window, rightKey) || checkKeyEdge(window, downKey)) {
            if (!entries.isEmpty()) {
                selectedIndex = (selectedIndex + 1) % entries.size();
            }
        }
        if (checkKeyEdge(window, leftKey) || checkKeyEdge(window, upKey)) {
            if (!entries.isEmpty()) {
                selectedIndex = (selectedIndex - 1 + entries.size()) % entries.size();
            }
        }

        prevArrowState.clear();
        addIfPressed(window, rightKey);
        addIfPressed(window, leftKey);
        addIfPressed(window, upKey);
        addIfPressed(window, downKey);
    }

    private boolean checkKeyEdge(long window, int keyCode) {
        if (keyCode <= 0) return false;
        return Compat.isKeyDown(window, keyCode) && !prevArrowState.contains(keyCode);
    }

    private void addIfPressed(long window, int keyCode) {
        if (keyCode > 0 && Compat.isKeyDown(window, keyCode)) {
            prevArrowState.add(keyCode);
        }
    }

    //?if >= 1.20.2 {
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!entries.isEmpty()) {
            if (scrollY < 0) {
                selectedIndex = (selectedIndex + 1) % entries.size();
            } else if (scrollY > 0) {
                selectedIndex = (selectedIndex - 1 + entries.size()) % entries.size();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    //?} else {
    /*@Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (!entries.isEmpty()) {
            if (scrollY < 0) {
                selectedIndex = (selectedIndex + 1) % entries.size();
            } else if (scrollY > 0) {
                selectedIndex = (selectedIndex - 1 + entries.size()) % entries.size();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollY);
    }
    *///?}

    @Override
    //?if >= 26.1 {
    public void extractBackground(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
    //?} else {
    /*public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
    *///?}
        Font font = this.font;
        int screenW = this.width;
        int screenH = this.height;

        int count = entries.size();
        int shown = Math.min(VISIBLE_CARDS, count);
        int rowW = shown * CARD_W + Math.max(0, shown - 1) * CARD_GAP;
        int titleH = font.lineHeight + 6;
        int hintH = font.lineHeight + 6;
        int panelW = Math.max(180, rowW + PANEL_PADDING * 2);
        int panelH = PANEL_PADDING + titleH + CARD_H + hintH + PANEL_PADDING;
        int panelX = (screenW - panelW) / 2;
        int panelY = (screenH - panelH) / 2;

        gui.fill(0, 0, screenW, screenH, COLOR_SCRIM);
        gui.fill(panelX, panelY, panelX + panelW, panelY + panelH, WandScreen.COLOR_PANEL_BACKGROUND);

        String title = Compat.translatable("screen.wands.blueprint_carousel.title").getString()
                + " (" + (selectedIndex + 1) + "/" + count + ")";
        int titleW = font.width(title);
        gui.text(font, title, panelX + (panelW - titleW) / 2, panelY + PANEL_PADDING, WandScreen.COLOR_TEXT_PRIMARY);

        int startIdx = 0;
        if (count > VISIBLE_CARDS) {
            startIdx = Math.max(0, Math.min(count - VISIBLE_CARDS, selectedIndex - VISIBLE_CARDS / 2));
        }

        int cardsStartX = panelX + (panelW - rowW) / 2;
        int cardsY = panelY + PANEL_PADDING + titleH;
        String activeId = ClientRender.wand != null ? ClientRender.wand.getActiveBlueprintId() : "";

        for (int i = 0; i < shown; i++) {
            int entryIdx = startIdx + i;
            CarouselEntry entry = entries.get(entryIdx);
            int cx = cardsStartX + i * (CARD_W + CARD_GAP);
            int cy = cardsY;

            // Check mouse hover to also allow hovering a card
            if (mouseX >= cx && mouseX < cx + CARD_W && mouseY >= cy && mouseY < cy + CARD_H) {
                selectedIndex = entryIdx;
            }

            boolean isSelected = (entryIdx == selectedIndex);
            boolean isActive = entry.id.equals(activeId);

            int bg = isSelected ? COLOR_CARD_HIGHLIGHT : COLOR_CARD_NORMAL;
            gui.fill(cx, cy, cx + CARD_W, cy + CARD_H, bg);

            if (isSelected) {
                drawBorder(gui, cx, cy, CARD_W, CARD_H, COLOR_HIGHLIGHT_BORDER);
            } else if (isActive) {
                drawBorder(gui, cx, cy, CARD_W, CARD_H, COLOR_ACTIVE_BORDER);
            }

            // Icon centered near top of card
            int iconX = cx + (CARD_W - 16) / 2;
            int iconY = cy + 6;
            gui.item(entry.iconStack, iconX, iconY);

            // Name
            String shortName = font.plainSubstrByWidth(entry.name, CARD_W - 6);
            int nw = font.width(shortName);
            gui.text(font, shortName, cx + (CARD_W - nw) / 2, cy + 26, 0xFFFFFFFF);

            // Info (block count + dimensions)
            String shortInfo = font.plainSubstrByWidth(entry.info, CARD_W - 6);
            int iw = font.width(shortInfo);
            gui.text(font, shortInfo, cx + (CARD_W - iw) / 2, cy + 38, 0xFFBBBBBB);
        }

        String keyName = WandsModClient.getKeyName(WandsMod.WandKeys.BLUEPRINT);
        String hint = "Scroll wheel / Arrows  |  Release [" + keyName + "] to select";
        int hintW = font.width(hint);
        int hintY = cardsY + CARD_H + (panelY + panelH - cardsY - CARD_H - font.lineHeight) / 2;
        gui.text(font, hint, panelX + (panelW - hintW) / 2, hintY, COLOR_HINT_TEXT);
    }

    //?if >= 1.21.1 && < 26.1 {
    /*@Override
    public void renderBackground(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        // Handled in render()
    }
    *///?}

    @Override
    public void onClose() {
        resetHold();
        super.onClose();
    }

    private static void drawBorder(GuiGraphicsExtractor gui, int x, int y, int w, int h, int color) {
        gui.fill(x, y, x + w, y + 1, color);
        gui.fill(x, y + h - 1, x + w, y + h, color);
        gui.fill(x, y, x + 1, y + h, color);
        gui.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static int getBlueprintKeyCode() {
        var km = WandsModClient.reverseKeys.get(WandsMod.WandKeys.BLUEPRINT);
        if (km == null) return -1;
        return Compat.getKeyCode(km);
    }

    private static int getArrowKeyCode(WandsMod.WandKeys key) {
        var km = WandsModClient.reverseKeys.get(key);
        if (km == null) return -1;
        return Compat.getKeyCode(km);
    }
}
