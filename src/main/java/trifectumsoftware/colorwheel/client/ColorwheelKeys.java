package trifectumsoftware.colorwheel.client;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

public final class ColorwheelKeys {

    public static final String CATEGORY = "key.categories.colorwheel";

    public static final KeyBinding UNDO = new KeyBinding("key.colorwheel.undo", Keyboard.KEY_Z, CATEGORY);
    public static final KeyBinding REDO = new KeyBinding("key.colorwheel.redo", Keyboard.KEY_Y, CATEGORY);
    public static final KeyBinding GRADIENT = new KeyBinding("key.colorwheel.gradient", Keyboard.KEY_G, CATEGORY);
    public static final KeyBinding GRADIENT_EDITOR = new KeyBinding(
        "key.colorwheel.gradientEditor",
        Keyboard.KEY_H,
        CATEGORY);

    private ColorwheelKeys() {}
}
