package net.nicguzzo;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.client.util.math.MatrixStack;
public class WandsMod implements ModInitializer {

	public static final WandItem DIAMOND_WAND_ITEM = new WandItem(32,4096);
	public static final WandItem IRON_WAND_ITEM = new WandItem(16,512);
	public static final WandItem STONE_WAND_ITEM = new WandItem(8,128);
	private static FabricKeyBinding mode_keyBinding;
	private static FabricKeyBinding orientation_keyBinding;
	@Override
	public void onInitialize() {

		//System.out.println("Hello from UtilityblocksMod!");
		Registry.register(Registry.ITEM, new Identifier("wands", "diamond_wand"), DIAMOND_WAND_ITEM);
		Registry.register(Registry.ITEM, new Identifier("wands", "iron_wand"), IRON_WAND_ITEM);
		Registry.register(Registry.ITEM, new Identifier("wands", "stone_wand"), STONE_WAND_ITEM);

		mode_keyBinding = FabricKeyBinding.Builder.create(
				new Identifier("wands", "wand_mode"),
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_V,
				"Wands"
		).build();
		orientation_keyBinding = FabricKeyBinding.Builder.create(
				new Identifier("wands", "wand_orientation"),
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_X,
				"Wands"
		).build();
		KeyBindingRegistry.INSTANCE.addCategory("Wands");
		KeyBindingRegistry.INSTANCE.register(mode_keyBinding);
		KeyBindingRegistry.INSTANCE.register(orientation_keyBinding);
		ClientTickCallback.EVENT.register(e ->
		{
			if(mode_keyBinding.wasPressed()) {
				WandItem.toggleMode();
			}
			if(orientation_keyBinding.wasPressed()) {
				WandItem.cycleOrientation();
			}
		});
	}
	public static void render(float partialTicks,MatrixStack matrixStack){
		MinecraftClient client=MinecraftClient.getInstance();
		ClientPlayerEntity player=client.player;
		ItemStack item=player.inventory.getMainHandStack();		
		
        if(item.getItem() instanceof WandItem){
            HitResult hitResult = client.crosshairTarget;            
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
				int lim=((WandItem)item.getItem()).getLimit();
				System.out.println("lim: "+lim);
                BlockHitResult block_hit=(BlockHitResult)hitResult;                
				Direction side=block_hit.getSide();              
				BlockPos pos = block_hit.getBlockPos(); 
				BlockState block_state=client.world.getBlockState(pos);
				Box bb=player.getBoundingBox();
                Matrix4f matrix4f = matrixStack.peek().getModel();
                RenderSystem.pushMatrix();
                RenderSystem.multMatrix(matrix4f);
                Entity cplayer = MinecraftClient.getInstance().getCameraEntity();
                double cameraX = cplayer.lastRenderX + (cplayer.getX() - cplayer.lastRenderX) * (double)partialTicks;
                double cameraY = cplayer.lastRenderY + (cplayer.getY() - cplayer.lastRenderY) * (double)partialTicks + cplayer.getEyeHeight(cplayer.getPose());
                double cameraZ = cplayer.lastRenderZ + (cplayer.getZ() - cplayer.lastRenderZ) * (double)partialTicks;        
                RenderSystem.translated(-cameraX, -cameraY, -cameraZ);
                RenderSystem.disableTexture();
                RenderSystem.disableBlend();
                RenderSystem.lineWidth(1.0f);
                RenderSystem.enableAlphaTest(); 
                Tessellator tessellator=Tessellator.getInstance();
                BufferBuilder bufferBuilder = tessellator.getBuffer();
				bufferBuilder.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
				GlStateManager.lineWidth(3.0f);
				int x1=0,y1=0,z1=0;
				int x2=0,y2=0,z2=0;				
				boolean preview=false;
				Direction dir=Direction.EAST;
				
				if(block_state.isFullCube(client.world, pos)){
					if(WandItem.getMode()==0 ){										
						
						float x=pos.getX();
						float y=pos.getY();
						float z=pos.getZ();
						float o=0.01f;
						switch(side){
							case UP:
								y+=1.0f+o;
								break;
							case DOWN:
								y-=o;
								break;
							case SOUTH:
								z+=1.0f+o;
								break;
							case NORTH:
								z-=o;
								break;
							case EAST:
								x+=1.0f+o;
								break;
							case WEST:
								x-=o;
								break;
						}
						switch(side){
							case UP:                        
							case DOWN:                    
								{
								bufferBuilder.vertex(x      , y, z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+1.00f, y, z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();                        
								bufferBuilder.vertex(x      , y, z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x      , y, z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();                        
								bufferBuilder.vertex(x+1.00f, y, z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+1.00f, y, z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x      , y, z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+1.00f, y, z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x      , y, z+0.25f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+1.00f, y, z+0.25f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x      , y, z+0.75f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+1.00f, y, z+0.75f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.25f, y, z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.25f, y, z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.75f, y, z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.75f, y, z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();						
								bufferBuilder.vertex(x+0.40f, y, z+0.20f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.50f, y, z+0.05f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.60f, y, z+0.20f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.50f, y, z+0.05f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.40f, y, z+0.80f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.50f, y, z+0.95f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.60f, y, z+0.80f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.50f, y, z+0.95f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.20f, y, z+0.40f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.05f, y, z+0.50f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();						
								bufferBuilder.vertex(x+0.20f, y, z+0.60f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.05f, y, z+0.50f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.80f, y, z+0.40f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.95f, y, z+0.50f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();						
								bufferBuilder.vertex(x+0.80f, y, z+0.60f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.95f, y, z+0.50f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.40f, y, z+0.50f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.50f, y, z+0.40f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.40f, y, z+0.50f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.50f, y, z+0.60f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.60f, y, z+0.50f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.50f, y, z+0.60f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.50f, y, z+0.40f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.60f, y, z+0.50f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();

							}break;
							case NORTH:                        
							case SOUTH:{
								bufferBuilder.vertex(x      ,y      , z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+1.00f,y      , z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();                        
								bufferBuilder.vertex(x      ,y      , z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x      ,y+1.00f, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();                        
								bufferBuilder.vertex(x+1.00f,y      , z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+1.00f,y+1.00f, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x      ,y+1.00f, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+1.00f,y+1.00f, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x      ,y+0.25f, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+1.00f,y+0.25f, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x      ,y+0.75f, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+1.00f,y+0.75f, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.25f,y      , z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.25f,y+1.00f, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.75f,y      , z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.75f,y+1.00f, z) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.40f,y+0.20f, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.50f,y+0.05f, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.60f,y+0.20f, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.50f,y+0.05f, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.40f,y+0.80f, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.50f,y+0.95f, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.60f,y+0.80f, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.50f,y+0.95f, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.20f,y+0.40f, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.05f,y+0.50f, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();						
								bufferBuilder.vertex(x+0.20f,y+0.60f, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.05f,y+0.50f, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.80f,y+0.40f, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.95f,y+0.50f, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();						
								bufferBuilder.vertex(x+0.80f,y+0.60f, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.95f,y+0.50f, z) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.40f,y+0.50f, z) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.50f,y+0.40f, z) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.40f,y+0.50f, z) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.50f,y+0.60f, z) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.60f,y+0.50f, z) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.50f,y+0.60f, z) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.50f,y+0.40f, z) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x+0.60f,y+0.50f, z) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
							}
							break;
							case EAST:                        
							case WEST:{
								bufferBuilder.vertex(x,y      ,z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+1.00f,z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();                        
								bufferBuilder.vertex(x,y      ,z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x,y      ,z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();                        
								bufferBuilder.vertex(x,y+1.00f,z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+1.00f,z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x,y      ,z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+1.00f,z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x,y      ,z+0.25f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+1.00f,z+0.25f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x,y      ,z+0.75f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+1.00f,z+0.75f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.25f,z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.25f,z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.75f,z      ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.75f,z+1.00f) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.40f,z+0.20f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.50f,z+0.05f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.60f,z+0.20f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.50f,z+0.05f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.40f,z+0.80f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.50f,z+0.95f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.60f,z+0.80f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.50f,z+0.95f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.20f,z+0.40f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.05f,z+0.50f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();						
								bufferBuilder.vertex(x,y+0.20f,z+0.60f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.05f,z+0.50f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.80f,z+0.40f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.95f,z+0.50f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();						
								bufferBuilder.vertex(x,y+0.80f,z+0.60f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.95f,z+0.50f) .color(0.7f, 0.0f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.40f,z+0.50f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.50f,z+0.40f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.40f,z+0.50f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.50f,z+0.60f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.60f,z+0.50f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.50f,z+0.60f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.50f,z+0.40f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
								bufferBuilder.vertex(x,y+0.60f,z+0.50f) .color(0.0f, 0.7f, 0.0f, 1.0f).next();
							}
							break;
						}
						Direction d=WandItem.getDirectionMode1(block_hit.getPos(),side);
					
						if(d!=null){												
							int p = WandItem.find_next_pos(client.world, block_state, d, pos,lim);							
							if (p >= 0) {
								BlockPos pv=pos.offset(d,p+1);
								x1=pv.getX();
								y1=pv.getY();
								z1=pv.getZ();
								x2=x1+1;
								y2=y1+1;
								z2=z1+1;
								
								if(bb.intersects(x1, y1, z1, x2, y2, z2)){
									preview=false;
									WandItem.valid=false;
								}else{
									preview=true;																									
									WandItem.valid=true;
									WandItem.mode2_dir=d;
									WandItem.x1=x1;
									WandItem.y1=y1;
									WandItem.z1=z1;
									WandItem.x2=x2;
									WandItem.y2=y2;
									WandItem.z2=z2;
								}
								
							}
						}
					}else{
						int inc=1;
						int offx=0;
						int offy=0;
						int offz=0;	
						switch(side){
							case UP:
							case DOWN:
								switch(WandItem.getOrientation()){
									case HORIZONTAL:
										dir=Direction.EAST;
									break;
									case VERTICAL:
										dir=Direction.SOUTH;
									break;
								}
								break;
							case SOUTH:
								switch(WandItem.getOrientation()){
									case HORIZONTAL:
										dir=Direction.EAST;
									break;
									case VERTICAL:
										dir=Direction.UP;
									break;
								}
								break;
							case NORTH:
								switch(WandItem.getOrientation()){
									case HORIZONTAL:
										dir=Direction.WEST;
										offx=-1;
										inc=-1;
									break;
									case VERTICAL:
										dir=Direction.UP;
										//offy=1;
									break;
								}							
								
								
								break;
							case EAST:
								switch(WandItem.getOrientation()){
									case HORIZONTAL:
										dir=Direction.SOUTH;								
									break;
									case VERTICAL:
										dir=Direction.UP;
									break;
								}	
								break;
							case WEST:							
								switch(WandItem.getOrientation()){
									case HORIZONTAL:
										dir=Direction.NORTH;
										inc=-1;
										offz=-1;
									break;
									case VERTICAL:
										dir=Direction.UP;
									break;
								}								
								break;
						}
						int j=0;
						
						int l1=lim/2;
						for (j = 0;j < l1;j++) {												
							BlockState bs0 =client.world.getBlockState(pos.offset(side,1).offset(dir.getOpposite(),j+1));
							BlockState bs1 =client.world.getBlockState(pos.offset(dir.getOpposite(),j+1));
							if(!bs1.equals(block_state) || !bs0.isAir()){
								break;
							}
						}
						//System.out.println("j: "+j);					
						if(j>0){						
							pos=pos.offset(dir.getOpposite(),j);
						}
						BlockPos pos2=pos.offset(side,1);
						if(client.world.getBlockState(pos2).isAir()){
							x1=pos2.getX()-offx;
							y1=pos2.getY()-offy;
							z1=pos2.getZ()-offz;					
							x2=x1+1+offx*2;
							y2=y1+1+offy*2;
							z2=z1+1+offz*2;
							//int l2=(lim-j);
							for (int i = 1;i <= lim;i++) {						
								BlockState bs2 =client.world.getBlockState(pos.offset(side,1).offset(dir,i));
								BlockState bs3 =client.world.getBlockState(pos.offset(dir,i));
								if(bs3!=null ) {													
									if(bs2.isAir() && bs3.equals(block_state) ){
										switch(dir){
											case NORTH:
											case SOUTH:
												z2+=inc;
											break;
											case EAST:
											case WEST:
												x2+=inc;										
											break;
											case UP:
											case DOWN:
												y2+=inc;
											break;									
										}
									}
										
									else break;
								}
							}
							if(bb.intersects(x1, y1, z1, x2, y2, z2)){
								preview=false;
								WandItem.valid=false;
							}else{
								preview=true;
								WandItem.valid=true;
								WandItem.mode2_dir=dir;
								WandItem.x1=x1+offx;
								WandItem.y1=y1+offy;
								WandItem.z1=z1+offz;
								WandItem.x2=x2+offx;
								WandItem.y2=y2+offy;
								WandItem.z2=z2+offz;
							}
						}else{
							preview=false;
							WandItem.valid=false;
						}
					}
				}else{
					preview=false;
					WandItem.valid=false;
				}
				if(preview){
					float fx1=x1+0.001f;
					float fy1=y1+0.001f;
					float fz1=z1+0.001f;
					float fx2=x2-0.001f;
					float fy2=y2-0.001f;
					float fz2=z2-0.001f;
					bufferBuilder.vertex(fx1, fy1,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy1,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy1,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy1,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();					
					bufferBuilder.vertex(fx1, fy1,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy1,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy1,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy1,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy2,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy2,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy2,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy2,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();					
					bufferBuilder.vertex(fx1, fy2,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy2,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy2,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy2,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy1,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy2,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy1,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy2,fz1 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy1,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx1, fy2,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy1,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
					bufferBuilder.vertex(fx2, fy2,fz2 ) .color(1.0f, 1.0f, 1.0f, 1.0f).next();
				}
                tessellator.draw();
                RenderSystem.translated(0, 0, 0);
                RenderSystem.lineWidth(1.0f);
                RenderSystem.enableBlend();
                RenderSystem.enableTexture();
                RenderSystem.popMatrix();
            }else{
				WandItem.valid=false;
			}
        }
	}
}
