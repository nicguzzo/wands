package net.nicguzzo.wands.networking;


import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
#if MC_VERSION >= 12005
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.component.CustomData;
#else
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
#endif
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.compat.Compat;
import net.nicguzzo.compat.MyIdExt;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.utils.WandUtils;
import net.nicguzzo.wands.wand.PlayerWand;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;
import net.nicguzzo.wands.wand.modes.RockMode;
import org.jetbrains.annotations.NotNull;
import net.nicguzzo.wands.WandsMod;

public class Networking {

    static private MyIdExt KB_PACKET = new MyIdExt(WandsMod.MOD_ID,"key_packet");
    static private MyIdExt SND_PACKET = new MyIdExt(WandsMod.MOD_ID,"sound_packet");
    static private MyIdExt TOAST_PACKET = new MyIdExt(WandsMod.MOD_ID,"toast_packet");
    static private MyIdExt PALETTE_PACKET = new MyIdExt(WandsMod.MOD_ID,"palette_packet");
    static private MyIdExt STATE_PACKET = new MyIdExt(WandsMod.MOD_ID,"state_packet");
    static private MyIdExt WAND_PACKET = new MyIdExt(WandsMod.MOD_ID,"wand_packet");
    static private MyIdExt PLAYER_DATA_PACKET = new MyIdExt(WandsMod.MOD_ID,"player_data_packet");
    static private MyIdExt POS_PACKET = new MyIdExt(WandsMod.MOD_ID,"pos_packet");
    static private MyIdExt CONF_PACKET = new MyIdExt(WandsMod.MOD_ID,"conf_packet");
    static private MyIdExt GLOBAL_SETTINGS_PACKET = new MyIdExt(WandsMod.MOD_ID,"global_settings_packet");
    static private MyIdExt SYNC_ROCK_PACKET = new MyIdExt(WandsMod.MOD_ID,"sync_rock_packet");

    public static class Vec3d {
        public double x;
        public double y;
        public double z;

        public Vec3d(double _x, double _y, double _z) {
            x = _x;
            y = _y;
            z = _z;
        }
        #if MC_VERSION >= 12005
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
        #endif
    }
#if MC_VERSION >= 12005
    public record KbPacket(int key, boolean shift, boolean alt) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<KbPacket> TYPE = new CustomPacketPayload.Type<>(KB_PACKET.res);
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
        public static final CustomPacketPayload.Type<SndPacket> TYPE = new CustomPacketPayload.Type<>(SND_PACKET.res);
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
        public static final CustomPacketPayload.Type<ToastPacket> TYPE = new CustomPacketPayload.Type<>(TOAST_PACKET.res);
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
        public static final CustomPacketPayload.Type<PalettePacket> TYPE = new CustomPacketPayload.Type<>(PALETTE_PACKET.res);
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
        public static final CustomPacketPayload.Type<WandPacket> TYPE = new CustomPacketPayload.Type<>(WAND_PACKET.res);
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
        public static final CustomPacketPayload.Type<PlayerDataPacket> TYPE = new CustomPacketPayload.Type<>(PLAYER_DATA_PACKET.res);
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
        public static final CustomPacketPayload.Type<StatePacket> TYPE = new CustomPacketPayload.Type<>(STATE_PACKET.res);
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
        public static final CustomPacketPayload.Type<PosPacket> TYPE = new CustomPacketPayload.Type<>(POS_PACKET.res);
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
        public static final CustomPacketPayload.Type<ConfPacket> TYPE = new CustomPacketPayload.Type<>(CONF_PACKET.res);
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
        public static final CustomPacketPayload.Type<GlobalSettingsPacket> TYPE = new CustomPacketPayload.Type<>(GLOBAL_SETTINGS_PACKET.res);
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
        public static final CustomPacketPayload.Type<SyncRockPacket> TYPE = new CustomPacketPayload.Type<>(SYNC_ROCK_PACKET.res);
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
    #endif

    static public void RegisterS2C(){
        #if MC_VERSION >= 12005
            NetworkManager.registerS2CPayloadType(Networking.ConfPacket.TYPE, Networking.ConfPacket.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(Networking.SndPacket.TYPE, Networking.SndPacket.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(Networking.ToastPacket.TYPE, Networking.ToastPacket.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(Networking.StatePacket.TYPE, Networking.StatePacket.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(Networking.PlayerDataPacket.TYPE, Networking.PlayerDataPacket.STREAM_CODEC);
        #endif
    }

    static public void SendStatePacket(ServerPlayer player, int mode, int slot, boolean xp, int levels, float prog){

        #if MC_VERSION >= 12005
            NetworkManager.sendToPlayer(player, new Networking.StatePacket(mode, slot, xp, levels, prog));
        #else
            FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
            //packet.writeLong(wand.palette.seed);
            packet.writeInt(mode);
            packet.writeInt(slot);
            packet.writeBoolean(xp);
            packet.writeInt(levels);
            packet.writeFloat(prog);
            NetworkManager.sendToPlayer(player, STATE_PACKET.res, packet);
        #endif
    }
    static public void SendConfPacket(ServerPlayer player,float blocks_per_xp, boolean destroy_in_survival_drop, boolean survival_unenchanted_drops, boolean mend_tools) {
        #if MC_VERSION >= 12005
            NetworkManager.sendToPlayer(player, new Networking.ConfPacket(blocks_per_xp, destroy_in_survival_drop, survival_unenchanted_drops, mend_tools));
        #else
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeFloat(blocks_per_xp);
        packet.writeBoolean(destroy_in_survival_drop);
        packet.writeBoolean(survival_unenchanted_drops);
        packet.writeBoolean(mend_tools);
        //packet.writeNbt(wand.player_data);
        NetworkManager.sendToPlayer(player, CONF_PACKET.res, packet);
        #endif
    }
    static public void SendPlayerData(ServerPlayer player,CompoundTag player_data) {
        #if MC_VERSION >= 12005
            NetworkManager.sendToPlayer(player, new Networking.PlayerDataPacket(player_data));
        #else
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeNbt(player_data);
        NetworkManager.sendToPlayer(player, PLAYER_DATA_PACKET.res, packet);
        #endif
    }
    static public void SendGlobalSettings(boolean drop_pos) {
        #if MC_VERSION >= 12005
        NetworkManager.sendToServer(new GlobalSettingsPacket(drop_pos));
        #else
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeBoolean(drop_pos);
        NetworkManager.sendToServer(GLOBAL_SETTINGS_PACKET.res, packet);
        #endif
    }
    static public void RegisterReceivers(){
        #if MC_VERSION >= 12005
        NetworkManager.registerReceiver(Side.C2S, GlobalSettingsPacket.TYPE, GlobalSettingsPacket.STREAM_CODEC, (packet, context) -> {
            Player player = context.getPlayer();
            boolean drop_pos=packet.drop_pos();
        #else
        NetworkManager.registerReceiver(Side.C2S, GLOBAL_SETTINGS_PACKET.res, (packet,context)->{
            boolean drop_pos=packet.readBoolean();
            context.queue(()-> {
                Player player = context.getPlayer();
        #endif
                if (player != null) {
                    Wand wand = PlayerWand.get(player);
                    if (wand != null) {
                        wand.drop_on_player = drop_pos;
                    }
                }
        #if MC_VERSION < 12005
            });
        #endif
        });

        #if MC_VERSION >= 12005
         NetworkManager.registerReceiver(NetworkManager.Side.C2S, Networking.KbPacket.TYPE, Networking.KbPacket.STREAM_CODEC, (data, context) -> {
             WandsMod.process_keys(context.getPlayer(), data.key(), data.shift(), data.alt());
        });
        #else
        NetworkManager.registerReceiver(Side.C2S, KB_PACKET.res, (packet,context)->{
            int key=packet.readInt();
            boolean shift=packet.readBoolean();
            boolean alt=packet.readBoolean();
            context.queue(()->WandsMod.process_keys(context.getPlayer(), key,shift,alt));
        });
        #endif

        #if MC_VERSION >= 12005
        NetworkManager.registerReceiver(Side.C2S, Networking.PalettePacket.TYPE, Networking.PalettePacket.STREAM_CODEC, (data, context) -> {
            WandsMod.process_palette(context.getPlayer(), data.mode(), data.rotate(),data.grad_h());
        });
        #else
        NetworkManager.registerReceiver(Side.C2S, PALETTE_PACKET.res, (packet,context)->{
            boolean mode=packet.readBoolean();
            boolean rotate=packet.readBoolean();
            int grad_h=packet.readInt();
            context.queue(()-> WandsMod.process_palette(context.getPlayer(), mode,rotate,grad_h));
        });
        #endif

        #if MC_VERSION >= 12005
        NetworkManager.registerReceiver(Side.C2S, Networking.WandPacket.TYPE, Networking.WandPacket.STREAM_CODEC, (data, context) -> {
            ItemStack wand_stack = context.getPlayer().getMainHandItem();
            var custom_data=data.item_stack().get(DataComponents.CUSTOM_DATA);
            if(custom_data!=null) {
                CompoundTag tag = custom_data.copyTag();
                CustomData.set(DataComponents.CUSTOM_DATA, wand_stack, tag);
            }
        });
        #else
        NetworkManager.registerReceiver(Side.C2S, WAND_PACKET.res, (packet,context)->{
            ItemStack item=packet.readItem();
            context.queue(()->{
                ItemStack wand_stack=context.getPlayer().getMainHandItem();
                CompoundTag tag=item.getTag();
                if(tag!=null) {
                    wand_stack.setTag(tag);
                }
            });
        });
        #endif

        #if MC_VERSION >= 12005
        NetworkManager.registerReceiver(Side.C2S, Networking.SyncRockPacket.TYPE, Networking.SyncRockPacket.STREAM_CODEC, (data, context) -> {
            int rx=data.rx();
            int ry=data.ry();
            int rz=data.rz();
        #else
        NetworkManager.registerReceiver(Side.C2S, SYNC_ROCK_PACKET.res, (packet,context)->{
            int rx=packet.readInt();
            int ry=packet.readInt();
            int rz=packet.readInt();
            context.queue(()->{
        #endif

            Player player = context.getPlayer();
            if (player == null) {
                WandsMod.LOGGER.error("player is null");
                return;
            }
            ItemStack stack = context.getPlayer().getMainHandItem();
            if (!WandUtils.is_wand(stack)) {
                WandsMod.LOGGER.error("player doesn't have a wand in main hand");
                return;
            }
            Wand wand = PlayerWand.get(player);
            if (wand == null) {
                WandsMod.LOGGER.error("wand is null");
                return;
            }
            if(wand.mode == WandProps.Mode.ROCK) {
                WandMode m=wand.get_mode();
                if( m instanceof RockMode){
                    ((RockMode)m).set_random_pos(rx, ry, rz);
                }
            }
        #if MC_VERSION < 12005
            });
        #endif
        });

        //register PosPacket receviers
#if MC_VERSION >= 12005
        NetworkManager.registerReceiver(Side.C2S, Networking.PosPacket.TYPE, Networking.PosPacket.STREAM_CODEC, (data, context) -> {
#else
        NetworkManager.registerReceiver(Side.C2S, POS_PACKET.res, (packet,context)->{
#endif
            //LOGGER.info("got PosPacket");
            Player player = context.getPlayer();
            if (player == null) {
                WandsMod.LOGGER.error("player is null");
                return;
            }
            Level level = Compat.player_level(player);
            ItemStack stack = context.getPlayer().getMainHandItem();
            if (!WandUtils.is_wand(stack)) {
                WandsMod.LOGGER.error("player doesn't have a wand in main hand");
                return;
            }
            Wand wand = PlayerWand.get(player);
            if (wand == null) {
                WandsMod.LOGGER.error("wand is null");
                return;
            }
            BlockPos p1;
            BlockPos p2;
#if MC_VERSION >= 12005
            int d = data.dir();
            int has_p1_p2 = data.has_p1_p2();

#else
            int d=packet.readInt();
            int has_p1_p2 =packet.readInt();
#endif

            Direction side = Direction.values()[d];
            if (has_p1_p2 == 1) {
#if MC_VERSION >= 12005
                p1 = data.p1();
#else
                p1 = packet.readBlockPos();
#endif
            } else {
                p1 = null;
                //WandsMod.LOGGER.info("needs at least 1 position");
                //return;
            }
            if (has_p1_p2 == 2) {
#if MC_VERSION >= 12005
                p2 = data.p2();
#else
                p2 = packet.readBlockPos();
#endif
            } else {
                p2 = null;
            }
            if (has_p1_p2 == 3) {
#if MC_VERSION >= 12005
                p1 = data.p1();
                p2 = data.p2();
#else
                p1 = packet.readBlockPos();
                p2 = packet.readBlockPos();
#endif
            }
            if (p1 == null) {
                //WandsMod.LOGGER.info("needs at least 1 position");
                return;
            }
            #if MC_VERSION >= 12005
                Vec3 hit = new Vec3(data.hit().x, data.hit().y, data.hit().z);
                long seed = data.seed();
            #else
                double hit_x = packet.readDouble();
                double hit_y = packet.readDouble();
                double hit_z = packet.readDouble();
                Vec3 hit = new Vec3(hit_x, hit_y, hit_z);
                long seed = packet.readLong();
            #endif
            BlockPos finalP1 = p1;
            BlockPos finalP2 = p2;
            #if MC_VERSION < 12005
            context.queue(()-> {
            #endif
                BlockState block_state;
                BlockPos pos;
                // Always prefer P1's block state - in 2-click modes, P1 defines the block type to place
                block_state = level.getBlockState(finalP1);
                pos = finalP1;
                if (block_state.isAir() && finalP2 != null) {
                    block_state = level.getBlockState(finalP2);
                    pos = finalP2;
                }
                if (block_state.isAir()) {
                    block_state = level.getBlockState(finalP1);
                }
                // When include_block is disabled and mode supports it, P1/P2 are offset into air - find adjacent solid block
                WandProps.Mode mode = WandProps.getMode(stack);
                boolean modeSupportsIncSel = WandProps.flagAppliesTo(WandProps.Flag.INCSELBLOCK, mode);
                if (block_state.isAir() && modeSupportsIncSel && !WandProps.getFlag(stack, WandProps.Flag.INCSELBLOCK)) {
                    for (Direction dir : Direction.values()) {
                        BlockState adjacent = level.getBlockState(finalP1.relative(dir));
                        if (!adjacent.isAir()) {
                            block_state = adjacent;
                            break;
                        }
                    }
                }
                wand.setP1(finalP1);
                wand.setP2(finalP2);

                //WandsMod.log(" received_placement palette seed: " + seed,true);
                wand.palette.seed = seed;
                // Sync prevMode before do_or_preview to prevent false mode-change detection
                // (client may have changed modes between placements without server knowing)
                wand.prevMode = mode;
                //wand.lastPlayerDirection=player_dir;
                //WandsMod.LOGGER.info("got_placement p1: "+ wand.getP1() +" p2: "+ wand.getP2() +" pos:"+ pos);
                wand.do_or_preview(player, level, block_state, pos, side, hit, stack, (WandItem) stack.getItem(), true);
                wand.clear(wand.mode == WandProps.Mode.PASTE || wand.mode == WandProps.Mode.COPY || mode== WandProps.Mode.AREA);
            #if MC_VERSION < 12005
            });
            #endif
        });

    }

    static public void RegisterReceiversS2C(){
#if MC_VERSION >= 12005
        NetworkManager.registerReceiver(Side.S2C, Networking.PlayerDataPacket.TYPE, Networking.PlayerDataPacket.STREAM_CODEC, (data, context) -> {
            CompoundTag player_data=data.tag();
#else
        NetworkManager.registerReceiver(Side.S2C, Networking.PLAYER_DATA_PACKET.res, (packet, context) -> {
            CompoundTag player_data=packet.readNbt();
            context.queue(() -> {
#endif
                if (ClientRender.wand != null) {
                    ClientRender.wand.player_data = player_data;
                }
#if MC_VERSION < 12005
            });
#endif
        });

#if MC_VERSION >= 12005
        NetworkManager.registerReceiver(Side.S2C, Networking.ConfPacket.TYPE, Networking.ConfPacket.STREAM_CODEC, (data, context) -> {

            float blocks_per_xp = data.blocks_per_xp();
            boolean destroy_in_survival_drop = data.destroy_in_survival_drop();
            boolean survival_unenchanted_drops = data.survival_unenchanted_drops();
            boolean mend_tools = data.mend_tools();
#else
        NetworkManager.registerReceiver(Side.S2C, Networking.CONF_PACKET.res, (packet, context) -> {
            float blocks_per_xp = packet.readFloat();
            boolean destroy_in_survival_drop = packet.readBoolean();
            boolean survival_unenchanted_drops = packet.readBoolean();
            boolean mend_tools = packet.readBoolean();
#endif
            ServerData srv = Minecraft.getInstance().getCurrentServer();
            if (srv != null && WandsMod.config != null) {
                WandsMod.config.blocks_per_xp = blocks_per_xp;
                WandsMod.config.destroy_in_survival_drop = destroy_in_survival_drop;
                WandsMod.config.survival_unenchanted_drops = survival_unenchanted_drops;
                WandsMod.config.mend_tools = mend_tools;
            }
        });

    #if MC_VERSION >= 12005
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, Networking.SndPacket.TYPE, Networking.SndPacket.STREAM_CODEC, (data, context) -> {
            BlockPos pos = data.pos();
            boolean destroy = data.destroy();
            ItemStack item_stack = data.item_stack();
            int i_sound = data.i_sound();
    #else
        NetworkManager.registerReceiver(Side.S2C, Networking.SND_PACKET.res, (packet, context) -> {
            BlockPos pos = packet.readBlockPos();
            boolean destroy = packet.readBoolean();
            ItemStack item_stack = packet.readItem();
            int i_sound = packet.readInt();
            context.queue(() -> {
    #endif

                if (i_sound > -1 && i_sound < Wand.Sounds.values().length) {
                    Wand.Sounds snd = Wand.Sounds.values()[i_sound];
                    SoundEvent sound = snd.get_sound();
                    Compat.player_level(context.getPlayer()).playSound(context.getPlayer(), pos, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
                } else {
                    if (!item_stack.isEmpty()) {
                        Block block = Block.byItem(item_stack.getItem());
                        BlockState bs = block.defaultBlockState();
                        SoundType sound_type = bs.getSoundType();
                        //SoundType sound_type = ((BlockBehaviourInvoker)block).invokeGetSoundType(block.defaultBlockState());
                        SoundEvent sound = (destroy ? sound_type.getBreakSound() : sound_type.getPlaceSound());
                        Compat.player_level(context.getPlayer()).playSound(context.getPlayer(), pos, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
                    }
                }
#if MC_VERSION < 12005
            });
#endif
        });
#if MC_VERSION >= 12005
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, Networking.ToastPacket.TYPE, Networking.ToastPacket.STREAM_CODEC, (data, context) -> {
            boolean no_tool = data.no_tool();
            boolean damaged_tool = data.damaged_tool();
            String needed_tool = data.needed_tool();
#else
        NetworkManager.registerReceiver(Side.S2C, Networking.TOAST_PACKET.res, (packet, context) -> {
            boolean no_tool = packet.readBoolean();
            boolean damaged_tool = packet.readBoolean();
            String needed_tool = packet.readUtf();
            context.queue(() -> {
#endif
                Player player = context.getPlayer();
                String toolsKey = "Shift+" + WandsModClient.getKeyName(WandsMod.WandKeys.MENU);
                if (no_tool) {
                    player.displayClientMessage(Compat.translatable("wands.message.no_tool", toolsKey), true);
                }
                if (damaged_tool) {
                    player.displayClientMessage(Compat.translatable("wands.message.damaged_tool", toolsKey), true);
                }
                if (!needed_tool.isEmpty()) {
                    player.displayClientMessage(Compat.translatable("wands.message.wrong_tool", needed_tool, toolsKey), true);
                }
#if MC_VERSION < 12005
            });
#endif
        });
    #if MC_VERSION >= 12005
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, Networking.StatePacket.TYPE, Networking.StatePacket.STREAM_CODEC, (data, context) -> {
            int mode = data.mode();
            int slot = data.slot();
            boolean xp = data.xp();
            int levels = data.levels();
            float prog = data.prog();
    #else
        NetworkManager.registerReceiver(Side.S2C, Networking.STATE_PACKET.res, (packet, context) -> {
            int mode = packet.readInt();
            int slot = packet.readInt();
            boolean xp = packet.readBoolean();
            int levels = packet.readInt();
            float prog = packet.readFloat();
            context.queue(() -> {
    #endif
                if (ClientRender.wand != null) {
                    ClientRender.wand.mode = WandProps.Mode.values()[mode];
                    if (ClientRender.wand.mode == WandProps.Mode.DIRECTION)
                        ClientRender.wand.palette.slot = slot;
                    if (xp) {
                        context.getPlayer().experienceLevel = levels;
                        context.getPlayer().experienceProgress = prog;
                    }
                }
#if MC_VERSION < 12005
            });
#endif
        });
    }
    static public void sendSndPacket(ServerPlayer player,BlockPos pos,boolean destroy,ItemStack is,int send_sound ) {
#if MC_VERSION >= 12005
        NetworkManager.sendToPlayer(player,
                new Networking.SndPacket(pos, destroy, is, send_sound)
        );
#else
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeBlockPos(pos);
        packet.writeBoolean(destroy);
        packet.writeItem(is);
        packet.writeInt(send_sound);
        NetworkManager.sendToPlayer( player, Networking.SND_PACKET.res, packet);
#endif
    }
    static public void sendToastPacket(ServerPlayer player,boolean no_tool,boolean damaged_tool,String needed_tool ) {
#if MC_VERSION >= 12005
        NetworkManager.sendToPlayer(player,
                new Networking.ToastPacket(no_tool, damaged_tool, needed_tool)
        );
#else
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeBoolean(no_tool);
        packet.writeBoolean(damaged_tool);
        packet.writeUtf(needed_tool);
        NetworkManager.sendToPlayer( player, Networking.TOAST_PACKET.res, packet);
#endif
    }

    public static void send_key(int key, boolean shift, boolean alt) {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() != null) {
#if MC_VERSION >= 12005
            NetworkManager.sendToServer(new Networking.KbPacket(key, shift, alt));
#else
            FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
            packet.writeInt(key);
            packet.writeBoolean(shift);
            packet.writeBoolean(alt);
            NetworkManager.sendToServer(Networking.KB_PACKET.res, packet);
#endif
        }
    }

    public static void send_palette(boolean next_mode, boolean toggle_rotate, int grad_h) {
#if MC_VERSION >= 12005
        NetworkManager.sendToServer(new Networking.PalettePacket(next_mode, toggle_rotate, grad_h));
#else
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeBoolean(next_mode);
        packet.writeBoolean(toggle_rotate);
        packet.writeInt(grad_h);
        NetworkManager.sendToServer(Networking.PALETTE_PACKET.res, packet);
#endif
    }

    public static void send_wand(ItemStack item) {
#if MC_VERSION >= 12005
        NetworkManager.sendToServer(new Networking.WandPacket(item));
#else
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeItem(item);
        NetworkManager.sendToServer(Networking.WAND_PACKET.res, packet);
#endif
    }
    public static void sendSyncRockPacket(int x,int y,int z) {
       #if MC_VERSION >= 12005
         NetworkManager.sendToServer(new SyncRockPacket(x, y, z));
       #else
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeInt(x);
        packet.writeInt(y);
        packet.writeInt(z);
        NetworkManager.sendToServer(Networking.SYNC_ROCK_PACKET.res, packet);
       #endif
    }

    public static void sendPosPacket(Direction side, int has_p1_p2,BlockPos p1, BlockPos p2, Vec3 hit, long seed) {
       #if MC_VERSION >= 12005
         NetworkManager.sendToServer(new Networking.PosPacket(side.ordinal(), has_p1_p2, p1, p2, new Networking.Vec3d(hit.x, hit.y, hit.z), seed));
       #else
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeInt(side.ordinal());
        packet.writeInt(has_p1_p2);
        if(has_p1_p2==1){
            packet.writeBlockPos(p1);
        }
        if(has_p1_p2==2){
            packet.writeBlockPos(p2);
        }
        if(has_p1_p2==3){
            packet.writeBlockPos(p1);
            packet.writeBlockPos(p2);
        }
        packet.writeDouble(hit.x);
        packet.writeDouble(hit.y);
        packet.writeDouble(hit.z);
        packet.writeLong(seed);
        NetworkManager.sendToServer(Networking.POS_PACKET.res, packet);
       #endif
    }

}
