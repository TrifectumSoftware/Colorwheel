package trifectumsoftware.colorwheel.storage;

public final class ColorUtil {

    private ColorUtil() {}

    public static long chunkKey(int x, int z) {
        return chunkKeyFromChunk(x >> 4, z >> 4);
    }

    public static long chunkKeyFromChunk(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    public static int chunkXFromKey(long key) {
        return (int) (key >> 32);
    }

    public static int chunkZFromKey(long key) {
        return (int) key;
    }

    public static int localKey(int x, int y, int z) {
        return (y << 8) | ((z & 15) << 4) | (x & 15);
    }

    public static int getRed(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    public static int getGreen(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    public static int getBlue(int rgb) {
        return rgb & 0xFF;
    }

    public static int tint(int argb) {
        int a = argb >>> 24;
        if (a >= 255) {
            return argb & 0xFFFFFF;
        }
        if (a <= 0) {
            return 0xFFFFFF;
        }
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int nr = 255 - (255 - r) * a / 255;
        int ng = 255 - (255 - g) * a / 255;
        int nb = 255 - (255 - b) * a / 255;
        return (nr << 16) | (ng << 8) | nb;
    }
}
