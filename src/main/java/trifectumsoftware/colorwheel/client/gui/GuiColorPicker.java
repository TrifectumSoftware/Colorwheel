package trifectumsoftware.colorwheel.client.gui;

import java.awt.Color;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import trifectumsoftware.colorwheel.items.ItemColorTool;
import trifectumsoftware.colorwheel.network.PacketHandler;
import trifectumsoftware.colorwheel.network.PacketSetToolData;
import trifectumsoftware.colorwheel.storage.ColorUtil;

public class GuiColorPicker extends GuiColorwheelScreen {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(
        "colorwheel",
        "textures/gui/color_picker.png");
    private static final int CONTENT_WIDTH = 252;
    private static final int CONTENT_HEIGHT = 168;

    private static final int HUE_X = 6;
    private static final int HUE_Y = 7;
    private static final int HUE_WIDTH = 126;
    private static final int HUE_HEIGHT = 121;

    private static final int SV_X = 148;
    private static final int SV_Y = 7;
    private static final int SV_WIDTH = 86;
    private static final int SV_HEIGHT = 86;

    private static final int ALPHA_X = 148;
    private static final int ALPHA_Y = 97;
    private static final int ALPHA_WIDTH = 86;
    private static final int ALPHA_HEIGHT = 18;

    private static final int PREVIEW_X = 148;
    private static final int PREVIEW_Y = 117;
    private static final int PREVIEW_WIDTH = 86;
    private static final int PREVIEW_HEIGHT = 18;

    private static final int HEX_X = 34;
    private static final int HEX_Y = 143;
    private static final int HEX_WIDTH = 80;
    private static final int HEX_HEIGHT = 14;

    private static final int CANCEL_X = 158;
    private static final int CANCEL_Y = 145;
    private static final int APPLY_X = 208;
    private static final int APPLY_Y = 145;
    private static final int BUTTON_SIZE = 18;

    private static final int GRADIENT_X = 183;
    private static final int GRADIENT_Y = 145;
    private static final int GRADIENT_SIZE = 18;

    public static GuiScreen returnTo;

    private final ItemStack tool;
    private float hue;
    private float saturation;
    private float value;
    private int alpha;
    private String hexInput = "";
    private boolean hexFocused;
    private boolean hexReplaceOnType = true;
    private int dragMode;

    public GuiColorPicker(ItemStack tool) {
        this.tool = tool;
        int argb = ItemColorTool.getEditColor(tool);
        float[] hsv = Color.RGBtoHSB(ColorUtil.getRed(argb), ColorUtil.getGreen(argb), ColorUtil.getBlue(argb), null);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
        this.alpha = Math.max(1, argb >>> 24);
        updateHexFromColor();
    }

    private int guiLeft() {
        return (width - CONTENT_WIDTH) / 2;
    }

    private int guiTop() {
        return (height - CONTENT_HEIGHT) / 2;
    }

    private int selectedRgb() {
        return Color.HSBtoRGB(hue, saturation, value) & 0xFFFFFF;
    }

    private int selectedArgb() {
        return (alpha << 24) | selectedRgb();
    }

    private void updateHexFromColor() {
        hexInput = String.format("%08X", selectedArgb());
        hexReplaceOnType = true;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int left = guiLeft();
        int top = guiTop();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(BACKGROUND);
        drawTexturedModalRect(left, top, 0, 0, 256, 256);

        drawHueArea(left, top);
        drawSaturationValueSquare(left, top);
        drawAlphaBar(left, top);
        drawPreview(left, top);
        drawHexField(left, top);
    }

    private void drawHueArea(int left, int top) {
        final int x = left + HUE_X;
        final int y = top + HUE_Y;
        drawUntextured(() -> {
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawing(GL11.GL_QUAD_STRIP);
            int segments = 64;
            for (int i = 0; i <= segments; i++) {
                float hueAt = i / (float) segments;
                int rgb = Color.HSBtoRGB(hueAt, 1.0F, 1.0F);
                float vertexX = x + hueAt * HUE_WIDTH;
                tessellator.setColorOpaque_F(
                    ColorUtil.getRed(rgb) / 255.0F,
                    ColorUtil.getGreen(rgb) / 255.0F,
                    ColorUtil.getBlue(rgb) / 255.0F);
                tessellator.addVertex(vertexX, y, zLevel);
                tessellator.addVertex(vertexX, y + HUE_HEIGHT, zLevel);
            }
            tessellator.draw();
        });

        int markerX = Math.min((int) (x + hue * HUE_WIDTH), x + HUE_WIDTH - 1);
        drawRect(markerX - 1, y, markerX + 2, y + HUE_HEIGHT, 0xFF000000);
        drawRect(markerX, y, markerX + 1, y + HUE_HEIGHT, 0xFFFFFFFF);
    }

    private void drawSaturationValueSquare(int left, int top) {
        final int x = left + SV_X;
        final int y = top + SV_Y;
        final int pure = Color.HSBtoRGB(hue, 1.0F, 1.0F);
        drawUntextured(() -> {
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
            tessellator.addVertex(x, y, zLevel);
            tessellator.setColorOpaque_F(
                ColorUtil.getRed(pure) / 255.0F,
                ColorUtil.getGreen(pure) / 255.0F,
                ColorUtil.getBlue(pure) / 255.0F);
            tessellator.addVertex(x + SV_WIDTH, y, zLevel);
            tessellator.setColorOpaque_F(0.0F, 0.0F, 0.0F);
            tessellator.addVertex(x + SV_WIDTH, y + SV_HEIGHT, zLevel);
            tessellator.setColorOpaque_F(0.0F, 0.0F, 0.0F);
            tessellator.addVertex(x, y + SV_HEIGHT, zLevel);
            tessellator.draw();
        });

        int markerX = (int) (x + saturation * SV_WIDTH);
        int markerY = (int) (y + (1.0F - value) * SV_HEIGHT);
        drawRect(markerX - 2, markerY - 2, markerX + 3, markerY + 3, 0xFF000000);
        drawRect(markerX - 1, markerY - 1, markerX + 2, markerY + 2, 0xFFFFFFFF);
    }

    private void drawAlphaBar(int left, int top) {
        final int x = left + ALPHA_X;
        final int y = top + ALPHA_Y;

        drawCheckerboard(x, y, ALPHA_WIDTH, ALPHA_HEIGHT);
        final int rgb = selectedRgb();
        final float r = ColorUtil.getRed(rgb) / 255.0F;
        final float g = ColorUtil.getGreen(rgb) / 255.0F;
        final float b = ColorUtil.getBlue(rgb) / 255.0F;
        drawUntextured(() -> {
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA_F(r, g, b, 0.0F);
            tessellator.addVertex(x, y, zLevel);
            tessellator.addVertex(x, y + ALPHA_HEIGHT, zLevel);
            tessellator.setColorRGBA_F(r, g, b, 1.0F);
            tessellator.addVertex(x + ALPHA_WIDTH, y + ALPHA_HEIGHT, zLevel);
            tessellator.addVertex(x + ALPHA_WIDTH, y, zLevel);
            tessellator.draw();
        });

        int handleX = x + (int) (((alpha - 1) / 254.0F) * (ALPHA_WIDTH - 1));
        drawRect(handleX - 1, y - 1, handleX + 2, y + ALPHA_HEIGHT + 1, 0xFF000000);
        drawRect(handleX, y - 1, handleX + 1, y + ALPHA_HEIGHT + 1, 0xFFFFFFFF);
    }

    private void drawPreview(int left, int top) {
        int x = left + PREVIEW_X;
        int y = top + PREVIEW_Y;
        drawCheckerboard(x, y, PREVIEW_WIDTH, PREVIEW_HEIGHT);
        drawRect(x, y, x + PREVIEW_WIDTH, y + PREVIEW_HEIGHT, selectedArgb());
    }

    private void drawCheckerboard(int x, int y, int width, int height) {
        for (int cy = 0; cy < height; cy += 6) {
            for (int cx = 0; cx < width; cx += 6) {
                int color = (((cx / 6) + (cy / 6)) & 1) == 0 ? 0xFF585858 : 0xFF8A8A8A;
                drawRect(x + cx, y + cy, x + Math.min(cx + 6, width), y + Math.min(cy + 6, height), color);
            }
        }
    }

    private void drawHexField(int left, int top) {
        int x = left + HEX_X;
        int y = top + HEX_Y;
        String text = "#" + hexInput;
        int centerX = x + HEX_WIDTH / 2;
        int textX = centerX - fontRendererObj.getStringWidth(text) / 2;
        fontRendererObj.drawString(text, textX, y + 3, hexFocused ? 0xFFFFFF : 0xC0C0C0);
        if (hexFocused) {
            drawRect(x - 1, y - 1, x + HEX_WIDTH + 1, y, 0xFF909090);
            drawRect(x - 1, y + HEX_HEIGHT, x + HEX_WIDTH + 1, y + HEX_HEIGHT + 1, 0xFF909090);
            drawRect(x - 1, y, x, y + HEX_HEIGHT, 0xFF909090);
            drawRect(x + HEX_WIDTH, y, x + HEX_WIDTH + 1, y + HEX_HEIGHT, 0xFF909090);
            if (System.currentTimeMillis() % 1000 < 500) {
                int caretX = textX + fontRendererObj.getStringWidth(text) + 1;
                drawRect(caretX, y + 2, caretX + 1, y + HEX_HEIGHT - 2, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public void onGuiClosed() {

        returnTo = null;
        super.onGuiClosed();
    }

    private void applyColor() {
        int argb = selectedArgb();
        ItemColorTool.setEditColor(tool, argb);

        PacketHandler.network.sendToServer(PacketSetToolData.fromStack(tool));
        GuiScreen back = returnTo;
        returnTo = null;
        mc.displayGuiScreen(back);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0) {
            int left = guiLeft();
            int top = guiTop();
            if (inRect(mouseX, mouseY, left + APPLY_X, top + APPLY_Y, BUTTON_SIZE, BUTTON_SIZE)) {
                applyColor();
                return;
            }
            if (inRect(mouseX, mouseY, left + CANCEL_X, top + CANCEL_Y, BUTTON_SIZE, BUTTON_SIZE)) {
                mc.displayGuiScreen(null);
                return;
            }
            if (inRect(mouseX, mouseY, left + HUE_X, top + HUE_Y, HUE_WIDTH, HUE_HEIGHT)) {
                hexFocused = false;
                dragMode = 1;
                updateHue(mouseX);
                return;
            }
            if (inRect(mouseX, mouseY, left + SV_X, top + SV_Y, SV_WIDTH, SV_HEIGHT)) {
                hexFocused = false;
                dragMode = 2;
                updateSaturationValue(mouseX, mouseY);
                return;
            }
            if (inRect(mouseX, mouseY, left + ALPHA_X, top + ALPHA_Y - 2, ALPHA_WIDTH, ALPHA_HEIGHT + 4)) {
                hexFocused = false;
                dragMode = 3;
                updateAlpha(mouseX);
                return;
            }
            if (inRect(mouseX, mouseY, left + HEX_X, top + HEX_Y, HEX_WIDTH, HEX_HEIGHT)) {
                hexFocused = true;
                updateHexFromColor();
                return;
            }
            if (inRect(mouseX, mouseY, left + GRADIENT_X, top + GRADIENT_Y, GRADIENT_SIZE, GRADIENT_SIZE)) {
                hexFocused = false;
                mc.displayGuiScreen(new GuiGradientEditor(tool));
                return;
            }
            hexFocused = false;
            updateHexFromColor();
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int lastButton, long timeSinceLastClick) {
        if (dragMode == 1) {
            updateHue(mouseX);
        } else if (dragMode == 2) {
            updateSaturationValue(mouseX, mouseY);
        } else if (dragMode == 3) {
            updateAlpha(mouseX);
        }
        super.mouseClickMove(mouseX, mouseY, lastButton, timeSinceLastClick);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int button) {
        if (button >= 0) {
            dragMode = 0;
        }
        super.mouseMovedOrUp(mouseX, mouseY, button);
    }

    private void updateHue(int mouseX) {
        hue = clamp((mouseX - (guiLeft() + HUE_X)) / (float) HUE_WIDTH);
        updateHexFromColor();
    }

    private void updateSaturationValue(int mouseX, int mouseY) {
        int x = guiLeft() + SV_X;
        int y = guiTop() + SV_Y;
        saturation = clamp((mouseX - x) / (float) SV_WIDTH);
        value = 1.0F - clamp((mouseY - y) / (float) SV_HEIGHT);
        updateHexFromColor();
    }

    private void updateAlpha(int mouseX) {
        float fraction = clamp((mouseX - (guiLeft() + ALPHA_X)) / (float) (ALPHA_WIDTH - 1));
        alpha = 1 + Math.round(fraction * 254.0F);
        updateHexFromColor();
    }

    private void commitHex() {
        try {
            if (hexInput.length() == 6) {
                int rgb = Integer.parseInt(hexInput, 16);
                float[] hsv = Color
                    .RGBtoHSB(ColorUtil.getRed(rgb), ColorUtil.getGreen(rgb), ColorUtil.getBlue(rgb), null);
                hue = hsv[0];
                saturation = hsv[1];
                value = hsv[2];
            } else if (hexInput.length() == 8) {
                int argb = (int) Long.parseLong(hexInput, 16);
                float[] hsv = Color
                    .RGBtoHSB(ColorUtil.getRed(argb), ColorUtil.getGreen(argb), ColorUtil.getBlue(argb), null);
                hue = hsv[0];
                saturation = hsv[1];
                value = hsv[2];
                alpha = Math.max(1, argb >>> 24);
            }
        } catch (NumberFormatException ignored) {

        }
        hexFocused = false;
        updateHexFromColor();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (hexFocused) {
            if (keyCode == 1) {
                hexFocused = false;
                updateHexFromColor();
                return;
            }
            if (keyCode == 28) {
                commitHex();
                return;
            }
            if (keyCode == 14) {
                if (hexInput.length() > 0) {
                    hexInput = hexInput.substring(0, hexInput.length() - 1);
                }
                return;
            }
            char digit = Character.toUpperCase(typedChar);
            if ((digit >= '0' && digit <= '9') || (digit >= 'A' && digit <= 'F')) {
                if (hexReplaceOnType) {
                    hexInput = "";
                    hexReplaceOnType = false;
                }
                if (hexInput.length() < 8) {
                    hexInput += digit;
                }
            }
            return;
        }
        if (keyCode == 28) {
            applyColor();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    private static float clamp(float v) {
        if (v < 0.0F) {
            return 0.0F;
        }
        return Math.min(v, 1.0F);
    }
}
