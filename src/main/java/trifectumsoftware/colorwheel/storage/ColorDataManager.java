package trifectumsoftware.colorwheel.storage;

import java.util.ArrayList;

import net.minecraft.world.World;

import trifectumsoftware.colorwheel.Colorwheel;

public final class ColorDataManager {

    private static final String LEGACY_NAME_PREFIX = "colorwheel_data_";

    private ColorDataManager() {}

    public static void setBlockColor(World world, int x, int y, int z, int color) {
        if (world.isRemote) {
            return;
        }
        ColorRegionData.get(world, x, z)
            .setColor(x, y, z, color);
    }

    public static boolean removeBlockColor(World world, int x, int y, int z) {
        if (world.isRemote) {
            return false;
        }
        return ColorRegionData.get(world, x, z)
            .removeColor(x, y, z);
    }

    public static Integer getBlockColor(World world, int x, int y, int z) {
        if (world.isRemote) {
            return null;
        }
        return ColorRegionData.get(world, x, z)
            .getColor(x, y, z);
    }

    public static IntIntMap getChunkColors(World world, int chunkX, int chunkZ) {
        if (world.isRemote) {
            return null;
        }
        return ColorRegionData.get(world, chunkX << 4, chunkZ << 4)
            .getChunkColors(chunkX, chunkZ);
    }

    public static int clearAll(World world) {
        int removed = 0;
        String dimensionPrefix = ColorRegionData.PREFIX + world.provider.dimensionId + "_";
        for (ColorRegionData region : new ArrayList<>(ColorRegionData.getLoaded())) {
            if (region.mapName.startsWith(dimensionPrefix)) {
                removed += region.totalColors();
                region.clear();
            }
        }
        for (String name : ColorManifestData.get(world)
            .getRegions()) {
            ColorRegionData region = (ColorRegionData) world.mapStorage.loadData(ColorRegionData.class, name);
            if (region != null) {
                removed += region.totalColors();
                region.clear();
            }
        }
        return removed;
    }

    public static void migrateLegacy(World world) {
        String name = LEGACY_NAME_PREFIX + world.provider.dimensionId;
        LegacyColorData legacy = (LegacyColorData) world.mapStorage.loadData(LegacyColorData.class, name);
        if (legacy == null) {
            return;
        }
        int[] colors = legacy.getColors();
        int migrated = 0;
        for (int i = 0; i + 3 < colors.length; i += 4) {
            int color = colors[i + 3];
            if ((color >>> 24) == 0) {

                color |= 0xFF000000;
            }
            setBlockColor(world, colors[i], colors[i + 1], colors[i + 2], color);
            migrated++;
        }
        legacy.clear();
        if (migrated > 0) {
            Colorwheel.LOG.info(
                "Colorwheel: migrated {} painted blocks in dimension {} to the region format",
                migrated,
                world.provider.dimensionId);
        }
    }
}
