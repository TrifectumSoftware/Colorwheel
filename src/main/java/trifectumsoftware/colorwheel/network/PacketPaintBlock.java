package trifectumsoftware.colorwheel.network;

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import trifectumsoftware.colorwheel.events.ColorwheelEventHandler;
import trifectumsoftware.colorwheel.logic.ColorToolLogic;

public class PacketPaintBlock implements IMessage {

    private int x;
    private int y;
    private int z;
    private int side;
    private boolean remove;

    public PacketPaintBlock() {}

    public PacketPaintBlock(int x, int y, int z, int side, boolean remove) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.side = side;
        this.remove = remove;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        side = buf.readInt();
        remove = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(side);
        buf.writeBoolean(remove);
    }

    public static class Handler implements IMessageHandler<PacketPaintBlock, IMessage> {

        @Override
        public IMessage onMessage(PacketPaintBlock message, MessageContext ctx) {
            if (ctx.side.isServer()) {
                final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
                if (player != null) {
                    final int x = message.x;
                    final int y = message.y;
                    final int z = message.z;
                    final int side = message.side;
                    final boolean remove = message.remove;

                    ColorwheelEventHandler.schedule(() -> ColorToolLogic.paint(player, x, y, z, side, remove));
                }
            }
            return null;
        }
    }
}
