package trifectumsoftware.colorwheel.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import trifectumsoftware.colorwheel.client.ClientColorData;
import trifectumsoftware.colorwheel.storage.ColorUtil;

@Pseudo
@Mixin(targets = "org.embeddedt.embeddium.impl.biome.BiomeColorCache", remap = false)
public class MixinBiomeColorCache {

    @Inject(method = "getColor", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void colorwheel$paintedColor(Object resolver, int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        Integer stored = ClientColorData.get(x, y, z);
        if (stored != null) {
            cir.setReturnValue(ColorUtil.tint(stored));
        }
    }
}
