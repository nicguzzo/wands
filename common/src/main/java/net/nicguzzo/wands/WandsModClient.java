package net.nicguzzo.wands;

import org.lwjgl.glfw.GLFW;

import io.netty.buffer.Unpooled;
import me.shedaniel.architectury.event.events.client.ClientLifecycleEvent;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.registry.KeyBindings;
import me.shedaniel.architectury.registry.MenuRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class WandsModClient {
    
    public static void initialize() {
        KeyMapping modeKB=new KeyMapping("key.wands.wand_mode",GLFW.GLFW_KEY_V,"category.wands");
        
        KeyBindings.registerKeyBinding(modeKB);
        
        ClientTickEvent.CLIENT_WORLD_POST.register(e -> {
            if (modeKB.consumeClick()) {
				System.out.println("mode kbinding");
                send_key(modeKB.getDefaultKey().getValue());
			}
        });

        ClientLifecycleEvent.CLIENT_SETUP.register(e->{
            MenuRegistry.registerScreenFactory(WandsMod.PALETTE_SCREEN_HANDLER.get(), PaletteScreen::new);
        });
    }
    static void send_key(int key){
        Minecraft client=Minecraft.getInstance();
        ItemStack item_stack=client.player.getMainHandItem();
            if(item_stack.getItem() instanceof WandItem){
            FriendlyByteBuf packet=new FriendlyByteBuf(Unpooled.buffer());
            packet.writeInt(key);
            NetworkManager.sendToServer(WandsMod.KB_PACKET, packet);
        }                
    }

}