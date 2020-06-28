package net.nicguzzo;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import java.util.Random;
import java.util.Vector;
import java.util.function.Consumer;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.RandomRandomFeature;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class WandsMod implements ModInitializer {

	public static WandsConfig config;
	public static final Identifier WAND_PACKET_ID      = new Identifier("wands", "wand");
	public static final Identifier WANDXP_PACKET_ID    = new Identifier("wands", "wandxp");
	public static final Identifier WANDCONF_PACKET_ID  = new Identifier("wands", "wandconf");
	public static final Identifier WAND_UNDO_PACKET_ID = new Identifier("wands", "wandundo");
	
	public static final WandItem NETHERITE_WAND_ITEM = new WandItem(ToolMaterials.NETHERITE,37, 2031);
	public static final WandItem DIAMOND_WAND_ITEM   = new WandItem(ToolMaterials.DIAMOND,27, 1561);
	public static final WandItem IRON_WAND_ITEM      = new WandItem(ToolMaterials.IRON,17, 250);
	public static final WandItem STONE_WAND_ITEM     = new WandItem(ToolMaterials.STONE, 7, 131);
	
	@Override
	public void onInitialize() {

		load_config();
		Registry.register(Registry.ITEM, new Identifier("wands", "netherite_wand"), NETHERITE_WAND_ITEM);
		Registry.register(Registry.ITEM, new Identifier("wands", "diamond_wand"), DIAMOND_WAND_ITEM);
		Registry.register(Registry.ITEM, new Identifier("wands", "iron_wand"), IRON_WAND_ITEM);
		Registry.register(Registry.ITEM, new Identifier("wands", "stone_wand"), STONE_WAND_ITEM);
		
		ServerSidePacketRegistry.INSTANCE.register(WANDCONF_PACKET_ID, (packetContext, attachedData) -> {			
			packetContext.getTaskQueue().execute(() -> {
				final PlayerEntity player = packetContext.getPlayer();
				final PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
				passedData.writeFloat(WandsMod.config.blocks_per_xp);
				System.out.println("sending blocks_per_xp : "+WandsMod.config.blocks_per_xp);
				ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, WandsMod.WANDCONF_PACKET_ID,passedData);				
			});
		});
		ServerSidePacketRegistry.INSTANCE.register(WAND_PACKET_ID, (packetContext, attachedData) -> {
			final BlockPos pos_state = attachedData.readBlockPos();
			final BlockPos pos0 = attachedData.readBlockPos();
			final BlockPos pos1 = attachedData.readBlockPos();
			final boolean randomize = attachedData.readBoolean();
			packetContext.getTaskQueue().execute(() -> {				
				if (!World.isHeightInvalid(pos_state) && !World.isHeightInvalid(pos0)) {
					final PlayerEntity player = packetContext.getPlayer();
					BlockState state = player.world.getBlockState(pos_state);
					if(pos0.equals(pos1)){
						place(player,state,pos0);
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
						System.out.println("from "+pos0);
						System.out.println("to   "+pos1);
						System.out.println("xs="+xs);
						System.out.println("xe="+xe);
						System.out.println("ys="+ys);
						System.out.println("ye="+ye);
						System.out.println("zs="+zs);
						System.out.println("ze="+ze);
						int count=0;
						
						Vector<Block> slots=new Vector<Block>();
						if(randomize){
							for (int i = 0; i < player.inventory.main.size(); ++i) {							
								final ItemStack stack2 = (ItemStack) player.inventory.main.get(i);
								Block blk=Block.getBlockFromItem(stack2.getItem());						
								if(blk != Blocks.AIR){
									slots.add(blk);
								}				
							}
						}
						
						for(int z=zs;z<ze;z++){
							for(int y=ys;y<ye;y++){
								for(int x=xs;x<xe;x++){
									BlockPos pos=new BlockPos(x,y,z);
									if(randomize){
										if(slots.size()>0){										
											int random_slot = player.world.random.nextInt(slots.size());
											state=slots.get(random_slot).getDefaultState();
											state=state.rotate(BlockRotation.random(player.world.random));
										}
										/*Direction dir=Direction.random(player.world.random);						
										BlockRotation rot=BlockRotation.NONE;
										rot.rotate(dir);
										System.out.println("dir "+dir);
										System.out.println("rot "+rot);
										state=state.rotate(rot);*/
									}
									if(place(player,state,pos)){
										count++;
									}
									//System.out.println("pos "+pos);
								}
							}
						}
						System.out.println("Placed "+count+" blocks.");
					}
				}
			});
		});
		ServerSidePacketRegistry.INSTANCE.register(WAND_UNDO_PACKET_ID, (packetContext, attachedData) -> {
			final BlockPos pos0 = attachedData.readBlockPos();
			packetContext.getTaskQueue().execute(() -> {
				if (!World.isHeightInvalid(pos0)) {
					final PlayerEntity player = packetContext.getPlayer();
					final BlockState state = player.world.getBlockState(pos0);
					if(!state.isAir()){
						if (player.abilities.creativeMode) {
							player.world.setBlockState(pos0, Blocks.VOID_AIR.getDefaultState());
						}
					}
				}
			});
		});
	}
	private boolean place(PlayerEntity player,BlockState state,BlockPos pos){
		boolean placed = false;				
		float BLOCKS_PER_XP=WandsMod.config.blocks_per_xp;
		
		Block block=state.getBlock();		
		BlockState state2=player.world.getBlockState(pos);
		int d=1;
		boolean is_water = state2.getFluidState().isIn(FluidTags.WATER);
		if (state2.isAir() || is_water) {
			int slot = -1;
			/*
			
			for (int i = 0; i < player.inventory.main.size(); ++i) {
				//int random_slot = player.world.random.nextInt(player.inventory.main.size());
				final ItemStack stack2 = (ItemStack) player.inventory.main.get(i);
				Block blk=Block.getBlockFromItem(stack2.getItem());
				if(blk != Blocks.AIR && stack2.getCount()>=d){
					slot=random_slot;
					state=blk.getDefaultState();
					break;
				}				
			}
			System.out.println("slot: "+slot);*/

			if((block instanceof  PaneBlock) || (block instanceof  FenceBlock)){
				state=state.getBlock().getDefaultState();
			}else if(block instanceof SlabBlock){
				if(state.get(SlabBlock.TYPE)==SlabType.DOUBLE){
					d=2;//should consume 2 if its a double slab
				}
			}
		
			if (player.abilities.creativeMode) {
				player.world.setBlockState(pos, state);
			} else {						
				float xp=WandsMod.calc_xp(player.experienceLevel,player.experienceProgress);		
				float dec=0.0f;
				if(BLOCKS_PER_XP!=0){
					dec=  (1.0f/BLOCKS_PER_XP);
				}
				if (BLOCKS_PER_XP == 0 ||  (xp - dec) > 0) {
					
					final ItemStack item_stack = new ItemStack(state.getBlock());
					final ItemStack off_hand_stack = (ItemStack) player.inventory.offHand.get(0);
					if (!off_hand_stack.isEmpty() && item_stack.getItem() == off_hand_stack.getItem()
							&& ItemStack.areTagsEqual(item_stack, off_hand_stack)
							&& off_hand_stack.getCount()>=d
						) {
											
						placed = player.world.setBlockState(pos, state);
						if(placed)
							player.inventory.offHand.get(0).decrement(d);
					} else {
						
						for (int i = 0; i < player.inventory.main.size(); ++i) {
							final ItemStack stack2 = (ItemStack) player.inventory.main.get(i);
							if (!((ItemStack) player.inventory.main.get(i)).isEmpty()
									&& item_stack.getItem() == stack2.getItem()
									&& ItemStack.areTagsEqual(item_stack, stack2)
									&& stack2.getCount()>=d) {
								slot = i;
							}
						}						
						if (slot > -1) {
							placed = player.world.setBlockState(pos, state);
							//placed = true;
							if(placed){
								player.inventory.getStack(slot).decrement(d);
							}
						}
					}
					if (placed) {
						final ItemStack wand_item = player.getMainHandStack();					
						wand_item.damage(1, (LivingEntity)player, 
							(Consumer)((p) -> {
									((LivingEntity)p).sendToolBreakStatus(Hand.MAIN_HAND);
								}
							)
						);
																						
						if(BLOCKS_PER_XP!=0){														
							float diff=WandsMod.calc_xp_to_next_level(player.experienceLevel);
							float prog=player.experienceProgress;
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
										diff=WandsMod.calc_xp_to_next_level(player.experienceLevel);
										a=(1.0f/diff)/BLOCKS_PER_XP;
										if(prog-a>0){
											prog=prog-a;
										}
									}
								}
							}									
							player.experienceProgress = prog;

							final PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
							passedData.writeInt(player.experienceLevel);
							passedData.writeFloat(player.experienceProgress);
							ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, WandsMod.WANDXP_PACKET_ID,
									passedData);
						}
					}
				}
			}
		}
		return placed;
	}
	public static float calc_xp(final int level,float prog) {
		float xp=WandsMod.calc_xp_level(level);
		if(prog>0){							
			xp=xp+ prog * (WandsMod.calc_xp_level(level+1)-xp);
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

	private void load_config(){
		File configFile = new File(FabricLoader.getInstance().getConfigDirectory(), "wands.json");
		try (FileReader reader = new FileReader(configFile)) {
			config = new Gson().fromJson(reader, WandsConfig.class);
			try (FileWriter writer = new FileWriter(configFile)) {
				writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(config));
			} catch (IOException e2) {
				System.out.println("Failed to update config file!");
			}
			System.out.println("Config loaded!");
			
		} catch (IOException e) {
			System.out.println("No config found, generating!");
			config = new WandsConfig();
			try (FileWriter writer = new FileWriter(configFile)) {
				writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(config));
			} catch (IOException e2) {
				System.out.println("Failed to generate config file!");
			}
		}
	}
}