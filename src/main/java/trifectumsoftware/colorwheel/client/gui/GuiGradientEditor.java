package trifectumsoftware.colorwheel.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import trifectumsoftware.colorwheel.items.ItemColorTool;
import trifectumsoftware.colorwheel.logic.ColorToolLogic;
import trifectumsoftware.colorwheel.network.PacketHandler;
import trifectumsoftware.colorwheel.network.PacketSetToolData;

public class GuiGradientEditor extends GuiColorwheelScreen {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(
        "colorwheel",
        "textures/gui/gradient_editor.png");
    private static final int CONTENT_WIDTH = 252;
    private static final int CONTENT_HEIGHT = 168;

    private static final int BAR_X = 11;
    private static final int BAR_Y = 17;
    private static final int BAR_W = 230;
    private static final int BAR_H = 22;

    private static final int BLEND_X = 10;
    private static final int BLEND_Y = 48;
    private static final int BLEND_W = 150;
    private static final int BLEND_H = 14;
    private static final int RESET_X = 164;
    private static final int RESET_Y = 48;
    private static final int RESET_W = 78;
    private static final int RESET_H = 14;

    private static final int PREVIEW_X = 10;
    private static final int PREVIEW_Y = 70;
    private static final int PREVIEW_W = 20;
    private static final int PREVIEW_H = 20;
    private static final int INFO_X = 34;
    private static final int INFO_Y = 70;
    private static final int INFO_W = 208;
    private static final int INFO_H = 20;

    private static final int CANCEL_X = 200;
    private static final int CANCEL_Y = 145;
    private static final int APPLY_X = 224;
    private static final int APPLY_Y = 145;
    private static final int BUTTON_SIZE = 18;

    private static final int MARKER_HIT = 6;

    private final ItemStack tool;
    private int selectedStop;
    private int dragStop = -1;
    private int lastMouseX;
    private int lastMouseY;

    private final List<ItemColorTool.GradientStop> originalStops;
    private final int originalMode;
    private final int originalGradient;

    public GuiGradientEditor(ItemStack tool) {
        this.tool = tool;
        this.selectedStop = ItemColorTool.getSelectedStop(tool);
        this.originalMode = ItemColorTool.getGradientMode(tool);
        this.originalGradient = ItemColorTool.getGradient(tool);
        this.originalStops = copyStops(ItemColorTool.getStops(tool));

        if (originalGradient == ItemColorTool.GRADIENT_OFF) {
            ItemColorTool.setGradient(tool, ItemColorTool.GRADIENT_FIRST);
            sync();
        }
    }

    private static List<ItemColorTool.GradientStop> copyStops(List<ItemColorTool.GradientStop> stops) {
        List<ItemColorTool.GradientStop> copy = new ArrayList<>();
        for (ItemColorTool.GradientStop stop : stops) {
            copy.add(new ItemColorTool.GradientStop(stop.pos, stop.color));
        }
        return copy;
    }

    private void sync() {
        PacketHandler.network.sendToServer(PacketSetToolData.fromStack(tool));
    }

    private List<ItemColorTool.GradientStop> stops() {
        return ItemColorTool.getStops(tool);
    }

    private int guiLeft() {
        return (width - CONTENT_WIDTH) / 2;
    }

    private int guiTop() {
        return (height - CONTENT_HEIGHT) / 2;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        drawDefaultBackground();
        int left = guiLeft();
        int top = guiTop();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(BACKGROUND);
        drawTexturedModalRect(left, top, 0, 0, 256, 256);

        drawGradientBar(left, top);
        drawStopMarkers(left, top);
        drawBlendButton(left, top);
        drawResetButton(left, top);
        drawStopInfo(left, top);
    }

    private void drawGradientBar(int left, int top) {
        final int x = left + BAR_X;
        final int y = top + BAR_Y;
        final List<ItemColorTool.GradientStop> stops = stops();
        final int mode = ItemColorTool.getGradientMode(tool);
        drawUntextured(() -> {
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawing(GL11.GL_QUAD_STRIP);
            int segments = 96;
            for (int i = 0; i <= segments; i++) {
                float pos = i / (float) segments;
                int rgb = ColorToolLogic.sampleGradient(stops, pos, mode) & 0xFFFFFF;
                tessellator.setColorOpaque_F(
                    ((rgb >> 16) & 0xFF) / 255.0F,
                    ((rgb >> 8) & 0xFF) / 255.0F,
                    (rgb & 0xFF) / 255.0F);
                float vertexX = x + pos * BAR_W;
                tessellator.addVertex(vertexX, y, zLevel);
                tessellator.addVertex(vertexX, y + BAR_H, zLevel);
            }
            tessellator.draw();
        });
    }

    private void drawStopMarkers(int left, int top) {
        int x = left + BAR_X;
        int y = top + BAR_Y;
        List<ItemColorTool.GradientStop> stops = stops();
        for (int i = 0; i < stops.size(); i++) {
            int markerX = x + Math.round(stops.get(i).pos * (BAR_W - 1));
            boolean selected = i == selectedStop;
            int stopRgb = stops.get(i).color & 0xFFFFFF;

            boolean light = ((stopRgb >> 16) & 0xFF) > 200 && ((stopRgb >> 8) & 0xFF) > 200 && (stopRgb & 0xFF) > 200;
            int fill = selected && light ? 0xFF000000 | (~stopRgb & 0xFFFFFF) : 0xFF000000 | stopRgb;
            int outline = selected ? 0xFFFFFFFF : 0xFF000000;
            int centerY = y + BAR_H / 2;

            drawRect(markerX - 1, y, markerX + 2, y + BAR_H, 0xFF000000);
            drawRect(markerX, y, markerX + 1, y + BAR_H, fill);
            drawRect(markerX - 4, centerY - 4, markerX + 5, centerY + 5, outline);
            drawRect(markerX - 3, centerY - 3, markerX + 4, centerY + 4, 0xFF000000);
            drawRect(markerX - 2, centerY - 2, markerX + 3, centerY + 3, fill);
        }
    }

    private void drawBlendButton(int left, int top) {
        String label = StatCollector.translateToLocal("colorwheel.gui.blend") + " "
            + ItemColorTool.gradientModeName(ItemColorTool.getGradientMode(tool));
        drawWellButton(left, top, BLEND_X, BLEND_Y, BLEND_W, BLEND_H, label);
    }

    private void drawResetButton(int left, int top) {
        drawWellButton(
            left,
            top,
            RESET_X,
            RESET_Y,
            RESET_W,
            RESET_H,
            StatCollector.translateToLocal("colorwheel.gui.reset"));
    }

    private void drawWellButton(int left, int top, int x, int y, int w, int h, String label) {
        int px = left + x;
        int py = top + y;
        boolean hovered = inRect(lastMouseX, lastMouseY, px, py, w, h);
        if (hovered) {
            drawRect(px + 1, py + 1, px + w - 1, py + h - 1, 0x30FFFFFF);
        }
        fontRendererObj.drawString(
            label,
            px + (w - fontRendererObj.getStringWidth(label)) / 2,
            py + (h - 8) / 2,
            hovered ? 0xFFFFFF : 0xE0E0E0);
    }

    private void drawStopInfo(int left, int top) {
        int px = left + PREVIEW_X;
        int py = top + PREVIEW_Y;
        List<ItemColorTool.GradientStop> stops = stops();
        int index = Math.max(0, Math.min(selectedStop, stops.size() - 1));
        int color = index < stops.size() ? stops.get(index).color : 0xFFFFFF;

        drawRect(px + 1, py + 1, px + PREVIEW_W - 1, py + PREVIEW_H - 1, color);
        if (index < stops.size()) {
            ItemColorTool.GradientStop stop = stops.get(index);
            String text = "#" + index
                + "   "
                + String.format("%.2f", stop.pos)
                + "   "
                + String.format("#%08X", stop.color);
            int ix = left + INFO_X;
            int iy = top + INFO_Y;
            fontRendererObj.drawString(
                text,
                ix + (INFO_W - fontRendererObj.getStringWidth(text)) / 2,
                iy + (INFO_H - 8) / 2,
                0xFFFFFF);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int left = guiLeft();
        int top = guiTop();
        if (inRect(mouseX, mouseY, left + APPLY_X, top + APPLY_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            applyAndClose();
            return;
        }
        if (inRect(mouseX, mouseY, left + CANCEL_X, top + CANCEL_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            cancelAndClose();
            return;
        }
        if (inRect(mouseX, mouseY, left + BLEND_X, top + BLEND_Y, BLEND_W, BLEND_H)) {
            ItemColorTool.cycleGradientMode(tool);
            sync();
            mc.thePlayer.playSound("random.click", 0.3F, 1.5F);
            return;
        }
        if (inRect(mouseX, mouseY, left + RESET_X, top + RESET_Y, RESET_W, RESET_H)) {
            List<ItemColorTool.GradientStop> stops = new ArrayList<>();
            stops.add(new ItemColorTool.GradientStop(0.0F, ItemColorTool.getColor(tool)));
            ItemColorTool.setStops(tool, stops);
            ItemColorTool.setSelectedStop(tool, 0);
            selectedStop = 0;
            sync();
            mc.thePlayer.playSound("random.click", 0.3F, 1.0F);
            return;
        }

        if (inRect(mouseX, mouseY, left + PREVIEW_X, top + PREVIEW_Y, PREVIEW_W, PREVIEW_H)
            || inRect(mouseX, mouseY, left + INFO_X, top + INFO_Y, INFO_W, INFO_H)) {
            GuiColorPicker.returnTo = this;
            mc.displayGuiScreen(new GuiColorPicker(tool));
            return;
        }
        if (inRect(mouseX, mouseY, left + BAR_X - MARKER_HIT, top + BAR_Y - 7, BAR_W + MARKER_HIT * 2, BAR_H + 9)) {
            handleBarClick(mouseX, mouseY, button, left, top);
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleBarClick(int mouseX, int mouseY, int button, int left, int top) {
        int barX = left + BAR_X;
        int relX = mouseX - barX;
        List<ItemColorTool.GradientStop> stops = stops();

        int closest = -1;
        int closestDist = MARKER_HIT + 1;
        for (int i = 0; i < stops.size(); i++) {
            int markerX = Math.round(stops.get(i).pos * (BAR_W - 1));
            int dist = Math.abs(relX - markerX);
            if (dist < closestDist) {
                closestDist = dist;
                closest = i;
            }
        }
        if (button == 0) {
            if (closest >= 0) {
                selectedStop = closest;
                ItemColorTool.setSelectedStop(tool, closest);
                dragStop = closest;
            } else {

                float pos = Math.max(0.0F, Math.min(1.0F, relX / (float) (BAR_W - 1)));
                int mode = ItemColorTool.getGradientMode(tool);
                int color = ColorToolLogic.sampleGradient(stops, pos, mode);
                ItemColorTool.GradientStop added = new ItemColorTool.GradientStop(pos, color);
                stops.add(added);
                stops.sort((a, b) -> Float.compare(a.pos, b.pos));
                ItemColorTool.setStops(tool, stops);
                selectedStop = stops.indexOf(added);
                ItemColorTool.setSelectedStop(tool, selectedStop);
                sync();
                mc.thePlayer.playSound("random.click", 0.3F, 1.5F);
            }
        } else if (button == 1 && closest >= 0 && stops.size() > 1) {
            stops.remove(closest);
            ItemColorTool.setStops(tool, stops);
            selectedStop = Math.max(0, Math.min(closest, stops.size() - 1));
            ItemColorTool.setSelectedStop(tool, selectedStop);
            sync();
            mc.thePlayer.playSound("random.click", 0.3F, 0.8F);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLastClick) {
        if (dragStop >= 0) {
            int relX = mouseX - (guiLeft() + BAR_X);
            float pos = Math.max(0.0F, Math.min(1.0F, relX / (float) (BAR_W - 1)));
            List<ItemColorTool.GradientStop> stops = stops();
            if (dragStop < stops.size()) {
                ItemColorTool.GradientStop moved = stops.get(dragStop);
                moved.pos = pos;
                ItemColorTool.setStops(tool, stops);
            }
        }
        super.mouseClickMove(mouseX, mouseY, button, timeSinceLastClick);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int button) {
        if (button >= 0 && dragStop >= 0) {
            dragStop = -1;

            List<ItemColorTool.GradientStop> stops = stops();
            ItemColorTool.GradientStop selected = selectedStop >= 0 && selectedStop < stops.size()
                ? stops.get(selectedStop)
                : null;
            stops.sort((a, b) -> Float.compare(a.pos, b.pos));
            ItemColorTool.setStops(tool, stops);
            if (selected != null) {
                selectedStop = stops.indexOf(selected);
                ItemColorTool.setSelectedStop(tool, selectedStop);
            }
            sync();
        }
        super.mouseMovedOrUp(mouseX, mouseY, button);
    }

    private void applyAndClose() {
        sync();
        mc.displayGuiScreen(null);
    }

    private void cancelAndClose() {
        ItemColorTool.setStops(tool, originalStops);
        ItemColorTool.setGradientMode(tool, originalMode);
        ItemColorTool.setGradient(tool, originalGradient);
        ItemColorTool.setSelectedStop(tool, 0);
        sync();
        mc.displayGuiScreen(null);
    }
}
