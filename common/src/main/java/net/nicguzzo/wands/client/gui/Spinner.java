package net.nicguzzo.wands.client.gui;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.nicguzzo.wands.utils.Colorf;
import net.nicguzzo.wands.utils.Compat;
import net.minecraft.client.gui.GuiGraphics;

public class Spinner  extends Wdgt{
    int value;
    public int inc_val=1;
    int min;
    int max;
    Btn inc;
    Btn dec;
    public boolean label_side=false;
    public int shift_inc_val=10;
    public int ctrl_inc_val=100;
    public int shift_ctrl_inc_val=1000;
    Component label=null;
    int col=  new Colorf(0.4f, 0.4f, 0.40f, 0.9f).toInt();
    public int label_col=  new Colorf(0.0f, 0.0f, 0.0f, 1.0f).toInt();
    public int label_bg =  new Colorf(0.5f, 0.5f, 0.50f, 0.1f).toInt();
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
                int iv=get_inc();
                if (value + iv <= max) {
                    value += iv;
                } else {
                    value = max;
                }
                onInc(mx,my,value);
            }
        };
        inc.ox=0;
        inc.oy=0;
        dec=new Btn(x+w-10,y+h/2,10,h/2, Compat.literal("-"))
        {
            public void onClick(int mx,int my){
                int iv=get_inc();
                if (value - iv >= min) {
                    value -= iv;
                } else {
                    value = min;
                }
                onDec(mx,my,value);
            }
        };
        dec.ox=0;
        dec.oy=0;
    }
    private int get_inc(){
        if(Screen.hasControlDown() && Screen.hasShiftDown()){
            return shift_ctrl_inc_val;
        }else {
            if (Screen.hasControlDown()) {
                return ctrl_inc_val;
            }else if (Screen.hasShiftDown()) {
                return shift_inc_val;
            }else{
                return inc_val;
            }
        }
    }
    public void onInc(int mx,int my,int v){
    }
    public void onDec(int mx,int my,int v){
    }
        public void render(GuiGraphics gui, Font font, int mx, int my){
        int fh=0;
        if(label!=null) {
            int lw=font.width(label);
            gui.drawString(font, label, x-lw-1, y+3, 0xff000000,false);
            if(!label_side) {
                fh = font.lineHeight;
            }
            gui.fill(x-lw-2,y+ fh,x-2,y+ fh +h,label_bg);
            gui.drawString(font, label, x - lw - 1, y + 3, label_col, false);
        }
        int sw=font.width(String.valueOf(value));

        gui.fill(x,y+ fh,x+w,y+ fh +h,col);
        inc.y = y + fh;

        dec.y=y+h/2+fh;
        inc.render(gui,font,mx,my);
        gui.drawString(font,String.valueOf(value), x+ w - 12 - sw, y + fh + 3, 0xff000000,false);
        dec.render(gui,font,mx,my);
    }
    public void click(int mx,int my){
        dec.click(mx,my);
        inc.click(mx,my);
    }
}
