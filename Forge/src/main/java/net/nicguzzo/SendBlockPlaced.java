package net.nicguzzo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.nicguzzo.common.WandItem;

import java.util.function.Supplier;

public class SendBlockPlaced {
    private final BlockPos pos;    
	private final boolean destroy;
    
    public SendBlockPlaced(PacketBuffer buffer) {        
        pos=buffer.readBlockPos();        
        destroy=buffer.readBoolean();
    }

    public SendBlockPlaced(BlockPos p,boolean d) {
        pos=p;
        destroy=d;
    }

    public void toBytes(PacketBuffer buf) {        
        buf.writeBlockPos(pos);        
        buf.writeBoolean(destroy);
    }

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        Context c=ctx.get();        
        ctx.get().enqueueWork(() -> {
            PlayerEntity player=c.getSender();
            if(WandItem.last_state!=null)
				WandsMod.compat.playBlockSound(player,WandItem.last_state,pos,destroy);				
        });
        ctx.get().setPacketHandled(true);
    }
}
