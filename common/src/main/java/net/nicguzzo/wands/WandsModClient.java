package net.nicguzzo.wands;

import org.lwjgl.glfw.GLFW;

import me.shedaniel.architectury.event.events.client.ClientLifecycleEvent;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import me.shedaniel.architectury.registry.KeyBindings;
import me.shedaniel.architectury.registry.MenuRegistry;
import net.minecraft.client.KeyMapping;



public class WandsModClient {
    static public boolean valid=false;
    public static void initialize() {
        KeyMapping modeKB=new KeyMapping("key.wands.wand_mode",GLFW.GLFW_KEY_V,"category.wands");
        
        KeyBindings.registerKeyBinding(modeKB);
        
        ClientTickEvent.CLIENT_WORLD_POST.register(e -> {
            if (modeKB.consumeClick()) {
				System.out.println("mode kbinding");
			}
        });

        ClientLifecycleEvent.CLIENT_SETUP.register(e->{
            MenuRegistry.registerScreenFactory(WandsMod.PALETTE_SCREEN_HANDLER.get(), PaletteScreen::new);
        });
    }
    /*public static void render(PoseStack matrixStack, double camX, double camY, double camZ,MultiBufferSource.BufferSource bufferIn) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		ItemStack stack = player.getMainHandItem();
		if (stack!=null && stack.isEmpty() && stack.getItem() instanceof WandItem) {
			//WandItem wand = (WandItem) stack.getItem();
            ClientRender.render(matrixStack,camX,camY,camZ, bufferIn,client.hitResult,stack);
		}
	}*/
}