package trifectumsoftware.colorwheel.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import org.lwjgl.input.Mouse;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import trifectumsoftware.colorwheel.client.gui.GuiGradientEditor;
import trifectumsoftware.colorwheel.items.ItemColorTool;
import trifectumsoftware.colorwheel.network.PacketCancelArea;
import trifectumsoftware.colorwheel.network.PacketHandler;
import trifectumsoftware.colorwheel.network.PacketSetToolData;
import trifectumsoftware.colorwheel.network.PacketUndoRedo;

public class ClientEventHandler {

    private boolean lastUndo;
    private boolean lastRedo;
    private boolean lastGradient;
    private boolean lastEditor;
    private boolean lastAttack;

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        ClientColorData.clear();
        ClientAreaSelection.clear();
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {

        if (event.world.isRemote && event.entity instanceof EntityPlayerSP) {
            ClientColorData.clear();
            ClientAreaSelection.clear();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP player = mc.thePlayer;
        boolean held = player != null && isTool(player.getCurrentEquippedItem());

        boolean undo = ColorwheelKeys.UNDO.getIsKeyPressed();
        if (undo && !lastUndo && held) {
            PacketHandler.network.sendToServer(new PacketUndoRedo(false));
        }
        lastUndo = undo;

        boolean redo = ColorwheelKeys.REDO.getIsKeyPressed();
        if (redo && !lastRedo && held) {
            PacketHandler.network.sendToServer(new PacketUndoRedo(true));
        }
        lastRedo = redo;

        boolean gradient = ColorwheelKeys.GRADIENT.getIsKeyPressed();
        if (gradient && !lastGradient && held) {
            ItemStack stack = player.getCurrentEquippedItem();
            boolean on = ItemColorTool.getGradient(stack) == ItemColorTool.GRADIENT_OFF;
            ItemColorTool.setGradient(stack, on ? ItemColorTool.GRADIENT_FIRST : ItemColorTool.GRADIENT_OFF);
            PacketHandler.network.sendToServer(PacketSetToolData.fromStack(stack));
            player.addChatMessage(
                new ChatComponentText(
                    StatCollector.translateToLocal("item.colorwheel.colorTool.gradient") + " "
                        + (on ? "on" : StatCollector.translateToLocal("item.colorwheel.colorTool.gradientOff"))));
            player.playSound("random.click", 0.3F, 1.5F);
        }
        lastGradient = gradient;

        boolean editor = ColorwheelKeys.GRADIENT_EDITOR.getIsKeyPressed();
        if (editor && !lastEditor && held) {
            mc.displayGuiScreen(new GuiGradientEditor(player.getCurrentEquippedItem()));
        }
        lastEditor = editor;

        boolean attack = mc.currentScreen == null && Mouse.isButtonDown(0);
        if (attack && !lastAttack && held) {
            ItemStack stack = player.getCurrentEquippedItem();
            if (ItemColorTool.getMode(stack) == ItemColorTool.MODE_AREA && ClientAreaSelection.isActive()) {
                PacketHandler.network.sendToServer(new PacketCancelArea());
                ClientAreaSelection.clear();
                player.playSound("random.click", 0.3F, 0.8F);
            }
        }
        lastAttack = attack;
    }

    private static boolean isTool(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemColorTool;
    }
}
