package net.nicguzzo;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeybindingRegistry {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->{
             ClientRegistry.registerKeyBinding(KeyBoardInput.WAND_MODE_KEY);
             ClientRegistry.registerKeyBinding(KeyBoardInput.WAND_ORIENTATION_KEY);
             ClientRegistry.registerKeyBinding(KeyBoardInput.WAND_INVERT_KEY);
             ClientRegistry.registerKeyBinding(KeyBoardInput.WAND_PALETTE_MODE_KEY);
        });
    }
}
