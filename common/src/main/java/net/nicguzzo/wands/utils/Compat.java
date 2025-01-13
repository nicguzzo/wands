package net.nicguzzo.wands.utils;

#if MC >= "1205"
#if MC>="1210"
import com.mojang.blaze3d.vertex.BufferUploader;
#if MC>="1212"
import net.minecraft.client.renderer.CoreShaders;
#endif
#endif
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.components.toasts.Toast;

import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
#endif
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
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
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
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
#if MC >= "1200"
#endif
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.menues.MagicBagMenu;
import net.nicguzzo.wands.menues.PaletteMenu;
import net.nicguzzo.wands.menues.WandMenu;
import org.jetbrains.annotations.NotNull;

#if MC >= "1205"
import org.joml.Matrix4fStack;

import java.util.NoSuchElementException;

#endif
public class Compat {

    static public boolean has_mending(ItemStack item,Level l){
    
        #if MC>="1210"
            RegistryAccess ra=l.registryAccess();
            int level=0;
            try {
                level = ra.lookup(Registries.ENCHANTMENT).map(e -> e.get(Enchantments.MENDING)).orElseThrow().map(e -> EnchantmentHelper.getItemEnchantmentLevel(e, item)).orElseThrow();
            }catch( NoSuchElementException ignored){

            }
            return level>0;
         #else
            return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, tool)>0;
         #endif
     }

    static public int get_fortune_level(ItemStack item,Level l){
         #if MC>="1210"
            RegistryAccess ra=l.registryAccess();
            int level=0;
            try {
                level = ra.lookup(Registries.ENCHANTMENT).map(e -> e.get(Enchantments.FORTUNE)).orElseThrow().map(e -> EnchantmentHelper.getItemEnchantmentLevel(e, item)).orElseThrow();
            }catch( NoSuchElementException ignored){

            }
            return level;
         #else
            #if MC>="1200"
                return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FORTUNE,item);
            #else
                return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE,item);
            #endif
         #endif
    }
    static public boolean has_silktouch(ItemStack item,Level l){
         #if MC>="1210"
            RegistryAccess ra=l.registryAccess();
            boolean st=false;
            try {
                st = 0 != ra.lookup(Registries.ENCHANTMENT).map(e -> e.get(Enchantments.SILK_TOUCH)).orElseThrow().map(e -> EnchantmentHelper.getItemEnchantmentLevel(e, item)).orElseThrow();
            }catch( NoSuchElementException ignored){

            }
            return st;
         #else
            return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH,item)!=0;
         #endif
    }

    static public ResourceLocation create_resource(String res){
        #if MC < "1210"
        return new ResourceLocation(WandsMod.MOD_ID, res);
        #else
        return ResourceLocation.fromNamespaceAndPath(WandsMod.MOD_ID,res);
        #endif
    }

    static public ResourceLocation create_resource_mc(String res){
        #if MC < "1210"
        return new ResourceLocation(res);
        #else
        return ResourceLocation.withDefaultNamespace(res);
        #endif
    }

    static public void addVertex(BufferBuilder bufferBuilder,float x,float y,float z,Colorf c,float u,float v,float nx,float ny,float nz){
        #if MC<"1210"
        bufferBuilder.vertex(x, y, z, c.r,c.g,c.b,c.a, u, v, 0, 0, nx,ny,nz);
        #else
        bufferBuilder.addVertex(x,y,z,c.toInt(), u, v, 0, 0, nx, ny,nz);
        #endif
    }
    static public void addVertex_pos_uv(BufferBuilder bufferBuilder,float x,float y,float z,float u,float v){
        #if MC<"1210"
        bufferBuilder.vertex(x, y, z).uv(u, v).endVertex();
        #else
        bufferBuilder.addVertex(x,y,z).setUv(u,v);
        #endif
    }
    static public void addVertex_pos_color(BufferBuilder bufferBuilder,float x,float y,float z,Colorf c){
        #if MC<"1210"
        bufferBuilder.vertex(x, y, z).color(c.r,c.g,c.b,c.a).endVertex();
        #else
        bufferBuilder.addVertex(x,y,z).setColor(c.r,c.g,c.b,c.a);
        #endif
    }
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
            #if MC < "1212"
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
            #else
                RenderSystem.setShader(CoreShaders.POSITION_TEX);
            #endif
        #endif
    }
    static public void set_texture(ResourceLocation tex){
        #if MC=="1165"
            Minecraft.getInstance().getTextureManager().bind(tex);
        #else
            RenderSystem.setShaderTexture(0, tex);
        #endif
    }
    static public void set_shader_block(){
        #if MC<"1200"
            #if MC>"1165"
                RenderSystem.setShader(GameRenderer::getBlockShader);
            #endif
        #else
            #if MC < "1212"
                RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
            #else
                RenderSystem.setShader(CoreShaders.RENDERTYPE_CUTOUT);
            #endif
        #endif
    }
    static public void set_shader_pos_col_tex_light(){
        #if MC<"1200"
            #if MC>"1165"
                RenderSystem.setShader(GameRenderer::getBlockShader);
            #endif
        #else
            #if MC < "1212"
                RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
            #else
                RenderSystem.setShader(CoreShaders.POSITION_COLOR_TEX_LIGHTMAP);
            #endif
        #endif
    }
    static public void tesselator_end(Tesselator tesselator,BufferBuilder bufferBuilder){
        #if MC<"1210"
            tesselator.end();
        #else
        try {
            BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        } catch (Exception e) {
            //WandsMod.LOGGER.error("tesselator_end exception "+e.getMessage());
        }
        #endif
    }
    static public void set_shader_pos_tex(){
        #if MC<"1200"
            #if MC>"1165"
                RenderSystem.setShader(GameRenderer::getBlockShader);
            #endif
        #else
            #if MC < "1212"
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
            #else
                RenderSystem.setShader(CoreShaders.POSITION_TEX);
            #endif
        #endif
    }
    static public void set_shader_pos_color(){
        #if MC<"1200"
            #if MC>"1165"
                RenderSystem.setShader(GameRenderer::getBlockShader);
            #endif
        #else
            #if MC < "1212"
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
            #else
                RenderSystem.setShader(CoreShaders.POSITION_COLOR);
            #endif
        #endif
    }
    static public void set_shader_lines(){
        #if MC>="1200"
            #if MC < "1212"
                RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
            #else
                RenderSystem.setShader(CoreShaders.RENDERTYPE_LINES);
            #endif
        #endif
    }
    static public void set_render_quads_block(BufferBuilder bufferBuilder){
        #if MC<"1200"
            #if MC=="1165"
                bufferBuilder.begin(7, DefaultVertexFormat.BLOCK);
            #else
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            #endif
        #else
            #if MC<"1210"
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            #endif
        #endif
    }
    static public void set_render_quads_pos_tex(BufferBuilder bufferBuilder){
        #if MC<"1200"
            #if MC=="1165"
                bufferBuilder.begin(7, DefaultVertexFormat.BLOCK);
            #else
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            #endif
        #else
            #if MC<"1210"
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            #endif
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
            #if MC<"1210"
            bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
            #endif
        #endif
    }
    static public void set_render_quads_pos_col(BufferBuilder bufferBuilder){
        #if MC=="1165"
            bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
        #else
        #if MC<"1210"
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        #endif
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
            #if MC < "1205"
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
            #else
                Matrix4fStack poseMatrix = RenderSystem.getModelViewStack();
                poseMatrix.pushMatrix();
                poseMatrix.mul(poseStack.last().pose());
                poseMatrix.translate((float)-c.x,(float)-c.y,(float)-c.z);

            #endif
            #if MC < "1212"
            RenderSystem.applyModelViewMatrix();
            #else
                //TODO: ???
            #endif
        #endif
    }
    static public void post_render(PoseStack poseStack){
        #if MC=="1165"
            if(WandsMod.config.render_last) {
    			GlStateManager._popMatrix();
    			poseStack.popPose();
    		}
        #else
            #if MC < "1206"
            PoseStack poseStack2 = RenderSystem.getModelViewStack();
            poseStack2.popPose();
            #else
            Matrix4fStack poseMatrix = RenderSystem.getModelViewStack();
            poseMatrix.popMatrix();
            #endif
            #if MC < "1212"
            RenderSystem.applyModelViewMatrix();
            #else
                //TODO: ???
            #endif
        #endif
    }
    /*
    static public void send_to_player(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buf){
        #if MC=="1165"
            NetworkManager.sendToPlayer(player, id, buf);
        #else
            //TODO: fix 1.21
            //NetworkManager.sendToPlayer(player, id, buf);
        #endif
    }
    static public void send_to_server(ResourceLocation id, FriendlyByteBuf packet) {
        //TODO: fix 1.21
        //NetworkManager.sendToServer(id, packet);
    }*/
    #if MC < "1205"
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
		}
        );
    }
    #else
    static public void open_menu(ServerPlayer player, ItemStack item, int m){

        MenuRegistry.openExtendedMenu(player, new MenuProvider(){
			@Override
			public @NotNull Component getDisplayName(){
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
                    case 0: {
                        menu = new WandMenu(syncId, inv, item);
                    }
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
		},
          buf -> {
            Level level = player.level();
            HolderLookup.Provider provider = level.registryAccess();
            Tag tag = item.save(provider);
            buf.writeNbt(tag);
          }
        );
    }
    #endif



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
    static public MutableComponent translatable_item_name(ItemStack item){
        #if MC=="1165"

            return new TranslatableComponent(item.getDescriptionId());
        #else
            #if MC>="1190"
                #if MC>="1212"
                return Component.translatable(item.getItemName().toString());
                #else
                return Component.translatable(item.getDescriptionId());
                #endif
            #else
                return new TranslatableComponent(item.getDescriptionId());
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
            #if MC < "1212"
            return Block.shouldRenderFace(blockState,blockGetter, blockPos, direction, blockPos2);
            #else
                //TODO: ???
                //return Block.shouldRenderFace(blockState,blockState,direction);
                return true;
            #endif

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
            ClientGuiEvent.RENDER_HUD.register((e,d)->{ WandsModClient.render_wand_info(e);});
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
    static public CompoundTag getTags(ItemStack stack){
        #if MC < "1205"
            return stack.getOrCreateTag();
        #else
            var data = stack.get(DataComponents.CUSTOM_DATA);
            if(data!=null){
                return data.copyTag();
            }else{
                return new CompoundTag();
            }
        #endif
    }

    static public void toast(Toast toast){
        #if MC<"1212"
            Minecraft.getInstance().getToasts().addToast(toast);
        #else
            Minecraft.getInstance().getToastManager().addToast(toast);
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
