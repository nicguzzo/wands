package net.nicguzzo.wands.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

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
    public void render(PoseStack poseStack, Font font, int mx, int my) {
        int k=0;
        if(label!=null) {
            font.draw(poseStack, label, x, y, 0xff000000);
            k++;
        }
        for (int j=0;j<selections.size();j++) {
            Btn btn=selections.get(j);
            btn.x=x;
            btn.y=y+h*k;
            btn.w=w;
            btn.h=h;
            btn.selected=(this.selected==j);
            btn.render(poseStack,font,mx,my);
            k++;
        }
    }
    public void click(int mx,int my) {
        for (int j = 0; j < selections.size(); j++) {
            selections.get(j).click(mx,my);
        }
    }
};