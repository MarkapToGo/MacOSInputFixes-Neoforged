package com.hamarb123.macos_input_fixes.mixin;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFWNativeCocoa;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.MacOSInputFixesMod;
import com.hamarb123.macos_input_fixes.ModOptions;
import com.hamarb123.macos_input_fixes.client.KeyCallback;
import com.hamarb123.macos_input_fixes.client.ScrollCallback;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @SuppressWarnings("resource") // Minecraft instance is managed by the game, not us
    @Inject(at = @At("TAIL"), method = "onGameLoadFinished")
    private void onGameLoadFinished(CallbackInfo info) {
        MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] ========================================");
        MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] onGameLoadFinished called!");
        MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] IS_SYSTEM_MAC = {}", Common.IS_SYSTEM_MAC);

        if (!Common.IS_SYSTEM_MAC) {
            MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] Not macOS, skipping native callbacks");
            MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] ========================================");
            return;
        }

        Minecraft client = (Minecraft) (Object) this;
        long glfwWindow = ((MinecraftClientAccessor) client).getWindow().getWindow();
        // Get the Cocoa NSWindow handle - this is what the native code expects!
        long cocoaWindow = GLFWNativeCocoa.glfwGetCocoaWindow(glfwWindow);
        MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] GLFW window handle = {}", glfwWindow);
        MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] Cocoa NSWindow handle = {}", cocoaWindow);

        // Create scroll callback to handle macOS scroll events
        // Native signature: accept(DDDDDD)V - 6 doubles: x, y, xWithMomentum,
        // yWithMomentum, ungroupedX, ungroupedY
        ScrollCallback scrollCallback = (x, y, xWithMomentum, yWithMomentum, ungroupedX, ungroupedY) -> {
            MacOSInputFixesMod.LOGGER.info(
                    "[NativeCallback] SCROLL from native: x={}, y={}, xMom={}, yMom={}, uX={}, uY={}",
                    x, y, xWithMomentum, yWithMomentum, ungroupedX, ungroupedY);
            Common.setAllowedInputOSX(true);
            try {
                // Use the grouped x/y values for scroll input - use GLFW window for mouse
                // handler
                ((MouseInvokerMixin) client.mouseHandler).invokeOnScroll(glfwWindow, x, y);
            } finally {
                Common.setAllowedInputOSX(false);
            }
        };

        // Create key callback to handle certain macOS key events (ctrl+tab, ctrl+esc)
        // Native signature: accept(IIII)V - 4 ints: key, scancode, action, modifiers
        KeyCallback keyCallback = (key, scancode, action, modifiers) -> {
            MacOSInputFixesMod.LOGGER.info("[NativeCallback] KEY from native: key={}, scancode={}, action={}, mods={}",
                    key, scancode, action, modifiers);
            Common.setAllowedInputOSX2(true);
            try {
                // Use GLFW window for keyboard handler
                client.keyboardHandler.keyPress(glfwWindow, key, scancode, action, modifiers);
            } finally {
                Common.setAllowedInputOSX2(false);
            }
        };

        // Try to register native callbacks - if it fails, the mod still works for basic
        // features
        MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] Registering native callbacks with Cocoa window...");
        try {
            // Pass the COCOA window handle to native code, not the GLFW window!
            MacOSInputFixesMod.registerCallbacks(scrollCallback, keyCallback, cocoaWindow);
            Common.setNativeCallbacksRegistered(true);
            MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] SUCCESS! Native callbacks registered!");

            // Apply saved settings to native code
            MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] Applying settings to native code...");
            MacOSInputFixesMod.setTrackpadSensitivity(ModOptions.trackpadSensitivity);
            MacOSInputFixesMod.setMomentumScrolling(ModOptions.momentumScrolling);
            MacOSInputFixesMod.setInterfaceSmoothScroll(ModOptions.interfaceSmoothScroll);
        } catch (Throwable e) {
            Common.setNativeCallbacksRegistered(false);
            MacOSInputFixesMod.LOGGER
                    .warn("[MinecraftClientMixin] Native callbacks FAILED - mod will work with limited features");
            MacOSInputFixesMod.LOGGER.warn("[MinecraftClientMixin] Error: {}", e.getMessage());
            // Don't rethrow - allow game to continue with reduced functionality
        }

        MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] ========================================");
    }
}
