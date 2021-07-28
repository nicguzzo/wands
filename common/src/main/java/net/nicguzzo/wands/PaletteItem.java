package net.nicguzzo.wands;


import java.util.List;

import org.jetbrains.annotations.Nullable;

import me.shedaniel.architectury.registry.MenuRegistry;
import me.shedaniel.architectury.registry.menu.ExtendedMenuProvider;
import me.shedaniel.architectury.utils.NbtType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class PaletteItem extends Item{
    public enum PaletteMode {
        RANDOM, ROUND_ROBIN
    }
    static public Component mode_val_random=new TranslatableComponent("item.wands.random");
    static public Component mode_val_rr=new TranslatableComponent("item.wands.round_robin");
    public PaletteItem(Properties properties) {
        super(properties);        
    }
    //static private final int max_mode=PaletteMode.values().length;
    @Environment(EnvType.CLIENT)
    @Override    
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        CompoundTag tag=stack.getOrCreateTag();
        ListTag inventory = tag.getList("Palette",  NbtType.COMPOUND);//10 COMPOUND
        int s =inventory.size();
        for(int i=0;i<s;i++){
            // formatted red text
            CompoundTag stackTag = (CompoundTag) inventory.get(i);
            ItemStack stack2 = ItemStack.of(stackTag.getCompound("Block"));
            if(!stack2.isEmpty()){
                list.add( new TranslatableComponent(stack2.getDescriptionId()).withStyle(ChatFormatting.GREEN) );
            }
        }
        PaletteMode mode=PaletteItem.getMode(stack);            
        Component mode_val=new TextComponent("mode: ");
        if(mode==PaletteMode.ROUND_ROBIN){
                mode_val=new TextComponent("mode: "+PaletteItem.mode_val_rr.getString());
        }else{
            mode_val=new TextComponent("mode: "+PaletteItem.mode_val_random.getString());
        }
        // default white text        
        list.add( mode_val);
        list.add(new TextComponent("rotate: "+(tag.getBoolean("rotate")? "on": "off") ));
        
    }
    static public PaletteMode getMode(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            //if(!tag.contains("mode")){
            //tag.putInt("mode", 0);
            //}else{
                int mode=tag.getInt("mode");
                if(mode<PaletteMode.values().length)
                    return PaletteMode.values()[mode];
            //}
        }
        return PaletteMode.RANDOM;
    }
    static public boolean getRotate(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            //if(!tag.contains("rotate"))
                return tag.getBoolean("rotate");
            //else
                //tag.putBoolean("rotate", false);

        }
        return false;
    }
    static public void toggleRotate(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            boolean rotate=tag.getBoolean("rotate");
            WandsMod.LOGGER.info("toggleRotate: "+ !rotate);
            tag.putBoolean("rotate", !rotate);
            
        }
    }
    static public void nextMode(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int mode=(tag.getInt("mode")+1) % (2);
            WandsMod.LOGGER.info("nextMode: "+mode);
            tag.putInt("mode", mode);
            
            //LOGGER.info("wand tag: ("+tag+")");
        }
    }
    
   
    @Override
    public  InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {
        
        ItemStack paletteItemStack =player.getItemInHand(interactionHand);
        WandsMod.LOGGER.info("paletteItemStack "+paletteItemStack.getTag());
        if(!world.isClientSide()) {
            MenuRegistry.openExtendedMenu((ServerPlayer)player, new ExtendedMenuProvider(){
                @Override                
                public void saveExtraData(FriendlyByteBuf packetByteBuf) {
                    packetByteBuf.writeItem(paletteItemStack);
                }
                @Override
                public Component getDisplayName(){
                    return new TranslatableComponent(paletteItemStack.getItem().getDescriptionId());
                }
                @Override
                public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                    return new PaletteScreenHandler(syncId, inv, paletteItemStack);
                }                
            });
        }
        return InteractionResultHolder.fail(player.getItemInHand(interactionHand));
        //return super.use(world, player, hand);
    }
}
