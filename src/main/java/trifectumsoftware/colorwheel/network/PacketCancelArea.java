package trifectumsoftware.colorwheel.network;

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import trifectumsoftware.colorwheel.events.ColorwheelEventHandler;
import trifectumsoftware.colorwheel.logic.ColorToolLogic;

public class PacketCancelArea implements IMessage {

    public PacketCancelArea() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<PacketCancelArea, IMessage> {

        @Override
        public IMessage onMessage(PacketCancelArea message, MessageContext ctx) {
            if (ctx.side.isServer()) {
                final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
                if (player != null) {
                    ColorwheelEventHandler.schedule(() -> ColorToolLogic.clearAreaSelection(player, true));
                }
            }
            return null;
        }
    }
}
