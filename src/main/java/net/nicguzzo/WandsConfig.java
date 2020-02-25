package net.nicguzzo;

import net.minecraft.util.PacketByteBuf;


public class WandsConfig {
	public int blocks_per_xp;
	
	public WandsConfig(int bpxp) {
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
		buf.writeInt(config.blocks_per_xp);		
		return buf;
	}

	public static WandsConfig readConfig(PacketByteBuf buf) {
		int bpxp = buf.readInt();		
		return new WandsConfig(bpxp);
	}

	public boolean equals(WandsConfig config) {
		return (
				config.blocks_per_xp==blocks_per_xp
		);
	}
}
