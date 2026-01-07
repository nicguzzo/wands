package net.nicguzzo.wands.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
import java.util.Vector;

public class Select extends Wdgt{
    Component label=null;
    public Vector<Btn> selections=new Vector<>();
    public int selected=-1;
    public Select(int x,int y,int w,int h,Component label) {
        this.x=x;
        this.y=y;
        this.w=w;
        this.h=h;
        this.label=label;
    }

    public void add(Btn b){
        selections.add(b);
    }
    public void render(GuiGraphics gui, Font font, int mx, int my){
        int k=0;
        if(label!=null) {
            gui.drawString(font, label, x, y, 0xff000000,false);
            k++;
        }
        for (int j=0;j<selections.size();j++) {
            Btn btn=selections.get(j);
            btn.x=x;
            btn.y=y+h*k;
            btn.w=w;
            btn.h=h;
            btn.selected=(this.selected==j);
            btn.render(gui,font,mx,my);
            k++;
        }
    }
    public void click(int mx,int my) {
        for (int j = 0; j < selections.size(); j++) {
            selections.get(j).click(mx,my);
        }
    }
};