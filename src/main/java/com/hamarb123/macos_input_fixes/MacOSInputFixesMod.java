package com.hamarb123.macos_input_fixes;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.hamarb123.macos_input_fixes.client.MacOSInputFixesClientMod;
import com.hamarb123.macos_input_fixes.client.KeyCallback;
import com.hamarb123.macos_input_fixes.client.ScrollCallback;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(MacOSInputFixesMod.MODID)
public class MacOSInputFixesMod {
    public static final String MODID = "macos_input_fixes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MacOSInputFixesMod(IEventBus modEventBus) {
        LOGGER.info("[MacOSInputFixes] ========================================");
        LOGGER.info("[MacOSInputFixes] Mod constructor called!");
        LOGGER.info("[MacOSInputFixes] IS_SYSTEM_MAC = {}", Common.IS_SYSTEM_MAC);
        LOGGER.info("[MacOSInputFixes] ========================================");

        // Load the native library via the bridge class
        if (Common.IS_SYSTEM_MAC) {
            MacOSInputFixesClientMod.ensureLoaded();
        }

        modEventBus.addListener(this::clientSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("[MacOSInputFixes] clientSetup event fired");
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
}
