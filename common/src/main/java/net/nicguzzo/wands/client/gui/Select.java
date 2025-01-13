package net.nicguzzo.wands.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
#if MC >= "1200"
import net.minecraft.client.gui.GuiGraphics;
#endif
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
    #if MC < "1200"
        public void render(PoseStack poseStack, Font font, int mx, int my){
    #else
        public void render(GuiGraphics gui, Font font, int mx, int my){
    #endif
        int k=0;
        if(label!=null) {
            #if MC < "1200"
            font.draw(poseStack, label, x, y, 0xff000000);
            #else
                gui.drawString(font, label, x, y, 0xff000000,false);
            #endif
            k++;
        }
        for (int j=0;j<selections.size();j++) {
            Btn btn=selections.get(j);
            btn.x=x;
            btn.y=y+h*k;
            btn.w=w;
            btn.h=h;
            btn.selected=(this.selected==j);
            #if MC < "1200"
                btn.render(poseStack,font,mx,my);
            #else
                btn.render(gui,font,mx,my);
            #endif
            k++;
        }
    }
    public void click(int mx,int my) {
        for (int j = 0; j < selections.size(); j++) {
            selections.get(j).click(mx,my);
        }
    }
};