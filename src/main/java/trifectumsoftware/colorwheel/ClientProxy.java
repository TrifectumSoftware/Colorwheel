package trifectumsoftware.colorwheel;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import trifectumsoftware.colorwheel.client.ClientEventHandler;
import trifectumsoftware.colorwheel.client.ColorwheelKeys;
import trifectumsoftware.colorwheel.client.gui.GuiColorPicker;
import trifectumsoftware.colorwheel.client.render.AreaSelectionRenderer;
import trifectumsoftware.colorwheel.client.render.ColoredBlockItemRenderer;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ClientEventHandler handler = new ClientEventHandler();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance()
            .bus()
            .register(handler);
        MinecraftForge.EVENT_BUS.register(new AreaSelectionRenderer());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientRegistry.registerKeyBinding(ColorwheelKeys.UNDO);
        ClientRegistry.registerKeyBinding(ColorwheelKeys.REDO);
        ClientRegistry.registerKeyBinding(ColorwheelKeys.GRADIENT);
        ClientRegistry.registerKeyBinding(ColorwheelKeys.GRADIENT_EDITOR);
        if (Colorwheel.coloredBlock != null) {
            MinecraftForgeClient.registerItemRenderer(Colorwheel.coloredBlock, new ColoredBlockItemRenderer());
        }
    }

    @Override
    public void openColorPicker(ItemStack tool) {
        Minecraft.getMinecraft()
            .displayGuiScreen(new GuiColorPicker(tool));
    }
}
