package net.nicguzzo.wands;

/*//beginMC1_16_5
import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.event.events.client.ClientLifecycleEvent;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.NetworkManager.Side;
import me.shedaniel.architectury.registry.KeyBindings;
import me.shedaniel.architectury.registry.MenuRegistry;
//endMC1_16_5*/  
//beginMC1_17_1
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.menu.MenuRegistry;
//endMC1_17_1  
import io.netty.buffer.Unpooled;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.nicguzzo.wands.mcver.MCVer;

public class WandsModClient {
    static boolean shift =false;
    static boolean alt =false;
    public static boolean has_optifine=false;
    public static KeyMapping wand_menu_km;
    public static KeyMapping palette_menu_km;
    public static void initialize() {
        wand_menu_km=new KeyMapping("key.wands.wand_menu",WandsMod.wand_menu_key,"itemGroup.wands.wands_tab");
        palette_menu_km=new KeyMapping("key.wands.palette_menu",WandsMod.palette_menu_key,"itemGroup.wands.wands_tab");
        KeyMapping[] km={
            new KeyMapping("key.wands.wand_mode",WandsMod.wand_mode_key,"itemGroup.wands.wands_tab"),
            new KeyMapping("key.wands.wand_action",WandsMod.wand_action_key,"itemGroup.wands.wands_tab"),
            wand_menu_km,
            new KeyMapping("key.wands.wand_orientation",WandsMod.wand_orientation_key,"itemGroup.wands.wands_tab"),
            new KeyMapping("key.wands.wand_invert",WandsMod.wand_invert_key,"itemGroup.wands.wands_tab"),
            new KeyMapping("key.wands.wand_fill_circle",WandsMod.wand_fill_circle_key,"itemGroup.wands.wands_tab"),
            new KeyMapping("key.wands.wand_undo",WandsMod.wand_undo_key,"itemGroup.wands.wands_tab"),
            palette_menu_km,
            new KeyMapping("key.wands.wand_palette_mode",WandsMod.palette_mode_key,"itemGroup.wands.wands_tab"),
            //new KeyMapping("key.wands.wand_state_mode",WandsMod.wand_state_mode_key,"itemGroup.wands.wands_tab"),
            new KeyMapping("key.wands.wand_conf",WandsMod.wand_conf_key,"itemGroup.wands.wands_tab"),
        };
        for(KeyMapping k: km){
            MCVer.inst.register_key(k);
        }
        ClientTickEvent.CLIENT_PRE.register(e -> {
            boolean any=false;
            for(KeyMapping k: km){
                if (k.consumeClick()) {
                    if(!any) any=true;
                    if(k.getDefaultKey().getValue()== WandsMod.wand_conf_key){
                        
                    }
                    send_key(k.getDefaultKey().getValue(),Screen.hasShiftDown(),Screen.hasAltDown());
                }
            }

            if(!any){
                if(alt !=Screen.hasAltDown() || shift !=Screen.hasShiftDown()){
                    alt =Screen.hasAltDown();
                    shift =Screen.hasShiftDown();
                    ClientRender.wand.is_alt_pressed=alt;
                    ClientRender.wand.is_shift_pressed=shift;
                    send_key(-1, shift, alt);
                }
            }
        });

        MCVer.inst.render_info();

        if(WandsMod.is_forge) {
            ClientLifecycleEvent.CLIENT_SETUP.register(e -> {
                WandsMod.LOGGER.info("registering menues...");
                try {
                    MenuRegistry.registerScreenFactory(WandsMod.PALETTE_SCREEN_HANDLER.get(), PaletteScreen::new);
                    MenuRegistry.registerScreenFactory(WandsMod.WAND_SCREEN_HANDLER.get(), WandScreen::new);
                } catch (Exception ex) {
                    WandsMod.LOGGER.error(ex.getMessage());
                }
                WandsMod.LOGGER.info("registering menues.");
            });
        }else {
            MenuRegistry.registerScreenFactory(WandsMod.PALETTE_SCREEN_HANDLER.get(), PaletteScreen::new);
            MenuRegistry.registerScreenFactory(WandsMod.WAND_SCREEN_HANDLER.get(), WandScreen::new);
        }
        NetworkManager.registerReceiver(Side.S2C, WandsMod.SND_PACKET, (packet, context)->{
            BlockPos pos=packet.readBlockPos();
            boolean destroy=packet.readBoolean();
            ItemStack item_stack=packet.readItem();
            boolean no_tool=packet.readBoolean();
            boolean damaged_tool=packet.readBoolean();
            context.queue(()->{
                WandsMod.LOGGER.info("got sound msg "+item_stack);
                if(!item_stack.isEmpty()){
                    Block block=Block.byItem(item_stack.getItem());
                    SoundType sound_type = block.getSoundType(block.defaultBlockState());
                    SoundEvent sound=(destroy? sound_type.getBreakSound() : sound_type.getPlaceSound());
                    context.getPlayer().level.playSound(context.getPlayer(),pos,sound,SoundSource.BLOCKS, 1.0f, 1.0f);
                }
                if(no_tool){
                    Minecraft.getInstance().getToasts().addToast(new WandToast("no tool"));
                }
                if (damaged_tool) {
                    Minecraft.getInstance().getToasts().addToast(new WandToast("invalid or damaged"));
                }                
            });
        });
        NetworkManager.registerReceiver(Side.S2C, WandsMod.SLAB_PACKET, (packet, context)->{
            boolean slab=packet.readBoolean();
            context.queue(()->{
                ClientRender.wand.slab_stair_bottom=slab;
            });
        });
        NetworkManager.registerReceiver(Side.S2C,WandsMod.STATE_PACKET, (packet,context)->{
            long seed=packet.readLong();
            int  mode=packet.readInt();
            int  slot=packet.readInt();
            boolean  xp=packet.readBoolean();
            int  levels=packet.readInt();
            float prog=packet.readFloat();
            context.queue(()->{
                if(ClientRender.wand!=null) {
                    ClientRender.wand.palette_seed = seed;
                    ClientRender.wand.mode= WandItem.Mode.values()[mode];
                    if(ClientRender.wand.mode== WandItem.Mode.DIRECTION)
                        ClientRender.wand.slot = slot;
                    if(xp){
                        context.getPlayer().experienceLevel=levels;
                        context.getPlayer().experienceProgress=prog;
                    }
                }
            });
        });
    }
    public static void send_key(int key,boolean shift, boolean alt){
        Minecraft client=Minecraft.getInstance();
        if(client.getConnection() != null) {
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

    public static void send_wand(int mode,int action,int orientation,int plane,int axis,int invert,int fill,int rot,int state_mode){
        FriendlyByteBuf packet=new FriendlyByteBuf(Unpooled.buffer());
        packet.writeInt(mode);
        packet.writeInt(action);
        packet.writeInt(orientation);
        packet.writeInt(plane);
        packet.writeInt(axis);
        packet.writeInt(invert);
        packet.writeInt(fill);
        packet.writeInt(rot);
        packet.writeInt(state_mode);
        NetworkManager.sendToServer(WandsMod.WAND_PACKET, packet);
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
                MCVer.inst.set_color(1.0F, 1.0F, 1.0F, 1.0F);
                MCVer.inst.set_pos_tex_shader();

                Wand wand=ClientRender.wand;
                WandItem.Mode mode=WandItem.getMode(stack);
                WandItem.Action action=WandItem.getAction(stack);

                String ln1="";
                String ln2="Action: "+action.toString();
                String ln3="Mode: "+mode.toString();
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
                            ln1="Blocks: "+wand.block_buffer.get_length();
                            break;
                        case COPY:
                        case PASTE:
                            ln1="Copied Blocks: "+wand.copy_paste_buffer.size();
                            break;
                    }
                }
                int h=3*font.lineHeight;
                float x=(int)(screenWidth* (((float)WandsMod.config.wand_mode_display_x_pos)/100.0f));
                float y=(int)((screenHeight-h)* (((float)WandsMod.config.wand_mode_display_y_pos)/100.0f));
                font.draw(poseStack,ln1,x,y,0xffffff);
                font.draw(poseStack,ln2,x,y+font.lineHeight,0xffffff);
                font.draw(poseStack,ln3,x,y+font.lineHeight*2,0xffffff);
                if(WandsMod.config.show_tools_info) {
                    ItemRenderer itemRenderer = client.getItemRenderer();
                    CompoundTag ctag = stack.getOrCreateTag();
                    ListTag tag = ctag.getList("Tools", MCVer.NbtType.COMPOUND);
                    int ix = 16;
                    int iy = screenHeight-20;
                    tag.forEach(element -> {
                        CompoundTag stackTag = (CompoundTag) element;
                        int slot = stackTag.getInt("Slot");
                        ItemStack item = ItemStack.of(stackTag.getCompound("Tool"));
                        itemRenderer.renderAndDecorateItem(item, ix + slot * 16, iy);
                        itemRenderer.renderGuiItemDecorations(font, item, ix + slot * 16, iy, null);
                    });
                }
            }
        }
    }
}