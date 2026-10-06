package trifectumsoftware.colorwheel.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public final class ClientColorRefresher {

    private ClientColorRefresher() {}

    private static void onClient(Runnable task) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.func_152344_a(() -> {
            if (mc.theWorld != null) {
                task.run();
            }
        });
    }

    public static void refreshBlock(int x, int y, int z) {
        onClient(() -> Minecraft.getMinecraft().theWorld.markBlockRangeForRenderUpdate(x, y, z, x, y, z));
    }

    public static void refreshChunk(int chunkX, int chunkZ) {
        onClient(() -> {
            int minX = chunkX << 4;
            int minZ = chunkZ << 4;
            Minecraft.getMinecraft().theWorld.markBlockRangeForRenderUpdate(minX, 0, minZ, minX + 15, 255, minZ + 15);
        });
    }

    public static void refreshBlocks(int[] data) {
        onClient(() -> {
            World world = Minecraft.getMinecraft().theWorld;
            for (int i = 0; i + 4 < data.length; i += 5) {
                world.markBlockRangeForRenderUpdate(
                    data[i],
                    data[i + 1],
                    data[i + 2],
                    data[i],
                    data[i + 1],
                    data[i + 2]);
            }
        });
    }

    public static void reloadAll() {
        Minecraft mc = Minecraft.getMinecraft();
        mc.func_152344_a(() -> {
            if (mc.renderGlobal != null) {
                mc.renderGlobal.loadRenderers();
            }
        });
    }
}
