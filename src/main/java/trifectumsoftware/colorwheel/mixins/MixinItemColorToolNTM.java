package trifectumsoftware.colorwheel.mixins;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;

import api.hbm.energymk2.IBatteryItem;
import trifectumsoftware.colorwheel.energy.ColorEnergy;
import trifectumsoftware.colorwheel.items.ItemColorToolEU;

@Mixin(value = ItemColorToolEU.class, remap = false)
public class MixinItemColorToolNTM implements IBatteryItem {

    @Override
    public void chargeBattery(ItemStack stack, long power) {
        ColorEnergy.receiveHe(stack, power);
    }

    @Override
    public void setCharge(ItemStack stack, long power) {
        ColorEnergy.setHe(stack, power);
    }

    @Override
    public void dischargeBattery(ItemStack stack, long power) {
        ColorEnergy.dischargeHe(stack, power);
    }

    @Override
    public long getCharge(ItemStack stack) {
        return ColorEnergy.getHe(stack);
    }

    @Override
    public long getMaxCharge(ItemStack stack) {
        return ColorEnergy.maxHe();
    }

    @Override
    public long getChargeRate(ItemStack stack) {
        return ColorEnergy.heRate();
    }

    @Override
    public long getDischargeRate(ItemStack stack) {
        return 0;
    }
}
