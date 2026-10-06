package trifectumsoftware.colorwheel.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

public class ColorRegionData extends WorldSavedData {

    public static final int REGION_SHIFT = 8;
    public static final String PREFIX = "colorwheel_";

    private static final String TAG_CHUNKS = "chunks";
    private static final String TAG_X = "x";
    private static final String TAG_Z = "z";
    private static final String TAG_COLORS = "c";
    private static final int TAG_TYPE_COMPOUND = 10;

    private static final Set<ColorRegionData> LOADED = Collections
        .newSetFromMap(new IdentityHashMap<ColorRegionData, Boolean>());

    private final Map<Long, IntIntMap> chunks = new HashMap<>();

    public ColorRegionData(String name) {
        super(name);
    }

    public static Set<ColorRegionData> getLoaded() {
        return LOADED;
    }

    public static ColorRegionData get(World world, int x, int z) {
        int regionX = x >> REGION_SHIFT;
        int regionZ = z >> REGION_SHIFT;
        String name = PREFIX + world.provider.dimensionId + "_" + regionX + "_" + regionZ;
        ColorRegionData data = (ColorRegionData) world.mapStorage.loadData(ColorRegionData.class, name);
        if (data == null) {
            data = new ColorRegionData(name);
            world.mapStorage.setData(name, data);
        }
        LOADED.add(data);
        ColorManifestData.get(world)
            .addRegion(name);
        return data;
    }

    public Integer getColor(int x, int y, int z) {
        IntIntMap map = chunks.get(ColorUtil.chunkKey(x, z));
        if (map == null) {
            return null;
        }
        int local = ColorUtil.localKey(x, y, z);
        return map.containsKey(local) ? map.get(local) : null;
    }

    public void setColor(int x, int y, int z, int color) {
        long chunkKey = ColorUtil.chunkKey(x, z);
        IntIntMap map = chunks.get(chunkKey);
        if (map == null) {
            map = new IntIntMap();
            chunks.put(chunkKey, map);
        }
        map.put(ColorUtil.localKey(x, y, z), color);
        markDirty();
    }

    public boolean removeColor(int x, int y, int z) {
        long chunkKey = ColorUtil.chunkKey(x, z);
        IntIntMap map = chunks.get(chunkKey);
        if (map == null) {
            return false;
        }
        int local = ColorUtil.localKey(x, y, z);
        if (!map.containsKey(local)) {
            return false;
        }
        map.remove(local);
        if (map.isEmpty()) {
            chunks.remove(chunkKey);
        }
        markDirty();
        return true;
    }

    public IntIntMap getChunkColors(int chunkX, int chunkZ) {
        return chunks.get(ColorUtil.chunkKeyFromChunk(chunkX, chunkZ));
    }

    public void clear() {
        chunks.clear();
        markDirty();
    }

    public int totalColors() {
        int total = 0;
        for (IntIntMap map : chunks.values()) {
            total += map.size();
        }
        return total;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<Long, IntIntMap> entry : chunks.entrySet()) {
            long chunkKey = entry.getKey();
            IntIntMap map = entry.getValue();
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger(TAG_X, ColorUtil.chunkXFromKey(chunkKey));
            tag.setInteger(TAG_Z, ColorUtil.chunkZFromKey(chunkKey));
            int[] packed = new int[map.size() * 2];
            int[] index = { 0 };
            map.forEach((local, color) -> {
                packed[index[0]++] = local;
                packed[index[0]++] = color;
            });
            tag.setIntArray(TAG_COLORS, packed);
            list.appendTag(tag);
        }
        nbt.setTag(TAG_CHUNKS, list);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        chunks.clear();
        NBTTagList list = nbt.getTagList(TAG_CHUNKS, TAG_TYPE_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            long chunkKey = ColorUtil.chunkKeyFromChunk(tag.getInteger(TAG_X), tag.getInteger(TAG_Z));
            int[] packed = tag.getIntArray(TAG_COLORS);
            if (packed.length == 0) {
                continue;
            }
            IntIntMap map = new IntIntMap(packed.length / 2);
            for (int j = 0; j + 1 < packed.length; j += 2) {
                int color = packed[j + 1];
                if ((color >>> 24) == 0) {

                    color |= 0xFF000000;
                }
                map.put(packed[j], color);
            }
            chunks.put(chunkKey, map);
        }
    }
}
