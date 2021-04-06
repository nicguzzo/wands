package net.nicguzzo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nicguzzo.common.WandItem;

import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class KeyBoardInput {
    //"key.wands.wand_mode": "Mode",
    //"key.wands.wand_orientation": "Orientation",
    //"key.wands.wand_invert": "Invert",
    //"key.wands.wand_palette_mode": "Palette Mode",
    public static final KeyBinding WAND_MODE_KEY = new KeyBinding("key.wands.wand_mode",
            KeyConflictContext.IN_GAME,KeyModifier.NONE,
            InputMappings.Type.KEYSYM,GLFW.GLFW_KEY_V,"category.wands");
    
    public static final KeyBinding WAND_ORIENTATION_KEY = new KeyBinding("key.wands.wand_orientation",
            KeyConflictContext.IN_GAME,KeyModifier.NONE,
            InputMappings.Type.KEYSYM,GLFW.GLFW_KEY_X,"category.wands");

    public static final KeyBinding WAND_INVERT_KEY = new KeyBinding("key.wands.wand_invert",
            KeyConflictContext.IN_GAME,KeyModifier.NONE,
            InputMappings.Type.KEYSYM,GLFW.GLFW_KEY_I,"category.wands");

    public static final KeyBinding WAND_PALETTE_MODE_KEY = new KeyBinding("key.wands.wand_palette_mode",
            KeyConflictContext.IN_GAME,KeyModifier.NONE,
            InputMappings.Type.KEYSYM,GLFW.GLFW_KEY_R,"category.wands");
    
    public static final KeyBinding WAND_UNDO_MODE_KEY = new KeyBinding("key.wands.undo",
            KeyConflictContext.IN_GAME,KeyModifier.NONE,
            InputMappings.Type.KEYSYM,GLFW.GLFW_KEY_U,"category.wands");

    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.KeyInputEvent event) {
        if (WAND_MODE_KEY.isDown()) {            
            WandItem.cycleMode();
        }
        if (WAND_ORIENTATION_KEY.isDown()) {            
            WandItem.cycleOrientation();
        }
        if (WAND_UNDO_MODE_KEY.isDown()) {
            if (Minecraft.getInstance().player != null && WandsMod.compat.is_player_holding_wand(Minecraft.getInstance().player)) {
                int n = 1;
                if (Screen.hasAltDown())
                    n = 10;
                if (Screen.hasShiftDown()) {
                    //WandItemForge.redo(n);
                    WandsPacketHandler.INSTANCE.sendToServer(new SendUndo(n,1));
                } else {
                    WandsPacketHandler.INSTANCE.sendToServer(new SendUndo(n,0));
                    //WandItemForge.undo(n);
                }
            }
        }
        if (WAND_INVERT_KEY.isDown()) {            
            WandItem.toggleInvert();
        }
        if (WAND_PALETTE_MODE_KEY.isDown()) {            
            WandItem.cyclePalleteMode();
        }
    }
}
