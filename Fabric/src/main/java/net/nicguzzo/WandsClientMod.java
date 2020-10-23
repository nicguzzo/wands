package net.nicguzzo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.util.InputUtil;
import net.nicguzzo.common.WandItem;
import net.nicguzzo.common.WandsBaseRenderer;
import net.nicguzzo.common.WandsBaseRenderer.MyDir;

import org.lwjgl.glfw.GLFW;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.client.util.math.MatrixStack;

public class WandsClientMod implements ClientModInitializer {

	
	private static KeyBinding modeKB;
	private static KeyBinding orientationKB;
	private static KeyBinding invertKB;
	//private static KeyBinding undoKB;
	private static KeyBinding palKB;
	public static float BLOCKS_PER_XP = 0;
	public static boolean conf = false;
	public static BlockPos fill_pos1=null;
	
	@Override
	public void onInitializeClient() {

		ClientSidePacketRegistry.INSTANCE.register(WandsMod.WANDXP_PACKET_ID, (packetContext, attachedData) -> {

			int xp_l = attachedData.readInt();
			float xp_p = attachedData.readFloat();
			packetContext.getTaskQueue().execute(() -> {
				MinecraftClient client = MinecraftClient.getInstance();
				ClientPlayerEntity player = client.player;
				player.experienceLevel = xp_l;
				player.experienceProgress = xp_p;
				// System.out.println("update xp!");
				// System.out.println("xp prog: "+ player.experienceProgress);
			});
		});
		ClientSidePacketRegistry.INSTANCE.register(WandsMod.WANDCONF_PACKET_ID, (packetContext, attachedData) -> {

			float bpxp = attachedData.readFloat();

			packetContext.getTaskQueue().execute(() -> {
				WandsClientMod.BLOCKS_PER_XP = bpxp;
				System.out.println("got BLOCKS_PER_XP from server " + BLOCKS_PER_XP);
			});
		});

		modeKB = new KeyBinding("key.wands.wand_mode", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.wands");
		KeyBindingHelper.registerKeyBinding(modeKB);
		orientationKB = new KeyBinding("key.wands.wand_orientation", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "category.wands");
		KeyBindingHelper.registerKeyBinding(orientationKB);
		//undoKB        = new KeyBinding("key.wands.wand_undo", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, "category.wands" );
		//KeyBindingHelper.registerKeyBinding(undoKB);
		invertKB      = new KeyBinding("key.wands.wand_invert", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, "category.wands" );
		KeyBindingHelper.registerKeyBinding(invertKB);
		palKB      = new KeyBinding("key.wands.wand_palette_mode", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.wands" );
		KeyBindingHelper.registerKeyBinding(palKB);
		
		ClientTickEvents.END_CLIENT_TICK.register(e -> {
			
			if (modeKB.wasPressed()) {
				if(hasWandOnHand(e.player)) WandItem.cycleMode();
			}
			if (orientationKB.wasPressed()) {
				if(hasWandOnHand(e.player)) WandItem.cycleOrientation();
			}
			//if (undoKB.wasPressed()) {
			//	WandItem.undo();
			//}
			if (invertKB.wasPressed()) {
				if(hasWandOnHand(e.player)) WandItem.toggleInvert();
			}
			if (palKB.wasPressed()) {
				if(hasWandOnHand(e.player)) WandItem.cyclePalleteMode();
			}
		});
		
	/*	AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			ItemStack item = player.inventory.getMainHandStack();	
			if (item.getItem() instanceof WandItem) {
					//player.sendMessage(new LiteralText("wand attack"),true);        
					return ActionResult.SUCCESS;
			} else {
					return ActionResult.PASS;
			}
		});*/
	}
	public boolean hasWandOnHand(PlayerEntity player){
		ItemStack item = player.inventory.getMainHandStack();	
		return (item.getItem() instanceof WandItemFabric);
	}
	public static void render(MatrixStack matrixStack,double camX, double camY, double camZ) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		ItemStack stack = player.inventory.getMainHandStack();
		if (stack.getItem() instanceof WandItemFabric) {
			WandItemFabric wnd=(WandItemFabric)stack.getItem();
			HitResult hitResult = client.crosshairTarget;
			if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
				BlockHitResult block_hit = (BlockHitResult) hitResult;
				Direction side = block_hit.getSide();
				BlockPos pos = block_hit.getBlockPos();
				BlockState block_state = client.world.getBlockState(pos);				
				boolean is_double_slab=false;
				boolean is_slab_top=false;
				if (block_state.getBlock() instanceof SlabBlock) {
					is_double_slab=block_state.get(SlabBlock.TYPE) == SlabType.DOUBLE;
					is_slab_top=block_state.get(SlabBlock.TYPE) == SlabType.TOP;
				}
				MyDir dir=MyDir.values()[side.ordinal()];
				WandsBaseRenderer.render(player.world,player,pos, block_state, camX, camY, camZ, 
					wnd.wand.getLimit(),wnd.wand.isCreative(player),
					is_double_slab,is_slab_top,
					player.experienceProgress,
					dir,
					block_state.isFullCube(client.world, pos),
					block_hit.getPos().x,block_hit.getPos().y,block_hit.getPos().z
				);	
			} else {
				WandItem.valid = false;
			}

		}
	}

}
