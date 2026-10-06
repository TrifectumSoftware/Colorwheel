package trifectumsoftware.colorwheel.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import trifectumsoftware.colorwheel.items.ItemColoredBlock;
import trifectumsoftware.colorwheel.storage.ColorUtil;

public class ColoredBlockItemRenderer implements IItemRenderer {

    private final RenderBlocks fallbackRenderBlocks = new RenderBlocks();

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        if (!(item.getItem() instanceof ItemColoredBlock)) {
            return false;
        }
        switch (type) {
            case ENTITY:
            case EQUIPPED:
            case EQUIPPED_FIRST_PERSON:
                return true;
            case INVENTORY:

                return is3d(item);
            default:
                return false;
        }
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        boolean threeD = is3d(item);
        switch (type) {
            case ENTITY:

                return helper != ItemRendererHelper.BLOCK_3D || threeD;
            case EQUIPPED:
            case EQUIPPED_FIRST_PERSON:
                return threeD && helper == ItemRendererHelper.EQUIPPED_BLOCK;
            case INVENTORY:
                return helper == ItemRendererHelper.INVENTORY_BLOCK;
            default:
                return false;
        }
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        Block block = ItemColoredBlock.getStoredBlock(item);
        RenderBlocks renderBlocks = data.length > 0 && data[0] instanceof RenderBlocks ? (RenderBlocks) data[0]
            : fallbackRenderBlocks;
        int tint = ColorUtil.tint(ItemColoredBlock.getStoredColor(item));
        float red = ((tint >> 16) & 0xFF) / 255.0F;
        float green = ((tint >> 8) & 0xFF) / 255.0F;
        float blue = (tint & 0xFF) / 255.0F;

        GL11.glPushMatrix();
        GL11.glColor4f(red, green, blue, 1.0F);
        if (is3d(item)) {
            if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {

                GL11.glTranslatef(0.5F, 0.5F, 0.5F);
            }
            boolean oldInventoryTint = renderBlocks.useInventoryTint;
            renderBlocks.useInventoryTint = false;
            try {
                renderBlocks.renderBlockAsItem(block, ItemColoredBlock.getStoredMeta(item), 1.0F);
            } finally {
                renderBlocks.useInventoryTint = oldInventoryTint;
            }
        } else {
            renderFlatIcon(item, type);
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }

    private static boolean is3d(ItemStack item) {
        return RenderBlocks.renderItemIn3d(
            ItemColoredBlock.getStoredBlock(item)
                .getRenderType());
    }

    private static void renderFlatIcon(ItemStack item, ItemRenderType type) {
        IIcon icon = item.getIconIndex();
        if (icon == null) {
            return;
        }
        if (type == ItemRenderType.ENTITY) {
            GL11.glTranslatef(0.0F, -0.3F, 0.0F);
            GL11.glScalef(1.5F, 1.5F, 1.5F);
            GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
        }
        ItemRenderer.renderItemIn2D(
            Tessellator.instance,
            icon.getMaxU(),
            icon.getMinV(),
            icon.getMinU(),
            icon.getMaxV(),
            icon.getIconWidth(),
            icon.getIconHeight(),
            0.0625F);
    }
}
