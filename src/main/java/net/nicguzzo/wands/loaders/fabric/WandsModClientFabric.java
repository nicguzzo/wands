//? if fabric {
package net.nicguzzo.wands.loaders.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;


import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screens.MenuScreens;

import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.PaletteClientTooltip;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.screens.MagicBagScreen;
import net.nicguzzo.wands.client.screens.PaletteScreen;
import net.nicguzzo.wands.client.screens.WandToolScreen;
import java.util.Optional;

import net.nicguzzo.wands.compat.RcId;
import net.nicguzzo.wands.networking.ClientNetworking;
import net.nicguzzo.wands.networking.Networking;
//? if < 26.1{
/*import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
*///?}
//?if >=1.21.11{


import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
//?}
//? if >= 1.20.5 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//?}else{

/*import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
*///?}

public class WandsModClientFabric implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        WandsModClient.initialize();
        ClientTooltipComponentCallback.EVENT.register(PaletteClientTooltip::tryCreate);

        Optional<ModContainer> cont= FabricLoader.getInstance().getModContainer("optifabric");
        if(cont.isPresent()){
            WandsModClient.has_optifine=true;
            //WandsMod.log("has optifine!!!!!!!!!!!!",true);
        }

        Optional<ModContainer> opac= FabricLoader.getInstance().getModContainer("openpartiesandclaims");
         if(opac.isPresent()){
            WandsModClient.has_opac=true;
            //WandsMod.log("cli has opac!!!!!!!!!!!!",true);
        }else{
             //WandsMod.log("cli NO opac!!!!!!!!!!!!",true);
         }

         MenuScreens.register(FabricEntrypoint.WAND_TOOLS_MENU_TYPE, WandToolScreen::new);
         MenuScreens.register(FabricEntrypoint.MAGIC_BAG_MENU_TYPE, MagicBagScreen::new);
         MenuScreens.register(FabricEntrypoint.PALETTE_MENU_TYPE, PaletteScreen::new);
         registerPackets();
         ClientTickEvents.END_CLIENT_TICK.register(WandsModClient::client_tick);
         //? if >=1.21.11{
         HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            RcId.fromNamespaceAndPath(WandsMod.MOD_ID, "custom_element_id").id(),
            (graphics, tickCounter) -> {
                WandsModClient.render_hud(graphics);
            }
         );
         //?}else{
         /*HudRenderCallback.EVENT.register((e, d) -> {
              WandsModClient.render_hud(e);
         });
         *///?}
    }

    public static void registerPackets() {
        //? if >= 1.20.5 {

        PayloadTypeRegistry.clientboundPlay().register(Networking.ConfPacket.TYPE, Networking.ConfPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Networking.StatePacket.TYPE, Networking.StatePacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Networking.PlayerDataPacket.TYPE, Networking.PlayerDataPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Networking.SndPacket.TYPE, Networking.SndPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Networking.GlobalSettingsPacket.TYPE, Networking.GlobalSettingsPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Networking.ToastPacket.TYPE, Networking.ToastPacket.STREAM_CODEC);

        ClientPlayNetworking.registerGlobalReceiver(Networking.ConfPacket.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                ClientNetworking.ReceiveConfPacket(
                        payload.blocks_per_xp(),
                        payload.destroy_in_survival_drop(),
                        payload.survival_unenchanted_drops(),
                        payload.mend_tools());
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Networking.StatePacket.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                ClientNetworking.ReceiveStatePacket(
                        context.player(),
                        payload.mode(),
                        payload.slot(),
                        payload.xp(),
                        payload.levels(),
                        payload.prog()
                        );
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Networking.PlayerDataPacket.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                ClientNetworking.ReceivePlayerData(payload.tag());
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Networking.SndPacket.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                ClientNetworking.ReceiveSndPacket(context.player(),payload.pos(),payload.destroy(),payload.item_stack(),payload.i_sound() );
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Networking.ToastPacket.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                ClientNetworking.ReceiveToastPacket(context.player(),payload.no_tool(),payload.damaged_tool(),payload.needed_tool());
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Networking.GlobalSettingsPacket.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                ClientNetworking.ReceiveGlobalSettings(context.player(),payload.drop_pos());
            });
        });
        //?}else{
        /*ClientPlayNetworking.registerGlobalReceiver(Networking.CONF_PACKET.id(), (client, handler, buf, responseSender) -> {
            float blocks_per_xp = buf.readFloat();
            boolean destroy_in_survival_drop = buf.readBoolean();
            boolean survival_unenchanted_drops = buf.readBoolean();
            boolean mend_tools = buf.readBoolean();
            client.execute(() -> {
                ClientNetworking.ReceiveConfPacket(blocks_per_xp, destroy_in_survival_drop, survival_unenchanted_drops, mend_tools);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Networking.STATE_PACKET.id(), (client, handler, buf, responseSender) -> {
            int mode = buf.readInt();
            int slot = buf.readInt();
            boolean xp = buf.readBoolean();
            int levels = buf.readInt();
            float prog = buf.readFloat();
            client.execute(() -> {
                ClientNetworking.ReceiveStatePacket(client.player, mode, slot, xp, levels, prog);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Networking.PLAYER_DATA_PACKET.id(), (client, handler, buf, responseSender) -> {
            CompoundTag player_data = buf.readNbt();
            client.execute(() -> {
                ClientNetworking.ReceivePlayerData(player_data);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Networking.GLOBAL_SETTINGS_PACKET.id(), (client, handler, buf, responseSender) -> {
            boolean drop_pos = buf.readBoolean();
            client.execute(() -> {
                ClientNetworking.ReceiveGlobalSettings(client.player, drop_pos);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Networking.SND_PACKET.id(), (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            boolean destroy = buf.readBoolean();
            ItemStack item_stack = buf.readItem();
            int i_sound = buf.readInt();
            client.execute(() -> {
                ClientNetworking.ReceiveSndPacket(client.player, pos, destroy, item_stack, i_sound);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Networking.TOAST_PACKET.id(), (client, handler, buf, responseSender) -> {
            boolean no_tool = buf.readBoolean();
            boolean damaged_tool = buf.readBoolean();
            String needed_tool = buf.readUtf();
            client.execute(() -> {
                ClientNetworking.ReceiveToastPacket(client.player, no_tool, damaged_tool, needed_tool);
            });
        });
        *///?}
    }
}
//?}
