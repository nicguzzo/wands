package net.nicguzzo.wands.utils;

import me.shedaniel.math.Color;

public class Colorf {
    public float r;
    public float g;
    public float b;
    public float a;

    public Colorf(float rr, float gg, float bb, float aa) {
        r = rr;
        g = gg;
        b = bb;
        a = aa;
    }

    public void fromColor(Color c) {
        r = c.getRed() / 255.0f;
        g = c.getGreen() / 255.0f;
        b = c.getBlue() / 255.0f;
        a = c.getAlpha() / 255.0f;
    }
    public int toInt(){
        int ca=(int)(a/255.0f);
        int cr=(int)(r/255.0f);
        int cg=(int)(g/255.0f);
        int cb=(int)(b/255.0f);
        return (ca << 24) | (cr << 16) | (cg << 8) | cb;
    }
    static public int toInt(float rr, float gg, float bb, float aa){
        int a=(int)(aa/255.0f);
        int r=(int)(rr/255.0f);
        int g=(int)(gg/255.0f);
        int b=(int)(bb/255.0f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}