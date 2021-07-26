package net.nicguzzo.wands.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.nicguzzo.wands.PlayerWandInfo;


@Mixin(ServerLevel.class)
public class ServerWorldMixin {	
	@Inject(at = @At("HEAD"), method = "addPlayer")
	public void addPlayer(ServerPlayer player,CallbackInfo info) 
	{
		System.out.println("player connected !!!");
		if (player != null) {
			System.out.println("adding wand info for player: "+player.getName().getString());			
//			PlayerWandInfo.add_player(player);
		}
	}

	@Inject(at = @At("HEAD"), method = "removePlayerImmediately")
	public void removePlayer(ServerPlayer player,CallbackInfo info) 
	{
		System.out.println("player disconnected !!!");
		if (player != null) {
			System.out.println("removing undo from player: "+player.getName().getString());			
			PlayerWandInfo.remove_player(player);
		}
	}
}