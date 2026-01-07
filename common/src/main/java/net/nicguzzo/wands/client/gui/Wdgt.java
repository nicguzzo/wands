package net.nicguzzo.wands.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class Wdgt{
    int x;
    int y;
    int w;
    int h;
    public boolean visible=true;
    public void render(GuiGraphics gui, Font font, int mx, int my){
    }
    public void click(int mx,int my){

    }
    public boolean inside(int mx,int my){
        return mx>=x && mx<=(x+w) && my>=y && my<=(y+h);
    }
}