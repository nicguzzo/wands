package net.nicguzzo;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.nicguzzo.common.WandItem;

public class  WandItemForge extends Item
{   
    private static final Logger LOGGER = LogManager.getLogger();
    
    class WandItemImpl extends WandItem{

        public WandItemImpl(int lim,boolean removes_water,boolean removes_lava) {
            super(lim,removes_water,removes_lava);
        }

        @Override
        public boolean placeBlock(BlockPos block_state, BlockPos pos0, BlockPos pos1) {
            //LOGGER.info("placeBlock");
            int pm=0;
            if(WandItem.getMode()==2){
                pm=WandItem.getPaletteMode().ordinal();
            }else{
                pm=WandItem.PaletteMode.SAME.ordinal();
            }
            WandsPacketHandler.INSTANCE.sendToServer(new SendPlace(block_state,pos0,pos1,pm,WandItem.getMode(),WandItem.getPlane().ordinal()));
            return true;
        }

        @Override
        public boolean isCreative(PlayerEntity player) {            
            return player.isCreative();
        }

        @Override
        public boolean isClient(World world) {         
            //LOGGER.info("world.isRemote "+world.isRemote);   
            return world.isClientSide;
        }

        @Override
        public void playSound(PlayerEntity player,BlockState block_state,BlockPos pos) {
            //LOGGER.info("playSound");
            SoundType sounttype = block_state.getSoundType();            
            player.level.playSound(player, pos,sounttype.getPlaceSound(), SoundCategory.BLOCKS, (sounttype.getVolume() + 1.0F) / 2.0F, sounttype.getPitch() * 0.8F);
        }

        @Override
        public boolean playerInvContains(PlayerEntity player, ItemStack item) {
            return player.inventory.contains(item);
        }
    }
    public WandItemImpl wand=null;
    public WandItemForge(int lim,boolean removes_water,boolean removes_lava,Item.Properties prop)
    {   
        super(prop);
        wand=new WandItemImpl(lim,removes_water,removes_lava);
    }
    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity player, Hand hand){
        //LOGGER.info("Use");        
        wand.left_click_use(worldIn);
        return ActionResult.pass(player.getItemInHand(hand));
    }
    @Override
    public ActionResultType useOn(ItemUseContext context) {
        //LOGGER.info("onItemUse");        
        if(!wand.right_click_use_on_block(context.getPlayer() , context.getLevel(), context.getClickedPos())){
            return ActionResultType.FAIL;
        }        
        return ActionResultType.FAIL;
    }

}