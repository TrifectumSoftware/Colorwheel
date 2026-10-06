package trifectumsoftware.colorwheel.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import trifectumsoftware.colorwheel.client.ClientAreaSelection;

public class PacketAreaCorner implements IMessage {

    private int x;
    private int y;
    private int z;
    private boolean clear;

    public PacketAreaCorner() {}

    public PacketAreaCorner(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static PacketAreaCorner clearing() {
        PacketAreaCorner message = new PacketAreaCorner();
        message.clear = true;
        return message;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        clear = buf.readBoolean();
        if (!clear) {
            x = buf.readInt();
            y = buf.readInt();
            z = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(clear);
        if (!clear) {
            buf.writeInt(x);
            buf.writeInt(y);
            buf.writeInt(z);
        }
    }

    public static class Handler implements IMessageHandler<PacketAreaCorner, IMessage> {

        @Override
        public IMessage onMessage(PacketAreaCorner message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                if (message.clear) {
                    ClientAreaSelection.clear();
                } else {
                    ClientAreaSelection.set(message.x, message.y, message.z);
                }
            }
            return null;
        }
    }
}
