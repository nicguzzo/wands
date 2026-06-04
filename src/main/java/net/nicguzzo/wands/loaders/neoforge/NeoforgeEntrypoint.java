//? if neoforge {

/*package net.nicguzzo.deepslateinstamine.loaders.neoforge;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import java.nio.file.Path;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.player.Player;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.WandsCommon;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.client.PaletteClientTooltip;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.screens.MagicBagScreen;
import net.nicguzzo.wands.client.screens.PaletteScreen;
import net.nicguzzo.wands.client.screens.WandToolScreen;
import net.nicguzzo.wands.items.PaletteTooltip;
import net.nicguzzo.wands.wand.Wand;
import java.util.function.Supplier;

@Mod("wands")
public class NeoforgeEntrypoint {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, WandsMod.MOD_ID);

//?if >= 1.21.11 {
    public static final Supplier<AttachmentType<CompoundTag>> PLAYER_DATA = ATTACHMENT_TYPES.register(
    "player_data", () -> AttachmentType.builder(holder -> new CompoundTag()).serialize(CompoundTag.CODEC.fieldOf("value")).build());
//?}else{

    /^public static final Supplier<AttachmentType<CompoundTag>> PLAYER_DATA = ATTACHMENT_TYPES.register(
    "player_data", () -> AttachmentType.builder(() -> new CompoundTag()).serialize(CompoundTag.CODEC).build());

^///?}


    public NeoforgeEntrypoint() {
        NeoForge.EVENT_BUS.register(this);
    }
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        Path configDir = FMLPaths.CONFIGDIR.get();
        WandsConfig.configDir=configDir.toString();
        WandsCommon.onServerStarted(event.getServer());
    }
    @SubscribeEvent
    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(WandsMod.PALETTE_MENU_TYPE, PaletteScreen::new);
        event.register(WandsMod.WAND_MENU_TYPE, WandToolScreen::new);
        event.register(WandsMod.MAGICBAG_MENU_TYPE, MagicBagScreen::new);
    }

    @SubscribeEvent
    private void onCommonSetup(FMLCommonSetupEvent event) {
        WandsMod.has_opac = ModList.get().isLoaded("openpartiesandclaims");
        WandsMod.log("Has opac!! " + WandsMod.has_opac, true);

        WandsMod.has_ftbchunks = ModList.get().isLoaded("ftbchunks");
        WandsMod.log("Has ftbchunks!! " + WandsMod.has_ftbchunks, true);

        WandsMod.has_flan = ModList.get().isLoaded("flan");
        WandsMod.log("Has flan!! " + WandsMod.has_flan, true);
    }
    @SubscribeEvent
    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            WandsModClient.initialize();
        });
    }

    public static CompoundTag getPlayerData(Player player){
        return player.getData(PLAYER_DATA);
    }
}

*///?}
