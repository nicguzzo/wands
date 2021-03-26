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

import java.util.HashMap;


public class WandServerSide {
    private static final Logger LOGGER = LogManager.getLogger();
	private static final int MAX_UNDO=2048;

	private static HashMap<Integer,CircularBuffer> player_undo=new  HashMap<Integer,CircularBuffer>();

    public static void placeBlock(PlayerEntity player,BlockPos pos_state,BlockPos pos0,BlockPos pos1,int pm,boolean isCreative,float experienceProgress,ItemStack wand_stack,int mode,int plane){
        World world=player.world;

		WandItem.PaletteMode palatte_mode=WandItem.PaletteMode.values()[pm];
        BlockState state = world.getBlockState(pos_state);
        
		LOGGER.info("mode " +mode);
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
			if(mode==4){//line
				System.out.println("Line! pos0 "+pos0+" pos1 "+pos1);
				WandServerSide.line(pos0,pos1,player,state,palatte_mode,isCreative,experienceProgress,wand_stack);
			}else if(mode==5){//circle
				System.out.println("circle! pos0 "+pos0+" pos1 "+pos1);
				WandServerSide.circle(pos0,pos1,player,state,palatte_mode,isCreative,experienceProgress,wand_stack,plane);
			}else{
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
			}
            //System.out.println("Placed "+count+" blocks.");
            //System.out.println("Block: "+state);
        }
    }
	static public void undo(PlayerEntity player,int n){
		CircularBuffer u=player_undo.get(player.getEntityId());
		if(u!=null){
			for(int i=0;i<n && i<u.size();i++){
				BlockPos p=u.pop();
				if(p!=null){					
					player.world.setBlockState(p, Blocks.AIR.getDefaultState());					
				}
			}
			//u.print();
		}
	}
	static public void redo(PlayerEntity player,int n){
		CircularBuffer u=player_undo.get(player.getEntityId());
		if(u!=null){
			//System.out.println("redo");
			for(int i=0;i<n && u.can_go_forward();i++){
				//System.out.println("redo "+i);
				u.forward();
				CircularBuffer.P p=u.peek();
				if(p!=null && p.pos!=null && p.state!=null){					
					player.world.setBlockState(p.pos, p.state);					
				}
			}
			//u.print();
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
		if (WandsBaseRenderer.can_place(state2, wand,player.world,pos)) {
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
				int id=player.getEntityId();
				if(player_undo.get(id)==null){
					player_undo.put(id,new CircularBuffer(MAX_UNDO));
				}
				CircularBuffer u=player_undo.get(id);
				u.put(pos,state);
				//u.print();
				player.world.setBlockState(pos, state);
			} else {						
				float xp=WandItem.calc_xp(player.experienceLevel,experienceProgress);		
				float dec=0.0f;
				//System.out.println("BLOCKS_PER_XP "+BLOCKS_PER_XP);
				//LOGGER.info("BLOCKS_PER_XP "+BLOCKS_PER_XP);
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
						//LOGGER.info("placed "+placed);
						if(placed)
                            WandsMod.compat.player_offhand_stack_dec(player.inventory,d);
					} else {
						//LOGGER.info("inv size "+WandsMod.compat.get_main_inventory_size(player.inventory));		
						//LOGGER.info("item_stack "+item_stack);
						for (int i = 0; i < WandsMod.compat.get_main_inventory_size(player.inventory); ++i) {
							ItemStack stack2 = WandsMod.compat.get_player_main_stack(player.inventory, i);
							//LOGGER.info("stack2 "+stack2);
							if (!stack2.isEmpty() &&
								item_stack.getItem() == stack2.getItem() /*&& 
								WandsMod.compat.item_stacks_equal(item_stack, stack2)*/ && 
								stack2.getCount()>=d) {
								slot = i;
							}
						}				
						//LOGGER.info("slot "+slot);		
						if (slot > -1) {

							placed = player.world.setBlockState(pos, state);
							//LOGGER.info("placed "+placed);
							//placed = true;
							if(placed){
                                WandsMod.compat.player_stack_dec(player.inventory,slot,d);								
							}
						}
                    }
                    //LOGGER.info("placed"+placed);
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
	// bresenham 3d from https://www.geeksforgeeks.org/bresenhams-algorithm-for-3-d-line-drawing/
	private static void line(BlockPos pos0,BlockPos pos1,PlayerEntity player,BlockState state,WandItem.PaletteMode palatte_mode,boolean isCreative,float experienceProgress,ItemStack wand_stack)  
    {  
		
		int x1=pos0.getX();
		int y1=pos0.getY();
		int z1=pos0.getZ();
		int x2=pos1.getX();
		int y2=pos1.getY();
		int z2=pos1.getZ();
		int dx,dy,dz,xs,ys,zs,p1,p2;
		dx = Math.abs(x2 - x1);
		dy = Math.abs(y2 - y1); 
		dz = Math.abs(z2 - z1); 
		if (x2 > x1){
			xs = 1;
		}else{
			xs = -1;
		}
		if (y2 > y1){
			ys = 1;
		}else{
			ys = -1;
		}
		if (z2 > z1){
			zs = 1;
		}else{
			zs = -1;
		}
	  
		//X
		if (dx >= dy && dx >= dz){
			p1 = 2 * dy - dx ;
			p2 = 2 * dz - dx ;
			while (x1 != x2){
				x1 += xs ;
				if (p1 >= 0){
					y1 += ys ;
					p1 -= 2 * dx ;
				}
				if (p2 >= 0){
					z1 += zs ;
					p2 -= 2 * dx ;
				}
				p1 += 2 * dy ;
				p2 += 2 * dz ;
				BlockPos pos=new BlockPos(x1,y1,z1);
				//LOGGER.info("line pos " +pos);
				place(player,state,pos,palatte_mode,isCreative,experienceProgress,wand_stack);
			}
		}else if (dy >= dx && dy >= dz){
			p1 = 2 * dx - dy ;
			p2 = 2 * dz - dy ;
			while (y1 != y2){
				y1 += ys ;
				if (p1 >= 0){
					x1 += xs ;
					p1 -= 2 * dy ;
				}
				if (p2 >= 0){
					z1 += zs ;
					p2 -= 2 * dy ;
				}
				p1 += 2 * dx ;
				p2 += 2 * dz ;
				BlockPos pos=new BlockPos(x1,y1,z1);
				//LOGGER.info("line pos " +pos);
				place(player,state,pos,palatte_mode,isCreative,experienceProgress,wand_stack);
			}
		}else{
			p1 = 2 * dy - dz ;
			p2 = 2 * dx - dz ;
			while (z1 != z2){ 
				z1 += zs ;
				if (p1 >= 0){ 
					y1 += ys ;
					p1 -= 2 * dz ;
				}
				if (p2 >= 0){
					x1 += xs ;
					p2 -= 2 * dz ;
				}
				p1 += 2 * dy ;
				p2 += 2 * dx ;				
				place(player,state,new BlockPos(x1,y1,z1),palatte_mode,isCreative,experienceProgress,wand_stack);
			}
		}
    }  

	static private void drawCircleXY(int xc, int yc,int zc, int x, int y,int z,PlayerEntity player,BlockState state,WandItem.PaletteMode palatte_mode,boolean isCreative,float experienceProgress,ItemStack wand_stack)
	{
		place(player,state,new BlockPos(xc+x, yc+y,zc),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc-x, yc+y,zc),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc+x, yc-y,zc),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc-x, yc-y,zc),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc+y, yc+x,zc),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc-y, yc+x,zc),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc+y, yc-x,zc),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc-y, yc-x,zc),palatte_mode,isCreative,experienceProgress,wand_stack);		
	}
	static private void drawCircleXZ(int xc, int yc,int zc, int x, int y,int z,PlayerEntity player,BlockState state,WandItem.PaletteMode palatte_mode,boolean isCreative,float experienceProgress,ItemStack wand_stack)
	{
		place(player,state,new BlockPos(xc+x, yc,zc+z),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc-x, yc,zc+z),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc+x, yc,zc-z),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc-x, yc,zc-z),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc+z, yc,zc+x),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc-z, yc,zc+x),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc+z, yc,zc-x),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc-z, yc,zc-x),palatte_mode,isCreative,experienceProgress,wand_stack);		
	}
	static private void drawCircleYZ(int xc, int yc,int zc, int x, int y,int z,PlayerEntity player,BlockState state,WandItem.PaletteMode palatte_mode,boolean isCreative,float experienceProgress,ItemStack wand_stack)
	{
		place(player,state,new BlockPos(xc, yc-y, zc+z),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc, yc+y, zc+z),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc, yc+y, zc-z),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc, yc-y, zc-z),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc, yc+z, zc+y),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc, yc-z, zc+y),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc, yc+z, zc-y),palatte_mode,isCreative,experienceProgress,wand_stack);
		place(player,state,new BlockPos(xc, yc-z, zc-y),palatte_mode,isCreative,experienceProgress,wand_stack);		
	}
	private static void circle(BlockPos pos0,BlockPos pos1,PlayerEntity player,BlockState state,WandItem.PaletteMode palatte_mode,boolean isCreative,float experienceProgress,ItemStack wand_stack,int plane)
	{
		int r =1;
		int xc=pos0.getX();
		int yc=pos0.getY();
		int zc=pos0.getZ();
		int px=pos1.getX()-pos0.getX();
		int py=pos1.getY()-pos0.getY();
		int pz=pos1.getZ()-pos0.getZ();
		r=(int)Math.sqrt(px*px+py*py+pz*pz );
		/*int x = 0, z = r;
		int y=0;
		int d = 3 - 2 * r;
		drawCircleXZ(xc, yc,zc, x, y,z,player,state,palatte_mode,isCreative,experienceProgress,wand_stack);
		while (z >= x)
		{
			x++; 
			if (d > 0)
			{
				z--; 
				d = d + 4 * (x - z) + 10;
			}
			else
				d = d + 4 * x + 6;
			drawCircleXZ(xc, yc,zc, x, y,z,player,state,palatte_mode,isCreative,experienceProgress,wand_stack);
			//drawCircle(xc, yc, x, y);
			
		}*/

		if(plane==0){//XZ;
			int x = 0, y=0, z = r;
			int d = 3 - 2 * r;
			drawCircleXZ(xc, yc,zc, x, y,z,player,state,palatte_mode,isCreative,experienceProgress,wand_stack);
			while (z >= x)
			{
				x++; 
				if (d > 0)
				{
					z--; 
					d = d + 4 * (x - z) + 10;
				}
				else
					d = d + 4 * x + 6;
					drawCircleXZ(xc, yc,zc, x, y,z,player,state,palatte_mode,isCreative,experienceProgress,wand_stack);
			}
		}else if(plane==1){//XY;
			int x = 0, y = r, z=0;
			int d = 3 - 2 * r;
			drawCircleXY(xc, yc,zc, x, y,z,player,state,palatte_mode,isCreative,experienceProgress,wand_stack);
			while (y >= x)
			{
				x++; 
				if (d > 0)
				{
					y--; 
					d = d + 4 * (x - y) + 10;
				}
				else
					d = d + 4 * x + 6;
					drawCircleXY(xc, yc,zc, x, y,z,player,state,palatte_mode,isCreative,experienceProgress,wand_stack);
			}
		}else if(plane==2){//YZ;
			int x = 0, y = 0, z=r;
			int d = 3 - 2 * r;
			drawCircleYZ(xc, yc,zc, x, y,z,player,state,palatte_mode,isCreative,experienceProgress,wand_stack);
			while (z >= y)
			{
				y++; 
				if (d > 0)
				{
					z--; 
					d = d + 4 * (y - z) + 10;
				}
				else
					d = d + 4 * y + 6;
					drawCircleYZ(xc, yc,zc, x, y,z,player,state,palatte_mode,isCreative,experienceProgress,wand_stack);
			}
		}
	}
}
