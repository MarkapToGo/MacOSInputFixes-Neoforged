package com.hamarb123.macos_input_fixes.mixin;

import net.minecraft.client.input.InputWithModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.ModOptions;

/**
 * Vanilla uses {@link InputWithModifiers#hasControlDownWithQuirk()} for text shortcuts (copy, paste, cut,
 * select all, word jumps) and maps it to Command (⌘) on macOS. Vanilla stack drop checks the physical
 * Control key instead, so the two never affect each other. "Ctrl for Text Shortcuts" lets players move the text
 * shortcuts to Ctrl as well.
 */
@Mixin(InputWithModifiers.class)
public interface InputWithModifiersMixin {

    @Inject(method = "hasControlDownWithQuirk", at = @At("HEAD"), cancellable = true)
    private void macosInputFixes$ctrlForTextShortcuts(CallbackInfoReturnable<Boolean> cir) {
        if (Common.IS_SYSTEM_MAC && ModOptions.ctrlForTextShortcuts) {
            cir.setReturnValue(((InputWithModifiers) (Object) this).hasControlDown());
        }
    }
}
