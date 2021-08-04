package net.nicguzzo.wands;
import java.util.HashMap;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class PlayerWand{
    
    public static HashMap<String,Wand> player_wand= new HashMap<String, Wand>();
    
    static public void add_player(Player player){
        WandsMod.log("add_player",true);
        player_wand.put(player.getStringUUID(), new Wand()) ;
    }
    static public void remove_player(Player player){
        player_wand.remove(player.getStringUUID());
    }
    static public Wand get(Player player){
        return player_wand.get(player.getStringUUID());
    }
}
    