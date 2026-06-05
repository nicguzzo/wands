//? if neoforge {

/*package net.nicguzzo.wands.loaders.neoforge;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import java.nio.file.Path;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.WandsCommon;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.screens.MagicBagScreen;
import net.nicguzzo.wands.client.screens.PaletteScreen;
import net.nicguzzo.wands.client.screens.WandToolScreen;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.menues.MagicBagMenu;
import net.nicguzzo.wands.menues.PaletteMenu;
import net.nicguzzo.wands.menues.WandToolsMenu;

import java.util.function.Supplier;

@Mod("wands")
public class NeoforgeEntrypoint {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "wands");
    public static final Supplier<MenuType<WandToolsMenu>> WAND_TOOLS_MENU_TYPE = MENUS.register("wand_tools_menu", () -> IMenuTypeExtension.create(WandToolsMenu::new));
    public static final Supplier<MenuType<MagicBagMenu>> MAGIC_BAG_MENU_TYPE = MENUS.register("magic_bag_menu", () -> IMenuTypeExtension.create(MagicBagMenu::new));
    public static final Supplier<MenuType<PaletteMenu>> PALETTE_MENU_TYPE = MENUS.register("palette_menu", () -> IMenuTypeExtension.create(PaletteMenu::new));
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, WandsMod.MOD_ID);

//?if >= 1.21.11 {
    /^public static final Supplier<AttachmentType<CompoundTag>> PLAYER_DATA = ATTACHMENT_TYPES.register(
    "player_data", () -> AttachmentType.builder(holder -> new CompoundTag()).serialize(CompoundTag.CODEC.fieldOf("value")).build());
^///?}else{
    public static final Supplier<AttachmentType<CompoundTag>> PLAYER_DATA = ATTACHMENT_TYPES.register(
    "player_data", () -> AttachmentType.builder(() -> new CompoundTag()).serialize(CompoundTag.CODEC).build());
//?}

    public NeoforgeEntrypoint(IEventBus modBus) {
        Path configDir = FMLPaths.CONFIGDIR.get();
        WandsConfig.configDir=configDir.toString();
        NeoForge.EVENT_BUS.register(this);
        MENUS.register(modBus);
        modBus.addListener(this::registerScreens);
        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::onCommonSetup);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        WandsCommon.onServerStarted(event.getServer());
    }

    public void onCommonSetup(FMLCommonSetupEvent event) {
        WandsMod.has_opac = ModList.get().isLoaded("openpartiesandclaims");
        WandsMod.log("Has opac!! " + WandsMod.has_opac, true);

        WandsMod.has_ftbchunks = ModList.get().isLoaded("ftbchunks");
        WandsMod.log("Has ftbchunks!! " + WandsMod.has_ftbchunks, true);

        WandsMod.has_flan = ModList.get().isLoaded("flan");
        WandsMod.log("Has flan!! " + WandsMod.has_flan, true);
    }

    public void registerScreens(RegisterMenuScreensEvent event) {
        event.register(WAND_TOOLS_MENU_TYPE.get(), WandToolScreen::new);
        event.register(MAGIC_BAG_MENU_TYPE.get(), MagicBagScreen::new);
        event.register(PALETTE_MENU_TYPE.get(), PaletteScreen::new);
    }
    public void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            WandsModClient.initialize();
        });
    }

    public static CompoundTag getPlayerData(Player player){
        return player.getData(PLAYER_DATA);
    }
}

*///?}
