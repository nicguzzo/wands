//? if forge {
/*package net.nicguzzo.wands.loaders.forge;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nicguzzo.wands.client.screens.MagicBagScreen;
import net.nicguzzo.wands.client.screens.PaletteScreen;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.items.PaletteItem;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.menues.MagicBagMenu;
import net.nicguzzo.wands.menues.PaletteMenu;
import net.nicguzzo.wands.menues.WandToolsMenu;

import java.nio.file.Path;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.screens.WandToolScreen;

@Mod("wands")
public class ForgeEntrypoint {


    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,WandsMod.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WandsMod.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "wands");

    public static final RegistryObject<MenuType<WandToolsMenu>> WAND_TOOLS_MENU_TYPE = MENUS.register("wand_tools_menu",
            () -> IForgeMenuType.create(WandToolsMenu::new));

    public static final RegistryObject<MenuType<MagicBagMenu>> MAGIC_BAG_MENU_TYPE = MENUS.register("magic_bag_menu",
            () -> IForgeMenuType.create(MagicBagMenu::new));

    public static final RegistryObject<MenuType<PaletteMenu>> PALETTE_MENU_TYPE = MENUS.register("palette_menu",
            () -> IForgeMenuType.create(PaletteMenu::new));

    public static final RegistryObject<Item> WOODEN_WAND_ITEM = ITEMS.register(WandsMod.wooden_wand_str,
            WandItem::wooden_Wand);

    public static final RegistryObject<Item> STONE_WAND_ITEM = ITEMS.register(WandsMod.stone_wand_str,
            WandItem::stone_Wand);

    public static final RegistryObject<Item> COPPER_WAND_ITEM = ITEMS.register(WandsMod.copper_wand_str,
            WandItem::copper_Wand);

    public static final RegistryObject<Item> GOLD_WAND_ITEM = ITEMS.register(WandsMod.gold_wand_str,
            WandItem::gold_Wand);

    public static final RegistryObject<Item> IRON_WAND_ITEM = ITEMS.register(WandsMod.iron_wand_str,
            WandItem::iron_Wand);

    public static final RegistryObject<Item> DIAMOND_WAND_ITEM = ITEMS.register(WandsMod.diamond_wand_str,
            WandItem::diamond_Wand);

    public static final RegistryObject<Item> NETHERITE_WAND_ITEM = ITEMS.register(WandsMod.netherite_wand_str,
            WandItem::netherite_Wand);

    public static final RegistryObject<Item> CREATIVE_WAND_ITEM = ITEMS.register(WandsMod.creative_wand_str,
            WandItem::creative_Wand);


    public static final RegistryObject<Item> MAGIC_BAG_TIER1_ITEM = ITEMS.register(WandsMod.magic_bag_1_str,
            MagicBagItem::create_tier_1);

    public static final RegistryObject<Item> MAGIC_BAG_TIER2_ITEM = ITEMS.register(WandsMod.magic_bag_2_str,
            MagicBagItem::create_tier_2);

    public static final RegistryObject<Item> MAGIC_BAG_TIER3_ITEM = ITEMS.register(WandsMod.magic_bag_3_str,
            MagicBagItem::create_tier_3);

    public static final RegistryObject<Item> PALETTE_ITEM = ITEMS.register(WandsMod.palette_str,
            PaletteItem::create);



    public static final RegistryObject<CreativeModeTab> WANDS_TAB = CREATIVE_MODE_TABS.register("wands_tab", () -> CreativeModeTab.builder()
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
    public ForgeEntrypoint() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        WandsMod.is_forge=true;
        Path configDir = FMLPaths.CONFIGDIR.get();
        WandsConfig.configDir=configDir.toString();
        MENUS.register(modBus);
        ITEMS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
        MinecraftForge.EVENT_BUS.register(this);
        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::onCommonSetup);
        WandsMod.init();
    }
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        //WandsCommon.onServerStarted(event.getServer());
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof ServerPlayer) {
            WandsMod.onPlayerJoin((ServerPlayer) event.getEntity());
        }
    }

    @SubscribeEvent
    public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.getEntity() instanceof ServerPlayer) {
            WandsMod.onPlayerQuit((ServerPlayer) event.getEntity());
        }
    }

    public void onCommonSetup(FMLCommonSetupEvent event) {
        //? if < 1.20.5 {
        
        /^event.enqueueWork(() -> {
            net.nicguzzo.wands.networking.Networking.init();
        });
        
        ^///?}
        WandsMod.has_opac = ModList.get().isLoaded("openpartiesandclaims");
        WandsMod.log("Has opac!! " + WandsMod.has_opac, true);

        WandsMod.has_ftbchunks = ModList.get().isLoaded("ftbchunks");
        WandsMod.log("Has ftbchunks!! " + WandsMod.has_ftbchunks, true);

        WandsMod.has_flan = ModList.get().isLoaded("flan");
        WandsMod.log("Has flan!! " + WandsMod.has_flan, true);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(WAND_TOOLS_MENU_TYPE.get(), WandToolScreen::new);
            MenuScreens.register(MAGIC_BAG_MENU_TYPE.get(), MagicBagScreen::new);
            MenuScreens.register(PALETTE_MENU_TYPE.get(), PaletteScreen::new);
            WandsModClient.initialize();
        });
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new Object() {
            @net.minecraftforge.eventbus.api.SubscribeEvent
            public void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent e) {
                if (e.phase == net.minecraftforge.event.TickEvent.Phase.END) {
                    WandsModClient.client_tick(net.minecraft.client.Minecraft.getInstance());
                }
            }
            @net.minecraftforge.eventbus.api.SubscribeEvent
            public void onRenderHud(net.minecraftforge.client.event.RenderGuiOverlayEvent.Post e) {
                if (e.getOverlay() == net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.HOTBAR.type()) {
                    WandsModClient.render_hud(e.getGuiGraphicsExtractor());
                }
            }
        });
    }
     public static CompoundTag getPlayerData(Player player){
        CompoundTag forgeData = player.getPersistentData();
        String key = "wands_player_data";
        if (!forgeData.contains(key)) {
            forgeData.put(key, new CompoundTag());
        }
        return forgeData.getCompound(key);
    }
}
*///?}
