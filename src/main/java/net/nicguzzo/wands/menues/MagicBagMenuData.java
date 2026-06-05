//?if fabric && > 1.21{
package net.nicguzzo.wands.menues;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record MagicBagMenuData(ItemStack item) {
    public static final StreamCodec<RegistryFriendlyByteBuf, MagicBagMenuData> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, MagicBagMenuData::item,
            MagicBagMenuData::new
    );
}
//?}