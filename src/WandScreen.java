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
    //private Button[] btn_mode=new Button[WandItem.max_mode];
    //private Button btn_rotate;
    class Btn{
        int x;
        int y;
        int w;
        int h;
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
        void onClick(int mx,int my){

        }
        public void click(int mx,int my){
            if (inside(mx, my)) {
                onClick(mx,my);
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F, 1.0F));

            }
        }
        public boolean inside(int mx,int my){
            return mx>=x && mx<=(x+w) && my>=y && my<=(y+h);
        }
        public void render(PoseStack poseStack, Font font,int mx,int my){
            float r,g,b,a;

            if(selected||inside(mx,my)){
                r=c2.r;g=c2.g;b=c2.b;a=c2.a;
            }else{
                r=c1.r;g=c1.g;b=c1.b;a=c1.a;
            }
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            //Matrix4f matrix4f=poseStack.last().pose();
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            MCVer.inst.set_render_quads_pos_col(bufferBuilder);
            //RenderSystem.setShader(GameRenderer::getPositionColorShader);
            //bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            quad(bufferBuilder,x,y,w,h,r,g,b,a);

            if(selected) {
                quad(bufferBuilder,x-2,y-2,w+4,2,0.0f, 0.8f, 0.8f, 1.0f);
                quad(bufferBuilder,x-2,y+h,w+4,2,0.0f, 0.8f, 0.8f, 1.0f);
                quad(bufferBuilder,x-2,y-2,2,h+4,0.0f, 0.8f, 0.8f, 1.0f);
                quad(bufferBuilder,x+w,y-2,2,h+4,0.0f, 0.8f, 0.8f, 1.0f);
            }
            Tesselator.getInstance().end();
            int text_color=0xffffffff;
            if(selected)
                text_color=0xff000000;
            font.draw(poseStack,text ,x+2,y+3, text_color);
            RenderSystem.disableBlend();
        }
        void quad(BufferBuilder bufferBuilder,float x,float y,float w, float h,float r,float g,float b, float a){
            bufferBuilder.vertex(x,   y  , 0.0F).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(x,   y+h, 0.0F).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(x+w, y+h, 0.0F).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(x+w, y  , 0.0F).color(r, g, b, a).endVertex();
        }
    }

    //Btn[] mode_btns=new Btn[WandItem.modes.length];
    //Btn[] mode2_btns=new Btn[WandItem.place_modes.length];

    Component axis;
    class BtnGroup{
        public Vector<Btn> selections=new Vector<>();
        public int selected=-1;
        public void add(Btn b){
            selections.add(b);
        }
    };
    Vector<BtnGroup> buttons=new Vector<>();

    BtnGroup modes_grp;
    BtnGroup action_grp;
    BtnGroup orientation_grp;
    BtnGroup plane_grp;
    BtnGroup axis_grp;
    BtnGroup inv_grp_btn;
    BtnGroup fill_grp_btn;
    BtnGroup rot_grp;
    BtnGroup state_grp;
    Btn conf_btn;
    Btn show_inv_btn;
    Btn button_mult_p;
    Btn button_mult_m;
    boolean show_inv=false;

    Component text_mode=MCVer.inst.translatable("screen.wands.mode");
    Component text_action=MCVer.inst.literal("Action");
    Component text_orientation=MCVer.inst.literal("Orientation");
    Component text_plane=MCVer.inst.literal("Plane");
    Component text_axis=MCVer.inst.literal("Axis");
    Component text_rot=MCVer.inst.literal("Rotation");
    int left;
    int right;
    int bottom;
    int top;
    public WandScreen(WandScreenHandler handler, Inventory inventory,Component title) {
        super(handler, inventory, title);
    }
    int multiplier=1;
    @Override
    public void init(){
        super.init();
        wand_stack=this.menu.wand;
        if(wand_stack!=null && wand_stack.getItem() instanceof WandItem){
            wand=(WandItem)wand_stack.getItem();
        }else{
            return;
        }
        multiplier=WandItem.getMultiplier(wand_stack);
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

        button_mult_p=new Btn(left+22,bottom+20,10,12,MCVer.inst.literal("+"))
        {
            void onClick(int mx,int my){
                if(multiplier+1<= wand.limit) {
                    multiplier++;
                    WandItem.setMultiplier(wand_stack, multiplier);
                    WandsModClient.send_wand(wand_stack);
                }
            }
        };
        button_mult_m=new Btn(left,bottom+20,10,12,MCVer.inst.literal("-"))
        {
            void onClick(int mx,int my){
                if(multiplier-1>=1) {
                    multiplier--;
                    WandItem.setMultiplier(wand_stack, multiplier);
                    WandsModClient.send_wand(wand_stack);
                }
            }
        };
        modes_grp=new BtnGroup();
        for (int i=0;i<WandItem.modes.length;i++) {
            int finalI=i;
            Btn b=new Btn(left+35,bottom+h2*i +20,btn_w,btn_h,
                    MCVer.inst.literal(WandItem.modes[i].toString())){
                void onClick(int mx,int my){
                    //WandsModClient.send_wand(finalI,-1,-1,-1,-1,-1,-1,-1,-1);
                    WandItem.setMode(wand_stack, WandItem.modes[finalI]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            modes_grp.add(b);
        }
        action_grp =new BtnGroup();
        for (int i=0;i<WandItem.actions.length;i++) {
            int finalp=i;
            Btn b=new Btn(left+100,bottom+h2*i  +20,btn_w+10,btn_h,
                    MCVer.inst.literal(WandItem.actions[i].toString())){
                void onClick(int mx,int my){
                    //WandsModClient.send_wand(-1,finalp,-1,-1,-1,-1,-1,-1,-1);
                    WandItem.setAction(wand_stack, WandItem.actions[finalp]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            action_grp.add(b);
        }
        orientation_grp=new BtnGroup();
        for (int i=0;i<WandItem.orientations.length;i++) {
            int finalo=i;
            Btn b=new Btn(left+180,bottom+h2*i  +20,btn_w,btn_h,
                    MCVer.inst.literal(WandItem.orientations[i].toString())){
                void onClick(int mx,int my){
                    //WandsModClient.send_wand(-1,-1,finalo,-1,-1,-1,-1,-1,-1);
                    WandItem.setOrientation(wand_stack, WandItem.orientations[finalo]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            orientation_grp.add(b);
        }
        plane_grp=new BtnGroup();
        for (int i=0;i<WandItem.planes.length;i++) {
            int finalp=i;
            Btn b=new Btn(left+180,bottom+h2*i +60 ,btn_w,btn_h,
                    MCVer.inst.literal(WandItem.planes[i].toString())){
                void onClick(int mx,int my){
                    //WandsModClient.send_wand(-1,-1,-1,finalp,-1,-1,-1,-1,-1);
                    WandItem.setPlane(wand_stack, WandItem.planes[finalp]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            plane_grp.add(b);
        }
        axis_grp=new BtnGroup();

        for (int i=0;i<WandItem.axes.length;i++) {
            int finala=i;
            Btn b=new Btn(left+180,bottom+h2*i +110+h2,btn_w,btn_h,
                    MCVer.inst.literal(WandItem.axes[i].toString())){
                void onClick(int mx,int my){
                    //WandsModClient.send_wand(-1,-1,-1,-1,finala,-1,-1,-1,-1);
                    WandItem.setAxis(wand_stack, WandItem.axes[finala]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            axis_grp.add(b);
        }
        state_grp=new BtnGroup();
        Btn b1=new Btn(left+100,bottom+h2 +70,btn_w+10,btn_h,
                MCVer.inst.literal("same state")){
            void onClick(int mx,int my){
                //WandsModClient.send_wand(-1,-1,-1,-1,-1,-1,-1,-1,0);
                WandItem.setStateMode(wand_stack, WandItem.StateMode.CLONE);
                WandsModClient.send_wand(wand_stack);
            }
        };
        state_grp.add(b1);
        Btn b2=new Btn(left+100,bottom+h2 +70+h2,btn_w+10,btn_h,
                MCVer.inst.literal("rot/axis/slab")){
            void onClick(int mx,int my){
                //WandsModClient.send_wand(-1,-1,-1,-1,-1,-1,-1,-1,1);
                WandItem.setStateMode(wand_stack, WandItem.StateMode.APPLY);
                WandsModClient.send_wand(wand_stack);
            }
        };
        state_grp.add(b2);


        rot_grp=new BtnGroup();
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
            Btn b=new Btn(left+100,bottom+h2*i +110+h2,btn_w+10,btn_h,
                    MCVer.inst.literal(rot)){
                void onClick(int mx,int my){
                    //WandsModClient.send_wand(-1,-1,-1,-1,-1,-1,-1,finalr,-1);
                    WandItem.setRotation(wand_stack, WandItem.rotations[finalr]);
                    WandsModClient.send_wand(wand_stack);
                }
            };
            rot_grp.add(b);
        }
        BtnGroup show_grp_btn=new BtnGroup();
        show_inv_btn=new Btn(right-70,bottom-21,60,12,MCVer.inst.literal("Pick Tools")){
            void onClick(int mx,int my) {
                show_inv=!show_inv;
            }
        };
        show_grp_btn.add(show_inv_btn);

        inv_grp_btn=new BtnGroup();
        Btn inv_btn=new Btn(left+35,bottom+148,40,12,MCVer.inst.literal("Invert")){
            void onClick(int mx,int my) {
                boolean inv =!WandItem.isInverted(wand_stack);
                //WandsModClient.send_wand(-1,-1,-1,-1,-1,(inv?1:0),-1,-1,-1);
                WandItem.setInvert(wand_stack,inv);
                WandsModClient.send_wand(wand_stack);
            }
        };
        inv_grp_btn.add(inv_btn);

        fill_grp_btn=new BtnGroup();
        Btn fill_btn=new Btn(left+35,bottom+162,60,12,MCVer.inst.literal("Filled circle")){
            void onClick(int mx,int my) {
                boolean fill =!WandItem.isCircleFill(wand_stack);
                //WandsModClient.send_wand(-1,-1,-1,-1,-1,-1,(fill?1:0),-1,-1);
                WandItem.setFill(wand_stack,fill);
                WandsModClient.send_wand(wand_stack);
            }
        };
        fill_grp_btn.add(fill_btn);

        Screen parent=(Screen)this;
        conf_btn=new Btn(left+210,bottom+175,27,12,MCVer.inst.literal("Conf")){
            void onClick(int mx,int my) {
                Minecraft.getInstance().setScreen(WandConfigScreen.create(parent));
            }
        };
        show_grp_btn.add(conf_btn);

        buttons.add(modes_grp);
        buttons.add(action_grp);
        buttons.add(orientation_grp);
        buttons.add(plane_grp);
        buttons.add(state_grp);
        buttons.add(axis_grp);
        buttons.add(rot_grp);
        buttons.add(show_grp_btn);
        buttons.add(inv_grp_btn);
        buttons.add(fill_grp_btn);
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

        button_mult_m.render(poseStack,this.font,mouseX,mouseY);
        font.draw(poseStack,"x"+multiplier ,left+12,bottom+24, 0xff000000);
        button_mult_p.render(poseStack,this.font,mouseX,mouseY);
        font.draw(poseStack,text_mode ,left+35,bottom+10, 0xff000000);
        font.draw(poseStack,text_action ,left+100,bottom+10, 0xff000000);
        font.draw(poseStack,text_orientation ,left+180,bottom+10, 0xff000000);
        font.draw(poseStack,text_plane ,left+180,bottom+50, 0xff000000);
        font.draw(poseStack,text_axis  ,left+180,bottom+115, 0xff000000);
        font.draw(poseStack,text_rot  ,left+100,bottom+115, 0xff000000);

        for (int i=0;i<4;i++) {
            Slot s=this.menu.slots.get(36+i);
            x = (width - img_w)/2 +75;
            y = (height - img_h) / 2;
            this.itemRenderer.renderAndDecorateItem(s.getItem(),x+i*30, y+41);
            this.itemRenderer.renderGuiItemDecorations(this.font, s.getItem(),x+i*30, y+41 , null);
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
            ItemStack wand_stack=this.menu.wand;
            if(wand!=null) {
                modes_grp.selected=WandItem.getMode(wand_stack).ordinal();
                action_grp.selected=WandItem.getAction(wand_stack).ordinal();
                orientation_grp.selected=WandItem.getOrientation(wand_stack).ordinal();
                plane_grp.selected=WandItem.getPlane(wand_stack).ordinal();
                axis_grp.selected =WandItem.getAxis(wand_stack).ordinal();
                state_grp.selected=WandItem.getStateMode(wand_stack).ordinal();
                rot_grp.selected=WandItem.getRotation(wand_stack).ordinal();
                inv_grp_btn.selected=(WandItem.isInverted(wand_stack)?0:-1);
                fill_grp_btn.selected=(WandItem.isCircleFill(wand_stack)?0:-1);
            }
            for (int i=0;i<buttons.size();i++) {
                BtnGroup grp= buttons.get(i);
                for (int j=0;j<grp.selections.size();j++) {
                    grp.selections.get(j).selected=(grp.selected==j);
                    grp.selections.get(j).render(poseStack, this.font, mouseX, mouseY);
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
            for (int i=0;i<buttons.size();i++) {
                BtnGroup grp= buttons.get(i);
                for (int j=0;j<grp.selections.size();j++) {
                    grp.selections.get(j).click((int)mouseX,(int)mouseY);
                }
            }
            button_mult_p.click((int)mouseX,(int)mouseY);
            button_mult_m.click((int)mouseX,(int)mouseY);
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
            show_inv_btn.click((int)mouseX,(int)mouseY);
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