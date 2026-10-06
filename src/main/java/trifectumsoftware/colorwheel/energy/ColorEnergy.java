package trifectumsoftware.colorwheel.energy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import trifectumsoftware.colorwheel.Config;
import trifectumsoftware.colorwheel.network.PacketEnergySync;
import trifectumsoftware.colorwheel.network.PacketHandler;

public final class ColorEnergy {

    private static final String TAG_ENERGY = "ColorwheelEnergy";

    public static final boolean RF_PRESENT = classExists("cofh.api.energy.IEnergyContainerItem");

    public static final boolean HE_PRESENT = classExists("api.hbm.energymk2.IBatteryItem");

    public static final boolean EU_PRESENT = classExists("ic2.api.item.IElectricItem");

    private ColorEnergy() {}

    public static boolean classExists(String name) {
        try {
            Class.forName(name, false, ColorEnergy.class.getClassLoader());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasSystem() {
        return RF_PRESENT || HE_PRESENT || EU_PRESENT;
    }

    public static boolean costsEnergy(EntityPlayer player) {
        return Config.useEnergy && hasSystem() && !player.capabilities.isCreativeMode;
    }

    public static boolean showsCharge(ItemStack stack) {
        return hasSystem() && (Config.useEnergy || getEnergy(stack) > 0);
    }

    public static int getEnergy(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return 0;
        }
        int energy = stack.getTagCompound()
            .getInteger(TAG_ENERGY);
        if (energy < 0) {
            return 0;
        }
        return Math.min(energy, Config.energyCapacity);
    }

    public static void setEnergy(ItemStack stack, int energy) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound()
            .setInteger(TAG_ENERGY, Math.max(0, Math.min(energy, Config.energyCapacity)));
    }

    public static int receive(ItemStack stack, int amount) {
        if (amount <= 0) {
            return 0;
        }
        int stored = getEnergy(stack);
        int accepted = Math.min(amount, Config.energyCapacity - stored);
        if (accepted > 0) {
            setEnergy(stack, stored + accepted);
        }
        return accepted;
    }

    public static void receiveHe(ItemStack stack, long power) {
        if (power > 0) {
            receive(stack, (int) Math.min(Integer.MAX_VALUE, power * Config.rfPerHE));
        }
    }

    public static void setHe(ItemStack stack, long power) {
        setEnergy(stack, (int) Math.min(Integer.MAX_VALUE, Math.max(0L, power) * Config.rfPerHE));
    }

    public static void dischargeHe(ItemStack stack, long power) {
        if (power > 0) {
            long drain = Math.min(Integer.MAX_VALUE, power * Config.rfPerHE);
            setEnergy(stack, (int) Math.max(0L, getEnergy(stack) - drain));
        }
    }

    public static long getHe(ItemStack stack) {
        return getEnergy(stack) / Config.rfPerHE;
    }

    public static long maxHe() {
        return Config.energyCapacity / Config.rfPerHE;
    }

    public static long heRate() {
        return Math.max(1L, maxHe() / 100L);
    }

    public static boolean spend(EntityPlayerMP player, ItemStack stack, long amount) {
        if (amount <= 0 || !costsEnergy(player)) {
            return true;
        }
        int stored = getEnergy(stack);
        if (stored < amount) {
            player.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.RED + "Colorwheel: not enough energy (" + stored + " / " + amount + " RF)"));
            return false;
        }
        setEnergy(stack, (int) (stored - amount));
        PacketHandler.network.sendTo(new PacketEnergySync(getEnergy(stack)), player);
        return true;
    }
}
