//? if fabric {
package net.nicguzzo.wands.loaders.fabric;

import com.mojang.logging.LogUtils;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

//?if >=26.1{
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
//?}else{
/*import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
*///?}
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.ModInitializer;
import java.nio.file.Path;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.compat.RcId;
import net.nicguzzo.wands.items.MagicBagItem;
import net.nicguzzo.wands.items.PaletteItem;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.menues.*;
import net.nicguzzo.wands.networking.Networking;
import org.slf4j.Logger;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.WandsCommon;
import net.nicguzzo.wands.config.WandsConfig;

public class FabricEntrypoint implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    //?if >1.21{
    public static final ExtendedMenuType<WandToolsMenu, WandToolsMenuData> WAND_TOOLS_MENU_TYPE =
        new ExtendedMenuType<WandToolsMenu, WandToolsMenuData>(WandToolsMenu::new, WandToolsMenuData.STREAM_CODEC);

    public static final ExtendedMenuType<MagicBagMenu, MagicBagMenuData> MAGIC_BAG_MENU_TYPE =
        new ExtendedMenuType<MagicBagMenu, MagicBagMenuData>(MagicBagMenu::new, MagicBagMenuData.STREAM_CODEC);

    public static final ExtendedMenuType<PaletteMenu, PaletteMenuData> PALETTE_MENU_TYPE =
        new ExtendedMenuType<PaletteMenu,PaletteMenuData>(PaletteMenu::new, PaletteMenuData.STREAM_CODEC);

    //?}else{
    /*public static final ExtendedMenuType<WandToolsMenu> WAND_TOOLS_MENU_TYPE =
        new ExtendedMenuType<>(WandToolsMenu::new);
    public static final ExtendedMenuType<MagicBagMenu> MAGIC_BAG_MENU_TYPE =
        new ExtendedMenuType<>(MagicBagMenu::new);
    public static final ExtendedMenuType<PaletteMenu> PALETTE_MENU_TYPE =
        new ExtendedMenuType<>(PaletteMenu::new);
    *///?}

    public static final ResourceKey<CreativeModeTab> CUSTOM_ITEM_GROUP_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), RcId.fromNamespaceAndPath(WandsMod.MOD_ID, "wands_tab").id());
    public static CreativeModeTab CUSTOM_ITEM_GROUP;

    public static void initialize() {
    }

    @Override
    public void onInitialize() {

        WandsMod.is_fabric=true;
        Path configDir = FabricLoader.getInstance().getConfigDir();
        WandsConfig.configDir=configDir.toString();

        WandsMod.has_opac=FabricLoader.getInstance().getModContainer("openpartiesandclaims").isPresent();
        WandsMod.log("Has opac!! "+WandsMod.has_opac,true);

        WandsMod.has_ftbchunks=FabricLoader.getInstance().getModContainer("ftbchunks").isPresent();
        WandsMod.log("Has ftbchunks!! "+WandsMod.has_ftbchunks,true);

        WandsMod.has_flan=FabricLoader.getInstance().getModContainer("flan").isPresent();
        WandsMod.log("Has flan!! "+WandsMod.has_flan,true);

        WandsMod.has_goml=FabricLoader.getInstance().getModContainer("goml").isPresent();
        WandsMod.log("Has goml!! "+WandsMod.has_goml,true);

        Registry.register(BuiltInRegistries.MENU, RcId.fromNamespaceAndPath("wands", "wand_tools_menu").id(), WAND_TOOLS_MENU_TYPE);
        Registry.register(BuiltInRegistries.MENU, RcId.fromNamespaceAndPath("wands", "magic_bag_menu").id(), MAGIC_BAG_MENU_TYPE);
        Registry.register(BuiltInRegistries.MENU, RcId.fromNamespaceAndPath("wands", "palette_menu").id(), PALETTE_MENU_TYPE);


        ServerLifecycleEvents.SERVER_STARTED.register(WandsCommon::onServerStarted);
        WandsMod.init();

        Item WOODEN_WAND_ITEM = Registry.register(BuiltInRegistries.ITEM, WandsMod.wooden_wand_id.id(), WandItem.wooden_Wand());
        Item STONE_WAND_ITEM = Registry.register(BuiltInRegistries.ITEM, WandsMod.stone_wand_id.id(), WandItem.stone_Wand());
        Item COPPER_WAND_ITEM = Registry.register(BuiltInRegistries.ITEM, WandsMod.copper_wand_id.id(), WandItem.copper_Wand());
        Item GOLD_WAND_ITEM = Registry.register(BuiltInRegistries.ITEM, WandsMod.gold_wand_id.id(), WandItem.gold_Wand());
        Item IRON_WAND_ITEM = Registry.register(BuiltInRegistries.ITEM, WandsMod.iron_wand_id.id(), WandItem.iron_Wand());
        Item DIAMOND_WAND_ITEM = Registry.register(BuiltInRegistries.ITEM, WandsMod.diamond_wand_id.id(), WandItem.diamond_Wand());
        Item NETHERITE_WAND_ITEM = Registry.register(BuiltInRegistries.ITEM, WandsMod.netherite_wand_id.id(), WandItem.netherite_Wand());
        Item CREATIVE_WAND_ITEM = Registry.register(BuiltInRegistries.ITEM, WandsMod.creative_wand_id.id(), WandItem.creative_Wand());
        Item MAGIC_BAG_TIER1_ITEM = Registry.register(BuiltInRegistries.ITEM, WandsMod.magic_bag_1_id.id(), MagicBagItem.create_tier_1());
        Item MAGIC_BAG_TIER2_ITEM = Registry.register(BuiltInRegistries.ITEM, WandsMod.magic_bag_2_id.id(), MagicBagItem.create_tier_2());
        Item MAGIC_BAG_TIER3_ITEM = Registry.register(BuiltInRegistries.ITEM, WandsMod.magic_bag_3_id.id(), MagicBagItem.create_tier_3());
        Item PALETTE_ITEM = Registry.register(BuiltInRegistries.ITEM, WandsMod.palette_id.id(), PaletteItem.create());

        //?if >=26.1{
        CUSTOM_ITEM_GROUP=FabricCreativeModeTab.builder()
        .icon(() -> new ItemStack(DIAMOND_WAND_ITEM))
		.title(Compat.translatable("itemGroup.wands.wands_tab"))
        .displayItems((params, output) -> {
            output.accept(WOODEN_WAND_ITEM);
            output.accept(STONE_WAND_ITEM);
            output.accept(COPPER_WAND_ITEM);
            output.accept(GOLD_WAND_ITEM);
            output.accept(IRON_WAND_ITEM);
            output.accept(DIAMOND_WAND_ITEM);
            output.accept(NETHERITE_WAND_ITEM);
            output.accept(CREATIVE_WAND_ITEM);
            output.accept(MAGIC_BAG_TIER1_ITEM);
            output.accept(MAGIC_BAG_TIER2_ITEM);
            output.accept(MAGIC_BAG_TIER3_ITEM);
            output.accept(PALETTE_ITEM);
        })
		.build();
        //?}else{
        /*CUSTOM_ITEM_GROUP=FabricItemGroup.builder()

		.icon(() -> new ItemStack(DIAMOND_WAND_ITEM))
		.title(Compat.translatable("itemGroup.wands.wands_tab"))
		.build();

        ItemGroupEvents.modifyEntriesEvent(CUSTOM_ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.accept(WOODEN_WAND_ITEM);
            itemGroup.accept(STONE_WAND_ITEM);
            itemGroup.accept(COPPER_WAND_ITEM);
            itemGroup.accept(GOLD_WAND_ITEM);
            itemGroup.accept(IRON_WAND_ITEM);
            itemGroup.accept(DIAMOND_WAND_ITEM);
            itemGroup.accept(NETHERITE_WAND_ITEM);
            itemGroup.accept(CREATIVE_WAND_ITEM);
            itemGroup.accept(MAGIC_BAG_TIER1_ITEM);
            itemGroup.accept(MAGIC_BAG_TIER2_ITEM);
            itemGroup.accept(MAGIC_BAG_TIER3_ITEM);
            itemGroup.accept(PALETTE_ITEM);
        });
        *///?}
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CUSTOM_ITEM_GROUP_KEY, CUSTOM_ITEM_GROUP);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            WandsMod.onPlayerJoin(handler.player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, sender) -> {
            WandsMod.onPlayerQuit(handler.player);
        });
    }
}
//?}
