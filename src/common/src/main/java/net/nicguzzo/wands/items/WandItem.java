package net.nicguzzo.wands.items;
#if MC=="1165"
import me.shedaniel.architectury.networking.NetworkManager;
#else
import dev.architectury.networking.NetworkManager;
#endif
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.wand.PlayerWand;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.wand.WandProps;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.nicguzzo.wands.wand.WandProps.Mode;

public class WandItem extends TieredItem implements Vanishable {

    public int limit = 0;
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
    public InteractionResult useOn(UseOnContext context) {    
        //WandsMod.LOGGER.info("UseOn");
        Player player=context.getPlayer();
        if(player==null){
            return InteractionResult.FAIL;
        }
        Level world=context.getLevel();
        Wand wand=null;
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

        ItemStack stack = player.getMainHandItem();
        if (!wand.is_alt_pressed && !stack.isEmpty() && stack.getItem() instanceof WandItem) {
            Vec3 hit = context.getClickLocation();
            BlockPos pos = context.getClickedPos();
            Direction side = context.getClickedFace();
            BlockState block_state = world.getBlockState(pos);
            Mode mode = WandProps.getMode(stack);
            //WandsMod.log("mode "+mode,true);

            if(mode==Mode.FILL||mode==Mode.LINE||mode==Mode.CIRCLE||mode==Mode.COPY/*||mode==Mode.RECT*/){
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
                        wand.p2 = false;
                        wand.p1 = pos;
                        wand.x1 = pos.getX();
                        wand.y1 = pos.getY();
                        wand.z1 = pos.getZ();
                        return InteractionResult.SUCCESS;
                    } else {
                        block_state = wand.p1_state;
                        wand.p2 = true;
                    }
                }
            }
            wand.lastPlayerDirection=context.getPlayer().getDirection();
            wand.do_or_preview(context.getPlayer(),world, block_state, pos, side, hit,stack,true);
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
        }
        
        return InteractionResult.SUCCESS;
    }
    @Override
    public  InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {   
        //WandsMod.LOGGER.info("use");
        Wand wand=null;
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
        if(!wand.is_alt_pressed) {
            wand.clear();
        }
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }
    public void send_placement(Wand wand){
        Minecraft client=Minecraft.getInstance();
        if(client.getConnection() != null) {
            if(wand.lastHitResult!=null && ClientRender.last_pos!=null){
                FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
                packet.writeBlockHitResult(wand.lastHitResult);
                if(wand.p1!=null) {
                    packet.writeBlockPos(wand.p1);
                }else {
                    packet.writeBlockPos(wand.lastHitResult.getBlockPos());
                }
                if(wand.mode==Mode.FILL||wand.mode==Mode.LINE||wand.mode==Mode.CIRCLE
                        ||wand.mode==Mode.COPY) {
                    if (ClientRender.last_pos != null) {
                        wand.p2 = true;
                    }
                }
                packet.writeBlockPos(ClientRender.last_pos);
                packet.writeBoolean(wand.p2);
                packet.writeInt(ClientRender.wand.lastPlayerDirection.ordinal());
                NetworkManager.sendToServer(WandsMod.POS_PACKET, packet);
                WandsMod.LOGGER.info("send_placement");
                //wand.lastHitResult=null;
            }
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
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
                        int slot = stackTag.getInt("Slot");
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
