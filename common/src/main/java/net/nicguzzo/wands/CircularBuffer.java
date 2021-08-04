package net.nicguzzo.wands;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

class CircularBuffer {
    class P{
        BlockPos pos=null;
        BlockState state=null;
        boolean destroyed=false;
    };
    private int size;
    private P[] elem;
    private int max;
    private int top;
    private int bottom;
    private int last;

    public CircularBuffer(int max) {
        this.max = max;
        elem = new P[this.max];
        size = 0;
        bottom = -1;
        top = -1;
        last=-1;
    }

    int size(){
        return size;
    }
    boolean can_go_forward(){        
        if(top>=bottom){
            return top<last ;
        }else{
            return top>last;
        }        
    }

    public void put(BlockPos p,BlockState s,boolean d){
        //System.out.println("block: "+p+" state: "+s);

        forward();
        if(elem[top]==null)
            elem[top]=new P();
        elem[top].pos=p;
        elem[top].state=s;
        elem[top].destroyed=d;
        
    }

    public void forward(){
        top = (top + 1) % max;
        if(top>last)
            last = top;
        if (top <=bottom) {
            bottom =(bottom + 1) % max;
        }        
        if(bottom==-1){
            bottom=0;
        }        
        if(size<max)
            size++;
    }

    public P peek(){
        if (size==0) {
            return null;
        }else {
            return elem[top];
        }
    }

    public P pop(){
        P e;
        if (size==0) {
            return null;
        }else {
            e = elem[top];
            //elem[top] = null;
            if(size==1){
                top=-1;
                bottom=-1;
                size=0;
            }else{
                if(top==0){
                    top=max-1;
                }else{
                    top = Math.abs((top - 1) % max);
                }
                size--;
            }
        }
        return e;
    }

    public boolean isEmpty() {
        return (size == 0);
    }

    public void print(){
        P e;
        System.out.println("undo buffer");
        for(int i=0;i<last+1 && i<max;i++){
            e = elem[i];
            System.out.println("i: "+i+" elem: "+e.state);
        }
        System.out.println("top: "+ top);
        System.out.println("bottom: "+ bottom);
        System.out.println("size: "+ size);
        System.out.println("last: "+ last);
    }
}
