package net.nicguzzo.wands.mixin;

//?if < 26.3 {
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ShovelItem.class)
public interface ShovelItemAccessor {

    @Accessor("FLATTENABLES")
    static Map<Block, BlockState> getFlattenables(){
        throw new AssertionError();
    }
}
//?} else {
/*import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public interface ShovelItemAccessor {
}
*///?}
