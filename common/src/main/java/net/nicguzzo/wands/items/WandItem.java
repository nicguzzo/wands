package net.nicguzzo.wands.items;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.networking.Networking;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandProps;
import net.nicguzzo.wands.wand.WandProps.Mode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.ItemLike;
import java.util.List;

import dev.architectury.networking.NetworkManager;
import net.minecraft.world.item.Vanishable;

public class WandItem extends Item implements Vanishable, Tier {
     public enum WandTier{
        STONE_WAND,
        COPPER_WAND,
        IRON_WAND,
        DIAMOND_WAND,
        NETHERITE_WAND,
        CREATIVE_WAND
    }
    public WandTier tier;
    @Override
    public int getUses() {return 0;}
    @Override
    public float getSpeed() {return 0;}
    @Override
    public float getAttackDamageBonus() {return 0;}
    @Override
    public int getLevel() {return 0;}
    @Override
    public @NotNull Ingredient getRepairIngredient() {
        switch (tier){
            case STONE_WAND -> {
                return Ingredient.of(ItemTags.STONE_TOOL_MATERIALS);
            }
            case COPPER_WAND -> {
            return Ingredient.of(new ItemLike[]{Items.COPPER_INGOT});
            }
            case IRON_WAND -> {
                return Ingredient.of(new ItemLike[]{Items.IRON_INGOT});
            }
            case DIAMOND_WAND -> {
                return Ingredient.of(new ItemLike[]{Items.DIAMOND});
            }
            case NETHERITE_WAND -> {
                return Ingredient.of(new ItemLike[]{Items.NETHERITE_INGOT});
            }
            case CREATIVE_WAND -> {
                return Ingredient.EMPTY;
            }
        }
        return Ingredient.EMPTY;
    }


    public int limit;
    public boolean can_blast;
    public boolean unbreakable;
    public boolean removes_water;
    public boolean removes_lava;
    //TODO: check ecnchantments!
    public WandItem(WandTier tier, int limit, boolean removes_water, boolean removes_lava, boolean unbreakable, boolean can_blast, Properties properties) {
        super(properties);
        this.tier = tier;
        this.limit = limit;
        this.removes_lava = removes_lava;
        this.removes_water = removes_water;
        this.unbreakable = unbreakable;
        this.can_blast = can_blast;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level world=context.getLevel();
        if(!world.isClientSide()){
            return InteractionResult.FAIL;
        }
        Player player=context.getPlayer();
        if(player==null){
            return InteractionResult.FAIL;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(!stack.isEmpty() && stack.getItem() instanceof WandItem)) {
            return InteractionResult.FAIL;
        }
        //WandsMod.LOGGER.info("UseOn");
        Mode mode = WandProps.getMode(stack);
        BlockPos pos = context.getClickedPos();
        BlockState block_state = world.getBlockState(pos);
        Direction side = context.getClickedFace();
        //WandsMod.LOGGER.info("UseOn p1 "+ClientRender.wand.getP1());
        //WandsMod.LOGGER.info("UseOn pps "+pos);
        boolean inc_sel = WandProps.getFlag(stack, WandProps.Flag.INCSELBLOCK);
        if(ClientRender.wand.getP1() ==null){
            ClientRender.wand.setP1(pos);
        }else{
            if(ClientRender.wand.getP2() ==null && mode.n_clicks()==2){
                ClientRender.wand.setP2(pos);
                //boolean target_air=WandProps.getFlag(stack, WandProps.Flag.TARGET_AIR);
                if (inc_sel && !block_state.isAir()) {
                    ClientRender.wand.setP2(ClientRender.wand.getP2().relative(side, 1));
                }
            }
        }
        if( (ClientRender.wand.getP1() !=null && mode.n_clicks()==1)||
           ((ClientRender.wand.getP1() !=null && ClientRender.wand.getP2() !=null && mode.n_clicks()==2))
        ) {
            send_placement(side, ClientRender.wand.getP1(), ClientRender.wand.getP2(), context.getClickLocation(),ClientRender.wand.palette.seed);
            ClientRender.wand.palette.seed= System.currentTimeMillis();
            ClientRender.wand.copy();
            ClientRender.wand.clear(ClientRender.wand.mode==Mode.PASTE || ClientRender.wand.mode== WandProps.Mode.COPY);
        }
        return InteractionResult.SUCCESS;
    }
    @Override
    public  InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {
        if(!world.isClientSide()){
            return InteractionResultHolder.fail(player.getItemInHand(interactionHand));
        }
        //WandsMod.LOGGER.info("use");
        ItemStack stack = player.getMainHandItem();
        if (!(!stack.isEmpty() && stack.getItem() instanceof WandItem)) {
            return InteractionResultHolder.fail(player.getItemInHand(interactionHand));
        }
        Wand wand=ClientRender.wand;
        Mode mode = WandProps.getMode(stack);
        if(wand.target_air && mode.can_target_air() ){
            if(wand.getP1() ==null){
                wand.setP1(ClientRender.last_pos);
                wand.setP2(null);
                ClientRender.has_target=true;
            }else{
                if(mode.n_clicks()==2) {
                    wand.setP2(wand.get_pos_from_air(wand.hit));
                }else{
                    wand.setP2(null);
                }

                send_placement(ClientRender.wand.player.getDirection().getOpposite(), wand.getP1(), wand.getP2(),wand.hit,wand.palette.seed);
                wand.palette.seed= System.currentTimeMillis();
                ClientRender.wand.copy();
                ClientRender.wand.clear(mode==Mode.PASTE || wand.mode== WandProps.Mode.COPY);
            }
        }else{
            //ClientRender.wand.clear();
            //if(player!=null)
                //player.displayClientMessage(Compat.literal("wand cleared"),false);
        }
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }

    public void send_placement(Direction side,BlockPos p1,BlockPos p2,Vec3 hit,long seed){
        Minecraft client=Minecraft.getInstance();
        if(client.getConnection() == null) {
            return;
        }
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeInt(side.ordinal());
        if(p1!=null){
            packet.writeBoolean(true);
            packet.writeBlockPos(p1);
        }else{
            packet.writeBoolean(false);
        }
        if(p2!=null){
            packet.writeBoolean(true);
            packet.writeBlockPos(p2);
        }else{
            packet.writeBoolean(false);
        }
        packet.writeDouble(hit.x);
        packet.writeDouble(hit.y);
        packet.writeDouble(hit.z);
        packet.writeLong(seed);
        NetworkManager.sendToServer(Networking.POS_PACKET, packet);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, @NotNull TooltipFlag tooltipFlag)
     {
        CompoundTag tag= Compat.getTags(stack);
        //CompoundTag tag=stack.getOrCreateTag();
        //TODO Complete translations
        //TODO add tools info
        list.add(Compat.literal("mode: ").append(Compat.translatable( WandProps.getMode(stack).toString() )));
        list.add(Compat.literal("limit: " + this.limit ));
        list.add(Compat.literal("orientation: ").append(Compat.translatable(WandProps.orientations[tag.getInt("orientation")].toString())));
        int a=tag.getInt("axis");
        if(a<WandProps.axes.length)
            list.add(Compat.literal("axis: "+WandProps.axes[a].toString()));
        else
            list.add(Compat.literal("axis: none"));
        list.add(Compat.literal("plane: "+ WandProps.Plane.values()[tag.getInt("plane")].toString()));
        list.add(Compat.literal("fill circle: "+ tag.getBoolean("cfill")));
        list.add(Compat.literal("rotation: "+ tag.getInt("rotation")));
        ListTag tools = tag.getList("Tools", Compat.NbtType.COMPOUND);
        if(ClientRender.wand!=null) {
            tools.forEach(element -> {
                 CompoundTag stackTag = (CompoundTag) element;
                //int slot = stackTag.getInt("Slot");
                 ItemStack item = ItemStack.of(stackTag.getCompound("Tool"));
                 list.add(Compat.literal("tool: ").append(item.getDisplayName()));
            });
            //list.add(Compat.literal("tools: " +tools.size()));
        }
    }
}
