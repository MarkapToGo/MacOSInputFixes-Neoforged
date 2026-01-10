package com.hamarb123.macos_input_fixes;

import com.hamarb123.macos_input_fixes.mixin.MinecraftClientAccessor;
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
     * Custom hasControlDown implementation that recognizes actual Control key on
     * macOS.
     * By default, macOS maps Ctrl+Click to right-click, which breaks many Minecraft
     * features.
     * 
     * Behavior depends on ModOptions.useCommandKey:
     * - false (default): Check actual Ctrl key (STRG on German keyboards)
     * - true: Check Command key (⌘)
     */
    public static boolean hasControlDownInjector() {
        // Disable the injector for this call but restore after
        boolean oldValue = injectHasControlDown();
        setInjectHasControlDown(false);
        boolean returnValue;

        if (!IS_SYSTEM_MAC) {
            returnValue = Screen.hasControlDown();
        } else {
            long windowHandle = ((MinecraftClientAccessor) Minecraft.getInstance()).getWindow().getWindow();

            if (ModOptions.useCommandKey) {
                // When useCommandKey is ON, use Command key (⌘)
                // Screen.hasControlDown() on macOS already maps to Command
                returnValue = Screen.hasControlDown();
            } else {
                // When useCommandKey is OFF (default), use actual Ctrl key (STRG)
                // Check left control (341) and right control (345)
                returnValue = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                        || GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
            }
        }

        setInjectHasControlDown(oldValue);
        return returnValue;
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

    // Enable/disable the hasControlDown mixin
    private static ThreadLocal<Boolean> _injectHasControlDown = new ThreadLocal<>();

    public static boolean injectHasControlDown() {
        Boolean value = _injectHasControlDown.get();
        return value != null && value;
    }

    public static void setInjectHasControlDown(boolean value) {
        _injectHasControlDown.set(value);
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
