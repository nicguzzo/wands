package net.nicguzzo.wands.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
#if MC_VERSION >= 12111
import net.minecraft.client.input.MouseButtonEvent;
#endif
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.compat.MyIdExt;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.client.gui.Btn;
import net.nicguzzo.wands.client.gui.CycleToggle;
import net.nicguzzo.wands.client.gui.Spinner;
import net.nicguzzo.wands.client.gui.Wdgt;
import net.nicguzzo.wands.items.PaletteItem;
import net.nicguzzo.wands.items.PaletteItem.PaletteMode;
import net.nicguzzo.wands.menues.PaletteMenu;
import net.nicguzzo.compat.Compat;
import net.nicguzzo.wands.networking.Networking;
import net.nicguzzo.wands.wand.Palette;
import net.nicguzzo.wands.WandsMod;

public class PaletteScreen extends AbstractContainerScreen<PaletteMenu> {

    private static final MyIdExt TEXTURE = new MyIdExt("textures/gui/container/generic_54.png");
    private static final MyIdExt SMALL_BUTTON_TEX = new MyIdExt(WandsMod.MOD_ID,"textures/gui/small_button.png");
    private static final MyIdExt SMALL_BUTTON_PRESSED_TEX = new MyIdExt(WandsMod.MOD_ID,"textures/gui/small_button_pressed.png");
    private static final MyIdExt ROTATE_TEX = new MyIdExt(WandsMod.MOD_ID,"textures/gui/small_rotate.png");
    private static final int CONTROL_SPACING = 6;

    private Btn btn_rotate;
    private CycleToggle<PaletteMode> modeToggle;
    private final int containerRows;
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

        // Rotate button - 10x10 with small_button.png background and small_rotate.png icon (6px) centered
        btn_rotate = new Btn(SMALL_BUTTON_TEX, 10, ROTATE_TEX, 6, (mouseX, mouseY) -> {
            if(this.menu.palette!=null){
                PaletteItem.toggleRotate(palette_itemStack);
                Palette.version++;
                Networking.send_palette(false,true,-1);
            }
        });
        btn_rotate.backgroundTextureSelected = SMALL_BUTTON_PRESSED_TEX;
        btn_rotate.useNineSlice = true;
        btn_rotate.withTooltip(
            Compat.translatable("tooltip.wands.palette.rotate"),
            Compat.translatable("tooltip.wands.palette.rotate_desc")
        );
        btn_rotate.x = (width/2) - 38;
        btn_rotate.y = (height/2) - 106;

        // Mode toggle - positioned to the right of rotate button, no label
        Component[] modeLabels = {
            PaletteItem.mode_val_random,
            PaletteItem.mode_val_rr,
            PaletteItem.mode_val_gradient
        };
        modeToggle = new CycleToggle<>(
            null,
            PaletteMode.values(),
            modeLabels,
            () -> PaletteItem.getMode(palette_itemStack),
            mode -> {
                PaletteItem.setMode(palette_itemStack, mode);
                Palette.version++;
                Networking.send_palette(true, false, -1);
            }
        );
        modeToggle.width = 54;
        modeToggle.height = 12;
        modeToggle.showBackground = false;
        modeToggle.drawShadow = false;
        modeToggle.valueColor = Compat.DARK_GRAY;  // Dark grey text
        modeToggle.setTooltipProvider(index -> {
            return switch (index) {
                case 0 -> Compat.translatable("tooltip.wands.palette.mode.random_desc");
                case 1 -> Compat.translatable("tooltip.wands.palette.mode.round_robin_desc");
                case 2 -> Compat.translatable("tooltip.wands.palette.mode.gradient_desc");
                default -> null;
            };
        });
        modeToggle.x = btn_rotate.x + btn_rotate.width + CONTROL_SPACING;
        modeToggle.y = btn_rotate.y - 1;

        // Gradient height spinner - no label, no background
        int v = PaletteItem.getGradientHeight(palette_itemStack);
        gradient_h = new Spinner(v, 1, 1000, 30, 12, null)
            .withOnChange(value -> {
                PaletteItem.setGradientHeight(palette_itemStack, value);
                Palette.version++;
                Networking.send_palette(false, true, value);
            });
        gradient_h.showBackground = false;
        gradient_h.drawShadow = false;
        gradient_h.valueColor = Compat.DARK_GRAY;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta){
        super.render(gui, mouseX, mouseY, delta);

        if(this.menu.palette!=null){
            PaletteMode mode = PaletteItem.getMode(this.menu.palette);

            // Update rotate button selected state
            CompoundTag tag = Compat.getTags(this.menu.palette);
            //boolean rot = tag.getBoolean("rotate").orElse(false);
            boolean rot = Compat.getBoolean(tag,"rotate").orElse(false);
            btn_rotate.selected = rot;

            // Render controls
            btn_rotate.render(gui, this.font, mouseX, mouseY);
            modeToggle.render(gui, this.font, mouseX, mouseY);

            // Show gradient height spinner only in gradient mode
            if(mode == PaletteMode.GRADIENT){
                gradient_h.x = modeToggle.x + modeToggle.width;
                gradient_h.y = modeToggle.y;
                gradient_h.render(gui, this.font, mouseX, mouseY);
            }

            // Render widget tooltips using vanilla tooltip rendering
            Wdgt hoveredWidget = null;
            if (btn_rotate.shouldShowTooltip(mouseX, mouseY)) {
                hoveredWidget = btn_rotate;
            } else if (modeToggle.shouldShowTooltip(mouseX, mouseY)) {
                hoveredWidget = modeToggle;
            }
            if (hoveredWidget != null) {
                Compat.renderComponentTooltip(gui, font, hoveredWidget.getTooltipLines(), mouseX, mouseY);
            }
        }
        this.renderTooltip(gui, mouseX, mouseY);
    }
    @Override
    protected void renderBg(GuiGraphics gui, float f, int i, int j) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        //gui.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, containerRows * 18 + 17, 256, 256);
		//gui.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + containerRows * 18 + 17, 0.0F, 126.0F, this.imageWidth, 96, 256, 256);

        Compat.blit(gui, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, containerRows * 18 + 17, 256, 256);
        Compat.blit(gui, TEXTURE, x, y + containerRows * 18 + 17, 0.0F, 126.0F, this.imageWidth, 96, 256, 256);

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
    #if MC_VERSION >= 12111
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl)
    {
        int mouseX=(int)mouseButtonEvent.x();
        int mouseY=(int)mouseButtonEvent.y();
        int button=mouseButtonEvent.button();
        boolean hasShiftDown=mouseButtonEvent.hasShiftDown();
    #else
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean hasShiftDown=hasShiftDown();
    #endif
        btn_rotate.click((int)mouseX, (int)mouseY);
        modeToggle.click((int)mouseX, (int)mouseY);
        if(PaletteItem.getMode(this.menu.palette) == PaletteMode.GRADIENT) {
            gradient_h.click((int)mouseX, (int)mouseY);
        }
        Slot slot = this.find_slot(mouseX, mouseY);
        if(slot!=null){            
            switch(button){
                case 0:
                    if (hasShiftDown) {
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
    #if MC_VERSION >= 12111
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent){ return true; }
    #else
    public boolean mouseReleased(double mouseX, double mouseY, int button){ return true; }
    #endif
    @Override
    #if MC_VERSION >= 12111
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double d, double e){
        int mouseX=(int)mouseButtonEvent.x();
        int mouseY=(int)mouseButtonEvent.y();
        int button=mouseButtonEvent.button();
    #else
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY){
    #endif
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

