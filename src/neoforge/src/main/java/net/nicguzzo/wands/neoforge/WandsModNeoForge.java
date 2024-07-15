package net.nicguzzo.wands.neoforge;

#if MC=="1165"
import me.shedaniel.architectury.platform.forge.EventBuses;
import me.shedaniel.architectury.utils.Env;
import me.shedaniel.architectury.utils.EnvExecutor;
#else
import dev.architectury.platform.forge.EventBuses;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
#endif

//import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.WandsModClient;

@Mod(WandsMod.MOD_ID)

public class WandsModNeoForge {

    public WandsModNeoForge() {
        WandsMod.is_forge=true;
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(WandsMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        WandsMod.init();
        EnvExecutor.runInEnv(Env.CLIENT, () -> 
            ()-> {
                WandsModClient.initialize();
                //MinecraftForge.EVENT_BUS.register(new KeyBindingRegistry());
            }
        );
        //MinecraftForge.EVENT_BUS.register(this);
    }
    /*@Mod.EventBusSubscriber(modid = WandsMod.MOD_ID, value = Dist.CLIENT)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            WandsModClient.keys.forEach((km,v) -> event.register((KeyMapping)km));
        }
    }*/
}
