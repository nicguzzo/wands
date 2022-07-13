package net.nicguzzo.wands;
import java.util.HashMap;

import net.minecraft.world.entity.player.Player;

public class PlayerWand{
    
    public static HashMap<String,Wand> player_wand= new HashMap<String, Wand>();
    
    static public void add_player(Player player){
        WandsMod.log("add_player wand",true);
        Wand wand=new Wand();
        wand.player=player;
        player_wand.put(player.getStringUUID(), wand) ;
    }
    static public void remove_player(Player player){
        WandsMod.log("remove player wand",true);
        player_wand.remove(player.getStringUUID());
    }
    static public Wand get(Player player){
        Wand wand=player_wand.get(player.getStringUUID());
        if(wand!=null) {
            wand.player = player;
        }
        return wand;
    }
}
    