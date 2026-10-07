package trifectumsoftware.colorwheel.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gcewing.architecture.client.render.target.RenderTargetWorld;
import trifectumsoftware.colorwheel.client.ClientColorData;
import trifectumsoftware.colorwheel.storage.ColorUtil;

@Mixin(value = RenderTargetWorld.class, remap = false)
public abstract class MixinRenderTargetWorld {

    @Shadow(remap = false)
    protected gcewing.architecture.compat.BlockPos blockPos;

    @Shadow(remap = false)
    protected float vr;

    @Shadow(remap = false)
    protected float vg;

    @Shadow(remap = false)
    protected float vb;

    @Unique
    private boolean colorwheel$tintResolved;

    @Unique
    private float colorwheel$tintR = 1.0F;

    @Unique
    private float colorwheel$tintG = 1.0F;

    @Unique
    private float colorwheel$tintB = 1.0F;

    @Inject(method = "setLight", at = @At("TAIL"), remap = false, require = 0)
    private void colorwheel$applyTint(float shadow, int brightness, CallbackInfo ci) {
        if (!colorwheel$tintResolved) {
            colorwheel$tintResolved = true;
            if (blockPos != null) {
                Integer stored = ClientColorData.get(blockPos.x, blockPos.y, blockPos.z);
                if (stored != null) {
                    int blend = ColorUtil.tint(stored);
                    colorwheel$tintR = ((blend >> 16) & 0xFF) / 255.0F;
                    colorwheel$tintG = ((blend >> 8) & 0xFF) / 255.0F;
                    colorwheel$tintB = (blend & 0xFF) / 255.0F;
                }
            }
        }
        vr *= colorwheel$tintR;
        vg *= colorwheel$tintG;
        vb *= colorwheel$tintB;
    }
}
