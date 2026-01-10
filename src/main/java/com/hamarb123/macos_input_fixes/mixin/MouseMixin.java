package com.hamarb123.macos_input_fixes.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.MacOSInputFixesMod;
import com.hamarb123.macos_input_fixes.ModOptions;

@Mixin(MouseHandler.class)
public class MouseMixin {

    @Inject(at = @At("HEAD"), method = "onScroll(JDD)V", cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo info) {
        MacOSInputFixesMod.LOGGER.info(
                "[MouseMixin] SCROLL: h={}, v={}, isMac={}, nativeRegistered={}, allowInputOSX={}",
                horizontal, vertical, Common.IS_SYSTEM_MAC, Common.areNativeCallbacksRegistered(),
                Common.allowInputOSX());

        if (Common.IS_SYSTEM_MAC && Common.areNativeCallbacksRegistered()) {
            // Native callbacks are working - only allow scroll events from our native
            // callback
            if (vertical == 0 && horizontal == 0) {
                MacOSInputFixesMod.LOGGER.info("[MouseMixin] -> CANCELLED: both h and v are 0");
                info.cancel();
                return;
            }
            if (!Common.allowInputOSX()) {
                MacOSInputFixesMod.LOGGER.info("[MouseMixin] -> CANCELLED: not from native callback");
                info.cancel();
                return;
            }
            MacOSInputFixesMod.LOGGER.info("[MouseMixin] -> ALLOWED: from native callback");
        } else {
            // Native callbacks not registered or not macOS - pass through all scroll events
            MacOSInputFixesMod.LOGGER.info("[MouseMixin] -> ALLOWED: native callbacks not active, passing through");
        }
    }

    @ModifyVariable(method = "onScroll(JDD)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double maybeReverseHScroll(double value) {
        return ModOptions.reverseScrolling ? -value : value;
    }

    @ModifyVariable(method = "onScroll(JDD)V", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private double maybeReverseVScroll(double value) {
        return ModOptions.reverseScrolling ? -value : value;
    }
}
