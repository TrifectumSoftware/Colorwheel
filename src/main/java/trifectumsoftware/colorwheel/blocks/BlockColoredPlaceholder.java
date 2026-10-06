package trifectumsoftware.colorwheel.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockColoredPlaceholder extends Block {

    public BlockColoredPlaceholder() {
        super(Material.rock);
        setBlockName("colorwheel.colored_block_placeholder");
        setBlockTextureName("stone");
        setCreativeTab(null);
        setHardness(-1.0F);
        setResistance(6000000.0F);
        setStepSound(soundTypeStone);
    }
}
