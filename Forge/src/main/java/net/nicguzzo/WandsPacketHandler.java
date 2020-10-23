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
                SendPack.class,
                SendPack::toBytes,
                SendPack::new,
                SendPack::handler
        );
    }
    public static int nextID() {
        return ID++;
    }
}