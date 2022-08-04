package net.nicguzzo.wands;

#if MC=="1165"
import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.event.events.client.ClientLifecycleEvent;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.NetworkManager.Side;
import me.shedaniel.architectury.registry.KeyBindings;
import me.shedaniel.architectury.registry.MenuRegistry;
#else
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.registry.menu.MenuRegistry;
#endif
import io.netty.buffer.Unpooled;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.nicguzzo.wands.mcver.MCVer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WandsModClient {
    static boolean shift =false;
    static boolean alt =false;
    public static boolean has_optifine=false;
    public static KeyMapping wand_menu_km;
    public static KeyMapping palette_menu_km;
    public static final Logger LOGGER = LogManager.getLogger();
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
            new KeyMapping("key.wands.wand_rotate",WandsMod.wand_rotate,"itemGroup.wands.wands_tab"),
            new KeyMapping("key.wands.wand_conf",WandsMod.wand_conf_key,"itemGroup.wands.wands_tab"),
            new KeyMapping("key.wands.m_inc",WandsMod.wand_m_inc_key,"itemGroup.wands.wands_tab"),
            new KeyMapping("key.wands.m_dec",WandsMod.wand_m_dec_key,"itemGroup.wands.wands_tab"),
            new KeyMapping("key.wands.n_inc",WandsMod.wand_n_inc_key,"itemGroup.wands.wands_tab"),
            new KeyMapping("key.wands.n_dec",WandsMod.wand_n_dec_key,"itemGroup.wands.wands_tab"),
            new KeyMapping("key.wands.toggle_stair_slab",WandsMod.toggle_stair_slab_key,"itemGroup.wands.wands_tab"),
            new KeyMapping("key.wands.area_diagonal_spread",WandsMod.area_diagonal_spread,"itemGroup.wands.wands_tab"),
            new KeyMapping("key.wands.inc_sel_block",WandsMod.inc_sel_block,"itemGroup.wands.wands_tab"),

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
            int i_sound=packet.readInt();
            context.queue(()->{
                //WandsMod.LOGGER.info("got sound msg "+item_stack);
                if(i_sound> -1 && i_sound< Wand.Sounds.values().length){
                    Wand.Sounds snd=Wand.Sounds.values()[i_sound];
                    SoundEvent sound = snd.get_sound();
                    context.getPlayer().level.playSound(context.getPlayer(), pos, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
                }else {
                    if (!item_stack.isEmpty()) {
                        Block block = Block.byItem(item_stack.getItem());
                        SoundType sound_type = block.getSoundType(block.defaultBlockState());
                        SoundEvent sound = (destroy ? sound_type.getBreakSound() : sound_type.getPlaceSound());
                        context.getPlayer().level.playSound(context.getPlayer(), pos, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
                    }
                }
                if(no_tool){
                    Minecraft.getInstance().getToasts().addToast(new WandToast("no tool"));
                }
                if (damaged_tool) {
                    Minecraft.getInstance().getToasts().addToast(new WandToast("invalid or damaged"));
                }                
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

        NetworkManager.registerReceiver(Side.S2C,WandsMod.CONF_PACKET, (packet,context)->{
            ServerData srv = Minecraft.getInstance().getCurrentServer();
            if(srv!=null && WandsMod.config!=null){
                WandsMod.config.blocks_per_xp=packet.readFloat();
                /*WandsMod.config.stone_wand_limit=packet.readInt();
                WandsMod.config.iron_wand_limit=packet.readInt();
                WandsMod.config.diamond_wand_limit=packet.readInt();
                WandsMod.config.netherite_wand_limit=packet.readInt();
                WandsMod.config.stone_wand_durability=packet.readInt();
                WandsMod.config.iron_wand_durability=packet.readInt();
                WandsMod.config.diamond_wand_durability=packet.readInt();
                WandsMod.config.netherite_wand_durability=packet.readInt();*/
                WandsMod.config.destroy_in_survival_drop=packet.readBoolean();
                WandsMod.config.survival_unenchanted_drops=packet.readBoolean();
                WandsMod.config.allow_wand_to_break=packet.readBoolean();
                WandsMod.config.allow_offhand_to_break=packet.readBoolean();
                WandsMod.config.mend_tools=packet.readBoolean();
                LOGGER.info("got config");
                context.queue(()->{


                });
            }
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

    public static void send_wand(ItemStack item) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeItem(item);
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
                Rotation r = WandItem.getRotation(stack);
                String rot = "";
                switch (r) {
                    case NONE:
                        rot = "0°";
                        break;
                    case CLOCKWISE_90:
                        rot = "90°";
                        break;
                    case CLOCKWISE_180:
                        rot = "180°";
                        break;
                    case COUNTERCLOCKWISE_90:
                        rot = "270°";
                        break;
                }
                String ln1="";
                String ln2="Action: "+action.toString();
                String ln3="Mode: "+mode.toString()+" Rot:"+rot;
                if(wand.valid) {
                    switch(mode){
                        case DIRECTION:
                            int mult=WandItem.getVal(stack, WandItem.Value.MULTIPLIER);
                            ln1="pos: ["+wand.pos.getX()+","+wand.pos.getY()+","+wand.pos.getZ()+"] x"+mult;
                            break;
                        case GRID:
                            int gm=WandItem.getVal(stack, WandItem.Value.GRIDM);
                            int gn=WandItem.getVal(stack, WandItem.Value.GRIDN);
                            int gms=WandItem.getVal(stack, WandItem.Value.GRIDMS);
                            int gns=WandItem.getVal(stack, WandItem.Value.GRIDNS);
                            String skp="";
                            if(gms>0||gns>0){
                                skp=" - ("+gms+"x"+gns+")";
                            }
                            ln1="Grid "+gm+"x"+gn+ skp;
                            break;

                        case ROW_COL:
                            ln1="Blocks: "+wand.block_buffer.get_length();
                        case FILL:
                        case LINE:
                        case AREA:
                            int arealim=WandItem.getVal(stack, WandItem.Value.AREALIM);
                            if(arealim>0){
                                ln1+=" Limit: "+arealim;
                            }
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
                    //int ix = 4;
                    //int iy = screenHeight-20;
                    int ix=(int)(screenWidth* (((float)WandsMod.config.wand_tools_display_x_pos)/100.0f));
                    int iy=(int)((screenHeight-20)* (((float)WandsMod.config.wand_tools_display_y_pos)/100.0f));
                    

                    tag.forEach(element -> {
                        CompoundTag stackTag = (CompoundTag) element;
                        int slot = stackTag.getInt("Slot");
                        ItemStack item = ItemStack.of(stackTag.getCompound("Tool"));
                        int yoff=0;
                        if(ClientRender.has_target && slot==ClientRender.wand.digger_item_slot){
                            yoff=-5;
                        }
                        itemRenderer.renderAndDecorateItem(item, ix + slot * 16, iy+yoff);
                        itemRenderer.renderGuiItemDecorations(font, item, ix + slot * 16, iy, null);
                    });
                }
            }
        }
    }
}