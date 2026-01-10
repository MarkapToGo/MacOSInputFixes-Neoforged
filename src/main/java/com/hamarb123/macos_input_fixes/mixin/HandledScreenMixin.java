package com.hamarb123.macos_input_fixes.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.MacOSInputFixesMod;
import com.hamarb123.macos_input_fixes.ModOptions;

@Mixin(AbstractContainerScreen.class)
public class HandledScreenMixin {

    /**
     * Enable the hasControlDown injection for mouse clicks in container screens.
     * This ensures Ctrl+Click is detected properly for item manipulation.
     */
    @Inject(at = @At("HEAD"), method = "mouseClicked(DDI)Z")
    private void mouseClickedHead(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
        MacOSInputFixesMod.LOGGER.info(
                "[HandledScreenMixin] mouseClicked HEAD: button={}, isMac={}, disableCtrlClickFix={}",
                button, Common.IS_SYSTEM_MAC, ModOptions.disableCtrlClickFix);
        if (!Common.IS_SYSTEM_MAC)
            return;
        if (ModOptions.disableCtrlClickFix)
            return;
        Common.setInjectHasControlDown(true);
        MacOSInputFixesMod.LOGGER.info("[HandledScreenMixin] Enabled injectHasControlDown for click");
    }

    @Inject(at = @At("RETURN"), method = "mouseClicked(DDI)Z")
    private void mouseClickedReturn(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
        if (!Common.IS_SYSTEM_MAC)
            return;
        if (ModOptions.disableCtrlClickFix)
            return;
        Common.setInjectHasControlDown(false);
        MacOSInputFixesMod.LOGGER.info("[HandledScreenMixin] Disabled injectHasControlDown after click");
    }

    @Inject(at = @At("HEAD"), method = "mouseReleased(DDI)Z")
    private void mouseReleasedHead(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
        if (!Common.IS_SYSTEM_MAC)
            return;
        if (ModOptions.disableCtrlClickFix)
            return;
        Common.setInjectHasControlDown(true);
    }

    @Inject(at = @At("RETURN"), method = "mouseReleased(DDI)Z")
    private void mouseReleasedReturn(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
        if (!Common.IS_SYSTEM_MAC)
            return;
        if (ModOptions.disableCtrlClickFix)
            return;
        Common.setInjectHasControlDown(false);
    }
}
