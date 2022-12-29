package net.nicguzzo.wands.client.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.items.PaletteItem;
import net.nicguzzo.wands.items.PaletteItem.PaletteMode;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.gui.Btn;
import net.nicguzzo.wands.menues.PaletteMenu;
import net.nicguzzo.wands.utils.Compat;

public class PaletteScreen extends AbstractContainerScreen<PaletteMenu> {
    
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/shulker_box.png");
    private Btn btn_mode;
    private Btn btn_rotate;
    
    
    Component mode_val;
    Component rot_on = Compat.literal("rotate: on");
    Component rot_off=Compat.literal("rotate: off");
    
    public PaletteScreen(PaletteMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }    
    @Override
    public void init(){
        super.init();
        btn_mode = new Btn((width/2)+(imageWidth/2),(height/2)-80 , 40, 20, Compat.literal("mode"), (x,y) -> {
            if(this.menu.palette!=null){
                PaletteItem.nextMode(this.menu.palette);
                WandsModClient.send_palette(true,false);
            }
        });
        btn_rotate = new Btn((width/2)+(imageWidth/2),(height/2)-60 , 40, 20, Compat.literal("rotate"), (x,y) -> {
            if(this.menu.palette!=null){
                PaletteItem.toggleRotate(this.menu.palette);
                WandsModClient.send_palette(false,true);
            }
        });
/*
        #if MC=="1165"
            this.addWidget(btn_mode);
            this.addWidget(btn_rotate);
        #else
            this.addRenderableWidget(btn_mode);
            this.addRenderableWidget(btn_rotate);
        #endif
*/
    }

    @Override 
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta){
        super.render(poseStack, mouseX, mouseY, delta);
        
        //this.btn_mode.render(poseStack, mouseX, mouseY, delta);
        //this.btn_rotate.render(poseStack, mouseX, mouseY, delta);
        
        if(this.menu.palette!=null){
            PaletteMode mode=PaletteItem.getMode(this.menu.palette);            
            switch(mode){
                case RANDOM:
                    mode_val= PaletteItem.mode_val_random;
                break;
                case ROUND_ROBIN:
                    mode_val=PaletteItem.mode_val_rr;
                break;
                default:
                    mode_val= PaletteItem.mode_val_random;
                break;
            };
            boolean rot=this.menu.palette.getOrCreateTag().getBoolean("rotate");
            this.font.draw(poseStack,(rot?rot_on:rot_off)  , (width/2)-30, (height/2)-77, 4210752);
            
            this.font.draw(poseStack,mode_val , (width/2)+30, (height/2)-77, 4210752);
            btn_mode.render(poseStack,this.font, mouseX, mouseY);
            btn_rotate.render(poseStack,this.font, mouseX, mouseY);
        }
    }
    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        Compat.set_color(1.0F, 1.0F, 1.0F, 1.0F);
        Compat.set_texture(TEXTURE);
        Compat.set_pos_tex_shader();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        blit(matrices, x, y, 0, 0, imageWidth, imageHeight);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        btn_mode.click((int)mouseX, (int)mouseY);
        btn_rotate.click((int)mouseX, (int)mouseY);
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
        return true;
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return true;
    }
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        Slot slot = this.find_slot(mouseX, mouseY);
        if(slot!=null){
            Minecraft client=Minecraft.getInstance();
            ItemStack itemStack = Compat.get_carried(client.player,this.menu);
            if(itemStack != ItemStack.EMPTY && slot.getItem() == ItemStack.EMPTY){
                this.slotClicked(slot, slot.index, button, ClickType.QUICK_CRAFT);
            }
        }
        return true;
    }
}