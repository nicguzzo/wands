package net.nicguzzo.wands.mixin;
#if false
#if MC>="1205"
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
#endif
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
#if MC>="1210"
import net.minecraft.server.level.ServerPlayer;
#endif
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
#if MC>="1210"
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
#endif

import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.utils.Compat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

@Mixin(ExperienceOrb.class)
public class  ExperienceOrbMixin{
    @Shadow
    private int value;
    private int throwTime;
    private int xpToDurability(int i) {
        return i * 2;
    }
    private int durabilityToXp(int i) {
        return i / 2;
    }
#if MC=="1165"
    @Inject(method = "playerTouch",
            at = @At(value = "INVOKE",
            target=
                    "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getRandomItemWith(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Predicate;)Ljava/util/Map$Entry;")
            )
    public void playerTouch(Player player, CallbackInfo ci) {
        if(WandsMod.config.mend_tools) {
            ItemStack stack=null;
            ItemStack stack1 = player.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack stack2 = player.getItemInHand(InteractionHand.OFF_HAND);
            boolean valid=false;
            if (stack1.getItem() instanceof WandItem) {
                stack=stack1;
            }else{
                if(stack2.getItem() instanceof WandItem){
                    stack=stack2;
                }
            }
            if (stack!=null) {
                ListTag tag = stack.getOrCreateTag().getList("Tools", Compat.NbtType.COMPOUND);
                tag.forEach(element -> {
                    CompoundTag stackTag = (CompoundTag) element;
                    ItemStack tool = ItemStack.of(stackTag.getCompound("Tool"));
                    if (!tool.isEmpty()
                            && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, tool) > 0
                            && tool.isDamaged()
                    ) {
                        int j = Math.min(this.xpToDurability(this.value), tool.getDamageValue());
                        tool.setDamageValue(tool.getDamageValue() - j);
                        this.value = this.value - this.durabilityToXp(j);
                    }
                });
            }
        }
    }
#else

    @Inject(method = "repairPlayerItems", at = @At(value = "HEAD"), cancellable = true)
    #if MC<"1210"
    public void repairPlayerItems(Player player, int i, CallbackInfoReturnable ci) {
    #else
    public void repairPlayerItems(ServerPlayer player, int i, CallbackInfoReturnable ci) {
    #endif
        if(WandsMod.config.mend_tools) {
            ItemStack stack=null;
            ItemStack stack1 = player.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack stack2 = player.getItemInHand(InteractionHand.OFF_HAND);
            boolean valid=false;
            if (stack1.getItem() instanceof WandItem) {
                stack=stack1;
            }else{
                if(stack2.getItem() instanceof WandItem){
                    stack=stack2;
                }
            }
            if (stack!=null) {
                #if MC>="1205"
                //TODO: fix mending inside the wand
                CompoundTag tags= Compat.getTags(stack);
                ListTag tools_list_tag = tags.getList("Tools", Compat.NbtType.COMPOUND);
                int k=i;
                boolean update_tags=false;
                for(int l=0;l<tools_list_tag.size();l++){
                    CompoundTag stackTag = (CompoundTag) tools_list_tag.get(l);
                    ItemStack tool = ItemStack.parse(player.level().registryAccess(),stackTag.getCompound("Tool")).orElse(ItemStack.EMPTY);
                    if (!tool.isEmpty()
                            //&& EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, tool) > 0
                            //&& tool.isDamaged()
                    ) {
                        boolean mending=Compat.has_mending(tool,player.level());
                        //int mending=EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, tool);
                        
                        boolean damaged= tool.isDamaged();
                        if(mending && damaged)
                        {
                            int j = Math.min(this.xpToDurability(this.value), tool.getDamageValue());
                            tool.setDamageValue(tool.getDamageValue() - j);
                            Tag tool_tag=tool.save(player.level().registryAccess());
                            stackTag.put("Tool",tool_tag);
                            k = k - this.durabilityToXp(j);
                            update_tags=true;
                            break;
                        }
                    }
                };
                if(update_tags){
                    //Tag item_tag=stack.save(player.level().registryAccess());
                    tags.put("Tools",tools_list_tag);
                    CustomData.set(DataComponents.CUSTOM_DATA, stack, tags);
                    if(k<=0){
                        ci.setReturnValue(k);
                        ci.cancel();
                    }
                }
                #else
                    ListTag tag = stack.getOrCreateTag().getList("Tools", Compat.NbtType.COMPOUND);
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
                #endif
            }
        }
    }
#endif
}
#endif