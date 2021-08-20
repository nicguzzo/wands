/*//beginMC1_17_1
package net.nicguzzo.wands.mcver.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.registry.CreativeTabs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.mcver.MCVer;

import java.util.function.Supplier;

public class MCVer1_17_1 extends MCVer {
    @Override
    public CreativeModeTab create_tab(ResourceLocation res){
        return CreativeTabs.create(res, new Supplier<ItemStack>() {
            @Override
            public ItemStack get() {
                return new ItemStack(WandsMod.DIAMOND_WAND_ITEM.get());
            }
        });
    }
    @Override
    public boolean is_creative(Player player) {
        return player.getAbillities().instabuild;
    }
    @Override
    public void set_color(float r, float g, float b, float a){
        RenderSystem.setShaderColor(r,g,b,a);
    }
    @Override
    public void set_texture(ResourceLocation tex){
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.enableTexture();
    }
    @Override
    public void set_render_quads(BufferBuilder bufferBuilder){
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
    }
    @Override
    public void set_render_lines(BufferBuilder bufferBuilder) {
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
    }

    @Override
    public void pre_render(PoseStack poseStack) {

    }

    @Override
    public void post_render() {

    }
}
//endMC1_17_1*/