package net.nicguzzo.wands;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class WandsConfig {
	private static WandsConfig INSTANCE=null;
	final public static String[] default_allowed={"minecraft:soul_sand","minecraft:grass_path"};
	final public static String[] default_denied={};
	public float blocks_per_xp = 0.0f;
	public int stone_wand_limit = 9;
	public int iron_wand_limit = 49;
	public int diamond_wand_limit = 256;
	public int netherite_wand_limit = 1024;
	public int stone_wand_durability = 131;
	public int iron_wand_durability = 250;
	public int diamond_wand_durability = 1561;
	public int netherite_wand_durability = 2031;
	public boolean destroy_in_survival_drop=true;

	public String[] str_allowed=default_allowed;
	public String[] str_denied=default_denied;
	static public List<Block> allowed=new ArrayList<Block>();
	static public List<Block> denied=new ArrayList<Block>();

	public void generate_lists(){
		System.out.println("generating allow/deny lists");
		for (String id : str_allowed) {
			ResourceLocation res=ResourceLocation.tryParse(id);
			Item item=Registry.ITEM.get(res);
			Block blk=Block.byItem(item);
			if(blk!=null)
				allowed.add(blk);
		}
		for (String id : str_denied) {
			ResourceLocation res=ResourceLocation.tryParse(id);
			Item item=Registry.ITEM.get(res);
			Block blk=Block.byItem(item);
			if(blk!=null)
				denied.add(blk);
		}
	}

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
			INSTANCE.generate_lists();
		}
		return INSTANCE;
	}
}
