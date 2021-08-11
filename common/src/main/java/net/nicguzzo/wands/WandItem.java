package net.nicguzzo.wands;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WandItem extends Item{
    
    public enum Orientation {
        ROW, COL
    }
    public enum Plane {
        XZ,XY,YZ
    }
    //TODO: state placement mode
    public enum BState {
        CLONE,DEFAULT
    }
    
    public int limit = 0;
    public boolean removes_water;
    public boolean removes_lava;
    static private final int max_mode=7;
    
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

    static public String getModeString(ItemStack stack) {

        if(stack!=null && !stack.isEmpty()) {
            int mode = stack.getOrCreateTag().getInt("mode");
            switch (mode){
                case 0: return "Direction";
                case 1: return "Row/Col";
                case 2: return "Fill";
                case 3: return "Area";
                case 4: return "Line";
                case 5: return "Circle";
                case 6: return "Copy";
                case 7: return "Paste";
            }
        }
        return "";
    }
    
    static public void nextMode(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int mode=(tag.getInt("mode")+1) % (max_mode+1);        
            tag.putInt("mode", mode);
        }
    }
    static public void prevMode(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int mode=tag.getInt("mode")-1;
            if(mode<0){
                mode=max_mode;
            }
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
        return Orientation.ROW;
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
    static public boolean isCircleFill(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            return stack.getOrCreateTag().getBoolean("cfill");
        }
        return false;
    }

    static public int getRotation(ItemStack stack) {
        if(stack!=null && !stack.isEmpty())
            return stack.getOrCreateTag().getInt("rotation");
        return 0;
    }
    static public void nextRotation(ItemStack stack) {
        if(stack!=null && !stack.isEmpty()){
            CompoundTag tag=stack.getOrCreateTag();
            int rot=(tag.getInt("rotation")+1) % Rotation.values().length;
            tag.putInt("rotation", rot);
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
            int mode = WandItem.getMode(stack);
            //WandsMod.log("mode "+mode,true);

            if(mode==2||mode==4||mode==5||mode==6){
                if(wand.is_alt_pressed){
                    pos=pos.relative(side,1);
                }
                if(mode==6) {
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
            if(mode==6 && wand.copy_pos1!=null && wand.copy_pos2!=null){
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
                player.displayClientMessage(new TextComponent("Wand mode: " + getModeString(stack)), false);
            }
        }*/
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }
    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        CompoundTag tag=stack.getOrCreateTag();
        list.add(new TextComponent("mode: " + getModeString(stack)));
        list.add(new TextComponent("orientation: "+Orientation.values()[tag.getInt("orientation")].toString()));
        list.add(new TextComponent("plane: "+ Plane.values()[tag.getInt("plane")].toString()));
        list.add(new TextComponent("fill circle: "+ tag.getBoolean("cfill")));
        list.add(new TextComponent("rotation: "+ tag.getInt("rotation")));
    }
   
}
