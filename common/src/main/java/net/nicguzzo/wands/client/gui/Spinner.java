package net.nicguzzo.wands.client.gui;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.nicguzzo.wands.utils.Compat;
#if MC >= "1200"
import net.minecraft.client.gui.GuiGraphics;
#endif
public class Spinner  extends Wdgt{
    int value;
    public int inc_val=1;
    int min;
    int max;
    Btn inc;
    Btn dec;
    public boolean label_side=false;
    public int shift_inc_val=10;
    Component label=null;
    public Spinner(int _value,int min,int max,int x,int y,int w,int h,Component label){
        this.value=_value;
        this.min=min;
        this.max=max;
        this.x=x;
        this.y=y;
        this.w=w;
        this.h=h;
        this.label=label;
        inc=new Btn(x+w-10,y,10,h/2, Compat.literal("+"))
        {
            public void onClick(int mx,int my){
                int iv=inc_val;
                if(Screen.hasControlDown()){
                    value=max;
                }else {
                    if (Screen.hasShiftDown()) {
                        iv = shift_inc_val;
                    }
                    if (value + iv <= max) {
                        value += iv;
                    } else {
                        value = max;
                    }
                }
                onInc(mx,my,value);
            }
        };
        inc.ox=0;
        inc.oy=0;
        dec=new Btn(x+w-10,y+h/2,10,h/2, Compat.literal("-"))
        {
            public void onClick(int mx,int my){
                int iv=inc_val;
                if(Screen.hasControlDown()){
                    value=min;
                }else {
                    if (Screen.hasShiftDown()) {
                        iv = shift_inc_val;
                    }
                    if (value - iv >= min) {
                        value -= iv;
                    } else {
                        value = min;
                    }
                }
                onDec(mx,my,value);
            }
        };
        dec.ox=0;
        dec.oy=0;
    }
    public void onInc(int mx,int my,int v){
    }
    public void onDec(int mx,int my,int v){
    }
    #if MC < "1200"
        public void render(PoseStack poseStack, Font font, int mx, int my){
    #else
        public void render(GuiGraphics gui, Font font, int mx, int my){
    #endif
        int fh=0;
        if(label!=null) {
            int lw=font.width(label);

            #if MC < "1200"
                font.draw(poseStack, label, x-lw-1, y+3, 0xff000000);
            #else
                gui.drawString(font, label, x-lw-1, y+3, 0xff000000,false);
            #endif
            if(!label_side) {
                fh = font.lineHeight;
            }
        }
        int sw=font.width(String.valueOf(value));

        BufferBuilder bufferBuilder=init_quads();
        quad(bufferBuilder,x,y+fh,w,h,0.4f,0.4f,0.40f,0.9f);
        end_quads(bufferBuilder);
        inc.y=y+fh;

        dec.y=y+h/2+fh;
        #if MC < "1200"
            inc.render(poseStack,font,mx,my);
            font.draw(poseStack, String.valueOf(value), x+ w - 12 - sw, y + fh + 3, 0xff000000);
            dec.render(poseStack,font,mx,my);
        #else
            inc.render(gui,font,mx,my);
            gui.drawString(font,String.valueOf(value), x+ w - 12 - sw, y + fh + 3, 0xff000000,false);
            dec.render(gui,font,mx,my);
        #endif
    }
    public void click(int mx,int my){
        dec.click(mx,my);
        inc.click(mx,my);
    }
}