package net.nicguzzo.wands.items;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
#if MC_VERSION<12111
import net.minecraft.world.InteractionResultHolder;
#endif
#if MC_VERSION >= 12111
import net.minecraft.world.item.component.TooltipDisplay;
#endif
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.render.ClientRender;
import net.nicguzzo.wands.networking.Networking;
import net.nicguzzo.compat.Compat;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.wand.WandProps;
import net.nicguzzo.wands.wand.WandProps.Mode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class WandItem extends Item {
    public enum WandTier{
        STONE_WAND,
        COPPER_WAND,
        IRON_WAND,
        DIAMOND_WAND,
        NETHERITE_WAND,
        CREATIVE_WAND
    }
    public WandTier tier; //0 stone, 1 iron, 2 diamond, 3 netherite 4 creative
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
        Level world = context.getLevel();
        if (!world.isClientSide()) {
            return InteractionResult.FAIL;
        }
        Player player = context.getPlayer();
        if (player == null) {
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
        // Only apply INCSELBLOCK offset for modes that show the toggle
        boolean inc_sel = WandProps.getFlag(stack, WandProps.Flag.INCSELBLOCK);
        boolean modeSupportsIncSel = WandProps.flagAppliesTo(WandProps.Flag.INCSELBLOCK, mode);
        boolean shouldOffset = modeSupportsIncSel && !inc_sel && !block_state.isAir();

        // Pin: override target position (offset already applied when pin was set)
        boolean pinActive = ClientRender.wand.pin.isActive();
        if (pinActive) {
            pos = ClientRender.wand.pin.getPos();
            // Dynamic side: if clicking on the pin block, use clicked face
            if (context.getClickedPos().equals(ClientRender.wand.pin.getPos())) {
                side = context.getClickedFace();
            } else {
                side = ClientRender.wand.pin.getSide() != null ? ClientRender.wand.pin.getSide() : side;
            }
            block_state = world.getBlockState(pos);
            shouldOffset = false;
        }
        if (ClientRender.wand.getP1() == null) {
            if (shouldOffset) {
                ClientRender.wand.setP1(pos.relative(side, 1));
            } else {
                ClientRender.wand.setP1(pos);
            }
            // Store P1's block state so client preview uses it in 2-click modes
            ClientRender.wand.p1_state = block_state;
        } else {
            if (ClientRender.wand.getP2() == null && mode.n_clicks() == 2) {
                ClientRender.wand.setP2(pos);
                if (shouldOffset) {
                    ClientRender.wand.setP2(ClientRender.wand.getP2().relative(side, 1));
                }
            } else if (mode == Mode.COPY) {
                BlockPos clickedPos = shouldOffset ? pos.relative(side, 1) : pos;
                ClientRender.wand.extendBbox(clickedPos);
            }
        }
        if ((ClientRender.wand.getP1() != null && mode.n_clicks() == 1) || ((ClientRender.wand.getP1() != null && ClientRender.wand.getP2() != null && mode.n_clicks() == 2))) {
            send_placement(side, ClientRender.wand.getP1(), ClientRender.wand.getP2(), context.getClickLocation(), ClientRender.wand.palette.seed);
            ClientRender.wand.palette.seed = System.currentTimeMillis();
            ClientRender.wand.copy();
            ClientRender.wand.pin.clear();
            if (mode != Mode.COPY) {
                ClientRender.wand.clear(mode == Mode.PASTE  || mode==Mode.AREA || mode == Mode.VEIN);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
#if MC_VERSION>=12111
    public InteractionResult use(Level world, Player player, InteractionHand interactionHand) {
#else
    public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {
#endif
        if (!world.isClientSide()) {
            #if MC_VERSION>=12111
               return InteractionResult.FAIL;
            #else
                return InteractionResultHolder.fail(player.getItemInHand(interactionHand));
            #endif
        }
        //WandsMod.LOGGER.info("use");
        ItemStack stack = player.getMainHandItem();
        if (!(!stack.isEmpty() && stack.getItem() instanceof WandItem)) {
            #if MC_VERSION>=12111
               return InteractionResult.FAIL;
            #else
                return InteractionResultHolder.fail(player.getItemInHand(interactionHand));
            #endif
        }
        Wand wand = ClientRender.wand;
        Mode mode = WandProps.getMode(stack);
        if (wand.pin.isActive()) {
            BlockPos pinPos = wand.pin.getPos();
            Direction side = wand.pin.getSide() != null ? wand.pin.getSide() : player.getDirection().getOpposite();
            if (wand.getP1() == null) {
                wand.setP1(pinPos);
                if (mode.n_clicks() == 1) {
                    send_placement(side, pinPos, null, player.getEyePosition(), wand.palette.seed);
                    wand.palette.seed = System.currentTimeMillis();
                    wand.copy();
                    wand.pin.clear();
                    wand.clear(mode == Mode.PASTE || mode == Mode.AREA);
                }
            } else if (mode.n_clicks() == 2 && wand.getP2() == null) {
                wand.setP2(pinPos);
                send_placement(side, wand.getP1(), pinPos, player.getEyePosition(), wand.palette.seed);
                wand.palette.seed = System.currentTimeMillis();
                wand.copy();
                wand.pin.clear();
                wand.clear(mode == Mode.PASTE || mode == Mode.AREA);
            }
            #if MC_VERSION>=12111
            return InteractionResult.SUCCESS;
            #else
            return InteractionResultHolder.success(player.getItemInHand(interactionHand));
            #endif
        }
        if (wand.target_air && mode.can_target_air()) {
            // Check if player has something to place with (offhand block, palette, or copy/paste mode)
            // Destroy and Use actions don't need an offhand block
            WandProps.Action action = WandProps.getAction(stack);
            boolean hasPalette = wand.palette.has_palette && !wand.palette.palette_slots.isEmpty();
            Block offhandBlock = Block.byItem(player.getOffhandItem().getItem());
            boolean hasOffhand = offhandBlock != Blocks.AIR;
            if (!hasOffhand && !hasPalette && mode != Mode.PASTE && mode != Mode.COPY
                    && action != WandProps.Action.DESTROY && action != WandProps.Action.USE) {
                player.displayClientMessage(Compat.translatable("wands.message.target_air_needs_offhand"), true);
#if MC_VERSION>=12111
                return InteractionResult.FAIL;
#else
                return InteractionResultHolder.fail(player.getItemInHand(interactionHand));
#endif
            }
            if (wand.getP1() == null) {
                wand.setP1(ClientRender.last_pos);
                wand.setP2(null);
                ClientRender.has_target = true;
            } else {
                if (mode.n_clicks() == 2) {
                    wand.setP2(wand.get_pos_from_air(wand.hit));
                } else {
                    wand.setP2(null);
                }

                send_placement(ClientRender.wand.player.getDirection().getOpposite(), wand.getP1(), wand.getP2(), wand.hit, wand.palette.seed);
                wand.palette.seed = System.currentTimeMillis();
                ClientRender.wand.copy();
                ClientRender.wand.clear(mode==Mode.PASTE || wand.mode== WandProps.Mode.COPY || mode == Mode.VEIN);
            }
        } else {
            //ClientRender.wand.clear();
            //if(player!=null)
            //player.displayClientMessage(Compat.literal("wand cleared"),false);
        }
#if MC_VERSION>=12111
        return InteractionResult.PASS;
#else
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
#endif
    }
    @Environment(EnvType.CLIENT)
    public void send_placement(Direction side, BlockPos p1, BlockPos p2, Vec3 hit, long seed) {
        BlockPos _p1 = new BlockPos(0, 0, 0);
        BlockPos _p2 = new BlockPos(0, 0, 0);
        int has_p1_p2 = 0;
        if (p1 != null && p2 != null) {
            has_p1_p2 = 3;
            _p1 = p1;
            _p2 = p2;
        } else {
            if (p1 != null) {
                has_p1_p2 = 1;
                _p1 = p1;
            } else {
                if (p2 != null) {
                    has_p1_p2 = 2;
                    _p2 = p2;
                }
            }
        }
        //WandsMod.LOGGER.info("send_placement p1: "+p1+" p2: "+p2);
        Networking.sendPosPacket(side,has_p1_p2,_p1,_p2,hit,seed);
        //NetworkManager.sendToServer(new Networking.PosPacket(side.ordinal(), has_p1_p2, _p1, _p2, new Networking.Vec3d(hit.x, hit.y, hit.z), seed));
    }

    @Environment(EnvType.CLIENT)
#if MC_VERSION >= 12111
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag)
    #else
#if MC_VERSION >= 12101
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag)
#else
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag)
#endif
#endif
    {
        CompoundTag tag = Compat.getTags(stack);
        //CompoundTag tag=stack.getOrCreateTag();
        //TODO Complete translations
        //TODO add tools info
        Component l0=Compat.literal("mode: ").append(Compat.translatable(WandProps.getMode(stack).toString()));
        Component l1=Compat.literal("limit: " + this.limit);
        Component l2=Compat.literal("orientation: ").append(Compat.translatable(WandProps.orientations[Compat.getInt(tag,"orientation").orElse(0)].toString()));
        int a = Compat.getInt(tag,"axis").orElse(0);
        Component l3;
        if (a < WandProps.axes.length) {
            l3=Compat.literal("axis: " + WandProps.axes[a].toString());
        }
        else{
            l3=Compat.literal("axis: none");
        }
        Component l4=Compat.literal("plane: " + WandProps.Plane.values()[Compat.getInt(tag,"plane").orElse(0)].toString());
        Component l5=Compat.literal("fill circle: " + Compat.getBoolean(tag,"cfill").orElse(false));
        Component l6=Compat.literal("rotation: " + Compat.getInt(tag,"rotation").orElse(0));

        #if MC_VERSION >= 12111
        consumer.accept(l0);
        consumer.accept(l1);
        consumer.accept(l2);
        consumer.accept(l3);
        consumer.accept(l4);
        consumer.accept(l5);
        consumer.accept(l6);
        #else
        list.add(l0);
        list.add(l1);
        list.add(l2);
        list.add(l3);
        list.add(l4);
        list.add(l5);
        list.add(l6);
        #endif
    }
}
