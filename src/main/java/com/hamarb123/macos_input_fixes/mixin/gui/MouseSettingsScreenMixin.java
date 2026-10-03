package com.hamarb123.macos_input_fixes.mixin.gui;

import net.minecraft.client.Options;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.MouseSettingsScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hamarb123.macos_input_fixes.ModOptions;

/**
 * Adds our options below the vanilla ones in Controls -> Mouse Settings. Hooks the screen itself, so it
 * works in every language and no matter how many options vanilla shows.
 */
@Mixin(MouseSettingsScreen.class)
public abstract class MouseSettingsScreenMixin extends OptionsSubScreen {

    private MouseSettingsScreenMixin(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
    }

    @Inject(method = "addOptions", at = @At("RETURN"))
    private void macosInputFixes$addModOptions(CallbackInfo info) {
        OptionInstance<?>[] modOptions = ModOptions.getModOptions();
        if (this.list != null && modOptions.length > 0) {
            this.list.addSmall(modOptions);
        }
    }
}
