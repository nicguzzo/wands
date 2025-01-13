package net.nicguzzo.wands.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.nicguzzo.wands.utils.Colorf;

public class Btn extends Wdgt {
    int ox = 2;
    int oy = 2;
    Component text;
    Colorf c1 = new Colorf(0.1f, 0.1f, 0.1f, 0.8f);
    Colorf c2 = new Colorf(0.4f, 0.4f, 0.40f, 0.9f);
    Colorf c_disabled = new Colorf(0.7f, 0.7f, 0.70f, 0.7f);
    boolean selected = false;
    public boolean disabled = false;
    BtnClick on_click = null;

    public Btn(int x, int y, int w, int h, Component text) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public Btn(int x, int y, int w, int h, Component text, BtnClick click) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.on_click = click;
    }

    public Btn(Component text) {
        this(0, 0, 0, 0, text);
    }

    public Btn(Component text, BtnClick click) {
        this(0, 0, 0, 0, text, click);
    }

    public void onClick(int mx, int my) {

    }

    public void click(int mx, int my) {
        if (inside(mx, my)) {
            onClick(mx, my);
            if (on_click != null && !disabled) {
                on_click.onClick(mx, my);
            }
            if (!disabled) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F, 1.0F));
            }
        }
    }

    public void render(PoseStack poseStack, Font font, int mx, int my) {
        float r, g, b, a;
        if (disabled) {
            r = c_disabled.r;
            g = c_disabled.g;
            b = c_disabled.b;
            a = c_disabled.a;
        } else if (selected || inside(mx, my)) {
            r = c2.r;
            g = c2.g;
            b = c2.b;
            a = c2.a;
        } else {
            r = c1.r;
            g = c1.g;
            b = c1.b;
            a = c1.a;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = init_quads();
        quad(bufferBuilder, x, y, w, h, r, g, b, a);
        if (selected) {
            int border = 1;
            quad(bufferBuilder, x, y, w, h, 0.0f, 0.8f, 0.8f, 0.5f);
            /*quad(bufferBuilder,x-border,y-border,w+border*2,border,0.0f, 0.8f, 0.8f, 0.5f);
            quad(bufferBuilder,x-border,y+h,w+border*2,border,0.0f, 0.8f, 0.8f, 0.5f);
            quad(bufferBuilder,x-border,y-border,border,h+border*2,0.0f, 0.8f, 0.8f, 0.5f);
            quad(bufferBuilder,x+w,y-border,border,h+border*2,0.0f, 0.8f, 0.8f, 0.5f);*/
        }

        end_quads(bufferBuilder);
        RenderSystem.disableBlend();
        int text_color = 0xffffffff;
        if (selected) text_color = 0xff000000;
        font.draw(poseStack, text, x + ox, y + oy, text_color);
    }
}
