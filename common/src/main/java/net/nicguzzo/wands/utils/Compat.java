package net.nicguzzo.wands.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.registry.*;
import me.shedaniel.architectury.registry.KeyBindings;
import me.shedaniel.architectury.registry.menu.ExtendedMenuProvider;

import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.menues.MagicBagMenu;
import net.nicguzzo.wands.menues.PaletteMenu;
import net.nicguzzo.wands.menues.WandMenu;

import java.util.function.Supplier;

public class Compat {

    static public boolean has_mending(ItemStack item, Level l) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, item) > 0;
    }

    static public int get_fortune_level(ItemStack item, Level l) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, item);
    }

    static public boolean has_silktouch(ItemStack item, Level l) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, item) != 0;
    }

    static public ResourceLocation create_resource(String res) {
        return new ResourceLocation(WandsMod.MOD_ID, res);
    }

    static public ResourceLocation create_resource_mc(String res) {
        return new ResourceLocation(res);
    }

    static public void addVertex(BufferBuilder bufferBuilder, float x, float y, float z, Colorf c, float u, float v, float nx, float ny, float nz) {
        bufferBuilder.vertex(x, y, z, c.r, c.g, c.b, c.a, u, v, 0, 0, nx, ny, nz);
    }

    static public void addVertex_pos_uv(BufferBuilder bufferBuilder, float x, float y, float z, float u, float v) {
        bufferBuilder.vertex(x, y, z).uv(u, v).endVertex();
    }

    static public void addVertex_pos_color(BufferBuilder bufferBuilder, float x, float y, float z, Colorf c) {
        bufferBuilder.vertex(x, y, z).color(c.r, c.g, c.b, c.a).endVertex();
    }

    static public CreativeModeTab create_tab(ResourceLocation res) {
        return CreativeTabs.create(res, new Supplier<ItemStack>() {
            @Override
            public ItemStack get() {
                return new ItemStack(WandsMod.DIAMOND_WAND_ITEM.get());
            }
        });
    }

    static public boolean is_creative(Player player) {
        return player.abilities.instabuild;
    }

    static public Inventory get_inventory(Player player) {
        return player.inventory;
    }

    static public void set_color(float r, float g, float b, float a) {
        RenderSystem.color4f(r, g, b, a);
    }

    static public void set_pos_tex_shader() {
    }

    static public void set_texture(ResourceLocation tex) {
        Minecraft.getInstance().getTextureManager().bind(tex);
    }

    static public void set_shader_block() {
    }

    static public void set_shader_pos_col_tex_light() {
    }

    static public void tesselator_end(Tesselator tesselator, BufferBuilder bufferBuilder) {
        tesselator.end();
    }

    static public void set_shader_pos_tex() {
    }

    static public void set_shader_pos_color() {
    }

    static public void set_shader_lines() {
    }

    static public void set_render_quads_block(BufferBuilder bufferBuilder) {
        bufferBuilder.begin(7, DefaultVertexFormat.BLOCK);

    }

    static public void set_render_quads_pos_tex(BufferBuilder bufferBuilder) {
        bufferBuilder.begin(7, DefaultVertexFormat.BLOCK);
    }

    static public void set_render_lines(BufferBuilder bufferBuilder) {
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        RenderSystem.shadeModel(7425);
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        bufferBuilder.begin(1, DefaultVertexFormat.POSITION_COLOR);
    }

    static public void set_render_quads_pos_col(BufferBuilder bufferBuilder) {
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
    }

    static public void pre_render(PoseStack poseStack) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 c = camera.getPosition();
        if (WandsMod.config.render_last) {
            poseStack.pushPose();
            poseStack.translate(-c.x, -c.y, -c.z); // translate
            GlStateManager._pushMatrix();
            RenderSystem.multMatrix(poseStack.last().pose());
        } else {
            RenderSystem.translated(-c.x, -c.y, -c.z);
        }

    }

    static public void post_render(PoseStack poseStack) {
        if (WandsMod.config.render_last) {
            GlStateManager._popMatrix();
            poseStack.popPose();
        }
    }

    static public void open_menu(ServerPlayer player, ItemStack item, int m) {

        MenuRegistry.openExtendedMenu(player, new ExtendedMenuProvider() {
                    @Override
                    public void saveExtraData(FriendlyByteBuf packetByteBuf) {
                        packetByteBuf.writeItem(item);
                    }

                    @Override
                    public Component getDisplayName() {
                        return new TranslatableComponent(item.getItem().getDescriptionId());
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                        AbstractContainerMenu menu = null;
                        switch (m) {
                            case 0:
                                menu = new WandMenu(syncId, inv, item);
                                break;
                            case 1:
                                menu = new PaletteMenu(syncId, inv, item);
                                break;
                            case 2:
                                menu = new MagicBagMenu(syncId, inv, item);
                                break;
                        }
                        return menu;
                    }
                }
        );
    }

    static public void set_carried(Player player, AbstractContainerMenu menu, ItemStack itemStack) {
        player.inventory.setCarried(itemStack);
    }

    static public ItemStack get_carried(Player player, AbstractContainerMenu menu) {
        return player.inventory.getCarried();
    }

    static public void set_identity(PoseStack m) {
        m.last().pose().setIdentity();
    }

    static public MutableComponent translatable(String key) {
        return new TranslatableComponent(key);
    }

    static public MutableComponent translatable_item_name(ItemStack item) {
        return new TranslatableComponent(item.getDescriptionId());
    }

    static public MutableComponent literal(String msg) {
        return new TextComponent(msg);
    }

    static public boolean shouldRenderFace(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction, BlockPos blockPos2) {
        return Block.shouldRenderFace(blockState, blockGetter, blockPos, direction);
    }

    static public void register_key(KeyMapping k) {
        KeyBindings.registerKeyBinding(k);
    }

    static public void render_info() {
        GuiEvent.RENDER_HUD.register((pose, delta) -> {
            WandsModClient.render_wand_info(pose);
        });
    }

    static public void enableTexture() {
        RenderSystem.enableTexture();
    }

    static public void disableTexture() {
        RenderSystem.disableTexture();
    }

    static public BlockPos get_player_pos(Player player) {
        return player.blockPosition();
    }

    static public Vec3 get_player_pos_center(Player player) {
        return player.position();
    }

    static public boolean is_same(ItemStack i1, ItemStack i2) {
        return i1.sameItem(i2);
    }

    static public Level player_level(Player player) {
        return player.level;
    }

    static public CompoundTag getTags(ItemStack stack) {
        return stack.getOrCreateTag();
    }

    static public void toast(Toast toast) {
        Minecraft.getInstance().getToasts().addToast(toast);
    }

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

        private NbtType() {
        }
    }
}
