package net.nicguzzo.wands.wand.modes;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.nicguzzo.wands.networking.Networking;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;

public class RockMode extends WandMode {
    SimplexNoise noise=null;
    public long seed=123427349;
    public RandomSource random = RandomSource.create();
    int rx=0,ry=0,rz=0;

    @Override
    public void randomize(){
        if (Platform.getEnvironment() == Env.CLIENT) {
            rx = random.nextInt(-1000000, 1000000);
            ry = random.nextInt(-1000000, 1000000);
            rz = random.nextInt(-1000000, 1000000);
            last_pos=null;
            Networking.sendSyncRockPacket(rx,ry,rz);
        }
    }

    public void set_random_pos(int _rx,int _ry, int _rz){
        rx=_rx;
        ry=_ry;
        rz=_rz;
        last_pos=null;
    }

    @Override
    public void place_in_buffer(Wand wand) {
        if(noise==null) {
            random.setSeed(seed);
            noise = new SimplexNoise(random);
            randomize();
        }
        if(!need_update(wand,true)){
            return;
        }
        //if(wand.preview && last_pos!=null && last_pos.getX()==wand.pos.getX() && last_pos.getY()==wand.pos.getY() && last_pos.getZ()==wand.pos.getZ()){
        //    if(wand.block_buffer.get_length()>0) {
        //        wand.valid = true;
        //    }
        //    return;
        //}
        //last_pos=wand.pos;
        wand.block_buffer.reset();
        if (wand.pos != null) {
            int xc;
            int yc;
            int zc;
            xc = wand.pos.getX();
            yc = wand.pos.getY();
            zc = wand.pos.getZ();
            int r = WandProps.getVal(wand.wand_stack, WandProps.Value.ROCK_RADIUS);
            int nf= WandProps.getVal(wand.wand_stack, WandProps.Value.ROCK_NOISE);
            //int r = 2;
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
            int x,y,z;
            int xx,yy,zz;
            double n;
            //WandsMod.log( String.format("%d,%d,%d",x1,y1,z1),true);
            //WandsMod.log( String.format("%d,%d,%d",rx,ry,rz),true);

            x=-r;
            for(int i=x1;i<=x2;i++,x++){
                xx=x*x;
                y=-r;
                for(int j=y1;j<=y2;j++,y++){
                    yy=y*y;
                    z=-r;
                    for(int k=z1;k<=z2;k++,z++){
                        zz=z*z;
                        n=noise.getValue(x+rx,y+ry,z+rz)*nf;
                        if( xx + yy + zz < r2+n )
                        {
                            wand.add_to_buffer(i, j, k);
                        }
                    }
                }
            }
            wand.block_buffer.renmove_neighbors_lte(1);
            //wand.validate_buffer();
            wand.valid=true;
        }
    }
}
