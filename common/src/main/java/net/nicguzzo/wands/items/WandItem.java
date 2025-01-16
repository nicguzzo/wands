package net.nicguzzo.wands.items;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
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

import java.util.List;

public class WandItem extends Item {

    public int tier; //0 stone, 1 iron, 2 diamond, 3 netherite 4 creative
    public int limit;
    public boolean can_blast;
    public boolean unbreakable;
    public boolean removes_water;
    public boolean removes_lava;
    //TODO: check ecnchantments!

    public WandItem(int tier, int limit, boolean removes_water, boolean removes_lava, boolean unbreakable, boolean can_blast, Properties properties) {
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
        boolean inc_sel = WandProps.getFlag(stack, WandProps.Flag.INCSELBLOCK);
        if (ClientRender.wand.getP1() == null) {
            ClientRender.wand.setP1(pos);
        } else {
            if (ClientRender.wand.getP2() == null && mode.n_clicks() == 2) {
                ClientRender.wand.setP2(pos);
                //boolean target_air=WandProps.getFlag(stack, WandProps.Flag.TARGET_AIR);
                if (inc_sel && !block_state.isAir()) {
                    ClientRender.wand.setP2(ClientRender.wand.getP2().relative(side, 1));
                }
            }
        }
        if ((ClientRender.wand.getP1() != null && mode.n_clicks() == 1) || ((ClientRender.wand.getP1() != null && ClientRender.wand.getP2() != null && mode.n_clicks() == 2))) {
            send_placement(side, ClientRender.wand.getP1(), ClientRender.wand.getP2(), context.getClickLocation(), ClientRender.wand.palette.seed);
            ClientRender.wand.palette.seed = System.currentTimeMillis();
            ClientRender.wand.copy();
            ClientRender.wand.clear();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand interactionHand) {
        if (!world.isClientSide()) {
            return InteractionResult.FAIL;
        }
        //WandsMod.LOGGER.info("use");
        ItemStack stack = player.getMainHandItem();
        if (!(!stack.isEmpty() && stack.getItem() instanceof WandItem)) {
            return InteractionResult.FAIL;
        }
        Wand wand = ClientRender.wand;
        Mode mode = WandProps.getMode(stack);
        if (wand.target_air && mode.can_target_air()) {
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
                ClientRender.wand.clear();
            }
        } else {
            //ClientRender.wand.clear();
            //if(player!=null)
            //player.displayClientMessage(Compat.literal("wand cleared"),false);
        }
        return InteractionResult.PASS;
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
        NetworkManager.sendToServer(new Networking.PosPacket(side.ordinal(), has_p1_p2, _p1, _p2, new Networking.Vec3d(hit.x, hit.y, hit.z), seed));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        CompoundTag tag = Compat.getTags(stack);
        //CompoundTag tag=stack.getOrCreateTag();
        //TODO Complete translations
        //TODO add tools info
        list.add(Compat.literal("mode: ").append(Compat.translatable(WandProps.getMode(stack).toString())));
        list.add(Compat.literal("limit: " + this.limit));
        list.add(Compat.literal("orientation: ").append(Compat.translatable(WandProps.orientations[tag.getInt("orientation")].toString())));
        int a = tag.getInt("axis");
        if (a < WandProps.axes.length) list.add(Compat.literal("axis: " + WandProps.axes[a].toString()));
        else list.add(Compat.literal("axis: none"));
        list.add(Compat.literal("plane: " + WandProps.Plane.values()[tag.getInt("plane")].toString()));
        list.add(Compat.literal("fill circle: " + tag.getBoolean("cfill")));
        list.add(Compat.literal("rotation: " + tag.getInt("rotation")));
        ListTag tools = tag.getList("Tools", Compat.NbtType.COMPOUND);
    }
}
