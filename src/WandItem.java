package net.nicguzzo.wands;
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
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.mcver.MCVer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WandItem extends TieredItem implements Vanishable {
    public enum Mode{
        DIRECTION{
            public String toString() {
                return "Direction";
            }
        },
        ROW_COL{
            public String toString() {
                return "Row/Column";
            }
        },
        FILL{
            public String toString() {
                return "Fill";
            }
        },
        AREA{
            public String toString() {
                return "Area";
            }
        },
        GRID{
            public String toString() {
                return "Grid";
            }
        },
        LINE{
            public String toString() {
                return "Line";
            }
        },
        CIRCLE{
            public String toString() {
                return "Circle";
            }
        },
        RECT{
            public String toString() {
                return "Rectangle";
            }
        },
        COPY{
            public String toString() {
                return "Copy";
            }
        },
        PASTE{
            public String toString() {
                return "Paste";
            }
        },
        BLAST{
            public String toString() {
                return "Blast";
            }
        }
    }
    public enum Orientation {
        ROW, COL
    }
    public enum Plane {
        XZ,XY,YZ
    }

   public enum StateMode {
        CLONE{
            public String toString() {
                return "Clone";
            }
        },
        APPLY{
            public String toString() {
                return "Apply rotaion/axis";
            }
        }
    }
    public enum Action{
        PLACE{
            public String toString() {
                return "Place";
            }
        },
        REPLACE{
            public String toString() {
                return "Replace";
            }
        },
        DESTROY{
            public String toString() {
                return "Destroy";
            }
        },
        USE{
            public String toString() {
                return "Use";
            }
        }
    }

    public int limit = 0;
    //public int grid_limit=1;
    public boolean can_blast;
    public boolean unbreakable;
    public boolean removes_water;
    public boolean removes_lava;

    static public final Mode[] modes=Mode.values();
    //static public final StateMode[] state_modes=StateMode.values();
    static public final Action[] actions=Action.values();
    static public final Orientation[] orientations=Orientation.values();
    static public final Plane[] planes=Plane.values();
    static public final Direction.Axis[] axes=Direction.Axis.values();
    static public final Rotation[] rotations=Rotation.values();
    static public final StateMode[] state_modes=StateMode.values();

    
    public WandItem(Tier tier, int limit, boolean removes_water, boolean removes_lava, boolean unbreakable,boolean can_blast, Properties properties) {
        super(tier,properties);
        this.limit=limit;
        this.removes_lava=removes_lava;
        this.removes_water=removes_water;
        this.unbreakable=unbreakable;
        this.can_blast=can_blast;
        //this.grid_limit=(int)Math.sqrt(limit);
    }
    static public boolean is_wand(ItemStack stack) {
        return stack!=null && !stack.isEmpty() && stack.getItem() instanceof WandItem;
    }
    static public Mode getMode(ItemStack stack) {
        if(is_wand(stack)) {
            int m=stack.getOrCreateTag().getInt("mode");
            if(m<modes.length)
                return modes[m];
        }
        return Mode.DIRECTION;
    }
    static public void setMode(ItemStack stack,Mode mode) {
        if(is_wand(stack)) {
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("mode", mode.ordinal());
        }
    }
    static public void nextMode(ItemStack stack) {
        if(is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            int mode=(tag.getInt("mode")+1) % (modes.length);
            WandItem wand=(WandItem)stack.getItem();
            if(wand.can_blast && mode==Mode.BLAST.ordinal()){
                mode=Mode.DIRECTION.ordinal();
            }
            tag.putInt("mode", mode);
        }
    }
    static public void prevMode(ItemStack stack) {
        if(is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            int mode=tag.getInt("mode")-1;
            if(mode<0){
                mode=modes.length-1;
            }
            WandItem wand=(WandItem)stack.getItem();
            if(wand.can_blast && mode==Mode.BLAST.ordinal()){
                mode=Mode.BLAST.ordinal()-1;
            }
            tag.putInt("mode", mode);
        }
    }
    static public boolean isInverted(ItemStack stack) {
        if(is_wand(stack))
            return stack.getOrCreateTag().getBoolean("inverted");
        return false;
    }
    static public void invert(ItemStack stack) {
        if(is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            boolean inverted=tag.getBoolean("inverted");
            tag.putBoolean("inverted", !inverted);
        }
    }
    static public void setInvert(ItemStack stack,boolean i) {
        if(is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putBoolean("inverted", i);
        }
    }
    static public Orientation getOrientation(ItemStack stack) {
        if(is_wand(stack)){
            int o=stack.getOrCreateTag().getInt("orientation");
            return orientations[o];
        }
        return Orientation.ROW;
    }
    static public void setOrientation(ItemStack stack,Orientation o) {
        if(is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("orientation", o.ordinal());
        }
    }
    static public void nextOrientation(ItemStack stack) {
        if(is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            int o=(tag.getInt("orientation")+1) %2;
            tag.putInt("orientation", o);
        }
    }
    static public Plane getPlane(ItemStack stack) {
        Plane plane=Plane.XZ;
        if(is_wand(stack)){
            int p=stack.getOrCreateTag().getInt("plane");
            if(p>=0 && p<planes.length)
                plane=planes[p];
        }
        return plane;
    }
    static public void setPlane(ItemStack stack,Plane p) {
        if(is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("plane", p.ordinal());
        }
    }
    static public void nextPlane(ItemStack stack) {
        if(is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            int plane=(tag.getInt("plane")+1) % 3;
            tag.putInt("plane", plane);
        }
    }
    static public void toggleCircleFill(ItemStack stack) {
        if(is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            boolean cfill=tag.getBoolean("cfill");
            tag.putBoolean("cfill", !cfill);
        }
    }
    static public void setFill(ItemStack stack,boolean cfill) {
        if(is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putBoolean("cfill",cfill);
        }
    }
    static public boolean isCircleFill(ItemStack stack) {
        if(is_wand(stack)){
            return stack.getOrCreateTag().getBoolean("cfill");
        }
        return false;
    }

    static public Rotation getRotation(ItemStack stack) {
        if(is_wand(stack))
            return rotations[stack.getOrCreateTag().getInt("rotation")];
        return Rotation.NONE;
    }
    static public void nextRotation(ItemStack stack) {
        if(is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            int rot=(tag.getInt("rotation")+1) % rotations.length;
            tag.putInt("rotation", rot);
        }
    }
    static public void setRotation(ItemStack stack,Rotation rot) {
        if(is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("rotation", rot.ordinal());
        }
    }
    static public Action getAction(ItemStack stack) {
        if(is_wand(stack)) {
            int m=stack.getOrCreateTag().getInt("action");
            if(m<actions.length)
                return actions[m];
        }
        return Action.PLACE;
    }
    static public void setAction(ItemStack stack,Action a) {
        if(is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("action", a.ordinal());
        }
    }
    static public void nextAction(ItemStack stack) {
        if(is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            int a=(tag.getInt("action")+1) % (actions.length);
            tag.putInt("action", a);
        }
    }
    static public void prevAction(ItemStack stack) {
        if(is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            int a=tag.getInt("action")-1;
            if(a<0){
                a=actions.length-1;
            }
            tag.putInt("action", a);
        }
    }

    static public Direction.Axis getAxis(ItemStack stack) {
        Direction.Axis axis= Direction.Axis.Y;
        if(is_wand(stack)){
            int p=stack.getOrCreateTag().getInt("axis");
            if(p>=0 && p< axes.length)
                axis=axes[p];
        }
        return axis;
    }
    static public void setAxis(ItemStack stack,Direction.Axis axis) {
        if(is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("axis", axis.ordinal());
        }
    }
    static public void setAxis(ItemStack stack,int a) {
        if (stack != null && is_wand(stack) && !stack.isEmpty()) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt("axis", a);
        }
    }
    static public void nextAxis(ItemStack stack) {
        if(is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            int axis=(tag.getInt("axis")+1) % 3;
            tag.putInt("axis", axis);
        }
    }
    static public StateMode getStateMode(ItemStack stack) {
        if(is_wand(stack)) {
            int m=stack.getOrCreateTag().getInt("state_mode");
            if(m<state_modes.length)
                return state_modes[m];
        }
        return StateMode.CLONE;
    }
    static public void setStateMode(ItemStack stack,StateMode mode) {
        if(is_wand(stack)) {
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("state_mode", mode.ordinal());
        }
    }
    static public void setMultiplier(ItemStack stack,int m) {
        if(is_wand(stack)) {
            CompoundTag tag=stack.getOrCreateTag();
            WandItem w=(WandItem)stack.getItem();
            if(m>=1 && m<=16){
                tag.putInt("multiplier", m);
            }
        }
    }
    static public int getMultiplier(ItemStack stack) {
        if(is_wand(stack))
        {
            WandItem w=(WandItem)stack.getItem();
            int m=stack.getOrCreateTag().getInt("multiplier");
            if(m>=1 && m<=16){
                return m;
            }else{
                setMultiplier(stack,1);
            }
        }
        return 1;
    }
    static public void setRowColLimit(ItemStack stack,int m) {
        if(is_wand(stack)) {
            CompoundTag tag=stack.getOrCreateTag();
            WandItem w=(WandItem)stack.getItem();
            if(m>=0 && m<w.limit){
                tag.putInt("row_col_limit", m);
            }
        }
    }
    static public int getRowColLimit(ItemStack stack) {
        if(is_wand(stack))
        {
            WandItem w=(WandItem)stack.getItem();
            int m=stack.getOrCreateTag().getInt("row_col_limit");
            if(m>=0 && m<w.limit){
                return m;
            }else{
                setRowColLimit(stack,0);
            }
        }
        return 0;
    }
    static public void setAreaLimit(ItemStack stack,int m) {
        if(is_wand(stack)) {
            CompoundTag tag=stack.getOrCreateTag();
            WandItem w=(WandItem)stack.getItem();
            if(m>=0 && m<w.limit){
                tag.putInt("area_limit", m);
            }
        }
    }
    static public int getAreaLimit(ItemStack stack) {
        if(is_wand(stack))
        {
            WandItem w=(WandItem)stack.getItem();
            int m=stack.getOrCreateTag().getInt("area_limit");
            if(m>=0 && m<w.limit){
                return m;
            }else{
                setAreaLimit(stack,0);
            }
        }
        return 0;
    }
    static public void setBlastRadius(ItemStack stack,int r) {
        if(is_wand(stack)) {
            CompoundTag tag=stack.getOrCreateTag();
            WandItem w=(WandItem)stack.getItem();
            if(r>=4 && r<=16){
                tag.putInt("blast_radius", r);
            }
        }
    }
    static public int getBlastRadius(ItemStack stack) {
        if(is_wand(stack))
        {
            WandItem w=(WandItem)stack.getItem();
            int r=stack.getOrCreateTag().getInt("blast_radius");
            if(r>=4 && r<=16){
                return r;
            }else{
                setMultiplier(stack,4);
            }
        }
        return 4;
    }
    static public void setGridM(ItemStack stack, int m) {
        if(is_wand(stack)) {
            CompoundTag tag=stack.getOrCreateTag();
            WandItem w=(WandItem)stack.getItem();
            if(m>=1){
                int n=stack.getOrCreateTag().getInt("grid_n");
                if( (m*n)<=w.limit) {
                    tag.putInt("grid_m", m);
                }
            }
        }
    }
    static public void setGridN(ItemStack stack, int n) {
        if(is_wand(stack)) {
            CompoundTag tag=stack.getOrCreateTag();
            WandItem w=(WandItem)stack.getItem();
            if(n>=1){
                int m=stack.getOrCreateTag().getInt("grid_m");
                if( (m*n)<=w.limit) {
                    tag.putInt("grid_n", n);
                }
            }
        }
    }

    static public int getGridM(ItemStack stack) {
        if(is_wand(stack)) {
            CompoundTag tag = stack.getOrCreateTag();
            int m = tag.getInt("grid_m");
            if (m <= 0) {
                tag.putInt("grid_m", 3);
            }
            return m;
        }
        return 3;
    }
    static public int getGridN(ItemStack stack) {
        if(is_wand(stack)) {
            CompoundTag tag = stack.getOrCreateTag();
            int n = tag.getInt("grid_n");
            if (n <= 0) {
                tag.putInt("grid_n", 3);
            }
            return n;
        }
        return 3;
    }
    static public void setAreaDiagonalSpread(ItemStack stack,boolean s) {
        if(is_wand(stack)) {
            stack.getOrCreateTag().putBoolean("diag_spread", s);
        }
    }
    static public boolean getAreaDiagonalSpread(ItemStack stack) {
        if(is_wand(stack))
        {
            WandItem w=(WandItem)stack.getItem();
            return stack.getOrCreateTag().getBoolean("diag_spread");
        }
        return false;
    }
    static public void setMatchState(ItemStack stack,boolean s) {
        if(is_wand(stack)) {
            stack.getOrCreateTag().putBoolean("match_state", s);
        }
    }
    static public boolean getMatchState(ItemStack stack) {
        if(is_wand(stack))
        {
            WandItem w=(WandItem)stack.getItem();
            return stack.getOrCreateTag().getBoolean("match_state");
        }
        return false;
    }
    static public void setIncSelBlock(ItemStack stack,boolean s) {
        if(is_wand(stack)) {
            stack.getOrCreateTag().putBoolean("inc_sel_block", s);
        }
    }
    static public boolean getIncSelBlock(ItemStack stack) {
        if(is_wand(stack))
        {
            WandItem w=(WandItem)stack.getItem();
            return stack.getOrCreateTag().getBoolean("inc_sel_block");
        }
        return false;
    }
    static public void setStairSlab(ItemStack stack,boolean s) {
        if(is_wand(stack)) {
            stack.getOrCreateTag().putBoolean("stair_slab", s);
        }
    }
    static public boolean getStairSlab(ItemStack stack) {
        if(is_wand(stack))
        {
            WandItem w=(WandItem)stack.getItem();
            return stack.getOrCreateTag().getBoolean("stair_slab");
        }
        return false;
    }
    @Override
    public InteractionResult useOn(UseOnContext context) {    
        //WandsMod.LOGGER.info("UseOn");
        Level world=context.getLevel();
        Wand wand=null;
        if(!world.isClientSide()){
            wand=PlayerWand.get(context.getPlayer());
            if(wand==null){
                PlayerWand.add_player(context.getPlayer());
                wand=PlayerWand.get(context.getPlayer());
                if(wand==null){
                    return InteractionResult.FAIL;
                }
            }
        }else{
            wand=ClientRender.wand;
        }
        wand.force_render=true;
        ItemStack stack = context.getPlayer().getMainHandItem();//check anyway...
        if (!wand.is_alt_pressed && stack!=null && !stack.isEmpty() && stack.getItem() instanceof WandItem) {

            Vec3 hit = context.getClickLocation();
            BlockPos pos = context.getClickedPos();
            Direction side = context.getClickedFace();
            BlockState block_state = world.getBlockState(pos);
            Mode mode = WandItem.getMode(stack);
            //WandsMod.log("mode "+mode,true);

            if(mode==Mode.FILL||mode==Mode.LINE||mode==Mode.CIRCLE||mode==Mode.COPY||mode==Mode.RECT){
                if (WandItem.getIncSelBlock(stack)) {
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
            wand.do_or_preview(context.getPlayer(),world, block_state, pos, side, hit,stack,true);
            if(!world.isClientSide()) {
                wand.palette_seed = world.random.nextInt(20000000);
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
            //wand.clear();
        }
        if(!wand.is_alt_pressed) {
            wand.clear();
        }
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }
    public void send_placement(Wand wand){
        Minecraft client=Minecraft.getInstance();
        if(client.getConnection() != null) {
            if(wand.lastHitResult!=null){
                FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
                packet.writeBlockHitResult(wand.lastHitResult);
                if(wand.p1!=null)
                    packet.writeBlockPos(wand.p1);
                else
                    packet.writeBlockPos(wand.lastHitResult.getBlockPos());
                if(wand.mode==Mode.FILL||wand.mode==Mode.LINE||wand.mode==Mode.CIRCLE
                        ||wand.mode==Mode.COPY||wand.mode==Mode.RECT) {
                    if (ClientRender.last_pos != null) {
                        wand.p2 = true;
                    }
                }
                packet.writeBlockPos(ClientRender.last_pos);
                packet.writeBoolean(wand.p2);
                NetworkManager.sendToServer(WandsMod.POS_PACKET, packet);
            }
        }
    }
    static public boolean has_tools(ItemStack stack){
        ListTag tag = stack.getOrCreateTag().getList("Tools", MCVer.NbtType.COMPOUND);
        for (int i = 0; i < tag.size() && i<9; i++) {
            CompoundTag stackTag = (CompoundTag) tag.get(i);
            ItemStack itemStack = ItemStack.of(stackTag.getCompound("Tool"));
            if(!itemStack.isEmpty()){
                return true;
            }
        }
        return false;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        CompoundTag tag=stack.getOrCreateTag();

        list.add(MCVer.inst.literal("mode: " + WandItem.getMode(stack).toString() ));
        list.add(MCVer.inst.literal("limit: " + this.limit ));
        list.add(MCVer.inst.literal("orientation: "+orientations[tag.getInt("orientation")].toString()));
        int a=tag.getInt("axis");
        if(a<axes.length)
            list.add(MCVer.inst.literal("axis: "+axes[a].toString()));
        else
            list.add(MCVer.inst.literal("axis: none"));
        list.add(MCVer.inst.literal("plane: "+ Plane.values()[tag.getInt("plane")].toString()));
        list.add(MCVer.inst.literal("fill circle: "+ tag.getBoolean("cfill")));
        list.add(MCVer.inst.literal("rotation: "+ tag.getInt("rotation")));
    }
    public int getEnchantmentValue() {

        return this.getTier().getEnchantmentValue();

    }
}
