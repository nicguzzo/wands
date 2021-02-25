package net.nicguzzo;

import java.util.function.Consumer;
import net.minecraft.block.BushBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.nicguzzo.common.ICompatMod;
import net.nicguzzo.common.MyDir;
import net.nicguzzo.common.WandItem;

public class ICompatModImpl implements ICompatMod{
    public boolean is_fluid(BlockState state,WandItem wand){
        if(wand!=null){
            if(wand.removes_water && wand.removes_lava){
                return state.getFluidState().isTagged(FluidTags.WATER)||state.getFluidState().isTagged(FluidTags.LAVA);
            }else if(wand.removes_water){
                return state.getFluidState().isTagged(FluidTags.WATER);
            }
        }
        return false;        
    }
    
    public boolean is_double_slab(BlockState state){
        //if(state.getBlock() instanceof SlabBlock){
            return state.get(SlabBlock.TYPE)==SlabType.DOUBLE;
        //}
        //return false;
    }
    public BlockPos pos_offset(BlockPos pos,MyDir dir,int o)
    {
        Direction dir1=Direction.values()[dir.ordinal()];
        return pos.offset(dir1, o);
    }
    public int get_next_int_random(PlayerEntity player,int b){
        return player.world.rand.nextInt(b);
    }
    public BlockState random_rotate(BlockState state,World world){        
        return state.rotate(Rotation.randomRotation(world.rand));
    }
    public int get_main_inventory_size(PlayerInventory inv){
        return inv.mainInventory.size();
    }
    public ItemStack get_player_main_stack(PlayerInventory inv,int i){
        return inv.mainInventory.get(i);
    }
    public ItemStack get_player_offhand_stack(PlayerInventory inv){
        return inv.offHandInventory.get(0);
    }
    public void player_offhand_stack_inc(PlayerInventory inv,int i){
        inv.offHandInventory.get(0).grow(i);
    }
    public void player_offhand_stack_dec(PlayerInventory inv,int i){
        inv.offHandInventory.get(0).shrink(i);
    }
    public void player_stack_inc(PlayerInventory inv,int slot,int i){
        inv.mainInventory.get(slot).grow(i);
    }
    public void player_stack_dec(PlayerInventory inv,int slot,int i){
        inv.mainInventory.get(slot).shrink(i);
    }
    public void set_player_xp(PlayerEntity player,float xp){
        player.experience=xp;
    }
    public boolean item_stacks_equal(ItemStack i1,ItemStack i2){
        return ItemStack.areItemStackTagsEqual(i1,i2);
    }
    public boolean is_player_holding_wand(PlayerEntity player){
        if(player.inventory.getCurrentItem()!=null){
            return player.inventory.getCurrentItem().getItem() instanceof WandItemForge;
        }else{
            return false;
        }
    }
    public WandItem get_player_wand(PlayerEntity player){
        if(is_player_holding_wand(player))
            return ((WandItemForge)player.inventory.getCurrentItem().getItem()).wand;
        else
            return null;
    }
    public void inc_wand_damage(PlayerEntity player,ItemStack stack,int damage){

        stack.damageItem(damage, (LivingEntity)player, 
						(Consumer<LivingEntity>)((p) -> {
								((LivingEntity)p).sendBreakAnimation(Hand.MAIN_HAND);
							}
						)
					);
        
    }
    public boolean interescts_player_bb(PlayerEntity player,double x1,double y1,double z1,double x2,double y2,double z2){
        AxisAlignedBB bb=player.getBoundingBox();
        return bb.intersects(x1,y1,z1,x2,y2,z2);
    }
    public void send_message_to_player(String msg){
        assert Minecraft.getInstance().player != null;
        Minecraft instance=Minecraft.getInstance();
        instance.player.sendStatusMessage(new TranslationTextComponent(msg), true);
    }
    @Override
    public boolean is_plant(BlockState state) {
        return (state.getBlock() instanceof BushBlock);        
    }
}
