package net.nicguzzo.wands.forge;

import me.shedaniel.architectury.platform.forge.EventBuses;
import me.shedaniel.architectury.utils.Env;
import me.shedaniel.architectury.utils.EnvExecutor;

import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.WandsModClient;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(WandsMod.MOD_ID)
public class WandsModForge {
    public WandsModForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(WandsMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        WandsMod.init();        
        EnvExecutor.runInEnv(Env.CLIENT, () -> WandsModClient::initialize);
    }
}
