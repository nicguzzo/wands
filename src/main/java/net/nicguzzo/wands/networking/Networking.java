package net.nicguzzo.wands.networking;



import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.minecraft.nbt.CompoundTag;

import net.minecraft.server.level.ServerPlayer;
//? if >= 1.20.5 {
    //?if fabric {
    import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
    //?}
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//? } else {
/*import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
*///? }
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.compat.RcId;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.utils.WandUtils;
import net.nicguzzo.wands.wand.PlayerWand;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandMode;
import net.nicguzzo.wands.wand.WandProps;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.wand.modes.RockMode;
import org.jetbrains.annotations.NotNull;
//?if fabric {
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//?}

public class Networking {


    //C2S
    static public RcId KB_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID, "key_packet");
    static public RcId PALETTE_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID, "palette_packet");
    static public RcId WAND_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID, "wand_packet");
    static public RcId POS_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID, "pos_packet");
    static public RcId SYNC_ROCK_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID, "sync_rock_packet");

    //S2C
    static public RcId SND_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID, "sound_packet");
    static public RcId TOAST_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID, "toast_packet");
    static public RcId CONF_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID, "conf_packet");
    static public RcId GLOBAL_SETTINGS_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID, "global_settings_packet");
    static public RcId PLAYER_DATA_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID, "player_data_packet");
    static public RcId STATE_PACKET = RcId.fromNamespaceAndPath(WandsMod.MOD_ID, "state_packet");

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
        public Type<? extends CustomPacketPayload> type() {
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
        public Type<? extends CustomPacketPayload> type() {
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
        public Type<? extends CustomPacketPayload> type() {
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
        public Type<? extends CustomPacketPayload> type() {
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
        public Type<? extends CustomPacketPayload> type() {
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
        public Type<? extends CustomPacketPayload> type() {
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
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record PosPacket(
            int dir,
            boolean has_p2,
            BlockPos p1,
            BlockPos p2,
            Vec3d hit,
            long seed
    ) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PosPacket> TYPE = new CustomPacketPayload.Type<>(POS_PACKET.id());
        public static final StreamCodec<RegistryFriendlyByteBuf, PosPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                PosPacket::dir,
                ByteBufCodecs.BOOL,
                PosPacket::has_p2,
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
        public Type<? extends CustomPacketPayload> type() {
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
        public Type<? extends CustomPacketPayload> type() {
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
        public Type<? extends CustomPacketPayload> type() {
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
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    //? }

    static public void SendStatePacket(ServerPlayer player, int mode, int slot, boolean xp, int levels, float prog) {
        //? if >= 1.20.5 {
            //?if fabric {
            ServerPlayNetworking.send(player,new Networking.StatePacket(mode,slot,xp,levels,prog));
            //?}
        //?}else{
        /*FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeInt(mode);
        packet.writeInt(slot);
        packet.writeBoolean(xp);
        packet.writeInt(levels);
        packet.writeFloat(prog);
            //?if fabric {
            /^ServerPlayNetworking.send(player, STATE_PACKET.id(), packet);
            ^///?}
            //?if forge {
            /^Networking.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new Networking.WandsPacket(Networking.STATE_PACKET.id(), packet));
            ^///?}
        *///? }
    }

    static public void SendConfPacket(ServerPlayer player,float blocks_per_xp, boolean destroy_in_survival_drop, boolean survival_unenchanted_drops, boolean mend_tools) {
        //? if >= 1.20.5 {
            //?if fabric {
            ServerPlayNetworking.send(player,new Networking.ConfPacket(blocks_per_xp,destroy_in_survival_drop,survival_unenchanted_drops,mend_tools));
            //?}
        //?}else{
        /*FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeFloat(blocks_per_xp);
        packet.writeBoolean(destroy_in_survival_drop);
        packet.writeBoolean(survival_unenchanted_drops);
        packet.writeBoolean(mend_tools);
            //?if fabric {
            /^ServerPlayNetworking.send(player, CONF_PACKET.id(), packet);
            ^///?}
            //?if forge {
            /^Networking.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new Networking.WandsPacket(Networking.CONF_PACKET.id(), packet));
            ^///?}
        *///? }
    }

    static public void SendPlayerData(ServerPlayer player,CompoundTag player_data) {
        //? if >= 1.20.5 {
            //?if fabric {
            ServerPlayNetworking.send(player,new Networking.PlayerDataPacket(player_data));
            //?}
        //?}else{
        /*FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeNbt(player_data);
            //?if fabric {
            /^ServerPlayNetworking.send(player, PLAYER_DATA_PACKET.id(), packet);
            ^///? }
            //?if forge {
            /^Networking.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new Networking.WandsPacket(Networking.PLAYER_DATA_PACKET.id(), packet));
            ^///?}
        *///? }
    }

    static public void SendSndPacket(ServerPlayer player, BlockPos pos, boolean destroy, ItemStack is, int send_sound ) {
        //? if >= 1.20.5 {
            //?if fabric {
            ServerPlayNetworking.send(player,new Networking.SndPacket(pos,destroy,is,send_sound));
            //?}
        //?}else{
        /*FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeBlockPos(pos);
        packet.writeBoolean(destroy);
        packet.writeItem(is);
        packet.writeInt(send_sound);
            //?if fabric {
            /^ServerPlayNetworking.send( player, Networking.SND_PACKET.id(), packet);
            ^///?}
            //?if forge {
            /^Networking.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new Networking.WandsPacket(Networking.SND_PACKET.id(), packet));
            ^///?}
        *///? }
    }

    static public void SendToastPacket(ServerPlayer player, boolean no_tool, boolean damaged_tool, String needed_tool ) {
        //? if >= 1.20.5 {
            //? if fabric {
                ServerPlayNetworking.send(player,new Networking.ToastPacket(no_tool,damaged_tool,needed_tool));
            //?}
        //?}else{
        /*FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeBoolean(no_tool);
        packet.writeBoolean(damaged_tool);
        packet.writeUtf(needed_tool);
            //?if fabric {
            /^ServerPlayNetworking.send( player, Networking.TOAST_PACKET.id(), packet);
            ^///?}
            //?if forge {
            /^Networking.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new Networking.WandsPacket(Networking.TOAST_PACKET.id(), packet));
            ^///?}
        *///? }
    }

    public static void ReceivePalette(Player player, boolean next_mode, boolean toggle_rotate, int grad_h) {
        WandsMod.process_palette(player, next_mode,toggle_rotate,grad_h);
    }

    public static void ReceivePosPacket(ServerPlayer player, Direction side,boolean has_p1,boolean has_p2,BlockPos p1, BlockPos p2, Vec3 hit, long seed) {

        if (player == null) {
            WandsMod.LOGGER.error("player is null");
            return;
        }
        Level level = Compat.player_level(player);
        ItemStack stack = player.getMainHandItem();
        if (!WandUtils.is_wand(stack)) {
            WandsMod.LOGGER.error("player doesn't have a wand in main hand");
            return;
        }
        Wand wand = PlayerWand.get(player);
        if (wand == null) {
            WandsMod.LOGGER.error("wand is null");
            return;
        }

        if (!has_p1) {
            //WandsMod.LOGGER.info("needs at least 1 position");
            return;
        }
        BlockState block_state;
        BlockPos pos;
        // Always prefer P1's block state - in 2-click modes, P1 defines the block type to place
        block_state = level.getBlockState(p1);
        pos = p1;
        if (block_state.isAir()  && has_p2 &&  p2 != null) {
            block_state = level.getBlockState(p2);
            pos = p2;
        }
        if (block_state.isAir()) {
            block_state = level.getBlockState(p1);
        }
        // When include_block is disabled and mode supports it, P1/P2 are offset into air - find adjacent solid block
        WandProps.Mode mode = WandProps.getMode(stack);
        boolean modeSupportsIncSel = WandProps.flagAppliesTo(WandProps.Flag.INCSELBLOCK, mode);
        if (block_state.isAir() && modeSupportsIncSel && !WandProps.getFlag(stack, WandProps.Flag.INCSELBLOCK)) {
            for (Direction dir : Direction.values()) {
                BlockState adjacent = level.getBlockState(p1.relative(dir));
                if (!adjacent.isAir()) {
                    block_state = adjacent;
                    break;
                }
            }
        }
        wand.setP1(p1);
        if(has_p2){
            wand.setP2(p2);
        }else{
            wand.setP2(null);
        }
        //WandsMod.log(" received_placement palette seed: " + seed,true);
        wand.palette.seed = seed;
        // Sync prevMode before do_or_preview to prevent false mode-change detection
        // (client may have changed modes between placements without server knowing)
        wand.prevMode = mode;
        //WandsMod.LOGGER.info("got_placement p1: "+ wand.getP1() +" p2: "+ wand.getP2() +" pos:"+ pos);
        wand.do_or_preview(player, level, block_state, pos, side, hit, stack, (WandItem) stack.getItem(), true);
        wand.clear(wand.mode == WandProps.Mode.PASTE || wand.mode == WandProps.Mode.COPY || mode== WandProps.Mode.AREA || mode== WandProps.Mode.VEIN);
    }

    public static void ReceiveSyncRockPacket(Player player,int x,int y,int z) {

        if (player == null) {
            WandsMod.LOGGER.error("player is null");
            return;
        }
        ItemStack stack = player.getMainHandItem();
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
                ((RockMode)m).set_random_pos(x, y, z);
            }
        }
    }

    public static void RegisterS2C() {
        //?if >= 1.20.5 {
            //?if fabric {
            PayloadTypeRegistry.clientboundPlay().register(Networking.ConfPacket.TYPE, Networking.ConfPacket.STREAM_CODEC);
            PayloadTypeRegistry.clientboundPlay().register(Networking.SndPacket.TYPE, Networking.SndPacket.STREAM_CODEC);
            PayloadTypeRegistry.clientboundPlay().register(Networking.ToastPacket.TYPE, Networking.ToastPacket.STREAM_CODEC);
            PayloadTypeRegistry.clientboundPlay().register(Networking.StatePacket.TYPE, Networking.StatePacket.STREAM_CODEC);
            PayloadTypeRegistry.clientboundPlay().register(Networking.PlayerDataPacket.TYPE, Networking.PlayerDataPacket.STREAM_CODEC);
            //?}
        //?}else{

        //?}
    }
    public static void RegisterC2S(){
        //?if >= 1.20.5 {
            //?if fabric {
                PayloadTypeRegistry.serverboundPlay().register(Networking.SyncRockPacket.TYPE, Networking.SyncRockPacket.STREAM_CODEC);
                PayloadTypeRegistry.serverboundPlay().register(Networking.PalettePacket.TYPE, Networking.PalettePacket.STREAM_CODEC);
                PayloadTypeRegistry.serverboundPlay().register(Networking.PosPacket.TYPE, Networking.PosPacket.STREAM_CODEC);
                PayloadTypeRegistry.serverboundPlay().register(Networking.KbPacket.TYPE, Networking.KbPacket.STREAM_CODEC);
                PayloadTypeRegistry.serverboundPlay().register(Networking.WandPacket.TYPE, Networking.WandPacket.STREAM_CODEC);

                ServerPlayNetworking.registerGlobalReceiver(Networking.SyncRockPacket.TYPE,(payload, context) -> {
                    context.server().execute(()->{
                        Networking.ReceiveSyncRockPacket(context.player(), payload.rx(), payload.ry(), payload.rz());
                    });
                });
                ServerPlayNetworking.registerGlobalReceiver(Networking.PalettePacket.TYPE,(payload, context) -> {
                    context.server().execute(()->{
                        Networking.ReceivePalette(context.player(), payload.mode(),payload.rotate(),payload.grad_h);
                    });
                });
                ServerPlayNetworking.registerGlobalReceiver(Networking.PosPacket.TYPE,(payload, context) -> {
                    context.server().execute(()->{
                        Direction side = Direction.values()[payload.dir()];
                        Vec3 hit = new Vec3(payload.hit().x, payload.hit().y, payload.hit().z);

                        Networking.ReceivePosPacket(context.player(), side,payload.p1()!=null,payload.has_p2(),payload.p1(),payload.p2(),hit,payload.seed());
                    });
                });
                ServerPlayNetworking.registerGlobalReceiver(Networking.KbPacket.TYPE,(payload, context) -> {
                    context.server().execute(()->{
                        WandsMod.process_keys(context.player(), payload.key(),payload.shift(),payload.alt());
                    });
                });
                ServerPlayNetworking.registerGlobalReceiver(Networking.WandPacket.TYPE,(payload, context) -> {
                    context.server().execute(()->{
                        ItemStack wand_stack = context.player().getMainHandItem();
                        var custom_data=payload.item_stack().get(DataComponents.CUSTOM_DATA);
                        if(custom_data!=null) {
                            CompoundTag tag = custom_data.copyTag();
                            CustomData.set(DataComponents.CUSTOM_DATA, wand_stack, tag);
                        }
                    });
                });
            //?}
        //?}
        //? if >= 1.20.5 {
        //?}else{
            /*//?if fabric {
            /^ServerPlayNetworking.registerGlobalReceiver(Networking.SYNC_ROCK_PACKET.id(), (server,player, handler, buf, responseSender) -> {
                int x=buf.readInt();
                int y=buf.readInt();
                int z=buf.readInt();
                server.execute(() -> {
                    Networking.ReceiveSyncRockPacket(player,x,y,z);
                });
            });

            ServerPlayNetworking.registerGlobalReceiver(Networking.PALETTE_PACKET.id(), (server,player, handler, buf, responseSender) -> {
                boolean mode=buf.readBoolean();
                boolean rotate=buf.readBoolean();
                int grad_h=buf.readInt();
                server.execute(() -> {
                    Networking.ReceivePalette(player,mode,rotate,grad_h);
                });
            });

            ServerPlayNetworking.registerGlobalReceiver(Networking.POS_PACKET.id(), (server,player, handler, buf, responseSender) -> {
                int d=buf.readInt();
                boolean has_p1 =buf.readBoolean();
                boolean has_p2 =buf.readBoolean();
                BlockPos p1 = buf.readBlockPos();
                BlockPos p2 = buf.readBlockPos();
                double hit_x = buf.readDouble();
                double hit_y = buf.readDouble();
                double hit_z = buf.readDouble();
                Vec3 hit = new Vec3(hit_x, hit_y, hit_z);
                Direction side = Direction.values()[d];
                long seed = buf.readLong();

                if (!has_p1) {
                    //WandsMod.LOGGER.info("needs at least 1 position");
                    return;
                }
                server.execute(() -> {
                    Networking.ReceivePosPacket(player,side,has_p1,has_p2,p1,p2,hit,seed);
                });
            });
            ServerPlayNetworking.registerGlobalReceiver(Networking.KB_PACKET.id(), (server,player, handler, buf, responseSender) -> {
                int key=buf.readInt();
                boolean shift=buf.readBoolean();
                boolean alt=buf.readBoolean();
                server.execute(() -> {
                    WandsMod.process_keys(player, key,shift,alt);
                });
            });

            ServerPlayNetworking.registerGlobalReceiver(Networking.WAND_PACKET.id(), (server,player, handler, buf, responseSender) -> {
                ItemStack item=buf.readItem();
                server.execute(() -> {
                    ItemStack wand_stack=player.getMainHandItem();
                    CompoundTag tag=item.getTag();
                    if(tag!=null) {
                        wand_stack.setTag(tag);
                    }
                });
            });
            ^///?}
        *///?}
    }

    //Client

//? if forge && < 1.20.5 {

    /*public static class WandsPacket {
        public net.minecraft.resources.ResourceLocation id;
        public FriendlyByteBuf buf;
        public WandsPacket(net.minecraft.resources.ResourceLocation id, FriendlyByteBuf buf) {
            this.id = id;
            this.buf = buf;
        }
        public WandsPacket(FriendlyByteBuf buffer) {
            id = buffer.readResourceLocation();
            int len = buffer.readableBytes();
            buf = new FriendlyByteBuf(buffer.readBytes(len));
        }
        public void encode(FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(id);
            buffer.writeBytes(buf);
        }
    }
    public static final String PROTOCOL_VERSION = "1";
    public static final net.minecraftforge.network.simple.SimpleChannel CHANNEL = net.minecraftforge.network.NetworkRegistry.newSimpleChannel(
        new net.minecraft.resources.ResourceLocation(net.nicguzzo.wands.WandsMod.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    public static void init() {
        int id = 0;
        CHANNEL.registerMessage(id++, WandsPacket.class, WandsPacket::encode, WandsPacket::new, (packet, ctx) -> {
            ctx.get().enqueueWork(() -> {
                net.minecraft.resources.ResourceLocation pid = packet.id;
                FriendlyByteBuf buf = packet.buf;
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    if (pid.equals(Networking.SYNC_ROCK_PACKET.id())) {
                        int x = buf.readInt();
                        int y = buf.readInt();
                        int z = buf.readInt();
                        Networking.ReceiveSyncRockPacket(player, x, y, z);
                    } else if (pid.equals(Networking.PALETTE_PACKET.id())) {
                        boolean mode = buf.readBoolean();
                        boolean rotate = buf.readBoolean();
                        int grad_h = buf.readInt();
                        Networking.ReceivePalette(player, mode, rotate, grad_h);
                    } else if (pid.equals(Networking.POS_PACKET.id())) {
                        int d = buf.readInt();
                        boolean has_p1 = buf.readBoolean();
                        boolean has_p2 = buf.readBoolean();
                        BlockPos p1 = buf.readBlockPos();
                        BlockPos p2 = buf.readBlockPos();
                        double hit_x = buf.readDouble();
                        double hit_y = buf.readDouble();
                        double hit_z = buf.readDouble();
                        Vec3 hit = new Vec3(hit_x, hit_y, hit_z);
                        Direction side = Direction.values()[d];
                        long seed = buf.readLong();
                        Networking.ReceivePosPacket(player, side, has_p1, has_p2, p1, p2, hit, seed);
                    } else if (pid.equals(Networking.KB_PACKET.id())) {
                        int key = buf.readInt();
                        boolean shift = buf.readBoolean();
                        boolean alt = buf.readBoolean();
                        WandsMod.process_keys(player, key, shift, alt);
                    } else if (pid.equals(Networking.WAND_PACKET.id())) {
                        ItemStack item = buf.readItem();
                        ItemStack wand_stack = player.getMainHandItem();
                        CompoundTag tag = item.getTag();
                        if (tag != null) {
                            wand_stack.setTag(tag);
                        }
                    }
                } else {
                    if (pid.equals(Networking.CONF_PACKET.id())) {
                        float blocks_per_xp = buf.readFloat();
                        boolean destroy_in_survival_drop = buf.readBoolean();
                        boolean survival_unenchanted_drops = buf.readBoolean();
                        boolean mend_tools = buf.readBoolean();
                        ClientNetworking.ReceiveConfPacket(blocks_per_xp, destroy_in_survival_drop, survival_unenchanted_drops, mend_tools);
                    } else if (pid.equals(Networking.STATE_PACKET.id())) {
                        int mode = buf.readInt();
                        int slot = buf.readInt();
                        boolean xp = buf.readBoolean();
                        int levels = buf.readInt();
                        float prog = buf.readFloat();
                        net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
                        ClientNetworking.ReceiveStatePacket(client.player, mode, slot, xp, levels, prog);
                    } else if (pid.equals(Networking.GLOBAL_SETTINGS_PACKET.id())) {
                        boolean drop_pos = buf.readBoolean();
                        net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
                        ClientNetworking.ReceiveGlobalSettings(client.player, drop_pos);
                    } else if (pid.equals(Networking.SND_PACKET.id())) {
                        BlockPos pos = buf.readBlockPos();
                        boolean destroy = buf.readBoolean();
                        ItemStack is = buf.readItem();
                        int send_sound = buf.readInt();
                        net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
                        ClientNetworking.ReceiveSndPacket(client.player, pos, destroy, is, send_sound);
                    } else if (pid.equals(Networking.TOAST_PACKET.id())) {
                        boolean no_tool = buf.readBoolean();
                        boolean damaged_tool = buf.readBoolean();
                        String needed_tool = buf.readUtf();
                        net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
                        ClientNetworking.ReceiveToastPacket(client.player, no_tool, damaged_tool, needed_tool);
                    } else if (pid.equals(Networking.PLAYER_DATA_PACKET.id())) {
                        CompoundTag player_data = buf.readNbt();
                        ClientNetworking.ReceivePlayerData(player_data);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        });
    }

*///?}

}
