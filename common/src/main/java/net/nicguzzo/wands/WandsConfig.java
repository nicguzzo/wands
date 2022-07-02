package net.nicguzzo.wands;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.shedaniel.math.Color;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class WandsConfig {

	private static WandsConfig INSTANCE=null;
	final public static String[] default_pickaxe_allowed={"minecraft:sea_lantern"};
	final public static String[] default_axe_allowed={};
	final public static String[] default_shovel_allowed={};
	final public static String[] default_hoe_allowed={};
	final public static String[] default_denied={};
	static public float def_blocks_per_xp=0.0f;
	static public int def_stone_wand_limit = 16;
	static public int def_iron_wand_limit = 32;
	static public int def_diamond_wand_limit = 64;
	static public int def_netherite_wand_limit = 256;
	static public int def_stone_wand_durability = 256;
	static public int def_iron_wand_durability = 512;
	static public int def_diamond_wand_durability = 2048;
	static public int def_netherite_wand_durability = 4096;
	public float blocks_per_xp =def_blocks_per_xp;
	public int stone_wand_limit = def_stone_wand_limit;
	public int iron_wand_limit = def_iron_wand_limit;
	public int diamond_wand_limit = def_diamond_wand_limit;
	public int netherite_wand_limit = def_netherite_wand_limit;
	public int stone_wand_durability = def_stone_wand_durability;
	public int iron_wand_durability = def_iron_wand_durability;
	public int diamond_wand_durability = def_diamond_wand_durability;
	public int netherite_wand_durability = def_netherite_wand_durability;
	public boolean destroy_in_survival_drop=true;
	public boolean survival_unenchanted_drops=false;
	public boolean allow_wand_to_break=false;
	public boolean allow_offhand_to_break=false;
	public boolean mend_tools=true;
	public float wand_mode_display_x_pos = 80.0f;
	public float wand_mode_display_y_pos = 100.0f;
	public float preview_opacity = 0.8f;
	public boolean fancy_preview = true;
	public boolean block_outlines=true;
	public boolean fill_outlines=true;
	public boolean copy_outlines=true;
	public boolean paste_outlines=true;
	public boolean no_lines=false;
	public boolean fat_lines=true;
	public boolean render_last=false;
	public boolean show_tools_info=true;
	public boolean check_advancements=false;
	public String advancement_allow_stone_wand="";
	public String advancement_allow_iron_wand="";
	//public String advancement_allow_diamond_wand="minecraft:mine_diamond";
	public String advancement_allow_diamond_wand="";
	public String advancement_allow_netherite_wand="";
	public float fat_lines_width=0.025f;
	public String block_outline_color="220,220,220,255";
	public String bounding_box_color="0,0,200,255";
	public String destroy_color="220,0,0,255";
	public String tool_use_color="240,240,0,255";
	public String start_color="0,200,200,255";
	public String end_color="0,200,0,255";
	public String line_color="200,0,200,200";
	public String paste_bb_color="80,40,0,255";
	public String block_color="255,255,255,255";

	public String[] str_pickaxe_allowed=default_pickaxe_allowed;
	public String[] str_axe_allowed=default_axe_allowed;
	public String[] str_shovel_allowed=default_shovel_allowed;
	public String[] str_hoe_allowed=default_hoe_allowed;
	public String[] str_denied=default_denied;
	static public List<Block> pickaxe_allowed=new ArrayList<Block>();
	static public List<Block> axe_allowed=new ArrayList<Block>();
	static public List<Block> shovel_allowed=new ArrayList<Block>();
	static public List<Block> hoe_allowed=new ArrayList<Block>();
	static public List<Block> denied=new ArrayList<Block>();
	static public Color c_block_outline;
	static public Color c_bounding_box;
	static public Color c_destroy;
	static public Color c_tool_use;
	static public Color c_start;
	static public Color c_end;
	static public Color c_line;
	static public Color c_paste_bb;
	static public Color c_block;

	Color parse_color(String col){
		int r=255,g=255,b=255,a=255;
		String [] temp = col.split(",");
		if(temp.length==4){
			try {
				r = Integer.parseInt(temp[0]);
				g = Integer.parseInt(temp[1]);
				b = Integer.parseInt(temp[2]);
				a = Integer.parseInt(temp[3]);
			}catch (NumberFormatException e){
				System.out.printf("error parsing color, "+e.getMessage());
			}
		}
		return Color.ofRGBA(r,g,b,a);
	}
	public void generate_allow_list(List<Block> out,String[] str) {
		for (String id : str) {			
			ResourceLocation res=ResourceLocation.tryParse(id);
			if(res!=null){
				Item item=Registry.ITEM.get(res);
				if(item!=null && item!=Items.AIR){
					Block blk=Block.byItem(item);
					if(blk!=null){
						out.add(blk);
					}
				}
			}
		}
	}
	public void parse_colors(){
		c_block_outline=parse_color(block_outline_color);
		c_bounding_box=parse_color(bounding_box_color);
		c_destroy=parse_color(destroy_color);
		c_tool_use=parse_color(tool_use_color);
		c_start=parse_color(start_color);
		c_end=parse_color(end_color);
		c_line=parse_color(line_color);
		c_paste_bb=parse_color(paste_bb_color);
		c_block=parse_color(block_color);
	}
	public void generate_lists(){
		System.out.println("generating allow/deny lists");
		generate_allow_list(pickaxe_allowed,str_pickaxe_allowed);
		generate_allow_list(axe_allowed,str_axe_allowed);
		generate_allow_list(shovel_allowed,str_shovel_allowed);
		generate_allow_list(hoe_allowed,str_hoe_allowed);
		generate_allow_list(denied,str_denied);
		System.out.println("denied "+denied.size());
		for (Block b : denied) {
			System.out.println("denied "+b);
		}
		parse_colors();
	}
	//TODO: catch json errors
	public static void load_config() {
		INSTANCE = new WandsConfig();
		Gson gson=new Gson();
		File configFile = new File(WandsExpectPlatform.getConfigDirectory().toString(), "wands.json");
		try (FileReader reader = new FileReader(configFile)) {
			INSTANCE = gson.fromJson(reader, WandsConfig.class);
			System.out.println("Config: "+INSTANCE);
			try (FileWriter writer = new FileWriter(configFile)) {
				writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(INSTANCE));
				System.out.println("Config updated!");
			} catch (IOException e2) {
				System.out.println("Failed to update config file!");
			}
			System.out.println("Config loaded!");

		} catch (IOException e) {
			System.out.println("No config found, generating!");
			INSTANCE = new WandsConfig();
			try (FileWriter writer = new FileWriter(configFile)) {
				writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(INSTANCE));
			} catch (IOException e2) {
				System.out.println("Failed to generate config file!");
			}
		}
		INSTANCE.parse_colors();
	}
	public static String save_color(Color c) {
		return c.getRed()+","+c.getGreen()+","+c.getBlue()+","+c.getAlpha();
	}
	public static void save_config() {
		File configFile = new File(WandsExpectPlatform.getConfigDirectory().toString(), "wands.json");
		if(INSTANCE==null)
			INSTANCE = new WandsConfig();

		INSTANCE.block_outline_color=save_color(c_block_outline);
		INSTANCE.bounding_box_color=save_color(c_bounding_box);
		INSTANCE.destroy_color=save_color(c_destroy);
		INSTANCE.tool_use_color=save_color(c_tool_use);
		INSTANCE.start_color=save_color(c_start);
		INSTANCE.end_color=save_color(c_end);
		INSTANCE.line_color=save_color(c_line);
		INSTANCE.paste_bb_color=save_color(c_paste_bb);
		INSTANCE.block_color=save_color(c_block);

		try (FileWriter writer = new FileWriter(configFile)) {
			writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(INSTANCE));
		} catch (IOException e2) {
			System.out.println("Failed to generate config file!");
		}
	}

	public static WandsConfig get_instance(){
		if(INSTANCE==null){
			load_config();
		}
		return INSTANCE;
	}
}
