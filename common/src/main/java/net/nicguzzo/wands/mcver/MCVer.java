package net.nicguzzo.wands.mcver;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.nicguzzo.wands.mcver.impl.MCVer1_16_5;

public abstract class MCVer{
    //beginMC1_16_5
    public static final MCVer inst=new MCVer1_16_5();
    //endMC1_16_5
    /*//beginMC1_17_1
    public static final MCVer mcver=new MCVer1_17_1();
    //endMC1_17_1*/
	public abstract CreativeModeTab create_tab(ResourceLocation res);
    public abstract boolean is_creative(Player player);
    public abstract void set_color(float r, float g, float b, float a);
    public abstract void set_texture(ResourceLocation tex);
    public abstract void set_render_quads(BufferBuilder bufferBuilder);
    public abstract void set_render_lines(BufferBuilder bufferBuilder);
    public abstract void pre_render(PoseStack poseStack);
    public abstract void post_render(PoseStack poseStack);
}