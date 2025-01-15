package net.nicguzzo.wands.client.screens;
#if USE_CLOTHCONFIG
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.math.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.config.WandsConfig;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.render.ClientRender;


public class WandConfigScreen {
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Compat.translatable("title.wands.config"));
        WandsConfig conf = WandsMod.config;
        builder.setSavingRunnable(() -> {
            WandsConfig.save_config();
        });
        ServerData srv = Minecraft.getInstance().getCurrentServer();

        ConfigCategory general = builder.getOrCreateCategory(Compat.translatable("category.wands.general"));
        ConfigCategory preview = builder.getOrCreateCategory(Compat.translatable("category.wands.preview"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        if (srv == null)
        {
    //blocks_per_xp
                general.addEntry(entryBuilder.startFloatField(Compat.translatable("option.wands.blocks_per_xp"), conf.blocks_per_xp)
                        .setDefaultValue(WandsConfig.def_blocks_per_xp)
                        .setTooltip(Compat.translatable("option.wands.blocks_per_xp_tt"))
                        .setSaveConsumer(newValue -> conf.blocks_per_xp = newValue).build());

    //stone_wand_limit
            general.addEntry(entryBuilder.startIntField(Compat.translatable("option.wands.stone_wand_limit"), conf.stone_wand_limit)
                    .setDefaultValue(WandsConfig.def_stone_wand_limit)
                    .setMin(0)
                    .setMax(WandsConfig.max_limit)
                    .setTooltip(Compat.translatable("option.wands.stone_wand_limit_tt").append("restart required"))
                    .setSaveConsumer(newValue -> conf.stone_wand_limit = newValue)
                    .build());
    //iron_wand_limit
            general.addEntry(entryBuilder.startIntField(Compat.translatable("option.wands.iron_wand_limit"), conf.iron_wand_limit)
                    .setDefaultValue(WandsConfig.def_iron_wand_limit)
                    .setMin(0)
                    .setMax(WandsConfig.max_limit)
                    .setTooltip(Compat.translatable("option.wands.iron_wand_limit_tt").append("restart required"))
                    .setSaveConsumer(newValue -> conf.iron_wand_limit = newValue)
                    .build());
    //diamond_wand_limit
            general.addEntry(entryBuilder.startIntField(Compat.translatable("option.wands.diamond_wand_limit"), conf.diamond_wand_limit)
                    .setDefaultValue(WandsConfig.def_diamond_wand_limit)
                    .setMin(0)
                    .setMax(WandsConfig.max_limit)
                    .setTooltip(Compat.translatable("option.wands.diamond_wand_limit_tt").append("restart required"))
                    .setSaveConsumer(newValue -> conf.diamond_wand_limit = newValue)
                    .build());
    //netherite_wand_limit
            general.addEntry(entryBuilder.startIntField(Compat.translatable("option.wands.netherite_wand_limit"), conf.netherite_wand_limit)
                    .setDefaultValue(WandsConfig.def_netherite_wand_limit)
                    .setMin(0)
                    .setMax(WandsConfig.max_limit)
                    .setTooltip(Compat.translatable("option.wands.netherite_wand_limit_tt").append("restart required"))
                    .setSaveConsumer(newValue -> conf.netherite_wand_limit = newValue)
                    .build());
    //stone_wand_durability
            general.addEntry(entryBuilder.startIntField(Compat.translatable("option.wands.stone_wand_durability"), conf.stone_wand_durability)
                    .setDefaultValue(WandsConfig.def_stone_wand_durability)
                    .setMin(0)
                    .setTooltip(Compat.translatable("option.wands.stone_wand_durability_tt").append("restart required"))
                    .setSaveConsumer(newValue -> conf.stone_wand_durability = newValue)
                    .build());
    //iron_wand_durability
            general.addEntry(entryBuilder.startIntField(Compat.translatable("option.wands.iron_wand_durability"), conf.iron_wand_durability)
                    .setDefaultValue(WandsConfig.def_iron_wand_durability)
                    .setMin(0)
                    .setTooltip(Compat.translatable("option.wands.iron_wand_durability_tt").append("restart required"))
                    .setSaveConsumer(newValue -> conf.iron_wand_durability = newValue)
                    .build());
    //diamond_wand_durability
            general.addEntry(entryBuilder.startIntField(Compat.translatable("option.wands.diamond_wand_durability"), conf.diamond_wand_durability)
                    .setDefaultValue(WandsConfig.def_diamond_wand_durability)
                    .setMin(0)
                    .setTooltip(Compat.translatable("option.wands.diamond_wand_durability_tt").append("restart required"))
                    .setSaveConsumer(newValue -> conf.diamond_wand_durability = newValue)
                    .build());
    //netherite_wand_durability
            general.addEntry(entryBuilder.startIntField(Compat.translatable("option.wands.netherite_wand_durability"), conf.netherite_wand_durability)
                    .setDefaultValue(WandsConfig.def_netherite_wand_durability)
                    .setMin(0)
                    .setTooltip(Compat.translatable("option.wands.netherite_wand_durability_tt").append("restart required"))
                    .setSaveConsumer(newValue -> conf.netherite_wand_durability = newValue)
                    .build());
    //destroy_in_survival_drop
            general.addEntry(entryBuilder.startBooleanToggle(Compat.translatable("option.wands.destroy_in_survival_drop"), conf.destroy_in_survival_drop)
                    .setDefaultValue(true)
                    .setTooltip(Compat.translatable("option.wands.destroy_in_survival_drop_tt"))
                    .setSaveConsumer(newValue -> conf.destroy_in_survival_drop = newValue)
                    .build());
    //survival_unenchanted_drops
            general.addEntry(entryBuilder.startBooleanToggle(Compat.translatable("option.wands.survival_unenchanted_drops"), conf.survival_unenchanted_drops)
                    .setDefaultValue(true)
                    .setTooltip(Compat.translatable("option.wands.survival_unenchanted_drops_tt"))
                    .setSaveConsumer(newValue -> conf.survival_unenchanted_drops = newValue)
                    .build());
    //allow_wand_to_break
            general.addEntry(entryBuilder.startBooleanToggle(Compat.translatable("option.wands.allow_wand_to_break"), conf.allow_wand_to_break)
                    .setDefaultValue(true)
                    .setTooltip(Compat.translatable("option.wands.allow_wand_to_break_tt"))
                    .setSaveConsumer(newValue -> conf.allow_wand_to_break = newValue)
                    .build());
    //allow_offhand_to_break
            general.addEntry(entryBuilder.startBooleanToggle(Compat.translatable("option.wands.allow_offhand_to_break"), conf.allow_offhand_to_break)
                    .setDefaultValue(true)
                    .setTooltip(Compat.translatable("option.wands.allow_offhand_to_break_tt"))
                    .setSaveConsumer(newValue -> conf.allow_offhand_to_break = newValue)
                    .build());
    //check_advancements
            general.addEntry(entryBuilder.startBooleanToggle(Compat.translatable("option.wands.check_advancements"), conf.check_advancements)
                    .setDefaultValue(true)
                    .setTooltip(Compat.translatable("option.wands.check_advancements_tt"))
                    .setSaveConsumer(newValue -> conf.check_advancements = newValue)
                    .build());
    //advancement_allow_stone_wand
            general.addEntry(entryBuilder.startStrField(Compat.translatable("option.wands.advancement_allow_stone_wand"), conf.advancement_allow_stone_wand)
                    .setDefaultValue("")
                    .setTooltip(Compat.translatable("option.wands.advancement_allow_stone_wand_tt"))
                    .setSaveConsumer(newValue -> conf.advancement_allow_stone_wand = newValue)
                    .build());
    //advancement_allow_iron_wand
            general.addEntry(entryBuilder.startStrField(Compat.translatable("option.wands.advancement_allow_iron_wand"), conf.advancement_allow_iron_wand)
                    .setDefaultValue("")
                    .setTooltip(Compat.translatable("option.wands.advancement_allow_iron_wand_tt"))
                    .setSaveConsumer(newValue -> conf.advancement_allow_iron_wand = newValue)
                    .build());
    //advancement_allow_diamond_wand
            general.addEntry(entryBuilder.startStrField(Compat.translatable("option.wands.advancement_allow_diamond_wand"), conf.advancement_allow_diamond_wand)
                    .setDefaultValue("")
                    .setTooltip(Compat.translatable("option.wands.advancement_allow_diamond_wand_tt"))
                    .setSaveConsumer(newValue -> conf.advancement_allow_diamond_wand = newValue)
                    .build());
    //advancement_allow_netherite_wand
            general.addEntry(entryBuilder.startStrField(Compat.translatable("option.wands.advancement_allow_netherite_wand"), conf.advancement_allow_netherite_wand)
                    .setDefaultValue("")
                    .setTooltip(Compat.translatable("option.wands.advancement_allow_netherite_wand_tt"))
                    .setSaveConsumer(newValue -> conf.advancement_allow_netherite_wand = newValue)
                    .build());
        }
        preview.addEntry(entryBuilder.startBooleanToggle(Compat.translatable("option.wands.thick_lines"), conf.fat_lines)
                .setDefaultValue(true)
                .setTooltip(Compat.translatable("option.wands.thick_lines_tt"))
                .setSaveConsumer(newValue -> conf.fat_lines = newValue)
                .build());
        preview.addEntry(entryBuilder.startFloatField(Compat.translatable("option.wands.line_thickness"), conf.fat_lines_width)
                .setDefaultValue(0.025f)
                .setTooltip(Compat.translatable("option.wands.fancy_preview_tt"))
                .setSaveConsumer(newValue -> conf.fat_lines_width = newValue)
                .build());

        preview.addEntry(entryBuilder.startBooleanToggle(Compat.translatable("option.wands.fancy_preview"), conf.fancy_preview)
                .setDefaultValue(true)
                .setTooltip(Compat.translatable("option.wands.line_thickness_tt"))
                .setSaveConsumer(newValue -> conf.fancy_preview = newValue)
                .build());

        preview.addEntry(entryBuilder.startFloatField(Compat.translatable("option.wands.opacity"), conf.preview_opacity)
                .setDefaultValue(0.8f)
                .setTooltip(Compat.translatable("option.wands.opacity_tt"))
                .setSaveConsumer(newValue -> conf.preview_opacity = newValue)
                .build());

        preview.addEntry(entryBuilder.startBooleanToggle(Compat.translatable("option.wands.lines"), conf.lines)
                .setDefaultValue(true)
                .setTooltip(Compat.translatable("option.wands.lines_tt"))
                .setSaveConsumer(newValue -> conf.lines = newValue)
                .build());

        preview.addEntry(entryBuilder.startBooleanToggle(Compat.translatable("option.wands.block_outlines"), conf.block_outlines)
                .setDefaultValue(true)
                .setTooltip(Compat.translatable("option.wands.block_outlines_tt"))
                .setSaveConsumer(newValue -> conf.block_outlines = newValue)
                .build());
        preview.addEntry(entryBuilder.startBooleanToggle(Compat.translatable("option.wands.fill_outlines"), conf.fill_outlines)
                .setDefaultValue(true)
                .setTooltip(Compat.translatable("option.wands.fill_outlines_tt"))
                .setSaveConsumer(newValue -> conf.fill_outlines = newValue)
                .build());
        preview.addEntry(entryBuilder.startBooleanToggle(Compat.translatable("option.wands.copy_outlines"), conf.copy_outlines)
                .setDefaultValue(true)
                .setTooltip(Compat.translatable("option.wands.copy_outlines_tt"))
                .setSaveConsumer(newValue -> conf.copy_outlines = newValue)
                .build());
        preview.addEntry(entryBuilder.startBooleanToggle(Compat.translatable("option.wands.paste_outlines"), conf.paste_outlines)
                .setDefaultValue(true)
                .setTooltip(Compat.translatable("option.wands.paste_outlines_tt"))
                .setSaveConsumer(newValue -> conf.paste_outlines = newValue)
                .build());

        preview.addEntry(entryBuilder.startBooleanToggle(Compat.translatable("option.wands.render_last"), conf.render_last)
                .setDefaultValue(true)
                .setTooltip(Compat.translatable("option.wands.render_last_tt"))
                .setSaveConsumer(newValue -> conf.render_last = newValue)
                .build());
        preview.addEntry(entryBuilder.startFloatField(Compat.translatable("option.wands.wand_mode_display_x_pos"), conf.wand_mode_display_x_pos)
                .setDefaultValue(75.0f)
                .setTooltip(Compat.translatable("option.wands.wand_mode_display_x_pos_tt"))
                .setSaveConsumer(newValue -> conf.wand_mode_display_x_pos = newValue)
                .build());
        preview.addEntry(entryBuilder.startFloatField(Compat.translatable("option.wands.wand_mode_display_y_pos"), conf.wand_mode_display_y_pos)
                .setDefaultValue(100.0f)
                .setTooltip(Compat.translatable("option.wands.wand_mode_display_y_pos_tt"))
                .setSaveConsumer(newValue -> conf.wand_mode_display_y_pos = newValue)
                .build());

        preview.addEntry(entryBuilder.startFloatField(Compat.translatable("option.wands.wand_tools_display_x_pos"), conf.wand_tools_display_x_pos)
                .setDefaultValue(0.0f)
                .setTooltip(Compat.translatable("option.wands.wand_tools_display_x_pos_tt"))
                .setSaveConsumer(newValue -> conf.wand_tools_display_x_pos = newValue)
                .build());
        preview.addEntry(entryBuilder.startFloatField(Compat.translatable("option.wands.wand_tools_display_y_pos"), conf.wand_tools_display_y_pos)
                .setDefaultValue(100.0f)
                .setTooltip(Compat.translatable("option.wands.wand_tools_display_y_pos_tt"))
                .setSaveConsumer(newValue -> conf.wand_tools_display_y_pos = newValue)
                .build());
        preview.addEntry(entryBuilder.startIntField(Compat.translatable("option.wands.wand_screen_x_offset"), conf.wand_screen_x_offset)
                .setDefaultValue(0)
                .setTooltip(Compat.translatable("option.wands.wand_screen_x_offset_tt"))
                .setSaveConsumer(newValue -> conf.wand_screen_x_offset = newValue)
                .build());
        preview.addEntry(entryBuilder.startIntField(Compat.translatable("option.wands.wand_screen_y_offset"), conf.wand_screen_y_offset)
                .setDefaultValue(0)
                .setTooltip(Compat.translatable("option.wands.wand_screen_y_offset_tt"))
                .setSaveConsumer(newValue -> conf.wand_screen_y_offset = newValue)
                .build());

        preview.addEntry(entryBuilder
                .startAlphaColorField(
                        Compat.translatable("option.wands.block_outline_color"),
                        WandsConfig.c_block_outline)
                .setAlphaMode(true)
                .setDefaultValue(WandsConfig.def_c_block_outline.getColor())
                .setTooltip(Compat.translatable("option.wands.block_outline_color_tt"))
                .setSaveConsumer(newValue -> {
                    WandsConfig.c_block_outline=Color.ofTransparent(newValue);
                    ClientRender.update_colors();
                }).build());

        preview.addEntry(entryBuilder
                .startAlphaColorField(
                        Compat.translatable("option.wands.bounding_box_color"),
                        WandsConfig.c_bounding_box)
                .setAlphaMode(true)
                .setDefaultValue(WandsConfig.def_c_bounding_box.getColor())
                .setTooltip(Compat.translatable("option.wands.bounding_box_tt"))
                .setSaveConsumer(newValue -> {
                    WandsConfig.c_bounding_box=Color.ofTransparent(newValue);
                    ClientRender.update_colors();
                }).build());

        preview.addEntry(entryBuilder
                .startAlphaColorField(
                        Compat.translatable("option.wands.destroy_color"),
                        WandsConfig.c_destroy)
                .setAlphaMode(true)
                .setDefaultValue(WandsConfig.def_c_destroy.getColor())
                .setTooltip(Compat.translatable("option.wands.destroy_color_tt"))
                .setSaveConsumer(newValue -> {
                    WandsConfig.c_destroy=Color.ofTransparent(newValue);
                    ClientRender.update_colors();
                }).build());

        preview.addEntry(entryBuilder
                .startAlphaColorField(
                        Compat.translatable("option.wands.tool_use_color"),
                        WandsConfig.c_tool_use)
                .setAlphaMode(true)
                .setDefaultValue(WandsConfig.def_c_tool_use.getColor())
                .setTooltip(Compat.translatable("option.wands.tool_use_color_tt"))
                .setSaveConsumer(newValue -> {
                    WandsConfig.c_tool_use=Color.ofTransparent(newValue);
                    ClientRender.update_colors();
                }).build());

        preview.addEntry(entryBuilder
                .startAlphaColorField(
                    Compat.translatable("option.wands.start_color"),
                    WandsConfig.c_start)
                .setAlphaMode(true)
                .setDefaultValue(WandsConfig.def_c_start.getColor())
                .setTooltip(Compat.translatable("option.wands.start_color_tt"))
                .setSaveConsumer(newValue -> {
                    WandsConfig.c_start=Color.ofTransparent(newValue);
                    ClientRender.update_colors();
                }).build());

        preview.addEntry(entryBuilder
            .startAlphaColorField(
                    Compat.translatable("option.wands.end_color"),
                    WandsConfig.c_end)
            .setAlphaMode(true)
            .setDefaultValue(WandsConfig.def_c_end.getColor())
            .setTooltip(Compat.translatable("option.wands.end_color_tt"))
            .setSaveConsumer(newValue -> {
                WandsConfig.c_end=Color.ofTransparent(newValue);
                ClientRender.update_colors();
            }).build());

        preview.addEntry(entryBuilder
                .startAlphaColorField(
                        Compat.translatable("option.wands.line_color"),
                        WandsConfig.c_line)
                .setAlphaMode(true)
                .setDefaultValue(WandsConfig.def_c_line.getColor())
                .setTooltip(Compat.translatable("option.wands.line_color_tt"))
                .setSaveConsumer(newValue -> {
                    WandsConfig.c_line=Color.ofTransparent(newValue);
                    ClientRender.update_colors();
                }).build());

        preview.addEntry(entryBuilder
                .startAlphaColorField(
                        Compat.translatable("option.wands.paste_bb_color"),
                        WandsConfig.c_paste_bb)
                .setAlphaMode(true)
                .setDefaultValue(WandsConfig.def_c_paste_bb.getColor())
                .setTooltip(Compat.translatable("option.wands.paste_bb_color_tt"))
                .setSaveConsumer(newValue -> {
                    WandsConfig.c_paste_bb=Color.ofTransparent(newValue);
                    ClientRender.update_colors();
                }).build());

        preview.addEntry(entryBuilder
                .startAlphaColorField(
                        Compat.translatable("option.wands.block_color"),
                        WandsConfig.c_block)
                .setAlphaMode(true)
                .setDefaultValue(WandsConfig.def_c_block.getColor())
                .setTooltip(Compat.translatable("option.wands.block_color_tt"))
                .setSaveConsumer(newValue -> {
                    WandsConfig.c_block=Color.ofTransparent(newValue);
                    ClientRender.update_colors();
                }).build());
        Screen screen = builder.build();
        return screen;
    }
}
#endif