package net.nicguzzo.wands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
public class WandItem extends Item{
    
    public enum Orientation {
        HORIZONTAL, VERTICAL
    }
    public enum Plane {
        XZ,XY,YZ
    }
    
    public int limit = 0;
    public boolean removes_water;
    public boolean removes_lava;
    static private final int max_mode=5;
    
    public WandItem(int limit,boolean removes_water,boolean removes_lava,Properties properties) {
        super(properties);
        this.limit=limit;
        this.removes_lava=removes_lava;
        this.removes_water=removes_water;
    }
    static public int getMode(ItemStack stack) {
        if(stack!=null && !stack.isEmpty())
            return stack.getOrCreateTag().getInt("mode");
        return -1;
    }   
    
    static public void nextMode(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int mode=(tag.getInt("mode")+1) % (max_mode+1);        
            tag.putInt("mode", mode);
        }
    }
    
    static public boolean isInverted(ItemStack stack) {
        if(stack!=null && !stack.isEmpty())
            return stack.getOrCreateTag().getBoolean("inverted");
        return false;
    }
    static public void invert(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            boolean inverted=tag.getBoolean("inverted");
            tag.putBoolean("inverted", !inverted);
        }
    }
    static public Orientation getOrientation(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            int o=stack.getOrCreateTag().getInt("orientation");
            return Orientation.values()[o];
        }
        return Orientation.HORIZONTAL;
    }
    static public void nextOrientation(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int o=(tag.getInt("orientation")+1) %2;
            tag.putInt("orientation", o);
        }
    }
    static public Plane getPlane(ItemStack stack) {
        Plane plane=Plane.XZ;
        if(stack!=null && !stack.isEmpty()){
            int p=stack.getOrCreateTag().getInt("plane");
            if(p>=0 && p< Plane.values().length)
                plane=Plane.values()[p];
        }
        return plane;
    }
    static public void nextPlane(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int plane=(tag.getInt("plane")+1) % 3;
            tag.putInt("plane", plane);
        }
    }
    static public void toggleCircleFill(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            boolean cfill=tag.getBoolean("cfill");
            tag.putBoolean("cfill", !cfill);
        }
    }
    //TODO: send feedback to player
    static public boolean isCircleFill(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            return stack.getOrCreateTag().getBoolean("cfill");
        }
        return false;
    }
 
    @Override
    public InteractionResult useOn(UseOnContext context) {    
        WandsMod.LOGGER.info("UseOn");
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
        ItemStack stack = context.getPlayer().getMainHandItem();//check anyway...
        if (stack!=null && !stack.isEmpty() && stack.getItem() instanceof WandItem) {
            Vec3 hit = context.getClickLocation();
            BlockPos pos = context.getClickedPos();
            Direction side = context.getClickedFace();
            BlockState block_state = world.getBlockState(pos);
            int mode = WandItem.getMode(stack);
            if(mode==2||mode==4||mode==5){
                if(wand.p1==null){
                    //clear();
                    wand.p1_state=block_state;
                    wand.p2=false;
                    wand.p1=pos;
                    wand.x1=pos.getX();
                    wand.y1=pos.getY();
                    wand.z1=pos.getZ();
                    WandsMod.log("pos1 "+pos,true);
                    return InteractionResult.SUCCESS;
                }else{
                    block_state=wand.p1_state;
                    wand.p2=true;
                }
            }
            wand.do_or_preview(context.getPlayer(),world, block_state, pos, side, hit,stack,true);
            
        }
        
        return InteractionResult.SUCCESS;
    }
    @Override
    public  InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {   
        WandsMod.LOGGER.info("use");     
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
        }
        wand.clear();
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }

   
}
