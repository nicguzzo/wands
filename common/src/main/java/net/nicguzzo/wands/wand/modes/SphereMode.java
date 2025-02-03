package net.nicguzzo.wands.wand.modes;

import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;

import java.util.stream.IntStream;

public class SphereMode implements WandMode {
    PerlinNoise noise=null;


    @Override
    public void place_in_buffer(Wand wand) {
        if(noise==null) {
            noise = PerlinNoise.create(wand.level.random, IntStream.rangeClosed(-4, 4));
        }
        wand.block_buffer.reset();
        int diameter=0;
        if (wand.getP1() != null && (wand.getP2() !=null || wand.preview)) {
            int xc = wand.getP1().getX();
            int yc = wand.getP1().getY();
            int zc = wand.getP1().getZ();
            int px = wand.pos.getX() - xc;
            int py = wand.pos.getY() - yc;
            int pz = wand.pos.getZ() - zc;
            // log("circle plane:"+plane+ " fill: "+fill);
            int r = (int) Math.sqrt(px * px + py * py + pz * pz);
            //int radius=r+1;
            if(r<1){
                return;
            }
            int r2=r*r;
            //diameter=2*r;
            int margin=2;
            int x1=xc-r-margin;
            int y1=yc-r-margin;
            int z1=zc-r-margin;
            int x2=xc+r+margin;
            int y2=yc+r+margin;
            int z2=zc+r+margin;
            int x=-r;
            int y=-r;
            int z=-r;
            int xx,yy,zz;
            double n=0;
            //WandsMod.log( String.format("%d,%d,%d",x1,y1,z1),true);
            for(int i=x1;i<=x2;i++,x++){
                xx=x*x;
                y=-r;
                for(int j=y1;j<=y2;j++,y++){
                    yy=y*y;
                    z=-r;
                    for(int k=z1;k<=z2;k++,z++){
                        zz=z*z;
                        n=noise.getValue(i,j,k);
                        if( xx + yy + zz < r2+n )
                        {

                            wand.add_to_buffer(i, j, k);
                        }
                    }
                }
            }
            wand.validate_buffer();
        }
    }
}
