package net.nicguzzo.wands.loaders.fabric.mixin;

//?if >=1.21.11 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.utils.IEntityDataSaver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Optional;

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

    //?if >=1.21.11 {
    @Inject(method = "addAdditionalSaveData", at = @At(value = "HEAD"))
    public void addAdditionalSaveData(ValueOutput valueOutput, CallbackInfo cb){
        if(wandPlayerData !=null){
            valueOutput.storeNullable("wandPlayerData",CompoundTag.CODEC,wandPlayerData);
        }
    }
    @Inject(method = "readAdditionalSaveData", at = @At(value = "HEAD"))
    public void readAdditionalSaveData(ValueInput valueInput, CallbackInfo cb) {
        Optional<CompoundTag> tag= valueInput.read("wandPlayerData",CompoundTag.CODEC);
        tag.ifPresent(compoundTag -> wandPlayerData = compoundTag);

    }
    //?}else{
    
    /*@Inject(method = "addAdditionalSaveData", at = @At(value = "HEAD"))
    public void addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo cb){
        if(wandPlayerData !=null){
            compoundTag.put("wands.wand_data", wandPlayerData);
        }
    }
    @Inject(method = "readAdditionalSaveData", at = @At(value = "HEAD"))
    public void readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo cb) {

        if(compoundTag.contains("wands.wand_data", Compat.NbtType.COMPOUND)){
            wandPlayerData =compoundTag.getCompound("wands.wand_data");
        }
    }
    
    *///?}



}
