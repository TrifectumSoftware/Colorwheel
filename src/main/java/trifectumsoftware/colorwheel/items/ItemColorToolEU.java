package trifectumsoftware.colorwheel.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import trifectumsoftware.colorwheel.Config;
import trifectumsoftware.colorwheel.energy.IC2EnergyManager;

public class ItemColorToolEU extends ItemColorToolEnergy implements ISpecialElectricItem {

    @Override
    public boolean canProvideEnergy(ItemStack stack) {
        return false;
    }

    @Override
    public Item getChargedItem(ItemStack stack) {
        return this;
    }

    @Override
    public Item getEmptyItem(ItemStack stack) {
        return this;
    }

    @Override
    public double getMaxCharge(ItemStack stack) {
        return Config.energyCapacity / (double) Config.rfPerEU;
    }

    @Override
    public int getTier(ItemStack stack) {
        return Config.energyTier;
    }

    @Override
    public double getTransferLimit(ItemStack stack) {
        switch (Config.energyTier) {
            case 2:
                return 128.0D;
            case 3:
                return 512.0D;
            case 4:
                return 2048.0D;
            default:
                return 32.0D;
        }
    }

    @Override
    public IElectricItemManager getManager(ItemStack stack) {
        return IC2EnergyManager.INSTANCE;
    }
}
