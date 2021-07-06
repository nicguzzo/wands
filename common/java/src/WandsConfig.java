package net.nicguzzo.common;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.GsonBuilder;

import net.minecraft.block.Block;
import net.nicguzzo.WandsMod;

public class WandsConfig {
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
/*
	static public class BlockDeserializer implements JsonDeserializer<Block> {
		public Block deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			String id=json.getAsJsonPrimitive().getAsString();				
			return Registry.BLOCK.get(Identifier.tryParse(id));
		}
	}

	static  public class BlockSerializer implements JsonSerializer<Block> {
		public JsonElement serialize(Block src, Type typeOfT, JsonSerializationContext context){			
			return new JsonPrimitive(src.getLootTableId().toString());
		}
	}*/


/*
	public WandsConfig(float bpxp, int s_l, int i_l, int d_l, int n_l, int s_d, int i_d, int d_d, int n_d,String[]a,String[]d,boolean disd) {
		if (bpxp >= 0.0f)
			this.blocks_per_xp = bpxp;
		stone_wand_limit = s_l;
		iron_wand_limit = i_l;
		diamond_wand_limit = d_l;
		netherite_wand_limit = n_l;
		stone_wand_durability = s_d;
		iron_wand_durability = i_d;
		diamond_wand_durability = d_d;
		netherite_wand_durability = n_d;
		str_allowed=a;
		str_denied=d;
		destroy_in_survival_drop=disd;
	}
	
	public WandsConfig() {
		this(0.0f, 9, 27, 49, 81, 131, 250, 1561, 2031,default_allowed,default_denied,true);
	}*/

	public void generate_lists(){
		System.out.println("generating allow/deny lists");
		for (String id : str_allowed) {
			Block blk=WandsMod.compat.block_from_id(id);
			if(blk!=null)
				allowed.add(blk);
		}
		for (String id : str_denied) {
			Block blk=WandsMod.compat.block_from_id(id);
			if(blk!=null)
				denied.add(blk);
		}
	}
/*
	public String toString() {
		return "blocks_per_xp: " + blocks_per_xp;
	}

	public boolean equals(WandsConfig config) {
		return (config.blocks_per_xp == blocks_per_xp);
	}*/

	static public void save_conf(WandsConfig config,File configFile){
		try (FileWriter writer = new FileWriter(configFile)) {
			GsonBuilder gson =new GsonBuilder();			
			//gson.registerTypeAdapter(Block.class, new BlockSerializer());
			//gson.registerTypeAdapter(Block.class, new BlockDeserializer());	
			writer.write(gson.setPrettyPrinting().create().toJson(config));
		} catch (IOException e2) {
			System.out.println("Failed to generate config file!");
		}
	}
	static public WandsConfig load_config(Path confdir) {
		WandsConfig config = null;
		System.out.println("load_config dir: " + confdir);
		File configFile = new File(confdir.toString(), "wands.json");
		try (FileReader reader = new FileReader(configFile)) {
			GsonBuilder gson=new GsonBuilder();

			//gson.registerTypeAdapter(Block.class, new BlockSerializer());
			//gson.registerTypeAdapter(Block.class, new BlockDeserializer());
			config = gson.create().fromJson(reader, WandsConfig.class);
			save_conf(config,configFile);			
			System.out.println("Config loaded!");
		} catch (IOException e) {
			System.out.println("No config found, generating!");		
			save_conf(new WandsConfig(),configFile);
		}
		if(config==null){
            config=new WandsConfig();
        }
		return config;
	}
}
