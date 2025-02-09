package net.nicguzzo.wands.wand.modes;

import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;

public class LineMode extends WandMode {
    public void place_in_buffer(Wand wand) {
        wand.block_buffer.reset();
        if (wand.getP1() != null && (wand.getP2() !=null || wand.preview)) {
            int x1 = wand.getP1().getX();
            int y1 = wand.getP1().getY();
            int z1 = wand.getP1().getZ();
            int x2 = wand.pos.getX();
            int y2 = wand.pos.getY();
            int z2 = wand.pos.getZ();
            int dx, dy, dz, xs, ys, zs, lp1, lp2;
            dx = Math.abs(x2 - x1);
            dy = Math.abs(y2 - y1);
            dz = Math.abs(z2 - z1);
            if (x2 > x1) {
                xs = 1;
            } else {
                xs = -1;
            }
            if (y2 > y1) {
                ys = 1;
            } else {
                ys = -1;
            }
            if (z2 > z1) {
                zs = 1;
            } else {
                zs = -1;
            }
            wand.add_to_buffer(x1, y1, z1);
            // X
            if (dx >= dy && dx >= dz) {
                lp1 = 2 * dy - dx;
                lp2 = 2 * dz - dx;
                while (x1 != x2) {
                    x1 += xs;
                    if (lp1 >= 0) {
                        y1 += ys;
                        lp1 -= 2 * dx;
                    }
                    if (lp2 >= 0) {
                        z1 += zs;
                        lp2 -= 2 * dx;
                    }
                    lp1 += 2 * dy;
                    lp2 += 2 * dz;
                    wand.add_to_buffer(x1, y1, z1);
                }
            } else if (dy >= dx && dy >= dz) {
                lp1 = 2 * dx - dy;
                lp2 = 2 * dz - dy;
                while (y1 != y2) {
                    y1 += ys;
                    if (lp1 >= 0) {
                        x1 += xs;
                        lp1 -= 2 * dy;
                    }
                    if (lp2 >= 0) {
                        z1 += zs;
                        lp2 -= 2 * dy;
                    }
                    lp1 += 2 * dx;
                    lp2 += 2 * dz;
                    wand.add_to_buffer(x1, y1, z1);
                }
            } else {
                lp1 = 2 * dy - dz;
                lp2 = 2 * dx - dz;
                while (z1 != z2) {
                    z1 += zs;
                    if (lp1 >= 0) {
                        y1 += ys;
                        lp1 -= 2 * dz;
                    }
                    if (lp2 >= 0) {
                        x1 += xs;
                        lp2 -= 2 * dz;
                    }
                    lp1 += 2 * dy;
                    lp2 += 2 * dx;
                    wand.add_to_buffer(x1, y1, z1);
                }
            }
        }
        wand.validate_buffer();
    }
}
