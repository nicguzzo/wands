package net.nicguzzo.wands.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.screens.Screen;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.gui.Btn;
import net.nicguzzo.wands.client.gui.Select;
import net.nicguzzo.wands.client.gui.Spinner;
import net.nicguzzo.wands.client.gui.Wdgt;
import net.nicguzzo.wands.menues.WandMenu;
import net.nicguzzo.wands.items.WandItem.Value;
import net.nicguzzo.wands.utils.Compat;

import java.util.Vector;

public class WandScreen extends AbstractContainerScreen<WandMenu> {
    ItemStack wand_stack=null;
    WandItem wand=null;
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
    Select fill_grp_btn;
    Select even_grp_btn;
    Select rot_grp;
    Select state_grp;
    Select slab_grp_btn;
    Select diag_grp_btn;
    Select inc_sel_grp_btn;
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
    Select match_state_sel;
    Btn show_inv_btn;
    boolean show_inv=false;
    int left;
    int right;
    int bottom;
    int top;
    public WandScreen(WandMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }
    private Spinner valSpinner(WandItem.Value val,int x,int y,int w,int h,Component label) {
        int v=WandItem.getVal(wand_stack, val);
        return new Spinner(v, val.min, val.max,x,y,w,h,label) {
            public void onInc(int mx, int my, int value) {
                WandItem.setVal(wand_stack, val, value);
                WandsModClient.send_wand(wand_stack);
            }
            public void onDec(int mx, int my, int value) {
                WandItem.setVal(wand_stack, val, value);
                WandsModClient.send_wand(wand_stack);
            }
        };
    }

    @Override
    public void init(){
        super.init();
        wand_stack=this.menu.wand;
        if(wand_stack!=null && wand_stack.getItem() instanceof WandItem){
            wand=(WandItem)wand_stack.getItem();
        }else{
            return;
        }
        int btn_h=12;
        int btn_w=60;
        int btn_margin=2;
        int h2=btn_h+btn_margin;
        int h=WandItem.modes.length*h2;
        left=(width/2)-(img_w/2);
        right=(width/2)+(img_w/2);
        bottom=(height/2)-(h/2)-12;
        top=(height/2)+(h/2);


        mult_spn=valSpinner(Value.MULTIPLIER,left+200,bottom+25,25,14, Compat.translatable("screen.wands.multiplier"));
        mult_spn.label_side=true;
        wdgets.add(mult_spn);

        row_col_spn=valSpinner(Value.ROWCOLLIM,left+200,bottom+70,25,14,Compat.translatable("screen.wands.limit"));
        row_col_spn.label_side=true;
        wdgets.add(row_col_spn);

        blast_radius_spn=valSpinner(Value.BLASTRAD,left+210,bottom+25,25,14,Compat.translatable("screen.wands.blast_radius"));
        blast_radius_spn.label_side=true;
        blast_radius_spn.inc_val=2;
        blast_radius_spn.shift_inc_val=4;
        wdgets.add(blast_radius_spn);

        grid_m_spn=new Spinner(WandItem.getVal(wand_stack,Value.GRIDM),1,wand.limit,left+180,bottom+25,25,14,Compat.translatable("screen.wands.grid_mxn"))
        {
            public void onInc(int mx,int my,int value){
                WandItem.incGrid(wand_stack,Value.GRIDM,1);
                WandsModClient.send_wand(wand_stack);
            }
            public void onDec(int mx,int my,int value){
                WandItem.decVal(wand_stack,Value.GRIDM,1);
                WandsModClient.send_wand(wand_stack);
            }
        };
        grid_m_spn.label_side=true;
        wdgets.add(grid_m_spn);

        grid_n_spn=new Spinner(WandItem.getVal(wand_stack,Value.GRIDN),1,wand.limit,left+215,bottom+25,25,14,Compat.literal("x"))
        {
            public void onInc(int mx,int my,int value){
                WandItem.incGrid(wand_stack,Value.GRIDN,1);
                WandsModClient.send_wand(wand_stack);
            }
            public void onDec(int mx,int my,int value){
                WandItem.decVal(wand_stack,Value.GRIDN,1);
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

        arealim_spn=valSpinner(Value.AREALIM, left+200,bottom+50,25,14,Compat.translatable("screen.wands.limit"));
        arealim_spn.label_side=true;
        wdgets.add(arealim_spn);

        modes_grp=new Select(left+80,bottom+10,btn_w,btn_h,Compat.translatable("screen.wands.mode"));
        int l=WandItem.modes.length-1;
        if(wand.can_blast){
            l+=1;
        }
        for (int i=0;i<l;i++) {
            int finalI=i;
            Btn b=new Btn(Compat.literal(WandItem.modes[i].toString())){
                public void onClick(int mx,int my){
                    WandItem.setMode(wand_stack, WandItem.modes[finalI]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            modes_grp.add(b);
        }
        wdgets.add(modes_grp);

        action_grp =new Select(left+10,bottom+5,btn_w,btn_h,Compat.translatable("screen.wands.action"));
        for (int i=0;i<WandItem.actions.length;i++) {
            int finalp=i;
            Btn b=new Btn(Compat.literal(WandItem.actions[i].toString())){
                public void onClick(int mx,int my){
                    WandItem.setAction(wand_stack, WandItem.actions[finalp]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            action_grp.add(b);
        }
        wdgets.add(action_grp);

        orientation_grp=new Select(left+170,bottom+30,btn_w,btn_h,Compat.translatable("screen.wands.orientation"));
        for (int i=0;i<WandItem.orientations.length;i++) {
            int finalo=i;
            Btn b=new Btn(Compat.literal(WandItem.orientations[i].toString())){
                public void onClick(int mx,int my){
                    WandItem.setOrientation(wand_stack, WandItem.orientations[finalo]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            orientation_grp.add(b);
        }
        wdgets.add(orientation_grp);

        plane_grp=new Select(left+170,bottom+30,btn_w,btn_h,Compat.translatable("screen.wands.plane"));
        for (int i=0;i<WandItem.planes.length;i++) {
            int finalp=i;
            Btn b=new Btn(Compat.literal(WandItem.planes[i].toString())){
                public void onClick(int mx,int my){
                    WandItem.setPlane(wand_stack, WandItem.planes[finalp]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            plane_grp.add(b);
        }
        wdgets.add(plane_grp);

        axis_grp=new Select(left+10,bottom+135,btn_w,btn_h,Compat.translatable("screen.wands.axis"));
        for (int i=0;i<WandItem.axes.length;i++) {
            int finala=i;
            Btn b=new Btn(Compat.literal(WandItem.axes[i].toString())){
                public void onClick(int mx,int my){
                    WandItem.setAxis(wand_stack, WandItem.axes[finala]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            axis_grp.add(b);
        }
        wdgets.add(axis_grp);


        mirror_axis=new Select(left+170,bottom+30,btn_w,btn_h,Compat.translatable("screen.wands.mirror"));
        for (int i=0;i<WandItem.mirrorAxes.length;i++) {
            int mo=i;
            Btn b=new Btn(Compat.literal(WandItem.mirrorAxes[i].toString())){
                public void onClick(int mx,int my){
                    WandItem.setVal(wand_stack,Value.MIRRORAXIS, mo);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            mirror_axis.add(b);
        }
        wdgets.add(mirror_axis);

        state_grp=new Select(left+80,bottom+170,btn_w+20,btn_h,null);
        Btn b1=new Btn(Compat.translatable("screen.wands.use_same_state")){
            public void onClick(int mx,int my){
                WandItem.setStateMode(wand_stack, WandItem.StateMode.CLONE);
                WandsModClient.send_wand(wand_stack);
            }
        };
        state_grp.add(b1);
        Btn b2=new Btn(Compat.translatable("screen.wands.apply_rot")){
            public void onClick(int mx,int my){
                WandItem.setStateMode(wand_stack, WandItem.StateMode.APPLY);
                WandsModClient.send_wand(wand_stack);
            }
        };
        state_grp.add(b2);
        Btn b3=new Btn(Compat.translatable("screen.wands.target")){
            public void onClick(int mx,int my){
                WandItem.setStateMode(wand_stack, WandItem.StateMode.TARGET);
                WandsModClient.send_wand(wand_stack);
            }
        };
        state_grp.add(b3);
        wdgets.add(state_grp);

        rot_grp=new Select(left+10,bottom+70,btn_w,btn_h,Compat.translatable("screen.wands.rotation"));
        for (int i=0;i<WandItem.rotations.length;i++) {
            int finalr=i;
            String rot="";
            switch(WandItem.rotations[i]) {
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
                    WandItem.setRotation(wand_stack, WandItem.rotations[finalr]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            rot_grp.add(b);
        }
        wdgets.add(rot_grp);


        show_inv_btn=new Btn(right-100,bottom+10,60,12,Compat.literal("Pick Tools")){
            public void onClick(int mx,int my) {
                show_inv=!show_inv;
            }
        };
        wdgets.add(show_inv_btn);


        match_state_sel=new Select(left+170,bottom+120,80,12,null);
        Btn match_state_btn=new Btn(Compat.translatable("screen.wands.match_state")){
            public void onClick(int mx,int my) {
                WandItem.toggleFlag(wand_stack, WandItem.Flag.MATCHSTATE);
                WandsModClient.send_wand(wand_stack);
            }
        };
        match_state_sel.add(match_state_btn);
        wdgets.add(match_state_sel);

        inc_sel_grp_btn=new Select(left+170,bottom+135,80,12,null);
        Btn inc_sel_btn=new Btn(Compat.translatable("screen.wands.inc_sel")){
            public void onClick(int mx,int my) {
                WandItem.toggleFlag(wand_stack, WandItem.Flag.INCSELBLOCK);
                WandsModClient.send_wand(wand_stack);
            }
        };
        inc_sel_grp_btn.add(inc_sel_btn);
        wdgets.add(inc_sel_grp_btn);
        slab_grp_btn=new Select(left+170,bottom+150,70,12,null);
        Btn slab_btn=new Btn(Compat.translatable("screen.wands.slab")){
            public void onClick(int mx,int my) {
                WandItem.toggleFlag(wand_stack, WandItem.Flag.STAIRSLAB);
                WandsModClient.send_wand(wand_stack);
            }
        };
        slab_grp_btn.add(slab_btn);
        wdgets.add(slab_grp_btn);

        inv_grp_btn=new Select(left+170,bottom+165,40,12,null);
        Btn inv_btn=new Btn(Compat.translatable("screen.wands.invert")){
            public void onClick(int mx,int my) {
                WandItem.toggleFlag(wand_stack, WandItem.Flag.INVERTED);
                WandsModClient.send_wand(wand_stack);
            }
        };
        inv_grp_btn.add(inv_btn);
        wdgets.add(inv_grp_btn);

        skip_spn=valSpinner(Value.SKIPBLOCK, left+220,bottom+180,25,14,Compat.translatable("screen.wands.skip_block"));
        skip_spn.label_side=true;
        wdgets.add(skip_spn);

        diag_grp_btn=new Select(left+150,bottom+30,100,12,null);
        Btn diag_btn=new Btn(Compat.translatable("screen.wands.diagonal_spread")){
            public void onClick(int mx,int my) {
                WandItem.toggleFlag(wand_stack, WandItem.Flag.DIAGSPREAD);
                WandsModClient.send_wand(wand_stack);
            }
        };
        diag_grp_btn.add(diag_btn);
        wdgets.add(diag_grp_btn);

        fill_grp_btn=new Select(left+170,bottom+80,60,12,null);
        Btn fill_btn=new Btn(Compat.translatable("screen.wands.filled_circle")){
            public void onClick(int mx,int my) {
                WandItem.toggleFlag(wand_stack, WandItem.Flag.FILLED);
                WandsModClient.send_wand(wand_stack);
            }
        };
        fill_grp_btn.add(fill_btn);
        wdgets.add(fill_grp_btn);

        even_grp_btn=new Select(left+170,bottom+95,60,12,null);
        Btn even_btn=new Btn(Compat.translatable("screen.wands.even_circle")){
            public void onClick(int mx,int my) {
                WandItem.toggleFlag(wand_stack, WandItem.Flag.EVEN);
                WandsModClient.send_wand(wand_stack);
            }
        };
        even_grp_btn.add(even_btn);
        wdgets.add(even_grp_btn);

#if USE_CLOTHCONFIG
        if(WandsMod.platform!=2){
            Screen parent=this;
            conf_btn=new Btn(left+210,bottom+195,27,12,Compat.literal("Conf")){
                public void onClick(int mx,int my) {
                    Minecraft.getInstance().setScreen(WandConfigScreen.create(parent));
                }
            };
            wdgets.add(conf_btn);
        }
#endif

    }
    void update_selections(){
        if(wand!=null && wand_stack!=null) {
            mirror_axis.selected=WandItem.getVal(wand_stack,Value.MIRRORAXIS);
            mirror_axis.visible=modes_grp.selected==WandItem.Mode.PASTE.ordinal();
            modes_grp.selected=WandItem.getMode(wand_stack).ordinal();
            mult_spn.visible=modes_grp.selected==WandItem.Mode.DIRECTION.ordinal();
            action_grp.selected=WandItem.getAction(wand_stack).ordinal();
            orientation_grp.selected=WandItem.getOrientation(wand_stack).ordinal();
            orientation_grp.visible=modes_grp.selected==WandItem.Mode.ROW_COL.ordinal();
            row_col_spn.visible=modes_grp.selected==WandItem.Mode.ROW_COL.ordinal();
            arealim_spn.visible=(modes_grp.selected==WandItem.Mode.AREA.ordinal()||modes_grp.selected==WandItem.Mode.VEIN.ordinal());
            plane_grp.selected=WandItem.getPlane(wand_stack).ordinal();
            plane_grp.visible=modes_grp.selected==WandItem.Mode.CIRCLE.ordinal();
            axis_grp.selected =WandItem.getAxis(wand_stack).ordinal();
            state_grp.selected=WandItem.getStateMode(wand_stack).ordinal();
            rot_grp.selected=WandItem.getRotation(wand_stack).ordinal();
            inv_grp_btn.selected=(WandItem.getFlag(wand_stack, WandItem.Flag.INVERTED)?0:-1);
            inc_sel_grp_btn.selected=(WandItem.getFlag(wand_stack, WandItem.Flag.INCSELBLOCK)?0:-1);
            fill_grp_btn.selected=(WandItem.getFlag(wand_stack, WandItem.Flag.FILLED)?0:-1);
            fill_grp_btn.visible=modes_grp.selected==WandItem.Mode.CIRCLE.ordinal()||modes_grp.selected==WandItem.Mode.RECT.ordinal();
            even_grp_btn.selected=(WandItem.getFlag(wand_stack, WandItem.Flag.EVEN)?0:-1);
            even_grp_btn.visible=modes_grp.selected==WandItem.Mode.CIRCLE.ordinal();
            grid_n_spn.visible=modes_grp.selected==WandItem.Mode.GRID.ordinal();
            grid_m_spn.visible=modes_grp.selected==WandItem.Mode.GRID.ordinal();
            grid_moff_spn.visible=modes_grp.selected==WandItem.Mode.GRID.ordinal();
            grid_noff_spn.visible=modes_grp.selected==WandItem.Mode.GRID.ordinal();
            grid_mskp_spn.visible=modes_grp.selected==WandItem.Mode.GRID.ordinal();
            grid_nskp_spn.visible=modes_grp.selected==WandItem.Mode.GRID.ordinal();
            blast_radius_spn.visible=modes_grp.selected==WandItem.Mode.BLAST.ordinal();
            slab_grp_btn.selected=(WandItem.getFlag(wand_stack, WandItem.Flag.STAIRSLAB)?0:-1);
            diag_grp_btn.visible=modes_grp.selected==WandItem.Mode.AREA.ordinal();
            diag_grp_btn.selected=(!WandItem.getFlag(wand_stack, WandItem.Flag.DIAGSPREAD)?0:-1);
            match_state_sel.selected=(WandItem.getFlag(wand_stack, WandItem.Flag.MATCHSTATE)?0:-1);
        }
    }
    @Override 
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta){

        Compat.set_color(1.0F, 1.0F, 1.0F, 1.0F);
        Compat.set_texture(BG_TEX);
        Compat.set_pos_tex_shader();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        int x = (width - img_w) / 2;
        int y = (height - img_h) / 2;

        if(show_inv) {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            Compat.set_texture(INV_TEX);
            x = (width - imageWidth) / 2;
            y = (height - imageHeight) / 2;
            blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);
            super.render(poseStack,mouseX,mouseY,delta);            
            show_inv_btn.render(poseStack,this.font,mouseX,mouseY);

        }else{
            blit(poseStack, x, y, 0, 0, img_w, img_h);            
            update_selections();
            for (int i = 0; i< wdgets.size(); i++) {
                if(wdgets.get(i).visible) {
                    wdgets.get(i).render(poseStack, this.font, mouseX, mouseY);
                }
            }
            x = (width - img_w)/2 +48;
            y = ((height - img_h) / 2)+22;
            for (int i=0;i<9;i++) {
                Slot s=this.menu.slots.get(36+i);
                int xx=x+i*18;
                this.itemRenderer.renderAndDecorateItem(s.getItem(),xx, y);
                this.itemRenderer.renderGuiItemDecorations(this.font, s.getItem(),xx, y , null);
                if(mouseX>xx && mouseX<xx+16 && mouseY>y && mouseY<y+16) {
                    this.hoveredSlot=s;
                }
            }
        }
        this.renderTooltip(poseStack, mouseX,mouseY);

        RenderSystem.depthMask(true);
    }
    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {

    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!show_inv) {
            for (int i = 0; i< wdgets.size(); i++) {
                wdgets.get(i).click((int)mouseX,(int)mouseY);
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
                return true;
            }else{
                onClose();
                return true;
            }
        }else {
            return super.keyPressed(i, j, k);
        }
    }
}