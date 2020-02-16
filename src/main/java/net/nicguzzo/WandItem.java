package net.nicguzzo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class  WandItem extends Item
{
    enum Orientation {
        HORIZONTAL,
        VERTICAL
    }
    private int limit=32;
    private static int mode=0;
    private static Orientation orientation=Orientation.HORIZONTAL;
    public static int x1;
    public static int y1;
    public static int z1;
    public static int x2;
    public static int y2;
    public static int z2;
    public static boolean valid;
    public static Direction mode2_dir;
    public WandItem(int lim,int max_damage)
    {            
        super(new Item.Settings().group(ItemGroup.MISC).maxCount(1).maxDamage(max_damage));
        limit=lim;
    }
    static public int getMode(){
        return mode;
    }
    public int getLimit(){
        return limit;
    }
    static public Orientation getOrientation(){
        return orientation;
    }
    static public void cycleOrientation(){
        
        int o=(orientation.ordinal()+1)% 2;
        orientation=Orientation.values()[o];
        System.out.println("Wands orientation: "+orientation);
    }
    static public void toggleMode(){
        mode=(mode+1)% 2;        
        System.out.println("Wands mode: "+mode);
    }
    private void placeBlock(World world,PlayerEntity player,BlockPos pos,BlockState block_state){
        if(player.abilities.creativeMode){
            world.setBlockState(pos, block_state);
        }else{
            ItemStack b=new ItemStack(block_state.getBlock());
            if(player.inventory.contains(b)){
                world.setBlockState(pos, block_state);
                int slot=player.inventory.getSlotWithStack(b);
                player.inventory.getInvStack(slot).decrement(1);
                ItemStack stack = player.getMainHandStack();
                stack.setDamage(stack.getDamage() + 1);
            }
        }
    }
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        if(!valid){
            return ActionResult.FAIL;
        }
        PlayerEntity player= context.getPlayer();        
        World world=context.getWorld();
        BlockPos pos=context.getBlockPos();        
        //Vec3d hitPos = context.getHitPos();
        Direction side = context.getSide();        
        BlockState block_state=world.getBlockState(pos);
        Block block=block_state.getBlock();

        
        System.out.println("side: "+side);
        //System.out.printf("x: %f%n", x);
        //System.out.printf("y: %f%n", y);
        //System.out.printf("z: %f%n", z);
        
        
        switch (mode) {
            case 0:
                BlockPos pos0=new BlockPos(x1, y1, z1);
                placeBlock(world,player,pos0,block_state);
                /*Direction place_in_dir=getDirectionMode1(hitPos,side);
                if (place_in_dir != null) {            
                    if(block_state.isFullCube(world, pos)){
                        int p = find_next_pos(world, block_state, place_in_dir, pos);
                        //System.out.println("p=" + p);
                        if (p >= 0) {
                            if(player.abilities.creativeMode){
                                world.setBlockState(pos.offset(place_in_dir, p + 1), block_state.getBlock().getDefaultState());
                            }else{
                                ItemStack b=new ItemStack(block);
                                if(player.inventory.contains(b)){
                                    world.setBlockState(pos.offset(place_in_dir, p + 1), block_state.getBlock().getDefaultState());
                                    int slot=player.inventory.getSlotWithStack(b);
                                    player.inventory.getInvStack(slot).decrement(1);
                                    ItemStack stack = player.getMainHandStack();
                                    stack.setDamage(stack.getDamage() + 1);
                                }
                            }
                        }
                    }
                }*/
            break;
            case 1:
                int l=0;
                switch(WandItem.mode2_dir){
                    case NORTH:
                    case SOUTH:
                        l=Math.abs(WandItem.z2-WandItem.z1);
                    break;
                    case EAST:
                    case WEST:
                        l=Math.abs(WandItem.x2-WandItem.x1);
                    break;
                    case UP:
                    case DOWN:
                        l=Math.abs(WandItem.y2-WandItem.y1);
                    break;									
                }                
                
                //System.out.println("mode2_dir: "+mode2_dir);
                
                BlockPos pos2=new BlockPos(x1, y1, z1);
                //System.out.println("pos2: "+pos2);
                for (int i = 0;i <l ;i++) {            
                    placeBlock(world,player,pos2.offset(WandItem.mode2_dir, i),block_state);        
                    /*if(player.abilities.creativeMode){
                        world.setBlockState(pos2.offset(WandItem.mode2_dir, i), block_state.getBlock().getDefaultState());                        
                    }else{
                        ItemStack b=new ItemStack(block);
                        if(player.inventory.contains(b)){
                            world.setBlockState(pos2.offset(WandItem.mode2_dir, i), block_state.getBlock().getDefaultState());
                            int slot=player.inventory.getSlotWithStack(b);
                            player.inventory.getInvStack(slot).decrement(1);
                            ItemStack stack = player.getMainHandStack();
                            stack.setDamage(stack.getDamage() + 1);
                        }else break;
                    }*/
                }
            break;
        }
        return ActionResult.SUCCESS;
    }
    static public int find_next_pos(World world,BlockState block_state,Direction dir,BlockPos pos,int limit){        
        for(int i=0;i<limit;i++){
            BlockState bs =world.getBlockState(pos.offset(dir,i+1));
            if(bs!=null){
                if(!bs.equals(block_state)){
                    if(bs.isAir()){
                        return i;
                    }else{
                        return -1;
                    }
                } 
            }
        }
        return -1;
    }
    static public float unitCoord(float x){
        float y=x-((int) x);        
        if(y<0)
            y=1.0f+y;        
        return y;
    }
    static public Direction getDirectionMode1(Vec3d hitPos,Direction side){
        MinecraftClient client=MinecraftClient.getInstance();
		ClientPlayerEntity player=client.player;
        float x=unitCoord((float)hitPos.getX());
        float y=unitCoord((float)hitPos.getY());
        float z=unitCoord((float)hitPos.getZ());
        
        switch (side) {
            case UP:
            case DOWN:
                if (x >= 0.25 && x <= 0.75) {
                    if (z <= 0.25){
                        return Direction.NORTH;
                    }else {
                        if (z >= 0.75){
                            return Direction.SOUTH;
                        }else{
                            if(player.isSneaking()){
                                return side;
                            }else{
                                return side.getOpposite();
                            }
                        }
                    }
                } else {
                    if (z >= 0.25 && z <= 0.75) {
                        if (x <= 0.25){
                            return Direction.WEST;
                        }else {
                            if (x >= 0.75){
                                return Direction.EAST;
                            }
                        }
                    }
                }
                break;
            case EAST:
            case WEST:
                if (z >= 0.25 && z <= 0.75) {
                    if (y <= 0.25)
                        return Direction.DOWN;
                    else {
                        if (y >= 0.75){
                            return Direction.UP;
                        }else{
                            if(player.isSneaking()){
                                return side;
                            }else{
                                return side.getOpposite();
                            }
                        }
                    }
                } else {
                    if (y >= 0.25 && y <= 0.75) {
                        if (z <= 0.25){
                            return Direction.NORTH;
                        }else {
                            if (z >= 0.75){
                                return Direction.SOUTH;
                            }
                        }
                    }
                }
                break;
            case NORTH:
            case SOUTH:
                if (x >= 0.25 && x <= 0.75) {
                    if (y <= 0.25)
                        return Direction.DOWN;
                    else {
                        if (y >= 0.75){
                            return Direction.UP;
                        }else{
                            if(player.isSneaking()){
                                return side;
                            }else
                                return side.getOpposite();
                        }
                    }
                } else {
                    if (y >= 0.25 && y <= 0.75) {
                        if (x <= 0.25){
                            return Direction.WEST;
                        }else {
                            if (x >= 0.75){
                                return Direction.EAST;
                            }
                        }
                    }
                }
            break;
        }
        return null;        
    }
}