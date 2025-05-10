package net.nicguzzo.wands.fabric.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.utils.IEntityDataSaver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin implements IEntityDataSaver {
    @Unique
    private CompoundTag wandPlayerData;

    public CompoundTag getPersistentData(){
        if(wandPlayerData==null){
            wandPlayerData=new CompoundTag();
        }
        return wandPlayerData;
    }

    @Inject(method = "addAdditionalSaveData", at = @At(value = "HEAD"))
    public void addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo cb){
        if(wandPlayerData !=null){
            compoundTag.put("wands.wand_data", wandPlayerData);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At(value = "HEAD"))
    public void readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo cb) {
        if(compoundTag.getCompound("wands.wand_data").isPresent()){
            wandPlayerData =compoundTag.getCompound("wands.wand_data").get();
        }
    }
}
