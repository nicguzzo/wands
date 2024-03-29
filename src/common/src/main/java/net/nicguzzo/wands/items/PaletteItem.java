package net.nicguzzo.wands.items;

import java.util.List;
import net.nicguzzo.wands.utils.Compat;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
public class PaletteItem extends Item{
    public enum PaletteMode {
        RANDOM, ROUND_ROBIN
    }
    static public Component mode_val_random=Compat.translatable("item.wands.random");
    static public Component mode_val_rr=Compat.translatable("item.wands.round_robin");
    public PaletteItem(Properties properties) {
        super(properties);        
    }
    @Environment(EnvType.CLIENT)
    @Override    
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        CompoundTag tag=stack.getOrCreateTag();
        ListTag inventory = tag.getList("Palette", Compat.NbtType.COMPOUND);//10 COMPOUND
        int s =inventory.size();
        for(int i=0;i<s;i++){
            CompoundTag stackTag = (CompoundTag) inventory.get(i);
            ItemStack stack2 = ItemStack.of(stackTag.getCompound("Block"));
            if(!stack2.isEmpty()){
                list.add( Compat.translatable(stack2.getDescriptionId()).withStyle(ChatFormatting.GREEN) );
            }
        }
        PaletteMode mode=PaletteItem.getMode(stack);            
        Component mode_val;
        if(mode==PaletteMode.ROUND_ROBIN){
                mode_val=Compat.literal("mode: "+PaletteItem.mode_val_rr.getString());
        }else{
            mode_val=Compat.literal("mode: "+PaletteItem.mode_val_random.getString());
        }
        list.add( mode_val);
        list.add(Compat.literal("rotate: "+(tag.getBoolean("rotate")? "on": "off") ));
    }
    static public PaletteMode getMode(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int mode=tag.getInt("mode");
            if(mode<PaletteMode.values().length)
                return PaletteMode.values()[mode];
        }
        return PaletteMode.RANDOM;
    }
    static public boolean getRotate(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            return tag.getBoolean("rotate");
        }
        return false;
    }
    static public void toggleRotate(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            boolean rotate=tag.getBoolean("rotate");
            tag.putBoolean("rotate", !rotate);
        }
    }
    static public void nextMode(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int mode=(tag.getInt("mode")+1) % (2);
            tag.putInt("mode", mode);
        }
    }
    @Override
    public  InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {
        ItemStack paletteItemStack =player.getItemInHand(interactionHand);
        if(!world.isClientSide()) {
            Compat.open_menu((ServerPlayer) player,paletteItemStack,1);
        }
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }
}
