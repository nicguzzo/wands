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

    public BufferBuilder init_quads() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Compat.set_shader_pos_color();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        return bufferBuilder;
    }

    public void end_quads(BufferBuilder buffer) {
        Tesselator.getInstance().end();
        RenderSystem.disableBlend();
    }

    void quad(BufferBuilder bufferBuilder, float x, float y, float w, float h, float r, float g, float b, float a) {
        bufferBuilder.vertex(x, y, 0.0F).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(x, y + h, 0.0F).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(x + w, y + h, 0.0F).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(x + w, y, 0.0F).color(r, g, b, a).endVertex();
    }

    public boolean inside(int mx, int my) {
        return mx >= x && mx <= (x + w) && my >= y && my <= (y + h);
    }
}