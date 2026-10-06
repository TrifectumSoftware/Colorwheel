package trifectumsoftware.colorwheel.network;

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import trifectumsoftware.colorwheel.events.ColorwheelEventHandler;
import trifectumsoftware.colorwheel.logic.ColorToolLogic;

public class PacketUndoRedo implements IMessage {

    private boolean redo;

    public PacketUndoRedo() {}

    public PacketUndoRedo(boolean redo) {
        this.redo = redo;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        redo = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(redo);
    }

    public static class Handler implements IMessageHandler<PacketUndoRedo, IMessage> {

        @Override
        public IMessage onMessage(PacketUndoRedo message, MessageContext ctx) {
            if (ctx.side.isServer()) {
                final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
                if (player != null) {
                    final boolean redo = message.redo;
                    ColorwheelEventHandler.schedule(() -> ColorToolLogic.undoRedo(player, redo));
                }
            }
            return null;
        }
    }
}
