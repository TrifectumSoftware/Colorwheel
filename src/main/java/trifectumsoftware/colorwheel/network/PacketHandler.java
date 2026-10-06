package trifectumsoftware.colorwheel.network;

import net.minecraft.world.World;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public final class PacketHandler {

    private static final String CHANNEL_NAME = "colorwheel";
    private static final double BROADCAST_RANGE = 128.0D;

    public static SimpleNetworkWrapper network;

    private PacketHandler() {}

    public static void init() {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL_NAME);
        network.registerMessage(PacketColorUpdate.Handler.class, PacketColorUpdate.class, 0, Side.CLIENT);
        network.registerMessage(PacketChunkColors.Handler.class, PacketChunkColors.class, 1, Side.CLIENT);
        network.registerMessage(PacketSetToolData.Handler.class, PacketSetToolData.class, 2, Side.SERVER);
        network.registerMessage(PacketPaintBlock.Handler.class, PacketPaintBlock.class, 3, Side.SERVER);
        network.registerMessage(PacketColorBatch.Handler.class, PacketColorBatch.class, 4, Side.CLIENT);
        network.registerMessage(PacketColorClear.Handler.class, PacketColorClear.class, 5, Side.CLIENT);
        network.registerMessage(PacketUndoRedo.Handler.class, PacketUndoRedo.class, 6, Side.SERVER);
        network.registerMessage(PacketEnergySync.Handler.class, PacketEnergySync.class, 7, Side.CLIENT);
        network.registerMessage(PacketAreaCorner.Handler.class, PacketAreaCorner.class, 8, Side.CLIENT);
        network.registerMessage(PacketCancelArea.Handler.class, PacketCancelArea.class, 9, Side.SERVER);
    }

    public static void broadcastColorUpdate(World world, int x, int y, int z, int color, boolean remove) {
        network.sendToAllAround(
            new PacketColorUpdate(x, y, z, color, remove),
            new NetworkRegistry.TargetPoint(world.provider.dimensionId, x + 0.5D, y + 0.5D, z + 0.5D, BROADCAST_RANGE));
    }
}
