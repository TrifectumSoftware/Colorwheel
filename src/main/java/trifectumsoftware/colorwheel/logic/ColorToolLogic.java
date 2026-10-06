package trifectumsoftware.colorwheel.logic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.NetworkRegistry;
import trifectumsoftware.colorwheel.Config;
import trifectumsoftware.colorwheel.energy.ColorEnergy;
import trifectumsoftware.colorwheel.items.ItemColorTool;
import trifectumsoftware.colorwheel.network.PacketAreaCorner;
import trifectumsoftware.colorwheel.network.PacketColorBatch;
import trifectumsoftware.colorwheel.network.PacketHandler;
import trifectumsoftware.colorwheel.storage.ColorDataManager;

public final class ColorToolLogic {

    private static final int MAX_CONNECTED = 1024;
    private static final int[] NEIGHBOR_X = { 0, 0, 0, 0, -1, 1 };
    private static final int[] NEIGHBOR_Y = { -1, 1, 0, 0, 0, 0 };
    private static final int[] NEIGHBOR_Z = { 0, 0, -1, 1, 0, 0 };

    private static final Map<String, int[]> AREA_CORNERS = new HashMap<>();

    private ColorToolLogic() {}

    public static void paint(EntityPlayerMP player, int x, int y, int z, int side, boolean remove) {
        World world = player.worldObj;
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack == null || !(stack.getItem() instanceof ItemColorTool)) {
            return;
        }
        if (!world.blockExists(x, y, z)) {
            return;
        }
        double reach = Math.max(Config.paintReach, player.theItemInWorldManager.getBlockReachDistance() + 2.0D);
        if (player.getDistanceSq(x + 0.5D, y + 0.5D, z + 0.5D) > reach * reach) {
            return;
        }

        int mode = ItemColorTool.getMode(stack);
        List<int[]> positions;
        if (mode == ItemColorTool.MODE_AREA) {

            positions = areaSelection(player, world, x, y, z);
        } else {

            clearAreaSelection(player, true);
            positions = collectPositions(world, x, y, z, side, mode);
        }
        if (positions.isEmpty()) {
            return;
        }

        List<ColorHistory.Change> changes = new ArrayList<>();
        if (remove) {
            for (int[] p : positions) {
                Integer old = ColorDataManager.getBlockColor(world, p[0], p[1], p[2]);
                if (old != null) {
                    changes.add(ColorHistory.Change.of(p[0], p[1], p[2], old, null));
                }
            }
        } else {
            int colorA = ItemColorTool.getColor(stack);
            boolean gradientOn = ItemColorTool.getGradient(stack) != ItemColorTool.GRADIENT_OFF;
            int gradientMode = ItemColorTool.getGradientMode(stack);
            List<ItemColorTool.GradientStop> stops = ItemColorTool.getStops(stack);
            int axis = 0;
            int min = 0;
            int max = 0;
            if (gradientOn) {
                int[] bounds = bounds(positions);
                if (bounds[1] > bounds[0]) {
                    axis = 0;
                    min = bounds[0];
                    max = bounds[1];
                } else if (bounds[5] > bounds[4]) {
                    axis = 2;
                    min = bounds[4];
                    max = bounds[5];
                } else {
                    axis = 1;
                    min = bounds[2];
                    max = bounds[3];
                }
            }
            for (int[] p : positions) {
                int color = colorA;
                if (gradientOn) {
                    float t = max == min ? 0.0F : (coord(p, axis) - min) / (float) (max - min);
                    color = sampleGradient(stops, t, gradientMode);
                }
                Integer old = ColorDataManager.getBlockColor(world, p[0], p[1], p[2]);
                if (old != null && old == color) {
                    continue;
                }
                changes.add(ColorHistory.Change.of(p[0], p[1], p[2], old, color));
            }
        }
        if (changes.isEmpty()) {
            return;
        }

        int cost = (int) Math.min(Integer.MAX_VALUE, (long) changes.size() * Config.energyPerBlock);
        if (!ColorEnergy.spend(player, stack, cost)) {
            return;
        }

        apply(world, changes, true);
        ColorHistory.push(
            player.getUniqueID()
                .toString(),
            world.provider.dimensionId,
            changes);
        broadcast(world, changes, true);
        world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "dig.cloth", 0.4F, remove ? 0.8F : 1.4F);
        if (mode == ItemColorTool.MODE_AREA) {
            player.addChatMessage(
                new ChatComponentText(
                    "Colorwheel: " + (remove ? "cleared " : "painted ") + changes.size() + " blocks"));
        }
    }

    public static void undoRedo(EntityPlayerMP player, boolean redo) {
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack == null || !(stack.getItem() instanceof ItemColorTool)) {
            return;
        }
        String key = player.getUniqueID()
            .toString();
        ColorHistory.Op op = redo ? ColorHistory.redo(key) : ColorHistory.undo(key);
        if (op == null) {
            player.addChatMessage(new ChatComponentText("Colorwheel: nothing to " + (redo ? "redo" : "undo")));
            return;
        }
        MinecraftServer server = MinecraftServer.getServer();
        World world = server.worldServerForDimension(op.dimension);
        if (world == null) {
            return;
        }
        apply(world, op.changeList(), redo);
        broadcast(world, op.changeList(), redo);
        world.playSoundEffect(player.posX, player.posY, player.posZ, "random.click", 0.4F, redo ? 1.4F : 0.8F);
    }

    private static void apply(World world, List<ColorHistory.Change> changes, boolean applyNew) {
        for (ColorHistory.Change change : changes) {
            if (change.present(applyNew)) {
                ColorDataManager.setBlockColor(world, change.x, change.y, change.z, change.color(applyNew));
            } else {
                ColorDataManager.removeBlockColor(world, change.x, change.y, change.z);
            }
        }
    }

    private static void broadcast(World world, List<ColorHistory.Change> changes, boolean appliedNew) {
        int[] data = new int[changes.size() * 5];
        int i = 0;
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (ColorHistory.Change change : changes) {
            data[i++] = change.x;
            data[i++] = change.y;
            data[i++] = change.z;
            data[i++] = change.present(appliedNew) ? 0 : 1;
            data[i++] = change.color(appliedNew);
            minX = Math.min(minX, change.x);
            maxX = Math.max(maxX, change.x);
            minY = Math.min(minY, change.y);
            maxY = Math.max(maxY, change.y);
            minZ = Math.min(minZ, change.z);
            maxZ = Math.max(maxZ, change.z);
        }
        double centerX = (minX + maxX) / 2.0D + 0.5D;
        double centerY = (minY + maxY) / 2.0D + 0.5D;
        double centerZ = (minZ + maxZ) / 2.0D + 0.5D;
        int extent = Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
        PacketHandler.network.sendToAllAround(
            new PacketColorBatch(data),
            new NetworkRegistry.TargetPoint(world.provider.dimensionId, centerX, centerY, centerZ, 128.0D + extent));
    }

    private static List<int[]> collectPositions(World world, int x, int y, int z, int side, int mode) {
        List<int[]> positions = new ArrayList<>();
        switch (mode) {
            case ItemColorTool.MODE_CONNECTED: {
                if (!isPaintable(world, x, y, z)) {
                    break;
                }
                Block target = world.getBlock(x, y, z);
                Set<Long> visited = new HashSet<>();
                ArrayDeque<int[]> queue = new ArrayDeque<>();
                queue.add(new int[] { x, y, z });
                visited.add(pack(x, y, z));
                while (!queue.isEmpty() && positions.size() < MAX_CONNECTED) {
                    int[] p = queue.poll();
                    positions.add(p);
                    for (int d = 0; d < 6; d++) {
                        int nx = p[0] + NEIGHBOR_X[d];
                        int ny = p[1] + NEIGHBOR_Y[d];
                        int nz = p[2] + NEIGHBOR_Z[d];
                        long key = pack(nx, ny, nz);
                        if (visited.contains(key)) {
                            continue;
                        }
                        if (!world.blockExists(nx, ny, nz) || world.getBlock(nx, ny, nz) != target) {
                            continue;
                        }
                        visited.add(key);
                        queue.add(new int[] { nx, ny, nz });
                    }
                }
                break;
            }
            default:
                if (isPaintable(world, x, y, z)) {
                    positions.add(new int[] { x, y, z });
                }
                break;
        }
        return positions;
    }

    private static boolean isPaintable(World world, int x, int y, int z) {
        return world.blockExists(x, y, z) && world.getBlock(x, y, z)
            .getMaterial() != Material.air;
    }

    private static List<int[]> areaSelection(EntityPlayerMP player, World world, int x, int y, int z) {
        String key = player.getUniqueID()
            .toString();
        int[] corner = AREA_CORNERS.get(key);
        if (corner == null || corner[3] != world.provider.dimensionId) {
            AREA_CORNERS.put(key, new int[] { x, y, z, world.provider.dimensionId });
            PacketHandler.network.sendTo(new PacketAreaCorner(x, y, z), player);
            player.addChatMessage(new ChatComponentText("Colorwheel: corner 1 at " + x + ", " + y + ", " + z));
            world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.click", 0.4F, 1.2F);
            return new ArrayList<>();
        }

        long dx = Math.abs((long) corner[0] - x) + 1L;
        long dy = Math.abs((long) corner[1] - y) + 1L;
        long dz = Math.abs((long) corner[2] - z) + 1L;
        long volume = dx * dy * dz;
        if (volume > Config.maxAreaBlocks) {
            player.addChatMessage(
                new ChatComponentText("Colorwheel: too large (" + volume + " > " + Config.maxAreaBlocks + ")"));
            return new ArrayList<>();
        }

        clearAreaSelection(player, true);
        List<int[]> positions = new ArrayList<>();
        int minX = Math.min(corner[0], x);
        int maxX = Math.max(corner[0], x);
        int minY = Math.min(corner[1], y);
        int maxY = Math.max(corner[1], y);
        int minZ = Math.min(corner[2], z);
        int maxZ = Math.max(corner[2], z);
        for (int px = minX; px <= maxX; px++) {
            for (int py = minY; py <= maxY; py++) {
                for (int pz = minZ; pz <= maxZ; pz++) {
                    if (isPaintable(world, px, py, pz)) {
                        positions.add(new int[] { px, py, pz });
                    }
                }
            }
        }
        if (positions.isEmpty()) {
            player.addChatMessage(new ChatComponentText("Colorwheel: the selection contains no blocks."));
        }
        return positions;
    }

    public static void clearAreaSelection(EntityPlayerMP player, boolean notify) {
        if (AREA_CORNERS.remove(
            player.getUniqueID()
                .toString())
            != null && notify) {
            PacketHandler.network.sendTo(PacketAreaCorner.clearing(), player);
        }
    }

    public static void clearAreaSelection(String playerKey) {
        AREA_CORNERS.remove(playerKey);
    }

    private static long pack(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (y & 0xFFF) << 26) | (z & 0x3FFFFFF);
    }

    private static int[] bounds(List<int[]> positions) {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (int[] p : positions) {
            minX = Math.min(minX, p[0]);
            maxX = Math.max(maxX, p[0]);
            minY = Math.min(minY, p[1]);
            maxY = Math.max(maxY, p[1]);
            minZ = Math.min(minZ, p[2]);
            maxZ = Math.max(maxZ, p[2]);
        }
        return new int[] { minX, maxX, minY, maxY, minZ, maxZ };
    }

    private static int coord(int[] position, int axis) {
        return axis == 0 ? position[0] : (axis == 1 ? position[1] : position[2]);
    }

    private static int lerpArgb(int a, int b, float t) {
        int aa = (a >>> 24) & 0xFF;
        int ar = (a >> 16) & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = a & 0xFF;
        int ba = (b >>> 24) & 0xFF;
        int br = (b >> 16) & 0xFF;
        int bg = (b >> 8) & 0xFF;
        int bb = b & 0xFF;
        int ra = Math.round(aa + (ba - aa) * t);
        int rr = Math.round(ar + (br - ar) * t);
        int rg = Math.round(ag + (bg - ag) * t);
        int rb = Math.round(ab + (bb - ab) * t);
        return (ra << 24) | (rr << 16) | (rg << 8) | rb;
    }

    private static int interpolateArgb(int a, int b, float t, int mode) {
        switch (mode) {
            case ItemColorTool.GRADIENT_MODE_CUBIC:
                return lerpArgb(a, b, t * t * (3.0F - 2.0F * t));
            case ItemColorTool.GRADIENT_MODE_OKLAB:
                return oklabArgb(a, b, t);
            default:
                return lerpArgb(a, b, t);
        }
    }

    public static int sampleGradient(List<ItemColorTool.GradientStop> stops, float t, int mode) {
        if (stops.isEmpty()) {
            return Config.defaultColor;
        }
        if (stops.size() == 1 || t <= stops.get(0).pos) {
            return stops.get(0).color;
        }
        int last = stops.size() - 1;
        if (t >= stops.get(last).pos) {
            return stops.get(last).color;
        }
        for (int i = 0; i < last; i++) {
            ItemColorTool.GradientStop a = stops.get(i);
            ItemColorTool.GradientStop b = stops.get(i + 1);
            if (t >= a.pos && t <= b.pos) {
                float local = (t - a.pos) / (b.pos - a.pos);
                return interpolateArgb(a.color, b.color, local, mode);
            }
        }
        return stops.get(last).color;
    }

    private static int oklabArgb(int a, int b, float t) {
        float[] labA = srgbToOklab(a);
        float[] labB = srgbToOklab(b);
        float l = labA[0] + (labB[0] - labA[0]) * t;
        float okA = labA[1] + (labB[1] - labA[1]) * t;
        float okB = labA[2] + (labB[2] - labA[2]) * t;
        int alphaA = (a >>> 24) & 0xFF;
        int alphaB = (b >>> 24) & 0xFF;
        int alpha = Math.round(alphaA + (alphaB - alphaA) * t);
        return oklabToArgb(l, okA, okB, alpha);
    }

    private static float[] srgbToOklab(int argb) {
        float r = srgbToLinear(((argb >> 16) & 0xFF) / 255.0F);
        float g = srgbToLinear(((argb >> 8) & 0xFF) / 255.0F);
        float b = srgbToLinear((argb & 0xFF) / 255.0F);
        float l = 0.41222146F * r + 0.53633255F * g + 0.05144599F * b;
        float m = 0.21190350F * r + 0.68069955F * g + 0.10739696F * b;
        float s = 0.08830246F * r + 0.28171884F * g + 0.62997870F * b;
        float lc = (float) Math.cbrt(l);
        float mc = (float) Math.cbrt(m);
        float sc = (float) Math.cbrt(s);
        return new float[] { 0.21045426F * lc + 0.79361779F * mc - 0.00407205F * sc,
            1.97799850F * lc - 2.42859221F * mc + 0.45059371F * sc,
            0.02590404F * lc + 0.78277177F * mc - 0.80867577F * sc };
    }

    private static int oklabToArgb(float l, float a, float b, int alpha) {
        float lc = l + 0.39633778F * a + 0.21580376F * b;
        float mc = l - 0.10556135F * a - 0.06385417F * b;
        float sc = l - 0.08948418F * a - 1.2914855F * b;
        float l3 = lc * lc * lc;
        float m3 = mc * mc * mc;
        float s3 = sc * sc * sc;
        float r = 4.0767417F * l3 - 3.3077116F * m3 + 0.23096993F * s3;
        float g = -1.2684380F * l3 + 2.6097574F * m3 - 0.34131940F * s3;
        float blue = -0.00419609F * l3 - 0.70341861F * m3 + 1.7076147F * s3;
        int strength = Math.max(0, Math.min(255, alpha));
        return (strength << 24) | (linearToSrgb8(r) << 16) | (linearToSrgb8(g) << 8) | linearToSrgb8(blue);
    }

    private static float srgbToLinear(float c) {
        return c <= 0.04045F ? c / 12.92F : (float) Math.pow((c + 0.055F) / 1.055F, 2.4D);
    }

    private static int linearToSrgb8(float c) {
        if (c < 0.0F) {
            c = 0.0F;
        } else if (c > 1.0F) {
            c = 1.0F;
        }
        float s = c <= 0.0031308F ? c * 12.92F : (float) (1.055F * Math.pow(c, 1.0D / 2.4D) - 0.055F);
        if (s < 0.0F) {
            s = 0.0F;
        } else if (s > 1.0F) {
            s = 1.0F;
        }
        return Math.round(s * 255.0F);
    }
}
