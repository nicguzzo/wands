package net.nicguzzo;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.function.Consumer;

import net.nicguzzo.common.WandServerSide;
import net.nicguzzo.common.WandsConfig;
import net.nicguzzo.common.WandsBaseRenderer.MyDir;

public class WandsMod implements ModInitializer {

	public static WandsConfig config=null;
	public static final Identifier WAND_PACKET_ID      = new Identifier("wands", "wand");
	public static final Identifier WANDXP_PACKET_ID    = new Identifier("wands", "wandxp");
	public static final Identifier WANDCONF_PACKET_ID  = new Identifier("wands", "wandconf");
	public static final Identifier WAND_UNDO_PACKET_ID = new Identifier("wands", "wandundo");
	
	public static final WandItemFabric NETHERITE_WAND_ITEM = new WandItemFabric(ToolMaterials.NETHERITE,37, 2031);
	public static final WandItemFabric DIAMOND_WAND_ITEM   = new WandItemFabric(ToolMaterials.DIAMOND  ,27, 1561);
	public static final WandItemFabric IRON_WAND_ITEM      = new WandItemFabric(ToolMaterials.IRON     ,17, 250);
	public static final WandItemFabric STONE_WAND_ITEM     = new WandItemFabric(ToolMaterials.STONE    , 7, 131);
	private static boolean is_netherite_wand=false;
	public static final PaletteItem PALETTE_ITEM = new PaletteItem();
	
	@Override
	public void onInitialize() {
		config=WandsConfig.load_config(FabricLoader.getInstance().getConfigDir());
		Registry.register(Registry.ITEM, new Identifier("wands", "netherite_wand"), NETHERITE_WAND_ITEM);
		Registry.register(Registry.ITEM, new Identifier("wands", "diamond_wand"), DIAMOND_WAND_ITEM);
		Registry.register(Registry.ITEM, new Identifier("wands", "iron_wand"), IRON_WAND_ITEM);
		Registry.register(Registry.ITEM, new Identifier("wands", "stone_wand"), STONE_WAND_ITEM);
		Registry.register(Registry.ITEM, new Identifier("wands", "palette"), PALETTE_ITEM);
		
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
			final BlockPos state_pos = attachedData.readBlockPos();
			final BlockPos pos1 = attachedData.readBlockPos();
			final BlockPos pos2 = attachedData.readBlockPos();
			final int p = attachedData.readInt();
			
			packetContext.getTaskQueue().execute(() -> {				
				if (!World.isHeightInvalid(state_pos) && !World.isHeightInvalid(pos1) && !World.isHeightInvalid(pos2)) {
					final PlayerEntity player = packetContext.getPlayer();
					ItemStack stack=player.getMainHandStack();					
					if (stack.getItem() instanceof WandItemFabric) {
						WandServerSide.placeBlock(player,state_pos,pos1,pos2,p,player.abilities.creativeMode,player.experienceProgress,stack);
					}
				}
			});
		});
	/*	ServerSidePacketRegistry.INSTANCE.register(WAND_UNDO_PACKET_ID, (packetContext, attachedData) -> {
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
		});*/
	}
	
	
	static public boolean is_fluid(BlockState state){
		/*if(is_netherite_wand)
			return state.getFluidState().isIn(FluidTags.WATER)||state.getFluidState().isIn(FluidTags.LAVA);
		else*/
		return state.getFluidState().isIn(FluidTags.WATER);
    }
    static public boolean is_double_slab(BlockState state){
        return state.get(SlabBlock.TYPE)==SlabType.DOUBLE;        
    }
    static public BlockPos pos_offset(BlockPos pos,MyDir dir,int o)
    {
        Direction dir1=Direction.values()[dir.ordinal()];
        return pos.offset(dir1, o);
    }
    static public int get_next_int_random(PlayerEntity player,int b){
        return player.world.random.nextInt(b);
    }
    static public BlockState random_rotate(BlockState state,World world){        
        return state.rotate(BlockRotation.random(world.random));
    }
    static public int get_main_inventory_size(PlayerInventory inv){
        return inv.main.size();
    }
    static public ItemStack get_player_main_stack(PlayerInventory inv,int i){
        return inv.main.get(i);
    }
    static public ItemStack get_player_offhand_stack(PlayerInventory inv){
        return inv.offHand.get(0);
    }
    static public void player_offhand_stack_inc(PlayerInventory inv,int i){
        inv.offHand.get(0).increment(i);
    }
    static public void player_offhand_stack_dec(PlayerInventory inv,int i){
        inv.offHand.get(0).decrement(i);
    }
    static public void player_stack_inc(PlayerInventory inv,int slot,int i){
        inv.main.get(slot).increment(i);
    }
    static public void player_stack_dec(PlayerInventory inv,int slot,int i){
        inv.main.get(slot).decrement(i);
    }
    static public void set_player_xp(PlayerEntity player,float xp){
        player.experienceProgress=xp;
    }
    static public boolean item_stacks_equal(ItemStack i1,ItemStack i2){
        return ItemStack.areEqual(i1,i2);
    }
    static public boolean is_player_holding_wand(PlayerEntity player){
        return player.getActiveItem().getItem() instanceof WandItemFabric;
    }
    static public void inc_wand_damage(PlayerEntity player,ItemStack stack,int damage){
        
		stack.damage(damage, (LivingEntity)player, 
						(Consumer<LivingEntity>)((p) -> {
								((LivingEntity)p).sendToolBreakStatus(Hand.MAIN_HAND);
							}
						)
					);
        
    }
    static public boolean interescts_player_bb(PlayerEntity player,double x1,double y1,double z1,double x2,double y2,double z2){
        Box bb=player.getBoundingBox();
        return bb.intersects(x1,y1,z1,x2,y2,z2);
    }
    static public void send_message_to_player(String msg){
        assert MinecraftClient.getInstance().player != null;
        MinecraftClient instance=MinecraftClient.getInstance();
        instance.player.sendMessage(new LiteralText(msg), true);
    }
}