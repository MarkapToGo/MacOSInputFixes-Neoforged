package com.hamarb123.macos_input_fixes.mixin;

import net.minecraft.client.KeyboardHandler;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;

import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.MacOSInputFixesMod;
import com.hamarb123.macos_input_fixes.ModOptions;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {

    @Inject(at = @At("HEAD"), method = "keyPress(JILnet/minecraft/client/input/KeyEvent;)V", cancellable = true)
    private void onKey(long window, int action, net.minecraft.client.input.KeyEvent event, CallbackInfo info) {
        if (!Common.IS_SYSTEM_MAC) {
            return;
        }
        int key = event.key();
        int keycode = event.keycode();
        int modifiers = event.modifiers();

        Common.setLastKeyboardModifiers(modifiers);

        // Key events are only logged with -DmacosInputFixes.debugDropModifier=true (privacy and log spam)
        if (Common.debugDropModifier()) {
            if (key == InputConstants.KEY_Q && action != InputConstants.RELEASE) {
                Minecraft mc = Minecraft.getInstance();
                if (mc != null) {
                    MacOSInputFixesMod.LOGGER.info(
                            "[MacOSInputFixes][drop] key Q: action={} mods=0x{} | physicalStrg={} | Screen.hasControlDown()={}",
                            action,
                            Integer.toHexString(modifiers),
                            Common.physicalStrgKeysDown(mc.getWindow()),
                            Common.vanillaStyleHasControlDown(mc.getWindow()));
                }
            }
            if (key == InputConstants.KEY_ESCAPE || key == InputConstants.KEY_TAB || key == InputConstants.KEY_Q
                    || (modifiers & InputConstants.MOD_CONTROL) != 0 || (modifiers & InputConstants.MOD_SUPER) != 0) {
                MacOSInputFixesMod.LOGGER.info("[KeyboardMixin] KEY: key={}, keycode={}, action={}, mods={}",
                        key, keycode, action, modifiers);
            }
        }

        // Block Command+Q from quitting the game if option is enabled
        // MOD_SUPER = Command key on macOS
        if (ModOptions.blockCommandQQuit && key == InputConstants.KEY_Q && (modifiers & InputConstants.MOD_SUPER) != 0) {
            if (action == InputConstants.PRESS) {
                MacOSInputFixesMod.LOGGER.info("[KeyboardMixin] Blocked Command+Q (quit prevention)");
            }
            info.cancel();
            return;
        }

        if (!Common.areNativeCallbacksRegistered()) {
            // Native callbacks not registered - pass through all key events
            return;
        }

        // The native code forwards ALL Tab and Escape key events to Java
        // So we need to cancel the duplicate SDL event for these keys
        // The native event sets allowInputOSX2 = true before calling keyPress
        if (key == InputConstants.KEY_TAB || key == InputConstants.KEY_ESCAPE) {
            if (!Common.allowInputOSX2()) {
                // This is the SDL event (duplicate) - cancel it
                if (Common.debugDropModifier()) {
                    MacOSInputFixesMod.LOGGER
                            .info("[KeyboardMixin] -> CANCELLED: Tab/Esc not from native (SDL duplicate)");
                }
                info.cancel();
            } else if (Common.debugDropModifier()) {
                // This is the native event - allow it
                MacOSInputFixesMod.LOGGER.info("[KeyboardMixin] -> ALLOWED: from native callback");
            }
        }
        // All other keys go through SDL normally - native code doesn't forward them
    }
}
