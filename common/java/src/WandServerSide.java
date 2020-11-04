package net.nicguzzo.common;

import java.util.Vector;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.nicguzzo.WandsMod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WandServerSide {
    private static final Logger LOGGER = LogManager.getLogger();
    public static void placeBlock(PlayerEntity player,BlockPos pos_state,BlockPos pos0,BlockPos pos1,int pm,boolean isCreative,float experienceProgress,ItemStack wand_stack){
        World world=player.world;
            
        WandItem.PaletteMode palatte_mode=WandItem.PaletteMode.values()[pm];
        BlockState state = world.getBlockState(pos_state);
            
        if(pos0.equals(pos1)){
            place(player,state,pos0,palatte_mode,isCreative,experienceProgress,wand_stack);
        }else{
            int xs,ys,zs,xe,ye,ze;
            if(pos0.getX()>=pos1.getX()){
                xs=pos1.getX()+1;
                xe=pos0.getX()+1;
            }else{
                xs=pos0.getX();
                xe=pos1.getX();
            }
            if(pos0.getY()>=pos1.getY()){
                ys=pos1.getY()+1;
                ye=pos0.getY()+1;
            }else{
                ys=pos0.getY();
                ye=pos1.getY();
            }
            if(pos0.getZ()>=pos1.getZ()){
                zs=pos1.getZ()+1;
                ze=pos0.getZ()+1;
            }else{
                zs=pos0.getZ();
                ze=pos1.getZ();							
            }

            
            Vector<Block> slots=new Vector<Block>();
            if(	palatte_mode==WandItem.PaletteMode.RANDOM || 
                palatte_mode==WandItem.PaletteMode.ROUND_ROBIN)
            {                							
                for (int i = 0; i < WandsMod.compat.get_main_inventory_size(player.inventory); ++i) {
                    ItemStack stack2 =WandsMod.compat.get_player_main_stack(player.inventory,i);
                    Block blk=Block.getBlockFromItem(stack2.getItem());						
                    if(blk!=null && blk != Blocks.AIR){
                        slots.add(blk);
                    }				
                }
            }
            int last_slot=0;
            for(int z=zs;z<ze;z++){
                for(int y=ys;y<ye;y++){
                    for(int x=xs;x<xe;x++){
                        BlockPos pos=new BlockPos(x,y,z);
                        Block blk=null;
                        if(palatte_mode==WandItem.PaletteMode.RANDOM){
                            int random_slot = WandsMod.compat.get_next_int_random(player,slots.size());
                            blk=slots.get(random_slot);                            				
                            state=WandsMod.compat.random_rotate(state,player.world);
                            
                        }else if (palatte_mode==WandItem.PaletteMode.ROUND_ROBIN){
                            blk=slots.get(last_slot);										
                            last_slot=(last_slot+1)% slots.size();										
                        }
                        if(blk!=null){
                            if((blk instanceof  SnowBlock)){
                                //disabled for now
                            }else{
                                state=blk.getDefaultState();
                            }
                        }
                        
                        if(place(player,state,pos,palatte_mode,isCreative,experienceProgress,wand_stack)){
                            //count++;
                        }
                        //System.out.println("pos "+pos);
                    }
                }
            }
            //System.out.println("Placed "+count+" blocks.");
            //System.out.println("Block: "+state);
        }
    }
    static private boolean place(PlayerEntity player,BlockState state,BlockPos pos,WandItem.PaletteMode palatte_mode,boolean isCreative,float experienceProgress,ItemStack wand_stack){
		boolean placed = false;				
        float BLOCKS_PER_XP=WandsMod.config.blocks_per_xp;
        //float BLOCKS_PER_XP=0.0f;
		
		Block block=state.getBlock();		
		BlockState state2=player.world.getBlockState(pos);
		int d=1;		
		WandItem wand=WandsMod.compat.get_player_wand(player);
		if (state2.isAir() || WandsMod.compat.is_fluid(state2,wand)) {
			int slot = -1;

			if((block instanceof  PaneBlock) || (block instanceof  FenceBlock)){
				state=state.getBlock().getDefaultState();
			}else if(block instanceof SlabBlock){
				if(WandsMod.compat.is_double_slab(state)){
					d=2;//should consume 2 if its a double slab
				}
			}
			if(palatte_mode==WandItem.PaletteMode.RANDOM && (block instanceof  SnowBlock)){
				d = WandsMod.compat.get_next_int_random(player,7)+1;
				state=block.getDefaultState().with(SnowBlock.LAYERS,d);
			}
		
			if (isCreative) {
				player.world.setBlockState(pos, state);
			} else {						
				float xp=WandItem.calc_xp(player.experienceLevel,experienceProgress);		
				float dec=0.0f;
				if(BLOCKS_PER_XP!=0){
					dec=  (1.0f/BLOCKS_PER_XP);
				}
				if (BLOCKS_PER_XP == 0 ||  (xp - dec) > 0) {
					
					ItemStack item_stack = new ItemStack(state.getBlock());
					ItemStack off_hand_stack = WandsMod.compat.get_player_offhand_stack(player.inventory);
					if (!off_hand_stack.isEmpty() && item_stack.getItem() == off_hand_stack.getItem()
							&& WandsMod.compat.item_stacks_equal(item_stack, off_hand_stack)
							&& off_hand_stack.getCount()>=d
						) {
											
						placed = player.world.setBlockState(pos, state);
						if(placed)
                            WandsMod.compat.player_offhand_stack_dec(player.inventory,d);
					} else {
						
						for (int i = 0; i < WandsMod.compat.get_main_inventory_size(player.inventory); ++i) {
							ItemStack stack2 = WandsMod.compat.get_player_main_stack(player.inventory, i);
							if (!stack2.isEmpty() &&
								item_stack.getItem() == stack2.getItem() && 
								WandsMod.compat.item_stacks_equal(item_stack, stack2) && 
								stack2.getCount()>=d) {
								slot = i;
							}
						}						
						if (slot > -1) {
							placed = player.world.setBlockState(pos, state);
							//placed = true;
							if(placed){
                                WandsMod.compat.player_stack_dec(player.inventory,slot,d);								
							}
						}
                    }
                    LOGGER.info("placed"+placed);
					if (placed) {
                        WandsMod.compat.inc_wand_damage(player,wand_stack,1);
						/*ItemStack wand_item = ;
						wand_item.damage(1, (LivingEntity)player, 
							(Consumer)((p) -> {
									((LivingEntity)p).sendToolBreakStatus(Hand.MAIN_HAND);
								}
							)
						);*/
																						
						if(BLOCKS_PER_XP!=0){														
							float diff=WandItem.calc_xp_to_next_level(player.experienceLevel);
							float prog=experienceProgress;
							if(diff>0 && BLOCKS_PER_XP!=0.0f){
								float a=(1.0f/diff)/BLOCKS_PER_XP;
								if(prog-a>0){
									prog=prog-a;
								}else{
									if(prog>0.0f){				
										//TODO: dirty solution....
										prog=1.0f+(a-prog);
									}else{
										prog=1.0f;
									}
									if(player.experienceLevel>0){
										player.experienceLevel--;
										diff=WandItem.calc_xp_to_next_level(player.experienceLevel);
										a=(1.0f/diff)/BLOCKS_PER_XP;
										if(prog-a>0){
											prog=prog-a;
										}
									}
								}
							}									
							/*player.experienceProgress = prog;

							final PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
							passedData.writeInt(player.experienceLevel);
							passedData.writeFloat(player.experienceProgress);
							ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, WandsMod.compat.WANDXP_PACKET_ID,
									passedData);*/
						}
					}
				}
			}
		}
		return placed;
	}
}
