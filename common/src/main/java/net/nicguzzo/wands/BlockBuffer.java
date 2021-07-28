package net.nicguzzo.wands;

import net.minecraft.core.BlockPos;
//import net.minecraft.world.level.block.state.BlockState;

public class BlockBuffer{
    
    public  int max=0;
    public  int length=0;
    public  int[] buffer_x=null;
    public  int[] buffer_y=null;
    public  int[] buffer_z=null;
    //public  BlockState[] state=null;

    public BlockBuffer(int n){
        max=n;
        buffer_x=new int[max];
        buffer_y=new int[max];
        buffer_z=new int[max];
        //state=new BlockState[max];
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
        }
    }    
    public void add(int x,int y,int z){
        if(length<max){
            buffer_x[length]=x;
            buffer_y[length]=y;
            buffer_z[length]=z;
            length++;
        }
    }
    public void set(int i,BlockPos p){
        set(i,p.getX(),p.getY(),p.getZ());
    }
    public void add(BlockPos p){
        add(p.getX(),p.getY(),p.getZ());
    }
}