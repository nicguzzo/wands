package net.nicguzzo;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class WandsMod implements ModInitializer {

	public static final Identifier WAND_PACKET_ID = new Identifier("wands", "wand");

	//public static final WandItem NETHERITE_WAND_ITEM = new WandItem(31,2031);
	public static final WandItem DIAMOND_WAND_ITEM = new WandItem(27,1561);
	public static final WandItem IRON_WAND_ITEM = new WandItem(9,250);
	public static final WandItem STONE_WAND_ITEM = new WandItem(5,131);

    @Override
    public void onInitialize() {

        Registry.register(Registry.ITEM, new Identifier("wands", "diamond_wand"), DIAMOND_WAND_ITEM);
		Registry.register(Registry.ITEM, new Identifier("wands", "iron_wand"), IRON_WAND_ITEM);
		Registry.register(Registry.ITEM, new Identifier("wands", "stone_wand"), STONE_WAND_ITEM);

		ServerSidePacketRegistry.INSTANCE.register(WAND_PACKET_ID, (packetContext, attachedData) -> {
            // Get the BlockPos we put earlier in the IO thread
			BlockPos pos0 = attachedData.readBlockPos();
			BlockPos pos1 = attachedData.readBlockPos();
			//BlockState state = attachedData.read
            packetContext.getTaskQueue().execute(() -> {
                // Execute on the main thread 
                // ALWAYS validate that the information received is valid in a C2S packet!
                if(World.isValid(pos0) && World.isValid(pos1)){
                    BlockState state=packetContext.getPlayer().world.getBlockState(pos0);
                    packetContext.getPlayer().world.setBlockState(pos1, state);
                }
 
            });
        });
    }
    
}