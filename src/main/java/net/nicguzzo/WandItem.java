package net.nicguzzo;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList; 
import io.netty.buffer.Unpooled;
import net.minecraft.world.World;

public class  WandItem extends ToolItem
{
    enum Orientation {
        HORIZONTAL,
        VERTICAL
    }
    class Undo{
        public ArrayList<BlockPos> undo_buffer=new ArrayList<BlockPos>();
        public Block undo_block=null;
    }
    
    public static Deque <Undo> undos=new LinkedList<>();
    private int MAX_UNDO=32;
    private int limit=32;
    private static int mode = 0;
    private static boolean invert=false;
    private static boolean randomize=false;
    private static Orientation orientation=Orientation.HORIZONTAL;
    public static int x1;
    public static int y1;
    public static int z1;
    public static int x2;
    public static int y2;
    public static int z2;
    public static boolean valid;
    public static Direction mode2_dir;
    public WandItem(ToolMaterial toolMaterial,int lim,int max_damage)
    {            
        super(toolMaterial,new Item.Settings().group(ItemGroup.TOOLS).maxCount(1).maxDamage(max_damage));
        limit=lim;
    }

    static public int getMode() {
        return mode;
    }

    static public boolean getInvert() {
        return invert;
    }

    static public void toggleInvert() {
        MinecraftClient client=MinecraftClient.getInstance();
        String state="off";
        invert = !invert;
        if(invert){
            state="on";
        }
        String s="Wand inverted "+state;
        System.out.println(s);                
        client.player.sendMessage(new LiteralText(s),true);
    }
    static public void toggleRandomize() {
        MinecraftClient client=MinecraftClient.getInstance();        
        String state="off";

        randomize = !randomize;        
        if(randomize){
            state="on";
        }
        String s="Wand randomize "+state;
        System.out.println(s);
        client.player.sendMessage(new LiteralText(s),true);
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
        String s="Wand orientation: "+orientation;
        System.out.println(s);
        MinecraftClient client=MinecraftClient.getInstance();
        client.player.sendMessage(new LiteralText(s),true);
    }
    
    static public void toggleMode(){
        mode=(mode+1)% 3;
        String s="Wand mode: "+mode;
        System.out.println(s);        
        MinecraftClient client=MinecraftClient.getInstance();
        client.player.sendMessage(new LiteralText(s),true);
    }
    static public void undo(){
        if(!undos.isEmpty()){
            Undo u=undos.pollLast();
            for (BlockPos i : u.undo_buffer) {
                //System.out.println("undo: "+i);   
                PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                passedData.writeBlockPos(i);
                ClientSidePacketRegistry.INSTANCE.sendToServer(WandsMod.WAND_UNDO_PACKET_ID, passedData);
            }
            u.undo_buffer.clear();
        }
        System.out.println("undo: "+undos.size());   
    }
    
    private boolean     placeBlock(BlockPos block_state,BlockPos pos0,BlockPos pos1,ItemStack itemStack){                
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeBlockPos(block_state);
        passedData.writeBlockPos(pos0);
        passedData.writeBlockPos(pos1);
        passedData.writeBoolean(WandItem.randomize);
        ClientSidePacketRegistry.INSTANCE.sendToServer(WandsMod.WAND_PACKET_ID, passedData);
        return true;
    }
    /*public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return !miner.isCreative();
     }
     public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        //MinecraftClient client=MinecraftClient.getInstance();
        //client.player.sendMessage(new LiteralText("wand postMine"),true);
        return false;
     }
     public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        //MinecraftClient client=MinecraftClient.getInstance();
        //client.player.sendMessage(new LiteralText("wand postHit"),true);        
        return false;
     }
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.sendMessage(new LiteralText("wand reset"),true);
        WandsClientMod.fill_pos1=null;
        return TypedActionResult.pass(user.getStackInHand(hand));
    }*/
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        //System.out.println("blocks per xp: "+ WandsClientMod.BLOCKS_PER_XP);
        PlayerEntity player= context.getPlayer();        
        if(!valid){
            //WandsClientMod.fill_pos1=null;
            player.sendMessage(new LiteralText("invalid"),true);
            return ActionResult.FAIL;
        }
        if(!context.getWorld().isClient()){
            return ActionResult.FAIL;
        }
        
        World world=context.getWorld();
        BlockPos pos_state=context.getBlockPos();  
        BlockState block_state=world.getBlockState(pos_state);
        ItemStack item_stack=new ItemStack(block_state.getBlock());
        if(player.inventory.contains(item_stack) || player.abilities.creativeMode || mode==2){
            switch (mode) {
                case 0:
                    WandsClientMod.fill_pos1=null;
                    BlockPos pos0=new BlockPos(x1, y1, z1);
                    BlockPos pos1=new BlockPos(x1, y1, z1);
                   // WandItem.undo_buffer.clear();
                    if(placeBlock(pos_state,pos0,pos1,item_stack)){
                        //WandItem.undo_buffer.add(new Vec3i(pos1.getX(),pos1.getY(),pos1.getZ()));
                        BlockSoundGroup blockSoundGroup = block_state.getSoundGroup();
                        world.playSound(player, pos_state, block_state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F);
                    }
                break;
                case 1:
                    WandsClientMod.fill_pos1=null;
                    int placed=0;
                    /*int l=0;
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
                    
                    //WandItem.undo_buffer.clear();                 
                    Undo u=null;
                    if(player.abilities.creativeMode){
                        u=new Undo();
                    }                    
                    for (int i = 0;i <l ;i++) {            
                        BlockPos pos3=pos2.offset(WandItem.mode2_dir, i);
                        if(placeBlock(pos_state,pos3,block_state,item_stack)){
                            //WandItem.undo_buffer.add(new Vec3i(pos3.getX(),pos3.getY(),pos3.getZ()));                        
                            if(u!=null){
                                u.undo_buffer.add(pos3);
                            }
                            placed++;
                        }
                    }*/
                    placeBlock(pos_state,new BlockPos(x1, y1, z1),new BlockPos(x2, y2, z2),item_stack);
                    if(placed>0){
                        /*if(u!=null){
                            //u.undo_block=block_state.getBlock();                            
                            undos.addLast(u);
                            if(undos.size()>MAX_UNDO){
                                u=undos.pollFirst();
                                u.undo_buffer.clear();                                
                            }
                            //System.out.println("undo: "+undos.size());   
                        }*/
                        BlockSoundGroup blockSoundGroup = block_state.getSoundGroup();
                        world.playSound(player, pos_state, block_state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F);
                    }
                break;
                case 2:                    
                    if(WandsClientMod.fill_pos1==null){
                        WandsClientMod.fill_pos1=pos_state;         
                        x1=pos_state.getX();
                        y1=pos_state.getY();
                        z1=pos_state.getZ();
                        player.sendMessage(new LiteralText("from "+pos_state),true);
                    }else{                        
                        if(WandsClientMod.fill_pos1!=pos_state){
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
                            placeBlock(WandsClientMod.fill_pos1,WandsClientMod.fill_pos1,fill_pos2,item_stack);
                            player.sendMessage(new LiteralText("fill from "+WandsClientMod.fill_pos1+" to "+fill_pos2),true);
                        }
                        WandsClientMod.fill_pos1=null;
                    }
                    
                break;
            }
        }
        return ActionResult.SUCCESS;
    }
    
}