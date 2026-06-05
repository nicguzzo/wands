package net.nicguzzo.wands.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.items.PaletteTooltip;

import java.util.List;

public class PaletteClientTooltip implements ClientTooltipComponent {

    /** Fabric tooltip callback — returns null for non-palette data */
    public static ClientTooltipComponent tryCreate(TooltipComponent data) {
        return data instanceof PaletteTooltip pt ? new PaletteClientTooltip(pt) : null;
    }
    private static final int SLOT_SIZE = 18;
    private static final int MAX_COLUMNS = 9;
    private static final int PADDING = 1;

    private final List<ItemStack> items;

    public PaletteClientTooltip(PaletteTooltip tooltip) {
        this.items = tooltip.items();
    }

    //?if >= 1.21.11{
    @Override
    public int getHeight(Font font) {
        if (items.isEmpty()) return 0;
        int rows = (items.size() + MAX_COLUMNS - 1) / MAX_COLUMNS;
        return rows * SLOT_SIZE + PADDING * 2;
    }
    //?}else{
    
    /*@Override
    public int getHeight() {
        if (items.isEmpty()) return 0;
        int rows = (items.size() + MAX_COLUMNS - 1) / MAX_COLUMNS;
        return rows * SLOT_SIZE + PADDING * 2;
    }
    
    *///?}

    @Override
    public int getWidth(Font font) {
        if (items.isEmpty()) return 0;
        int cols = Math.min(items.size(), MAX_COLUMNS);
        return cols * SLOT_SIZE + PADDING * 2;
    }

    @Override
    //?if >= 1.21.11{
        //?if >= 26.1{
        /*public void extractImage(Font font, int x, int y, int width, int height, GuiGraphics gui) {
        *///?}else{
        public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics gui) {
        //?}
            renderGrid(font, x, y, gui);
        }
    //?}else{
    
    /*public void renderImage(Font font, int x, int y, GuiGraphics gui) {
        renderGrid(font, x, y, gui);
    }
    
    *///?}

    private void renderGrid(Font font, int x, int y, GuiGraphics gui) {
        if (items.isEmpty()) return;

        int cols = Math.min(items.size(), MAX_COLUMNS);
        int rows = (items.size() + MAX_COLUMNS - 1) / MAX_COLUMNS;

        // Draw items
        for (int i = 0; i < items.size(); i++) {
            int col = i % MAX_COLUMNS;
            int row = i / MAX_COLUMNS;
            int itemX = x + PADDING + col * SLOT_SIZE;
            int itemY = y + PADDING + row * SLOT_SIZE;
            gui.renderItem(items.get(i), itemX, itemY);
        }
    }
}
