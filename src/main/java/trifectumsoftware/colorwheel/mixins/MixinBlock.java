package trifectumsoftware.colorwheel.mixins;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import trifectumsoftware.colorwheel.client.ClientColorData;

@Mixin(Block.class)
public class MixinBlock {

    @Inject(method = "colorMultiplier", at = @At("HEAD"), cancellable = true)
    private void colorwheel$applyStoredColor(IBlockAccess world, int x, int y, int z,
        CallbackInfoReturnable<Integer> cir) {
        if (world == null) {
            return;
        }
        Integer color = ClientColorData.get(x, y, z);
        if (color != null) {
            cir.setReturnValue(color);
        }
    }
}
