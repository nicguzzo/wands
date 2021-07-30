package net.nicguzzo.wands;

import io.netty.buffer.Unpooled;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class WandsModClient {
    
    public static void initialize() {
        KeyMapping[] km={
            new KeyMapping("key.wands.wand_mode",WandsMod.wand_mode_key,"category.wands"),
            new KeyMapping("key.wands.wand_orientation",WandsMod.wand_orientation_key,"category.wands"),
            new KeyMapping("key.wands.wand_invert",WandsMod.wand_invert_key,"category.wands"),
            new KeyMapping("key.wands.wand_palette_mode",WandsMod.palette_mode_key,"category.wands"),
            new KeyMapping("key.wands.wand_fill_circle",WandsMod.wand_fill_circle_key,"category.wands"),
            new KeyMapping("key.wands.wand_undo",WandsMod.wand_undo,"category.wands")
        };
        for(KeyMapping k: km){
            KeyMappingRegistry.register(k);
        }
        
        ClientTickEvent.CLIENT_PRE.register(e -> {
            for(KeyMapping k: km){
                if (k.consumeClick()) {
                    //WandsMod.LOGGER.info("key binding: "+k.getDefaultKey().getValue());
                    //Minecraft client=Minecraft.getInstance();
                    //WandsMod.process_keys(client.player, k.getDefaultKey().getValue(),Screen.hasShiftDown(),Screen.hasAltDown());                    
                    send_key(k.getDefaultKey().getValue());
                }
            }
        });

        ClientLifecycleEvent.CLIENT_SETUP.register(e->{
            MenuRegistry.registerScreenFactory(WandsMod.PALETTE_SCREEN_HANDLER.get(), PaletteScreen::new);
            
        });
        NetworkManager.registerReceiver(Side.S2C, WandsMod.SND_PACKET, (packet,context)->{
            BlockPos pos=packet.readBlockPos();
            boolean destroy=packet.readBoolean();
            ItemStack item_stack=packet.readItem();
            context.queue(()->{
                //WandsMod.LOGGER.info("got sound msg "+item_stack);
                if(!item_stack.isEmpty()){
                    Block block=Block.byItem(item_stack.getItem());
                    SoundType sound_type = block.getSoundType(block.defaultBlockState());
                    SoundEvent sound=(destroy? sound_type.getBreakSound() : sound_type.getPlaceSound());
                    context.getPlayer().level.playSound(context.getPlayer(),pos,sound,SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            });
        });
    }
    public static void send_key(int key){

        FriendlyByteBuf packet=new FriendlyByteBuf(Unpooled.buffer());
        packet.writeInt(key);
        packet.writeBoolean(Screen.hasShiftDown());
        packet.writeBoolean(Screen.hasAltDown());
        NetworkManager.sendToServer(WandsMod.KB_PACKET, packet);
    }
    public static void send_palette(boolean next_mode,boolean toggle_rotate){
        FriendlyByteBuf packet=new FriendlyByteBuf(Unpooled.buffer());            
        packet.writeBoolean(next_mode);
        packet.writeBoolean(toggle_rotate);
        NetworkManager.sendToServer(WandsMod.PALETTE_PACKET, packet);
    }
}