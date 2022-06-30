package net.nicguzzo.wands.mixin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.nicguzzo.wands.WandItem;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.mcver.MCVer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperienceOrb.class)
public class  ExperienceOrbMixin{
    @Shadow
    private int value;
    private int xpToDurability(int i) {
        return i * 2;
    }
    private int durabilityToXp(int i) {
        return i / 2;
    }

    @Inject(method = "repairPlayerItems", at = @At(value = "HEAD"), cancellable = true)
    public void repairPlayerItems(Player player, int i, CallbackInfoReturnable ci) {
        if(WandsMod.config.mend_tools) {
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (stack.getItem() instanceof WandItem) {
                ListTag tag = stack.getOrCreateTag().getList("Tools", MCVer.NbtType.COMPOUND);
                tag.forEach(element -> {
                    CompoundTag stackTag = (CompoundTag) element;
                    ItemStack tool = ItemStack.of(stackTag.getCompound("Tool"));
                    if (!tool.isEmpty()
                            && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, tool) > 0
                            && tool.isDamaged()
                    ) {
                        int j = Math.min(this.xpToDurability(this.value), tool.getDamageValue());
                        tool.setDamageValue(tool.getDamageValue() - j);
                        int k = i - this.durabilityToXp(j);
                        int ret = k > 0 ? k : 0;
                        ci.setReturnValue(ret);
                        ci.cancel();
                    }
                });
            }
        }
    }

}
