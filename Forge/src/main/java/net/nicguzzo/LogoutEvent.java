package net.nicguzzo;

import net.minecraftforge.fml.common.Mod;
import net.nicguzzo.common.WandServerSide;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = WandsMod.MODID)
public class LogoutEvent{
    @SubscribeEvent
    public static void logout_event(PlayerLoggedOutEvent event) {
		PlayerEntity player = event.getPlayer();
		if (player != null && !player.level.isClientSide()) {
			System.out.println("removing undo from player: "+player.getName().getString());
			WandServerSide.player_undo.remove(player.getStringUUID());
		}
    }
}
