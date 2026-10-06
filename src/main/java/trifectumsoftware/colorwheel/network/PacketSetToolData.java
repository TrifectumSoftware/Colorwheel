package trifectumsoftware.colorwheel.network;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import trifectumsoftware.colorwheel.events.ColorwheelEventHandler;
import trifectumsoftware.colorwheel.items.ItemColorTool;

public class PacketSetToolData implements IMessage {

    private int colorA;
    private int mode;
    private int gradient;
    private int gradientMode;
    private int stopCount;
    private float[] stopPositions;
    private int[] stopColors;

    public PacketSetToolData() {}

    public static PacketSetToolData fromStack(ItemStack stack) {
        PacketSetToolData message = new PacketSetToolData();
        message.colorA = ItemColorTool.getColor(stack);
        message.mode = ItemColorTool.getMode(stack);
        message.gradient = ItemColorTool.getGradient(stack);
        message.gradientMode = ItemColorTool.getGradientMode(stack);
        List<ItemColorTool.GradientStop> stops = ItemColorTool.getStops(stack);
        message.stopCount = stops.size();
        message.stopPositions = new float[stops.size()];
        message.stopColors = new int[stops.size()];
        for (int i = 0; i < stops.size(); i++) {
            message.stopPositions[i] = stops.get(i).pos;
            message.stopColors[i] = stops.get(i).color;
        }
        return message;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        colorA = buf.readInt();
        mode = buf.readInt();
        gradient = buf.readInt();
        gradientMode = buf.readInt();
        stopCount = buf.readInt();
        stopPositions = new float[stopCount];
        stopColors = new int[stopCount];
        for (int i = 0; i < stopCount; i++) {
            stopPositions[i] = buf.readFloat();
            stopColors[i] = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(colorA);
        buf.writeInt(mode);
        buf.writeInt(gradient);
        buf.writeInt(gradientMode);
        buf.writeInt(stopCount);
        for (int i = 0; i < stopCount; i++) {
            buf.writeFloat(stopPositions[i]);
            buf.writeInt(stopColors[i]);
        }
    }

    public static class Handler implements IMessageHandler<PacketSetToolData, IMessage> {

        @Override
        public IMessage onMessage(PacketSetToolData message, MessageContext ctx) {
            if (ctx.side.isServer()) {
                EntityPlayerMP player = ctx.getServerHandler().playerEntity;
                if (player != null) {
                    final int colorA = message.colorA;
                    final int mode = message.mode;
                    final int gradient = message.gradient;
                    final int gradientMode = message.gradientMode;
                    final int stopCount = message.stopCount;
                    final float[] stopPositions = message.stopPositions;
                    final int[] stopColors = message.stopColors;

                    ColorwheelEventHandler.schedule(() -> {
                        ItemStack stack = player.inventory.getCurrentItem();
                        if (stack != null && stack.getItem() instanceof ItemColorTool) {
                            ItemColorTool.setColor(stack, colorA);
                            ItemColorTool.setMode(stack, mode);
                            ItemColorTool.setGradient(stack, gradient);
                            ItemColorTool.setGradientMode(stack, gradientMode);
                            List<ItemColorTool.GradientStop> stops = new java.util.ArrayList<>();
                            for (int i = 0; i < stopCount; i++) {
                                stops.add(new ItemColorTool.GradientStop(stopPositions[i], stopColors[i]));
                            }
                            ItemColorTool.setStops(stack, stops);
                        }
                    });
                }
            }
            return null;
        }
    }
}
