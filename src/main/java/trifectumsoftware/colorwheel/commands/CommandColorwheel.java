package trifectumsoftware.colorwheel.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;

import trifectumsoftware.colorwheel.network.PacketColorClear;
import trifectumsoftware.colorwheel.network.PacketHandler;
import trifectumsoftware.colorwheel.storage.ColorDataManager;

public class CommandColorwheel extends CommandBase {

    @Override
    public String getCommandName() {
        return "colorwheel";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/colorwheel clear [all]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1 || !"clear".equalsIgnoreCase(args[0])) {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        boolean all = args.length >= 2 && "all".equalsIgnoreCase(args[1]);
        MinecraftServer server = MinecraftServer.getServer();
        List<WorldServer> worlds = new ArrayList<>();
        if (all) {
            for (WorldServer world : server.worldServers) {
                worlds.add(world);
            }
        } else {
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            WorldServer world = server.worldServerForDimension(player.dimension);
            if (world != null) {
                worlds.add(world);
            }
        }

        int removed = 0;
        Set<Integer> dimensions = new HashSet<>();
        for (WorldServer world : worlds) {
            removed += ColorDataManager.clearAll(world);
            dimensions.add(world.provider.dimensionId);
        }

        for (EntityPlayerMP player : server.getConfigurationManager().playerEntityList) {
            if (all || dimensions.contains(player.dimension)) {
                PacketHandler.network.sendTo(new PacketColorClear(), player);
            }
        }

        sender.addChatMessage(
            new ChatComponentText(
                "Removed " + removed
                    + " colored block"
                    + (removed == 1 ? "" : "s")
                    + (all ? " from all dimensions." : ".")));
    }
}
