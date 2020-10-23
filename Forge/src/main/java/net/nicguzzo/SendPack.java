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

public class SendPack {
    private final BlockPos state_pos ;
    private final BlockPos pos1;
    private final BlockPos pos2;
    private final int palette_mode;
    private static final Logger LOGGER = LogManager.getLogger();

    public SendPack(PacketBuffer buffer) {        
        state_pos=buffer.readBlockPos();
        pos1=buffer.readBlockPos();
        pos2=buffer.readBlockPos();
        palette_mode=buffer.readInt();
    }

    public SendPack(BlockPos s,BlockPos p1, BlockPos p2,int m) {
        state_pos=s;
        pos1=p1;
        pos2=p2;
        palette_mode=m;
    }

    public void toBytes(PacketBuffer buf) {        
        buf.writeBlockPos(state_pos);
        buf.writeBlockPos(pos1);
        buf.writeBlockPos(pos2);
        buf.writeInt(palette_mode);
    }

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        Context c=ctx.get();
        ctx.get().enqueueWork(() -> {
            /*LOGGER.info("state_pos: "+this.state_pos);
            LOGGER.info("pos1: "+this.pos1);
            LOGGER.info("pos2: "+this.pos2);
            LOGGER.info("mode: "+this.mode);        */
            PlayerEntity player=c.getSender();

            //Item item=player.getActiveItemStack().getItem();
            ItemStack item=player.getHeldItem(Hand.MAIN_HAND);
            //LOGGER.info("item: "+ item);        
            if (item.getItem() instanceof WandItemForge){
                //LOGGER.info("wand!");        
                //WandItemForge wand=(WandItemForge)item;
                /*if(wand==NETHERITE_WAND_ITEM){
                    is_netherite_wand=true;
                }*/
                //LOGGER.info("isCreativeMode! "+player.abilities.isCreativeMode);        
                if( World.isValid(state_pos) &&
                    World.isValid(pos1) &&
                    World.isValid(pos2) )
                {                        
                    WandServerSide.placeBlock(player,state_pos,pos1,pos2,palette_mode,player.abilities.isCreativeMode,player.experience,item);
                }        
            }
            
        });
        ctx.get().setPacketHandled(true);
    }
}
