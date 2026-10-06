package trifectumsoftware.colorwheel;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import trifectumsoftware.colorwheel.blocks.BlockColoredPlaceholder;
import trifectumsoftware.colorwheel.energy.ColorEnergy;
import trifectumsoftware.colorwheel.events.ColorwheelEventHandler;
import trifectumsoftware.colorwheel.items.ItemColorTool;
import trifectumsoftware.colorwheel.items.ItemColorToolEU;
import trifectumsoftware.colorwheel.items.ItemColorToolEnergy;
import trifectumsoftware.colorwheel.items.ItemColorToolNTM;
import trifectumsoftware.colorwheel.items.ItemColoredBlock;
import trifectumsoftware.colorwheel.network.PacketHandler;

@Mod(modid = Colorwheel.MODID, version = Tags.VERSION, name = "Colorwheel", acceptedMinecraftVersions = "[1.7.10]")
public class Colorwheel {

    public static final String MODID = "colorwheel";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "trifectumsoftware.colorwheel.ClientProxy",
        serverSide = "trifectumsoftware.colorwheel.CommonProxy")
    public static CommonProxy proxy;

    public static ItemColorTool colorTool;
    public static ItemColoredBlock coloredBlock;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);

        colorTool = createColorTool();
        GameRegistry.registerItem(colorTool, "color_tool");

        Block placeholder = new BlockColoredPlaceholder();
        GameRegistry.registerBlock(placeholder, ItemColoredBlock.class, "colored_block");
        coloredBlock = (ItemColoredBlock) Item.getItemFromBlock(placeholder);

        PacketHandler.init();
        ColorwheelEventHandler eventHandler = new ColorwheelEventHandler();
        MinecraftForge.EVENT_BUS.register(eventHandler);
        FMLCommonHandler.instance()
            .bus()
            .register(eventHandler);

        LOG.info("Colorwheel " + Tags.VERSION + " loaded. Time to paint!");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ModRecipes.register(new ItemStack(colorTool));
    }

    private static ItemColorTool createColorTool() {
        if (ColorEnergy.EU_PRESENT) {
            ItemColorTool tool = tryCreate("GT/IC2", ItemColorToolEU.class);
            if (tool != null) {
                return tool;
            }
        }
        if (ColorEnergy.HE_PRESENT) {
            ItemColorTool tool = tryCreate("NTM", ItemColorToolNTM.class);
            if (tool != null) {
                return tool;
            }
        }
        if (ColorEnergy.RF_PRESENT) {
            ItemColorTool tool = tryCreate("Redstone Flux", ItemColorToolEnergy.class);
            if (tool != null) {
                return tool;
            }
        }
        return new ItemColorTool();
    }

    private static ItemColorTool tryCreate(String name, Class<? extends ItemColorTool> type) {
        try {
            ItemColorTool tool = type.getDeclaredConstructor()
                .newInstance();
            LOG.info("Colorwheel: " + name + " charging enabled for the Color Tool.");
            return tool;
        } catch (Throwable t) {
            LOG.warn("Colorwheel: " + name + " charging could not be enabled, continuing without it.", t);
            return null;
        }
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new trifectumsoftware.colorwheel.commands.CommandColorwheel());
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        ColorwheelEventHandler.migrateLegacyWorlds();
    }
}
