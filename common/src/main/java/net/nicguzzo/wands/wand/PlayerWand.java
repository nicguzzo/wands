package net.nicguzzo.wands.wand;
import java.util.HashMap;

import net.minecraft.world.entity.player.Player;
import net.nicguzzo.wands.WandsExpectPlatform;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.utils.IEntityDataSaver;

public class PlayerWand{
    
    public static HashMap<String,Wand> player_wand= new HashMap<String, Wand>();
    
    static public void add_player(Player player){
        WandsMod.log("add_player wand",true);
        Wand wand=new Wand();
        wand.player=player;
        if(WandsMod.is_fabric) {
            wand.player_data = ((IEntityDataSaver) player).getPersistentData();
        }

        if(WandsMod.is_neoforge) {
            wand.player_data= WandsExpectPlatform.getPlayerData(player);
        }
        WandsMod.log("player_data "+wand.player_data.toString(),true);

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
    