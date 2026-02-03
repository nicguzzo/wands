package net.nicguzzo.wands.utils;

import java.util.ArrayList;
import java.util.List;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.config.WandsConfig;


public class BlockBuffer{
    
    public  int max=0;
    private  int length=0;
    public  int[] buffer_x=null;
    public  int[] buffer_y=null;
    public  int[] buffer_z=null;
    public  BlockState[] state=null;
    public  Item[] item=null;
    public int min_y = 1000;
    public int max_y = -1000;

    public BlockBuffer(int n){
        max=n;
        buffer_x=new int[max];
        buffer_y=new int[max];
        buffer_z=new int[max];
        state=new BlockState[max];
        item=new Item[max];
    }

    static public List<Item> denied_item=new ArrayList<Item>();

    public void reset(){
        length = 0;
        min_y  = 1000;
        max_y  = -1000;
    }
    public int get_length(){
        return length;
    }
    public boolean in_buffer(BlockPos p){
        for(int i=0;i<length && i<max;i++){
            if( p.getX() == buffer_x[i] &&
                p.getY() == buffer_y[i] &&
                p.getZ() == buffer_z[i]
            ){
                return true;
            }
        }
        return false;
    }
    public void calc_min_max(){
        for(int i=0;i<length && i<max;i++){
            int y=buffer_y[i];
            if(y<min_y){
                min_y=y;
            }
            if(y>max_y){
                max_y=y;
            }
        }
    }
    public BlockPos get(int i){
        if(i<max)
            return new BlockPos(buffer_x[i],buffer_y[i],buffer_z[i]);
        return null;
    }    
    public void set(int i,int x,int y,int z){
        if(i<max){
            buffer_x[i]=x;
            buffer_y[i]=y;
            buffer_z[i]=z;
            //if(y<min_y){
            //    min_y=y;
            //}
            //if(y>max_y){
            //    max_y=y;
            //}
        }
    }
    public void add(int x, int y, int z,BlockState s,Item i) {
        if(length<max){
            buffer_x[length]=x;
            buffer_y[length]=y;
            buffer_z[length]=z;
            state[length]=s;
            item[length]=i;
            length++;
            //if(y<min_y){
            //    min_y=y;
            //}
            //if(y>max_y){
            //    max_y=y;
            //}
        }
    }
    public boolean add(int x, int y, int z, Wand w,BlockState with_state){
        if(length<max){
            BlockState st=w.get_state(y,with_state);
            if(st!=null){
                Item it=w.get_item(st);
                if(!WandsConfig.denied.contains(st.getBlock())){
                    add(x,y,z,st,it);
                    //buffer_x[length]=x;
                    //buffer_y[length]=y;
                    //buffer_z[length]=z;
                    //state[length]=st;
                    //item[length]=it;
                    //length++;
                    return true;
                }
            }
        }
        return false;
    }
    public void set(int i,BlockPos p){
        set(i,p.getX(),p.getY(),p.getZ());
    }

    public void add(BlockPos p,Wand w,BlockState with_state){
        add(p.getX(),p.getY(),p.getZ(),w,with_state);
    }

    public void renmove_neighbors_lte(int n){
        LongSet positions = new LongOpenHashSet(this.length);

        for (int i = 0; i < this.length; i++) {
            positions.add(BlockPos.asLong(buffer_x[i], buffer_y[i], buffer_z[i]));
        }

        for (int i = 0; i < this.length; i++) {
            int x = buffer_x[i];
            int y = buffer_y[i];
            int z = buffer_z[i];
            byte count = 0;

            if (positions.contains(BlockPos.asLong(x + 1, y, z))) count++;
            if (positions.contains(BlockPos.asLong(x - 1, y, z))) count++;
            if (positions.contains(BlockPos.asLong(x, y + 1, z))) count++;
            if (positions.contains(BlockPos.asLong(x, y - 1, z))) count++;
            if (positions.contains(BlockPos.asLong(x, y, z + 1))) count++;
            if (positions.contains(BlockPos.asLong(x, y, z - 1))) count++;

            if (count <= n) {
                this.state[i] = null;
                if (this.item != null) {
                    this.item[i] = null;
                }
            }
        }
    }
}