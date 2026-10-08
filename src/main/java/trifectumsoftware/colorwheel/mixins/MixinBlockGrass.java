package trifectumsoftware.colorwheel.mixins;

import net.minecraft.block.BlockGrass;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import trifectumsoftware.colorwheel.client.ClientColorData;

@Mixin(BlockGrass.class)
public class MixinBlockGrass {

    @Inject(method = "colorMultiplier", at = @At("HEAD"), cancellable = true)
    private void colorwheel$applyStoredColor(IBlockAccess world, int x, int y, int z,
        CallbackInfoReturnable<Integer> cir) {
        Integer color = ClientColorData.paintedColor(world, x, y, z);
        if (color != null) {
            cir.setReturnValue(color);
        }
    }
}
