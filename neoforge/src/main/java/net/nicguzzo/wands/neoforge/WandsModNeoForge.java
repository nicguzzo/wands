package net.nicguzzo.wands.neoforge;


import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.screens.MagicBagScreen;
import net.nicguzzo.wands.client.screens.PaletteScreen;
import net.nicguzzo.wands.client.screens.WandScreen;

import java.util.function.Supplier;


class ModMenuTypes {
    public static void clientRegister(IEventBus eventBus) {
        eventBus.addListener(ModMenuTypes::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(WandsMod.PALETTE_CONTAINER.get(), PaletteScreen::new);
        event.register(WandsMod.WAND_CONTAINER.get(), WandScreen::new);
        event.register(WandsMod.MAGIC_WAND_CONTANIER.get(), MagicBagScreen::new);
    }
}

@Mod(WandsMod.MOD_ID)

public class WandsModNeoForge {

    // Create the DeferredRegister for attachment types
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, WandsMod.MOD_ID);

    public static final Supplier<AttachmentType<CompoundTag>> PLAYER_DATA = ATTACHMENT_TYPES.register("player_data", () -> AttachmentType.builder(CompoundTag::new).serialize(CompoundTag.CODEC).build());

    public WandsModNeoForge(IEventBus modEventBus) {
        WandsMod.is_neoforge = true;

        WandsMod.has_opac = ModList.get().isLoaded("openpartiesandclaims");
        WandsMod.log("Has opac!! " + WandsMod.has_goml, true);

        WandsMod.has_ftbchunks = ModList.get().isLoaded("ftbchunks");
        WandsMod.log("Has ftbchunks!! " + WandsMod.has_goml, true);

        WandsMod.has_flan = ModList.get().isLoaded("flan");
        WandsMod.log("Has flan!! " + WandsMod.has_goml, true);

        WandsMod.init();
        NeoForge.EVENT_BUS.register(this);
        ATTACHMENT_TYPES.register(modEventBus);

        ModMenuTypes.clientRegister(modEventBus);
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            WandsModClient.initialize();
            //MinecraftForge.EVENT_BUS.register(new KeyBindingRegistry());
        });
        //MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerAboutToStartEvent(ServerAboutToStartEvent event) {

    }

}
