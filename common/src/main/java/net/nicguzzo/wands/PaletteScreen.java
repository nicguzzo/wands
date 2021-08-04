package net.nicguzzo.wands;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.PaletteItem.PaletteMode;
public class PaletteScreen extends AbstractContainerScreen<PaletteScreenHandler> {
    
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/shulker_box.png");
    private Button btn_mode;
    private Button btn_rotate;
    
    
    Component mode_val;
    Component rot_on =new TextComponent("rotate: on");
    Component rot_off=new TextComponent("rotate: off");
    
    public PaletteScreen(PaletteScreenHandler handler, Inventory inventory,Component title) {
        super(handler, inventory, title);
    }    
    @Override
    public void init(){
        super.init();
        btn_mode = new Button((width/2)+(imageWidth/2),(height/2)-80 , 40, 20, new TextComponent("mode"), (button) -> {
            if(this.menu.palette!=null){
                PaletteItem.nextMode(this.menu.palette);
                WandsModClient.send_palette(true,false);
            }
        });
        btn_rotate = new Button((width/2)+(imageWidth/2),(height/2)-60 , 40, 20, new TextComponent("rotate"), (button) -> {
            if(this.menu.palette!=null){
                PaletteItem.toggleRotate(this.menu.palette);
                WandsModClient.send_palette(false,true);
            }
        });
        this.addRenderableWidget(btn_mode);
        this.addRenderableWidget(btn_rotate);
        //this.addButton(btn_mode);
        //this.addButton(btn_rotate);
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
        }
    }
    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        //RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        //this.minecraft.getTextureManager().bind(TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        blit(matrices, x, y, 0, 0, imageWidth, imageHeight);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        btn_mode.mouseClicked(mouseX, mouseY, button);
        btn_rotate.mouseClicked(mouseX, mouseY, button);
        Slot slot = this.findSlot(mouseX, mouseY);
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
        Slot slot = this.findSlot(mouseX, mouseY);
        if(slot!=null){
            ItemStack itemStack = this.menu.getCarried();
            if(itemStack != ItemStack.EMPTY && slot.getItem() == ItemStack.EMPTY){
                this.slotClicked(slot, slot.index, button, ClickType.QUICK_CRAFT);
            }
        }
        return true;
    }
}