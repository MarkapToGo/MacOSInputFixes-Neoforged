package com.hamarb123.macos_input_fixes.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.ModOptions;

/**
 * Mixin to enable hasControlDown injection during keybind handling.
 * This allows Ctrl+Q to properly drop entire stacks on macOS.
 */
@Mixin(Minecraft.class)
public class MinecraftDropMixin {

    /**
     * Enable hasControlDown injection at the start of handleKeybinds
     * so that drop key (Q) properly detects Ctrl key on macOS.
     */
    @Inject(at = @At("HEAD"), method = "handleKeybinds()V")
    private void handleKeybindsHead(CallbackInfo ci) {
        if (!Common.IS_SYSTEM_MAC)
            return;
        if (ModOptions.disableCtrlClickFix)
            return;
        Common.setInjectHasControlDown(true);
    }

    /**
     * Disable hasControlDown injection at the end of handleKeybinds.
     */
    @Inject(at = @At("RETURN"), method = "handleKeybinds()V")
    private void handleKeybindsReturn(CallbackInfo ci) {
        if (!Common.IS_SYSTEM_MAC)
            return;
        if (ModOptions.disableCtrlClickFix)
            return;
        Common.setInjectHasControlDown(false);
    }
}
