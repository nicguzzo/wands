package net.nicguzzo;

import net.minecraft.network.PacketByteBuf;
import net.nicguzzo.common.WandsConfig;

public class WandsConfigFabric {
	
	/*public PacketByteBuf writeConfig(PacketByteBuf buf) {
		return writeConfig(buf, this);
	}

	public static PacketByteBuf writeConfig(PacketByteBuf buf, WandsConfig config) {
		buf.writeFloat(config.blocks_per_xp);		
		return buf;
	}

	public static WandsConfig readConfig(PacketByteBuf buf) {
		float bpxp = buf.readFloat();		
		return new WandsConfig(bpxp);
	}

	public boolean equals(WandsConfig config) {
		return (
				config.blocks_per_xp==blocks_per_xp
		);
	}*/
}
