package net.nicguzzo.wands;


import java.util.List;
import java.util.Random;

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
    static private final int max_mode=PaletteMode.values().length;
    @Environment(EnvType.CLIENT)
    @Override    
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        ListTag inventory = stack.getOrCreateTag().getList("Palette",  NbtType.COMPOUND);//10 COMPOUND
        int s =inventory.size();
        for(int i=0;i<s;i++){
            // formatted red text
            CompoundTag stackTag = (CompoundTag) inventory.get(i);
            ItemStack stack2 = ItemStack.of(stackTag.getCompound("Block"));
            if(!stack2.isEmpty()){
                list.add( new TranslatableComponent(stack2.getDescriptionId()).withStyle(ChatFormatting.GREEN) );
            }
        }
        // default white text        
        //list.add( new TranslatableComponent("wands.palette") );
        
    }
    static public PaletteMode getMode(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            int mode=stack.getOrCreateTag().getInt("mode");
            if(mode<PaletteMode.values().length)
                return PaletteMode.values()[mode];
        }
        return PaletteMode.RANDOM;
    }
    static public void nextMode(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int mode=(tag.getInt("mode")+1) % (2);
            WandsMod.LOGGER.info("palette next mode: "+mode);
            tag.putInt("mode", mode);
            //LOGGER.info("wand tag: ("+tag+")");
        }
    }
    static public ItemStack get_item(ItemStack palette,PlayerWandInfo s_info,Player player){
        if(s_info==null || palette==null){
            return ItemStack.EMPTY;
        }
        s_info.slots.clear();
        PaletteMode palatte_mode=PaletteItem.getMode(palette);
        ListTag palette_inv = palette.getOrCreateTag().getList("Palette", NbtType.COMPOUND);
        //WandsMod.LOGGER.info("palette_inv: "+palette_inv);
        int s =palette_inv.size();
        for(int i=0;i<s;i++){
            CompoundTag stackTag = (CompoundTag) palette_inv.get(i);
            ItemStack stack = ItemStack.of(stackTag.getCompound("Block"));
            if(!stack.isEmpty()){
                if(player.abilities.instabuild){
                    s_info.slots.add(i);
                }else{
                    int[] count=WandUtils.count_in_player(player, stack);
                    if(count[0]+count[1]>0){
                        s_info.slots.add(i);
                    }
                }
            }
        }
        //WandsMod.LOGGER.info("slots: "+s_info.slots);
        if(s_info.slots.size()>0){
            nextSlot(s_info, palatte_mode, player.level.random,s_info.slots.size());
            CompoundTag stackTag = (CompoundTag) palette_inv.get(s_info.slots.get(s_info.slot));
            ItemStack stack = ItemStack.of(stackTag.getCompound("Block"));
            if(stack!=ItemStack.EMPTY){
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
    static void nextSlot(PlayerWandInfo s_info,PaletteMode palatte_mode,Random random,int bound) {
		if (palatte_mode == PaletteMode.RANDOM) {
			s_info.slot = random.nextInt(bound);
		} else if (palatte_mode == PaletteMode.ROUND_ROBIN) {
			s_info.slot = (s_info.slot + 1) % bound;
		}
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
