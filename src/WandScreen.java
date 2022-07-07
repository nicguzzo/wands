package net.nicguzzo.wands;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.screens.Screen;
import net.nicguzzo.wands.mcver.MCVer;

import java.util.Vector;

public class WandScreen extends AbstractContainerScreen<WandScreenHandler> {
    ItemStack wand_stack=null;
    WandItem wand=null;
    static final int img_w=256;
    static final int img_h=256;
    private static final ResourceLocation BG_TEX = new ResourceLocation("wands", "textures/gui/wand.png");
    private static final ResourceLocation INV_TEX = new ResourceLocation("wands", "textures/gui/inventory.png");

    class Wdgt{
        int x;
        int y;
        int w;
        int h;
        boolean visible=true;
        public void render(PoseStack poseStack, Font font,int mx,int my){

        }
        public void click(int mx,int my){

        }
        public BufferBuilder init_quads() {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            MCVer.inst.set_render_quads_pos_col(bufferBuilder);
            return bufferBuilder;
        }
        public void end_quads() {
            Tesselator.getInstance().end();
            RenderSystem.disableBlend();
        }
        void quad(BufferBuilder bufferBuilder,float x,float y,float w, float h,float r,float g,float b, float a){
            bufferBuilder.vertex(x,   y  , 0.0F).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(x,   y+h, 0.0F).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(x+w, y+h, 0.0F).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(x+w, y  , 0.0F).color(r, g, b, a).endVertex();
        }
        public boolean inside(int mx,int my){
            return mx>=x && mx<=(x+w) && my>=y && my<=(y+h);
        }
    }
    class Btn extends Wdgt{
        int ox=2;
        int oy=3;
        Component text;
        ClientRender.Colorf c1=new ClientRender.Colorf(0.1f,0.1f,0.1f,0.8f);
        ClientRender.Colorf c2=new ClientRender.Colorf(0.4f,0.4f,0.40f,0.9f);
        boolean selected=false;
        Btn(int x,int y,int w,int h,Component text){
            this.text=text;
            this.x=x;
            this.y=y;
            this.w=w;
            this.h=h;
        }
        Btn(Component text){
            this(0,0,0,0,text);
        }
        void onClick(int mx,int my){

        }
        public void click(int mx,int my){
            if (inside(mx, my)) {
                onClick(mx,my);
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F, 1.0F));
            }
        }

        public void render(PoseStack poseStack, Font font,int mx,int my){
            float r,g,b,a;
            if(selected||inside(mx,my)){
                r=c2.r;g=c2.g;b=c2.b;a=c2.a;
            }else{
                r=c1.r;g=c1.g;b=c1.b;a=c1.a;
            }
            BufferBuilder bufferBuilder=init_quads();
            quad(bufferBuilder,x,y,w,h,r,g,b,a);
            if(selected) {
                quad(bufferBuilder,x-2,y-2,w+4,2,0.0f, 0.8f, 0.8f, 1.0f);
                quad(bufferBuilder,x-2,y+h,w+4,2,0.0f, 0.8f, 0.8f, 1.0f);
                quad(bufferBuilder,x-2,y-2,2,h+4,0.0f, 0.8f, 0.8f, 1.0f);
                quad(bufferBuilder,x+w,y-2,2,h+4,0.0f, 0.8f, 0.8f, 1.0f);
            }
            end_quads();
            int text_color=0xffffffff;
            if(selected)
                text_color=0xff000000;
            font.draw(poseStack,text ,x+ox,y+oy, text_color);
        }
    }
    class Spinner  extends Wdgt{
        int value;
        int min;
        int max;
        Btn inc;
        Btn dec;
        boolean label_side=false;
        Component label=null;
        Spinner(int _value,int min,int max,int x,int y,int w,int h,Component label){
            this.value=_value;
            this.min=min;
            this.max=max;
            this.x=x;
            this.y=y;
            this.w=w;
            this.h=h;
            this.label=label;
            inc=new Btn(x+w-10,y,10,h/2,MCVer.inst.literal("+"))
            {
                void onClick(int mx,int my){
                    if(value+1<= max) {
                        value++;
                        onInc(mx,my,value);
                    }
                }
            };
            inc.ox=0;
            inc.oy=0;
            dec=new Btn(x+w-10,y+h/2,10,h/2,MCVer.inst.literal("-"))
            {
                void onClick(int mx,int my){
                    if(value-1>=min) {
                        value--;
                        onDec(mx,my,value);
                    }
                }
            };
            dec.ox=0;
            dec.oy=0;
        }
        void onInc(int mx,int my,int v){
        }
        void onDec(int mx,int my,int v){
        }
        public void render(PoseStack poseStack, Font font,int mx,int my) {
            int fh=0;
            if(label!=null) {
                int lw=font.width(label);
                font.draw(poseStack, label, x-lw-1, y+3, 0xff000000);
                if(!label_side) {
                    fh = font.lineHeight;
                }
            }
            int sw=font.width(String.valueOf(value));

            BufferBuilder bufferBuilder=init_quads();
            quad(bufferBuilder,x,y+fh,w,h,0.4f,0.4f,0.40f,0.9f);
            end_quads();
            inc.y=y+fh;
            inc.render(poseStack,font,mx,my);

            font.draw(poseStack, String.valueOf(value), x+ w - 12 - sw, y + fh + 3, 0xff000000);

            dec.y=y+h/2+fh;
            dec.render(poseStack,font,mx,my);
        }
        public void click(int mx,int my){
            dec.click(mx,my);
            inc.click(mx,my);
        }
    }
    class Select extends Wdgt{
        Component label=null;
        public Vector<Btn> selections=new Vector<>();
        public int selected=-1;
        Select(int x,int y,int w,int h,Component label) {
            this.x=x;
            this.y=y;
            this.w=w;
            this.h=h;
            this.label=label;
        }

        public void add(Btn b){
            selections.add(b);
        }
        public void render(PoseStack poseStack, Font font,int mx,int my) {
            int k=0;
            if(label!=null) {
                font.draw(poseStack, label, x, y, 0xff000000);
                k++;
            }
            for (int j=0;j<selections.size();j++) {
                Btn btn=selections.get(j);
                btn.x=x;
                btn.y=y+h*k;
                btn.w=w;
                btn.h=h;
                btn.selected=(this.selected==j);
                btn.render(poseStack,font,mx,my);
                k++;
            }
        }
        public void click(int mx,int my) {
            for (int j = 0; j < selections.size(); j++) {
                selections.get(j).click(mx,my);
            }
        }
    };
    Vector<Wdgt> wdgets =new Vector<>();

    Select modes_grp;
    Select action_grp;
    Select orientation_grp;
    Select plane_grp;
    Select axis_grp;
    Select inv_grp_btn;
    Select fill_grp_btn;
    Select rot_grp;
    Select state_grp;
    Btn conf_btn;
    Btn show_inv_btn;
    Spinner mult_spn;
    Spinner grid_n_spn;
    Spinner grid_m_spn;
    //Btn button_mult_p;
    //Btn button_mult_m;
    boolean show_inv=false;
    int left;
    int right;
    int bottom;
    int top;
    public WandScreen(WandScreenHandler handler, Inventory inventory,Component title) {
        super(handler, inventory, title);
    }
    //int multiplier=1;
    @Override
    public void init(){
        super.init();
        wand_stack=this.menu.wand;
        if(wand_stack!=null && wand_stack.getItem() instanceof WandItem){
            wand=(WandItem)wand_stack.getItem();
        }else{
            return;
        }
//
        int btn_h=12;
        int btn_w=60;
        int btn_margin=2;
        int h2=btn_h+btn_margin;
        int h=WandItem.modes.length*h2;
        left=(width/2)-(img_w/2);
        right=(width/2)+(img_w/2);
        bottom=(height/2)-(h/2)-12;
        top=(height/2)+(h/2);

        //mult_grp=new BtnGroup();
        int multiplier=WandItem.getMultiplier(wand_stack);
        mult_spn=new Spinner(multiplier,1,wand.limit,left+200,bottom+25,25,14,MCVer.inst.translatable("screen.wands.multiplier"))
        {
            void onInc(int mx,int my,int value){
                WandItem.setMultiplier(wand_stack, value);
                WandsModClient.send_wand(wand_stack);
            }
            void onDec(int mx,int my,int value){
                WandItem.setMultiplier(wand_stack, value);
                WandsModClient.send_wand(wand_stack);
            }
        };
        mult_spn.label_side=true;
        int grid_n=WandItem.getGridNxM(wand_stack,false);
        grid_n_spn=new Spinner(grid_n,1,wand.limit,left+170,bottom+25,25,14,MCVer.inst.translatable("screen.wands.grid_nxm"))
        {
            void onInc(int mx,int my,int value){
                WandItem.setGridNxM(wand_stack, value,false);
                WandsModClient.send_wand(wand_stack);
            }
            void onDec(int mx,int my,int value){
                WandItem.setGridNxM(wand_stack, value,false);
                WandsModClient.send_wand(wand_stack);
            }
        };
        grid_n_spn.label_side=true;
        int grid_m=WandItem.getGridNxM(wand_stack,true);
        grid_m_spn=new Spinner(grid_m,1,wand.limit,left+205,bottom+25,25,14,MCVer.inst.literal("x"))
        {
            void onInc(int mx,int my,int value){
                WandItem.setGridNxM(wand_stack, value,true);
                WandsModClient.send_wand(wand_stack);
            }
            void onDec(int mx,int my,int value){
                WandItem.setGridNxM(wand_stack, value,true);
                WandsModClient.send_wand(wand_stack);
            }
        };
        grid_m_spn.label_side=true;
        modes_grp=new Select(left+80,bottom+5,btn_w,btn_h,MCVer.inst.translatable("screen.wands.mode"));
        for (int i=0;i<WandItem.modes.length;i++) {
            int finalI=i;
            Btn b=new Btn(MCVer.inst.literal(WandItem.modes[i].toString())){
                void onClick(int mx,int my){
                    WandItem.setMode(wand_stack, WandItem.modes[finalI]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            modes_grp.add(b);
        }
        action_grp =new Select(left+10,bottom+5,btn_w,btn_h,MCVer.inst.translatable("screen.wands.action"));
        for (int i=0;i<WandItem.actions.length;i++) {
            int finalp=i;
            Btn b=new Btn(MCVer.inst.literal(WandItem.actions[i].toString())){
                void onClick(int mx,int my){
                    WandItem.setAction(wand_stack, WandItem.actions[finalp]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            action_grp.add(b);
        }
        orientation_grp=new Select(left+170,bottom+5,btn_w,btn_h,MCVer.inst.translatable("screen.wands.orientation"));
        for (int i=0;i<WandItem.orientations.length;i++) {
            int finalo=i;
            Btn b=new Btn(MCVer.inst.literal(WandItem.orientations[i].toString())){
                void onClick(int mx,int my){
                    WandItem.setOrientation(wand_stack, WandItem.orientations[finalo]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            orientation_grp.add(b);
        }
        plane_grp=new Select(left+170,bottom+5,btn_w,btn_h,MCVer.inst.translatable("screen.wands.plane"));
        for (int i=0;i<WandItem.planes.length;i++) {
            int finalp=i;
            Btn b=new Btn(MCVer.inst.literal(WandItem.planes[i].toString())){
                void onClick(int mx,int my){
                    WandItem.setPlane(wand_stack, WandItem.planes[finalp]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            plane_grp.add(b);
        }
        axis_grp=new Select(left+10,bottom+135,btn_w,btn_h,MCVer.inst.translatable("screen.wands.axis"));
        for (int i=0;i<WandItem.axes.length;i++) {
            int finala=i;
            Btn b=new Btn(MCVer.inst.literal(WandItem.axes[i].toString())){
                void onClick(int mx,int my){
                    WandItem.setAxis(wand_stack, WandItem.axes[finala]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            axis_grp.add(b);
        }
        state_grp=new Select(left+80,bottom+160,btn_w+10,btn_h,null);
        Btn b1=new Btn(MCVer.inst.translatable("screen.wands.same_state")){
            void onClick(int mx,int my){
                WandItem.setStateMode(wand_stack, WandItem.StateMode.CLONE);
                WandsModClient.send_wand(wand_stack);
            }
        };
        state_grp.add(b1);
        Btn b2=new Btn(MCVer.inst.translatable("screen.wands.diff_state")){
            void onClick(int mx,int my){
                WandItem.setStateMode(wand_stack, WandItem.StateMode.APPLY);
                WandsModClient.send_wand(wand_stack);
            }
        };
        state_grp.add(b2);

        rot_grp=new Select(left+10,bottom+70,btn_w,btn_h,MCVer.inst.translatable("screen.wands.rotation"));
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
            Btn b=new Btn(MCVer.inst.literal(rot)){
                void onClick(int mx,int my){
                    WandItem.setRotation(wand_stack, WandItem.rotations[finalr]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            rot_grp.add(b);
        }
        //Select show_grp_btn=new Select(null);
        show_inv_btn=new Btn(right-70,bottom-21,60,12,MCVer.inst.literal("Pick Tools")){
            void onClick(int mx,int my) {
                show_inv=!show_inv;
            }
        };
        //show_grp_btn.add(show_inv_btn);

        inv_grp_btn=new Select(left+170,bottom+145,40,12,null);
        Btn inv_btn=new Btn(MCVer.inst.literal("Invert")){
            void onClick(int mx,int my) {
                boolean inv =!WandItem.isInverted(wand_stack);
                WandItem.setInvert(wand_stack,inv);
                WandsModClient.send_wand(wand_stack);
            }
        };
        inv_grp_btn.add(inv_btn);

        fill_grp_btn=new Select(left+170,bottom+80,60,12,null);
        Btn fill_btn=new Btn(MCVer.inst.literal("Filled circle")){
            void onClick(int mx,int my) {
                boolean fill =!WandItem.isCircleFill(wand_stack);
                WandItem.setFill(wand_stack,fill);
                WandsModClient.send_wand(wand_stack);
            }
        };
        fill_grp_btn.add(fill_btn);

        Screen parent=this;
        conf_btn=new Btn(left+210,bottom+175,27,12,MCVer.inst.literal("Conf")){
            void onClick(int mx,int my) {
                Minecraft.getInstance().setScreen(WandConfigScreen.create(parent));
            }
        };
        //show_grp_btn.add(conf_btn);

        wdgets.add(modes_grp);
        wdgets.add(action_grp);
        wdgets.add(orientation_grp);
        wdgets.add(plane_grp);
        wdgets.add(state_grp);
        wdgets.add(axis_grp);
        wdgets.add(rot_grp);
        wdgets.add(conf_btn);
        wdgets.add(inv_grp_btn);
        wdgets.add(fill_grp_btn);
        wdgets.add(mult_spn);
        wdgets.add(grid_n_spn);
        wdgets.add(grid_m_spn);

    }
    void update_selections(){
        if(wand!=null && wand_stack!=null) {
            modes_grp.selected=WandItem.getMode(wand_stack).ordinal();
            mult_spn.visible=modes_grp.selected==0;
            action_grp.selected=WandItem.getAction(wand_stack).ordinal();
            orientation_grp.selected=WandItem.getOrientation(wand_stack).ordinal();
            orientation_grp.visible=modes_grp.selected==1;
            plane_grp.selected=WandItem.getPlane(wand_stack).ordinal();
            plane_grp.visible=modes_grp.selected==6;
            axis_grp.selected =WandItem.getAxis(wand_stack).ordinal();
            state_grp.selected=WandItem.getStateMode(wand_stack).ordinal();
            rot_grp.selected=WandItem.getRotation(wand_stack).ordinal();
            inv_grp_btn.selected=(WandItem.isInverted(wand_stack)?0:-1);
            fill_grp_btn.selected=(WandItem.isCircleFill(wand_stack)?0:-1);
            fill_grp_btn.visible=modes_grp.selected==6||modes_grp.selected==7;
            grid_n_spn.visible=modes_grp.selected==4;
            grid_m_spn.visible=modes_grp.selected==4;
        }
    }
    @Override 
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta){

        MCVer.inst.set_color(1.0F, 1.0F, 1.0F, 1.0F);
        MCVer.inst.set_texture(BG_TEX);
        MCVer.inst.set_pos_tex_shader();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        int x = (width - img_w) / 2;
        int y = (height - img_h) / 2;
        blit(poseStack, x, y, 0, 0, img_w, img_h);

        /*font.draw(poseStack,text_mode ,left+35,bottom+10, 0xff000000);
        font.draw(poseStack,text_action ,left+100,bottom+10, 0xff000000);
        font.draw(poseStack,text_orientation ,left+180,bottom+10, 0xff000000);
        font.draw(poseStack,text_plane ,left+180,bottom+50, 0xff000000);
        font.draw(poseStack,text_axis  ,left+180,bottom+115, 0xff000000);
        font.draw(poseStack,text_rot  ,left+100,bottom+115, 0xff000000);*/

        for (int i=0;i<4;i++) {
            Slot s=this.menu.slots.get(36+i);
            x = (width - img_w)/2 +75;
            y = (height - img_h) / 2;
            this.itemRenderer.renderAndDecorateItem(s.getItem(),x+i*30, y+24);
            this.itemRenderer.renderGuiItemDecorations(this.font, s.getItem(),x+i*30, y+24 , null);
        }
        show_inv_btn.render(poseStack,this.font,mouseX,mouseY);
        if(show_inv) {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            MCVer.inst.set_texture(INV_TEX);
            x = (width - imageWidth) / 2;
            y = (height - imageHeight) / 2;
            blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);
            super.render(poseStack,mouseX,mouseY,delta);
            this.renderTooltip(poseStack, mouseX,mouseY);
        }else{
            update_selections();
            for (int i = 0; i< wdgets.size(); i++) {
                if(wdgets.get(i).visible) {
                    wdgets.get(i).render(poseStack, this.font, mouseX, mouseY);
                }
            }
        }
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
            show_inv_btn.click((int)mouseX,(int)mouseY);
        }else{
            Slot slot = this.find_slot(mouseX, mouseY);
            if(slot!=null){
                switch(button){
                    case 0:
                        if (hasShiftDown()) {
                            this.slotClicked(slot, slot.index, button, ClickType.QUICK_MOVE);
                        }else{
                            this.slotClicked(slot, slot.index, button, ClickType.PICKUP);
                        }
                        break;
                    case 1:
                        this.slotClicked(slot, slot.index, button, ClickType.PICKUP);
                        break;
                    case 2:
                        this.slotClicked(slot, slot.index, button, ClickType.CLONE);
                        break;
                }
            }

        }
        return true;
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        /*if(show_inv) {
            super.mouseReleased( mouseX,  mouseY,  button);
        }*/
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if(show_inv) {
            Slot slot = this.find_slot(mouseX, mouseY);
            if(slot!=null){
                Minecraft client=Minecraft.getInstance();
                ItemStack itemStack = MCVer.inst.get_carried(client.player,this.menu);
                if(itemStack != ItemStack.EMPTY && slot.getItem() == ItemStack.EMPTY){
                    this.slotClicked(slot, slot.index, button, ClickType.QUICK_CRAFT);
                }
            }
        }
        return true;
    }
    boolean is_hovering(int i, int j, int k, int l, double d, double e) {
        int m = this.leftPos;
        int n = this.topPos;
        d -= (double)m;
        e -= (double)n;
        return d >= (double)(i - 1) && d < (double)(i + k + 1) && e >= (double)(j - 1) && e < (double)(j + l + 1);
    }
    public final Slot find_slot(double d, double e) {
        for(int i = 0; i < this.menu.slots.size(); ++i) {
            Slot slot = (Slot)this.menu.slots.get(i);
            if (is_hovering(slot.x, slot.y, 16, 16, d, e) && slot.isActive()) {
                return slot;
            }
        }
        return null;
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