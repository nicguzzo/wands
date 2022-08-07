package net.nicguzzo.wands;
#if USE_CLOTHCONFIG
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.math.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.nicguzzo.wands.mcver.MCVer;

public class WandConfigScreen {
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(MCVer.inst.translatable("title.wands.config"));
        WandsConfig conf = WandsMod.config;
        builder.setSavingRunnable(() -> {
            WandsConfig.save_config();
        });
        ServerData srv = Minecraft.getInstance().getCurrentServer();

        ConfigCategory general = builder.getOrCreateCategory(MCVer.inst.translatable("category.wands.general"));
        ConfigCategory preview = builder.getOrCreateCategory(MCVer.inst.translatable("category.wands.preview"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        if (srv == null)
        {
    //blocks_per_xp
                general.addEntry(entryBuilder.startFloatField(MCVer.inst.translatable("option.wands.blocks_per_xp"), conf.blocks_per_xp)
                        .setDefaultValue(WandsConfig.def_blocks_per_xp)
                        .setTooltip(MCVer.inst.translatable("option.wands.blocks_per_xp_tt"))
                        .setSaveConsumer(newValue -> conf.blocks_per_xp = newValue)
                        .build());

    //stone_wand_limit
            general.addEntry(entryBuilder.startIntField(MCVer.inst.translatable("option.wands.stone_wand_limit"), conf.stone_wand_limit)
                    .setDefaultValue(WandsConfig.def_stone_wand_limit)
                    .setMin(0)
                    .setMax(Wand.MAX_LIMIT)
                    .setTooltip(MCVer.inst.translatable("option.wands.stone_wand_limit_tt"))
                    .setSaveConsumer(newValue -> conf.stone_wand_limit = newValue)
                    .build());
    //iron_wand_limit
            general.addEntry(entryBuilder.startIntField(MCVer.inst.translatable("option.wands.iron_wand_limit"), conf.iron_wand_limit)
                    .setDefaultValue(WandsConfig.def_iron_wand_limit)
                    .setMin(0)
                    .setMax(Wand.MAX_LIMIT)
                    .setTooltip(MCVer.inst.translatable("option.wands.iron_wand_limit_tt"))
                    .setSaveConsumer(newValue -> conf.iron_wand_limit = newValue)
                    .build());
    //diamond_wand_limit
            general.addEntry(entryBuilder.startIntField(MCVer.inst.translatable("option.wands.diamond_wand_limit"), conf.diamond_wand_limit)
                    .setDefaultValue(WandsConfig.def_diamond_wand_limit)
                    .setMin(0)
                    .setMax(Wand.MAX_LIMIT)
                    .setTooltip(MCVer.inst.translatable("option.wands.diamond_wand_limit_tt"))
                    .setSaveConsumer(newValue -> conf.diamond_wand_limit = newValue)
                    .build());
    //netherite_wand_limit
            general.addEntry(entryBuilder.startIntField(MCVer.inst.translatable("option.wands.netherite_wand_limit"), conf.netherite_wand_limit)
                    .setDefaultValue(WandsConfig.def_netherite_wand_limit)
                    .setMin(0)
                    .setMax(Wand.MAX_LIMIT)
                    .setTooltip(MCVer.inst.translatable("option.wands.netherite_wand_limit_tt"))
                    .setSaveConsumer(newValue -> conf.netherite_wand_limit = newValue)
                    .build());
    //stone_wand_durability
            general.addEntry(entryBuilder.startIntField(MCVer.inst.translatable("option.wands.stone_wand_durability"), conf.stone_wand_durability)
                    .setDefaultValue(WandsConfig.def_stone_wand_durability)
                    .setMin(0)
                    .setTooltip(MCVer.inst.translatable("option.wands.stone_wand_durability_tt"))
                    .setSaveConsumer(newValue -> conf.stone_wand_durability = newValue)
                    .build());
    //iron_wand_durability
            general.addEntry(entryBuilder.startIntField(MCVer.inst.translatable("option.wands.iron_wand_durability"), conf.iron_wand_durability)
                    .setDefaultValue(WandsConfig.def_iron_wand_durability)
                    .setMin(0)
                    .setTooltip(MCVer.inst.translatable("option.wands.iron_wand_durability_tt"))
                    .setSaveConsumer(newValue -> conf.iron_wand_durability = newValue)
                    .build());
    //diamond_wand_durability
            general.addEntry(entryBuilder.startIntField(MCVer.inst.translatable("option.wands.diamond_wand_durability"), conf.diamond_wand_durability)
                    .setDefaultValue(WandsConfig.def_diamond_wand_durability)
                    .setMin(0)
                    .setTooltip(MCVer.inst.translatable("option.wands.diamond_wand_durability_tt"))
                    .setSaveConsumer(newValue -> conf.diamond_wand_durability = newValue)
                    .build());
    //netherite_wand_durability
            general.addEntry(entryBuilder.startIntField(MCVer.inst.translatable("option.wands.netherite_wand_durability"), conf.netherite_wand_durability)
                    .setDefaultValue(WandsConfig.def_netherite_wand_durability)
                    .setMin(0)
                    .setTooltip(MCVer.inst.translatable("option.wands.netherite_wand_durability_tt"))
                    .setSaveConsumer(newValue -> conf.netherite_wand_durability = newValue)
                    .build());
    //destroy_in_survival_drop
            general.addEntry(entryBuilder.startBooleanToggle(MCVer.inst.translatable("option.wands.destroy_in_survival_drop"), conf.destroy_in_survival_drop)
                    .setDefaultValue(true)
                    .setTooltip(MCVer.inst.translatable("option.wands.destroy_in_survival_drop_tt"))
                    .setSaveConsumer(newValue -> conf.destroy_in_survival_drop = newValue)
                    .build());
    //survival_unenchanted_drops
            general.addEntry(entryBuilder.startBooleanToggle(MCVer.inst.translatable("option.wands.survival_unenchanted_drops"), conf.survival_unenchanted_drops)
                    .setDefaultValue(true)
                    .setTooltip(MCVer.inst.translatable("option.wands.survival_unenchanted_drops_tt"))
                    .setSaveConsumer(newValue -> conf.survival_unenchanted_drops = newValue)
                    .build());
    //allow_wand_to_break
            general.addEntry(entryBuilder.startBooleanToggle(MCVer.inst.translatable("option.wands.allow_wand_to_break"), conf.allow_wand_to_break)
                    .setDefaultValue(true)
                    .setTooltip(MCVer.inst.translatable("option.wands.allow_wand_to_break_tt"))
                    .setSaveConsumer(newValue -> conf.allow_wand_to_break = newValue)
                    .build());
    //allow_offhand_to_break
            general.addEntry(entryBuilder.startBooleanToggle(MCVer.inst.translatable("option.wands.allow_offhand_to_break"), conf.allow_offhand_to_break)
                    .setDefaultValue(true)
                    .setTooltip(MCVer.inst.translatable("option.wands.allow_offhand_to_break_tt"))
                    .setSaveConsumer(newValue -> conf.allow_offhand_to_break = newValue)
                    .build());
    //check_advancements
            general.addEntry(entryBuilder.startBooleanToggle(MCVer.inst.translatable("option.wands.check_advancements"), conf.check_advancements)
                    .setDefaultValue(true)
                    .setTooltip(MCVer.inst.translatable("option.wands.check_advancements_tt"))
                    .setSaveConsumer(newValue -> conf.check_advancements = newValue)
                    .build());
    //advancement_allow_stone_wand
            general.addEntry(entryBuilder.startStrField(MCVer.inst.translatable("option.wands.advancement_allow_stone_wand"), conf.advancement_allow_stone_wand)
                    .setDefaultValue("")
                    .setTooltip(MCVer.inst.translatable("option.wands.advancement_allow_stone_wand_tt"))
                    .setSaveConsumer(newValue -> conf.advancement_allow_stone_wand = newValue)
                    .build());
    //advancement_allow_iron_wand
            general.addEntry(entryBuilder.startStrField(MCVer.inst.translatable("option.wands.advancement_allow_iron_wand"), conf.advancement_allow_iron_wand)
                    .setDefaultValue("")
                    .setTooltip(MCVer.inst.translatable("option.wands.advancement_allow_iron_wand_tt"))
                    .setSaveConsumer(newValue -> conf.advancement_allow_iron_wand = newValue)
                    .build());
    //advancement_allow_diamond_wand
            general.addEntry(entryBuilder.startStrField(MCVer.inst.translatable("option.wands.advancement_allow_diamond_wand"), conf.advancement_allow_diamond_wand)
                    .setDefaultValue("")
                    .setTooltip(MCVer.inst.translatable("option.wands.advancement_allow_diamond_wand_tt"))
                    .setSaveConsumer(newValue -> conf.advancement_allow_diamond_wand = newValue)
                    .build());
    //advancement_allow_netherite_wand
            general.addEntry(entryBuilder.startStrField(MCVer.inst.translatable("option.wands.advancement_allow_netherite_wand"), conf.advancement_allow_netherite_wand)
                    .setDefaultValue("")
                    .setTooltip(MCVer.inst.translatable("option.wands.advancement_allow_netherite_wand_tt"))
                    .setSaveConsumer(newValue -> conf.advancement_allow_netherite_wand = newValue)
                    .build());
        }
        preview.addEntry(entryBuilder.startBooleanToggle(MCVer.inst.translatable("option.wands.thick_lines"), conf.fat_lines)
                .setDefaultValue(true)
                .setTooltip(MCVer.inst.translatable("option.wands.thick_lines_tt"))
                .setSaveConsumer(newValue -> conf.fat_lines = newValue)
                .build());
        preview.addEntry(entryBuilder.startFloatField(MCVer.inst.translatable("option.wands.line_thickness"), conf.fat_lines_width)
                .setDefaultValue(0.025f)
                .setTooltip(MCVer.inst.translatable("option.wands.fancy_preview_tt"))
                .setSaveConsumer(newValue -> conf.fat_lines_width = newValue)
                .build());

        preview.addEntry(entryBuilder.startBooleanToggle(MCVer.inst.translatable("option.wands.fancy_preview"), conf.fancy_preview)
                .setDefaultValue(true)
                .setTooltip(MCVer.inst.translatable("option.wands.line_thickness_tt"))
                .setSaveConsumer(newValue -> conf.fancy_preview = newValue)
                .build());

        preview.addEntry(entryBuilder.startFloatField(MCVer.inst.translatable("option.wands.opacity"), conf.preview_opacity)
                .setDefaultValue(0.8f)
                .setTooltip(MCVer.inst.translatable("option.wands.opacity_tt"))
                .setSaveConsumer(newValue -> conf.preview_opacity = newValue)
                .build());

        preview.addEntry(entryBuilder.startBooleanToggle(MCVer.inst.translatable("option.wands.no_lines"), conf.no_lines)
                .setDefaultValue(true)
                .setTooltip(MCVer.inst.translatable("option.wands.no_lines_tt"))
                .setSaveConsumer(newValue -> conf.no_lines = newValue)
                .build());

        preview.addEntry(entryBuilder.startBooleanToggle(MCVer.inst.translatable("option.wands.block_outlines"), conf.block_outlines)
                .setDefaultValue(true)
                .setTooltip(MCVer.inst.translatable("option.wands.block_outlines_tt"))
                .setSaveConsumer(newValue -> conf.block_outlines = newValue)
                .build());
        preview.addEntry(entryBuilder.startBooleanToggle(MCVer.inst.translatable("option.wands.fill_outlines"), conf.fill_outlines)
                .setDefaultValue(true)
                .setTooltip(MCVer.inst.translatable("option.wands.fill_outlines_tt"))
                .setSaveConsumer(newValue -> conf.fill_outlines = newValue)
                .build());
        preview.addEntry(entryBuilder.startBooleanToggle(MCVer.inst.translatable("option.wands.copy_outlines"), conf.copy_outlines)
                .setDefaultValue(true)
                .setTooltip(MCVer.inst.translatable("option.wands.copy_outlines_tt"))
                .setSaveConsumer(newValue -> conf.copy_outlines = newValue)
                .build());
        preview.addEntry(entryBuilder.startBooleanToggle(MCVer.inst.translatable("option.wands.paste_outlines"), conf.paste_outlines)
                .setDefaultValue(true)
                .setTooltip(MCVer.inst.translatable("option.wands.paste_outlines_tt"))
                .setSaveConsumer(newValue -> conf.paste_outlines = newValue)
                .build());

        preview.addEntry(entryBuilder.startBooleanToggle(MCVer.inst.translatable("option.wands.render_last"), conf.render_last)
                .setDefaultValue(true)
                .setTooltip(MCVer.inst.translatable("option.wands.render_last_tt"))
                .setSaveConsumer(newValue -> conf.render_last = newValue)
                .build());
        preview.addEntry(entryBuilder.startFloatField(MCVer.inst.translatable("option.wands.wand_mode_display_x_pos"), conf.wand_mode_display_x_pos)
                .setDefaultValue(75.0f)
                .setTooltip(MCVer.inst.translatable("option.wands.wand_mode_display_x_pos_tt"))
                .setSaveConsumer(newValue -> conf.wand_mode_display_x_pos = newValue)
                .build());
        preview.addEntry(entryBuilder.startFloatField(MCVer.inst.translatable("option.wands.wand_mode_display_y_pos"), conf.wand_mode_display_y_pos)
                .setDefaultValue(100.0f)
                .setTooltip(MCVer.inst.translatable("option.wands.wand_mode_display_y_pos_tt"))
                .setSaveConsumer(newValue -> conf.wand_mode_display_y_pos = newValue)
                .build());

        preview.addEntry(entryBuilder.startFloatField(MCVer.inst.translatable("option.wands.wand_tools_display_x_pos"), conf.wand_mode_display_x_pos)
                .setDefaultValue(0.0f)
                .setTooltip(MCVer.inst.translatable("option.wands.wand_tools_display_x_pos_tt"))
                .setSaveConsumer(newValue -> conf.wand_tools_display_x_pos = newValue)
                .build());
        preview.addEntry(entryBuilder.startFloatField(MCVer.inst.translatable("option.wands.wand_tools_display_y_pos"), conf.wand_mode_display_y_pos)
                .setDefaultValue(100.0f)
                .setTooltip(MCVer.inst.translatable("option.wands.wand_tools_display_y_pos_tt"))
                .setSaveConsumer(newValue -> conf.wand_tools_display_y_pos = newValue)
                .build());


        preview.addEntry(entryBuilder.startColorField(MCVer.inst.translatable("option.wands.block_outline_color"), WandsConfig.c_block_outline)
                .setDefaultValue(Color.ofRGBA(220,220,220,255).getColor())
                .setTooltip(MCVer.inst.translatable("option.wands.block_outline_color_tt"))
                .setSaveConsumer(newValue -> {
                    WandsConfig.c_block_outline=Color.ofOpaque(newValue);
                    ClientRender.update_colors();
            })
                .build());

        preview.addEntry(entryBuilder.startColorField(MCVer.inst.translatable("option.wands.bounding_box_color"), WandsConfig.c_bounding_box)
                .setDefaultValue(Color.ofRGBA(0,0,200,255).getColor())
                .setTooltip(MCVer.inst.translatable("option.wands.bounding_box_tt"))
                .setSaveConsumer(newValue -> {
                    WandsConfig.c_bounding_box=Color.ofOpaque(newValue);
                    ClientRender.update_colors();
            })
                .build());

        preview.addEntry(entryBuilder.startColorField(MCVer.inst.translatable("option.wands.destroy_color"), WandsConfig.c_destroy)
                .setDefaultValue(Color.ofRGBA(220,0,0,255).getColor())
                .setTooltip(MCVer.inst.translatable("option.wands.destroy_color_tt"))
                .setSaveConsumer(newValue -> {
                    WandsConfig.c_destroy=Color.ofOpaque(newValue);
                    ClientRender.update_colors();
            })
                .build());

        preview.addEntry(entryBuilder.startColorField(MCVer.inst.translatable("option.wands.tool_use_color"), WandsConfig.c_tool_use)
                .setDefaultValue(Color.ofRGBA(240,240,0,255).getColor())
                .setTooltip(MCVer.inst.translatable("option.wands.tool_use_color_tt"))
                .setSaveConsumer(newValue -> {
                    WandsConfig.c_tool_use=Color.ofOpaque(newValue);
                    ClientRender.update_colors();
            })
                .build());

        preview.addEntry(entryBuilder.startColorField(MCVer.inst.translatable("option.wands.start_color"), WandsConfig.c_start)
                .setDefaultValue(Color.ofRGBA(0,200,200,255).getColor())
                .setTooltip(MCVer.inst.translatable("option.wands.start_color_tt"))
                .setSaveConsumer(newValue -> {
                    WandsConfig.c_start=Color.ofOpaque(newValue);
                    ClientRender.update_colors();
            })
                .build());

        preview.addEntry(entryBuilder.startColorField(MCVer.inst.translatable("option.wands.end_color"), WandsConfig.c_end)
                .setDefaultValue(Color.ofRGBA(0,200,0,255).getColor())
                .setTooltip(MCVer.inst.translatable("option.wands.end_color_tt"))
                .setSaveConsumer(newValue -> {
                    WandsConfig.c_end=Color.ofOpaque(newValue);
                    ClientRender.update_colors();
            })
                .build());
/*
                "block_color": "255,255,255,255",*/
                preview.addEntry(entryBuilder.startColorField(MCVer.inst.translatable("option.wands.line_color"), WandsConfig.c_line)
                        .setDefaultValue(Color.ofRGBA(200,0,200,200).getColor())
                        .setTooltip(MCVer.inst.translatable("option.wands.line_color_tt"))
                        .setSaveConsumer(newValue -> {
                            WandsConfig.c_line=Color.ofOpaque(newValue);
                            ClientRender.update_colors();
                    })
                        .build());

                preview.addEntry(entryBuilder.startColorField(MCVer.inst.translatable("option.wands.paste_bb_color"), WandsConfig.c_paste_bb)
                        .setDefaultValue(Color.ofRGBA(80,40,0,255).getColor())
                        .setTooltip(MCVer.inst.translatable("option.wands.paste_bb_color_tt"))
                        .setSaveConsumer(newValue -> {
                            WandsConfig.c_paste_bb=Color.ofOpaque(newValue);
                            ClientRender.update_colors();
                    })
                        .build());

                preview.addEntry(entryBuilder.startColorField(MCVer.inst.translatable("option.wands.block_color"), WandsConfig.c_block)
                        .setDefaultValue(Color.ofRGBA(255,255,255,255).getColor())
                        .setTooltip(MCVer.inst.translatable("option.wands.block_color_tt"))
                        .setSaveConsumer(newValue -> {
                            WandsConfig.c_block=Color.ofOpaque(newValue);
                            ClientRender.update_colors();
                    })
                        .build());
                Screen screen = builder.build();
                return screen;
        }
}
#endif