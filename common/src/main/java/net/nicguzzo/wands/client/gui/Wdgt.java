package net.nicguzzo.wands.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.Font;
import net.nicguzzo.wands.utils.Compat;
#if MC >= "1212"
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderProgram;
#endif
#if MC >= "1200"
import net.minecraft.client.gui.GuiGraphics;
#endif
public class Wdgt{
    int x;
    int y;
    int w;
    int h;
    public boolean visible=true;
     #if MC < "1200"
        public void render(PoseStack poseStack, Font font, int mx, int my){
    #else
        public void render(GuiGraphics gui, Font font, int mx, int my){
    #endif
    }
    public void click(int mx,int my){

    }
    public BufferBuilder init_quads() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Compat.set_shader_pos_color();
        #if MC < "1210"
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        Compat.set_render_quads_pos_col(bufferBuilder);
        return bufferBuilder;
        #else
        return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        #endif
    }

    public void end_quads(BufferBuilder buffer) {
        #if MC < "1210"
        Tesselator.getInstance().end();
        #else
            try {
                BufferUploader.drawWithShader(buffer.buildOrThrow());
            }catch (Exception ignore){

            }
        #endif
        RenderSystem.disableBlend();
    }
    void quad(BufferBuilder bufferBuilder,float x,float y,float w, float h,float r,float g,float b, float a){
        #if MC < "1210"
            bufferBuilder.vertex(x,   y  , 0.0F).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(x,   y+h, 0.0F).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(x+w, y+h, 0.0F).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(x+w, y  , 0.0F).color(r, g, b, a).endVertex();
        #else
            bufferBuilder.addVertex(x,   y  , 0.0F).setColor(r, g, b, a);
            bufferBuilder.addVertex(x,   y+h, 0.0F).setColor(r, g, b, a);
            bufferBuilder.addVertex(x+w, y+h, 0.0F).setColor(r, g, b, a);
            bufferBuilder.addVertex(x+w, y  , 0.0F).setColor(r, g, b, a);
        #endif
    }
    public boolean inside(int mx,int my){
        return mx>=x && mx<=(x+w) && my>=y && my<=(y+h);
    }
}