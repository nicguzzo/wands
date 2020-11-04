package net.nicguzzo.common;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
//import io.netty.buffer.Unpooled;
import net.minecraft.world.World;
import net.nicguzzo.WandsMod;

abstract public class WandItem  {
    enum Orientation {
        HORIZONTAL, VERTICAL
    }

    public enum PaletteMode {
        SAME, RANDOM, ROUND_ROBIN
    }

    class Undo {
        public ArrayList<BlockPos> undo_buffer = new ArrayList<BlockPos>();
        public Block undo_block = null;
    }
    
    public BlockPos[] block_buffer;
    public int block_buffer_length=0;
    public static Deque<Undo> undos = new LinkedList<>();
    //private int MAX_UNDO = 32;
    private int limit = 0;
    public int limit2 = 0;
    public int x0=0;
    private static BlockState fill1_state=null;
    private static int mode = 0;
    private static PaletteMode palette_mode = PaletteMode.SAME;
    private static boolean invert = false;
    public boolean removes_water;
    public boolean removes_lava;
    private static Orientation orientation = Orientation.HORIZONTAL;
    public static int x1;
    public static int y1;
    public static int z1;
    public static int x2;
    public static int y2;
    public static int z2;
    public static BlockPos fill_pos1;
    public static boolean valid = false;

    public WandItem(int lim,boolean removes_water,boolean removes_lava) {
        limit = lim;
        limit2 = (int)Math.floor(Math.sqrt(lim));
        x0=limit2/2;        
        this.removes_water=removes_water;
        this.removes_lava=removes_lava;
        block_buffer=new BlockPos[limit];
    }

    static public int getMode() {
        return mode;
    }
    static public PaletteMode getPaletteMode() {
        return palette_mode;
    }

    static public boolean getInvert() {
        return invert;
    }

    static public void toggleInvert() {
        String state = "off";
        invert = !invert;
        if (invert) {
            state = "on";
        }
        WandsMod.compat.send_message_to_player("Wand inverted " + state);
    }

    static public void cyclePalleteMode() {
        String state="";
        switch (palette_mode) {
            case SAME:
                palette_mode = PaletteMode.RANDOM;
                state="random";
                break;
            case RANDOM:
                palette_mode = PaletteMode.ROUND_ROBIN;
                state="round robin";
                break;
            case ROUND_ROBIN:
                palette_mode = PaletteMode.SAME;
                state="same";
                break;
        }
        WandsMod.compat.send_message_to_player("Wand palette mode "+state);
    }

    public int getLimit() {
        return limit;
    }

    static public Orientation getOrientation() {
        return orientation;
    }

    static public void cycleOrientation() {

        int o = (orientation.ordinal() + 1) % 2;
        orientation = Orientation.values()[o];
        // String s="Wand orientation: "+orientation;
        // sendMessage(s);
        WandsMod.compat.send_message_to_player("Wand orientation: "+orientation);
    }

    static public void cycleMode() {
        mode = (mode + 1) % 4;        
        WandsMod.compat.send_message_to_player("Wand mode: "+mode);
    }

    boolean in_buffer(BlockPos p){
        for(int i=0;i<block_buffer_length && i<limit;i++){
            if(p.equals(block_buffer[i])){
                return true;
            }
        }
        return false;
    }
    void add_buffer(BlockPos p){
        if(block_buffer_length<limit){
            block_buffer[block_buffer_length]=p;
            block_buffer_length++;
        }
    }

    static public void undo() {
        /*
         * if(!undos.isEmpty()){ Undo u=undos.pollLast(); for (BlockPos i :
         * u.undo_buffer) { //System.out.println("undo: "+i); PacketByteBuf passedData =
         * new PacketByteBuf(Unpooled.buffer()); passedData.writeBlockPos(i);
         * ClientSidePacketRegistry.INSTANCE.sendToServer(WandsMod.compat.mNDO_PACKET_ID,
         * passedData); } u.undo_buffer.clear(); }
         * System.out.println("undo: "+undos.size());
         */
    }

    /*private boolean placeBlock(BlockPos block_state, BlockPos pos0, BlockPos pos1) {
        
         PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
         passedData.writeBlockPos(block_state); passedData.writeBlockPos(pos0);
         passedData.writeBlockPos(pos1); if(WandItem.mode==2){
         passedData.writeInt(WandItem.palette_mode.ordinal()); }else{
         passedData.writeInt(PaletteMode.SAME.ordinal()); }
         ClientSidePacketRegistry.INSTANCE.sendToServer(WandsMod.compat.mACKET_ID,
         passedData);
         
         return true;
        
    }*/
    abstract public boolean placeBlock(BlockPos block_state, BlockPos pos0, BlockPos pos1);
    abstract public boolean isCreative(PlayerEntity player);
    abstract public boolean isClient(World world);
    abstract public void playSound(PlayerEntity player,BlockState block_state,BlockPos pos);
    abstract public boolean playerInvContains(PlayerEntity player,ItemStack item);
    
    public void left_click_use(World world) {
        if(isClient(world)){
            WandItem.fill_pos1=null;
        }
    }

    public boolean right_click_use_on_block(PlayerEntity player,World world, BlockPos pos_state) {
        //System.out.println("blocks per xp: "+ WandsClientMod.BLOCKS_PER_XP);
        if(!isClient(world)){
            return false;
        }
                
        if(!valid){
            WandItem.fill_pos1=null;            
            return false;
        }
        
        BlockState block_state=world.getBlockState(pos_state);
        ItemStack item_stack=new ItemStack(block_state.getBlock());
        if(playerInvContains(player,item_stack) || isCreative(player) || mode==2){
            switch (mode) {
                case 0:
                    WandItem.fill_pos1=null;
                    BlockPos pos0=new BlockPos(x1, y1, z1);
                    BlockPos pos1=new BlockPos(x1, y1, z1);
                   // WandItem.undo_buffer.clear();
                    if(placeBlock(pos_state,pos0,pos1)){
                        //WandItem.undo_buffer.add(new Vec3i(pos1.getX(),pos1.getY(),pos1.getZ()));
                        playSound(player,block_state,pos_state);
                    }
                break;
                case 1:
                    WandItem.fill_pos1=null;
                    //int placed=0;
                   
                    placeBlock(pos_state,new BlockPos(x1, y1, z1),new BlockPos(x2, y2, z2));
                    //if(placed>0){                        
                        playSound(player,block_state,pos_state);                        
                    //}
                break;
                case 2:                    
                    if(WandItem.fill_pos1==null){
                        WandItem.fill_pos1=pos_state;         
                        fill1_state=block_state;
                        x1=pos_state.getX();
                        y1=pos_state.getY();
                        z1=pos_state.getZ();
                        WandsMod.compat.send_message_to_player("from "+pos_state);
                        //player.sendMessage(new LiteralText("from "+pos_state),true);
                        //System.out.println("state "+block_state.getBlock());
                    }else{                        
                        if(WandItem.fill_pos1!=pos_state){
                            x2=pos_state.getX();
                            y2=pos_state.getY();
                            z2=pos_state.getZ();
                            if(x1<x2){
                                x2+=1;
                            }else{
                                x2-=1;
                            }										
                            if(y1<y2){
                                y2+=1;											
                            }else{
                                y2-=1;
                            }
                            if(z1<z2){
                                z2+=1;
                            }else{
                                z2-=1;											
                            }
                            BlockPos fill_pos2=new BlockPos(x2,y2,z2);
                            placeBlock(WandItem.fill_pos1,WandItem.fill_pos1,fill_pos2);
                            if(fill1_state!=null){
                                playSound(player,fill1_state,WandItem.fill_pos1); 
                            }
                            WandsMod.compat.send_message_to_player("fill from "+WandItem.fill_pos1+" to "+fill_pos2);
                            //player.sendMessage(new LiteralText("fill from "+WandItem.fill_pos1+" to "+fill_pos2),true);
                        }
                        WandItem.fill_pos1=null;
                    }                    
                break;
                case 3:{
                    
                    for(int i=0;i<block_buffer_length && i<getLimit();i++){
                        placeBlock(pos_state,block_buffer[i],block_buffer[i]);
                    }
                    if(block_buffer_length>0)
                        playSound(player,block_state,block_buffer[0]); 

                    System.out.println("block_buffer_length: "+block_buffer_length);
                }
                break;
            }
        }
        return true;
    }
    public static float calc_xp(final int level,float prog) {
		float xp=WandItem.calc_xp_level(level);
		if(prog>0){							
			xp=xp+ prog * (WandItem.calc_xp_level(level+1)-xp);
		}
		return xp;
	}
	public static float calc_xp_level(final int level) {
		float xp_points = 0;
		final int level2 = level * level;
		if(level>=32){
			xp_points=4.5f*level2 - 162.5f *level + 2220.0f;
		}else if(level>=17){
			xp_points=2.5f*level2 - 40.5f *level + 360.0f;
		}else {
			xp_points=level2 + 6*level;
		}
		return xp_points;
	}
	public static int calc_xp_to_next_level(int level){
		int xp=0;
		if(level>=32){
			xp = 9 * level - 158;	
		}else if(level>=17){
			xp = 5 * level - 38 ;	
		}else {
			xp = 2  *level + 7 ;	
		}
		return xp;
	}
}