package trifectumsoftware.colorwheel.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import trifectumsoftware.colorwheel.client.ClientColorData;
import trifectumsoftware.colorwheel.client.ClientColorRefresher;
import trifectumsoftware.colorwheel.storage.IntIntMap;

public class PacketChunkColors implements IMessage {

    private static final int MAX_ENTRIES = 65536;

    private int chunkX;
    private int chunkZ;
    private boolean clear;
    private int[] data;

    public PacketChunkColors() {}

    public PacketChunkColors(int chunkX, int chunkZ, IntIntMap colors) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.data = new int[colors.size() * 2];
        int[] index = { 0 };
        colors.forEach((local, color) -> {
            data[index[0]++] = local;
            data[index[0]++] = color;
        });
    }

    private PacketChunkColors(int chunkX, int chunkZ, boolean clear) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.clear = clear;
        this.data = new int[0];
    }

    public static PacketChunkColors clearing(int chunkX, int chunkZ) {
        return new PacketChunkColors(chunkX, chunkZ, true);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        clear = buf.readBoolean();
        int length = buf.readInt();
        if (length < 0 || length > MAX_ENTRIES * 2 || (length & 1) != 0) {
            length = 0;
        }
        data = new int[length];
        for (int i = 0; i < length; i++) {
            data[i] = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeBoolean(clear);
        buf.writeInt(data.length);
        for (int value : data) {
            buf.writeInt(value);
        }
    }

    public static class Handler implements IMessageHandler<PacketChunkColors, IMessage> {

        @Override
        public IMessage onMessage(PacketChunkColors message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                if (message.clear) {
                    ClientColorData.clearChunk(message.chunkX, message.chunkZ);
                } else {
                    for (int i = 0; i + 1 < message.data.length; i += 2) {
                        ClientColorData.setLocal(message.chunkX, message.chunkZ, message.data[i], message.data[i + 1]);
                    }
                    if (message.data.length > 0) {
                        ClientColorRefresher.refreshChunk(message.chunkX, message.chunkZ);
                    }
                }
            }
            return null;
        }
    }
}
