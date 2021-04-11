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

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class SendPlace {
    private final BlockPos state_pos ;
    private final BlockPos pos1;
    private final BlockPos pos2;
    private final int palette_mode;
    private final int mode;
	private final int plane;
    //private static final Logger LOGGER = LogManager.getLogger();

    public SendPlace(PacketBuffer buffer) {        
        state_pos=buffer.readBlockPos();
        pos1=buffer.readBlockPos();
        pos2=buffer.readBlockPos();
        palette_mode=buffer.readInt();
        mode=buffer.readInt();
        plane=buffer.readInt();

    }

    public SendPlace(BlockPos s,BlockPos p1, BlockPos p2,int pm,int m,int p) {
        state_pos=s;
        pos1=p1;
        pos2=p2;
        palette_mode=pm;
        mode=m;
        plane=p;
    }

    public void toBytes(PacketBuffer buf) {        
        buf.writeBlockPos(state_pos);
        buf.writeBlockPos(pos1);
        buf.writeBlockPos(pos2);
        buf.writeInt(palette_mode);
        buf.writeInt(mode);
        buf.writeInt(plane);
    }

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        Context c=ctx.get();        
        ctx.get().enqueueWork(() -> {
            PlayerEntity player=c.getSender();

            ItemStack item=player.getItemInHand(Hand.MAIN_HAND);
            if (item.getItem() instanceof WandItemForge){
                if( World.isInWorldBounds(state_pos) &&
                    World.isInWorldBounds(pos1) &&
                    World.isInWorldBounds(pos2) )
                {                        
                    WandServerSide srv = new WandServerSide(player.level,player, state_pos, pos1, pos2, palette_mode,
                            player.isCreative(), player.experienceProgress, item, mode, plane);
                    srv.placeBlock();
                    srv = null;
                    //WandServerSide.placeBlock(player,state_pos,pos1,pos2,palette_mode,player.isCreative(),player.experienceProgress,item);
                }        
            }
            
        });
        ctx.get().setPacketHandled(true);
    }
}
