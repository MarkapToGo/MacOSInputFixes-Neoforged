package com.hamarb123.macos_input_fixes.mixin;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.MacOSInputFixesMod;
import com.hamarb123.macos_input_fixes.ModOptions;

/**
 * NeoForge {@code KeyModifier.CONTROL} ({@code KeyModifier$1}) uses
 * {@link net.minecraft.client.gui.screens.Screen#hasControlDown()} for
 * {@code isActive()}. Our {@link ScreenMixin} maps that to physical Control (Strg)
 * for vanilla gameplay, but then {@code KeyModifier.NONE#isActive} thinks a
 * modifier is held and suppresses unmodified keys (e.g. drop) in GUI contexts.
 * <p>
 * Keep NeoForge's original macOS meaning here: "control modifier" = Command (⌘),
 * while {@link ScreenMixin} continues to expose Strg to vanilla code paths.
 */
@Mixin(targets = "net.neoforged.neoforge.client.settings.KeyModifier$1")
public class KeyModifierControlMixin {

    @Unique
    private static boolean macosInputFixes$loggedKeyModifierOnce;

    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true, remap = false)
    private void macosInputFixes$neoForgeControlModifierUsesSuperOnMac(IKeyConflictContext conflictContext,
            CallbackInfoReturnable<Boolean> cir) {
        if (!Common.IS_SYSTEM_MAC) {
            return;
        }
        if (ModOptions.disableCtrlClickFix || ModOptions.useCommandKey) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return;
        }
        long window = mc.getWindow().getWindow();
        boolean commandHeld = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SUPER) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SUPER) == GLFW.GLFW_PRESS;
        if (!macosInputFixes$loggedKeyModifierOnce) {
            macosInputFixes$loggedKeyModifierOnce = true;
            MacOSInputFixesMod.LOGGER.info(
                    "[MacOSInputFixes] NeoForge KeyModifier.CONTROL.isActive uses Command (⌘) on mac when Use Command key=OFF (so NONE keybinds still work). DEBUG logs each call.");
        }
        MacOSInputFixesMod.LOGGER.debug(
                "[MacOSInputFixes] KeyModifier.CONTROL.isActive -> {} (ctx={})",
                commandHeld,
                conflictContext);
        cir.setReturnValue(commandHeld);
    }
}
