package trifectumsoftware.colorwheel.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.IBlockAccess;

import trifectumsoftware.colorwheel.storage.ColorUtil;
import trifectumsoftware.colorwheel.storage.IntIntMap;

public final class ClientColorData {

    private static final Map<Long, IntIntMap> CHUNKS = new ConcurrentHashMap<>();

    private ClientColorData() {}

    public static void set(int x, int y, int z, int color) {
        setLocal(x >> 4, z >> 4, ColorUtil.localKey(x, y, z), color);
    }

    public static void setLocal(int chunkX, int chunkZ, int local, int color) {
        long key = ColorUtil.chunkKeyFromChunk(chunkX, chunkZ);
        IntIntMap map = CHUNKS.computeIfAbsent(key, k -> new IntIntMap());
        synchronized (map) {
            map.put(local, color);
        }
    }

    public static void remove(int x, int y, int z) {
        long key = ColorUtil.chunkKey(x, z);
        IntIntMap map = CHUNKS.get(key);
        if (map == null) {
            return;
        }
        synchronized (map) {
            map.remove(ColorUtil.localKey(x, y, z));
            if (map.isEmpty()) {
                CHUNKS.remove(key, map);
            }
        }
    }

    public static void clearChunk(int chunkX, int chunkZ) {
        CHUNKS.remove(ColorUtil.chunkKeyFromChunk(chunkX, chunkZ));
    }

    public static Integer get(int x, int y, int z) {
        IntIntMap map = CHUNKS.get(ColorUtil.chunkKey(x, z));
        if (map == null) {
            return null;
        }
        int local = ColorUtil.localKey(x, y, z);
        synchronized (map) {
            return map.containsKey(local) ? map.get(local) : null;
        }
    }

    public static Integer paintedColor(IBlockAccess world, int x, int y, int z) {
        if (world == null) {
            return null;
        }
        Integer stored = get(x, y, z);
        return stored == null ? null : ColorUtil.tint(stored);
    }

    public static void clear() {
        CHUNKS.clear();
    }
}
