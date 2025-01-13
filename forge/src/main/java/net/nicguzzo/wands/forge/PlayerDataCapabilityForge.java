package net.nicguzzo.wands.forge;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nicguzzo.wands.WandsMod;

import java.util.function.Supplier;

public class PlayerDataCapabilityForge {
    @CapabilityInject(PlayerDataCapability.IPlayerData.class)
    public static Capability<PlayerDataCapability.IPlayerData> PLAYER_DATA_CAPABILITY = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(PlayerDataCapability.IPlayerData.class, new Capability.IStorage<PlayerDataCapability.IPlayerData>() {
            @Override
            public CompoundTag writeNBT(Capability<PlayerDataCapability.IPlayerData> capability, PlayerDataCapability.IPlayerData instance, Direction side) {
                return instance.getCustomData();
            }


            @Override
            public void readNBT(Capability<PlayerDataCapability.IPlayerData> capability, PlayerDataCapability.IPlayerData instance, Direction side, Tag nbt) {
                instance.setCustomData((CompoundTag) nbt);
            }
        }, PlayerDataCapability.PlayerData::new);
    }

    public static Supplier<PlayerDataCapability.IPlayerData> createPlayerData() {
        return PlayerDataCapability.PlayerData::new;
    }

    @Mod.EventBusSubscriber(modid = WandsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class EventHandler {
        @SubscribeEvent
        public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player) {
                event.addCapability(PlayerDataCapability.ID, new ICapabilitySerializable<CompoundTag>() {
                    private final PlayerDataCapability.IPlayerData instance = PlayerDataCapabilityForge.createPlayerData().get();
                    private final LazyOptional<PlayerDataCapability.IPlayerData> holder = LazyOptional.of(() -> instance);

                    @Override
                    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
                        return PLAYER_DATA_CAPABILITY != null ? PLAYER_DATA_CAPABILITY.orEmpty(cap, holder) : LazyOptional.empty();
                    }

                    @Override
                    public CompoundTag serializeNBT() {
                        return instance.getCustomData();
                    }

                    @Override
                    public void deserializeNBT(CompoundTag nbt) {
                        instance.setCustomData(nbt);
                    }
                });
            }
        }
    }
}
