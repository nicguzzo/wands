package net.nicguzzo.wands.wand.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;

import java.util.stream.IntStream;

public class SphereMode extends WandMode {
    PerlinNoise noise=null;


    @Override
    public void place_in_buffer(Wand wand) {
        //sif(noise==null) {
        //    noise = PerlinNoise.create(wand.level.random, IntStream.rangeClosed(-4, 4));
        //}
        wand.block_buffer.reset();
        int diameter=0;
        if (wand.getP1() != null && (wand.getP2() !=null || wand.preview)) {
            int xc = wand.getP1().getX();
            int yc = wand.getP1().getY();
            int zc = wand.getP1().getZ();
            BlockPos radiusPos = (wand.getP2() != null) ? wand.getP2() : wand.getEffectiveEndPos();
            int px = radiusPos.getX() - xc;
            int py = radiusPos.getY() - yc;
            int pz = radiusPos.getZ() - zc;
            // log("circle plane:"+plane+ " fill: "+fill);
            int r = (int) Math.sqrt(px * px + py * py + pz * pz);
            //int radius=r+1;
            wand.radius = r;
            if(r<1){
                return;
            }
            int r2=r*r;

            int x1=xc-r;
            int y1=yc-r;
            int z1=zc-r;
            int x2=xc+r;
            int y2=yc+r;
            int z2=zc+r;
            // Pre-set min/max Y for gradient palette mode
            wand.block_buffer.min_y = y1;
            wand.block_buffer.max_y = y2;
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
                        //n=noise.getValue(i,j,k);
                        if( xx + yy + zz <= r2+n )
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
