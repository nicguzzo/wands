package net.nicguzzo.common;

import java.util.Vector;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ICompatMod {
    public PlayerEntity get_player();
    public String get_player_uuid(PlayerEntity player);
    public boolean destroy_block(World world,BlockPos pos,boolean drop);
    public boolean is_fluid(BlockState state,WandItem wand);
    public boolean is_double_slab(BlockState state);
    public BlockPos pos_offset(BlockPos pos,MyDir dir,int o);
    public int get_next_int_random(PlayerEntity player,int b);
    public BlockState random_rotate(BlockState state,World world);
    public int get_main_inventory_size(PlayerEntity inv);
    public ItemStack get_player_main_stack(PlayerEntity inv,int i);
    public ItemStack get_player_offhand_stack(PlayerEntity inv);
    public void player_offhand_stack_inc(PlayerEntity inv,int i);
    public void player_offhand_stack_dec(PlayerEntity inv,int i);
    public void player_stack_inc(PlayerEntity inv,int slot,int i);
    public void player_stack_dec(PlayerEntity inv,int slot,int i);
    public void set_player_xp(PlayerEntity player,float xp);
    public boolean item_stacks_equal(ItemStack i1,ItemStack i2);
    public boolean is_player_holding_wand(PlayerEntity player);
    public WandItem get_player_wand(PlayerEntity player);
    public void inc_wand_damage(PlayerEntity player,ItemStack stack,int damage);
    public boolean interescts_player_bb(PlayerEntity player,double x1,double y1,double z1,double x2,double y2,double z2);
    public void send_message_to_player(String msg);
    public boolean is_plant(BlockState state);
    public boolean has_tag(ItemStack item_stack);
    public int in_inventory(PlayerEntity player,ItemStack item_stack);
    public boolean is_shulker(PlayerEntity player,ItemStack item_stack);
    public int in_shulker(PlayerEntity player,ItemStack item_stack);
    public int in_shulker_slot(PlayerEntity player,ItemStack item_stack,BlockState state);
    public Vector<Integer> shulker_slots(PlayerEntity player,ItemStack item_stack);
    public ItemStack item_from_shulker(ItemStack shulker,int slot);
    public void remove_item_from_shulker(ItemStack shulker,int slot,int n);
    public void send_xp_to_player(PlayerEntity player);
    public Block block_from_item(Item it);
    public World world(PlayerEntity player);
    public BlockState getDefaultBlockState(Block b);
    public boolean setBlockState(World w,BlockPos p,BlockState s);
    public BlockState with_snow_layers(Block block,int n);
    public void playBlockSound(PlayerEntity player,BlockState block_state,BlockPos pos,boolean destroy);
    public boolean can_destroy(BlockState block_state,ItemStack offhand,boolean isCreative);
    public int get_silk_touch_level(ItemStack item);
    public int get_fortune_level(ItemStack item);
    public void dropStacks(BlockState state,World world,BlockPos pos);
    public void dropStack(World world,BlockPos pos,ItemStack item_stack);
    public Block block_from_id(String id);
    public void send_block_placed(PlayerEntity player,BlockPos pos,boolean destroy);
}
