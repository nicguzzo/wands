package net.nicguzzo;

import java.util.function.Consumer;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.LiteralText;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.nicguzzo.common.ICompatMod;
import net.nicguzzo.common.MyDir;
import net.nicguzzo.common.WandItem;

public class ICompatModImpl implements ICompatMod{
    public boolean is_fluid(BlockState state,WandItem wand){
        if(wand!=null){
            if(wand.removes_water && wand.removes_lava){
                return state.getFluidState().isIn(FluidTags.WATER)||state.getFluidState().isIn(FluidTags.LAVA);
            }else if(wand.removes_water){
                return state.getFluidState().isIn(FluidTags.WATER);
            }
        }
        return false;        
    }
    public boolean is_double_slab(BlockState state){
        return state.get(SlabBlock.TYPE)==SlabType.DOUBLE;        
    }
    public BlockPos pos_offset(BlockPos pos,MyDir dir,int o)
    {
        Direction dir1=Direction.values()[dir.ordinal()];
        return pos.offset(dir1, o);
    }
    public int get_next_int_random(PlayerEntity player,int b){
        return player.world.random.nextInt(b);
    }
    public BlockState random_rotate(BlockState state,World world){        
        return state.rotate(BlockRotation.random(world.random));
    }
    public int get_main_inventory_size(PlayerInventory inv){
        return inv.main.size();
    }
    public ItemStack get_player_main_stack(PlayerInventory inv,int i){
        return inv.main.get(i);
    }
    public ItemStack get_player_offhand_stack(PlayerInventory inv){
        return inv.offHand.get(0);
    }
    public void player_offhand_stack_inc(PlayerInventory inv,int i){
        inv.offHand.get(0).increment(i);
    }
    public void player_offhand_stack_dec(PlayerInventory inv,int i){
        inv.offHand.get(0).decrement(i);
    }
    public void player_stack_inc(PlayerInventory inv,int slot,int i){
        inv.main.get(slot).increment(i);
    }
    public void player_stack_dec(PlayerInventory inv,int slot,int i){
        inv.main.get(slot).decrement(i);
    }
    public void set_player_xp(PlayerEntity player,float xp){
        player.experienceProgress=xp;
    }
    public boolean item_stacks_equal(ItemStack i1,ItemStack i2){
        return ItemStack.areEqual(i1,i2);
    }
    public boolean is_player_holding_wand(PlayerEntity player){
        ItemStack item = player.inventory.getMainHandStack();	
        return item.getItem() instanceof WandItemFabric;
    }
    public WandItem get_player_wand(PlayerEntity player){
        if(is_player_holding_wand(player))
            return ((WandItemFabric)player.inventory.getMainHandStack().getItem()).wand;
        else
            return null;
    }
    public void inc_wand_damage(PlayerEntity player,ItemStack stack,int damage){
        
		stack.damage(damage, (LivingEntity)player, 
						(Consumer<LivingEntity>)((p) -> {
								((LivingEntity)p).sendToolBreakStatus(Hand.MAIN_HAND);
							}
						)
					);
        
    }
    public boolean interescts_player_bb(PlayerEntity player,double x1,double y1,double z1,double x2,double y2,double z2){
        Box bb=player.getBoundingBox();
        return bb.intersects(x1,y1,z1,x2,y2,z2);
    }
    public void send_message_to_player(String msg){
        assert MinecraftClient.getInstance().player != null;
        MinecraftClient instance=MinecraftClient.getInstance();
        instance.player.sendMessage(new LiteralText(msg), true);
    }

    @Override
    public boolean is_plant(BlockState state) {
        return (state.getBlock() instanceof PlantBlock);        
    }
    
}
