package trifectumsoftware.colorwheel.items;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import trifectumsoftware.colorwheel.Colorwheel;
import trifectumsoftware.colorwheel.Config;
import trifectumsoftware.colorwheel.network.PacketHandler;
import trifectumsoftware.colorwheel.storage.ColorDataManager;
import trifectumsoftware.colorwheel.storage.ColorUtil;

public class ItemColoredBlock extends ItemBlock {

    private static final String TAG_BLOCK = "ColorwheelBlock";
    private static final String TAG_META = "ColorwheelMeta";
    private static final String TAG_COLOR = "ColorwheelColor";

    public ItemColoredBlock(Block placeholder) {
        super(placeholder);
        setUnlocalizedName("colorwheel.coloredBlock");
        setMaxStackSize(64);
    }

    public static ItemStack create(Block block, int meta, int color) {
        if (Item.getItemFromBlock(block) == null) {
            return null;
        }
        String name = Block.blockRegistry.getNameForObject(block);
        ItemStack stack = new ItemStack(Colorwheel.coloredBlock);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(TAG_BLOCK, name);
        tag.setInteger(TAG_META, meta);
        tag.setInteger(TAG_COLOR, color);
        stack.setTagCompound(tag);
        return stack;
    }

    public static Block getStoredBlock(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound()
            .hasKey(TAG_BLOCK)) {
            Block block = Block.getBlockFromName(
                stack.getTagCompound()
                    .getString(TAG_BLOCK));
            if (block != null) {
                return block;
            }
        }
        return Blocks.stone;
    }

    public static int getStoredMeta(ItemStack stack) {
        if (stack.hasTagCompound()) {
            return Math.max(
                0,
                stack.getTagCompound()
                    .getInteger(TAG_META));
        }
        return 0;
    }

    public static int getStoredColor(ItemStack stack) {
        int color = 0xFFFFFF;
        if (stack.hasTagCompound() && stack.getTagCompound()
            .hasKey(TAG_COLOR)) {
            color = stack.getTagCompound()
                .getInteger(TAG_COLOR);
        }
        if ((color >>> 24) == 0) {

            color |= 0xFF000000;
        }
        return color;
    }

    public static ItemStack getMimic(ItemStack stack) {
        return new ItemStack(getStoredBlock(stack), 1, getStoredMeta(stack));
    }

    @Override
    public String getUnlocalizedName() {
        return "colorwheel.coloredBlock";
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "colorwheel.coloredBlock";
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector
            .translateToLocalFormatted("item.colorwheel.coloredBlock.name", getMimic(stack).getDisplayName());
    }

    @Override
    public CreativeTabs getCreativeTab() {
        return CreativeTabs.tabTools;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        if (tab == CreativeTabs.tabTools) {
            list.add(new ItemStack(this));
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        int color = getStoredColor(stack);
        list.add(
            EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.colorwheel.coloredBlock.color")
                + " "
                + EnumChatFormatting.WHITE
                + String.format("#%08X", color));
        int alpha = color >>> 24;
        if (alpha >= 255) {
            return;
        }
        if (GuiScreen.isShiftKeyDown()) {
            list.add(
                EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("item.colorwheel.coloredBlock.strength")
                    + " "
                    + Math.round(alpha * 100.0F / 255.0F)
                    + "%");
        } else {
            ItemColorTool.addMoreInfoHint(list);
        }
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (!Config.placeColoredBlocks || stack.stackSize <= 0) {
            return false;
        }
        Block block = getStoredBlock(stack);
        ItemStack mimic = getMimic(stack);

        Block clicked = world.getBlock(x, y, z);
        if (clicked == Blocks.snow_layer && (world.getBlockMetadata(x, y, z) & 7) < 1) {
            side = 1;
        } else if (clicked != Blocks.vine && clicked != Blocks.tallgrass
            && clicked != Blocks.deadbush
            && !clicked.isReplaceable(world, x, y, z)) {
                x += Facing.offsetsXForSide[side];
                y += Facing.offsetsYForSide[side];
                z += Facing.offsetsZForSide[side];
            }
        if (y < 0 || y > 255) {
            return false;
        }
        if (!player.canPlayerEdit(x, y, z, side, stack)) {
            return false;
        }
        if (!world.canPlaceEntityOnSide(block, x, y, z, false, side, player, mimic)) {
            return false;
        }
        int meta = getStoredMeta(stack);
        int placedMeta = block.onBlockPlaced(world, x, y, z, side, hitX, hitY, hitZ, meta);
        if (!placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, placedMeta)) {
            return false;
        }
        world.playSoundEffect(
            x + 0.5F,
            y + 0.5F,
            z + 0.5F,
            block.stepSound.func_150496_b(),
            (block.stepSound.getVolume() + 1.0F) / 2.0F,
            block.stepSound.getPitch() * 0.8F);
        stack.stackSize--;
        return true;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ, int metadata) {
        Block block = getStoredBlock(stack);
        if (!world.setBlock(x, y, z, block, metadata, 3)) {
            return false;
        }
        if (world.getBlock(x, y, z) == block) {
            block.onBlockPlacedBy(world, x, y, z, player, stack);
            block.onPostBlockPlaced(world, x, y, z, metadata);
        }
        if (!world.isRemote) {
            int color = getStoredColor(stack);
            ColorDataManager.setBlockColor(world, x, y, z, color);
            PacketHandler.broadcastColorUpdate(world, x, y, z, color, false);
        }
        return true;
    }

    @Override
    public int getSpriteNumber() {
        return 0;
    }

    @Override
    public void registerIcons(net.minecraft.client.renderer.texture.IIconRegister register) {

    }

    @Override
    public IIcon getIconFromDamage(int meta) {
        return Blocks.stone.getIcon(0, 0);
    }

    @Override
    public IIcon getIconIndex(ItemStack stack) {
        return getIcon(stack, 0);
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass) {
        ItemStack mimic = getMimic(stack);
        if (mimic == null) {
            return getIconFromDamage(0);
        }
        Item mimicItem = mimic.getItem();
        return mimicItem != null ? mimicItem.getIcon(mimic, pass) : getIconFromDamage(0);
    }

    @Override
    public int getColorFromItemStack(ItemStack stack, int pass) {
        return ColorUtil.tint(getStoredColor(stack));
    }
}
