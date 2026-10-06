package trifectumsoftware.colorwheel.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import trifectumsoftware.colorwheel.client.ClientAreaSelection;
import trifectumsoftware.colorwheel.client.gui.GuiColorwheelScreen;
import trifectumsoftware.colorwheel.items.ItemColorTool;
import trifectumsoftware.colorwheel.storage.ColorUtil;

public class AreaSelectionRenderer {

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null || !ClientAreaSelection.isActive()) {
            return;
        }
        ItemStack held = mc.thePlayer.getCurrentEquippedItem();
        if (held == null || !(held.getItem() instanceof ItemColorTool)
            || ItemColorTool.getMode(held) != ItemColorTool.MODE_AREA) {
            return;
        }

        int ax = ClientAreaSelection.getX();
        int ay = ClientAreaSelection.getY();
        int az = ClientAreaSelection.getZ();
        int bx = ax;
        int by = ay;
        int bz = az;

        MovingObjectPosition target = ItemColorTool.traceLookedAtBlock(mc.theWorld, mc.thePlayer);
        if (target != null && target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            bx = target.blockX;
            by = target.blockY;
            bz = target.blockZ;
        }

        int color;
        if (mc.thePlayer.isSneaking()) {
            color = (System.currentTimeMillis() / 250L) % 2L == 0L ? 0xFF3030 : 0xFFD800;
        } else {
            color = ColorUtil.tint(ItemColorTool.getColor(held));
        }
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        double px = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * event.partialTicks;
        double py = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * event.partialTicks;
        double pz = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * event.partialTicks;

        boolean depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean textureEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean lightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);

        GL11.glPushMatrix();
        GL11.glTranslated(-px, -py, -pz);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(4.0F);
        GL11.glColor4f(red, green, blue, 1.0F);
        drawBox(
            Math.min(ax, bx),
            Math.min(ay, by),
            Math.min(az, bz),
            Math.max(ax, bx) + 1.0D,
            Math.max(ay, by) + 1.0D,
            Math.max(az, bz) + 1.0D);
        GL11.glLineWidth(1.0F);
        GuiColorwheelScreen.setCapability(GL11.GL_BLEND, blendEnabled);
        GuiColorwheelScreen.setCapability(GL11.GL_DEPTH_TEST, depthEnabled);
        GuiColorwheelScreen.setCapability(GL11.GL_LIGHTING, lightingEnabled);
        GuiColorwheelScreen.setCapability(GL11.GL_TEXTURE_2D, textureEnabled);
        GL11.glPopMatrix();
    }

    private static void drawBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        GL11.glBegin(GL11.GL_LINES);

        vertex(x1, y1, z1);
        vertex(x2, y1, z1);
        vertex(x2, y1, z1);
        vertex(x2, y1, z2);
        vertex(x2, y1, z2);
        vertex(x1, y1, z2);
        vertex(x1, y1, z2);
        vertex(x1, y1, z1);

        vertex(x1, y2, z1);
        vertex(x2, y2, z1);
        vertex(x2, y2, z1);
        vertex(x2, y2, z2);
        vertex(x2, y2, z2);
        vertex(x1, y2, z2);
        vertex(x1, y2, z2);
        vertex(x1, y2, z1);

        vertex(x1, y1, z1);
        vertex(x1, y2, z1);
        vertex(x2, y1, z1);
        vertex(x2, y2, z1);
        vertex(x2, y1, z2);
        vertex(x2, y2, z2);
        vertex(x1, y1, z2);
        vertex(x1, y2, z2);
        GL11.glEnd();
    }

    private static void vertex(double x, double y, double z) {
        GL11.glVertex3d(x, y, z);
    }
}
