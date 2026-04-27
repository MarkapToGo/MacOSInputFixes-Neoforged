package com.hamarb123.macos_input_fixes.mixin;

import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.ModOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    /**
     * MC-122296: since 1.21, vanilla remaps Ctrl+left to right click inside
     * {@code onPress} after parameters are visible to {@code @ModifyVariable} at
     * HEAD. Skip that whole OS X branch when our fix is enabled by making the
     * {@code Minecraft.ON_OSX} check false for this method only.
     */
    @Redirect(
            method = "onPress",
            at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "Lnet/minecraft/client/Minecraft;ON_OSX:Z"))
    private boolean macosInputFixes$skipVanillaOsxCtrlClickRemap() {
        if (Common.IS_SYSTEM_MAC && !ModOptions.disableCtrlClickFix) {
            return false;
        }
        return Minecraft.ON_OSX;
    }
}
