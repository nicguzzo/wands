package net.nicguzzo.wands.client;

import net.minecraft.world.level.Level;
import dev.architectury.utils.EnvExecutor;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;

public class ClientUtils {
    public static Level getClientLevelSafe() {
        return EnvExecutor.getEnvSpecific(
                () -> () -> Minecraft.getInstance().level,
                () -> () -> null
        );
    }
}
