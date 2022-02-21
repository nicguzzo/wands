package net.nicguzzo.wands.mcver;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.KeyMapping;
/*//beginMC1_16_5
import net.nicguzzo.wands.mcver.impl.MCVer1_16_5;
//endMC1_16_5*/  
//beginMC1_17_1
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.mcver.impl.MCVer1_17_1;
//endMC1_17_1  
public abstract class MCVer{
    /*//beginMC1_16_5
    public static final MCVer inst=new MCVer1_16_5();
    //endMC1_16_5*/  
    //beginMC1_17_1
    public static final MCVer inst=new MCVer1_17_1();
    //endMC1_17_1  
	public abstract CreativeModeTab create_tab(ResourceLocation res);
    public abstract boolean is_creative(Player player);
    public abstract Inventory get_inventory(Player player);
    public abstract void set_color(float r, float g, float b, float a);
    public abstract void set_pos_tex_shader();
    public abstract void set_texture(ResourceLocation tex);
    public abstract void set_render_quads_block(BufferBuilder bufferBuilder);
    public abstract void set_render_quads_pos_tex(BufferBuilder bufferBuilder);
    public abstract void set_render_lines(BufferBuilder bufferBuilder);
    public abstract void set_render_quads_pos_col(BufferBuilder bufferBuilder);
    public abstract void pre_render(PoseStack poseStack);
    public abstract void post_render(PoseStack poseStack);
    public abstract void send_to_player(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buf);
    public abstract void open_palette(ServerPlayer player, ItemStack paletteItemStack);
    public abstract void open_wand_menu(ServerPlayer player, ItemStack wandItemStack);
    public abstract void set_carried(Player player, AbstractContainerMenu menu, ItemStack itemStack);
    public abstract ItemStack get_carried(Player player,AbstractContainerMenu menu);
    public abstract void set_identity(PoseStack m);
    public abstract boolean shouldRenderFace(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction, BlockPos blockPos2);
    public abstract void register_key(KeyMapping k);
    public abstract void render_info();
    public abstract boolean is_1_16();
    public abstract boolean is_1_17();
    public abstract boolean is_1_18();
    public final class NbtType {
        public static final int END = 0;
        public static final int BYTE = 1;
        public static final int SHORT = 2;
        public static final int INT = 3;
        public static final int LONG = 4;
        public static final int FLOAT = 5;
        public static final int DOUBLE = 6;
        public static final int BYTE_ARRAY = 7;
        public static final int STRING = 8;
        public static final int LIST = 9;
        public static final int COMPOUND = 10;
        public static final int INT_ARRAY = 11;
        public static final int LONG_ARRAY = 12;
        public static final int NUMBER = 99;
        private NbtType() {}
    }
}