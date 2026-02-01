package net.nicguzzo.compat;

import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.screens.Screen;

import net.minecraft.core.*;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.MutableComponent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.menu.MenuRegistry;
#if MC_VERSION < 12005
import dev.architectury.registry.menu.ExtendedMenuProvider;
#endif
#if MC_VERSION >= 12100
import net.minecraft.core.registries.Registries;
#endif
#if MC_VERSION >= 12005
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
#endif
#if MC_VERSION >= 12106
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import com.mojang.blaze3d.textures.GpuTexture;
#endif
import net.nicguzzo.wands.utils.Colorf;
import org.jetbrains.annotations.NotNull;
#if MC_VERSION >= 12111
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
#endif
import org.joml.Matrix4f;
import java.util.NoSuchElementException;
import java.util.Optional;

public class Compat {
    //public int debugVersion = MC_VERSION;
    #if MC_VERSION == 12001
        //MC 1.20.1
    #endif
    #if MC_VERSION == 12101
        //MC 1.21.0
    #endif
    #if MC_VERSION == 12111
        //MC 1.21.11
    #endif

    static public boolean has_mending(ItemStack item,Level l){
        #if MC_VERSION >= 12100
            RegistryAccess ra=l.registryAccess();
            int level=0;
            try {
                level = ra.lookup(Registries.ENCHANTMENT).map(
                    e -> e.get(Enchantments.MENDING)).orElseThrow().map(
                    e -> EnchantmentHelper.getItemEnchantmentLevel(e, item)).orElseThrow();
            }catch( NoSuchElementException ignored){

            }
            return level>0;
        #else
            return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING,item)!=0;
        #endif
     }

    static public int get_fortune_level(ItemStack item, Level l){
        #if MC_VERSION >= 12100
            RegistryAccess ra=l.registryAccess();
            int level=0;
            try {
                level = ra.lookup(Registries.ENCHANTMENT).map(e -> e.get(Enchantments.FORTUNE)).orElseThrow().map(e -> EnchantmentHelper.getItemEnchantmentLevel(e, item)).orElseThrow();
            }catch( NoSuchElementException ignored){

            }
            return level;
        #else
            return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE,item);
        #endif
    }
    static public boolean has_silktouch(ItemStack item,Level l){
        #if MC_VERSION >= 12100
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

    static public boolean is_creative(Player player){
        #if MC_VERSION==11605
            return player.abilities.instabuild;
        #else
            return player.getAbilities().instabuild;
        #endif
    }
    static public Inventory get_inventory(Player player){
        #if MC_VERSION==11605
            return player.inventory;
        #else
            return player.getInventory();
        #endif
    }
    static public void set_color(float r, float g, float b, float a){
        #if MC_VERSION==11605
            RenderSystem.color4f(r,g,b,a);
        #else
            #if MC_VERSION < 12111
            RenderSystem.setShaderColor(r,g,b,a);
            #endif
        #endif
    }

    static public void open_menu(ServerPlayer player, ItemStack item, int m){

            #if MC_VERSION <= 12004
            MenuRegistry.openExtendedMenu(player, new ExtendedMenuProvider(){
            @Override
            public void saveExtraData(FriendlyByteBuf packetByteBuf) {
                        packetByteBuf.writeItem(item);
                    }
                    #else
            MenuRegistry.openExtendedMenu(player, new MenuProvider(){
            #endif
			@Override
			public @NotNull Component getDisplayName(){
                    return translatable(item.getItem().getDescriptionId());
			}
			@Override
			public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                AbstractContainerMenu menu=null;
                //switch(m){
                //    case 0: {
                //        menu = new WandMenu(syncId, inv, item);
                //    }
                //    break;
                //    case 1:
                //        menu=new PaletteMenu(syncId, inv, item);
                //    break;
                //    case 2:
                //        menu=new MagicBagMenu(syncId, inv, item);
                //    break;
                //}
                return menu;
			}
		}
        #if MC_VERSION > 12004
        ,
          buf -> {
            buf.writeNbt(ItemStack_save(item,player.level()));
          }
        #endif
        );
    }

    static public void set_carried(Player player, AbstractContainerMenu menu, ItemStack itemStack){
            menu.setCarried(itemStack);
    }
    static public ItemStack get_carried(Player player,AbstractContainerMenu menu){
            return menu.getCarried();
    }
    static public void set_identity(PoseStack m){
            m.setIdentity();
    }
    static public MutableComponent translatable(String key){
        return Component.translatable(key);
    }

    static public MutableComponent literal(String msg){
        return Component.literal(msg);
    }
    static public void register_key(KeyMapping k){
        KeyMappingRegistry.register(k);
    }
    static public void render_info(){
            //ClientGuiEvent.RENDER_HUD.register((e,d)->{ WandsModClient.render_wand_info(e);});
    }

    static public BlockPos get_player_pos(Player player){
        return player.getOnPos();
    }
    static public Vec3 get_player_pos_center(Player player) {
        return player.getOnPos().getCenter();
    }
    static public boolean is_same(ItemStack i1,ItemStack i2){
        return ItemStack.isSameItem(i2,i2);
    }
    static public Level player_level(Player player){
        return player.level();
    }
    static public CompoundTag getTags(ItemStack stack){
        #if MC_VERSION < 12005
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
        #if MC_VERSION >= 12106
        Minecraft.getInstance().getToastManager().addToast(toast);
        #else
        Minecraft.getInstance().getToasts().addToast(toast);
        #endif
    }
    static public CompoundTag ItemStack_save(ItemStack item, Level level){
        #if MC_VERSION >= 12106
            HolderLookup.Provider provider=level.registryAccess();
            ProblemReporter.Collector reporter = new ProblemReporter.Collector();
            TagValueOutput tvo=TagValueOutput.createWithContext(reporter,provider);
            tvo.store(ItemStack.MAP_CODEC,item);
            return tvo.buildResult();
        #else
             #if MC_VERSION >= 12005
                return (CompoundTag) item.save(level.registryAccess());
            #else
                return item.save(new CompoundTag());
            #endif
        #endif
    }
    static public Optional<ItemStack> ItemStack_read(CompoundTag item,  Level level){
    #if MC_VERSION >= 12106
        HolderLookup.Provider provider=level.registryAccess();
        ProblemReporter.Collector reporter = new ProblemReporter.Collector();
        TagValueInput tvi= (TagValueInput) TagValueInput.create(reporter,provider,item);
        return tvi.read(ItemStack.MAP_CODEC);
    #else
        #if MC_VERSION >= 12005
        return ItemStack.parse(level.registryAccess(), item);
        #else
        return Optional.of(ItemStack.of(item));
        #endif
    #endif
    }


    static public void blit(GuiGraphics gui, MyIdExt tx, int x, int y, float u, float v, int w, int h, int tex_w, int tex_h) {
        #if MC_VERSION < 12000
            blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);
        #else
            #if MC_VERSION >= 12111
                gui.blit(RenderPipelines.GUI_TEXTURED, tx.res, x, y, u, v, w, h, tex_w, tex_h);
            #else
                #if MC_VERSION >= 12102
                    gui.blit(RenderType::guiTextured, tx.res, x, y, u, v, w, h,tex_w, tex_h);
                #else
                    gui.blit(tx.res,x, y, u, v, w, h, tex_w, tex_h);
                #endif
            #endif
        #endif
    }
    static public boolean hasShiftDown(){
        #if MC_VERSION >= 12111
             return Minecraft.getInstance().hasShiftDown();
        #else
            return Screen.hasShiftDown();
        #endif
    }
    static public boolean hasControlDown(){
        #if MC_VERSION >= 12111
             return Minecraft.getInstance().hasControlDown();
        #else
        return Screen.hasControlDown();
        #endif
    }
    static public boolean hasAltDown(){
        #if MC_VERSION >= 12111
             return Minecraft.getInstance().hasAltDown();
        #else
        return Screen.hasAltDown();
        #endif
    }
    static public Optional<Boolean> getBoolean(CompoundTag tag,String key) {
        #if MC_VERSION >= 12111
        return tag.getBoolean(key);
        #else
             if(tag.contains(key)) {
                 return Optional.of(tag.getBoolean(key));
             }else{
                 return Optional.empty();
             }
        #endif
    }
    static public Optional<Integer> getInt(CompoundTag tag, String key) {
        #if MC_VERSION >= 12111
        return tag.getInt(key);
        #else
        if(tag.contains(key)) {
            return Optional.of(tag.getInt(key));
        }else{
            return Optional.empty();
        }
        #endif
    }
    static public Optional<CompoundTag> getCompound(CompoundTag tag, String key) {
        #if MC_VERSION >= 12111
        return tag.getCompound(key);
        #else
        if(tag.contains(key)) {
            return Optional.of(tag.getCompound(key));
        }else{
            return Optional.empty();
        }
        #endif
    }
    static public Optional<ListTag> getList(CompoundTag tag, String key) {
        #if MC_VERSION >= 12111
        return tag.getList(key);
        #else
        if(tag.contains(key)) {
            return Optional.of(tag.getList(key,NbtType.COMPOUND));
        }else{
            return Optional.empty();
        }
        #endif
    }
    static public long getWindow() {
        #if MC_VERSION >= 12111
            return Minecraft.getInstance().getWindow().handle();
        #else
            return Minecraft.getInstance().getWindow().getWindow();
        #endif
    }
#if MC_VERSION >= 12111
    static public KeyMapping newKeyMapping(String name,int key,KeyMapping.Category tab) {
        return new KeyMapping(name,key,tab);
    }
#else
    static public KeyMapping newKeyMapping(String name,int key,String tab) {
        return new KeyMapping(name, key,tab);
    }
#endif

    static public boolean isInsideBuildHeight(Level level, int y) {
#if MC_VERSION >= 12111
        return level.isInsideBuildHeight(y);
#else
        return !level.isOutsideBuildHeight(y);
#endif
    }

    static public void saveCustomData(ItemStack stack,CompoundTag tag){
        #if MC_VERSION >=12005
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        #endif
    }
    static public void consumerAddVertexColor(VertexConsumer consumer, Matrix4f matrix, float x,float y,float z,Colorf c) {
        #if MC_VERSION >=12100
        consumer.addVertex(matrix, x, y, z).setColor(c.r, c.g, c.b, c.a);
        #else
        consumer.vertex(matrix, x, y, z).color(c.r, c.g, c.b, c.a);
        #endif
    }
    static public void consumerAddVertexColor(VertexConsumer consumer, float x,float y,float z,Colorf c) {
        #if MC_VERSION >=12100
        consumer.addVertex(x, y, z).setColor(c.r, c.g, c.b, c.a);
        #else
        consumer.vertex(x, y, z).color(c.r, c.g, c.b, c.a);
        #endif
    }


    static public void consumerAddVertexUvColorNormalLight(VertexConsumer consumer, float x,float y,float z,float u,float v,int color,float nx,float ny, float nz,int light) {
        #if MC_VERSION >=12100
        consumer.addVertex(x, y, z).setUv(u, v).setColor(color).setNormal(nx,ny,nz).setLight(light);
        #else
        consumer.vertex(x, y, z).uv(u,v).color(color).normal(nx,ny,nz).uv2(light);
        #endif
    }
    static public void consumerAddVertexUvColorNormalLightOverlay(VertexConsumer consumer,Matrix4f matrix, float x,float y,float z,float u,float v,int color,float nx,float ny, float nz,int light,int overlay) {
        #if MC_VERSION >=12100
        consumer.addVertex(matrix,x, y, z).setUv(u, v).setColor(color).setNormal(nx,ny,nz).setLight(light).setOverlay(overlay);
        #else
        consumer.vertex(matrix,x, y, z).uv(u,v).color(color).normal(nx,ny,nz).uv2(light).overlayCoords(overlay);
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
    public static final int WHITE = -1;
    public static final int BLACK = -16777216;
    public static final int GRAY = -8355712;
    public static final int DARK_GRAY = -12566464;
    public static final int LIGHT_GRAY = -6250336;
    public static final int LIGHTER_GRAY = -4539718;
    public static final int RED = -65536;
    public static final int SOFT_RED = -2142128;
    public static final int GREEN = -16711936;
    public static final int BLUE = -16776961;
    public static final int YELLOW = -256;
    public static final int SOFT_YELLOW = -171;
    public static final int DARK_PURPLE = -11534256;
    public static final int HIGH_CONTRAST_DIAMOND = -11010079;
    public static final int COSMOS_PINK = -13108;
    public static final int TEXT_GRAY = -2039584;
}
