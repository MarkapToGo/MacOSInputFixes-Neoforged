package com.hamarb123.macos_input_fixes;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.hamarb123.macos_input_fixes.client.MacOSInputFixesClientMod;
import com.hamarb123.macos_input_fixes.client.KeyCallback;
import com.hamarb123.macos_input_fixes.client.ScrollCallback;
import net.fabricmc.api.ClientModInitializer;

public class MacOSInputFixesMod implements ClientModInitializer {
    public static final String MODID = "macos_input_fixes";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitializeClient() {
        LOGGER.info("[MacOSInputFixes] ========================================");
        LOGGER.info("[MacOSInputFixes] Client initializer called!");
        LOGGER.info("[MacOSInputFixes] IS_SYSTEM_MAC = {}", Common.IS_SYSTEM_MAC);
        LOGGER.info("[MacOSInputFixes] ========================================");

        // Load the native library via the bridge class
        if (Common.IS_SYSTEM_MAC) {
            MacOSInputFixesClientMod.ensureLoaded();
        }

        ModOptions.loadOptions();
        LOGGER.info("[MacOSInputFixes] Options loaded successfully");
    }

    // Delegate to the bridge class for native methods
    public static void registerCallbacks(ScrollCallback scrollCallback, KeyCallback keyCallback, long window) {
        MacOSInputFixesClientMod.registerCallbacks(scrollCallback, keyCallback, window);
    }

    public static void setTrackpadSensitivity(double sensitivity) {
        MacOSInputFixesClientMod.setTrackpadSensitivity(sensitivity);
    }

    public static void setMomentumScrolling(boolean option) {
        MacOSInputFixesClientMod.setMomentumScrolling(option);
    }

    public static void setInterfaceSmoothScroll(boolean option) {
        MacOSInputFixesClientMod.setInterfaceSmoothScroll(option);
    }

    public static void setBlockCommandQQuit(boolean block) {
        if (!Common.IS_SYSTEM_MAC) {
            return;
        }
        try {
            MacOSInputFixesClientMod.setBlockCommandQQuit(block);
        } catch (UnsatisfiedLinkError e) {
            LOGGER.warn("[MacOSInputFixes] Native setBlockCommandQQuit unavailable; rebuild src/main/native (dylib out of date)", e);
        }
    }
}
