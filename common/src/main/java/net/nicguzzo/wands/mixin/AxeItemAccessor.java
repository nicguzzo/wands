package net.nicguzzo.wands.mixin;


import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AxeItem.class)
public interface AxeItemAccessor {
    #if MC=="1165"
        @Accessor("STRIPABLES")

    #else
        @Accessor("STRIPPABLES")
    #endif
    static Map<Block, Block> getStrippables(){
        throw new AssertionError();
    }

}
