package net.nicguzzo.wands.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
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

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.client.renderer.GameRenderer;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.client.WandsModClient;
import net.nicguzzo.wands.menues.MagicBagMenu;
import net.nicguzzo.wands.menues.PaletteMenu;
import net.nicguzzo.wands.menues.WandMenu;
import org.jetbrains.annotations.NotNull;

public class Compat {

    static public boolean has_silktouch(ItemStack item,Level l){

            return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH,item)!=0;

    }

    static public ResourceLocation create_resource(String res){
        return new ResourceLocation(WandsMod.MOD_ID, res);
    }

    static public ResourceLocation create_resource_mc(String res){
        return new ResourceLocation(res);
    }

    static public boolean is_creative(Player player){
            return player.getAbilities().instabuild;
    }
    static public Inventory get_inventory(Player player){
            return player.getInventory();
    }
    static public void set_color(float r, float g, float b, float a){
            RenderSystem.setShaderColor(r,g,b,a);
    }
    static public void set_pos_tex_shader(){
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }
    static public void set_texture(ResourceLocation tex){
            RenderSystem.setShaderTexture(0, tex);
    }

    static public void open_menu(ServerPlayer player, ItemStack item, int m){

        MenuRegistry.openExtendedMenu(player, new ExtendedMenuProvider(){
			@Override
			public void saveExtraData(FriendlyByteBuf packetByteBuf) {
				packetByteBuf.writeItem(item);
			}

			@Override
			public Component getDisplayName(){
                    return translatable(item.getItem().getDescriptionId());
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
    static public void set_carried(Player player, AbstractContainerMenu menu, ItemStack itemStack){
            menu.setCarried(itemStack);
    }
    static public ItemStack get_carried(Player player,AbstractContainerMenu menu){
            return menu.getCarried();
    }
    static public MutableComponent translatable(String key){
        return Component.translatable(key);
    }
    static public MutableComponent translatable_item_name(ItemStack item){
       return Component.translatable(item.getDescriptionId());
    }
    static public MutableComponent literal(String msg){
       return Component.literal(msg);
    }
    static public void register_key(KeyMapping k){
       KeyMappingRegistry.register(k);
    }
    static public void render_info(){
        ClientGuiEvent.RENDER_HUD.register((e,d)->{ WandsModClient.render_wand_info(e);});
    }

    static public boolean is_same(ItemStack i1,ItemStack i2){
        return ItemStack.isSameItem(i2,i2);
    }
    static public Level player_level(Player player){
        return player.level();
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

    //static public void toast(Toast toast){
    //    Minecraft.getInstance().getToasts().addToast(toast);
    //}


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
