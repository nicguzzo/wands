package net.nicguzzo.wands.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.screens.Screen;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.items.*;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.gui.Btn;
import net.nicguzzo.wands.client.gui.Select;
import net.nicguzzo.wands.client.gui.Spinner;
import net.nicguzzo.wands.client.gui.Wdgt;
import net.nicguzzo.wands.menues.WandMenu;
import net.nicguzzo.wands.wand.WandProps;
import net.nicguzzo.wands.wand.WandProps.Value;
import net.nicguzzo.wands.utils.Compat;
import org.jetbrains.annotations.NotNull;
#if MC >= "1200"
import net.minecraft.client.gui.GuiGraphics;
#endif
import java.util.Vector;

public class WandScreen extends AbstractContainerScreen<WandMenu> {
    ItemStack wand_stack=null;
    WandItem wand_item =null;
    static final int img_w=256;
    static final int img_h=256;
    private static final ResourceLocation BG_TEX = new ResourceLocation("wands", "textures/gui/wand.png");
    private static final ResourceLocation INV_TEX = new ResourceLocation("wands", "textures/gui/inventory.png");

    Vector<Wdgt> wdgets =new Vector<>();
    Select modes_grp;
    Select action_grp;
    Select orientation_grp;
    Select plane_grp;
    Select axis_grp;
    Select inv_grp_btn;
    Select cfill_grp_btn;
    Select rfill_grp_btn;
    Select even_grp_btn;
    Select rot_grp;
    Select state_grp;
    Select slab_grp_btn;
    Select diag_grp_btn;
    Select inc_sel_grp_btn;
    Select target_air_grp_btn;
    Select mirror_axis;
    Btn conf_btn;
    Spinner mult_spn;
    Spinner grid_m_spn;
    Spinner grid_n_spn;
    Spinner grid_mskp_spn;
    Spinner grid_nskp_spn;
    Spinner grid_moff_spn;
    Spinner grid_noff_spn;
    Spinner blast_radius_spn;
    Spinner row_col_spn;
    Spinner arealim_spn;
    Spinner skip_spn;
    Spinner tunnel_w;
    Spinner tunnel_h;
    Spinner tunnel_d;
    Spinner tunnel_ox;
    Spinner tunnel_oy;
    Select match_state_sel;
    Select drop_pos_sel;
    Btn show_inv_btn;
    boolean show_inv=false;
    int left;
    int right;
    int bottom;
    int top;
    int xoff;
    int yoff;
    public WandScreen(WandMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }
    private Spinner valSpinner(WandProps.Value val,int x,int y,int w,int h,Component label) {
        int v=WandProps.getVal(wand_stack, val);
        return new Spinner(v, val.min, val.max,x,y,w,h,label) {
            public void onInc(int mx, int my, int value) {
                WandProps.setVal(wand_stack, val, value);
                WandsModClient.send_wand(wand_stack);
            }
            public void onDec(int mx, int my, int value) {
                WandProps.setVal(wand_stack, val, value);
                WandsModClient.send_wand(wand_stack);
            }
        };
    }

    @Override
    public void init(){
        super.init();
        wand_stack=this.menu.wand;
        if(wand_stack==null){
            return;
        }
        if(wand_stack!=null && wand_stack.getItem() instanceof WandItem){
            wand_item =(WandItem)wand_stack.getItem();
        }else{
            return;
        }
        if(wand_item ==null){
            return;
        }
        int btn_h=10;
        int btn_w=60;
        int btn_margin=2;
        int h2=btn_h+btn_margin;
        int h=WandProps.modes.length*h2;
        xoff=WandsMod.config.wand_screen_x_offset;
        yoff=WandsMod.config.wand_screen_x_offset;
        left=(width/2)-(img_w/2)-xoff;
        right=(width/2)+(img_w/2)-xoff;
        bottom=(height/2)-(h/2)-12-yoff;
        top=(height/2)+(h/2)-yoff;


        mult_spn=valSpinner(Value.MULTIPLIER,left+200,bottom+25,25,14, Compat.translatable("screen.wands.multiplier"));
        mult_spn.label_side=true;
        wdgets.add(mult_spn);

        row_col_spn=valSpinner(Value.ROWCOLLIM,left+170,bottom+70,50,14,Compat.translatable("screen.wands.limit"));
        row_col_spn.label_side=true;
        wdgets.add(row_col_spn);

        blast_radius_spn=valSpinner(Value.BLASTRAD,left+210,bottom+25,25,14,Compat.translatable("screen.wands.blast_radius"));
        blast_radius_spn.label_side=true;
        blast_radius_spn.inc_val=2;
        blast_radius_spn.shift_inc_val=4;
        wdgets.add(blast_radius_spn);

        grid_m_spn=new Spinner(WandProps.getVal(wand_stack,Value.GRIDM),1, wand_item.limit,left+180,bottom+25,25,14,Compat.translatable("screen.wands.grid_mxn"))
        {
            public void onInc(int mx,int my,int value){
                WandProps.incGrid(wand_stack,Value.GRIDM,1, wand_item.limit);
                WandsModClient.send_wand(wand_stack);
            }
            public void onDec(int mx,int my,int value){
                WandProps.decVal(wand_stack,Value.GRIDM,1, wand_item.limit);
                WandsModClient.send_wand(wand_stack);
            }
        };
        grid_m_spn.label_side=true;
        wdgets.add(grid_m_spn);

        grid_n_spn=new Spinner(WandProps.getVal(wand_stack,Value.GRIDN),1, wand_item.limit,left+215,bottom+25,25,14,Compat.literal("x"))
        {
            public void onInc(int mx,int my,int value){
                WandProps.incGrid(wand_stack,Value.GRIDN,1, wand_item.limit);
                WandsModClient.send_wand(wand_stack);
            }
            public void onDec(int mx,int my,int value){
                WandProps.decVal(wand_stack,Value.GRIDN,1, wand_item.limit);
                WandsModClient.send_wand(wand_stack);
            }
        };
        grid_n_spn.label_side=true;
        wdgets.add(grid_n_spn);

        grid_mskp_spn=valSpinner(Value.GRIDMS,left+180,bottom+40,25,14,Compat.translatable("screen.wands.grid_mxn_skip"));
        grid_mskp_spn.label_side=true;
        wdgets.add(grid_mskp_spn);

        grid_nskp_spn=valSpinner(Value.GRIDNS,left+215,bottom+40,25,14,Compat.literal(","));
        grid_nskp_spn.label_side=true;
        wdgets.add(grid_nskp_spn);

        grid_moff_spn=valSpinner(Value.GRIDMOFF,left+180,bottom+55,25,14,Compat.translatable("screen.wands.grid_offset"));
        grid_moff_spn.label_side=true;
        wdgets.add(grid_moff_spn);

        grid_noff_spn=valSpinner(Value.GRIDNOFF,left+215,bottom+55,25,14,Compat.literal(","));
        grid_noff_spn.label_side=true;
        wdgets.add(grid_noff_spn);

        arealim_spn=valSpinner(Value.AREALIM, left+170,bottom+50,50,14,Compat.translatable("screen.wands.limit"));
        arealim_spn.label_side=true;
        wdgets.add(arealim_spn);

        tunnel_w=valSpinner(Value.TUNNEL_W,left+190,bottom+25,50,14,Compat.translatable("screen.wands.tunnel_w"));
        tunnel_w.label_side=true;
        wdgets.add(tunnel_w);

        tunnel_h=valSpinner(Value.TUNNEL_H,left+190,bottom+40,50,14,Compat.translatable("screen.wands.tunnel_h"));
        tunnel_h.label_side=true;
        wdgets.add(tunnel_h);

        tunnel_d=valSpinner(Value.TUNNEL_DEPTH,left+190,bottom+55,50,14,Compat.translatable("screen.wands.tunnel_depth"));
        tunnel_d.label_side=true;
        wdgets.add(tunnel_d);

        tunnel_ox=valSpinner(Value.TUNNEL_OX,left+190,bottom+70,50,14,Compat.translatable("screen.wands.tunnel_ox"));
        tunnel_ox.label_side=true;
        wdgets.add(tunnel_ox);

        tunnel_oy=valSpinner(Value.TUNNEL_OY,left+190,bottom+85,50,14,Compat.translatable("screen.wands.tunnel_oy"));
        tunnel_oy.label_side=true;
        wdgets.add(tunnel_oy);

        modes_grp=new Select(left+80,bottom+5,btn_w,btn_h,Compat.translatable("screen.wands.mode"));
        int l=WandProps.modes.length-1;
        if(wand_item.can_blast){
            l+=1;
        }
        for (int i=0;i<l;i++) {
            int finalI=i;
            Btn b=new Btn(Compat.translatable(WandProps.modes[i].toString())){
                public void onClick(int mx,int my){
                    WandProps.setMode(wand_stack, WandProps.modes[finalI]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            modes_grp.add(b);
        }
        wdgets.add(modes_grp);

        action_grp =new Select(left+10,bottom+5,btn_w,btn_h,Compat.translatable("screen.wands.action"));
        for (int i=0;i<WandProps.actions.length;i++) {
            int finalp=i;
            Btn b=new Btn(Compat.translatable(WandProps.actions[i].toString())){
                public void onClick(int mx,int my){
                    WandProps.setAction(wand_stack, WandProps.actions[finalp]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            if((WandProps.actions[i]== WandProps.Action.DESTROY ||WandProps.actions[i]== WandProps.Action.REPLACE) &&
                WandsMod.config.disable_destroy_replace){
                b.disabled=true;
            }
            action_grp.add(b);
        }
        wdgets.add(action_grp);

        orientation_grp=new Select(left+170,bottom+30,btn_w,btn_h,Compat.translatable("screen.wands.orientation"));
        for (int i=0;i<WandProps.orientations.length;i++) {
            int finalo=i;
            Btn b=new Btn(Compat.translatable(WandProps.orientations[i].toString())){
                public void onClick(int mx,int my){
                    WandProps.setOrientation(wand_stack, WandProps.orientations[finalo]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            orientation_grp.add(b);
        }
        wdgets.add(orientation_grp);

        plane_grp=new Select(left+170,bottom+30,btn_w,btn_h,Compat.translatable("screen.wands.plane"));
        for (int i=0;i<WandProps.planes.length;i++) {
            int finalp=i;
            Btn b=new Btn(Compat.literal(WandProps.planes[i].toString())){
                public void onClick(int mx,int my){
                    WandProps.setPlane(wand_stack, WandProps.planes[finalp]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            plane_grp.add(b);
        }
        wdgets.add(plane_grp);

        axis_grp=new Select(left+10,bottom+114,btn_w,btn_h,Compat.translatable("screen.wands.axis"));
        for (int i=0;i<WandProps.axes.length;i++) {
            int finala=i;
            Btn b=new Btn(Compat.literal(WandProps.axes[i].toString())){
                public void onClick(int mx,int my){
                    WandProps.setAxis(wand_stack, WandProps.axes[finala]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            axis_grp.add(b);
        }
        wdgets.add(axis_grp);

        drop_pos_sel=new Select(left+80,bottom+175,btn_w+20,btn_h,null);

        drop_pos_sel.add(new Btn(Compat.translatable("screen.wands.drop_pos.player"),(int mx,int my)->{
            FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
            packet.writeBoolean(true);
            ClientRender.wand.drop_on_player=true;
            Compat.send_to_server(WandsMod.GLOBAL_SETTINGS_PACKET, packet);
        }));
        drop_pos_sel.add(new Btn(Compat.translatable("screen.wands.drop_pos.block"),(int mx,int my)->{
            FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
            packet.writeBoolean(false);
            ClientRender.wand.drop_on_player=false;
            Compat.send_to_server(WandsMod.GLOBAL_SETTINGS_PACKET, packet);
        }));
        wdgets.add(drop_pos_sel);

        mirror_axis=new Select(left+170,bottom+30,btn_w,btn_h,Compat.translatable("screen.wands.mirror"));
        for (int i=0;i<WandProps.mirrorAxes.length;i++) {
            int mo=i;
            Btn b=new Btn(Compat.literal(WandProps.mirrorAxes[i].toString())){
                public void onClick(int mx,int my){
                    WandProps.setVal(wand_stack,Value.MIRRORAXIS, mo);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            mirror_axis.add(b);
        }
        wdgets.add(mirror_axis);

        state_grp=new Select(left+80,bottom+140,btn_w+20,btn_h,null);
        Btn b1=new Btn(Compat.translatable("screen.wands.use_same_state")){
            public void onClick(int mx,int my){
                WandProps.setStateMode(wand_stack, WandProps.StateMode.CLONE);
                WandsModClient.send_wand(wand_stack);
            }
        };
        state_grp.add(b1);
        Btn b2=new Btn(Compat.translatable("screen.wands.apply_rot")){
            public void onClick(int mx,int my){
                WandProps.setStateMode(wand_stack, WandProps.StateMode.APPLY);
                WandsModClient.send_wand(wand_stack);
            }
        };
        state_grp.add(b2);
        Btn b3=new Btn(Compat.translatable("screen.wands.target")){
            public void onClick(int mx,int my){
                WandProps.setStateMode(wand_stack, WandProps.StateMode.TARGET);
                WandsModClient.send_wand(wand_stack);
            }
        };
        state_grp.add(b3);
        wdgets.add(state_grp);

        rot_grp=new Select(left+10,bottom+60,btn_w,btn_h,Compat.translatable("screen.wands.rotation"));
        for (int i=0;i<WandProps.rotations.length;i++) {
            int finalr=i;
            String rot="";
            switch(WandProps.rotations[i]) {
                case NONE:
                    rot="0°";
                    break;
                case CLOCKWISE_90:
                    rot="90°";
                    break;
                case CLOCKWISE_180:
                    rot="180°";
                    break;
                case COUNTERCLOCKWISE_90:
                    rot="270°";
                    break;
            }
            Btn b=new Btn(Compat.literal(rot)){
                public void onClick(int mx,int my){
                    WandProps.setRotation(wand_stack, WandProps.rotations[finalr]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            rot_grp.add(b);
        }
        wdgets.add(rot_grp);


        show_inv_btn=new Btn(right-80,bottom,30,12,Compat.translatable("screen.wands.tools")){
            public void onClick(int mx,int my) {
                show_inv=!show_inv;
            }
        };
        wdgets.add(show_inv_btn);


        match_state_sel=new Select(left+170,bottom+110,70,12,null);
        Btn match_state_btn=new Btn(Compat.translatable("screen.wands.match_state")){
            public void onClick(int mx,int my) {
                WandProps.toggleFlag(wand_stack, WandProps.Flag.MATCHSTATE);
                WandsModClient.send_wand(wand_stack);
            }
        };
        match_state_sel.add(match_state_btn);
        wdgets.add(match_state_sel);

        inc_sel_grp_btn=new Select(left+170,bottom+122,70,12,null);
        Btn inc_sel_btn=new Btn(Compat.translatable("screen.wands.inc_sel")){
            public void onClick(int mx,int my) {
                WandProps.toggleFlag(wand_stack, WandProps.Flag.INCSELBLOCK);
                WandsModClient.send_wand(wand_stack);
            }
        };
        inc_sel_grp_btn.add(inc_sel_btn);
        wdgets.add(inc_sel_grp_btn);
        slab_grp_btn=new Select(left+170,bottom+134,70,12,null);
        Btn slab_btn=new Btn(Compat.translatable("screen.wands.slab")){
            public void onClick(int mx,int my) {
                WandProps.toggleFlag(wand_stack, WandProps.Flag.STAIRSLAB);
                WandsModClient.send_wand(wand_stack);
            }
        };
        slab_grp_btn.add(slab_btn);
        wdgets.add(slab_grp_btn);

        inv_grp_btn=new Select(left+170,bottom+146,70,12,null);
        Btn inv_btn=new Btn(Compat.translatable("screen.wands.invert")){
            public void onClick(int mx,int my) {
                WandProps.toggleFlag(wand_stack, WandProps.Flag.INVERTED);
                WandsModClient.send_wand(wand_stack);
            }
        };
        inv_grp_btn.add(inv_btn);
        wdgets.add(inv_grp_btn);

        target_air_grp_btn=new Select(left+170,bottom+158,70,12,null);
        Btn target_air_btn=new Btn(Compat.translatable("screen.wands.target_air")){
            public void onClick(int mx,int my) {
                WandProps.toggleFlag(wand_stack, WandProps.Flag.TARGET_AIR);
                WandsModClient.send_wand(wand_stack);
            }
        };
        target_air_grp_btn.add(target_air_btn);
        wdgets.add(target_air_grp_btn);

        skip_spn=valSpinner(Value.SKIPBLOCK, left+215,bottom+180,25,14,Compat.translatable("screen.wands.skip_block"));
        skip_spn.label_side=true;
        wdgets.add(skip_spn);

        diag_grp_btn=new Select(left+150,bottom+30,100,12,null);
        Btn diag_btn=new Btn(Compat.translatable("screen.wands.diagonal_spread")){
            public void onClick(int mx,int my) {
                WandProps.toggleFlag(wand_stack, WandProps.Flag.DIAGSPREAD);
                WandsModClient.send_wand(wand_stack);
            }
        };
        diag_grp_btn.add(diag_btn);
        wdgets.add(diag_grp_btn);

        cfill_grp_btn =new Select(left+170,bottom+80,60,12,null);
        Btn fill_btn=new Btn(Compat.translatable("screen.wands.filled_circle")){
            public void onClick(int mx,int my) {
                WandProps.toggleFlag(wand_stack, WandProps.Flag.CFILLED);
                WandsModClient.send_wand(wand_stack);
            }
        };
        cfill_grp_btn.add(fill_btn);
        wdgets.add(cfill_grp_btn);

        rfill_grp_btn =new Select(left+170,bottom+30,60,12,null);
        Btn fill_btn2=new Btn(Compat.literal("fill rect")){
            public void onClick(int mx,int my) {
                WandProps.toggleFlag(wand_stack, WandProps.Flag.RFILLED);
                WandsModClient.send_wand(wand_stack);
            }
        };
        rfill_grp_btn.add(fill_btn2);
        wdgets.add(rfill_grp_btn);

        even_grp_btn=new Select(left+170,bottom+95,60,12,null);
        Btn even_btn=new Btn(Compat.translatable("screen.wands.even_circle")){
            public void onClick(int mx,int my) {
                WandProps.toggleFlag(wand_stack, WandProps.Flag.EVEN);
                WandsModClient.send_wand(wand_stack);
            }
        };
        even_grp_btn.add(even_btn);
        wdgets.add(even_grp_btn);

#if USE_CLOTHCONFIG
        if(WandsMod.platform!=2){
            Screen parent=this;
            conf_btn=new Btn(left+10,bottom+180,27,12,Compat.translatable("screen.wands.conf")){
                public void onClick(int mx,int my) {
                    Minecraft.getInstance().setScreen(WandConfigScreen.create(parent));
                }
            };
            wdgets.add(conf_btn);
        }
#endif

    }
    void update_selections(){
        if(wand_item !=null && wand_stack!=null) {
            mirror_axis.selected=WandProps.getVal(wand_stack,Value.MIRRORAXIS);
            mirror_axis.visible=modes_grp.selected==WandProps.Mode.PASTE.ordinal();
            modes_grp.selected=WandProps.getMode(wand_stack).ordinal();
            mult_spn.visible=modes_grp.selected==WandProps.Mode.DIRECTION.ordinal();
            action_grp.selected=WandProps.getAction(wand_stack).ordinal();
            orientation_grp.selected=WandProps.getOrientation(wand_stack).ordinal();
            orientation_grp.visible=modes_grp.selected==WandProps.Mode.ROW_COL.ordinal();
            row_col_spn.visible=modes_grp.selected==WandProps.Mode.ROW_COL.ordinal();
            arealim_spn.visible=(modes_grp.selected==WandProps.Mode.AREA.ordinal()||modes_grp.selected==WandProps.Mode.VEIN.ordinal());
            plane_grp.selected=WandProps.getPlane(wand_stack).ordinal();
            plane_grp.visible=modes_grp.selected==WandProps.Mode.CIRCLE.ordinal();
            axis_grp.selected =WandProps.getAxis(wand_stack).ordinal();
            state_grp.selected=WandProps.getStateMode(wand_stack).ordinal();
            rot_grp.selected=WandProps.getRotation(wand_stack).ordinal();
            inv_grp_btn.selected=(WandProps.getFlag(wand_stack, WandProps.Flag.INVERTED)?0:-1);
            inc_sel_grp_btn.selected=(WandProps.getFlag(wand_stack, WandProps.Flag.INCSELBLOCK)?0:-1);
            cfill_grp_btn.selected=(WandProps.getFlag(wand_stack, WandProps.Flag.CFILLED)?0:-1);
            cfill_grp_btn.visible=modes_grp.selected==WandProps.Mode.CIRCLE.ordinal();
            rfill_grp_btn.selected=(WandProps.getFlag(wand_stack, WandProps.Flag.RFILLED)?0:-1);
            rfill_grp_btn.visible=modes_grp.selected==WandProps.Mode.FILL.ordinal();
            even_grp_btn.selected=(WandProps.getFlag(wand_stack, WandProps.Flag.EVEN)?0:-1);
            even_grp_btn.visible=modes_grp.selected==WandProps.Mode.CIRCLE.ordinal();
            grid_n_spn.visible=modes_grp.selected==WandProps.Mode.GRID.ordinal();
            grid_m_spn.visible=modes_grp.selected==WandProps.Mode.GRID.ordinal();
            grid_moff_spn.visible=modes_grp.selected==WandProps.Mode.GRID.ordinal();
            grid_noff_spn.visible=modes_grp.selected==WandProps.Mode.GRID.ordinal();
            grid_mskp_spn.visible=modes_grp.selected==WandProps.Mode.GRID.ordinal();
            grid_nskp_spn.visible=modes_grp.selected==WandProps.Mode.GRID.ordinal();
            blast_radius_spn.visible=modes_grp.selected==WandProps.Mode.BLAST.ordinal();
            slab_grp_btn.selected=(WandProps.getFlag(wand_stack, WandProps.Flag.STAIRSLAB)?0:-1);
            diag_grp_btn.visible=modes_grp.selected==WandProps.Mode.AREA.ordinal();
            diag_grp_btn.selected=(!WandProps.getFlag(wand_stack, WandProps.Flag.DIAGSPREAD)?0:-1);
            match_state_sel.selected=(WandProps.getFlag(wand_stack, WandProps.Flag.MATCHSTATE)?0:-1);
            target_air_grp_btn.selected=(WandProps.getFlag(wand_stack, WandProps.Flag.TARGET_AIR)?0:-1);
            drop_pos_sel.selected=(ClientRender.wand.drop_on_player ?0:-1);
            tunnel_w.visible=modes_grp.selected==WandProps.Mode.TUNNEL.ordinal();
            tunnel_h.visible=modes_grp.selected==WandProps.Mode.TUNNEL.ordinal();
            tunnel_d.visible=modes_grp.selected==WandProps.Mode.TUNNEL.ordinal();
            tunnel_ox.visible=modes_grp.selected==WandProps.Mode.TUNNEL.ordinal();
            tunnel_oy.visible=modes_grp.selected==WandProps.Mode.TUNNEL.ordinal();
        }
    }
    @Override 
    #if MC < "1200"
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
    #else
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
    #endif

        Compat.set_color(1.0F, 1.0F, 1.0F, 1.0F);
        Compat.set_texture(BG_TEX);
        Compat.set_pos_tex_shader();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        int x = ((width - img_w) / 2)-xoff;
        int y = ((height - img_h) / 2)-yoff;

        if(show_inv) {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            Compat.set_texture(INV_TEX);
            x =( (width - imageWidth) / 2);
            y =( (height - imageHeight) / 2);
            #if MC < "1200"
                blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);
                super.render(poseStack,mouseX,mouseY,delta);
                show_inv_btn.render(poseStack,this.font,mouseX,mouseY);
            #else
                gui.blit(INV_TEX, x, y, 0, 0, imageWidth, imageHeight);
                super.render(gui,mouseX,mouseY,delta);
                show_inv_btn.render(gui,this.font,mouseX,mouseY);
            #endif
        }else{
            #if MC < "1200"
                blit(poseStack, x, y, 0, 0, img_w, img_h);
            #else
                gui.blit(BG_TEX, x, y, 0, 0, img_w, img_h);
            #endif
            update_selections();
            for (Wdgt wdget : wdgets) {
                if (wdget.visible) {
                    #if MC < "1200"
                        wdget.render(poseStack, this.font, mouseX, mouseY);
                    #else
                        wdget.render(gui, this.font, mouseX, mouseY);
                    #endif
                }
            }
            x = ((width - img_w)/2 +48)-xoff;
            y = (((height - img_h) / 2)+22)-yoff;
            for (int i=0;i<9;i++) {
                Slot s=this.menu.slots.get(36+i);
                int xx=x+i*18;
                #if MC < "1200"
                    #if MC <= "1193"
                        this.itemRenderer.renderAndDecorateItem(s.getItem(),xx, y);
                        this.itemRenderer.renderGuiItemDecorations(this.font, s.getItem(),xx, y , null);
                    #else
                        this.itemRenderer.renderAndDecorateItem(poseStack,s.getItem(),xx, y);
                        this.itemRenderer.renderGuiItemDecorations(poseStack,this.font, s.getItem(),xx, y , null);
                    #endif
                #else
                    gui.renderFakeItem(s.getItem(),xx, y);
                    gui.renderItemDecorations(font,s.getItem(),xx, y);
                    //this.itemRenderer.renderAndDecorateItem(poseStack,s.getItem(),xx, y);
                    //this.itemRenderer.renderGuiItemDecorations(poseStack,this.font, s.getItem(),xx, y , null);
                #endif
                if(mouseX>xx && mouseX<xx+16 && mouseY>y && mouseY<y+16) {
                    this.hoveredSlot=s;
                }
            }
        }
        #if MC < "1200"
            this.renderTooltip(poseStack, mouseX,mouseY);
        #else
            this.renderTooltip(gui, mouseX,mouseY);
        #endif
        RenderSystem.depthMask(true);
    }
    @Override
    #if MC < "1200"
        protected void renderBg(PoseStack poseStack, float f, int i, int j) {
    #else
        protected void renderBg(GuiGraphics gui, float f, int i, int j) {
    #endif

    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!show_inv) {
            for (Wdgt wdget : wdgets) {
                wdget.click((int) mouseX, (int) mouseY);
            }            
        }else{
            super.mouseClicked(mouseX, mouseY, button);
            show_inv_btn.click((int)mouseX,(int)mouseY);
        }
        
        return true;
    }

    @Override
    public boolean keyPressed(int i, int j, int k)
    {
        if ((WandsModClient.wand_menu_km.matches(i, j) || i==256) ) {
            if(show_inv) {
                show_inv = false;
            }else{
                onClose();
            }
            return true;
        }else {
            return super.keyPressed(i, j, k);
        }
    }
}