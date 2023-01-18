package net.nicguzzo.wands.wand;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.items.WandItem;
import net.nicguzzo.wands.utils.WandUtils;
import net.nicguzzo.wands.wand.modes.*;

public class WandProps {
    public enum Mode{
        DIRECTION { public String toString() {return "wands.modes.direction";} public WandMode get_mode(){return new DirectionMode();}},
        ROW_COL   { public String toString() {return "wands.modes.row_col";}   public WandMode get_mode(){return new RowColMode();}},
        FILL      { public String toString() {return "wands.modes.fill";}      public WandMode get_mode(){return new FillMode();}},
        AREA      { public String toString() {return "wands.modes.area";}      public WandMode get_mode(){return new AreaMode();}},
        GRID      { public String toString() {return "wands.modes.grid";}      public WandMode get_mode(){return new GridMode();}},
        LINE      { public String toString() {return "wands.modes.line";}      public WandMode get_mode(){return new LineMode();}},
        CIRCLE    { public String toString() {return "wands.modes.circle";} public WandMode get_mode(){return new CircleMode();}},
        COPY      { public String toString() {return "wands.modes.copy";}   public WandMode get_mode(){return new CopyMode();}},
        PASTE     { public String toString() {return "wands.modes.paste";}  public WandMode get_mode(){return new PasteMode();}},
        VEIN      { public String toString() {return "wands.modes.vein";}   public WandMode get_mode(){return new VeinMode();}},
        BLAST     { public String toString() {return "wands.modes.blast";}  public WandMode get_mode(){return new BlastMode();}},
        TUNNEL    { public String toString() {return "wands.modes.tunnel";} public WandMode get_mode(){return new TunnelMode();}};
        public abstract WandMode get_mode();
    }
    public enum Orientation {
        ROW { public String toString() {return "wands.orientation.row";}},
        COL { public String toString() {return "wands.orientation.col";}};
    }
    public enum Plane {
        XZ,XY,YZ
    }
    public enum MirrorAxis {
        NONE,X,Y,Z
    }
    public enum StateMode {
        CLONE  { public String toString() { return "wands.state_mode.clone";}},
        APPLY  { public String toString() { return "wands.state_mode.apply";}},
        TARGET { public String toString() { return "wands.state_mode.target";}}
    }
    public enum Action{
        PLACE  { public String toString() { return "wands.action.place";   }},
        REPLACE{ public String toString() { return "wands.action.replace"; }},
        DESTROY{ public String toString() { return "wands.action.destroy"; }},
        USE    { public String toString() { return "wands.action.use";     }}
    }
    public enum Flag{
        INVERTED    { public String toString() {return "inverted";}      public boolean get_default(){return false;}; },
        CFILLED     { public String toString() {return "cfill";}         public boolean get_default(){return false;}; },
        EVEN        { public String toString() {return "circle_even";}   public boolean get_default(){return false;}; },
        DIAGSPREAD  { public String toString() {return "diag_spread";}   public boolean get_default(){return false;}; },
        MATCHSTATE  { public String toString() {return "match_state";}   public boolean get_default(){return false;}; },
        INCSELBLOCK { public String toString() {return "inc_sel_block";} public boolean get_default(){return false;}; },
        STAIRSLAB   { public String toString() {return "stair_slab";}    public boolean get_default(){return false;}; },
        RFILLED     { public String toString() {return "rfill";}         public boolean get_default(){return  true;}; };
        public abstract boolean get_default();
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
        SKIPBLOCK  { public String toString() {return  "skip_block" ;}},
        TUNNEL_W      { public String toString() {return  "tunnel_w" ;}},
        TUNNEL_H      { public String toString() {return  "tunnel_h" ;}},
        TUNNEL_OX     { public String toString() {return  "tunnel_ox" ;}},
        TUNNEL_OY     { public String toString() {return  "tunnel_oy" ;}},
        TUNNEL_DEPTH  { public String toString() {return  "tunnel_d" ;}};
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
            TUNNEL_W.min=1;
            TUNNEL_H.min=1;
            TUNNEL_H.def=2;
            TUNNEL_DEPTH.def=3;
            TUNNEL_DEPTH.min=1;
        }
    }
    static public Mode[] modes=Mode.values();
    static public Action[] actions=Action.values();
    static public Orientation[] orientations=Orientation.values();
    static public Plane[] planes=Plane.values();
    static public Direction.Axis[] axes=Direction.Axis.values();
    static public Rotation[] rotations=Rotation.values();
    static public StateMode[] state_modes=StateMode.values();
    static public MirrorAxis[] mirrorAxes=MirrorAxis.values();


    static public boolean getFlag(ItemStack stack, Flag flag) {
        if(WandUtils.is_wand(stack)) {
            CompoundTag tag=stack.getOrCreateTag();
            if(tag.contains(flag.toString())) {
                return tag.getBoolean(flag.toString());
            }else{
                tag.putBoolean(flag.toString(),flag.get_default());
                return flag.get_default();
            }
        }
        return false;
    }
    static public void setFlag(ItemStack stack,Flag flag,boolean f) {
        if(WandUtils.is_wand(stack)) {
            stack.getOrCreateTag().putBoolean(flag.toString(), f);
        }
    }
    static public void toggleFlag(ItemStack stack,Flag flag) {
        if(WandUtils.is_wand(stack)) {
            CompoundTag tag=stack.getOrCreateTag();
            boolean b=getFlag(stack,flag);
            tag.putBoolean(flag.toString(), !b);
        }
    }

    static public void setVal(ItemStack stack,Value v, int n) {
        if(WandUtils.is_wand(stack)) {
            stack.getOrCreateTag().putInt(v.toString(), n);
        }
    }
    static public void incVal(ItemStack stack,Value v, int inc,int max) {
        if(WandUtils.is_wand(stack)) {
            int n=stack.getOrCreateTag().getInt(v.toString());
            if(n+inc<=max)
                stack.getOrCreateTag().putInt(v.toString(), n+inc);
        }
    }
    static public void incVal(ItemStack stack,Value v, int inc) {
        incVal(stack,v,inc,v.max);
    }
    static public void decVal(ItemStack stack,Value v, int inc,int min) {
        if(WandUtils.is_wand(stack)) {
            int n=stack.getOrCreateTag().getInt(v.toString());
            if(n-inc>=min)
                stack.getOrCreateTag().putInt(v.toString(), n-inc);
        }
    }
    static public void decVal(ItemStack stack,Value v, int dec) {
        decVal(stack,v,dec,v.min);
    }
    static public int getVal(ItemStack stack,Value v) {
        if(WandUtils.is_wand(stack)) {
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
        if(WandUtils.is_wand(stack)) {
            int m=stack.getOrCreateTag().getInt("mode");
            if(m>=0 && m<modes.length)
                return modes[m];
        }
        return Mode.DIRECTION;
    }
    static public void setMode(ItemStack stack,Mode mode) {
        if(WandUtils.is_wand(stack)) {
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("mode", mode.ordinal());
        }
    }
    static public void nextMode(ItemStack stack) {
        if(WandUtils.is_wand(stack)){
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
        if(WandUtils.is_wand(stack)){
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
        if(WandUtils.is_wand(stack)){
            int o=stack.getOrCreateTag().getInt("orientation");
            return orientations[o];
        }
        return Orientation.ROW;
    }
    static public void setOrientation(ItemStack stack,Orientation o) {
        if(WandUtils.is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("orientation", o.ordinal());
        }
    }
    static public void nextOrientation(ItemStack stack) {
        if(WandUtils.is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            int o=(tag.getInt("orientation")+1) %2;
            tag.putInt("orientation", o);
        }
    }
    static public Plane getPlane(ItemStack stack) {
        Plane plane=Plane.XZ;
        if(WandUtils.is_wand(stack)){
            int p=stack.getOrCreateTag().getInt("plane");
            if(p>=0 && p<planes.length)
                plane=planes[p];
        }
        return plane;
    }
    static public void setPlane(ItemStack stack,Plane p) {
        if(WandUtils.is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("plane", p.ordinal());
        }
    }
    static public void nextPlane(ItemStack stack) {
        if(WandUtils.is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            int plane=(tag.getInt("plane")+1) % 3;
            tag.putInt("plane", plane);
        }
    }

    static public Rotation getRotation(ItemStack stack) {
        if(WandUtils.is_wand(stack))
            return rotations[stack.getOrCreateTag().getInt("rotation")];
        return Rotation.NONE;
    }
    static public void nextRotation(ItemStack stack) {
        if(WandUtils.is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            int rot=(tag.getInt("rotation")+1) % rotations.length;
            tag.putInt("rotation", rot);
        }
    }
    static public void setRotation(ItemStack stack,Rotation rot) {
        if(WandUtils.is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("rotation", rot.ordinal());
        }
    }
    static public Action getAction(ItemStack stack) {
        if(WandUtils.is_wand(stack)) {
            int m=stack.getOrCreateTag().getInt("action");
            if(m<actions.length)
                return actions[m];
        }
        return Action.PLACE;
    }
    static public void setAction(ItemStack stack,Action a) {
        if(WandUtils.is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("action", a.ordinal());
        }
    }
    static public void nextAction(ItemStack stack) {
        if(WandUtils.is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            int a=(tag.getInt("action")+1) % (actions.length);
            tag.putInt("action", a);
        }
    }
    static public void prevAction(ItemStack stack) {
        if(WandUtils.is_wand(stack)){
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
        if(WandUtils.is_wand(stack)){
            int p=stack.getOrCreateTag().getInt("axis");
            if(p>=0 && p< axes.length)
                axis=axes[p];
        }
        return axis;
    }
    static public void setAxis(ItemStack stack,Direction.Axis axis) {
        if(WandUtils.is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("axis", axis.ordinal());
        }
    }
    static public void setAxis(ItemStack stack,int a) {
        if (stack != null && WandUtils.is_wand(stack) && !stack.isEmpty()) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt("axis", a);
        }
    }
    static public void nextAxis(ItemStack stack) {
        if(WandUtils.is_wand(stack)){
            CompoundTag tag=stack.getOrCreateTag();
            int axis=(tag.getInt("axis")+1) % 3;
            tag.putInt("axis", axis);
        }
    }
    static public StateMode getStateMode(ItemStack stack) {
        if(WandUtils.is_wand(stack)) {
            int m=stack.getOrCreateTag().getInt("state_mode");
            if(m<state_modes.length)
                return state_modes[m];
        }
        return StateMode.TARGET;
    }
    static public void setStateMode(ItemStack stack,StateMode mode) {
        if(WandUtils.is_wand(stack)) {
            CompoundTag tag=stack.getOrCreateTag();
            tag.putInt("state_mode", mode.ordinal());
        }
    }
    static public void incGrid(ItemStack stack,Value v, int n) {
        if(WandUtils.is_wand(stack)) {
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
}
