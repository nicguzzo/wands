package net.nicguzzo.wands;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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

    
    public WandItem(Tier tier, int limit, boolean removes_water, boolean removes_lava, boolean unbreakable, Properties properties) {
        super(tier,properties);
        this.limit=limit;
        this.removes_lava=removes_lava;
        this.removes_water=removes_water;
        this.unbreakable=unbreakable;    
    }
    static public boolean is_wand(ItemStack stack) {
        return stack.getItem() instanceof WandItem;
    }
    static public Mode getMode(ItemStack stack) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()) {
            int m=stack.getOrCreateTag().getInt("mode");
            if(m<modes.length)
                return modes[m];
        }
        return Mode.DIRECTION;
    }
    static public void setMode(ItemStack stack,Mode mode) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()) {
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("mode", mode.ordinal());
        }
    }
    static public void nextMode(ItemStack stack) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int mode=(tag.getInt("mode")+1) % (modes.length);
            tag.putInt("mode", mode);
        }
    }
    static public void prevMode(ItemStack stack) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int mode=tag.getInt("mode")-1;
            if(mode<0){
                mode=modes.length-1;
            }
            tag.putInt("mode", mode);
        }
    }
    static public boolean isInverted(ItemStack stack) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty())
            return stack.getOrCreateTag().getBoolean("inverted");
        return false;
    }
    static public void invert(ItemStack stack) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            boolean inverted=tag.getBoolean("inverted");
            tag.putBoolean("inverted", !inverted);
        }
    }
    static public void setInvert(ItemStack stack,boolean i) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putBoolean("inverted", i);
        }
    }
    static public Orientation getOrientation(ItemStack stack) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            int o=stack.getOrCreateTag().getInt("orientation");
            return orientations[o];
        }
        return Orientation.ROW;
    }
    static public void setOrientation(ItemStack stack,Orientation o) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("orientation", o.ordinal());
        }
    }
    static public void nextOrientation(ItemStack stack) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int o=(tag.getInt("orientation")+1) %2;
            tag.putInt("orientation", o);
        }
    }
    static public Plane getPlane(ItemStack stack) {
        Plane plane=Plane.XZ;
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            int p=stack.getOrCreateTag().getInt("plane");
            if(p>=0 && p<planes.length)
                plane=planes[p];
        }
        return plane;
    }
    static public void setPlane(ItemStack stack,Plane p) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("plane", p.ordinal());
        }
    }
    static public void nextPlane(ItemStack stack) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int plane=(tag.getInt("plane")+1) % 3;
            tag.putInt("plane", plane);
        }
    }
    static public void toggleCircleFill(ItemStack stack) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            boolean cfill=tag.getBoolean("cfill");
            tag.putBoolean("cfill", !cfill);
        }
    }
    static public void setFill(ItemStack stack,boolean cfill) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putBoolean("cfill",cfill);
        }
    }
    static public boolean isCircleFill(ItemStack stack) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            return stack.getOrCreateTag().getBoolean("cfill");
        }
        return false;
    }

    static public Rotation getRotation(ItemStack stack) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty())
            return rotations[stack.getOrCreateTag().getInt("rotation")];
        return Rotation.NONE;
    }
    static public void nextRotation(ItemStack stack) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int rot=(tag.getInt("rotation")+1) % rotations.length;
            tag.putInt("rotation", rot);
        }
    }
    static public void setRotation(ItemStack stack,Rotation rot) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("rotation", rot.ordinal());
        }
    }
    static public Action getAction(ItemStack stack) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()) {
            int m=stack.getOrCreateTag().getInt("action");
            if(m<actions.length)
                return actions[m];
        }
        return Action.PLACE;
    }
    static public void setAction(ItemStack stack,Action a) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("action", a.ordinal());
        }
    }
    static public void nextAction(ItemStack stack) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int a=(tag.getInt("action")+1) % (actions.length);
            tag.putInt("action", a);
        }
    }
    static public void prevAction(ItemStack stack) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
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
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            int p=stack.getOrCreateTag().getInt("axis");
            if(p>=0 && p< axes.length)
                axis=axes[p];
        }
        return axis;
    }
    static public void setAxis(ItemStack stack,Direction.Axis axis) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
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
        if(stack!=null && is_wand(stack) && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int axis=(tag.getInt("axis")+1) % 3;
            tag.putInt("axis", axis);
        }
    }
    static public StateMode getStateMode(ItemStack stack) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()) {
            int m=stack.getOrCreateTag().getInt("state_mode");
            if(m<state_modes.length)
                return state_modes[m];
        }
        return StateMode.CLONE;
    }
    static public void setStateMode(ItemStack stack,StateMode mode) {
        if(stack!=null && is_wand(stack) && !stack.isEmpty()) {
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("state_mode", mode.ordinal());
        }
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
        if (stack!=null && !stack.isEmpty() && stack.getItem() instanceof WandItem) {
            Vec3 hit = context.getClickLocation();
            BlockPos pos = context.getClickedPos();
            Direction side = context.getClickedFace();
            BlockState block_state = world.getBlockState(pos);
            Mode mode = WandItem.getMode(stack);
            //WandsMod.log("mode "+mode,true);

            if(mode==Mode.FILL||mode==Mode.LINE||mode==Mode.CIRCLE||mode==Mode.COPY||mode==Mode.RECT){
                if(wand.is_alt_pressed){
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
        }
        wand.clear();
        /*if(!world.isClientSide()) {
            ItemStack stack = player.getMainHandItem();//check anyway...
            if (stack != null && !stack.isEmpty() && stack.getItem() instanceof WandItem) {
                player.displayClientMessage(Component.literal("Wand mode: " + getModeString(stack)), false);
            }
        }*/
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }
    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        CompoundTag tag=stack.getOrCreateTag();

        list.add(Component.literal("mode: " + WandItem.getMode(stack).toString() ));
        list.add(Component.literal("limit: " + this.limit ));
        list.add(Component.literal("orientation: "+orientations[tag.getInt("orientation")].toString()));
        int a=tag.getInt("axis");
        if(a<axes.length)
            list.add(Component.literal("axis: "+axes[a].toString()));
        else
            list.add(Component.literal("axis: none"));
        list.add(Component.literal("plane: "+ Plane.values()[tag.getInt("plane")].toString()));
        list.add(Component.literal("fill circle: "+ tag.getBoolean("cfill")));
        list.add(Component.literal("rotation: "+ tag.getInt("rotation")));
    }
    public int getEnchantmentValue() {

        return this.getTier().getEnchantmentValue();

    }
}
