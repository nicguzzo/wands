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

    public PaletteItem(Properties properties) {
        super(properties);        
    }
    
    @Environment(EnvType.CLIENT)
    @Override    
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        ListTag inventory = stack.getOrCreateTag().getList("Inventory",  NbtType.COMPOUND);//10 COMPOUND
        int s =inventory.size();
        for(int i=0;i<s;i++){
            // formatted red text
            CompoundTag stackTag = (CompoundTag) inventory.get(i);
            ItemStack stack2 = ItemStack.of(stackTag.getCompound("Stack"));
            if(!stack2.isEmpty()){
                list.add( new TranslatableComponent(stack2.getDescriptionId()).withStyle(ChatFormatting.GREEN) );
            }
        }
        // default white text        
        list.add( new TranslatableComponent("wands.palette") );
        
    }
    static public ItemStack get_item(ItemStack palette,int slot){
        //System.out.println("get_item "+palette);
        ListTag inventory = palette.getOrCreateTag().getList("Inventory", NbtType.COMPOUND);
        //System.out.println("inventory "+inventory);
        int s =inventory.size();
        for(int i=0;i<s;i++){
        
            CompoundTag stackTag = (CompoundTag) inventory.get(i);
            int slot2 = stackTag.getInt("Slot");
            if(slot==slot2){
                ItemStack stack = ItemStack.of(stackTag.getCompound("Stack"));
                //if(stack!=ItemStack.EMPTY)
                return stack;
            }
        };   
        return ItemStack.EMPTY;
    }
    @Override
    public  InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {
        
        ItemStack paletteItemStack =player.getItemInHand(interactionHand);
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
