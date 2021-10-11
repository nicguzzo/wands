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

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class WandsConfig {
	static class Color{
		public float r;
		public float g;
		public float b;
		public float a;
		Color(float rr,float gg,float bb,float aa){
			r=rr; g=gg; b=bb; a=aa;
		}
	}
	private static WandsConfig INSTANCE=null;
	final public static String[] default_pickaxe_allowed={"minecraft:sea_lantern"};
	final public static String[] default_axe_allowed={};
	final public static String[] default_shovel_allowed={};
	final public static String[] default_hoe_allowed={};
	final public static String[] default_denied={};
	public float blocks_per_xp = 0.0f;
	public int stone_wand_limit = 16;
	public int iron_wand_limit = 32;
	public int diamond_wand_limit = 64;
	public int netherite_wand_limit = 256;
	public int stone_wand_durability = 256;
	public int iron_wand_durability = 512;
	public int diamond_wand_durability = 2048;
	public int netherite_wand_durability = 4096;
	public boolean destroy_in_survival_drop=true;
	public boolean survival_unenchanted_drops=false;
	public boolean allow_wand_to_break=false;
	public boolean allow_offhand_to_break=false;
	public float wand_mode_display_x_pos = 80.0f;
	public float wand_mode_display_y_pos = 100.0f;
	public float preview_opacity = 0.8f;
	public boolean fancy_preview = true;
	public boolean fat_lines=true;
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
	public enum Colors{
		BLOCK_OUTLINE,
		BOUNDING_BOX,
		DESTROY,
		TOOL_USE,
		START,
		END,
		LINE,
		PASTE_BB,
		BlOCK
	}
	static public HashMap<Colors,Color> colors=new HashMap();

	void parse_colors(){
		colors.put(Colors.BLOCK_OUTLINE,parse_color(block_outline_color));
		colors.put(Colors.BOUNDING_BOX,parse_color(bounding_box_color));
		colors.put(Colors.DESTROY,parse_color(destroy_color));
		colors.put(Colors.TOOL_USE,parse_color(tool_use_color));
		colors.put(Colors.START,parse_color(start_color));
		colors.put(Colors.END,parse_color(end_color));
		colors.put(Colors.LINE,parse_color(line_color));
		colors.put(Colors.PASTE_BB,parse_color(paste_bb_color));
		colors.put(Colors.BlOCK,parse_color(block_color));
	}
	float clamp(float f){
		if(f<0) return 0.0f;
		if(f>1.0f) return 1.0f;
		return f;
	}
	Color parse_color(String col){
		float r=1.0f,g=1.0f,b=1.0f,a=1.0f;
		String [] temp = col.split(",");
		if(temp.length==4){
			try {
				r = clamp(Integer.parseInt(temp[0]) / 255.0f);
				g = clamp(Integer.parseInt(temp[1]) / 255.0f);
				b = clamp(Integer.parseInt(temp[2]) / 255.0f);
				a = clamp(Integer.parseInt(temp[3]) / 255.0f);
			}catch (NumberFormatException e){
				WandsMod.LOGGER.error("error parsing color, "+e.getMessage());
			}
		}
		return new Color(r,g,b,a);
	}
	public void generate_allow_list(List<Block> out,String[] str) {
		for (String id : str) {
			ResourceLocation res=ResourceLocation.tryParse(id);
			Item item=Registry.ITEM.get(res);
			Block blk=Block.byItem(item);
			if(blk!=null)
				out.add(blk);
		}
	}
	public void generate_lists(){
		//System.out.println("generating allow/deny lists");
		generate_allow_list(pickaxe_allowed,str_pickaxe_allowed);
		generate_allow_list(axe_allowed,str_axe_allowed);
		generate_allow_list(shovel_allowed,str_shovel_allowed);
		generate_allow_list(hoe_allowed,str_hoe_allowed);
		generate_allow_list(denied,str_denied);
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
	}

	public static WandsConfig get_instance(){
		if(INSTANCE==null){
			load_config();
			INSTANCE.parse_colors();
			INSTANCE.generate_lists();
		}
		return INSTANCE;
	}
}
