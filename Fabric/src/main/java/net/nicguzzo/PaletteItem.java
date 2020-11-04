package net.nicguzzo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.google.common.collect.Lists;
import java.util.List;
//import javax.annotation.Nullable;

public class  PaletteItem extends Item {
  //public ArrayList<Block> palatte;
  private final List<String> blocks = Lists.newArrayList();
	public PaletteItem() {    
    super(new Item.Settings().group(ItemGroup.MISC).maxCount(1));		
	}
  public ActionResult useOnBlock(ItemUsageContext context) {

    if(!context.getWorld().isClient()){
        return ActionResult.FAIL;
    }
    PlayerEntity player= context.getPlayer();        
    World world=context.getWorld();
    BlockPos pos_state=context.getBlockPos();  
    BlockState block_state=world.getBlockState(pos_state);
    Block block =block_state.getBlock();
    //ItemStack stack=player.getOffHandStack();
    ItemStack stack=context.getStack();
    if(block!=null){
      CompoundTag compoundTag = stack.getSubTag("palette");
      int id = Block.getRawIdFromState(block_state);
      int[] ids=null;
      if(compoundTag!=null){
        int[] _ids=compoundTag.getIntArray("blockList");
        ids=new int[_ids.length+1];
        int i=0;
        for(i=0;i<_ids.length;i++){
          ids[i]=_ids[i];
        }
        ids[i]=id;
      }else{
        compoundTag = new CompoundTag();
        ids=new int[1];
        ids[0]=id;
      }
      
      System.out.println("id: "+id);      
      
      compoundTag.putIntArray("blockList", ids);
      System.out.println("ids in palette:");
      for(int i=0;i<ids.length;i++){
        System.out.println("  id: "+ids[i]);      
      }
      //compoundTag.putUuid("", );
      stack.putSubTag("palette",compoundTag);
      //ItemStack stack=this.getStackForRender();
      //blocks.
    }
    return ActionResult.FAIL;
  }
  @Override
  @Environment(EnvType.CLIENT)
  public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {      
    CompoundTag compoundTag = stack.getSubTag("blockList");
    if (compoundTag != null && compoundTag.contains("blockList")) {
      int[] ids=compoundTag.getIntArray("blockList");
      for(int i=0;i<ids.length;i++){
        System.out.println("--- id: "+ids[i]);
        tooltip.add(new LiteralText("block "+ids[i]));
      }
    }
  }
}