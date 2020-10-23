package net.nicguzzo;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.nicguzzo.common.WandsConfig;
import net.nicguzzo.common.WandsBaseRenderer.MyDir;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("wands")
public class WandsMod
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "wands";
    public static WandsConfig config=null;
    public WandsMod() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
        RegistryHandler.init();

    }

    private void setup(final FMLCommonSetupEvent event)
    {        
        // some preinit code
        //LOGGER.info("HELLO FROM PREINIT");
        //LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
        Path path= FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath());
        LOGGER.info("config path:" + path);
        config=WandsConfig.load_config(path);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        //LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        //InterModComms.sendTo("wpbmod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        //LOGGER.info("Got IMC {}", event.getIMCStream().
        //        map(m->m.getMessageSupplier().get()).
        //        collect(Collectors.toList()));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        //LOGGER.info("HELLO from server starting");
        
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        /*@SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            //LOGGER.info("HELLO from Register Block");
        }*/
        
        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            LOGGER.info("HELLO from onCommonSetup");
            event.enqueueWork(WandsPacketHandler::registerMessage);
        }        
    }   
    static public boolean is_fluid(BlockState state){
		/*if(is_netherite_wand)
			return state.getFluidState().isIn(FluidTags.WATER)||state.getFluidState().isIn(FluidTags.LAVA);
		else*/
		return state.getFluidState().isTagged(FluidTags.WATER);
    }
    static public boolean is_double_slab(BlockState state){
        //if(state.getBlock() instanceof SlabBlock){
            return state.get(SlabBlock.TYPE)==SlabType.DOUBLE;
        //}
        //return false;
    }
    static public BlockPos pos_offset(BlockPos pos,MyDir dir,int o)
    {
        Direction dir1=Direction.values()[dir.ordinal()];
        return pos.offset(dir1, o);
    }
    static public int get_next_int_random(PlayerEntity player,int b){
        return player.world.rand.nextInt(b);
    }
    static public BlockState random_rotate(BlockState state,World world){        
        return state.rotate(Rotation.randomRotation(world.rand));
    }
    static public int get_main_inventory_size(PlayerInventory inv){
        return inv.mainInventory.size();
    }
    static public ItemStack get_player_main_stack(PlayerInventory inv,int i){
        return inv.mainInventory.get(i);
    }
    static public ItemStack get_player_offhand_stack(PlayerInventory inv){
        return inv.offHandInventory.get(0);
    }
    static public void player_offhand_stack_inc(PlayerInventory inv,int i){
        inv.offHandInventory.get(0).grow(i);
    }
    static public void player_offhand_stack_dec(PlayerInventory inv,int i){
        inv.offHandInventory.get(0).shrink(i);
    }
    static public void player_stack_inc(PlayerInventory inv,int slot,int i){
        inv.mainInventory.get(slot).grow(i);
    }
    static public void player_stack_dec(PlayerInventory inv,int slot,int i){
        inv.mainInventory.get(slot).shrink(i);
    }
    static public void set_player_xp(PlayerEntity player,float xp){
        player.experience=xp;
    }
    static public boolean item_stacks_equal(ItemStack i1,ItemStack i2){
        return ItemStack.areItemStackTagsEqual(i1,i2);
    }
    static public boolean is_player_holding_wand(PlayerEntity player){
        return player.inventory.mainInventory.get(player.inventory.currentItem).getItem() instanceof WandItemForge;
    }
    static public void inc_wand_damage(PlayerEntity player,ItemStack stack,int damage){

        stack.damageItem(damage, (LivingEntity)player, 
						(Consumer<LivingEntity>)((p) -> {
								((LivingEntity)p).sendBreakAnimation(Hand.MAIN_HAND);
							}
						)
					);
        
    }
    static public boolean interescts_player_bb(PlayerEntity player,double x1,double y1,double z1,double x2,double y2,double z2){
        AxisAlignedBB bb=player.getBoundingBox();
        return bb.intersects(x1,y1,z1,x2,y2,z2);
    }
    static public void send_message_to_player(String msg){
        assert Minecraft.getInstance().player != null;
        Minecraft instance=Minecraft.getInstance();
        instance.player.sendStatusMessage(new TranslationTextComponent(msg), true);
    }
}
