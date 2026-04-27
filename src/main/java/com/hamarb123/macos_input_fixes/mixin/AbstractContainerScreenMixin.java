package com.hamarb123.macos_input_fixes.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.MacOSInputFixesMod;
import com.hamarb123.macos_input_fixes.ModOptions;

/**
 * Diagnostics for inventory drop + control detection (Strg vs ⌘).
 */
@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void macosInputFixes$logDropKeyContext(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!Common.IS_SYSTEM_MAC) {
            return;
        }
        if (!Common.debugDropModifier()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.options == null) {
            return;
        }
        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
        boolean dropMatches = mc.options.keyDrop.isActiveAndMatches(key);
        if (!dropMatches) {
            return;
        }
        AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
        Slot hovered = self.getSlotUnderMouse();
        boolean hoveredHasStack = hovered != null && hovered.hasItem();
        long w = mc.getWindow().getWindow();
        MacOSInputFixesMod.LOGGER.info(
                "[MacOSInputFixes][drop] inventory keyPressed: keyDrop.isActiveAndMatches={} | keyCode={} scan={} | Screen.hasControlDown()={} (full stack drop uses this) | physicalStrg={} | disableCtrlFix={} useCommandKey={} | hoveredHasStack={}",
                dropMatches,
                keyCode,
                scanCode,
                Screen.hasControlDown(),
                Common.physicalStrgKeysDown(w),
                ModOptions.disableCtrlClickFix,
                ModOptions.useCommandKey,
                hoveredHasStack);
    }
}
