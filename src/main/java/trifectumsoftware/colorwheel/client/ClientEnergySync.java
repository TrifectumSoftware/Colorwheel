package trifectumsoftware.colorwheel.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import trifectumsoftware.colorwheel.energy.ColorEnergy;
import trifectumsoftware.colorwheel.items.ItemColorTool;

public final class ClientEnergySync {

    private ClientEnergySync() {}

    public static void apply(final int energy) {
        final Minecraft mc = Minecraft.getMinecraft();
        mc.func_152344_a(() -> {
            EntityPlayer player = mc.thePlayer;
            if (player == null) {
                return;
            }
            ItemStack stack = player.inventory.getCurrentItem();
            if (stack != null && stack.getItem() instanceof ItemColorTool) {
                ColorEnergy.setEnergy(stack, energy);
            }
        });
    }
}
