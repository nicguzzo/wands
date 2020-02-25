package net.nicguzzo;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;

import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import io.netty.buffer.Unpooled;
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
    
    private boolean placeBlock(World world,PlayerEntity player,BlockPos pos0,BlockPos pos1,BlockState block_state,ItemStack itemStack){
                
        if(player.inventory.contains(itemStack) || player.abilities.creativeMode){
            
            if(world.isClient){                
                PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                passedData.writeBlockPos(pos0);
                passedData.writeBlockPos(pos1);                
                ClientSidePacketRegistry.INSTANCE.sendToServer(WandsMod.WAND_PACKET_ID, passedData);
            }            
            return true;
        }
        return false;
    }
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        //System.out.println("blocks per xp: "+ WandsClientMod.BLOCKS_PER_XP);
        if(!valid){
            return ActionResult.FAIL;
        }
        PlayerEntity player= context.getPlayer();        
        World world=context.getWorld();
        BlockPos pos0=context.getBlockPos();  
        BlockState block_state=world.getBlockState(pos0);
        ItemStack item_stack=new ItemStack(block_state.getBlock());
        switch (mode) {
            case 0:
                BlockPos pos1=new BlockPos(x1, y1, z1);
                
                if(placeBlock(world,player,pos0,pos1,block_state,item_stack)){
                    BlockSoundGroup blockSoundGroup = block_state.getSoundGroup();
                    world.playSound(player, pos1, block_state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F);
                }
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
                int placed=0;
                for (int i = 0;i <l ;i++) {            
                    if(placeBlock(world,player,pos0,pos2.offset(WandItem.mode2_dir, i),block_state,item_stack)){
                        placed++;
                    }
                }                
                if(placed>0){
                    BlockSoundGroup blockSoundGroup = block_state.getSoundGroup();
                    world.playSound(player, pos0, block_state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F);
                }
            break;
        }
        return ActionResult.SUCCESS;
    }
    
}