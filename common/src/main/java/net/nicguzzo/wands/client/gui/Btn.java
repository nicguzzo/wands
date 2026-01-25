package net.nicguzzo.wands.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.nicguzzo.wands.client.screens.WandScreen;

/**
 * A clickable button widget that can display either text or an icon texture.
 */
public class Btn extends Wdgt {
    // Background colors for different button states
    public static final int COLOR_NORMAL = 0x00000000;  // Fully transparent
    public static final int COLOR_HOVER = WandScreen.COLOR_BTN_HOVER;
    public static final int COLOR_SELECTED = WandScreen.COLOR_BTN_SELECTED;
    public static final int COLOR_DISABLED = WandScreen.COLOR_BTN_DISABLED;
    public static final int COLOR_TEXT_NORMAL = WandScreen.COLOR_TEXT_PRIMARY;

    // Layout constants
    public static final int DEFAULT_BTN_SIZE = 16;   // Button clickable area size
    public static final int DEFAULT_ICON_SIZE = 16;  // Icon texture size (centered in button)

    // Content - either text OR texture (not both)
    public Component labelText;
    public Identifier texture;
    public int textureWidth;
    public int textureHeight;

    // Background texture (optional - if set, draws this instead of solid color)
    public Identifier backgroundTexture;
    public Identifier backgroundTextureSelected;  // Used when selected (optional)
    public int backgroundTextureWidth;
    public int backgroundTextureHeight;

    // 9-slice settings (for scalable button backgrounds)
    public boolean useNineSlice = false;  // If true, render background as 9-slice
    public int nineSliceBorder = 2;       // Border size for 9-slice (corners and edges)
    public int nineSliceTexWidth = 8;     // Source texture width
    public int nineSliceTexHeight = 8;    // Source texture height

    // State
    public boolean selected = false;
    public boolean disabled = false;
    public boolean showBackground = true;  // If false, only show background when selected/hovered
    public boolean centerText = false;  // If true, center text horizontally in button
    public boolean useBackgroundTexture = false;  // If true, use backgroundTexture instead of solid color

    // Click handler (alternative to overriding onClick)
    private BtnClick onClickHandler = null;

    /** Text button with explicit position */
    public Btn(int x, int y, int width, int height, Component labelText, BtnClick onClick) {
        this.labelText = labelText;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onClickHandler = onClick;
    }

    /** Text button without position (set by layout) */
    public Btn(int width, int height, Component labelText, BtnClick onClick) {
        this.labelText = labelText;
        this.width = width;
        this.height = height;
        this.onClickHandler = onClick;
    }

    /** Icon button with default sizes */
    public Btn(Identifier texture, BtnClick onClick) {
        this(texture, DEFAULT_BTN_SIZE, DEFAULT_ICON_SIZE, onClick);
    }

    /** Icon button with custom button size, default icon size */
    public Btn(Identifier texture, int buttonSize, BtnClick onClick) {
        this(texture, buttonSize, DEFAULT_ICON_SIZE, onClick);
    }

    /** Icon button with custom button and icon sizes */
    public Btn(Identifier texture, int buttonSize, int iconSize, BtnClick onClick) {
        this.width = buttonSize;
        this.height = buttonSize;
        this.texture = texture;
        this.textureWidth = iconSize;
        this.textureHeight = iconSize;
        this.onClickHandler = onClick;
    }

    /** Icon button with background texture - icon is centered on the background */
    public Btn(Identifier backgroundTex, int bgSize, Identifier iconTex, int iconSize, BtnClick onClick) {
        this.width = bgSize;
        this.height = bgSize;
        this.backgroundTexture = backgroundTex;
        this.backgroundTextureWidth = bgSize;
        this.backgroundTextureHeight = bgSize;
        this.texture = iconTex;
        this.textureWidth = iconSize;
        this.textureHeight = iconSize;
        this.useBackgroundTexture = true;
        this.onClickHandler = onClick;
    }

    @Override
    protected boolean handleClick(int mouseX, int mouseY) {
        if (!inside(mouseX, mouseY)) return false;

        if (onClickHandler != null && !disabled) {
            onClickHandler.onClick(mouseX, mouseY);
            playClickSound();
        }
        return true;
    }

    @Override
    public void render(GuiGraphics gui, Font font, int mouseX, int mouseY) {
        if (!visible) return;

        // Determine background color based on state
        boolean isHovered = inside(mouseX, mouseY);
        boolean shouldDrawBackground = showBackground || selected || isHovered || disabled;

        if (shouldDrawBackground) {
            if (useBackgroundTexture && backgroundTexture != null) {
                // Draw background texture (use selected texture if available and selected)
                Identifier bgTex = (selected && backgroundTextureSelected != null) ? backgroundTextureSelected : backgroundTexture;
                if (useNineSlice) {
                    blitNineSlice(gui, bgTex, x, y, width, height, nineSliceBorder, nineSliceTexWidth, nineSliceTexHeight);
                } else {
                    gui.blit(RenderPipelines.GUI_TEXTURED, bgTex, x, y, 0, 0,
                        backgroundTextureWidth, backgroundTextureHeight, backgroundTextureWidth, backgroundTextureHeight);
                }
                // Draw hover overlay (skip if selected, the texture handles that)
                if (!selected && isHovered) {
                    gui.fill(x, y, x + width, y + height, 0x40FFFFFF);  // Light overlay on hover
                }
            } else {
                int backgroundColor;
                if (disabled) {
                    backgroundColor = COLOR_DISABLED;
                } else if (selected) {
                    backgroundColor = COLOR_SELECTED;
                } else if (isHovered) {
                    backgroundColor = COLOR_HOVER;
                } else {
                    backgroundColor = COLOR_NORMAL;
                }

                // Draw background
                gui.fill(x, y, x + width, y + height, backgroundColor);
            }
        }

        // Draw content (icon or text)
        if (texture != null) {
            // Center the icon in the button
            int iconX = x + (width - textureWidth) / 2;
            int iconY = y + (height - textureHeight) / 2;

            gui.blit(RenderPipelines.GUI_TEXTURED, texture, iconX, iconY, 0, 0,
                textureWidth, textureHeight, textureWidth, textureHeight);
        } else if (labelText != null) {
            int textY = y + (height - font.lineHeight + 1) / 2;
            int textX;
            if (centerText) {
                textX = x + (width - font.width(labelText)) / 2;
            } else {
                textX = x + TEXT_PADDING;
            }

            gui.drawString(font, labelText, textX, textY, COLOR_TEXT_NORMAL, true);
        }
    }

    /**
     * Render a texture using 9-slice scaling.
     * The texture is divided into 9 regions: 4 corners (fixed size), 4 edges (stretch one direction), 1 center (stretch both).
     *
     * @param gui Graphics context
     * @param tex Texture identifier
     * @param destX Destination X position
     * @param destY Destination Y position
     * @param destW Destination width
     * @param destH Destination height
     * @param border Size of the corner/edge regions in the source texture
     * @param texW Source texture width
     * @param texH Source texture height
     */
    private void blitNineSlice(GuiGraphics gui, Identifier tex, int destX, int destY, int destW, int destH, int border, int texW, int texH) {
        int centerW = texW - border * 2;  // Source center width
        int centerH = texH - border * 2;  // Source center height
        int destCenterW = destW - border * 2;  // Destination center width
        int destCenterH = destH - border * 2;  // Destination center height

        // Top-left corner
        gui.blit(RenderPipelines.GUI_TEXTURED, tex, destX, destY, 0, 0, border, border, texW, texH);
        // Top-right corner
        gui.blit(RenderPipelines.GUI_TEXTURED, tex, destX + destW - border, destY, texW - border, 0, border, border, texW, texH);
        // Bottom-left corner
        gui.blit(RenderPipelines.GUI_TEXTURED, tex, destX, destY + destH - border, 0, texH - border, border, border, texW, texH);
        // Bottom-right corner
        gui.blit(RenderPipelines.GUI_TEXTURED, tex, destX + destW - border, destY + destH - border, texW - border, texH - border, border, border, texW, texH);

        // Top edge (stretch horizontally)
        if (destCenterW > 0) {
            blitStretched(gui, tex, destX + border, destY, destCenterW, border, border, 0, centerW, border, texW, texH);
        }
        // Bottom edge (stretch horizontally)
        if (destCenterW > 0) {
            blitStretched(gui, tex, destX + border, destY + destH - border, destCenterW, border, border, texH - border, centerW, border, texW, texH);
        }
        // Left edge (stretch vertically)
        if (destCenterH > 0) {
            blitStretched(gui, tex, destX, destY + border, border, destCenterH, 0, border, border, centerH, texW, texH);
        }
        // Right edge (stretch vertically)
        if (destCenterH > 0) {
            blitStretched(gui, tex, destX + destW - border, destY + border, border, destCenterH, texW - border, border, border, centerH, texW, texH);
        }

        // Center (stretch both)
        if (destCenterW > 0 && destCenterH > 0) {
            blitStretched(gui, tex, destX + border, destY + border, destCenterW, destCenterH, border, border, centerW, centerH, texW, texH);
        }
    }

    /**
     * Blit a texture region with tiling (for 9-slice edges and center).
     * Tiles the source region to fill the destination area.
     */
    private void blitStretched(GuiGraphics gui, Identifier tex, int destX, int destY, int destW, int destH, int srcX, int srcY, int srcW, int srcH, int texW, int texH) {
        // Tile the source region to fill the destination
        for (int tileY = 0; tileY < destH; tileY += srcH) {
            int drawH = Math.min(srcH, destH - tileY);
            for (int tileX = 0; tileX < destW; tileX += srcW) {
                int drawW = Math.min(srcW, destW - tileX);
                gui.blit(RenderPipelines.GUI_TEXTURED, tex, destX + tileX, destY + tileY, srcX, srcY, drawW, drawH, texW, texH);
            }
        }
    }
}
