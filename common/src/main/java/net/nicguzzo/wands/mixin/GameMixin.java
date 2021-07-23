package net.nicguzzo.wands.mixin;

//import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Game;

@Mixin(Game.class)
public abstract class GameMixin {

	@Inject(at = @At("HEAD"), method = "onStartGameSession()V")
	private void onStartGameSession(CallbackInfo info) {
		System.out.println("onStartGameSession!");
		System.out.println("request config: ");
		//PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
		//ClientPlayNetworking.send(WandsMod.WANDCONF_PACKET_ID, passedData);
	}
	@Inject(at = @At("HEAD"), method = "onLeaveGameSession()V")
	public void onLeaveGameSession(CallbackInfo info) {
		System.out.println("onLeaveGameSession!");		
	}

}
