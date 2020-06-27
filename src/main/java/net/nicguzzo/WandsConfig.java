package net.nicguzzo;

import net.minecraft.network.PacketByteBuf;

public class WandsConfig {
	public float blocks_per_xp=0.0f;
	
	public WandsConfig(float bpxp) {
		if(bpxp>=0.0f)
			this.blocks_per_xp = bpxp;		
	}
	public WandsConfig() {
		this(0);
	}
	public String toString() {
		return "blocks_per_xp: "+blocks_per_xp;
	}

	public PacketByteBuf writeConfig(PacketByteBuf buf) {
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
	}
}
