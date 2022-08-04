package net.nicguzzo.wands;

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

    void fromColor(Color c) {
        r = c.getRed() / 255.0f;
        g = c.getGreen() / 255.0f;
        b = c.getBlue() / 255.0f;
        a = c.getAlpha() / 255.0f;
    }
}