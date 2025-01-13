package net.nicguzzo.wands.forge;

import me.shedaniel.architectury.platform.forge.EventBuses;
import me.shedaniel.architectury.utils.Env;
import me.shedaniel.architectury.utils.EnvExecutor;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.WandsModClient;
import net.minecraftforge.fml.ModList;

import javax.swing.text.html.parser.Entity;

@Mod(WandsMod.MOD_ID)

public class WandsModForge {



    public WandsModForge() {
        WandsMod.is_forge=true;
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(WandsMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        WandsMod.has_opac=ModList.get().isLoaded("openpartiesandclaims");
        WandsMod.log("Has opac!! "+WandsMod.has_goml,true);

        WandsMod.has_ftbchunks=ModList.get().isLoaded("ftbchunks");
        WandsMod.log("Has ftbchunks!! "+WandsMod.has_goml,true);

        WandsMod.has_flan=ModList.get().isLoaded("flan");
        WandsMod.log("Has flan!! "+WandsMod.has_goml,true);


        WandsMod.init();
        EnvExecutor.runInEnv(Env.CLIENT, () -> 
            ()-> {
                WandsModClient.initialize();
                //MinecraftForge.EVENT_BUS.register(new KeyBindingRegistry());
            }
        );
        PlayerDataCapabilityForge.register();
        //MinecraftForge.EVENT_BUS.register(this);
        //CapabilityManager.INSTANCE.register(PlayerDataCapability.IPlayerData.class, new PlayerDataCapability.Storage(), PlayerDataCapability.PlayerData::new);
    }
}
