package trifectumsoftware.colorwheel.items;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import trifectumsoftware.colorwheel.Colorwheel;
import trifectumsoftware.colorwheel.Config;
import trifectumsoftware.colorwheel.client.ClientColorData;
import trifectumsoftware.colorwheel.energy.ColorEnergy;
import trifectumsoftware.colorwheel.network.PacketHandler;
import trifectumsoftware.colorwheel.network.PacketPaintBlock;
import trifectumsoftware.colorwheel.network.PacketSetToolData;

public class ItemColorTool extends Item {

    private static final String TAG_COLOR = "ColorwheelColor";
    private static final String TAG_MODE = "ColorwheelMode";
    private static final String TAG_GRADIENT = "ColorwheelGradient";
    private static final String TAG_GRADIENT_MODE = "ColorwheelGradientMode";
    private static final String TAG_GRADIENT_STOPS = "ColorwheelGradientStops";
    private static final String TAG_GRADIENT_SELECTED = "ColorwheelGradientSelected";

    public static final int MODE_SINGLE = 0;
    public static final int MODE_CONNECTED = 1;

    public static final int MODE_AREA = 2;

    public static final int GRADIENT_OFF = 0;
    public static final int GRADIENT_FIRST = 1;
    public static final int GRADIENT_SECOND = 2;

    public static final int GRADIENT_MODE_LERP = 0;
    public static final int GRADIENT_MODE_CUBIC = 1;
    public static final int GRADIENT_MODE_OKLAB = 2;

    private static final String[] MODE_NAMES = { "1x1", "Connected", "Area" };
    private static final String[] GRADIENT_MODE_NAMES = { "Lerp", "Cubic", "OKLab" };

    public static final class GradientStop {

        public float pos;
        public int color;

        public GradientStop(float pos, int color) {
            this.pos = pos;
            this.color = color;
        }
    }

    public ItemColorTool() {
        setMaxStackSize(1);
        setUnlocalizedName("colorwheel.colorTool");
        setTextureName("colorwheel:color_tool");
        setCreativeTab(CreativeTabs.tabTools);
    }

    public static int getColor(ItemStack stack) {
        if (!has(stack, TAG_COLOR)) {
            return 0xFF000000 | Config.defaultColor;
        }
        int color = stack.getTagCompound()
            .getInteger(TAG_COLOR);
        return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
    }

    public static void setColor(ItemStack stack, int argb) {
        setInt(stack, TAG_COLOR, argb);
    }

    public static List<GradientStop> getStops(ItemStack stack) {
        List<GradientStop> stops = new ArrayList<>();
        if (has(stack, TAG_GRADIENT_STOPS)) {
            NBTTagList list = stack.getTagCompound()
                .getTagList(TAG_GRADIENT_STOPS, 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                stops.add(new GradientStop(tag.getFloat("pos"), tag.getInteger("color")));
            }
        }
        if (stops.isEmpty()) {
            stops.add(new GradientStop(0.0F, getColor(stack)));
        }
        stops.sort((a, b) -> Float.compare(a.pos, b.pos));
        return stops;
    }

    public static void setStops(ItemStack stack, List<GradientStop> stops) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagList list = new NBTTagList();
        for (GradientStop stop : stops) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setFloat("pos", stop.pos);
            tag.setInteger("color", stop.color);
            list.appendTag(tag);
        }
        stack.getTagCompound()
            .setTag(TAG_GRADIENT_STOPS, list);
    }

    public static int getSelectedStop(ItemStack stack) {
        int selected = getInt(stack, TAG_GRADIENT_SELECTED, 0);
        return Math.max(0, Math.min(selected, getStops(stack).size() - 1));
    }

    public static void setSelectedStop(ItemStack stack, int index) {
        setInt(stack, TAG_GRADIENT_SELECTED, index);
    }

    public static int getMode(ItemStack stack) {
        int mode = getInt(stack, TAG_MODE, MODE_SINGLE);
        return mode >= 0 && mode < MODE_NAMES.length ? mode : MODE_SINGLE;
    }

    public static void setMode(ItemStack stack, int mode) {
        setInt(stack, TAG_MODE, mode);
    }

    public static int cycleMode(ItemStack stack) {
        int mode = (getMode(stack) + 1) % MODE_NAMES.length;
        setMode(stack, mode);
        return mode;
    }

    public static String modeName(int mode) {
        return name(MODE_NAMES, mode);
    }

    public static int getGradient(ItemStack stack) {
        int gradient = getInt(stack, TAG_GRADIENT, GRADIENT_OFF);
        return gradient >= 0 && gradient <= GRADIENT_SECOND ? gradient : GRADIENT_OFF;
    }

    public static void setGradient(ItemStack stack, int gradient) {
        setInt(stack, TAG_GRADIENT, gradient);
    }

    public static int getGradientMode(ItemStack stack) {
        int mode = getInt(stack, TAG_GRADIENT_MODE, GRADIENT_MODE_LERP);
        return mode >= 0 && mode < GRADIENT_MODE_NAMES.length ? mode : GRADIENT_MODE_LERP;
    }

    public static void setGradientMode(ItemStack stack, int mode) {
        setInt(stack, TAG_GRADIENT_MODE, mode);
    }

    public static int cycleGradientMode(ItemStack stack) {
        int mode = (getGradientMode(stack) + 1) % GRADIENT_MODE_NAMES.length;
        setGradientMode(stack, mode);
        return mode;
    }

    public static String gradientModeName(int mode) {
        return name(GRADIENT_MODE_NAMES, mode);
    }

    public static int getEditColor(ItemStack stack) {
        if (getGradient(stack) == GRADIENT_OFF) {
            return getColor(stack);
        }
        List<GradientStop> stops = getStops(stack);
        int selected = getSelectedStop(stack);
        if (selected >= 0 && selected < stops.size()) {
            return stops.get(selected).color;
        }
        return getColor(stack);
    }

    public static void setEditColor(ItemStack stack, int argb) {
        if (getGradient(stack) == GRADIENT_OFF) {
            setColor(stack, argb);
            return;
        }
        List<GradientStop> stops = getStops(stack);
        int selected = getSelectedStop(stack);
        if (selected >= 0 && selected < stops.size()) {
            stops.get(selected).color = argb;
            setStops(stack, stops);
        }
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            previewClient(stack, player, world, x, y, z, player.isSneaking());
            PacketHandler.network.sendToServer(new PacketPaintBlock(x, y, z, side, player.isSneaking()));
        }

        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) {

            MovingObjectPosition hit = traceLookedAtBlock(world, player);
            if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                if (!player.isSneaking()) {
                    previewClient(stack, player, world, hit.blockX, hit.blockY, hit.blockZ, false);
                }
                PacketHandler.network.sendToServer(
                    new PacketPaintBlock(hit.blockX, hit.blockY, hit.blockZ, hit.sideHit, player.isSneaking()));
                return stack;
            }
            if (player.isSneaking()) {
                int mode = cycleMode(stack);
                PacketHandler.network.sendToServer(PacketSetToolData.fromStack(stack));
                player.addChatMessage(
                    new ChatComponentText(
                        StatCollector.translateToLocal("item.colorwheel.colorTool.mode") + " " + modeName(mode)));
                player.playSound("random.click", 0.3F, 1.5F);
            } else {
                Colorwheel.proxy.openColorPicker(stack);
            }
        }
        return stack;
    }

    public static MovingObjectPosition traceLookedAtBlock(World world, EntityPlayer player) {
        Vec3 start = player.getPosition(1.0F);
        Vec3 look = player.getLook(1.0F);
        Vec3 end = start.addVector(
            look.xCoord * Config.paintReach,
            look.yCoord * Config.paintReach,
            look.zCoord * Config.paintReach);
        return world.func_147447_a(start, end, false, false, true);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        list.add(
            EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.colorwheel.colorTool.color")
                + " "
                + EnumChatFormatting.WHITE
                + String.format("#%08X", getColor(stack)));
        if (ColorEnergy.showsCharge(stack)) {
            list.add(
                EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.colorwheel.colorTool.energy")
                    + " "
                    + EnumChatFormatting.WHITE
                    + ColorEnergy.getEnergy(stack)
                    + " / "
                    + Config.energyCapacity
                    + " RF");
        }
        if (!GuiScreen.isShiftKeyDown()) {
            addMoreInfoHint(list);
            return;
        }
        list.add(
            EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.colorwheel.colorTool.mode")
                + " "
                + EnumChatFormatting.WHITE
                + modeName(getMode(stack)));
        int gradient = getGradient(stack);
        if (gradient == GRADIENT_OFF) {
            list.add(
                EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.colorwheel.colorTool.gradient")
                    + " "
                    + EnumChatFormatting.WHITE
                    + StatCollector.translateToLocal("item.colorwheel.colorTool.gradientOff"));
        } else {
            list.add(
                EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.colorwheel.colorTool.gradient")
                    + " "
                    + EnumChatFormatting.WHITE
                    + getStops(stack).size()
                    + " stops, "
                    + gradientModeName(getGradientMode(stack)));
        }
        list.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("item.colorwheel.colorTool.tip1"));
        list.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("item.colorwheel.colorTool.tip2"));
        list.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("item.colorwheel.colorTool.tip3"));
        list.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("item.colorwheel.colorTool.tip4"));
        list.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("item.colorwheel.colorTool.tip5"));
        if (getMode(stack) == MODE_AREA) {
            list.add(
                EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("item.colorwheel.colorTool.tipArea"));
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void addMoreInfoHint(List list) {
        list.add(
            EnumChatFormatting.YELLOW + "LSHIFT"
                + EnumChatFormatting.GRAY
                + " "
                + StatCollector.translateToLocal("colorwheel.tooltip.moreInfo"));
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return ColorEnergy.showsCharge(stack);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        int capacity = Math.max(1, Config.energyCapacity);
        return 1.0D - Math.min(1.0D, ColorEnergy.getEnergy(stack) / (double) capacity);
    }

    private static boolean has(ItemStack stack, String tag) {
        return stack.hasTagCompound() && stack.getTagCompound()
            .hasKey(tag);
    }

    private static int getInt(ItemStack stack, String tag, int fallback) {
        return has(stack, tag) ? stack.getTagCompound()
            .getInteger(tag) : fallback;
    }

    private static void setInt(ItemStack stack, String tag, int value) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound()
            .setInteger(tag, value);
    }

    private static String name(String[] names, int index) {
        return names[(index % names.length + names.length) % names.length];
    }

    private static void previewClient(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
        boolean remove) {
        if (getMode(stack) != MODE_SINGLE || ColorEnergy.costsEnergy(player)) {
            return;
        }
        if (remove) {
            ClientColorData.remove(x, y, z);
        } else if (getGradient(stack) == GRADIENT_OFF) {
            ClientColorData.set(x, y, z, getColor(stack));
        } else {
            return;
        }
        world.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
    }
}
