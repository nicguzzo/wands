package net.nicguzzo.wands.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
//? if >= 1.20.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.component.CustomData;
//? } else {

/*import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;

*///? }
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.compat.RcId;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.utils.WandUtils;
import net.nicguzzo.wands.wand.PlayerWand;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;
import net.nicguzzo.wands.wand.modes.RockMode;
import net.nicguzzo.wands.WandsMod;

import org.jetbrains.annotations.NotNull;

public class Networking {

    static private RcId KB_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID,"key_packet");
    static private RcId SND_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID,"sound_packet");
    static private RcId TOAST_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID,"toast_packet");
    static private RcId PALETTE_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID,"palette_packet");
    static private RcId STATE_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID,"state_packet");
    static private RcId WAND_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID,"wand_packet");
    static private RcId PLAYER_DATA_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID,"player_data_packet");
    static private RcId POS_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID,"pos_packet");
    static private RcId CONF_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID,"conf_packet");
    static private RcId GLOBAL_SETTINGS_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID,"global_settings_packet");
    static private RcId SYNC_ROCK_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID,"sync_rock_packet");

    public static class Vec3d {
        public double x;
        public double y;
        public double z;

        public Vec3d(double _x, double _y, double _z) {
            x = _x;
            y = _y;
            z = _z;
        }
        //? if >= 1.20.5 {
        public static final StreamCodec<ByteBuf, Vec3d> STREAM_CODEC;

        static {
            STREAM_CODEC = new StreamCodec<ByteBuf, Vec3d>() {
                public @NotNull Vec3d decode(ByteBuf byteBuf) {
                    double x = byteBuf.readDouble();
                    double y = byteBuf.readDouble();
                    double z = byteBuf.readDouble();
                    return new Vec3d(x, y, z);
                }

                public void encode(ByteBuf byteBuf, Vec3d v) {
                    byteBuf.writeDouble(v.x);
                    byteBuf.writeDouble(v.y);
                    byteBuf.writeDouble(v.z);
                }
            };
        }
        //? }
    }
//? if >= 1.20.5 {
    public record KbPacket(int key, boolean shift, boolean alt) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<KbPacket> TYPE = new CustomPacketPayload.Type<>(KB_PACKET.id());
        public static final StreamCodec<ByteBuf, KbPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                KbPacket::key,
                ByteBufCodecs.BOOL,
                KbPacket::shift,
                ByteBufCodecs.BOOL,
                KbPacket::alt,
                KbPacket::new
        );

        @Override
        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }


    public record SndPacket(BlockPos pos, boolean destroy, ItemStack item_stack,
                            int i_sound) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SndPacket> TYPE = new CustomPacketPayload.Type<>(SND_PACKET.id());
        public static final StreamCodec<RegistryFriendlyByteBuf, SndPacket> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC,
                SndPacket::pos,
                ByteBufCodecs.BOOL,
                SndPacket::destroy,
                ItemStack.STREAM_CODEC,
                SndPacket::item_stack,
                ByteBufCodecs.VAR_INT,
                SndPacket::i_sound,
                SndPacket::new
        );

        @Override
        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ToastPacket(boolean no_tool, boolean damaged_tool, String needed_tool) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ToastPacket> TYPE = new CustomPacketPayload.Type<>(TOAST_PACKET.id());
        public static final StreamCodec<RegistryFriendlyByteBuf, ToastPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL,
                ToastPacket::no_tool,
                ByteBufCodecs.BOOL,
                ToastPacket::damaged_tool,
                ByteBufCodecs.STRING_UTF8,
                ToastPacket::needed_tool,
                ToastPacket::new
        );

        @Override
        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record PalettePacket(boolean mode, boolean rotate,int grad_h) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PalettePacket> TYPE = new CustomPacketPayload.Type<>(PALETTE_PACKET.id());
        public static final StreamCodec<ByteBuf, PalettePacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL,
                PalettePacket::mode,
                ByteBufCodecs.BOOL,
                PalettePacket::rotate,
                ByteBufCodecs.VAR_INT,
                PalettePacket::grad_h,
                PalettePacket::new
        );

        @Override
        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record WandPacket(ItemStack item_stack) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<WandPacket> TYPE = new CustomPacketPayload.Type<>(WAND_PACKET.id());
        public static final StreamCodec<RegistryFriendlyByteBuf, WandPacket> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC,
                WandPacket::item_stack,
                WandPacket::new
        );

        @Override
        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record PlayerDataPacket(CompoundTag tag) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PlayerDataPacket> TYPE = new CustomPacketPayload.Type<>(PLAYER_DATA_PACKET.id());
        public static final StreamCodec<RegistryFriendlyByteBuf, PlayerDataPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.fromCodec(CompoundTag.CODEC),
                PlayerDataPacket::tag,
                PlayerDataPacket::new
        );

        @Override
        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record StatePacket(int mode, int slot, boolean xp, int levels, float prog) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<StatePacket> TYPE = new CustomPacketPayload.Type<>(STATE_PACKET.id());
        public static final StreamCodec<ByteBuf, StatePacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                StatePacket::mode,
                ByteBufCodecs.VAR_INT,
                StatePacket::slot,
                ByteBufCodecs.BOOL,
                StatePacket::xp,
                ByteBufCodecs.VAR_INT,
                StatePacket::levels,
                ByteBufCodecs.FLOAT,
                StatePacket::prog,
                StatePacket::new
        );

        @Override
        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record PosPacket(
            int dir,
            int has_p1_p2, // 1 p1 2 p2
            BlockPos p1,
            BlockPos p2,
            Vec3d hit,
            long seed
    ) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PosPacket> TYPE = new CustomPacketPayload.Type<>(POS_PACKET.id());
        public static final StreamCodec<RegistryFriendlyByteBuf, PosPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                PosPacket::dir,
                ByteBufCodecs.VAR_INT,
                PosPacket::has_p1_p2,
                BlockPos.STREAM_CODEC,
                PosPacket::p1,
                BlockPos.STREAM_CODEC,
                PosPacket::p2,
                Vec3d.STREAM_CODEC,
                PosPacket::hit,
                ByteBufCodecs.VAR_LONG,
                PosPacket::seed,
                PosPacket::new
        );

        @Override
        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ConfPacket(
            float blocks_per_xp,
            boolean destroy_in_survival_drop,
            boolean survival_unenchanted_drops,
            boolean mend_tools
    ) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ConfPacket> TYPE = new CustomPacketPayload.Type<>(CONF_PACKET.id());
        public static final StreamCodec<RegistryFriendlyByteBuf, ConfPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT,
                ConfPacket::blocks_per_xp,
                ByteBufCodecs.BOOL,
                ConfPacket::destroy_in_survival_drop,
                ByteBufCodecs.BOOL,
                ConfPacket::survival_unenchanted_drops,
                ByteBufCodecs.BOOL,
                ConfPacket::mend_tools,
                ConfPacket::new
        );

        @Override
        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record GlobalSettingsPacket(boolean drop_pos) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<GlobalSettingsPacket> TYPE = new CustomPacketPayload.Type<>(GLOBAL_SETTINGS_PACKET.id());
        public static final StreamCodec<ByteBuf, GlobalSettingsPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL,
                GlobalSettingsPacket::drop_pos,
                GlobalSettingsPacket::new
        );

        @Override
        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SyncRockPacket(int rx,int ry,int rz) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncRockPacket> TYPE = new CustomPacketPayload.Type<>(SYNC_ROCK_PACKET.id());
        public static final StreamCodec<ByteBuf, SyncRockPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                SyncRockPacket::rx,
                ByteBufCodecs.VAR_INT,
                SyncRockPacket::ry,
                ByteBufCodecs.VAR_INT,
                SyncRockPacket::rz,
                SyncRockPacket::new
        );

        @Override
        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    //? }

    static public void RegisterS2C(){
        // if >= 1.20.5 {

             //NetworkManager.registerS2CPayloadType(Networking.ConfPacket.TYPE, Networking.ConfPacket.STREAM_CODEC);
             //NetworkManager.registerS2CPayloadType(Networking.SndPacket.TYPE, Networking.SndPacket.STREAM_CODEC);
             //NetworkManager.registerS2CPayloadType(Networking.ToastPacket.TYPE, Networking.ToastPacket.STREAM_CODEC);
             //NetworkManager.registerS2CPayloadType(Networking.StatePacket.TYPE, Networking.StatePacket.STREAM_CODEC);
             //NetworkManager.registerS2CPayloadType(Networking.PlayerDataPacket.TYPE, Networking.PlayerDataPacket.STREAM_CODEC);
        // }
    }

    static public void SendStatePacket(ServerPlayer player, int mode, int slot, boolean xp, int levels, float prog){

    }
    static public void SendConfPacket(ServerPlayer player,float blocks_per_xp, boolean destroy_in_survival_drop, boolean survival_unenchanted_drops, boolean mend_tools) {
    }
    static public void SendPlayerData(ServerPlayer player,CompoundTag player_data) {
    }
    static public void SendGlobalSettings(boolean drop_pos) {
    }
    static public void RegisterReceivers(){

    }

    static public void RegisterReceiversS2C(){

    }
    static public void sendSndPacket(ServerPlayer player,BlockPos pos,boolean destroy,ItemStack is,int send_sound ) {
    }
    static public void sendToastPacket(ServerPlayer player,boolean no_tool,boolean damaged_tool,String needed_tool ) {
    }

    public static void send_key(int key, boolean shift, boolean alt) {
    }

    public static void send_palette(boolean next_mode, boolean toggle_rotate, int grad_h) {

    }

    public static void send_wand(ItemStack item) {

    }
    public static void sendSyncRockPacket(int x,int y,int z) {
    }

    public static void sendPosPacket(Direction side, int has_p1_p2,BlockPos p1, BlockPos p2, Vec3 hit, long seed) {
    }

    static public void send_placement(Direction side, BlockPos p1, BlockPos p2, Vec3 hit, long seed) {
    }

}
