package net.nicguzzo.wands.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.nicguzzo.wands.WandsMod;
import net.fabricmc.api.ModInitializer;
import java.util.Optional;

public class WandsModFabric implements ModInitializer {
    @Override
    public void onInitialize() {


        WandsMod.has_opac=FabricLoader.getInstance().getModContainer("openpartiesandclaims").isPresent();
        WandsMod.log("Has opac!! "+WandsMod.has_opac,true);

        WandsMod.has_ftbchunks=FabricLoader.getInstance().getModContainer("ftbchunks").isPresent();
        WandsMod.log("Has ftbchunks!! "+WandsMod.has_ftbchunks,true);

        WandsMod.has_flan=FabricLoader.getInstance().getModContainer("flan").isPresent();
        WandsMod.log("Has flan!! "+WandsMod.has_flan,true);

        WandsMod.has_goml=FabricLoader.getInstance().getModContainer("goml").isPresent();
        WandsMod.log("Has goml!! "+WandsMod.has_goml,true);

        WandsMod.init();
    }
}
