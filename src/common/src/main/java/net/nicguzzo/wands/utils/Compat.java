package net.nicguzzo.wands.utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
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
#if MC=="1165"
    import com.mojang.blaze3d.platform.GlStateManager;
    import me.shedaniel.architectury.event.events.GuiEvent;
    import me.shedaniel.architectury.networking.NetworkManager;
    import me.shedaniel.architectury.registry.*;
    import me.shedaniel.architectury.registry.KeyBindings;
    import me.shedaniel.architectury.registry.menu.ExtendedMenuProvider;
    import net.minecraft.network.chat.TextComponent;
    import net.minecraft.network.chat.TranslatableComponent;
#else
    import com.mojang.blaze3d.vertex.VertexFormat;
    import dev.architectury.event.events.client.ClientGuiEvent;
    import dev.architectury.networking.NetworkManager;
    import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
    import dev.architectury.registry.menu.ExtendedMenuProvider;
    import dev.architectury.registry.menu.MenuRegistry;
    import net.minecraft.client.renderer.GameRenderer;
    #if MC <"1190"
        import net.minecraft.network.chat.TextComponent;
        import net.minecraft.network.chat.TranslatableComponent;
    #endif
    #if MC < "1193"
        import dev.architectury.registry.CreativeTabRegistry;
    #endif
#endif
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.menues.MagicBagMenu;
import net.nicguzzo.wands.menues.PaletteMenu;
import net.nicguzzo.wands.menues.WandMenu;
import com.google.common.base.Supplier;

public class Compat {
    static public CreativeModeTab create_tab(ResourceLocation res){

        #if MC=="1165"
        return CreativeTabs.create(res, new Supplier<ItemStack>() {
			@Override
			public ItemStack get() {
				return new ItemStack(WandsMod.DIAMOND_WAND_ITEM.get());
			}
		});
        #else
            #if MC < "1193"
                return CreativeTabRegistry.create(res, new Supplier<ItemStack>() {
                    @Override
                    public ItemStack get() {
                        return new ItemStack(WandsMod.DIAMOND_WAND_ITEM.get() );
                    }
                });
            #else
                return null;
            #endif
        #endif
    }

    static public boolean is_creative(Player player){
        #if MC=="1165"
            return player.abilities.instabuild;
        #else
            return player.getAbilities().instabuild;
        #endif
    }
    static public Inventory get_inventory(Player player){
        #if MC=="1165"
            return player.inventory;
        #else
            return player.getInventory();
        #endif
    }
    static public void set_color(float r, float g, float b, float a){
        #if MC=="1165"
            RenderSystem.color4f(r,g,b,a);
        #else
            RenderSystem.setShaderColor(r,g,b,a);
        #endif
    }
    static public void set_pos_tex_shader(){
        #if MC>"1165"
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
        #endif
    }
    static public void set_texture(ResourceLocation tex){
        #if MC=="1165"
            Minecraft.getInstance().getTextureManager().bind(tex);
        #else
            RenderSystem.setShaderTexture(0, tex);
        #endif
    }
    static public void set_render_quads_block(BufferBuilder bufferBuilder){
        #if MC<"1200"
            #if MC=="1165"
                bufferBuilder.begin(7, DefaultVertexFormat.BLOCK);
            #else
                RenderSystem.setShader(GameRenderer::getBlockShader);
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            #endif
        #else
            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        #endif
    }
    static public void set_render_quads_pos_tex(BufferBuilder bufferBuilder){
        #if MC<"1200"
            #if MC=="1165"
                bufferBuilder.begin(7, DefaultVertexFormat.BLOCK);
            #else
                RenderSystem.setShader(GameRenderer::getBlockShader);
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            #endif
        #else
            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        #endif
    }
    static public void set_render_lines(BufferBuilder bufferBuilder){
        #if MC=="1165"
            RenderSystem.disableTexture();
    		RenderSystem.disableBlend();
    		RenderSystem.shadeModel(7425);
    		RenderSystem.enableAlphaTest();
    		RenderSystem.defaultAlphaFunc();
    		bufferBuilder.begin(1, DefaultVertexFormat.POSITION_COLOR);
        #else
            RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
            RenderSystem.lineWidth(5.0f);
            bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        #endif
    }
    static public void set_render_quads_pos_col(BufferBuilder bufferBuilder){
        #if MC=="1165"
            bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
        #else
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        #endif
    }
    static public void pre_render(PoseStack poseStack){
        #if MC=="1165"
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
    		Vec3 c = camera.getPosition();
    		if(WandsMod.config.render_last) {
    			poseStack.pushPose();
    			poseStack.translate(-c.x, -c.y, -c.z); // translate
    			GlStateManager._pushMatrix();
    			RenderSystem.multMatrix(poseStack.last().pose());
    		}else{
    			RenderSystem.translated(-c.x, -c.y, -c.z);
    		}
        #else
            Minecraft client=Minecraft.getInstance();
            Camera camera = client.gameRenderer.getMainCamera();
            Vec3 c = camera.getPosition();

            PoseStack poseStack2 = RenderSystem.getModelViewStack();
            poseStack2.pushPose();
            if(WandsMod.config.render_last)
            {
                poseStack2.mulPoseMatrix(poseStack.last().pose());
            }else{
                #if MC >= "1194"
                poseStack2.mulPoseMatrix(poseStack.last().pose());
                #endif
            }
            poseStack2.translate(-c.x,-c.y,-c.z);
            RenderSystem.applyModelViewMatrix();
        #endif
    }
    static public void post_render(PoseStack poseStack){
        #if MC=="1165"
            if(WandsMod.config.render_last) {
    			GlStateManager._popMatrix();
    			poseStack.popPose();
    		}
        #else
            PoseStack poseStack2 = RenderSystem.getModelViewStack();
            poseStack2.popPose();
            RenderSystem.applyModelViewMatrix();
        #endif
    }
    static public void send_to_player(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buf){
        #if MC=="1165"
            NetworkManager.sendToPlayer(player, id, buf);
        #else
            NetworkManager.sendToPlayer(player, id, buf);
        #endif
    }
    static public void send_to_server(ResourceLocation id, FriendlyByteBuf packet) {
        NetworkManager.sendToServer(id, packet);
    }
    static public void open_menu(ServerPlayer player, ItemStack item, int m){

        MenuRegistry.openExtendedMenu(player, new ExtendedMenuProvider(){
			@Override
			public void saveExtraData(FriendlyByteBuf packetByteBuf) {
				packetByteBuf.writeItem(item);
			}
			@Override
			public Component getDisplayName(){
                #if MC=="1165"
				    return new TranslatableComponent(item.getItem().getDescriptionId());
                #else
                    return translatable(item.getItem().getDescriptionId());
                #endif
			}
			@Override
			public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                AbstractContainerMenu menu=null;
                switch(m){
                    case 0:
                        menu= new WandMenu(syncId, inv, item);
                    break;
                    case 1:
                        menu=new PaletteMenu(syncId, inv, item);
                    break;
                    case 2:
                        menu=new MagicBagMenu(syncId, inv, item);
                    break;
                }
                return menu;
			}
		});
    }

    static public void set_carried(Player player, AbstractContainerMenu menu, ItemStack itemStack){
        #if MC=="1165"
            player.inventory.setCarried(itemStack);
        #else
            menu.setCarried(itemStack);
        #endif
    }
    static public ItemStack get_carried(Player player,AbstractContainerMenu menu){
        #if MC=="1165"
            return player.inventory.getCarried();
        #else
            return menu.getCarried();
        #endif
    }
    static public void set_identity(PoseStack m){
        #if MC=="1165"
            m.last().pose().setIdentity();
        #else
            m.setIdentity();
        #endif
    }
    static public MutableComponent translatable(String key){
        #if MC=="1165"
            return new TranslatableComponent(key);
        #else
            #if MC>="1190"
                return Component.translatable(key);
            #else
                return new TranslatableComponent(key);
            #endif
        #endif
    }
    static public MutableComponent literal(String msg){
        #if MC=="1165"
            return new TextComponent(msg);
        #else
            #if MC>="1190"
                return Component.literal(msg);
            #else
                return new TextComponent(msg);
            #endif
        #endif
    }
    static public boolean shouldRenderFace(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction, BlockPos blockPos2){
        #if MC=="1165"
            return Block.shouldRenderFace(blockState,blockGetter, blockPos, direction);
        #else
            return Block.shouldRenderFace(blockState,blockGetter, blockPos, direction, blockPos2);
        #endif
    }
    static public void register_key(KeyMapping k){
        #if MC=="1165"
            KeyBindings.registerKeyBinding(k);
        #else
            KeyMappingRegistry.register(k);
        #endif
    }
    static public void render_info(){
        #if MC<"1200"
            #if MC=="1165"
                GuiEvent.RENDER_HUD.register((pose, delta)->{WandsModClient.render_wand_info(pose);});
            #else
                ClientGuiEvent.RENDER_HUD.register((pose, delta)->{ WandsModClient.render_wand_info(pose);});
            #endif
        #else
            ClientGuiEvent.RENDER_HUD.register((e,d)->{ WandsModClient.render_wand_info(e.pose());});
        #endif
    }
    static public void enableTexture() {
        #if MC <= "1193"
        RenderSystem.enableTexture();
        #endif
    }
    static public void disableTexture() {
        #if MC <= "1193"
        RenderSystem.disableTexture();
        #endif
    }
    static public BlockPos get_player_pos(Player player){
        #if MC <= "1193"
            return player.blockPosition();
        #else
            return player.getOnPos();
        #endif
    }
    static public Vec3 get_player_pos_center(Player player) {
        #if MC <= "1193"
            return player.position();
        #else
            return player.getOnPos().getCenter();
        #endif
    }

    static public boolean is_same(ItemStack i1,ItemStack i2){
        #if MC < "1200"
            return i1.sameItem(i2);
        #else
            return ItemStack.isSameItem(i2,i2);
        #endif
    }
    static public Level player_level(Player player){
        #if MC < "1200"
            return player.level;
        #else
            return player.level();
        #endif
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
        private NbtType() {}
    }
}