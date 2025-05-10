package net.nicguzzo.wands.client.screens;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.client.gui.Spinner;
import net.nicguzzo.wands.items.PaletteItem;
import net.nicguzzo.wands.items.PaletteItem.PaletteMode;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.gui.Btn;
import net.nicguzzo.wands.menues.PaletteMenu;
import net.nicguzzo.wands.utils.Colorf;
import net.nicguzzo.wands.utils.Compat;
import net.minecraft.client.gui.GuiGraphics;
import net.nicguzzo.wands.wand.Palette;

public class PaletteScreen extends AbstractContainerScreen<PaletteMenu> {
    
    private static final ResourceLocation TEXTURE = Compat.create_resource_mc("textures/gui/container/generic_54.png");
    private Btn btn_mode;
    private Btn btn_rotate;
    private final int containerRows;
    Component mode_val;
    Component rot_on  = Compat.literal("rotate: on");
    Component rot_off = Compat.literal("rotate: off");
    Spinner gradient_h;
    
    public PaletteScreen(PaletteMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.containerRows = 6;
        this.imageHeight = 114 + this.containerRows * 18;
		this.inventoryLabelY = this.imageHeight - 94;
    }    
    @Override
    public void init(){
        super.init();
        ItemStack palette_itemStack=this.menu.palette;
        btn_mode = new Btn((width/2)+(imageWidth/2),(height/2)-80 , 40, 20, Compat.literal("mode"), (x,y) -> {
            if(this.menu.palette!=null){
                PaletteItem.nextMode(palette_itemStack);
                Palette.version++;
                WandsModClient.send_palette(true,false,-1);
            }
        });
        btn_rotate = new Btn((width/2)+(imageWidth/2),(height/2)-60 , 40, 20, Compat.literal("rotate"), (x,y) -> {
            if(this.menu.palette!=null){
                PaletteItem.toggleRotate(palette_itemStack);
                Palette.version++;
                WandsModClient.send_palette(false,true,-1);
            }
        });



        int v=PaletteItem.getGradientHeight(palette_itemStack);
        gradient_h=new Spinner(v, 1, 1000,(width/2)+(imageWidth/2),(height/2)-122 , 40, 12,Compat.literal("gradient height")) {
            public void onInc(int mx, int my, int value) {
                PaletteItem.setGradientHeight(palette_itemStack,value);
                Palette.version++;
                WandsModClient.send_palette(false,true,value);
            }
            public void onDec(int mx, int my, int value) {
                PaletteItem.setGradientHeight(palette_itemStack,value);
                Palette.version++;
                WandsModClient.send_palette(false,true,value);
            }
        };
        gradient_h.label_side=true;
        gradient_h.label_col = new Colorf(1.0f, 1.0f, 1.0f, 1.0f).toInt();
        gradient_h.label_bg  = new Colorf(0.2f, 0.2f, 0.2f, 1.0f).toInt();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta){
        super.render(gui, mouseX, mouseY, delta);

        if(this.menu.palette!=null){
            PaletteMode mode=PaletteItem.getMode(this.menu.palette);            
            switch(mode){
                case RANDOM:
                    mode_val= PaletteItem.mode_val_random;
                break;
                case ROUND_ROBIN:
                    mode_val=PaletteItem.mode_val_rr;
                break;
                case GRADIENT:
                    mode_val=PaletteItem.mode_val_gradient;
                break;
                default:
                    mode_val= PaletteItem.mode_val_random;
                break;
            };
            CompoundTag tag= Compat.getTags(this.menu.palette);
            boolean rot=tag.getBoolean("rotate").orElse(false);
            gui.drawString(this.font,(rot?rot_on:rot_off)  , (width/2)-30, (height/2)-105,4210752,false);
            gui.drawString(this.font,mode_val , (width/2)+30, (height/2)-105, 4210752,false);
            btn_mode.render(gui,this.font, mouseX, mouseY);
            btn_rotate.render(gui,this.font, mouseX, mouseY);
            gradient_h.render(gui,this.font, mouseX, mouseY);
        }
        this.renderTooltip(gui, mouseX, mouseY);
    }
    @Override
    protected void renderBg(GuiGraphics gui, float f, int i, int j) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        gui.blit(RenderType::guiTextured, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, containerRows * 18 + 17, 256, 256);
		gui.blit(RenderType::guiTextured, TEXTURE, x, y + containerRows * 18 + 17, 0.0F, 126.0F, this.imageWidth, 96, 256, 256);

        //gui.blit(RenderType::guiTextured, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

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
        gradient_h.click((int)mouseX, (int)mouseY);
        Slot slot = this.find_slot(mouseX, mouseY);
        if(slot!=null){            
            switch(button){
                case 0:
                    if (hasShiftDown()) {
                        this.slotClicked(slot, slot.index, button, ClickType.QUICK_MOVE);
                    }else{
                        this.slotClicked(slot, slot.index, button, ClickType.PICKUP);
                    }
                    Palette.version++;
                break;
                case 1:
                    this.slotClicked(slot, slot.index, button, ClickType.PICKUP);
                    Palette.version++;
                break;
                case 2:
                    this.slotClicked(slot, slot.index, button, ClickType.CLONE);
                    Palette.version++;
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
                Palette.version++;
            }
        }
        return true;
    }
}

