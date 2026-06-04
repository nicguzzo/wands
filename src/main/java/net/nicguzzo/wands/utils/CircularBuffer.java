package net.nicguzzo.wands.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fixed-size circular buffer used as the undo/redo history for wand actions.
 * Entries are pushed (put) and popped from the "top" end. When the buffer is
 * full, the oldest entry at "bottom" is silently overwritten.
 * "last" tracks the high-water mark so that redo (forward) knows where
 * previously undone entries end.
 */
public class CircularBuffer {
    /** A single undo/redo entry recording one block change. */
    public class P{
        public BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
        /** The original block state (before the action). Used to restore on undo. */
        public BlockState state=null;
        /** true = block was destroyed/modified (undo restores state); false = block was placed (undo removes it). */
        public boolean destroyed=false;
        /** Groups entries that belong to the same wand action so undo/redo operates on entire actions at once. */
        public int actionId=-1;
        /** For use-actions (tilling, stripping, etc.): the state after modification. Used by redo to re-apply. */
        public BlockState newState=null;
        /** The wand mode translation key (e.g. "wands.mode.circle") at the time of recording. Used in undo/redo messages. */
        public String modeName=null;
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

    public  int size(){
        return size;
    }
    /** Returns true if there are redo-able entries ahead of the current top position. */
    public boolean can_go_forward(){
        if(top>=bottom){
            return top<last ;
        }else{
            return top>last;
        }        
    }

    public void put(BlockPos p,BlockState s,boolean d){
        put(p,s,d,-1,null,null);
    }

    public void put(BlockPos p,BlockState s,boolean d,int actionId,BlockState newState){
        put(p,s,d,actionId,newState,null);
    }

    public void put(BlockPos p,BlockState s,boolean d,int actionId,BlockState newState,String modeName){
        forward();
        if(elem[top]==null)
            elem[top]=new P();
        elem[top].pos.set(p);

        elem[top].state=s;
        elem[top].destroyed=d;
        elem[top].actionId=actionId;
        elem[top].newState=newState;
        elem[top].modeName=modeName;

    }

    /** Advances top by one slot (used by put and redo). Pushes bottom forward if buffer is full. */
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

    /** Returns the next element ahead of top without advancing. Used by redo to check actionId before committing. */
    public P peekForward(){
        if(!can_go_forward()){
            return null;
        }
        int next=(top + 1) % max;
        return elem[next];
    }

    /** Removes and returns the top element, moving top backward. Used by undo. */
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
            System.out.println("i: "+i+" elem: "+e.state +" pos"+e.pos);
        }
        System.out.println("top: "+ top);
        System.out.println("bottom: "+ bottom);
        System.out.println("size: "+ size);
        System.out.println("last: "+ last);
    }
}
