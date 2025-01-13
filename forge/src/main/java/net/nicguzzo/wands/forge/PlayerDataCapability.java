package net.nicguzzo.wands.forge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class PlayerDataCapability {
    public static final ResourceLocation ID = new ResourceLocation("yourmodid", "player_data"); // Replace "yourmodid"

    public interface IPlayerData {
        CompoundTag getCustomData();
        void setCustomData(CompoundTag tag);
    }

    public static class PlayerData implements IPlayerData {
        private CompoundTag customData = new CompoundTag();

        @Override
        public CompoundTag getCustomData() {
            return customData;
        }

        @Override
        public void setCustomData(CompoundTag tag) {
            this.customData = tag;
        }
    }

    //@ExpectPlatform
    //public static Supplier<IPlayerData> createPlayerData() {
    //    throw new AssertionError(); // Should never be called directly
    //}
}