package com.hamarb123.macos_input_fixes.mixin;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.MacOSInputFixesMod;
import com.hamarb123.macos_input_fixes.ModOptions;

@Mixin(Screen.class)
public class ScreenMixin {

    /**
     * Patches hasControlDown() on macOS to properly detect the Control key.
     * macOS normally treats Ctrl+Click as right-click, which breaks many features.
     */
    @Inject(at = @At("HEAD"), method = "hasControlDown", cancellable = true)
    private static void hasControlDown(CallbackInfoReturnable<Boolean> info) {
        if (!Common.IS_SYSTEM_MAC)
            return;
        if (ModOptions.disableCtrlClickFix)
            return;
        if (!Common.injectHasControlDown())
            return;

        // Use our custom implementation that properly detects Control key
        boolean result = Common.hasControlDownInjector();
        MacOSInputFixesMod.LOGGER.info("[ScreenMixin] hasControlDown called, returning: {}", result);
        info.setReturnValue(result);
    }
}
