package trifectumsoftware.colorwheel.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import trifectumsoftware.colorwheel.Config;
import trifectumsoftware.colorwheel.energy.ColorEnergy;
import trifectumsoftware.colorwheel.items.ItemColorTool;
import trifectumsoftware.colorwheel.items.ItemColoredBlock;
import trifectumsoftware.colorwheel.logic.ColorToolLogic;
import trifectumsoftware.colorwheel.network.PacketChunkColors;
import trifectumsoftware.colorwheel.network.PacketEnergySync;
import trifectumsoftware.colorwheel.network.PacketHandler;
import trifectumsoftware.colorwheel.storage.ColorDataManager;
import trifectumsoftware.colorwheel.storage.IntIntMap;

public class ColorwheelEventHandler {

    private static final Queue<Runnable> SERVER_TASKS = new ConcurrentLinkedQueue<>();
    private static final int ENERGY_SYNC_INTERVAL = 10;

    private final Map<String, Integer> lastSyncedEnergy = new HashMap<>();
    private int energySyncTimer;

    public static void schedule(Runnable task) {
        SERVER_TASKS.add(task);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Runnable task;
        while ((task = SERVER_TASKS.poll()) != null) {
            task.run();
        }
        if (++energySyncTimer >= ENERGY_SYNC_INTERVAL) {
            energySyncTimer = 0;
            syncHeldToolEnergy(MinecraftServer.getServer());
        }
    }

    private void syncHeldToolEnergy(MinecraftServer server) {
        if (server == null) {
            return;
        }
        if (!ColorEnergy.hasSystem()) {
            lastSyncedEnergy.clear();
            return;
        }
        List<?> players = server.getConfigurationManager().playerEntityList;
        for (Object entry : players) {
            EntityPlayerMP player = (EntityPlayerMP) entry;
            String key = player.getUniqueID()
                .toString();
            ItemStack stack = player.getCurrentEquippedItem();
            if (stack == null || !(stack.getItem() instanceof ItemColorTool)) {
                lastSyncedEnergy.remove(key);
                continue;
            }
            int energy = ColorEnergy.getEnergy(stack);
            Integer last = lastSyncedEnergy.get(key);
            if (last == null || last != energy) {
                lastSyncedEnergy.put(key, energy);
                PacketHandler.network.sendTo(new PacketEnergySync(energy), player);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        String key = event.player.getUniqueID()
            .toString();
        lastSyncedEnergy.remove(key);
        ColorToolLogic.clearAreaSelection(key);
    }

    public static void migrateLegacyWorlds() {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            return;
        }
        for (WorldServer world : server.worldServers) {
            ColorDataManager.migrateLegacy(world);
        }
    }

    @SubscribeEvent
    public void onChunkWatch(ChunkWatchEvent.Watch event) {
        EntityPlayerMP player = event.player;
        ChunkCoordIntPair chunk = event.chunk;
        IntIntMap colors = ColorDataManager.getChunkColors(player.worldObj, chunk.chunkXPos, chunk.chunkZPos);
        if (colors != null && !colors.isEmpty()) {
            PacketHandler.network.sendTo(new PacketChunkColors(chunk.chunkXPos, chunk.chunkZPos, colors), player);
        }
    }

    @SubscribeEvent
    public void onChunkUnWatch(ChunkWatchEvent.UnWatch event) {
        EntityPlayerMP player = event.player;
        ChunkCoordIntPair chunk = event.chunk;
        IntIntMap colors = ColorDataManager.getChunkColors(player.worldObj, chunk.chunkXPos, chunk.chunkZPos);
        if (colors != null && !colors.isEmpty()) {

            PacketHandler.network.sendTo(PacketChunkColors.clearing(chunk.chunkXPos, chunk.chunkZPos), player);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!Config.clearColorOnBreak || event.world.isRemote) {
            return;
        }
        EntityPlayer player = event.getPlayer();
        if (Config.dropColoredBlocks && player != null && !player.capabilities.isCreativeMode) {

            final World world = event.world;
            final int x = event.x;
            final int y = event.y;
            final int z = event.z;
            schedule(() -> {
                if (world.blockExists(x, y, z) && world.isAirBlock(x, y, z)
                    && ColorDataManager.removeBlockColor(world, x, y, z)) {
                    PacketHandler.broadcastColorUpdate(world, x, y, z, 0, true);
                }
            });
            return;
        }
        if (ColorDataManager.removeBlockColor(event.world, event.x, event.y, event.z)) {
            PacketHandler.broadcastColorUpdate(event.world, event.x, event.y, event.z, 0, true);
        }
    }

    @SubscribeEvent
    public void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        if (event.world.isRemote) {
            return;
        }
        Integer color = ColorDataManager.getBlockColor(event.world, event.x, event.y, event.z);
        if (color == null) {
            return;
        }
        boolean converted = false;
        if (Config.dropColoredBlocks) {
            ItemStack colored = ItemColoredBlock.create(event.block, event.blockMetadata, color);
            if (colored != null) {
                event.drops.clear();
                event.drops.add(colored);
                event.dropChance = 1.0F;
                converted = true;
            }
        }
        if (converted || Config.clearColorOnBreak) {
            ColorDataManager.removeBlockColor(event.world, event.x, event.y, event.z);
            PacketHandler.broadcastColorUpdate(event.world, event.x, event.y, event.z, 0, true);
        }
    }
}
