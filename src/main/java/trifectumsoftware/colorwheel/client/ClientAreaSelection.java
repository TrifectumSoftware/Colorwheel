package trifectumsoftware.colorwheel.client;

public final class ClientAreaSelection {

    private static volatile boolean active;
    private static volatile int x;
    private static volatile int y;
    private static volatile int z;

    private ClientAreaSelection() {}

    public static void set(int cornerX, int cornerY, int cornerZ) {
        active = true;
        x = cornerX;
        y = cornerY;
        z = cornerZ;
    }

    public static void clear() {
        active = false;
    }

    public static boolean isActive() {
        return active;
    }

    public static int getX() {
        return x;
    }

    public static int getY() {
        return y;
    }

    public static int getZ() {
        return z;
    }
}
