package com.hamarb123.macos_input_fixes.mixin.gui;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hamarb123.macos_input_fixes.MacOSInputFixesMod;
import com.hamarb123.macos_input_fixes.ModOptions;

@Mixin(OptionsList.class)
public class OptionListWidgetMixin {

    private static boolean modOptionsAdded = false;

    /**
     * Inject our mod options after the main mouse options are added.
     * We detect mouse options by looking for 5 options (typical mouse settings
     * count).
     */
    @Inject(at = @At("RETURN"), method = "addSmall([Lnet/minecraft/client/OptionInstance;)V")
    private void afterAddSmall(OptionInstance<?>[] options, CallbackInfo info) {
        MacOSInputFixesMod.LOGGER.info("[OptionListWidgetMixin] addSmall called with {} options", options.length);

        // Check if these are mouse options - use option caption text
        boolean isMouseOptions = false;
        for (OptionInstance<?> option : options) {
            try {
                // Get the caption (translated text) of the option
                String caption = option.toString();
                MacOSInputFixesMod.LOGGER.info("[OptionListWidgetMixin] Option: {}", caption);

                // Check for mouse sensitivity option by the text content
                if (caption.contains("sensitivity") || caption.contains("Sensitivity")) {
                    isMouseOptions = true;
                    break;
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        // Also check by option count - mouse settings typically has 5 options
        // This is the Controls -> Mouse Settings screen
        if (!isMouseOptions && options.length == 5 && !modOptionsAdded) {
            // Heuristic: Mouse settings screen typically shows 5 options
            // We'll try adding our options and set a flag to prevent adding twice
            isMouseOptions = true;
        }

        if (isMouseOptions && !modOptionsAdded) {
            MacOSInputFixesMod.LOGGER.info("[OptionListWidgetMixin] Found mouse options, adding mod options");
            // Add our mod options
            OptionsList self = (OptionsList) (Object) this;
            OptionInstance<?>[] modOptions = ModOptions.getModOptions();
            MacOSInputFixesMod.LOGGER.info("[OptionListWidgetMixin] Adding {} mod options", modOptions.length);
            if (modOptions.length > 0) {
                modOptionsAdded = true;
                self.addSmall(modOptions);
            }
        }
    }

    /**
     * Reset the flag when a new options list is created.
     */
    @Inject(at = @At("RETURN"), method = "<init>")
    private void onInit(CallbackInfo info) {
        modOptionsAdded = false;
    }
}
