package net.nicguzzo.wands;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.PaletteItem.PaletteMode;
public class PaletteScreen extends AbstractContainerScreen<PaletteScreenHandler> {
    
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/shulker_box.png");
    private Button btn;
    
    static private Component mode_val_random=new TranslatableComponent("item.wands.random");
    static private Component mode_val_rr=new TranslatableComponent("item.wands.round_robin");
    Component mode_val;
    
    public PaletteScreen(PaletteScreenHandler handler, Inventory inventory,Component title) {
        super(handler, inventory, title);
        
        //mode_val_random=new TranslatableComponent("item.wands.random");
        //mode_val_rr=new TranslatableComponent("item.wands.random");
    }
    @Override
    public void init(){
        super.init();
        //update_palette_info(this.menu.palette);
        WandsMod.LOGGER.info("width: "+width+ " height: "+height);
        btn = new Button((width/2)+(imageWidth/2),(height/2)-80 , 40, 20, new TextComponent("mode"), (button) -> {
            if(this.menu.palette!=null){
                
                PaletteItem.nextMode(this.menu.palette);
                WandsModClient.send_key(WandsMod.palette_mode_key);
            }
        });
        this.addButton(btn);
    }
    //static public void update_palette_info(ItemStack palette){
    //    WandsMod.LOGGER.info("update_palette_info "+palette);
    //    if(palette!=null){
    //        PaletteMode mode=PaletteItem.getMode(palette);
    //        //WandsMod.LOGGER.info("mode"+mode);
    //    }
    //}
    @Override 
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta){
        super.render(poseStack, mouseX, mouseY, delta);
        
        this.btn.render(poseStack, mouseX, mouseY, delta);
        
        if(this.menu.palette!=null){
            PaletteMode mode=PaletteItem.getMode(this.menu.palette);            
            switch(mode){
                case RANDOM:
                    mode_val= mode_val_random;
                break;
                case ROUND_ROBIN:
                    mode_val=mode_val_rr;
                break;
                default:
                    mode_val= mode_val_random;
                break;
            };
            this.font.draw(poseStack,mode_val , width/2, (height/2)-76, 4210752);
        }
    }
    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        //RenderSystem.setShader(GameRenderer::getPositionTexShader);
        //RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        //RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        blit(matrices, x, y, 0, 0, imageWidth, imageHeight);
    }
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        btn.mouseClicked(mouseX, mouseY, button);
        Slot slot = this.findSlot(mouseX, mouseY);
        if(slot!=null){
            //System.out.println("mouseClicked "+button);
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
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return true;
    }
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        Slot slot = this.findSlot(mouseX, mouseY);
        if(slot!=null){
            ItemStack itemStack = this.menu.playerInventory.getCarried();
            if(itemStack != ItemStack.EMPTY && slot.getItem() == ItemStack.EMPTY){
                this.slotClicked(slot, slot.index, button, ClickType.QUICK_CRAFT);
            }
        }
        return true;
    }
}