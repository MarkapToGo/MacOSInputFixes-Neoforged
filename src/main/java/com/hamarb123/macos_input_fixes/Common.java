package com.hamarb123.macos_input_fixes;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

/**
 * Common utility methods for the mod.
 */
public class Common {
    public static final boolean IS_SYSTEM_MAC = Util.getPlatform() == Util.OS.OSX;

    /**
     * Latest {@code modifiers} bitmask from GLFW key callback (updated every key event on Mac).
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

    private static boolean physicalStrgKeysDownUncached(long windowHandle) {
        return InputConstants.isKeyDown(windowHandle, GLFW.GLFW_KEY_LEFT_CONTROL)
                || InputConstants.isKeyDown(windowHandle, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    /**
     * Physical Control / Strg held: GLFW key poll plus, on macOS only, the Control bit from the last
     * key callback (covers driver/layout quirks where Strg+Q stack drop saw {@code false} from keys alone).
     */
    public static boolean physicalStrgKeysDown(long windowHandle) {
        if (physicalStrgKeysDownUncached(windowHandle)) {
            return true;
        }
        return IS_SYSTEM_MAC && (lastKeyboardModifiers & GLFW.GLFW_MOD_CONTROL) != 0;
    }

    /**
     * Same mapping as vanilla {@link Screen#hasControlDown()} but without calling Screen (avoids mixin
     * re-entrancy). Used when options ask for vanilla ⌘/Ctrl semantics.
     */
    public static boolean vanillaStyleHasControlDown(long windowHandle) {
        if (IS_SYSTEM_MAC) {
            return InputConstants.isKeyDown(windowHandle, GLFW.GLFW_KEY_LEFT_SUPER)
                    || InputConstants.isKeyDown(windowHandle, GLFW.GLFW_KEY_RIGHT_SUPER);
        }
        return InputConstants.isKeyDown(windowHandle, GLFW.GLFW_KEY_LEFT_CONTROL)
                || InputConstants.isKeyDown(windowHandle, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    /**
     * Whether the “drop whole stack” modifier is held: mirrors mod options and never calls
     * {@link Screen#hasControlDown()} (safe from recursive mixin).
     */
    public static boolean macStrgParityFullStackModifier(Minecraft mc) {
        if (mc == null) {
            return false;
        }
        if (!IS_SYSTEM_MAC) {
            return Screen.hasControlDown();
        }
        long w = mc.getWindow().getWindow();
        if (ModOptions.disableCtrlClickFix || ModOptions.useCommandKey) {
            return vanillaStyleHasControlDown(w);
        }
        return physicalStrgKeysDown(w);
    }

    /** OR in {@link GLFW#GLFW_MOD_CONTROL} when GLFW key poll sees Strg — call before {@code handleKeybinds}. */
    public static void mergeStrgKeysIntoModifierCache(long windowHandle) {
        if (!IS_SYSTEM_MAC) {
            return;
        }
        if (physicalStrgKeysDownUncached(windowHandle)) {
            lastKeyboardModifiers |= GLFW.GLFW_MOD_CONTROL;
        }
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
