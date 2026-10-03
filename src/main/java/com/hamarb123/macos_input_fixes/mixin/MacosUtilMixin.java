package com.hamarb123.macos_input_fixes.mixin;

import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.ModOptions;
import com.mojang.blaze3d.platform.MacosUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MacosUtil.class)
public class MacosUtilMixin {

    /**
     * MC-122296: since 26.3 the Ctrl + left click to right click remap is an SDL hint driven by the
     * vanilla "Ctrl + Click Emulates Right Click" option. Keep it off while our fix is enabled.
     */
    @ModifyVariable(method = "setCtrlClickEmulatesRightClick", at = @At("HEAD"), argsOnly = true)
    private static boolean macosInputFixes$keepCtrlClickAsLeftClick(boolean value) {
        if (Common.IS_SYSTEM_MAC && !ModOptions.disableCtrlClickFix) {
            return false;
        }
        return value;
    }
}
