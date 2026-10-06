package trifectumsoftware.colorwheel.energy;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import ic2.api.item.IElectricItem;
import ic2.api.item.IElectricItemManager;
import trifectumsoftware.colorwheel.Config;

public class IC2EnergyManager implements IElectricItemManager {

    public static final IC2EnergyManager INSTANCE = new IC2EnergyManager();

    private IC2EnergyManager() {}

    private static double transferLimit(ItemStack stack) {
        return ((IElectricItem) stack.getItem()).getTransferLimit(stack);
    }

    @Override
    public double charge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
        double limit = ignoreTransferLimit ? amount : Math.min(amount, transferLimit(stack));
        int requested = (int) Math.min(Integer.MAX_VALUE, Math.floor(limit * Config.rfPerEU));
        if (simulate) {
            int stored = ColorEnergy.getEnergy(stack);
            return Math.min(requested, Config.energyCapacity - stored) / (double) Config.rfPerEU;
        }
        return ColorEnergy.receive(stack, requested) / (double) Config.rfPerEU;
    }

    @Override
    public double discharge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean externally,
        boolean simulate) {
        double limit = ignoreTransferLimit ? amount : Math.min(amount, transferLimit(stack));
        int requested = (int) Math.min(Integer.MAX_VALUE, Math.floor(limit * Config.rfPerEU));
        int stored = ColorEnergy.getEnergy(stack);
        int given = Math.min(requested, stored);
        if (!simulate) {
            ColorEnergy.setEnergy(stack, stored - given);
        }
        return given / (double) Config.rfPerEU;
    }

    @Override
    public double getCharge(ItemStack stack) {
        return ColorEnergy.getEnergy(stack) / (double) Config.rfPerEU;
    }

    @Override
    public boolean canUse(ItemStack stack, double amount) {
        return getCharge(stack) >= amount;
    }

    @Override
    public boolean use(ItemStack stack, double amount, EntityLivingBase user) {
        int requested = (int) Math.ceil(amount * Config.rfPerEU);
        int stored = ColorEnergy.getEnergy(stack);
        if (stored < requested) {
            return false;
        }
        ColorEnergy.setEnergy(stack, stored - requested);
        return true;
    }

    @Override
    public void chargeFromArmor(ItemStack stack, EntityLivingBase entity) {}

    @Override
    public String getToolTip(ItemStack stack) {

        return null;
    }
}
