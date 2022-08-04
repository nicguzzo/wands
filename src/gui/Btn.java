package net.nicguzzo.wands.gui;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.nicguzzo.wands.Colorf;

public class Btn extends Wdgt{
    int ox=2;
    int oy=3;
    Component text;
    Colorf c1=new Colorf(0.1f,0.1f,0.1f,0.8f);
    Colorf c2=new Colorf(0.4f,0.4f,0.40f,0.9f);
    boolean selected=false;
    public Btn(int x,int y,int w,int h,Component text){
        this.text=text;
        this.x=x;
        this.y=y;
        this.w=w;
        this.h=h;
    }
    public Btn(Component text){
        this(0,0,0,0,text);
    }
    public void onClick(int mx,int my){

    }
    public void click(int mx,int my){
        if (inside(mx, my)) {
            onClick(mx,my);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F, 1.0F));
        }
    }

    public void render(PoseStack poseStack, Font font, int mx, int my){
        float r,g,b,a;
        if(selected||inside(mx,my)){
            r=c2.r;g=c2.g;b=c2.b;a=c2.a;
        }else{
            r=c1.r;g=c1.g;b=c1.b;a=c1.a;
        }
        BufferBuilder bufferBuilder=init_quads();
        quad(bufferBuilder,x,y,w,h,r,g,b,a);
        if(selected) {
            quad(bufferBuilder,x-2,y-2,w+4,2,0.0f, 0.8f, 0.8f, 1.0f);
            quad(bufferBuilder,x-2,y+h,w+4,2,0.0f, 0.8f, 0.8f, 1.0f);
            quad(bufferBuilder,x-2,y-2,2,h+4,0.0f, 0.8f, 0.8f, 1.0f);
            quad(bufferBuilder,x+w,y-2,2,h+4,0.0f, 0.8f, 0.8f, 1.0f);
        }
        end_quads();
        int text_color=0xffffffff;
        if(selected)
            text_color=0xff000000;
        font.draw(poseStack,text ,x+ox,y+oy, text_color);
    }
}