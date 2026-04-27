package com.hamarb123.macos_input_fixes.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.MacOSInputFixesMod;

@Mixin(Screen.class)
public class ScreenMixin {

    @Unique
    private static boolean macosInputFixes$loggedScreenOverrideOnce;

    /**
     * On macOS, vanilla {@link Screen#hasControlDown()} maps ⌘ to “control”. We replace the whole method
     * outcome via {@link Common#macStrgParityFullStackModifier} so we never recurse into this inject.
     */
    @Inject(at = @At("HEAD"), method = "hasControlDown", cancellable = true)
    private static void hasControlDown(CallbackInfoReturnable<Boolean> info) {
        if (!Common.IS_SYSTEM_MAC) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return;
        }
        boolean value = Common.macStrgParityFullStackModifier(mc);
        if (!macosInputFixes$loggedScreenOverrideOnce) {
            macosInputFixes$loggedScreenOverrideOnce = true;
            MacOSInputFixesMod.LOGGER.info(
                    "[MacOSInputFixes] Screen.hasControlDown() routed through macStrgParity (no recursion). "
                            + "-DmacosInputFixes.debugDropModifier=true for traces.");
        }
        if (Common.debugDropModifier()) {
            MacOSInputFixesMod.LOGGER.info("[MacOSInputFixes] Screen.hasControlDown -> {}", value);
        } else {
            MacOSInputFixesMod.LOGGER.debug("[MacOSInputFixes] Screen.hasControlDown -> {}", value);
        }
        info.setReturnValue(value);
    }
}
