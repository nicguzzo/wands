package net.nicguzzo.wands.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Game;
import net.nicguzzo.wands.WandsMod;

@Mixin(Game.class)
public abstract class GameMixin {

	@Inject(at = @At("HEAD"), method = "onStartGameSession()V")
	private void onStartGameSession(CallbackInfo info) {
		WandsMod.LOGGER.info("onStartGameSession!");
		//WandsMod.LOGGER.info("request config: ");
		//PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
		//ClientPlayNetworking.send(WandsMod.WANDCONF_PACKET_ID, passedData);
	}
	@Inject(at = @At("HEAD"), method = "onLeaveGameSession()V")
	public void onLeaveGameSession(CallbackInfo info) {
		WandsMod.LOGGER.info("onLeaveGameSession!");		
	}

}
