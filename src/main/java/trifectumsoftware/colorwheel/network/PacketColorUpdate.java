package trifectumsoftware.colorwheel.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import trifectumsoftware.colorwheel.client.ClientColorData;
import trifectumsoftware.colorwheel.client.ClientColorRefresher;

public class PacketColorUpdate implements IMessage {

    private int x;
    private int y;
    private int z;
    private int color;
    private boolean remove;

    public PacketColorUpdate() {}

    public PacketColorUpdate(int x, int y, int z, int color, boolean remove) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
        this.remove = remove;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        color = buf.readInt();
        remove = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(color);
        buf.writeBoolean(remove);
    }

    public static class Handler implements IMessageHandler<PacketColorUpdate, IMessage> {

        @Override
        public IMessage onMessage(PacketColorUpdate message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                if (message.remove) {
                    ClientColorData.remove(message.x, message.y, message.z);
                } else {
                    ClientColorData.set(message.x, message.y, message.z, message.color);
                }
                ClientColorRefresher.refreshBlock(message.x, message.y, message.z);
            }
            return null;
        }
    }
}
