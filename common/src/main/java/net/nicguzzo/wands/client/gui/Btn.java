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
#if MC >= "1200"
import net.minecraft.client.gui.GuiGraphics;
#endif
public class Btn extends Wdgt{
    int ox=2;
    int oy=2;
    Component text;
    int c1=new Colorf(0.1f,0.1f,0.1f,0.8f).toInt();
    int c2=new Colorf(0.4f,0.4f,0.40f,0.9f).toInt();
    int c_disabled=new Colorf(0.7f,0.7f,0.70f,0.7f).toInt();
    int c_selected=  new Colorf(0.0f, 0.8f, 0.8f, 0.5f).toInt();
    boolean selected=false;
    public boolean disabled=false;
    BtnClick on_click=null;
    public Btn(int x,int y,int w,int h,Component text){
        this.text=text;
        this.x=x;
        this.y=y;
        this.w=w;
        this.h=h;
    }
    public Btn(int x,int y,int w,int h,Component text,BtnClick click){
        this.text=text;
        this.x=x;
        this.y=y;
        this.w=w;
        this.h=h;
        this.on_click=click;
    }
    public Btn(Component text){
        this(0,0,0,0,text);
    }
    public Btn(Component text,BtnClick click){
        this(0,0,0,0,text,click);
    }

    public void onClick(int mx,int my){

    }
    public void click(int mx,int my){
        if (inside(mx, my)) {
            if(!disabled) {
                onClick(mx, my);
            }
            if(on_click!=null && !disabled) {
                on_click.onClick(mx, my);
            }
            if(!disabled) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F));
            }
        }
    }
    public void render(GuiGraphics gui, Font font, int mx, int my){
        int c;
        if (disabled) {
            c = c_disabled;
        } else if (selected || inside(mx, my)) {
            c=c2;
        } else {
            c=c1;
        }
        gui.fill(x,y,x+w,y+h,c);
        if (selected) {
            gui.fill(x,y,x+w,y+h,c_selected);
        }
        int text_color = 0xffffffff;
        if (selected)
            text_color = 0xff000000;
        if (disabled)
            text_color = 0xff7f7f7f;
        net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
        gui.drawString(client.font, text, x + ox, y + oy, text_color, false);

    }
}
