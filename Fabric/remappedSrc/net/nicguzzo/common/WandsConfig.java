package net.nicguzzo.common;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WandsConfig{
	public float blocks_per_xp=0.0f;
	public int stone_wand_limit=9;
	public int iron_wand_limit=25;
	public int diamond_wand_limit=49;
	public int netherite_wand_limit=81;
    public int stone_wand_durability= 131;
	public int iron_wand__durability= 250;
	public int diamond_wand__durability=1561;
	public int netherite_wand_durability=2031;
	
	public WandsConfig(float bpxp,int s_l,int i_l,int d_l,int n_l,int s_d,int i_d,int d_d,int n_d) {
		if(bpxp>=0.0f)
			this.blocks_per_xp = bpxp;
		stone_wand_limit=s_l;
		iron_wand_limit=i_l;
		diamond_wand_limit=d_l;
		netherite_wand_limit=n_l;
		stone_wand_durability=s_d;
		iron_wand__durability=i_d;
		diamond_wand__durability=d_d;
		netherite_wand_durability=n_d;
	}
	public WandsConfig() {
		this(0.0f,9,27,49,81,131,250,1561,2031);
	}
	public String toString() {
		return "blocks_per_xp: "+blocks_per_xp;
	}

	public boolean equals(WandsConfig config) {
		return (
			config.blocks_per_xp==blocks_per_xp
		);
	}
	static public WandsConfig load_config(Path confdir){
		WandsConfig config=null;
		System.out.println("load_config dir: "+confdir);
		File configFile = new File(confdir.toString(), "wands.json");
		try (FileReader reader = new FileReader(configFile)) {
			config = new Gson().fromJson(reader, WandsConfig.class);
			try (FileWriter writer = new FileWriter(configFile)) {
				writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(config));
			} catch (IOException e2) {
				System.out.println("Failed to update config file!");
			}
			System.out.println("Config loaded!");
			
		} catch (IOException e) {
			System.out.println("No config found, generating!");
			config = new WandsConfig();
			try (FileWriter writer = new FileWriter(configFile)) {
				writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(config));
			} catch (IOException e2) {
				System.out.println("Failed to generate config file!");
			}
		}
		return config;
	}
}
