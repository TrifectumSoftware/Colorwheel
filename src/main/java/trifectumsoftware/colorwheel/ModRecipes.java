package trifectumsoftware.colorwheel;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import cpw.mods.fml.common.registry.GameRegistry;
import trifectumsoftware.colorwheel.energy.ColorEnergy;

public final class ModRecipes {

    private ModRecipes() {}

    public static void register(ItemStack result) {
        String themed = "";
        if (ColorEnergy.EU_PRESENT && addIC2Recipe(result)) {
            themed += " IC2";
        }
        if (ColorEnergy.classExists("gregtech.api.enums.ItemList") && addGregTechRecipe(result)) {
            themed += " GregTech";
        }
        if (ColorEnergy.HE_PRESENT && addNTMRecipe(result)) {
            themed += " NTM";
        }
        if (themed.isEmpty()) {
            GameRegistry.addRecipe(new ShapedOreRecipe(result, "D", "S", 'D', "dye", 'S', "stickWood"));
            themed = " base";
        }
        Colorwheel.LOG.info("Colorwheel: crafting recipes registered (" + themed.trim() + ")");
    }

    private static boolean addIC2Recipe(ItemStack result) {
        ItemStack circuit = ic2Item("electronicCircuit");
        ItemStack coil = ic2Item("coil");
        ItemStack battery = ic2Item("reBattery");
        if (circuit == null || coil == null || battery == null) {
            return false;
        }
        GameRegistry.addRecipe(new ShapedOreRecipe(result, "C", "c", "B", 'C', circuit, 'c', coil, 'B', battery));
        return true;
    }

    private static boolean addGregTechRecipe(ItemStack result) {
        ItemStack circuit = gregTechItem("Circuit_Basic");
        ItemStack battery = gregTechItem("Battery_RE_LV_Lithium");
        if (circuit == null || battery == null) {
            return false;
        }
        GameRegistry
            .addRecipe(new ShapedOreRecipe(result, "C", "B", "P", 'C', circuit, 'B', battery, 'P', "plateSteel"));
        return true;
    }

    private static boolean addNTMRecipe(ItemStack result) {
        ItemStack circuit = ntmItem("circuit", ntmCircuitMeta("ANALOG", 7));
        ItemStack coil = ntmItem("coil_copper", 0);
        ItemStack plate = ntmItem("plate_steel", 0);
        if (circuit == null || coil == null || plate == null) {
            return false;
        }
        GameRegistry.addRecipe(new ShapedOreRecipe(result, "C", "c", "P", 'C', circuit, 'c', coil, 'P', plate));
        return true;
    }

    private static ItemStack ic2Item(String name) {
        try {
            Class<?> items = Class.forName("ic2.api.item.IC2Items");
            Object stack = items.getMethod("getItem", String.class)
                .invoke(null, name);
            return stack instanceof ItemStack ? (ItemStack) stack : null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static ItemStack gregTechItem(String name) {
        try {
            Class<?> itemList = Class.forName("gregtech.api.enums.ItemList");
            Object entry = enumConstant(itemList, name);
            if (entry == null) {
                return null;
            }
            Object stack = itemList.getMethod("get", long.class, Object[].class)
                .invoke(entry, 1L, new Object[0]);
            return stack instanceof ItemStack ? (ItemStack) stack : null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static ItemStack ntmItem(String field, int meta) {
        try {
            Class<?> modItems = Class.forName("com.hbm.items.ModItems");
            Object item = modItems.getField(field)
                .get(null);
            return item instanceof Item ? new ItemStack((Item) item, 1, meta) : null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static int ntmCircuitMeta(String name, int fallback) {
        try {
            Object constant = enumConstant(Class.forName("com.hbm.items.machine.ItemCircuit$EnumCircuitType"), name);
            if (constant != null) {
                return ((Enum<?>) constant).ordinal();
            }
        } catch (Throwable ignored) {

        }
        return fallback;
    }

    private static Object enumConstant(Class<?> type, String name) {
        for (Object constant : type.getEnumConstants()) {
            if (constant instanceof Enum && ((Enum<?>) constant).name()
                .equals(name)) {
                return constant;
            }
        }
        return null;
    }
}
