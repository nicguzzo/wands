package net.nicguzzo.wands.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

@Mixin(ServerLevel.class)
public class ServerWorldMixin {	
	@Inject(at = @At("HEAD"), method = "removePlayerImmediately")
	public void removePlayer(ServerPlayer player,CallbackInfo info) 
	{
		System.out.println("disconnect clean player undo history!!!");
		if (player != null) {
			System.out.println("removing undo from player: "+player.getName().getString());
			//WandServerSide.player_undo.remove(player.getUuidAsString());
		}
	}

}