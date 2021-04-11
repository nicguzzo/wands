package net.nicguzzo;

import java.util.Vector;
import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractGlassBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.LiteralText;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.nicguzzo.common.ICompatMod;
import net.nicguzzo.common.MyDir;
import net.nicguzzo.common.WandItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class ICompatModImpl implements ICompatMod{
    @Environment(EnvType.CLIENT)
    public PlayerEntity get_player(){
        assert MinecraftClient.getInstance().player != null;
        MinecraftClient instance=MinecraftClient.getInstance();
        return instance.player;
    }
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
    public int get_main_inventory_size(PlayerEntity player){
        return player.inventory.main.size();
    }
    public ItemStack get_player_main_stack(PlayerEntity player,int i){
        return player.inventory.main.get(i);
    }
    public ItemStack get_player_offhand_stack(PlayerEntity player){
        return player.inventory.offHand.get(0);
    }
    public void player_offhand_stack_inc(PlayerEntity player,int i){
        player.inventory.offHand.get(0).increment(i);
    }
    public void player_offhand_stack_dec(PlayerEntity player,int i){
        player.inventory.offHand.get(0).decrement(i);
    }
    public void player_stack_inc(PlayerEntity player,int slot,int i){
        player.inventory.main.get(slot).increment(i);
    }
    public void player_stack_dec(PlayerEntity player,int slot,int i){
        player.inventory.main.get(slot).decrement(i);
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
    
    @Environment(EnvType.CLIENT)
    public void send_message_to_player(String msg){
        assert MinecraftClient.getInstance().player != null;
        MinecraftClient instance=MinecraftClient.getInstance();
        instance.player.sendMessage(new LiteralText(msg), true);
    }

    @Override
    public boolean is_plant(BlockState state) {
        return (state.getBlock() instanceof PlantBlock);        
    }
    @Override
    public boolean is_shulker(PlayerEntity player,ItemStack item_stack){        
        ItemStack offhand = get_player_offhand_stack(player);
        Block blk = block_from_item(offhand.getItem());        
		return offhand != null && blk instanceof ShulkerBoxBlock ;
    }
    @Override
    public int in_shulker(PlayerEntity player,ItemStack item_stack){
        ListTag shulker_items=null;
        int in_shulker=0;        
        if(is_shulker(player, item_stack)){
            ItemStack shulker = WandsMod.compat.get_player_offhand_stack(player);
            CompoundTag entity_tag =shulker.getSubTag("BlockEntityTag");
            if(entity_tag!=null){
                shulker_items = entity_tag.getList("Items", 10);		
                if(shulker_items!=null){
                    for (int i = 0, len = shulker_items.size(); i < len; ++i) {
                        CompoundTag itemTag = shulker_items.getCompound(i);
                        ItemStack s = ItemStack.fromTag(itemTag);
                        if(WandItem.fill_pos1!=null){
                            Item it=Item.fromBlock(player.world.getBlockState(WandItem.fill_pos1).getBlock());
                            if( s.getItem()== it){
                                in_shulker+=s.getCount();
                            }
                        }else{
                            if( s.getItem()== item_stack.getItem()){
                                in_shulker+=s.getCount();
                            }							
                        }
                    }
                    //System.out.println("shulker "+in_shulker);
                }
            }
        }
        return in_shulker;
    }
    @Override
    public int in_shulker_slot(PlayerEntity player,ItemStack item_stack,BlockState state){
        
        CompoundTag entity_tag = item_stack.getSubTag("BlockEntityTag");
        ListTag shulker_items= entity_tag.getList("Items", 10);        
        
        if (shulker_items != null) {
            for (int i = 0, len = shulker_items.size(); i < len; ++i) {
                CompoundTag itemTag = shulker_items.getCompound(i);
                ItemStack s = ItemStack.fromTag(itemTag);
                Block b = Block.getBlockFromItem(s.getItem());
                if (b != null && b == state.getBlock()) {
                    return i;
                }
            }
        }
        
        return -1;
    }
    @Override
    public Vector<Integer> shulker_slots(PlayerEntity player,ItemStack item_stack){
        Vector<Integer> slots= new Vector<Integer>();
        CompoundTag entity_tag = item_stack.getSubTag("BlockEntityTag");
        ListTag shulker_items= entity_tag.getList("Items", 10);       
        for (int i = 0, len = shulker_items.size(); i < len; ++i) {
            CompoundTag itemTag = shulker_items.getCompound(i);
            ItemStack s = ItemStack.fromTag(itemTag);
            if (Block.getBlockFromItem(s.getItem()) != null) {
                slots.add(i);
            }
        }
        return slots;
    }
    @Override
    public ItemStack item_from_shulker(ItemStack shulker,int slot){
        ItemStack stack_item=null;
        CompoundTag entity_tag = shulker.getSubTag("BlockEntityTag");
        ListTag shulker_items= entity_tag.getList("Items", 10);        
        CompoundTag itemTag = shulker_items.getCompound(slot);
		stack_item = ItemStack.fromTag(itemTag);
        return stack_item;
    }
    @Override
    public void remove_item_from_shulker(ItemStack shulker,int slot,int n){
        ItemStack stack_item=null;
        CompoundTag entity_tag = shulker.getSubTag("BlockEntityTag");
        ListTag shulker_items= entity_tag.getList("Items", 10);        
        CompoundTag itemTag = shulker_items.getCompound(slot);
		stack_item = ItemStack.fromTag(itemTag);
        if(stack_item!=null && stack_item.getCount()>0){
            stack_item.setCount(stack_item.getCount() - n);
		    shulker_items.set(slot, stack_item.toTag(itemTag));
        }
    }
    //@Environment(EnvType.SERVER)
    @Override
    public void send_xp_to_player(PlayerEntity player){
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
		passedData.writeInt(player.experienceLevel);
		passedData.writeFloat(player.experienceProgress);
		ServerPlayNetworking.send((ServerPlayerEntity) player, WandsMod.WANDXP_PACKET_ID,passedData);
    }
    @Override
    public Block block_from_item(Item it){
        return Block.getBlockFromItem(it);
    }
    @Override
    public World world(PlayerEntity player){
        return player.world;
    }
    @Override
    public BlockState getDefaultBlockState(Block b){
        return b.getDefaultState();
    }
    @Override
    public boolean setBlockState(World w,BlockPos p,BlockState s){
        return w.setBlockState(p,s);
    }
    @Override
    public BlockState with_snow_layers(Block block,int n){
        return WandsMod.compat.getDefaultBlockState(block).with(SnowBlock.LAYERS, n);
    }
    @Override
    public int in_inventory(PlayerEntity player,ItemStack item_stack){
        return player.inventory.count(item_stack.getItem());
    }
    @Override
    public boolean has_tag(ItemStack item_stack)
    {
        return item_stack.hasTag();
    }
    @Environment(EnvType.CLIENT)
    @Override
    public void playBlockSound(PlayerEntity player,BlockState block_state,BlockPos pos,boolean destroy) {
        BlockSoundGroup blockSoundGroup = block_state.getSoundGroup();
        SoundEvent sound=null;
        if(destroy){
            sound=blockSoundGroup.getBreakSound();
        }else{
            sound=blockSoundGroup.getPlaceSound();
        }
        //System.out.println("sound "+sound);
        player.world.playSound(player, pos,sound, SoundCategory.BLOCKS, blockSoundGroup.getVolume(), blockSoundGroup.getPitch());

    }
    @Override
    public boolean can_destroy(BlockState block_state,ItemStack offhand,boolean isCreative){
        boolean destroy=false;
        boolean is_glass=block_state.getBlock() instanceof AbstractGlassBlock;
		if(offhand.getItem() instanceof MiningToolItem){
			MiningToolItem mt=(MiningToolItem)offhand.getItem();
			destroy= isCreative|| mt.getMiningSpeedMultiplier(null, block_state) > 1.0f|is_glass;			
		}
        return destroy;
    }
    @Override
    public String get_player_uuid(PlayerEntity player) {        
        return player.getUuidAsString();
    } 
    @Override
    public boolean destroy_block(World world,BlockPos pos,boolean drop){
        return world.breakBlock(pos, drop);
    }
    @Override
    public int get_silk_touch_level(ItemStack item) {        
        return EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, item);
    }
    @Override
    public int get_fortune_level(ItemStack item) {
        return EnchantmentHelper.getLevel(Enchantments.FORTUNE, item);
    }
    @Override
    public void dropStacks(BlockState state, World world, BlockPos pos) {
        Block.dropStacks(state,world, pos);        
    }
    @Override
    public void dropStack(World world, BlockPos pos, ItemStack item_stack) {
        Block.dropStack(world, pos,item_stack);        
    }
    @Override
    public Block block_from_id(String id) {        
        return Registry.BLOCK.get(Identifier.tryParse(id));
    }
    //@Environment(EnvType.SERVER)
    @Override
    public void send_block_placed(PlayerEntity player,BlockPos pos,boolean destroy){
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
		passedData.writeBlockPos(pos);
        passedData.writeBoolean(destroy);        
		ServerPlayNetworking.send((ServerPlayerEntity)player, WandsMod.WAND_PLACED_PACKET_ID, passedData);
    }
}
