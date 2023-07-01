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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandProps;
import net.nicguzzo.wands.wand.WandProps.Mode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

#if MC=="1165"
import me.shedaniel.architectury.networking.NetworkManager;
#else
import dev.architectury.networking.NetworkManager;
#endif
public class WandItem extends TieredItem implements Vanishable {
    public int limit;
    public boolean can_blast;
    public boolean unbreakable;
    public boolean removes_water;
    public boolean removes_lava;

    public WandItem(Tier tier, int limit, boolean removes_water, boolean removes_lava, boolean unbreakable,boolean can_blast, Properties properties) {
        super(tier,properties);
        this.limit=limit;
        this.removes_lava=removes_lava;
        this.removes_water=removes_water;
        this.unbreakable=unbreakable;
        this.can_blast=can_blast;
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
            ClientRender.wand.clear();
        }
/*
        Player player=context.getPlayer();
        if(player==null){
            return InteractionResult.FAIL;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(!stack.isEmpty() && stack.getItem() instanceof WandItem)) {
            return InteractionResult.FAIL;
        }
        Level world=context.getLevel();
        Wand wand;
        if(!world.isClientSide()){
            wand= PlayerWand.get(player);
            if(wand==null){
                PlayerWand.add_player(player);
                wand=PlayerWand.get(player);
                if(wand==null){
                    return InteractionResult.FAIL;
                }
            }
        }else{
            wand= ClientRender.wand;
        }
        wand.force_render=true;

        if (!wand.is_alt_pressed) {
            Vec3 hit = context.getClickLocation();
            BlockPos pos = context.getClickedPos();
            Direction side = context.getClickedFace();
            BlockState block_state = world.getBlockState(pos);
            Mode mode = WandProps.getMode(stack);
            //WandsMod.log("mode "+mode,true);

            if(mode==Mode.FILL||mode==Mode.LINE||mode==Mode.CIRCLE||mode==Mode.COPY){
                //if (WandItem.getIncSelBlock(stack)) {
                if (WandProps.getFlag(stack,WandProps.Flag.INCSELBLOCK)) {
                    pos=pos.relative(side,1);
                }
                if(mode==Mode.COPY) {
                    if (wand.copy_pos1 == null) {
                        wand.copy_pos1 = pos;
                        return InteractionResult.SUCCESS;
                    } else {
                        wand.copy_pos2 = pos;
                    }
                }else {
                    if (wand.p1 == null) {
                        //clear();
                        wand.p1_state = block_state;
                        wand.p2 = null;
                        wand.p1 = pos;
                        wand.x1 = pos.getX();
                        wand.y1 = pos.getY();
                        wand.z1 = pos.getZ();
                        return InteractionResult.SUCCESS;
                    } else {
                        block_state = wand.p1_state;
                        wand.p2 = pos;
                    }
                }
            }
            wand.lastPlayerDirection=context.getPlayer().getDirection();
            wand.do_or_preview(context.getPlayer(),world, block_state, pos, side, hit,stack,(WandItem)stack.getItem(),true);
            //wand.lastHitResult=null;
            if(!world.isClientSide()) {
                wand.palette.seed = world.random.nextInt(20000000);
                WandsMod.send_state((ServerPlayer) context.getPlayer(),wand);
            }
            if(mode==Mode.COPY && wand.copy_pos1!=null && wand.copy_pos2!=null){
                wand.copy_pos1=null;
                wand.copy_pos2=null;
            }
        }else{
            if(world.isClientSide()) {
                send_placement(wand);
            }
        }*/
        
        return InteractionResult.SUCCESS;
    }
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level world, @NotNull Player player, @NotNull InteractionHand interactionHand) {
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
                ClientRender.wand.clear();
            }
        }else{
            ClientRender.wand.clear();
            if(player!=null)
                player.displayClientMessage(Compat.literal("wand cleared"),false);
        }
        /*
        Wand wand;
        if(!world.isClientSide()){
            wand=PlayerWand.get(player);
            if(wand==null){
                PlayerWand.add_player(player);
                wand=PlayerWand.get(player);
                if(wand==null){
                    return InteractionResultHolder.fail(player.getItemInHand(interactionHand));
                }
            }
        }else{
            wand=ClientRender.wand;
            wand.force_render=true;
            if(wand.is_alt_pressed) {
                send_placement(wand);
            }
        }
        Mode mode = WandProps.getMode(stack);
        //if(wand.p1!=null && wand.p2){
        //    wand.clear();
        //}
        if(wand.target_air && mode.can_target_air() ){
            if(world.isClientSide()){
                if(wand.p1==null){
                    wand.p1=ClientRender.last_pos;
                    ClientRender.has_target=true;
                }else{
                    wand.p2=wand.get_pos_from_air(wand.hit);
                    WandsMod.LOGGER.info("p2: "+wand.p2);
                    send_placement(wand);
                }
            }
        }else {
            if (!wand.is_alt_pressed) {
                wand.clear();
            }
        }*/
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
        //WandsMod.log(" send_placement palette seed: " + seed,true);
        NetworkManager.sendToServer(WandsMod.POS_PACKET, packet);
        //WandsMod.LOGGER.info("send_placement p1: "+p1+" p2: "+p2);

    }
    /*public void send_placement(Wand wand) {
        Mode mode = WandProps.getMode(stack);
        if (mode == WandProps.Mode.FILL || mode == WandProps.Mode.LINE ||
                mode == WandProps.Mode.CIRCLE || mode == WandProps.Mode.COPY) {
            boolean inc_sel = WandProps.getFlag(stack, WandProps.Flag.INCSELBLOCK);
            //boolean target_air=WandProps.getFlag(stack, WandProps.Flag.TARGET_AIR);
            if (inc_sel && !block_state.isAir()) {
                p2 = p2.relative(side[0], 1);
            }
        }
    }*/
    /*public void send_placement(Wand wand){
        Minecraft client=Minecraft.getInstance();
        if(client.getConnection() != null) {
            if(wand.lastHitResult!=null && ClientRender.last_pos!=null && (wand.lastHitResult.getType()!= HitResult.Type.ENTITY)){
                FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
                if(wand.lastHitResult.getType()== HitResult.Type.BLOCK){
                    packet.writeBoolean(true);
                    BlockHitResult block_hit=(BlockHitResult) wand.lastHitResult;
                    packet.writeBlockHitResult(block_hit);
                    if(wand.p1!=null){
                        packet.writeBlockPos(wand.p1);
                    }else{
                        packet.writeBlockPos(block_hit.getBlockPos());
                    }
                }else{
                    packet.writeBoolean(false);
                    packet.writeVector3f(wand.lastHitResult.getLocation().toVector3f());
                     if(wand.p1!=null){
                        packet.writeBoolean(true);
                        packet.writeBlockPos(wand.p1);
                    }else{
                        packet.writeBoolean(false);
                        //packet.writeBlockPos(ClientRender.last_pos);
                    }
                }
                //if(wand.mode==Mode.FILL||wand.mode==Mode.LINE||wand.mode==Mode.CIRCLE||wand.mode==Mode.COPY) {
                //     wand.p2 = pos;
                //}
                if(wand.p2!=null) {
                    packet.writeBoolean(true);
                    packet.writeBlockPos(wand.p2);
                }else{
                    packet.writeBoolean(false);
                    //packet.writeBlockPos(ClientRender.last_pos);
                }
                packet.writeInt(ClientRender.wand.player.getDirection().getOpposite().ordinal());
                NetworkManager.sendToServer(WandsMod.POS_PACKET, packet);
                WandsMod.LOGGER.info("send_placement p1: "+wand.p1+" p2: "+wand.p2+" last_pos:"+ClientRender.last_pos);
            }
        }
    }*/

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, @NotNull TooltipFlag tooltipFlag) {
        CompoundTag tag=stack.getOrCreateTag();
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
    public int getEnchantmentValue() {
        return this.getTier().getEnchantmentValue();
    }
}
