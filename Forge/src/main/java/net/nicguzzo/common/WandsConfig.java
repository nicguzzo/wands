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
	
	public WandsConfig(float bpxp) {
		if(bpxp>=0.0f)
			this.blocks_per_xp = bpxp;		
	}
	public WandsConfig() {
		this(0);
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
