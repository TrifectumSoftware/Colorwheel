package trifectumsoftware.colorwheel.items;

import net.minecraft.item.ItemStack;

import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.common.Optional;
import trifectumsoftware.colorwheel.Config;
import trifectumsoftware.colorwheel.energy.ColorEnergy;

@Optional.Interface(iface = "cofh.api.energy.IEnergyContainerItem", modid = "CoFHCore")
public class ItemColorToolEnergy extends ItemColorTool implements IEnergyContainerItem {

    @Override
    public int receiveEnergy(ItemStack stack, int maxReceive, boolean simulate) {
        if (simulate) {
            return Math.min(Math.max(0, maxReceive), Config.energyCapacity - ColorEnergy.getEnergy(stack));
        }
        return ColorEnergy.receive(stack, Math.max(0, maxReceive));
    }

    @Override
    public int extractEnergy(ItemStack stack, int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored(ItemStack stack) {
        return ColorEnergy.getEnergy(stack);
    }

    @Override
    public int getMaxEnergyStored(ItemStack stack) {
        return Config.energyCapacity;
    }
}
