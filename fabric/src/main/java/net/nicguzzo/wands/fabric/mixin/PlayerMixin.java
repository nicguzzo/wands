package net.nicguzzo.wands.fabric.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.utils.IEntityDataSaver;
import net.nicguzzo.wands.utils.WandUtils;
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

        //if(tag.isPresent() &&  tag.get().getCompound("wands.wand_data").isPresent()){
        //    wandPlayerData =tag.get().getCompound("wands.wand_data").get();
        //}
    }
}
