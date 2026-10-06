package trifectumsoftware.colorwheel.client.gui;

import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.opengl.GL11;

public class GuiColorwheelScreen extends GuiScreen {

    protected void drawUntextured(Runnable body) {
        boolean texture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        try {
            body.run();
        } finally {
            GL11.glShadeModel(GL11.GL_FLAT);
            setCapability(GL11.GL_BLEND, blend);
            setCapability(GL11.GL_DEPTH_TEST, depth);
            setCapability(GL11.GL_CULL_FACE, cull);
            setCapability(GL11.GL_LIGHTING, lighting);
            setCapability(GL11.GL_TEXTURE_2D, texture);
        }
    }

    public static void setCapability(int capability, boolean enabled) {
        if (enabled) {
            GL11.glEnable(capability);
        } else {
            GL11.glDisable(capability);
        }
    }

    public static boolean inRect(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
