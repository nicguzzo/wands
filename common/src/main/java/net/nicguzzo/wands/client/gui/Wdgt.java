package net.nicguzzo.wands.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.nicguzzo.wands.utils.Compat;

public class Wdgt {
    int x;
    int y;
    int w;
    int h;
    public boolean visible = true;

    public void render(GuiGraphics gui, Font font, int mx, int my) {
    }

    public void click(int mx, int my) {

    }

    public boolean inside(int mx, int my) {
        return mx >= x && mx <= (x + w) && my >= y && my <= (y + h);
    }
}