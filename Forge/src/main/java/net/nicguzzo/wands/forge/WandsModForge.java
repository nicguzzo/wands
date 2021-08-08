package net.nicguzzo.wands.forge;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import dev.architectury.platform.forge.EventBuses;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.WandsModClient;

@Mod(WandsMod.MOD_ID)

public class WandsModForge {
    //public static final Logger LOGGER = LogManager.getLogger();
    
    public WandsModForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(WandsMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        WandsMod.init();
        
        EnvExecutor.runInEnv(Env.CLIENT, () -> 
            ()-> {
                WandsModClient.initialize();
                WandsModClient.is_forge=true;
                MinecraftForge.EVENT_BUS.register(new WandsForgeEventHandler());
            }
        );
        //MinecraftForge.EVENT_BUS.register(this);
    }
}
