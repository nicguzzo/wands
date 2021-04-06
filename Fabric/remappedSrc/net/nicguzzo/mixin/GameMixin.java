package net.nicguzzo.mixin;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClientGame;
import net.minecraft.network.PacketByteBuf;
import net.nicguzzo.WandsMod;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.buffer.Unpooled;


@Mixin(MinecraftClientGame.class)
public abstract class GameMixin {

    @Inject(at = @At("HEAD"), method = "onStartGameSession()V")
	private void onStartGameSession(CallbackInfo info) {
        System.out.println("onStartGameSession!");
        System.out.println("request config: ");
		PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());                
		ClientSidePacketRegistry.INSTANCE.sendToServer(WandsMod.WANDCONF_PACKET_ID, passedData);
	}

}
