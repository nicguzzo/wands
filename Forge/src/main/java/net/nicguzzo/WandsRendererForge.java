package net.nicguzzo;

import net.nicguzzo.common.WandsBaseRenderer;
import net.nicguzzo.common.MyDir;

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
		ItemStack item =player.inventory.getSelected();
		return (item.getItem() instanceof WandItemForge);
	}
	
	public static void render(MatrixStack matrixStack,double camX, double camY, double camZ) {
		
		Minecraft client = Minecraft.getInstance();
		ClientPlayerEntity player = client.player;
		ItemStack item = player.inventory.getSelected();
		//LOGGER.info("wand render mixin!!! "+item );
		if (item.getItem() instanceof WandItemForge && client.hitResult instanceof BlockRayTraceResult) {
			BlockRayTraceResult ray_result=(BlockRayTraceResult)client.hitResult;
			//LOGGER.info("ray_result!!! "+ray_result);
			if(ray_result!=null && ray_result.getType() == RayTraceResult.Type.BLOCK){
				//LOGGER.info("wand hit block!!!");
				BlockState block_state=client.level.getBlockState(ray_result.getBlockPos());
				WandItemForge wnd=(WandItemForge)item.getItem();
				boolean is_double_slab=false;
				boolean is_slab_top=false;
				if (block_state.getBlock() instanceof SlabBlock) {					
					is_double_slab=block_state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE;
					is_slab_top=block_state.getValue(SlabBlock.TYPE) == SlabType.TOP;
				}
				MyDir dir=MyDir.values()[ray_result.getDirection().ordinal()];
				VoxelShape shape=block_state.getShape(client.level, ray_result.getBlockPos());
				
				boolean is_full_cube=Block.isShapeFullBlock(shape);
				
				WandsBaseRenderer.render(client.level,player,ray_result.getBlockPos(), block_state, camX, camY, camZ, 
					wnd.wand.getLimit(),wnd.wand.isCreative(player),
					is_double_slab,is_slab_top,
					player.experienceProgress,
					dir,
					is_full_cube,
					ray_result.getLocation().x,ray_result.getLocation().y,ray_result.getLocation().z
				);
			}
		}
	}
}
