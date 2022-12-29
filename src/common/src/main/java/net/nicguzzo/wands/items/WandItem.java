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
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.wand.PlayerWand;
import net.nicguzzo.wands.wand.Wand;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.render.ClientRender;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WandItem extends TieredItem implements Vanishable {

      public enum Mode{
        DIRECTION { public String toString() {return "Direction"; }},
        ROW_COL   { public String toString() {return "Row/Column";}},
        FILL      { public String toString() {return "Fill";      }},
        AREA      { public String toString() {return "Area";      }},
        GRID      { public String toString() {return "Grid";      }},
        LINE      { public String toString() {return "Line";      }},
        CIRCLE    { public String toString() {return "Circle";    }},
        RECT      { public String toString() {return "Rectangle"; }},
        COPY      { public String toString() {return "Copy";      }},
        PASTE     { public String toString() {return "Paste";     }},
        VEIN      { public String toString() {return "Vein";      }},
        BLAST     { public String toString() {return "Blast";     }},
    }
    public enum Orientation {
        ROW, COL
    }
    public enum Plane {
        XZ,XY,YZ
    }
    public enum MirrorAxis {
        NONE,X,Y,Z
    }
    public enum StateMode {
        CLONE  { public String toString() { return "Clone";             }},
        APPLY  { public String toString() { return "Apply rotaion/axis";}},
        TARGET { public String toString() { return "Use Target";}}
    }
    public enum Action{
        PLACE  { public String toString() { return "Place";   }},
        REPLACE{ public String toString() { return "Replace"; }},
        DESTROY{ public String toString() { return "Destroy"; }},
        USE    { public String toString() { return "Use";     }}
    }
    public enum Flag{
        INVERTED    { public String toString() {return "inverted";}},
        FILLED      { public String toString() {return "cfill";}},
        EVEN        { public String toString() {return "circle_even";}},
        DIAGSPREAD  { public String toString() {return "diag_spread";}},
        MATCHSTATE  { public String toString() {return "match_state";}},
        INCSELBLOCK { public String toString() {return "inc_sel_block";}},
        STAIRSLAB   { public String toString() {return "stair_slab";}}
    }
    public enum Value{
        MULTIPLIER { public String toString() {return  "multiplier" ;}},
        ROWCOLLIM  { public String toString() {return  "row_col_limit" ;}},
        AREALIM    { public String toString() {return  "area_limit" ;}},
        BLASTRAD   { public String toString() {return  "blast_radius" ;}},
        GRIDM      { public String toString() {return  "grid_m" ;}},
        GRIDN      { public String toString() {return  "grid_n" ;}},
        GRIDMS     { public String toString() {return  "grid_msp" ;}},
        GRIDNS     { public String toString() {return  "grid_nsp" ;}},
        GRIDMOFF   { public String toString() {return  "grid_moff" ;}},
        GRIDNOFF   { public String toString() {return  "grid_noff" ;}},
        MIRRORAXIS { public String toString() {return  "mirror_axis" ;}},
        SKIPBLOCK { public String toString() {return  "skip_block" ;}};
        public int def=0;
        public int min=0;
        public int max= Wand.MAX_LIMIT;
        public Value coval=null;
        static{
            MULTIPLIER.def=1; MULTIPLIER.min=1; MULTIPLIER.max=16;
            ROWCOLLIM.def=0; ROWCOLLIM.min=0;
            BLASTRAD.def=4; BLASTRAD.min=4; BLASTRAD.max=16;
            GRIDM.coval=GRIDN;
            GRIDN.coval=GRIDM;
            GRIDM.min=1;
            GRIDN.min=1;
            MIRRORAXIS.min=0;//0 disabled - 1=X - 2=Y - 3=Z
            MIRRORAXIS.max=3;
            SKIPBLOCK.max=100;
        }        
    }

    public int limit = 0;
    public boolean can_blast;
    public boolean unbreakable;
    public boolean removes_water;
    public boolean removes_lava;

    static public final Mode[] modes=Mode.values();
    static public final Action[] actions=Action.values();
    static public final Orientation[] orientations=Orientation.values();
    static public final Plane[] planes=Plane.values();
    static public final Direction.Axis[] axes=Direction.Axis.values();
    static public final Rotation[] rotations=Rotation.values();
    static public final StateMode[] state_modes=StateMode.values();
    static public final MirrorAxis[] mirrorAxes=MirrorAxis.values();

    public WandItem(Tier tier, int limit, boolean removes_water, boolean removes_lava, boolean unbreakable,boolean can_blast, Properties properties) {
        super(tier,properties);
        this.limit=limit;
        this.removes_lava=removes_lava;
        this.removes_water=removes_water;
        this.unbreakable=unbreakable;
        this.can_blast=can_blast;
    }
    static public boolean is_wand(ItemStack stack) {
        return stack!=null && !stack.isEmpty() && stack.getItem() instanceof WandItem;
    }
    static public boolean getFlag(ItemStack stack,Flag flag) {
        if(is_wand(stack)) {
            return stack.getOrCreateTag().getBoolean(flag.toString());
        }
        return false;
    }
    static public void setFlag(ItemStack stack,Flag flag,boolean f) {
        if(is_wand(stack)) {
            stack.getOrCreateTag().putBoolean(flag.toString(), f);
        }
    }
    static public void toggleFlag(ItemStack stack,Flag flag) {
        if(is_wand(stack)) {
            stack.getOrCreateTag().putBoolean(flag.toString(), !stack.getOrCreateTag().getBoolean(flag.toString()));
        }
    }

    static public void setVal(ItemStack stack,Value v, int n) {
        if(is_wand(stack)) {
            stack.getOrCreateTag().putInt(v.toString(), n);
        }
    }
    static public void incVal(ItemStack stack,Value v, int inc,int max) {
        if(is_wand(stack)) {
            int n=stack.getOrCreateTag().getInt(v.toString());
            if(n+inc<=max)
                stack.getOrCreateTag().putInt(v.toString(), n+inc);
        }
    }
    static public void incVal(ItemStack stack,Value v, int inc) {
        incVal(stack,v,inc,v.max);
    }
    static public void decVal(ItemStack stack,Value v, int inc,int min) {
        if(is_wand(stack)) {
            int n=stack.getOrCreateTag().getInt(v.toString());
            if(n-inc>=min)
                stack.getOrCreateTag().putInt(v.toString(), n-inc);
        }
    }
    static public void decVal(ItemStack stack,Value v, int dec) {
        decVal(stack,v,dec,v.min);
    }
    static public int getVal(ItemStack stack,Value v) {
        if(is_wand(stack)) {
            int i= stack.getOrCreateTag().getInt(v.toString());
            if(i<v.min){
                return v.min;
            }else{
                if(i>v.max){
                    return v.max;
                }
            }
            return i;
        }
        return -1;
    }

    static public Mode getMode(ItemStack stack) {
        if(is_wand(stack)) {
            int m=stack.getOrCreateTag().getInt("mode");
            if(m>=0 && m<modes.length)
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
            if(mode==Mode.VEIN.ordinal()  && !WandsMod.config.enable_vein_mode){
                mode = Mode.BLAST.ordinal();
            }
            if( (!wand.can_blast && mode==Mode.BLAST.ordinal())
                || (!WandsMod.config.enable_blast_mode && mode==Mode.BLAST.ordinal())
            )
            {
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

            if( (!wand.can_blast && mode==Mode.BLAST.ordinal())
                    || (!WandsMod.config.enable_blast_mode && mode==Mode.BLAST.ordinal())
            )
            {
                mode=Mode.BLAST.ordinal()-1;
            }
            if(mode==Mode.VEIN.ordinal()  && !WandsMod.config.enable_vein_mode){
                mode = Mode.VEIN.ordinal()-1;
            }
            tag.putInt("mode", mode);
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
        return StateMode.TARGET;
    }
    static public void setStateMode(ItemStack stack,StateMode mode) {
        if(is_wand(stack)) {
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("state_mode", mode.ordinal());
        }
    }
    static public void incGrid(ItemStack stack,Value v, int n) {
        if(is_wand(stack)) {
            CompoundTag tag=stack.getOrCreateTag();
            WandItem w=(WandItem)stack.getItem();
            if(v.coval!=null){
                int c=stack.getOrCreateTag().getInt(v.coval.toString());
                if( (c*n)<=w.limit) {
                    int nn=stack.getOrCreateTag().getInt(v.toString());
                    tag.putInt(v.toString(), nn+n);
                }
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {    
        //WandsMod.LOGGER.info("UseOn");
        Level world=context.getLevel();
        Wand wand=null;
        if(!world.isClientSide()){
            wand= PlayerWand.get(context.getPlayer());
            if(wand==null){
                PlayerWand.add_player(context.getPlayer());
                wand=PlayerWand.get(context.getPlayer());
                if(wand==null){
                    return InteractionResult.FAIL;
                }
            }
        }else{
            wand= ClientRender.wand;
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
                //if (WandItem.getIncSelBlock(stack)) {
                if (WandItem.getFlag(stack,Flag.INCSELBLOCK)) {
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
                packet.writeInt(ClientRender.wand.lastPlayerDirection.ordinal());
                NetworkManager.sendToServer(WandsMod.POS_PACKET, packet);
            }
        }
    }
    static public boolean has_tools(ItemStack stack){
        ListTag tag = stack.getOrCreateTag().getList("Tools", Compat.NbtType.COMPOUND);
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

        list.add(Compat.literal("mode: " + WandItem.getMode(stack).toString() ));
        list.add(Compat.literal("limit: " + this.limit ));
        list.add(Compat.literal("orientation: "+orientations[tag.getInt("orientation")].toString()));
        int a=tag.getInt("axis");
        if(a<axes.length)
            list.add(Compat.literal("axis: "+axes[a].toString()));
        else
            list.add(Compat.literal("axis: none"));
        list.add(Compat.literal("plane: "+ Plane.values()[tag.getInt("plane")].toString()));
        list.add(Compat.literal("fill circle: "+ tag.getBoolean("cfill")));
        list.add(Compat.literal("rotation: "+ tag.getInt("rotation")));
    }
    public int getEnchantmentValue() {

        return this.getTier().getEnchantmentValue();

    }
}
