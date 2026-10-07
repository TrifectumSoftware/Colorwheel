package trifectumsoftware.colorwheel.mixins;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.RenderBlockFluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import trifectumsoftware.colorwheel.client.ClientColorData;

@Mixin(RenderBlockFluid.class)
public class MixinRenderBlockFluid {

    @Redirect(
        require = 0,
        method = "renderWorldBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;colorMultiplier(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int colorwheel$fluid(Block block, IBlockAccess world, int x, int y, int z) {
        return ClientColorData.resolve(block, world, x, y, z);
    }
}
