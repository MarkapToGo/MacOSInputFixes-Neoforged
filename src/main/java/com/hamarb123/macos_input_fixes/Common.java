package com.hamarb123.macos_input_fixes;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.MacosUtil;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.sdl.SDLKeycode;

/**
 * Common utility methods for the mod.
 */
public class Common {
    public static final boolean IS_SYSTEM_MAC = Util.getPlatform() == Util.OS.OSX;

    /**
     * Latest {@code modifiers} bitmask (SDL modifier bits, see {@link InputConstants#MOD_CONTROL}) from the key callback (updated every key event on Mac).
     * macOS sometimes reports Ctrl+key combinations via this bitmask before/alongside stable
     * {@link InputConstants#isKeyDown} results for the Control keys alone — needed for Ctrl/Strg+Q stack drop.
     */
    private static volatile int lastKeyboardModifiers;

    /**
     * Log extra detail for Ctrl/Strg + drop (hotbar and inventory). Enable with
     * {@code -DmacosInputFixes.debugDropModifier=true}.
     */
    public static boolean debugDropModifier() {
        return Boolean.getBoolean("macosInputFixes.debugDropModifier");
    }

    public static void setLastKeyboardModifiers(int modifiers) {
        lastKeyboardModifiers = modifiers;
    }

    private static boolean physicalStrgKeysDownUncached(com.mojang.blaze3d.platform.Window window) {
        return InputConstants.isKeyDown(InputConstants.KEY_LCONTROL)
                || InputConstants.isKeyDown(InputConstants.KEY_RCONTROL);
    }

    /**
     * Physical Control / Strg held: key poll plus, on macOS only, the Control bit from the last
     * key callback (covers driver/layout quirks where Strg+Q stack drop saw {@code false} from keys alone).
     */
    public static boolean physicalStrgKeysDown(com.mojang.blaze3d.platform.Window window) {
        if (physicalStrgKeysDownUncached(window)) {
            return true;
        }
        return IS_SYSTEM_MAC && (lastKeyboardModifiers & InputConstants.MOD_CONTROL) != 0;
    }

    /**
     * Same mapping as vanilla {@code Screen#hasControlDown()} but without calling Screen (avoids mixin
     * re-entrancy). Used when options ask for vanilla ⌘/Ctrl semantics.
     */
    public static boolean vanillaStyleHasControlDown(com.mojang.blaze3d.platform.Window window) {
        if (IS_SYSTEM_MAC) {
            return InputConstants.isKeyDown(InputConstants.KEY_LGUI)
                    || InputConstants.isKeyDown(InputConstants.KEY_RGUI);
        }
        return InputConstants.isKeyDown(InputConstants.KEY_LCONTROL)
                || InputConstants.isKeyDown(InputConstants.KEY_RCONTROL);
    }

    /**
     * Whether the “drop whole stack” modifier is held: mirrors mod options and never calls
     * {@code Screen#hasControlDown()} (safe from recursive mixin).
     */
    public static boolean macStrgParityFullStackModifier(Minecraft mc) {
        if (mc == null) {
            return false;
        }
        com.mojang.blaze3d.platform.Window w = mc.getWindow();
        if (!IS_SYSTEM_MAC) {
            return vanillaStyleHasControlDown(w);
        }
        if (ModOptions.disableCtrlClickFix || ModOptions.useCommandKey) {
            return vanillaStyleHasControlDown(w);
        }
        return physicalStrgKeysDown(w);
    }

    /** OR in {@link InputConstants#MOD_CONTROL} when the key poll sees Strg — call before {@code handleKeybinds}. */
    public static void mergeStrgKeysIntoModifierCache(com.mojang.blaze3d.platform.Window window) {
        if (!IS_SYSTEM_MAC) {
            return;
        }
        if (physicalStrgKeysDownUncached(window)) {
            lastKeyboardModifiers |= InputConstants.MOD_CONTROL;
        }
    }

    /**
     * The native library reports Tab/Escape in GLFW terms (GLFW key token and modifier bits). Since 26.3
     * Minecraft uses SDL, so {@link KeyEvent} needs an SDL scancode, SDL keycode and SDL modifier bits.
     */
    public static KeyEvent keyEventFromNative(int glfwKey, int glfwModifiers) {
        int key;
        int keycode;
        if (glfwKey == 258 /* GLFW_KEY_TAB */) {
            key = InputConstants.KEY_TAB;
            keycode = SDLKeycode.SDLK_TAB;
        } else if (glfwKey == 256 /* GLFW_KEY_ESCAPE */) {
            key = InputConstants.KEY_ESCAPE;
            keycode = SDLKeycode.SDLK_ESCAPE;
        } else {
            key = InputConstants.UNKNOWN.getValue();
            keycode = 0;
        }
        int modifiers = 0;
        if ((glfwModifiers & 0x01) != 0) modifiers |= InputConstants.MOD_SHIFT;
        if ((glfwModifiers & 0x02) != 0) modifiers |= InputConstants.MOD_CONTROL;
        if ((glfwModifiers & 0x04) != 0) modifiers |= InputConstants.MOD_ALT;
        if ((glfwModifiers & 0x08) != 0) modifiers |= InputConstants.MOD_SUPER;
        if ((glfwModifiers & 0x10) != 0) modifiers |= InputConstants.MOD_CAPS_LOCK;
        return new KeyEvent(key, keycode, modifiers);
    }

    /** GLFW action from the native library (0 release, 1 press, 2 repeat) to the SDL-era action (repeat is -1). */
    public static int keyActionFromNative(int glfwAction) {
        return glfwAction == 2 ? -1 : glfwAction;
    }

    /**
     * Re-sends the vanilla "Ctrl + Click Emulates Right Click" value to SDL so
     * {@code MacosUtilMixin} re-applies our MC-122296 fix after our options change.
     */
    public static void applyCtrlClickEmulation() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.options == null) {
            return;
        }
        MacosUtil.setCtrlClickEmulatesRightClick(mc.options.ctrlClickEmulatesRightClick().get());
    }

    // Thread-local flags for controlling various mixin behaviors

    // Enable/disable the onMouseScroll function
    private static ThreadLocal<Boolean> _allowInputOSX = new ThreadLocal<>();

    public static boolean allowInputOSX() {
        Boolean value = _allowInputOSX.get();
        return value != null && value;
    }

    public static void setAllowedInputOSX(boolean value) {
        _allowInputOSX.set(value);
    }

    // Enable/disable the onKey function (for specific key codes)
    private static ThreadLocal<Boolean> _allowInputOSX2 = new ThreadLocal<>();

    public static boolean allowInputOSX2() {
        Boolean value = _allowInputOSX2.get();
        return value != null && value;
    }

    public static void setAllowedInputOSX2(boolean value) {
        _allowInputOSX2.set(value);
    }

    // Enable/disable the CyclingButtonWidgetMixin builder mixin
    private static ThreadLocal<Boolean> _omitBuilderKeyText = new ThreadLocal<>();

    public static boolean omitBuilderKeyText() {
        Boolean value = _omitBuilderKeyText.get();
        return value != null && value;
    }

    public static void setOmitBuilderKeyText(boolean value) {
        _omitBuilderKeyText.set(value);
    }

    // Helper for when java struggles with undefined types
    public static Object asObject(Object o) {
        return o;
    }

    // Flag to track if native callbacks were registered successfully
    private static volatile boolean nativeCallbacksRegistered = false;

    public static boolean areNativeCallbacksRegistered() {
        return nativeCallbacksRegistered;
    }

    public static void setNativeCallbacksRegistered(boolean value) {
        nativeCallbacksRegistered = value;
    }
}
