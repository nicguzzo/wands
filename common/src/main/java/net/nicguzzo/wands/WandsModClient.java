package net.nicguzzo.wands;

import io.netty.buffer.Unpooled;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.client.ClientGuiEvent.ScreenRenderPost;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class WandsModClient {
    static boolean shift =false;
    static boolean alt =false;
    public static boolean is_forge=false;
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
            boolean any=false;
            for(KeyMapping k: km){
                if (k.consumeClick()) {
                    if(!any) any=true;
                    send_key(k.getDefaultKey().getValue(),Screen.hasShiftDown(),Screen.hasAltDown());
                }
            }
            if(!any){
                //WandsMod.LOGGER.info("ClientTickEvent");
                if(alt !=Screen.hasAltDown() || shift !=Screen.hasShiftDown()){
                    alt =Screen.hasAltDown();
                    shift =Screen.hasShiftDown();
                    ClientRender.wand.is_alt_pressed=alt;
                    ClientRender.wand.is_shift_pressed=shift;
                    send_key(-1, shift, alt);
                }
            }
        });

        ClientGuiEvent.RENDER_HUD.register((pose,delta)->{
            render_wand_info(pose);
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
        NetworkManager.registerReceiver(Side.S2C,WandsMod.STATE_PACKET, (packet,context)->{
            long seed=packet.readLong();
            int  axis=packet.readInt();
            int  plane=packet.readInt();
            int  mode=packet.readInt();
            int  slot=packet.readInt();
            boolean  xp=packet.readBoolean();
            int  levels=packet.readInt();
            float prog=packet.readFloat();
            context.queue(()->{
                if(ClientRender.wand!=null) {
                    ClientRender.wand.axis=Direction.Axis.values()[axis];
                    ClientRender.wand.plane= WandItem.Plane.values()[plane];
                    ClientRender.wand.palette_seed = seed;
                    ClientRender.wand.mode= WandItem.Mode.values()[mode];
                    if(ClientRender.wand.mode== WandItem.Mode.DIRECTION)
                        ClientRender.wand.slot = slot;
                    if(xp){
                        context.getPlayer().experienceLevel=levels;
                        context.getPlayer().experienceProgress=prog;
                    }
                    //WandsMod.log(" got palette_seed: "+seed,true);
                    //WandsMod.log(" got axis "+axis,true);
                }
            });
        });
    }
    public static void send_key(int key,boolean shift, boolean alt){
        Minecraft client=Minecraft.getInstance();
        if(client.getConnection() != null) {
            //WandsMod.LOGGER.info("send_key"+key+" shift "+shift +" alt "+alt);
            FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
            packet.writeInt(key);
            packet.writeBoolean(shift);
            packet.writeBoolean(alt);
            NetworkManager.sendToServer(WandsMod.KB_PACKET, packet);
        }
    }
    public static void send_palette(boolean next_mode,boolean toggle_rotate){
        FriendlyByteBuf packet=new FriendlyByteBuf(Unpooled.buffer());            
        packet.writeBoolean(next_mode);
        packet.writeBoolean(toggle_rotate);
        NetworkManager.sendToServer(WandsMod.PALETTE_PACKET, packet);
    }

    public static void render_wand_info(PoseStack poseStack){
        
        Minecraft client = Minecraft.getInstance();
        if(client!=null && client.player!=null){
            ItemStack stack=client.player.getMainHandItem();
            if(stack!=null && !stack.isEmpty() && stack.getItem() instanceof WandItem){
                int screenWidth =client.getWindow().getGuiScaledWidth();
                int screenHeight = client.getWindow().getGuiScaledHeight();
                Font font = client.font;
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                Wand wand=ClientRender.wand;
                WandItem wand_item=(WandItem)stack.getItem();
                WandItem.Mode mode=wand_item.getMode(stack);

                String ln1="";
                String ln2="Mode: "+mode.toString();
                if(wand.valid) {
                    switch(mode){
                        case DIRECTION:
                            ln1="pos: ["+wand.pos.getX()+","+wand.pos.getY()+","+wand.pos.getZ()+"]";
                            break;
                        case ROW_COL:
                        case FILL:
                        case AREA:
                        case LINE:
                            ln1="Blocks: "+wand.block_buffer.get_length();
                            break;
                        case CIRCLE:
                            ln1="Radius: "+wand.radius + " N: "+wand.block_buffer.get_length();
                            break;
                        case RECT:
                            WandItem.Plane plane=wand_item.getPlane(stack);
                            ln1="Blocks: "+wand.block_buffer.get_length()+" Plane: "+plane;
                            break;
                        case COPY:
                        case PASTE:
                            ln1="Copied Blocks: "+wand.copy_paste_buffer.size();
                            break;
                    }
                }
                //int w=font.width(msg);
                int h=2*font.lineHeight;
                float x=(int)(screenWidth* (((float)WandsMod.config.wand_mode_display_x_pos)/100.0f));
                float y=(int)((screenHeight-h)* (((float)WandsMod.config.wand_mode_display_y_pos)/100.0f));
                font.draw(poseStack,ln1,x,y,0xffffff);
                font.draw(poseStack,ln2,x,y+font.lineHeight,0xffffff);
            }
        }
    }
}