package trifectumsoftware.colorwheel.storage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

public class LegacyColorData extends WorldSavedData {

    private static final String TAG_COLORS = "colors";

    private int[] colors = new int[0];

    public LegacyColorData(String name) {
        super(name);
    }

    public int[] getColors() {
        return colors;
    }

    public void clear() {
        colors = new int[0];
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        colors = nbt.getIntArray(TAG_COLORS);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setIntArray(TAG_COLORS, colors);
    }
}
