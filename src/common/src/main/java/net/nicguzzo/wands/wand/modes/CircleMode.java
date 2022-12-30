package net.nicguzzo.wands.wand.modes;


import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;

public class CircleMode implements WandMode {
    public void place_in_buffer(Wand wand) {
        int plane = WandProps.getPlane(wand.wand_stack).ordinal();
        boolean fill = WandProps.getFlag(wand.wand_stack, WandProps.Flag.CFILLED);
        boolean even= WandProps.getFlag(wand.wand_stack, WandProps.Flag.EVEN);
        wand.block_buffer.reset();
        int diameter=0;
        if (wand.p1 != null && (wand.p2 || wand.preview)) {
            int xc = wand.p1.getX();
            int yc = wand.p1.getY();
            int zc = wand.p1.getZ();
            int px = wand.pos.getX() - xc;
            int py = wand.pos.getY() - yc;
            int pz = wand.pos.getZ() - zc;
            // log("circle plane:"+plane+ " fill: "+fill);
            int r = (int) Math.sqrt(px * px + py * py + pz * pz);
            int radius=r+1;
            if(r<1){
                return;
            }
            diameter=2*r;
            if (plane == 0) {// XZ;
                int x = r;
                int y = 0;
                int z = 0;
                int d = 1 - r;
                do {
                    drawCircleOctants(xc, yc, zc, x, y, z, plane,even,fill,wand);
                    z++;
                    if (d< 0 )
                        d+= 2 * z + 1;
                    else {
                        x--;
                        d+=  2 * (z - x) + 1;
                    }
                    //break;
                }while(z<=x);
                if (fill  && !even) {
                    if(r==1){
                        wand.add_to_buffer(xc, yc, zc);
                    }else {
                        int r2 = r * r;
                        for (z = -r; z <= r; z++) {
                            for (x = -r; x <= r; x++) {
                                if ( (x * x) + (z * z) < r2) {
                                    wand.add_to_buffer(xc + x, yc, zc + z);
                                }
                            }
                        }
                    }
                }
            } else if (plane == 1) {// XY;
                int x = r;
                int y = 0;
                int z = 0;
                int d = 1 - r;
                do {
                    drawCircleOctants(xc, yc, zc, x, y, z, plane,even,fill,wand);
                    y++;
                    if (d< 0 )
                        d+= 2 * y + 1;
                    else {
                        x--;
                        d+=  2 * (y - x) + 1;
                    }
                    //break;
                }while(y<=x);
                if (fill  && !even) {
                    if(r==1){
                        wand.add_to_buffer(xc, yc, zc);
                    }else {
                        int r2 = r * r;
                        for (y = -r; y <= r; y++) {
                            for (x = -r; x <= r; x++) {
                                if ((x * x) + (y * y) <= r2) {
                                    wand.add_to_buffer(xc + x, yc + y, zc);
                                }
                            }
                        }
                    }
                }
            } else if (plane == 2) {// YZ;

                int x = 0;
                int y = 0;
                int z = r;
                int d = 1 - r;
                do {
                    drawCircleOctants(xc, yc, zc, x, y, z, plane,even,fill,wand);
                    y++;
                    if (d< 0 )
                        d+= 2 * y + 1;
                    else {
                        z--;
                        d+=  2 * (y - z) + 1;
                    }
                    //break;
                }while(y<=z);
                if (fill && !even) {
                    int r2 = r * r;
                    for (z = -r; z <= r; z++) {
                        for (y = -r; y <= r; y++) {
                            if ((y * y) + (z * z) <= r2) {
                                wand.add_to_buffer(xc, yc + y, zc + z);
                            }
                        }
                    }
                }
            }
        }
        if (wand.preview) {
            wand.valid = (wand.block_buffer.get_length() > 0) && diameter< wand.wand_item.limit;
            if(wand.prnt && diameter>= wand.wand_item.limit){
                wand.player.displayClientMessage( Compat.literal("limit reached"), true);
            }
        }else{
            if(diameter>= wand.wand_item.limit){
                wand.player.displayClientMessage( Compat.literal("limit reached"), false);
            }
        }
    }
    void drawCircleOctants(int xc, int yc, int zc, int x, int y, int z, int plane,boolean even,boolean fill,Wand wand) {
        switch (plane) {

            case 0: {// XZ
                if(even){
                    wand.add_to_buffer(xc + x, yc, zc + z);
                    wand.add_to_buffer(xc + z, yc, zc + x);
                    wand.add_to_buffer(xc + z, yc, zc - x-1);
                    wand.add_to_buffer(xc + x, yc, zc - z-1);
                    wand.add_to_buffer(xc - x-1, yc, zc - z-1);
                    wand.add_to_buffer(xc - z-1, yc, zc - x-1);
                    wand.add_to_buffer(xc - z-1, yc, zc + x);
                    wand.add_to_buffer(xc - x-1, yc, zc + z);
                    if(fill){
//                        int xx=x;
                        //for (int fz = z; fz >0; fz--) {
//                            wand.add_to_buffer(xc+x, yc+1, zc+fz-1);
//                        }
                        //wand.add_to_buffer(xc, yc, zc);
                        //wand.add_to_buffer(xc-1, yc, zc);
                        //wand.add_to_buffer(xc-1, yc, zc-1);
                        //wand.add_to_buffer(xc, yc, zc-1);
                    }

                }else {
                    wand.add_to_buffer(xc + x, yc, zc + z);
                    if (x != z) {
                        wand.add_to_buffer(xc + z, yc, zc + x);
                        wand.add_to_buffer(xc + z, yc, zc - x);
                        wand.add_to_buffer(xc - x, yc, zc - z);
                    }
                    if (z > 0) {
                        wand.add_to_buffer(xc + x, yc, zc - z);
                        wand.add_to_buffer(xc - z, yc, zc - x);
                        wand.add_to_buffer(xc - z, yc, zc + x);
                        if (x != z) {
                            wand.add_to_buffer(xc - x, yc, zc + z);
                        }
                    }
                }
            }break;
            case 1: {// XY
                if(even){
                    wand.add_to_buffer(xc + x, yc + y, zc);
                    wand.add_to_buffer(xc + y, yc + x, zc);
                    wand.add_to_buffer(xc + y, yc - x-1, zc);
                    wand.add_to_buffer(xc - x-1, yc - y-1, zc);
                    wand.add_to_buffer(xc + x, yc - y-1, zc);
                    wand.add_to_buffer(xc - y-1, yc - x-1, zc);
                    wand.add_to_buffer(xc - y-1, yc + x, zc);
                    wand.add_to_buffer(xc - x-1, yc + y, zc);
                }else {
                    wand.add_to_buffer(xc + x, yc + y, zc);
                    if (x != y) {
                        wand.add_to_buffer(xc + y, yc + x, zc);
                        wand.add_to_buffer(xc + y, yc - x, zc);
                        wand.add_to_buffer(xc - x, yc - y, zc);
                    }
                    if (y > 0) {
                        wand.add_to_buffer(xc + x, yc - y, zc);
                        wand.add_to_buffer(xc - y, yc - x, zc);
                        wand.add_to_buffer(xc - y, yc + x, zc);
                        if (x != y) {
                            wand.add_to_buffer(xc - x, yc + y, zc);
                        }
                    }
                }
            }break;
            case 2: {// YZ
                if(even){
                    wand.add_to_buffer(xc, yc + z  , zc + y);
                    wand.add_to_buffer(xc, yc + y  , zc + z);
                    wand.add_to_buffer(xc, yc + y  , zc - z-1);
                    wand.add_to_buffer(xc, yc - z-1, zc - y-1);
                    wand.add_to_buffer(xc, yc + z  , zc - y-1);
                    wand.add_to_buffer(xc, yc - y-1, zc - z-1);
                    wand.add_to_buffer(xc, yc - y-1, zc + z);
                    wand.add_to_buffer(xc, yc - z-1, zc + y);
                }else {
                    wand.add_to_buffer(xc, yc + y, zc + z);
                    if (y != z) {
                        wand.add_to_buffer(xc, yc + z, zc + y);
                        wand.add_to_buffer(xc, yc + z, zc - y);
                        wand.add_to_buffer(xc, yc - y, zc - z);
                    }
                    if (z > 0) {
                        wand.add_to_buffer(xc, yc + y, zc - z);
                        wand.add_to_buffer(xc, yc - z, zc - y);
                        wand.add_to_buffer(xc, yc - z, zc + y);
                        if (y != z) {
                            wand.add_to_buffer(xc, yc - y, zc + z);
                        }
                    }
                }
            }break;
        }
    }
}
