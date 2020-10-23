package net.nicguzzo;

import net.nicguzzo.common.WandsBaseRenderer;
import net.nicguzzo.common.WandsBaseRenderer.MyDir;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WandsRendererForge {
	
	public static float BLOCKS_PER_XP = 0;
	public static boolean conf = false;
	public static BlockPos fill_pos1=null;	
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	public boolean hasWandOnHand(PlayerEntity player){				
		ItemStack item =player.inventory.getCurrentItem();
		return (item.getItem() instanceof WandItemForge);
	}
	
	public static void render(MatrixStack matrixStack,double camX, double camY, double camZ) {
		
		Minecraft client = Minecraft.getInstance();
		ClientPlayerEntity player = client.player;
		ItemStack item = player.inventory.getCurrentItem();
		//LOGGER.info("wand render mixin!!!");
		if (item.getItem() instanceof WandItemForge && client.objectMouseOver instanceof BlockRayTraceResult) {
			BlockRayTraceResult ray_result=(BlockRayTraceResult)client.objectMouseOver;
			if(ray_result!=null && ray_result.getType() == RayTraceResult.Type.BLOCK){
				//LOGGER.info("wand hit block!!!");
				BlockState block_state=client.world.getBlockState(ray_result.getPos());
				WandItemForge wnd=(WandItemForge)item.getItem();
				boolean is_double_slab=false;
				boolean is_slab_top=false;
				if (block_state.getBlock() instanceof SlabBlock) {
					is_double_slab=block_state.get(SlabBlock.TYPE) == SlabType.DOUBLE;
					is_slab_top=block_state.get(SlabBlock.TYPE) == SlabType.TOP;
				}
				MyDir dir=MyDir.values()[ray_result.getFace().getIndex()];
				VoxelShape shape=block_state.getShape(client.world, ray_result.getPos());
				
				boolean is_full_cube=Block.isOpaque(shape);
				
				WandsBaseRenderer.render(client.world,player,ray_result.getPos(), block_state, camX, camY, camZ, 
					wnd.wand.getLimit(),wnd.wand.isCreative(player),
					is_double_slab,is_slab_top,
					player.experience,
					dir,
					is_full_cube,
					ray_result.getHitVec().x,ray_result.getHitVec().y,ray_result.getHitVec().z
				);
			}
		}
	}
}
