package net.nicguzzo.wands.networking;

//? if >= 1.20.5 {
//?}else{
/*import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
*///?}
//? if fabric {
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//?}
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.compat.Compat;
import net.nicguzzo.wands.wand.PlayerWand;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandProps;


public class ClientNetworking {

    static public void ReceiveToastPacket(Player player, boolean no_tool, boolean damaged_tool, String needed_tool ) {
        String toolsKey = "Shift+" + WandsModClient.getKeyName(WandsMod.WandKeys.MENU);
        if (no_tool) {
            Compat.displayClientMessage(player,Compat.translatable("wands.message.no_tool", toolsKey).withStyle(ChatFormatting.RED),true);
        }
        if (damaged_tool) {
            Compat.displayClientMessage(player,Compat.translatable("wands.message.damaged_tool", toolsKey).withStyle(ChatFormatting.RED), true);
        }
        if (!needed_tool.isEmpty()) {
            Compat.displayClientMessage(player,Compat.translatable("wands.message.wrong_tool", needed_tool, toolsKey).withStyle(ChatFormatting.RED), true);
        }
    }
    static public void ReceiveStatePacket(Player player,int mode, int slot, boolean xp, int levels, float prog){
        if(ClientRender.wand !=null){
            ClientRender.wand.mode = WandProps.Mode.values()[mode];
            if (ClientRender.wand.mode == WandProps.Mode.DIRECTION)
                ClientRender.wand.palette.slot = slot;
            if (xp) {
                player.experienceLevel = levels;
                player.experienceProgress = prog;
            }
        }
    }
    static public void ReceivePlayerData(CompoundTag player_data) {
        if (ClientRender.wand != null) {
            ClientRender.wand.player_data = player_data;
        }
    }
    static public void ReceiveConfPacket(float blocks_per_xp, boolean destroy_in_survival_drop, boolean survival_unenchanted_drops, boolean mend_tools){
        ServerData srv = Minecraft.getInstance().getCurrentServer();
        if (srv != null && WandsMod.config != null) {
            WandsMod.config.blocks_per_xp = blocks_per_xp;
            WandsMod.config.destroy_in_survival_drop = destroy_in_survival_drop;
            WandsMod.config.survival_unenchanted_drops = survival_unenchanted_drops;
            WandsMod.config.mend_tools = mend_tools;
        }
    }
    static public void ReceiveGlobalSettings(Player player,boolean drop_pos) {
        if (player != null) {
             Wand wand = PlayerWand.get(player);
             if (wand != null) {
                 wand.drop_on_player = drop_pos;
             }
        }
    }
    static public void ReceiveSndPacket(Player player,BlockPos pos, boolean destroy, ItemStack item_stack, int i_sound ) {
        if (i_sound > -1 && i_sound < Wand.Sounds.values().length) {
            Wand.Sounds snd = Wand.Sounds.values()[i_sound];
            SoundEvent sound = snd.get_sound();
            Compat.player_level(player).playSound(player, pos, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
        } else {
            if (!item_stack.isEmpty()) {
                Block block = Block.byItem(item_stack.getItem());
                BlockState bs = block.defaultBlockState();
                SoundType sound_type = bs.getSoundType();
                //SoundType sound_type = ((BlockBehaviourInvoker)block).invokeGetSoundType(block.defaultBlockState());
                SoundEvent sound = (destroy ? sound_type.getBreakSound() : sound_type.getPlaceSound());
                Compat.player_level(player).playSound(player, pos, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
    }
    public static void SendKbPacket(int key, boolean shift, boolean alt) {
        //? if >= 1.20.5 {
            //? if fabric {
            ClientPlayNetworking.send( new Networking.KbPacket(key, shift, alt));
            //?}
            //? if neoforge {
            /*//? if >= 1.21.11 {
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new Networking.KbPacket(key, shift, alt));
            //?} else {
            /^net.neoforged.neoforge.network.PacketDistributor.sendToServer(new Networking.KbPacket(key, shift, alt));
            ^///?}
            *///?}
        //?}else{
        /*Minecraft client = Minecraft.getInstance();
        if (client.getConnection() != null) {
            FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
            packet.writeInt(key);
            packet.writeBoolean(shift);
            packet.writeBoolean(alt);
            //? if fabric {
            ClientPlayNetworking.send(Networking.KB_PACKET.id(), packet);
            //?}
            //? if forge {
            /^Networking.CHANNEL.sendToServer(new Networking.WandsPacket(Networking.KB_PACKET.id(), packet));
            ^///?}
        }
        *///?}
    }
    public static void SendPosPacket(Direction side, BlockPos p1, BlockPos p2, Vec3 hit, long seed) {
        //? if >= 1.20.5 {
        if(p1==null) return;
            //? if fabric {
            ClientPlayNetworking.send( new Networking.PosPacket(side.ordinal(),p2!=null,p1,(p2!=null?p2:new BlockPos(0,0,0)),new Networking.Vec3d(hit.x, hit.y, hit.z),seed));
            //?}
            //? if neoforge {
            /*//? if >= 1.21.11 {
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new Networking.PosPacket(side.ordinal(),p2!=null,p1,(p2!=null?p2:new BlockPos(0,0,0)),new Networking.Vec3d(hit.x, hit.y, hit.z),seed));
            //?} else {
            /^net.neoforged.neoforge.network.PacketDistributor.sendToServer(new Networking.PosPacket(side.ordinal(),p2!=null,p1,(p2!=null?p2:new BlockPos(0,0,0)),new Networking.Vec3d(hit.x, hit.y, hit.z),seed));
            ^///?}
            *///?}
        //?}else{
        /*FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeInt(side.ordinal());
        packet.writeBoolean(p1!=null);
        packet.writeBoolean(p2!=null);
        if(p1!=null)
            packet.writeBlockPos(p1);
        else
            packet.writeBlockPos(new BlockPos(0,0,0));
        if(p2!=null)
            packet.writeBlockPos(p2);
        else
            packet.writeBlockPos(new BlockPos(0,0,0));

        packet.writeDouble(hit.x);
        packet.writeDouble(hit.y);
        packet.writeDouble(hit.z);
        packet.writeLong(seed);
        //? if fabric {
        ClientPlayNetworking.send(Networking.POS_PACKET.id(), packet);
        //?}
        //? if forge {
        /^Networking.CHANNEL.sendToServer(new Networking.WandsPacket(Networking.POS_PACKET.id(), packet));
        ^///?}
        *///?}
    }
    static public void SendGlobalSettings(boolean drop_pos) {
        //? if >= 1.20.5 {
            //? if fabric {
            ClientPlayNetworking.send( new Networking.GlobalSettingsPacket(drop_pos));
            //?}
            //? if neoforge {
            /*//? if >= 1.21.11 {
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new Networking.GlobalSettingsPacket(drop_pos));
            //?} else {
            /^net.neoforged.neoforge.network.PacketDistributor.sendToServer(new Networking.GlobalSettingsPacket(drop_pos));
            ^///?}
            *///?}
        //?}else{
        /*FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeBoolean(drop_pos);
        //? if fabric {
        ClientPlayNetworking.send(Networking.GLOBAL_SETTINGS_PACKET.id(), packet);
        //?}
        //? if forge {
        /^Networking.CHANNEL.sendToServer(new Networking.WandsPacket(Networking.GLOBAL_SETTINGS_PACKET.id(), packet));
        ^///?}
        *///?}
    }
    public static void SendPalette(boolean next_mode, boolean toggle_rotate, int grad_h) {
        //? if >= 1.20.5 {
            //? if fabric {
            ClientPlayNetworking.send( new Networking.PalettePacket(next_mode,toggle_rotate,grad_h));
            //?}
            //? if neoforge {
            /*//? if >= 1.21.11 {
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new Networking.PalettePacket(next_mode,toggle_rotate,grad_h));
            //?} else {
            /^net.neoforged.neoforge.network.PacketDistributor.sendToServer(new Networking.PalettePacket(next_mode,toggle_rotate,grad_h));
            ^///?}
            *///?}
        //?}else{
        /*FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeBoolean(next_mode);
        packet.writeBoolean(toggle_rotate);
        packet.writeInt(grad_h);
        //? if fabric {
        ClientPlayNetworking.send(Networking.PALETTE_PACKET.id(), packet);
        //?}
        //? if forge {
        /^Networking.CHANNEL.sendToServer(new Networking.WandsPacket(Networking.PALETTE_PACKET.id(), packet));
        ^///?}
        *///?}
    }
    public static void SendWand(ItemStack item) {
        //? if >= 1.20.5 {
            //? if fabric {
            ClientPlayNetworking.send( new Networking.WandPacket(item));
            //?}
            //? if neoforge {
            /*//? if >= 1.21.11 {
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new Networking.WandPacket(item));
            //?} else {
            /^net.neoforged.neoforge.network.PacketDistributor.sendToServer(new Networking.WandPacket(item));
            ^///?}
            *///?}
        //?}else{
        /*FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeItem(item);
        //? if fabric {
        ClientPlayNetworking.send(Networking.WAND_PACKET.id(), packet);
        //?}
        //? if forge {
        /^Networking.CHANNEL.sendToServer(new Networking.WandsPacket(Networking.WAND_PACKET.id(), packet));
        ^///?}
        *///?}
    }
    public static void SendSyncRockPacket(int x,int y,int z) {
        //? if >= 1.20.5 {
            //? if fabric {
            ClientPlayNetworking.send( new Networking.SyncRockPacket(x,y,z));
            //?}
            //? if neoforge {
            /*//? if >= 1.21.11 {
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new Networking.SyncRockPacket(x,y,z));
            //?} else {
            /^net.neoforged.neoforge.network.PacketDistributor.sendToServer(new Networking.SyncRockPacket(x,y,z));
            ^///?}
            *///?}
        //?}else{
        /*FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeInt(x);
        packet.writeInt(y);
        packet.writeInt(z);
        //? if fabric {
        ClientPlayNetworking.send(Networking.SYNC_ROCK_PACKET.id(), packet);
        //?}
        //? if forge {
        /^Networking.CHANNEL.sendToServer(new Networking.WandsPacket(Networking.SYNC_ROCK_PACKET.id(), packet));
        ^///?}
        *///?}
    }
}
