package net.nicguzzo;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.network.NetworkRegistry;

public class WandsPacketHandler
{
    private static final String PROTOCOL_VERSION = "1";
    private static int ID = 0;
    public static SimpleChannel INSTANCE;
    public static void registerMessage(){
        INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("wands", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
        );
        INSTANCE.registerMessage(
                nextID(),
                SendPlace.class,
                SendPlace::toBytes,
                SendPlace::new,
                SendPlace::handler
        );
        INSTANCE.registerMessage(
                nextID(),
                SendUndo.class,
                SendUndo::toBytes,
                SendUndo::new,
                SendUndo::handler
        );
        INSTANCE.registerMessage(
                nextID(),
                SendSrvClick.class,
                SendSrvClick::toBytes,
                SendSrvClick::new,
                SendSrvClick::handler
        );
        INSTANCE.registerMessage(
                nextID(),
                SendBlockPlaced.class,
                SendBlockPlaced::toBytes,
                SendBlockPlaced::new,
                SendBlockPlaced::handler
        );
    }
    public static int nextID() {
        return ID++;
    }
}