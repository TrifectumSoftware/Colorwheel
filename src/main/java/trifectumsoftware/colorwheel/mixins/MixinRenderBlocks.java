package trifectumsoftware.colorwheel.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockStem;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import trifectumsoftware.colorwheel.client.ClientColorData;

@Mixin(RenderBlocks.class)
public class MixinRenderBlocks {

    @Redirect(
        method = "renderStandardBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;colorMultiplier(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int colorwheel$standardBlock(Block block, IBlockAccess world, int x, int y, int z) {
        return ClientColorData.resolve(block, world, x, y, z);
    }

    @Redirect(
        method = "renderBlockVine",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;colorMultiplier(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int colorwheel$vine(Block block, IBlockAccess world, int x, int y, int z) {
        return ClientColorData.resolve(block, world, x, y, z);
    }

    @Redirect(
        method = "renderBlockStainedGlassPane",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;colorMultiplier(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int colorwheel$stainedGlassPane(Block block, IBlockAccess world, int x, int y, int z) {
        return ClientColorData.resolve(block, world, x, y, z);
    }

    @Redirect(
        method = "renderCrossedSquares",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;colorMultiplier(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int colorwheel$crossedSquares(Block block, IBlockAccess world, int x, int y, int z) {
        return ClientColorData.resolve(block, world, x, y, z);
    }

    @Redirect(
        method = "renderBlockLiquid",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;colorMultiplier(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int colorwheel$liquid(Block block, IBlockAccess world, int x, int y, int z) {
        return ClientColorData.resolve(block, world, x, y, z);
    }

    @Redirect(
        method = "renderBlockCactus",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;colorMultiplier(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int colorwheel$cactus(Block block, IBlockAccess world, int x, int y, int z) {
        return ClientColorData.resolve(block, world, x, y, z);
    }

    @Redirect(
        method = "renderBlockFlowerpot",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;colorMultiplier(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int colorwheel$flowerpotPlant(Block block, IBlockAccess world, int x, int y, int z) {
        return ClientColorData.resolve(block, world, x, y, z);
    }

    @Redirect(
        method = "renderBlockBrewingStand",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockBrewingStand;colorMultiplier(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int colorwheel$brewingStand(BlockBrewingStand block, IBlockAccess world, int x, int y, int z) {
        return ClientColorData.resolve(block, world, x, y, z);
    }

    @Redirect(
        method = "renderBlockCauldron",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockCauldron;colorMultiplier(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int colorwheel$cauldron(BlockCauldron block, IBlockAccess world, int x, int y, int z) {
        return ClientColorData.resolve(block, world, x, y, z);
    }

    @Redirect(
        method = "renderBlockFlowerpot",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockFlowerPot;colorMultiplier(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int colorwheel$flowerpot(BlockFlowerPot block, IBlockAccess world, int x, int y, int z) {
        return ClientColorData.resolve(block, world, x, y, z);
    }

    @Redirect(
        method = "renderBlockAnvilMetadata",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockAnvil;colorMultiplier(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int colorwheel$anvil(BlockAnvil block, IBlockAccess world, int x, int y, int z) {
        return ClientColorData.resolve(block, world, x, y, z);
    }

    @Redirect(
        method = "renderBlockPane",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockPane;colorMultiplier(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int colorwheel$pane(BlockPane block, IBlockAccess world, int x, int y, int z) {
        return ClientColorData.resolve(block, world, x, y, z);
    }

    @Redirect(
        method = "renderBlockDoublePlant",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockDoublePlant;colorMultiplier(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int colorwheel$doublePlant(BlockDoublePlant block, IBlockAccess world, int x, int y, int z) {
        return ClientColorData.resolve(block, world, x, y, z);
    }

    @Redirect(
        method = "renderBlockStem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockStem;colorMultiplier(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int colorwheel$stem(BlockStem block, IBlockAccess world, int x, int y, int z) {
        return ClientColorData.resolve(block, world, x, y, z);
    }

    @Redirect(
        method = "renderBlockHopper",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockHopper;colorMultiplier(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int colorwheel$hopper(BlockHopper block, IBlockAccess world, int x, int y, int z) {
        return ClientColorData.resolve(block, world, x, y, z);
    }

    @Redirect(
        method = "renderBlockHopperMetadata",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockHopper;colorMultiplier(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int colorwheel$hopperMetadata(BlockHopper block, IBlockAccess world, int x, int y, int z) {
        return ClientColorData.resolve(block, world, x, y, z);
    }
}
