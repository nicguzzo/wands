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
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import java.util.function.Consumer;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class WandsMod implements ModInitializer {

	public static WandsConfig config;
	public static final Identifier WAND_PACKET_ID = new Identifier("wands", "wand");
	public static final Identifier WANDXP_PACKET_ID = new Identifier("wands", "wandxp");
	public static final Identifier WANDCONF_PACKET_ID = new Identifier("wands", "wandconf");
	public static final Identifier WAND_UNDO_PACKET_ID = new Identifier("wands", "wandundo");
	
	// public static final WandItem NETHERITE_WAND_ITEM = new WandItem(31,2031);
	public static final WandItem DIAMOND_WAND_ITEM = new WandItem(27, 1561);
	public static final WandItem IRON_WAND_ITEM = new WandItem(9, 250);
	public static final WandItem STONE_WAND_ITEM = new WandItem(5, 131);

	@Override
	public void onInitialize() {

		load_config();
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
			final BlockPos pos0 = attachedData.readBlockPos();
			final BlockPos pos1 = attachedData.readBlockPos();
			packetContext.getTaskQueue().execute(() -> {
				if (World.isValid(pos0) && World.isValid(pos1)) {
					final PlayerEntity player = packetContext.getPlayer();
					//final BlockState state = player.world.getBlockState(pos0);
					place(player,pos0,pos1);					
				}
			});
		});
		ServerSidePacketRegistry.INSTANCE.register(WAND_UNDO_PACKET_ID, (packetContext, attachedData) -> {
			final BlockPos pos0 = attachedData.readBlockPos();
			packetContext.getTaskQueue().execute(() -> {
				if (World.isValid(pos0)) {
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
	private void place(PlayerEntity player,BlockPos pos0,BlockPos pos1){
		float BLOCKS_PER_XP=WandsMod.config.blocks_per_xp;
		BlockState state = player.world.getBlockState(pos0);
		Block block=state.getBlock();		
		int d=1;
		if((block instanceof  PaneBlock) || (block instanceof  FenceBlock)){
			state=state.getBlock().getDefaultState();
		}else if(block instanceof SlabBlock){
			if(state.get(SlabBlock.TYPE)==SlabType.DOUBLE){
				d=2;//should consume 2 if its a double slab
			}
		}
		if (player.abilities.creativeMode) {
			player.world.setBlockState(pos1, state);
		} else {						
			float xp=WandsMod.calc_xp(player.experienceLevel,player.experienceProgress);		
			float dec=0.0f;
			if(BLOCKS_PER_XP!=0){
				dec=  (1.0f/BLOCKS_PER_XP);
			}
			if (BLOCKS_PER_XP == 0 ||  (xp - dec) > 0) {
				boolean placed = false;				
				final ItemStack item_stack = new ItemStack(state.getBlock());
				final ItemStack off_hand_stack = (ItemStack) player.inventory.offHand.get(0);
				if (!off_hand_stack.isEmpty() && item_stack.getItem() == off_hand_stack.getItem()
						&& ItemStack.areTagsEqual(item_stack, off_hand_stack)
						&& off_hand_stack.getCount()>=d
					) {
					placed = player.world.setBlockState(pos1, state);					
					//placed = true;
					if(placed)
						player.inventory.offHand.get(0).decrement(d);
				} else {
					int slot = -1;
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
						placed = player.world.setBlockState(pos1, state);
						//placed = true;
						if(placed)
							player.inventory.getInvStack(slot).decrement(d);
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