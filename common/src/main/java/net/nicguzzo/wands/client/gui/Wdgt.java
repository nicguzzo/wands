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
/*
    public VertexConsumer init_quads(GuiGraphics gui) {
        //RenderSystem.enableBlend();
        //RenderSystem.defaultBlendFunc();
        //Compat.set_shader_pos_color();
        //return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        return gui.
    }

    public void end_quads(BufferBuilder buffer) {
        try {
            buffer.buildOrThrow();
            //BufferUploader.drawWithShader(buffer.buildOrThrow());
        } catch (Exception ignore) {

        }
        //RenderSystem.disableBlend();
    }

    void quad(BufferBuilder bufferBuilder, float x, float y, float w, float h, float r, float g, float b, float a) {
        bufferBuilder.addVertex(x, y, 0.0F).setColor(r, g, b, a);
        bufferBuilder.addVertex(x, y + h, 0.0F).setColor(r, g, b, a);
        bufferBuilder.addVertex(x + w, y + h, 0.0F).setColor(r, g, b, a);
        bufferBuilder.addVertex(x + w, y, 0.0F).setColor(r, g, b, a);
    }
*/
    public boolean inside(int mx, int my) {
        return mx >= x && mx <= (x + w) && my >= y && my <= (y + h);
    }
}