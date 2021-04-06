package net.nicguzzo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.nicguzzo.common.WandServerSide;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class SendUndo {
    private final int n;
    private final int u;//0 undo 1 redo

    public SendUndo(PacketBuffer buffer) {        
        n=buffer.readInt();
        u=buffer.readInt();
    }

    public SendUndo(int i,int j) {
        n=i;
        u=j;
    }

    public void toBytes(PacketBuffer buf) {        
        buf.writeInt(n);
        buf.writeInt(u);
    }

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        Context c=ctx.get();        
        ctx.get().enqueueWork(() -> {
            PlayerEntity player=c.getSender();

            ItemStack item=player.getItemInHand(Hand.MAIN_HAND);
            if (item.getItem() instanceof WandItemForge){
                if(u==0){
                    WandServerSide.undo(player,n);
                }else{
                    WandServerSide.redo(player,n);
                }
            }
            
        });
        ctx.get().setPacketHandled(true);
    }
}
