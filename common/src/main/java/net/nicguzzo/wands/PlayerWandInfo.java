package net.nicguzzo.wands;
import java.util.HashMap;
import java.util.Vector;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerWandInfo{
    public static final int MAX_UNDO = 2048;
    public static final int MAX_LIMIT = 32768;
    public static HashMap<String,PlayerWandInfo> players_info= new HashMap<String, PlayerWandInfo>();
    
    public BlockPos p1 =null;
    public BlockPos p2 =null;
    public BlockState p1_state=null;
    public BlockState sound_block_state=null;
    public boolean is_double_slab=false;
    public ItemStack palette=null;
    public int slot=0;
    public Vector<Integer> slots= new Vector<Integer>();
    //public Vector<Integer> slots_tmp= new Vector<Integer>();
    BlockBuffer block_buffer=new BlockBuffer(MAX_LIMIT);
    public CircularBuffer undo_buffer = new CircularBuffer(MAX_UNDO);
    static public void add_player(Player player){
        players_info.put(player.getStringUUID(), new PlayerWandInfo()) ;
    }
    static public void remove_player(Player player){
        players_info.remove(player.getStringUUID());
    }
    static public PlayerWandInfo get(Player player){
        return players_info.get(player.getStringUUID());
    }
    public void clear(){
        p1=null;
        p2=null;
        p1_state=null;
        sound_block_state=null;
    }
}
    