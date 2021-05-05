package net.nicguzzo;

import java.util.Vector;
import java.util.function.Consumer;
import net.minecraft.block.BushBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.AbstractGlassBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.CompoundNBT;
import net.nicguzzo.common.ICompatMod;
import net.nicguzzo.common.MyDir;
import net.nicguzzo.common.WandItem;

public class ICompatModImpl implements ICompatMod{
    @OnlyIn(Dist.CLIENT)
    public PlayerEntity get_player(){
        assert Minecraft.getInstance().player != null;
        Minecraft instance=Minecraft.getInstance();
        return instance.player;
    }
    public boolean is_fluid(BlockState state,WandItem wand){
        if(wand!=null){
            if(wand.removes_water && wand.removes_lava){                
                return state.getFluidState().is(FluidTags.WATER)||state.getFluidState().is(FluidTags.LAVA);
            }else if(wand.removes_water){
                return state.getFluidState().is(FluidTags.WATER);
            }
        }
        return false;        
    }
    
    public boolean is_double_slab(BlockState state){        
        if(state.getBlock() instanceof SlabBlock){
            return state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE;
        }
        return false;
    }
    public BlockPos pos_offset(BlockPos pos,MyDir dir,int o)
    {
        Direction dir1=Direction.values()[dir.ordinal()];                
        return new BlockPos(pos.getX() + dir1.getStepX()* o, pos.getY() + dir1.getStepY()*o, pos.getZ() + dir1.getStepZ()*o);
    }
    public int get_next_int_random(PlayerEntity player,int b){
        return player.level.random.nextInt(b);
    }
    public BlockState random_rotate(BlockState state,World world){
        return state.rotate(world, null,Rotation.getRandom(world.random));
    }
    public int get_main_inventory_size(PlayerEntity player){
        
        return player.inventory.items.size();
    }
    public ItemStack get_player_main_stack(PlayerEntity player,int i){
        return player.inventory.items.get(i);
    }
    public ItemStack get_player_offhand_stack(PlayerEntity player){
        return player.inventory.offhand.get(0);
    }
    public void player_offhand_stack_inc(PlayerEntity player,int i){
        player.inventory.offhand.get(0).grow(i);
    }
    public void player_offhand_stack_dec(PlayerEntity player,int i){
        player.inventory.offhand.get(0).shrink(i);
    }
    public void player_stack_inc(PlayerEntity player,int slot,int i){
        player.inventory.items.get(slot).grow(i);
    }
    public void player_stack_dec(PlayerEntity player,int slot,int i){
        player.inventory.items.get(slot).shrink(i);
    }
    public void set_player_xp(PlayerEntity player,float xp){
        player.experienceProgress=xp;
    }
    public boolean item_stacks_equal(ItemStack i1,ItemStack i2){        
        return ItemStack.tagMatches(i1,i2);
    }
    public boolean is_player_holding_wand(PlayerEntity player){
        if(player.inventory.getSelected()!=null){
            return player.inventory.getSelected().getItem() instanceof WandItemForge;
        }else{
            return false;
        }
    }
    public WandItem get_player_wand(PlayerEntity player){
        if(is_player_holding_wand(player))
            return ((WandItemForge)player.inventory.getSelected().getItem()).wand;
        else
            return null;
    }
    public void inc_wand_damage(PlayerEntity player,ItemStack stack,int damage){
        
        stack.hurtAndBreak(damage, (LivingEntity)player, 
						(Consumer<LivingEntity>)((p) -> {
								((LivingEntity)p).broadcastBreakEvent(Hand.MAIN_HAND);
							}
						)
					);
        
    }
    public boolean interescts_player_bb(PlayerEntity player,double x1,double y1,double z1,double x2,double y2,double z2){
        AxisAlignedBB bb=player.getBoundingBox();
        return bb.intersects(x1,y1,z1,x2,y2,z2);
    }
    @OnlyIn(Dist.CLIENT)
    public void send_message_to_player(String msg){
        assert Minecraft.getInstance().player != null;
        Minecraft instance=Minecraft.getInstance();
        instance.player.displayClientMessage(new TranslationTextComponent(msg), true);
    }
    @Override
    public boolean is_plant(BlockState state) {
        return (state.getBlock() instanceof BushBlock);        
    }
    @Override
    public boolean is_shulker(PlayerEntity player,ItemStack item_stack){
        ItemStack offhand = get_player_offhand_stack(player);
        Block blk = block_from_item(offhand.getItem());        
		return offhand != null && blk instanceof ShulkerBoxBlock ;
    }
    @Override
    public int in_shulker(PlayerEntity player,ItemStack item_stack){
        ListNBT shulker_items=null;
        int in_shulker=0;        
        if(is_shulker(player, item_stack)){
            ItemStack shulker = WandsMod.compat.get_player_offhand_stack(player);
            CompoundNBT entity_tag =shulker.getTagElement("BlockEntityTag");
            if(entity_tag!=null){
                shulker_items = entity_tag.getList("Items", 10);		
                if(shulker_items!=null){
                    for (int i = 0, len = shulker_items.size(); i < len; ++i) {
                        CompoundNBT itemTag = shulker_items.getCompound(i);
                        ItemStack s = ItemStack.of(itemTag);
                        if(WandItem.fill_pos1!=null){
                            Item it=Item.BY_BLOCK.getOrDefault(WandItem.fill_pos1, Items.AIR);
                            //Item it=Item.byBlock(player.level.getBlockState(WandItem.fill_pos1).getBlock());
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
        
        CompoundNBT entity_tag = item_stack.getTagElement("BlockEntityTag");
        ListNBT shulker_items= entity_tag.getList("Items", 10);        
        
        if (shulker_items != null) {
            for (int i = 0, len = shulker_items.size(); i < len; ++i) {
                CompoundNBT itemTag = shulker_items.getCompound(i);
                ItemStack s = ItemStack.of(itemTag);
                Block b = Block.byItem(s.getItem());
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
        CompoundNBT entity_tag = item_stack.getTagElement("BlockEntityTag");
        ListNBT shulker_items= entity_tag.getList("Items", 10);       
        for (int i = 0, len = shulker_items.size(); i < len; ++i) {
            CompoundNBT itemTag = shulker_items.getCompound(i);
            ItemStack s = ItemStack.of(itemTag);
            if (Block.byItem(s.getItem()) != null) {
                slots.add(i);
            }
        }
        return slots;
    }
    @Override
    public ItemStack item_from_shulker(ItemStack shulker,int slot){
        ItemStack stack_item=null;
        CompoundNBT entity_tag = shulker.getTagElement("BlockEntityTag");
        ListNBT shulker_items= entity_tag.getList("Items", 10);        
        CompoundNBT itemTag = shulker_items.getCompound(slot);
		stack_item = ItemStack.of(itemTag);
        return stack_item;
    }
    @Override
    public void remove_item_from_shulker(ItemStack shulker,int slot,int n){
        ItemStack stack_item=null;
        CompoundNBT entity_tag = shulker.getTagElement("BlockEntityTag");
        ListNBT shulker_items= entity_tag.getList("Items", 10);        
        CompoundNBT itemTag = shulker_items.getCompound(slot);
		stack_item = ItemStack.of(itemTag);
        if(stack_item!=null && stack_item.getCount()>0){
            stack_item.setCount(stack_item.getCount() - n);
		    shulker_items.setTag(slot, stack_item.save(itemTag));
        }
    }
    @Override
    public void send_xp_to_player(PlayerEntity player){
        /*PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
		passedData.writeInt(player.experienceLevel);
		passedData.writeFloat(player.experienceProgress);
		ServerPlayNetworking.send((ServerPlayerEntity) player, WandsMod.WANDXP_PACKET_ID,passedData);*/
    }
    @Override
    public Block block_from_item(Item it){
        return Block.byItem(it);
    }
    @Override
    public World world(PlayerEntity player){
        return player.level;
    }
    @Override
    public BlockState getDefaultBlockState(Block b){
        return b.defaultBlockState();
    }
    @Override
    public boolean setBlockState(World w,BlockPos p,BlockState s){
        return w.setBlockAndUpdate(p,s);
    }
    @Override
    public BlockState with_snow_layers(Block block,int n){
        return WandsMod.compat.getDefaultBlockState(block).setValue(SnowBlock.LAYERS, n);
    }
    @Override
    public int in_inventory(PlayerEntity player,ItemStack item_stack){
        return player.inventory.countItem(item_stack.getItem());
    }
    @Override
    public boolean has_tag(ItemStack item_stack){
        return item_stack.getTag()!=null;
    }
    @Override
    public boolean can_destroy(BlockState block_state,ItemStack offhand,boolean isCreative){
        boolean destroy=false;
        boolean is_glass=block_state.getBlock() instanceof AbstractGlassBlock;
		if(offhand.getItem() instanceof ToolItem){
			ToolItem mt=(ToolItem)offhand.getItem();
			destroy= isCreative|| mt.getDestroySpeed(null, block_state) > 1.0f|is_glass;			
		}
        return destroy;
    } 
    @Override
    public String get_player_uuid(PlayerEntity player) {        
        return player.getStringUUID();
    } 
    @Override
    public boolean destroy_block(World world,BlockPos pos,boolean drop){
        return world.destroyBlock(pos, drop);
    }
    @Override
    public int get_silk_touch_level(ItemStack item) {        
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, item);
    }
    @Override
    public int get_fortune_level(ItemStack item) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, item);
    }
    @Override
    public void dropStacks(BlockState state, World world, BlockPos pos) {
        Block.dropResources(state,world, pos);        
    }
    @Override
    public void dropStack(World world, BlockPos pos, ItemStack item_stack) {
        Block.popResource(world, pos,item_stack);        
    }
    @Override
    public void playBlockSound(PlayerEntity player, BlockState block_state, BlockPos pos, boolean destroy) {
        player=get_player();        
        SoundType soundtype = block_state.getSoundType();
        SoundEvent sound=null;
        if(destroy){
            sound=soundtype.getBreakSound();
        }else{
            sound=soundtype.getPlaceSound();
        }
        player.level.playSound(player, pos,sound, SoundCategory.BLOCKS, soundtype.getVolume() , soundtype.getPitch());
    }
    @Override
    public Block block_from_id(String id) {        
        return ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(id));
        //return Registry.BLOCK.get(ResourceLocation.tryParse(id));
    }
    @Override
    public void send_block_placed(PlayerEntity player, BlockPos pos, boolean destroy) {        
        WandsPacketHandler.INSTANCE.sendTo(new SendBlockPlaced(pos,destroy), ((ServerPlayerEntity)player).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }
    @Override
    public void block_after_break(Block block, World world, PlayerEntity player, BlockPos pos, BlockState state,
            ItemStack stack) {        
            block.playerDestroy(world, player, pos, state, null, stack);
    }
    
}
