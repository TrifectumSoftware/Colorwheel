package trifectumsoftware.colorwheel.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import trifectumsoftware.colorwheel.client.ClientColorData;
import trifectumsoftware.colorwheel.client.ClientColorRefresher;

public class PacketColorClear implements IMessage {

    public PacketColorClear() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<PacketColorClear, IMessage> {

        @Override
        public IMessage onMessage(PacketColorClear message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                ClientColorData.clear();
                ClientColorRefresher.reloadAll();
            }
            return null;
        }
    }
}
