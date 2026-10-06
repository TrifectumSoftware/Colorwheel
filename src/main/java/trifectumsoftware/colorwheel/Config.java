package trifectumsoftware.colorwheel;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static int defaultColor = 0xFFFFFF;
    public static boolean clearColorOnBreak = true;
    public static boolean dropColoredBlocks = true;
    public static boolean placeColoredBlocks = true;

    public static int maxAreaBlocks = 8192;

    public static int paintReach = 128;

    public static boolean useEnergy = false;
    public static int energyCapacity = 1_000_000;
    public static int energyPerBlock = 50;
    public static int energyTier = 1;
    public static int rfPerHE = 5;
    public static int rfPerEU = 4;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        defaultColor = configuration.getInt(
            "defaultColor",
            "general",
            defaultColor,
            0,
            0xFFFFFF,
            "Color a freshly crafted Color Tool starts with, as RGB (e.g. 16711680 for red).");
        clearColorOnBreak = configuration.getBoolean(
            "clearColorOnBreak",
            "general",
            clearColorOnBreak,
            "Remove the stored color when the block at a painted position is broken.");
        dropColoredBlocks = configuration.getBoolean(
            "dropColoredBlocks",
            "general",
            dropColoredBlocks,
            "Breaking a painted block drops it as a portable Colored Block item that can be placed again with its color. Set to false to keep the vanilla drops only.");
        placeColoredBlocks = configuration.getBoolean(
            "placeColoredBlocks",
            "general",
            placeColoredBlocks,
            "Allow portable Colored Block items to be placed back down with their color. Set to false to turn the portable form into a collectible that cannot be placed.");
        maxAreaBlocks = configuration.getInt(
            "maxAreaBlocks",
            "general",
            maxAreaBlocks,
            1,
            Integer.MAX_VALUE,
            "Maximum number of blocks an Area-mode selection may fill in one go.");
        paintReach = configuration.getInt(
            "paintReach",
            "general",
            paintReach,
            5,
            512,
            "How far away (in blocks) a block can be painted or selected. The block is picked along your line of sight.");

        useEnergy = configuration.getBoolean(
            "useEnergy",
            "energy",
            useEnergy,
            "Require energy to paint. Only takes effect when a supported energy mod (Redstone Flux, NTM or GT/IC2) is installed.");
        energyCapacity = configuration.getInt(
            "energyCapacity",
            "energy",
            energyCapacity,
            1,
            Integer.MAX_VALUE,
            "Tool energy buffer size, in RF.");
        energyPerBlock = configuration.getInt(
            "energyPerBlock",
            "energy",
            energyPerBlock,
            0,
            Integer.MAX_VALUE,
            "Energy used per block painted or cleared, in RF. 0 means free.");
        energyTier = configuration.getInt(
            "energyTier",
            "energy",
            energyTier,
            1,
            4,
            "GT/IC2 voltage tier the tool charges at (1=LV up to 4=EV).");
        rfPerHE = configuration.getInt(
            "rfPerHE",
            "energy",
            rfPerHE,
            1,
            Integer.MAX_VALUE,
            "RF gained per 1 HE charged (NTM compatibility).");
        rfPerEU = configuration.getInt(
            "rfPerEU",
            "energy",
            rfPerEU,
            1,
            Integer.MAX_VALUE,
            "RF gained per 1 EU charged (GT/IC2 compatibility).");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
