package trifectumsoftware.colorwheel.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import trifectumsoftware.colorwheel.client.ClientColorData;
import trifectumsoftware.colorwheel.client.ClientColorRefresher;

public class PacketColorBatch implements IMessage {

    private static final int MAX_INTS = 16384 * 5;

    private int[] data;

    public PacketColorBatch() {}

    public PacketColorBatch(int[] data) {
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int length = buf.readInt();
        if (length < 0 || length > MAX_INTS || length % 5 != 0) {
            length = 0;
        }
        data = new int[length];
        for (int i = 0; i < length; i++) {
            data[i] = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(data.length);
        for (int value : data) {
            buf.writeInt(value);
        }
    }

    public static class Handler implements IMessageHandler<PacketColorBatch, IMessage> {

        @Override
        public IMessage onMessage(PacketColorBatch message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                int[] data = message.data;
                for (int i = 0; i + 4 < data.length; i += 5) {
                    if (data[i + 3] != 0) {
                        ClientColorData.remove(data[i], data[i + 1], data[i + 2]);
                    } else {
                        ClientColorData.set(data[i], data[i + 1], data[i + 2], data[i + 4]);
                    }
                }
                if (data.length > 0) {
                    ClientColorRefresher.refreshBlocks(data);
                }
            }
            return null;
        }
    }
}
