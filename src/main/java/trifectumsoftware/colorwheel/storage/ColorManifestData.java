package trifectumsoftware.colorwheel.storage;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

public class ColorManifestData extends WorldSavedData {

    private static final String PREFIX = "colorwheel_manifest_";
    private static final String TAG_REGIONS = "regions";
    private static final int TAG_TYPE_STRING = 8;

    private final Set<String> regions = new HashSet<>();

    public ColorManifestData(String name) {
        super(name);
    }

    public static ColorManifestData get(World world) {
        String name = PREFIX + world.provider.dimensionId;
        ColorManifestData data = (ColorManifestData) world.mapStorage.loadData(ColorManifestData.class, name);
        if (data == null) {
            data = new ColorManifestData(name);
            world.mapStorage.setData(name, data);
        }
        return data;
    }

    public void addRegion(String name) {
        if (regions.add(name)) {
            markDirty();
        }
    }

    public Set<String> getRegions() {
        return regions;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        regions.clear();
        NBTTagList list = nbt.getTagList(TAG_REGIONS, TAG_TYPE_STRING);
        for (int i = 0; i < list.tagCount(); i++) {
            regions.add(list.getStringTagAt(i));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (String region : regions) {
            list.appendTag(new NBTTagString(region));
        }
        nbt.setTag(TAG_REGIONS, list);
    }
}
