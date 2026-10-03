package com.hamarb123.macos_input_fixes.mixin;

import net.minecraft.client.Minecraft;
import org.lwjgl.sdl.SDLProperties;
import org.lwjgl.sdl.SDLVideo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.MacOSInputFixesMod;
import com.hamarb123.macos_input_fixes.ModOptions;
import com.hamarb123.macos_input_fixes.client.KeyCallback;
import com.hamarb123.macos_input_fixes.client.ScrollCallback;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    /**
     * Avoids {@code onGameLoadFinished(Minecraft$GameLoadCookie)} on the mixin callback
     * (AT must match runtime or Mixin rejects {@code Object} vs cookie). First
     * {@link Minecraft#runTick} after {@link Minecraft#isGameLoadFinished()} is enough
     * for GLFW/Cocoa to exist.
     */
    @Unique
    private static volatile boolean macosInputFixes$nativeHooksInitialized;

    @SuppressWarnings("resource")
    @Inject(method = "runTick", at = @At("HEAD"))
    private void macosInputFixes$registerNativeOnFirstRunTick(boolean renderTick, CallbackInfo ci) {
        if (macosInputFixes$nativeHooksInitialized) {
            return;
        }
        Minecraft client = (Minecraft) (Object) this;
        if (!client.isGameLoadFinished()) {
            return;
        }
        macosInputFixes$nativeHooksInitialized = true;

        MacOSInputFixesMod.LOGGER.info("[MacOSInputFixes] First runTick after game load — initializing macOS native hooks");
        macosInputFixes$registerNativeHooks(client);
    }

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void macosInputFixes$mergeStrgBeforeKeybinds(CallbackInfo ci) {
        if (!Common.IS_SYSTEM_MAC) {
            return;
        }
        Minecraft mc = (Minecraft) (Object) this;
        Common.mergeStrgKeysIntoModifierCache(mc.getWindow());
    }

    /**
     * Forces stack drop modifier from {@link Common#macStrgParityFullStackModifier} so hotbar drop does not
     * depend on mixin ordering or Minecraft.hasControlDown quirks at this callsite.
     */
    @ModifyArg(
            method = "handleKeybinds",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;dropItem(Lnet/minecraft/client/player/LocalPlayer;Z)V"),
            index = 1)
    private boolean macosInputFixes$hotbarDropFullStackArg(boolean vanillaFullStack) {
        if (!Common.IS_SYSTEM_MAC) {
            return vanillaFullStack;
        }
        Minecraft mc = (Minecraft) (Object) this;
        boolean v = Common.macStrgParityFullStackModifier(mc);
        if (Common.debugDropModifier()) {
            MacOSInputFixesMod.LOGGER.info(
                    "[MacOSInputFixes][drop] ModifyArg MultiPlayerGameMode.dropItem all: vanilla={} -> {}",
                    vanillaFullStack,
                    v);
        }
        return v;
    }

    @Inject(
            method = "handleKeybinds",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;dropItem(Lnet/minecraft/client/player/LocalPlayer;Z)V"))
    private void macosInputFixes$logHotbarDropAttempt(CallbackInfo ci) {
        if (!Common.IS_SYSTEM_MAC || !Common.debugDropModifier()) {
            return;
        }
        Minecraft mc = (Minecraft) (Object) this;
        com.mojang.blaze3d.platform.Window w = mc.getWindow();
        MacOSInputFixesMod.LOGGER.info(
                "[MacOSInputFixes][drop] hotbar drop invoke: macStrgParity={} physicalStrgKeysDown={} | disableCtrlFix={} useCommandKey={}",
                Common.macStrgParityFullStackModifier(mc),
                Common.physicalStrgKeysDown(w),
                ModOptions.disableCtrlClickFix,
                ModOptions.useCommandKey);
    }

    @Unique
    private void macosInputFixes$registerNativeHooks(Minecraft client) {
        MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] IS_SYSTEM_MAC = {}", Common.IS_SYSTEM_MAC);

        if (!Common.IS_SYSTEM_MAC) {
            MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] Not macOS, skipping native callbacks");
            return;
        }

        // Options are loaded by now: push our MC-122296 choice into SDL (see MacosUtilMixin).
        Common.applyCtrlClickEmulation();

        // Since 26.3 the window is an SDL window; ask SDL for the backing NSWindow.
        long sdlWindow = ((MinecraftClientAccessor) client).getWindow().handle();
        long cocoaWindow = SDLProperties.SDL_GetPointerProperty(
                SDLVideo.SDL_GetWindowProperties(sdlWindow), SDLVideo.SDL_PROP_WINDOW_COCOA_WINDOW_POINTER, 0L);
        MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] SDL window handle = {}", sdlWindow);
        MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] Cocoa NSWindow handle = {}", cocoaWindow);
        if (cocoaWindow == 0L) {
            MacOSInputFixesMod.LOGGER.warn("[MinecraftClientMixin] No Cocoa NSWindow from SDL, skipping native callbacks");
            return;
        }

        ScrollCallback scrollCallback = (x, y, xWithMomentum, yWithMomentum, ungroupedX, ungroupedY) -> {
            if (Common.debugDropModifier()) {
                MacOSInputFixesMod.LOGGER.info(
                        "[NativeCallback] SCROLL from native: x={}, y={}, xMom={}, yMom={}, uX={}, uY={}",
                        x,
                        y,
                        xWithMomentum,
                        yWithMomentum,
                        ungroupedX,
                        ungroupedY);
            }
            Common.setAllowedInputOSX(true);
            try {
                ((MouseInvokerMixin) client.mouseHandler).invokeOnScroll(sdlWindow, x, y);
            } finally {
                Common.setAllowedInputOSX(false);
            }
        };

        KeyCallback keyCallback = (key, scancode, action, modifiers) -> {
            if (Common.debugDropModifier()) {
                MacOSInputFixesMod.LOGGER.info(
                        "[NativeCallback] KEY from native: key={}, scancode={}, action={}, mods={}",
                        key,
                        scancode,
                        action,
                        modifiers);
            }
            Common.setAllowedInputOSX2(true);
            try {
                ((KeyboardHandlerAccessor) client.keyboardHandler).invokeKeyPress(sdlWindow,
                        Common.keyActionFromNative(action), Common.keyEventFromNative(key, modifiers));
            } finally {
                Common.setAllowedInputOSX2(false);
            }
        };

        MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] Registering native callbacks with Cocoa window...");
        try {
            MacOSInputFixesMod.registerCallbacks(scrollCallback, keyCallback, cocoaWindow);
            Common.setNativeCallbacksRegistered(true);
            MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] SUCCESS! Native callbacks registered!");

            MacOSInputFixesMod.LOGGER.info("[MinecraftClientMixin] Applying settings to native code...");
            MacOSInputFixesMod.setTrackpadSensitivity(ModOptions.trackpadSensitivity);
            MacOSInputFixesMod.setMomentumScrolling(ModOptions.momentumScrolling);
            MacOSInputFixesMod.setInterfaceSmoothScroll(ModOptions.interfaceSmoothScroll);
            MacOSInputFixesMod.setBlockCommandQQuit(ModOptions.blockCommandQQuit);
        } catch (Throwable e) {
            Common.setNativeCallbacksRegistered(false);
            MacOSInputFixesMod.LOGGER
                    .warn("[MinecraftClientMixin] Native callbacks FAILED - mod will work with limited features");
            MacOSInputFixesMod.LOGGER.warn("[MinecraftClientMixin] Error: {}", e.toString(), e);
        }
    }
}
