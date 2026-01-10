package com.hamarb123.macos_input_fixes.mixin;

import net.minecraft.client.KeyboardHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.MacOSInputFixesMod;
import com.hamarb123.macos_input_fixes.ModOptions;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {

    @Inject(at = @At("HEAD"), method = "keyPress(JIIII)V", cancellable = true)
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo info) {
        if (!Common.IS_SYSTEM_MAC) {
            return;
        }

        // Only log special keys to avoid spam
        if (key == 256 || key == 258 || key == 81 || (modifiers & GLFW.GLFW_MOD_CONTROL) != 0
                || (modifiers & GLFW.GLFW_MOD_SUPER) != 0) {
            MacOSInputFixesMod.LOGGER.info("[KeyboardMixin] KEY: key={}, scancode={}, action={}, mods={}",
                    key, scancode, action, modifiers);
        }

        // Block Command+Q from quitting the game if option is enabled
        // Key 81 = Q, GLFW_MOD_SUPER = Command key on macOS
        if (ModOptions.blockCommandQQuit && key == 81 && (modifiers & GLFW.GLFW_MOD_SUPER) != 0) {
            MacOSInputFixesMod.LOGGER.info("[KeyboardMixin] -> BLOCKED: Command+Q (quit prevention)");
            info.cancel();
            return;
        }

        if (!Common.areNativeCallbacksRegistered()) {
            // Native callbacks not registered - pass through all key events
            return;
        }

        // The native code forwards ALL Tab (258) and Escape (256) key events to Java
        // So we need to cancel the duplicate GLFW event for these keys
        // The native event sets allowInputOSX2 = true before calling keyPress
        if (key == 258 || key == 256) {
            if (!Common.allowInputOSX2()) {
                // This is the GLFW event (duplicate) - cancel it
                MacOSInputFixesMod.LOGGER
                        .info("[KeyboardMixin] -> CANCELLED: Tab/Esc not from native (GLFW duplicate)");
                info.cancel();
            } else {
                // This is the native event - allow it
                MacOSInputFixesMod.LOGGER.info("[KeyboardMixin] -> ALLOWED: from native callback");
            }
        }
        // All other keys go through GLFW normally - native code doesn't forward them
    }
}
