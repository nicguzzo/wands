package net.nicguzzo.wands;

#if MC=="1165"
import me.shedaniel.architectury.ExpectPlatform;
import me.shedaniel.architectury.platform.Platform;
#else
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.platform.Platform;
#endif

import java.nio.file.Path;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;

public class WandsExpectPlatform {
    /**
     * We can use {@link Platform#getConfigFolder()} but this is just an example of {@link ExpectPlatform}.
     * <p>
     * This must be a public static method. The platform-implemented solution must be placed under a
     * platform sub-package, with its class suffixed with {@code Impl}.
     * <p>
     * Example:
     * Expect: net.examplemod.ExampleExpectPlatform#getConfigDirectory()
     * Actual Fabric: net.examplemod.fabric.ExampleExpectPlatformImpl#getConfigDirectory()
     * Actual Forge: net.examplemod.forge.ExampleExpectPlatformImpl#getConfigDirectory()
     */
    @ExpectPlatform
    public static Path getConfigDirectory() {
        // Just throw an error, the content should get replaced at runtime.
        throw new AssertionError();
    }
    @ExpectPlatform
    public static boolean claimCanInteract(ServerLevel world, BlockPos pos, Player player){
        throw new AssertionError();
        //return true;
    }
    @ExpectPlatform
    public static CompoundTag getPlayerData(Player player){
        throw new AssertionError();
    }
}
