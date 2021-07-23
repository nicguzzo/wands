package net.nicguzzo.wands;

import net.minecraft.core.BlockPos;

public class BlockBuffer{
    public  BlockPos[] buffer=null;
    public  int max=0;
    public  int length=0;
    public BlockBuffer(int n){
        max=n;
        buffer=new BlockPos[max];
    }
    boolean in_buffer(BlockPos p){
        for(int i=0;i<length && i<max;i++){
            if(p.equals(buffer[i])){
                return true;
            }
        }
        return false;
    }
    void add_buffer(BlockPos p){
        if(length<max){
            buffer[length]=p;
            length++;
        }
    }
}