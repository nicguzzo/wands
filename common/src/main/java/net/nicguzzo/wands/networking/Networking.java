package net.nicguzzo.wands.networking;


import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.utils.Compat;
import org.jetbrains.annotations.NotNull;

public class Networking {

    static public Identifier KB_PACKET = Compat.create_resource("key_packet");
    static public Identifier SND_PACKET = Compat.create_resource("sound_packet");
    static public Identifier TOAST_PACKET = Compat.create_resource("toast_packet");
    static public Identifier PALETTE_PACKET = Compat.create_resource("palette_packet");
    static public Identifier STATE_PACKET = Compat.create_resource("state_packet");
    static public Identifier WAND_PACKET = Compat.create_resource("wand_packet");
    static public Identifier PLAYER_DATA_PACKET = Compat.create_resource("player_data_packet");
    static public Identifier POS_PACKET = Compat.create_resource("pos_packet");
    static public Identifier CONF_PACKET = Compat.create_resource("conf_packet");
    static public Identifier GLOBAL_SETTINGS_PACKET = Compat.create_resource("global_settings_packet");
    static public Identifier SYNC_ROCK_PACKET = Compat.create_resource("sync_rock_packet");

    public static class Vec3d {
        public double x;
        public double y;
        public double z;

        public Vec3d(double _x, double _y, double _z) {
            x = _x;
            y = _y;
            z = _z;
        }

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
    }

    public record KbPacket(int key, boolean shift, boolean alt) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<KbPacket> TYPE = new CustomPacketPayload.Type<>(KB_PACKET);
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
        public static final CustomPacketPayload.Type<SndPacket> TYPE = new CustomPacketPayload.Type<>(SND_PACKET);
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
        public static final CustomPacketPayload.Type<ToastPacket> TYPE = new CustomPacketPayload.Type<>(TOAST_PACKET);
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
        public static final CustomPacketPayload.Type<PalettePacket> TYPE = new CustomPacketPayload.Type<>(PALETTE_PACKET);
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
        public static final CustomPacketPayload.Type<WandPacket> TYPE = new CustomPacketPayload.Type<>(WAND_PACKET);
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
        public static final CustomPacketPayload.Type<PlayerDataPacket> TYPE = new CustomPacketPayload.Type<>(PLAYER_DATA_PACKET);
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
        public static final CustomPacketPayload.Type<StatePacket> TYPE = new CustomPacketPayload.Type<>(STATE_PACKET);
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
        public static final CustomPacketPayload.Type<PosPacket> TYPE = new CustomPacketPayload.Type<>(POS_PACKET);
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
            //boolean allow_wand_to_break,
            //boolean allow_offhand_to_break,
            boolean mend_tools
    ) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ConfPacket> TYPE = new CustomPacketPayload.Type<>(CONF_PACKET);
        public static final StreamCodec<RegistryFriendlyByteBuf, ConfPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT,
                ConfPacket::blocks_per_xp,
                ByteBufCodecs.BOOL,
                ConfPacket::destroy_in_survival_drop,
                ByteBufCodecs.BOOL,
                ConfPacket::survival_unenchanted_drops,
                ByteBufCodecs.BOOL,
//                ConfPacket::allow_wand_to_break,
//                ByteBufCodecs.BOOL,
//                ConfPacket::allow_offhand_to_break,
//                ByteBufCodecs.BOOL,
                ConfPacket::mend_tools,
                ConfPacket::new
        );

        @Override
        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record GlobalSettingsPacket(boolean drop_pos) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<GlobalSettingsPacket> TYPE = new CustomPacketPayload.Type<>(GLOBAL_SETTINGS_PACKET);
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
        public static final CustomPacketPayload.Type<SyncRockPacket> TYPE = new CustomPacketPayload.Type<>(SYNC_ROCK_PACKET);
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
}
