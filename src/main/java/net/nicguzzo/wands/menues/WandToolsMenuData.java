//?if fabric && > 1.21{
package net.nicguzzo.wands.menues;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;


public record WandToolsMenuData(ItemStack item) {
    public static final StreamCodec<RegistryFriendlyByteBuf, WandToolsMenuData> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, WandToolsMenuData::item,
            WandToolsMenuData::new
    );
}
//?}