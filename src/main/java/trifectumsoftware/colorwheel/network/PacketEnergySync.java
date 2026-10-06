package trifectumsoftware.colorwheel.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import trifectumsoftware.colorwheel.client.ClientEnergySync;

public class PacketEnergySync implements IMessage {

    private int energy;

    public PacketEnergySync() {}

    public PacketEnergySync(int energy) {
        this.energy = energy;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        energy = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(energy);
    }

    public static class Handler implements IMessageHandler<PacketEnergySync, IMessage> {

        @Override
        public IMessage onMessage(PacketEnergySync message, MessageContext ctx) {
            if (ctx.side.isClient()) {

                ClientEnergySync.apply(message.energy);
            }
            return null;
        }
    }
}
