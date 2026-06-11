//? if neoforge {

/*package net.nicguzzo.wands.loaders.neoforge;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
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
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.screens.MagicBagScreen;
import net.nicguzzo.wands.client.screens.PaletteScreen;
import net.nicguzzo.wands.client.screens.WandToolScreen;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.items.PaletteItem;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.menues.MagicBagMenu;
import net.nicguzzo.wands.menues.PaletteMenu;
import net.nicguzzo.wands.menues.WandToolsMenu;

import java.util.function.Supplier;

@Mod(WandsMod.MOD_ID)
public class NeoforgeEntrypoint {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, WandsMod.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WandsMod.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WandsMod.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "wands");
    public static final Supplier<MenuType<WandToolsMenu>> WAND_TOOLS_MENU_TYPE = MENUS.register("wand_tools_menu", () -> IMenuTypeExtension.create(WandToolsMenu::new));
    public static final Supplier<MenuType<MagicBagMenu>> MAGIC_BAG_MENU_TYPE = MENUS.register("magic_bag_menu", () -> IMenuTypeExtension.create(MagicBagMenu::new));
    public static final Supplier<MenuType<PaletteMenu>> PALETTE_MENU_TYPE = MENUS.register("palette_menu", () -> IMenuTypeExtension.create(PaletteMenu::new));


     public static final Supplier<WandItem> WOODEN_WAND_ITEM = ITEMS.register(WandsMod.wooden_wand_str,
             WandItem::wooden_Wand);

    public static final Supplier<WandItem> STONE_WAND_ITEM = ITEMS.register(WandsMod.stone_wand_str,
            WandItem::stone_Wand);

    public static final Supplier<WandItem> COPPER_WAND_ITEM = ITEMS.register(WandsMod.copper_wand_str,
            WandItem::copper_Wand);

    public static final Supplier<WandItem> GOLD_WAND_ITEM = ITEMS.register(WandsMod.gold_wand_str,
            WandItem::gold_Wand);

    public static final Supplier<WandItem> IRON_WAND_ITEM = ITEMS.register(WandsMod.iron_wand_str,
            WandItem::iron_Wand);

    public static final Supplier<WandItem> DIAMOND_WAND_ITEM = ITEMS.register(WandsMod.diamond_wand_str,
            WandItem::diamond_Wand);

    public static final Supplier<WandItem> NETHERITE_WAND_ITEM = ITEMS.register(WandsMod.netherite_wand_str,
            WandItem::netherite_Wand);

    public static final Supplier<WandItem> CREATIVE_WAND_ITEM = ITEMS.register(WandsMod.creative_wand_str,
            WandItem::creative_Wand);

    public static final Supplier<MagicBagItem> MAGIC_BAG_TIER1_ITEM = ITEMS.register(WandsMod.magic_bag_1_str,
            MagicBagItem::create_tier_1);

    public static final Supplier<MagicBagItem> MAGIC_BAG_TIER2_ITEM = ITEMS.register(WandsMod.magic_bag_2_str,
            MagicBagItem::create_tier_2);

    public static final Supplier<MagicBagItem> MAGIC_BAG_TIER3_ITEM = ITEMS.register(WandsMod.magic_bag_3_str,
            MagicBagItem::create_tier_3);

    public static final Supplier<PaletteItem> PALETTE_ITEM = ITEMS.register(WandsMod.palette_str,
            PaletteItem::create
    );

    public static final Supplier<CreativeModeTab> WANDS_TAB = CREATIVE_MODE_TABS.register("wands_tab", () -> CreativeModeTab.builder()
    .title(Component.translatable("itemGroup.wands.wands_tab"))
    .icon(() -> new ItemStack(DIAMOND_WAND_ITEM.get()))
    .displayItems((params, output) -> {
        output.accept(WOODEN_WAND_ITEM.get());
        output.accept(STONE_WAND_ITEM.get());
        output.accept(COPPER_WAND_ITEM.get());
        output.accept(GOLD_WAND_ITEM.get());
        output.accept(IRON_WAND_ITEM.get());
        output.accept(DIAMOND_WAND_ITEM.get());
        output.accept(NETHERITE_WAND_ITEM.get());
        output.accept(CREATIVE_WAND_ITEM.get());
        output.accept(MAGIC_BAG_TIER1_ITEM.get());
        output.accept(MAGIC_BAG_TIER2_ITEM.get());
        output.accept(MAGIC_BAG_TIER3_ITEM.get());
        output.accept(PALETTE_ITEM.get());
    })
    .build()
    );

//?if >= 1.21.11 {
    public static final Supplier<AttachmentType<CompoundTag>> PLAYER_DATA = ATTACHMENT_TYPES.register(
    "player_data", () -> AttachmentType.builder(holder -> new CompoundTag()).serialize(CompoundTag.CODEC.fieldOf("value")).build());
//?}else{
    /^public static final Supplier<AttachmentType<CompoundTag>> PLAYER_DATA = ATTACHMENT_TYPES.register(
    "player_data", () -> AttachmentType.builder(() -> new CompoundTag()).serialize(CompoundTag.CODEC).build());
^///?}

    public NeoforgeEntrypoint(IEventBus modBus) {
        Path configDir = FMLPaths.CONFIGDIR.get();
        WandsConfig.configDir=configDir.toString();
        NeoForge.EVENT_BUS.register(this);
        ATTACHMENT_TYPES.register(modBus);
        MENUS.register(modBus);
        ITEMS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
        modBus.addListener(this::registerScreens);
        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::onCommonSetup);
        WandsMod.init();
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {

    }

    @SubscribeEvent
    public void onPlayerJoin(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer) {
            WandsMod.onPlayerJoin((net.minecraft.server.level.ServerPlayer) event.getEntity());
        }
    }

    @SubscribeEvent
    public void onPlayerQuit(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer) {
            WandsMod.onPlayerQuit((net.minecraft.server.level.ServerPlayer) event.getEntity());
        }
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
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new Object() {
            @net.neoforged.bus.api.SubscribeEvent
            public void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e) {
                WandsModClient.client_tick(net.minecraft.client.Minecraft.getInstance());
            }
            @net.neoforged.bus.api.SubscribeEvent
            public void onRenderHud(net.neoforged.neoforge.client.event.RenderGuiEvent.Post e) {
                WandsModClient.render_hud(e.getGuiGraphicsExtractor());
            }
        });
    }

    public static CompoundTag getPlayerData(Player player){
        return player.getData(PLAYER_DATA);
    }
}

*///?}
