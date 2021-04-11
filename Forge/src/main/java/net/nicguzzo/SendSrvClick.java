package net.nicguzzo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
//import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.nicguzzo.common.WandItem;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import java.util.function.Supplier;

public class SendSrvClick {
    private final BlockPos pos;
    private boolean air;
    //private static final Logger LOGGER = LogManager.getLogger();

    public SendSrvClick(PacketBuffer buffer) {        
        pos=buffer.readBlockPos();
        air=buffer.readBoolean();
    }

    public SendSrvClick(BlockPos p,boolean a) {
        pos=p;
        air=a;
    }

    public void toBytes(PacketBuffer buf) {        
        buf.writeBlockPos(pos);
        buf.writeBoolean(air);
    }

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        //Context c=ctx.get();        
        ctx.get().enqueueWork(() -> {
            
            PlayerEntity player= WandsMod.compat.get_player();
            System.out.println("got click from server player "+player.getName().getString());
            if(player !=null && player.level.isClientSide){
                System.out.println("got click from server");
                if(WandsMod.compat.is_player_holding_wand(player)){
                    WandItem wand=WandsMod.compat.get_player_wand(player);
                    wand.right_click_use_on_block(player , player.level, pos);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
