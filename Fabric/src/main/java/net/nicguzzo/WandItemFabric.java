package net.nicguzzo;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import io.netty.buffer.Unpooled;

import net.nicguzzo.common.WandItem;

public class  WandItemFabric extends ToolItem
{
    static public void undo(int n) {
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeInt(n);
        ClientPlayNetworking.send(WandsMod.WAND_UNDO_PACKET_ID, passedData);
    }
    static public void redo(int n) {
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeInt(n);
        ClientPlayNetworking.send(WandsMod.WAND_REDO_PACKET_ID, passedData);
    }
    class WandItemImpl extends WandItem{
        public WandItemImpl(int lim,boolean removes_water,boolean removes_lava) {
            super(lim,removes_water,removes_lava);
        }
        
        
        @Override
        public boolean placeBlock(BlockPos block_state, BlockPos pos0, BlockPos pos1) {
            //LOGGER.info("placeBlock");
            //WandsPacketHandler.INSTANCE.sendToServer(new SendPack(block_state,pos0,pos1,WandItem.getMode()));            
            PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
            passedData.writeBlockPos(block_state);
            passedData.writeBlockPos(pos0);
            passedData.writeBlockPos(pos1);            
            //if(WandItem.getMode()==2){
                passedData.writeInt(WandItem.getPaletteMode().ordinal());
            //}else{
                //passedData.writeInt(PaletteMode.SAME.ordinal());
            //}
            passedData.writeInt(WandItem.getMode());
            passedData.writeInt(WandItem.getPlane().ordinal());
            //ClientSidePacketRegistry.INSTANCE.sendToServer(WandsMod.WAND_PACKET_ID, passedData);
            ClientPlayNetworking.send(WandsMod.WAND_PACKET_ID, passedData);
            return true;
        }

        @Override
        public boolean isCreative(PlayerEntity player) {            
            return player.abilities.creativeMode;
        }

        @Override
        public boolean isClient(World world) {         
            return world.isClient;
        }

        @Override
        public void playSound(PlayerEntity player,BlockState block_state,BlockPos pos) {
            BlockSoundGroup blockSoundGroup = block_state.getSoundGroup();
            player.world.playSound(player, pos,blockSoundGroup.getPlaceSound(), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F);
        }

        @Override
        public boolean playerInvContains(PlayerEntity player, ItemStack item) {
            return player.inventory.contains(item);
        }
    }
    public WandItemImpl wand=null;
    
    public WandItemFabric(ToolMaterial toolMaterial,int lim,int max_damage,boolean removes_water,boolean removes_lava)
    {            
        super(toolMaterial,new Item.Settings().group(ItemGroup.TOOLS).maxCount(1).maxDamage(max_damage));
        wand=new WandItemImpl(lim,removes_water,removes_lava);        
    }
  
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {        
        wand.left_click_use(world);        
        return TypedActionResult.pass(user.getStackInHand(hand));
    }
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if(!wand.right_click_use_on_block(context.getPlayer() , context.getWorld(), context.getBlockPos())){
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;        
    }
    
}