package com.hamarb123.macos_input_fixes.mixin;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.Minecraft;
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
        // Fires for every scroll step, so only log with -DmacosInputFixes.debugDropModifier=true
        if (Common.debugDropModifier()) {
            MacOSInputFixesMod.LOGGER.info(
                    "[MouseMixin] SCROLL: h={}, v={}, isMac={}, nativeRegistered={}, allowInputOSX={}",
                    horizontal, vertical, Common.IS_SYSTEM_MAC, Common.areNativeCallbacksRegistered(),
                    Common.allowInputOSX());
        }

        if (Common.IS_SYSTEM_MAC && Common.areNativeCallbacksRegistered()) {
            // Native callbacks are working - only allow non-zero scroll events from our native callback
            if ((vertical == 0 && horizontal == 0) || !Common.allowInputOSX()) {
                info.cancel();
            }
        }
        // Native callbacks not registered or not macOS - pass through all scroll events
    }

    @ModifyVariable(method = "onScroll(JDD)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double maybeReverseHScroll(double value) {
        return ModOptions.reverseScrolling ? -value : value;
    }

    @ModifyVariable(method = "onScroll(JDD)V", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private double maybeReverseVScroll(double value) {
        double v = ModOptions.reverseScrolling ? -value : value;
        if (ModOptions.reverseHotbarScrolling && Minecraft.getInstance().gui.screen() == null) {
            v = -v;
        }
        return v;
    }
}
