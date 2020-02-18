package net.nicguzzo;


import net.minecraft.block.BlockState;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

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
        Direction side = context.getSide();        
        BlockState block_state=world.getBlockState(pos);
  
        System.out.println("side: "+side);

        switch (mode) {
            case 0:
                BlockPos pos0=new BlockPos(x1, y1, z1);
                placeBlock(world,player,pos0,block_state);

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
                BlockPos pos2=new BlockPos(x1, y1, z1);
                for (int i = 0;i <l ;i++) {            
                    placeBlock(world,player,pos2.offset(WandItem.mode2_dir, i),block_state);        
                }
            break;
        }
        return ActionResult.SUCCESS;
    }
    
}