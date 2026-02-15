package net.nicguzzo.wands.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.Font;

#if MC_VERSION >=12111
import net.minecraft.client.gui.components.toasts.ToastManager;
#else
import net.minecraft.client.gui.components.toasts.ToastComponent;
#endif
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.compat.Compat;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class WandToast implements Toast {
    private static final int BG_COLOR = 0xDD442222;  // dark red with alpha
    private static final int BORDER_COLOR = 0xFF882222;  // brighter red border
    private static final long DURATION_MS = 2000L;  // 2 seconds
    private static final int PADDING = 6;  // padding on left and right edges
    private static final int TEXT_START = 30;  // x position where text starts (after icon)

    private static WandToast currentToast = null;

    Component text;
    Component text2;
    private Toast.Visibility wantedVisibility;
    private int calculatedWidth = 160;  // default, recalculated in render
    private long startTime = -1;

    public WandToast(String s){
        text= Compat.literal(s);
        text2 = null;
    }
    public WandToast(String s, String s2){
        text= Compat.literal(s);
        text2 = Compat.literal(s2);
    }
    public WandToast(Component c){
        text = c;
        text2 = null;
    }
    public WandToast(Component c, Component c2){
        text = c;
        text2 = c2;
    }
#if MC_VERSION >=12111
    public static void show(WandToast toast) {
        Minecraft client = Minecraft.getInstance();
        if (currentToast != null && currentToast.wantedVisibility == Visibility.SHOW) {
            // Update existing toast instead of creating new one
            currentToast.text = toast.text;
            currentToast.text2 = toast.text2;
            currentToast.startTime = -1;  // Reset timer
        } else {
            currentToast = toast;
            client.getToastManager().addToast(toast);
        }
    }
#endif
    @Override
    public int width() {
        return calculatedWidth;
    }

    @Override
    #if MC_VERSION < 12111
    public @NotNull Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long l)
    #else
    public void render(GuiGraphics guiGraphics, Font font, long l)
    #endif
    {
        Minecraft client=Minecraft.getInstance();

        // Calculate width based on text content
        int textWidth = client.font.width(text);
        if (text2 != null) {
            textWidth = Math.max(textWidth, client.font.width(text2));
        }
        calculatedWidth = TEXT_START + textWidth + PADDING;

        int w = this.width();
        int h = this.height();

        // Draw background
        guiGraphics.fill(0, 0, w, h, BG_COLOR);
        // Draw border
        guiGraphics.fill(0, 0, w, 1, BORDER_COLOR);  // top
        guiGraphics.fill(0, h - 1, w, h, BORDER_COLOR);  // bottom
        guiGraphics.fill(0, 0, 1, h, BORDER_COLOR);  // left
        guiGraphics.fill(w - 1, 0, w, h, BORDER_COLOR);  // right

        if(client.player!=null) {
            ItemStack s = client.player.getMainHandItem();
            int iconY = (h - 16) / 2;  // 16 = item icon size, center vertically
            guiGraphics.renderFakeItem(s, PADDING, iconY);
        }
        if (text2 != null) {
            guiGraphics.drawString(client.font, text, TEXT_START, 7, 0xffffffff, false);
            guiGraphics.drawString(client.font, text2, TEXT_START, 18, 0xffcccccc, false);
        } else {
            guiGraphics.drawString(client.font, text, TEXT_START, 12, 0xffffffff, false);
        }

        #if MC_VERSION<12111
        return l >= DURATION_MS ? Visibility.HIDE : Visibility.SHOW;
        #endif
    }
    #if MC_VERSION >=12111
    public Toast.Visibility getWantedVisibility() {
        return this.wantedVisibility;
    }
    public void update(ToastManager toastManager, long l) {
        if (startTime == -1) {
            startTime = l;
        }
        long elapsed = l - startTime;
        if (elapsed >= DURATION_MS) {
            this.wantedVisibility = Visibility.HIDE;
            if (currentToast == this) {
                currentToast = null;
            }
        } else {
            this.wantedVisibility = Visibility.SHOW;
        }
    }
    #endif
}
